<template>
  <div class="fertilization-page">
    <!-- 选择与概览区域 -->
    <a-row :gutter="16" class="top-row">
      
      <a-col :xs="24" :md="12">
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
            <div class="nutrient-item">
              <span class="label">pH：</span>
              <div class="value-text">{{ phValueText }}</div>
              <Tag :color="phTagColor">{{ phStateLabel }}</Tag>
            </div>
            <div class="nutrient-item">
              <span class="label">有机质：</span>
              <div class="value-text">{{ omValueText }}</div>
              <Tag :color="omTagColor">{{ omStateLabel }}</Tag>
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
      <a-col :xs="24" :md="12">
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
          </div>
        </a-card>
      </a-col>
    </a-row>

    

    <!-- QUEFTS 推荐施肥比：保留并置于底部，增加数字比例 -->
    <a-card :bordered="false" class="chart-card">
      <template #title>
        <div class="table-title">
          <Icon icon="ant-design:area-chart" /> QUEFTS 推荐施肥比
        </div>
      </template>
      <div class="chart-box">
        <div class="chart-title">推荐用量柱状图</div>
        <div ref="recommendBarsRef" class="chart-ref"></div>
      </div>
      <div class="ratio-summary">
        <div class="ratio-line">百分比：N {{ ratio.percN }}% / P₂O₅ {{ ratio.percP }}% / K₂O {{ ratio.percK }}%</div>
        <div class="ratio-line">配比：{{ ratioBaseLabel }} {{ ratio.rN }} : {{ ratio.rP }} : {{ ratio.rK }}</div>
      </div>
    </a-card>


    <!-- 施肥管理列表区域（已删除） -->


    <!-- 表单弹窗 -->
    <FertilizationModal @register="registerModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { BasicTable, useTable, TableAction } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useMessage } from '/@/hooks/web/useMessage';
import { Icon } from '/@/components/Icon';
import { columns, searchFormSchema } from './fertilization.data';
import { getFertilizationList, deleteFertilization, exportFertilization, importFertilization, getPlotNutrientStatus, getFertilizationRecommendation, getSoilHistorySeries } from './fertilization.api';
import FertilizationModal from './FertilizationModal.vue';
import BaseSelect from '@/views/rapeseed/production-plan/plot-production-plan/components/BaseSelect.vue';
import PlotSelect from '@/views/rapeseed/production-plan/plot-production-plan/components/PlotSelect.vue';
import { useSelectStore } from '/@/store/selectStore';
import { getBaseList, getPlotsByBaseId } from '/@/views/rapeseed/production-plan/plot-production-plan/base.api';
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

// 全局状态管理
const selectStore = useSelectStore();

// 基地和地块数据列表
const baseList = ref<any[]>([]);
const plotList = ref<any[]>([]);

// 下拉框展开状态
const isDropdownOpen = ref(false);

// 切换下拉框展开/收起状态
function toggleDropdown() {
  isDropdownOpen.value = !isDropdownOpen.value;
}

// 选择基地或地块项
async function selectItem(item: any, type: 'base' | 'plot') {
  if (type === 'base') {
    // 选择基地
    selectedBaseId.value = item.id || item.baseId;
    selectStore.updateSelectedBase({ baseId: item.id || item.baseId, baseName: item.baseName || item.name || '未命名基地' });
    
    // 清空地块选择
    selectedPlotId.value = undefined;
    selectStore.updateSelectedPlot(null);
    
    // 加载对应的地块数据
    if (selectedBaseId.value) {
      await fetchPlotListByBaseId(selectedBaseId.value as string);
    }
    
    // 触发原有变更逻辑
    onBaseChange(selectedBaseId.value);
  } else {
    // 选择地块
    selectedPlotId.value = item.id || item.plotId;
    selectStore.updateSelectedPlot({ plotId: item.id || item.plotId, plotName: item.plotName || item.name || '' });
    
    // 触发原有变更逻辑
    onPlotChange(selectedPlotId.value);
  }
}

// 从数据库获取基地列表
async function fetchBaseListData() {
  try {
    const res = await getBaseList({});
    // 兼容不同返回格式
    const baseDataList = (res && Array.isArray(res.records)) 
      ? res.records 
      : (Array.isArray(res) ? res : (res?.result || []));
    
    baseList.value = baseDataList.map((item: any) => ({
      ...item,
      id: item.id || item.baseId,
      baseName: item.baseName || item.name || '未命名基地'
    }));
    
    // 如果有基地数据且当前未选择基地，默认选择第一个
    if (baseList.value.length > 0 && !selectedBaseId.value) {
      const firstBase = baseList.value[0];
      selectedBaseId.value = firstBase.id;
      selectStore.updateSelectedBase({ baseId: firstBase.id, baseName: firstBase.baseName });
      
      // 加载第一个基地的地块数据
      await fetchPlotListByBaseId(firstBase.id as string);
    }
  } catch (error) {
    console.error('获取基地列表失败:', error);
  }
}

// 根据基地ID从数据库获取地块列表
async function fetchPlotListByBaseId(baseId: string) {
  if (!baseId) return;
  
  try {
    const res = await getPlotsByBaseId(baseId);
    // 兼容不同返回格式
    const plotDataList = (res && Array.isArray(res.records)) 
      ? res.records 
      : (Array.isArray(res) ? res : (res?.result || []));
    
    plotList.value = plotDataList.map((item: any) => ({
      ...item,
      id: item.id || item.plotId,
      plotName: item.plotName || item.name || '未命名地块'
    }));
    
    // 如果有地块数据且当前未选择地块，默认选择第一个
    if (plotList.value.length > 0 && !selectedPlotId.value) {
      const firstPlot = plotList.value[0];
      selectedPlotId.value = firstPlot.id;
      selectStore.updateSelectedPlot({ plotId: firstPlot.id, plotName: firstPlot.plotName });
      onPlotChange(firstPlot.id);
    }
  } catch (error) {
    console.error('获取地块列表失败:', error);
  }
}
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

