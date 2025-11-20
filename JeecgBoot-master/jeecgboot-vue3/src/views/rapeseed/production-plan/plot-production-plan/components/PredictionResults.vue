<template>
  <div class="yield-indicator">
    <!-- 目标产量卡片 -->
    <div class="input-card">
      <div class="card-header">目标产量</div>
      <div class="main-value">
        {{ targetYield !== null ? targetYield.toFixed(1) : '暂无数据' }}
      </div>
      <div class="unit">公斤 / 亩</div>
    </div>

    <!-- 种子计划投入卡片（修复 toFixed 报错：补充 undefined 判断） -->
    <div class="input-card">
      <div class="card-header">种子计划投入</div>
      <div class="main-value">
        {{ seedInput != null ? seedInput.toFixed(3) : '0.00' }} <!-- 关键修复：用 != null 同时判断 null/undefined -->
      </div>
      <div class="unit">kg / 亩</div>
      <div v-if="missingSeedParam" class="missing-tip">
        {{ missingSeedParam }}
      </div>
    </div>

    <!-- 肥料计划投入卡片 -->
    <div class="input-card">
      <div class="card-header">肥料计划投入</div>
      <div class="main-value">
        {{ fertilizerInput || '0.0' }}
      </div>
      <div class="unit">kg / 亩</div>
    </div>

    <!-- 农药计划投入卡片 -->
    <div class="input-card">
      <div class="card-header">农药计划投入</div>
      <div class="main-value">
        {{ pesticideInput || '0.0' }}
      </div>
      <div class="unit">kg / 亩</div>
    </div>

  </div>
</template>

<script>
import { useCropVarietyStore } from '@/store/selectStore';
import { storeToRefs } from 'pinia';
// 替换为你的实际接口路径（必须修改！）
import { getSeedParamsByVarietyId } from '../base.api';

