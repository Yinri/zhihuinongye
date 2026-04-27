<template>
  <div class="seedling-quality-page">
    <a-row :gutter="16" class="main-content">
      <a-col class="monitor-column" flex="2">
        <a-card title="监控截图" :bordered="false" class="content-card">
          <template #extra>
            <div class="card-extra">
              <a-tag color="blue">共 {{ monitorCards.length }} 张截图</a-tag>
              <a-tag color="processing">{{ workflowMessage }}</a-tag>
              <a-button type="primary" size="small" :loading="pageLoading" @click="refreshData">
                <ReloadOutlined />
                刷新
              </a-button>
            </div>
          </template>

          <a-spin :spinning="pageLoading" tip="正在加载监控截图并分析苗情...">
            <div v-if="monitorLoadError" class="state-wrap">
              <a-result status="error" title="监控截图加载失败" :sub-title="monitorLoadError">
                <template #extra>
                  <a-button type="primary" @click="refreshData">重新加载</a-button>
                </template>
              </a-result>
            </div>

            <div v-else-if="!monitorCards.length" class="state-wrap">
              <a-empty description="当前基地暂无监控截图" />
            </div>

            <template v-else>
              <div class="overview-banner">
                <div class="overview-title">{{ currentBaseTitle }}</div>
                <div class="overview-desc">
                  左侧展示基地实时监控截图，右侧输出大模型对当前油菜苗情的综合判断。
                </div>
              </div>

              <div class="video-section">
                <div class="video-section-header">
                  <span class="video-section-title">实时监控</span>
                  <a-tag color="blue">共 {{ videoDevices.length }} 路</a-tag>
                </div>
                <a-spin :spinning="videoLoading" tip="正在加载实时监控...">
                  <div v-if="videoDevices.length === 0" class="video-empty">
                    <a-empty description="暂无视频监控设备" />
                  </div>
                  <div v-else class="video-grid">
                    <div
                      v-for="device in videoDevices"
                      :key="device.equipmentCode"
                      class="video-monitor-card"
                    >
                      <FlvPlayer
                        :url="device.streamUrl || ''"
                        :title="device.equipmentName"
                        class="video-player"
                      />
                      <PtzControlPanel
                        class="video-control-panel"
                        :device-code="device.equipmentCode"
                        :channel-num="device.channelNum"
                      />
                    </div>
                  </div>
                </a-spin>
              </div>

              <div class="image-grid">
                <div
                  v-for="card in monitorCards"
                  :key="card.id"
                  class="monitor-card"
                  @click="previewImage(card.imageUrl)"
                >
                  <div class="image-shell">
                    <img
                      :src="card.imageUrl"
                      :alt="card.deviceName || `监控截图${card.order}`"
                      @load="markImageLoaded(card.id)"
                      @error="markImageError(card.id)"
                    />
                    <div class="image-overlay">
                      <EyeOutlined />
                    </div>
                    <div class="image-status">
                      <a-tag v-if="card.imageStatus === 'error'" color="error">截图异常</a-tag>
                      <a-tag v-else-if="card.imageStatus === 'loaded'" color="success">可用</a-tag>
                      <a-tag v-else color="processing">加载中</a-tag>
                    </div>
                  </div>

                  <div class="monitor-meta">
                    <div class="device-name">
                      {{ card.deviceName || `监控点 ${card.order}` }}
                    </div>
                    <div class="device-time">
                      {{ formatImageTime(card.dateCreated) }}
                    </div>
                  </div>
                </div>
              </div>
            </template>
          </a-spin>
        </a-card>
      </a-col>

      <a-col class="analysis-column" flex="3">
        <a-card title="AI苗情分析" :bordered="false" class="content-card analysis-card">
          <template #extra>
            <div class="analysis-extra">
              <a-tag v-if="analysisResult" :color="getLevelColor(analysisResult.seedlingLevel)">
                {{ analysisResult.seedlingLevel }}
              </a-tag>
              <a-button
                size="small"
                :loading="analysisLoading"
                :disabled="!monitorCards.length"
                @click="runSeedlingAnalysis"
              >
                重新分析
              </a-button>
            </div>
          </template>

          <div class="analysis-panel">
            <div v-if="analysisLoading && !analysisResult" class="state-wrap">
              <a-spin size="large" tip="正在生成苗情分析..." />
            </div>

            <div v-else-if="!analysisResult" class="state-wrap">
              <a-empty description="暂无苗情分析结果" />
            </div>

            <template v-else>
              <div class="summary-card">
                <div class="summary-head">
                  <div>
                    <div class="summary-label">综合苗情</div>
                    <div class="summary-level">{{ analysisResult.seedlingLevel }}</div>
                  </div>
                  <div class="summary-score">
                    置信度 {{ Math.round((analysisResult.confidence || 0) * 100) }}%
                  </div>
                </div>
                <div class="summary-text">{{ analysisResult.summary || '暂无综合结论' }}</div>
              </div>

              <a-descriptions bordered size="small" :column="1" class="analysis-descriptions">
                <a-descriptions-item label="生育阶段">
                  {{ analysisResult.growthStage || '未知' }}
                </a-descriptions-item>
                <a-descriptions-item label="主要依据">
                  {{ analysisResult.evidence || '暂无依据' }}
                </a-descriptions-item>
                <a-descriptions-item label="分析范围">
                  基于 {{ monitorCards.length }} 张监控截图综合判断
                </a-descriptions-item>
                <a-descriptions-item label="分析时间">
                  {{ formatImageTime(analysisResult.analysisTime) }}
                </a-descriptions-item>
              </a-descriptions>

              <div class="section-block">
                <div class="section-title">关键指标</div>
                <div class="indicator-grid">
                  <div
                    v-for="(item, index) in analysisIndicators"
                    :key="`${item.name}-${index}`"
                    class="indicator-card"
                  >
                    <div class="indicator-head">
                      <span class="indicator-name">{{ item.name }}</span>
                      <a-tag :color="getIndicatorColor(item.level)">{{ item.level || '未知' }}</a-tag>
                    </div>
                    <div class="indicator-value">{{ item.value || '暂无结果' }}</div>
                    <div class="indicator-desc">{{ item.description || '暂无说明' }}</div>
                  </div>
                </div>
              </div>

              <div class="section-block">
                <div class="section-title">主要问题</div>
                <div v-if="analysisProblems.length" class="tag-list">
                  <a-tag v-for="(item, index) in analysisProblems" :key="`${item}-${index}`" color="orange">
                    {{ item }}
                  </a-tag>
                </div>
                <div v-else class="empty-inline">当前未识别到明显异常苗情</div>
              </div>

              <div class="section-block">
                <div class="section-title">田间建议</div>
                <a-timeline v-if="analysisSuggestions.length" class="suggestion-list">
                  <a-timeline-item v-for="(item, index) in analysisSuggestions" :key="`${item}-${index}`">
                    {{ item }}
                  </a-timeline-item>
                </a-timeline>
                <div v-else class="empty-inline">暂无管理建议</div>
              </div>
            </template>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <a-modal v-model:open="previewVisible" title="监控截图预览" :footer="null" width="920px">
      <img :src="previewImageUrl" alt="监控截图预览" class="preview-image" />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { message } from 'ant-design-vue';
