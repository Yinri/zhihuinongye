<template>
  <div class="aerial-map-container" :class="{ readonly }">
    <div class="map-tools" v-if="!readonly">
      <a-space>
        <a-input-search
          v-model:value="searchKeyword"
          placeholder="搜索地块位置"
          style="width: 200px"
          @search="handleSearch"
        />
        <a-button @click="resetMapView">重置视图</a-button>
        <a-button @click="toggleDrawMode" :type="drawMode ? 'primary' : 'default'">
          {{ drawMode ? '完成绘制' : '绘制地块' }}
        </a-button>
        <a-button @click="savePolygons" :disabled="polygons.length === 0">
          保存地块
        </a-button>
        <a-button @click="clearAllPolygons" :disabled="polygons.length === 0">
          清除所有
        </a-button>
      </a-space>
    </div>
    
    <div 
      ref="mapContainer" 
      class="map-canvas-container" 
      @mousedown="handleMouseDown"
      @mousemove="handleMouseMove"
      @mouseup="handleMouseUp"
      @wheel="handleWheel"
      @contextmenu.prevent
    >
      <!-- 航拍图底图 -->
      <img 
        ref="aerialImage" 
        :src="aerialImageUrl" 
        class="aerial-image" 
        :style="imageStyle"
        @load="onImageLoad"
        @error="onImageError"
      />
      
      <!-- Canvas层用于绘制多边形 -->
      <canvas 
        ref="canvas" 
        class="polygon-canvas" 
        :style="canvasStyle"
      ></canvas>
      
      <!-- SVG层用于显示交互元素 -->
      <svg 
        ref="svg" 
        class="polygon-svg" 
        :style="canvasStyle"
      >
        <!-- 已完成的多边形 -->
        <g v-for="(polygon, index) in polygons" :key="index">
          <polygon
            :points="getPolygonPoints(polygon.points)"
            :fill="polygon.color"
            :stroke="polygon.strokeColor"
            :stroke-width="2"
            :fill-opacity="0.3"
            @click="selectPolygon(index)"
            @mouseenter="highlightPolygon(index)"
            @mouseleave="unhighlightPolygon(index)"
            class="polygon-shape"
          />
          <!-- 多边形顶点 -->
          <circle
            v-for="(point, pointIndex) in polygon.points"
            :key="pointIndex"
            :cx="point.x"
            :cy="point.y"
            r="5"
            :fill="polygon.strokeColor"
            @click.stop="selectPoint(index, pointIndex)"
            class="polygon-point"
          />
        </g>
        
        <!-- 当前正在绘制的多边形 -->
        <g v-if="currentPolygon.points.length > 0 && !readonly">
          <polyline
            :points="getPolylinePoints(currentPolygon.points)"
            :stroke="currentPolygon.strokeColor"
            :stroke-width="2"
            fill="none"
            stroke-dasharray="5,5"
          />
          <!-- 当前多边形的顶点 -->
          <circle
            v-for="(point, index) in currentPolygon.points"
            :key="index"
            :cx="point.x"
            :cy="point.y"
            r="5"
            :fill="currentPolygon.strokeColor"
            class="polygon-point"
          />
        </g>
      </svg>
      
      <!-- 地块信息弹窗 -->
      <div 
        v-if="selectedPolygonInfo" 
        class="polygon-info-popup"
        :style="popupStyle"
      >
        <div class="popup-header">
          <h4>{{ selectedPolygonInfo.name }}</h4>
          <button @click="closePopup" class="close-btn">&times;</button>
        </div>
        <div class="popup-content">
          <p v-if="selectedPolygonInfo.fieldCode"><strong>地块编号:</strong> {{ selectedPolygonInfo.fieldCode }}</p>
          <p><strong>状态:</strong> {{ getStatusText(selectedPolygonInfo.status) }}</p>
          <p><strong>面积:</strong> {{ selectedPolygonInfo.area }} 亩</p>
          <p><strong>负责人:</strong> {{ selectedPolygonInfo.manager }}</p>
          <p><strong>联系电话:</strong> {{ selectedPolygonInfo.phone }}</p>
          <p><strong>预计产量:</strong> {{ selectedPolygonInfo.expectedYield }} 吨</p>
          <p><strong>实际产量:</strong> {{ selectedPolygonInfo.actualYield }} 吨</p>
          <p><strong>收割进度:</strong> {{ selectedPolygonInfo.progress }}%</p>
          <p><strong>预计收割日期:</strong> {{ selectedPolygonInfo.estimatedDate }}</p>
          <p><strong>土壤类型:</strong> {{ selectedPolygonInfo.soilType }}</p>
          <p><strong>灌溉方式:</strong> {{ selectedPolygonInfo.irrigation }}</p>
          <p><strong>油菜品种:</strong> {{ selectedPolygonInfo.variety }}</p>
          <p><strong>种植日期:</strong> {{ selectedPolygonInfo.plantDate }}</p>
        </div>
        <div class="popup-actions" v-if="!readonly">
          <a-button size="small" @click="editPolygon">编辑</a-button>
          <a-button 
            size="small" 
            @click="openAssociationModal" 
            :disabled="selectedPolygonInfo?.isAssociated"
          >
            {{ selectedPolygonInfo?.isAssociated ? '已关联' : '关联地块' }}
          </a-button>
          <a-button size="small" danger @click="deletePolygon">删除</a-button>
        </div>
      </div>
    </div>
    
    <!-- 地图图例 -->
    <div class="map-legend">
      <div class="legend-item">
        <div class="legend-marker harvesting"></div>
        <span>收割中</span>
      </div>
      <div class="legend-item">
        <div class="legend-marker completed"></div>
        <span>已完成</span>
      </div>
      <div class="legend-item">
        <div class="legend-marker pending"></div>
        <span>未开始</span>
      </div>
    </div>
    
    <!-- 地块编辑对话框 -->
    <a-modal
      v-model:open="editModalVisible"
      title="编辑地块信息"
      @ok="savePolygonInfo"
      @cancel="cancelEdit"
    >
      <a-form :model="editingPolygon" layout="vertical">
        <a-form-item label="地块编号" name="fieldCode" v-if="editingPolygon.fieldCode">
          <a-input v-model:value="editingPolygon.fieldCode" disabled />
        </a-form-item>
        <a-form-item label="地块名称" name="name">
          <a-input v-model:value="editingPolygon.name" />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-select v-model:value="editingPolygon.status">
            <a-select-option value="pending">未开始</a-select-option>
            <a-select-option value="harvesting">收割中</a-select-option>
            <a-select-option value="completed">已完成</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="面积(亩)" name="area">
          <a-input-number v-model:value="editingPolygon.area" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item label="负责人" name="manager">
          <a-input v-model:value="editingPolygon.manager" />
        </a-form-item>
        <a-form-item label="联系电话" name="phone">
          <a-input v-model:value="editingPolygon.phone" />
        </a-form-item>
        <a-form-item label="预计产量(吨)" name="expectedYield">
          <a-input-number v-model:value="editingPolygon.expectedYield" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item label="实际产量(吨)" name="actualYield">
          <a-input-number v-model:value="editingPolygon.actualYield" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item label="收割进度(%)" name="progress">
          <a-input-number v-model:value="editingPolygon.progress" :min="0" :max="100" style="width: 100%" />
        </a-form-item>
        <a-form-item label="预计收割日期" name="estimatedDate">
          <a-date-picker v-model:value="editingPolygon.estimatedDate" style="width: 100%" />
        </a-form-item>
        <a-form-item label="土壤类型" name="soilType">
          <a-input v-model:value="editingPolygon.soilType" />
        </a-form-item>
        <a-form-item label="灌溉方式" name="irrigation">
          <a-input v-model:value="editingPolygon.irrigation" />
        </a-form-item>
        <a-form-item label="油菜品种" name="variety">
          <a-input v-model:value="editingPolygon.variety" />
        </a-form-item>
        <a-form-item label="种植日期" name="plantDate">
          <a-date-picker v-model:value="editingPolygon.plantDate" style="width: 100%" />
        </a-form-item>
      </a-form>
    </a-modal>
    
    <!-- 地块关联对话框 -->
    <FieldAssociation
      v-model:visible="associationModalVisible"
      :polygon-info="selectedPolygonForAssociation || {}"
      @confirm="handleAssociationConfirm"
    />
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, computed, onMounted, nextTick, watch } from 'vue';
import { message } from 'ant-design-vue';
import dayjs from 'dayjs';
import FieldAssociation from '../FieldAssociation/index.vue';

