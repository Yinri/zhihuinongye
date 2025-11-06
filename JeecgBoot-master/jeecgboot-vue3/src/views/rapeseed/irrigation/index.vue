<template>
  <div class="irrigation-page">
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
            <PlotSelect ref="plotSelectRef" v-model="selectedPlotId" :base-id="selectedBaseId" @change="onPlotChange" @loaded="onPlotLoaded" />
          </a-space>
        </a-card>
      </a-col>
      <a-col :xs="24" :md="8">
        <a-card :bordered="false" class="rich-card">
          <template #title>
            <div class="table-title">
              <Icon icon="ant-design:dot-chart-outlined" /> 土壤水分概览
            </div>
          </template>
          <div class="moisture-card">
            <a-progress type="dashboard" :percent="soilMoisturePercent" :status="moistureStatus" />
            <div class="moisture-info">
              <div>当前含水率：{{ soilMoisturePercent }}%</div>
              <div>
                含水状态：<Tag :color="moistureStateColor">{{ moistureStateLabel }}</Tag>
              </div>
            </div>
          </div>
          <div class="statistic-grid">
            <div class="metric">
              <div class="metric-title">趋势</div>
              <div class="metric-value">{{ soilMoistureTrendText }}</div>
            </div>
            <div class="metric">
              <div class="metric-title">上次更新</div>
              <div class="metric-value">{{ lastUpdatedText }}</div>
            </div>
          </div>
          
        </a-card>
      </a-col>
      <a-col :xs="24" :md="8">
        <a-card :bordered="false" class="rich-card">
          <template #title>
            <div class="table-title">
              <Icon icon="ant-design:solution-outlined" /> 灌溉建议（Penman）
            </div>
          </template>
          <div class="suggestion-card">
            <a-descriptions bordered size="small" :column="1">
              <a-descriptions-item label="是否需要灌溉">
                <Tag :color="penmanSuggestion.needIrrigation ? 'red' : 'green'">{{ penmanSuggestion.needIrrigation ? '需要' : '暂不需要' }}</Tag>
              </a-descriptions-item>
              <a-descriptions-item label="建议时间">{{ penmanSuggestion.recommendedTime || '-' }}</a-descriptions-item>
              <a-descriptions-item label="建议方式">{{ penmanSuggestion.method || '-' }}</a-descriptions-item>
              <a-descriptions-item label="推荐灌水量">
                {{ recommendedVolumeMm }} mm（约 {{ mmToM3PerMu(recommendedVolumeMm) }} m³/亩）
              </a-descriptions-item>
              <a-descriptions-item label="原因" v-if="penmanSuggestion.reason">{{ penmanSuggestion.reason }}</a-descriptions-item>
            </a-descriptions>
            <div class="actions">
              <a-button type="primary" size="small" @click="handleCreateWithSuggestion" :disabled="!selectedPlotId">按建议新增灌溉记录</a-button>
              <a-button size="small" @click="copySuggestion" :disabled="!selectedPlotId">复制建议</a-button>
              <a-button size="small" @click="refresh" :disabled="!selectedPlotId">刷新数据</a-button>
            </div>
            
          </div>
        </a-card>
      </a-col>
    </a-row>

    

    <!-- 趋势折线图板块（四个方框） -->
    <a-row :gutter="16" class="mt-4">
      <a-col :xs="24" :md="12">
        <a-card :bordered="false" class="rich-card">
          <template #title>
            <div class="table-title">
              <Icon icon="ant-design:line-chart-outlined" /> 土壤含水率（近7天）
            </div>
          </template>
          <div ref="moistureTrendRef" class="chart-ref chart-220" />
        </a-card>
      </a-col>
      <a-col :xs="24" :md="12">
        <a-card :bordered="false" class="rich-card">
          <template #title>
            <div class="table-title">
              <Icon icon="ant-design:line-chart-outlined" /> 参考蒸散（ET0，近7天）
            </div>
          </template>
          <div ref="et0ChartRef" style="width: 100%; height: 220px" />
        </a-card>
      </a-col>
      <a-col :xs="24" :md="12">
        <a-card :bordered="false" class="rich-card">
          <template #title>
            <div class="table-title">
              <Icon icon="ant-design:line-chart-outlined" /> 降雨量（近7天）
            </div>
          </template>
          <div ref="rainChartRef" style="width: 100%; height: 220px" />
        </a-card>
      </a-col>
      <a-col :xs="24" :md="12">
        <a-card :bordered="false" class="rich-card">
          <template #title>
            <div class="table-title">
              <Icon icon="ant-design:line-chart-outlined" /> 平均温度（近7天）
            </div>
          </template>
          <div ref="tempChartRef" style="width: 100%; height: 220px" />
        </a-card>
      </a-col>
    </a-row>

    <!-- Penman 输入数据折线图 -->
    <a-row :gutter="16" class="mt-4">
      <a-col :xs="24" :lg="24">
        <a-card :bordered="false" class="rich-card">
          <template #title>
            <div class="table-title">
              <Icon icon="ant-design:line-chart-outlined" /> Penman 算法输入数据（近7天）
            </div>
          </template>
          <div ref="penmanInputsChartRef" style="width: 100%; height: 320px" />
        </a-card>
      </a-col>
    </a-row>

    <!-- 对比图 -->
    <a-row :gutter="16" class="mt-4">
      <a-col :xs="24" :lg="24">
        <a-card :bordered="false">
          <template #title>
            <div class="table-title">
              <Icon icon="ant-design:area-chart-outlined" /> 干预灌溉 vs 不干预对比
            </div>
          </template>
          <div ref="comparisonChartRef" class="chart-ref chart-320" />
        </a-card>
      </a-col>
    </a-row>

    <!-- 天气与蒸散 / 风险与操作 -->
    <a-row :gutter="16" class="mt-4 row-stretch">
      <a-col :xs="24" :md="12">
        <a-card :bordered="false" class="rich-card">
          <template #title>
            <div class="table-title">
              <Icon icon="ant-design:cloud-outlined" /> 天气与蒸散概览
            </div>
          </template>
          <a-space direction="vertical" style="width: 100%">
            <div class="statistic-grid">
              <div class="metric">
                <div class="metric-title">今日 ET0</div>
                <div class="metric-value">{{ et0Forecast[0] }} mm</div>
              </div>
              <div class="metric">
                <div class="metric-title">明日 ET0</div>
                <div class="metric-value">{{ et0Forecast[1] }} mm</div>
              </div>
              <div class="metric">
                <div class="metric-title">后日 ET0</div>
                <div class="metric-value">{{ et0Forecast[2] }} mm</div>
              </div>
            </div>
            <div class="hint">基于 Penman 估算的参考蒸散量，供灌溉决策参考。</div>
          </a-space>
        </a-card>
      </a-col>
      <a-col :xs="24" :md="12">
        <a-card :bordered="false" class="rich-card">
          <template #title>
            <div class="table-title">
              <Icon icon="ant-design:warning-outlined" /> 风险与提示 / 快捷操作
            </div>
          </template>
          <a-space direction="vertical" style="width: 100%">
            <a-alert :message="`当前灌溉风险：${riskLevel}`" type="warning" show-icon />
            <ul class="tips">
              <li v-for="(tip, idx) in riskTips" :key="idx">{{ tip }}</li>
            </ul>
            <div class="actions">
              <a-button type="primary" size="small" @click="handleCreateWithSuggestion" :disabled="!selectedPlotId">按建议新增灌溉记录</a-button>
              <a-button size="small" @click="copySuggestion" :disabled="!selectedPlotId">复制建议</a-button>
              <a-button size="small" @click="refresh" :disabled="!selectedPlotId">刷新数据</a-button>
            </div>
          </a-space>
        </a-card>
      </a-col>
    </a-row>

    <!-- 已删除灌溉管理列表板块 -->

    <!-- 一键灌溉管理（移至页面底部） -->
    <a-row class="mt-4">
      <a-col :span="24">
        <a-card :bordered="false" class="rich-card">
          <template #title>
            <div class="table-title">
              <Icon icon="ant-design:tool-outlined" /> 一键灌溉管理
            </div>
          </template>
          <div class="quick-card">
            <a-space direction="vertical" style="width: 100%; gap: 8px;">
              <a-row :gutter="12" justify="center" align="middle">
                <a-col :xs="24" :sm="12" :md="3">
                  <div class="form-item form-inline">
                    <div class="label">算法推荐</div>
                    <a-switch v-model:checked="quickUseAlgorithm" size="small" />
                  </div>
                </a-col>
                <a-col :xs="24" :sm="12" :md="5">
                  <div class="form-item">
                    <div class="label">选择基地</div>
                    <BaseSelect v-model="selectedBaseId" @change="onBaseChange" />
                  </div>
                </a-col>
                <a-col :xs="24" :sm="12" :md="5">
                  <div class="form-item">
                    <div class="label">选择地块</div>
                    <PlotSelect ref="plotSelectRef" v-model="selectedPlotId" :base-id="selectedBaseId" @change="onPlotChange" />
                  </div>
                </a-col>
                <a-col :xs="24" :sm="12" :md="4">
                  <div class="form-item">
                    <div class="label">灌溉面积(亩)</div>
                    <a-input-number v-model:value="quickArea" :min="0" :step="0.1" style="width: 100%" />
                  </div>
                </a-col>
                <a-col :xs="24" :sm="12" :md="4">
                  <div class="form-item">
                    <div class="label">灌溉次数</div>
                    <a-input-number v-model:value="quickTimes" :min="1" :step="1" style="width: 100%" />
                  </div>
                </a-col>
                <a-col :xs="24" :sm="12" :md="3">
                  <div class="form-item">
                    <div class="label">灌溉方式</div>
                    <a-select v-model:value="quickMethod" :options="irrigationMethodOptions" style="width: 100%" />
                  </div>
                </a-col>
                <!-- 已移除：用水量(立方米/亩) 输入项 -->
              </a-row>

              <a-row :gutter="12" justify="center" align="middle">
                <a-col :xs="24" :md="8">
                  <div class="form-item">
                    <div class="label">灌溉日期</div>
                    <a-date-picker v-model:value="quickDate" style="width: 100%" />
                  </div>
                </a-col>
                <a-col :xs="24" :md="16">
                  <div class="form-item">
                    <div class="label">备注</div>
                    <a-input v-model:value="quickRemark" placeholder="备注（可选）" />
                  </div>
                </a-col>
              </a-row>

              <a-divider dashed style="margin: 8px 0;" />
              <div class="calc-result">
                <span v-if="quickUseAlgorithm">按建议：{{ recommendedVolumeMm }} mm ≈ {{ algoWaterPerMuM3 }} m³/亩</span>
                <span v-else>人工设定：每亩 {{ manualWaterPerMu }} m³</span>
                <span>分次灌溉：{{ quickTimes }} 次，每次约 {{ perTimeWaterM3 }} m³</span>
                <span>总用水量：{{ totalWaterUsageM3 }} m³，预估时长：{{ durationHours }} 小时</span>
              </div>

              <div class="quick-actions">
                <a-space :size="12" align="center">
                  <a-button type="primary" @click="calcQuickIrrigation" :disabled="!selectedPlotId">计算用水量</a-button>
                  <a-button @click="generateRecordQuick" :disabled="!selectedPlotId">生成记录</a-button>
                  <a-button @click="executeAutomationQuick" :disabled="!selectedPlotId">执行自动化（占位）</a-button>
                </a-space>
              </div>
            </a-space>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 表单弹窗 -->
    <IrrigationModal @register="registerModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, watch, Ref, computed } from 'vue';