import { EyeOutlined, ReloadOutlined } from '@ant-design/icons-vue';
import dayjs from 'dayjs';
import { useSelectStore } from '/@/store/selectStore';
import FlvPlayer from '../work-area/components/FlvPlayer.vue';
import PtzControlPanel from '../work-area/components/PtzControlPanel.vue';
import { getVideoDevices, getVideoStream } from '../work-area/workArea.api';
import {
  getSeedlingAnalysisTask,
  getMonitorImages,
  submitSeedlingAnalysis,
  type ImageInfo,
  type SeedlingAnalysisResponse,
  type SeedlingIndicator,
} from './seedlingQuality.api';

type ImageLoadStatus = 'loading' | 'loaded' | 'error';

interface MonitorImageCard extends ImageInfo {
  id: string;
  order: number;
  imageStatus: ImageLoadStatus;
}

interface VideoMonitorDevice {
  equipmentCode: string;
  equipmentName: string;
  channelNum: string;
  streamUrl?: string | null;
}

const ANALYSIS_POLL_INTERVAL = 2000;
const ANALYSIS_MAX_POLL_COUNT = 90;

const selectStore = useSelectStore();

const monitorCards = ref<MonitorImageCard[]>([]);
const videoDevices = ref<VideoMonitorDevice[]>([]);
const analysisResult = ref<SeedlingAnalysisResponse | null>(null);

