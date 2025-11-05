<template>
  <div class="harvest-page">
    <div class="selection-timeline-row">
      <a-row :gutter="16" align="middle">
        <a-col :span="12">
          <div class="page-header">
            <div class="page-title">
              <Icon icon="ant-design:home-outlined" />
              地块收获监测
            </div>
            <div class="page-description">
              实时监控收割进度、产量统计及农机作业状态
            </div>
          </div>
        </a-col>

      </a-row>
    </div>

    <!-- 数据概览卡片 -->
    <a-row :gutter="16" class="mb-6">
      <a-col :xs="24" :sm="12" :md="12" :lg="6" :xl="6">
        <a-card class="stat-card" :bordered="false">
          <div class="stat-content">
            <div class="stat-info">
              <p class="stat-label">已收割面积</p>
              <h3 class="stat-value">{{ harvestStats.harvestedArea.toLocaleString() }} <span class="stat-unit">亩</span></h3>
              <p class="stat-trend" :class="harvestStats.harvestedAreaTrend > 0 ? 'stat-up' : 'stat-down'">
                <Icon :icon="harvestStats.harvestedAreaTrend > 0 ? 'ant-design:arrow-up-outlined' : 'ant-design:arrow-down-outlined'" />
                {{ Math.abs(harvestStats.harvestedAreaTrend) }}% <span class="stat-compare">较昨日</span>
              </p>
            </div>
            <div class="stat-icon stat-icon-primary">
              <Icon icon="ant-design:check-square-outlined" />
            </div>
          </div>
        </a-card>
      </a-col>
      
      <a-col :xs="24" :sm="12" :md="12" :lg="6" :xl="6">
        <a-card class="stat-card" :bordered="false">
          <div class="stat-content">
            <div class="stat-info">
              <p class="stat-label">未收割面积</p>
              <h3 class="stat-value">{{ harvestStats.unharvestedArea.toLocaleString() }} <span class="stat-unit">亩</span></h3>
              <p class="stat-trend" :class="harvestStats.unharvestedAreaTrend > 0 ? 'stat-up' : 'stat-down'">
                <Icon :icon="harvestStats.unharvestedAreaTrend > 0 ? 'ant-design:arrow-up-outlined' : 'ant-design:arrow-down-outlined'" />
                {{ Math.abs(harvestStats.unharvestedAreaTrend) }}% <span class="stat-compare">较昨日</span>
              </p>
            </div>
            <div class="stat-icon stat-icon-warning">
              <Icon icon="ant-design:border-outlined" />
            </div>
          </div>
        </a-card>
      </a-col>
      
      <a-col :xs="24" :sm="12" :md="12" :lg="6" :xl="6">
        <a-card class="stat-card" :bordered="false">
          <div class="stat-content">
            <div class="stat-info">
              <p class="stat-label">累计产量</p>
              <h3 class="stat-value">{{ harvestStats.totalYield.toLocaleString() }} <span class="stat-unit">吨</span></h3>
              <p class="stat-trend" :class="harvestStats.totalYieldTrend > 0 ? 'stat-up' : 'stat-down'">
                <Icon :icon="harvestStats.totalYieldTrend > 0 ? 'ant-design:arrow-up-outlined' : 'ant-design:arrow-down-outlined'" />
                {{ Math.abs(harvestStats.totalYieldTrend) }}% <span class="stat-compare">较昨日</span>
              </p>
            </div>
            <div class="stat-icon stat-icon-info">
              <Icon icon="ant-design:bar-chart-outlined" />
            </div>
          </div>
        </a-card>
      </a-col>
      
      <a-col :xs="24" :sm="12" :md="12" :lg="6" :xl="6">
        <a-card class="stat-card" :bordered="false">
          <div class="stat-content">
            <div class="stat-info">
              <p class="stat-label">作业农机数</p>
              <h3 class="stat-value">{{ harvestStats.machineCount }} <span class="stat-unit">台</span></h3>
              <p class="stat-trend stat-normal">
                <Icon icon="ant-design:check-circle-outlined" />
                全部正常运行
              </p>
            </div>
            <div class="stat-icon stat-icon-dark">
              <Icon icon="ant-design:car-outlined" />
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 地图和实时监控面板 -->
    <a-row :gutter="16" class="mb-6">
      <a-col :xs="24" :lg="16">
        <a-card class="map-card" :bordered="false">
          <template #title>
            <div class="map-title">
              <Icon icon="ant-design:environment-outlined" />
              收割作业地图
            </div>
          </template>
          <div class="map-container">
              <!-- 使用航拍图组件 -->
              <AerialMap 
                :aerial-image-url="aerialImageUrl"
                :initial-polygons="mapPolygons"
                @polygons-updated="handlePolygonsUpdated"
                @polygon-selected="handlePolygonSelected"
              />
            </div>
        </a-card>
      </a-col>
      
      <a-col :xs="24" :lg="8">
        <!-- 右侧信息面板 -->
        <div class="info-panel">
          <!-- 收割机状态 -->
          <a-card class="harvester-status-card" :bordered="false">
            <template #title>
              <div class="card-title">
                <Icon icon="ant-design:car-outlined" />
                收割机实时状态
              </div>
            </template>
            <div class="harvester-list">
                <div v-for="(harvester, index) in harvesterStatus" :key="index" class="harvester-item" :class="harvester.status">
                  <div class="harvester-icon">
                    <Icon icon="ant-design:car-outlined" />
                  </div>
                  <div class="harvester-info">
                    <div class="harvester-header">
                      <h4 class="harvester-name">{{ harvester.name }}</h4>
                      <span class="status-badge" :class="harvester.status">{{ harvester.status === 'working' ? '作业中' : harvester.status === 'idle' ? '待命' : '维护中' }}</span>
                    </div>
                    <p class="harvester-location">{{ harvester.location }}</p>
                  </div>
                </div>
              </div>
          </a-card>
          
          <!-- 产量统计 -->
          <a-card class="yield-chart-card" :bordered="false">
            <template #title>
              <div class="card-title">
                <Icon icon="ant-design:bar-chart-outlined" />
                今日产量对比
              </div>
            </template>
            <div class="chart-container">
              <BarMulti 
                :chartData="chartDataForBarMulti"
                :width="'100%'"
                :height="'220px'"
                :seriesColor="seriesColor"
                :option="chartOption"
              />
            </div>
          </a-card>
        </div>
      </a-col>
    </a-row>

    <a-card class="table-card" :bordered="false">
      
      <BasicTable @register="registerTable" :searchInfo="searchInfo">

        <template #toolbar>
          <a-button type="primary" @click="handleCreate">
            <Icon icon="ant-design:plus-outlined" />
            新增地块收获
          </a-button>
          <a-button @click="handleExport">
            <Icon icon="ant-design:download-outlined" />
            导出
          </a-button>
          <a-upload
            :file-list="fileList"
            :before-upload="beforeUpload"
            @remove="handleRemove"
          >
            <a-button>
              <Icon icon="ant-design:upload-outlined" />
              导入
            </a-button>
          </a-upload>
        </template>
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'action'">
            <TableAction
              :actions="[
                {
                  icon: 'ant-design:edit-outlined',
                  tooltip: '编辑',
                  onClick: handleEdit.bind(null, record),
                },
                {
                  icon: 'ant-design:delete-outlined',
                  color: 'error',
                  tooltip: '删除',
                  popConfirm: {
                    title: '是否确认删除',
                    confirm: handleDelete.bind(null, record),
                  },
                },
              ]"
            />
          </template>
        </template>
      </BasicTable>
    </a-card>

    <HarvestModal @register="registerModal" @success="handleSuccess" />
    
    <!-- 地块详情弹窗 -->
    <a-modal
      v-model:open="detailModalVisible"
      title="地块详情"
      width="800px"
      :footer="null"
    >
      <div v-if="selectedField" class="field-detail">
        <a-descriptions :column="2" bordered>
          <a-descriptions-item label="地块编号">{{ selectedField.id }}</a-descriptions-item>
          <a-descriptions-item label="地块名称">{{ selectedField.title }}</a-descriptions-item>
          <a-descriptions-item label="面积">{{ selectedField.area }} 亩</a-descriptions-item>
          <a-descriptions-item label="状态">
            <a-tag :color="getStatusColor(selectedField.status)">{{ getStatusText(selectedField.status) }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="负责人">{{ selectedField.manager }}</a-descriptions-item>
          <a-descriptions-item label="联系电话">{{ selectedField.phone }}</a-descriptions-item>
          <a-descriptions-item label="预计产量">{{ selectedField.expectedYield }} 吨</a-descriptions-item>
          <a-descriptions-item label="实际产量">{{ selectedField.actualYield || '待收割' }} 吨</a-descriptions-item>
          <a-descriptions-item label="收割进度">{{ selectedField.progress }}%</a-descriptions-item>
          <a-descriptions-item label="预计完成时间">{{ selectedField.estimatedDate || '待定' }}</a-descriptions-item>
          <a-descriptions-item label="土壤类型">{{ selectedField.soilType || '壤土' }}</a-descriptions-item>
          <a-descriptions-item label="灌溉方式">{{ selectedField.irrigation || '滴灌' }}</a-descriptions-item>
          <a-descriptions-item label="种植品种">{{ selectedField.variety || '优质油菜' }}</a-descriptions-item>
          <a-descriptions-item label="种植日期">{{ selectedField.plantDate || '2023-09-15' }}</a-descriptions-item>
        </a-descriptions>
        
        <div class="mt-4">
          <h4>收割作业记录</h4>
          <a-table 
            :dataSource="selectedField.harvestRecords || []" 
            :columns="harvestRecordColumns" 
            :pagination="false"
            size="small"
          />
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { reactive, ref, nextTick, computed, onMounted, onUnmounted, unref } from 'vue';
import { BasicTable, useTable, TableAction } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useMessage } from '/@/hooks/web/useMessage';
import { Icon } from '/@/components/Icon';
import { columns, searchFormSchema } from './harvest.data';
import { getHarvestList, deleteHarvest, exportHarvest, importHarvest } from './harvest.api';
import HarvestModal from './HarvestModal.vue';
import BarMulti from '/@/components/chart/BarMulti.vue';
import { useScript } from '/@/hooks/web/useScript';
import AerialMap from '/@/components/AerialMap/index.vue';

