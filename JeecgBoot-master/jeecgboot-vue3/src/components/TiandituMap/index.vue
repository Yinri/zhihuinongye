<template>
  <div class="tianditu-map-container" :style="{ width: mapWidthComputed, height: mapHeightComputed }">
    <div ref="mapContainer" class="map-container"></div>
    
    <!-- 地块信息弹窗 -->
    <div v-if="selectedPlot" class="plot-info-popup" :style="{ left: popupPosition.x + 'px', top: popupPosition.y + 'px' }">
      <a-card size="small" :title="selectedPlot.plotName || selectedPlot.name || '地块信息'" class="plot-popup-card">
        <template #extra>
          <a-button type="text" size="small" @click="closePlotInfo">
            <Icon icon="ant-design:close-outlined" />
          </a-button>
        </template>
        <div class="plot-info-content">
          <p><strong>地块编号：</strong>{{ selectedPlot.id || selectedPlot.code || '-' }}</p>
          <p><strong>地块面积：</strong>{{ selectedPlot.area }} 亩</p>
          <p><strong>土壤类型：</strong>{{ selectedPlot.soilType || '-' }}</p>
          <p><strong>作物类型：</strong>{{ selectedPlot.cropType || '-' }}</p>
          <p><strong>生长阶段：</strong>{{ selectedPlot.growthStage || '-' }}</p>
          <p><strong>创建时间：</strong>{{ selectedPlot.createTime || '-' }}</p>
          <p v-if="selectedPlot.description"><strong>描述：</strong>{{ selectedPlot.description }}</p>
        </div>
        <div class="plot-actions" v-if="enableManagement">
          <a-button size="small" @click="editPlot(selectedPlot)">编辑</a-button>
          <a-button size="small" type="primary" @click="viewPlotDetail(selectedPlot)">详情</a-button>
        </div>
      </a-card>
    </div>
    
    <!-- 地图工具栏 -->
    <div class="map-toolbar" v-if="enableManagement">
      <a-space>
        <a-button 
          :type="drawingMode === 'polygon' ? 'primary' : 'default'" 
          size="small" 
          @click="toggleDrawingMode('polygon')"
          :disabled="!selectedBaseId"
        >
          <Icon icon="ant-design:aim-outlined" />
          绘制地块
        </a-button>
        <a-button size="small" @click="clearDrawing">
          <Icon icon="ant-design:clear-outlined" />
          清除绘制
        </a-button>
        <a-button size="small" @click="resetMapView">
          <Icon icon="ant-design:reload-outlined" />
          重置视图
        </a-button>
      </a-space>
    </div>
    
    <!-- 地块表单弹窗 -->
    <PlotInfoModal 
      v-if="enableManagement" 
      @register="registerModal" 
      @success="handlePlotFormSuccess" 
      @cancel="handlePlotFormCancel"
      :baseId="selectedBaseId"
      :plotData="currentPlotData"
    />
    
    <div v-if="loading" class="map-loading">
      <a-spin size="large" tip="地图加载中..." />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, onUnmounted, watch, nextTick, computed } from 'vue';
import { useModal } from '/@/components/Modal';
import { useMessage } from '/@/hooks/web/useMessage';
import { Icon } from '/@/components/Icon';
import PlotInfoModal from '../../views/rapeseed/plot-info/PlotInfoModal.vue';
import { getPlotInfoList, savePlotInfo } from '../../views/rapeseed/plot-info/plotInfo.api';
import { getSensorList } from '../sensor/sensor.api';
import { getBaseById } from '/@/views/rapeseed/production-plan/base-production-plan/base.api';
import { useSelectStore } from '/@/store/selectStore';
import { storeToRefs } from 'pinia';

// 组件属性
const props = defineProps({
  enableManagement: {
    type: Boolean,
    default: false
  },
  mapWidth: {
    type: [Number, String],
    default: '100%'
  },
  mapHeight: {
    type: [Number, String],
    default: '800px'
  },
  showSensors: {
    type: Boolean,
    default: true
  }
});

// 计算地图宽度和高度
const mapWidthComputed = computed(() => {
  if (typeof props.mapWidth === 'number') {
    return `${props.mapWidth}px`;
  }
  return props.mapWidth || '100%';
});

