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

      <!-- 种子计划投入卡片 -->
      <div class="input-card">
        <div class="card-header">种子计划投入</div>
        <div class="main-value">
          {{ seedInput || '0.0' }}
        </div>
        <div class="unit">kg / 亩</div>
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

export default {
  name: 'YieldIndicator',
  setup() {
    // 从Pinia获取存储的平均单产和递增率
    const cropStore = useCropVarietyStore();
    const { yieldCalcData } = storeToRefs(cropStore);
    return {
      yieldCalcData
    };
  },
  data() {
    return {
      // 投入类示例数据（可替换为接口请求数据或计算值）
      seedInput: 85.5,    // 种子投入示例值
      fertilizerInput: 120.0, // 肥料投入示例值
      pesticideInput: 68.3   // 农药投入示例值
    };
  },
  computed: {
    // 前三年平均单产（从Pinia获取）
    avgThreeYearYield() {
      return this.yieldCalcData.avgThreeYearYield ? Number(this.yieldCalcData.avgThreeYearYield) : null;
    },
    // 递增率（从Pinia获取，转为小数）
    increaseRate() {
      return this.yieldCalcData.increaseRate ? Number(this.yieldCalcData.increaseRate) / 100 : 0;
    },
    // 计算目标产量：前三年平均单产 × (1 + 递增率)
    targetYield() {
      if (this.avgThreeYearYield === null || isNaN(this.avgThreeYearYield)) {
        return null;
      }
      return this.avgThreeYearYield * (1 + this.increaseRate);
    },
    // 是否显示警告（目标产量超过平均单产时）
    showWarning() {
      return this.targetYield !== null && this.avgThreeYearYield !== null && this.targetYield > this.avgThreeYearYield;
    }
  }
};
</script>

<style scoped>

.yield-indicator {
  display: flex; /* 横向排列 */
  align-items: center; /* 垂直居中 */
  justify-content: center; /* 水平居中 */
  gap: 20px; /* 卡片之间间距 */
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  margin-top: 5px;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  padding: 20px; /* 增加内边距 */
  background-color: #fff;
  height:200px;
  /* 移除原有的flex-direction: column和gap: 20px */
}



/* 投入卡片样式（与目标产量卡片一致） */
.input-card {
  min-width: 180px;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 16px;
  text-align: center;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.input-card:hover {
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

/* 卡片标题样式 */
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

/* 警告提示样式 */
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
