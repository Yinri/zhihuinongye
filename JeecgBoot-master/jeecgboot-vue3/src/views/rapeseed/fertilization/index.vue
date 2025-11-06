<template>
  <div class="fertilization-page">
    <!-- 选择与概览区域 -->
    <a-row :gutter="16" class="top-row">
      <a-col :xs="24" :md="8">
        <a-card :bordered="false">
          <template #title>
            <div class="table-title">
              <Icon icon="ant-design:environment-outlined" /> 地块选择
            </div>
          </template>
          <a-space direction="vertical" style="width: 100%">
            <BaseSelect v-model="selectedBaseId" @change="onBaseChange" />
            <PlotSelect ref="plotSelectRef" v-model="selectedPlotId" :base-id="selectedBaseId" @change="onPlotChange" />
          </a-space>
        </a-card>
      </a-col>
      <a-col :xs="24" :md="8">
        <a-card :bordered="false" class="rich-card">
          <template #title>
            <div class="table-title">
              <Icon icon="ant-design:dot-chart-outlined" /> 土壤养分概览
            </div>
          </template>
          <div class="nutrient-card">
            <div class="nutrient-item">
              <span class="label">氮(N)：</span>
              <a-progress type="line" :percent="nPercent" :status="nStatus" :stroke-color="nColor" />
              <Tag :color="nTagColor">{{ nStateLabel }}</Tag>
            </div>
            <div class="nutrient-item">
              <span class="label">磷(P)：</span>
              <a-progress type="line" :percent="pPercent" :status="pStatus" :stroke-color="pColor" />
              <Tag :color="pTagColor">{{ pStateLabel }}</Tag>
            </div>
            <div class="nutrient-item">
              <span class="label">钾(K)：</span>
              <a-progress type="line" :percent="kPercent" :status="kStatus" :stroke-color="kColor" />
              <Tag :color="kTagColor">{{ kStateLabel }}</Tag>
            </div>
          </div>
          <div class="statistic-grid">
            <div class="metric">
              <div class="metric-label">近7天趋势摘要</div>
              <div class="metric-value">{{ trendSummary }}</div>
            </div>
            <div class="metric">
              <div class="metric-label">淋溶风险</div>
              <div class="metric-value">
                <Tag :color="leachingRiskColor">{{ leachingRiskLabel }}</Tag>
              </div>
            </div>
            <div class="metric">
              <div class="metric-label">目标产量</div>
              <div class="metric-value">{{ targetYieldText }}</div>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="24" :md="8">
        <a-card :bordered="false" class="rich-card">
          <template #title>
            <div class="table-title">
              <Icon icon="ant-design:experiment-outlined" /> 施肥建议（QUEFTS）
            </div>
          </template>
          <div class="suggestion-card">
            <div class="suggestion-line">
              建议类型：<Tag :color="suggestion.needFertilization ? 'gold' : 'green'">{{ suggestion.needFertilization ? '需要施肥' : '暂不施肥' }}</Tag>
            </div>
            <div class="suggestion-line">推荐时间：{{ suggestion.recommendedTime || '—' }}</div>
            <div class="suggestion-line">推荐方式：{{ suggestion.method || '—' }}</div>
            <div class="dose-grid">
              <div class="dose-item">
                <div class="dose-label">氮肥(N)</div>
                <div class="dose-value">{{ suggestion.recommendN }} kg/亩</div>
              </div>
              <div class="dose-item">
                <div class="dose-label">磷肥(P₂O₅)</div>
                <div class="dose-value">{{ suggestion.recommendP2O5 }} kg/亩</div>
              </div>
              <div class="dose-item">
                <div class="dose-label">钾肥(K₂O)</div>
                <div class="dose-value">{{ suggestion.recommendK2O }} kg/亩</div>
              </div>
            </div>
            <div class="suggestion-reason">{{ suggestion.reason }}</div>
            <a-space>
              <a-button type="primary" @click="oneClickCreate">一键生成施肥记录</a-button>
              <a-button @click="scrollToCharts">查看趋势与输入</a-button>
            </a-space>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 趋势折线图区域：四个方框 -->
    <a-card :bordered="false" class="chart-card" ref="chartsAnchorRef">
      <template #title>
        <div class="table-title">
          <Icon icon="ant-design:line-chart" /> 趋势折线图
        </div>
      </template>
      <a-row :gutter="16" class="chart-grid">
        <a-col :xs="24" :md="12">
          <div class="chart-box">
            <div class="chart-title">近七天氮含量趋势</div>
            <div ref="nTrendRef" class="chart-ref"></div>
          </div>
        </a-col>
        <a-col :xs="24" :md="12">
          <div class="chart-box">
            <div class="chart-title">近七天磷含量趋势</div>
            <div ref="pTrendRef" class="chart-ref"></div>
          </div>
        </a-col>
        <a-col :xs="24" :md="12">
          <div class="chart-box">
            <div class="chart-title">一周降雨量</div>
            <div ref="rainTrendRef" class="chart-ref"></div>
          </div>
        </a-col>
        <a-col :xs="24" :md="12">
          <div class="chart-box">
            <div class="chart-title">一周平均温度</div>
            <div ref="tempTrendRef" class="chart-ref"></div>
          </div>
        </a-col>
      </a-row>
    </a-card>

    <!-- 施肥管理列表区域（已删除） -->

    <!-- 一键施肥管理 -->
    <a-card :bordered="false" class="quick-card">
      <template #title>
        <div class="table-title">
          <Icon icon="ant-design:tool-outlined" /> 一键施肥管理
        </div>
      </template>
      <a-space direction="vertical" style="width: 100%">
        <div class="form-item">
          <a-switch v-model:checked="useAlgorithm" checked-children="算法推荐" un-checked-children="手动参数" />
        </div>
        <a-row :gutter="6" justify="center" align="middle">
          <a-col :xs="24" :sm="12" :md="4">
            <div class="form-item">
              <div class="label">肥料配方</div>
              <a-select v-model:value="fertilizerFormula" :options="formulaOptions" :disabled="useAlgorithm" />
            </div>
          </a-col>
          <!-- 新增：选择基地 -->
          <a-col :xs="24" :sm="12" :md="4">
            <div class="form-item">
              <div class="label">选择基地</div>
              <BaseSelect v-model="selectedBaseId" @change="onBaseChange" />
            </div>
          </a-col>
          <!-- 新增：选择地块 -->
          <a-col :xs="24" :sm="12" :md="4">
            <div class="form-item">
              <div class="label">选择地块</div>
              <PlotSelect v-model="selectedPlotId" :base-id="selectedBaseId" @change="onPlotChange" />
            </div>
          </a-col>
          <a-col :xs="24" :sm="12" :md="4">
            <div class="form-item">
              <div class="label">施肥面积(亩)</div>
              <a-input-number v-model:value="quickArea" :min="0" :precision="2" style="width:100%" />
            </div>
          </a-col>
          <a-col :xs="24" :sm="12" :md="4">
            <div class="form-item">
              <div class="label">分施次数</div>
              <a-select v-model:value="splitTimes" :options="splitOptions" :disabled="useAlgorithm" />
            </div>
          </a-col>
          <a-col :xs="24" :sm="12" :md="4">
            <div class="form-item">
              <div class="label">施肥方式</div>
              <a-select v-model:value="quickMethod" :options="methodOptions" :disabled="useAlgorithm" />
            </div>
          </a-col>
        </a-row>
        <a-row :gutter="12">
          <a-col :xs="24" :md="8">
            <div class="form-item">
              <div class="label">计划日期</div>
              <a-date-picker v-model:value="quickDate" value-format="YYYY-MM-DD" style="width:100%" :disabled="useAlgorithm" />
            </div>
          </a-col>
          <a-col :xs="24" :md="16">
            <div class="form-item">
              <div class="label">备注</div>
              <a-input v-model:value="quickRemark" placeholder="备注（可选）" />
            </div>
          </a-col>
        </a-row>
        <div class="calc-result">{{ calcResultText }}</div>
        <div class="quick-actions">
          <a-space :size="8" align="center">
            <a-button type="default" v-if="useAlgorithm" @click="refreshQuickPlan">刷新推荐</a-button>
            <a-button type="default" v-else @click="calcProductUsage">计算用量</a-button>
            <a-button type="primary" @click="quickGenerateRecord">生成记录</a-button>
            <a-button @click="quickExecute">执行自动化（占位）</a-button>
          </a-space>
        </div>
      </a-space>
    </a-card>

    <!-- 表单弹窗 -->
    <FertilizationModal @register="registerModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, watch } from 'vue';
