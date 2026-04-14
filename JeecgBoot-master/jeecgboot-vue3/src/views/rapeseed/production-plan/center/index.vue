<template>
  <div class="production-center">
    <a-spin :spinning="loading">
      <!-- 农田分布 -->
      <a-row :gutter="16" class="section-row">
        <a-col :span="24">
          <a-card title="农田分布" :bordered="false" class="plot-card">
            <template #extra><span class="plot-summary">共 {{ plotList.length }} 块地块 | 已计划 {{ Array.from(plotPlans.values()).filter(p => p).length }} 块</span></template>
            <div class="plot-grid">
              <div v-for="plot in plotList" :key="plot.id" class="plot-item" :class="{ active: selectedPlotId === plot.id, 'has-plan': getPlotPlan(plot.id) }" @click="selectPlot(plot)">
                <div class="plot-icon"><EnvironmentOutlined /></div>
                <div class="plot-name">{{ plot.plotName }}</div>
                <div class="plot-area">{{ plot.area }}亩</div>
                <div class="plot-tag" :class="getPlotPlan(plot.id) ? 'planned' : 'empty'">{{ getPlotPlan(plot.id) ? '已计划' : '未计划' }}</div>
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>

      <!-- 无计划提示 -->
      <a-row :gutter="16" class="section-row" v-if="!currentPlan">
        <a-col :span="24">
          <a-card class="empty-card">
            <a-result status="warning" title="当前地块未生成生产计划">
              <template #subTitle>{{ selectedPlotInfo?.plotName || '请选择一个地块' }} 尚未生成生产计划</template>
              <template #extra>
                <a-button type="primary" size="large" @click="generateDraftPlan" :loading="generating">
                  生成计划(草稿)
                </a-button>
              </template>
            </a-result>
          </a-card>
        </a-col>
      </a-row>

      <!-- 有计划的显示完整内容 -->
      <template v-else>
        <!-- 生产计划概览 -->
        <a-row :gutter="16" class="section-row">
          <a-col :span="24">
            <a-card title="生产计划概览" :bordered="false" size="small" class="overview-card">
              <a-descriptions :column="5" bordered size="small" class="overview-desc">
                <a-descriptions-item label="计划名称">{{ currentPlan.planName || '-' }}</a-descriptions-item>
                <a-descriptions-item label="计划年份">{{ currentPlan.planYear || '-' }}年</a-descriptions-item>
                <a-descriptions-item label="种植面积">{{ currentPlan.plantingArea || selectedPlotInfo?.area || 0 }}亩</a-descriptions-item>
                <a-descriptions-item label="目标产量">{{ currentPlan.targetYield || 0 }} kg/亩</a-descriptions-item>
                <a-descriptions-item label="状态">
                  <a-tag :color="getStatusColor(currentPlan.status)">{{ currentPlan.status || '草稿' }}</a-tag>
                </a-descriptions-item>
              </a-descriptions>
            </a-card>
          </a-col>
        </a-row>

        <!-- 核心指标卡片 -->
        <a-row :gutter="16" class="section-row">
          <a-col :xs="24" :sm="12" :md="6">
            <a-card class="stat-card primary-card" size="small">
              <div class="stat-icon-wrap"><EnvironmentOutlined /></div>
              <div class="stat-content">
                <div class="stat-label">当前地块</div>
                <div class="stat-value">{{ selectedPlotInfo?.area || 0 }} <span class="stat-unit">亩</span></div>
                <div class="stat-sub">{{ selectedPlotInfo?.plotName }}</div>
              </div>
            </a-card>
          </a-col>
          <a-col :xs="24" :sm="12" :md="6">
            <a-card class="stat-card variety-card" size="small">
              <div class="stat-icon-wrap"><ExperimentOutlined /></div>
              <div class="stat-content">
                <div class="stat-label">品种信息</div>
                <div class="stat-value variety-name">{{ selectedVariety?.varietyName || '-' }}</div>
                <div class="stat-sub">{{ formData.plannedSowingDate ? formatDate(formData.plannedSowingDate) : '-' }} 播种</div>
              </div>
            </a-card>
          </a-col>
          <a-col :xs="24" :sm="12" :md="6">
            <a-card class="stat-card schedule-card" size="small">
              <div class="stat-icon-wrap"><CalendarOutlined /></div>
              <div class="stat-content">
                <div class="stat-label">播种收获</div>
                <div class="stat-value schedule-value">
                  <span class="schedule-dot sowing"></span>{{ formData.plannedSowingDate ? formatDate(formData.plannedSowingDate) : '未设置' }}
                </div>
                <div class="stat-sub"><span class="schedule-dot harvest"></span>{{ formData.plannedHarvestDate ? formatDate(formData.plannedHarvestDate) : '未设置' }}</div>
              </div>
            </a-card>
          </a-col>
          <a-col :xs="24" :sm="12" :md="6">
            <a-card class="stat-card soil-card" size="small">
              <div class="stat-icon-wrap"><AppstoreOutlined /></div>
              <div class="stat-content">
                <div class="stat-label">土壤养分</div>
                <div class="soil-grid" v-if="soilData && soilData.hasData">
                  <div class="soil-item"><span class="soil-label">N</span><b>{{ soilData.nPercent || '-' }}</b></div>
                  <div class="soil-item"><span class="soil-label">P</span><b>{{ soilData.pPercent || '-' }}</b></div>
                  <div class="soil-item"><span class="soil-label">K</span><b>{{ soilData.kPercent || '-' }}</b></div>
                </div>
                <div class="no-data" v-else-if="soilData && soilData.deviceNotConfigured">设备未配置</div>
                <div class="no-data" v-else>暂无数据</div>
              </div>
            </a-card>
          </a-col>
        </a-row>

        <!-- 投入品卡片 -->
        <a-row :gutter="16" class="section-row">
          <a-col :xs="24" :sm="8">
            <a-card class="input-card seed-card" size="small">
              <template #title><span class="card-title"><DashboardOutlined /> 种子投入</span></template>
              <div class="input-main">
                <div class="input-num">{{ formData.seedTotal || 0 }} <span>kg</span></div>
                <div class="input-desc">计划总量</div>
              </div>
              <div class="input-details">
                <div class="detail-row"><span>推荐品种</span><b>{{ selectedVariety?.varietyName || '-' }}</b></div>
                <div class="detail-row"><span>亩用量</span><b>{{ seedPerMu }}kg/亩</b></div>
              </div>
            </a-card>
          </a-col>
          <a-col :xs="24" :sm="8">
            <a-card class="input-card fertilizer-card" size="small">
              <template #title><span class="card-title"><ExperimentOutlined /> 肥料投入</span></template>
              <div class="input-main">
                <div class="input-num">{{ (parseFloat(formData.fertilizerTotalN||0) + parseFloat(formData.fertilizerTotalP||0) + parseFloat(formData.fertilizerTotalK||0)).toFixed(1) }} <span>kg</span></div>
                <div class="input-desc">N+P₂O₅+K₂O 总计</div>
              </div>
              <div class="input-details">
                <div class="detail-row"><span>已选肥料</span><b class="fertilizer-name">{{ selectedFertilizerNames }}</b></div>
                <div class="detail-row npk-row"><span>N</span><b>{{ formData.fertilizerTotalN || 0 }}kg</b><span>P₂O₅</span><b>{{ formData.fertilizerTotalP || 0 }}kg</b><span>K₂O</span><b>{{ formData.fertilizerTotalK || 0 }}kg</b></div>
              </div>
            </a-card>
          </a-col>
          <a-col :xs="24" :sm="8">
            <a-card class="input-card pesticide-card" size="small">
              <template #title><span class="card-title"><SafetyOutlined /> 农药投入</span></template>
              <div class="input-main">
                <div class="input-num">{{ formData.pesticideTotal || 0 }} <span>ml</span></div>
                <div class="input-desc">计划总量</div>
              </div>
              <div class="input-details">
                <div class="detail-row"><span>已选农药</span><b class="fertilizer-name">{{ selectedPesticideNames }}</b></div>
                <div class="detail-row"><span>类型</span><b>{{ selectedPesticideTypes }}</b></div>
              </div>
            </a-card>
          </a-col>
        </a-row>

        <!-- 操作区域 -->
        <a-row :gutter="16" class="section-row action-row">
          <a-col :span="24">
            <div class="action-bar">
              <div class="action-left">
                <a-button type="primary" size="large" class="action-btn primary-btn" @click="editModalVisible = true">
                  <EditOutlined /> {{ formData.status === '草稿' ? '编辑修改' : '查看/编辑' }}
                </a-button>
                <a-button type="primary" size="large" class="action-btn success-btn" @click="confirmPlan" v-if="formData.status === '草稿'" :loading="confirming">
                  <CheckOutlined /> 确认发布
                </a-button>
              </div>
              <div class="action-right">
                <a-tag color="green" v-if="formData.status === '已发布'" size="large" class="status-tag">
                  <CheckCircleOutlined /> 已发布
                </a-tag>
                <a-tag color="blue" v-if="formData.status === '执行中'" size="large" class="status-tag">
                  <SyncOutlined /> 执行中
                </a-tag>
                <span class="target-yield-badge">目标产量 <strong>{{ formData.targetYield || 0 }}</strong> kg/亩</span>
              </div>
            </div>
          </a-col>
        </a-row>
      </template>
    </a-spin>

    <!-- 编辑弹窗 -->
    <a-modal v-model:open="editModalVisible" title="编辑生产计划" width="900px" centered class="edit-plan-modal" :footer="null" @cancel="editModalVisible = false">
      <div class="modal-content">
        <a-form :model="formData" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }" class="edit-form">
          <a-divider orientation="left"><span class="divider-title"><EnvironmentOutlined /> 基本信息</span></a-divider>
          <a-row :gutter="24">
            <a-col :span="12">
              <a-form-item label="目标产量">
                <a-input-number v-model:value="formData.targetYield" style="width: 100%" :min="0" placeholder="kg/亩" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="种植面积">
                <a-input-number v-model:value="formData.plantingArea" style="width: 100%" :min="0" placeholder="亩" />
              </a-form-item>
            </a-col>
          </a-row>
          
          <a-divider orientation="left"><span class="divider-title"><ExperimentOutlined /> 品种与时间</span></a-divider>
          <a-row :gutter="24">
            <a-col :span="12">
              <a-form-item label="油菜品种">
                <a-select v-model:value="formData.varietyId" style="width: 100%" @change="onVarietyChange" placeholder="请选择品种">
                  <a-select-option v-for="v in varietyList" :key="v.id" :value="v.id">{{ v.varietyName }}</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="计划播种日期">
                <a-date-picker v-model:value="formData.plannedSowingDate" style="width: 100%" placeholder="选择日期" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="计划收获日期">
                <a-date-picker v-model:value="formData.plannedHarvestDate" style="width: 100%" placeholder="选择日期" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="种子总量">
                <a-input-number v-model:value="formData.seedTotal" style="width: 100%" :min="0" placeholder="kg" />
              </a-form-item>
            </a-col>
          </a-row>

          <a-divider orientation="left"><span class="divider-title"><ExperimentOutlined /> 肥料投入 (N-P₂O₅-K₂O)</span></a-divider>
          <a-row :gutter="24">
            <a-col :span="24">
              <a-form-item label="选择肥料">
                <a-select v-model:value="selectedFertilizerIds" mode="multiple" style="width: 100%" placeholder="请选择肥料（可多选）" @change="onFertilizerChange">
                  <a-select-option v-for="f in fertilizerList" :key="f.id" :value="f.id">
                    {{ f.fertilizerName }} ({{ f.fertilizerType }} - NPK: {{ f.npkRatio || '未设置' }})
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="氮 N">
                <a-input-number v-model:value="formData.fertilizerTotalN" style="width: 100%" :min="0" :step="0.1" placeholder="kg" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="磷 P₂O₅">
                <a-input-number v-model:value="formData.fertilizerTotalP" style="width: 100%" :min="0" :step="0.1" placeholder="kg" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="钾 K₂O">
                <a-input-number v-model:value="formData.fertilizerTotalK" style="width: 100%" :min="0" :step="0.1" placeholder="kg" />
              </a-form-item>
            </a-col>
          </a-row>

          <a-divider orientation="left"><span class="divider-title"><SafetyOutlined /> 农药投入</span></a-divider>
          <a-row :gutter="24">
            <a-col :span="24">
              <a-form-item label="选择农药">
                <a-select v-model:value="selectedPesticideIds" mode="multiple" style="width: 100%" placeholder="请选择农药（可多选）" @change="onPesticideChange">
                  <a-select-option v-for="p in pesticideList" :key="p.id" :value="p.id">
                    {{ p.pesticideName }} ({{ p.pesticideType }} - {{ p.activeIngredient }})
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="农药总量">
                <a-input-number v-model:value="formData.pesticideTotal" style="width: 100%" :min="0" placeholder="ml" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="农药备注">
                <a-input v-model:value="formData.pesticideNote" placeholder="用药说明" />
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
        
        <div class="modal-footer">
          <a-button @click="editModalVisible = false" size="large">取消</a-button>
          <a-button type="primary" @click="saveDraft" :loading="saving" size="large" class="save-btn">保存修改</a-button>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import { storeToRefs } from 'pinia';