const mapHeightComputed = computed(() => {
  if (typeof props.mapHeight === 'number') {
    return `${props.mapHeight}px`;
  }
  return props.mapHeight || '800px';
});

// 地图容器大小变化监听
const resizeObserver = ref<ResizeObserver | null>(null);

// 使用Pinia store管理基地和地块选择状态
const selectStore = useSelectStore();
const { selectedBase } = storeToRefs(selectStore);
const selectedBaseId = computed(() => selectedBase.value.baseId);

const { createMessage } = useMessage();

// 地图相关变量
const mapContainer = ref<HTMLDivElement | null>(null);
let mapInstance: any = null;
let T: any = null; // 天地图全局对象

// 地块和传感器数据
const plotData = ref<any[]>([]);
const sensorData = ref<any[]>([]);
const plotMarkers = ref<any[]>([]);
const sensorMarkers = ref<any[]>([]);

// 绘制相关
const drawingMode = ref<string>(''); // 当前绘制模式：'polygon'
const drawingTool = ref<any>(null);
const currentPolygon = ref<any>(null);

// 弹窗相关
const selectedPlot = ref<any>(null);
const popupPosition = ref({ x: 0, y: 0 });

// 表单弹窗
const [registerModal, { openModal }] = useModal();
const currentPlotData = ref<any>(null);

// 加载天地图API
const loadTiandituAPI = () => {
  return new Promise((resolve, reject) => {
    if (window.T) {
      T = window.T;
      resolve(T);
      return;
    }
    
    const script = document.createElement('script');
    script.type = 'text/javascript';
    script.src = 'https://api.tianditu.gov.cn/api?v=4.0&tk=46fdb68b960da6af775e3287cae51e81';
    script.onload = () => {
      T = window.T;
      resolve(T);
    };
    script.onerror = () => {
      reject(new Error('Failed to load Tianditu API'));
    };
    document.head.appendChild(script);
  });
};

// 初始化地图
const initMap = async () => {
  try {
    await loadTiandituAPI();
    
    if (!mapContainer.value) return;
    
    // 创建地图实例
    mapInstance = new T.Map(mapContainer.value);
    
    // 设置地图类型为卫星图
    mapInstance.setMapType(window.TMAP_SATELLITE_MAP);
    
    // 添加缩放控件
    const control = new T.Control.Zoom();
    mapInstance.addControl(control);
    
    // 设置地图中心和缩放级别
    // 优先使用store中的基地经纬度信息，如果没有则使用默认坐标
    if (selectedBase.value.longitude && selectedBase.value.latitude) {
      console.log('使用基地坐标设置地图中心:', selectedBase.value.longitude, selectedBase.value.latitude);
      mapInstance.centerAndZoom(new T.LngLat(selectedBase.value.longitude, selectedBase.value.latitude), 16);
    } else {
      // 设置地图中心和缩放级别（默认显示中国中心）
      console.log('使用默认坐标设置地图中心');
      mapInstance.centerAndZoom(new T.LngLat(116.40969, 39.89945), 5);
    }
    
    // 不添加缩放控件，因为缩放级别已固定
    
    // 如果有选中的基地，调整地图视图
    if (selectedBaseId.value) {
      await loadBaseLocation();
    }
    
    // 加载地块和传感器数据
    await loadPlotData();
    if (props.showSensors) {
      await loadSensorData();
    }
    
    // 如果启用地块管理功能，初始化绘制工具
    if (props.enableManagement) {
      initDrawingTools();
    }
  } catch (error) {
    console.error('初始化地图失败:', error);
    createMessage.error('地图初始化失败，请刷新页面重试');
  }
};

