<template>
  <div class="insect-control-page">
    <a-card :bordered="false" class="toolbar-card">
      <div class="toolbar">
        <div class="toolbar-left">
          <span class="toolbar-label">查询时间</span>
          <a-range-picker
            v-model:value="queryRange"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 320px"
          />
          <a-button type="primary" :loading="pageLoading" @click="handleSearch">查询</a-button>
          <a-button @click="resetToDefaultRange">近7天</a-button>
        </div>
        <div class="toolbar-right">
          <a-tag color="blue">基地：{{ currentBaseName || '未选择' }}</a-tag>
          <a-tag color="green">图片数：{{ pestImages.length }}</a-tag>
          <a-tag color="orange">虫量总数：{{ totalInsectCount }}</a-tag>
          <a-tag color="purple">虫种数量：{{ insectStatistics.length }}</a-tag>
        </div>
      </div>
    </a-card>

    <a-row :gutter="16" class="main-content-row">
      <a-col :xs="24" :xl="15">
        <a-card title="虫情图像" :bordered="false" class="content-card">
          <a-spin :spinning="pageLoading">
            <div v-if="pestImages.length === 0" class="empty-container">
              <a-empty description="当前时间范围内暂无虫情图片" />
            </div>
            <div v-else class="image-grid">
              <div
                v-for="(record, index) in pestImages"
                :key="`${record.analysis_time || record.dateCreated || index}`"
                class="image-card"
                @click="previewImage(record.image_url || record.thumbnail)"
              >
                <div class="image-wrapper">
                  <img :src="record.thumbnail || record.image_url" class="pest-image" alt="虫情图片" />
                  <div class="image-overlay">点击查看大图</div>
                </div>
                <div class="image-info">
                  <div class="image-time">采集时间：{{ formatDisplayTime(record.dateCreated) }}</div>
                  <div class="image-tags">
                    <a-tag color="blue">虫量 {{ record.total_count || 0 }}</a-tag>
                    <a-tag color="green">虫种 {{ record.species_count || 0 }}</a-tag>
                  </div>
                </div>
              </div>
            </div>
          </a-spin>
        </a-card>
      </a-col>

      <a-col :xs="24" :xl="9">
        <a-card title="虫情统计" :bordered="false" class="content-card chart-card">
          <div ref="barChartRef" class="chart-container"></div>
          <div class="update-time">统计范围：{{ queryRangeText }}</div>
        </a-card>
      </a-col>
    </a-row>

    <a-card title="虫情趋势与防治建议" :bordered="false" class="analysis-card">
      <a-spin :spinning="analysisLoading">
        <div v-if="!currentBaseName" class="analysis-empty">
          <a-empty description="请先选择基地" />
        </div>
        <div v-else-if="pestImages.length === 0 && !pageLoading" class="analysis-empty">
          <a-empty description="当前时间范围内暂无虫情数据" />
        </div>
        <div v-else-if="analysisError" class="analysis-error">
          {{ analysisError }}
        </div>
        <div v-else-if="llmAnalysis" class="analysis-result" style="white-space: pre-line;">
          {{ llmAnalysis }}
        </div>
        <div v-else class="analysis-placeholder">正在根据虫情图片生成趋势与防治建议...</div>
      </a-spin>
    </a-card>

    <a-modal v-model:open="previewVisible" title="虫情图片预览" :footer="null" width="1000px">
      <img :src="previewImageUrl" style="width: 100%" alt="虫情图片预览" />
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue';
import * as echarts from 'echarts';
import { message } from 'ant-design-vue';
import { useSelectStore } from '/@/store/selectStore';
import { getPestAnalysisTask, getPestImages, submitPestAnalysisTask, type PestImageQueryParams } from './insectControl.api';

interface PestRecord {
  dateCreated?: string;
  analysis_time?: string;
  image_url?: string;
  thumbnail?: string;
  total_count?: number;
  species_count?: number;
  insects?: Record<string, number>;
}

interface InsectChartItem {
  name: string;
  value: number;
}

const ANALYSIS_POLL_INTERVAL = 2000;
const ANALYSIS_MAX_POLL_COUNT = 90;

const selectStore = useSelectStore();
const currentBaseName = computed(() => selectStore.selectedBase.baseName || '');

// 默认查询近 7 天，进入页面后直接展示这一段时间内的全部虫情图片。
function getDefaultDateRange() {
  const today = new Date();
  const sevenDaysAgo = new Date(today);
  sevenDaysAgo.setDate(today.getDate() - 6);
  const formatDate = (date: Date) => date.toISOString().slice(0, 10);
  return {
    start_date: formatDate(sevenDaysAgo),
    end_date: formatDate(today),
  };
}

const { start_date, end_date } = getDefaultDateRange();
const queryRange = ref<string[]>([start_date, end_date]);
const queryRangeText = computed(() => `${queryRange.value[0]} 至 ${queryRange.value[1]}`);

