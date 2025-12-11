<template>
  <div class="plan-status-wrapper">
    <!-- 提示标签 -->
    <span class="status-label">生产计划状态</span>

    <!-- 状态内容区 -->
    <div class="status-content">
      <!-- 有计划时显示 -->
      <div v-if="hasProductionPlan" class="status-info existing-plan">
        <span class="status-text">当前地块生产计划已存在，以下为计划详情</span>
        <button class="modify-btn" @click="handleModifyPlan">修改计划</button>
      </div>

      <!-- 无计划时显示 -->
      <div v-else class="status-info no-plan">
        <span class="status-text">当前地块暂无生产计划, 点击【生成计划】按钮可创建新计划</span>
        <button class="generate-btn" @click="handleGeneratePlan">生成计划</button>
      </div>
    </div>
  </div>

</template>

<script>
import { useSelectStore,usePlanStore } from '/src/store/selectStore'
import { getPlotPlanByPlotId } from '../base.api'

export default {
  data() {
    return {
      hasProductionPlan: false,
      productionPlan: null
    }
  },

  computed: {
    currentPlotId() {
      return useSelectStore().selectedPlot?.plotId
    }
  },

  watch: {
    currentPlotId: {
      immediate: true,
      async handler(newPlotId) {
        if (!newPlotId) {
          this.hasProductionPlan = false
          this.productionPlan = null
          return
        }
        await this.checkProductionPlan(newPlotId)
      }
    }
  },

  methods: {
    async checkProductionPlan(plotId) {
      const planStore = usePlanStore()

      try {
        // ① 查看缓存（undefined 代表未查过）
        const cached = planStore.getPlan(plotId)

        if (cached !== undefined) {
          console.log("📌 使用缓存计划数据：", cached)
          this.productionPlan = cached
          this.hasProductionPlan = cached !== null
          return
        }

        // ② 未查询过 → 调接口
        const res = await getPlotPlanByPlotId(plotId)
        console.log("📡 后端返回生产计划：", res)

        // ⭐ 正确从 result 中取计划数据
        const plan = res|| null

        this.productionPlan = plan
        this.hasProductionPlan = plan !== null

        // ③ 写入缓存
        planStore.setPlan(plotId, plan)

      } catch (err) {
        console.error("❌ 查询生产计划失败：", err)

        this.productionPlan = null
        this.hasProductionPlan = false

        // 防止重复查
        planStore.setPlan(plotId, null)
      }
    },

    // 生成新计划
    handleGeneratePlan() {
      this.$emit("generate-plan", this.currentPlotId)
    },

    // 修改已有计划
    handleModifyPlan() {
      this.$emit("modify-plan", {
        plotId: this.currentPlotId,
        planData: this.productionPlan
      })
    }
  }
}
</script>

<style>
/* 样式部分保持不变 */
.plan-status-wrapper {
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  padding: 1px;
  display: flex;
  align-items: center;
  gap: 16px;
  background-color: white;
  width: 100%;
}

.status-label {
  font-weight: bold;
  color: #333;
  white-space: nowrap;
  padding-left: 5px;
}

.status-content {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  flex-wrap: wrap;
  flex: 1;
}

.status-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.status-text {
  color: #666;
  line-height: 1.5;
}

.existing-plan .status-text {
  color: #22c55e;
}

.no-plan .status-text {
  color: #f59e0b;
}

.generate-btn {
  padding: 6px 14px;
  border: 1px solid #22c55e;
  border-radius: 10px;
  background-color: #22c55e;
  color: white;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.generate-btn:hover {
  background-color: #16a34a;
  border-color: #16a34a;
}
.modify-btn {
  padding: 6px 14px;
  border: 1px solid #3b82f6;
  border-radius: 10px;
  background-color: #3b82f6;
  color: white;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.modify-btn:hover {
  background-color: #2563eb;
  border-color: #2563eb;
}
</style>