// 地图数据类型定义
interface MapPoint {
  lng: number;
  lat: number;
}

interface MapMarker {
  id: number;
  title: string;
  status: 'harvesting' | 'completed' | 'pending';
  lng: number;
  lat: number;
  area?: number;
  manager?: string;
  phone?: string;
  expectedYield?: number;
  actualYield?: number;
  progress?: number;
  estimatedDate?: string;
  soilType?: string;
  irrigation?: string;
  variety?: string;
  plantDate?: string;
  harvestRecords?: any[];
}

interface FieldBoundary {
  id: number;
  name: string;
  status: 'harvesting' | 'completed' | 'pending';
  points: MapPoint[]; // 地块边界的坐标点数组
  center: MapPoint; // 地块中心点
  area?: number; // 地块面积
  manager?: string;
  phone?: string;
  expectedYield?: number;
  actualYield?: number;
  progress?: number;
  estimatedDate?: string;
  soilType?: string;
  irrigation?: string;
  variety?: string;
  plantDate?: string;
  harvestRecords?: any[];
}

interface MapData {
  center: MapPoint;
  zoom: number;
  markers: MapMarker[];
  boundaries?: FieldBoundary[]; // 添加地块边界数据
}

const { createMessage } = useMessage();
const [registerModal, { openModal }] = useModal();
const searchInfo = reactive<Recordable>({});
const fileList = ref<any[]>([]);

