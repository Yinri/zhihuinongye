// store/selectStore.ts
import { defineStore } from 'pinia';

// 保存下拉框选中的基地id和基地名，地块id和地块名
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


// 保存选中的品种id和name
export const useCropVarietyStore = defineStore('cropVariety', {
  // 状态：存储选中的品种信息
  state: () => ({
    selected: {
      id: '', // 品种ID（对应value）
      name: '' // 品种名称（对应label）
    },
    yieldCalcData: {
      avgThreeYearYield: null, // 前三年平均单产
      increaseRate: '12' // 递增率（默认12%，与组件默认一致）
    }
  }),

  // 动作：修改状态的方法（推荐通过方法修改，便于追踪和扩展）
  actions: {
    // 更新选中的品种
    setSelectedVariety(variety) {
      // 校验参数，确保传入有效数据
      if (variety && variety.id && variety.name) {
        this.selected = { ...variety } // 解构赋值，确保响应式
      }
    },
    updateYieldCalcData(data) {
      // 合并新数据，保留未传入的原有字段
      this.yieldCalcData = { ...this.yieldCalcData, ...data };
    },

    // 清空选中状态（可选，根据业务需求）
    clearSelected() {
      this.selected = { id: '', name: '' }
    }
  },

  //  getter：派生状态（可选，用于格式化或计算）
  getters: {
    isSelected: (state) => {
      return !!state.selected.id // 判断是否有选中的品种
    }
  }
})