import { useSelectStore } from '@/store/selectStore';
import { getPlotsByBaseId, getPlotPlanByPlotId, getAllVariety } from './center.api';
import { defHttp } from '/@/utils/http/axios';
import { message } from 'ant-design-vue';
import { EditOutlined, CheckOutlined, EnvironmentOutlined, ExperimentOutlined, CalendarOutlined, AppstoreOutlined, DashboardOutlined, SafetyOutlined, CheckCircleOutlined, SyncOutlined } from '@ant-design/icons-vue';
import dayjs from 'dayjs';

const selectStore = useSelectStore();
const { selectedBase } = storeToRefs(selectStore);

const loading = ref(false);
const generating = ref(false);
const saving = ref(false);
const confirming = ref(false);
const editModalVisible = ref(false);
const plotList = ref<any[]>([]);
const selectedPlotId = ref('');
const plotPlans = ref<Map<string, any>>(new Map());
const soilData = ref<any>(null);
const varietyList = ref<any[]>([]);
const fertilizerList = ref<any[]>([]);
const pesticideList = ref<any[]>([]);

const selectedVariety = ref<any>(null);
const selectedFertilizer = ref<any>(null);
const selectedPesticide = ref<any>(null);
const selectedFertilizerIds = ref<string[]>([]);
const selectedPesticideIds = ref<string[]>([]);

