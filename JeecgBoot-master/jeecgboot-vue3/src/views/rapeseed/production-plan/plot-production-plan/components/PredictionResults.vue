<template>
  <div class="yield-indicator">
    <!-- 目标产量卡片 -->
    <div class="input-card">
      <div class="card-header">目标产量</div>
      <div class="main-value">
        {{ targetYield !== null ? targetYield.toFixed(1) : '暂无数据' }}
      </div>
      <div class="unit">公斤 / 亩</div>
    </div>

    <!-- 种子计划投入卡片 -->
    <div class="input-card">
      <div class="card-header">种子计划投入</div>
      <div class="main-value">
        {{ seedInput != null ? seedInput.toFixed(3) : '0.00' }}
      </div>
      <div class="unit">kg / 亩</div>
      <div v-if="missingSeedParam" class="missing-tip">
        {{ missingSeedParam }}
      </div>
    </div>
    <!-- 农药计划投入卡片 -->
    <!-- 👉 新增：农药计划投入组合卡片 -->
    <div class="pesticide-combination-card input-card" style="min-width: 600px; flex: 1;">
      <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
        <span>农药计划投入组合</span>

        <!-- 农药安全系数 -->
        <span style="display:flex; align-items:center; gap:5px;">
        <span>安全系数：</span>
        <a-select
          v-model:value="pesticideSafetyCoeff"
          size="small"
          style="width: 80px;"
        >
          <a-select-option :value="0.8">0.8</a-select-option>
          <a-select-option :value="1.0">1.0</a-select-option>
          <a-select-option :value="1.1">1.1</a-select-option>
          <a-select-option :value="1.2">1.2</a-select-option>
          <a-select-option :value="1.3">1.3</a-select-option>
          <a-select-option :value="1.5">1.5</a-select-option>
        </a-select>

        </span>

      </div>

      <!-- 选择农药 + 添加按钮 -->
      <div style="display: flex; gap: 10px; margin-bottom: 10px;">
        <a-select
          v-model:value="selectedPesticideId"
          placeholder="请选择农药类型"
          style="flex: 1; text-align: left;"
        >
          <a-select-option
            v-for="item in pesticideList"
            :key="item.id"
            :value="String(item.id)"
          >
            {{ item.name  }}（建议：{{ item.defaultDose }}{{ item.unit }}）
          </a-select-option>
        </a-select>

        <a-button
          type="primary"
          size="small"
          @click="addPesticideToCombination"
          :disabled="selectedPesticideId === '' || selectedPesticideId == null"
        >
          添加
        </a-button>
      </div>

      <!-- 农药表格 -->
      <a-table
        :columns="pesticideColumns"
        :dataSource="pesticideCombinationList"
        bordered
        :pagination="false"
        style="margin-bottom: 10px;"
        :scroll="{ y: 300 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'operation'">
            <a-button type="text" @click="deletePesticide(record.id)" style="color: #f5222d;">
              删除
            </a-button>
          </template>
          <template v-if="column.key === 'safetyCoeff'">
            {{ record.safetyCoeff }}
          </template>
        </template>
      </a-table>
    </div>


    <!-- 多肥料组合展示卡片（复杂场景） -->
    <div class="fertilizer-combination-card input-card" style="min-width: 600px; flex: 1;">
      <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
        <span>肥料计划投入组合</span>

        <!-- 安全系数一排显示 -->
        <span style="font-size: 13px; color: #555;">
    安全系数：
    <b>{{ fertilizerParams?.safetyCoefficient || 1.2 }}</b>
  </span>
      </div>

      <div style="display: flex; gap: 10px; margin-bottom: 10px;">
        <a-select
          v-model="selectedFertilizerId"
          placeholder="请选择肥料类型"
          style="flex: 1;"
          @change="handleFertilizerSelect"
        >
          <a-select-option
            v-for="item in fertilizerList"
            :key="item.id"
            :value="String(item.id)"
          >
            {{ item.fertilizerName }}（{{ item.npkRatio }}）
          </a-select-option>
        </a-select>

        <a-button
          type="primary"
          size="small"
          @click="addFertilizerToCombination"
          :disabled="!selectedFertilizerId || !cropStore.selected?.id || !selectStore.selectedPlot?.plotId"
        >
          添加
        </a-button>
      </div>
      <div class="fertilizer-table-container">
        <a-table
        :columns="fertilizerColumns"
        :dataSource="fertilizerCombinationList"
        bordered
        :pagination="false"
        style="margin-bottom: 10px;"
        :scroll="{ y: 300 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'operation'">
            <a-button type="text" @click="deleteFertilizer(record.id)" style="color: #f5222d;">
              删除
            </a-button>
          </template>
          <template v-if="column.key === 'inputAmount'">
            {{ record.inputAmount.toFixed(2) }}
          </template>
          <template v-if="column.key === 'nContent'">
            {{ record.nContent.toFixed(2) }}
          </template>
          <template v-if="column.key === 'pContent'">
            {{ record.pContent.toFixed(2) }}
          </template>
          <template v-if="column.key === 'kContent'">
            {{ record.kContent.toFixed(2) }}
          </template>
        </template>
      </a-table>
      </div>
      <!-- 合计行 -->
      <div class="fertilizer-total">
        <span>总N投入：{{ totalN.toFixed(2) }} kg/亩 | </span>
        <span>总P₂O₅投入：{{ totalP.toFixed(2) }} kg/亩 | </span>
        <span>总K₂O投入：{{ totalK.toFixed(2) }} kg/亩</span>
      </div>
    </div>


  </div>
