<template>
  <div class="fertilization-page">
    <!-- 物联网设备提示 -->
    <div v-if="!hasIoTDevice" class="iot-warning">
      <a-alert type="warning" :closable="false">
        <template #message>
          <span>{{ deviceMessage || '该基地未配置物联网设备，无法获取实时数据' }}</span>
        </template>
      </a-alert>
    </div>

    <!-- 土壤养分概览 + 智能施肥建议 -->
    <a-row :gutter="16" class="top-row">
      <a-col :xs="24" :md="10">
        <a-card :bordered="false" class="rich-card">
          <template #title>
            <div class="table-title">
              <Icon icon="ant-design:dot-chart-outlined" /> 土壤养分概览
            </div>
          </template>
          <div v-if="sensorDataReady" class="overview-content">
            <div class="ec-display">
              <div class="ec-title">土壤电导率 (mS/cm)</div>
              <div class="ec-grid">
                <div class="ec-item">
                  <div class="ec-label">10cm</div>
                  <div class="ec-value">{{ sensorData.ec1 || '-' }}</div>
                </div>
                <div class="ec-item">
                  <div class="ec-label">30cm</div>
                  <div class="ec-value">{{ sensorData.ec2 || '-' }}</div>
                </div>
                <div class="ec-item">
                  <div class="ec-label">60cm</div>
                  <div class="ec-value">{{ sensorData.ec3 || '-' }}</div>
                </div>
              </div>
            </div>
            <a-divider style="margin: 12px 0" />
            <div class="nutrient-grid">
              <div class="nutrient-item">
                <div class="nutrient-label">估算氮(N)</div>
                <div class="nutrient-value">{{ sensorData.estimatedN || '-' }} <span class="unit">mg/kg</span></div>
              </div>
              <div class="nutrient-item">
                <div class="nutrient-label">估算磷(P)</div>
                <div class="nutrient-value">{{ sensorData.estimatedP || '-' }} <span class="unit">mg/kg</span></div>
              </div>
              <div class="nutrient-item">
                <div class="nutrient-label">估算钾(K)</div>
                <div class="nutrient-value">{{ sensorData.estimatedK || '-' }} <span class="unit">mg/kg</span></div>
              </div>
            </div>
            <a-divider style="margin: 12px 0" />
            <div class="percent-grid">
              <div class="percent-item">
                <a-progress type="circle" :percent="sensorData.nPercent || 0" :width="60" :stroke-color="'#52c41a'" />
                <div class="percent-label">N</div>
              </div>
              <div class="percent-item">
                <a-progress type="circle" :percent="sensorData.pPercent || 0" :width="60" :stroke-color="'#1890ff'" />
                <div class="percent-label">P</div>
              </div>
              <div class="percent-item">
                <a-progress type="circle" :percent="sensorData.kPercent || 0" :width="60" :stroke-color="'#faad14'" />
                <div class="percent-label">K</div>
              </div>
            </div>
          </div>
          <a-empty v-else :description="deviceMessage || '暂无数据'" />
        </a-card>
      </a-col>

      <a-col :xs="24" :md="14">
        <a-card :bordered="false" class="rich-card">
          <template #title>
            <div class="table-title">
              <Icon icon="ant-design:solution-outlined" /> 智能施肥建议
            </div>
          </template>
          <div v-if="recommendDataReady">
            <div class="recommend-header">
              <Tag :color="recommend.needFertilization ? 'red' : 'green'" style="font-size: 14px; padding: 4px 12px">
                {{ recommend.needFertilization ? '需要施肥' : '暂不需要' }}
              </Tag>
              <span class="data-source">数据来源：物联网实时传感器</span>
            </div>
            <a-descriptions bordered size="small" :column="2" class="recommend-desc">
              <a-descriptions-item label="推荐时间">{{ recommend.recommendedTime || '-' }}</a-descriptions-item>
              <a-descriptions-item label="推荐方式">{{ recommend.method || '-' }}</a-descriptions-item>
              <a-descriptions-item label="氮(N)推荐">
                <span class="recommend-value">{{ recommend.recommendN || 0 }} <span class="unit">kg/亩</span></span>
              </a-descriptions-item>
              <a-descriptions-item label="磷(P₂O₅)推荐">
                <span class="recommend-value">{{ recommend.recommendP2O5 || 0 }} <span class="unit">kg/亩</span></span>
              </a-descriptions-item>
              <a-descriptions-item label="钾(K₂O)推荐" :span="2">
                <span class="recommend-value">{{ recommend.recommendK2O || 0 }} <span class="unit">kg/亩</span></span>
              </a-descriptions-item>
              <a-descriptions-item label="分析建议" :span="2">
                <div class="reason-text">{{ recommend.reason || '暂无' }}</div>
              </a-descriptions-item>
            </a-descriptions>
            <div class="actions">
              <a-space :size="12">
                <a-button size="small" @click="copySuggestion" :disabled="!sensorDataReady">
                  <Icon icon="ant-design:copy-outlined" /> 复制建议
                </a-button>
                <a-button size="small" @click="refreshData" :disabled="!selectedBaseId">
                  <Icon icon="ant-design:reload-outlined" /> 刷新数据
                </a-button>
                <a-button size="small" @click="openReportModal" :disabled="!recommendDataReady">
                  <Icon icon="ant-design:file-word-outlined" /> 生成报告
                </a-button>
              </a-space>
            </div>
          </div>
          <a-empty v-else :description="deviceMessage || '暂无推荐数据'" />
        </a-card>
      </a-col>
    </a-row>

    <!-- 土壤分层数据 -->
    <a-row :gutter="16" class="mt-4" v-if="sensorDataReady">
      <a-col :xs="24">
        <a-card :bordered="false" class="rich-card">
          <template #title>
            <div class="table-title">
              <Icon icon="ant-design:database-outlined" /> 土壤分层数据
            </div>
          </template>
          <div class="soil-layers">
            <div class="soil-layer layer-top">
              <div class="layer-label">上层 (10cm)</div>
              <div class="layer-data">
                <div class="layer-item">
                  <span class="layer-item-label">土壤温度</span>
                  <span class="layer-item-value">{{ sensorData.soilTemp1 || '-' }} <span class="unit">℃</span></span>
                </div>
                <div class="layer-item">
                  <span class="layer-item-label">土壤湿度</span>
                  <span class="layer-item-value">{{ sensorData.soilMoist1 || '-' }} <span class="unit">%</span></span>
                </div>
                <div class="layer-item">
                  <span class="layer-item-label">土壤电导率</span>
                  <span class="layer-item-value">{{ sensorData.ec1 || '-' }} <span class="unit">mS/cm</span></span>
                </div>
              </div>
            </div>
            <div class="soil-layer layer-middle">
              <div class="layer-label">中层 (30cm)</div>
              <div class="layer-data">
                <div class="layer-item">
                  <span class="layer-item-label">土壤温度</span>
                  <span class="layer-item-value">{{ sensorData.soilTemp2 || '-' }} <span class="unit">℃</span></span>
                </div>
                <div class="layer-item">
                  <span class="layer-item-label">土壤湿度</span>
                  <span class="layer-item-value">{{ sensorData.soilMoist2 || '-' }} <span class="unit">%</span></span>
                </div>
                <div class="layer-item">
                  <span class="layer-item-label">土壤电导率</span>
                  <span class="layer-item-value">{{ sensorData.ec2 || '-' }} <span class="unit">mS/cm</span></span>
                </div>
              </div>
            </div>
            <div class="soil-layer layer-bottom">
              <div class="layer-label">深层 (60cm)</div>
              <div class="layer-data">
                <div class="layer-item">
                  <span class="layer-item-label">土壤温度</span>
                  <span class="layer-item-value">{{ sensorData.soilTemp3 || '-' }} <span class="unit">℃</span></span>
                </div>
                <div class="layer-item">
                  <span class="layer-item-label">土壤湿度</span>
                  <span class="layer-item-value">{{ sensorData.soilMoist3 || '-' }} <span class="unit">%</span></span>
                </div>
                <div class="layer-item">
                  <span class="layer-item-label">土壤电导率</span>
                  <span class="layer-item-value">{{ sensorData.ec3 || '-' }} <span class="unit">mS/cm</span></span>
                </div>
              </div>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 推荐施肥量柱状图 -->
    <a-row :gutter="16" class="mt-4" v-if="recommendDataReady">
      <a-col :xs="24" :md="24">
        <a-card :bordered="false" class="rich-card">
          <template #title>
            <div class="table-title">
              <Icon icon="ant-design:bar-chart-outlined" /> 推荐施肥量（kg/亩）
            </div>
          </template>
          <div ref="barChartRef" style="width: 100%; height: 280px" />
        </a-card>
      </a-col>
    </a-row>

    <!-- 报告弹窗 -->
    <a-modal v-model:open="reportModalVisible" title="智能施肥报告" :width="800">
      <div style="padding: 20px" v-if="recommendDataReady" id="fertilizationReport">
        <h2 style="text-align: center; margin-bottom: 20px">智能施肥推荐报告</h2>
        <p><strong>基地名称：</strong>{{ baseName }}</p>
        <p><strong>数据来源：</strong>物联网实时传感器数据</p>
        <p><strong>生成时间：</strong>{{ currentTime }}</p>
        <a-divider />
        <h4>一、土壤养分状况</h4>
        <p>土壤电导率：{{ sensorData.ec1 }}/{{ sensorData.ec2 }}/{{ sensorData.ec3 }} mS/cm</p>
        <p>估算养分：氮 {{ sensorData.estimatedN }} mg/kg，磷 {{ sensorData.estimatedP }} mg/kg，钾 {{ sensorData.estimatedK }} mg/kg</p>
        <a-divider />
        <h4>二、施肥推荐</h4>
        <p><strong>是否需要施肥：</strong>{{ recommend.needFertilization ? '需要' : '暂不需要' }}</p>
        <p><strong>推荐施肥时间：</strong>{{ recommend.recommendedTime || '-' }}</p>
        <p><strong>推荐施肥方式：</strong>{{ recommend.method || '-' }}</p>
        <p><strong>推荐施肥量：</strong></p>
        <ul>
          <li>氮(N)：{{ recommend.recommendN || 0 }} kg/亩</li>
          <li>磷(P₂O₅)：{{ recommend.recommendP2O5 || 0 }} kg/亩</li>
          <li>钾(K₂O)：{{ recommend.recommendK2O || 0 }} kg/亩</li>
        </ul>
        <p v-if="recommend.reason"><strong>分析建议：</strong>{{ recommend.reason }}</p>
      </div>
      <template #footer>
        <a-space>
          <a-button type="primary" @click="downloadReport" :disabled="!recommendDataReady">下载报告</a-button>
          <a-button @click="reportModalVisible = false">关闭</a-button>
        </a-space>
      </template>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, watch, onMounted, nextTick, computed } from 'vue';
