<template>
  <div class="insect-control-page">
    <a-card class="pest-card" :bordered="false">
      <a-row class="main-content-row">
        <!-- 实时虫情图像 -->
        <a-col :span="8">
          <a-card title="实时虫情图像" :bordered="false" class="inner-card">
            <div class="image-container">
              <img
                class="insect-image"
                :src="currentImageUrl"
                alt="实时虫情图像"
                v-if="currentImageUrl"
              />
              <div v-else class="no-image">暂无图像</div>
            </div>
            <div class="update-time">更新于：{{ lastUpdateTime }}</div>
          </a-card>
        </a-col>

        <!-- 实时虫情报告 -->
        <a-col :span="8">
          <a-card title="实时虫情报告" :bordered="false" class="inner-card">
            <div ref="barChartRef" class="chart-container"></div>
            <div class="update-time">更新于：{{ lastUpdateTime }}</div>
          </a-card>
        </a-col>
        <!-- 农药选择及分析结果 -->
        <a-col :span="8">
          <a-card title="农药选择与分析" :bordered="false" class="inner-card">
            <a-button type="primary" @click="handleLLMAnalysis" style="margin-bottom: 12px;">
              Ai生成最近十天虫情的防治建议
            </a-button>
            <!-- 分析结果展示（来自虫情报告） -->
            <div v-if="llmAnalysis" class="analysis-section">
              <div class="analysis-result" :class="{ 'collapsed': !isAnalysisExpanded }" style="white-space: pre-line;">
                {{ llmAnalysis }}
              </div>
              <div class="analysis-controls">
                <a-button type="link" @click="toggleAnalysis" class="toggle-button">
                  {{ isAnalysisExpanded ? '收起' : '展开' }}
                  <Icon :icon="isAnalysisExpanded ? 'ant-design:up-outlined' : 'ant-design:down-outlined'" />
                </a-button>
              </div>
            </div>
            <!-- 农药选择与记录模块 -->
            <div v-if="llmAnalysis" class="pesticide-selection-section" style="margin-top: 20px;">
              <a-divider>农药选择与使用记录</a-divider>
              <a-form :model="pesticideForm" layout="vertical">
                <a-row :gutter="16">
                  <a-col :span="24">
                    <a-form-item label="选择农药">
                      <a-select
                        v-model:value="pesticideForm.selectedPesticide"
                        placeholder="请选择农药"
                        @change="handlePesticideChange"
                        show-search
                        option-filter-prop="children"
                      >
                        <a-select-option
                          v-for="pesticide in pesticideOptions"
                          :key="pesticide.id"
                          :value="pesticide.id"
                        >
                          {{ pesticide.name }}
                        </a-select-option>
                      </a-select>
                    </a-form-item>
                  </a-col>
                </a-row>
                <a-row :gutter="16">
                  <a-col :span="12">
                    <a-form-item label="使用剂量">
                      <a-input
                        v-model:value="pesticideForm.dosage"
                        placeholder="请输入使用剂量"
                      />
                    </a-form-item>
                  </a-col>
                  <a-col :span="12">
                    <a-form-item label="使用方法">
                      <a-input
                        v-model:value="pesticideForm.method"
                        placeholder="请输入使用方法"
                      />
                    </a-form-item>
                  </a-col>
                </a-row>
                <a-form-item>
                  <a-button
                    type="primary"
                    @click="savePesticideRecord"
                    :loading="savingRecord"
                    :disabled="!pesticideForm.selectedPesticide"
                  >
                    保存使用记录
                  </a-button>
                </a-form-item>
              </a-form>
            </div>
          </a-card>
        </a-col>
      </a-row>
    </a-card>
    <a-card :bordered="false" class="history-card">
      <a-tabs v-model:activeKey="activeTab">
        <!-- 🐛 历史虫情记录 -->
        <a-tab-pane key="pest" tab="历史虫情记录">
          <!-- 时间选择 + 查询 -->
          <a-row :gutter="16" align="middle">
            <a-col :span="8">
              <a-range-picker
                v-model:value="selectedHistoryRange"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                show-time
                style="width: 100%;"
              />
            </a-col>
            <a-col :span="2">
              <a-button type="primary" @click="handleQueryHistory">查询</a-button>
            </a-col>
          </a-row>
          <!-- 展开 / 收起 -->
          <div style="margin: 10px 0;">
            <a-button type="link" @click="historyCollapsed  = !historyCollapsed ">
              {{ historyCollapsed  ? '展开记录' : '收起记录' }}
            </a-button>
          </div>
          <!-- 数据展示 -->
          <div v-if="!historyCollapsed  && filteredHistory.length" class="history-grid">
            <div v-for="(record, idx) in filteredHistory" :key="idx" class="history-card-item">
              <div class="history-content">
                <div class="image-wrapper">
                  <img :src="record.image_url" class="pest-image" />
                </div>
                <div class="record-info">
                  <div class="record-time">获取时间：{{ record.dateCreated }}</div>
                  <div class="record-time">分析时间：{{ record.analysis_time }}</div>
                  <div class="pest-list">
                    <div
                      v-for="([name, count], idx2) in Object.entries(record.insects || {})"
                      :key="idx2"
                      class="pest-entry"
                    >{{ name }}（{{ count }}只）</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </a-tab-pane>
        <!-- 🛠 历史防控记录 -->
        <a-tab-pane key="control" tab="历史防控记录">
          <!-- 时间选择 + 查询 -->
          <a-row :gutter="16" align="middle">
            <a-col :span="8">
              <a-range-picker
                v-model:value="selectedControlRange"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                show-time
                style="width: 100%;"
              />
            </a-col>
            <a-col :span="2">
              <a-button type="primary" @click="handleQueryControl">查询</a-button>
            </a-col>
          </a-row>
          <!-- 展开 / 收起 -->
          <div style="margin: 10px 0;">
            <a-button type="link" @click="controlCollapsed = !controlCollapsed">
              {{ controlCollapsed ? '展开记录' : '收起记录' }}
            </a-button>
          </div>
          <!-- 数据展示 -->
          <div v-if="!controlCollapsed && controlList.length" class="history-grid">
            <div v-for="(item, idx) in controlList" :key="idx" class="history-card-item">
              <div class="history-content">
                <div class="record-info">
                  <div class="record-time">防控时间：{{ item.controlDate }}</div>
                  <div class="record-time">防控方式：{{ item.applicationMethod || '无' }}</div>
                  <div class="record-time">农药名称：{{ item.pesticideName || '无' }}</div>
                  <div class="record-time">农药用量：{{ item.pesticideDosage || '无' }}</div>
                  <div class="record-time">效果评估：{{ item.controlEffectiveness || '无' }}</div>
                </div>
              </div>
            </div>
          </div>
        </a-tab-pane>
      </a-tabs>
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
// import { getInsectControlList, deleteInsectControl, exportInsectControl, importInsectControl } from './insectControl.api';
import InsectControlModal from './InsectControlModal.vue';
import BaseSelect from '../production-plan/components/BaseSelect.vue';
import PlotSelect from '../production-plan/components/PlotSelect.vue';
import GrowthTimeline from '../production-plan/components/GrowthTimeline.vue';
import * as echarts from 'echarts'
import { message } from 'ant-design-vue'
import axios from 'axios'
import { useSelectStore } from '/@/store/selectStore';
import { message as createMessage } from 'ant-design-vue'
// 获取Pinia存储实例



