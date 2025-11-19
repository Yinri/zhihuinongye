<template>
  <div class="yield-calc-basis">
    <h3 class="section-title">产量计算依据</h3>
    <div class="cards-container">
      <div class="basis-cards">
        <!-- 前三年单产模块 -->
        <div class="basis-card">
          <p class="card-title">前三年平均单产</p>
          <p class="card-value">{{ avgThreeYearYield || '未录入' }} kg / 亩</p>
          <button @click="handleShowModal" class="edit-btn">录入数据</button>
          <div class="increase-rate-container">
            <label class="rate-label">递增率：</label>
            <select v-model="increaseRate" class="rate-select">
              <option value="10">10%</option>
              <option value="12">12%</option>
              <option value="15">15%</option>
            </select>
          </div>
        </div>

        <!-- 气象预测模块 -->
        <div class="basis-card info-card">
          <p class="card-title">当年气象预测</p>
          <p class="card-desc">作为目标产量调整参考</p>
        </div>

        <!-- 土壤肥力均值模块 -->
        <div class="basis-card info-card">
          <p class="card-title">土壤肥力均值</p>
          <p class="card-desc">作为目标产量调整参考</p>
        </div>

        <!-- 种子计算参数卡片 -->
        <div class="basis-card">
          <p class="card-title">种子计算参数</p>
          <button @click="handleSeedParamModal" class="edit-btn">录入/修改参数</button>
          <div class="param-values">
            <p class="param-item">收获系数：{{ seedParams.harvestCoefficient || '未设置' }}</p>
            <p class="param-item">田间保苗率：{{ seedParams.seedlingRate || '未设置' }}%</p>
            <p class="param-item">结实率：{{ seedParams.settingRate || '未设置' }}%</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 弹窗：录入前三年具体单产 -->
    <div v-if="showYearInput" class="modal-overlay">
      <!-- 保持不变 -->
      <div class="modal">
        <div class="modal-header">
          <h4>录入前三年单产数据</h4>
          <button @click="closeModal" class="close-btn">×</button>
        </div>
        <div class="modal-body">
          <div class="year-input-item" v-for="(year, index) in threeYears" :key="index">
            <label>{{ year }}年单产 (kg/亩)：</label>
            <input
              type="number"
              v-model.number="yearValues[index]"
              class="year-input"
              min="0"
              :placeholder="yearValues[index] === undefined ? '无历史数据' : '请输入'"
            />
          </div>
        </div>
        <div class="modal-footer">
          <button @click="closeModal" class="cancel-btn">取消</button>
          <button @click="calculateAvg" class="confirm-btn">确认</button>
        </div>
      </div>
    </div>

    <!-- 种子参数录入弹窗 -->
    <div v-if="showSeedParamModal" class="modal-overlay">
      <div class="modal">
        <div class="modal-header">
          <h4>录入种子计算参数</h4>
          <button @click="showSeedParamModal = false" class="close-btn">×</button>
        </div>
        <div class="modal-body">
          <!-- 收获系数输入 -->
          <div class="param-input-item">
            <label>收获系数：</label>
            <input
              type="number"
              v-model.number="tempSeedParams.harvestCoefficient"
              class="param-input"
              step="0.01"
              min="0.3"
              max="0.5"
              placeholder="如：0.45（范围0.3-0.5）"
            />
          </div>
          <!-- 田间保苗率输入 -->
          <div class="param-input-item">
            <label>田间保苗率（%）：</label>
            <input
              type="number"
              v-model.number="tempSeedParams.seedlingRate"
              class="param-input"
              step="5"
              min="50"
              max="100"
              placeholder="如：85（范围50-100）"
            />
          </div>
          <!-- 结实率输入 -->
          <div class="param-input-item">
            <label>结实率（%）：</label>
            <input
              type="number"
              v-model.number="tempSeedParams.settingRate"
              class="param-input"
              step="5"
              min="70"
              max="95"
              placeholder="如：85（范围70-95）"
            />
          </div>
        </div>
        <div class="modal-footer">
          <button @click="showSeedParamModal = false" class="cancel-btn">取消</button>
          <button @click="saveSeedParams" class="confirm-btn">确认</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import {
  getVarietyHistoryByVarietyId,
  addVarietyHistory,
  updateVarietyHistory,
  // 新增：种子参数相关接口
  getSeedParamsByVarietyId,
  addSeedParams,
  updateSeedParams
} from '../base.api';
import { useCropVarietyStore } from '@/store/selectStore';
import { storeToRefs } from 'pinia';
import { message } from 'ant-design-vue';