const pestImages = ref<PestRecord[]>([]);
const pageLoading = ref(false);
const previewVisible = ref(false);
const previewImageUrl = ref('');

const llmAnalysis = ref('');
const analysisLoading = ref(false);
const analysisError = ref('');
let latestAnalysisToken = 0;

const barChartRef = ref<HTMLDivElement | null>(null);
let chartInstance: echarts.ECharts | null = null;
let refreshTimer: number | undefined;
let resizeHandler: (() => void) | undefined;

// 将所有图片中的虫种数量做聚合，图表直接展示这个汇总结果。
const insectStatistics = computed<InsectChartItem[]>(() => {
  const counter = new Map<string, number>();
  pestImages.value.forEach((record) => {
    Object.entries(record.insects || {}).forEach(([name, count]) => {
      counter.set(name, (counter.get(name) || 0) + Number(count || 0));
    });
  });
  return Array.from(counter.entries())
    .map(([name, value]) => ({ name, value }))
    .sort((a, b) => b.value - a.value);
});

const totalInsectCount = computed(() => insectStatistics.value.reduce((sum, item) => sum + item.value, 0));

function buildPestQueryParams(): PestImageQueryParams {
  return {
    baseName: currentBaseName.value,
    StarDate: queryRange.value[0],
    EndDate: queryRange.value[1],
  };
}

function previewImage(url?: string) {
  if (!url) {
    return;
  }
  previewImageUrl.value = url;
  previewVisible.value = true;
}

// 将接口返回的 ISO 时间统一格式化为页面展示用的本地时间。
function formatDisplayTime(value?: string) {
  if (!value) {
    return '-';
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  const pad = (num: number) => String(num).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
}

function clearPestData() {
  latestAnalysisToken += 1;
  pestImages.value = [];
  llmAnalysis.value = '';
  analysisError.value = '';
}

async function loadPestImages() {
  if (!currentBaseName.value) {
    clearPestData();
    return;
  }

  pageLoading.value = true;
  try {
    const res = await getPestImages(buildPestQueryParams());
    pestImages.value = Array.isArray(res) ? res : [];
    llmAnalysis.value = '';
    analysisError.value = '';
    if (pestImages.value.length === 0) {
      message.info('当前时间范围内无虫情图片');
      return;
    }
  } catch (error) {
    console.error('加载虫情图片失败:', error);
    pestImages.value = [];
    llmAnalysis.value = '';
    analysisError.value = '加载虫情图片失败';
    message.error('加载虫情图片失败');
  } finally {
    pageLoading.value = false;
  }

  void handleLLMAnalysis(pestImages.value);
}

function renderBarChart() {
  if (!barChartRef.value) {
    return;
  }

  if (!chartInstance) {
    chartInstance = echarts.init(barChartRef.value);
  }

  chartInstance.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '8%', right: '5%', bottom: '8%', top: '8%', containLabel: true },
    xAxis: {
      type: 'category',
      data: insectStatistics.value.map((item) => item.name),
      axisLabel: {
        color: '#666',
        interval: 0,
        rotate: insectStatistics.value.length > 6 ? 30 : 0,
      },
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#666' },
    },
    series: [
      {
        data: insectStatistics.value.map((item) => item.value),
        type: 'bar',
        barMaxWidth: 42,
        itemStyle: {
          color: '#73c0de',
          borderRadius: [6, 6, 0, 0],
        },
      },
    ],
  });

  chartInstance.resize();
}

async function handleLLMAnalysis(records: PestRecord[]) {
  if (records.length === 0) {
    return;
  }

  const imageUrls = records
    .map((item) => item.image_url || item.thumbnail)
    .filter((url): url is string => Boolean(url));

  if (imageUrls.length === 0) {
    llmAnalysis.value = '';
    analysisError.value = '当前虫情图片缺少可分析的图片地址';
    return;
  }

  const token = ++latestAnalysisToken;
  analysisLoading.value = true;
  analysisError.value = '';
  try {
    const requestData = {
      pest_data: records.map((item) => ({
        analysis_time: item.analysis_time,
        insects: item.insects,
      })),
      image_urls: imageUrls,
    };

    const submitResult = await submitPestAnalysisTask(requestData);
    const taskResult = await pollPestAnalysisTask(submitResult.taskId, token);
    if (token !== latestAnalysisToken) {
      return;
    }
    llmAnalysis.value = typeof taskResult === 'string' ? taskResult : JSON.stringify(taskResult, null, 2);
  } catch (error) {
    if (token !== latestAnalysisToken) {
      return;
    }
    console.error('AI分析请求失败:', error);
    llmAnalysis.value = '';
    analysisError.value = 'AI分析请求失败，请稍后重试';
    message.error('AI分析请求失败，请稍后重试');
  } finally {
    if (token === latestAnalysisToken) {
      analysisLoading.value = false;
    }
  }
}

