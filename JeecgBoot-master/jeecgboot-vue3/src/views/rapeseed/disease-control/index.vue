<template>
  <div class="disease-control-page">
    <a-row :gutter="16" class="main-content">
      <a-col :span="12">
        <a-card title="监控截图 - 病害分析" :bordered="false" class="content-card">
          <template #extra>
            <a-tag color="blue">共 {{ monitorImages.length }} 张图片</a-tag>
          </template>
          
          <div class="image-section">
            <div class="image-gallery">
              <a-spin :spinning="monitorLoading">
                <div v-if="monitorImages.length === 0" class="empty-container">
                  <a-empty description="暂无监控截图数据" />
                </div>
                <div v-else class="image-grid">
                  <div
                    v-for="(image, index) in monitorImages"
                    :key="index"
                    class="image-item"
                    @click="previewImage(image.imageUrl)"
                  >
                    <img :src="image.imageUrl" :alt="'监控截图' + (index + 1)" />
                    <div class="image-overlay">
                      <EyeOutlined class="preview-icon" />
                    </div>
                  </div>
                </div>
              </a-spin>
            </div>

            <div class="action-bar">
              <a-button 
                type="primary" 
                :disabled="monitorImages.length === 0" 
                :loading="analyzeLoading"
                @click="handleAnalyzeMonitorBatch"
              >
                <ApiOutlined /> 批量AI病害分析
              </a-button>
              <span style="margin-left: 12px; color: #666; font-size: 13px">
                将综合分析所有监控截图
              </span>
            </div>
          </div>

          <a-divider v-if="diseaseResult" />

          <div v-if="diseaseResult" class="analysis-result">
            <a-descriptions title="病害分析结果" bordered :column="2" size="small">
              <a-descriptions-item label="病害名称">
                <a-tag :color="getDiseaseColor(diseaseResult.diseaseName)">
                  {{ diseaseResult.diseaseName }}
                </a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="危害程度">
                <a-tag :color="getSeverityColor(diseaseResult.severity)">
                  {{ diseaseResult.severity }}
                </a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="置信度">
                <a-progress 
                  :percent="Math.round((diseaseResult.confidence || 0) * 100)" 
                  :stroke-color="getProgressColor(diseaseResult.confidence)"
                  size="small"
                />
              </a-descriptions-item>
              <a-descriptions-item label="最佳防治时期">
                {{ diseaseResult.bestPreventionTime || '暂无' }}
              </a-descriptions-item>
              <a-descriptions-item label="病害描述" :span="2">
                {{ diseaseResult.description }}
              </a-descriptions-item>
            </a-descriptions>

            <a-card title="防治建议" size="small" style="margin-top: 16px">
              <a-list :data-source="diseaseResult.preventionMeasures" size="small">
                <template #renderItem="{ item, index }">
                  <a-list-item>
                    <a-tag color="blue">{{ index + 1 }}</a-tag>
                    {{ item }}
                  </a-list-item>
                </template>
              </a-list>
            </a-card>

            <a-card title="推荐农药" size="small" style="margin-top: 16px">
              <a-table
                :columns="pesticideColumns"
                :data-source="diseaseResult.recommendedPesticides"
                :pagination="false"
                size="small"
                row-key="name"
              />
            </a-card>

            <a-alert 
              v-if="diseaseResult.summary" 
              :message="diseaseResult.summary" 
              type="info" 
              show-icon 
              style="margin-top: 16px"
            />
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
              <a-spin :spinning="sporeLoading">
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
              <a-button 
                type="primary" 
                :disabled="sporeImages.length === 0" 
                :loading="sporeAnalyzeLoading"
                @click="handleAnalyzeSpore"
              >
                <ApiOutlined /> 综合分析
              </a-button>
              <span style="margin-left: 12px; color: #666; font-size: 13px">
                将综合分析所有孢子图片
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
import { ApiOutlined, EyeOutlined } from '@ant-design/icons-vue';
import dayjs from 'dayjs';
import { useSelectStore } from '/@/store/selectStore';
import {
  getSporeImages,
  getMonitorImages,
  analyzeMonitorBatch,
  analyzeSpore,
  DiseaseAnalysisResponse,
  SporeAnalysisResponse,
  type ImageInfo,
} from './diseaseControl.api';

const selectStore = useSelectStore();

const dateRange = ref<[string, string]>([
  dayjs().subtract(7, 'day').format('YYYY-MM-DD'),
  dayjs().format('YYYY-MM-DD'),
]);

const monitorImages = ref<ImageInfo[]>([]);
const sporeImages = ref<ImageInfo[]>([]);

const monitorLoading = ref(false);
const sporeLoading = ref(false);
const analyzeLoading = ref(false);
const sporeAnalyzeLoading = ref(false);

const timeRange = computed(() => `${dateRange.value[0]} 至 ${dateRange.value[1]}`);

const diseaseResult = ref<DiseaseAnalysisResponse | null>(null);
const sporeResult = ref<SporeAnalysisResponse | null>(null);

const previewVisible = ref(false);
const previewImageUrl = ref('');

