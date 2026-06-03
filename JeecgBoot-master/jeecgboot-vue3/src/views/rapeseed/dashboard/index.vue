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
.dashboard_page {
  height: calc(100vh - 90px);
  display: flex;
  flex-direction: row;
  gap: 12px;
  padding: 8px 12px;
}

:deep(.warning-container) {
  height: 100%;
  flex: 0 0 360px;
  width: 360px;
  display: flex;
  flex-direction: column;
}

@media (max-width: 1200px) {
  :deep(.warning-container) {
    flex: 0 0 300px;
    width: 300px;
  }
}
</style>
