<template>
  <div class="disease-control-page">
    <a-row :gutter="16" class="main-content">
      <a-col :span="12">
        <a-card title="监控截图 - 病害分析" :bordered="false" class="content-card">
          <template #extra>
            <div class="card-extra">
              <a-tag color="blue">共 {{ monitorCards.length }} 张图片</a-tag>
              <a-tag color="processing">已分析 {{ monitorSuccessCount }}/{{ analyzableMonitorCount }}</a-tag>
              <a-tag v-if="monitorFailedCount > 0" color="red">失败 {{ monitorFailedCount }}</a-tag>
              <a-dropdown>
                <a-button size="small" :disabled="monitorCards.length === 0">
                  <DownloadOutlined /> 导出报告 <DownOutlined />
                </a-button>
                <template #overlay>
                  <a-menu @click="handleExportMenuClick">
                    <a-menu-item key="excel">导出Excel</a-menu-item>
                    <a-menu-item key="pdf">导出PDF</a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
            </div>
          </template>

          <div class="image-section">
            <div class="image-gallery">
              <a-spin :spinning="monitorLoading" tip="正在加载监控截图...">
                <div v-if="monitorLoadError" class="empty-container">
                  <a-result status="error" title="截图加载失败" :sub-title="monitorLoadError">
                    <template #extra>
                      <a-button type="primary" @click="retryMonitorWorkflow">
                        <ReloadOutlined /> 点击重试
                      </a-button>
                    </template>
                  </a-result>
                </div>
                <div v-else-if="monitorCards.length === 0" class="empty-container">
                  <a-empty description="暂无监控截图数据" />
                </div>
                <template v-else>
                  <div class="workflow-tip">
                    <a-alert
                      type="info"
                      show-icon
                      :message="monitorWorkflowMessage"
                    />
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
                      v-for="card in pagedMonitorCards"
                      :key="card.id"
                      class="monitor-card"
                    >
                      <div class="image-item" @click="previewImage(card.imageUrl)">
                        <img
                          :src="card.imageUrl"
                          :alt="card.deviceName || '监控截图'"
                          @load="markMonitorImageLoaded(card.id)"
                          @error="markMonitorImageError(card.id)"
                        />
                        <div class="image-overlay">
                          <EyeOutlined class="preview-icon" />
                        </div>
                        <div class="image-status">
                          <a-tag v-if="card.imageStatus === 'error'" color="red">截图失效</a-tag>
                          <a-tag v-else-if="card.analyzeStatus === 'success'" :color="getSeverityColor(card.analyzeResult?.severity)">
                            {{ card.analyzeResult?.severity || '已完成' }}
                          </a-tag>
                          <a-tag v-else-if="card.analyzeStatus === 'failed'" color="error">分析失败</a-tag>
                          <a-tag v-else-if="card.analyzeStatus === 'analyzing'" color="processing">分析中</a-tag>
                          <a-tag v-else color="default">等待分析</a-tag>
                        </div>
                      </div>

                      <div class="monitor-card-body">
                        <div class="monitor-card-head">
                          <div class="monitor-card-title">
                            {{ card.deviceName || `监控截图 ${card.order}` }}
                          </div>
                        </div>

                        <div class="monitor-card-meta">
                          <span>{{ formatImageTime(card.dateCreated) }}</span>
                        </div>

                        <div v-if="card.analyzeStatus === 'analyzing'" class="card-pending">
                          <a-skeleton :paragraph="{ rows: 3 }" active :title="false" />
                        </div>

                        <div v-else-if="card.analyzeStatus === 'failed'" class="card-error">
                          <a-alert
                            type="error"
                            show-icon
                            :message="card.errorMessage || '分析失败'"
                          />
                          <a-button type="link" size="small" @click="retryAnalyzeCard(card.id)">
                            <ReloadOutlined /> 重试该张
                          </a-button>
                        </div>

                        <div v-else-if="card.analyzeStatus === 'success'">
                          <div class="card-summary">
                            <a-tag :color="getDiseaseColor(card.analyzeResult?.diseaseName || '')">
                              {{ card.analyzeResult?.diseaseName || '未知' }}
                            </a-tag>
                            <span class="confidence-text">
                              置信度 {{ Math.round((card.analyzeResult?.confidence || 0) * 100) }}%
                            </span>
                          </div>

                          <div class="card-detail">
                            <p>{{ card.analyzeResult?.description || '暂无描述' }}</p>
                            <div class="detail-section">
                              <div class="detail-title">防治建议</div>
                              <a-tag
                                v-for="(item, index) in card.analyzeResult?.preventionMeasures || []"
                                :key="`${card.id}-measure-${index}`"
                                color="blue"
                              >
                                {{ item }}
                              </a-tag>
                            </div>
                            <div v-if="card.analyzeResult?.summary" class="detail-summary">
                              {{ card.analyzeResult?.summary }}
                            </div>
                            <div class="detail-actions">
                              <a-button type="link" size="small" @click="retryAnalyzeCard(card.id)">
                                <ReloadOutlined /> 重新分析
                              </a-button>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>

                  <div v-if="shouldPaginateMonitor" class="pagination-wrap">
                    <a-pagination
                      v-model:current="monitorCurrentPage"
                      :page-size="monitorPageSize"
                      :total="monitorCards.length"
                      size="small"
                      show-less-items
                    />
                  </div>
                </template>
              </a-spin>
            </div>
          </div>
        </a-card>
      </a-col>

      <a-col :span="12">
        <a-card title="孢子捕捉仪 - 早期预警" :bordered="false" class="content-card">
          <template #extra>
            <a-tag color="green">共 {{ sporeImages.length }} 张图片</a-tag>
          </template>

          <div class="image-section">
            <div class="image-gallery">
              <a-spin :spinning="sporeLoading" tip="正在加载孢子图片...">
                <div v-if="sporeImages.length === 0" class="empty-container">
                  <a-empty description="暂无孢子捕捉仪数据" />
                </div>
                <div v-else class="image-grid spore-grid">
                  <div
                    v-for="(image, index) in sporeImages"
                    :key="index"
                    class="image-item spore-image-item"
                    @click="previewImage(image.imageUrl)"
                  >
                    <img :src="image.thumbnail || image.imageUrl" :alt="'孢子图片' + (index + 1)" />
                    <div class="image-overlay">
                      <EyeOutlined class="preview-icon" />
                    </div>
                    <div class="spore-image-meta">
                      <div class="spore-image-title">样本 {{ index + 1 }}</div>
                      <div class="spore-image-time">{{ formatImageTime(image.dateCreated) }}</div>
                    </div>
                  </div>
                </div>
              </a-spin>
            </div>

            <div class="action-bar">
              <span class="action-tip">
                {{ sporeAnalyzeLoading ? '已自动发起综合分析，请稍候...' : '图片加载完成后将自动进行综合分析' }}
              </span>
            </div>
          </div>

          <a-divider v-if="sporeResult" />

          <div v-if="sporeResult" class="analysis-result">
            <a-descriptions title="孢子综合分析结果" bordered :column="2" size="small">
              <a-descriptions-item label="预警等级">
                <a-tag :color="getWarningColor(sporeResult.warningLevel)">
                  {{ sporeResult.warningLevel }}
                </a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="分析图片数">
                {{ sporeResult.statistics?.totalImages || 0 }} 张
              </a-descriptions-item>
              <a-descriptions-item label="时间范围" :span="2">
                {{ sporeResult.statistics?.timeRange || timeRange }}
              </a-descriptions-item>
            </a-descriptions>

            <a-card title="病害早期预警" size="small" style="margin-top: 16px">
              <a-list :data-source="sporeResult.diseaseWarnings" size="small">
                <template #renderItem="{ item }">
                  <a-list-item>
                    <a-list-item-meta>
                      <template #title>
                        <a-tag :color="getRiskColor(item.riskLevel)">{{ item.riskLevel }}</a-tag>
                        {{ item.diseaseName }}
                      </template>
                      <template #description>
                        <div>{{ item.description }}</div>
                        <div style="color: #1890ff; margin-top: 4px">建议：{{ item.suggestion }}</div>
                      </template>
                    </a-list-item-meta>
                  </a-list-item>
                </template>
              </a-list>
            </a-card>

            <a-card title="科学用药指导" size="small" style="margin-top: 16px">
              <a-table
                :columns="medicationColumns"
                :data-source="sporeResult.medicationGuides"
                :pagination="false"
                size="small"
                row-key="pesticideName"
              />
            </a-card>

            <a-alert
              v-if="sporeResult.summary"
              :message="sporeResult.summary"
              type="info"
              show-icon
              style="margin-top: 16px"
            />
          </div>
        </a-card>
      </a-col>
    </a-row>

    <a-modal
      v-model:open="previewVisible"
      title="图片预览"
      :footer="null"
      width="800px"
    >
      <img :src="previewImageUrl" style="width: 100%" alt="预览图片" />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { message } from 'ant-design-vue';