const monitorLoading = ref(false);
const analysisLoading = ref(false);
const videoLoading = ref(false);
const monitorLoadError = ref('');
let latestAnalysisToken = 0;

const previewVisible = ref(false);
const previewImageUrl = ref('');

const pageLoading = computed(() => monitorLoading.value || analysisLoading.value);
const currentBaseTitle = computed(() => selectStore.selectedBase?.baseName || '当前基地');
const workflowMessage = computed(() => {
  if (monitorLoadError.value) {
    return '截图加载失败';
  }
  if (monitorLoading.value) {
    return '正在加载截图';
  }
  if (analysisLoading.value) {
    return '正在分析苗情';
  }
  if (!monitorCards.value.length) {
    return '暂无截图';
  }
  return '分析完成';
});
const analysisIndicators = computed<SeedlingIndicator[]>(() => analysisResult.value?.indicators || []);
const analysisProblems = computed<string[]>(() => analysisResult.value?.mainProblems || []);
const analysisSuggestions = computed<string[]>(() => analysisResult.value?.managementSuggestions || []);

watch(
  () => [selectStore.selectedBase?.baseId, selectStore.selectedBase?.baseName],
  ([baseId, baseName]) => {
    if (baseId && baseName) {
      void refreshData();
      return;
    }
    resetState();
  },
  { immediate: true }
);

function resetState() {
  latestAnalysisToken += 1;
  monitorCards.value = [];
  videoDevices.value = [];
  analysisResult.value = null;
  monitorLoadError.value = '';
  analysisLoading.value = false;
}

function createMonitorCard(image: ImageInfo, index: number): MonitorImageCard {
  return {
    ...image,
    id: `${image.imageUrl}-${index}`,
    order: index + 1,
    imageStatus: 'loading',
  };
}

async function loadMonitorImages() {
  const baseId = selectStore.selectedBase?.baseId;
  if (!baseId) {
    resetState();
    return;
  }

  monitorLoading.value = true;
  monitorLoadError.value = '';
  monitorCards.value = [];

  try {
    const images = await getMonitorImages({
      baseId: String(baseId),
    });
    monitorCards.value = (images || []).map(createMonitorCard);
    if (!monitorCards.value.length) {
      analysisResult.value = null;
      message.info('当前基地暂无监控截图');
    }
  } catch (error) {
    console.error('加载监控截图失败', error);
    monitorCards.value = [];
    analysisResult.value = null;
    monitorLoadError.value = '请检查监控设备与截图服务后重试';
    message.error('加载监控截图失败');
  } finally {
    monitorLoading.value = false;
  }
}

async function refreshData() {
  if (!selectStore.selectedBase?.baseId || !selectStore.selectedBase?.baseName) {
    resetState();
    return;
  }
  videoDevices.value = [];
  videoLoading.value = false;
  await loadMonitorImages();
  if (monitorCards.value.length) {
    await runSeedlingAnalysis();
  }
}

async function loadVideoMonitorDevices() {
  const baseId = selectStore.selectedBase?.baseId;
  if (!baseId) {
    videoDevices.value = [];
    return;
  }

  videoLoading.value = true;
  try {
    const devices = await getVideoDevices(String(baseId));
    const mappedDevices: VideoMonitorDevice[] = (devices || []).map((device: any) => ({
      equipmentCode: device.equipmentCode,
      equipmentName: device.equipmentName,
      channelNum: device.channelNum,
      streamUrl: '',
    }));
    videoDevices.value = mappedDevices;

    await Promise.allSettled(
      videoDevices.value.map(async (device) => {
        try {
          device.streamUrl = await getVideoStream(device.equipmentCode, device.channelNum);
        } catch (error) {
          console.error(`加载视频流失败: ${device.equipmentName}`, error);
          device.streamUrl = '';
        }
      })
    );
  } catch (error) {
    console.error('加载实时监控失败', error);
    videoDevices.value = [];
  } finally {
    videoLoading.value = false;
  }
}