// 已移除表格相关依赖
import { useModal } from '/@/components/Modal';
import { useMessage } from '/@/hooks/web/useMessage';
import { Icon } from '/@/components/Icon';
import { getPlotStatus, getPenmanPredict, getInterventionComparison } from './irrigation.api';
import IrrigationModal from './IrrigationModal.vue';
import BaseSelect from '../production-plan/components/BaseSelect.vue';
import PlotSelect from '../production-plan/components/PlotSelect.vue';
// GrowthTimeline 已移除对应板块
import { useECharts } from '/@/hooks/web/useECharts';
import { Tag } from 'ant-design-vue';

const { createMessage } = useMessage();

const [registerModal, { openModal }] = useModal();
const searchInfo = reactive<Recordable>({});
// 已移除导入相关的文件列表

// 选择相关
const selectedBaseId = ref<string | number | undefined>(undefined);
const selectedPlotId = ref<string | number | undefined>(undefined);
const selectedPlotName = ref<string>('');
const plotSelectRef = ref<any>(null);
const selectedVarietyId = ref<string | number | undefined>(undefined);
const currentStageId = ref<string | number | undefined>(undefined);

// 土壤水分与建议
const soilMoisturePercent = ref<number>(0);
const soilMoistureTrendText = ref<string>('稳定');
const penmanSuggestion = reactive({ needIrrigation: false, recommendedTime: '', method: '', reason: '' });