const formData = ref<any>({
  targetYield: 0, plantingArea: 0, varietyId: '', plannedSowingDate: null, plannedHarvestDate: null,
  seedTotal: 0, fertilizerTotalN: 0, fertilizerTotalP: 0, fertilizerTotalK: 0,
  pesticideTotal: 0, pesticideNote: '', status: '草稿'
});

const currentPlan = computed(() => selectedPlotId.value ? plotPlans.value.get(selectedPlotId.value) : null);
const selectedPlotInfo = computed(() => selectedPlotId.value ? plotList.value.find(p => p.id === selectedPlotId.value) : null);
const totalArea = computed(() => plotList.value.reduce((s, p) => s + (parseFloat(p.area) || 0), 0).toFixed(2));
const plannedArea = computed(() => { let t = 0; plotPlans.value.forEach(p => { if (p?.plantingArea) t += parseFloat(p.plantingArea) || 0; }); return t.toFixed(2); });
const totalTargetYield = computed(() => { let t = 0; plotPlans.value.forEach(p => { if (p?.targetYield && p?.plantingArea) t += (parseFloat(p.targetYield) || 0) * (parseFloat(p.plantingArea) || 0); }); return t.toFixed(0); });
const planCompletionRate = computed(() => { if (plotList.value.length === 0) return '0'; const planned = Array.from(plotPlans.value.values()).filter(p => p).length; return ((planned / plotList.value.length) * 100).toFixed(0); });
const seedPerMu = computed(() => { if (!formData.value.plantingArea || !formData.value.seedTotal) return '0'; return (parseFloat(formData.value.seedTotal) / parseFloat(formData.value.plantingArea)).toFixed(2); });