import { Icon } from '/@/components/Icon';
import { Tag } from 'ant-design-vue';
import { useSelectStore } from '/@/store/selectStore';
import { getPlotStatusByBase, getBaseRecommendation, getBaseSoilSeries } from './fertilization.api';
import { useECharts } from '/@/hooks/web/useECharts';
import { useMessage } from '/@/hooks/web/useMessage';

const selectStore = useSelectStore();
const selectedBaseId = ref<string | number | undefined>(undefined);
const lastRequestBaseId = ref<string | number | undefined>(undefined);
const baseName = computed(() => selectStore.selectedBase.baseName || '');
const { createMessage } = useMessage();

const deviceMessage = ref('');
const sensorDataReady = ref(false);
const recommendDataReady = ref(false);
const hasIoTDevice = ref(true);
const currentTime = ref('');

const sensorData = reactive({
  ec1: 0,
  ec2: 0,
  ec3: 0,
  soilTemp1: 0,
  soilTemp2: 0,
  soilTemp3: 0,
  soilMoist1: 0,
  soilMoist2: 0,
  soilMoist3: 0,
  estimatedN: 0,
  estimatedP: 0,
  estimatedK: 0,
  nPercent: 0,
  pPercent: 0,
  kPercent: 0
});

const recommend = reactive({
  needFertilization: false,
  recommendedTime: '',
  method: '',
  recommendN: 0,
  recommendP2O5: 0,
  recommendK2O: 0,
  reason: ''
});

