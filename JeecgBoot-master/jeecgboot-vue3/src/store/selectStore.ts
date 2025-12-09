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
    },
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
    seedParamWarned: false, // 避免重复弹窗提示
    // 新增：肥料计算参数（含安全系数，默认1.2）
    fertilizerParams: {
      safetyCoefficient: 1.2, // 安全系数默认值
      // 单位产量需肥量（新增核心字段）
      nutrientDemand: {
        n: 5.8, // N肥默认值（kg/100kg产量）
        p: 2.5, // P₂O₅默认值
        k: 4.3  // K₂O默认值
      },
      // 肥料利用率（提前加上，避免后续再改）
      utilizationRate: {
        n: 0.4, // N肥利用率40%（小数形式）
        p: 0.3, // P₂O₅利用率30%
        k: 0.45 // K₂O利用率45%
      }
    },
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
    // 新增：更新肥料参数（安全系数、利用率、需肥量等）
    updateFertilizerParams(params) {
      this.fertilizerParams = { ...this.fertilizerParams, ...params };
    },
    // 新增：单独更新单位产量需肥量（简化组件调用）
    updateNutrientDemand(data) {
      this.fertilizerParams.nutrientDemand = {
        ...this.fertilizerParams.nutrientDemand,
        ...data
      };
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
    // 新增：重置需肥量为默认值（品种切换为空时调用）
    resetNutrientDemand() {
      this.fertilizerParams.nutrientDemand = {
        n: 5.8,
        p: 2.5,
        k: 4.3
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
      this.resetNutrientDemand(); // 清空品种时重置需肥量
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
    },
    // 新增：判断需肥量是否完整（可选，用于校验）
    isNutrientDemandComplete: (state) => {
      return (
        state.fertilizerParams.nutrientDemand.n !== null &&
        state.fertilizerParams.nutrientDemand.p !== null &&
        state.fertilizerParams.nutrientDemand.k !== null
      );
    }
  }
});
//存储当前地块是否存在生产计划
export const usePlanStore = defineStore('planStore', {
  state: () => ({
    // 缓存所有地块对应的生产计划
    // 结构：
    // {
    //   'plotId1': { plan data ... },
    //   'plotId2': null,          // 查询结果为空也存 null（避免重复查）
    // }
    planCache: {},
  }),

  actions: {
    /** 保存某地块的生产计划数据（null 也要保存） */
    setPlan(plotId, planData) {
      this.planCache[plotId] = planData;
    },

    /** 获取指定地块的计划（可能为 null） */
    getPlan(plotId) {
      return this.planCache.hasOwnProperty(plotId)
        ? this.planCache[plotId]
        : undefined;     // undefined 表示「没查过」
    },

    /** 清空缓存（比如用户切换基地后清空） */
    clear() {
      this.planCache = {};
    }
  },

  getters: {
    /** 判断地块是否已有计划 */
    hasPlan: (state) => {
      return (plotId) => {
        const plan = state.planCache[plotId];
        return plan != null; // null 表无计划；undefined 表未查询
      };
    }
  }
});