// 定义接口类型
interface Point {
  x: number;
  y: number;
  lng?: number;
  lat?: number;
}

interface Polygon {
  id?: string;
  name: string;
  points: Point[];
  status: 'pending' | 'harvesting' | 'completed';
  color: string;
  strokeColor: string;
  area: number;
  manager: string;
  phone: string;
  expectedYield: number;
  actualYield: number;
  progress: number;
  estimatedDate: string;
  soilType: string;
  irrigation: string;
  variety: string;
  plantDate: string;
  // 关联字段信息
  fieldId?: string;
  fieldCode?: string;
  isAssociated?: boolean;
}

// 组件属性
const props = defineProps({
  aerialImageUrl: {
    type: String,
    default: '/images/aerial-map.jpg' // 默认航拍图路径
  },
  initialPolygons: {
    type: Array as () => Polygon[],
    default: () => []
  },
  readonly: {
    type: Boolean,
    default: false
  }
});

// 事件定义
const emit = defineEmits(['polygons-updated', 'polygon-selected', 'load']);

// 响应式数据
const mapContainer = ref<HTMLDivElement | null>(null);
const aerialImage = ref<HTMLImageElement | null>(null);
const canvas = ref<HTMLCanvasElement | null>(null);
const svg = ref<SVGSVGElement | null>(null);

