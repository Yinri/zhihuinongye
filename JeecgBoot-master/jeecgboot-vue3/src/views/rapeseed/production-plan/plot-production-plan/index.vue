<template>
  <div class="production-plan-page">
    <!-- 有计划时：显示生产计划详情和投入进度跟踪 -->
    <template v-if="hasProductionPlan && productionPlanData">
      <!-- 生产计划详情 -->·
      <div class="plan-detail-wrapper">
        <div class="plan-detail-header">
          <h3>{{ productionPlanData.planName }}</h3>
          <span class="plan-status">{{ productionPlanData.status }}</span>
        </div>
        <div class="plan-detail-content">
          <div class="detail-row">
            <span class="detail-label">计划年份：</span>
            <span class="detail-value">{{ productionPlanData.planYear }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">种子品种：</span>
            <span class="detail-value">{{ getVarietyNameById(productionPlanData.varietyId) || '未设置' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">种植面积：</span>
            <span class="detail-value">{{ productionPlanData.plantingArea }} 亩</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">目标产量：</span>
            <span class="detail-value">{{ productionPlanData.targetYield }} kg/亩</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">计划播种日期：</span>
            <span class="detail-value">{{ productionPlanData.plannedSowingDate }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">计划收获日期：</span>
            <span class="detail-value">{{ productionPlanData.plannedHarvestDate }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">肥料计划投入总量：</span>
            <span class="detail-value">N: {{ productionPlanData.fertilizerTotalN }}kg, P: {{ productionPlanData.fertilizerTotalP }}kg, K: {{ productionPlanData.fertilizerTotalK }}kg</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">农药计划投入总量：</span>
            <span class="detail-value">{{ productionPlanData.pesticideTotal }} g</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">农药备注：</span>
            <span class="detail-value">{{ productionPlanData.pesticideNote || '无' }}</span>
          </div>
        </div>
      </div>
          
      <!-- ProgressTrack 组件内容 -->
      <div class="input-tracking-container">
          <div class="section-header">
            <svg class="icon-menu" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M19 3H5C3.9 3 3 3.9 3 5V19C3 20.1 3.9 21 5 21H19C20.1 21 21 20.1 21 19V5C21 3.9 20.1 3 19 3ZM19 19H5V5H19V19Z" fill="#1890ff"/>
              <path d="M7 10H9V17H7V10Z" fill="#1890ff"/>
              <path d="M11 7H13V17H11V7Z" fill="#1890ff"/>
              <path d="M15 13H17V17H15V13Z" fill="#1890ff"/>
            </svg>
            <span>投入进度跟踪</span>
          </div>
          <!-- 种子投入项 -->
          <div class="tracking-item">
            <div class="item-title">
              <span>种子投入</span>
              <span class="target">目标：{{ productionPlanData.seedTotal || 0 }}kg</span>
            </div>
            <div class="progress-bar">
              <div class="bar base" :style="{ width: Math.min(autoSeedProgress, 100) + '%' }"></div>
              <div class="bar exceed" v-if="autoSeedProgress > 100" :style="{ width: (autoSeedProgress - 100) + '%' }"></div>
              <span class="progress-text">{{ autoSeedProgress }}%</span>
            </div>
            <div class="calibration-info">
              <span>实际投入：{{ autoSeedValue }}kg</span>
              <button @click="showSeedCalibration" class="calibration-btn">校准</button>
            </div>
          </div>
          
          <!-- 农药投入项 -->
          <div class="tracking-item">
            <div class="item-title">
              <span>农药投入</span>
              <span class="target">目标：{{ ((productionPlanData.pesticideTotal || 0) / 1000).toFixed(2) }}kg</span>
            </div>
            <div class="progress-bar">
              <div class="bar base" :style="{ width: Math.min(autoPesticideProgress, 100) + '%' }"></div>
              <div class="bar exceed" v-if="autoPesticideProgress > 100" :style="{ width: (autoPesticideProgress - 100) + '%' }"></div>
              <span class="progress-text">{{ autoPesticideProgress }}%</span>
            </div>
            <div class="calibration-info">
              <span>实际投入：{{ autoPesticideValue }}kg</span>
              <button @click="showPesticideCalibration" class="calibration-btn">校准</button>
            </div>
          </div>
          
          <!-- 肥料投入项 -->
          <div class="tracking-item">
            <div class="item-title">
              <span>肥料投入</span>
              <span class="target">目标：{{ ((productionPlanData.fertilizerTotalN || 0) + (productionPlanData.fertilizerTotalP || 0) + (productionPlanData.fertilizerTotalK || 0)).toFixed(2) }}kg</span>
            </div>
            <div class="progress-bar">
              <div class="bar base" :style="{ width: Math.min(autoFertilizerProgress, 100) + '%' }"></div>
              <div class="bar exceed" v-if="autoFertilizerProgress > 100" :style="{ width: (autoFertilizerProgress - 100) + '%' }"></div>
              <span class="progress-text">{{ autoFertilizerProgress }}%</span>
            </div>
            <div class="calibration-info">
              <span>实际投入：{{ autoFertilizerValue }}kg</span>
              <button @click="showFertilizerCalibration" class="calibration-btn">校准</button>
            </div>
          </div>
        </div>
    </template>

    <!-- 没有计划时：显示生成计划按钮或生成计划详情 -->
    <template v-else>
      <!-- 显示生成计划按钮 -->
      <div v-if="showGeneratePlanButton && !showGeneratedPlan" class="generate-plan-wrapper">
        <div class="generate-plan-card">
          <div class="generate-plan-icon">📋</div>
          <h3 class="generate-plan-title">智能生成生产计划</h3>
          <p class="generate-plan-desc">基于当前地块信息、品种数据和土壤肥力，智能生成最优生产计划</p>
          <button @click="handleGeneratePlan" class="generate-btn" :disabled="!canGeneratePlan">
            <span class="btn-icon">✨</span>
            {{ canGeneratePlan ? '立即生成计划' : '请选择地块' }}
          </button>
        </div>
      </div>

      <!-- 显示生成的计划详情（可编辑） -->
      <div v-else-if="showGeneratedPlan && tempPlanData" class="generated-plan-wrapper">
        <div class="generated-plan-header">
          <h3>智能生成的生产计划</h3>
          <div class="header-actions">
            <button @click="cancelGeneratedPlan" class="cancel-plan-btn">取消</button>
            <button @click="confirmGeneratedPlan" class="confirm-plan-btn">确认生成计划</button>
          </div>
        </div>
        
        <div class="generated-plan-content">
          <div class="plan-section">
            <h4 class="section-title">基本信息</h4>
            <div class="plan-form">
              <div class="form-item">
                <label class="form-label">计划名称</label>
                <input v-model="tempPlanData.planName" class="form-input" placeholder="请输入计划名称" />
              </div>
              <div class="form-item">
                <label class="form-label">计划年份</label>
                <input v-model="tempPlanData.planYear" class="form-input" type="number" />
              </div>
              <div class="form-item">
                <label class="form-label">种植品种</label>
                <input v-model="tempPlanData.varietyName" class="form-input" disabled />
              </div>
              <div class="form-item">
                <label class="form-label">种植面积（亩）</label>
                <input v-model="tempPlanData.plantingArea" class="form-input" type="number" step="0.1" />
              </div>
            </div>
          </div>

          <div class="plan-section">
            <h4 class="section-title">产量目标</h4>
            <div class="plan-form">
              <div class="form-item">
                <label class="form-label">目标产量（kg/亩）</label>
                <input v-model="tempPlanData.targetYield" class="form-input" type="number" step="0.1" />
              </div>
              <div class="form-item">
                <label class="form-label">计划播种日期</label>
                <input v-model="tempPlanData.plannedSowingDate" class="form-input" type="date" />
              </div>
              <div class="form-item">
                <label class="form-label">计划收获日期</label>
                <input v-model="tempPlanData.plannedHarvestDate" class="form-input" type="date" />
              </div>
            </div>
          </div>

          <div class="plan-section">
            <h4 class="section-title">投入计划</h4>
            <div class="plan-form">
              <div class="form-item">
                <label class="form-label">种子计划投入总量（kg）</label>
                <input v-model="tempPlanData.seedTotal" class="form-input" type="number" step="0.01" />
              </div>
              <div class="form-item">
                <label class="form-label">农药计划投入总量（g）</label>
                <input v-model="tempPlanData.pesticideTotal" class="form-input" type="number" step="1" />
              </div>
              <div class="form-item">
                <label class="form-label">农药备注</label>
                <input v-model="tempPlanData.pesticideNote" class="form-input" placeholder="请输入备注信息" />
              </div>
              <div class="form-item">
                <label class="form-label">肥料N（kg）</label>
                <input v-model="tempPlanData.fertilizerTotalN" class="form-input" type="number" step="0.1" />
              </div>
              <div class="form-item">
                <label class="form-label">肥料P（kg）</label>
                <input v-model="tempPlanData.fertilizerTotalP" class="form-input" type="number" step="0.1" />
              </div>
              <div class="form-item">
                <label class="form-label">肥料K（kg）</label>
                <input v-model="tempPlanData.fertilizerTotalK" class="form-input" type="number" step="0.1" />
              </div>
              <div class="form-item">
                <label class="form-label">肥料安全系数</label>
                <input v-model="tempPlanData.fertilizerSafetyCoeff" class="form-input" type="number" step="0.1" min="1" max="2" />
              </div>
              <div class="form-item">
                <label class="form-label">农药安全系数</label>
                <input v-model="tempPlanData.pesticideSafetyCoeff" class="form-input" type="number" step="0.1" min="1" max="2" />
              </div>
            </div>
          </div>

          <div class="plan-section">
            <h4 class="section-title">参考信息</h4>
            <div class="plan-form">
              <div class="form-item">
                <label class="form-label">土壤肥力快照</label>
                <div v-if="parsedSoilFertility" class="snapshot-display">
                  <div class="snapshot-item">
                    <span class="snapshot-label">pH值：</span>
                    <span class="snapshot-value">{{ parsedSoilFertility.phValue }}</span>
                  </div>
                  <div class="snapshot-item">
                    <span class="snapshot-label">有机质：</span>
                    <span class="snapshot-value">{{ parsedSoilFertility.organicMatter }} g/kg</span>
                  </div>
                  <div class="snapshot-item">
                    <span class="snapshot-label">氮含量：</span>
                    <span class="snapshot-value">{{ parsedSoilFertility.nitrogen }} mg/kg</span>
                  </div>
                  <div class="snapshot-item">
                    <span class="snapshot-label">磷含量：</span>
                    <span class="snapshot-value">{{ parsedSoilFertility.phosphorus }} mg/kg</span>
                  </div>
                  <div class="snapshot-item">
                    <span class="snapshot-label">钾含量：</span>
                    <span class="snapshot-value">{{ parsedSoilFertility.potassium }} mg/kg</span>
                  </div>
                  <div class="snapshot-item">
                    <span class="snapshot-label">肥力等级：</span>
                    <span class="snapshot-value">{{ parsedSoilFertility.fertilityLevel }}</span>
                  </div>
                </div>
                <textarea v-else v-model="tempPlanData.soilFertilitySnapshot" class="form-textarea" readonly rows="3"></textarea>
              </div>
              <div class="form-item">
                <label class="form-label">种子参数快照</label>
                <div v-if="parsedSeedParams" class="snapshot-display">
                  <div class="snapshot-item">
                    <span class="snapshot-label">收获系数：</span>
                    <span class="snapshot-value">{{ parsedSeedParams.harvestCoefficient }}</span>
                  </div>
                  <div class="snapshot-item">
                    <span class="snapshot-label">成苗率：</span>
                    <span class="snapshot-value">{{ parsedSeedParams.seedlingSurvivalRate }}%</span>
                  </div>
                  <div class="snapshot-item">
                    <span class="snapshot-label">结籽率：</span>
                    <span class="snapshot-value">{{ parsedSeedParams.seedSettingRate }}%</span>
                  </div>
                  <div class="snapshot-item">
                    <span class="snapshot-label">单株角果数：</span>
                    <span class="snapshot-value">{{ parsedSeedParams.singlePlantPods }}</span>
                  </div>
                  <div class="snapshot-item">
                    <span class="snapshot-label">每角粒数：</span>
                    <span class="snapshot-value">{{ parsedSeedParams.seedsPerPod }}</span>
                  </div>
                  <div class="snapshot-item">
                    <span class="snapshot-label">千粒重：</span>
                    <span class="snapshot-value">{{ parsedSeedParams.thousandGrainWeight }} g</span>
                  </div>
                  <div class="snapshot-item">
                    <span class="snapshot-label">发芽率：</span>
                    <span class="snapshot-value">{{ parsedSeedParams.germinationRate }}%</span>
                  </div>
                </div>
                <textarea v-else v-model="tempPlanData.seedParamsSnapshot" class="form-textarea" readonly rows="2"></textarea>
              </div>
              <div class="form-item">
                <label class="form-label">养分需求快照</label>
                <div v-if="parsedNutrientDemand" class="snapshot-display">
                  <div class="snapshot-item">
                    <span class="snapshot-label">氮需求：</span>
                    <span class="snapshot-value">{{ parsedNutrientDemand.nDemand }} kg/100kg</span>
                  </div>
                  <div class="snapshot-item">
                    <span class="snapshot-label">磷需求：</span>
                    <span class="snapshot-value">{{ parsedNutrientDemand.pDemand }} kg/100kg</span>
                  </div>
                  <div class="snapshot-item">
                    <span class="snapshot-label">钾需求：</span>
                    <span class="snapshot-value">{{ parsedNutrientDemand.kDemand }} kg/100kg</span>
                  </div>
                </div>
                <textarea v-else v-model="tempPlanData.nutrientDemandSnapshot" class="form-textarea" readonly rows="2"></textarea>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
    
    <!-- ProgressTrack 组件对话框 -->
    <a-modal
      v-model:open="calibrationModalVisible"
      :title="calibrationTitle"
      @ok="handleSaveCalibration"
      @cancel="handleCancelCalibration"
      width="520px"
      :ok-button-props="{ loading: false }"
    >
      <div class="calibration-content">
        <div class="calibration-info">
          <div class="info-item">
            <span class="info-label">当前投入量：</span>
            <span class="info-value">{{ getCurrentValue() }} kg</span>
          </div>
          <div class="info-item">
            <span class="info-label">目标投入量：</span>
            <span class="info-value">{{ getTargetValue() }} kg</span>
          </div>
          <div class="info-item">
            <span class="info-label">当前进度：</span>
            <span class="info-value" :class="{ 'completed': getCurrentProgress() >= 100 }">
              {{ getCurrentProgress() }}%
            </span>
          </div>
        </div>
        <a-divider />
        <a-form :model="calibrationForm" layout="vertical">
          <a-form-item :label="calibrationLabel" :rules="[{ required: true, message: '请输入投入量' }]">
            <a-input-number
              v-model:value="calibrationForm.value"
              :min="0"
              :step="0.01"
              :precision="2"
              placeholder="请输入投入量"
              style="width: 100%"
              size="large"
            />
          </a-form-item>
        </a-form>
      </div>
    </a-modal>

    <!-- SelectVariety 组件对话框 -->
    <a-modal
      v-model:open="addDialogVisible"
      title="添加品种"
      @ok="addVarietyToDisplay"
      @cancel="closeAddDialog"
    >
      <a-form layout="vertical">
        <a-form-item label="品种名称">
          <a-input v-model:value="newVarietyName" placeholder="请输入品种名称" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script lang="ts" name="rapeseed-production-plan" setup>
  import { ref, onMounted, watch, computed } from 'vue';
  import { useRoute } from 'vue-router';
  import { useMessage } from '/src/hooks/web/useMessage';
  import { getPlotPlanByPlotId, getAllVariety, getVarietyHistoryByVarietyId, addVarietyHistory, updateVarietyHistory, getSeedParamsByVarietyId, addSeedParams, updateSeedParams, saveFertilizerParams, getFertilizerParamsByVarietyId, createProductionPlan, generateProductionPlan } from './base.api';
  import { useSelectStore } from '/@/store/selectStore';

  const { createMessage } = useMessage();
  
  const plotId = ref<string>('');
  const hasProductionPlan = ref(false);
  const selectStore = useSelectStore();
  
  const productionPlanData = ref<any>(null);
  
  const showGeneratePlanButton = ref(false);
  const showGeneratedPlan = ref(false);
  const tempPlanData = ref<any>(null);

  const canGeneratePlan = computed(() => {
    return selectStore.selectedPlot && 
           selectStore.selectedPlot.plotId && 
           selectStore.selectedPlot.plotId !== 'all';
  });

  const parsedSoilFertility = computed(() => {
    if (!tempPlanData.value?.soilFertilitySnapshot) return null;
    try {
      return JSON.parse(tempPlanData.value.soilFertilitySnapshot);
    } catch (e) {
      console.error('解析土壤肥力快照失败:', e);
      return null;
    }
  });

  const parsedSeedParams = computed(() => {
    if (!tempPlanData.value?.seedParamsSnapshot) return null;
    try {
      return JSON.parse(tempPlanData.value.seedParamsSnapshot);
    } catch (e) {
      console.error('解析种子参数快照失败:', e);
      return null;
    }
  });

  const parsedNutrientDemand = computed(() => {
    if (!tempPlanData.value?.nutrientDemandSnapshot) return null;
    try {
      return JSON.parse(tempPlanData.value.nutrientDemandSnapshot);
    } catch (e) {
      console.error('解析养分需求快照失败:', e);
      return null;
    }
  });

  // 检查是否已有生产计划
  const checkProductionPlan = async () => {
    try {
      const res = await getPlotPlanByPlotId(plotId.value);
      console.log('检查生产计划响应:', res);
      if (res) {
        hasProductionPlan.value = true;
        productionPlanData.value = res;
        showGeneratePlanButton.value = false;
        showGeneratedPlan.value = false;
        
        const plan = res;
        
        targetYield.value = plan.targetYield || null;
        fertilizerInput.value = (plan.fertilizerTotalN || 0) + (plan.fertilizerTotalP || 0) + (plan.fertilizerTotalK || 0);
        seedInput.value = null;
        pesticideInput.value = plan.pesticideTotal ? plan.pesticideTotal / 1000 : null;
        
        if (plan.seedParamsSnapshot) {
          const seedParams = JSON.parse(plan.seedParamsSnapshot);
          seedParameter.value = seedParams.seedlingRate || null;
        }
        
        if (plan.nutrientDemandSnapshot) {
          const nutrientDemand = JSON.parse(plan.nutrientDemandSnapshot);
          avgThreeYearYield.value = nutrientDemand.nDemand || null;
        }
        
        seedCoefficient.value = plan.fertilizerSafetyCoeff || 1.2;
        pesticideCoefficient.value = plan.pesticideSafetyCoeff || 1.2;
        fertilizerCoefficient.value = plan.fertilizerSafetyCoeff || 1.2;
        
        if (plan.varietyId) {
          selectedVariety.value = plan.varietyId;
        }
        
      } else {
        hasProductionPlan.value = false;
        productionPlanData.value = null;
        showGeneratePlanButton.value = true;
        showGeneratedPlan.value = false;
        targetYield.value = null;
        fertilizerInput.value = null;
        seedInput.value = null;
        pesticideInput.value = null;
        selectedVariety.value = '';
      }
    } catch (error) {
      console.error('检查生产计划失败:', error);
      hasProductionPlan.value = false;
      productionPlanData.value = null;
      showGeneratePlanButton.value = true;
      showGeneratedPlan.value = false;
      targetYield.value = null;
      fertilizerInput.value = null;
      seedInput.value = null;
      pesticideInput.value = null;
      selectedVariety.value = '';
    }
  };

  // 生成计划
  const handleGeneratePlan = async () => {
    if (!selectStore.selectedPlot || !selectStore.selectedPlot.plotId || selectStore.selectedPlot.plotId === 'all') {
      createMessage.warning('请选择地块');
      return;
    }
    
    try {
      createMessage.loading('正在生成生产计划...', 0);
      
      const currentYear = new Date().getFullYear();
      const baseName = selectStore.selectedBase?.baseName || '';
      const plotName = selectStore.selectedPlot?.plotName || '';
      
      const res = await generateProductionPlan({
        plotId: selectStore.selectedPlot.plotId
      });
      
      createMessage.destroy();
      
      if (res) {
        const varietyName = getVarietyNameById(res.varietyId);
        
        tempPlanData.value = {
          planName: `${baseName}-${plotName}-${currentYear}生产计划`,
          planYear: currentYear,
          varietyId: res.varietyId || '',
          varietyName: varietyName || '未知品种',
          plantingArea: res.plantingArea || 100,
          targetYield: res.targetYield || 200,
          plannedSowingDate: res.plannedSowingDate || `${currentYear}-09-20`,
          plannedHarvestDate: res.plannedHarvestDate || `${currentYear + 1}-05-20`,
          seedTotal: res.seedTotal || '0.00',
          pesticideTotal: res.pesticideTotal || '0',
          pesticideNote: res.pesticideNote || '',
          fertilizerTotalN: res.fertilizerTotalN || '0.0',
          fertilizerTotalP: res.fertilizerTotalP || '0.0',
          fertilizerTotalK: res.fertilizerTotalK || '0.0',
          fertilizerSafetyCoeff: res.fertilizerSafetyCoeff || 1.2,
          pesticideSafetyCoeff: res.pesticideSafetyCoeff || 1.2,
          status: '草稿',
          seedParamsSnapshot: res.seedParamsSnapshot || '',
          nutrientDemandSnapshot: res.nutrientDemandSnapshot || '',
          soilFertilitySnapshot: res.soilFertilitySnapshot || '',
          fertilizerCombination: res.fertilizerCombination || '',
          pesticideCombination: res.pesticideCombination || ''
        };
        
        showGeneratedPlan.value = true;
        showGeneratePlanButton.value = false;
        createMessage.success('生产计划生成成功');
      } else {
        createMessage.error('生产计划生成失败，请重试');
      }
    } catch (error) {
      createMessage.destroy();
      createMessage.error('生产计划生成失败，请重试');
      console.error('生成生产计划失败:', error);
    }
  };

  // 取消生成的计划
  const cancelGeneratedPlan = () => {
    showGeneratedPlan.value = false;
    showGeneratePlanButton.value = true;
    tempPlanData.value = null;
  };

  // 确认生成计划
  const confirmGeneratedPlan = async () => {
    try {
      const submitData = {
        ...tempPlanData.value,
        plotId: plotId.value,
        fertilizerSafetyCoeff: fertilizerCoefficient.value,
        pesticideSafetyCoeff: pesticideCoefficient.value,
        seedParamsSnapshot: JSON.stringify({
          harvestCoefficient: seedParams.value?.harvestCoefficient,
          seedlingRate: seedParams.value?.seedlingRate,
          settingRate: seedParams.value?.settingRate
        }),
        nutrientDemandSnapshot: JSON.stringify({
          nDemand: nutrientDemand.value?.n,
          pDemand: nutrientDemand.value?.p,
          kDemand: nutrientDemand.value?.k
        })
      };
      
      await createProductionPlan(submitData);
      createMessage.success('生产计划创建成功');
      
      await checkProductionPlan();
      showGeneratedPlan.value = false;
      tempPlanData.value = null;
    } catch (error) {
      createMessage.error('生产计划创建失败，请重试');
      console.error('创建生产计划失败:', error);
    }
  };

  // 修改计划
  const handleModifyPlan = () => {
    // 触发父组件事件，切换到修改模式
    emit('modify-plan');
  };

  // 定义事件
  const emit = defineEmits(['generate-plan', 'modify-plan']);

  // 获取plotId
  onMounted(() => {
    // 从store获取地块ID
    if (selectStore.selectedPlot.plotId) {
      plotId.value = selectStore.selectedPlot.plotId;
      checkProductionPlan();
    }
    // 加载品种数据
    loadVarieties();
  });

  // 监听store中plotId的变化
  watch(
    () => selectStore.selectedPlot.plotId,
    (newPlotId) => {
      if (newPlotId && newPlotId !== plotId.value) {
        plotId.value = newPlotId;
        checkProductionPlan();
      }
    }
  );

  // SelectVariety 组件逻辑
  const varieties = ref([]);
  const displayedVarieties = ref([]);
  const selectedVariety = ref('');
  const addDialogVisible = ref(false);
  const newVarietyName = ref('');
  const newVarietyId = ref('');

  // 加载品种列表
  const loadVarieties = async () => {
    try {
      const res = await getAllVariety();
      console.log('加载品种列表响应:', res);
      if (res) {
        varieties.value = res.map(item => ({
          label: item.varietyName,
          value: item.id
        }));
        displayedVarieties.value = varieties.value.slice(0, 3);
        if (displayedVarieties.value.length > 0) {
          selectedVariety.value = (displayedVarieties.value[0] as { value: string }).value;
        }
      }
    } catch (error) {
      console.error('加载品种列表失败:', error);
      createMessage.error('加载品种列表失败');
    }
  };

  // 根据品种ID获取品种名称
  const getVarietyNameById = (varietyId: string) => {
    if (!varietyId) return '';
    const variety = varieties.value.find(v => v.value === varietyId);
    return variety ? variety.label : '';
  };

  // 选择品种
  const selectVariety = (varietyId) => {
    selectedVariety.value = varietyId;
  };

  // 打开添加品种对话框
  const openAddDialog = () => {
    addDialogVisible.value = true;
  };

  // 关闭添加品种对话框
  const closeAddDialog = () => {
    addDialogVisible.value = false;
    newVarietyName.value = '';
    newVarietyId.value = '';
  };

  // 添加新品种到显示列表
  const addVarietyToDisplay = () => {
    if (!newVarietyName.value.trim()) {
      createMessage.warning('请输入品种名称');
      return;
    }

    // 检查是否已在显示列表中
    const exists = displayedVarieties.value.find(v => v.label === newVarietyName.value);
    if (exists) {
      createMessage.warning('该品种已在显示列表中');
      return;
    }

    // 添加到显示列表
    const newVariety = {
      label: newVarietyName.value,
      value: newVarietyId.value || `temp_${Date.now()}`
    };

    displayedVarieties.value.push(newVariety);
    closeAddDialog();
    createMessage.success('添加品种成功');
  };

  // PredictionResults 组件逻辑
  const targetYield = ref(null);
  const seedInput = ref(null);
  const pesticideInput = ref(null);
  const fertilizerInput = ref(null);
  const seedCoefficient = ref(1.2);
  const pesticideCoefficient = ref(1.2);
  const fertilizerCoefficient = ref(1.2);

  // 种子保险系数调整
  const increaseSeedCoefficient = () => {
    if (seedCoefficient.value < 2.0) {
      seedCoefficient.value += 0.1;
      seedCoefficient.value = Math.round(seedCoefficient.value * 10) / 10;
    }
  };

  const decreaseSeedCoefficient = () => {
    if (seedCoefficient.value > 1.0) {
      seedCoefficient.value -= 0.1;
      seedCoefficient.value = Math.round(seedCoefficient.value * 10) / 10;
    }
  };

  // 农药保险系数调整
  const increasePesticideCoefficient = () => {
    if (pesticideCoefficient.value < 2.0) {
      pesticideCoefficient.value += 0.1;
      pesticideCoefficient.value = Math.round(pesticideCoefficient.value * 10) / 10;
    }
  };

  const decreasePesticideCoefficient = () => {
    if (pesticideCoefficient.value > 1.0) {
      pesticideCoefficient.value -= 0.1;
      pesticideCoefficient.value = Math.round(pesticideCoefficient.value * 10) / 10;
    }
  };

  // 肥料保险系数调整
  const increaseFertilizerCoefficient = () => {
    if (fertilizerCoefficient.value < 2.0) {
      fertilizerCoefficient.value += 0.1;
      fertilizerCoefficient.value = Math.round(fertilizerCoefficient.value * 10) / 10;
    }
  };

  const decreaseFertilizerCoefficient = () => {
    if (fertilizerCoefficient.value > 1.0) {
      fertilizerCoefficient.value -= 0.1;
      fertilizerCoefficient.value = Math.round(fertilizerCoefficient.value * 10) / 10;
    }
  };

  // DataBasis 组件逻辑
  const modalVisible = ref(false);
  const avgThreeYearYield = ref(null);
  const seedParameter = ref(null);
  const fertilizerCoefficientData = ref(null);
  const formData = ref({
    avgThreeYearYield: null,
    seedParameter: null,
    fertilizerCoefficient: null
  });

  // 本地状态存储数据基础信息
  const dataBasisInfo = ref({
    avgThreeYearYield: null,
    seedParameter: null,
    fertilizerCoefficient: null
  });

  // DataBasis 组件新增数据属性
  const showYearInput = ref(false);
  const currentYear = new Date().getFullYear();
  const threeYears = [currentYear - 3, currentYear - 2, currentYear - 1];
  const yearValues = ref([undefined, undefined, undefined]);
  const increaseRate = ref('12');
  const localSafetyCoefficient = ref(1.2);

  // 种子参数相关
  const showSeedParamModal = ref(false);
  const seedParams = ref({
    harvestCoefficient: null,
    seedlingRate: null,
    settingRate: null,
    id: null
  });
  const tempSeedParams = ref({
    harvestCoefficient: null,
    seedlingRate: null,
    settingRate: null
  });

  // 单位产量需肥量
  const showNutrientDemandModal = ref(false);
  const nutrientDemand = ref({
    n: 5.8,
    p: 2.5,
    k: 4.3
  });
  const tempNutrientDemand = ref({
    n: 5.8,
    p: 2.5,
    k: 4.3
  });

  // 显示模态框
  const handleShowModal = () => {
    if (!selectedVariety.value) {
      createMessage.warning('请先选择作物品种');
      return;
    }
    fetchThreeYearYield();
    showYearInput.value = true;
  };

  // 获取前三年产量数据
  const fetchThreeYearYield = async () => {
    try {
      const response = await getVarietyHistoryByVarietyId({
        varietyId: selectedVariety.value,
        pageNo: 1,
        pageSize: 10
      });
      const currentVarietyYields = response.records.filter(
        item => (item.varietyId + '') === (selectedVariety.value + '')
      );
      yearValues.value = threeYears.map(targetYear => {
        const matchedItem = currentVarietyYields.find(
          item => Number(item.year) === Number(targetYear) && item.yield != null
        );
        return matchedItem ? matchedItem.yield : undefined;
      });
    } catch (error) {
      createMessage.error('拉取历史产量数据失败，请重试');
      console.error('历史产量请求错误：', error);
      yearValues.value = [undefined, undefined, undefined];
    }
  };

  // 计算平均值
  const calculateAvg = () => {
    const validValues = yearValues.value.filter(v =>
      v !== undefined && v !== null && !isNaN(Number(v))
    );
    if (validValues.length === 0) {
      createMessage.warning('请至少录入一年的单产数据');
      return;
    }
    const submitData = threeYears.map((year, index) => {
      const yieldValue = yearValues.value[index];
      return yieldValue !== undefined && yieldValue !== null && !isNaN(Number(yieldValue))
        ? {
          varietyId: selectedVariety.value,
          year: year,
          yield: Number(yieldValue),
        }
        : null;
    }).filter(item => item !== null);
    submitHistoryYield(submitData, validValues);
  };

  // 提交历史产量数据
  const submitHistoryYield = async (submitData, validValues) => {
    try {
      const historyRes = await getVarietyHistoryByVarietyId({
        varietyId: selectedVariety.value,
        pageSize: 100
      });
      const existingYields = historyRes?.data?.records || historyRes?.records || [];
      const addList = [];
      const updateList = [];
      submitData.forEach(item => {
        const existingItem = existingYields.find(
          y => (y.varietyId + '') === (selectedVariety.value + '') && Number(y.year) === Number(item.year)
        );
        if (existingItem) {
          updateList.push({
            id: existingItem.id,
            varietyId: selectedVariety.value,
            year: item.year,
            yield: item.yield,
            plot: item.plot || existingItem.plot
          });
        } else {
          addList.push({
            varietyId: selectedVariety.value,
            year: item.year,
            yield: item.yield,
            plot: item.plot
          });
        }
      });
      let addCount = 0;
      for (const item of addList) {
        try {
          await addVarietyHistory(item);
          addCount++;
        } catch (err) {
          handleDbError(err, '新增', item.year);
          return;
        }
      }
      let updateCount = 0;
      for (const item of updateList) {
        try {
          await updateVarietyHistory(item);
          updateCount++;
        } catch (err) {
          handleDbError(err, '更新', item.year);
          return;
        }
      }
      const sum = validValues.reduce((total, val) => total + Number(val), 0);
      avgThreeYearYield.value = (sum / validValues.length).toFixed(1);
      showYearInput.value = false;
      createMessage.success(`成功新增${addCount}条，更新${updateCount}条数据`);
    } catch (error) {
      console.error('提交流程异常：', error);
      createMessage.error('系统异常，请稍后重试');
    }
  };

  // 关闭模态框
  const closeModal = () => {
    showYearInput.value = false;
  };

  // 处理数据库错误
  const handleDbError = (error, operation, year) => {
    const errorMsg = error?.response?.data?.msg || error?.message || '';
    let userMsg = '';
    if (errorMsg.includes('uk_variety_year') || errorMsg.includes('Duplicate entry')) {
      userMsg = `【${year}年】数据已存在，无法${operation}（同一品种同一年份仅允许一条记录）`;
    } else if (errorMsg.includes('NOT NULL')) {
      userMsg = `【${year}年】必填字段缺失（品种ID、年份、单产不能为空）`;
    } else {
      userMsg = `【${year}年】${operation}失败：${errorMsg.slice(0, 50)}`;
    }
    createMessage.error(userMsg);
    console.error(`【${year}年】错误详情：`, error);
  };

  // 种子参数相关方法
  const handleSeedParamModal = async () => {
    if (!selectedVariety.value) {
      createMessage.warning('请先选择作物品种');
      return;
    }
    await fetchSeedParams();
    tempSeedParams.value = {...seedParams.value};
    showSeedParamModal.value = true;
  };

  const fetchSeedParams = async () => {
    const varietyId = selectedVariety.value;
    if (!varietyId) {
      seedParams.value = {
        id: null,
        harvestCoefficient: null,
        seedlingRate: null,
        settingRate: null
      };
      return;
    }
    try {
      const response = await getSeedParamsByVarietyId(varietyId);
      if (response && response.id) {
        seedParams.value = {
          id: response.id,
          harvestCoefficient: response.harvestCoefficient,
          seedlingRate: response.seedlingSurvivalRate,
          settingRate: response.seedSettingRate
        };
      } else {
        seedParams.value = {
          id: null,
          harvestCoefficient: null,
          seedlingRate: null,
          settingRate: null
        };
      }
    } catch (error) {
      createMessage.error('获取种子参数失败，请重试');
      seedParams.value = {
        id: null,
        harvestCoefficient: null,
        seedlingRate: null,
        settingRate: null
      };
    }
  };

  const saveSeedParams = async () => {
    const {harvestCoefficient, seedlingRate, settingRate, id} = tempSeedParams.value;
    if (harvestCoefficient === null || harvestCoefficient < 0.3 || harvestCoefficient > 0.5) {
      createMessage.warning('请输入0.3-0.5之间的收获系数');
      return;
    }
    if (seedlingRate === null || seedlingRate < 50 || seedlingRate > 100) {
      createMessage.warning('请输入50-100之间的田间保苗率');
      return;
    }
    if (settingRate === null || settingRate < 70 || settingRate > 95) {
      createMessage.warning('请输入70-95之间的结实率');
      return;
    }
    const submitData = {
      varietyId: selectedVariety.value,
      harvestCoefficient,
      seedlingSurvivalRate: seedlingRate,
      seedSettingRate: settingRate
    };
    try {
      if (id) {
        submitData.id = id;
        await updateSeedParams(submitData);
        createMessage.success('种子参数更新成功');
      } else {
        await addSeedParams(submitData);
        createMessage.success('种子参数新增成功');
      }
      showSeedParamModal.value = false;
      fetchSeedParams();
    } catch (error) {
      console.error('保存种子参数失败：', error);
      const errorMsg = error?.response?.data?.msg || '保存失败，请重试';
      createMessage.error(errorMsg);
    }
  };

  watch(selectedVariety, (newVarietyId) => {
    if (newVarietyId) {
      fetchSeedParams();
    }
  });

  // 需肥量相关方法
  const handleNutrientDemandModal = () => {
    if (!selectedVariety.value) {
      createMessage.warning('请先选择作物品种');
      return;
    }
    tempNutrientDemand.value = {
      n: nutrientDemand.value.n || 5.8,
      p: nutrientDemand.value.p || 2.5,
      k: nutrientDemand.value.k || 4.3
    };
    showNutrientDemandModal.value = true;
  };

  const saveNutrientDemand = async () => {
    const {n, p, k} = tempNutrientDemand.value;
    if (n === null || n === undefined || n < 4.0 || n > 7.0) {
      createMessage.warning('请输入4.0-7.0之间的N肥需肥量');
      return;
    }
    if (p === null || p === undefined || p < 1.5 || p > 3.5) {
      createMessage.warning('请输入1.5-3.5之间的P₂O₅需肥量');
      return;
    }
    if (k === null || k === undefined || k < 3.0 || k > 5.5) {
      createMessage.warning('请输入3.0-5.5之间的K₂O需肥量');
      return;
    }
    if (!selectedVariety.value) {
      createMessage.error('请先选择作物品种！');
      return;
    }

    const cleanN = Number(n).toFixed(1) * 1;
    const cleanP = Number(p).toFixed(1) * 1;
    const cleanK = Number(k).toFixed(1) * 1;
    const cleanVarietyId = String(selectedVariety.value).trim();

    const submitData = {
      varietyId: cleanVarietyId,
      nDemand: cleanN,
      pDemand: cleanP,
      kDemand: cleanK
    };

    try {
      const res = await saveFertilizerParams(submitData);
      const isSuccess = res === '更新成功' || res === '新增成功' || (res?.code === 200);
      if (isSuccess) {
        nutrientDemand.value = {n: cleanN, p: cleanP, k: cleanK};
        showNutrientDemandModal.value = false;
        createMessage.success(res?.msg || '需肥量保存成功');
        getNutrientDemandByVarietyId();
      } else {
        createMessage.error('操作失败：' + (res?.msg || res || '未知错误'));
      }
    } catch (error) {
      console.error('保存需肥量失败详情：', error);
      const errMsg = error.response?.data?.msg || error.message || '未知错误';
      if (errMsg.includes('惟一约束') || errMsg.includes('Duplicate')) {
        createMessage.error(`品种ID【${cleanVarietyId}】已存在，请更新而非新增！`);
      } else if (errMsg.includes('NumberFormatException')) {
        createMessage.error('品种ID格式错误（必须是数字字符串）！');
      } else {
        createMessage.error('保存失败：' + errMsg.slice(0, 50));
      }
      showNutrientDemandModal.value = false;
    }
  };

  const getNutrientDemandByVarietyId = async () => {
    if (!selectedVariety.value) {
      nutrientDemand.value = { n: 5.8, p: 2.5, k: 4.3 };
      tempNutrientDemand.value = { ...nutrientDemand.value };
      return;
    }

    const currentVarietyId = String(selectedVariety.value).trim();

    try {
      const res = await getFertilizerParamsByVarietyId(selectedVariety.value);

      let demandData = { n: 5.8, p: 2.5, k: 4.3 };
      if (res && res.success !== false) {
        if (res.result && res.result.varietyId) {
          demandData = {
            n: res.result.nDemand || 5.8,
            p: res.result.pDemand || 2.5,
            k: res.result.kDemand || 4.3
          };
        } else if (res.varietyId) {
          demandData = {
            n: res.nDemand || 5.8,
            p: res.pDemand || 2.5,
            k: res.kDemand || 4.3
          };
        }
      }

      nutrientDemand.value = demandData;
      tempNutrientDemand.value = { ...demandData };
    } catch (error) {
      console.error('获取需肥量失败：', error);
      nutrientDemand.value = { n: 5.8, p: 2.5, k: 4.3 };
      tempNutrientDemand.value = { ...nutrientDemand.value };
    }
  };

  // 显示模态框
  const handleShowModal_old = () => {
    formData.value = {
      avgThreeYearYield: dataBasisInfo.value.avgThreeYearYield || null,
      seedParameter: dataBasisInfo.value.seedParameter || null,
      fertilizerCoefficient: dataBasisInfo.value.fertilizerCoefficient || null
    };
    modalVisible.value = true;
  };

  // 保存数据
  const handleSaveData = () => {
    dataBasisInfo.value = {
      avgThreeYearYield: formData.value.avgThreeYearYield,
      seedParameter: formData.value.seedParameter,
      fertilizerCoefficient: formData.value.fertilizerCoefficient
    };
    
    avgThreeYearYield.value = formData.value.avgThreeYearYield;
    seedParameter.value = formData.value.seedParameter;
    fertilizerCoefficientData.value = formData.value.fertilizerCoefficient;
    
    modalVisible.value = false;
    createMessage.success('数据已保存');
  };

  // 取消模态框
  const handleCancelModal = () => {
    modalVisible.value = false;
  };

  // ProgressTrack 组件逻辑
  const calibrationModalVisible = ref(false);
  const calibrationTitle = ref('');
  const calibrationLabel = ref('');
  const calibrationType = ref('');
  const calibrationForm = ref({
    value: null
  });
  
  // 投入进度数据
  const autoSeedProgress = ref(0);
  const autoSeedValue = ref(0);
  const autoPesticideProgress = ref(0);
  const autoPesticideValue = ref(0);
  const autoFertilizerProgress = ref(0);
  const autoFertilizerValue = ref(0);

  // 校准配置
  const calibrationConfig = {
    seed: {
      title: '种子投入校准',
      label: '种子投入量 (kg)',
      targetKey: 'seedTotal',
      valueRef: autoSeedValue,
      progressRef: autoSeedProgress
    },
    pesticide: {
      title: '农药投入校准',
      label: '农药投入量 (kg)',
      targetKey: 'pesticideTotal',
      valueRef: autoPesticideValue,
      progressRef: autoPesticideProgress
    },
    fertilizer: {
      title: '肥料投入校准',
      label: '肥料投入量 (kg)',
      targetKey: 'fertilizerTotal',
      valueRef: autoFertilizerValue,
      progressRef: autoFertilizerProgress
    }
  };

  // 显示校准对话框
  const showCalibration = (type: 'seed' | 'pesticide' | 'fertilizer') => {
    const config = calibrationConfig[type];
    calibrationType.value = type;
    calibrationTitle.value = config.title;
    calibrationLabel.value = config.label;
    calibrationForm.value.value = config.valueRef.value ?? 0;
    calibrationModalVisible.value = true;
  };

  // 显示种子校准对话框
  const showSeedCalibration = () => showCalibration('seed');

  // 显示农药校准对话框
  const showPesticideCalibration = () => showCalibration('pesticide');

  // 显示肥料校准对话框
  const showFertilizerCalibration = () => showCalibration('fertilizer');

  // 获取当前投入量
  const getCurrentValue = () => {
    const config = calibrationConfig[calibrationType.value];
    return config.valueRef.value?.toFixed(2) || '0.00';
  };

  // 获取目标投入量
  const getTargetValue = () => {
    const config = calibrationConfig[calibrationType.value];
    const targetValue = productionPlanData.value[config.targetKey] || 0;
    return targetValue.toFixed(2);
  };

  // 获取当前进度
  const getCurrentProgress = () => {
    const config = calibrationConfig[calibrationType.value];
    return config.progressRef.value || 0;
  };

  // 保存校准数据
  const handleSaveCalibration = () => {
    const inputValue = calibrationForm.value.value;
    
    if (inputValue === null || inputValue === undefined || inputValue < 0) {
      createMessage.warning('请输入有效的投入量');
      return;
    }
    
    const config = calibrationConfig[calibrationType.value];
    const targetValue = productionPlanData.value[config.targetKey] || 0;
    
    config.valueRef.value = inputValue;
    config.progressRef.value = Math.min(100, Math.round((inputValue / targetValue) * 100));
    
    calibrationModalVisible.value = false;
    createMessage.success('校准已保存');
  };

  // 取消校准
  const handleCancelCalibration = () => {
    calibrationModalVisible.value = false;
  };

  // 监听种子、农药、肥料投入和系数的变化，更新投入进度
  watch([seedInput, pesticideInput, fertilizerInput, seedCoefficient, pesticideCoefficient, fertilizerCoefficient], () => {
    // 更新种子投入
    if (seedInput.value) {
      autoSeedValue.value = seedInput.value * seedCoefficient.value;
      autoSeedProgress.value = Math.round((autoSeedValue.value / 500) * 100);
    }
    // 更新农药投入
    if (pesticideInput.value) {
      autoPesticideValue.value = pesticideInput.value * pesticideCoefficient.value;
      autoPesticideProgress.value = Math.round((autoPesticideValue.value / 50) * 100);
    }
    // 更新肥料投入
    if (fertilizerInput.value) {
      autoFertilizerValue.value = fertilizerInput.value * fertilizerCoefficient.value;
      autoFertilizerProgress.value = Math.round((autoFertilizerValue.value / 200) * 100);
    }
  });

</script>
<style lang="less" scoped>
.production-plan-page {
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
  min-height: calc(100vh - 100px);
}

.parent-container {
  display: flex;
  gap: 20px;
  width: 100%;
  box-sizing: border-box;
  align-items: stretch;
}

.prediction-card {
  display: flex;
  flex-direction: column;
  flex: 0 0 45%;
  min-width: 320px;
}

.parent-container > div:nth-child(2) {
  flex: 1;
  min-width: 320px;
}

// QueryPlan 组件样式
.plan-status-wrapper {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;

  &:hover {
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
    transform: translateY(-2px);
  }

  .status-label {
    display: block;
    font-size: 15px;
    font-weight: 600;
    color: #1a1a1a;
    margin-bottom: 16px;
    letter-spacing: 0.3px;
  }

  .status-content {
    .status-info {
      display: flex;
      align-items: center;
      justify-content: space-between;

      .status-text {
        font-size: 14px;
        color: #595959;
        font-weight: 500;
      }

      button {
        padding: 8px 20px;
        border-radius: 6px;
        border: none;
        cursor: pointer;
        font-size: 14px;
        font-weight: 500;
        transition: all 0.3s ease;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

        &:active {
          transform: scale(0.98);
        }
      }
    }

    .existing-plan {
      .status-text {
        color: #52c41a;
        font-weight: 600;
      }

      .modify-btn {
        background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
        color: white;

        &:hover {
          background: linear-gradient(135deg, #40a9ff 0%, #1890ff 100%);
          box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
        }
      }
    }

    .no-plan {
      .status-text {
        color: #ff4d4f;
        font-weight: 600;
      }

      .generate-btn {
        background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
        color: white;

        &:hover {
          background: linear-gradient(135deg, #73d13d 0%, #52c41a 100%);
          box-shadow: 0 4px 12px rgba(82, 196, 26, 0.3);
        }
      }
    }
  }
}

// 生产计划详情样式
.plan-detail-wrapper {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;

  &:hover {
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
    transform: translateY(-2px);
  }

  .plan-detail-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 16px;
    border-bottom: 2px solid #e8ecf1;

    h3 {
      margin: 0;
      font-size: 18px;
      font-weight: 700;
      background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }

    .plan-status {
      display: inline-block;
      padding: 6px 16px;
      border-radius: 20px;
      font-size: 13px;
      font-weight: 600;
      background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
      color: #ffffff;
      box-shadow: 0 2px 8px rgba(82, 196, 26, 0.3);
    }
  }

  .plan-detail-content {
    .detail-row {
      display: flex;
      align-items: center;
      padding: 12px 0;
      border-bottom: 1px solid #f0f0f0;
      transition: all 0.3s ease;

      &:hover {
        background: rgba(24, 144, 255, 0.03);
        padding-left: 8px;
        border-radius: 6px;
      }

      &:last-child {
        border-bottom: none;
      }

      .detail-label {
        min-width: 140px;
        font-size: 14px;
        font-weight: 600;
        color: #595959;
      }

      .detail-value {
        flex: 1;
        font-size: 14px;
        color: #262626;
        font-weight: 500;
      }
    }
  }
}

// SelectVariety 组件样式
.crop-variety-wrapper {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;

  &:hover {
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
    transform: translateY(-2px);
  }

  .crop-variety-selector {
    .variety-label {
      display: block;
      font-size: 15px;
      font-weight: 600;
      color: #1a1a1a;
      margin-bottom: 16px;
      letter-spacing: 0.3px;
    }

    .variety-buttons {
      display: flex;
      flex-wrap: wrap;
      gap: 12px;

      .variety-btn {
        padding: 10px 20px;
        border: 2px solid #e8e8e8;
        border-radius: 8px;
        background: #ffffff;
        color: #595959;
        cursor: pointer;
        transition: all 0.3s ease;
        font-weight: 500;
        font-size: 14px;

        &:hover {
          border-color: #1890ff;
          color: #1890ff;
          background: #e6f7ff;
          transform: translateY(-2px);
          box-shadow: 0 4px 8px rgba(24, 144, 255, 0.2);
        }

        &.active {
          background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
          border-color: #1890ff;
          color: #fff;
          box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
        }
      }

      .add-btn {
        padding: 10px 16px;
        border: 2px dashed #d9d9d9;
        border-radius: 8px;
        background: transparent;
        color: #8c8c8c;
        cursor: pointer;
        transition: all 0.3s ease;
        font-size: 18px;
        font-weight: 300;

        &:hover {
          border-color: #1890ff;
          color: #1890ff;
          background: #e6f7ff;
          transform: translateY(-2px);
        }
      }
    }
  }
}

// ProductionAdjust 组件样式
.yield-adjust-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;

  &:hover {
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
    transform: translateY(-2px);
  }

  .yield-label {
    display: flex;
    align-items: center;
    font-size: 15px;
    font-weight: 600;
    color: #1a1a1a;
    letter-spacing: 0.3px;

    .icon-yield {
      margin-right: 10px;
      width: 22px;
      height: 22px;
    }

    .yield-formula {
      font-size: 12px;
      color: #8c8c8c;
      margin-left: 10px;
      font-weight: 400;
      background: #f5f5f5;
      padding: 4px 8px;
      border-radius: 4px;
    }
  }

  .adjust-btn {
    display: flex;
    align-items: center;
    padding: 8px 20px;
    background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
    color: white;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    transition: all 0.3s ease;
    font-weight: 500;
    box-shadow: 0 2px 8px rgba(24, 144, 255, 0.2);

    .icon-pencil {
      margin-right: 8px;
      width: 16px;
      height: 16px;
    }

    &:hover {
      background: linear-gradient(135deg, #40a9ff 0%, #1890ff 100%);
      box-shadow: 0 4px 12px rgba(24, 144, 255, 0.4);
      transform: translateY(-2px);
    }

    &:active {
      transform: scale(0.98);
    }
  }
}

// PredictionResults 组件样式
.yield-indicator {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 20px;

  .input-card {
    background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    border: 1px solid rgba(0, 0, 0, 0.05);
    text-align: center;
    transition: all 0.3s ease;

    &:hover {
      box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
      transform: translateY(-4px);
    }

    .card-header {
      font-size: 13px;
      color: #8c8c8c;
      margin-bottom: 10px;
      font-weight: 500;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }

    .main-value {
      font-size: 28px;
      font-weight: 700;
      background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      margin-bottom: 6px;
    }

    .unit {
      font-size: 12px;
      color: #bfbfbf;
      font-weight: 500;
    }
  }
}

.coefficient-adjustment {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;

  &:hover {
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
  }

  .coefficient-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding: 12px;
    border-radius: 8px;
    background: #fafafa;
    transition: all 0.3s ease;

    &:hover {
      background: #f0f0f0;
    }

    &:last-child {
      margin-bottom: 0;
    }

    .coefficient-label {
      font-size: 14px;
      color: #262626;
      font-weight: 500;
    }

    .coefficient-control {
      display: flex;
      align-items: center;

      .coefficient-btn {
        width: 32px;
        height: 32px;
        border-radius: 50%;
        border: 2px solid #e8e8e8;
        background: #ffffff;
        color: #595959;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: all 0.3s ease;
        font-weight: 600;
        font-size: 16px;

        &:hover {
          border-color: #1890ff;
          color: #1890ff;
          background: #e6f7ff;
          transform: scale(1.1);
        }

        &:active {
          transform: scale(0.95);
        }

        &.decrease {
          margin-right: 12px;
        }

        &.increase {
          margin-left: 12px;
        }
      }

      .coefficient-value {
        min-width: 40px;
        text-align: center;
        font-weight: 700;
        font-size: 16px;
        color: #1890ff;
      }
    }
  }
}

// DataBasis 组件样式
.yield-calc-basis {
  margin-bottom: 32px;
  background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
  border-radius: 20px;
  padding: 28px 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);

  .section-title {
    font-size: 18px;
    font-weight: 700;
    color: #1a1a1a;
    margin-bottom: 24px;
    padding-bottom: 16px;
    border-bottom: 3px solid #e8e8e8;
    letter-spacing: 0.5px;
    position: relative;

    &::after {
      content: '';
      position: absolute;
      bottom: -3px;
      left: 0;
      width: 60px;
      height: 3px;
      background: linear-gradient(90deg, #1890ff 0%, #36cfc9 100%);
      border-radius: 3px;
    }
  }

  .cards-container {
    display: flex;
    gap: 20px;

    .basis-cards {
      display: flex;
      gap: 20px;
      width: 100%;
    }

    .basis-card {
      flex: 1;
      background: linear-gradient(145deg, #ffffff 0%, #fafbfc 100%);
      border-radius: 20px;
      padding: 28px 24px;
      box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
      border: 1px solid rgba(0, 0, 0, 0.06);
      transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
      display: flex;
      flex-direction: column;
      min-height: 300px;
      position: relative;
      overflow: hidden;

      &::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        height: 4px;
        background: linear-gradient(90deg, #1890ff 0%, #36cfc9 100%);
        opacity: 0;
        transition: opacity 0.3s ease;
      }

      &:hover {
        box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
        transform: translateY(-6px);
        border-color: rgba(24, 144, 255, 0.2);

        &::before {
          opacity: 1;
        }
      }

      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;

        .card-title {
          font-size: 15px;
          color: #737373;
          font-weight: 700;
          text-transform: uppercase;
          letter-spacing: 0.8px;
          margin: 0;
        }

        .card-icon {
          font-size: 28px;
          opacity: 0.85;
          filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
          transition: transform 0.3s ease;
        }

        &:hover .card-icon {
          transform: scale(1.1) rotate(5deg);
        }
      }

      .card-content {
        flex: 1;
        display: flex;
        flex-direction: column;
        gap: 20px;
        margin-bottom: 20px;

        .card-value {
          font-size: 32px;
          font-weight: 800;
          background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
          background-clip: text;
          margin: 0;
          line-height: 1.2;
          letter-spacing: -0.5px;

          .unit {
            font-size: 15px;
            font-weight: 600;
            color: #8c8c8c;
            -webkit-text-fill-color: #8c8c8c;
            margin-left: 6px;
          }
        }

        .param-values {
          display: flex;
          flex-direction: column;
          gap: 14px;

          .param-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 14px 18px;
            background: linear-gradient(135deg, #fafafa 0%, #f5f5f5 100%);
            border-radius: 10px;
            transition: all 0.3s ease;
            border: 1px solid transparent;

            &:hover {
              background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
              transform: translateX(6px);
              border-color: rgba(24, 144, 255, 0.3);
              box-shadow: 0 4px 12px rgba(24, 144, 255, 0.15);
            }

            .param-label {
              font-size: 14px;
              color: #595959;
              font-weight: 600;
              letter-spacing: 0.2px;
            }

            .param-value {
              font-size: 16px;
              font-weight: 700;
              color: #1890ff;
              letter-spacing: -0.2px;

              .unit {
                font-size: 13px;
                font-weight: 600;
                color: #8c8c8c;
                margin-left: 4px;
              }
            }
          }
        }
      }

      .card-footer {
        padding-top: 20px;
        border-top: 1px solid #f0f0f0;
        display: flex;
        align-items: center;
        gap: 14px;

        .rate-label {
          font-size: 14px;
          color: #595959;
          font-weight: 600;
          white-space: nowrap;
          letter-spacing: 0.2px;
        }

        .rate-select {
          flex: 1;
          padding: 10px 14px;
          border: 1.5px solid #d9d9d9;
          border-radius: 8px;
          background: linear-gradient(135deg, #ffffff 0%, #fafafa 100%);
          font-size: 14px;
          color: #262626;
          cursor: pointer;
          transition: all 0.3s ease;
          font-weight: 500;

          &:hover {
            border-color: #1890ff;
            box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.1);
            background: #ffffff;
          }

          &:focus {
            outline: none;
            border-color: #1890ff;
            box-shadow: 0 0 0 4px rgba(24, 144, 255, 0.15);
          }
        }
      }

      .edit-btn {
        padding: 12px 24px;
        background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
        border: none;
        border-radius: 10px;
        color: #ffffff;
        cursor: pointer;
        font-size: 14px;
        font-weight: 700;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 8px;
        box-shadow: 0 4px 12px rgba(24, 144, 255, 0.35);
        letter-spacing: 0.3px;

        &:hover {
          background: linear-gradient(135deg, #40a9ff 0%, #1890ff 100%);
          box-shadow: 0 6px 16px rgba(24, 144, 255, 0.45);
          transform: translateY(-3px);
        }

        &:active {
          transform: translateY(-1px) scale(0.98);
        }

        .btn-icon {
          font-size: 15px;
        }
      }
    }
  }
}

// ProgressTrack 组件样式
.input-tracking-container {
  background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
  border-radius: 20px;
  padding: 28px 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  margin-bottom: 32px;

  .section-header {
    display: flex;
    align-items: center;
    font-size: 18px;
    font-weight: 700;
    color: #1a1a1a;
    margin-bottom: 24px;
    padding-bottom: 16px;
    border-bottom: 3px solid #e8e8e8;
    letter-spacing: 0.5px;
    position: relative;

    &::after {
      content: '';
      position: absolute;
      bottom: -3px;
      left: 0;
      width: 60px;
      height: 3px;
      background: linear-gradient(90deg, #1890ff 0%, #36cfc9 100%);
      border-radius: 3px;
    }

    .icon-menu {
      width: 24px;
      height: 24px;
      margin-right: 12px;
      filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
    }
  }

  .tracking-item {
    background: linear-gradient(145deg, #ffffff 0%, #fafbfc 100%);
    border-radius: 16px;
    padding: 24px;
    margin-bottom: 20px;
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
    border: 1px solid rgba(0, 0, 0, 0.06);
    transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;
    overflow: hidden;

    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 4px;
      background: linear-gradient(90deg, #1890ff 0%, #36cfc9 100%);
      opacity: 0;
      transition: opacity 0.3s ease;
    }

    &:hover {
      box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
      transform: translateY(-4px);
      border-color: rgba(24, 144, 255, 0.2);

      &::before {
        opacity: 1;
      }
    }

    .item-title {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;

      span:first-child {
        font-size: 16px;
        font-weight: 700;
        color: #262626;
        letter-spacing: 0.2px;
      }

      .target {
        font-size: 13px;
        color: #8c8c8c;
        background: linear-gradient(135deg, #fafafa 0%, #f5f5f5 100%);
        padding: 6px 14px;
        border-radius: 8px;
        font-weight: 600;
        border: 1px solid #e8e8e8;
      }
    }

    .progress-bar {
      position: relative;
      height: 32px;
      background: linear-gradient(135deg, #f0f0f0 0%, #e8e8e8 100%);
      border-radius: 16px;
      overflow: hidden;
      margin-bottom: 20px;
      box-shadow: inset 0 3px 6px rgba(0, 0, 0, 0.08);
      border: 1px solid #e0e0e0;

      .bar {
        position: absolute;
        height: 100%;
        transition: width 0.5s cubic-bezier(0.4, 0, 0.2, 1);

        &.base {
          background: linear-gradient(90deg, #1890ff 0%, #36cfc9 100%);
          left: 0;
          top: 0;
          box-shadow: 0 3px 10px rgba(24, 144, 255, 0.4);
        }

        &.exceed {
          background: linear-gradient(90deg, #ff4d4f 0%, #ff7875 100%);
          left: 100%;
          top: 0;
          box-shadow: 0 3px 10px rgba(255, 77, 79, 0.4);
        }
      }

      .progress-text {
        position: absolute;
        left: 50%;
        top: 50%;
        transform: translate(-50%, -50%);
        font-size: 14px;
        font-weight: 800;
        color: #fff;
        text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
        letter-spacing: 0.5px;
      }
    }

    .calibration-info {
      display: flex;
      justify-content: space-between;
      align-items: center;

      span {
        font-size: 14px;
        color: #595959;
        font-weight: 600;
        letter-spacing: 0.2px;
      }

      .calibration-btn {
        padding: 8px 20px;
        background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
        border: none;
        border-radius: 8px;
        color: #fff;
        cursor: pointer;
        font-size: 14px;
        font-weight: 700;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        box-shadow: 0 3px 10px rgba(24, 144, 255, 0.3);
        letter-spacing: 0.3px;

        &:hover {
          background: linear-gradient(135deg, #40a9ff 0%, #1890ff 100%);
          box-shadow: 0 5px 14px rgba(24, 144, 255, 0.45);
          transform: translateY(-2px);
        }

        &:active {
          transform: translateY(-1px) scale(0.98);
        }
      }
    }
  }
}

// 响应式布局
@media (max-width: 1200px) {
  .parent-container {
    flex-direction: column;
  }

  .prediction-card {
    flex: 1;
  }

  .yield-indicator {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .production-plan-page {
    padding: 16px;
  }

  .yield-indicator {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .cards-container {
    flex-direction: column;
  }

  .plan-status-wrapper,
  .crop-variety-wrapper,
  .yield-adjust-wrapper {
    padding: 16px;
  }

  .variety-buttons {
    gap: 8px;

    .variety-btn {
      padding: 8px 14px;
      font-size: 13px;
    }
  }
}

@media (max-width: 480px) {
  .production-plan-page {
    padding: 12px;
  }

  .plan-status-wrapper,
  .crop-variety-wrapper,
  .yield-adjust-wrapper {
    padding: 14px;
  }

  .yield-adjust-wrapper {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .adjust-btn {
    width: 100%;
    justify-content: center;
  }
}

// 弹窗样式
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.45);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease;

  .modal {
    background: #ffffff;
    border-radius: 16px;
    box-shadow: 0 12px 48px rgba(0, 0, 0, 0.15);
    width: 90%;
    max-width: 480px;
    max-height: 90vh;
    overflow-y: auto;
    animation: slideUp 0.3s ease;

    .modal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20px 24px;
      border-bottom: 1px solid #f0f0f0;
      background: linear-gradient(135deg, #fafafa 0%, #ffffff 100%);
      border-radius: 16px 16px 0 0;

      h4 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
        color: #262626;
        letter-spacing: 0.3px;
      }

      .close-btn {
        width: 32px;
        height: 32px;
        border: none;
        background: #f5f5f5;
        border-radius: 50%;
        font-size: 24px;
        color: #8c8c8c;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: all 0.3s ease;
        line-height: 1;

        &:hover {
          background: #ff4d4f;
          color: #ffffff;
          transform: rotate(90deg);
        }
      }
    }

    .modal-body {
      padding: 24px;
      display: flex;
      flex-direction: column;
      gap: 16px;

      .year-input-item,
      .param-input-item {
        display: flex;
        flex-direction: column;
        gap: 8px;

        label {
          font-size: 14px;
          font-weight: 500;
          color: #595959;
          letter-spacing: 0.2px;
        }

        .year-input,
        .param-input {
          padding: 12px 16px;
          border: 1.5px solid #d9d9d9;
          border-radius: 8px;
          font-size: 14px;
          color: #262626;
          background: #ffffff;
          transition: all 0.3s ease;

          &:hover {
            border-color: #40a9ff;
            box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
          }

          &:focus {
            outline: none;
            border-color: #1890ff;
            box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.15);
          }

          &::placeholder {
            color: #bfbfbf;
            font-size: 13px;
          }
        }
      }
    }

    .modal-footer {
      padding: 16px 24px;
      border-top: 1px solid #f0f0f0;
      display: flex;
      justify-content: flex-end;
      gap: 12px;
      background: #fafafa;
      border-radius: 0 0 16px 16px;

      .cancel-btn {
        padding: 10px 24px;
        background: #ffffff;
        border: 1.5px solid #d9d9d9;
        border-radius: 8px;
        color: #595959;
        font-size: 14px;
        font-weight: 500;
        cursor: pointer;
        transition: all 0.3s ease;

        &:hover {
          border-color: #8c8c8c;
          color: #262626;
          background: #f5f5f5;
        }

        &:active {
          transform: scale(0.98);
        }
      }

      .confirm-btn {
        padding: 10px 24px;
        background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
        border: none;
        border-radius: 8px;
        color: #ffffff;
        font-size: 14px;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.3s ease;
        box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);

        &:hover {
          background: linear-gradient(135deg, #40a9ff 0%, #1890ff 100%);
          box-shadow: 0 4px 12px rgba(24, 144, 255, 0.4);
          transform: translateY(-2px);
        }

        &:active {
          transform: translateY(0) scale(0.98);
        }
      }
    }
  }
}

// 弹窗动画
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
  }
}

// 校准对话框样式
.calibration-content {
  .calibration-info {
    background: linear-gradient(135deg, #f0f9ff 0%, #e6f7ff 100%);
    border-radius: 12px;
    padding: 20px;
    margin-bottom: 16px;

    .info-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 0;
      border-bottom: 1px solid rgba(24, 144, 255, 0.1);

      &:last-child {
        border-bottom: none;
        padding-bottom: 0;
      }

      &:first-child {
        padding-top: 0;
      }

      .info-label {
        font-size: 14px;
        color: #595959;
        font-weight: 500;
      }

      .info-value {
        font-size: 16px;
        color: #1890ff;
        font-weight: 700;

        &.completed {
          color: #52c41a;
        }
      }
    }
  }
}

// 生成计划按钮样式
.generate-plan-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  padding: 40px 20px;

  .generate-plan-card {
    background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
    border-radius: 20px;
    padding: 48px 60px;
    text-align: center;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
    border: 2px solid rgba(24, 144, 255, 0.1);
    transition: all 0.3s ease;
    max-width: 500px;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 12px 32px rgba(24, 144, 255, 0.15);
      border-color: rgba(24, 144, 255, 0.2);
    }

    .generate-plan-icon {
      font-size: 64px;
      margin-bottom: 24px;
      animation: bounce 2s infinite;
    }

    .generate-plan-title {
      font-size: 28px;
      font-weight: 700;
      color: #1a1a1a;
      margin-bottom: 16px;
      letter-spacing: 0.5px;
    }

    .generate-plan-desc {
      font-size: 15px;
      color: #8c8c8c;
      margin-bottom: 32px;
      line-height: 1.6;
    }

    .generate-btn {
      background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
      color: white;
      border: none;
      border-radius: 12px;
      padding: 14px 40px;
      font-size: 16px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s ease;
      box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
      display: inline-flex;
      align-items: center;
      gap: 8px;

      &:hover:not(:disabled) {
        background: linear-gradient(135deg, #40a9ff 0%, #1890ff 100%);
        box-shadow: 0 6px 16px rgba(24, 144, 255, 0.4);
        transform: translateY(-2px);
      }

      &:active:not(:disabled) {
        transform: translateY(0) scale(0.98);
      }

      &:disabled {
        background: #d9d9d9;
        color: #999;
        cursor: not-allowed;
        box-shadow: none;
      }

      .btn-icon {
        font-size: 18px;
      }
    }
  }
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

// 生成的计划详情样式
.generated-plan-wrapper {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(0, 0, 0, 0.05);

  .generated-plan-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 32px;
    padding-bottom: 20px;
    border-bottom: 2px solid #e8e8e8;

    h3 {
      font-size: 24px;
      font-weight: 700;
      color: #1a1a1a;
      margin: 0;
    }

    .header-actions {
      display: flex;
      gap: 12px;

      .cancel-plan-btn {
        background: #f5f5f5;
        color: #595959;
        border: none;
        border-radius: 8px;
        padding: 10px 24px;
        font-size: 14px;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.3s ease;

        &:hover {
          background: #e8e8e8;
          color: #262626;
        }
      }

      .confirm-plan-btn {
        background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
        color: white;
        border: none;
        border-radius: 8px;
        padding: 10px 24px;
        font-size: 14px;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.3s ease;
        box-shadow: 0 2px 8px rgba(82, 196, 26, 0.3);

        &:hover {
          background: linear-gradient(135deg, #73d13d 0%, #52c41a 100%);
          box-shadow: 0 4px 12px rgba(82, 196, 26, 0.4);
          transform: translateY(-2px);
        }
      }
    }
  }

  .generated-plan-content {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 24px;

    .plan-section {
      background: #ffffff;
      border-radius: 12px;
      padding: 24px;
      border: 1px solid #e8e8e8;
      transition: all 0.3s ease;

      &:hover {
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
        border-color: #d9d9d9;
      }

      .section-title {
        font-size: 18px;
        font-weight: 700;
        color: #1a1a1a;
        margin-bottom: 20px;
        padding-bottom: 12px;
        border-bottom: 2px solid #1890ff;
        display: inline-block;
      }

      .plan-form {
        display: flex;
        flex-direction: column;
        gap: 16px;

        .form-item {
          display: flex;
          flex-direction: column;
          gap: 8px;

          .form-label {
            font-size: 14px;
            font-weight: 600;
            color: #595959;
          }

          .form-input {
            padding: 10px 14px;
            border: 1px solid #d9d9d9;
            border-radius: 8px;
            font-size: 14px;
            color: #262626;
            transition: all 0.3s ease;
            background: #fafafa;

            &:focus {
              outline: none;
              border-color: #1890ff;
              background: #ffffff;
              box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.1);
            }

            &:disabled {
              background: #f5f5f5;
              color: #8c8c8c;
              cursor: not-allowed;
            }
          }
        }
      }
    }
  }
}
</style>
