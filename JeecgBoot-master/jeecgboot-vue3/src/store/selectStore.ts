// store/selectStore.ts
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
    },
    // 新增：种子计算参数（用户录入或从接口获取）
    seedParams: {
      harvestCoefficient: null, // 收获系数
      seedlingRate: null, // 田间保苗率（%）
      settingRate: null, // 结实率（%）
      id: null // 参数记录ID（用于修改）
    },
    // 新增：种子参数缺失提示状态标记
    seedParamWarned: false // 避免重复弹窗提示
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
// 新增：更新种子参数
    updateSeedParams(params) {
      this.seedParams = { ...this.seedParams, ...params };
    },
    // 新增：重置种子参数（品种切换时调用）
    resetSeedParams() {
      this.seedParams = {
        harvestCoefficient: null,
        seedlingRate: null,
        settingRate: null,
        id: null
      };
    },
    // 新增：更新种子参数提示状态
    setSeedParamWarned(status: boolean) {
      this.seedParamWarned = status;
    },
    // 清空选中状态（可选，根据业务需求）
    clearSelected() {
      this.selected = { id: '', name: '' };
      this.resetSeedParams(); // 清空品种时同时重置种子参数
    }
  },

  //  getter：派生状态（可选，用于格式化或计算）
  getters: {
    isSelected: (state) => {
      return !!state.selected.id // 判断是否有选中的品种
    },
    // 新增：判断种子参数是否完整（用于快速校验）
    isSeedParamsComplete: (state) => {
      return (
        state.seedParams.harvestCoefficient !== null &&
        state.seedParams.seedlingRate !== null &&
        state.seedParams.settingRate !== null
      );
    }
  }
})