// 加载基地位置信息
const loadBaseLocation = async () => {
  try {
    // 使用store中存储的基地经纬度信息
    if (selectedBase.value.longitude && selectedBase.value.latitude) {
      console.log('loadBaseLocation: 使用基地坐标设置地图中心:', selectedBase.value.longitude, selectedBase.value.latitude);
      mapInstance.centerAndZoom(new T.LngLat(selectedBase.value.longitude, selectedBase.value.latitude), 16);
    } else {
      // 如果store中没有经纬度信息，调用API获取
      console.log('loadBaseLocation: 基地坐标为空，调用API获取');
      try {
        const baseInfo = await getBaseById(selectedBaseId.value);
        if (baseInfo && baseInfo.longitude && baseInfo.latitude) {
          mapInstance.centerAndZoom(new T.LngLat(baseInfo.longitude, baseInfo.latitude), 16);
          // 更新store中的经纬度信息
          selectStore.updateSelectedBase({
            baseId: selectedBase.value.baseId,
            baseName: baseInfo.baseName || selectedBase.value.baseName,
            longitude: baseInfo.longitude,
            latitude: baseInfo.latitude
          });
        } else {
          // API也没有返回经纬度信息，使用默认坐标
          console.log('loadBaseLocation: API未返回经纬度信息，使用默认坐标');
          mapInstance.centerAndZoom(new T.LngLat(116.40969, 39.89945), 5);
        }
      } catch (error) {
        console.error('loadBaseLocation: 获取基地信息失败:', error);
        // API调用失败，使用默认坐标
        mapInstance.centerAndZoom(new T.LngLat(116.40969, 39.89945), 5);
      }
    }
  } catch (error) {
    console.error('加载基地位置失败:', error);
  }
};

// 加载地块数据
const loadPlotData = async () => {
  try {
    if (!selectedBaseId.value) return;
    
    const params = { baseId: selectedBaseId.value };
    const result = await getPlotInfoList(params);
    
    if (result && result.records) {
      console.log('加载地块数据成功:', result.records);
      plotData.value = result.records;
      displayPlots();
    }
  } catch (error) {
    console.error('加载地块数据失败:', error);
    createMessage.error('加载地块数据失败');
  }
};

// 加载传感器数据
const loadSensorData = async () => {
  try {
    if (!selectedBaseId.value) return;
    
    // 这里应该调用API获取传感器数据
    // const result = await getSensorList({ baseId: selectedBaseId.value });
    // if (result && result.records) {
    //   sensorData.value = result.records;
    //   displaySensors();
    // }
    
    // 模拟传感器数据
    sensorData.value = [
      { id: '1', name: '传感器1', longitude: 116.40969, latitude: 39.89945, type: '温度' },
      { id: '2', name: '传感器2', longitude: 116.41969, latitude: 39.90945, type: '湿度' },
      { id: '3', name: '传感器3', longitude: 116.42969, latitude: 39.91945, type: '光照' }
    ];
    displaySensors();
  } catch (error) {
    console.error('加载传感器数据失败:', error);
    createMessage.error('加载传感器数据失败');
  }
};

// 在地图上显示地块
const displayPlots = () => {
  if (!mapInstance || !plotData.value.length) return;
  
  // 清除现有地块标记
  clearPlotMarkers();
  
  plotData.value.forEach(plot => {
    // 只检查是否有多边形坐标，不依赖经纬度
    if (plot.polygonCoords) {
      try {
        // 解析多边形坐标（可能是JSON字符串或已经是数组）
        let coords;
        if (typeof plot.polygonCoords === 'string') {
          coords = JSON.parse(plot.polygonCoords);
        } else {
          coords = plot.polygonCoords;
        }
        
        // 确保coords是数组且不为空
        if (Array.isArray(coords) && coords.length > 0) {
          // 直接使用多边形坐标绘制多边形
          const points = coords.map(coord => new T.LngLat(coord.lng, coord.lat));
          const polygon = new T.Polygon(points, {
            color: 'blue',
            weight: 2,
            opacity: 0.8,
            fillColor: '#FFFFFF',
            fillOpacity: 0.3
          });
          
          polygon.addEventListener('click', (event) => {
            showPlotInfo(plot, event);
          });
          
          mapInstance.addOverLay(polygon);
          plotMarkers.value.push(polygon);
        }
      } catch (error) {
        console.error('解析多边形坐标失败:', error, plot.polygonCoords);
      }
    }
  });
  
  // 如果有地块数据，调整地图视野以包含所有地块
  if (plotMarkers.value.length > 0) {
    const bounds = calculateBounds(plotMarkers.value);
    if (bounds) {
      try {
        // 使用setViewport方法调整地图视野以包含所有地块
        mapInstance.setViewport(bounds);
      } catch (error) {
        console.error('调整地图视野失败:', error);
        // 如果setViewport失败，尝试使用centerAndZoom
        if (bounds.getCenter) {
          const center = bounds.getCenter();
          mapInstance.centerAndZoom(center, 14);
        }
      }
    }
  }
};