export default {
  name: 'YieldCalcBasis',
  setup() {
    const cropStore = useCropVarietyStore();
    const { selected, yieldCalcData } = storeToRefs(cropStore);
    const { updateYieldCalcData } = cropStore;
    return {
      selected,
      yieldCalcData,
      updateYieldCalcData
    };
  },
  data() {
    const currentYear = new Date().getFullYear();
    return {
      showYearInput: false,
      threeYears: [currentYear - 3, currentYear - 2, currentYear - 1],
      yearValues: [undefined, undefined, undefined],

      // 种子参数相关
      showSeedParamModal: false,
      seedParams: { // 显示用参数
        harvestCoefficient: null,
        seedlingRate: null,
        settingRate: null,
        id: null // 用于区分新增/修改
      },
      tempSeedParams: { // 弹窗临时参数
        harvestCoefficient: null,
        seedlingRate: null,
        settingRate: null
      }
    };
  },
  computed: {
    avgThreeYearYield: {
      get() {
        return this.yieldCalcData.avgThreeYearYield;
      },
      set(val) {
        this.updateYieldCalcData({ avgThreeYearYield: val });
      }
    },
    increaseRate: {
      get() {
        return this.yieldCalcData.increaseRate;
      },
      set(val) {
        this.updateYieldCalcData({ increaseRate: val });
      }
    }
  },
  watch: {
    'selected.id': {
      immediate: true,
      handler(newId, oldId) {
        if (newId && newId !== oldId) {
          this.fetchAndUpdateAvg();
          this.fetchSeedParams(); // 新增：获取种子参数
        } else if (newId) {
          this.fetchAndUpdateAvg();
          this.fetchSeedParams(); // 新增：获取种子参数
        } else {
          this.updateYieldCalcData({ avgThreeYearYield: null, increaseRate: '12' });
          this.yearValues = [undefined, undefined, undefined];
          this.seedParams = { harvestCoefficient: null, seedlingRate: null, settingRate: null, id: null };
        }
      }
    }
  },
  methods: {
    // 原有方法保持不变...
    async fetchAndUpdateAvg() {
      try {
        const response = await getVarietyHistoryByVarietyId({
          varietyId: this.selected.id,
          pageNo: 1,
          pageSize: 10
        });

        const records = response?.data?.records || response?.records || [];
        const currentVarietyYields = records.filter(
          item => Number(item.varietyId) === Number(this.selected.id)
        );

        this.yearValues = this.threeYears.map(targetYear => {
          const matchedItem = currentVarietyYields.find(
            item => Number(item.year) === Number(targetYear) && item.yield != null
          );
          return matchedItem ? matchedItem.yield : undefined;
        });

        this.autoCalculateAvg();

      } catch (error) {
        console.error('切换品种后拉取数据失败：', error);
        this.avgThreeYearYield = null;
        this.yearValues = [undefined, undefined, undefined];
      }
    },

    autoCalculateAvg() {
      const validValues = this.yearValues.filter(v =>
        v !== undefined && v !== null && !isNaN(Number(v))
      );
      if (validValues.length > 0) {
        const sum = validValues.reduce((total, val) => total + Number(val), 0);
        this.avgThreeYearYield = (sum / validValues.length).toFixed(1);
      } else {
        this.avgThreeYearYield = null;
      }
    },

    async handleShowModal() {
      if (!this.selected?.id) {
        message.warning('请先选择作物品种');
        return;
      }
      await this.fetchThreeYearYield();
      this.showYearInput = true;
    },

    async fetchThreeYearYield() {
      try {
        const response = await getVarietyHistoryByVarietyId({
          varietyId: this.selected.id,
          pageNo: 1,
          pageSize: 10
        });
        const currentVarietyYields = response.records.filter(
          item => Number(item.varietyId) === Number(this.selected.id)
        );
        this.yearValues = this.threeYears.map(targetYear => {
          const matchedItem = currentVarietyYields.find(
            item => Number(item.year) === Number(targetYear) && item.yield != null
          );
          return matchedItem ? matchedItem.yield : undefined;
        });
      } catch (error) {
        message.error('拉取历史产量数据失败，请重试');
        console.error('历史产量请求错误：', error);
        this.yearValues = [undefined, undefined, undefined];
      }
    },

    calculateAvg() {
      const validValues = this.yearValues.filter(v =>
        v !== undefined && v !== null && !isNaN(Number(v))
      );
      if (validValues.length === 0) {
        message.warning('请至少录入一年的单产数据');
        return;
      }
      const submitData = this.threeYears.map((year, index) => {
        const yieldValue = this.yearValues[index];
        return yieldValue !== undefined && yieldValue !== null && !isNaN(Number(yieldValue))
          ? {
            varietyId: this.selected.id,
            year: year,
            yield: Number(yieldValue),
          }
          : null;
      }).filter(item => item !== null);

      this.submitHistoryYield(submitData, validValues);
    },

    async submitHistoryYield(submitData, validValues) {
      try {
        const historyRes = await getVarietyHistoryByVarietyId({
          varietyId: this.selected.id,
          pageSize: 100
        });
        const existingYields = historyRes?.data?.records || historyRes?.records || [];

        const addList = [];
        const updateList = [];
        submitData.forEach(item => {
          const existingItem = existingYields.find(
            y => Number(y.varietyId) === Number(this.selected.id) && Number(y.year) === Number(item.year)
          );

          if (existingItem) {
            updateList.push({
              id: existingItem.id,
              varietyId: this.selected.id,
              year: item.year,
              yield: item.yield,
              plot: item.plot || existingItem.plot
            });
          } else {
            addList.push({
              varietyId: this.selected.id,
              year: item.year,
              yield: item.yield,
              plot: item.plot
            });
          }
        });

        let addCount = 0;
        for (const item of addList) {
          try {
            await addVarietyHistory(item);
            addCount++;
          } catch (err) {
            this.handleDbError(err, '新增', item.year);
            return;
          }
        }

        let updateCount = 0;
        for (const item of updateList) {
          try {
            await updateVarietyHistory(item);
            updateCount++;
          } catch (err) {
            this.handleDbError(err, '更新', item.year);
            return;
          }
        }

        const sum = validValues.reduce((total, val) => total + Number(val), 0);
        this.avgThreeYearYield = (sum / validValues.length).toFixed(1);
        this.showYearInput = false;
        message.success(`成功新增${addCount}条，更新${updateCount}条数据`);

      } catch (error) {
        console.error('提交流程异常：', error);
        message.error('系统异常，请稍后重试');
      }
    },

    closeModal() {
      this.showYearInput = false;
    },

    handleDbError(error, operation, year) {
      const errorMsg = error?.response?.data?.msg || error?.message || '';
      let userMsg = '';
      if (errorMsg.includes('uk_variety_year') || errorMsg.includes('Duplicate entry')) {
        userMsg = `【${year}年】数据已存在，无法${operation}（同一品种同一年份仅允许一条记录）`;
      } else if (errorMsg.includes('NOT NULL')) {
        userMsg = `【${year}年】必填字段缺失（品种ID、年份、单产不能为空）`;
      } else {
        userMsg = `【${year}年】${operation}失败：${errorMsg.slice(0, 50)}`;
      }
      message.error(userMsg);
      console.error(`【${year}年】错误详情：`, error);
    },

    // 新增：种子参数相关方法
    async handleSeedParamModal() {
      if (!this.selected?.id) {
        message.warning('请先选择作物品种');
        return;
      }
      // 打开弹窗前先拉取最新数据
      await this.fetchSeedParams();
      // 初始化弹窗表单（用当前参数值填充）
      this.tempSeedParams = {...this.seedParams};
      this.showSeedParamModal = true;
    },

    // 从接口获取种子参数
    async fetchSeedParams() {
      console.log('当前选中品种完整信息：', this.selected);
      const varietyId = this.selected?.id;
      console.log('传递给后端的id：', varietyId, '类型：', typeof varietyId);

      if (!varietyId) {
        console.error('品种ID为空，无法查询');
        this.seedParams = { id: null, harvestCoefficient: null, seedlingRate: null, settingRate: null };
        return;
      }

      try {
        const response = await getSeedParamsByVarietyId(varietyId);
        console.log('接口返回结果：', response);

        // 修正判断逻辑：直接判断response是否存在（因为后端返回的是数据对象本身）
        if (response && response.id) { // 只要有id，说明数据有效
          this.seedParams = {
            id: response.id, // 直接从response中取字段，无需response.data
            harvestCoefficient: response.harvestCoefficient,
            seedlingRate: response.seedlingSurvivalRate, // 与后端实体类字段对应
            settingRate: response.seedSettingRate
          };
          console.log('种子参数加载成功：', this.seedParams);
        } else {
          // 确实无数据（response为空或无id）
          this.seedParams = { id: null, harvestCoefficient: null, seedlingRate: null, settingRate: null };
          console.log('该品种未设置种子参数');
        }
      } catch (error) {
        console.error('接口调用失败：', error);
        message.error('获取种子参数失败，请重试');
        this.seedParams = { id: null, harvestCoefficient: null, seedlingRate: null, settingRate: null };
      }
    },
    // 保存种子参数（支持新增和修改）
    async saveSeedParams() {
      const {harvestCoefficient, seedlingRate, settingRate, id} = this.tempSeedParams;

      // 校验输入
      if (harvestCoefficient === null || harvestCoefficient < 0.3 || harvestCoefficient > 0.5) {
        message.warning('请输入0.3-0.5之间的收获系数');
        return;
      }
      if (seedlingRate === null || seedlingRate < 50 || seedlingRate > 100) {
        message.warning('请输入50-100之间的田间保苗率');
        return;
      }
      if (settingRate === null || settingRate < 70 || settingRate > 95) {
        message.warning('请输入70-95之间的结实率');
        return;
      }

      // 构造提交数据
      const submitData = {
        varietyId: this.selected.id,
        harvestCoefficient,
        seedlingSurvivalRate: seedlingRate, // 与后端字段对应
        seedSettingRate: settingRate
      };

      try {
        if (id) {
          // 有ID则为修改
          submitData.id = id;
          await updateSeedParams(submitData);
          message.success('种子参数更新成功');
        } else {
          // 无ID则为新增
          await addSeedParams(submitData);
          message.success('种子参数新增成功');
        }
        // 关闭弹窗并刷新数据
        this.showSeedParamModal = false;
        this.fetchSeedParams();
      } catch (error) {
        console.error('保存种子参数失败：', error);
        const errorMsg = error?.response?.data?.msg || '保存失败，请重试';
        message.error(errorMsg);
      }
    }
  }
};
</script>