const searchKeyword = ref('');
const drawMode = ref(false);
const isDragging = ref(false);
const isPanning = ref(false);
const dragStartPoint = ref<Point>({ x: 0, y: 0 });
const lastPanPoint = ref<Point>({ x: 0, y: 0 });

// 地图状态
const scale = ref(1);
const offsetX = ref(0);
const offsetY = ref(0);

// 多边形数据
const polygons = ref<Polygon[]>([]);
const currentPolygon = reactive<Polygon>({
  name: '',
  points: [],
  status: 'pending',
  color: '#ff4d4f',
  strokeColor: '#ff4d4f',
  area: 0,
  manager: '',
  phone: '',
  expectedYield: 0,
  actualYield: 0,
  progress: 0,
  estimatedDate: '',
  soilType: '',
  irrigation: '',
  variety: '',
  plantDate: ''
});

// 选中的多边形信息
const selectedPolygonIndex = ref(-1);
const selectedPolygonInfo = ref<Polygon | null>(null);
const popupPosition = ref<Point>({ x: 0, y: 0 });

// 编辑对话框
const editModalVisible = ref(false);
const editingPolygon = reactive<Polygon>({
  name: '',
  points: [],
  status: 'pending',
  color: '#ff4d4f',
  strokeColor: '#ff4d4f',
  area: 0,
  manager: '',
  phone: '',
  expectedYield: 0,
  actualYield: 0,
  progress: 0,
  estimatedDate: '',
  soilType: '',
  irrigation: '',
  variety: '',
  plantDate: ''
});

// 地块关联相关
const associationModalVisible = ref(false);
const selectedPolygonForAssociation = ref<Polygon | null>(null);

// 计算属性
const imageStyle = computed(() => {
  return {
    transform: `translate(${offsetX.value}px, ${offsetY.value}px) scale(${scale.value})`,
    transformOrigin: 'top left',
    width: '100%',
    height: '100%',
    objectFit: 'contain'
  };
});

const canvasStyle = computed(() => {
  return {
    position: 'absolute',
    top: 0,
    left: 0,
    width: '100%',
    height: '100%',
    pointerEvents: drawMode.value && !props.readonly ? 'auto' : 'none'
  };
});

const popupStyle = computed(() => {
  return {
    position: 'absolute',
    left: `${popupPosition.value.x}px`,
    top: `${popupPosition.value.y}px`,
    zIndex: 1000
  };
});

// 初始化
onMounted(() => {
  // 初始化多边形数据
  if (props.initialPolygons.length > 0) {
    polygons.value = [...props.initialPolygons];
  }
  
  // 设置画布大小
  resizeCanvas();
  
  // 监听窗口大小变化
  window.addEventListener('resize', resizeCanvas);
});

// 监听多边形变化
watch(polygons, (newPolygons) => {
  emit('polygons-updated', newPolygons);
}, { deep: true });

// 方法
const resizeCanvas = () => {
  if (!mapContainer.value || !canvas.value || !svg.value) return;
  
  const rect = mapContainer.value.getBoundingClientRect();
  canvas.value.width = rect.width;
  canvas.value.height = rect.height;
  
  svg.value.setAttribute('width', rect.width.toString());
  svg.value.setAttribute('height', rect.height.toString());
  
  // 重绘所有多边形
  redrawPolygons();
};