const barChartRef = ref<HTMLDivElement | null>(null);
const { setOptions: setBarOptions } = useECharts(barChartRef as any);
const reportModalVisible = ref(false);

function updateTime() {
  const now = new Date();
  currentTime.value = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')} ${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}:${String(now.getSeconds()).padStart(2, '0')}`;
}

function onBaseChange() {
  const { baseId } = selectStore.selectedBase as any;
  selectedBaseId.value = baseId;
}

async function fetchData() {
  updateTime();
  if (!selectedBaseId.value) {
    resetData();
    return;
  }

  const reqId = selectedBaseId.value;
  lastRequestBaseId.value = reqId;

  const results = await Promise.allSettled([
    getPlotStatusByBase(reqId as any),
    getBaseRecommendation(reqId as any)
  ]);

  if (lastRequestBaseId.value !== reqId) return;

  const statusRes = results[0].status === 'fulfilled' ? (results[0] as any).value : {};
  const recRes = results[1].status === 'fulfilled' ? (results[1] as any).value : {};

  hasIoTDevice.value = !(statusRes.deviceNotConfigured || recRes.deviceNotConfigured);
  deviceMessage.value = statusRes.deviceMessage || recRes.deviceMessage || '';

  if (hasIoTDevice.value) {
    sensorDataReady.value = Boolean(statusRes.hasData);
    recommendDataReady.value = Boolean(recRes.hasData);

    if (statusRes.hasData) {
      sensorData.ec1 = Number(statusRes.ec1) || 0;
      sensorData.ec2 = Number(statusRes.ec2) || 0;
      sensorData.ec3 = Number(statusRes.ec3) || 0;
      sensorData.soilTemp1 = Number(statusRes.soilTemp1) || 0;
      sensorData.soilTemp2 = Number(statusRes.soilTemp2) || 0;
      sensorData.soilTemp3 = Number(statusRes.soilTemp3) || 0;
      sensorData.soilMoist1 = Number(statusRes.soilMoist1) || 0;
      sensorData.soilMoist2 = Number(statusRes.soilMoist2) || 0;
      sensorData.soilMoist3 = Number(statusRes.soilMoist3) || 0;
      sensorData.estimatedN = Number(statusRes.estimatedN) || 0;
      sensorData.estimatedP = Number(statusRes.estimatedP) || 0;
      sensorData.estimatedK = Number(statusRes.estimatedK) || 0;
      sensorData.nPercent = Number(statusRes.nPercent) || 0;
      sensorData.pPercent = Number(statusRes.pPercent) || 0;
      sensorData.kPercent = Number(statusRes.kPercent) || 0;
    }

    if (recRes.hasData) {
      recommend.needFertilization = recRes.needFertilization || false;
      recommend.recommendedTime = recRes.recommendedTime || '';
      recommend.method = recRes.method || '';
      recommend.recommendN = Number(recRes.recommendN) || 0;
      recommend.recommendP2O5 = Number(recRes.recommendP2O5) || 0;
      recommend.recommendK2O = Number(recRes.recommendK2O) || 0;
      recommend.reason = recRes.reason || '';
    }
  } else {
    sensorDataReady.value = false;
    recommendDataReady.value = false;
  }

  await nextTick();
  if (recommendDataReady.value) {
    renderBarChart();
  }
}

