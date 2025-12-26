<template>
  <div class="fertilization-page">
    <a-row :gutter="16" class="top-row">
      <a-col :xs="24" :md="24">
        <a-card :bordered="false" class="rich-card">
          <template #title>
            <div class="table-title">
              <Icon icon="ant-design:dot-chart-outlined" /> 基地土壤概览
            </div>
          </template>
          <div v-if="status.hasData" class="overview-grid">
            <div class="metric">
              <div class="metric-title">最新检测日期</div>
              <div class="metric-value">{{ status.latestDate }}</div>
            </div>
            <div class="metric">
              <div class="metric-title">N百分比</div>
              <div class="metric-value">{{ status.nPercent }}%</div>
            </div>
            <div class="metric">
              <div class="metric-title">P百分比</div>
              <div class="metric-value">{{ status.pPercent }}%</div>
            </div>
            <div class="metric">
              <div class="metric-title">K百分比</div>
              <div class="metric-value">{{ status.kPercent }}%</div>
            </div>
            <div class="metric">
              <div class="metric-title">pH</div>
              <div class="metric-value">{{ status.ph }}</div>
            </div>
            <div class="metric">
              <div class="metric-title">有机质(g/kg)</div>
              <div class="metric-value">{{ status.om }}</div>
            </div>
          </div>
          <a-empty v-else description="暂无数据（该基地在 youcai_soil_fertility 表中没有记录）" />
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" class="mt-4 equal-row">
      <a-col :xs="24" :md="12">
        <a-card :bordered="false" class="rich-card">
          <template #title>
            <div class="table-title">
              <Icon icon="ant-design:line-chart-outlined" /> 近7天土壤趋势
            </div>
          </template>
          <div v-show="series.hasData && series.dates.length" ref="seriesRef" style="width: 100%; height: 100%; min-height: 260px" />
          <a-empty v-show="!(series.hasData && series.dates.length)" description="暂无趋势数据" />
        </a-card>
      </a-col>
      <a-col :xs="24" :md="12">
        <a-card :bordered="false" class="rich-card">
          <template #title>
            <div class="table-title">
              <Icon icon="ant-design:solution-outlined" /> QUEFTS施肥推荐
            </div>
          </template>
          <div v-if="recommend.hasData">
            <a-descriptions bordered size="small" :column="1">
              <a-descriptions-item label="是否需要施肥">
                <Tag :color="recommend.needFertilization ? 'red' : 'green'">
                  {{ recommend.needFertilization ? '需要' : '暂不需要' }}
                </Tag>
              </a-descriptions-item>
              <a-descriptions-item label="推荐时间">{{ recommend.recommendedTime || '-' }}</a-descriptions-item>
              <a-descriptions-item label="推荐方式">{{ recommend.method || '-' }}</a-descriptions-item>
              <a-descriptions-item label="N推荐量">{{ recommend.recommendN }} kg/亩</a-descriptions-item>
              <a-descriptions-item label="P₂O₅推荐量">{{ recommend.recommendP2O5 }} kg/亩</a-descriptions-item>
              <a-descriptions-item label="K₂O推荐量">{{ recommend.recommendK2O }} kg/亩</a-descriptions-item>
              <a-descriptions-item label="推荐理由" v-if="recommend.reason">{{ recommend.reason }}</a-descriptions-item>
            </a-descriptions>
            <div class="actions" style="margin-top: 8px;">
              <a-space :size="12">
                <a-button size="small" @click="copyFertilizationSuggestion" :disabled="!status.hasData">复制建议</a-button>
                <a-button size="small" @click="refreshBaseData" :disabled="!selectedBaseId">刷新数据</a-button>
                <a-button size="small" @click="openReportModal('fertilization')" :disabled="!recommend.hasData">生成报告</a-button>
              </a-space>
            </div>
          </div>
          <a-empty v-else description="暂无推荐数据" />
        </a-card>
      </a-col>
    </a-row>
    <a-row :gutter="16" class="mt-4">
      <a-col :xs="24" :md="12">
        <a-card :bordered="false" class="rich-card">
          <template #title>
            <div class="table-title">
              <Icon icon="ant-design:area-chart-outlined" /> 干预施肥 vs 不干预对比
            </div>
          </template>
          <div v-show="comparisonReady" ref="compareChartRef" style="width: 100%; height: 300px" />
          <a-empty v-show="!comparisonReady" description="暂无对比数据" />
        </a-card>
      </a-col>
      <a-col :xs="24" :md="12">
        <a-card :bordered="false" class="rich-card">
          <template #title>
            <div class="table-title">
              <Icon icon="ant-design:bar-chart-outlined" /> 推荐施肥量（kg/亩）
            </div>
          </template>
          <div v-show="recommend.hasData" ref="barChartRef" style="width: 100%; height: 300px" />
          <a-empty v-show="!recommend.hasData" description="暂无推荐数据" />
        </a-card>
      </a-col>
    </a-row>
    <a-modal v-model:open="reportModalVisible" title="施肥推荐报告" :width="800">
      <div style="padding:16px" v-if="recommend.hasData" id="fertilizationReport">
        <h3>施肥推荐报告</h3>
        <p>基地：{{ baseName }}</p>
        <p>是否需要施肥：{{ recommend.needFertilization ? '需要' : '暂不需要' }}</p>
        <p>时间：{{ recommend.recommendedTime || '-' }}</p>
        <p>方式：{{ recommend.method || '-' }}</p>
        <p>N推荐量：{{ recommend.recommendN }} kg/亩</p>
        <p>P₂O₅推荐量：{{ recommend.recommendP2O5 }} kg/亩</p>
        <p>K₂O推荐量：{{ recommend.recommendK2O }} kg/亩</p>
        <p v-if="recommend.reason">推荐理由：{{ recommend.reason }}</p>
      </div>
      <template #footer>
        <a-space>
          <a-button type="primary" @click="downloadFertilizationReport" :disabled="!recommend.hasData">下载 Word 文档</a-button>
          <a-button @click="reportModalVisible=false">关闭</a-button>
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