const onImageLoad = () => {
  console.log('航拍图加载完成');
  // 图片加载完成后，可以初始化一些数据
  emit('load');
};

const onImageError = () => {
  console.error('航拍图加载失败');
  message.error('航拍图加载失败，请检查图片路径');
};

const handleMouseDown = (e: MouseEvent) => {
  // 如果是只读模式，只允许平移地图
  if (props.readonly) {
    const rect = mapContainer.value?.getBoundingClientRect();
    if (!rect) return;
    
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;
    
    // 右键或Shift+左键：平移地图
    if (e.button === 2 || (e.button === 0 && e.shiftKey)) {
      isPanning.value = true;
      lastPanPoint.value = { x, y };
    }
    return;
  }
  
  const rect = mapContainer.value?.getBoundingClientRect();
  if (!rect) return;
  
  const x = e.clientX - rect.left;
  const y = e.clientY - rect.top;
  
  if (e.button === 2 || (e.button === 0 && e.shiftKey)) {
    // 右键或Shift+左键：平移地图
    isPanning.value = true;
    lastPanPoint.value = { x, y };
    return;
  }
  
  if (drawMode.value) {
    // 绘制模式：添加点
    addPointToPolygon(x, y);
  }
};

const handleMouseMove = (e: MouseEvent) => {
  const rect = mapContainer.value?.getBoundingClientRect();
  if (!rect) return;
  
  const x = e.clientX - rect.left;
  const y = e.clientY - rect.top;
  
  if (isPanning.value) {
    // 平移地图
    offsetX.value += x - lastPanPoint.value.x;
    offsetY.value += y - lastPanPoint.value.y;
    lastPanPoint.value = { x, y };
    
    // 重绘以反映平移变化
    redrawPolygons();
  }
};

const handleMouseUp = (e: MouseEvent) => {
  if (isPanning.value) {
    isPanning.value = false;
    
    // 重绘以反映平移结束
    redrawPolygons();
  }
};

const handleWheel = (e: WheelEvent) => {
  // 如果是只读模式，禁用缩放
  if (props.readonly) return;
  
  e.preventDefault();
  
  const rect = mapContainer.value?.getBoundingClientRect();
  if (!rect) return;
  
  const x = e.clientX - rect.left;
  const y = e.clientY - rect.top;
  
  // 计算缩放前的鼠标位置相对于图片的位置
  const beforeScaleX = (x - offsetX.value) / scale.value;
  const beforeScaleY = (y - offsetY.value) / scale.value;
  
  // 缩放
  const delta = e.deltaY > 0 ? 0.9 : 1.1;
  const newScale = Math.max(0.5, Math.min(3, scale.value * delta));
  
  // 计算缩放后的偏移量，使鼠标位置保持不变
  offsetX.value = x - beforeScaleX * newScale;
  offsetY.value = y - beforeScaleY * newScale;
  
  scale.value = newScale;
  
  // 重绘以反映缩放变化
  redrawPolygons();
};

const addPointToPolygon = (x: number, y: number) => {
  // 转换为相对于图片的坐标
  const imageX = (x - offsetX.value) / scale.value;
  const imageY = (y - offsetY.value) / scale.value;
  
  currentPolygon.points.push({ x: imageX, y: imageY });
  
  // 如果是第一个点，设置默认颜色
  if (currentPolygon.points.length === 1) {
    updatePolygonColor(currentPolygon);
  }
  
  // 立即重绘以显示新添加的点
  redrawPolygons();
};

const updatePolygonColor = (polygon: Polygon) => {
  switch (polygon.status) {
    case 'completed':
      polygon.color = '#52c41a';
      polygon.strokeColor = '#52c41a';
      break;
    case 'harvesting':
      polygon.color = '#faad14';
      polygon.strokeColor = '#faad14';
      break;
    case 'pending':
    default:
      polygon.color = '#ff4d4f';
      polygon.strokeColor = '#ff4d4f';
      break;
  }
};