// 在地图上显示传感器
const displaySensors = () => {
  if (!mapInstance || !sensorData.value.length) return;
  
  // 清除现有传感器标记
  clearSensorMarkers();
  
  sensorData.value.forEach(sensor => {
    if (sensor.longitude && sensor.latitude) {
      // 创建传感器图标
      const icon = new T.Icon({
        iconUrl: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjQiIGhlaWdodD0iMjQiIHZpZXdCb3g9IjAgMCAyNCAyNCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHBhdGggZD0iTTEyIDJDNi40OCAyIDIgNi40OCAyIDEyUzYuNDggMjIgMTIgMjJTMjIgMTcuNTIgMjIgMTJTMTcuNTIgMiAxMiAyWk0xMiA2QzE0LjIxIDYgMTYgNy43OSAxNiAxMFMxNC4yMSAxNCAxMiAxNFM4IDEyLjIxIDggMTBTOS43OSA2IDEyIDZaIiBmaWxsPSIjRkY1NjU2Ii8+CjxwYXRoIGQ9Ik0xMiA4QzEwLjkgOCAxMCA4LjkgMTAgMTBTMTAuOSAxNCAxMiAxNFMxNCAxMS4xIDE0IDEwUzEzLjEgOCAxMiA4WiIgZmlsbD0iI0ZGNTY1NiIvPgo8L3N2Zz4K',
        iconSize: new T.Point(24, 24),
        iconAnchor: new T.Point(12, 12)
      });
      
      // 创建传感器标记
      const lngLat = new T.LngLat(sensor.longitude, sensor.latitude);
      const marker = new T.Marker(lngLat, { icon: icon });
      
      // 添加点击事件
      marker.addEventListener('click', () => {
        showSensorInfo(sensor, event);
      });
      
      // 添加到地图
      mapInstance.addOverLay(marker);
      sensorMarkers.value.push(marker);
    }
  });
};

// 显示地块信息
const showPlotInfo = async (plot, event) => {
  try {
    // 确保使用从loadPlotData获取的数据
    // 首先尝试从plotData中找到完整的地块信息
    let fullPlotData = plotData.value.find(p => p.id === plot.id);
    
    // 如果没找到，直接使用传入的plot
    if (!fullPlotData) {
      fullPlotData = plot;
    }
    
    // 如果有基地ID但没有基地名称，尝试获取基地信息
    if (fullPlotData.baseId && !fullPlotData.baseName) {
      try {
        const baseInfo = await getBaseById(fullPlotData.baseId);
        if (baseInfo && baseInfo.success) {
          fullPlotData.baseName = baseInfo.result.baseName;
          // 可以添加更多基地信息字段
        }
      } catch (error) {
        console.error('获取基地信息失败:', error);
      }
    }
    
    // 设置选中的地块信息
    selectedPlot.value = fullPlotData;
    
    // 计算弹窗位置 - 使用地图坐标转换为屏幕坐标
    if (event && event.lngLat) {
      // 获取点击位置的经纬度
      const lngLat = event.lngLat;
      // 将经纬度转换为屏幕坐标
      const point = mapInstance.lngLatToContainerPoint(lngLat);
      
      // 设置弹窗位置，考虑弹窗大小，避免超出屏幕
      const popupWidth = 320; // 弹窗宽度（更新为最新宽度）
      const popupHeight = 400; // 预估弹窗高度（更新为最新高度）
      
      // 获取地图容器的位置和大小
      const mapRect = mapContainer.value.getBoundingClientRect();
      
      // 计算弹窗相对于地图容器的位置
      let x = point.x + 10; // 右侧偏移10px
      let y = point.y - popupHeight / 2; // 垂直居中
      
      // 检查是否超出右边界
      if (x + popupWidth > mapRect.width) {
        x = point.x - popupWidth - 10; // 显示在点击点左侧
      }
      
      // 检查是否超出左边界
      if (x < 0) {
        x = 10; // 最小左边距
      }
      
      // 检查是否超出下边界
      if (y + popupHeight > mapRect.height) {
        y = mapRect.height - popupHeight - 10;
      }
      
      // 检查是否超出上边界
      if (y < 10) {
        y = 10;
      }
      
      popupPosition.value = { x, y };
    } else if (event && event.domEvent) {
      // 备用方案：使用domEvent
      const mapRect = mapContainer.value.getBoundingClientRect();
      popupPosition.value = {
        x: event.domEvent.clientX - mapRect.left + 10,
        y: event.domEvent.clientY - mapRect.top + 10
      };
    } else {
      // 默认位置
      popupPosition.value = { x: 50, y: 50 };
    }
  } catch (error) {
    console.error('显示地块信息失败:', error);
    // 使用默认位置和传入的plot数据
    selectedPlot.value = plot;
    popupPosition.value = { x: 50, y: 50 };
  }
};