<style scoped>
/* 样式保持不变 */
.yield-calc-basis {
  border: 1px solid #d9d9d9;
  margin-top: 5px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  border-radius: 8px;
  padding: 16px 8px;
  background-color: #fff;
  height: 395px;
}

.cards-container {
  display: flex;
  justify-content: center;
  width: 100%;
  min-height: 300px;
  align-items: center;
}

.section-title {
  font-size: 14px;
  font-weight: bold;
  color: #333;
  margin: 0 0 16px 0;
  padding-left: 5px;
}

.basis-cards {
  display: flex;
  gap: 40px;
  width: 100%;
  justify-content: center;
  flex-wrap: wrap;
  padding: 0;
}

.basis-card {
  min-width: 200px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border: none;
  border-radius: 12px;
  padding: 24px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  min-height: 200px;
}

.basis-card:hover {
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.info-card {
  background: linear-gradient(135deg, #f0f8fb 0%, #e8f4f8 100%);
}

.card-title {
  font-size: 14px;
  color: #333;
  font-weight: bold;
  margin: 0;
}

.card-value {
  font-size: 22px;
  color: #2f5496;
  font-weight: 600;
  margin: 8px 0;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.edit-btn {
  padding: 8px 20px;
  background-color: #2f5496;
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: background-color 0.3s ease;
}

.edit-btn:hover {
  background-color: #1f3a6b;
}

.increase-rate-container {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.rate-label {
  font-size: 12px;
  color: #666;
}

.rate-select {
  padding: 4px 8px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 12px;
}

.card-desc {
  font-size: 12px;
  color: #666;
  line-height: 1.5;
  margin: 0;
  padding-top: 8px;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background-color: white;
  border-radius: 8px;
  width: 400px;
  padding: 16px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #eee;
}

.modal-header h4 {
  margin: 0;
  font-size: 16px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  color: #666;
}

.modal-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
}

.year-input-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.year-input-item label {
  font-size: 14px;
  width: 120px;
  text-align: right;
}

.year-input {
  flex: 1;
  padding: 6px 8px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
}

.modal-footer {
  display: flex;
  justify-content: center;
  gap: 10px;
}

.cancel-btn, .confirm-btn {
  padding: 6px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.cancel-btn {
  border: 1px solid #d9d9d9;
  background-color: white;
  color: #333;
}

.confirm-btn {
  border: none;
  background-color: #1890ff;
  color: white;
}

.param-values {
  margin-top: 16px;
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.param-item {
  font-size: 13px;
  color: #666;
  margin: 0;
  text-align: left;
  padding-left: 8px;
}

.param-input-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.param-input-item label {
  font-size: 14px;
  width: 120px;
  text-align: right;
}

.param-input {
  flex: 1;
  padding: 6px 8px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 13px;
}
</style>