import { DownOutlined, DownloadOutlined, EyeOutlined, ReloadOutlined } from '@ant-design/icons-vue';
import dayjs from 'dayjs';
import { useSelectStore } from '/@/store/selectStore';
import FlvPlayer from '../work-area/components/FlvPlayer.vue';
import PtzControlPanel from '../work-area/components/PtzControlPanel.vue';
import { getVideoDevices, getVideoStream } from '../work-area/workArea.api';
import {
  DiseaseAnalysisResponse,
  getAnalyzeDiseaseTask,
  getAnalyzeSporeTask,
  getMonitorImages,
  getSporeImages,
  SporeAnalysisResponse,
  submitAnalyzeDisease,
  submitAnalyzeSpore,
  type ImageInfo,
} from './diseaseControl.api';

type AnalyzeStatus = 'pending' | 'analyzing' | 'success' | 'failed';
type ImageLoadStatus = 'loading' | 'loaded' | 'error';

interface MonitorAnalysisCard extends ImageInfo {
  id: string;
  order: number;
  deviceName?: string;
  analyzeResult: DiseaseAnalysisResponse | null;
  analyzeStatus: AnalyzeStatus;
  imageStatus: ImageLoadStatus;
  errorMessage: string;
}

interface VideoMonitorDevice {
  equipmentCode: string;
  equipmentName: string;
  channelNum: string;
  streamUrl?: string | null;
}