// 图表
const comparisonChartRef = ref<HTMLDivElement | null>(null);
const { setOptions: setComparisonOptions } = useECharts(comparisonChartRef as Ref<HTMLDivElement>);
const moistureTrendRef = ref<HTMLDivElement | null>(null);
const { setOptions: setMoistureTrendOptions } = useECharts(moistureTrendRef as Ref<HTMLDivElement>);
const et0ChartRef = ref<HTMLDivElement | null>(null);
const { setOptions: setEt0Options } = useECharts(et0ChartRef as Ref<HTMLDivElement>);
const penmanInputsChartRef = ref<HTMLDivElement | null>(null);
const { setOptions: setPenmanInputsOptions } = useECharts(penmanInputsChartRef as Ref<HTMLDivElement>);
const rainChartRef = ref<HTMLDivElement | null>(null);
const { setOptions: setRainOptions } = useECharts(rainChartRef as Ref<HTMLDivElement>);
const tempChartRef = ref<HTMLDivElement | null>(null);
const { setOptions: setTempOptions } = useECharts(tempChartRef as Ref<HTMLDivElement>);

function handleCreateWithSuggestion() {
  const defaults: Recordable = {
    plotName: selectedPlotName.value,
    irrigationMethod: penmanSuggestion.method || '滴灌',
    irrigationDate: penmanSuggestion.recommendedTime || undefined,
  };
  openModal(true, {
    isUpdate: false,
    defaultValues: defaults,
  });
}