import { BasicTable, useTable, TableAction } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useMessage } from '/@/hooks/web/useMessage';
import { Icon } from '/@/components/Icon';
import { columns, searchFormSchema } from './fertilization.data';
import { getFertilizationList, deleteFertilization, exportFertilization, importFertilization, getPlotNutrientStatus, getFertilizationRecommendation, getWeatherForecast, executeQuickFertilization, planQuickFertilization } from './fertilization.api';
import FertilizationModal from './FertilizationModal.vue';
import BaseSelect from '@/views/rapeseed/production-plan/plot-production-plan/components/BaseSelect.vue';
import PlotSelect from '@/views/rapeseed/production-plan/plot-production-plan/components/PlotSelect.vue';
import { Tag } from 'ant-design-vue';
import { useECharts } from '/@/hooks/web/useECharts';

const { createMessage } = useMessage();
const [registerModal, { openModal }] = useModal();
const searchInfo = reactive<Recordable>({});
const fileList = ref<any[]>([]);

// 加载状态
const loading = ref(false);

const [registerTable, { reload }] = useTable({
  title: '施肥管理',
  api: getFertilizationList,
  columns,
  formConfig: {
    labelWidth: 120,
    schemas: searchFormSchema,
    autoSubmitOnEnter: true,
  },
  useSearchForm: true,
  showTableSetting: true,
  bordered: true,
  showIndexColumn: false,
  actionColumn: {
    width: 80,
    title: '操作',
    dataIndex: 'action',
    fixed: 'right',
  },
});