function resetData() {
  hasIoTDevice.value = true;
  sensorDataReady.value = false;
  recommendDataReady.value = false;
  deviceMessage.value = '';
  Object.assign(sensorData, {
    ec1: 0, ec2: 0, ec3: 0,
    soilTemp1: 0, soilTemp2: 0, soilTemp3: 0,
    soilMoist1: 0, soilMoist2: 0, soilMoist3: 0,
    estimatedN: 0, estimatedP: 0, estimatedK: 0,
    nPercent: 0, pPercent: 0, kPercent: 0
  });
  Object.assign(recommend, {
    needFertilization: false,
    recommendedTime: '', method: '',
    recommendN: 0, recommendP2O5: 0, recommendK2O: 0, reason: ''
  });
}

function renderBarChart() {
  const data = [
    recommend.recommendN,
    recommend.recommendP2O5,
    recommend.recommendK2O
  ];
  setBarOptions({
    tooltip: { trigger: 'axis' },
    grid: { left: 50, right: 50, top: 20, bottom: 30 },
    xAxis: { type: 'category', data: ['氮(N)', '磷(P₂O₅)', '钾(K₂O)'] },
    yAxis: { type: 'value', name: 'kg/亩' },
    series: [{
      type: 'bar',
      data: data,
      itemStyle: {
        color: (params: any) => {
          const colors = ['#52c41a', '#1890ff', '#faad14'];
          return colors[params.dataIndex] || '#69c0ff';
        }
      },
      label: {
        show: true,
        position: 'top',
        formatter: '{c} kg/亩'
      }
    }]
  });
}