const { createMessage } = useMessage();
// 加载状态
const loading = ref(false);
// 默认基地ID（可以从URL参数或用户设置中获取）
const defaultBaseId = ref<string | number | undefined>(undefined);
const [registerModal, { openModal }] = useModal();
const searchInfo = reactive<Recordable>({});
const fileList = ref<any[]>([]);
const pesticideOptions = ref([])
const selectedPesticide = ref(null)

const analysisResult = ref('')



// --- 实时虫情图像 ---
const currentImageUrl = ref('')
const lastUpdateTime = ref('')
const insectReportData = ref([])
const barChartRef = ref(null)
let chartInstance = null

// 获取当前日期和之前 10 天的日期，格式：YYYY-MM-DD
const getDateRange = () => {
  const today = new Date()
  const tenDaysAgo = new Date(today)
  tenDaysAgo.setDate(today.getDate() - 10)
  const formatDate = (date) => date.toISOString().slice(0, 10)
  return {
    start_date: formatDate(tenDaysAgo),
    end_date: formatDate(today)
  }
}
import { getPesticideList,addPestControlRecord,getFirstPest,analyzePestWithAI,getAllPest,getControlHistory } from './insectControl.api'
// 获取最新虫情图片及分析数据
const rawInsects = ref({})
const fetchLatestImageAndData = async () => {
  // const { start_date, end_date } = getDateRange()
  try {
    const images= await getFirstPest()
    rawInsects.value=images
    if (images && images.length > 0) {
      const latest = images[0]
      currentImageUrl.value = latest.image_url
      lastUpdateTime.value = latest.analysis_time.replace('T', ' ')
      // 提取 insects 对象并转为数组形式
      const insects = latest.insects || {}
      insectReportData.value = Object.entries(insects).map(([name, value]) => ({
        name,
        value
      }))
    } else {
      currentImageUrl.value = ''
      lastUpdateTime.value = '暂无数据'
      insectReportData.value = []
    }
  } catch (err) {
    console.error('获取虫情数据失败:', err)
    currentImageUrl.value = ''
    lastUpdateTime.value = '加载失败'
    insectReportData.value = []
  }
}
const renderBarChart = () => {
  if (!barChartRef.value) return
  if (!chartInstance) chartInstance = echarts.init(barChartRef.value)
  const option = {
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: insectReportData.value.map((i) => i.name),
      axisLabel: { color: '#666' }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#666' }
    },
    series: [
      {
        data: insectReportData.value.map((i) => i.value),
        type: 'bar',
        itemStyle: { color: '#73c0de', borderRadius: [4, 4, 0, 0] }
      }
    ],
    grid: { left: '10%', right: '5%', bottom: '10%', top: '10%' }
  }
  chartInstance.setOption(option)
  chartInstance.resize()
}
// 监听数据变化时重绘
watch(insectReportData, renderBarChart, { deep: true })
// 生命周期
onMounted(async () => {
  await nextTick()
  await fetchLatestImageAndData()
  renderBarChart()
  window.addEventListener('resize', () => chartInstance && chartInstance.resize())
  // 可选：每5分钟刷新一次
  setInterval(fetchLatestImageAndData, 5 * 60 * 1000)
})
onUnmounted(() => {
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})
const isAnalysisExpanded = ref(true)   // 控制分析结果展开/收起状态
const llmAnalysis = ref('')            // 完整分析结果
const toggleAnalysis = () => {
  isAnalysisExpanded.value = !isAnalysisExpanded.value
}
const handleLLMAnalysis = async () => {
  if (!rawInsects.value || rawInsects.value.length === 0) {
    message.warning('暂无虫情数据可供分析')
    return
  }
  try {
    // 构造发送给大模型的数据
    const requestData = {
      pest_data: rawInsects.value.map(item => ({
        analysis_time: item.analysis_time,
        insects: item.insects
      })),
    }
    // 调用后端大模型分析接口
    const response = await analyzePestWithAI(requestData)
    const data = response.data ?? response
    console.log(response)
    // 转换为字符串
    const fullText = typeof data === 'string' ? data : JSON.stringify(data, null, 2)
    // 直接显示完整结果
    llmAnalysis.value = fullText
    isAnalysisExpanded.value = true
    // 可选：解析农药选项
    fetchPesticideOptions()

  } catch (error) {
    console.error('AI分析请求失败:', error)
  }
}

