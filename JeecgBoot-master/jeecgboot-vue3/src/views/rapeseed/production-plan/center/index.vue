<template>
  <div class="production-center">
    <a-spin :spinning="loading">
      <!-- 农田分布 -->
      <a-row :gutter="16">
        <a-col :span="24">
          <a-card title="农田分布" :bordered="false">
            <div class="plot-grid">
              <div v-for="plot in plotList" :key="plot.id" class="plot-item" :class="{ active: selectedPlotId === plot.id, 'has-plan': getPlotPlan(plot.id) }" @click="selectPlot(plot)">
                <div class="plot-name">{{ plot.plotName }}</div>
                <div class="plot-area">{{ plot.area }}亩</div>
                <div class="plot-tag" :class="getPlotPlan(plot.id) ? 'planned' : 'empty'">{{ getPlotPlan(plot.id) ? '已计划' : '未计划' }}</div>
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>

      <!-- 无计划提示 -->
      <a-row :gutter="16" style="margin-top: 16px" v-if="!currentPlan">
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
        <a-row :gutter="16" style="margin-top: 16px">
          <a-col :span="24">
            <a-card title="生产计划概览" :bordered="false">
              <a-descriptions :column="4" bordered size="small">
                <a-descriptions-item label="总种植面积">{{ totalArea }} 亩</a-descriptions-item>
                <a-descriptions-item label="计划面积">{{ plannedArea }} 亩</a-descriptions-item>
                <a-descriptions-item label="目标产量">{{ totalTargetYield }} kg</a-descriptions-item>
                <a-descriptions-item label="完成率">{{ planCompletionRate }}%</a-descriptions-item>
              </a-descriptions>
            </a-card>
          </a-col>
        </a-row>

        <!-- 统计卡片 -->
        <a-row :gutter="16" style="margin-top: 16px">
          <a-col :span="6">
            <a-card class="stat-card primary">
              <template #title>当前地块</template>
              <div class="stat-value">{{ selectedPlotInfo?.area || 0 }}</div>
              <div class="stat-unit">亩 · {{ selectedPlotInfo?.plotName }}</div>
              <div class="stat-tag">目标产量 {{ formData.targetYield || 0 }} kg/亩</div>
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card class="stat-card">
              <template #title>品种信息</template>
              <div class="stat-value" style="font-size: 18px;">{{ selectedVariety?.varietyName || '-' }}</div>
              <div class="stat-unit">{{ formData.plannedSowingDate || '-' }} 播种</div>
              <div class="stat-tag">{{ formData.plannedHarvestDate || '-' }} 收获</div>
            </a-card>
          </a-col>
          <a-col :span="12">
            <a-card class="stat-card">
              <template #title>土壤养分概览</template>
              <div v-if="soilData && soilData.hasData">
                <div style="margin-bottom: 12px; font-size: 12px; color: #999;">
                  更新时间: {{ soilData.latestDate || '-' }}
                </div>
                <div style="margin-bottom: 8px; font-weight: 600; color: #333;">养分含量</div>
                <div class="soil-grid">
                  <div><span>氮(N)</span><b>{{ soilData.nPercent || '-' }}</b><span>%</span></div>
                  <div><span>磷(P)</span><b>{{ soilData.pPercent || '-' }}</b><span>%</span></div>
                  <div><span>钾(K)</span><b>{{ soilData.kPercent || '-' }}</b><span>%</span></div>
                </div>
                <div style="margin: 8px 0; font-weight: 600; color: #333;">估算施肥量</div>
                <div class="soil-grid">
                  <div><span>氮肥</span><b>{{ soilData.estimatedN || '-' }}</b><span>kg/亩</span></div>
                  <div><span>磷肥</span><b>{{ soilData.estimatedP || '-' }}</b><span>kg/亩</span></div>
                  <div><span>钾肥</span><b>{{ soilData.estimatedK || '-' }}</b><span>kg/亩</span></div>
                </div>
                <div style="margin: 8px 0; font-weight: 600; color: #333;">土壤环境</div>
                <div class="soil-grid">
                  <div><span>土壤温度1</span><b>{{ soilData.soilTemp1 || '-' }}</b><span>℃</span></div>
                  <div><span>土壤温度2</span><b>{{ soilData.soilTemp2 || '-' }}</b><span>℃</span></div>
                  <div><span>土壤温度3</span><b>{{ soilData.soilTemp3 || '-' }}</b><span>℃</span></div>
                  <div><span>土壤湿度1</span><b>{{ soilData.soilMoist1 || '-' }}</b><span>%</span></div>
                  <div><span>土壤湿度2</span><b>{{ soilData.soilMoist2 || '-' }}</b><span>%</span></div>
                  <div><span>土壤湿度3</span><b>{{ soilData.soilMoist3 || '-' }}</b><span>%</span></div>
                  <div><span>EC值1</span><b>{{ soilData.ec1 || '-' }}</b><span>mS/cm</span></div>
                  <div><span>EC值2</span><b>{{ soilData.ec2 || '-' }}</b><span>mS/cm</span></div>
                  <div><span>EC值3</span><b>{{ soilData.ec3 || '-' }}</b><span>mS/cm</span></div>
                </div>
              </div>
              <div class="no-data" v-else-if="soilData && soilData.deviceNotConfigured">设备未配置</div>
              <div class="no-data" v-else>暂无数据</div>
            </a-card>
          </a-col>
        </a-row>

        <!-- 投入卡片 -->
        <a-row :gutter="16" style="margin-top: 16px">
          <a-col :span="8">
            <a-card title="种子投入" :bordered="false" class="input-card green">
              <div class="input-header">
                <span class="input-icon">🌱</span>
                <span class="input-title">种子计划</span>
              </div>
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
          <a-col :span="8">
            <a-card title="肥料投入" :bordered="false" class="input-card blue">
              <div class="input-header">
                <span class="input-icon">🧪</span>
                <span class="input-title">肥料计划</span>
              </div>
              <div class="input-main">
                <div class="input-num">{{ (parseFloat(formData.fertilizerTotalN||0) + parseFloat(formData.fertilizerTotalP||0) + parseFloat(formData.fertilizerTotalK||0)).toFixed(1) }} <span>kg</span></div>
                <div class="input-desc">N+P₂O₅+K₂O 总计</div>
              </div>
              <div class="input-details">
                <div class="detail-row"><span>推荐肥料</span><b>{{ selectedFertilizer?.fertilizerName || '-' }}</b></div>
                <div class="detail-row"><span>N</span><b>{{ formData.fertilizerTotalN || 0 }}kg</b></div>
                <div class="detail-row"><span>P₂O₅</span><b>{{ formData.fertilizerTotalP || 0 }}kg</b></div>
                <div class="detail-row"><span>K₂O</span><b>{{ formData.fertilizerTotalK || 0 }}kg</b></div>
              </div>
            </a-card>
          </a-col>
          <a-col :span="8">
            <a-card title="农药投入" :bordered="false" class="input-card orange">
              <div class="input-header">
                <span class="input-icon">💊</span>
                <span class="input-title">农药计划</span>
              </div>
              <div class="input-main">
                <div class="input-num">{{ formData.pesticideTotal || 0 }} <span>g</span></div>
                <div class="input-desc">计划总量</div>
              </div>
              <div class="input-details">
                <div class="detail-row"><span>推荐农药</span><b>{{ selectedPesticide?.pesticideName || '-' }}</b></div>
                <div class="detail-row"><span>类型</span><b>{{ selectedPesticide?.pesticideType || '-' }}</b></div>
              </div>
            </a-card>
          </a-col>
        </a-row>

        <!-- 编辑按钮 -->
        <a-row :gutter="16" style="margin-top: 16px">
          <a-col :span="24">
            <a-button type="primary" size="large" @click="editModalVisible = true">
              <EditOutlined /> 编辑修改
            </a-button>
          </a-col>
        </a-row>
      </template>
    </a-spin>

    <!-- 编辑弹窗 -->
    <a-modal v-model:open="editModalVisible" title="编辑生产计划" width="800" centered :footer="null" @cancel="editModalVisible = false">
      <a-form :model="formData" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-row :gutter="24">
          <a-col :span="12">
            <a-form-item label="目标产量(kg/亩)">
              <a-input-number v-model:value="formData.targetYield" style="width: 100%" :min="0" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="种植面积(亩)">
              <a-input-number v-model:value="formData.plantingArea" style="width: 100%" :min="0" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="油菜品种">
              <a-select v-model:value="formData.varietyId" style="width: 100%" @change="onVarietyChange">
                <a-select-option v-for="v in varietyList" :key="v.id" :value="v.id">{{ v.varietyName }}</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="计划播种日期">
              <a-date-picker v-model:value="formData.plannedSowingDate" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="计划收获日期">
              <a-date-picker v-model:value="formData.plannedHarvestDate" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="种子总量(kg)">
              <a-input-number v-model:value="formData.seedTotal" style="width: 100%" :min="0" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="氮N(kg)">
              <a-input-number v-model:value="formData.fertilizerTotalN" style="width: 100%" :min="0" :step="0.1" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="磷P₂O₅(kg)">
              <a-input-number v-model:value="formData.fertilizerTotalP" style="width: 100%" :min="0" :step="0.1" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="钾K₂O(kg)">
              <a-input-number v-model:value="formData.fertilizerTotalK" style="width: 100%" :min="0" :step="0.1" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="农药总量(g)">
              <a-input-number v-model:value="formData.pesticideTotal" style="width: 100%" :min="0" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="农药备注">
              <a-input v-model:value="formData.pesticideNote" placeholder="用药说明" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row style="margin-top: 24px">
          <a-col :span="24" style="text-align: center">
            <a-space>
              <a-button @click="editModalVisible = false">取消</a-button>
              <a-button type="primary" @click="saveDraft" :loading="saving">保存</a-button>
            </a-space>
          </a-col>
        </a-row>
      </a-form>
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
import { EditOutlined } from '@ant-design/icons-vue';
import dayjs from 'dayjs';

