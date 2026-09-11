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
        <a-col :span="12" style="text-align: right">
          <a-space>
            <a-button @click="goMachine">
              <Icon icon="ant-design:tool-outlined" /> 管理农机
            </a-button>
            <a-button @click="goTask">
              <Icon icon="ant-design:profile-outlined" /> 管理任务
            </a-button>
          </a-space>
        </a-col>

      </a-row>
    </div>

    <!-- 数据概览卡片 -->
    <a-row :gutter="12" class="mb-4">
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
            <!-- 使用新建的天地图收获组件（轻量化） -->
            <TiandituHarvestMap
              :mapWidth="'100%'"
              :mapHeight="'480px'"
              :baseId="selectedBase?.baseId ? String(selectedBase.baseId) : null"
              :enableHover="true"
              :harvestedPlotIds="harvestedPlotIds"
              :harvestingPlotIds="harvestingPlotIds"
            />
          </div>
        </a-card>
      </a-col>
    </a-row>

    <a-card class="table-card" :bordered="false">
      <template #title>
        <div style="display: flex; align-items: center; gap: 16px;">
          <span>收割作业记录</span>
          <a-range-picker
            v-model:value="dateRangeValue"
            format="YYYY-MM-DD"
            :placeholder="['开始日期', '结束日期']"
            style="width: 260px;"
            @change="onDateRangeChange"
          />
          <a-button type="primary" :loading="recordsLoading" @click="loadHarvestRecords">
            查询
          </a-button>
        </div>
      </template>
      
      <BasicTable @register="registerTable">
        <template #toolbar>
          <a-button @click="handleExport">
            <Icon icon="ant-design:download-outlined" />
            导出
          </a-button>
        </template>
      </BasicTable>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, nextTick, computed, onMounted, onUnmounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { BasicTable, useTable } from '/@/components/Table';
import { useMessage } from '/@/hooks/web/useMessage';
import { Icon } from '/@/components/Icon';
import { columns } from './harvest.data';
import { getHarvestStats, getPlotHarvestSummary, getHarvestMachineList, getHarvestOperationRecords, YIELD_PER_MU } from './harvest.api';
import TiandituHarvestMap from '/@/components/TiandituHarvestMap';
import { useSelectStore } from '/@/store/selectStore';
import { storeToRefs } from 'pinia';
import dayjs, { Dayjs } from 'dayjs';


const { createMessage } = useMessage();
const router = useRouter();
const selectStore = useSelectStore();
const { selectedBase } = storeToRefs(selectStore);

const machineList = ref<any[]>([]);
const harvestRecords = ref<any[]>([]);
const dateRange = ref<[string, string] | null>(null);
const dateRangeValue = ref<[Dayjs, Dayjs] | null>(null);
const recordsLoading = ref(false);

function onDateRangeChange(dates: [Dayjs, Dayjs] | null) {
  if (dates) {
    dateRange.value = [
      dates[0].format('YYYY-MM-DD') + ' 00:00:00',
      dates[1].format('YYYY-MM-DD') + ' 23:59:59'
    ];
  } else {
    dateRange.value = null;
  }
}

const selectedBaseName = computed(() => {
  const name = selectedBase.value?.baseName || '';
  return name.replace('基地', '');
});

const filteredMachineList = computed(() => {
  if (!selectedBaseName.value) return machineList.value;
  return machineList.value.filter(m => m.baseName === selectedBaseName.value);
});

const loading = ref(false);

const harvestStats = ref({
  harvestedArea: 0,
  unharvestedArea: 0,
  totalYield: 0,
  machineCount: 0,
  harvestedAreaTrend: 0,
  unharvestedAreaTrend: 0,
  totalYieldTrend: 0,
});

async function loadMachineList() {
  try {
    const list = await getHarvestMachineList();
    machineList.value = Array.isArray(list) ? list : [];
  } catch (error) {
    console.error('加载农机列表失败:', error);
    machineList.value = [];
  }
}

async function loadHarvestRecords() {
  if (filteredMachineList.value.length === 0) {
    harvestRecords.value = [];
    return;
  }
  
  recordsLoading.value = true;
  try {
    const beidouSnList = filteredMachineList.value.map(m => m.beidouSn).filter(Boolean);
    if (beidouSnList.length === 0) {
      harvestRecords.value = [];
      return;
    }
    
    const today = new Date();
    const startDate = dateRange.value?.[0]?.split(' ')[0] || new Date(today.getFullYear(), today.getMonth(), 1).toISOString().split('T')[0];
    const endDate = dateRange.value?.[1]?.split(' ')[0] || today.toISOString().split('T')[0];
    
    const records = await getHarvestOperationRecords(beidouSnList, startDate, endDate);
    const rawRecords = Array.isArray(records) ? records : [];
    
    harvestRecords.value = rawRecords.map(record => ({
      ...record,
      totalYield: record.qualifiedArea ? Number(record.qualifiedArea) * YIELD_PER_MU : 0
    }));
    
    updateHarvestStats();
  } catch (error) {
    console.error('加载收割记录失败:', error);
    harvestRecords.value = [];
  } finally {
    recordsLoading.value = false;
  }
}