const toggleDrawMode = () => {
  if (drawMode.value) {
    // 完成绘制
    if (currentPolygon.points.length >= 3) {
      // 创建新多边形
      const newPolygon: Polygon = {
        ...currentPolygon,
        id: `polygon-${Date.now()}`,
        name: `地块${polygons.value.length + 1}`,
        area: calculatePolygonArea(currentPolygon.points),
        manager: '未指定',
        phone: '',
        expectedYield: 0,
        actualYield: 0,
        progress: 0,
        estimatedDate: dayjs().format('YYYY-MM-DD'),
        soilType: '',
        irrigation: '',
        variety: '',
        plantDate: dayjs().format('YYYY-MM-DD')
      };
      
      polygons.value.push(newPolygon);
      
      // 重置当前多边形
      currentPolygon.points = [];
      currentPolygon.name = '';
      
      // 选中新创建的多边形
      selectedPolygonIndex.value = polygons.value.length - 1;
      selectedPolygonInfo.value = newPolygon;
      
      // 直接打开关联地块对话框
      selectedPolygonForAssociation.value = newPolygon;
      associationModalVisible.value = true;
      
      message.success('地块绘制完成，请关联真实地块信息');
    } else {
      message.warning('至少需要3个点才能形成地块');
    }
  } else {
    // 开始绘制
    currentPolygon.points = [];
  }
  
  drawMode.value = !drawMode.value;
  
  // 重绘以反映模式变化
  redrawPolygons();
};

const calculatePolygonArea = (points: Point[]): number => {
  // 使用鞋带公式计算多边形面积
  if (points.length < 3) return 0;
  
  let area = 0;
  for (let i = 0; i < points.length; i++) {
    const j = (i + 1) % points.length;
    area += points[i].x * points[j].y;
    area -= points[j].x * points[i].y;
  }
  
  return Math.abs(area / 2) / 1000; // 转换为亩（假设1像素=1平方米，简化计算）
};

const getPolygonPoints = (points: Point[]): string => {
  // 将图片坐标转换为显示坐标
  return points.map(p => 
    `${p.x * scale.value + offsetX.value},${p.y * scale.value + offsetY.value}`
  ).join(' ');
};

const getPolylinePoints = (points: Point[]): string => {
  // 将图片坐标转换为显示坐标
  return points.map(p => 
    `${p.x * scale.value + offsetX.value},${p.y * scale.value + offsetY.value}`
  ).join(' ');
};

const selectPolygon = (index: number) => {
  selectedPolygonIndex.value = index;
  selectedPolygonInfo.value = polygons.value[index];
  
  // 计算弹窗位置
  const points = polygons.value[index].points;
  if (points.length > 0) {
    const firstPoint = points[0];
    popupPosition.value = {
      x: firstPoint.x * scale.value + offsetX.value,
      y: firstPoint.y * scale.value + offsetY.value
    };
  }
  
  emit('polygon-selected', polygons.value[index]);
};

const highlightPolygon = (index: number) => {
  // 高亮显示多边形
  // 这里可以添加高亮效果
};

const unhighlightPolygon = (index: number) => {
  // 取消高亮
  // 这里可以取消高亮效果
};

const selectPoint = (polygonIndex: number, pointIndex: number) => {
  // 选中多边形的顶点，可以用于编辑
  console.log(`选中多边形${polygonIndex}的点${pointIndex}`);
};

const closePopup = () => {
  selectedPolygonInfo.value = null;
  selectedPolygonIndex.value = -1;
};

const editPolygon = (index?: number) => {
  const polygonIndex = index !== undefined ? index : selectedPolygonIndex.value;
  if (polygonIndex < 0 || polygonIndex >= polygons.value.length) return;
  
  // 复制多边形数据到编辑表单
  const polygon = polygons.value[polygonIndex];
  Object.assign(editingPolygon, {
    ...polygon,
    estimatedDate: polygon.estimatedDate ? dayjs(polygon.estimatedDate) : null,
    plantDate: polygon.plantDate ? dayjs(polygon.plantDate) : null
  });
  
  editModalVisible.value = true;
};