const selectStore = useSelectStore();
const { selectedBase } = storeToRefs(selectStore);

const loading = ref(false);
const generating = ref(false);
const saving = ref(false);
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

function getPlotPlan(id: string) { return plotPlans.value.get(id); }

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
}

function onVarietyChange(varietyId: string) {
  selectedVariety.value = varietyList.value.find(v => v.id === varietyId) || null;
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
    const res = await defHttp.get({ url: `/rapeseed/fertilization/plotStatusByBase/${baseId}` });
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
    const data = { ...formData.value, baseId: selectedBase.value.baseId, plotId: selectedPlotId.value };
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
  } catch (e) { message.error('保存失败'); }
  finally { saving.value = false; }
}

onMounted(() => { loadAllData(); });
watch(() => selectedBase.value.baseId, (id) => { if (id) { loadPlots(id); loadSoilData(id); } }, { immediate: true });
watch(currentPlan, (plan) => { if (plan) initFormData(plan); });
</script>

<style scoped>
.production-center { padding: 16px; background: #f0f2f5; min-height: calc(100vh - 100px); }
.plot-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(100px, 1fr)); gap: 12px; }
.plot-item { padding: 12px; border: 2px solid #e8e8e8; border-radius: 8px; cursor: pointer; text-align: center; transition: all 0.2s; background: #fff; }
.plot-item:hover { border-color: #1890ff; }
.plot-item.active { border-color: #1890ff; background: #e6f7ff; }
.plot-item.has-plan { border-color: #52c41a; }
.plot-name { font-weight: 600; font-size: 14px; margin-bottom: 2px; }
.plot-area { font-size: 12px; color: #666; }
.plot-tag { font-size: 11px; padding: 2px 6px; border-radius: 4px; margin-top: 4px; display: inline-block; }
.plot-tag.planned { background: #f6ffed; color: #52c41a; }
.plot-tag.empty { background: #fff7e6; color: #fa8c16; }

.empty-card { text-align: center; padding: 40px; }
.stat-card { min-height: 120px; }
.stat-card.primary { background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%); }
.stat-card.primary :deep(.ant-card-head-title) { color: #fff; }
.stat-card.primary .stat-value { color: #fff; }
.stat-card.primary .stat-unit { color: rgba(255,255,255,0.8); }
.stat-card.primary .stat-tag { color: rgba(255,255,255,0.9); font-size: 12px; margin-top: 8px; background: rgba(255,255,255,0.2); padding: 4px 8px; border-radius: 4px; }
.stat-value { font-size: 28px; font-weight: 600; color: #1890ff; }
.stat-unit { font-size: 12px; color: #999; margin-top: 4px; }
.stat-tag { color: #52c41a; font-size: 12px; margin-top: 8px; }
.soil-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }
.soil-grid div { display: flex; justify-content: space-between; padding: 6px 0; font-size: 12px; border-bottom: 1px solid #f0f0f0; }
.soil-grid span:first-child { color: #666; }
.soil-grid b { color: #333; font-weight: 500; }
.soil-grid span:last-child { color: #999; font-size: 11px; margin-left: 4px; }
.no-data { color: #999; text-align: center; padding: 16px; }

.input-card { min-height: 180px; display: flex; flex-direction: column; }
.input-card.green { border-top: 3px solid #52c41a; }
.input-card.blue { border-top: 3px solid #1890ff; }
.input-card.orange { border-top: 3px solid #fa8c16; }
.input-header { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; }
.input-icon { font-size: 18px; }
.input-title { font-size: 14px; font-weight: 600; color: #333; }
.input-main { text-align: center; padding: 12px 0; border-bottom: 1px dashed #d9d9d9; margin-bottom: 12px; }
.input-num { font-size: 28px; font-weight: 600; }
.input-num span { font-size: 14px; font-weight: 400; color: #666; }
.input-desc { font-size: 12px; color: #999; margin-top: 4px; }
.input-details { flex: 1; }
.detail-row { display: flex; justify-content: space-between; padding: 6px 0; font-size: 12px; border-bottom: 1px solid #f0f0f0; }
.detail-row:last-child { border-bottom: none; }
.detail-row span { color: #999; }
.detail-row b { color: #333; font-weight: 500; }
</style>