const ANALYZE_CHUNK_SIZE = 6;
const MONITOR_PAGE_SIZE = 24;
const ANALYSIS_POLL_INTERVAL = 2000;
const ANALYSIS_MAX_POLL_COUNT = 90;

const selectStore = useSelectStore();

const dateRange = ref<[string, string]>([
  dayjs().subtract(7, 'day').format('YYYY-MM-DD'),
  dayjs().format('YYYY-MM-DD'),
]);

const timeRange = computed(() => `${dateRange.value[0]} 至 ${dateRange.value[1]}`);

const monitorCards = ref<MonitorAnalysisCard[]>([]);
const sporeImages = ref<ImageInfo[]>([]);
const videoDevices = ref<VideoMonitorDevice[]>([]);

const monitorLoading = ref(false);
const monitorAnalyzeRunning = ref(false);
const sporeLoading = ref(false);
const sporeAnalyzeLoading = ref(false);
const videoLoading = ref(false);

const monitorLoadError = ref('');
const sporeResult = ref<SporeAnalysisResponse | null>(null);

const previewVisible = ref(false);
const previewImageUrl = ref('');

const monitorCurrentPage = ref(1);
const monitorBatchVersion = ref(0);
const analyzedBatchVersion = ref(-1);

const medicationColumns = [
  { title: '农药名称', dataIndex: 'pesticideName', width: 120 },
  { title: '使用剂量', dataIndex: 'dosage', width: 100 },
  { title: '使用时机', dataIndex: 'timing', width: 120 },
  { title: '使用方法', dataIndex: 'method', width: 120 },
  { title: '注意事项', dataIndex: 'precautions' },
];

const monitorWorkflowLoading = computed(() => monitorLoading.value || monitorAnalyzeRunning.value);
const monitorSuccessCount = computed(() => monitorCards.value.filter((item) => item.analyzeStatus === 'success').length);
const monitorFailedCount = computed(() => monitorCards.value.filter((item) => item.analyzeStatus === 'failed').length);
const analyzableMonitorCount = computed(() => monitorCards.value.length);
const shouldPaginateMonitor = computed(() => monitorCards.value.length > 50);
const pagedMonitorCards = computed(() => {
  if (!shouldPaginateMonitor.value) {
    return monitorCards.value;
  }
  const start = (monitorCurrentPage.value - 1) * MONITOR_PAGE_SIZE;
  return monitorCards.value.slice(start, start + MONITOR_PAGE_SIZE);
});
const monitorWorkflowMessage = computed(() => {
  if (monitorLoadError.value) {
    return '截图加载失败，请重试';
  }
  if (monitorLoading.value) {
    return '正在加载监控截图...';
  }
  if (monitorAnalyzeRunning.value) {
    return `正在自动分析病害，已完成 ${monitorSuccessCount.value}/${analyzableMonitorCount.value}`;
  }
  if (!monitorCards.value.length) {
    return '暂无监控截图数据';
  }
  if (monitorFailedCount.value > 0) {
    return `自动分析完成，${monitorFailedCount.value} 张图片分析失败，可在卡片内单独重试`;
  }
  return '监控截图加载完成，病害分析结果已同步展示';
});