const trendSummary = ref<string>('—');
const leachingRiskLabel = ref<string>('—');
const leachingRiskColor = ref<string>('default');
const targetYieldText = ref<string>('—');

const phValue = ref<number | null>(null);
const omValue = ref<number | null>(null);
const phStateLabel = ref<string>('—');
const omStateLabel = ref<string>('—');
const phTagColor = ref<string>('default');
const omTagColor = ref<string>('default');
const phValueText = ref<string>('—');
const omValueText = ref<string>('—');

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
const recommendBarsRef = ref<HTMLDivElement | null>(null);

const { setOptions: setRecommendBarsOptions } = useECharts(recommendBarsRef as any);

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
    nPercent.value = status?.nPercent ?? 0;
    pPercent.value = status?.pPercent ?? 0;
    kPercent.value = status?.kPercent ?? 0;
    phValue.value = status?.ph != null ? Number(status.ph) : null;
    omValue.value = status?.om != null ? Number(status.om) : null;

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

    applyPHOMStates();

    targetYieldText.value = status?.targetYield ? `${status.targetYield} kg/亩` : '—';

    // 建议
    const rec = await getFertilizationRecommendation({ plotId: selectedPlotId.value });
    suggestion.needFertilization = rec?.needFertilization ?? false;
    suggestion.recommendedTime = rec?.recommendedTime ?? '';
    suggestion.method = rec?.method ?? '';
    suggestion.recommendN = rec?.recommendN ?? 0;
    suggestion.recommendP2O5 = rec?.recommendP2O5 ?? 0;
    suggestion.recommendK2O = rec?.recommendK2O ?? 0;
    suggestion.reason = rec?.reason ?? '';

    const series = await getSoilHistorySeries(selectedPlotId.value as any);
    const items = series?.items ?? [];
    leachingRiskLabel.value = '—';
    leachingRiskColor.value = 'default';
    renderRecommendBars();
  } catch (e) {
    const items: any[] = [];
    trendSummary.value = '—';
    leachingRiskLabel.value = '—';
    leachingRiskColor.value = 'default';
    phValue.value = null;
    omValue.value = null;
    applyPHOMStates();
    renderRecommendBars();
  } finally {
    loading.value = false;
  }
}




// 历史与记录板块已删除

function renderRecommendBars() {
  setRecommendBarsOptions({
    grid: { left: 48, right: 16, bottom: 32, top: 32 },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: ['N', 'P₂O₅', 'K₂O'] },
    yAxis: { type: 'value', name: 'kg/亩' },
    series: [
      { type: 'bar', data: [suggestion.recommendN, suggestion.recommendP2O5, suggestion.recommendK2O], color: '#f5222d' },
    ],
  });
}

// 数字比例（百分比与配比）
const ratio = computed(() => {
  const n = suggestion.recommendN || 0;
  const p = suggestion.recommendP2O5 || 0;
  const k = suggestion.recommendK2O || 0;
  const sum = n + p + k;
  const percN = sum ? Math.round((n / sum) * 100) : 0;
  const percP = sum ? Math.round((p / sum) * 100) : 0;
  const percK = sum ? Math.round((k / sum) * 100) : 0;
  const base = n > 0 ? n : p > 0 ? p : k > 0 ? k : 1;
  const rN = Number(((n || 0) / base).toFixed(2));
  const rP = Number(((p || 0) / base).toFixed(2));
  const rK = Number(((k || 0) / base).toFixed(2));
  return { percN, percP, percK, rN, rP, rK, baseId: n > 0 ? 'N' : p > 0 ? 'P₂O₅' : 'K₂O' };
});
const ratioBaseLabel = computed(() => `${ratio.value.baseId}:`);


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

function applyPHOMStates() {
  const ph = phValue.value;
  if (ph == null || Number.isNaN(Number(ph)) || Number(ph) <= 0) {
    phStateLabel.value = '—';
    phTagColor.value = 'default';
    phValueText.value = '—';
  } else {
    const v = Number(ph);
    phValueText.value = v.toFixed(2);
    if (v < 6.0) {
      phStateLabel.value = '偏酸';
      phTagColor.value = 'orange';
    } else if (v > 7.5) {
      phStateLabel.value = '偏碱';
      phTagColor.value = 'red';
    } else {
      phStateLabel.value = '适中';
      phTagColor.value = 'blue';
    }
  }
  const om = omValue.value;
  if (om == null || Number.isNaN(Number(om)) || Number(om) <= 0) {
    omStateLabel.value = '—';
    omTagColor.value = 'default';
    omValueText.value = '—';
  } else {
    const v = Number(om);
    omValueText.value = v.toFixed(2);
    if (v < 2) {
      omStateLabel.value = '低';
      omTagColor.value = 'orange';
    } else if (v > 4) {
      omStateLabel.value = '高';
      omTagColor.value = 'green';
    } else {
      omStateLabel.value = '中';
      omTagColor.value = 'blue';
    }
  }
}

// 已移除模拟展示与一键生成逻辑

function scrollToCharts() {
  const el = chartsAnchorRef.value?.$el || chartsAnchorRef.value;
  if (el && el.scrollIntoView) {
    el.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }
}

onMounted(() => {
  // 初始化加载基地数据
  fetchBaseListData();
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
.ratio-summary { margin-top: 8px; padding: 8px 12px; background: #fafafa; border-radius: 6px; }
.ratio-line { color: #595959; font-size: 13px; }
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
