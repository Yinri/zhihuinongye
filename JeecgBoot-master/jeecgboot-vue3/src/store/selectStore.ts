// store/selectStore.ts
import { defineStore } from 'pinia';

// 定义并导出状态仓库，命名为“select”（对应下拉框相关状态）
export const useSelectStore = defineStore('select', {
  // 1. 存储状态（基地、地块的选中值）
  state: () => ({
    selectedBase: {
      baseId: '', // 新增：存储基地ID
      baseName: '' // 保留：存储基地名称
    },// 基地下拉框当前选中值
    selectedPlot: { plotId: '', plotName: '' } // 地块：ID+名称
  }),
  // 2. 定义修改状态的方法（规范地更新选中值）
  actions: {
    // 更新基地选中值
    updateSelectedBase(baseInfo: { baseId: string | number; baseName: string }) {
      this.selectedBase = baseInfo; // 接收完整对象，同时更新ID和名称
    },
    // 更新地块选中值
    updateSelectedPlot(plotInfo: { plotId: string | number; plotName: string } | null) {
      this.selectedPlot = plotInfo || { plotId: '', plotName: '' }; // 空值处理
    }
  }
});