// 加载状态
const loading = ref(false);

// 动态数据
const harvestStats = ref({
  harvestedArea: 0,
  unharvestedArea: 0,
  totalYield: 0,
  machineCount: 0,
  harvestedAreaTrend: 0,
  unharvestedAreaTrend: 0,
  totalYieldTrend: 0,
});

const harvesterStatus = ref([
  {
    id: 1,
    name: '收割机#1',
    status: 'working',
    location: '地块A-3区域',
  },
  {
    id: 2,
    name: '收割机#2',
    status: 'idle',
    location: '维修区',
  },
  {
    id: 3,
    name: '收割机#3',
    status: 'working',
    location: '地块B-1区域',
  },
  {
    id: 4,
    name: '收割机#4',
    status: 'idle',
    location: '待命区',
  },
]);

const yieldChartData = ref([
  { fieldName: 'A区-03号', actual: 320, expected: 350 },
  { fieldName: 'B区-07号', actual: 410, expected: 400 },
  { fieldName: 'C区-02号', actual: 380, expected: 380 },
  { fieldName: 'D区-05号', actual: 290, expected: 320 },
  { fieldName: 'E区-01号', actual: 450, expected: 420 },
]);

const mapData = ref<MapData>({
  center: { lng: 112.616279, lat: 31.176021 },
  zoom: 12,
  markers: [],
  boundaries: [], // 初始化地块边界数据
});

// 图表颜色配置
const seriesColor = [
  { type: '实际产量', color: 'rgba(22, 93, 255, 0.7)' },
  { type: '预计产量', color: 'rgba(22, 93, 255, 0.2)' }
];

