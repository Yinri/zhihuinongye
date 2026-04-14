<template>
  <div class="insect-control-page">
    <a-card class="disease-card" :bordered="false">
      <a-row class="main-content-row">
        <!-- 实时病害图像 -->
        <a-col :span="8">
          <a-card title="实时病害图像" :bordered="false" class="inner-card">
            <div class="image-container">
              <img
                class="disease-image"
                :src="latestImageUrl"
                alt="实时虫情图像"
                v-if="latestImageUrl"
              />
              <div v-else class="no-image">暂无图像</div>
            </div>
            <div class="update-time">更新于：{{ latestDate }}</div>
          </a-card>
          <!-- Add this inside the main template, after the existing cards -->
          <a-card title="病害图像上传" :bordered="false" class="upload-card">
          <div class="upload-section">
            <!-- 本地上传 -->
            <a-upload
              name="file"
              :multiple="false"
              :before-upload="beforeUpload"
              :show-upload-list="false"
              accept="image/*"
            >
              <a-button>
                <UploadOutlined />
                选择本地图片
              </a-button>
            </a-upload>

            <!-- 固定线上图片选择 -->
            <div style="margin-top:12px;">
              <span>选择线上图片：</span>
              <a-button
                v-for="(url, index) in fixedUrls"
                :key="index"
                size="small"
                style="margin-right:8px;"
                @click="selectOnlineImage(url)"
              >
                图片 {{ index + 1 }}
              </a-button>
            </div>

            <!-- 图片预览 -->
            <div v-if="previewImage" class="image-preview" style="margin-top:12px;">
              <img :src="previewImage" alt="Preview" class="preview-img" />
              <div style="margin-top:8px;">
                <a-button @click="submitImage" type="primary" class="submit-btn">
                  提交图像
                </a-button>
                <a-button @click="clearSelection" style="margin-left:8px">
                  取消
                </a-button>
              </div>
            </div>
          </div>
        </a-card>
        </a-col>
        <a-col :span="8">
          <a-card title="实时病害预测" :bordered="false" class="inner-card">
            <div class="disease-row">
              <!-- 仅提交后显示图片 -->
              <div v-if="uploadedImage" class="image-preview">
                <img :src="uploadedImage" alt="Uploaded" class="image-container" />
                <!-- Enhanced prediction info styling -->
                <div class="prediction-info enhanced">
                  <div class="prediction-label">病害类型</div>
                  <div class="prediction-value">{{ predictedClass || '未检测' }}</div>
                </div>
              </div>
              <!-- Fallback for when no image is uploaded -->
              <div v-else class="prediction-info enhanced">
                <div class="prediction-label">病害类型</div>
                <div class="prediction-value">{{ predictedClass || '未检测' }}</div>
              </div>
            </div>
            <div class="update-time">更新于：{{ lastTime || '未更新' }}</div>
          </a-card>
        </a-col>

        <!-- 农药选择及分析结果 -->
        <a-col :span="8">
          <a-card title="农药选择与分析" :bordered="false" class="inner-card">
            <!-- 触发分析按钮 -->
            <a-button type="primary" @click="handleLLMAnalysis" style="margin-bottom: 12px;">
              Ai生成防治建议
            </a-button>
            <!-- 分析结果展示（来自病害报告） -->
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
            <!-- 农药选择下拉框，选择后直接显示剂量 -->
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

    <a-card title="历史病害记录" :bordered="false" class="history-card">
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
            <!-- 右侧信息 -->
            <div class="record-info">
              <div class="record-time">防控时间：{{ record.time }}</div>

              <div class="pest-entry">
                <div class="disease-name">病害名称：{{ record.disease }}</div>
              </div>
              <div class="common-info">
                农药：{{ record.pesticide }} &nbsp; | &nbsp; 剂量：{{ record.dosage }}
              </div>
              <div class="common-info">
                施用方式：{{ record.method }}
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
import {getFirstDisease,getPesticideList,addPestControlRecord,queryHistoryByTimeRange,uploadDiseaseImage,diseaseAnalysis} from "@/views/rapeseed/disease-control/diseaseControl.api";
import { UploadOutlined } from '@ant-design/icons-vue'


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

