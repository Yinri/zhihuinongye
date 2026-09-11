import { defineStore } from 'pinia';

// 保存下拉框选中的基地id和基地名，地块id和地块名
export const useSelectStore = defineStore('select', {
  // 1. 存储状态（基地、地块的选中值）
  state: () => ({
    selectedBase: {
      baseId: '', // 新增：存储基地ID
      baseName: '', // 保留：存储基地名称
      longitude: '', // 新增：存储基地经度
      latitude: '' // 新增：存储基地纬度
    },// 基地下拉框当前选中值
    selectedPlot: { plotId: '', plotName: '' } // 地块：ID+名称
  }),
  // 2. 定义修改状态的方法（规范地更新选中值）
  actions: {
    // 更新基地选中值
    updateSelectedBase(baseInfo: { baseId: string | number; baseName: string; longitude?: string; latitude?: string }) {
      this.selectedBase = {
        baseId: baseInfo.baseId,
        baseName: baseInfo.baseName,
        longitude: baseInfo.longitude || '',
        latitude: baseInfo.latitude || ''
      }; // 接收完整对象，同时更新ID、名称和经纬度
    },
    // 更新地块选中值
    updateSelectedPlot(plot: { plotId?: string | number; plotName?: string } | null) {
      if (plot) {
        this.selectedPlot = {
          plotId: String(plot.plotId || ''), // 强制转为字符串，避免类型问题
          plotName: plot.plotName || ''
        };
      } else {
        this.selectedPlot = { plotId: '', plotName: '' };
      }
    }
  }
});