function handleSuccess() {
  createMessage.success('操作成功');
}

// 选择与数据加载逻辑
function onBaseChange() {
  // 清空地块选择
  selectedPlotId.value = undefined;
  selectedPlotName.value = '';
}

function onPlotLoaded(options) {
  // 自动填充名称（如果有默认选择）
  if (selectedPlotId.value) {
    const found = options.find((o: any) => o.id === selectedPlotId.value);
    selectedPlotName.value = found ? found.plotName : '';
  }
}

function onPlotChange(value) {
  // 更新名称
  const options = plotSelectRef.value?.plotOptions || [];
  const found = options.find((o: any) => o.id === value);
  selectedPlotName.value = found ? found.plotName : '';

  // 拉取状态与建议
  fetchPlotStatusAndSuggest();
  fetchInterventionComparison();
}

async function fetchPlotStatusAndSuggest() {
  if (!selectedPlotId.value) return;
  try {
    const status = await getPlotStatus(selectedPlotId.value);
    soilMoisturePercent.value = Number(status?.soilMoisturePercent ?? 42);
    soilMoistureTrendText.value = status?.soilMoistureTrend ?? '下降';
    currentStageId.value = status?.currentStageId ?? '2';
    lastUpdatedText.value = new Date().toLocaleString();

    const suggest = await getPenmanPredict(selectedPlotId.value);
    penmanSuggestion.needIrrigation = Boolean(suggest?.needIrrigation ?? soilMoisturePercent.value < 45);
    penmanSuggestion.recommendedTime = suggest?.recommendedTime ?? '明日清晨 06:00-08:00';
    penmanSuggestion.method = suggest?.method ?? '滴灌';
    penmanSuggestion.reason = suggest?.reason ?? '土壤含水率偏低，蒸散需求较高';

    // 渲染折线图：含水率/ET0/Penman输入
    chartDates.value = ['D1','D2','D3','D4','D5','D6','D7'];
    moistureTrendData.value = generateMoistureTrendSeries(soilMoisturePercent.value, soilMoistureTrendText.value);
    renderMoistureTrendChart(chartDates.value, moistureTrendData.value);
    et0Forecast.value = [4.2, 4.0, 3.7, 4.3, 3.9, 4.1, 3.8];
    renderEt0Chart(chartDates.value, et0Forecast.value);
    penmanInputs.value = mockPenmanInputs(chartDates.value);
    renderPenmanInputsChart(penmanInputs.value);
    renderRainChart(chartDates.value, penmanInputs.value.precip);
    renderTempChart(chartDates.value, penmanInputs.value.temp);
  } catch (e) {
    // 兜底模拟
    soilMoisturePercent.value = 38;
    soilMoistureTrendText.value = '下降';
    currentStageId.value = '2';
    penmanSuggestion.needIrrigation = true;
    penmanSuggestion.recommendedTime = '明日清晨 06:00-08:00';
    penmanSuggestion.method = '滴灌';
    penmanSuggestion.reason = '土壤含水率较低且天气温热，建议适度灌溉';
    lastUpdatedText.value = new Date().toLocaleString();

    chartDates.value = ['D1','D2','D3','D4','D5','D6','D7'];
    moistureTrendData.value = generateMoistureTrendSeries(soilMoisturePercent.value, soilMoistureTrendText.value);
    renderMoistureTrendChart(chartDates.value, moistureTrendData.value);
    et0Forecast.value = [4.2, 4.0, 3.7, 4.3, 3.9, 4.1, 3.8];
    renderEt0Chart(chartDates.value, et0Forecast.value);
    penmanInputs.value = mockPenmanInputs(chartDates.value);
    renderPenmanInputsChart(penmanInputs.value);
    renderRainChart(chartDates.value, penmanInputs.value.precip);
    renderTempChart(chartDates.value, penmanInputs.value.temp);
  }
}