// --- 实时虫情图像 ---
const latestImageUrl = ref('');
const latestDate = ref('');
const fetchDiseaseData = async () => {
  try {
    const result = await getFirstDisease();
    console.log(result)
    // 判断数据是否存在
    if (result && Array.isArray(result) && result.length > 0) {
      // 后端返回已经按时间排序（最新在前面）
      const latestDisease = result[0];
      // 绑定给前端显示
      latestImageUrl.value = latestDisease.image_url;
      latestDate.value = new Date().toLocaleString();
    } else {
      console.warn('后端没有返回病害数据');
    }
  } catch (err) {
    console.error('获取病害数据失败:', err);
  }
};
onMounted(() => {
  fetchDiseaseData();
});

import { ref } from 'vue';
import { message } from 'ant-design-vue';
import axios from 'axios';
import { getToken } from '/@/utils/auth';

const previewImage = ref<string | null>(null);      // 选择时预览
const uploadedImage = ref<string | null>(null);     // 提交后显示
const selectedFile = ref<File | null>(null);
const predictedClass = ref<string>('');            // 预测结果
const lastTime = ref<string>('');                  // 更新时间
const { success, error, warning } = message;

// 前端校验图片
const beforeUpload = (file: File) => {
  const isImage = file.type.startsWith('image/');
  if (!isImage) {
    error('只能上传图片文件!');
    return false;
  }
  const isLt2M = file.size / 1024 / 1024 < 2;
  if (!isLt2M) {
    error('图片大小不能超过 2MB!');
    return false;
  }

  const reader = new FileReader();
  reader.onload = (e) => {
    previewImage.value = e.target?.result as string; // Base64 保存
  };
  reader.readAsDataURL(file);
  selectedFile.value = file;
  return false; // 阻止自动上传
};

const fixedUrls = [
  "https://th.bing.com/th/id/R.a8ef579a3032585305c68056d1e09e7a?rik=gXu6U4xQBrlcMw&riu=http%3a%2f%2fbaidu.zzokq.com%2fuploads%2f220722%2f1-220H20T221142.png&ehk=4Roa11gOvGSE%2fNCPwPfUycwZ%2fHYM2yvMqLFNJSgKCpw%3d&risl=&pid=ImgRaw&r=0",
  "https://th.bing.com/th/id/R.009cc61af3c2c73dc9f15441aad58ce3?rik=Vi1msIp17V8A%2bA&riu=http%3a%2f%2fatt.191.cn%2fattachment%2fMon_0704%2f63_1_e27cfd221e37d16.jpg%3f317&ehk=0bcJpEBwfz6W4YUPoDXPnooR4gZMIiktIzBXdu0UVCI%3d&risl=&pid=ImgRaw&r=0"
 
];
const selectOnlineImage = (url: string) => {
  previewImage.value = url;
  selectedFile.value = null; // 清空本地文件选择
};
const clearSelection = () => {
  previewImage.value = null;
  selectedFile.value = null;
  // 不清空 uploadedImage，保留提交后的显示
};
async function fetchImageToBase64(url: string): Promise<string> {
  const res = await fetch(url);
  const blob = await res.blob();
  return await new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onloadend = () => resolve(reader.result as string);
    reader.onerror = reject;
    reader.readAsDataURL(blob);
  });
}
// 手动上传
const submitImage = async () => {
  if (!previewImage.value) {
    warning('请先选择图片');
    return;
  }

  try {
    let base64: string;

    if (selectedFile.value) {
      // 本地文件已经是 Base64
      base64 = previewImage.value!;
    } else {
      // 线上图片 URL，需要先转换成 Base64
      base64 = await fetchImageToBase64(previewImage.value!);
    }

    const result = await directUploadDiseaseImage(base64);
    predictedClass.value = result;
    uploadedImage.value = previewImage.value;
    lastTime.value = new Date().toLocaleString();
    
  } catch (err: any) {
    error('图像上传失败: ' + (err?.response?.data ?? err.message));
  }
};

async function directUploadDiseaseImage(base64Image: string) {
  try {
    const response = await uploadDiseaseImage(base64Image);
    console.log("Direct Axios - Full Response:", response);

    return response.result;
  } catch (error: any) {
    console.error("Direct Axios - Upload failed:", error);
    throw error;
  }
}
const isAnalysisExpanded = ref(true)   // 控制分析结果展开/收起状态          
const toggleAnalysis = () => {
  isAnalysisExpanded.value = !isAnalysisExpanded.value
}
// 模拟大模型分析
async function handleLLMAnalysis() {
  try {
    const response = await diseaseAnalysis(predictedClass.value, {
      isTransformResponse: false, // 保留原始后端返回
    });

    llmAnalysis.value = response; // 直接赋值后端返回数据
    fetchPesticideOptions();      // 拉取农药列表
    return response;
  } catch (error: any) {
    console.error("LLM Analysis failed:", error);
    throw error;
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
    message.error('获取农药列表失败')
  }
}