const savePolygonInfo = () => {
  if (selectedPolygonIndex.value < 0) return;
  
  // 保存编辑后的多边形信息
  const polygon = polygons.value[selectedPolygonIndex.value];
  
  polygon.name = editingPolygon.name;
  polygon.status = editingPolygon.status;
  polygon.area = editingPolygon.area;
  polygon.manager = editingPolygon.manager;
  polygon.phone = editingPolygon.phone;
  polygon.expectedYield = editingPolygon.expectedYield;
  polygon.actualYield = editingPolygon.actualYield;
  polygon.progress = editingPolygon.progress;
  polygon.estimatedDate = editingPolygon.estimatedDate ? dayjs(editingPolygon.estimatedDate).format('YYYY-MM-DD') : '';
  polygon.soilType = editingPolygon.soilType;
  polygon.irrigation = editingPolygon.irrigation;
  polygon.variety = editingPolygon.variety;
  polygon.plantDate = editingPolygon.plantDate ? dayjs(editingPolygon.plantDate).format('YYYY-MM-DD') : '';
  
  // 更新颜色
  updatePolygonColor(polygon);
  
  editModalVisible.value = false;
  message.success('地块信息已更新');
};

const cancelEdit = () => {
  editModalVisible.value = false;
};

const deletePolygon = () => {
  if (selectedPolygonIndex.value < 0) return;
  
  polygons.value.splice(selectedPolygonIndex.value, 1);
  closePopup();
  message.success('地块已删除');
};

const savePolygons = () => {
  // 这里应该调用API保存多边形数据
  console.log('保存多边形数据:', polygons.value);
  
  // 模拟API调用
  setTimeout(() => {
    message.success('地块数据保存成功');
  }, 500);
  
  emit('polygons-updated', polygons.value);
};

const clearAllPolygons = () => {
  if (polygons.value.length === 0) return;
  
  polygons.value = [];
  currentPolygon.points = [];
  message.success('已清除所有地块');
};

const resetMapView = () => {
  scale.value = 1;
  offsetX.value = 0;
  offsetY.value = 0;
};

const handleSearch = () => {
  // 搜索功能
  if (!searchKeyword.value) return;
  
  // 在多边形中搜索匹配的地块
  const foundIndex = polygons.value.findIndex(p => 
    p.name.includes(searchKeyword.value) || 
    p.manager.includes(searchKeyword.value)
  );
  
  if (foundIndex >= 0) {
    selectPolygon(foundIndex);
    // 将地图中心移动到选中的多边形
    const points = polygons.value[foundIndex].points;
    if (points.length > 0) {
      const centerX = points.reduce((sum, p) => sum + p.x, 0) / points.length;
      const centerY = points.reduce((sum, p) => sum + p.y, 0) / points.length;
      
      const containerRect = mapContainer.value?.getBoundingClientRect();
      if (containerRect) {
        offsetX.value = containerRect.width / 2 - centerX * scale.value;
        offsetY.value = containerRect.height / 2 - centerY * scale.value;
      }
    }
  } else {
    message.warning('未找到匹配的地块');
  }
};

const redrawPolygons = () => {
  // 重绘所有多边形
  if (!canvas.value) return;
  
  const ctx = canvas.value.getContext('2d');
  if (!ctx) return;
  
  // 清空画布
  ctx.clearRect(0, 0, canvas.value.width, canvas.value.height);
  
  // 绘制所有已完成的多边形
  polygons.value.forEach(polygon => {
    ctx.beginPath();
    polygon.points.forEach((point, index) => {
      const x = point.x * scale.value + offsetX.value;
      const y = point.y * scale.value + offsetY.value;
      
      if (index === 0) {
        ctx.moveTo(x, y);
      } else {
        ctx.lineTo(x, y);
      }
    });
    ctx.closePath();
    
    ctx.fillStyle = polygon.color + '33'; // 添加透明度
    ctx.fill();
    ctx.strokeStyle = polygon.strokeColor;
    ctx.lineWidth = 2;
    ctx.stroke();
  });
  
  // 如果在绘制模式且有当前多边形，绘制当前多边形
  if (drawMode.value && currentPolygon.points.length > 0) {
    ctx.beginPath();
    currentPolygon.points.forEach((point, index) => {
      const x = point.x * scale.value + offsetX.value;
      const y = point.y * scale.value + offsetY.value;
      
      if (index === 0) {
        ctx.moveTo(x, y);
      } else {
        ctx.lineTo(x, y);
      }
    });
    
    if (currentPolygon.points.length > 2) {
      ctx.closePath();
      ctx.fillStyle = currentPolygon.color + '33'; // 添加透明度
      ctx.fill();
    }
    
    ctx.strokeStyle = currentPolygon.strokeColor;
    ctx.lineWidth = 2;
    ctx.stroke();
    
    // 绘制点
    currentPolygon.points.forEach(point => {
      const x = point.x * scale.value + offsetX.value;
      const y = point.y * scale.value + offsetY.value;
      
      ctx.beginPath();
      ctx.arc(x, y, 4, 0, 2 * Math.PI);
      ctx.fillStyle = currentPolygon.strokeColor;
      ctx.fill();
    });
  }
};

