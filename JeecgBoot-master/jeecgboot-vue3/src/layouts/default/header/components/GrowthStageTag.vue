<template>
  <div class="stage-selector-container">
    <!-- 左侧下拉框区域 -->
<!--    <div class="dropdowns-area">-->
<!--      <el-select v-model="selectedBase" placeholder="请选择基地">-->
<!--        <el-option label="基地a" value="基地a"></el-option>-->
<!--        <el-option label="基地b" value="基地b"></el-option>-->
<!--      </el-select>-->
<!--      <el-select v-model="selectedPlot" placeholder="请选择地块">-->
<!--        <el-option label="地块A" value="地块A"></el-option>-->
<!--        <el-option label="地块B" value="地块B"></el-option>-->
<!--      </el-select>-->
<!--    </div>-->
<!--    &lt;!&ndash; 生育阶段标签区域 &ndash;&gt;-->
<!--    <div class="stages-area">-->
<!--      <span class="stage-tag">未播种</span>-->
<!--      <span class="stage-tag">已播种</span>-->
<!--      <span class="stage-tag">苗期</span>-->
<!--      <span class="stage-tag">蕾薹期</span>-->
<!--      <span class="stage-tag">开花期</span>-->
<!--      <span class="stage-tag">角果成熟期</span>-->
<!--      <span class="stage-tag">收获与整地</span>-->
<!--    </div>-->
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { ElSelect, ElOption } from 'element-plus';

const selectedBase = ref('基地a');
const selectedPlot = ref('地块A');
</script>

<style scoped lang="less">
.stage-selector-container {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px;
  background-color: #fff;
  border: 1px solid #eaeaea;
  border-radius: 4px;
  width:100%;
  height:50px;
  background-color: greenyellow;
}

.dropdowns-area {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

:deep(.el-select) {
  width: 120px;
}

.stages-area {
  display: flex;
  gap: 4px;
  flex-wrap: nowrap;
}

.stage-tag {
  display: inline-block;
  padding: 6px 12px;
  background-color: #f0f0f0;
  border-radius: 4px;
  font-size: 14px;
  color: #333;
}
</style>