async function fetchInterventionComparison() {
  if (!selectedPlotId.value) return;
  try {
    const data = await getInterventionComparison(selectedPlotId.value);
    const dates = data?.dates ?? ['D1','D2','D3','D4','D5','D6','D7'];
    const withIrr = data?.withIrrigation ?? [55,58,60,62,60,58,56];
    const withoutIrr = data?.withoutIrrigation ?? [40,38,37,36,35,34,33];
    renderComparisonChart(dates, withIrr, withoutIrr);
  } catch (e) {
    renderComparisonChart(
      ['D1','D2','D3','D4','D5','D6','D7'],
      [55,58,60,62,60,58,56],
      [40,38,37,36,35,34,33]
    );
  }
}

function renderComparisonChart(dates: string[], withIrr: number[], withoutIrr: number[]) {
  setComparisonOptions({
    color: ['#37A2DA', '#ffd85c'],
    grid: { left: 40, right: 20, top: 40, bottom: 40 },
    tooltip: { trigger: 'axis', formatter: (params: any) => {
      const lines = params.map((p: any) => `${p.seriesName}: ${p.value}%`).join('<br/>');
      return `${params[0].axisValueLabel}<br/>${lines}`;
    } },
    legend: { data: ['干预灌溉','不干预灌溉'] },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value', name: '土壤含水率(%)' },
    series: [
      { name: '干预灌溉', type: 'line', smooth: true, data: withIrr, areaStyle: { opacity: 0.15 } },
      { name: '不干预灌溉', type: 'line', smooth: true, data: withoutIrr, areaStyle: { opacity: 0.15 } }
    ]
  });
}