// 图表配置选项
const chartOption = {
  grid: {
    top: 40,
    left: '3%',
    right: '4%',
    bottom: '15%', // 增加底部边距，确保标签不被裁剪
    containLabel: true
  },
  legend: {
    top: 10
  },
  xAxis: {
    axisLabel: {
      interval: 0, // 显示所有标签
      rotate: 30, // 旋转30度，避免标签重叠
      fontSize: 12
    }
  }
};

// 转换数据格式以适配BarMulti组件
const chartDataForBarMulti = computed(() => {
  const result = [];
  yieldChartData.value.forEach(item => {
    result.push({
      name: item.fieldName,
      type: '实际产量',
      value: item.actual
    });
    result.push({
      name: item.fieldName,
      type: '预计产量',
      value: item.expected
    });
  });
  return result;
});



  // 加载收获统计数据
  async function loadHarvestStats() {
    try {
      // 这里应该调用实际的API获取统计数据
      // const stats = await getHarvestStats();
      // harvestStats.value = stats;
      
      // 模拟数据
      harvestStats.value = {
        harvestedArea: 2456,
        unharvestedArea: 872,
        totalYield: 1842,
        machineCount: 8,
        harvestedAreaTrend: 12.5,
        unharvestedAreaTrend: -8.3,
        totalYieldTrend: 15.2,
      };
    } catch (error) {
      console.error('加载收获统计数据失败:', error);
    }
  }

// 加载收割机状态数据
async function loadHarvesterStatus() {
  try {
    // 这里应该调用实际的API获取收割机状态
    // const status = await getHarvesterStatus();
    // harvesterStatus.value = status;
    
    // 模拟数据
    harvesterStatus.value = [
      {
        id: 1,
        name: '收割机#1',
        status: 'working',
        location: '地块A-3区域',
      },
      {
        id: 2,
        name: '收割机#2',
        status: 'working',
        location: '地块B-1区域',
      },
      {
        id: 3,
        name: '收割机#3',
        status: 'idle',
        location: '维修区',
      },
      {
        id: 4,
        name: '收割机#4',
        status: 'working',
        location: '地块C-2区域',
      },
    ];
  } catch (error) {
    console.error('加载收割机状态失败:', error);
  }
}

// 加载产量图表数据
async function loadYieldChartData() {
  try {
    // 这里应该调用实际的API获取图表数据
    // const chartData = await getYieldChartData();
    // yieldChartData.value = chartData;
    
    // 模拟数据
    yieldChartData.value = [
      { fieldName: 'A区-03号', actual: 320, expected: 350 },
      { fieldName: 'B区-07号', actual: 410, expected: 400 },
      { fieldName: 'C区-02号', actual: 380, expected: 380 },
      { fieldName: 'D区-05号', actual: 290, expected: 320 },
      { fieldName: 'E区-01号', actual: 450, expected: 420 },
    ];
  } catch (error) {
    console.error('加载产量图表数据失败:', error);
  }
}

// 地图相关
  const mapRef = ref<HTMLDivElement | null>(null);
  let markers: any[] = [];
  
  // 地图工具栏相关
  const searchKeyword = ref('');

// 地块详情弹窗相关
const detailModalVisible = ref(false);
const selectedField = ref<any>(null);

// 航拍图相关
const aerialImageUrl = ref('/images/aerial-map.jpg'); // 航拍图URL
const mapPolygons = ref<any[]>([]); // 地图多边形数据

// 收割记录表格列
const harvestRecordColumns = [
  { title: '作业日期', dataIndex: 'date', width: 100 },
  { title: '作业人员', dataIndex: 'operator', width: 100 },
  { title: '作业时长', dataIndex: 'duration', width: 80 },
  { title: '作业面积', dataIndex: 'area', width: 80 },
  { title: '产量', dataIndex: 'yield', width: 80 },
  { title: '备注', dataIndex: 'remark' },
];

