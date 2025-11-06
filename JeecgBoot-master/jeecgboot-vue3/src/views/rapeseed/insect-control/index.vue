<template>
  <div class="insect-control-page">
    <!-- 基地与地块选择区域和生长周期时间轴区域 -->
    <a-card :bordered="false" class="combined-selection-timeline-card">
      <a-row :gutter="16" align="middle">
        <!-- 基地选择区域 -->
        <a-col :span="3">
          <div class="selection-area">
            <div class="selection-title">基地选择</div>
            <BaseSelect
              v-model:value="selectedBaseId"
              @change="handleBaseChange"
              :defaultBaseId="defaultBaseId"
              ref="baseSelectRef"
            />
          </div>
        </a-col>
        <!-- 地块选择区域 -->
        <a-col :span="3">
          <div class="selection-area">
            <div class="selection-title">地块选择</div>
            <PlotSelect
              v-model:value="selectedPlotId"
              :baseId="selectedBaseId"
              @change="handlePlotChange"
              ref="plotSelectRef"
            />
          </div>
        </a-col>
        <!-- 生长周期时间轴区域 -->
        <a-col :span="18">
          <div class="timeline-area">
            <GrowthTimeline
              :plotId="selectedPlotId"
              :varietyId="selectedVarietyId"
              :varietyName="selectedVarietyName"
              :currentStageId="currentStageId"
            />
          </div>
        </a-col>
      </a-row>
    </a-card>
    <a-card class="pest-card" :bordered="false">
      <a-row class="main-content-row">
        <!-- 实时虫情图像 -->
        <a-col :span="8">
          <a-card title="实时虫情图像" :bordered="false" class="inner-card">
            <div class="image-container">
              <img class="insect-image" :src="currentImageUrl" alt="实时虫情图像" />
            </div>
            <div class="update-time">更新于：{{ lastUpdateTime }}</div>
          </a-card>
        </a-col>

        <!-- 实时虫情报告 -->
        <a-col :span="8">
          <a-card title="实时虫情报告" :bordered="false" class="inner-card">
            <div ref="barChartRef" class="chart-container"></div>
            <div class="update-time">更新于：{{ lastTime }}</div>
          </a-card>
        </a-col>

        <!-- 农药选择及分析结果 -->
        <a-col :span="8">
          <a-card title="农药选择与分析" :bordered="false" class="inner-card">
            <!-- 触发分析按钮 -->
            <a-button type="primary" @click="handleLLMAnalysis" style="margin-bottom: 12px;">
              Ai生成防治建议
            </a-button>

            <!-- 分析结果展示（来自虫情报告） -->
            <div v-if="llmAnalysis" class="analysis-result" style="white-space: pre-line;">
              {{ llmAnalysis }}
            </div>

            <!-- 农药选择下拉框，选择后直接显示剂量 -->
            <a-select
              class="pesticide-select"
              v-model="selectedPesticide"
              placeholder="请选择农药"
              allowClear
              @change="handleAnalyze"
            >
              <a-select-option
                v-for="item in pesticideOptions"
                :key="item.value"
                :value="item.value"
              >
                {{ item.label }}
              </a-select-option>
            </a-select>

            <!-- 剂量分析 -->
            <div v-if="analysisResult" class="analysis-result">
              {{ analysisResult }}
            </div>
          </a-card>
        </a-col>
      </a-row>
    </a-card>

    <a-card title="历史虫情记录" :bordered="false" class="history-card">
      <a-row :gutter="16" align="middle">
        <!-- 时间段选择 -->
        <a-col :span="8">
          <a-range-picker
            v-model:value="selectedHistoryRange"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm"
            show-time
            style="width: 100%;"
          />
        </a-col>
        <!-- 查询按钮 -->
        <a-col :span="2">
          <a-button type="primary" @click="handleQueryHistory">查询</a-button>
        </a-col>
      </a-row>

      <!-- 历史虫情结果展示 -->
      <div v-if="filteredHistory.length" class="history-grid">
        <div
          v-for="record in filteredHistory"
          :key="record.id"
          class="history-card-item"
        >
          <div class="history-content">
            <!-- 左侧图片 -->
            <div class="image-wrapper">
              <img :src="record.image" alt="虫情图片" class="pest-image" />
            </div>

            <!-- 右侧虫情信息 -->
            <div class="record-info">
              <div class="record-time">处理时间：{{ record.time }}</div>

              <!-- 害虫信息横向排列 -->
              <div class="pest-list">
                <div
                  v-for="(pest, idx) in record.pests"
                  :key="idx"
                  class="pest-entry"
                >
                  <div class="pest-name">{{ pest.name }}（{{ pest.count }}只）</div>
                </div>
              </div>

              <!-- 统一农药和剂量 -->
              <div class="common-info">
                农药：{{ record.pesticide }} &nbsp; | &nbsp; 剂量：{{ record.dosage }}
              </div>
            </div>
          </div>
        </div>
      </div>

    </a-card>

  </div>