// 显示传感器信息
const showSensorInfo = (sensor, event) => {
  createMessage.info(`传感器: ${sensor.name}, 类型: ${sensor.type}`);
};

// 关闭地块信息弹窗
const closePlotInfo = () => {
  selectedPlot.value = null;
};

// 清除地块标记
const clearPlotMarkers = () => {
  if (!mapInstance) return;
  
  plotMarkers.value.forEach(marker => {
    mapInstance.removeOverLay(marker);
  });
  plotMarkers.value = [];
};

// 清除传感器标记
const clearSensorMarkers = () => {
  if (!mapInstance) return;
  
  sensorMarkers.value.forEach(marker => {
    mapInstance.removeOverLay(marker);
  });
  sensorMarkers.value = [];
};

// 初始化绘制工具
const initDrawingTools = () => {
  if (!mapInstance || !T) {
    console.error('地图实例或天地图API未初始化');
    return;
  }
  
  try {
    // 创建多边形绘制工具
    drawingTool.value = new T.PolygonTool(mapInstance, {
      showLabel: false, // 不显示面积信息
      color: "blue",
      weight: 2,
      opacity: 0.8,
      fillColor: '#FFFFFF',
      fillOpacity: 0.3
    });
    
    // 尝试监听多种可能的事件
    drawingTool.value.addEventListener('mouseup', handleDrawingComplete);
    drawingTool.value.addEventListener('draw', handleDrawingComplete);
    drawingTool.value.addEventListener('polygon', handleDrawingComplete);
    
    console.log('多边形绘制工具初始化成功');
  } catch (error) {
    console.error('初始化绘制工具失败:', error);
    createMessage.error('初始化绘制工具失败');
  }
};

// 切换绘制模式
const toggleDrawingMode = (mode) => {
  console.log('toggleDrawingMode called with mode:', mode);
  console.log('drawingTool.value:', drawingTool.value);
  console.log('drawingMode.value:', drawingMode.value);
  console.log('selectedBaseId.value:', selectedBaseId.value);
  
  if (!drawingTool.value) {
    console.error('绘制工具未初始化');
    createMessage.error('绘制工具未初始化，请刷新页面重试');
    return;
  }
  
  if (drawingMode.value === mode) {
    // 如果已经是当前模式，则关闭绘制
    console.log('关闭绘制模式');
    drawingTool.value.close();
    drawingMode.value = '';
  } else {
    // 切换到新模式
    console.log('切换到绘制模式:', mode);
    drawingMode.value = mode;
    
    if (mode === 'polygon') {
      try {
        // 对于PolygonTool，直接调用open()开始绘制
        drawingTool.value.open();
        console.log('多边形绘制模式已开启');
        createMessage.success('请在地图上点击绘制多边形地块');
      } catch (error) {
        console.error('开启多边形绘制模式失败:', error);
        createMessage.error('开启绘制模式失败，请重试');
      }
    }
  }
};