// 加载地图数据
const loadMapData = async () => {
  console.log('开始加载地图数据...');
  try {
    // 模拟从API获取地图数据
    const response = await new Promise(resolve => {
      setTimeout(() => {
        resolve({
          data: {
            center: { lng: 112.616279, lat: 31.176021 },
            zoom: 15,
            markers: [
              { id: 1, title: '东区地块A', status: 'harvesting', lng: 112.626279, lat: 31.186021 },
              { id: 2, title: '东区地块B', status: 'completed', lng: 112.636279, lat: 31.176021 },
              { id: 3, title: '西区地块C', status: 'pending', lng: 112.606279, lat: 31.166021 },
              { id: 4, title: '西区地块D', status: 'harvesting', lng: 112.616279, lat: 31.166021 },
              { id: 5, title: '南区地块E', status: 'completed', lng: 112.616279, lat: 31.156021 },
            ],
            // 添加地块边界数据
            boundaries: [
              {
                id: 1,
                name: '东区地块A',
                status: 'harvesting',
                center: { lng: 112.626279, lat: 31.186021 },
                points: [
                  { lng: 112.622279, lat: 31.189021 },
                  { lng: 112.630279, lat: 31.189021 },
                  { lng: 112.630279, lat: 31.183021 },
                  { lng: 112.622279, lat: 31.183021 },
                  { lng: 112.622279, lat: 31.189021 }
                ],
                area: 120,
                manager: '张三',
                phone: '13800138001',
                expectedYield: 60,
                actualYield: 35,
                progress: 58,
                estimatedDate: '2023-11-15',
                soilType: '壤土',
                irrigation: '滴灌',
                variety: '优质油菜',
                plantDate: '2023-09-15'
              },
              {
                id: 2,
                name: '东区地块B',
                status: 'completed',
                center: { lng: 112.636279, lat: 31.176021 },
                points: [
                  { lng: 112.632279, lat: 31.179021 },
                  { lng: 112.640279, lat: 31.179021 },
                  { lng: 112.640279, lat: 31.173021 },
                  { lng: 112.632279, lat: 31.173021 },
                  { lng: 112.632279, lat: 31.179021 }
                ],
                area: 95,
                manager: '李四',
                phone: '13800138002',
                expectedYield: 48,
                actualYield: 52,
                progress: 100,
                estimatedDate: '2023-11-10',
                soilType: '砂壤土',
                irrigation: '喷灌',
                variety: '高产油菜',
                plantDate: '2023-09-10'
              },
              {
                id: 3,
                name: '西区地块C',
                status: 'pending',
                center: { lng: 112.606279, lat: 31.166021 },
                points: [
                  { lng: 112.602279, lat: 31.169021 },
                  { lng: 112.610279, lat: 31.169021 },
                  { lng: 112.610279, lat: 31.163021 },
                  { lng: 112.602279, lat: 31.163021 },
                  { lng: 112.602279, lat: 31.169021 }
                ],
                area: 110,
                manager: '王五',
                phone: '13800138003',
                expectedYield: 55,
                actualYield: 0,
                progress: 0,
                estimatedDate: '2023-11-20',
                soilType: '黏土',
                irrigation: '漫灌',
                variety: '耐旱油菜',
                plantDate: '2023-09-20'
              },
              {
                id: 4,
                name: '西区地块D',
                status: 'harvesting',
                center: { lng: 112.616279, lat: 31.166021 },
                points: [
                  { lng: 112.612279, lat: 31.169021 },
                  { lng: 112.620279, lat: 31.169021 },
                  { lng: 112.620279, lat: 31.163021 },
                  { lng: 112.612279, lat: 31.163021 },
                  { lng: 112.612279, lat: 31.169021 }
                ],
                area: 85,
                manager: '赵六',
                phone: '13800138004',
                expectedYield: 42,
                actualYield: 20,
                progress: 48,
                estimatedDate: '2023-11-18',
                soilType: '壤土',
                irrigation: '滴灌',
                variety: '优质油菜',
                plantDate: '2023-09-12'
              },
              {
                id: 5,
                name: '南区地块E',
                status: 'completed',
                center: { lng: 112.616279, lat: 31.156021 },
                points: [
                  { lng: 112.612279, lat: 31.159021 },
                  { lng: 112.620279, lat: 31.159021 },
                  { lng: 112.620279, lat: 31.153021 },
                  { lng: 112.612279, lat: 31.153021 },
                  { lng: 112.612279, lat: 31.159021 }
                ],
                area: 100,
                manager: '钱七',
                phone: '13800138005',
                expectedYield: 50,
                actualYield: 53,
                progress: 100,
                estimatedDate: '2023-11-08',
                soilType: '砂土',
                irrigation: '喷灌',
                variety: '早熟油菜',
                plantDate: '2023-09-05'
              }
            ]
          }
        });
      }, 500);
    });
    
    mapData.value = response.data as any;
    console.log('地图数据加载完成:', mapData.value);
    
    // 将边界数据转换为航拍图组件需要的格式
    if (mapData.value.boundaries) {
      mapPolygons.value = mapData.value.boundaries.map(boundary => ({
        id: boundary.id,
        name: boundary.name,
        points: boundary.points.map(point => [point.lng, point.lat]),
        status: boundary.status,
        area: boundary.area,
        manager: boundary.manager,
        phone: boundary.phone,
        expectedYield: boundary.expectedYield,
        actualYield: boundary.actualYield,
        progress: boundary.progress,
        estimatedDate: boundary.estimatedDate,
        soilType: boundary.soilType,
        irrigation: boundary.irrigation,
        variety: boundary.variety,
        plantDate: boundary.plantDate
      }));
    }
  } catch (error) {
    console.error('加载地图数据失败:', error);
  }
};


  
  // 获取状态颜色
  const getStatusColor = (status: string) => {
    switch (status) {
      case 'completed':
        return 'success';
      case 'harvesting':
        return 'processing';
      case 'pending':
        return 'default';
      default:
        return 'default';
    }
  };
  
  // 获取状态文本
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
  
  // 处理地块详情
  const handleFieldDetail = (fieldId: string) => {
    // 根据ID查找地块数据
    const field = mapData.value.markers.find(item => item.id === fieldId);
    
    if (field) {
      // 补充地块详细信息
      selectedField.value = {
        ...field,
        manager: '张' + Math.floor(Math.random() * 1000), // 模拟负责人
        phone: '138' + Math.floor(Math.random() * 100000000), // 模拟电话
        expectedYield: (Math.random() * 50 + 20).toFixed(1), // 模拟预计产量
        actualYield: field.status === 'completed' ? (Math.random() * 40 + 15).toFixed(1) : null, // 已完成才有实际产量
        progress: field.status === 'completed' ? 100 : field.status === 'harvesting' ? Math.floor(Math.random() * 80) + 20 : 0,
        estimatedDate: field.status === 'pending' ? '2023-10-20' : field.status === 'harvesting' ? '2023-10-15' : '2023-10-10',
        soilType: ['壤土', '砂壤土', '黏土'][Math.floor(Math.random() * 3)],
        irrigation: ['滴灌', '喷灌', '漫灌'][Math.floor(Math.random() * 3)],
        variety: ['优质油菜', '高产油菜', '抗病油菜'][Math.floor(Math.random() * 3)],
        plantDate: '2023-09-15',
        harvestRecords: field.status === 'completed' ? [
          { date: '2023-10-10', operator: '王师傅', duration: '4小时', area: '25亩', yield: '12.5吨', remark: '天气良好' },
          { date: '2023-10-11', operator: '李师傅', duration: '3.5小时', area: '20亩', yield: '10.2吨', remark: '设备正常' }
        ] : field.status === 'harvesting' ? [
          { date: '2023-10-12', operator: '王师傅', duration: '2小时', area: '15亩', yield: '7.5吨', remark: '进行中' }
        ] : []
      };
      
      // 打开详情弹窗
      detailModalVisible.value = true;
    }
  };
  


