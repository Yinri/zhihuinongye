<template>
  <div class="yield-indicator">
    <div class="yield-value">
      <div class="main-value">200-220</div>
      <div class="unit">公斤 / 亩</div>
    </div>
    <div class="yield-ranges">
      <!-- 品种潜力范围（文字在进度条左侧） -->
      <div class="range-item">
        <div class="range-label">品种潜力范围 (160-190kg / 亩)</div>
        <div class="range-bar-container">
          <div class="range-bar">
            <div class="bar base-bar" :style="{ width: baseWidth }"></div>
          </div>
        </div>
      </div>

      <!-- 当前目标范围（文字在进度条左侧） -->
      <div class="range-item">
        <div class="range-label">当前目标范围 (200-220kg / 亩)</div>
        <div class="range-bar-container">
          <div class="range-bar composite-bar">
            <!-- 与基础范围重叠部分 -->
            <div class="bar overlap-bar" :style="{ width: overlapWidth }"></div>
            <!-- 超出基础范围的部分（红色） -->
            <div class="bar exceed-bar" :style="{ width: exceedWidth }"></div>
          </div>
        </div>
      </div>

      <div class="warning-tip">
        <i class="warning-icon">⚠️</i>
        <span class="warn-label">当前目标略高于品种历史最高产量</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'YieldIndicator',
  data() {
    return {
      // 基础范围最大值（品种潜力上限）
      baseMax: 190,
      // 当前目标最大值
      targetMax: 220,
      // 计算基准（取最大范围值）
      totalBase: 220
    }
  },
  computed: {
    // 基础进度条宽度
    baseWidth() {
      return `${(this.baseMax / this.totalBase) * 100}%`
    },
    // 重叠部分宽度（不超过基础范围的部分）
    overlapWidth() {
      return `${(this.baseMax / this.totalBase) * 100}%`
    },
    // 超出部分宽度（当前范围 - 基础范围）
    exceedWidth() {
      const exceedValue = this.targetMax - this.baseMax
      return `${(exceedValue / this.totalBase) * 100}%`
    }
  }
}
</script>

<style scoped>
.yield-indicator {
  display: flex;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  margin-top: 5px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  padding: 8px 8px;
  background-color: #fff;
  height:100px;
}

.yield-value {
  width: 180px;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 12px;
  text-align: center;
  margin-right: 24px;
}

.main-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.unit {
  font-size: 14px;
  color: #666;
  margin-top: 4px;
}

.yield-ranges {
  margin-left: 100px;
  flex: 1;
  min-width: 0;
  height:100px;
}

/* 关键：使用flex布局让文字和进度条并排显示 */
.range-item {
  display: flex;
  align-items: center; /* 文字与进度条垂直居中对齐 */
  margin-bottom: 10px;
  margin-top: 20px;
  gap: 12px; /* 文字与进度条之间的间距 */
}

/* 进度条前面的文字样式 */
.range-label {
  font-size: 12px; /* 小一号字体 */
  color: grey; /* 灰色 */
  width: 180px; /* 固定宽度，确保文字对齐 */
  white-space: nowrap;
  text-align: right; /* 文字右对齐，更贴近进度条 */
  font-weight: bold;
}

/* 进度条容器占剩余空间 */
.range-bar-container {
  flex: 1;
}
.warn-label{
  font-size: 12px;
}
.range-bar {
  height: 8px;
  background-color: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
  position: relative;
}

.composite-bar {
  background-color: transparent;
}

.bar {
  height: 100%;
  float: left;
}

.base-bar {
  background-color: #b6e8a8; /* 绿色 */
}

.overlap-bar {
  background-color: #fed090; /* 橙色 */
}

.exceed-bar {
  background-color: #f99295; /* 红色 */
}

.warning-tip {
  display: flex;
  align-items: center;
  color: #f59e0b;
  font-size: 14px;
  margin-left: 900px; /* 与上方文字对齐（180px+12px间距） */
  height:5px;
}

.warning-icon {
  margin-right: 4px;
}
</style>