async function copySuggestion() {
  const text = `【智能施肥建议】
基地：${baseName.value}
是否需要施肥：${recommend.needFertilization ? '需要' : '暂不需要'}
推荐时间：${recommend.recommendedTime || '-'}
推荐方式：${recommend.method || '-'}
氮(N)：${recommend.recommendN} kg/亩
磷(P₂O₅)：${recommend.recommendP2O5} kg/亩
钾(K₂O)：${recommend.recommendK2O} kg/亩
分析建议：${recommend.reason || '-'}
数据来源：物联网实时传感器`;
  try {
    await navigator.clipboard?.writeText(text);
    createMessage.success('已复制到剪贴板');
  } catch (e) {
    createMessage.warning('复制失败');
  }
}

async function refreshData() {
  await fetchData();
  createMessage.success('数据已刷新');
}

function openReportModal() {
  reportModalVisible.value = true;
}

function downloadReport() {
  try {
    const el = document.getElementById('fertilizationReport');
    if (!el) return;
    const html = '<!DOCTYPE html><html><head><meta charset="utf-8"><title>智能施肥报告</title><style>body{font-family:SimHei;padding:20px;}h2{text-align:center;}ul{padding-left:20px;}</style></head><body>' + el.outerHTML + '</body></html>';
    const blob = new Blob(['\ufeff', html], { type: 'application/msword;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `智能施肥报告_${baseName.value || '未知基地'}.doc`;
    a.click();
    URL.revokeObjectURL(url);
  } catch (e) {
    createMessage.warning('下载失败');
  }
}

watch(() => selectStore.selectedBase, () => onBaseChange());
watch(selectedBaseId, () => fetchData());

onMounted(() => {
  onBaseChange();
  fetchData();
});
</script>

<style scoped>
.fertilization-page {
  padding: 16px;
}
.iot-warning {
  margin-bottom: 16px;
}
.top-row {
  margin-bottom: 16px;
}
.rich-card {
  height: 100%;
  min-height: 280px;
}
.rich-card :deep(.ant-card-body) {
  height: 100%;
}
.table-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}
.overview-content {
  padding: 8px 0;
}
.ec-display {
  text-align: center;
}
.ec-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 12px;
}
.ec-grid {
  display: flex;
  justify-content: space-around;
}
.ec-item {
  text-align: center;
}
.ec-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}
.ec-value {
  font-size: 24px;
  font-weight: 600;
  color: #1890ff;
}
.nutrient-grid {
  display: flex;
  justify-content: space-around;
  padding: 8px 0;
}
.nutrient-item {
  text-align: center;
}
.nutrient-label {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}
.nutrient-value {
  font-size: 18px;
  font-weight: 600;
}
.unit {
  font-size: 12px;
  color: #999;
  font-weight: normal;
}
.percent-grid {
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 12px 0;
}
.percent-item {
  text-align: center;
}
.percent-label {
  margin-top: 8px;
  font-weight: 600;
}
.recommend-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.data-source {
  font-size: 12px;
  color: #999;
}
.recommend-desc {
  margin-bottom: 12px;
}
.recommend-value {
  font-size: 16px;
  font-weight: 600;
  color: #1890ff;
}
.reason-text {
  color: #666;
  line-height: 1.6;
}
.actions {
  margin-top: auto;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}
.soil-layers {
  display: flex;
  gap: 16px;
  justify-content: space-between;
}
.soil-layer {
  flex: 1;
  padding: 16px;
  border-radius: 8px;
  background: #fafafa;
}
.layer-top { background: #e6f7ff; }
.layer-middle { background: #f6ffed; }
.layer-bottom { background: #fff7e6; }
.layer-label {
  font-weight: 600;
  margin-bottom: 12px;
  text-align: center;
}
.layer-data {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.layer-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 12px;
  background: #fff;
  border-radius: 4px;
}
.layer-item-label {
  color: #666;
  font-size: 13px;
}
.layer-item-value {
  font-weight: 600;
  color: #333;
}
.mt-4 { margin-top: 16px; }
@media (max-width: 768px) {
  .soil-layers {
    flex-direction: column;
  }
  .nutrient-grid {
    flex-wrap: wrap;
    gap: 12px;
  }
  .nutrient-item {
    flex: 1 1 30%;
  }
}
</style>