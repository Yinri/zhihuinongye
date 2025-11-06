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
import {useSelectStore} from '/@/store/selectStore'; // 全局状态
import {getPlotPlanByPlotId} from '../base.api';

export default {
  data() {
    return {
      hasProductionPlan: false // 本地维护是否有计划的状态
    };
  },
  computed: {
    // 获取当前选中地块的ID
    currentPlotId() {
      return useSelectStore().selectedPlot?.plotId;
    }
  },
  watch: {
    // 当选中地块ID变化时重新查询计划
    currentPlotId: {
      immediate: true, // 初始化时立即执行
      handler(newId) {
        if (newId) {
          this.checkProductionPlan(newId);
        } else {
          this.hasProductionPlan = false;
        }
      }
    }
  },
  methods: {
    // 检查当前地块是否有生产计划
    async checkProductionPlan(plotId) {
      try {
        // 调用API查询生产计划
        const response = await getPlotPlanByPlotId(plotId);
        // 根据实际接口返回结构判断是否有计划
        this.productionPlan = response.data;
        // 有计划（data不为null）则设为true
        this.hasProductionPlan = !this.productionPlan;
      } catch (error) {
        console.error('查询生产计划失败:', error);
        this.hasProductionPlan = false;
        this.productionPlan = null;
      }
    },
    // 生成计划按钮点击事件
    handleGeneratePlan() {
      // 触发父组件的生成计划方法，可传递当前地块ID
      this.$emit('generate-plan', this.currentPlotId);
    },
    // 修改计划按钮点击事件
    handleModifyPlan() {
      // 触发父组件的修改计划方法，传递当前地块ID和现有计划数据
      this.$emit('modify-plan', {
        plotId: this.currentPlotId,
        planData: this.productionPlan
      });
    }
  }
};
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