function renderMoistureTrendChart(dates: string[], data: number[]) {
  setMoistureTrendOptions({
    color: ['#5B8FF9'],
    grid: { left: 38, right: 12, top: 24, bottom: 24 },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value', name: '%', min: 0, max: 100 },
    series: [{ name: '含水率', type: 'line', smooth: true, data, areaStyle: { opacity: 0.2 } }]
  });
}

function renderEt0Chart(dates: string[], data: number[]) {
  setEt0Options({
    color: ['#73C0DE'],
    grid: { left: 40, right: 20, top: 24, bottom: 24 },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value', name: 'mm/day' },
    series: [{ name: 'ET0', type: 'line', smooth: true, data, areaStyle: { opacity: 0.15 } }]
  });
}

function renderPenmanInputsChart(inputs: any) {
  setPenmanInputsOptions({
    grid: { left: 50, right: 30, top: 40, bottom: 40 },
    tooltip: { trigger: 'axis' },
    legend: { data: ['温度(°C)','湿度(%)','风速(m/s)','太阳辐射(MJ/m²)','降水(mm)'] },
    xAxis: { type: 'category', data: inputs.dates },
    yAxis: { type: 'value' },
    series: [
      { name: '温度(°C)', type: 'line', smooth: true, data: inputs.temp, areaStyle: { opacity: 0.08 } },
      { name: '湿度(%)', type: 'line', smooth: true, data: inputs.humidity, areaStyle: { opacity: 0.08 } },
      { name: '风速(m/s)', type: 'line', smooth: true, data: inputs.wind, areaStyle: { opacity: 0.08 } },
      { name: '太阳辐射(MJ/m²)', type: 'line', smooth: true, data: inputs.solar, areaStyle: { opacity: 0.08 } },
      { name: '降水(mm)', type: 'bar', data: inputs.precip, yAxisIndex: 0, itemStyle: { opacity: 0.6 } }
    ]
  });
}

function renderRainChart(dates: string[], precip: number[]) {
  setRainOptions({
    color: ['#54D8C7'],
    grid: { left: 40, right: 20, top: 24, bottom: 24 },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value', name: 'mm' },
    series: [{ name: '降雨量', type: 'bar', data: precip, itemStyle: { opacity: 0.7 } }]
  });
}

function renderTempChart(dates: string[], temp: number[]) {
  setTempOptions({
    color: ['#F6BD16'],
    grid: { left: 40, right: 20, top: 24, bottom: 24 },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value', name: '°C' },
    series: [{ name: '平均温度', type: 'line', smooth: true, data: temp, areaStyle: { opacity: 0.2 } }]
  });
}

// 水分状态颜色
const moistureStatus = computed(() => {
  if (soilMoisturePercent.value < 40) return 'exception';
  if (soilMoisturePercent.value < 60) return 'normal';
  return 'success';
});

// 额外状态与计算
const lastUpdatedText = ref<string>('-');
const moistureStateLabel = computed(() => {
  if (soilMoisturePercent.value < 40) return '偏低';
  if (soilMoisturePercent.value < 60) return '适中';
  return '偏高';
});
const moistureStateColor = computed(() => {
  if (soilMoisturePercent.value < 40) return 'red';
  if (soilMoisturePercent.value < 60) return 'blue';
  return 'green';
});

const recommendedVolumeMm = computed<number>(() => {
  if (!penmanSuggestion.needIrrigation) return 0;
  const m = soilMoisturePercent.value;
  if (m < 35) return 30;
  if (m < 45) return 25;
  if (m < 55) return 20;
  if (m < 60) return 15;
  return 10;
});

function mmToM3PerMu(mm: number): number {
  return Math.round(mm * 0.6667 * 10) / 10; // 1亩≈666.7m²，1mm≈0.6667m³/亩
}