async function pollPestAnalysisTask(taskId: string, token: number) {
  for (let count = 0; count < ANALYSIS_MAX_POLL_COUNT; count += 1) {
    const task = await getPestAnalysisTask(taskId);
    if (token !== latestAnalysisToken) {
      throw new Error('分析任务已取消');
    }
    if (task.status === 'SUCCESS') {
      return task.result || '';
    }
    if (task.status === 'FAILED') {
      throw new Error(task.errorMessage || 'AI分析失败');
    }
    await sleep(ANALYSIS_POLL_INTERVAL);
  }
  throw new Error('AI分析超时，请稍后重试');
}

function sleep(ms: number) {
  return new Promise((resolve) => window.setTimeout(resolve, ms));
}

async function handleSearch() {
  if (!currentBaseName.value) {
    message.warning('请先选择基地');
    return;
  }
  if (!queryRange.value || queryRange.value.length !== 2) {
    message.warning('请先选择查询时间');
    return;
  }
  await loadPestImages();
}

function resetToDefaultRange() {
  const range = getDefaultDateRange();
  queryRange.value = [range.start_date, range.end_date];
  void handleSearch();
}

watch(
  () => insectStatistics.value,
  () => {
    nextTick(() => renderBarChart());
  },
  { deep: true }
);

watch(
  () => currentBaseName.value,
  async () => {
    // 基地切换后按默认近 7 天范围重新加载当前基地数据。
    const range = getDefaultDateRange();
    queryRange.value = [range.start_date, range.end_date];
    await loadPestImages();
  },
  { immediate: true }
);

onMounted(async () => {
  await nextTick();
  renderBarChart();

  resizeHandler = () => {
    if (chartInstance) {
      chartInstance.resize();
    }
  };

  window.addEventListener('resize', resizeHandler);
  refreshTimer = window.setInterval(() => {
    void loadPestImages();
  }, 5 * 60 * 1000);
});

onUnmounted(() => {
  latestAnalysisToken += 1;
  if (resizeHandler) {
    window.removeEventListener('resize', resizeHandler);
  }
  if (refreshTimer) {
    window.clearInterval(refreshTimer);
  }
  if (chartInstance) {
    chartInstance.dispose();
    chartInstance = null;
  }
});
</script>

<style lang="less" scoped>
.insect-control-page {
  padding: 20px;
  background: #f0f2f5;
  min-height: calc(100vh - 64px);
}

.toolbar-card {
  margin-bottom: 16px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  flex-wrap: wrap;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.toolbar-right {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.toolbar-label {
  font-weight: 500;
  color: #262626;
}

.main-content-row {
  align-items: stretch;
}

.content-card {
  height: 100%;

  :deep(.ant-card-body) {
    height: calc(100% - 57px);
    display: flex;
    flex-direction: column;
  }
}

.empty-container {
  min-height: 320px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
  max-height: 780px;
  overflow-y: auto;
  padding-right: 4px;
}

.image-card {
  border-radius: 12px;
  overflow: hidden;
  background: #fff;
  border: 1px solid #f0f0f0;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
  }
}

.image-wrapper {
  position: relative;
  height: 180px;
  overflow: hidden;
}

.pest-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.image-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.35);
  color: #fff;
  font-size: 13px;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.image-card:hover .image-overlay {
  opacity: 1;
}

.image-info {
  padding: 12px;
}

.image-time {
  font-size: 13px;
  color: #595959;
  line-height: 1.6;
}

.image-tags {
  margin-top: 8px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.chart-card {
  margin-bottom: 16px;
}

.chart-container {
  width: 100%;
  height: 360px;
}

.update-time {
  margin-top: 8px;
  font-size: 12px;
  color: #8c8c8c;
  text-align: right;
}

.analysis-card {
  margin-top: 16px;
  min-height: 320px;

  :deep(.ant-card-body) {
    height: auto;
    display: block;
  }
}

.analysis-empty,
.analysis-placeholder {
  min-height: 240px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #8c8c8c;
}

.analysis-result {
  min-height: 240px;
  padding: 16px 18px;
  background-color: #f6ffed;
  border: 1px solid #b7eb8f;
  border-radius: 8px;
  color: #389e0d;
  font-weight: 500;
  text-align: left;
  line-height: 1.9;
}

.analysis-error {
  min-height: 240px;
  padding: 16px 18px;
  background: #fff2f0;
  border: 1px solid #ffccc7;
  border-radius: 8px;
  color: #cf1322;
  display: flex;
  align-items: center;
}

@media (max-width: 1200px) {
  .chart-container {
    height: 320px;
  }
}

@media (max-width: 768px) {
  .insect-control-page {
    padding: 16px;
  }

  .image-grid {
    grid-template-columns: 1fr;
  }

  .chart-container {
    height: 280px;
  }

}
</style>
