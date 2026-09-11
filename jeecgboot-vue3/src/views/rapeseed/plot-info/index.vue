<template>
  <div class="plot-info-page">
    <!-- 地图区域 -->
    <a-card :bordered="false" class="map-card">
      <template #title>
        <span class="map-title">
          <Icon icon="ant-design:environment-outlined" />
          地图区域
        </span>
      </template>
      <div class="map-wrapper">
        <TiandituMap
          :enableManagement="true"
          :showSensors="false"
        />
      </div>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  // 引入基础依赖
  import { ref, onMounted } from 'vue';
  import { Icon } from '/@/components/Icon';
  import TiandituMap from '/@/components/TiandituMap/index.vue';
  
  // 基础响应式数据
  const loading = ref(false);
  
  // 组件挂载时的初始化
  onMounted(() => {
    console.log('地块管理页面已初始化');
  });
</script>

<style lang="less" scoped>
  .plot-info-page {
    padding: 0;
    background-color: #f0f2f5;
    height: 100vh;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }

  .map-card {
    margin-top: 0;
    height: 100vh;
    display: flex;
    flex-direction: column;
    
    :deep(.ant-card-body) {
      padding: 0;
      flex: 1;
      display: flex;
      flex-direction: column;
    }
    
    .map-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 16px;
      font-weight: 500;
    }
    
    .map-wrapper {
      width: 100%;
      height: 100%;
      flex: 1;
      display: flex;
      justify-content: center;
      align-items: stretch;
      overflow: hidden;
      border-radius: 4px;
      background-color: #f5f5f5;
    }
  }
</style>