// 推荐肥料名称 - 根据NPK含量智能推荐
const recommendedFertilizerName = computed(() => {
  if (!fertilizerList.value || fertilizerList.value.length === 0) return '未配置肥料';
  
  const n = parseFloat(formData.value.fertilizerTotalN || 0);
  const p = parseFloat(formData.value.fertilizerTotalP || 0);
  const k = parseFloat(formData.value.fertilizerTotalK || 0);
  
  // 查找最匹配的肥料
  let bestMatch = null;
  let bestScore = 0;
  
  for (const f of fertilizerList.value) {
    const npk = f.npkRatio || '';
    const match = npk.match(/(\d+)-(\d+)-(\d+)/);
    if (!match) continue;
    
    const fn = parseFloat(match[1]) || 0;
    const fp = parseFloat(match[2]) || 0;
    const fk = parseFloat(match[3]) || 0;
    
    // 计算匹配度
    let score = 0;
    if (n > 0) score += 1 - Math.abs(fn - n) / n;
    if (p > 0) score += 1 - Math.abs(fp - p) / p;
    if (k > 0) score += 1 - Math.abs(fk - k) / k;
    
    if (score > bestScore) {
      bestScore = score;
      bestMatch = f;
    }
  }
  
  return bestMatch ? (bestMatch as any).fertilizerName : ((fertilizerList.value[0] as any)?.fertilizerName || '复合肥');
});

// 显示选中的肥料名称列表
const selectedFertilizerNames = computed(() => {
  if (!selectedFertilizer.value || selectedFertilizer.value.length === 0) return '-';
  return selectedFertilizer.value.map((f: any) => f.fertilizerName).join('、');
});

// 显示选中的农药名称列表
const selectedPesticideNames = computed(() => {
  if (!selectedPesticide.value || selectedPesticide.value.length === 0) return '-';
  return selectedPesticide.value.map((p: any) => p.pesticideName).join('、');
});

// 选中的农药类型
const selectedPesticideTypes = computed(() => {
  if (!selectedPesticide.value || selectedPesticide.value.length === 0) return '-';
  return selectedPesticide.value.map((p: any) => p.pesticideType).join('、');
});