// 处理绘制完成事件
const handleDrawingComplete = (event) => {
  console.log('handleDrawingComplete event:', event);
  
  // 尝试从event中获取多边形对象
  if (event && event.currentPolygon) {
    currentPolygon.value = event.currentPolygon;
    console.log('保存当前多边形对象:', currentPolygon.value);
  }
  
  // 尝试从event.currentLnglats获取坐标点
  if (event && event.currentLnglats) {
    try {
      // 确保currentLnglats是数组
      let points = Array.isArray(event.currentLnglats) ? event.currentLnglats : [event.currentLnglats];
      
      // 转换坐标点格式
      const polygonCoords = points.map((point) => {
        // 处理不同的坐标点格式
        let lng, lat;
        
        if (typeof point === 'object' && point !== null) {
          if (point.lng !== undefined && point.lat !== undefined) {
            lng = point.lng;
            lat = point.lat;
          } else if (typeof point.getLng === 'function' && typeof point.getLat === 'function') {
            lng = point.getLng();
            lat = point.getLat();
          } else {
            console.warn('未知的坐标点格式:', point);
            return null;
          }
        } else {
          console.warn('坐标点不是对象:', point);
          return null;
        }
        
        return { lng, lat };
      }).filter(point => point !== null); // 过滤掉无效的点
      
      if (polygonCoords.length === 0) {
        console.error('没有有效的坐标点');
        createMessage.error('没有有效的坐标点，请重试');
        return;
      }
      
      // 处理多边形坐标点
      // 直接去除最后一个点，只保留前面的点
      let finalCoords = [...polygonCoords];
      if (finalCoords.length > 3) {
        finalCoords = finalCoords.slice(0, -1);
      }
      
      // 获取绘制地块的面积并转换为亩
      let areaInMu = 0;
      if (event.currentArea) {
        // 从平方米转换为亩（1亩 ≈ 666.6667平方米）
        areaInMu = parseFloat((event.currentArea / 666.6667).toFixed(2));
        console.log(`绘制地块面积: ${event.currentArea}平方米 ≈ ${areaInMu}亩`);
      }
      
      // 如果没有从事件中获取到多边形对象，尝试从地图上获取最新的多边形
      if (!currentPolygon.value && mapInstance) {
        try {
          // 获取地图上所有的覆盖物
          const overlays = mapInstance.getOverlays();
          console.log('地图上的覆盖物数量:', overlays.length);
          
          // 查找最新的多边形覆盖物
          for (let i = overlays.length - 1; i >= 0; i--) {
            const overlay = overlays[i];
            // 检查是否是多边形对象
            if (overlay && overlay.getLngLats && typeof overlay.getLngLats === 'function') {
              // 检查这个多边形是否在plotMarkers中（已存在的地块标记）
              const isExistingPlot = plotMarkers.value.includes(overlay);
              
              if (!isExistingPlot) {
                currentPolygon.value = overlay;
                console.log('从地图上找到最新的多边形对象:', currentPolygon.value);
                break;
              }
            }
          }
        } catch (error) {
          console.error('获取地图上的多边形失败:', error);
        }
      }
      
      // 打开地块表单，传入多边形坐标和面积
      currentPlotData.value = {
        polygonCoords: finalCoords,
        baseId: selectedBaseId.value,
        area: areaInMu // 添加转换后的面积（亩）
      };
      
      openModal(true, {
        isUpdate: false,
        showFooter: true, // 添加此参数，确保表单可编辑
        polygonCoords: finalCoords,
        baseId: selectedBaseId.value,
        area: areaInMu // 添加转换后的面积（亩）
      });
      
      // 关闭绘制工具
      if (drawingTool.value) {
        drawingTool.value.close();
      }
      drawingMode.value = '';
      
      return; // 成功处理，直接返回
    } catch (error) {
      console.error('处理currentLnglats失败:', error);
    }
  }
  
  // 如果currentLnglats不可用，显示错误信息
  console.error('无法获取多边形坐标点，currentLnglats不可用');
  createMessage.error('无法获取多边形坐标点，请重试');
};