const et0Forecast = ref<number[]>([4.2, 4.0, 3.7]);
const chartDates = ref<string[]>(['D1','D2','D3','D4','D5','D6','D7']);
const moistureTrendData = ref<number[]>([40, 42, 38, 36, 35, 37, 39]);
const penmanInputs = ref<any>({
  dates: chartDates.value,
  temp: [12, 13, 14, 15, 14, 13, 12],
  humidity: [70, 68, 65, 64, 66, 67, 69],
  wind: [1.2, 1.5, 1.8, 2.0, 1.6, 1.4, 1.3],
  solar: [16, 18, 17, 19, 18, 17, 16],
  precip: [0, 0, 5, 0, 1, 0, 0],
});

function generateMoistureTrendSeries(current: number, trend: string): number[] {
  const base = current;
  const series: number[] = [];
  for (let i = 0; i < 7; i++) {
    const delta = trend === '下降' ? -i * 1.2 : trend === '上升' ? i * 1.1 : (Math.sin(i) * 0.8);
    series.push(Math.max(0, Math.min(100, Math.round(base + delta))));
  }
  return series;
}

function mockPenmanInputs(dates: string[]) {
  // 简易模拟数据，可与后端天气接口对接
  const len = dates.length;
  const arr = (start: number, step: number, jitter = 0.5) => Array.from({ length: len }, (_, i) => Math.round((start + i * step + (Math.random() - 0.5) * jitter) * 10) / 10);
  return {
    dates,
    temp: arr(12, 0.5, 1.2),
    humidity: arr(72, -0.8, 2.0),
    wind: arr(1.3, 0.2, 0.6),
    solar: arr(16, 0.5, 1.5),
    precip: Array.from({ length: len }, () => Math.random() < 0.2 ? Math.round(Math.random() * 8) : 0),
  };
}
const riskLevel = computed(() => {
  if (soilMoisturePercent.value < 35) return '较高';
  if (soilMoisturePercent.value < 45) return '中等';
  return '较低';
});
const riskTips = computed<string[]>(() => {
  if (soilMoisturePercent.value < 40) {
    return [
      '建议在清晨低蒸发时段进行滴灌，避免地表径流',
      '分次灌溉更利于均匀入渗，减少浪费',
    ];
  }
  if (soilMoisturePercent.value < 60) {
    return [
      '关注未来两日蒸散量变化，必要时小水勤灌',
      '保持田间通风，避免积水抑制根系生长',
    ];
  }
  return [
    '土壤含水率较高，暂不推荐灌溉',
    '注意病害风险，保持叶面干燥并加强田间巡查',
  ];
});

async function copySuggestion() {
  const text = `地块：${selectedPlotName.value}\n需要灌溉：${penmanSuggestion.needIrrigation ? '是' : '否'}\n时间：${penmanSuggestion.recommendedTime || '-'}\n方式：${penmanSuggestion.method || '-'}\n推荐灌水量：${recommendedVolumeMm.value} mm（约 ${mmToM3PerMu(recommendedVolumeMm.value)} m³/亩）\n原因：${penmanSuggestion.reason || '-'}`;
  try {
    await navigator.clipboard?.writeText(text);
    createMessage.success('已复制建议到剪贴板');
  } catch (e) {
    createMessage.warning('复制失败，请手动选择文本进行复制');
  }
}

function refresh() {
  fetchPlotStatusAndSuggest();
  fetchInterventionComparison();
}

// 快速灌溉管理（状态与计算）
const quickArea = ref<number>(10);
const quickMethod = ref<string>('滴灌');
const quickUseAlgorithm = ref<boolean>(true);
const manualWaterPerMu = ref<number>(15); // m³/亩（人工设定）
const quickDate = ref<any>();
const quickRemark = ref<string>('');
const quickTimes = ref<number>(1);

const irrigationMethodOptions = [
  { label: '滴灌', value: '滴灌' },
  { label: '喷灌', value: '喷灌' },
  { label: '漫灌', value: '漫灌' },
  { label: '微灌', value: '微灌' },
];

