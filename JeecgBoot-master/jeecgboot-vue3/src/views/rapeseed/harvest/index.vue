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
        <a-col :span="12">
          <a-card class="selection-card base-select-card" :bordered="false">
            <template #title>
              <div class="selection-title">
                <Icon icon="ant-design:environment-outlined" />
                基地选择
              </div>
            </template>
            <BaseSelect
              ref="baseSelectRef"
              v-model:value="selectedBaseId"
              @change="handleBaseChange"
            />
          </a-card>
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
              <div class="map-placeholder">
                <div class="map-markers">
                  <div v-for="(marker, index) in mapData.markers" :key="index" 
                       class="map-marker" 
                       :class="marker.status"
                       :style="{ left: marker.position.x + '%', top: marker.position.y + '%' }">
                    <a-tooltip :title="marker.title + (marker.status === 'harvesting' ? ' (收割中)' : marker.status === 'completed' ? ' (已完成)' : ' (未开始)')">
                      <Icon :icon="marker.status === 'harvesting' ? 'ant-design:sync-outlined' : marker.status === 'completed' ? 'ant-design:check-circle-outlined' : 'ant-design:clock-circle-outlined'" />
                    </a-tooltip>
                  </div>
                </div>
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
              </div>
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
  </div>
</template>

<script lang="ts" setup>
import { reactive, ref, onMounted, nextTick, computed } from 'vue';
import { BasicTable, useTable, TableAction } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useMessage } from '/@/hooks/web/useMessage';
import { Icon } from '/@/components/Icon';
import { columns, searchFormSchema } from './harvest.data';
import { getHarvestList, deleteHarvest, exportHarvest, importHarvest } from './harvest.api';
import HarvestModal from './HarvestModal.vue';
import BaseSelect from '../production-plan/components/BaseSelect.vue';
import BarMulti from '/@/components/chart/BarMulti.vue';

const { createMessage } = useMessage();
const [registerModal, { openModal }] = useModal();
const searchInfo = reactive<Recordable>({});
const fileList = ref<any[]>([]);

// 基地选择相关
const selectedBaseId = ref<string | number | undefined>(undefined);
const selectedBaseName = ref<string>('');
const baseSelectRef = ref();

// 加载状态
const loading = ref(false);

// 默认基地ID（可以从URL参数或用户设置中获取）
const defaultBaseId = ref<string | number | undefined>(undefined);

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

const mapData = ref({
  center: [116.404, 39.915],
  zoom: 12,
  markers: [],
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
    if (!selectedBaseId.value) {
      return;
    }
  
  try {
    // 这里应该调用实际的API获取统计数据
    // const stats = await getHarvestStats(selectedBaseId.value);
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
  if (!selectedBaseId.value) {
    return;
  }
  
  try {
    // 这里应该调用实际的API获取收割机状态
    // const status = await getHarvesterStatus(selectedBaseId.value);
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
  if (!selectedBaseId.value) {
    return;
  }
  
  try {
    // 这里应该调用实际的API获取图表数据
    // const chartData = await getYieldChartData(selectedBaseId.value);
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

// 加载地图数据
async function loadMapData() {
  if (!selectedBaseId.value) {
    return;
  }
  
  try {
    // const res = fieldMapApi.getMapData({ baseId: baseId.value });
    // mapData.value = res.result;
    
    // 模拟数据
    mapData.value = {
      center: [116.404, 39.915],
      zoom: 12,
      markers: [
        { id: 1, title: 'A区-03号地块', status: 'harvesting', position: { x: 25, y: 30 } },
        { id: 2, title: 'B区-07号地块', status: 'completed', position: { x: 65, y: 45 } },
        { id: 3, title: 'C区-02号地块', status: 'harvesting', position: { x: 40, y: 70 } },
        { id: 4, title: 'D区-05号地块', status: 'pending', position: { x: 75, y: 25 } },
      ]
    };
  } catch (error) {
    console.error('加载地图数据失败:', error);
  }
}

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
  beforeFetch: (params) => {
    // 如果有选中的基地ID，添加到查询参数
    if (selectedBaseId.value) {
      params.baseId = selectedBaseId.value;
    }
    return params;
  },
});

/**
 * 基地选择变化处理
 */
function handleBaseChange(baseId) {
  selectedBaseId.value = baseId;
  
  // 获取基地名称
  if (baseSelectRef.value && baseSelectRef.value.baseOptions) {
    const base = baseSelectRef.value.baseOptions.find(item => item.id === baseId);
    selectedBaseName.value = base ? base.baseName : '';
  }
  
  // 重新加载所有数据
  loadAllData();
}

/**
 * 加载所有数据
 */
async function loadAllData() {
  if (!selectedBaseId.value) {
    return;
  }
  
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

/**
 * 加载收获数据
 */
async function loadHarvestData() {
  if (!selectedBaseId.value) {
    return;
  }
  
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
  if (!selectedBaseId.value) {
    createMessage.warning('请先选择基地');
    return;
  }
  
  openModal(true, {
    isUpdate: false,
    baseId: selectedBaseId.value,
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

// 页面加载时获取数据
onMounted(() => {
  loadAllData();
});
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

.selection-title, .timeline-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.base-select-card,
.plot-select-card,
.confirm-card {
  height: 100%;
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
    height: 520px;
    border-radius: 8px;
    overflow: hidden;
    background-color: #f5f5f5;
    
    .map-placeholder {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #f5f5f5;
  border-radius: 4px;
  position: relative;
  overflow: hidden;
  color: #8c8c8c;
}

.map-markers {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.map-marker {
  position: absolute;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transform: translate(-50%, -50%);
  cursor: pointer;
  transition: all 0.3s ease;
}

.map-marker:hover {
  transform: translate(-50%, -50%) scale(1.2);
}

.map-marker.harvesting {
  background-color: #52c41a;
  color: white;
}

.map-marker.completed {
  background-color: #1890ff;
  color: white;
}

.map-marker.pending {
  background-color: #faad14;
  color: white;
}

.map-legend {
  position: absolute;
  bottom: 16px;
  right: 16px;
  background-color: rgba(255, 255, 255, 0.9);
  padding: 8px 12px;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
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
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 8px;
}

.legend-marker.harvesting {
  background-color: #52c41a;
}

.legend-marker.completed {
  background-color: #1890ff;
}

.legend-marker.pending {
  background-color: #faad14;
}
  }
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
  .selection-timeline-row {
    .ant-col {
      margin-bottom: 16px;
    }
  }
  
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
  
  .selection-timeline-row {
    .ant-col {
      margin-bottom: 16px;
    }
    
    .ant-col:first-child {
      margin-bottom: 0;
    }
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