// 清除绘制
const clearDrawing = () => {
  console.log('clearDrawing called');
  console.log('currentPolygon.value:', currentPolygon.value);
  console.log('drawingTool.value:', drawingTool.value);
  console.log('mapInstance:', mapInstance);
  
  if (drawingTool.value) {
    drawingTool.value.close();
  }
  
  // 只清除当前正在绘制的多边形，不影响已存在的地块标记
  if (currentPolygon.value && mapInstance) {
    console.log('尝试移除当前绘制的多边形');
    try {
      mapInstance.removeOverLay(currentPolygon.value);
      currentPolygon.value = null;
      console.log('成功移除当前绘制的多边形');
    } catch (error) {
      console.error('移除多边形失败:', error);
    }
  } else {
    console.log('没有找到当前绘制的多边形');
    
    // 如果没有保存的多边形对象，尝试清除最新的非地块多边形（可能是刚绘制的）
    if (mapInstance) {
      try {
        const overlays = mapInstance.getOverlays();
        console.log('地图上的覆盖物数量:', overlays.length);
        
        // 从后往前遍历，找到最新的非地块多边形
        for (let i = overlays.length - 1; i >= 0; i--) {
          const overlay = overlays[i];
          // 检查是否是多边形对象
          if (overlay && overlay.getLngLats && typeof overlay.getLngLats === 'function') {
            // 检查这个多边形是否在plotMarkers中（已存在的地块标记）
            const isExistingPlot = plotMarkers.value.includes(overlay);
            
            if (!isExistingPlot) {
              console.log('找到当前绘制的非地块多边形对象，正在移除');
              mapInstance.removeOverLay(overlay);
              break; // 只移除一个，避免影响其他多边形
            }
          }
        }
      } catch (error) {
        console.error('清除当前绘制的多边形失败:', error);
      }
    }
  }
  
  drawingMode.value = '';
};

// 重置地图视图
const resetMapView = () => {
  if (!mapInstance) return;
  
  if (selectedBaseId.value && selectedBase.value.longitude && selectedBase.value.latitude) {
    console.log('resetMapView: 使用基地坐标重置地图视图:', selectedBase.value.longitude, selectedBase.value.latitude);
    mapInstance.centerAndZoom(new T.LngLat(selectedBase.value.longitude, selectedBase.value.latitude), 14);
  } else {
    // 重置到默认视图
    console.log('resetMapView: 使用默认坐标重置地图视图');
    mapInstance.centerAndZoom(new T.LngLat(116.40969, 39.89945), 5);
  }
  
  // 重新加载地块和传感器数据
  loadPlotData();
  if (props.showSensors) {
    loadSensorData();
  }
};

// 编辑地块
const editPlot = (plot) => {
  currentPlotData.value = plot;
  openModal(true, {
    isUpdate: true,
    polygonCoords: plot.polygonCoords,
    baseId: plot.baseId
  });
};

// 查看地块详情
const viewPlotDetail = (plot) => {
  currentPlotData.value = plot;
  openModal(true, {
    isUpdate: true,
    showFooter: false,
    polygonCoords: plot.polygonCoords,
    baseId: plot.baseId
  });
};

// 处理地块表单提交成功
const handlePlotFormSuccess = () => {
  // 重新加载地块数据
  loadPlotData();
  
  // 清除当前多边形
  if (currentPolygon.value && mapInstance) {
    mapInstance.removeOverLay(currentPolygon.value);
    currentPolygon.value = null;
  }
};

// 处理地块表单取消
const handlePlotFormCancel = () => {
  console.log('地块表单取消，清除当前多边形');
  
  try {
    // 清除当前正在绘制的多边形
    if (currentPolygon.value && mapInstance) {
      console.log('移除当前多边形:', currentPolygon.value);
      mapInstance.removeOverLay(currentPolygon.value);
      currentPolygon.value = null;
    } else {
      console.log('currentPolygon.value为空或mapInstance不可用');
      
      // 如果currentPolygon.value为空，但地图上有覆盖物，尝试查找并移除最新的非地块多边形
      if (mapInstance) {
        const overlays = mapInstance.getOverlays();
        console.log('地图上的覆盖物数量:', overlays.length);
        
        // 从后往前遍历，找到最新的非地块多边形
        for (let i = overlays.length - 1; i >= 0; i--) {
          const overlay = overlays[i];
          
          // 检查是否是多边形对象
          if (overlay && overlay.getLngLats && typeof overlay.getLngLats === 'function') {
            // 检查这个多边形是否在plotMarkers中（已存在的地块标记）
            const isExistingPlot = plotMarkers.value.includes(overlay);
            
            if (!isExistingPlot) {
              console.log('找到并移除非地块多边形:', overlay);
              mapInstance.removeOverLay(overlay);
              break;
            }
          }
        }
      }
    }
    
    // 关闭绘制工具
    if (drawingTool.value) {
      drawingTool.value.close();
      drawingTool.value = null;
    }
    
    // 重置绘制模式
    drawingMode.value = '';
  } catch (error) {
    console.error('处理地块表单取消事件时发生错误:', error);
  }
};