watch(
  () => [selectStore.selectedBase?.baseId, selectStore.selectedBase?.baseName],
  ([baseId, baseName]) => {
    if (baseId && baseName) {
      void refreshData();
      return;
    }
    resetPageState();
  },
  { immediate: true }
);

watch(
  () => [monitorCards.value.length, monitorLoadError.value, monitorBatchVersion.value],
  ([cardCount, loadError]) => {
    if (!cardCount || loadError) {
      return;
    }
    if (analyzedBatchVersion.value === monitorBatchVersion.value) {
      return;
    }
    analyzedBatchVersion.value = monitorBatchVersion.value;
    void autoAnalyzeMonitorCards();
  }
);

function resetPageState() {
  monitorCards.value = [];
  sporeImages.value = [];
  videoDevices.value = [];
  sporeResult.value = null;
  monitorLoadError.value = '';
  monitorCurrentPage.value = 1;
  monitorBatchVersion.value += 1;
}

function createMonitorCard(image: ImageInfo, index: number): MonitorAnalysisCard {
  return {
    ...image,
    id: `${image.imageUrl}-${index}`,
    order: index + 1,
    analyzeResult: null,
    analyzeStatus: 'pending',
    imageStatus: 'loading',
    errorMessage: '',
  };
}

async function loadMonitorImages() {
  const baseId = selectStore.selectedBase?.baseId;
  if (!baseId) {
    monitorCards.value = [];
    monitorLoadError.value = '';
    return;
  }

  monitorLoading.value = true;
  monitorLoadError.value = '';
  monitorCards.value = [];
  monitorCurrentPage.value = 1;
  monitorBatchVersion.value += 1;
  analyzedBatchVersion.value = -1;

  try {
    const images = await getMonitorImages({
      baseId: String(baseId),
    });
    monitorCards.value = (images || []).map(createMonitorCard);
    if (!monitorCards.value.length) {
      message.info('当前基地暂无监控截图');
    }
  } catch (e) {
    console.error('加载监控截图失败', e);
    monitorCards.value = [];
    monitorLoadError.value = '截图加载失败，点击重试';
    message.error('加载监控截图失败');
  } finally {
    monitorLoading.value = false;
  }
}