</template>

<script lang="ts" setup>
import {ref, reactive, onMounted,onUnmounted, watch,nextTick } from 'vue';
import { BasicTable, useTable, TableAction } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useMessage } from '/@/hooks/web/useMessage';
import { Icon } from '/@/components/Icon';
import { columns, searchFormSchema } from './insectControl.data';
import { getInsectControlList, deleteInsectControl, exportInsectControl, importInsectControl } from './insectControl.api';
import InsectControlModal from './InsectControlModal.vue';
import BaseSelect from '@/views/rapeseed/production-plan/plot-production-plan/components/BaseSelect.vue';
import PlotSelect from '@/views/rapeseed/production-plan/plot-production-plan/components/PlotSelect.vue';
import GrowthTimeline from '@/views/rapeseed/production-plan/plot-production-plan/components/GrowthTimeline.vue';
import * as echarts from 'echarts'
import { message } from 'ant-design-vue'
import { message as createMessage } from 'ant-design-vue'


const { createMessage } = useMessage();
// 基地选择相关
const selectedBaseId = ref<string | number | undefined>(undefined);
const selectedBaseName = ref<string>('');
const baseSelectRef = ref();
// 地块选择相关
const selectedPlotId = ref<string | number | undefined>(undefined);
const selectedPlotName = ref<string>('');
const plotSelectRef = ref();
// 品种信息
const selectedVarietyId = ref<string | number | undefined>(undefined);
const selectedVarietyName = ref<string>('');
const currentStageId = ref<string | number | undefined>(undefined);
// 加载状态
const loading = ref(false);
// 默认基地ID（可以从URL参数或用户设置中获取）
const defaultBaseId = ref<string | number | undefined>(undefined);
const [registerModal, { openModal }] = useModal();
const searchInfo = reactive<Recordable>({});
const fileList = ref<any[]>([]);
const pesticideOptions = ref([])
const selectedPesticide = ref(null)
const llmAnalysis = ref('')
const analysisResult = ref('')
const selectedHistoryRange = ref([])
const filteredHistory = ref([])
// const [registerTable, { reload, getSelectRows, dataSource }] = useTable({
//   title: '虫害防控管理',
//   api: selectedPlotId.value ? (params) => {
//     // 如果有选中的地块ID，添加到查询参数
//     if (selectedPlotId.value) {
//       params.plotId = selectedPlotId.value;
//     }
//     return getInsectControlList(params);
//   } : getInsectControlList,
//   columns,
//   formConfig: {
//     labelWidth: 120,
//     schemas: searchFormSchema,
//     autoSubmitOnEnter: true,
//   },
//   useSearchForm: true,
//   showTableSetting: true,
//   bordered: true,
//   showIndexColumn: false,
//   actionColumn: {
//     width: 80,
//     title: '操作',
//     dataIndex: 'action',
//     fixed: 'right',
//   },
//   beforeFetch: (params) => {
//     // 如果有选中的基地ID或地块ID，添加到查询参数
//     if (selectedBaseId.value) {
//       params.baseId = selectedBaseId.value;
//     }
//     if (selectedPlotId.value) {
//       params.plotId = selectedPlotId.value;
//     }
//     return params;
//   },
// });

