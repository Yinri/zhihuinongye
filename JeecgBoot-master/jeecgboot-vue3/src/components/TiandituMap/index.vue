<template>
  <div class="tianditu-map-container" :style="{ width: mapWidthComputed, height: mapHeightComputed }">
    <div ref="mapContainer" class="map-container"></div>
    
    <!-- 地块信息弹窗 -->
    <transition name="popup-fade">
      <div v-if="selectedPlot" class="plot-info-popup" :style="{ left: popupPosition.x + 'px', top: popupPosition.y + 'px' }">
        <a-card size="small" :title="popupTitle" class="plot-popup-card" style="width: 320px; max-height: 90vh; overflow-y: auto; overflow-x: hidden;">
          <template #extra>
            <a-button type="text" size="small" @click="closePlotInfo">
              <Icon icon="ant-design:close-outlined" />
            </a-button>
          </template>
          
          <!-- 使用插槽内容，如果提供了自定义插槽则使用自定义内容，否则使用默认内容 -->
          <slot name="popup-content" :plot="selectedPlot">
            <div class="plot-info-content">
              <p><strong>地块面积：</strong>{{ selectedPlot.area }} 亩</p>
              <p><strong>土壤类型：</strong>{{ selectedPlot.soilType || '-' }}</p>
              <p><strong>地块状态：</strong>{{ selectedPlot.status || '-' }}</p>
            </div>
          </slot>
          
          <div class="plot-actions" v-if="enableManagement">
            <a-button size="small" @click="editPlot(selectedPlot)">编辑</a-button>
          </div>
        </a-card>
      </div>
    </transition>
    
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
      v-if="enableManagement || useModalForPlotClick" 
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
import { ref, onMounted, onUnmounted, watch, nextTick, computed, watchEffect } from 'vue';
import { useModal } from '/@/components/Modal';
import { useMessage } from '/@/hooks/web/useMessage';
import { useRoute, useRouter } from 'vue-router';
import { Icon } from '/@/components/Icon';
import PlotInfoModal from '../../views/rapeseed/plot-info/PlotInfoModal.vue';
import { getPlotInfoList, savePlotInfo } from '../../views/rapeseed/plot-info/plotInfo.api';
import { getBaseById } from '/@/views/rapeseed/production-plan/center/base.api';
import { getAllDevices } from '../../views/rapeseed/work-area/workArea.api';
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
  },
  plotRiskData: {
    type: Array,
    default: () => []
  },
  useModalForPlotClick: {
    type: Boolean,
    default: false
  },
  enablePlotHover: {
    type: Boolean,
    default: false
  },
  // 新增prop，用于控制是否在页面下方显示倒伏风险弹窗结构
  showLodgingRiskBelow: {
    type: Boolean,
    default: false
  },
  // 新增prop，用于自定义弹出框标题
  popupTitleFormatter: {
    type: Function,
    default: null
  }
});

// 定义emit事件
const emit = defineEmits(['plot-click']);

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

// 计算弹出框标题
const popupTitle = computed(() => {
  // 如果提供了自定义标题格式化函数，则使用它
  if (props.popupTitleFormatter && typeof props.popupTitleFormatter === 'function') {
    return props.popupTitleFormatter(selectedPlot.value);
  }
  // 否则使用默认格式
  return `${selectedPlot.value?.plotName || selectedPlot.value?.name || '地块信息'} (${selectedPlot.value?.plotCode || selectedPlot.value?.id || '-'})`;
});

// 地图容器大小变化监听
const resizeObserver = ref<ResizeObserver | null>(null);

// 使用Pinia store管理基地和地块选择状态
const selectStore = useSelectStore();
const { selectedBase } = storeToRefs(selectStore);
const selectedBaseId = computed(() => selectedBase.value.baseId);
const hasBaseCenter = computed(() => !!(selectedBase.value.longitude && selectedBase.value.latitude));

const { createMessage } = useMessage();

// 路由相关
const route = useRoute();
const router = useRouter();

// 地图相关变量
const mapContainer = ref<HTMLDivElement | null>(null);
let mapInstance: any = null;
let T: any = null; // 天地图全局对象