function getPlotPlan(id: string) { return plotPlans.value.get(id); }

function getStatusColor(status: string) {
  switch (status) {
    case '草稿': return 'default';
    case '已发布': return 'green';
    case '执行中': return 'blue';
    case '已完成': return 'purple';
    case '已取消': return 'red';
    default: return 'default';
  }
}

function formatDate(date: any) {
  if (!date) return '-';
  if (typeof date === 'string') return date;
  if (date.format) return date.format('MM-DD');
  return String(date);
}

function initFormData(plan: any) {
  formData.value = {
    targetYield: plan?.targetYield || 0,
    plantingArea: plan?.plantingArea || selectedPlotInfo.value?.area || 0,
    varietyId: plan?.varietyId || '',
    plannedSowingDate: plan?.plannedSowingDate ? dayjs(plan.plannedSowingDate) : null,
    plannedHarvestDate: plan?.plannedHarvestDate ? dayjs(plan.plannedHarvestDate) : null,
    seedTotal: plan?.seedTotal || 0,
    fertilizerTotalN: plan?.fertilizerTotalN || 0,
    fertilizerTotalP: plan?.fertilizerTotalP || 0,
    fertilizerTotalK: plan?.fertilizerTotalK || 0,
    pesticideTotal: plan?.pesticideTotal || 0,
    pesticideNote: plan?.pesticideNote || '',
    status: plan?.status || '草稿',
    id: plan?.id || ''
  };
  // 设置选中的品种
  if (plan?.varietyId) {
    selectedVariety.value = varietyList.value.find(v => v.id === plan.varietyId) || null;
  }
  // 解析肥料组合JSON并设置选中
  if (plan?.fertilizerCombination) {
    try {
      const fertCombo = typeof plan.fertilizerCombination === 'string' ? JSON.parse(plan.fertilizerCombination) : plan.fertilizerCombination;
      if (Array.isArray(fertCombo)) {
        selectedFertilizerIds.value = fertCombo.map((f: any) => f.id);
        selectedFertilizer.value = fertCombo.map((f: any) => fertilizerList.value.find(item => item.id === f.id) || f);
      }
    } catch (e) { console.error('解析肥料组合失败', e); }
  } else {
    selectedFertilizerIds.value = [];
    selectedFertilizer.value = [];
  }
  // 解析农药组合JSON并设置选中
  if (plan?.pesticideCombination) {
    try {
      const pestCombo = typeof plan.pesticideCombination === 'string' ? JSON.parse(plan.pesticideCombination) : plan.pesticideCombination;
      if (Array.isArray(pestCombo)) {
        selectedPesticideIds.value = pestCombo.map((p: any) => p.id);
        selectedPesticide.value = pestCombo.map((p: any) => pesticideList.value.find(item => item.id === p.id) || p);
      }
    } catch (e) { console.error('解析农药组合失败', e); }
  } else {
    selectedPesticideIds.value = [];
    selectedPesticide.value = [];
  }
}

function onVarietyChange(varietyId: string) {
  selectedVariety.value = varietyList.value.find(v => v.id === varietyId) || null;
}

function onFertilizerChange(values: string[]) {
  selectedFertilizerIds.value = values;
  selectedFertilizer.value = values.map(id => fertilizerList.value.find(f => f.id === id)).filter(Boolean);
}

function onPesticideChange(values: string[]) {
  selectedPesticideIds.value = values;
  selectedPesticide.value = values.map(id => pesticideList.value.find(p => p.id === id)).filter(Boolean);
}

async function loadAllData() {
  // 加载品种
  try {
    const res = await getAllVariety();
    varietyList.value = res || [];
    if (varietyList.value.length > 0 && !selectedVariety.value) {
      selectedVariety.value = varietyList.value[0];
    }
  } catch (e) { console.error('品种失败', e); }

  // 加载肥料
  try {
    const res = await defHttp.get({ url: '/youcai/youcaiFertilizerInfo/queryAll' });
    fertilizerList.value = res || [];
    if (fertilizerList.value.length > 0 && !selectedFertilizer.value) {
      selectedFertilizer.value = fertilizerList.value[0];
    }
  } catch (e) { console.error('肥料失败', e); }

  // 加载农药
  try {
    const res = await defHttp.get({ url: '/youcai/youcaiPesticideInfo/list', params: { pageSize: 100 } });
    pesticideList.value = res?.records || [];
    if (pesticideList.value.length > 0 && !selectedPesticide.value) {
      selectedPesticide.value = pesticideList.value[0];
    }
  } catch (e) { console.error('农药失败', e); }
}

