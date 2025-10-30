// store/selectStore.ts
import { defineStore } from 'pinia';

// 定义并导出状态仓库，命名为“select”（对应下拉框相关状态）
export const useSelectStore = defineStore('select', {
  // 1. 存储状态（基地、地块的选中值）
  state: () => ({
    selectedBase: '', // 基地下拉框当前选中值
    selectedPlot: ''  // 地块下拉框当前选中值
  }),
  // 2. 定义修改状态的方法（规范地更新选中值）
  actions: {
    // 更新基地选中值
    updateSelectedBase(value: string) {
      this.selectedBase = value;
    },
    // 更新地块选中值
    updateSelectedPlot(value: string) {
      this.selectedPlot = value;
    }
  }
});