// 添加农药选择相关变量
const pesticideForm = ref({
  selectedPesticide: null,
  dosage: '',
  method: '',
  defaultDosage: '',
  defaultMethod: ''
})

//获取农药列表
// 获取农药列表
const fetchPesticideOptions = async () => {
  try {
    const response = await getPesticideList()
    const formattedPesticides = response.map((name, index) => ({
      id: `pesticide_${index + 1}`,
      name: name
    }))
    pesticideOptions.value = formattedPesticides || []
  } catch (error) {
    console.error('获取农药列表失败:', error)
  }
}

// 农药选择变化处理

const handlePesticideChange = (value) => {
  // 清空剂量和方法字段，让用户自己填写
  pesticideForm.value.dosage = '';
  pesticideForm.value.method = '';
}
const savingRecord = ref(false)
// 保存农药使用记录

const selectStore = useSelectStore();

const { start_date, end_date } = getDateRange()
const savePesticideRecord = async () => {
  if (!pesticideForm.value.selectedPesticide) {
    message.warning('请选择农药')
    return
  }
  savingRecord.value = true
  try {
    const requestData = {
      plotId:selectStore.selectedPlot.plotId,
      controlDate: end_date,
      pesticideName: pesticideOptions.value.find(item => item.id === pesticideForm.value.selectedPesticide)?.name,
      pesticideDosage: pesticideForm.value.dosage,
      applicationMethod: pesticideForm.value.method,
    }
    await addPestControlRecord(requestData);
    message.success('使用记录保存成');
    // 重置表单
    pesticideForm.value.selectedPesticide = null;
    pesticideForm.value.dosage = '';
    pesticideForm.value.method = '';
  } catch (error) {
    console.error('保存使用记录失败:', error)
  } finally {
    savingRecord.value = false
  }
}