/**
 * 基地选择变化处理
 */
// --- 实时虫情图像 ---
const currentImageUrl = ref('https://tse1.mm.bing.net/th/id/OIP.iIA2SGZVYVnSf-165spLkgHaLH?rs=1&pid=ImgDetMain&o=7&rm=3')
const lastUpdateTime = ref('2025-10-27 10:30')

// --- 实时虫情报告 ---
const insectReportData = ref([
  { name: '稻飞虱', value: 120 },
  { name: '稻纵卷叶螟', value: 180 },
  { name: '螟虫', value: 90 },
])
const lastTime = ref('2025-10-27 18:30')

// 图表实例与引用
const barChartRef = ref(null)
let chartInstance = null

const renderBarChart = () => {
  if (!barChartRef.value) return
  if (!chartInstance) chartInstance = echarts.init(barChartRef.value)

  const option = {
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: insectReportData.value.map(i => i.name),
      axisLabel: { color: '#666' },
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#666' },
    },
    series: [
      {
        data: insectReportData.value.map(i => i.value),
        type: 'bar',
        itemStyle: { color: '#73c0de', borderRadius: [4, 4, 0, 0] },
      },
    ],
    grid: { left: '10%', right: '5%', bottom: '10%', top: '10%' },
  }

  chartInstance.setOption(option)
  chartInstance.resize()
}

// 组件挂载后绘制图表
onMounted(async () => {
  await nextTick()
  renderBarChart()
  window.addEventListener('resize', () => chartInstance && chartInstance.resize())
})

// 组件卸载时销毁图表
onUnmounted(() => {
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})

// 数据变化时自动更新图表
watch(insectReportData, renderBarChart, { deep: true })

// 模拟大模型分析
const handleLLMAnalysis = async () => {
  const report = insectReportData.value.map(i => `${i.name}: ${i.value}`).join('，')
  llmAnalysis.value = `根据虫情数据（${report}），分析如下：
1. 稻纵卷叶螟密度高，属Ⅲ级虫害，应立即防治；
2. 稻飞虱密度较高，建议使用吡虫啉；
3. 螟虫密度较低，可暂缓防治。
推荐农药：吡虫啉（30% 可湿性粉剂，每亩40克）或噻虫嗪（20% 水分散粒剂，每亩25克）。`

  pesticideOptions.value = [
    { label: '吡虫啉', value: 'imidacloprid' },
    { label: '噻虫嗪', value: 'thiamethoxam' },
  ]
}

// 农药选择后直接显示剂量
const handleAnalyze = (value) => {
  if (!value) {
    analysisResult.value = ''
    return
  }
  const dosageMap = {
    imidacloprid: '吡虫啉：30% 可湿性粉剂，每亩40克兑水30公斤喷雾。',
    thiamethoxam: '噻虫嗪：20% 水分散粒剂，每亩25克兑水均匀喷雾。',
  }
  analysisResult.value = dosageMap[value]
}
const historyData = ref([
  {
    id: 1,
    image: 'https://tse1.mm.bing.net/th/id/OIP.iIA2SGZVYVnSf-165spLkgHaLH?rs=1&pid=ImgDetMain&o=7&rm=3',
    time: '2025-10-28 07:30',
    pesticide: '啶虫脒', // 统一农药
    dosage: '50ml/亩',   // 统一剂量
    pests: [
      { name: '稻飞虱', count: 15 },
      { name: '稻纵卷叶螟', count: 8 },
      { name: '二化螟', count: 5 },
    ],
  },
  {
    id: 2,
    image: 'https://tse1.mm.bing.net/th/id/OIP.iIA2SGZVYVnSf-165spLkgHaLH?rs=1&pid=ImgDetMain&o=7&rm=3',
    time: '2025-10-25 08:30',
    pesticide: '啶虫脒',
    dosage: '50ml/亩',
    pests: [
      { name: '稻纵卷叶螟', count: 32 },
      { name: '稻飞虱', count: 10 },
    ],
  },
])
const handleQueryHistory = () => {
  const range = selectedHistoryRange.value
  if (!range || range.length !== 2) {
    message.warning('请先选择开始时间和结束时间')
    return
  }
  const [startTime, endTime] = range.map((t) => new Date(t).getTime())

  filteredHistory.value = historyData.value.filter((item) => {
    const recordTime = new Date(item.time).getTime()
    return recordTime >= startTime && recordTime <= endTime
  })

  if (filteredHistory.value.length === 0) {
    message.info('该时间段内无虫情记录')
  } else {
    console.log('查询结果：', filteredHistory.value)
  }
}


