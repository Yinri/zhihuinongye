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
import { getVarietyHistoryByVarietyId } from '../base.api';
import { useCropVarietyStore } from '@/store/selectStore';
import { storeToRefs } from 'pinia';
import { message } from 'ant-design-vue';

export default {
  name: 'YieldCalcBasis',
  // 1. 修复 Pinia 引入方式：Vue2 选项式 API 需通过 setup 注入
  setup() {
    const cropStore = useCropVarietyStore();
    const { selected } = storeToRefs(cropStore); // 保持 selected 响应式
    return {
      selected // 暴露给选项式 API 使用
    };
  },
  data() {
    const currentYear = new Date().getFullYear();
    return {
      showYearInput: false,
      threeYears: [currentYear - 3, currentYear - 2, currentYear - 1], // 前三年年份
      yearValues: [undefined, undefined, undefined], // 初始化改为 undefined（避免 null 警告）
      avgThreeYearYield: null,
      increaseRate: '12' // 递增率默认值
    };
  },
  watch: {
    // 监听 Pinia 中选中品种的 ID 变化
    'selected.id': {
      immediate: true, // 组件初始化时立即执行一次
      handler(newId, oldId) {
        // 新ID存在且与旧ID不同时，才更新数据
        if (newId && newId !== oldId) {
          this.fetchAndUpdateAvg(); // 封装拉取数据和计算平均的逻辑
        } else if (newId) {
          // 初始化时已有品种ID，直接加载
          this.fetchAndUpdateAvg();
        } else {
          // 未选择品种时，清空数据
          this.avgThreeYearYield = null;
          this.yearValues = [undefined, undefined, undefined];
        }
      }
    }
  },
  methods: {
    async fetchAndUpdateAvg() {
      try {
        // 1. 拉取当前品种的历史产量
        const response = await getVarietyHistoryByVarietyId(
          this.selected.id,
          1,
          10,
          {}
        );

        // 2. 筛选当前品种数据（兼容不同响应格式）
        const records = response?.data?.records || response?.records || [];
        const currentVarietyYields = records.filter(
          item => String(item.varietyId).trim() === String(this.selected.id).trim()
        );

        // 3. 匹配前三年数据
        this.yearValues = this.threeYears.map(targetYear => {
          const targetYearStr = String(targetYear).trim();
          const matchedItem = currentVarietyYields.find(
            item => String(item.year).trim() === targetYearStr && item.yield != null
          );
          return matchedItem ? matchedItem.yield : undefined;
        });

        // 4. 自动计算平均单产（如果三年数据都存在）
        this.autoCalculateAvg();

      } catch (error) {
        console.error('切换品种后拉取数据失败：', error);
        this.avgThreeYearYield = null;
        this.yearValues = [undefined, undefined, undefined];
      }
    },

    // 自动计算平均单产（复用原有逻辑，增加自动触发）
    autoCalculateAvg() {
      const validValues = this.yearValues.filter(v =>
        v !== undefined && v !== null && !isNaN(Number(v))
      );
      // 有数据就计算，无需用户手动点击确认
      if (validValues.length > 0) {
        const sum = validValues.reduce((total, val) => total + Number(val), 0);
        this.avgThreeYearYield = (sum / validValues.length).toFixed(1);
      } else {
        this.avgThreeYearYield = null; // 无数据时清空
      }
    },
    // 2. 优化弹窗打开逻辑：先判断品种ID，再拉取数据
    async handleShowModal() {
      // 检查是否已选择品种（避免无意义请求）
      if (!this.selected?.id) {
        alert('请先选择作物品种');
        return;
      }
      // 拉取数据后显示弹窗
      await this.fetchThreeYearYield();
      this.showYearInput = true;
    },

    // 3. 修复接口请求：传递正确参数 + 适配返回格式
    async fetchThreeYearYield() {
      try {
        // 接口参数：从 Pinia 获取 varietyId + 分页参数（后端默认 pageNo=1, pageSize=10）
        const response = await getVarietyHistoryByVarietyId({
          varietyId: this.selected.id, // 动态获取选中品种ID
          pageNo: 1,
          pageSize: 10
        });
        const currentVarietyYields = response.records.filter(
          item => item.varietyId.toString() === this.selected.id
        );
        // 筛选前三年数据（匹配年份 + 取单产字段，需与后端字段一致，这里假设为 yieldPerAcre）
        this.yearValues = this.threeYears.map(targetYear => {
          // 关键：两者都转为字符串后再匹配（彻底避免类型/格式问题）
          const targetYearStr = String(targetYear); // 如 2022 → "2022"
          const matchedItem = currentVarietyYields.find(
            item => String(item.year) === targetYearStr && item.yield != null
          );
          return matchedItem ? matchedItem.yield : undefined;
        });
      } catch (error) {
        // 4. 优化错误提示：使用项目统一的消息组件
        alert('拉取历史产量数据失败，请重试');
        console.error('历史产量请求错误：', error);
        this.yearValues = [undefined, undefined, undefined]; // 失败后重置
      }
    },

    // 5. 优化平均值计算：兼容 0 作为有效数据
    calculateAvg() {
      const validValues = this.yearValues.filter(v =>
        v !== undefined && v !== null && !isNaN(Number(v))
      );
      if (validValues.length === 0) {
        message.warning('请至少录入一年的单产数据');
        return;
      }
      const sum = validValues.reduce((total, val) => total + Number(val), 0);
      this.avgThreeYearYield = (sum / validValues.length).toFixed(1);
      this.showYearInput = false;
      message.success(`已计算 ${validValues.length} 年平均单产`);
    },

    // 原有关闭弹窗方法：增加数据重置
    closeModal() {
      this.showYearInput = false;
      // 可选：关闭时重置输入框（根据需求决定是否保留）
      // this.yearValues = [undefined, undefined, undefined];
    }
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
  gap: 200px;
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

.info-card {
  background-color: #f0f0f0;
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