const handlePesticideChange = (value) => {
  // 清空剂量和方法字段，让用户自己填写
  pesticideForm.value.dosage = '';
  pesticideForm.value.method = '';
}

import { useSelectStore } from '/@/store/selectStore';
const savingRecord = ref(false)
const selectStore = useSelectStore();
const savePesticideRecord = async () => {
  if (!pesticideForm.value.selectedPesticide) {
    message.warning('请选择农药')
    return
  }
  savingRecord.value = true
  try {
    const requestData = {
      baseId:selectStore.selectedBase.baseId,
      plotId:selectStore.selectedPlot.plotId,
      diseaseName:predictedClass.value,
      controlDate:lastTime.value,
      pesticideName: pesticideOptions.value.find(item => item.id === pesticideForm.value.selectedPesticide)?.name,
      pesticideDosage: pesticideForm.value.dosage,
      applicationMethod: pesticideForm.value.method,
    }
    await addPestControlRecord(requestData);
    message.success('使用记录保存成功');
    // 重置表单
    pesticideForm.value.selectedPesticide = null;
    pesticideForm.value.dosage = '';
    pesticideForm.value.method = '';
  } catch (error) {
    console.error('保存使用记录失败:', error)
    message.error('保存使用记录失败')
  } finally {
    savingRecord.value = false
  }
}














import dayjs from 'dayjs';

const handleQueryHistory = async () => {
  if (!selectedHistoryRange.value || selectedHistoryRange.value.length !== 2) {
    message.warning('请先选择开始时间和结束时间');
    return;
  }

  const [startTime, endTime] = selectedHistoryRange.value.map((t) =>
    dayjs(t).format('YYYY-MM-DD HH:mm')
  );

  // 假设 selectStore.selectedPlot.plotId 是当前选中地块
  const plotId = selectStore.selectedPlot.plotId;

  const res = await queryHistoryByTimeRange({ startTime, endTime, plotId });
  filteredHistory.value = res.map((item) => {
  return {
    id: item.id,
    // 无图片时显示默认图
    time: dayjs(item.controlDate).format('YYYY-MM-DD HH:mm'),
    disease: item.diseaseName,
    pesticide: item.pesticideName,
    dosage: item.pesticideDosage ? `${item.pesticideDosage}` : '—',
    method: item.applicationMethod || '—',
  };
});

  if (!res || res.length === 0) {
    message.info('该时间段内无虫情记录');
  } else {
    console.log('查询结果：', res);
  }
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
.no-image {
  color: #aaa;
}
.disease-card {
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
.disease-row {
  display: flex;
  align-items: center;
  gap: 16px;
}
.prediction-info {
  flex: 1;
}
.inner-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: flex-start; /* 保证卡片内上下元素间距 */
}
.upload-card {
  margin-top: 16px;

  .upload-section {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16px;
  }

  .image-preview {
    display: flex;
    flex-direction: column;
    align-items: center; /* 图片和按钮都居中 */
    gap: 12px;

    .preview-img {
      max-width: 100%;
      max-height: 300px;
      border-radius: 8px;
      object-fit: contain;
    }

    .submit-btn {
      align-self: center; /* 改成居中 */
    }
  }
}

/* ---------- 图像区域 ---------- */
.image-container {
  flex: none;
  height: 600px; /* 固定高度，防止塌陷 */
  display: flex;
  justify-content: center;
  align-items: center;
}

.disease-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 6px;
}
.lishi-image {
  width: 200px;
  height: 130px;
  object-fit: cover;
  border-radius: 8px;
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

.record-info {
  flex: 1;
}

.disease-entry {
  margin: 6px 0;
}

.common-info {
  color: #666;
  font-size: 13px;
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

.disease-name {
  font-weight: 500;
  color: #1677ff;
}

.disease-count {
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

.prediction-info.enhanced {
  display: flex;
  flex-direction: column;
  padding: 16px;
  background: linear-gradient(135deg, #f0f8ff, #e6f7ff);
  border-radius: 8px;
  margin-top: 16px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
  border: 1px solid #d9d9d9;

  .prediction-label {
    font-size: 14px;
    color: #666;
    margin-bottom: 4px;
    font-weight: 500;
  }

  .prediction-value {
    font-size: 18px;
    font-weight: 600;
    color: #1890ff;
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
</style>
