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
      </div>
    </div>

    <!-- 弹窗：录入前三年具体单产 -->
    <div v-if="showYearInput" class="modal-overlay">
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
  </div>
</template>

<script>
import { getVarietyHistoryByVarietyId, addVarietyHistory, updateVarietyHistory } from '../base.api';
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
      yearValues: [undefined, undefined, undefined]
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
        } else if (newId) {
          this.fetchAndUpdateAvg();
        } else {
          this.updateYieldCalcData({ avgThreeYearYield: null, increaseRate: '12' });
          this.yearValues = [undefined, undefined, undefined];
        }
      }
    }
  },
  methods: {
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
  }
};
</script>

<style scoped>
/* 样式部分无修改，保持原代码 */
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
  min-height: 300px; /* 可根据卡片高度调整，确保有足够空间居中 */
  align-items: center; /* 垂直居中对齐子元素 */
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
  gap: 40px; /* 原200px过大，改为40px更紧凑协调 */
  width: 100%;
  justify-content: center;
  flex-wrap: wrap;
  padding: 0;
}

.basis-card {
  min-width: 200px;
  /* 渐变背景+圆角优化 */
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border: none;
  border-radius: 12px;
  padding: 24px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  /* 增强阴影层次感 */
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  /* 过渡动画（hover用） */
  transition: all 0.3s ease;
  min-height: 200px;
}
/* hover交互效果 */
.basis-card:hover {
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}
.info-card {
  /* 浅色渐变，与可编辑卡片区分 */
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
  color: #2f5496; /* 主色突出数据 */
  font-weight: 600;
  margin: 8px 0;
  /* 增加文字阴影，更醒目 */
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}
.edit-btn {
  padding: 8px 20px;
  background-color: #2f5496;
  color: white;
  border: none;
  border-radius: 20px; /* 圆角按钮 */
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: background-color 0.3s ease;
}
.edit-btn:hover {
  background-color: #1f3a6b; /* hover加深 */
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
</style>
