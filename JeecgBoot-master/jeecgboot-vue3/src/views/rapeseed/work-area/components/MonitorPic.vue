<script setup lang="ts">
import { ref } from 'vue';

const isStatsVisible = ref(false);
const toggleStats = () => {
  isStatsVisible.value = !isStatsVisible.value;
};
</script>

<template>
  <div class="aerial-container">
    <!-- 航拍图片 -->
    <img src="../icons/image1.png" alt="基地土壤航拍图" class="aerial-image">

    <!-- 悬浮按钮（打开/关闭共用） -->
    <button class="float-btn" @click="toggleStats">
      <i class="icon">{{ isStatsVisible ? '-' : '+' }}</i>
    </button>

    <!-- 播种面积统计弹窗 -->
    <div v-if="isStatsVisible" class="stats-panel">
      <div class="panel-header">
        播种面积实时统计
      </div>
      <div class="panel-body">
        <div class="stats-item">
          <span>已播种</span>
          <span class="value sowed">XX.XX ㎡</span>
        </div>
        <div class="stats-item">
          <span>未播种</span>
          <span class="value unsowed">XX.XX ㎡</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
.aerial-container {
  position: relative;
  width: 100%;
}

.aerial-image {
  width: 100%;
  display: block;
  margin-top: 5px;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.float-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: #fff;
  border: 1px solid #d9d9d9;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  transition: all 0.2s;

  &:hover {
    background-color: #f5f5f5;
  }
}

.stats-panel {
  position: absolute;
  top: 40px;
  right: 0;
  width: 180px;
  background-color: #fff;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  padding: 12px;
  z-index: 20;
}

.panel-header {
  font-size: 14px;
  font-weight: bold;
  color: #333;
  margin-bottom: 12px;
  border-bottom: 1px solid #eee;
  padding-bottom: 8px;
}

.stats-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 13px;
  color: #666;

  .value {
    &.sowed {
      color: #52c41a;
    }
    &.unsowed {
      color: #faad14;
    }
  }
}
</style>