// 选择相关
const selectedBaseId = ref<string | number | undefined>(undefined);
const selectedPlotId = ref<string | number | undefined>(undefined);
const selectedPlotName = ref<string>('');
const plotSelectRef = ref<any>(null);

// 土壤养分
const nPercent = ref<number>(0);
const pPercent = ref<number>(0);
const kPercent = ref<number>(0);

const nColor = ref<any>({ from: '#91d5ff', to: '#1890ff' });
const pColor = ref<any>({ from: '#ffd666', to: '#faad14' });
const kColor = ref<any>({ from: '#b7eb8f', to: '#52c41a' });

const nStatus = ref<'normal' | 'exception' | 'active'>('normal');
const pStatus = ref<'normal' | 'exception' | 'active'>('normal');
const kStatus = ref<'normal' | 'exception' | 'active'>('normal');

const nStateLabel = ref<string>('适中');
const pStateLabel = ref<string>('适中');
const kStateLabel = ref<string>('适中');
const nTagColor = ref<string>('blue');
const pTagColor = ref<string>('gold');
const kTagColor = ref<string>('green');

const trendSummary = ref<string>('稳定');
const leachingRiskLabel = ref<string>('低');
const leachingRiskColor = ref<string>('green');
const targetYieldText = ref<string>('—');

// 建议（QUEFTS）
const suggestion = reactive({
  needFertilization: false,
  recommendedTime: '',
  method: '',
  recommendN: 0,
  recommendP2O5: 0,
  recommendK2O: 0,
  reason: '',
});

// 图表 refs
const chartsAnchorRef = ref<any>(null);
const nTrendRef = ref<HTMLDivElement | null>(null);
const pTrendRef = ref<HTMLDivElement | null>(null);
const rainTrendRef = ref<HTMLDivElement | null>(null);
const tempTrendRef = ref<HTMLDivElement | null>(null);

const { setOptions: setNOptions } = useECharts(nTrendRef);
const { setOptions: setPOptions } = useECharts(pTrendRef);
const { setOptions: setRainOptions } = useECharts(rainTrendRef);
const { setOptions: setTempOptions } = useECharts(tempTrendRef);

function onBaseChange(baseId) {
  selectedBaseId.value = baseId;
  selectedPlotId.value = undefined;
  selectedPlotName.value = '';
}

function onPlotChange(plotId) {
  selectedPlotId.value = plotId;
  if (plotSelectRef.value && plotSelectRef.value.plotOptions) {
    const plot = plotSelectRef.value.plotOptions.find((p) => p.id === plotId);
    selectedPlotName.value = plot ? plot.plotName : '';
  }
  loadFertilizationOverview();
}