// 存储watch监听器的引用，用于清理
const watchStoppers: (() => void)[] = [];

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
    
    // 设置地图中心和缩放级别
    // 优先使用store中的基地经纬度信息，如果没有则使用默认坐标
    if (selectedBase.value.longitude && selectedBase.value.latitude) {
      console.log('使用基地坐标设置地图中心:', selectedBase.value.longitude, selectedBase.value.latitude);
      mapInstance.centerAndZoom(new T.LngLat(selectedBase.value.longitude, selectedBase.value.latitude), 15);
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
    
    // 添加点击地图空白区域关闭弹出框的事件
    mapInstance.addEventListener('click', (event) => {
      // 检查点击的目标是否是地块多边形
      if (!event.target || !event.target.plotData) {
        // 如果不是地块，关闭弹出框
        closePlotInfo();
      }
    });
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
      mapInstance.centerAndZoom(new T.LngLat(selectedBase.value.longitude, selectedBase.value.latitude), 15);
    } else {
      // 如果store中没有经纬度信息，调用API获取
      console.log('loadBaseLocation: 基地坐标为空，调用API获取');
      try {
        const baseInfo = await getBaseById(selectedBaseId.value);
        if (baseInfo && baseInfo.longitude && baseInfo.latitude) {
          mapInstance.centerAndZoom(new T.LngLat(baseInfo.longitude, baseInfo.latitude), 18);
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
    
    // 调用API获取传感器数据
    const result = await getAllDevices(selectedBaseId.value);
    console.log('获取传感器数据:', result);
    if (result ) {
      sensorData.value = result.map(device => ({
        id: device.id,
        name: device.deviceName,
        status: device.state,
        longitude: parseFloat(device.lng),
        latitude: parseFloat(device.lat),
        type: device.deviceType || '未知设备'
      }));
      displaySensors();
    } else {
      console.warn('获取传感器数据失败:', result);
      createMessage.error('获取传感器数据失败');
    }
  } catch (error) {
    console.error('加载传感器数据失败:', error);
    createMessage.error('加载传感器数据失败');
  }
};

// 在地图上显示地块
const displayPlots = () => {
  if (!mapInstance || !plotData.value.length) return;
  
  console.log('displayPlots called, enablePlotHover:', props.enablePlotHover);
  
  // 清除现有地块标记
  clearPlotMarkers();
  
  // 创建防抖函数处理hover事件
  const createDebouncedHandler = (handler, delay = 50) => {
    let timeoutId;
    return (...args) => {
      clearTimeout(timeoutId);
      timeoutId = setTimeout(() => handler.apply(this, args), delay);
    };
  };
  
  // 事件委托处理函数
  const plotClickHandler = (event) => {
    const plot = event.target.plotData;
    if (plot) {
      showPlotInfo(plot, event);
    }
  };
  
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
          
          // 根据风险等级获取颜色
          let color = 'blue'; // 默认颜色
          let fillColor = '#FFFFFF'; // 默认填充色
          let fillOpacity = 0.3; // 默认透明度
          
          // 如果有风险数据，根据风险等级设置颜色
          if (props.plotRiskData && props.plotRiskData.length > 0) {
            const riskData = props.plotRiskData.find(risk => risk.plotId === plot.id);
            if (riskData && riskData.current_risk) {
              const riskLevel = riskData.current_risk.riskLevel;
              const riskScore = riskData.current_risk.riskScore;
              
              // 根据风险等级设置颜色
              if (riskLevel === '低风险' || riskScore < 30) {
                color = '#52c41a'; // 绿色
                fillColor = '#52c41a';
                fillOpacity = 0.3;
              } else if (riskLevel === '中低风险' || (riskScore >= 30 && riskScore < 50)) {
                color = '#afcb2b'; // 黄绿色
                fillColor = '#afcb2b';
                fillOpacity = 0.3;
              } else if (riskLevel === '中等风险' || (riskScore >= 50 && riskScore < 65)) {
                color = '#faad14'; // 橙色
                fillColor = '#faad14';
                fillOpacity = 0.3;
              } else if (riskLevel === '高风险' || (riskScore >= 65 && riskScore < 80)) {
                color = '#ff4d4f'; // 红色
                fillColor = '#ff4d4f';
                fillOpacity = 0.3;
              } else if (riskLevel === '极高风险' || riskScore >= 80) {
                color = '#d32f2f'; // 深红色
                fillColor = '#d32f2f';
                fillOpacity = 0.3;
              }
            }
          }
          
          const polygon = new T.Polygon(points, {
            color: color,
            weight: 2,
            opacity: 0.8,
            fillColor: fillColor,
            fillOpacity: fillOpacity
          });
          
          // 存储地块数据到多边形对象，用于事件委托
          polygon.plotData = plot;
          
          // 添加点击事件
          polygon.addEventListener('click', plotClickHandler);
          (polygon as any).__handlers = (polygon as any).__handlers || {};
          (polygon as any).__handlers.click = plotClickHandler;
          
          // 只有在启用hover效果时才添加hover事件
          if (props.enablePlotHover) {
            console.log('为地块添加hover效果:', plot.plotName || plot.id);
            
            // 存储原始样式
            const originalStyle = {
              color: color,
              weight: 2,
              opacity: 0.8,
              fillColor: fillColor,
              fillOpacity: fillOpacity
            };
            
            // 创建信息提示框
            const infoWindow = new T.InfoWindow();
            let infoWindowOpened = false;
            
            // 获取地块风险信息
            const getPlotRiskInfo = () => {
              if (props.plotRiskData && props.plotRiskData.length > 0) {
                const riskData = props.plotRiskData.find(risk => risk.plotId === plot.id);
                if (riskData && riskData.current_risk) {
                  return {
                    riskLevel: riskData.current_risk.riskLevel,
                    riskScore: riskData.current_risk.riskScore,
                    lodgingProbability: riskData.current_risk.lodgingProbability || '暂无数据'
                  };
                }
              }
              return {
                riskLevel: '暂无数据',
                riskScore: '暂无数据',
                lodgingProbability: '暂无数据'
              };
            };
            
            // 鼠标悬停事件处理 - 使用防抖优化
            const hoverHandler = createDebouncedHandler((e) => {
              console.log('鼠标悬停在地块上:', plot.plotName || plot.id);
              
              // 悬停时增加透明度和边框宽度
              polygon.setStyle({
                color: color,
                weight: 3,
                opacity: 1,
                fillColor: fillColor,
                fillOpacity: 0.5
              });
              
              // 改变鼠标样式为手型
              mapInstance.getContainer().style.cursor = 'pointer';
              
              // 获取风险信息
              const riskInfo = getPlotRiskInfo();
              
              // 创建信息窗口内容
              const content = `
                <div style="padding: 10px; min-width: 200px;">
                  <h4 style="margin: 0 0 8px 0; color: #333;">${plot.plotName || '未命名地块'}</h4>
                  <p style="margin: 4px 0; font-size: 14px;">风险等级: <span style="font-weight: bold; color: ${color};">${riskInfo.riskLevel}</span></p>
                  <p style="margin: 4px 0; font-size: 14px;">风险评分: <span style="font-weight: bold;">${riskInfo.riskScore}</span></p>
                  <p style="margin: 4px 0; font-size: 14px;">倒伏概率: <span style="font-weight: bold;">${riskInfo.lodgingProbability}</span></p>
                </div>
              `;
              
              // 设置信息窗口内容
              infoWindow.setContent(content);
              
              // 获取多边形中心点作为信息窗口位置
              const bounds = polygon.getBounds();
              const center = bounds.getCenter();
              
              // 使用地图的openInfoWindow方法打开信息窗口
              mapInstance.openInfoWindow(infoWindow, center);
              infoWindowOpened = true;
            });
            
            // 鼠标离开事件处理 - 使用防抖优化
            const leaveHandler = createDebouncedHandler(() => {
              console.log('鼠标离开地块:', plot.name || plot.id);
              
              // 恢复原始样式
              polygon.setStyle(originalStyle);
              
              // 恢复鼠标样式
              mapInstance.getContainer().style.cursor = '';
              
              // 关闭信息窗口
              if (infoWindowOpened) {
                mapInstance.closeInfoWindow();
                infoWindowOpened = false;
              }
            });
            
            // 尝试添加事件监听器
            try {
              polygon.addEventListener('mouseover', hoverHandler);
              polygon.addEventListener('mouseout', leaveHandler);
              (polygon as any).__handlers.mouseover = hoverHandler;
              (polygon as any).__handlers.mouseout = leaveHandler;
              console.log('地块hover事件添加成功');
            } catch (e) {
              console.error('添加地块hover事件失败:', e);
            }
          }
          
          // 将多边形添加到地图（不使用DocumentFragment，因为地图元素可能不是标准DOM节点）
          mapInstance.addOverLay(polygon);
          plotMarkers.value.push(polygon);
        }
      } catch (error) {
        console.error('解析多边形坐标失败:', error, plot.polygonCoords);
      }
    }
  });
  
  // 调整地图视野以包含所有地块
  if (plotMarkers.value.length > 0) {
    // 调整地图视野以包含所有地块
    const bounds = calculateBounds(plotMarkers.value);
    if (bounds) {
      if (!hasBaseCenter.value) {
        try {
          mapInstance.setViewport(bounds);
        } catch (error) {
          if (bounds.getCenter) {
            const center = bounds.getCenter();
          mapInstance.centerAndZoom(center, 15);
          }
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
  
  // 事件委托处理函数
  const sensorClickHandler = (event) => {
    const sensor = event.target.sensorData;
    if (sensor) {
      showSensorInfo(sensor, event);
    }
  };
  
  // 根据设备类型获取对应的图标
  const getDeviceIcon = (deviceType: string) => {
    const s = 28;
    const hs = s / 2;
    const r = hs - 2;
    const iconColors: Record<string, string> = {
      '虫情测报设备': '#FF6B6B',
      '孢子仪设备': '#4ECDC4',
      '杀虫灯设备': '#FFD93D',
      '气象传感器': '#6BCF7F',
      '土壤传感器': '#8B4513',
      '光谱设备': '#9B59B6',
      '视频设备': '#FF6347',
      '未知设备': '#95A5A6'
    };
    const color = iconColors[deviceType] || iconColors['未知设备'];

    const makeSvg = (inner: string) => `
      <svg width="${s}" height="${s}" viewBox="0 0 ${s} ${s}" fill="none" xmlns="http://www.w3.org/2000/svg">
        <circle cx="${hs}" cy="${hs}" r="${r}" fill="${color}" stroke="#fff" stroke-width="1.5" opacity="0.92"/>
        ${inner}
      </svg>`;

    const label = (text: string) => {
      const ty = hs + r * 0.38;
      return `
        <text x="${hs}" y="${ty}" text-anchor="middle" fill="#fff" font-size="7" font-weight="700" font-family="sans-serif"
          stroke="${color}" stroke-width="0.4" paint-order="stroke">${text}</text>`;
    };

    let svgIcon = '';
    switch (deviceType) {
      case '虫情测报设备':
        svgIcon = makeSvg(`
          <circle cx="${hs}" cy="${hs - 3}" r="4" fill="none" stroke="#fff" stroke-width="1.2"/>
          <circle cx="${hs - 2}" cy="${hs - 4}" r="1.2" fill="#fff"/>
          <circle cx="${hs + 2}" cy="${hs - 2}" r="1.2" fill="#fff"/>
          <line x1="${hs}" y1="${hs - 7}" x2="${hs}" y2="${hs - 9}" stroke="#fff" stroke-width="1.2"/>
          <circle cx="${hs}" cy="${hs - 10}" r="1.5" fill="#fff"/>
          ${label('虫情')}
        `);
        break;
      case '孢子仪设备':
        svgIcon = makeSvg(`
          <circle cx="${hs}" cy="${hs - 3}" r="5" fill="none" stroke="#fff" stroke-width="1.2"/>
          <circle cx="${hs}" cy="${hs - 3}" r="2" fill="#fff" opacity="0.5"/>
          <circle cx="${hs - 3}" cy="${hs - 6}" r="1" fill="#fff"/>
          <circle cx="${hs + 3}" cy="${hs - 4}" r="1" fill="#fff"/>
          <circle cx="${hs - 2}" cy="${hs}" r="1" fill="#fff"/>
          <circle cx="${hs + 2}" cy="${hs - 1}" r="1" fill="#fff"/>
          ${label('孢子')}
        `);
        break;
      case '杀虫灯设备':
        svgIcon = makeSvg(`
          <path d="M${hs - 4} ${hs - 8} L${hs + 4} ${hs - 8}" stroke="#fff" stroke-width="1.5" stroke-linecap="round"/>
          <rect x="${hs - 3}" y="${hs - 8}" width="6" height="3" rx="1" fill="#fff" opacity="0.7"/>
          <rect x="${hs - 4}" y="${hs - 5}" width="8" height="7" rx="1.5" fill="none" stroke="#fff" stroke-width="1.2"/>
          <circle cx="${hs}" cy="${hs - 1.5}" r="2.5" fill="#fff" opacity="0.5"/>
          <line x1="${hs - 2}" y1="${hs - 1.5}" x2="${hs + 2}" y2="${hs - 1.5}" stroke="#fff" stroke-width="0.8"/>
          <line x1="${hs}" y1="${hs - 3.5}" x2="${hs}" y2="${hs + 0.5}" stroke="#fff" stroke-width="0.8"/>
          <line x1="${hs - 3}" y1="${hs + 2}" x2="${hs + 3}" y2="${hs + 2}" stroke="#fff" stroke-width="1.2"/>
          ${label('杀虫灯')}
        `);
        break;
      case '气象传感器':
        svgIcon = makeSvg(`
          <circle cx="${hs}" cy="${hs - 4}" r="3" fill="none" stroke="#fff" stroke-width="1.2"/>
          <line x1="${hs}" y1="${hs - 7}" x2="${hs}" y2="${hs - 1}" stroke="#fff" stroke-width="1"/>
          <line x1="${hs - 3}" y1="${hs - 4}" x2="${hs + 3}" y2="${hs - 4}" stroke="#fff" stroke-width="1"/>
          <path d="M${hs - 5} ${hs - 7} Q${hs - 3} ${hs - 10} ${hs - 1} ${hs - 7}" fill="none" stroke="#fff" stroke-width="1"/>
          <path d="M${hs - 3} ${hs - 9} Q${hs - 1} ${hs - 12} ${hs + 1} ${hs - 9}" fill="none" stroke="#fff" stroke-width="1"/>
          ${label('气象')}
        `);
        break;
      case '土壤传感器':
        svgIcon = makeSvg(`
          <rect x="${hs - 4}" y="${hs - 8}" width="8" height="2" rx="0.5" fill="#fff" opacity="0.8"/>
          <rect x="${hs - 3}" y="${hs - 5}" width="6" height="1.5" rx="0.5" fill="#fff" opacity="0.6"/>
          <rect x="${hs - 3}" y="${hs - 3}" width="6" height="1.5" rx="0.5" fill="#fff" opacity="0.6"/>
          <rect x="${hs - 3}" y="${hs - 1}" width="6" height="1.5" rx="0.5" fill="#fff" opacity="0.6"/>
          <line x1="${hs - 2}" y1="${hs + 1}" x2="${hs + 2}" y2="${hs + 1}" stroke="#fff" stroke-width="1.2"/>
          <line x1="${hs - 3}" y1="${hs + 3}" x2="${hs + 3}" y2="${hs + 3}" stroke="#fff" stroke-width="1.2"/>
          ${label('土壤')}
        `);
        break;
      case '光谱设备':
        svgIcon = makeSvg(`
          <rect x="${hs - 5}" y="${hs - 7}" width="10" height="8" rx="1.5" fill="none" stroke="#fff" stroke-width="1.2"/>
          <line x1="${hs - 3}" y1="${hs - 4}" x2="${hs - 3}" y2="${hs + 0}" stroke="#fff" stroke-width="1.5" stroke-linecap="round"/>
          <line x1="${hs - 1}" y1="${hs - 5}" x2="${hs - 1}" y2="${hs + 0}" stroke="#fff" stroke-width="1.5" stroke-linecap="round"/>
          <line x1="${hs + 1}" y1="${hs - 3}" x2="${hs + 1}" y2="${hs + 0}" stroke="#fff" stroke-width="1.5" stroke-linecap="round"/>
          <line x1="${hs + 3}" y1="${hs - 6}" x2="${hs + 3}" y2="${hs + 0}" stroke="#fff" stroke-width="1.5" stroke-linecap="round"/>
          <circle cx="${hs}" cy="${hs - 9}" r="1.5" fill="#fff"/>
          ${label('光谱')}
        `);
        break;
      case '视频设备':
        svgIcon = makeSvg(`
          <rect x="${hs - 5}" y="${hs - 6}" width="9" height="7" rx="1" fill="none" stroke="#fff" stroke-width="1.2"/>
          <path d="M${hs + 4} ${hs - 5} L${hs + 8} ${hs - 7.5} L${hs + 8} ${hs + 1.5} L${hs + 4} ${hs - 0.5} Z" fill="#fff" opacity="0.8"/>
          <circle cx="${hs - 2}" cy="${hs - 2.5}" r="2" fill="none" stroke="#fff" stroke-width="0.8"/>
          <circle cx="${hs - 2}" cy="${hs - 2.5}" r="0.8" fill="#fff"/>
          ${label('视频')}
        `);
        break;
      default:
        svgIcon = makeSvg(`
          <circle cx="${hs}" cy="${hs - 3}" r="4" fill="none" stroke="#fff" stroke-width="1.2"/>
          <circle cx="${hs}" cy="${hs - 3}" r="1.5" fill="#fff"/>
          ${label('设备')}
        `);
        break;
    }

    const base64Icon = btoa(unescape(encodeURIComponent(svgIcon)));
    return new T.Icon({
      iconUrl: `data:image/svg+xml;base64,${base64Icon}`,
      iconSize: new T.Point(s, s),
      iconAnchor: new T.Point(hs, hs)
    });
  };
  
  sensorData.value.forEach(sensor => {
    if (sensor.longitude && sensor.latitude) {
      // 根据设备类型获取对应的图标
      const icon = getDeviceIcon(sensor.type);
      
      // 创建传感器标记
      const lngLat = new T.LngLat(sensor.longitude, sensor.latitude);
      const marker = new T.Marker(lngLat, { icon: icon });
      
      // 存储传感器数据到标记对象，用于事件委托
      marker.sensorData = sensor;
      
      // 添加点击事件并存储 handler 引用
      marker.addEventListener('click', sensorClickHandler);
      (marker as any).__handlers = (marker as any).__handlers || {};
      (marker as any).__handlers.click = sensorClickHandler;
      
      // 将标记添加到地图（不使用DocumentFragment，因为地图元素可能不是标准DOM节点）
      mapInstance.addOverLay(marker);
      sensorMarkers.value.push(marker);
    }
  });
  
  // 调整地图视野以包含所有传感器
  if (sensorMarkers.value.length > 0) {
    // 调整地图视野以包含所有传感器
    const bounds = calculateBounds(sensorMarkers.value);
    if (bounds) {
      if (!hasBaseCenter.value) {
        try {
          mapInstance.setViewport(bounds);
        } catch (error) {
          if (bounds.getCenter) {
            const center = bounds.getCenter();
          mapInstance.centerAndZoom(center, 15);
          }
        }
      }
    }
  }
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
    
    // 根据useModalForPlotClick属性决定使用哪种展示方式
    if (props.useModalForPlotClick) {
      // 使用模态框显示地块信息
      currentPlotData.value = fullPlotData;
      openModal(true, {
        record: fullPlotData,
        isUpdate: true,
        showFooter: false, // 只读模式，不显示底部按钮
      });
    } else if (props.showLodgingRiskBelow) {
      // 检查当前是否在倒伏风险页面
      const isLodgingRiskPage = route.path.includes('/lodging-risk');
      
      if (isLodgingRiskPage) {
        // 在倒伏风险页面，点击地块时跳转到地块风险详情页
        const plotId = fullPlotData.plotId || fullPlotData.id;
        router.push(`/rapeseed/plot-risk-detail/${plotId}`);
      } else {
        // 在其他页面，只传递地块ID给父组件
        const plotId = fullPlotData.plotId || fullPlotData.id;
        emit('plot-click', plotId);
      }
    } else {
      // 使用原来的弹窗方式
      selectedPlot.value = fullPlotData;
    
      
      // 计算弹窗位置 - 使用地图坐标转换为屏幕坐标
      if (event && event.lngLat) {
        // 获取点击位置的经纬度
        const lngLat = event.lngLat;
        // 将经纬度转换为屏幕坐标
        const point = mapInstance.lngLatToContainerPoint(lngLat);
        
        // 设置弹窗位置，考虑弹窗大小，避免超出屏幕
        // 根据屏幕尺寸调整弹窗大小
        const isMobile = window.innerWidth <= 768;
        const popupWidth = isMobile ? window.innerWidth * 0.9 : 320; // 移动设备上使用90%宽度
        const popupHeight = isMobile ? window.innerHeight * 0.6 : 200; // 移动设备上使用60%高度
        
        // 获取地图容器的位置和大小
        const mapRect = mapContainer.value.getBoundingClientRect();
        
        // 计算弹窗相对于地图容器的位置
        // 默认显示在点击点的右上方
        let x = point.x + 15; // 右侧偏移15px
        let y = point.y - popupHeight - 15; // 上方偏移15px
        
        // 移动设备上居中显示
        if (isMobile) {
          x = (mapRect.width - popupWidth) / 2;
          y = (mapRect.height - popupHeight) / 2;
        } else {
          // 桌面设备上的位置调整
          // 检查是否超出右边界
          if (x + popupWidth > mapRect.width - 10) {
            x = point.x - popupWidth - 15; // 显示在点击点左侧
          }
          
          // 检查是否超出左边界
          if (x < 10) {
            x = 10; // 最小左边距
          }
          
          // 检查是否超出上边界
          if (y < 10) {
            y = point.y + 15; // 改为显示在点击点下方
          }
          
          // 再次检查是否超出下边界
          if (y + popupHeight > mapRect.height - 10) {
            // 如果下方也放不下，则居中显示
            y = (mapRect.height - popupHeight) / 2;
            x = (mapRect.width - popupWidth) / 2;
          }
          
          // 最后确保不会超出任何边界
          x = Math.max(10, Math.min(x, mapRect.width - popupWidth - 10));
          y = Math.max(10, Math.min(y, mapRect.height - popupHeight - 10));
        }
        
        popupPosition.value = { x, y };
      } else if (event && event.domEvent) {
        // 备用方案：使用domEvent
        const mapRect = mapContainer.value.getBoundingClientRect();
        const isMobile = window.innerWidth <= 768;
        const popupWidth = isMobile ? window.innerWidth * 0.9 : 320;
        
        // 移动设备上居中显示
        if (isMobile) {
          popupPosition.value = {
            x: (mapRect.width - popupWidth) / 2,
            y: 50
          };
        } else {
          popupPosition.value = {
            x: event.domEvent.clientX - mapRect.left + 10,
            y: event.domEvent.clientY - mapRect.top + 10
          };
        }
      } else {
        // 默认位置
        const mapRect = mapContainer.value.getBoundingClientRect();
        const isMobile = window.innerWidth <= 768;
        const popupWidth = isMobile ? window.innerWidth * 0.9 : 320;
        
        popupPosition.value = {
          x: (mapRect.width - popupWidth) / 2,
          y: 50
        };
      }
    }
  } catch (error) {
    console.error('显示地块信息失败:', error);
    createMessage.error('获取地块信息失败');
  }
};

// 显示传感器信息
const showSensorInfo = (sensor, event) => {
  try {
    // 创建传感器信息弹窗内容
    const content = `
      <div style="padding: 10px; max-width: 280px;">
        <p style="margin: 5px 0; font-size: 14px;"><strong>类型:</strong> ${sensor.name || '未知'}</p>
        <p style="margin: 5px 0; font-size: 14px;"><strong>状态:</strong> 
          <span style="color: ${sensor.status === 1 ? '#52c41a' : '#ff4d4f'}; font-weight: bold;">
            ${sensor.status === 1 ? '在线' : '离线'}
          </span>
        </p>
      </div>
    `;
    
    // 创建信息弹窗
    const infoWindow = new T.InfoWindow(content, {
      offset: new T.Point(0, -30)
    });
    
    // 获取传感器位置
    const lngLat = new T.LngLat(sensor.longitude, sensor.latitude);
    
    // 打开信息弹窗
    mapInstance.openInfoWindow(infoWindow, lngLat);
  } catch (error) {
    console.error('显示传感器信息失败:', error);
    // 降级方案：使用message显示基本信息
    createMessage.info(`传感器: ${sensor.name || '未命名'}, 类型: ${sensor.type || '未知'}, 状态: ${sensor.status === 'online' ? '在线' : '离线'}`);
  }
};

// 关闭地块信息弹窗
const closePlotInfo = () => {
  selectedPlot.value = null;
};

// 监听ESC键关闭弹窗
const handleKeyDown = (event) => {
  if (event.key === 'Escape' && selectedPlot.value) {
    closePlotInfo();
  }
};

// 添加键盘事件监听
onMounted(() => {
  document.addEventListener('keydown', handleKeyDown);
});

// 移除键盘事件监听
onUnmounted(() => {
  document.removeEventListener('keydown', handleKeyDown);
});

// 清除地块标记
const clearPlotMarkers = () => {
  if (!mapInstance || !plotMarkers.value.length) return;
  
  try {
    // 批量移除所有地块多边形
    plotMarkers.value.forEach(polygon => {
      if (polygon) {
        // 移除所有事件监听器
        try {
          const handlers = (polygon as any).__handlers;
          if (polygon.removeEventListener && handlers) {
            if (handlers.click) polygon.removeEventListener('click', handlers.click);
            if (handlers.mouseover) polygon.removeEventListener('mouseover', handlers.mouseover);
            if (handlers.mouseout) polygon.removeEventListener('mouseout', handlers.mouseout);
            (polygon as any).__handlers = undefined;
          }
        } catch (e) {
          console.warn('移除地块事件监听器失败:', e);
        }
        
        // 从地图上移除多边形
        mapInstance.removeOverLay(polygon);
        
        // 清理多边形上的自定义数据
        if (polygon.plotData) {
          polygon.plotData = null;
        }
      }
    });
    
    // 清空数组
    plotMarkers.value = [];
  } catch (error) {
    console.error('清除地块标记失败:', error);
    // 即使出错也要清空数组，防止内存泄漏
    plotMarkers.value = [];
  }
};

// 清除传感器标记
const clearSensorMarkers = () => {
  if (!mapInstance || !sensorMarkers.value.length) return;
  
  try {
    // 批量移除所有传感器标记
    sensorMarkers.value.forEach(marker => {
      if (marker) {
        // 移除所有事件监听器
        try {
          const handlers = (marker as any).__handlers;
          if (marker.removeEventListener && handlers) {
            if (handlers.click) marker.removeEventListener('click', handlers.click);
            (marker as any).__handlers = undefined;
          }
        } catch (e) {
          console.warn('移除传感器事件监听器失败:', e);
        }
        
        // 从地图上移除标记
        mapInstance.removeOverLay(marker);
        
        // 清理标记上的自定义数据
        if (marker.sensorData) {
          marker.sensorData = null;
        }
      }
    });
    
    // 清空数组
    sensorMarkers.value = [];
  } catch (error) {
    console.error('清除传感器标记失败:', error);
    // 即使出错也要清空数组，防止内存泄漏
    sensorMarkers.value = [];
  }
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
  
  // 如果绘制工具未初始化，尝试重新初始化
  if (!drawingTool.value) {
    console.log('绘制工具未初始化，尝试重新初始化');
    initDrawingTools();
    
    // 如果初始化仍然失败，返回错误
    if (!drawingTool.value) {
      console.error('绘制工具初始化失败');
      createMessage.error('绘制工具初始化失败，请刷新页面重试');
      return;
    }
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
  
  try {
    // 尝试从event中获取多边形对象
    if (event && event.currentPolygon) {
      currentPolygon.value = event.currentPolygon;
      console.log('保存当前多边形对象:', currentPolygon.value);
    }
    
    // 尝试从event.currentLnglats获取坐标点
    if (event && event.currentLnglats) {
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
    }
    
    // 如果currentLnglats不可用，显示错误信息
    console.error('无法获取多边形坐标点，currentLnglats不可用');
    createMessage.error('无法获取多边形坐标点，请重试');
  } catch (error) {
    console.error('处理绘制完成事件失败:', error);
    createMessage.error('处理绘制完成事件失败');
  }
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
    mapInstance.centerAndZoom(new T.LngLat(selectedBase.value.longitude, selectedBase.value.latitude), 15);
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
  // 关闭当前信息弹窗
  closePlotInfo();
  
  currentPlotData.value = plot;
  openModal(true, {
    isUpdate: true,
    record: plot, // 添加record参数，确保编辑弹框能获取到完整的地块数据
    showFooter: true, // 确保表单可编辑
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
      // 不将drawingTool设置为null，以便后续可以继续使用
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
// 监听基地ID变化
const stopWatchBaseId = watch(selectedBaseId, (newBaseId) => {
  if (newBaseId && mapInstance) {
    loadBaseLocation();
    loadPlotData();
    if (props.showSensors) {
      loadSensorData();
    }
  }
});
watchStoppers.push(stopWatchBaseId);

// 监听基地经纬度变化
const stopWatchBaseLocation = watch(
  () => [selectedBase.value.longitude, selectedBase.value.latitude],
  ([longitude, latitude]) => {
    if (longitude && latitude && mapInstance) {
      console.log('基地经纬度变化，更新地图中心:', longitude, latitude);
      mapInstance.centerAndZoom(new T.LngLat(longitude, latitude), 15);
    }
  }
);
watchStoppers.push(stopWatchBaseLocation);

// 监听plotData变化
const stopWatchPlotData = watch(
  () => props.plotData,
  (newPlotData) => {
    if (newPlotData && mapInstance) {
      console.log('plotData变化，重新渲染地块:', newPlotData);
      plotData.value = newPlotData;
      displayPlots();
    }
  },
  { deep: true, immediate: true }
);
watchStoppers.push(stopWatchPlotData);

// 监听plotRiskData变化
const stopWatchPlotRiskData = watch(
  () => props.plotRiskData,
  (newPlotRiskData) => {
    if (newPlotRiskData && mapInstance) {
      console.log('plotRiskData变化，重新渲染地块:', newPlotRiskData);
      // 重新渲染地块以更新颜色和hover信息
      displayPlots();
      
      // 直接初始化趋势图
      nextTick(() => {
        // 移除趋势图初始化，因为趋势图功能已不再使用
      });
    }
  },
  { deep: true, immediate: true }
);
watchStoppers.push(stopWatchPlotRiskData);


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
  // 清理所有watch监听器
  watchStoppers.forEach(stop => {
    try {
      stop();
    } catch (error) {
      console.error('清理watch监听器失败:', error);
    }
  });
  watchStoppers.length = 0; // 清空数组
  
  // 清理ResizeObserver
  if (resizeObserver.value) {
    resizeObserver.value.disconnect();
    resizeObserver.value = null;
  }
  
  // 清理地块标记和传感器标记
  clearPlotMarkers();
  clearSensorMarkers();
  
  // 清理当前多边形
  if (currentPolygon.value) {
    try {
      const handlers = (currentPolygon.value as any).__handlers;
      if (currentPolygon.value.removeEventListener && handlers) {
        if (handlers.click) currentPolygon.value.removeEventListener('click', handlers.click);
        if (handlers.mouseover) currentPolygon.value.removeEventListener('mouseover', handlers.mouseover);
        if (handlers.mouseout) currentPolygon.value.removeEventListener('mouseout', handlers.mouseout);
        (currentPolygon.value as any).__handlers = undefined;
      }
      if (mapInstance) {
        mapInstance.removeOverLay(currentPolygon.value);
      }
    } catch (error) {
      console.error('清理当前多边形失败:', error);
    } finally {
      currentPolygon.value = null;
    }
  }
  
  // 清理绘制工具
  if (drawingTool.value) {
    try {
      drawingTool.value.close();
      // 移除所有可能的事件监听器
      drawingTool.value.removeEventListener('mouseup', handleDrawingComplete);
      drawingTool.value.removeEventListener('draw', handleDrawingComplete);
      drawingTool.value.removeEventListener('polygon', handleDrawingComplete);
    } catch (error) {
      console.error('清理绘制工具失败:', error);
    } finally {
      drawingTool.value = null;
    }
  }
  
  // 清理地图实例
  if (mapInstance) {
    try {
      mapInstance.clearOverLays();
      mapInstance = null;
    } catch (error) {
      console.error('清理地图实例失败:', error);
    }
  }
  
  // 清理所有响应式数据
  plotMarkers.value = [];
  sensorMarkers.value = [];
  currentPolygon.value = null;
  plotData.value = [];
  sensorData.value = [];
  // plotRiskData是props，不能直接赋值
  selectedPlot.value = null;
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
    transition: border-color 0.3s ease;
    
    &:hover {
      border-color: #40a9ff;
    }
  }
  
  .plot-info-popup {
    position: absolute; /* 改为绝对定位，相对于地图容器 */
    z-index: 9999; /* 提高z-index确保显示在最上层 */
    max-width: 95vw; /* 限制最大宽度，防止超出视口 */
    max-height: 95vh; /* 限制最大高度，防止超出视口 */
    overflow: visible; /* 确保内容可见 */
    
    .plot-popup-card {
      width: 320px; /* 调整宽度为更合适的尺寸 */
      max-height: 90vh; /* 减小最大高度，确保在视口内 */
      overflow-y: auto; /* 添加垂直滚动条 */
      overflow-x: hidden; /* 隐藏水平滚动条 */
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
      border-radius: 12px;
      position: relative; /* 确保相对定位 */
      border: none;
      
      /* 添加微妙的背景渐变 */
      background: linear-gradient(145deg, #ffffff 0%, #f8f9fa 100%);
      
      /* 添加微妙的边框光晕效果 */
      &::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        border-radius: 12px;
        padding: 1px;
        background: linear-gradient(145deg, rgba(24, 144, 255, 0.2), rgba(82, 196, 26, 0.2));
        -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
        -webkit-mask-composite: xor;
        mask-composite: exclude;
        z-index: -1;
      }
      
      /* 自定义滚动条样式 */
      &::-webkit-scrollbar {
        width: 6px;
      }
      
      &::-webkit-scrollbar-track {
        background: rgba(0, 0, 0, 0.05);
        border-radius: 3px;
      }
      
      &::-webkit-scrollbar-thumb {
        background: rgba(0, 0, 0, 0.2);
        border-radius: 3px;
        
        &:hover {
          background: rgba(0, 0, 0, 0.3);
        }
      }
      
      /* 卡片标题样式优化 */
      :deep(.ant-card-head) {
        background: linear-gradient(90deg, rgba(24, 144, 255, 0.05) 0%, rgba(82, 196, 26, 0.05) 100%);
        border-bottom: 1px solid rgba(0, 0, 0, 0.06);
        border-radius: 12px 12px 0 0;
        
        .ant-card-head-title {
          font-weight: 600;
          font-size: 15px;
          color: rgba(0, 0, 0, 0.85);
        }
        
        .ant-card-extra {
          .ant-btn {
            color: rgba(0, 0, 0, 0.45);
            border: none;
            box-shadow: none;
            
            &:hover {
              color: rgba(0, 0, 0, 0.85);
              background-color: rgba(0, 0, 0, 0.06);
            }
          }
        }
      }
      
      /* 卡片内容样式优化 */
      :deep(.ant-card-body) {
        padding: 16px;
      }
      
      .plot-info-content {
        p {
          margin-bottom: 12px;
          font-size: 14px;
          line-height: 1.6;
          color: rgba(0, 0, 0, 0.75);
          display: flex;
          align-items: center;
          
          strong {
            color: rgba(0, 0, 0, 0.9);
            font-weight: 600;
            margin-right: 8px;
            min-width: 80px;
          }
          
          /* 添加微妙的分隔线 */
          &:not(:last-child)::after {
            content: '';
            display: block;
            height: 1px;
            background: linear-gradient(90deg, transparent, rgba(0, 0, 0, 0.06), transparent);
            margin: 12px 0 8px;
          }
        }
      }
      
      .plot-actions {
        margin-top: 16px;
        text-align: right;
        padding-top: 12px;
        border-top: 1px solid rgba(0, 0, 0, 0.06);
        
        .ant-btn {
          border-radius: 6px;
          font-weight: 500;
          height: 32px;
          padding: 0 16px;
          
          &:not(:last-child) {
            margin-right: 8px;
          }
        }
      }
      
      // 风险等级总览卡片样式
      .risk-overview-card {
        margin-bottom: 16px;
        transition: transform 0.2s ease, box-shadow 0.2s ease;
        
        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
        }
        
        .risk-overview-content {
          display: flex;
          justify-content: space-between;
          align-items: center;
          
          .risk-level-indicator {
            display: flex;
            flex-direction: column;
            align-items: center;
            padding: 16px;
            transition: transform 0.2s ease;
            
            &:hover {
              transform: scale(1.05);
            }
            
            .risk-level-icon {
              font-size: 48px;
              margin-bottom: 8px;
              transition: transform 0.3s ease;
              
              &:hover {
                transform: rotate(10deg);
              }
            }
            
            .risk-level-text {
              font-size: 24px;
              font-weight: bold;
              margin-bottom: 4px;
            }
            
            .risk-probability-range {
              font-size: 14px;
              margin-bottom: 4px;
            }
            
            .last-update {
              font-size: 12px;
              color: #666;
            }
          }
          
          .risk-factors-info {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 16px;
            
            .factor-item {
              display: flex;
              align-items: center;
              padding: 8px;
              border-radius: 4px;
              transition: background-color 0.2s ease;
              
              &:hover {
                background-color: rgba(24, 144, 255, 0.1);
              }
              
              .factor-icon {
                font-size: 24px;
                margin-right: 8px;
                transition: transform 0.2s ease;
                
                &:hover {
                  transform: scale(1.1);
                }
              }
              
              .factor-details {
                flex: 1;
                
                .factor-name {
                  font-size: 14px;
                  margin-bottom: 2px;
                }
              }
              
              .factor-value {
                font-size: 14px;
                font-weight: bold;
              }
            }
          }
        }
      }
      
      // 风险预警卡片样式
      .risk-forecast-card {
        margin-bottom: 16px;
        transition: transform 0.2s ease, box-shadow 0.2s ease;
        
        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
        }
        
        .card-title {
          display: flex;
          align-items: center;
          font-weight: bold;
          
          .anticon {
            margin-right: 8px;
          }
        }
        
        .risk-forecast-container {
          display: grid;
          grid-template-columns: repeat(7, 1fr);
          gap: 8px;
          
          .risk-day-card {
            padding: 8px;
            border-radius: 4px;
            transition: all 0.2s ease;
            
            &:hover {
              transform: translateY(-3px);
              box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
            }
            
            .day-header {
              text-align: center;
              margin-bottom: 8px;
              
              .day-date {
                font-size: 16px;
                font-weight: bold;
              }
              
              .day-weekday {
                font-size: 12px;
                color: #666;
              }
            }
            
            .risk-level-badge {
              text-align: center;
              padding: 4px;
              border-radius: 4px;
              font-size: 12px;
              font-weight: bold;
              margin-bottom: 8px;
              transition: all 0.2s ease;
              
              &:hover {
                transform: scale(1.05);
              }
            }
            
            .risk-details {
              .risk-probability {
                text-align: center;
                margin-bottom: 8px;
                
                .probability-value {
                  font-size: 18px;
                  font-weight: bold;
                  transition: color 0.2s ease;
                  
                  &:hover {
                    color: #1890ff;
                  }
                }
                
                .probability-label {
                  font-size: 12px;
                  color: #666;
                  margin-left: 4px;
                }
              }
              
              .risk-factors {
                display: flex;
                flex-direction: column;
                gap: 4px;
                
                .factor-item {
                  font-size: 12px;
                  display: flex;
                  justify-content: space-between;
                  transition: background-color 0.2s ease;
                  
                  &:hover {
                    background-color: rgba(24, 144, 255, 0.05);
                  }
                }
              }
            }
          }
        }
        
        .risk-trend-chart {
          height: 300px; /* 增加趋势图高度 */
          min-height: 300px; /* 设置最小高度 */
          width: 100%;
          transition: all 0.3s ease;
          
          &:hover {
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
            border-radius: 4px;
          }
          
          .trend-chart {
            width: 100%;
            height: 100%;
            min-height: 300px;
          }
        }
      }
      
      // 防控建议卡片样式
      .suggestions-card {
        transition: transform 0.2s ease, box-shadow 0.2s ease;
        
        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
        }
        
        .card-title {
          display: flex;
          align-items: center;
          font-weight: bold;
          
          .anticon {
            margin-right: 8px;
          }
        }
        
        .suggestions-panel {
          margin-bottom: 8px;
          
          .suggestions-content {
            .suggestion-item {
              display: flex;
              align-items: flex-start;
              margin-bottom: 12px;
              padding: 8px;
              border-radius: 4px;
              transition: background-color 0.2s ease;
              
              &:hover {
                background-color: rgba(24, 144, 255, 0.05);
              }
              
              .suggestion-icon {
                font-size: 16px;
                margin-right: 8px;
                margin-top: 2px;
                transition: transform 0.2s ease;
                
                &:hover {
                  transform: scale(1.2);
                }
                
                &.danger {
                  color: #f5222d;
                }
                
                &.warning {
                  color: #faad14;
                }
              }
              
              .suggestion-text {
                flex: 1;
                
                .suggestion-title {
                  font-size: 14px;
                  font-weight: bold;
                  margin-bottom: 4px;
                  transition: color 0.2s ease;
                  
                  &:hover {
                    color: #1890ff;
                  }
                }
                
                .suggestion-desc {
                  font-size: 12px;
                  color: #666;
                }
              }
            }
          }
        }
      }
    }
  }
  
  .map-toolbar {
    position: absolute;
    top: 10px;
    right: 10px;
    z-index: 999;
    background: rgba(255, 255, 255, 0.9);
    padding: 8px;
    border-radius: 4px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
    transition: all 0.3s ease;
    
    &:hover {
      background: rgba(255, 255, 255, 0.95);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
    }
  }
}

/* 弹出框过渡动画 */
.popup-fade-enter-active,
.popup-fade-leave-active {
  transition: all 0.3s cubic-bezier(0.23, 1, 0.32, 1);
}

.popup-fade-enter-from {
  opacity: 0;
  transform: translateY(-10px) scale(0.95);
}

.popup-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px) scale(0.95);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .plot-info-popup .plot-popup-card {
    width: 700px;
  }
}

