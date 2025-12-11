// src/store/planDraftStore.ts
import { defineStore } from 'pinia';

export const usePlanDraftStore = defineStore('planDraft', {
  state: () => ({
    // ================================
    // 基础元数据（后端会自动填 createBy / time，可为空）
    // ================================
    id: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    sysOrgCode: null,
    delFlag: 0,

    // ================================
    // 生产计划核心字段（完全对应后端表结构）
    // ================================
    baseId: '',                // 基地ID
    planName: '',             // 计划名称
    plotId: '',               // 地块ID
    planYear: '',             // 计划年份（yyyy）
    varietyId: '',            // 品种ID

    targetYield: null,        // 目标产量 kg/亩
    plantingArea: null,       // 种植面积 亩
    plannedSowingDate: null,  // 计划播种时间
    plannedHarvestDate: null, // 计划收获时间

    status: '0',              // 计划状态（默认草稿 0）

    // ================================
    // 产量递增相关
    // ================================
    increaseRate: 12,        // 递增率（百分比，与 UI 一致）

    // ================================
    // 种子参数（快照 JSON）
    // ================================
    seedParamsSnapshot: null,

    // ================================
    // 养分需求（快照 JSON）
    // ================================
    nutrientDemandSnapshot: null,

    // ================================
    // 土壤肥力（快照 JSON）
    // ================================
    soilFertilitySnapshot: null,

    // ================================
    // 肥料相关
    // ================================
    fertilizerSafetyCoeff: 1.2,
    fertilizerCombination: null,  // JSON 字符串
    fertilizerTotalN: 0,
    fertilizerTotalP: 0,
    fertilizerTotalK: 0,

    // ================================
    // 农药相关
    // ================================
    pesticideSafetyCoeff: 1.1,
    pesticideCombination: null, // JSON 字符串
    pesticideTotal: 0,
    pesticideNote: '',          // 备注

  }),

  actions: {
    /** 更新草稿（安全合并） */
    updateDraft(data) {
      Object.assign(this.$state, data);
    },

    /** 重置草稿（切换地块时用） */
    resetDraft() {
      this.$reset();
    },

    /** 绑定当前地块 */
    setPlot(plotId) {
      this.plotId = plotId;
    },

    /** 绑定品种 */
    setVariety(id) {
      this.varietyId = id;
    },

    /** 设置肥料组合（数组 → JSON 字符串） */
    setFertilizerCombination(list) {
      this.fertilizerCombination = JSON.stringify(list);
    },

    /** 设置农药组合 */
    setPesticideCombination(list) {
      this.pesticideCombination = JSON.stringify(list);
      this.pesticideTotal = list.reduce((sum, item) => sum + Number(item.planDose || 0), 0);
    },

    /** 设置种子参数快照 */
    setSeedParamsSnapshot(seedParams) {
      this.seedParamsSnapshot = JSON.stringify(seedParams);
    },

    /** 设置养分需求快照 */
    setNutrientDemandSnapshot(nutrientDemand) {
      this.nutrientDemandSnapshot = JSON.stringify(nutrientDemand);
    },

    /** 设置土壤肥力快照 */
    setSoilFertilitySnapshot(data) {
      this.soilFertilitySnapshot = JSON.stringify(data);
    },
  },

  getters: {
    /** 返回可直接提交后端的 JSON */
    fullPlan: (state) => ({ ...state }),
  },
});