async function loadFertilizationOverview() {
  if (!selectedPlotId.value) return;
  try {
    loading.value = true;
    const status = await getPlotNutrientStatus(selectedPlotId.value as any);
    // 养分百分比（0-100）
    nPercent.value = status?.nPercent ?? 55;
    pPercent.value = status?.pPercent ?? 45;
    kPercent.value = status?.kPercent ?? 60;

    // 状态与标签
    const toState = (pct: number) => (pct < 40 ? '偏低' : pct > 70 ? '偏高' : '适中');
    const toTagColor = (pct: number) => (pct < 40 ? 'orange' : pct > 70 ? 'red' : 'blue');
    const toStatus = (pct: number) => (pct < 40 ? 'exception' : 'normal');

    nStateLabel.value = toState(nPercent.value);
    pStateLabel.value = toState(pPercent.value);
    kStateLabel.value = toState(kPercent.value);
    nTagColor.value = toTagColor(nPercent.value);
    pTagColor.value = toTagColor(pPercent.value);
    kTagColor.value = toTagColor(kPercent.value);
    nStatus.value = toStatus(nPercent.value) as any;
    pStatus.value = toStatus(pPercent.value) as any;
    kStatus.value = toStatus(kPercent.value) as any;

    targetYieldText.value = `${status?.targetYield ?? 180} kg/亩`;

    // 建议
    const rec = await getFertilizationRecommendation({ plotId: selectedPlotId.value });
    suggestion.needFertilization = rec?.needFertilization ?? true;
    suggestion.recommendedTime = rec?.recommendedTime ?? '本周内晴后施肥';
    suggestion.method = rec?.method ?? '条施/滴灌施肥';
    suggestion.recommendN = rec?.recommendN ?? 5.5;
    suggestion.recommendP2O5 = rec?.recommendP2O5 ?? 3.0;
    suggestion.recommendK2O = rec?.recommendK2O ?? 4.0;
    suggestion.reason = rec?.reason ?? '依据QUEFTS与一周天气预报，当前氮素偏低，建议少量多次分施以降低淋溶风险。';

    // 天气数据与趋势
    const forecast = await getWeatherForecast(selectedPlotId.value as any);
    const days = forecast?.days ?? buildMockForecast();
    lastForecastDays.value = days;
    trendSummary.value = summarizeTrend(days);
    leachingRiskLabel.value = calcLeachingRisk(days);
    leachingRiskColor.value = leachingRiskLabel.value === '高' ? 'red' : leachingRiskLabel.value === '中' ? 'orange' : 'green';

    renderNTrendChart(days);
    renderPTrendChart(days);
    renderRainChart(days);
    renderTempChart(days);
    // 加载完成后刷新算法推荐
    refreshQuickPlan();
  } catch (e) {
    // 使用回退数据
    const days = buildMockForecast();
    renderNTrendChart(days);
    renderPTrendChart(days);
    renderRainChart(days);
    renderTempChart(days);
    lastForecastDays.value = days;
    refreshQuickPlan();
  } finally {
    loading.value = false;
  }
}

function buildMockForecast() {
  const baseDate = new Date();
  return Array.from({ length: 7 }).map((_, i) => {
    const d = new Date(baseDate.getTime() - (6 - i) * 24 * 3600 * 1000);
    const day = `${d.getMonth() + 1}/${d.getDate()}`;
    return {
      day,
      rain: Math.round(Math.random() * 15),
      temp: Math.round(12 + Math.random() * 10),
      et0: Number((2 + Math.random() * 3).toFixed(2)),
      n: Number((40 + Math.random() * 20).toFixed(1)),
      p: Number((25 + Math.random() * 15).toFixed(1)),
    };
  });
}

function summarizeTrend(days: any[]) {
  const dn = days.map((d) => d.n);
  const dp = days.map((d) => d.p);
  const trendN = dn[6] - dn[0];
  const trendP = dp[6] - dp[0];
  const tN = trendN > 2 ? '上升' : trendN < -2 ? '下降' : '稳定';
  const tP = trendP > 2 ? '上升' : trendP < -2 ? '下降' : '稳定';
  return `N${tN} / P${tP}`;
}

function calcLeachingRisk(days: any[]) {
  const totalRain = days.reduce((acc, d) => acc + d.rain, 0);
  if (totalRain > 70) return '高';
  if (totalRain > 30) return '中';
  return '低';
}