const [registerTable, { reload, getSelectRows, dataSource }] = useTable({
  api: getHarvestList,
  title: "地块收获记录",
  columns,
  formConfig: {
    labelWidth: 120,
    schemas: searchFormSchema,
    autoSubmitOnEnter: true,
  },
  useSearchForm: true,
  showTableSetting: false,
  bordered: true,
  showIndexColumn: false,
});



/**
 * 加载所有数据
 */
async function loadAllData() {
  try {
    loading.value = true;
    
    // 并行加载所有数据
    await Promise.all([
      loadHarvestData(),
      loadHarvestStats(),
      loadHarvesterStatus(),
      loadYieldChartData(),
      loadMapData(),
    ]);
  } catch (error) {
    console.error('加载数据失败:', error);
    createMessage.error('加载数据失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}

// 页面挂载时初始化数据
onMounted(() => {
  loadAllData();
});

// 页面卸载时清理资源
onUnmounted(() => {
  // 清理地图相关资源
  markers = [];
});

/**
 * 加载收获数据
 */
async function loadHarvestData() {
  try {
    loading.value = true;
    // 重新加载数据
    await reload();
  } catch (error) {
    console.error('加载收获数据失败:', error);
    createMessage.error('加载收获数据失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}

function handleCreate() {
  openModal(true, {
    isUpdate: false,
  });
}

function handleEdit(record: Recordable) {
  openModal(true, {
    record,
    isUpdate: true,
  });
}

async function handleDelete(record: Recordable) {
  await deleteHarvest(record.id);
  createMessage.success('删除成功');
  reload();
}

function handleSuccess() {
  createMessage.success('操作成功');
  reload();
}

async function handleExport() {
  const data = await getHarvestList(searchInfo);
  exportHarvest(data);
}

function handleRemove() {
  fileList.value = [];
}

function beforeUpload(file) {
  fileList.value = [...fileList.value, file];
  return false;
}

async function handleImport() {
  if (fileList.value.length === 0) {
    createMessage.warning('请选择要导入的文件');
    return;
  }
  
  const formData = new FormData();
  fileList.value.forEach((file) => {
    formData.append('file', file);
  });
  
  try {
    await importHarvest(formData);
    createMessage.success('导入成功');
    reload();
    fileList.value = [];
  } catch (error) {
    createMessage.error('导入失败');
  }
}

// 航拍图相关方法
function handlePolygonsUpdated(polygons: any[]) {
  // 处理多边形更新事件
  mapPolygons.value = polygons;
  // 这里可以添加保存到后端的逻辑
  console.log('多边形已更新:', polygons);
}

function handlePolygonSelected(polygon: any) {
  // 处理多边形选中事件
  if (polygon) {
    selectedField.value = polygon;
    detailModalVisible.value = true;
  }
}


</script>

<style lang="less" scoped>
.harvest-page {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 64px);
}

.page-header {
  margin-bottom: 24px;
  
  .page-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 20px;
    font-weight: 600;
    color: #262626;
    margin-bottom: 8px;
  }
  
  .page-description {
    color: #8c8c8c;
    font-size: 14px;
  }
}

.selection-timeline-row {
  margin-bottom: 24px;
  
  // 确保两个卡片在同一行显示
  .ant-col {
    display: flex;
    
    .selection-card, .timeline-card {
      width: 100%;
      height: 100%;
      
      :deep(.ant-card-head) {
        background-color: #fafafa;
        border-bottom: 1px solid #f0f0f0;
        
        .ant-card-head-title {
          padding: 12px 16px;
          font-weight: 500;
        }
      }
      
      :deep(.ant-card-body) {
        padding: 16px;
      }
    }
  }
}

// 数据概览卡片样式
.stat-card {
  margin-bottom: 16px;
  transition: all 0.3s ease;
  
  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
  }
  
  :deep(.ant-card-body) {
    padding: 20px;
  }
  
  .stat-content {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    
    .stat-info {
      flex: 1;
      
      .stat-label {
        color: #8c8c8c;
        font-size: 14px;
        margin-bottom: 8px;
      }
      
      .stat-value {
        font-size: 24px;
        font-weight: 700;
        margin: 8px 0;
        color: #262626;
        
        .stat-unit {
          font-size: 14px;
          font-weight: 400;
          color: #8c8c8c;
          margin-left: 4px;
        }
      }
      
      .stat-trend {
        display: flex;
        align-items: center;
        font-size: 14px;
        margin-top: 8px;
        
        .stat-compare {
          color: #8c8c8c;
          margin-left: 4px;
        }
        
        &.stat-up {
          color: #52c41a;
        }
        
        &.stat-down {
          color: #ff4d4f;
        }
        
        &.stat-normal {
          color: #52c41a;
        }
      }
    }
    
    .stat-icon {
      width: 48px;
      height: 48px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;
      
      &.stat-icon-primary {
        background-color: rgba(24, 144, 255, 0.1);
        color: #1890ff;
      }
      
      &.stat-icon-warning {
        background-color: rgba(250, 173, 20, 0.1);
        color: #faad14;
      }
      
      &.stat-icon-info {
        background-color: rgba(82, 196, 26, 0.1);
        color: #52c41a;
      }
      
      &.stat-icon-dark {
        background-color: rgba(38, 38, 38, 0.1);
        color: #262626;
      }
    }
  }
}



.table-card {
  margin-top: 16px;
  
  .table-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 500;
  }
}

.empty-card,
.loading-card {
  margin-top: 16px;
  text-align: center;
}

// 地图和实时监控面板样式
.map-card {
  height: 600px;
  
  .map-container {
      position: relative;
      height: 520px;
      border-radius: 8px;
      overflow: hidden;
      background-color: #f5f5f5;
      
      .map-tools {
        position: absolute;
        top: 10px;
        left: 10px;
        z-index: 1000;
        background: rgba(255, 255, 255, 0.9);
        padding: 8px;
        border-radius: 6px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
      }
    }
}

.map-legend {
  position: absolute;
  bottom: 16px;
  right: 16px;
  background: rgba(255, 255, 255, 0.9);
  padding: 12px;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  z-index: 999;
}

.legend-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
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
  background-color: #ff4d4f;
}

.legend-marker.completed {
  background-color: #52c41a;
}

.legend-marker.pending {
  background-color: #1890ff;
}

.info-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 600px;
  
  .harvester-status-card {
    flex: 1;
    overflow: hidden;
    
    :deep(.ant-card-body) {
      padding: 16px;
      height: calc(100% - 57px);
      overflow-y: auto;
    }
    
    .harvester-list {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }
    
    .harvester-item {
      display: flex;
      gap: 12px;
      padding: 12px;
      border-radius: 8px;
      border: 1px solid #f0f0f0;
      
      &.working {
        background-color: rgba(24, 144, 255, 0.05);
        border-color: rgba(24, 144, 255, 0.2);
        
        .harvester-icon {
          background-color: rgba(24, 144, 255, 0.1);
          color: #1890ff;
        }
        
        .status-badge {
          background-color: rgba(82, 196, 26, 0.1);
          color: #52c41a;
        }
      }
      
      &.idle {
        background-color: #f5f5f5;
        border-color: #d9d9d9;
        
        .harvester-icon {
          background-color: #f0f0f0;
          color: #8c8c8c;
        }
        
        .status-badge {
          background-color: #f0f0f0;
          color: #8c8c8c;
        }
      }
      
      .harvester-icon {
        width: 40px;
        height: 40px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 20px;
      }
      
      .harvester-info {
        flex: 1;
        min-width: 0;
        
        .harvester-header {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          margin-bottom: 4px;
          
          .harvester-name {
            margin: 0;
            font-size: 14px;
            font-weight: 500;
          }
          
          .status-badge {
            padding: 2px 8px;
            border-radius: 10px;
            font-size: 12px;
            white-space: nowrap;
          }
        }
        
        .harvester-location {
          margin: 0 0 8px;
          font-size: 12px;
          color: #8c8c8c;
        }
      }
    }
  }
  
  .yield-chart-card {
    height: 280px;
    
    .chart-container {
      height: 220px;
    }
  }
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

// 响应式布局
@media (max-width: 1200px) {
  .stat-card {
    .stat-content {
      .stat-info {
        .stat-value {
          font-size: 22px;
        }
      }
      
      .stat-icon {
        width: 44px;
        height: 44px;
        font-size: 22px;
      }
    }
  }
  
  .map-card {
    height: 500px;
    
    .map-container {
      height: 420px;
    }
  }
  
  .info-panel {
    height: 500px;
    
    .harvester-status-card {
      .harvester-item {
        padding: 10px;
        
        .harvester-icon {
          width: 36px;
          height: 36px;
          font-size: 18px;
        }
        
        .harvester-info {
          .harvester-header {
            .harvester-name {
              font-size: 13px;
            }
            
            .status-badge {
              font-size: 11px;
            }
          }
          
          .harvester-location {
            font-size: 11px;
          }
        }
      }
    }
    
    .yield-chart-card {
      height: 240px;
      
      .chart-container {
        height: 180px;
      }
    }
  }
}

@media (max-width: 768px) {
  .harvest-page {
    padding: 16px;
  }
  
  .page-header {
    .page-title {
      font-size: 18px;
    }
  }
  
  .stat-card {
    .stat-content {
      flex-direction: column;
      align-items: flex-start;
      
      .stat-info {
        width: 100%;
        margin-bottom: 12px;
        
        .stat-value {
          font-size: 20px;
        }
      }
      
      .stat-icon {
        align-self: flex-end;
        width: 40px;
        height: 40px;
        font-size: 20px;
      }
    }
  }
  
  .map-card {
    height: 300px;
    margin-bottom: 16px;
    
    .map-container {
      height: 240px;
      
      .map-placeholder {
        .map-icon {
          font-size: 36px;
        }
        
        p {
          font-size: 14px;
        }
      }
    }
  }
  
  .info-panel {
    height: auto;
    
    .harvester-status-card {
      height: auto;
      margin-bottom: 16px;
      
      :deep(.ant-card-body) {
        height: auto;
      }
      
      .harvester-item {
        padding: 8px;
        
        .harvester-icon {
          width: 32px;
          height: 32px;
          font-size: 16px;
        }
        
        .harvester-info {
          .harvester-header {
            .harvester-name {
              font-size: 12px;
            }
            
            .status-badge {
              font-size: 10px;
            }
          }
          
          .harvester-location {
            font-size: 10px;
          }
        }
      }
    }
    
    .yield-chart-card {
      height: 160px;
      
      .chart-container {
        height: 100px;
        
        .chart-placeholder {
          .chart-icon {
            font-size: 24px;
          }
          
          p {
            font-size: 12px;
          }
        }
      }
    }
  }
}
</style>