</template>

<script>
import { useCropVarietyStore, useSelectStore } from '@/store/selectStore';
import { message } from 'ant-design-vue';

import { storeToRefs } from 'pinia';
import {
  getSeedParamsByVarietyId,
  getFertilizerParamsByVarietyId,
  getSoilFertilityByPlotId,
  getFertilizerUtilizationRate,
  getFertilizerList,
  getPesticideList
} from '../base.api';
import { nanoid } from 'nanoid';

export default {
  name: 'YieldIndicator',
  setup() {
    // 1. 实例化 Store（全局共享）
    const cropStore = useCropVarietyStore();
    const selectStore = useSelectStore();
    const cropVarietyStore = useCropVarietyStore(); // 关键：和品种组件的Store实例名一致

    // 2. 正确解构需要响应式的变量（保留 yieldCalcData/seedParams 等）
    const {
      yieldCalcData,
      selected, // 保留解构，但后续计算属性不用这个，用 cropStore.selected
      seedParams,
      fertilizerParams
    } = storeToRefs(cropStore);
    const { selectedPlot } = storeToRefs(selectStore); // 保留解构

    // 3. 返回所有需要的变量（包括 Store 实例 + 解构的变量）
    return {
      cropStore, // 暴露 Store 实例（关键：用于计算属性取值）
      selectStore,
      cropVarietyStore,
      yieldCalcData,
      selected,
      seedParams,
      fertilizerParams,
      selectedPlot
    };
  },
  data() {
    return {
      // 👉 新增：农药相关数据
      pesticideList: [],              // 来自农药信息表
      selectedPesticideId: null,        // 下拉选中值
      pesticideCombinationList: [],   // 当前组合
      pesticideSafetyCoeff: 1.1,      // 农药安全系数
      pesticideInput: 0,              // 农药合计投入显示（卡片用）
      defaultPesticides: [
        "1993510200000000001", // 咪鲜胺（杀菌）
        "1993510200000000002", // 高效氯氰菊酯（杀虫）
        "1993510200000000006"  // 赤霉酸（调节剂）
      ],

      pesticideColumns: [
        { title: '农药名称', dataIndex: 'name', key: 'name' },
        { title: '建议剂量(ml/亩)', dataIndex: 'defaultDose', key: 'defaultDose' },
        { title: '计划剂量(ml/亩)', dataIndex: 'planDose', key: 'planDose' },
        { title: '安全系数', dataIndex: 'safetyCoeff', key: 'safetyCoeff' },
        { title: '操作', key: 'operation' }
      ],


      varietyDetail: null,
      missingSeedParam: '',
      missingFertilizerParam: '',
      soilFertilityData: null,
      fertilizerUtilizationData: null,
      nutrientDemandData: null,
      fertilizerContentData: null,
      fertilizerList: [],
      selectedFertilizerId: '',
      fertilizerSelectTip: '',
      fertilizerCombinationList: [],
      totalN: 0,
      totalP: 0,
      totalK: 0,
      currentFertilizerUtilization: { nRate: 35, pRate: 25, kRate: 35 },
      // 新增：统一管理默认值（方便修改）
      defaultNutrientDemand: { nDemand: 5.8, pDemand: 2.5, kDemand: 4.3 },
      defaultSoilFertility: { nContent: 20, pContent: 10, kContent: 15 }, // 土壤肥力默认值
      fertilizerColumns: [
        {
          title: '肥料名称',
          dataIndex: 'fertilizerName',
          key: 'fertilizerName'
        },
        {
          title: '养分含量（N-P₂O₅-K₂O）',
          dataIndex: 'npkRatio',
          key: 'npkRatio'
        },
        {
          title: '利用率（N/P/K）',
          dataIndex: 'utilizationRate',
          key: 'utilizationRate'
        },
        {
          title: '安全系数',
          dataIndex: 'safetyCoeff',
          key: 'safetyCoeff'
        },
        {
          title: '计划投入量（kg/亩）',
          dataIndex: 'inputAmount',
          key: 'inputAmount'
        },
        {
          title: '实际N投入（kg/亩）',
          dataIndex: 'nContent',
          key: 'nContent'
        },
        {
          title: '实际P₂O₅投入（kg/亩）',
          dataIndex: 'pContent',
          key: 'pContent'
        },
        {
          title: '实际K₂O投入（kg/亩）',
          dataIndex: 'kContent',
          key: 'kContent'
        },
        {
          title: '操作',
          key: 'operation'
        }
      ]
    };
  },
  computed: {
    // 修复：直接从 Store 实例取值（响应式生效）
    selectedVarietyId() {
      return this.cropStore.selected?.id || ''; // 用全局 Store 实例，而非解构的 selected
    },
    selectedPlotId() {
      return this.selectStore.selectedPlot?.plotId || ''; // 用全局 Store 实例
    },
    avgThreeYearYield() {
      return this.yieldCalcData.avgThreeYearYield ? Number(this.yieldCalcData.avgThreeYearYield) : null;
    },
    increaseRate() {
      return this.yieldCalcData.increaseRate ? Number(this.yieldCalcData.increaseRate) / 100 : 0;
    },
    targetYield() {
      if (this.avgThreeYearYield === null || isNaN(this.avgThreeYearYield)) {
        return null;
      }
      return this.avgThreeYearYield * (1 + this.increaseRate);
    },
    seedInput() {
      this.missingSeedParam = '';
      const selectedVariety = this.cropStore.selected; // 改用 Store 实例的 selected
      const userSeedParams = this.seedParams.value;
      const targetYield = this.targetYield;
      const varietyDetail = this.varietyDetail;

      if (!selectedVariety?.id) {
        this.missingSeedParam = '请先选择品种';
        return null;
      }
      if (!varietyDetail) {
        this.missingSeedParam = '品种参数加载中...';
        return null;
      }
      if (targetYield === null) {
        this.missingSeedParam = '目标产量未计算';
        return null;
      }

      const harvestCoefficient = userSeedParams?.harvestCoefficient ?? varietyDetail.harvestCoefficient;
      const settingRate = userSeedParams?.settingRate ?? varietyDetail.seedSettingRate;
      const seedlingRate = userSeedParams?.seedlingRate ?? varietyDetail.seedlingSurvivalRate;

      const missingFields = [];
      if (!harvestCoefficient || harvestCoefficient <= 0) missingFields.push('收获系数');
      if (!settingRate || settingRate <= 0) missingFields.push('结实率');
      if (!seedlingRate || seedlingRate <= 0) missingFields.push('田间保苗率');

      if (missingFields.length > 0) {
        this.missingSeedParam = `请录入：${missingFields.join('、')}`;
        return null;
      }

      const params = {
        targetYieldKg: targetYield,
        singlePlantPods: Number(varietyDetail.singlePlantPods) || 0,
        seedsPerPod: Number(varietyDetail.seedsPerPod) || 0,
        thousandGrainWeight: Number(varietyDetail.thousandGrainWeight) || 0,
        germinationRate: Number(varietyDetail.germinationRate) || 0,
        harvestCoefficient: Number(harvestCoefficient) || 0,
        settingRate: Number(settingRate) || 0,
        seedlingRate: Number(seedlingRate) || 0
      };

      if (
        params.singlePlantPods <= 0 ||
        params.seedsPerPod <= 0 ||
        params.thousandGrainWeight <= 0 ||
        params.germinationRate <= 0 ||
        params.harvestCoefficient <= 0 ||
        params.settingRate <= 0 ||
        params.seedlingRate <= 0
      ) {
        this.missingSeedParam = '品种参数无效，请检查';
        return null;
      }

      try {
        const theoreticalSeedlings = (params.targetYieldKg * 1000) / (
          params.singlePlantPods *
          params.seedsPerPod *
          params.thousandGrainWeight *
          (params.settingRate / 100) *
          params.harvestCoefficient
        );

        const seedInputKg = (theoreticalSeedlings * params.thousandGrainWeight) / (
          (params.germinationRate / 100) *
          (params.seedlingRate / 100) *
          1000
        );
        return seedInputKg > 0 ? seedInputKg : null;
      } catch (error) {
        this.missingSeedParam = '计算失败，请检查参数';
        return null;
      }
    }
  },
  watch: {
    pesticideSafetyCoeff(newVal, oldVal) {
      if (!this.pesticideCombinationList.length) return;

      this.pesticideCombinationList = this.pesticideCombinationList.map(item => {
        return {
          ...item,
          safetyCoeff: newVal,
          planDose: item.defaultDose * newVal   // ⭐ 计划剂量 = 推荐剂量 × 安全系数
        };
      });

      this.calcPesticideTotal();
    },
    // 品种变化
    async 'cropStore.selected.id'(newVarietyId) {
      console.log('========== 品种ID变化 ==========');
      console.log('新的品种ID:', newVarietyId);
      console.log('================================');

      // 重置页面肥料组合
      this.fertilizerCombinationList = [];
      this.missingFertilizerParam = '';

      if (!newVarietyId) {
        this.varietyDetail = null;
        this.nutrientDemandData = null;
        return;
      }

      // 顺序不能乱：品种信息 → 养分需求
      await this.fetchVarietyDetail(newVarietyId);
      await this.fetchFertilizerByVariety(newVarietyId);

      // 等待页面刷新
      await this.$nextTick();
      this.addSmartDefaultPesticides();
      // ⭐ 仅在两个关键数据都齐全后再智能推荐肥料
      if (this.soilFertilityData && this.nutrientDemandData) {
        console.log("🔥 自动智能推荐肥料（因品种变化触发）");
        this.addSmartDefaultFertilizers();
      }
    },

    // 地块变化
    async 'selectStore.selectedPlot.plotId'(newPlotId) {
      console.log('========== 地块ID变化 ==========');
      console.log('新的地块ID:', newPlotId);
      console.log('================================');

      this.fertilizerCombinationList = [];
      this.missingFertilizerParam = '';

      if (!newPlotId) {
        this.soilFertilityData = null;
        return;
      }

      await this.fetchSoilFertilityByPlot(newPlotId);
      await this.$nextTick();
      this.addSmartDefaultPesticides();
      // 如果品种需求也加载好了，就重新智能推荐肥料
      if (this.nutrientDemandData) {
        console.log("🔥 自动智能推荐肥料（因地块变化触发）");
        this.addSmartDefaultFertilizers();
      }
    },

    // 自动更新合计
    fertilizerCombinationList: {
      deep: true,
      handler() {
        this.calcTotalFertilizerInput();
      }
    },

    // ⭐ 安全系数变化 → 自动刷新肥料计算
    'fertilizerParams.safetyCoefficient'(newVal, oldVal) {
      if (newVal === oldVal) return;
      if (!this.fertilizerCombinationList.length) return;

      console.log("🔄 安全系数更新，自动刷新所有肥料投入", newVal);

      this.fertilizerCombinationList = this.fertilizerCombinationList.map(item => {
        const fertilizer = this.fertilizerList.find(f => f.id === item.fertilizerId);
        if (!fertilizer) return item;

        const calc = this.calcSingleFertilizerInput(fertilizer);

        return {
          ...item,
          inputAmount: calc.inputAmount,
          nContent: calc.nContent,
          pContent: calc.pContent,
          kContent: calc.kContent,
          safetyCoeff: newVal
        };
      });
    }
  },

  methods: {
    addSmartDefaultPesticides() {
      if (this.pesticideCombinationList.length > 0) return;
      if (!this.pesticideList.length) return;

      this.defaultPesticides.forEach(pid => {
        const p = this.pesticideList.find(x => String(x.id) === pid);
        if (!p) return;

        const planDose = p.defaultDose * this.pesticideSafetyCoeff;

        this.pesticideCombinationList.push({
          id: nanoid(),
          pesticideId: p.id,
          name: p.pesticideName || p.name,
          defaultDose: p.defaultDose + p.unit,
          safetyCoeff: this.pesticideSafetyCoeff,
          planDose
        });
      });

      this.calcPesticideTotal();
    },

    // 👉 新增：加载农药信息（从你的农药信息表）
    async fetchPesticideList() {
      // TODO: 调你的后端接口（你有农药信息表）
      // 假设后端返回格式如下：
      // [{id: 1, name: '甲维盐', defaultDose: 20, unit: 'ml'}]

      const res = await getPesticideList();
      console.log("📦 加载农药信息", res);
      this.pesticideList = res || [];
    },

// 👉 新增：添加农药组合
    addPesticideToCombination() {
      // ❌ 禁止重复添加
      const exists = this.pesticideCombinationList.some(
        item => String(item.pesticideId) === String(this.selectedPesticideId)
      );
      if (exists) {
        message.warning("该农药已添加，不可重复添加");
        return;
      }

      const id = String(this.selectedPesticideId);
      const p = this.pesticideList.find(x => String(x.id) === id);

      if (!p) {
        console.warn("未找到农药信息：", id);
        return;
      }

      const planDose = p.defaultDose * this.pesticideSafetyCoeff;

      this.pesticideCombinationList.push({
        id: nanoid(),
        pesticideId: p.id,
        name: p.name,
        defaultDose: p.defaultDose,
        planDose,
        safetyCoeff: this.pesticideSafetyCoeff
      });

      this.selectedPesticideId = '';
      this.calcPesticideTotal();
    },

// 👉 新增：删除农药组合
    deletePesticide(id) {
      this.pesticideCombinationList =
        this.pesticideCombinationList.filter(x => x.id !== id);
      this.calcPesticideTotal();
    },

// 👉 新增：合计计算（用于卡片显示）
    calcPesticideTotal() {
      let total = 0;
      this.pesticideCombinationList.forEach(item => {
        total += item.actualDose;
      });
      this.pesticideInput = total.toFixed(1);
    },

    // 智能生成默认肥料组合（根据 N/P/K 缺口）
    generateSmartDefaultFertilizers() {
      if (!this.targetYield || !this.soilFertilityData || !this.nutrientDemandData) {
        console.warn("可用数据不足，无法生成默认肥料组合");
        return [];
      }

      // 1️⃣ 计算缺口（和 calcSingleFertilizerInput 一样的逻辑）
      const soil = this.soilFertilityData;

      const soilNDemand = soil.nContent * 0.15 * 2.25 / 1000;
      const soilPDemand = soil.pContent * 0.15 * 2.25 / 1000;
      const soilKDemand = soil.kContent * 0.15 * 2.25 / 1000;

      const targetNDemand = this.targetYield * this.nutrientDemandData.nDemand / 100;
      const targetPDemand = this.targetYield * this.nutrientDemandData.pDemand / 100;
      const targetKDemand = this.targetYield * this.nutrientDemandData.kDemand / 100;

      const needN = Math.max(0, targetNDemand - soilNDemand);
      const needP = Math.max(0, targetPDemand - soilPDemand);
      const needK = Math.max(0, targetKDemand - soilKDemand);

      console.log("🌾 缺口计算：", { needN, needP, needK });

      // 2️⃣ 智能挑选肥料：根据是否补 N/P/K 自动从列表挑肥料
      let recommended = [];

      // 补氮：找 N 含量最高的肥料（如尿素 46-0-0）
      if (needN > 0) {
        const nFert = this.fertilizerList.find(f => {
          const [n] = f.npkRatio.split('-').map(Number);
          return n > 20; // 氮含量高的单质或复合肥
        });
        if (nFert) recommended.push(nFert.id);
      }

      // 补磷：找 P 含量高的肥料（磷酸二铵 18-46-0 等）
      if (needP > 0) {
        const pFert = this.fertilizerList.find(f => {
          const [, p] = f.npkRatio.split('-').map(Number);
          return p > 20;
        });
        if (pFert) recommended.push(pFert.id);
      }

      // 补钾：找 K 含量高的肥料（氯化钾 0-0-60）
      if (needK > 0) {
        const kFert = this.fertilizerList.find(f => {
          const [, , k] = f.npkRatio.split('-').map(Number);
          return k > 20;
        });
        if (kFert) recommended.push(kFert.id);
      }

      // 去重（避免某些复合肥在 N/P/K 都命中）
      recommended = [...new Set(recommended)];

      console.log("🤖 智能推荐肥料列表：", recommended);

      return recommended;
    },
    async addSmartDefaultFertilizers() {
      const defaults = this.generateSmartDefaultFertilizers();

      for (const fertId of defaults) {
        const fert = this.fertilizerList.find(f => f.id === fertId);
        if (!fert) continue;

        // 触发利用率查询
        await this.handleFertilizerSelect(fertId);
        await this.$nextTick();

        this.selectedFertilizerId = fertId;
        this.addFertilizerToCombination();
      }
    },


    async fetchFertilizerList() {
      try {
        const res = await getFertilizerList();
        this.fertilizerList = res || [];
      } catch (error) {
        this.fertilizerSelectTip = '肥料列表加载失败';
        console.error('肥料列表接口失败：', error);
      }
    },
    async fetchVarietyDetail(varietyId) {
      try {
        this.missingSeedParam = '品种参数加载中...';
        const res = await getSeedParamsByVarietyId(varietyId);

        if (res.harvestCoefficient || res.settingRate || res.seedlingRate) {
          this.cropStore.updateSeedParams({
            harvestCoefficient: res.harvestCoefficient,
            settingRate: res.seedSettingRate,
            seedlingRate: res.seedlingSurvivalRate,
            id: res.id
          });
        }
        this.varietyDetail = res;
      } catch (error) {
        this.missingSeedParam = '品种参数加载失败';
        this.varietyDetail = null;
        console.error('品种参数接口失败：', error);
      }
    },
    async fetchFertilizerByVariety(varietyId) {
      console.log('📡 调用品种养分需求接口的参数：', varietyId);

      try {
        // ① 获取养分需求
        const nutrientDemandRes = await getFertilizerParamsByVarietyId(varietyId);
        const realNutrientData = nutrientDemandRes?.result || nutrientDemandRes || {};

        this.nutrientDemandData = {
          nDemand: realNutrientData.nDemand == null ? this.defaultNutrientDemand.nDemand : Number(realNutrientData.nDemand),
          pDemand: realNutrientData.pDemand == null ? this.defaultNutrientDemand.pDemand : Number(realNutrientData.pDemand),
          kDemand: realNutrientData.kDemand == null ? this.defaultNutrientDemand.kDemand : Number(realNutrientData.kDemand)
        };

        console.log("✅ 最终生效的养分需求：", this.nutrientDemandData);

        this.fertilizerUtilizationData = null; // 或不设置

      } catch (error) {
        console.error("❌ 品种肥料需求接口失败：", error);
        this.missingFertilizerParam = '品种肥料需求加载失败';
        // ❗ 这里不能覆盖 nutrientDemandData ，保持旧值 or 默认值
      }
    },

    async fetchSoilFertilityByPlot(plotId) {
      try {
        this.missingFertilizerParam = '土壤肥力数据加载中...';
        const soilRes = await getSoilFertilityByPlotId(plotId);
        // 修复2：土壤肥力兜底（避免全0）
        this.soilFertilityData = {
          nContent: Number(soilRes?.nContent) || this.defaultSoilFertility.nContent,
          pContent: Number(soilRes?.pContent) || this.defaultSoilFertility.pContent,
          kContent: Number(soilRes?.kContent) || this.defaultSoilFertility.kContent,
          ...soilRes // 保留其他字段
        };
      } catch (error) {
        this.missingFertilizerParam = '土壤肥力数据加载失败，使用默认值';
        this.soilFertilityData = {...this.defaultSoilFertility}; // 兜底默认值
        console.error('土壤肥力接口失败：', error);
      }
    },
    async handleFertilizerSelect(fertilizerId) {



      // 修复：先等待响应式更新，确保所有ID都已同步
      this.selectedFertilizerId = String(fertilizerId || '').trim();
      await this.$nextTick();

      // ========== 关键修复：修正品种ID的获取路径 ==========
      // 确认品种ID的正确路径（根据你的Store结构，替换为实际路径）
      // 若品种ID存在于 cropVarietyStore → 用 this.cropVarietyStore.selected.id
      // 若存在于 cropStore → 用 this.cropStore.selected.id
      // 先打印所有可能的路径，确认正确值
      console.log('品种ID排查：', {
        cropStore: this.cropStore?.selected?.id,
        cropVarietyStore: this.cropVarietyStore?.selected?.id,
        // 备用：直接从Pinia全局状态获取
        globalPinia: window.$pinia?.state?.value?.cropVariety?.selected?.id
      });

      // 强制获取正确的品种ID（根据打印结果选择路径）
      const varietyId = String(
        this.cropVarietyStore?.selected?.id || // 优先用这个（品种组件赋值的Store）
        this.cropStore?.selected?.id ||
        window.$pinia?.state?.value?.cropVariety?.selected?.id ||
        ''
      ).trim();
      const plotId = String(this.selectStore.selectedPlot?.plotId || '').trim();
      const fertId = String(fertilizerId || '').trim();

      // 严格校验
      if (!varietyId || !plotId || !fertId) {
        this.fertilizerSelectTip = `缺少必要参数：品种ID(${varietyId ? '有' : '无'}) | 地块ID(${plotId ? '有' : '无'}) | 肥料ID(${fertId ? '有' : '无'})`;
        console.error('接口调用失败：缺少参数', {varietyId, plotId, fertId});
        return;
      }

      try {
        this.fertilizerSelectTip = '正在查询肥料利用率...';
        const requestParams = {
          varietyId: varietyId,
          plotId: plotId,
          fertilizerId: fertId
        };
        console.log('接口请求参数（正确）：', requestParams);
        const res = await getFertilizerUtilizationRate(requestParams);
        console.log('肥料利用率接口返回：', res); // 新增：打印完整返回值
        // 修复3：正确解析利用率字段（nrate/prate/krate）
        this.currentFertilizerUtilization = {
          nRate: Number(res?.nrate) || 35,
          pRate: Number(res?.prate) || 25,
          kRate: Number(res?.krate) || 35
        };
        this.fertilizerSelectTip = '';
      } catch (error) {
        this.fertilizerSelectTip = '未查询到该组合利用率，使用默认值';
        this.currentFertilizerUtilization = {nRate: 35, pRate: 25, kRate: 35};
        console.warn('肥料利用率未配置，已自动使用默认配置');
      }
    },
    calcSingleFertilizerInput(fertilizerItem) {
      if (!this.targetYield || !this.soilFertilityData || !this.nutrientDemandData) {
        return {inputAmount: 0, nContent: 0, pContent: 0, kContent: 0};
      }

      // ========== 修复4：展开Proxy对象，提取真实土壤肥力数据 ==========
      const soilFertility = JSON.parse(JSON.stringify(this.soilFertilityData));
      console.log('🌱 展开后的土壤肥力数据：', soilFertility);

      // 取值（兜底避免NaN）
      const soilN = Number(soilFertility.nContent) || this.defaultSoilFertility.nContent;
      const soilP = Number(soilFertility.pContent) || this.defaultSoilFertility.pContent;
      const soilK = Number(soilFertility.kContent) || this.defaultSoilFertility.kContent;

      // 正确计算土壤养分贡献（农业通用公式）
      const soilNDemand = soilN * 0.15 * 2.25 / 1000;
      const soilPDemand = soilP * 0.15 * 2.25 / 1000;
      const soilKDemand = soilK * 0.15 * 2.25 / 1000;

      // 解析肥料NPK比例（兜底避免空）
      const npkStr = fertilizerItem.npkRatio?.replace(/[^0-9\-]/g, '') || '0-0-0';
      const npkArr = npkStr.split('-').map(num => Number(num) || 0);
      const [nRatio = 0, pRatio = 0, kRatio = 0] = npkArr;

      const nContentRatio = nRatio / 100;
      const pContentRatio = pRatio / 100;
      const kContentRatio = kRatio / 100;

      // 利用率（转小数，兜底避免0）
      const nRate = (this.currentFertilizerUtilization.nRate || 35) / 100;
      const pRate = (this.currentFertilizerUtilization.pRate || 25) / 100;
      const kRate = (this.currentFertilizerUtilization.kRate || 35) / 100;

      const safetyCoeff = this.fertilizerParams?.safetyCoefficient || 1.2;

      // 目标养分需求（正确计算）
      const targetNDemand = this.targetYield * this.nutrientDemandData.nDemand / 100;
      const targetPDemand = this.targetYield * this.nutrientDemandData.pDemand / 100;
      const targetKDemand = this.targetYield * this.nutrientDemandData.kDemand / 100;

      // 实际需肥量（扣除土壤贡献，兜底避免负数）
      const needN = Math.max(0.001, targetNDemand - soilNDemand); // 兜底0.001避免分母为0
      const needP = Math.max(0.001, targetPDemand - soilPDemand);
      const needK = Math.max(0.001, targetKDemand - soilKDemand);

      // ========== 打印计算过程，验证非0 ==========
      console.log('🧮 施肥量计算过程：', {
        targetNDemand, soilNDemand, needN,
        targetPDemand, soilPDemand, needP,
        targetKDemand, soilKDemand, needK,
        fertilizerNPK: npkArr,
        utilization: {nRate, pRate, kRate},
        safetyCoeff
      });

      // 计算计划投入量（取最大值，确保满足至少一种养分需求）
      let inputAmount = 0;
      // 修复5：分母为0时跳过该养分计算
      if (nContentRatio > 0 && nRate > 0) {
        inputAmount = Math.max(inputAmount, needN * safetyCoeff / (nContentRatio * nRate));
      }
      if (pContentRatio > 0 && pRate > 0) {
        inputAmount = Math.max(inputAmount, needP * safetyCoeff / (pContentRatio * pRate));
      }
      if (kContentRatio > 0 && kRate > 0) {
        inputAmount = Math.max(inputAmount, needK * safetyCoeff / (kContentRatio * kRate));
      }

      // 计算实际养分投入
      const nContent = inputAmount * nContentRatio * nRate;
      const pContent = inputAmount * pContentRatio * pRate;
      const kContent = inputAmount * kContentRatio * kRate;

      return {
        inputAmount: inputAmount > 0 ? inputAmount : 0,
        nContent,
        pContent,
        kContent
      };
    },
    addFertilizerToCombination() {
      // ❌ 禁止重复添加
      const exists = this.fertilizerCombinationList.some(
        item => String(item.fertilizerId) === String(this.selectedFertilizerId)
      );
      if (exists) {
        message.warning("该肥料已添加，不可重复添加");
        return;
      }

      // ========== 复制这部分打印代码 ==========
      console.log('========== 按钮点击时的关键值 ==========');
      // 打印肥料ID
      console.log('肥料ID（selectedFertilizerId）:', this.selectedFertilizerId, '类型:', typeof this.selectedFertilizerId);
      // 打印品种ID（从Store直接取）
      console.log('品种ID（cropStore.selected.id）:', this.cropStore.selected?.id, '类型:', typeof this.cropStore.selected?.id);
      // 打印地块ID（从Store直接取）
      console.log('地块ID（selectStore.selectedPlot.plotId）:', this.selectStore.selectedPlot?.plotId, '类型:', typeof this.selectStore.selectedPlot?.plotId);
      // 打印禁用条件的每一项结果
      console.log('禁用条件1（肥料ID为空）:', !Boolean(this.selectedFertilizerId?.trim()));
      console.log('禁用条件2（品种ID为空）:', !Boolean(this.cropStore.selected?.id?.trim()));
      console.log('禁用条件3（地块ID为空）:', !Boolean(this.selectStore.selectedPlot?.plotId?.trim()));
      console.log('总禁用条件（按钮是否禁用）:', !Boolean(this.selectedFertilizerId?.trim()) || !Boolean(this.cropStore.selected?.id?.trim()) || !Boolean(this.selectStore.selectedPlot?.plotId?.trim()));
      console.log('==========================================');
      // ========== 新增：打印施肥量计算的核心参数 ==========
      console.log('---------- 施肥量计算核心参数 ----------');
      // 1. 目标产量（是否有效）
      console.log('targetYield（目标产量）:', this.targetYield, '类型:', typeof this.targetYield);
      // 2. 土壤肥力数据（是否加载）
      console.log('soilFertilityData（土壤肥力）:', this.soilFertilityData);
      // 3. 品种养分需求数据（是否加载）
      console.log('nutrientDemandData（品种养分需求）:', this.nutrientDemandData);
      // 4. 当前选中肥料的NPK比例
      const selectedFertilizer = this.fertilizerList.find(item => item.id === this.selectedFertilizerId);
      console.log('选中肥料NPK:', selectedFertilizer?.npkRatio);
      // 5. 肥料利用率（是否正确获取）
      console.log('currentFertilizerUtilization（利用率）:', this.currentFertilizerUtilization);
      console.log('==========================================');
      // ========== 打印代码结束 ==========

      if (!this.selectedFertilizerId) {
        this.fertilizerSelectTip = '请先选择肥料';
        return;
      }

      if (!selectedFertilizer) {
        this.fertilizerSelectTip = '肥料信息不存在';
        return;
      }

      const calcResult = this.calcSingleFertilizerInput(selectedFertilizer);
      console.log('📊 施肥量计算结果：', calcResult); // 新增：打印计算结果

      const combinationItem = {
        id: nanoid(),
        fertilizerName: selectedFertilizer.fertilizerName,
        npkRatio: selectedFertilizer.npkRatio,
        utilizationRate: `${this.currentFertilizerUtilization.nRate || 35}%/${this.currentFertilizerUtilization.pRate || 25}%/${this.currentFertilizerUtilization.kRate || 35}%`,
        inputAmount: calcResult.inputAmount,
        nContent: calcResult.nContent,
        pContent: calcResult.pContent,
        kContent: calcResult.kContent,
        fertilizerId: selectedFertilizer.id,
        safetyCoeff: this.fertilizerParams?.safetyCoefficient || 1.2,
      };

      this.fertilizerCombinationList.push(combinationItem);

      this.selectedFertilizerId = '';
      this.fertilizerSelectTip = '';
    },
    deleteFertilizer(id) {
      this.fertilizerCombinationList = this.fertilizerCombinationList.filter(item => item.id !== id);
    },
    calcTotalFertilizerInput() {
      this.totalN = this.fertilizerCombinationList.reduce((sum, item) => sum + item.nContent, 0);
      this.totalP = this.fertilizerCombinationList.reduce((sum, item) => sum + item.pContent, 0);
      this.totalK = this.fertilizerCombinationList.reduce((sum, item) => sum + item.kContent, 0);
    }
  },
  async mounted() {
    await this.fetchFertilizerList();
    await this.fetchPesticideList();
    const varietyId = this.cropStore.selected?.id; // 改用 Store 实例
    const plotId = this.selectStore.selectedPlot?.plotId; // 改用 Store 实例

    if (varietyId) {
      await this.fetchVarietyDetail(varietyId);
      await this.fetchFertilizerByVariety(varietyId);
    }
    if (plotId) {
      await this.fetchSoilFertilityByPlot(plotId);
    }
    // ========== 复制这部分打印代码 ==========
    console.log('========== 页面初始化时的关键值 ==========');
    console.log('肥料列表数据:', this.fertilizerList);
    console.log('初始化品种ID:', this.cropStore.selected?.id);
    console.log('初始化地块ID:', this.selectStore.selectedPlot?.plotId);
    console.log('==========================================');
    // ========== 打印代码结束 ==========
  }
};
</script>