async function runSeedlingAnalysis() {
  const baseName = selectStore.selectedBase?.baseName;
  const baseId = selectStore.selectedBase?.baseId;
  const imageUrls = monitorCards.value
    .filter((item) => item.imageStatus !== 'error')
    .map((item) => item.imageUrl)
    .filter(Boolean);

  if (!baseName || !baseId) {
    message.warning('请先选择基地');
    return;
  }
  if (!imageUrls.length) {
    analysisResult.value = null;
    return;
  }

  const token = ++latestAnalysisToken;
  analysisLoading.value = true;
  try {
    const submitResult = await submitSeedlingAnalysis({
      baseName,
      imageUrls,
    });
    const result = await pollSeedlingAnalysisTask(submitResult.taskId, token, String(baseId));
    if (token !== latestAnalysisToken || baseId !== selectStore.selectedBase?.baseId) {
      return;
    }
    analysisResult.value = normalizeAnalysisResult(result);
    message.success('苗情分析完成');
  } catch (error) {
    if (token !== latestAnalysisToken) {
      return;
    }
    console.error('苗情分析失败', error);
    message.error('苗情分析失败');
  } finally {
    if (token === latestAnalysisToken) {
      analysisLoading.value = false;
    }
  }
}

async function pollSeedlingAnalysisTask(taskId: string, token: number, baseId: string) {
  for (let count = 0; count < ANALYSIS_MAX_POLL_COUNT; count += 1) {
    const task = await getSeedlingAnalysisTask(taskId);
    if (token !== latestAnalysisToken || baseId !== String(selectStore.selectedBase?.baseId || '')) {
      throw new Error('分析任务已取消');
    }
    if (task.status === 'SUCCESS') {
      return task.result || ({} as SeedlingAnalysisResponse);
    }
    if (task.status === 'FAILED') {
      throw new Error(task.errorMessage || '苗情分析失败');
    }
    await sleep(ANALYSIS_POLL_INTERVAL);
  }
  throw new Error('苗情分析超时，请稍后重试');
}

function sleep(ms: number) {
  return new Promise((resolve) => window.setTimeout(resolve, ms));
}

function normalizeAnalysisResult(result: SeedlingAnalysisResponse): SeedlingAnalysisResponse {
  return {
    ...result,
    indicators: Array.isArray(result?.indicators) ? result.indicators : [],
    mainProblems: Array.isArray(result?.mainProblems) ? result.mainProblems : [],
    managementSuggestions: Array.isArray(result?.managementSuggestions) ? result.managementSuggestions : [],
  };
}

function markImageLoaded(id: string) {
  const target = monitorCards.value.find((item) => item.id === id);
  if (!target || target.imageStatus === 'loaded') {
    return;
  }
  target.imageStatus = 'loaded';
}

function markImageError(id: string) {
  const target = monitorCards.value.find((item) => item.id === id);
  if (!target) {
    return;
  }
  target.imageStatus = 'error';
}

function previewImage(url?: string) {
  if (!url) {
    return;
  }
  previewImageUrl.value = url;
  previewVisible.value = true;
}

function formatImageTime(value?: string) {
  if (!value) {
    return '暂无时间';
  }
  const parsed = dayjs(value);
  return parsed.isValid() ? parsed.format('YYYY-MM-DD HH:mm:ss') : value;
}

function getLevelColor(level?: string) {
  switch (level) {
    case '壮苗':
      return 'success';
    case '正常':
      return 'processing';
    case '偏弱':
      return 'warning';
    case '旺长':
      return 'magenta';
    default:
      return 'default';
  }
}

function getIndicatorColor(level?: string) {
  if (!level) {
    return 'default';
  }
  if (['优', '良', '正常', '健康'].includes(level)) {
    return 'success';
  }
  if (['一般', '轻微异常'].includes(level)) {
    return 'processing';
  }
  if (['偏弱', '偏稀', '偏密', '不整齐', '异常'].includes(level)) {
    return 'warning';
  }
  if (level === '无法判断') {
    return 'default';
  }
  return 'blue';
}
</script>

<style lang="less" scoped>
.seedling-quality-page {
  padding: 16px;
  background: #f5f7fb;
}