function handleBaseChange(baseId) {
  selectedBaseId.value = baseId;

  // 获取基地名称
  if (baseSelectRef.value && baseSelectRef.value.baseOptions) {
    const base = baseSelectRef.value.baseOptions.find(item => item.id === baseId);
    selectedBaseName.value = base ? base.baseName : '';
  }

  // 清空地块选择
  selectedPlotId.value = undefined;
  selectedPlotName.value = '';

  // 清空品种信息
  selectedVarietyId.value = undefined;
  selectedVarietyName.value = '';
  currentStageId.value = undefined;

  // 重新加载数据
  loadInsectControlData();
}

/**
 * 地块选择变化处理
 */
function handlePlotChange(plotId) {
  selectedPlotId.value = plotId;

  // 获取地块名称
  if (plotSelectRef.value && plotSelectRef.value.plotOptions) {
    const plot = plotSelectRef.value.plotOptions.find(item => item.id === plotId);
    selectedPlotName.value = plot ? plot.plotName : '';

    // 获取品种信息（这里假设地块信息中包含品种信息）
    if (plot) {
      selectedVarietyId.value = plot.varietyId;
      selectedVarietyName.value = plot.varietyName || '';
      // 这里可以根据实际情况设置当前阶段ID
      // currentStageId.value = plot.currentStageId;
    }
  }

  // 重新加载数据
  loadInsectControlData();
}
/**
 * 加载虫害防控数据
 */
async function loadInsectControlData() {
  if (!selectedBaseId.value) {
    return;
  }

  try {
    loading.value = true;
    // 重新加载数据
    await reload();
  } catch (error) {
    console.error('加载虫害防控数据失败:', error);
    createMessage.error('加载虫害防控数据失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}

function handleCreate() {
  if (!selectedBaseId.value || !selectedPlotId.value) {
    createMessage.warning('请先选择基地和地块');
    return;
  }

  openModal(true, {
    isUpdate: false,
    baseId: selectedBaseId.value,
    plotId: selectedPlotId.value,
  });
}

function handleEdit(record: Recordable) {
  openModal(true, {
    record,
    isUpdate: true,
  });
}

async function handleDelete(record: Recordable) {
  await deleteInsectControl(record.id);
  createMessage.success('删除成功');
  reload();
}

function handleSuccess() {
  createMessage.success('操作成功');
  reload();
}

async function handleExport() {
  const data = await getInsectControlList(searchInfo);
  exportInsectControl(data);
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
    await importInsectControl(formData);
    createMessage.success('导入成功');
    reload();
    fileList.value = [];
  } catch (error) {
    createMessage.error('导入失败');
  }
}

// 组件挂载时，可以设置默认基地ID
onMounted(() => {
  // 这里可以从用户信息或系统设置中获取默认基地ID
  // const defaultBaseId = getDefaultBaseId();
  // if (defaultBaseId) {
  //   selectedBaseId.value = defaultBaseId;
  //   loadInsectControlData();
  // }

  // 模拟设置默认基地ID，实际项目中应该从API或配置中获取
  // defaultBaseId.value = '1';
});
</script>

<style lang="less" scoped>
.insect-control-page {
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
  margin-bottom: 16px;

  .selection-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 500;
  }

  .timeline-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 500;
  }
}