async function loadPlots(baseId: string) {
  loading.value = true;
  selectedPlotId.value = '';
  plotPlans.value.clear();
  try {
    const res = await getPlotsByBaseId(baseId);
    plotList.value = Array.isArray(res) ? res : res?.records || [];
    for (const plot of plotList.value) {
      try { const plan = await getPlotPlanByPlotId(plot.id); if (plan) plotPlans.value.set(plot.id, plan); } catch (e) { console.error('计划失败', e); }
    }
    if (plotList.value.length > 0) selectPlot(plotList.value[0]);
  } catch (e) { console.error('地块失败', e); }
  finally { loading.value = false; }
}

async function loadSoilData(baseId: string) {
  try {
    const res = await defHttp.get({ url: `/youcai/fertilization/plotStatusByBase/${baseId}` });
    soilData.value = res;
  } catch (e) { 
    console.error('土壤数据失败', e);
    soilData.value = null;
  }
}

function selectPlot(plot: any) {
  selectedPlotId.value = plot.id;
  selectStore.updateSelectedPlot({ plotId: plot.id, plotName: plot.plotName });
  const plan = plotPlans.value.get(plot.id);
  if (plan) initFormData(plan);
}

async function generateDraftPlan() {
  if (!selectedPlotId.value) { message.warning('请先选择地块'); return; }
  generating.value = true;
  try {
    const res = await defHttp.get({ url: `/youcai/youcaiProductionPlans/generate?plotId=${selectedPlotId.value}` });
    if (res) {
      plotPlans.value.set(selectedPlotId.value, res);
      initFormData(res);
      message.success('草稿生成成功！请编辑后保存');
    }
  } catch (e) { message.error('生成失败'); }
  finally { generating.value = false; }
}

async function saveDraft() {
  saving.value = true;
  try {
    const currentYear = new Date().getFullYear().toString();
    const baseName = selectedBase.value.baseName || '';
    const plotName = selectedPlotInfo.value?.plotName || '';
    const planName = `${baseName}-${plotName}-${currentYear}生产计划`;

    const fertilizerCombinationData = selectedFertilizer.value ? selectedFertilizer.value.map((f: any) => ({
      id: f.id,
      fertilizerName: f.fertilizerName,
      fertilizerType: f.fertilizerType,
      npkRatio: f.npkRatio
    })) : [];

    const pesticideCombinationData = selectedPesticide.value ? selectedPesticide.value.map((p: any) => ({
      id: p.id,
      pesticideName: p.pesticideName,
      pesticideType: p.pesticideType,
      activeIngredient: p.activeIngredient
    })) : [];

    const data = {
      ...formData.value,
      baseId: selectedBase.value.baseId,
      plotId: selectedPlotId.value,
      planYear: currentYear,
      planName: planName,
      fertilizerCombination: JSON.stringify(fertilizerCombinationData),
      pesticideCombination: JSON.stringify(pesticideCombinationData)
    };
    if (data.plannedSowingDate) data.plannedSowingDate = data.plannedSowingDate.format('YYYY-MM-DD');
    if (data.plannedHarvestDate) data.plannedHarvestDate = data.plannedHarvestDate.format('YYYY-MM-DD');

    if (formData.value.id) {
      await defHttp.put({ url: '/youcai/youcaiProductionPlans/edit', data });
    } else {
      await defHttp.post({ url: '/youcai/youcaiProductionPlans/add', data });
    }
    message.success('保存成功！');
    editModalVisible.value = false;
    loadPlots(selectedBase.value.baseId);
  } catch (e: any) { message.error('保存失败: ' + (e?.message || e)); }
  finally { saving.value = false; }
}

async function confirmPlan() {
  if (!formData.value.id) { message.warning('请先保存草稿'); return; }
  confirming.value = true;
  try {
    await defHttp.put({ url: '/youcai/youcaiProductionPlans/edit', data: { 
      id: formData.value.id, 
      status: '已发布', 
      baseId: selectedBase.value.baseId, 
      plotId: selectedPlotId.value,
      planYear: new Date().getFullYear().toString(),
      varietyId: formData.value.varietyId
    } });
    message.success('发布成功！');
    // 更新本地状态
    const currentPlanData = plotPlans.value.get(selectedPlotId.value);
    if (currentPlanData) {
      currentPlanData.status = '已发布';
      plotPlans.value.set(selectedPlotId.value, { ...currentPlanData });
    }
    initFormData({ ...formData.value, status: '已发布' });
  } catch (e) { message.error('发布失败'); }
  finally { confirming.value = false; }
}

