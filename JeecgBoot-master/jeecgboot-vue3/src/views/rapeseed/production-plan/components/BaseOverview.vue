<template>
  <div class="soil-fertility-overview">
    <!-- 左上角标题 -->
    <div class="section-title">
      <img src="../icons/icon3.svg" class="icon-soil">
      基地土壤肥力总览
    </div>

    <!-- 航拍图片 -->
    <img src="../icons/image1.jpg" alt="基地土壤航拍图" class="aerial-image">

    <!-- 图片下方的信息区域（数据列表 + 图例） -->
    <div class="bottom-info">
      <div class="info-list">
        <div class="info-item">
          <i class="icon-area">📊</i>
          <span>地块总面积（1250 亩）</span>
        </div>
        <div class="info-item">
          <i class="icon-fertility">🌱</i>
          <span>当前土壤肥力（中等）</span>
        </div>
        <div class="info-item">
          <i class="icon-plant">🌾</i>
          <span>当前种植面积（1200 亩）</span>
        </div>
      </div>

      <div class="legend">
        <span class="legend-item">
          <span class="legend-dot high"></span> 高肥力
        </span>
        <span class="legend-item">
          <span class="legend-dot low"></span> 低肥力
        </span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SoilFertilityOverview'
}
</script>

<style scoped>
.soil-fertility-overview {
  width: 60%; /* 组件占页面宽度60% */
  border-radius: 8px;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  /* 关键：使用块级布局，让子元素垂直排列 */
  display: block;
  margin-top: 5px;
  align-items: center;
  justify-content: space-between;
  border: 1px solid #d9d9d9;
  padding: 8px 8px;
}

/* 左上角标题 */
.section-title {
  display: flex;
  align-items: center;
  color: #333;
  font-size: 14px; /* 标题稍大一点 */
  font-weight: bold;
}

.icon-soil {
  width: 18px;
  height: 18px;
  margin-right: 8px;
}

/* 航拍图片 */
.aerial-image {
  width: 100%; /* 图片占满组件宽度 */
  border-radius: 4px;
  /* 保持图片比例，避免拉伸 */
  object-fit: cover;
}

/* 图片下方的信息容器 */
.bottom-info {
  display: flex;
  justify-content: space-between; /* 列表居左，图例居右 */
  align-items: flex-start; /* 顶部对齐 */
  width: 100%;
}

/* 数据列表 */
.info-list {
  display: flex;
  flex-direction: column;
}

.info-item {
  display: flex;
  align-items: center;
  color: black;
  font-size: 12px;
  font-weight:bold;
}

/* 图标样式（可替换为实际图标） */
.icon-area, .icon-fertility, .icon-plant {
  margin-right: 8px;
  font-size: 16px;
}

/* 图例 */
.legend {
  display: flex;
  flex-direction: column; /* 图例垂直排列 */
  gap: 8px; /* 图例项之间的间距 */
}

.legend-item {
  display: flex;
  align-items: center;
  color: #666;
  font-size: 14px;
}

.legend-dot {
  display: inline-block;
  width: 12px;
  height: 12px;
  margin-right: 6px;
}

.high {
  background-color: #E53935; /* 高肥力红色 */
}

.low {
  background-color: #4285F4; /* 低肥力蓝色 */
}
</style>