// 基地地块选择与时间轴组合卡片样式
.combined-selection-timeline-card {
  margin-bottom: 16px;

  :deep(.ant-card-body) {
    padding-left: 8px;
    padding-right: 8px;
    padding-top: 0px;
    padding-bottom: 0px;
  }

  .selection-area, .timeline-area {
    height: 100%;
  }

  .selection-title {
    font-size: 14px;
    font-weight: 500;
    color: #333;
    margin-bottom: 8px;
    display: flex;
    align-items: center;

    &::before {
      content: '';
      display: inline-block;
      width: 3px;
      height: 14px;
      background-color: #52c41a;
      margin-right: 8px;
      border-radius: 2px;
    }
  }

  .timeline-title {
    font-size: 14px;
    font-weight: 500;
    color: #333;
    margin-bottom: 12px;
    display: flex;
    align-items: center;

    &::before {
      content: '';
      display: inline-block;
      width: 3px;
      height: 14px;
      background-color: #1890ff;
      margin-right: 8px;
      border-radius: 2px;
    }
  }
}

// 间距调整类
.mb-8 {
  margin-bottom: 8px;
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

// 响应式布局
@media (max-width: 1200px) {
  .combined-selection-timeline-card {
    .ant-col {
      margin-bottom: 16px;
    }

    :deep(.ant-row) {
      flex-direction: column;

      .ant-col {
        width: 100% !important;
        margin-bottom: 16px;

        &:last-child {
          margin-bottom: 0;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .insect-control-page {
    padding: 16px;
  }

  .combined-selection-timeline-card {
    :deep(.ant-card-body) {
      padding-left: 8px;
      padding-right: 8px;
      padding-top: 5px;
      padding-bottom: 5px;
    }
  }
}

.pest-card {
  padding: 24px;
  background-color: #f0f2f5;
}

.main-content-row {
  display: flex;
  gap: 16px; /* 列间距 */
  align-items: stretch; /* 三列等高 */
}

.main-content-row > .ant-col {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 250px;
}

.inner-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: flex-start; /* 保证卡片内上下元素间距 */
}

/* ---------- 图像区域 ---------- */
.image-container {
  flex: none;
  height: 600px; /* 固定高度，防止塌陷 */
  display: flex;
  justify-content: center;
  align-items: center;
}

.insect-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 6px;
}

/* ---------- 图表区域 ---------- */
.chart-container {
  width: 100%;
  height: 600px; /* 图表固定高度，避免被压缩 */
}

/* ---------- 农药选择与分析 ---------- */
.pesticide-select {
  width: 100%;
  margin-top: 12px;
}

.analysis-result {
  margin-top: 8px;
  padding: 12px;
  background-color: #f6ffed;
  border: 1px solid #b7eb8f;
  border-radius: 4px;
  color: #389e0d;
  font-weight: 500;
  text-align: left;
}

/* ---------- 时间显示 ---------- */
.update-time {
  margin-top: 8px;
  font-size: 12px;
  color: #888;
  text-align: right;
}


.history-card {
  margin-top: 16px;           /* 与上面内容保持间距 */
  padding: 16px;              /* 内部间距 */
  background-color: #f0f2f5;  /* 和主卡片统一背景 */
  border-radius: 8px;         /* 圆角 */
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.history-card .ant-row {
  align-items: flex-start;    /* 左对齐，顶部对齐 */
  gap: 8px;                   /* 列间距 */
}

.history-card .ant-col {
  display: flex;
  align-items: flex-start;    /* 左对齐 */
}

.history-card a-select {
  width: 100%;
}

.history-card a-button {
  width: 100%;
}
.history-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 16px;           /* 与上面内容保持间距 */

}

.history-card-item {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 12px;
}

.history-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.image-wrapper {
  flex-shrink: 0;
}

.pest-image {
  width: 200px;
  height: 130px;
  object-fit: cover;
  border-radius: 8px;
}

.record-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.record-time {
  font-weight: bold;
  margin-bottom: 8px;
  color: #333;
}

.pest-entry {
  margin-bottom: 6px;
}

.pest-name {
  font-weight: 500;
  color: #1677ff;
}


.no-data {
  text-align: center;
  color: #999;
  margin-top: 20px;
}

/* 响应式：小屏幕换行 */
@media (max-width: 992px) {
  .main-content-row {
    flex-direction: column;
  }
  .main-content-row > .ant-col {
    min-width: 100%;
  }
}


</style>
