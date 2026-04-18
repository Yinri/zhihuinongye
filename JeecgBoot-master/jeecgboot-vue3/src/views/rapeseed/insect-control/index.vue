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

    <a-card title="农药选择与分析" :bordered="false" class="analysis-card">
      <div class="analysis-header">
        <a-button type="primary" size="large" @click="handleLLMAnalysis">
          AI生成近7天虫情防治建议
        </a-button>
        <div class="analysis-tip">基于当前时间范围内的虫情图片汇总结果生成防治建议</div>
      </div>

      <div v-if="llmAnalysis" class="analysis-content">
        <div class="analysis-section">
          <div class="analysis-result" :class="{ collapsed: !isAnalysisExpanded }" style="white-space: pre-line;">
            {{ llmAnalysis }}
          </div>
          <div class="analysis-controls">
            <a-button type="link" @click="toggleAnalysis" class="toggle-button">
              {{ isAnalysisExpanded ? '收起' : '展开' }}
              <Icon :icon="isAnalysisExpanded ? 'ant-design:up-outlined' : 'ant-design:down-outlined'" />
            </a-button>
          </div>
        </div>

        <div class="pesticide-selection-section">
          <a-divider>农药选择与使用记录</a-divider>
          <a-form :model="pesticideForm" layout="vertical">
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

            <a-row :gutter="16">
              <a-col :xs="24" :md="12">
                <a-form-item label="使用剂量">
                  <a-input v-model:value="pesticideForm.dosage" placeholder="请输入使用剂量" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12">
                <a-form-item label="使用方法">
                  <a-input v-model:value="pesticideForm.method" placeholder="请输入使用方法" />
                </a-form-item>
              </a-col>
            </a-row>

            <a-form-item>
              <a-button
                type="primary"
                @click="savePesticideRecord"
                :loading="savingRecord"
                :disabled="!pesticideForm.selectedPesticide"
                block
              >
                保存使用记录
              </a-button>
            </a-form-item>
          </a-form>
        </div>
      </div>
    </a-card>

    <a-modal v-model:open="previewVisible" title="虫情图片预览" :footer="null" width="1000px">
      <img :src="previewImageUrl" style="width: 100%" alt="虫情图片预览" />
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue';
import { Icon } from '/@/components/Icon';
import * as echarts from 'echarts';
import { message } from 'ant-design-vue';
import { useSelectStore } from '/@/store/selectStore';
import { addPestControlRecord, analyzePestWithAI, getAllPest, getPesticideList, type PestImageQueryParams } from './insectControl.api';

interface PestRecord {
  dateCreated?: string;
  analysis_time?: string;
  image_url?: string;
  thumbnail?: string;
  total_count?: number;
  species_count?: number;
  insects?: Record<string, number>;
}

interface PesticideOption {
  id: string;
  name: string;
}

interface InsectChartItem {
  name: string;
  value: number;
}

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

const isAnalysisExpanded = ref(true);
const llmAnalysis = ref('');
const savingRecord = ref(false);
const pesticideOptions = ref<PesticideOption[]>([]);
const pesticideForm = ref({
  selectedPesticide: null as string | null,
  dosage: '',
  method: '',
});

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
  pestImages.value = [];
  llmAnalysis.value = '';
}

async function loadPestImages() {
  if (!currentBaseName.value) {
    clearPestData();
    return;
  }

  pageLoading.value = true;
  try {
    const res = await getAllPest(buildPestQueryParams());
    pestImages.value = Array.isArray(res) ? res : [];
    if (pestImages.value.length === 0) {
      message.info('当前时间范围内无虫情图片');
    }
  } catch (error) {
    console.error('加载虫情图片失败:', error);
    pestImages.value = [];
    message.error('加载虫情图片失败');
  } finally {
    pageLoading.value = false;
  }
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

async function fetchPesticideOptions() {
  try {
    const response = await getPesticideList();
    const names = Array.isArray(response) ? response : [];
    pesticideOptions.value = names.map((name: string, index: number) => ({
      id: `pesticide_${index + 1}`,
      name,
    }));
  } catch (error) {
    console.error('获取农药列表失败:', error);
    message.error('获取农药列表失败');
  }
}

function handlePesticideChange() {
  // 更换农药后清空用户输入，避免沿用上一种农药的剂量和方法。
  pesticideForm.value.dosage = '';
  pesticideForm.value.method = '';
}

function toggleAnalysis() {
  isAnalysisExpanded.value = !isAnalysisExpanded.value;
}

async function handleLLMAnalysis() {
  if (pestImages.value.length === 0) {
    message.warning('暂无虫情数据可供分析');
    return;
  }

  try {
    const requestData = {
      pest_data: pestImages.value.map((item) => ({
        analysis_time: item.analysis_time,
        insects: item.insects,
      })),
    };

    const response = await analyzePestWithAI(requestData);
    const data = response.data ?? response;
    llmAnalysis.value = typeof data === 'string' ? data : JSON.stringify(data, null, 2);
    isAnalysisExpanded.value = true;
    await fetchPesticideOptions();
  } catch (error) {
    console.error('AI分析请求失败:', error);
    message.error('AI分析请求失败，请稍后重试');
  }
}

async function savePesticideRecord() {
  if (!pesticideForm.value.selectedPesticide) {
    message.warning('请选择农药');
    return;
  }
  if (!selectStore.selectedPlot.plotId) {
    message.warning('请先选择地块');
    return;
  }

  savingRecord.value = true;
  try {
    const requestData = {
      plotId: selectStore.selectedPlot.plotId,
      controlDate: queryRange.value[1],
      pesticideName: pesticideOptions.value.find((item) => item.id === pesticideForm.value.selectedPesticide)?.name,
      pesticideDosage: pesticideForm.value.dosage,
      applicationMethod: pesticideForm.value.method,
    };

    await addPestControlRecord(requestData);
    message.success('使用记录保存成功');
    pesticideForm.value.selectedPesticide = null;
    pesticideForm.value.dosage = '';
    pesticideForm.value.method = '';
  } catch (error) {
    console.error('保存使用记录失败:', error);
    message.error('保存使用记录失败');
  } finally {
    savingRecord.value = false;
  }
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

.analysis-header {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;

  .ant-btn {
    min-width: 260px;
  }
}

.analysis-tip {
  font-size: 13px;
  line-height: 1.6;
  color: #8c8c8c;
}

.analysis-content {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(320px, 0.8fr);
  gap: 16px;
  align-items: start;
}

.analysis-section {
  .analysis-result {
    padding: 14px 16px;
    background-color: #f6ffed;
    border: 1px solid #b7eb8f;
    border-radius: 8px;
    color: #389e0d;
    font-weight: 500;
    text-align: left;
    max-height: 280px;
    overflow-y: auto;
    line-height: 1.8;

    &.collapsed {
      max-height: 120px;
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
    padding: 10px 0 0;
  }
}

.pesticide-selection-section {
  margin-top: 0;
  padding: 16px;
  border-radius: 10px;
  background: #fafafa;
  border: 1px solid #f0f0f0;

  :deep(.ant-divider) {
    margin: 0 0 16px;
  }

  :deep(.ant-form-item) {
    margin-bottom: 14px;
  }
}

@media (max-width: 1200px) {
  .chart-container {
    height: 320px;
  }

  .analysis-content {
    grid-template-columns: 1fr;
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

  .analysis-header {
    .ant-btn {
      width: 100%;
      min-width: 0;
    }
  }
}
</style>