const pesticideColumns = [
  { title: '农药名称', dataIndex: 'name', width: 120 },
  { title: '使用剂量', dataIndex: 'dosage', width: 100 },
  { title: '使用方法', dataIndex: 'usage', width: 150 },
  { title: '注意事项', dataIndex: 'precautions' },
];

const medicationColumns = [
  { title: '农药名称', dataIndex: 'pesticideName', width: 120 },
  { title: '使用剂量', dataIndex: 'dosage', width: 100 },
  { title: '使用时机', dataIndex: 'timing', width: 120 },
  { title: '使用方法', dataIndex: 'method', width: 120 },
  { title: '注意事项', dataIndex: 'precautions' },
];

watch(
  () => [selectStore.selectedBase?.baseId, selectStore.selectedBase?.baseName],
  ([baseId, baseName]) => {
    if (baseId && baseName) {
      refreshData();
      return;
    }
    resetPageState();
  },
  { immediate: true }
);

function resetPageState() {
  monitorImages.value = [];
  sporeImages.value = [];
  diseaseResult.value = null;
  sporeResult.value = null;
}

async function loadMonitorImages() {
  const baseId = selectStore.selectedBase?.baseId;
  if (!baseId) {
    monitorImages.value = [];
    diseaseResult.value = null;
    return;
  }

  monitorLoading.value = true;
  try {
    monitorImages.value = await getMonitorImages({
      baseId: String(baseId),
    });
    diseaseResult.value = null;
  } catch (e) {
    console.error('加载监控截图失败', e);
    monitorImages.value = [];
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
  } catch (e) {
    console.error('加载孢子图片失败', e);
    sporeImages.value = [];
    message.error('加载孢子图片失败');
  } finally {
    sporeLoading.value = false;
  }
}

async function refreshData() {
  if (!selectStore.selectedBase?.baseId || !selectStore.selectedBase?.baseName) {
    resetPageState();
    return;
  }

  try {
    await Promise.all([loadMonitorImages(), loadSporeImages()]);
  } catch (e) {
    console.error('刷新病害防控数据失败', e);
    message.error('刷新病害防控数据失败');
  }
}

async function handleAnalyzeMonitorBatch() {
  const baseId = selectStore.selectedBase?.baseId;
  if (!baseId) {
    message.warning('请先选择基地');
    return;
  }

  analyzeLoading.value = true;
  try {
    const res = await analyzeMonitorBatch({
      baseId: String(baseId),
    });
    diseaseResult.value = res;
    message.success('批量病害分析完成');
  } catch (e) {
    console.error('批量病害分析失败', e);
    message.error('批量病害分析失败');
  } finally {
    analyzeLoading.value = false;
  }
}

async function handleAnalyzeSpore() {
  const baseName = selectStore.selectedBase?.baseName;
  if (!baseName) {
    message.warning('请先选择基地');
    return;
  }

  sporeAnalyzeLoading.value = true;
  try {
    const res = await analyzeSpore({
      baseName,
    });
    sporeResult.value = res;
    message.success('孢子综合分析完成');
  } catch (e) {
    console.error('孢子综合分析失败', e);
    message.error('孢子综合分析失败');
  } finally {
    sporeAnalyzeLoading.value = false;
  }
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

function getSeverityColor(severity: string): string {
  switch (severity) {
    case '轻度': return 'green';
    case '中度': return 'orange';
    case '重度': return 'red';
    default: return 'default';
  }
}

function getWarningColor(level: string): string {
  switch (level) {
    case '低': return 'green';
    case '中': return 'orange';
    case '高': return 'red';
    default: return 'default';
  }
}

function getRiskColor(risk: string): string {
  switch (risk) {
    case '低风险': return 'green';
    case '中风险': return 'orange';
    case '高风险': return 'red';
    default: return 'default';
  }
}

function getProgressColor(confidence: number): string {
  if (!confidence) return '#1890ff';
  if (confidence >= 0.8) return '#52c41a';
  if (confidence >= 0.6) return '#faad14';
  return '#ff4d4f';
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

.image-section {
  .image-gallery {
    min-height: 200px;
    max-height: 420px;
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
    min-height: 200px;
  }
  
  .image-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 12px;
  }
  
  .image-item {
    position: relative;
    aspect-ratio: 1 / 1;
    border-radius: 8px;
    overflow: hidden;
    cursor: pointer;
    border: 2px solid transparent;
    transition: all 0.3s;
    
    &:hover {
      border-color: #1890ff;
      
      .image-overlay {
        opacity: 1;
      }
    }
    
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    
    .image-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.3);
      display: flex;
      align-items: center;
      justify-content: center;
      opacity: 0;
      transition: opacity 0.3s;
      
      .preview-icon {
        font-size: 24px;
        color: #fff;
      }
    }
  }
  
  .action-bar {
    margin-top: 12px;
    display: flex;
    align-items: center;
  }
}

.spore-grid {
  grid-template-columns: repeat(3, 1fr);
}

.spore-image-item {
  aspect-ratio: 4 / 3;
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
  padding: 8px 10px;
  color: #fff;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.02), rgba(0, 0, 0, 0.58));
}

.spore-image-title {
  font-size: 13px;
  font-weight: 500;
}

.spore-image-time {
  margin-top: 2px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.85);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.analysis-result {
  margin-top: 16px;
}
</style>