const status = reactive<any>({ hasData: false, latestDate: '', nPercent: 0, pPercent: 0, kPercent: 0, ph: 0, om: 0 });
const recommend = reactive<any>({ hasData: false, needFertilization: false, recommendedTime: '', method: '', recommendN: 0, recommendP2O5: 0, recommendK2O: 0, reason: '' });
const series = reactive<any>({ hasData: false, dates: [], avgN: [], avgP: [], avgK: [] });

const seriesRef = ref<HTMLDivElement | null>(null);
const { setOptions } = useECharts(seriesRef as any);
const compareChartRef = ref<HTMLDivElement | null>(null);
const { setOptions: setCompareOptions } = useECharts(compareChartRef as any);
const barChartRef = ref<HTMLDivElement | null>(null);
const { setOptions: setBarOptions } = useECharts(barChartRef as any);
const comparisonReady = ref<boolean>(false);
const reportModalVisible = ref<boolean>(false);
const reportType = ref<string>('fertilization');
function toNumberArray(arr: any[], targetLen?: number): number[] {
  const base = Array.isArray(arr) ? arr.map((v: any) => Number(v) || 0) : [];
  if (typeof targetLen === 'number') {
    if (base.length > targetLen) return base.slice(0, targetLen);
    if (base.length < targetLen) return base.concat(Array(targetLen - base.length).fill(0));
  }
  return base;
}

function onBaseChange() {
  const { baseId } = selectStore.selectedBase as any;
  selectedBaseId.value = baseId;
}

async function fetchBaseData() {
  if (!selectedBaseId.value) {
    status.hasData = false;
    recommend.hasData = false;
    series.hasData = false;
    series.dates = [];
    series.avgN = [];
    series.avgP = [];
    series.avgK = [];
    return;
  }
  const reqId = selectedBaseId.value;
  lastRequestBaseId.value = reqId;
  const results = await Promise.allSettled([
    getPlotStatusByBase(reqId as any),
    getBaseRecommendation(reqId as any),
    getBaseSoilSeries(reqId as any),
  ]);
  if (lastRequestBaseId.value !== reqId) return;
  const s = results[0].status === 'fulfilled' ? (results[0] as any).value : {};
  const r = results[1].status === 'fulfilled' ? (results[1] as any).value : {};
  const ser = results[2].status === 'fulfilled' ? (results[2] as any).value : {};
  Object.assign(status, s || {});
  Object.assign(recommend, r || {});
  Object.assign(series, ser || {});
  status.hasData = Boolean(
    s &&
      (s.latestDate ||
        s.nPercent != null ||
        s.pPercent != null ||
        s.kPercent != null ||
        s.ph != null ||
        s.om != null)
  );
  recommend.hasData = Boolean(
    r &&
      (r.needFertilization != null ||
        r.recommendedTime ||
        r.method ||
        r.recommendN != null ||
        r.recommendP2O5 != null ||
        r.recommendK2O != null ||
        r.reason)
  );
  const hasSeries = Boolean(ser && Array.isArray(ser.dates) && ser.dates.length);
  series.hasData = hasSeries;
  series.dates = Array.isArray(ser.dates) ? ser.dates : [];
  series.avgN = Array.isArray(ser.avgN) ? ser.avgN : [];
  series.avgP = Array.isArray(ser.avgP) ? ser.avgP : [];
  series.avgK = Array.isArray(ser.avgK) ? ser.avgK : [];
  await nextTick();
  if (lastRequestBaseId.value !== reqId) return;
  if (series.hasData && series.dates.length) {
    const dates = Array.isArray(series.dates) ? series.dates : [];
    const n = toNumberArray(series.avgN, dates.length);
    const p = toNumberArray(series.avgP, dates.length);
    const k = toNumberArray(series.avgK, dates.length);
    setOptions({
      grid: { left: 30, right: 12, top: 16, bottom: 8 },
      tooltip: { trigger: 'axis' },
      legend: { data: ['N', 'P', 'K'] },
      xAxis: { type: 'category', data: dates, boundaryGap: false },
      yAxis: { type: 'value', min: 'dataMin', max: 'dataMax' },
      series: [
        { name: 'N', type: 'line', data: n, smooth: true, areaStyle: { opacity: 0.08 } },
        { name: 'P', type: 'line', data: p, smooth: true, areaStyle: { opacity: 0.08 } },
        { name: 'K', type: 'line', data: k, smooth: true, areaStyle: { opacity: 0.08 } },
      ],
    });
  }
  renderComparisonChart();
  renderBarChart();
}