@media (max-width: 992px) {
  .plot-info-popup .plot-popup-card {
    width: 600px;
  }
  
  .risk-overview-content {
    flex-direction: column;
  }
  
  .risk-factors-info {
    grid-template-columns: 1fr !important;
  }
  
  .risk-forecast {
    grid-template-columns: repeat(3, 1fr) !important;
  }
}

@media (max-width: 768px) {
  .plot-info-popup .plot-popup-card {
    width: 95vw;
    max-width: 500px;
  }
  
  .plot-popup-header {
    padding: 12px 16px;
    
    .plot-popup-title {
      font-size: 16px;
    }
  }
  
  .plot-popup-content {
    padding: 12px 16px;
  }
  
  .risk-overview-content {
    .risk-level-info {
      min-width: 100px;
      
      .risk-level-text {
        font-size: 14px;
      }
      
      .risk-level-value {
        font-size: 24px;
      }
    }
  }
  
  .risk-factors-info {
    grid-template-columns: 1fr;
    gap: 8px;
    
    .factor-item {
      padding: 8px;
      
      .factor-label {
        font-size: 12px;
      }
      
      .factor-value {
        font-size: 14px;
      }
    }
  }
  
  .risk-forecast {
    grid-template-columns: repeat(2, 1fr);
    gap: 8px;
    
    .forecast-day {
      padding: 8px;
      
      .day-date {
        font-size: 12px;
      }
      
      .risk-probability .probability-value {
        font-size: 16px;
      }
    }
  }
  
  .risk-trend-chart {
    height: 250px;
    min-height: 250px;
  }
}