function updateHarvestStats() {
  const totalArea = harvestRecords.value.reduce((sum, r) => sum + (Number(r.qualifiedArea) || 0), 0);
  const totalYield = harvestRecords.value.reduce((sum, r) => sum + (Number(r.totalYield) || 0), 0);
  
  harvestStats.value.harvestedArea = Number(totalArea.toFixed(2));
  harvestStats.value.totalYield = Number((totalYield / 1000).toFixed(2));
  harvestStats.value.machineCount = filteredMachineList.value.length;
}

async function loadHarvestStats() {
  try {
    const stats = await getHarvestStats();
    harvestStats.value = {
      harvestedArea: stats?.harvestedArea ?? 0,
      unharvestedArea: stats?.unharvestedArea ?? 0,
      totalYield: stats?.totalYield ?? 0,
      machineCount: stats?.machineCount ?? 0,
      harvestedAreaTrend: stats?.harvestedAreaTrend ?? 0,
      unharvestedAreaTrend: stats?.unharvestedAreaTrend ?? 0,
      totalYieldTrend: stats?.totalYieldTrend ?? 0,
    };
  } catch (error) {
    console.error('加载收获统计数据失败:', error);
  }
}



  
  


const [registerTable] = useTable({
  title: "收割作业记录",
  columns,
  dataSource: harvestRecords,
  loading: recordsLoading,
  showTableSetting: false,
  bordered: true,
  showIndexColumn: false,
  pagination: {
    defaultPageSize: 10,
    showSizeChanger: true,
    pageSizeOptions: ['10', '20', '50', '100'],
  },
});



/**
 * 加载所有数据
 */
async function loadAllData() {
  try {
    loading.value = true;
    
    await loadMachineList();
    
    await Promise.all([
      loadHarvestRecords(),
      loadHarvestStats(),
    ]);
  } catch (error) {
    console.error('加载数据失败:', error);
    createMessage.error('加载数据失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}

// 监听选中基地变化，重新加载数据
watch(
  () => selectedBase.value?.baseId,
  (newBaseId) => {
    if (newBaseId) {
      loadAllData();
      loadPlotSummary();
    }
  }
);

// 页面挂载时初始化数据
onMounted(() => {
  loadAllData();
  loadPlotSummary();
});

// 页面卸载时清理资源
onUnmounted(() => {
  // 页面资源清理占位
});

function goMachine() {
  router.push({ name: 'RapeseedHarvestMachine' });
}

function goTask() {
  router.push({ name: 'RapeseedHarvestTask' });
}

// 加载地块收割派生视图，用于地图状态
async function loadPlotSummary() {
  try {
    const baseId = selectedBase.value?.baseId;
    const params = baseId ? { baseId } : undefined; // 避免传递空值导致后端解析错误
    const list = await getPlotHarvestSummary(params);
    const arr = Array.isArray(list) ? list : [];
    harvestedPlotIds.value = arr.filter((item: any) => item.harvestStatus === 2).map((item: any) => item.plotId);
    harvestingPlotIds.value = arr.filter((item: any) => item.harvestStatus === 1).map((item: any) => item.plotId);
  } catch (e) {
    console.error('加载地块收割视图失败:', e);
  }
}

async function handleExport() {
  if (harvestRecords.value.length === 0) {
    createMessage.warning('暂无数据可导出');
    return;
  }
  
  const headers = ['农机编号', '收割面积(亩)', '累计产量(kg)', '开始时间', '结束时间', '作业位置'];
  const rows = harvestRecords.value.map(r => [
    r.vehicleNumber || '',
    r.qualifiedArea || 0,
    r.totalYield || 0,
    r.startTime || '',
    r.endTime || '',
    r.location || ''
  ]);
  
  const csvContent = [headers, ...rows].map(row => row.join(',')).join('\n');
  const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' });
  const link = document.createElement('a');
  link.href = URL.createObjectURL(blob);
  link.download = `收割作业记录_${dayjs().format('YYYY-MM-DD')}.csv`;
  link.click();
  URL.revokeObjectURL(link.href);
}

// 航拍图相关方法
function handlePolygonSelected(polygon: any) {
  // 处理多边形选中事件
  if (polygon) {
    selectedField.value = polygon;
    detailModalVisible.value = true;
  }
}

// 在地图加载完成后，自动适应容器大小
const onMapLoad = () => {
  nextTick(() => {
    // 重置地图视图以适应容器
    if (mapContainer.value) {
      // 这里我们无法直接访问AerialMap组件的内部状态
      // 所以我们需要通过AerialMap组件暴露的方法来设置地图视图
      // 由于我们已经设置了readonly=true，地图应该不会移动
      console.log('地图加载完成，已设置为只读模式');
    }
  });
};
</script>

<style lang="less" scoped>
.harvest-page {
  padding: 16px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 64px);
}

.page-header {
  margin-bottom: 16px;
  
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
  margin-bottom: 16px;
  
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
  margin-bottom: 12px;
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
  margin-top: 12px;
  
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
  margin-top: 12px;
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
}
</style>