watch(
  () => selectStore.selectedBase,
  () => onBaseChange()
);

watch(selectedBaseId, () => fetchBaseData());

onMounted(() => {
  onBaseChange();
  fetchBaseData();
});
function nutrientIndex(): number[] {
  if (!series.hasData || !series.dates.length) return [];
  const n = (series.avgN || []).map((v: any) => Number(v) || 0);
  const p = (series.avgP || []).map((v: any) => Number(v) || 0);
  const k = (series.avgK || []).map((v: any) => Number(v) || 0);
  return n.map((nv: number, i: number) => {
    const pv = p[i] || 0;
    const kv = k[i] || 0;
    return Math.round((nv * 0.5 + pv * 0.3 + kv * 0.2) * 100) / 100;
  });
}
function withInterventionIndex(base: number[]): number[] {
  if (!recommend.hasData) return base.slice();
  const inc = Math.max(0, Number(recommend.recommendN || 0) * 0.1 + Number(recommend.recommendP2O5 || 0) * 0.05 + Number(recommend.recommendK2O || 0) * 0.08);
  return base.map((v: number, i: number) => i === 0 ? v : Math.round((v + inc) * 100) / 100);
}
function renderComparisonChart() {
  const dates = series.dates || [];
  const baseLine = nutrientIndex();
  const withLine = withInterventionIndex(baseLine);
  const ok = dates.length && baseLine.length === dates.length && withLine.length === dates.length;
  comparisonReady.value = ok;
  if (!ok) return;
  setCompareOptions({
    grid: { left: 32, right: 12, top: 16, bottom: 8 },
    tooltip: { trigger: 'axis' },
    legend: { data: ['不干预', '干预施肥'] },
    xAxis: { type: 'category', data: dates, boundaryGap: false },
    yAxis: { type: 'value', name: '养分综合指数(%)', min: 'dataMin', max: 'dataMax' },
    series: [
      { name: '不干预', type: 'line', data: baseLine, smooth: true, areaStyle: { opacity: 0.1 } },
      { name: '干预施肥', type: 'line', data: withLine, smooth: true, areaStyle: { opacity: 0.1 } },
    ],
  });
}
function renderBarChart() {
  if (!recommend.hasData) return;
  const data = [
    Number(recommend.recommendN || 0),
    Number(recommend.recommendP2O5 || 0),
    Number(recommend.recommendK2O || 0),
  ];
  setBarOptions({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: ['N', 'P₂O₅', 'K₂O'] },
    yAxis: { type: 'value' },
    series: [{ type: 'bar', data, itemStyle: { color: '#69c0ff' } }],
  });
}
async function copyFertilizationSuggestion() {
  const text = `基地：${baseName.value}\n需要施肥：${recommend.needFertilization ? '是' : '否'}\n时间：${recommend.recommendedTime || '-'}\n方式：${recommend.method || '-'}\nN：${recommend.recommendN} kg/亩\nP₂O₅：${recommend.recommendP2O5} kg/亩\nK₂O：${recommend.recommendK2O} kg/亩\n理由：${recommend.reason || '-'}`;
  try {
    await navigator.clipboard?.writeText(text);
    createMessage.success('已复制建议到剪贴板');
  } catch (e) {
    createMessage.warning('复制失败，请手动选择文本进行复制');
  }
}
async function refreshBaseData() {
  await fetchBaseData();
  createMessage.success('数据已刷新');
}
function openReportModal(type?: string) {
  reportType.value = type || 'fertilization';
  reportModalVisible.value = true;
}
function downloadFertilizationReport() {
  try {
    const el = document.getElementById('fertilizationReport');
    if (!el) return;
    const html = '<!DOCTYPE html><html><head><meta charset="utf-8"><title>施肥推荐报告</title></head><body>' + el.outerHTML + '</body></html>';
    const blob = new Blob(['\ufeff', html], { type: 'application/msword;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `施肥推荐报告_${baseName.value || '未命名'}.doc`;
    a.click();
    URL.revokeObjectURL(url);
  } catch (e) {
    createMessage.warning('下载失败');
  }
}
</script>

<style scoped>
.table-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}
.equal-row {
  align-items: stretch;
}
.rich-card {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 220px;
}
.rich-card :deep(.ant-card-body) {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.actions {
  margin-top: auto;
}
.overview-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.metric-title {
  font-size: 12px;
  color: #888;
}
.metric-value {
  font-size: 16px;
  font-weight: 600;
}
.mt-4 { margin-top: 16px; }
.rich-card { min-height: 220px; }
</style>