const activeTab = ref("pest");
const historyCollapsed = ref(false);  // 默认展开历史记录
const selectedHistoryRange = ref([])
const filteredHistory = ref([])
const selectedControlRange = ref([]);    // 防控查询时间段

const controlCollapsed = ref(false);
// 防控查询结果
const handleQueryHistory = async () => {
  if (!selectedHistoryRange.value || selectedHistoryRange.value.length !== 2) {
    message.warning('请先选择查询时间段');
    return; // 没选择就不继续查询
  }
  // 精确到日
  const formatDate = (dateStr) => dateStr.slice(0, 10);
  const start_date = formatDate(selectedHistoryRange.value[0]);
  const end_date = formatDate(selectedHistoryRange.value[1]);
  try {
    const res = await getAllPest({ start_date, end_date })
    filteredHistory.value = res|| [];
    if (!filteredHistory.value.length) {
      message.info('该时间段内无虫情记录');
    }
  } catch (e) {
    console.error('获取虫情记录失败', e);
  }
};

// 查询防控

const controlList = ref([]);

const handleQueryControl = async () => {
  const res = await getControlHistory({
    plotId: selectStore.selectedPlot.plotId,
    start_date: selectedControlRange.value[0],
    end_date: selectedControlRange.value[1],
  });

  controlList.value = res;   // ★ 把返回的数据放进去
  console.log("防控记录：", controlList.value);
};


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


.pesticide-selection-section {
  .ant-divider {
    margin: 16px 0;
  }

  .ant-form-item {
    margin-bottom: 12px;
  }

  .ant-btn {
    width: 100%;
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

.no-image {
  color: #aaa;
}
/* ---------- 图表区域 ---------- */
.chart-container {
  width: 100%;
  height: 600px; /* 图表固定高度，避免被压缩 */
}

.analysis-section {
  .analysis-result {
    margin-top: 8px;
    padding: 12px;
    background-color: #f6ffed;
    border: 1px solid #b7eb8f;
    border-radius: 4px;
    color: #389e0d;
    font-weight: 500;
    text-align: left;
    max-height: 300px;
    overflow-y: auto;

    &.collapsed {
      max-height: 100px;
      overflow: hidden;
      position: relative;

      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        height: 30px;
        background: linear-gradient(to bottom, transparent, #f6ffed);
      }
    }
  }

  .analysis-controls {
    text-align: center;
    padding: 8px 0;

    .toggle-button {
      color: #1890ff;

      &:hover {
        color: #40a9ff;
      }
    }
  }
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