.main-content {
  min-height: calc(100vh - 180px);
}

.monitor-column {
  min-width: 0;
}

.analysis-column {
  min-width: 0;
}

.content-card {
  min-height: calc(100vh - 180px);
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
}

.card-extra,
.analysis-extra {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.state-wrap {
  min-height: 420px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.overview-banner {
  margin-bottom: 16px;
  padding: 16px 18px;
  border-radius: 14px;
  background: linear-gradient(135deg, #effaf1 0%, #f7fffb 100%);
  border: 1px solid #d9f7be;
}

.overview-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f1f1f;
}

.overview-desc {
  margin-top: 6px;
  color: #666;
  line-height: 1.6;
}

.video-section {
  margin-bottom: 16px;
  padding: 12px;
  border-radius: 14px;
  border: 1px solid #e6f4ff;
  background: #fcfeff;
}

.video-section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.video-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f1f1f;
}

.video-empty {
  min-height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.video-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.video-monitor-card {
  position: relative;
  min-height: 360px;
  overflow: hidden;
  border-radius: 14px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  background: #020617;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.12);
}

.video-player {
  flex: 1;
  min-height: 220px;
}

.video-monitor-card :deep(.flv-player-container) {
  height: 100%;
  border-radius: 0;
}

.video-monitor-card :deep(.video-wrapper) {
  min-height: 180px;
}

.video-control-panel {
  position: absolute;
  right: 12px;
  bottom: 12px;
  z-index: 20;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}

.monitor-card {
  overflow: hidden;
  border-radius: 14px;
  border: 1px solid #eef1f5;
  background: #fff;
  cursor: pointer;
  transition: all 0.2s ease;
}

@media (max-width: 1200px) {
  .monitor-column,
  .analysis-column {
    flex: 0 0 100% !important;
    max-width: 100% !important;
  }
}

.monitor-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.1);
}

.image-shell {
  position: relative;
  height: 180px;
  overflow: hidden;
  background: #f6f8fb;
}

.image-shell img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.28);
  color: #fff;
  font-size: 20px;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.monitor-card:hover .image-overlay {
  opacity: 1;
}

.image-status {
  position: absolute;
  top: 10px;
  right: 10px;
}

.monitor-meta {
  padding: 12px 14px;
}

.device-name {
  font-weight: 600;
  color: #1f1f1f;
}

.device-time {
  margin-top: 6px;
  font-size: 12px;
  color: #8c8c8c;
}

.analysis-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.summary-card {
  padding: 18px;
  border-radius: 16px;
  background: linear-gradient(135deg, #e6f4ff 0%, #f8fbff 100%);
  border: 1px solid #bae0ff;
}

.summary-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.summary-label {
  color: #666;
  font-size: 13px;
}

.summary-level {
  margin-top: 4px;
  font-size: 28px;
  font-weight: 700;
  color: #0958d9;
  line-height: 1.2;
}

.summary-score {
  color: #1677ff;
  font-weight: 600;
  white-space: nowrap;
}

.summary-text {
  margin-top: 14px;
  line-height: 1.8;
  color: #1f1f1f;
}

.analysis-descriptions {
  :deep(.ant-descriptions-item-label) {
    width: 92px;
  }
}

.section-block {
  padding: 16px;
  border-radius: 14px;
  background: #fff;
  border: 1px solid #eef1f5;
}

.section-title {
  margin-bottom: 14px;
  font-size: 15px;
  font-weight: 600;
  color: #1f1f1f;
}

.indicator-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.indicator-card {
  padding: 14px;
  border-radius: 12px;
  background: #fafcff;
  border: 1px solid #edf2f7;
}

.indicator-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.indicator-name {
  font-weight: 600;
  color: #1f1f1f;
}

.indicator-value {
  margin-top: 10px;
  font-size: 15px;
  color: #1677ff;
  font-weight: 600;
}

.indicator-desc {
  margin-top: 8px;
  color: #666;
  line-height: 1.6;
  font-size: 13px;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.suggestion-list {
  padding-left: 6px;
}

.empty-inline {
  color: #8c8c8c;
}

.preview-image {
  display: block;
  width: 100%;
  border-radius: 10px;
}
</style>