const algoWaterPerMuM3 = computed<number>(() => mmToM3PerMu(recommendedVolumeMm.value));
const waterPerMuM3 = computed<number>(() => (quickUseAlgorithm.value ? algoWaterPerMuM3.value : manualWaterPerMu.value));
const totalWaterUsageM3 = computed<number>(() => Math.round(waterPerMuM3.value * quickArea.value * 10) / 10);
const durationHours = computed<number>(() => {
  const rate = flowRateByMethod(quickMethod.value);
  if (!rate) return 0;
  return Math.round((totalWaterUsageM3.value / rate) * 10) / 10;
});
const perTimeWaterM3 = computed<number>(() => {
  const times = Math.max(1, quickTimes.value || 1);
  return Math.round((totalWaterUsageM3.value / times) * 10) / 10;
});

function flowRateByMethod(method: string): number {
  switch (method) {
    case '滴灌':
      return 8; // m³/小时（示例值）
    case '喷灌':
      return 12;
    case '漫灌':
      return 20;
    case '微灌':
      return 5;
    default:
      return 10;
  }
}

function calcQuickIrrigation() {
  // 计算通过计算属性完成，此处给用户反馈
  createMessage.success('已计算用水量');
}

function generateRecordQuick() {
  const defaults: any = {
    plotName: selectedPlotName.value,
    baseId: selectedBaseId.value,
    plotId: selectedPlotId.value,
    irrigationMethod: quickMethod.value,
    irrigationArea: quickArea.value,
    waterUsage: totalWaterUsageM3.value,
    irrigationTimes: quickTimes.value,
    perTimeWater: perTimeWaterM3.value,
    duration: durationHours.value,
    irrigationDate: quickDate.value || penmanSuggestion.recommendedTime || undefined,
    remark: quickRemark.value,
  };
  openModal(true, {
    isUpdate: false,
    defaultValues: defaults,
  });
}

function executeAutomationQuick() {
  createMessage.info('已触发自动化（占位）');
}
</script>

<style lang="less" scoped>
.irrigation-page {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 64px);
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

.moisture-card {
  display: flex;
  align-items: center;
  gap: 16px;
}

.suggestion-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.rich-card {
  :deep(.ant-card-body) {
    padding-top: 16px;
  }
}

.mini-title {
  margin: 8px 0 4px;
  font-size: 13px;
  color: #555;
}

.statistic-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-top: 8px;
}
.metric-title {
  font-size: 12px;
  color: #888;
}
.metric-value {
  font-size: 16px;
  font-weight: 600;
}

.actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.tips {
  margin: 0;
  padding-left: 18px;
  color: #666;
}

.mt-4 {
  margin-top: 16px;
}

.top-row {
  align-items: stretch;
  :deep(.ant-col) {
    display: flex;
  }
  :deep(.ant-card) {
    height: 100%;
    display: flex;
    flex-direction: column;
    width: 100%;
    flex: 1 1 auto;
  }
}

.row-stretch {
  align-items: stretch;
  :deep(.ant-col) {
    display: flex;
  }
  :deep(.ant-card) {
    height: 100%;
    display: flex;
    flex-direction: column;
    width: 100%;
    flex: 1 1 auto;
  }
}

/* 快速灌溉管理样式 */
.quick-card {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.form-item {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 6px;
}
.form-inline {
  flex-direction: row;
  align-items: center;
}
.form-item .label {
  width: auto;
  font-size: 13px;
  color: #555;
  font-weight: 500;
}

.calc-result {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 8px 12px;
  font-size: 13px;
  color: #555;
  background: #fcfcfc;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
}

.quick-actions {
  display: flex;
  justify-content: center;
  margin-top: 8px;
}

.chart-ref { width: 100%; }
.chart-220 { height: 220px; }
.chart-320 { height: 320px; }

@media (max-width: 768px) {
  .irrigation-page {
    padding: 16px;
  }
  .statistic-grid {
    grid-template-columns: 1fr;
  }
  .chart-220 { height: 180px; }
  .chart-320 { height: 260px; }
}

/* 大屏优化 */
@media (min-width: 1600px) {
  .irrigation-page { padding: 32px; }
  .chart-220 { height: 260px; }
  .chart-320 { height: 360px; }
  .quick-card { gap: 16px; }
}
</style>