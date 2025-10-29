<!-- src/components/GlobalComponents/GrowthStageTag.vue -->
<template>
  <!-- 组件内容（根据你的需求编写，示例为生育阶段选择器） -->
  <div class="growth-stage-tag-container">
    <!-- 两个下拉框（基地+地块）+ 生育阶段标签 -->
    <div class="dropdown-group">
      <el-select v-model="selectedBase" placeholder="请选择基地" class="base-select">
        <el-option label="基地a" value="基地a"></el-option>
        <el-option label="基地b" value="基地b"></el-option>
      </el-select>
      <el-select v-model="selectedPlot" placeholder="请选择地块" class="plot-select">
        <el-option label="地块A" value="地块A"></el-option>
        <el-option label="地块B" value="地块B"></el-option>
      </el-select>
    </div>
    <div class="stage-tag-group">
      <span class="stage-tag" :class="{ active: activeStage === stage }" @click="activeStage = stage" v-for="stage in stageList" :key="stage">
        {{ stage }}
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { ElSelect, ElOption } from 'element-plus';

// 下拉框数据
const selectedBase = ref('基地a');
const selectedPlot = ref('地块A');

// 生育阶段数据（可从接口获取，此处为示例）
const stageList = ref([
  '未播种', '已播种', '苗期', '蕾薹期', '开花期', '角果成熟期', '收获与整地'
]);
const activeStage = ref('苗期');
</script>

<style scoped lang="less">
.growth-stage-tag-container {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px 16px;
  width: 100%;
  box-sizing: border-box;

  .dropdown-group {
    display: flex;
    flex-direction: column;
    gap: 8px;

    .base-select, .plot-select {
      width: 120px;
    }
  }

  .stage-tag-group {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;

    .stage-tag {
      padding: 4px 12px;
      border-radius: 16px;
      background-color: #f0f0f0;
      font-size: 14px;
      cursor: pointer;
      transition: all 0.2s;

      &.active {
        background-color: #1890ff;
        color: #fff;
      }

      &:hover {
        background-color: #e6f7ff;
      }
    }
  }
}
</style>