function renderNTrendChart(days: any[]) {
  setNOptions({
    grid: { left: 48, right: 16, bottom: 32, top: 32 },
    xAxis: { type: 'category', data: days.map((d) => d.day) },
    yAxis: { type: 'value', name: 'N (mg/kg)' },
    tooltip: { trigger: 'axis' },
    series: [{ type: 'line', data: days.map((d) => d.n), smooth: true, color: '#1890ff' }],
  });
}

function renderPTrendChart(days: any[]) {
  setPOptions({
    grid: { left: 48, right: 16, bottom: 32, top: 32 },
    xAxis: { type: 'category', data: days.map((d) => d.day) },
    yAxis: { type: 'value', name: 'P (mg/kg)' },
    tooltip: { trigger: 'axis' },
    series: [{ type: 'line', data: days.map((d) => d.p), smooth: true, color: '#faad14' }],
  });
}

function renderRainChart(days: any[]) {
  setRainOptions({
    grid: { left: 48, right: 16, bottom: 32, top: 32 },
    xAxis: { type: 'category', data: days.map((d) => d.day) },
    yAxis: { type: 'value', name: '降雨量 (mm)' },
    tooltip: { trigger: 'axis' },
    series: [{ type: 'bar', data: days.map((d) => d.rain), color: '#69c0ff' }],
  });
}

function renderTempChart(days: any[]) {
  setTempOptions({
    grid: { left: 48, right: 16, bottom: 32, top: 32 },
    xAxis: { type: 'category', data: days.map((d) => d.day) },
    yAxis: { type: 'value', name: '温度 (°C)' },
    tooltip: { trigger: 'axis' },
    series: [{ type: 'line', data: days.map((d) => d.temp), smooth: true, color: '#ff7875' }],
  });
}

// 算法驱动一键施肥：开关与缓存
const useAlgorithm = ref(true);
const lastForecastDays = ref<any[]>([]);