async function loadSporeImages() {
  const baseName = selectStore.selectedBase?.baseName;
  if (!baseName) {
    sporeImages.value = [];
    sporeResult.value = null;
    return;
  }

  sporeLoading.value = true;
  try {
    const res = await getSporeImages({
      baseName,
    });
    sporeImages.value = res || [];
    sporeResult.value = null;
    if (sporeImages.value.length > 0) {
      void handleAnalyzeSpore(baseName, sporeImages.value.map((item) => item.imageUrl).filter(Boolean));
    }
  } catch (e) {
    console.error('加载孢子图片失败', e);
    sporeImages.value = [];
    message.error('加载孢子图片失败');
  } finally {
    sporeLoading.value = false;
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

async function refreshData() {
  if (!selectStore.selectedBase?.baseId || !selectStore.selectedBase?.baseName) {
    resetPageState();
    return;
  }

  try {
    await Promise.all([loadMonitorImages(), loadSporeImages(), loadVideoMonitorDevices()]);
  } catch (e) {
    console.error('刷新病害防控数据失败', e);
    message.error('刷新病害防控数据失败');
  }
}

function markMonitorImageLoaded(id: string) {
  const target = monitorCards.value.find((item) => item.id === id);
  if (!target || target.imageStatus === 'loaded') {
    return;
  }
  target.imageStatus = 'loaded';
}

function markMonitorImageError(id: string) {
  const target = monitorCards.value.find((item) => item.id === id);
  if (!target) {
    return;
  }
  target.imageStatus = 'error';
  target.analyzeStatus = 'failed';
  target.errorMessage = '截图加载失败';
}

async function autoAnalyzeMonitorCards() {
  const baseName = selectStore.selectedBase?.baseName;
  const pendingCards = monitorCards.value.filter((item) => item.analyzeStatus === 'pending');
  if (!pendingCards.length) {
    return;
  }

  monitorAnalyzeRunning.value = true;
  try {
    const chunks = chunkArray(pendingCards, ANALYZE_CHUNK_SIZE);
    for (const chunk of chunks) {
      chunk.forEach((item) => {
        item.analyzeStatus = 'analyzing';
        item.errorMessage = '';
      });
      const settled = await Promise.allSettled(
        chunk.map((item) => analyzeOneMonitorCard(item.imageUrl, baseName))
      );
      settled.forEach((result, index) => {
        const target = chunk[index];
        if (result.status === 'fulfilled') {
          target.analyzeResult = result.value;
          target.analyzeStatus = 'success';
        } else {
          target.analyzeStatus = 'failed';
          target.errorMessage = getErrorMessage(result.reason);
        }
      });
    }

    if (monitorFailedCount.value > 0) {
      message.warning(`病害分析完成，${monitorFailedCount.value} 张图片分析失败，可单独重试`);
    } else {
      message.success('病害自动分析完成');
    }
  } finally {
    monitorAnalyzeRunning.value = false;
  }
}

async function retryAnalyzeCard(id: string) {
  const target = monitorCards.value.find((item) => item.id === id);
  const baseName = selectStore.selectedBase?.baseName;
  if (!target || target.imageStatus !== 'loaded') {
    return;
  }

  target.analyzeStatus = 'analyzing';
  target.errorMessage = '';
  try {
    target.analyzeResult = await analyzeOneMonitorCard(target.imageUrl, baseName);
    target.analyzeStatus = 'success';
    message.success('该截图已重新分析');
  } catch (e) {
    target.analyzeStatus = 'failed';
    target.errorMessage = getErrorMessage(e);
    message.error('该截图分析失败');
  }
}

async function analyzeOneMonitorCard(imageUrl: string, baseName?: string) {
  const submitResult = await submitAnalyzeDisease([imageUrl], baseName);
  return pollDiseaseTask(submitResult.taskId);
}

function chunkArray<T>(items: T[], chunkSize: number) {
  const chunks: T[][] = [];
  for (let i = 0; i < items.length; i += chunkSize) {
    chunks.push(items.slice(i, i + chunkSize));
  }
  return chunks;
}

function getErrorMessage(error: unknown) {
  if (error instanceof Error && error.message) {
    return error.message;
  }
  return '分析失败';
}

function retryMonitorWorkflow() {
  void loadMonitorImages();
}

async function handleAnalyzeSpore(
  baseName = selectStore.selectedBase?.baseName,
  imageUrls = sporeImages.value.map((item) => item.imageUrl).filter(Boolean)
) {
  if (!baseName) {
    message.warning('请先选择基地');
    return;
  }
  if (!imageUrls.length) {
    return;
  }

  sporeAnalyzeLoading.value = true;
  try {
    const submitResult = await submitAnalyzeSpore({
      baseName,
      imageUrls,
    });
    const res = await pollSporeTask(submitResult.taskId);
    if (baseName !== selectStore.selectedBase?.baseName) {
      return;
    }
    sporeResult.value = res;
    message.success('孢子综合分析完成');
  } catch (e) {
    console.error('孢子综合分析失败', e);
    message.error('孢子综合分析失败');
  } finally {
    sporeAnalyzeLoading.value = false;
  }
}

async function pollDiseaseTask(taskId: string) {
  for (let count = 0; count < ANALYSIS_MAX_POLL_COUNT; count += 1) {
    const task = await getAnalyzeDiseaseTask(taskId);
    if (task.status === 'SUCCESS') {
      return task.result || ({} as DiseaseAnalysisResponse);
    }
    if (task.status === 'FAILED') {
      throw new Error(task.errorMessage || '病害分析失败');
    }
    await sleep(ANALYSIS_POLL_INTERVAL);
  }
  throw new Error('病害分析超时，请稍后重试');
}

async function pollSporeTask(taskId: string) {
  for (let count = 0; count < ANALYSIS_MAX_POLL_COUNT; count += 1) {
    const task = await getAnalyzeSporeTask(taskId);
    if (task.status === 'SUCCESS') {
      return task.result || ({} as SporeAnalysisResponse);
    }
    if (task.status === 'FAILED') {
      throw new Error(task.errorMessage || '孢子综合分析失败');
    }
    await sleep(ANALYSIS_POLL_INTERVAL);
  }
  throw new Error('孢子综合分析超时，请稍后重试');
}

function sleep(ms: number) {
  return new Promise((resolve) => window.setTimeout(resolve, ms));
}

function previewImage(url: string) {
  if (!url) return;
  previewImageUrl.value = url;
  previewVisible.value = true;
}

function formatImageTime(value?: string) {
  if (!value) return '暂无时间';
  return String(value);
}

function getDiseaseColor(disease: string): string {
  if (!disease || disease === '健康') return 'green';
  if (disease === '未知') return 'default';
  return 'red';
}

function getSeverityColor(severity?: string): string {
  switch (severity) {
    case '轻度':
      return 'green';
    case '中度':
      return 'orange';
    case '重度':
      return 'red';
    default:
      return 'processing';
  }
}

function getWarningColor(level: string): string {
  switch (level) {
    case '低':
      return 'green';
    case '中':
      return 'orange';
    case '高':
      return 'red';
    default:
      return 'default';
  }
}

function getRiskColor(risk: string): string {
  switch (risk) {
    case '低风险':
      return 'green';
    case '中风险':
      return 'orange';
    case '高风险':
      return 'red';
    default:
      return 'default';
  }
}

function handleExportMenuClick({ key }: { key: string }) {
  if (!monitorCards.value.length) {
    message.warning('暂无可导出的监控截图分析结果');
    return;
  }

  if (key === 'excel') {
    exportMonitorReportExcel();
    return;
  }
  if (key === 'pdf') {
    exportMonitorReportPdf();
  }
}

function exportMonitorReportExcel() {
  const headers = ['序号', '图片URL', '图片时间', '图片状态', '分析状态', '病害名称', '危害程度', '置信度', '病害描述', '综合总结'];
  const rows = monitorCards.value.map((item) => [
    item.order,
    item.imageUrl,
    formatImageTime(item.dateCreated),
    item.imageStatus,
    item.analyzeStatus,
    item.analyzeResult?.diseaseName || '',
    item.analyzeResult?.severity || '',
    item.analyzeResult?.confidence != null ? `${Math.round(item.analyzeResult.confidence * 100)}%` : '',
    sanitizeCsvText(item.analyzeResult?.description || item.errorMessage || ''),
    sanitizeCsvText(item.analyzeResult?.summary || ''),
  ]);
  const csvContent = [headers, ...rows]
    .map((row) => row.map((cell) => `"${String(cell ?? '').replace(/"/g, '""')}"`).join(','))
    .join('\n');
  downloadBlob(new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' }), '病害分析报告.csv');
}

function exportMonitorReportPdf() {
  const reportHtml = `
    <html>
      <head>
        <meta charset="utf-8" />
        <title>病害分析报告</title>
        <style>
          body { font-family: Arial, sans-serif; padding: 24px; color: #333; }
          h1 { font-size: 20px; margin-bottom: 12px; }
          table { width: 100%; border-collapse: collapse; font-size: 12px; }
          th, td { border: 1px solid #ddd; padding: 8px; vertical-align: top; }
          th { background: #f5f5f5; }
        </style>
      </head>
      <body>
        <h1>病害分析报告</h1>
        <p>基地：${selectStore.selectedBase?.baseName || '未选择'}</p>
        <p>导出时间：${dayjs().format('YYYY-MM-DD HH:mm:ss')}</p>
        <table>
          <thead>
            <tr>
              <th>序号</th>
              <th>病害名称</th>
              <th>危害程度</th>
              <th>置信度</th>
              <th>描述</th>
              <th>总结</th>
            </tr>
          </thead>
          <tbody>
            ${monitorCards.value
              .map(
                (item) => `
                  <tr>
                    <td>${item.order}</td>
                    <td>${item.analyzeResult?.diseaseName || item.errorMessage || '未完成'}</td>
                    <td>${item.analyzeResult?.severity || '-'}</td>
                    <td>${item.analyzeResult?.confidence != null ? `${Math.round(item.analyzeResult.confidence * 100)}%` : '-'}</td>
                    <td>${escapeHtml(item.analyzeResult?.description || '')}</td>
                    <td>${escapeHtml(item.analyzeResult?.summary || '')}</td>
                  </tr>`
              )
              .join('')}
          </tbody>
        </table>
      </body>
    </html>
  `;

  const exportWindow = window.open('', '_blank');
  if (!exportWindow) {
    message.error('浏览器拦截了导出窗口，请允许弹窗后重试');
    return;
  }
  exportWindow.document.write(reportHtml);
  exportWindow.document.close();
  exportWindow.focus();
  exportWindow.print();
}

function sanitizeCsvText(text: string) {
  return text.replace(/\r?\n/g, ' ').trim();
}

function escapeHtml(text: string) {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

function downloadBlob(blob: Blob, fileName: string) {
  const link = document.createElement('a');
  link.href = URL.createObjectURL(blob);
  link.download = fileName;
  link.click();
  URL.revokeObjectURL(link.href);
}
</script>

<style lang="less" scoped>
.disease-control-page {
  padding: 16px;
  background: #f0f2f5;
  min-height: calc(100vh - 100px);
}

.main-content {
  height: calc(100vh - 140px);
}

.content-card {
  height: 100%;
  overflow: hidden;

  :deep(.ant-card-body) {
    height: calc(100% - 57px);
    overflow-y: auto;
    padding: 16px;
  }
}

.card-extra {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.image-section {
  .image-gallery {
    min-height: 200px;
    max-height: 760px;
    overflow-y: auto;
    border: 1px solid #f0f0f0;
    border-radius: 8px;
    padding: 12px;
    background: #fafafa;
  }

  .empty-container {
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 220px;
  }

  .image-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 16px;
  }

  .action-bar {
    margin-top: 12px;
    display: flex;
    align-items: center;
  }
}

.workflow-tip {
  margin-bottom: 12px;
}

.video-section {
  margin-bottom: 16px;
  padding: 12px;
  border-radius: 10px;
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
  color: #262626;
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
}

.video-monitor-card {
  position: relative;
  min-height: 360px;
  overflow: hidden;
  border-radius: 12px;
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

.monitor-card {
  border-radius: 10px;
  overflow: hidden;
  background: #fff;
  border: 1px solid #f0f0f0;
}

.image-item {
  position: relative;
  aspect-ratio: 16 / 10;
  overflow: hidden;
  cursor: pointer;
  background: #000;

  &:hover {
    .image-overlay {
      opacity: 1;
    }
  }

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }

  .image-overlay {
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.3);
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: opacity 0.3s;
  }

  .preview-icon {
    font-size: 24px;
    color: #fff;
  }
}

.image-status {
  position: absolute;
  top: 10px;
  right: 10px;
}

.monitor-card-body {
  padding: 12px;
}

.monitor-card-head {
  display: flex;
  justify-content: space-between;
  gap: 8px;
}

.monitor-card-title {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
}

.monitor-card-meta {
  margin-top: 4px;
  color: #8c8c8c;
  font-size: 12px;
}

.card-summary {
  margin-top: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.confidence-text {
  font-size: 12px;
  color: #595959;
}

.card-detail {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed #f0f0f0;

  p {
    margin-bottom: 8px;
    color: #595959;
  }
}

.detail-section {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  align-items: flex-start;
}

.detail-title {
  min-width: 60px;
  font-size: 12px;
  color: #8c8c8c;
  line-height: 24px;
}

.detail-summary {
  margin-top: 8px;
  font-size: 12px;
  color: #595959;
}

.detail-actions {
  margin-top: 8px;
}

.card-error,
.card-pending {
  margin-top: 10px;
}

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.action-tip {
  margin-left: 12px;
  color: #666;
  font-size: 13px;
}

.image-section .image-grid.spore-grid {
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 6px;
}

.spore-image-item {
  aspect-ratio: auto;
  height: 72px;
  border: 1px solid #e8e8e8;
  background: #fff;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.spore-image-meta {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 3px 5px;
  color: #fff;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.02), rgba(0, 0, 0, 0.58));
}

.spore-image-title {
  font-size: 11px;
  font-weight: 500;
  line-height: 1.2;
}

.spore-image-time {
  display: none;
}

.analysis-result {
  margin-top: 16px;
}

@media (max-width: 1400px) {
  .image-section .image-grid:not(.spore-grid) {
    grid-template-columns: 1fr;
  }
}
</style>
