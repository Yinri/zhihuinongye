<template>
  <div class="dashboard_page">
    <FunctionButton></FunctionButton>
    <WarningBox></WarningBox>

  </div>
</template>

<script lang="ts" setup>

import WarningBox from "@/views/rapeseed/dashboard/components/WarningBox.vue";
import FunctionButton from "@/views/rapeseed/dashboard/components/FunctionButton.vue";
</script>

<style scoped>
html,
body {
  height: 100%;
  margin: 0; /* 清除浏览器默认边距，避免出现滚动条 */
  padding: 0;
}
/* 2. 让页面根div继承全屏高度 */
.dashboard_page {
  height: 100%;
  display: flex;
  flex-direction: row; /* 横向排列 */
}

</style>