onMounted(() => { loadAllData(); });
watch(() => selectedBase.value.baseId, (id) => { if (id) { loadPlots(id); loadSoilData(id); } }, { immediate: true });
watch(currentPlan, (plan) => { if (plan) initFormData(plan); });
watch(() => selectStore.selectedPlot.plotId, () => { if (selectStore.selectedPlot.plotId) { selectedPlotId.value = selectStore.selectedPlot.plotId; } });
</script>

<style lang="less" scoped>
.production-center {
  padding: 16px;
  background: #f5f5f5;
  min-height: calc(100vh - 100px);
}

.section-row {
  margin-bottom: 16px;
}

.plot-summary {
  color: #1890ff;
  font-size: 13px;
  font-weight: 500;
}

.plot-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 12px;
  padding: 8px 0;
}

.plot-item {
  padding: 12px 8px;
  border: 2px solid #e8e8e8;
  border-radius: 10px;
  cursor: pointer;
  text-align: center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: #fff;
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 3px;
    background: transparent;
    transition: all 0.3s;
  }
  
  &:hover {
    border-color: #1890ff;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(24, 144, 255, 0.15);
    
    &::before {
      background: linear-gradient(90deg, #1890ff, #40a9ff);
    }
  }
  
  &.active {
    border-color: #1890ff;
    background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
    
    &::before {
      background: linear-gradient(90deg, #1890ff, #40a9ff);
    }
  }
  
  &.has-plan {
    border-color: #52c41a;
    
    &::before {
      background: linear-gradient(90deg, #52c41a, #73d13d);
    }
    
    &:hover {
      border-color: #389e0d;
      box-shadow: 0 4px 12px rgba(82, 196, 26, 0.15);
    }
    
    &.active {
      border-color: #1890ff;
      background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
      
      &::before {
        background: linear-gradient(90deg, #1890ff, #40a9ff);
      }
    }
  }
}

.plot-icon {
  font-size: 20px;
  color: #1890ff;
  margin-bottom: 4px;
}

.plot-name {
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 2px;
  color: #262626;
}

.plot-area {
  font-size: 12px;
  color: #8c8c8c;
}

.plot-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
  margin-top: 4px;
  display: inline-block;
  font-weight: 500;
}

.plot-tag.planned {
  background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%);
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.plot-tag.empty {
  background: linear-gradient(135deg, #fff7e6 0%, #ffd591 100%);
  color: #fa8c16;
  border: 1px solid #ffa940;
}

.empty-card {
  text-align: center;
  padding: 40px;
  border-radius: 12px;
}

.overview-card {
  border-radius: 12px;
  overflow: hidden;
  
  :deep(.ant-card-head) {
    background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
    
    .ant-card-head-title {
      color: #fff;
      font-weight: 600;
    }
  }
}

.overview-desc {
  :deep(.ant-descriptions-item-label) {
    background: #fafafa;
    font-weight: 500;
    color: #595959;
  }
  
  :deep(.ant-descriptions-item-content) {
    color: #262626;
  }
}

.stat-card {
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s;
  height: 100%;
  min-height: 120px;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
  }
  
  :deep(.ant-card-body) {
    padding: 16px;
    display: flex;
    flex-direction: column;
    height: 100%;
  }
  
  .stat-icon-wrap {
    width: 40px;
    height: 40px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    margin-bottom: 12px;
  }
  
  .stat-content {
    flex: 1;
  }
  
  .stat-label {
    font-size: 12px;
    color: #8c8c8c;
    margin-bottom: 4px;
  }
  
  .stat-value {
    font-size: 22px;
    font-weight: 700;
    color: #262626;
    line-height: 1.3;
    
    .stat-unit {
      font-size: 13px;
      font-weight: 400;
      color: #8c8c8c;
    }
  }
  
  .stat-sub {
    font-size: 12px;
    color: #8c8c8c;
    margin-top: 4px;
  }
}

.primary-card {
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
  
  .stat-icon-wrap {
    background: rgba(255, 255, 255, 0.3);
    color: #fff;
  }
  
  .stat-label,
  .stat-value,
  .stat-sub {
    color: #fff;
  }
  
  .stat-value .stat-unit {
    color: rgba(255, 255, 255, 0.8);
  }
}

.variety-card {
  border-top: 3px solid #722ed1;
  
  .stat-icon-wrap {
    background: linear-gradient(135deg, #722ed1 0%, #b37feb 100%);
    color: #fff;
  }
}

.schedule-card {
  border-top: 3px solid #fa8c16;
  
  .stat-icon-wrap {
    background: linear-gradient(135deg, #fa8c16 0%, #ffd591 100%);
    color: #fff;
  }
  
  .schedule-value {
    display: flex;
    align-items: center;
    gap: 6px;
  }
  
  .schedule-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    
    &.sowing {
      background: #52c41a;
    }
    
    &.harvest {
      background: #fa8c16;
    }
  }
}

.soil-card {
  border-top: 3px solid #13c2c2;
  
  .stat-icon-wrap {
    background: linear-gradient(135deg, #13c2c2 0%, #36cfc9 100%);
    color: #fff;
  }
}

.soil-grid {
  display: flex;
  gap: 12px;
  margin-top: 4px;
}

.soil-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  background: #f5f5f5;
  padding: 8px 4px;
  border-radius: 8px;
  
  .soil-label {
    font-size: 11px;
    color: #8c8c8c;
    font-weight: 500;
  }
  
  b {
    font-size: 15px;
    color: #262626;
    margin-top: 2px;
  }
}

.no-data {
  color: #8c8c8c;
  text-align: center;
  padding: 12px;
  font-size: 12px;
}

.input-card {
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s;
  height: 100%;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
  }
  
  :deep(.ant-card-head) {
    min-height: 40px;
    
    .ant-card-head-title {
      padding: 8px 0;
      font-size: 14px;
    }
  }
  
  :deep(.ant-card-body) {
    padding: 12px 16px 16px;
  }
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.seed-card {
  border-top: 3px solid #52c41a;
  
  .card-title {
    color: #52c41a;
  }
}

.fertilizer-card {
  border-top: 3px solid #1890ff;
  
  .card-title {
    color: #1890ff;
  }
}

.pesticide-card {
  border-top: 3px solid #fa8c16;
  
  .card-title {
    color: #fa8c16;
  }
}

.input-main {
  text-align: center;
  padding: 12px 0;
  border-bottom: 1px dashed #d9d9d9;
  margin-bottom: 10px;
}

.input-num {
  font-size: 24px;
  font-weight: 700;
  color: #262626;
  
  span {
    font-size: 13px;
    font-weight: 400;
    color: #8c8c8c;
  }
}

.input-desc {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 4px;
}

.input-details {
  font-size: 12px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 6px 0;
  border-bottom: 1px solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }

  span {
    color: #8c8c8c;
    flex-shrink: 0;
  }

  b {
    color: #262626;
    font-weight: 600;
    max-width: 180px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    text-align: right;
  }
}

.npk-row {
  flex-wrap: wrap;
  gap: 4px;
  
  span {
    background: #f5f5f5;
    padding: 2px 6px;
    border-radius: 4px;
    font-size: 11px;
  }
  
  b {
    font-size: 12px;
    max-width: none;
  }
}

.fertilizer-name {
  color: #1890ff !important;
  word-break: break-word;
  white-space: normal;
  display: block;
  text-align: right;
  max-width: 180px;
}

.action-row {
  margin-bottom: 0;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.action-left {
  display: flex;
  gap: 12px;
}

.action-btn {
  border-radius: 8px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
}

.primary-btn {
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
  border: none;
  
  &:hover {
    background: linear-gradient(135deg, #40a9ff 0%, #69c0ff 100%);
  }
}

.success-btn {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  border: none;
  
  &:hover {
    background: linear-gradient(135deg, #73d13d 0%, #95de64 100%);
  }
}

.action-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.status-tag {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  border-radius: 6px;
  font-weight: 500;
}

.target-yield-badge {
  background: linear-gradient(135deg, #f0f5ff 0%, #d9e8ff 100%);
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 13px;
  color: #595959;
  
  strong {
    color: #1890ff;
    font-size: 15px;
    margin: 0 4px;
  }
}

.edit-plan-modal {
  :deep(.ant-modal-content) {
    border-radius: 16px;
    overflow: hidden;
  }
  
  :deep(.ant-modal-header) {
    background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
    padding: 16px 24px;
    
    .ant-modal-title {
      color: #fff;
      font-size: 18px;
      font-weight: 600;
    }
  }
  
  :deep(.ant-modal-close) {
    color: #fff;
    
    &:hover {
      color: #f0f0f0;
    }
  }
}

.modal-content {
  padding: 8px 4px;
}

.edit-form {
  :deep(.ant-form-item) {
    margin-bottom: 16px;
  }
  
  :deep(.ant-form-item-label) {
    padding-bottom: 4px;
    
    > .ant-form-item-required::before {
      color: #ff4d4f;
    }
  }
}

.divider-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #1890ff;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 16px;
  margin-top: 8px;
  border-top: 1px solid #f0f0f0;
  
  .save-btn {
    background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
    border: none;
    border-radius: 8px;
    min-width: 120px;
    
    &:hover {
      background: linear-gradient(135deg, #40a9ff 0%, #69c0ff 100%);
    }
  }
}
</style>