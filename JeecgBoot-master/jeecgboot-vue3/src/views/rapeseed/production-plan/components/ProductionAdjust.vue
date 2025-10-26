<template>
  <div class="yield-adjust-wrapper">
    <div class="yield-label">
      <i class="icon-yield" />
      目标产量
    </div>
    <button class="adjust-btn" @click="handleAdjust">
      <i class="icon-pencil" /> 调整
    </button>
  </div>
</template>

<script>
export default {
  methods: {
    handleAdjust() {
      // 点击“调整”后的逻辑，可在此打开弹窗、跳转页面等
      console.log('点击了调整目标产量');
      // 实际场景可替换为：this.$modal.open(...) 或路由跳转等操作
    }
  }
};
</script>

<style scoped>
.yield-adjust-wrapper {
  margin-top: 5px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  padding: 8px 16px;
  background-color: #fff;
}

.yield-label {
  display: flex;
  align-items: center;
  color: #333;
  font-size: 14px;
}

.icon-yield {
  /* 模拟产量图标，实际可替换为 SVG 或字体图标 */
  display: inline-block;
  width: 16px;
  height: 16px;
  background-color: #2f5496;
  border-radius: 2px;
  margin-right: 8px;
}

.adjust-btn {
  display: flex;
  align-items: center;
  background-color: #fff;
  color: #2f5496;
  border: 1px solid #2f5496;
  border-radius: 4px;
  padding: 4px 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.adjust-btn:hover {
  background-color: #2f5496;
  color: #fff;
}

.icon-pencil {
  /* 模拟铅笔图标，实际可替换为 SVG 或字体图标 */
  display: inline-block;
  width: 14px;
  height: 14px;
  background-color: #2f5496;
  margin-right: 4px;
}
</style>