const getStatusText = (status: string) => {
  switch (status) {
    case 'completed':
      return '已完成';
    case 'harvesting':
      return '收割中';
    case 'pending':
      return '未开始';
    default:
      return '未知';
  }
};

// 地块关联相关方法
const openAssociationModal = () => {
  if (selectedPolygonIndex.value < 0) return;
  
  selectedPolygonForAssociation.value = polygons.value[selectedPolygonIndex.value];
  associationModalVisible.value = true;
};

const handleAssociationConfirm = (data: { polygonId: string, fieldData: any }) => {
  // 找到对应的多边形
  const polygonIndex = polygons.value.findIndex(p => p.id === data.polygonId);
  if (polygonIndex < 0) return;
  
  const polygon = polygons.value[polygonIndex];
  
  // 更新多边形信息，使用关联地块的信息
  polygon.fieldId = data.fieldData.id;
  polygon.fieldCode = data.fieldData.fieldCode;
  polygon.name = data.fieldData.fieldName;
  polygon.area = data.fieldData.area;
  polygon.status = data.fieldData.status;
  polygon.manager = data.fieldData.manager;
  polygon.phone = data.fieldData.phone;
  polygon.expectedYield = data.fieldData.expectedYield;
  polygon.actualYield = data.fieldData.actualYield;
  polygon.progress = data.fieldData.progress;
  polygon.estimatedDate = data.fieldData.estimatedDate;
  polygon.soilType = data.fieldData.soilType;
  polygon.irrigation = data.fieldData.irrigation;
  polygon.variety = data.fieldData.variety;
  polygon.plantDate = data.fieldData.plantDate;
  polygon.isAssociated = true;
  
  // 更新颜色
  updatePolygonColor(polygon);
  
  // 关闭关联弹窗
  associationModalVisible.value = false;
  
  // 如果当前选中的是这个多边形，更新弹窗信息
  if (selectedPolygonIndex.value === polygonIndex) {
    selectedPolygonInfo.value = polygon;
  }
  
  message.success(`地块已关联到 ${data.fieldData.fieldCode} ${data.fieldData.fieldName}`);
};
</script>

<style scoped>
.aerial-map-container {
  position: relative;
  width: 100%;
  height: 520px;
  overflow: hidden;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
}

.map-tools {
  position: absolute;
  top: 10px;
  left: 10px;
  z-index: 100;
  background: rgba(255, 255, 255, 0.9);
  padding: 8px;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.map-canvas-container {
  position: relative;
  width: 100%;
  height: 100%;
  cursor: grab;
}

.map-canvas-container:active {
  cursor: grabbing;
}

/* 只读模式下的样式 */
.aerial-map-container.readonly .map-canvas-container {
  cursor: default;
}

.aerial-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: contain;
  user-select: none;
  -webkit-user-drag: none;
}

.polygon-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.polygon-svg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.polygon-shape {
  cursor: pointer;
  transition: fill-opacity 0.2s;
}

.polygon-shape:hover {
  fill-opacity: 0.5 !important;
}

.polygon-point {
  cursor: pointer;
  transition: r 0.2s;
}

.polygon-point:hover {
  r: 8;
}

.polygon-info-popup {
  position: absolute;
  background: white;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  padding: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  min-width: 250px;
  z-index: 1000;
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.popup-header h4 {
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 16px;
  cursor: pointer;
  color: #999;
}

.close-btn:hover {
  color: #333;
}

.popup-content {
  margin-bottom: 12px;
}

.popup-content p {
  margin: 4px 0;
}

.popup-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.map-legend {
  position: absolute;
  bottom: 10px;
  right: 10px;
  background: rgba(255, 255, 255, 0.9);
  padding: 8px;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.legend-item {
  display: flex;
  align-items: center;
  margin-bottom: 4px;
}

.legend-item:last-child {
  margin-bottom: 0;
}

.legend-marker {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  margin-right: 8px;
}

.legend-marker.harvesting {
  background-color: #faad14;
}

.legend-marker.completed {
  background-color: #52c41a;
}

.legend-marker.pending {
  background-color: #ff4d4f;
}
</style>