// 计算包含所有标记的边界
const calculateBounds = (markers) => {
  if (!markers || markers.length === 0) return null;
  
  let minLng = 180;
  let maxLng = -180;
  let minLat = 90;
  let maxLat = -90;
  
  markers.forEach(marker => {
    let lng, lat;
    
    if (marker.getLngLat) {
      // 标记
      const point = marker.getLngLat();
      lng = point.lng;
      lat = point.lat;
    } else if (marker.getLngLats) {
      // 多边形
      const points = marker.getLngLats();
      points.forEach(point => {
        minLng = Math.min(minLng, point.lng);
        maxLng = Math.max(maxLng, point.lng);
        minLat = Math.min(minLat, point.lat);
        maxLat = Math.max(maxLat, point.lat);
      });
      return;
    }
    
    minLng = Math.min(minLng, lng);
    maxLng = Math.max(maxLng, lng);
    minLat = Math.min(minLat, lat);
    maxLat = Math.max(maxLat, lat);
  });
  
  return new T.LngLatBounds(
    new T.LngLat(minLng, minLat),
    new T.LngLat(maxLng, maxLat)
  );
};

// 监听基地ID变化
watch(selectedBaseId, (newBaseId) => {
  if (newBaseId && mapInstance) {
    loadBaseLocation();
    loadPlotData();
    if (props.showSensors) {
      loadSensorData();
    }
  }
});

// 监听基地经纬度变化
watch(
  () => [selectedBase.value.longitude, selectedBase.value.latitude],
  ([longitude, latitude]) => {
    if (longitude && latitude && mapInstance) {
      console.log('基地经纬度变化，更新地图中心:', longitude, latitude);
      mapInstance.centerAndZoom(new T.LngLat(longitude, latitude), 14);
    }
  }
);

// 组件挂载时初始化地图
onMounted(() => {
  nextTick(() => {
    initMap();
    
    // 添加容器大小变化监听
    if (mapContainer.value) {
      resizeObserver.value = new ResizeObserver(() => {
        if (mapInstance) {
          // 触发地图重新计算大小
          mapInstance.checkResize();
        }
      });
      resizeObserver.value.observe(mapContainer.value);
    }
  });
});

// 组件卸载时清理资源
onUnmounted(() => {
  // 清理ResizeObserver
  if (resizeObserver.value) {
    resizeObserver.value.disconnect();
  }
  
  if (drawingTool.value) {
    drawingTool.value.close();
    // 移除所有可能的事件监听器
    drawingTool.value.removeEventListener('mouseup', handleDrawingComplete);
    drawingTool.value.removeEventListener('draw', handleDrawingComplete);
    drawingTool.value.removeEventListener('polygon', handleDrawingComplete);
  }
  
  if (mapInstance) {
    mapInstance.clearOverLays();
  }
});
</script>

<style lang="less" scoped>
.tianditu-map-container {
  position: relative;
  width: 100%;
  height: 100%;
  
  .map-container {
    width: 100%;
    height: 100%;
    border: 1px solid #d9d9d9;
    border-radius: 4px;
  }
  
  .plot-info-popup {
    position: absolute; /* 改为绝对定位，相对于地图容器 */
    z-index: 1000;
    
    .plot-popup-card {
      width: 320px; /* 增加宽度以容纳更多信息 */
      max-height: 400px; /* 设置最大高度 */
      overflow: auto; /* 添加滚动条 */
      
      .plot-info-content {
        p {
          margin-bottom: 8px;
          font-size: 13px;
          line-height: 1.4;
        }
      }
      
      .plot-actions {
        margin-top: 12px;
        text-align: right;
      }
    }
  }
  
  .map-toolbar {
    position: absolute;
    top: 10px;
    right: 10px;
    z-index: 999;
    background: rgba(255, 255, 255, 0.8);
    padding: 8px;
    border-radius: 4px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  }
}
</style>