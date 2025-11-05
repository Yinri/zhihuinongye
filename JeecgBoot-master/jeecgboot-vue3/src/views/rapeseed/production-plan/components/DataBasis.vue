<template>
  <div class="yield-calc-basis">
    <h3 class="section-title">产量计算依据</h3>
    <div class="cards-container">
      <div class="basis-cards">
        <!-- 前三年单产模块 -->
        <div class="basis-card">
          <p class="card-title">前三年平均单产</p>
          <p class="card-value">{{ avgThreeYearYield || '未录入' }} kg / 亩</p>
          <button @click="showYearInput = true" class="edit-btn">录入数据</button>
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
          <button @click="showYearInput = false" class="close-btn">×</button>
        </div>
        <div class="modal-body">
          <div class="year-input-item" v-for="(year, index) in threeYears" :key="index">
            <label>{{ year }}年单产 (kg/亩)：</label>
            <input
              type="number"
              v-model.number="yearValues[index]"
              class="year-input"
              min="0"
              placeholder="请输入"
            />
          </div>
        </div>
        <div class="modal-footer">
          <button @click="showYearInput = false" class="cancel-btn">取消</button>
          <button @click="calculateAvg" class="confirm-btn">确认</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'YieldCalcBasis',
  data() {
    const currentYear = new Date().getFullYear();
    return {
      showYearInput: false,
      threeYears: [currentYear - 3, currentYear - 2, currentYear - 1],
      yearValues: [null, null, null],
      avgThreeYearYield: null,
      increaseRate: '12'
    };
  },
  methods: {
    calculateAvg() {
      const validValues = this.yearValues.filter(v => v !== null && v !== '');
      if (validValues.length !== 3) {
        alert('请完善三年的单产数据');
        return;
      }
      const sum = validValues.reduce((total, val) => total + val, 0);
      this.avgThreeYearYield = (sum / 3).toFixed(1);
      this.showYearInput = false;
    }
  }
};
</script>

<style scoped>
.yield-calc-basis {
  border: 1px solid #d9d9d9;
  margin-top: 5px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  border-radius: 8px;
  padding: 16px 8px;
  background-color: #fff;
}

.cards-container {
  display: flex;
  justify-content: center;
  width: 100%;
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
  gap: 200px; /* 调大板块之间的间距 */
  width: 90%;
  justify-content: center;
  flex-wrap: wrap;
}

.basis-card {
  min-width: 220px;
  background-color: #f0f0f0;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

/* 统一所有卡片的样式，确保一致性 */
.info-card {
  background-color: #f0f0f0; /* 与前三年卡片样式统一 */
}

.card-title {
  font-size: 14px;
  color: #333;
  font-weight: bold;
  margin: 0;
}

.card-value {
  font-size: 18px;
  color: #333;
  font-weight: bold;
  margin: 0;
}

.edit-btn {
  padding: 6px 16px;
  background-color: #2f5496;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
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

/* 弹窗样式保持不变 */
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