@media (max-width: 576px) {
  .plot-info-popup {
    max-height: 80vh;
    
    .plot-popup-card {
      width: 95vw;
      max-width: none;
      max-height: 80vh;
      overflow-y: auto;
    }
  }
  
  .plot-popup-header {
    padding: 10px 12px;
    
    .plot-popup-title {
      font-size: 14px;
    }
  }
  
  .plot-popup-content {
    padding: 10px 12px;
  }
  
  .risk-overview {
    padding: 12px;
    
    .section-title {
      font-size: 14px;
    }
  }
  
  .risk-overview-content {
    gap: 12px;
    
    .risk-level-info {
      min-width: 80px;
      
      .risk-level-text {
        font-size: 12px;
      }
      
      .risk-level-value {
        font-size: 20px;
      }
    }
  }
  
  .risk-factors-info {
    gap: 6px;
    
    .factor-item {
      padding: 6px;
      
      .factor-label {
        font-size: 11px;
      }
      
      .factor-value {
        font-size: 13px;
      }
    }
  }
  
  .risk-forecast {
    grid-template-columns: 1fr;
    gap: 6px;
    
    .forecast-day {
      padding: 6px;
      
      .day-date {
        font-size: 11px;
      }
      
      .risk-probability .probability-value {
        font-size: 14px;
      }
      
      .risk-factors .factor-item {
        font-size: 11px;
      }
    }
  }
  
  .risk-trend-chart {
    height: 200px;
    min-height: 200px;
  }
  
  .suggestions-card {
    .suggestions-panel {
      .suggestions-content .suggestion-item {
        padding: 6px;
        
        .suggestion-text {
          .suggestion-title {
            font-size: 13px;
          }
          
          .suggestion-desc {
            font-size: 11px;
          }
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .plot-info-popup .plot-popup-card {
    width: 90vw;
    max-width: 500px;
  }
  
  .risk-forecast-container {
    grid-template-columns: repeat(4, 1fr) !important;
  }
}

@media (max-width: 576px) {
  .plot-info-popup .plot-popup-card {
    width: 95vw;
    max-width: 400px;
  }
  
  .risk-forecast-container {
    grid-template-columns: repeat(2, 1fr) !important;
  }
  
  .risk-level-indicator {
    padding: 8px !important;
    
    .risk-level-icon {
      font-size: 36px !important;
    }
    
    .risk-level-text {
      font-size: 20px !important;
    }
  }
}

/* 降低天地图缩放控件的z-index，防止遮挡下拉框 */
:deep(.tdt-control-zoom) {
  z-index: 10 !important;
}

/* 降低天地图所有控件的z-index */
:deep(.tdt-control) {
  z-index: 10 !important;
}
</style>