function formatDate(d: Date) {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${dd}`;
}

function applyPlanToUI(plan: any) {
  if (!plan) return;
  fertilizerFormula.value = plan.formula || fertilizerFormula.value;
  splitTimes.value = plan.splitTimes || splitTimes.value;
  quickMethod.value = plan.method || quickMethod.value;
  quickDate.value = plan.planDate || quickDate.value;
  const perMu = plan.perMuWeightKg ?? productWeightPerMu.value;
  const total = plan.totalWeightKg ?? totalProductWeight.value;
  productWeightPerMu.value = Number((perMu as number).toFixed ? (perMu as number).toFixed(2) : perMu);
  totalProductWeight.value = Number((total as number).toFixed ? (total as number).toFixed(2) : total);
  const perSplit = splitTimes.value > 0 ? Number((totalProductWeight.value / splitTimes.value).toFixed(2)) : totalProductWeight.value;
  calcResultText.value = `算法推荐：配方 ${fertilizerFormula.value}，每亩约 ${productWeightPerMu.value} kg，总计 ${totalProductWeight.value} kg；分施 ${splitTimes.value} 次，每次约 ${perSplit} kg；方式 ${quickMethod.value}`;
}

function computePlanFallback() {
  // 根据推荐 N/P/K 选择使每亩产品用量最小的配方
  let bestFormula = fertilizerFormula.value;
  let bestPerMu = Infinity;
  for (const opt of formulaOptions) {
    const fm = formulaMap[opt.value] || { N: 0, P: 0, K: 0 };
    const wN = fm.N > 0 ? suggestion.recommendN / (fm.N / 100) : 0;
    const wP = fm.P > 0 ? suggestion.recommendP2O5 / (fm.P / 100) : 0;
    const wK = fm.K > 0 ? suggestion.recommendK2O / (fm.K / 100) : 0;
    const perMuCandidate = Math.max(wN, wP, wK);
    if (perMuCandidate > 0 && perMuCandidate < bestPerMu) {
      bestPerMu = perMuCandidate;
      bestFormula = opt.value;
    }
  }
  const risk = leachingRiskLabel.value;
  const splits = risk === '高' ? 3 : risk === '中' ? 2 : 1;
  const method = risk === '高' ? '滴灌施肥' : '条施';
  // 找到一个较小降雨的计划日期（否则明天）
  let planD = new Date();
  planD.setDate(planD.getDate() + 1);
  try {
    const candidate = (lastForecastDays.value || []).find((d) => (d?.rain ?? 0) <= 5);
    if (candidate && typeof candidate.day === 'string') {
      const [mm, dd] = (candidate.day as string).split('/');
      const now = new Date();
      planD = new Date(now.getFullYear(), Number(mm) - 1, Number(dd));
    }
  } catch {}
  const perMu = bestPerMu === Infinity ? 0 : Number(bestPerMu.toFixed(2));
  const total = Number((perMu * quickArea.value).toFixed(2));
  return {
    formula: bestFormula,
    splitTimes: splits,
    method,
    planDate: formatDate(planD),
    perMuWeightKg: perMu,
    totalWeightKg: total,
  };
}

async function refreshQuickPlan() {
  if (!useAlgorithm.value) {
    calcProductUsage();
    return;
  }
  try {
    const res = await planQuickFertilization({
      plotId: selectedPlotId.value,
      recommend: {
        nKgPerMu: suggestion.recommendN,
        p2o5KgPerMu: suggestion.recommendP2O5,
        k2oKgPerMu: suggestion.recommendK2O,
      },
      risk: leachingRiskLabel.value,
      areaMu: quickArea.value,
    });
    const plan = (res && (res.plan || res)) || computePlanFallback();
    applyPlanToUI(plan);
  } catch (e) {
    applyPlanToUI(computePlanFallback());
  }
}

// 一键施肥管理：配方与参数
const fertilizerFormula = ref<string>('15-15-15');
const formulaOptions = [
  { label: '15-15-15', value: '15-15-15' },
  { label: '20-10-10', value: '20-10-10' },
  { label: '12-24-12', value: '12-24-12' },
  { label: '17-17-17', value: '17-17-17' },
  { label: '0-20-20', value: '0-20-20' },
];
const formulaMap: Record<string, { N: number; P: number; K: number }> = {
  '15-15-15': { N: 15, P: 15, K: 15 },
  '20-10-10': { N: 20, P: 10, K: 10 },
  '12-24-12': { N: 12, P: 24, K: 12 },
  '17-17-17': { N: 17, P: 17, K: 17 },
  '0-20-20': { N: 0, P: 20, K: 20 },
};
const quickArea = ref<number>(1);
const splitTimes = ref<number>(2);
const splitOptions = [
  { label: '1次', value: 1 },
  { label: '2次', value: 2 },
  { label: '3次', value: 3 },
];
const methodOptions = [
  { label: '条施', value: '条施' },
  { label: '撒施', value: '撒施' },
  { label: '滴灌施肥', value: '滴灌施肥' },
];
const quickMethod = ref<string>('条施');
const quickDate = ref<string>('');
const quickRemark = ref<string>('');
const productWeightPerMu = ref<number>(0);
const totalProductWeight = ref<number>(0);
const calcResultText = ref<string>('');

function calcProductUsage() {
  const fm = formulaMap[fertilizerFormula.value] || { N: 0, P: 0, K: 0 };
  const wN = fm.N > 0 ? suggestion.recommendN / (fm.N / 100) : 0;
  const wP = fm.P > 0 ? suggestion.recommendP2O5 / (fm.P / 100) : 0;
  const wK = fm.K > 0 ? suggestion.recommendK2O / (fm.K / 100) : 0;
  const candidates = [wN, wP, wK].filter((x) => x > 0);
  const perMu = candidates.length ? Math.max(...candidates) : 0;
  productWeightPerMu.value = Number(perMu.toFixed(2));
  totalProductWeight.value = Number((productWeightPerMu.value * quickArea.value).toFixed(2));
  const perSplit = splitTimes.value > 0 ? Number((totalProductWeight.value / splitTimes.value).toFixed(2)) : totalProductWeight.value;
  calcResultText.value = `配方 ${fertilizerFormula.value}，每亩约 ${productWeightPerMu.value} kg，总计 ${totalProductWeight.value} kg；分施 ${splitTimes.value} 次，每次约 ${perSplit} kg`;
}

async function quickGenerateRecord() {
  if (!selectedPlotName.value) {
    createMessage.warn('请先选择基地与地块');
    return;
  }
  if (useAlgorithm.value) {
    await refreshQuickPlan();
  } else {
    calcProductUsage();
  }
  const remark = `${quickRemark.value || ''}（建议：${suggestion.reason}；分施${splitTimes.value}次；配方${fertilizerFormula.value}）`;
  openModal(true, {
    isUpdate: false,
    defaultValues: {
      plotName: selectedPlotName.value,
      fertilizationType: suggestion.needFertilization ? '追肥' : '基肥',
      fertilizerName: `复合肥(${fertilizerFormula.value})`,
      fertilizerAmount: productWeightPerMu.value,
      fertilizationArea: quickArea.value,
      fertilizationDate: quickDate.value || undefined,
      fertilizationMethod: quickMethod.value,
      responsiblePerson: '',
      remark,
    },
  });
}

async function quickExecute() {
  if (!selectedPlotId.value) {
    createMessage.warn('请先选择基地与地块');
    return;
  }
  if (useAlgorithm.value) {
    await refreshQuickPlan();
  } else {
    calcProductUsage();
  }
  try {
    await executeQuickFertilization({
      plotId: selectedPlotId.value,
      formula: fertilizerFormula.value,
      areaMu: quickArea.value,
      splitTimes: splitTimes.value,
      method: quickMethod.value,
      planDate: quickDate.value || null,
      totalWeightKg: totalProductWeight.value,
      perMuWeightKg: productWeightPerMu.value,
      recommend: {
        nKgPerMu: suggestion.recommendN,
        p2o5KgPerMu: suggestion.recommendP2O5,
        k2oKgPerMu: suggestion.recommendK2O,
      },
      remark: quickRemark.value || '',
    });
    createMessage.success('已提交自动化执行（占位），待后端接入');
  } catch (e) {
    createMessage.error('执行失败（占位接口），请稍后重试');
  }
}

// 监听推荐剂量/配方/面积/分施次数变化，自动计算用量
watch([
  () => suggestion.recommendN,
  () => suggestion.recommendP2O5,
  () => suggestion.recommendK2O,
  fertilizerFormula,
  quickArea,
  splitTimes,
], () => {
  if (useAlgorithm.value) {
    refreshQuickPlan();
  } else {
    calcProductUsage();
  }
}, { immediate: true });

// 根据当前 N/P/K 百分比更新状态与标签
function applyNPKStates() {
  const toState = (pct: number) => (pct < 40 ? '偏低' : pct > 70 ? '偏高' : '适中');
  const toTagColor = (pct: number) => (pct < 40 ? 'orange' : pct > 70 ? 'red' : 'blue');
  const toStatus = (pct: number) => (pct < 40 ? 'exception' : 'normal');

  nStateLabel.value = toState(nPercent.value);
  pStateLabel.value = toState(pPercent.value);
  kStateLabel.value = toState(kPercent.value);
  nTagColor.value = toTagColor(nPercent.value);
  pTagColor.value = toTagColor(pPercent.value);
  kTagColor.value = toTagColor(kPercent.value);
  nStatus.value = toStatus(nPercent.value) as any;
  pStatus.value = toStatus(pPercent.value) as any;
  kStatus.value = toStatus(kPercent.value) as any;
}

// 初始展示的预设数据（无后端亦可展示）
function loadMockOverview() {
  // 预设 N/P/K 百分比与目标产量
  nPercent.value = 58;
  pPercent.value = 47;
  kPercent.value = 62;
  targetYieldText.value = '185 kg/亩';
  applyNPKStates();

  // 预设建议（QUEFTS）
  suggestion.needFertilization = true;
  suggestion.recommendedTime = '本周晴后分次施肥';
  suggestion.method = '条施/滴灌施肥';
  suggestion.recommendN = 6.0;
  suggestion.recommendP2O5 = 3.2;
  suggestion.recommendK2O = 4.5;
  suggestion.reason = '依据预设土壤供肥能力与近期天气，建议少量多次分施以降低淋溶风险。';

  // 预设天气与趋势
  const days = buildMockForecast();
  lastForecastDays.value = days;
  trendSummary.value = summarizeTrend(days);
  leachingRiskLabel.value = calcLeachingRisk(days);
  leachingRiskColor.value = leachingRiskLabel.value === '高' ? 'red' : leachingRiskLabel.value === '中' ? 'orange' : 'green';

  renderNTrendChart(days);
  renderPTrendChart(days);
  renderRainChart(days);
  renderTempChart(days);
  // 算法推荐计划
  refreshQuickPlan();
}

function oneClickCreate() {
  if (!selectedPlotName.value) {
    createMessage.warning('请先选择地块');
    return;
  }
  openModal(true, {
    isUpdate: false,
    defaultValues: {
      plotName: selectedPlotName.value,
      fertilizationType: suggestion.needFertilization ? '追肥' : '基肥',
      fertilizerName: '复合肥(N-P-K)',
      fertilizerAmount: Number((suggestion.recommendN + suggestion.recommendP2O5 + suggestion.recommendK2O).toFixed(2)),
      fertilizationArea: 1,
      fertilizationDate: new Date(),
      fertilizationMethod: suggestion.method || '条施',
      responsiblePerson: '',
      remark: suggestion.reason || '',
    },
  });
}

function scrollToCharts() {
  const el = chartsAnchorRef.value?.$el || chartsAnchorRef.value;
  if (el && el.scrollIntoView) {
    el.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }
}

onMounted(() => {
  // 初始加载预设数据，选择地块后再加载真实数据
  loadMockOverview();
});

function handleCreate() {
  openModal(true, {
    isUpdate: false,
  });
}

function handleEdit(record: Recordable) {
  openModal(true, {
    record,
    isUpdate: true,
  });
}

async function handleDelete(record: Recordable) {
  await deleteFertilization(record.id);
  createMessage.success('删除成功');
  reload();
}

function handleSuccess() {
  createMessage.success('操作成功');
  reload();
}

async function handleExport() {
  const data = await getFertilizationList(searchInfo);
  exportFertilization(data);
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
    await importFertilization(formData);
    createMessage.success('导入成功');
    reload();
    fileList.value = [];
  } catch (error) {
    createMessage.error('导入失败');
  }
}
</script>

<style lang="less" scoped>
.fertilization-page {
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

.top-row {
  margin-bottom: 16px;
  align-items: stretch;
  .ant-card {
    height: 100%;
  }
}

.rich-card {
  .nutrient-card {
    display: grid;
    grid-template-columns: 1fr;
    gap: 12px;
    margin-bottom: 12px;
  }
  .nutrient-item {
    display: grid;
    grid-template-columns: 56px 1fr auto;
    align-items: center;
    gap: 8px;
  }
  .label {
    color: #595959;
  }
  .statistic-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
  }
  .metric-label {
    color: #8c8c8c;
    font-size: 12px;
  }
  .metric-value {
    font-size: 16px;
    font-weight: 600;
  }
  .suggestion-card {
    display: grid;
    gap: 8px;
  }
  .suggestion-line {
    color: #595959;
  }
  .dose-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;
    margin: 8px 0;
  }
  .dose-item { background: #fafafa; padding: 8px; border-radius: 6px; }
  .dose-label { color: #8c8c8c; font-size: 12px; }
  .dose-value { font-weight: 600; font-size: 16px; }
}

.chart-card {
  margin-top: 16px;
}
.quick-card {
  margin-top: 16px;
}
.quick-card .form-item {
  background: #fafafa;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 8px 10px;
}
.form-item .label {
  font-size: 12px;
  color: #8c8c8c;
  margin-bottom: 4px;
}
.quick-card .form-item .label {
  font-size: 13px;
  color: #606266;
  margin-bottom: 6px;
}
.quick-card .ant-select,
.quick-card .ant-input-number,
.quick-card .ant-picker,
.quick-card .ant-input {
  width: 100%;
}
.calc-result {
  background: #fafafa;
  padding: 8px 12px;
  border-radius: 4px;
}
.quick-actions {
  display: flex;
  justify-content: center;
  width: 100%;
  margin-top: 4px;
}
.chart-grid { align-items: stretch; }
.chart-box { background: #fff; border: 1px solid #f0f0f0; border-radius: 8px; padding: 8px; height: 300px; display: flex; flex-direction: column; }
.chart-title { font-weight: 500; color: #434343; margin-bottom: 8px; }
.chart-ref { flex: 1; width: 100%; }

// 响应式布局
@media (max-width: 768px) {
  .fertilization-page {
    padding: 16px;
  }
  .statistic-grid { grid-template-columns: 1fr !important; }
  .dose-grid { grid-template-columns: 1fr !important; }
  .chart-box { height: 260px; }
}

/* 大屏优化 */
@media (min-width: 1600px) {
  .fertilization-page { padding: 32px; }
  .chart-box { height: 360px; }
  .quick-card { margin-top: 20px; }
}
</style>