<style scoped>
.yield-indicator {
  display: flex;
  align-items: flex-start;
  justify-content: flex-start;
  gap: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  margin-top: 5px;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  padding: 20px;
  background-color: #fff;
  min-height: 200px;
  flex-wrap: wrap;
}

.input-card {
  min-width: 180px;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 16px;
  text-align: center;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  position: relative;
  margin-bottom: 10px;
}

.input-card:hover {
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.card-header {
  font-size: 14px;
  color: #666;
  font-weight: 500;
  margin-bottom: 8px;
  text-align: left;
}

.main-value {
  font-size: 24px;
  font-weight: bold;
  color: #2f5496;
}

.missing-tip {
  position: absolute;
  bottom: 5px;
  left: 0;
  right: 0;
  font-size: 12px;
  color: #f5222d;
  line-height: 1.2;
  padding: 0 5px;
}

.fertilizer-select-card {
  min-width: 200px;
}

.fertilizer-combination-card {
  text-align: left;
}

.fertilizer-total {
  text-align: right;
  font-weight: 500;
  color: #2f5496;
  font-size: 14px;
}
.fertilizer-table-container {
  height: 460px;      /* 🔥 你想要的固定高度，随便设，比如 200/240/300px */
  margin-bottom: 10px;
}
.safety-coeff-display {
  font-size: 13px;
  color: #555;
  margin-bottom: 8px;
}

</style>