export default {
  name: 'YieldIndicator',
  setup() {
    const cropStore = useCropVarietyStore();
    const { yieldCalcData, selected, seedParams } = storeToRefs(cropStore);
    return {
      cropStore, // 关键修复：返回 cropStore 实例，组件中可直接访问
      yieldCalcData,
      selected,
      seedParams
    };
  },
  data() {
    return {
      fertilizerInput: 120.0,
      pesticideInput: 68.3,
      varietyDetail: null,
      missingSeedParam: ''
    };
  },
  computed: {
    avgThreeYearYield() {
      return this.yieldCalcData.avgThreeYearYield ? Number(this.yieldCalcData.avgThreeYearYield) : null;
    },
    increaseRate() {
      return this.yieldCalcData.increaseRate ? Number(this.yieldCalcData.increaseRate) / 100 : 0;
    },
    targetYield() {
      if (this.avgThreeYearYield === null || isNaN(this.avgThreeYearYield)) {
        return null;
      }
      return this.avgThreeYearYield * (1 + this.increaseRate);
    },
    seedInput() {
      this.missingSeedParam = '';

      const selectedVariety = this.selected;
      console.log(selectedVariety);
      const userSeedParams = this.seedParams.value;
      const targetYield = this.targetYield;
      const varietyDetail = this.varietyDetail;

      // 逐步校验参数
      if (!selectedVariety?.id) {
        this.missingSeedParam = '请先选择品种';
        return null;
      }
      if (!varietyDetail) {
        this.missingSeedParam = '品种参数加载中...';
        return null;
      }
      if (targetYield === null) {
        this.missingSeedParam = '目标产量未计算';
        return null;
      }
      // 关键修改：字段名对应接口返回
      const harvestCoefficient = userSeedParams?.harvestCoefficient ?? varietyDetail.harvestCoefficient;
      const settingRate = userSeedParams?.settingRate ?? varietyDetail.seedSettingRate;
      const seedlingRate = userSeedParams?.seedlingRate ?? varietyDetail.seedlingSurvivalRate;

// 新增：精准校验缺失字段
      const missingFields = [];
      if (!harvestCoefficient || harvestCoefficient <= 0) missingFields.push('收获系数');
      if (!settingRate || settingRate <= 0) missingFields.push('结实率');
      if (!seedlingRate || seedlingRate <= 0) missingFields.push('田间保苗率');

      if (missingFields.length > 0) {
        this.missingSeedParam = `请录入：${missingFields.join('、')}`;
        return null;
      }

// 新增：提取计算参数（包含优先级逻辑）
      const params = {
        targetYieldKg: targetYield,
        // 品种核心参数（接口返回，不变）
        singlePlantPods: Number(varietyDetail.singlePlantPods) || 0,
        seedsPerPod: Number(varietyDetail.seedsPerPod) || 0,
        thousandGrainWeight: Number(varietyDetail.thousandGrainWeight) || 0,
        germinationRate: Number(varietyDetail.germinationRate) || 0,
        // 种子计算参数（优先Store，其次接口）
        harvestCoefficient: Number(harvestCoefficient) || 0,
        settingRate: Number(settingRate) || 0,
        seedlingRate: Number(seedlingRate) || 0
      };

      // 校验参数有效性
      if (
        params.singlePlantPods <= 0 ||
        params.seedsPerPod <= 0 ||
        params.thousandGrainWeight <= 0 ||
        params.germinationRate <= 0 ||
        params.harvestCoefficient <= 0 ||
        params.settingRate <= 0 ||
        params.seedlingRate <= 0
      ) {
        this.missingSeedParam = '品种参数无效，请检查';
        return null;
      }

      // 计算种子投入量
      try {
        const theoreticalSeedlings = (params.targetYieldKg * 1000) / (
          params.singlePlantPods *
          params.seedsPerPod *
          params.thousandGrainWeight *
          (params.settingRate / 100) *
          params.harvestCoefficient
        );

        const seedInputKg = (theoreticalSeedlings * params.thousandGrainWeight) / (
          (params.germinationRate / 100) *
          (params.seedlingRate / 100) *
          1000
        );
        console.log('seedInputKg', seedInputKg);
        return seedInputKg > 0 ? seedInputKg : null;
      } catch (error) {
        this.missingSeedParam = '计算失败，请检查参数';
        return null;
      }
    }
  },
  watch: {
    // 监听品种变化，重置参数和提示状态
    'selected.id'(newVarietyId) {
      // 修复：通过返回的 cropStore 调用方法
      this.cropStore.setSeedParamWarned(false); // 重置提示标记

      if (newVarietyId) {
        this.fetchVarietyDetail(newVarietyId);
      } else {
        this.varietyDetail = null;
        this.missingSeedParam = '';
      }
    }
  },
  methods: {
    async fetchVarietyDetail(varietyId) {
      try {
        this.missingSeedParam = '品种参数加载中...';
        // 替换为你的实际接口（必须确保接口能返回以下字段）
        const res = await getSeedParamsByVarietyId(varietyId);
        console.log(res,"99999999999999999999");

        // 新增：接口返回的计算参数同步到Store
        if (res.harvestCoefficient || res.settingRate || res.seedlingRate) {
          this.cropStore.updateSeedParams({
            harvestCoefficient: res.harvestCoefficient,
            settingRate: res.seedSettingRate,
            seedlingRate: res.seedlingSurvivalRate,
            id: res.id // 接口返回的参数记录ID（用于修改保存）
          });
        }
        this.varietyDetail = res;
      } catch (error) {
        this.missingSeedParam = '品种参数加载失败';
        this.varietyDetail = null;
        console.error('接口调用失败：', error); // 便于排查接口问题
      }
    }
  }
};
</script>

<style scoped>
.yield-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  margin-top: 5px;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  padding: 20px;
  background-color: #fff;
  height: 200px;
}

.input-card {
  min-width: 180px;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 16px;
  text-align: center;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  position: relative;
}

.input-card:hover {
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.card-header {
  font-size: 14px;
  color: #666;
  font-weight: 500;
  margin-bottom: 8px;
}

.main-value {
  font-size: 24px;
  font-weight: bold;
  color: #2f5496;
}

.unit {
  font-size: 14px;
  color: #666;
  margin-top: 4px;
}

.missing-tip {
  position: absolute;
  bottom: 5px;
  left: 0;
  right: 0;
  font-size: 12px;
  color: #f5222d;
  line-height: 1.2;
  padding: 0 5px;
}

.warning-tip {
  display: flex;
  align-items: center;
  color: #f59e0b;
  font-size: 14px;
  justify-content: center;
  margin-top: 8px;
}

.warning-icon {
  margin-right: 4px;
}

.warn-label {
  font-size: 12px;
}
</style>
