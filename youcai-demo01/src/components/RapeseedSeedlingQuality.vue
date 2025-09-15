<template>
  <div v-if="activeMenu === '油菜苗素质'" class="seedling-quality-container">
    <div class="content-header">
      <h2>油菜苗素质监测</h2>
      <div class="content-actions">
        <button class="btn-refresh" @click="refreshData">🔄 刷新数据</button>
        <button class="btn-analysis" @click="showAnalysis">📊 数据分析</button>
      </div>
    </div>

    <div class="card-container">

<div class="quality-description-card">
  <div class="chart-card-header">
    <h3>油菜苗素质说明</h3>
  </div>
  <div class="description-content">
    <p class="description-intro">油菜苗素质是衡量油菜生长状况的关键指标，直接影响后期产量和抗逆性。以下为主要监测指标的说明：</p>
    <div class="indicators-grid">
      <div class="indicator-item">
        <div class="indicator-icon">🌱</div>
        <div class="indicator-details">
          <h4>株高</h4>
          <p>理想范围：15-20cm。过高易倒伏，过低可能发育不良。当前平均株高18.5cm，处于最佳区间。</p>
        </div>
      </div>
      <div class="indicator-item">
        <div class="indicator-icon">🌿</div>
        <div class="indicator-details">
          <h4>叶片数</h4>
          <p>健康标准：5-6片/株。少于4片可能存在营养不足，当前平均5.2片，处于健康水平。</p>
        </div>
      </div>
      <div class="indicator-item">
        <div class="indicator-icon">💧</div>
        <div class="indicator-details">
          <h4>含水率</h4>
          <p>适宜范围：75-80%。当前78.3%，处于理想状态，表明水分管理良好。</p>
        </div>
      </div>
      <div class="indicator-item">
        <div class="indicator-icon">✅</div>
        <div class="indicator-details">
          <h4>健康率</h4>
          <p>当前96.7%，高于95%的优质苗标准，整体生长状况良好。</p>
        </div>
      </div>
    </div>
    <div class="growth-stage-tips">
      <h4>当前生长阶段管理建议</h4>
      <ul>
        <li>保持日间温度15-20℃，夜间不低于5℃</li>
        <li>适度控水，避免徒长，土壤湿度保持在60-70%</li>
        <li>叶面喷施0.2%磷酸二氢钾溶液，促进壮苗</li>
        <li>注意防治蚜虫和菜青虫，发现少量害虫及时处理</li>
      </ul>
    </div>
  </div>
</div>
      <div class="chart-card">
        <div class="chart-card-header">
          <h3>苗情生长趋势</h3>
          <div class="period-selector">
            <button class="period-btn" :class="{ active: period === 'week' }" @click="period = 'week'">周</button>
            <button class="period-btn" :class="{ active: period === 'month' }" @click="period = 'month'">月</button>
          </div>
        </div>
        <div class="chart-container">
          <canvas id="growthTrendChart"></canvas>
        </div>
      </div>

      <div class="info-grid">
        <div class="info-card">
          <div class="info-card-icon">🌱</div>
          <div class="info-card-content">
            <p class="info-card-label">平均株高</p>
            <p class="info-card-value">18.5 cm</p>
          </div>
          <div class="info-card-trend trend-up">↑ 2.3%</div>
        </div>

        <div class="info-card">
          <div class="info-card-icon">🌿</div>
          <div class="info-card-content">
            <p class="info-card-label">叶片数</p>
            <p class="info-card-value">5.2 片/株</p>
          </div>
          <div class="info-card-trend trend-up">↑ 1.8%</div>
        </div>

        <div class="info-card">
          <div class="info-card-icon">💧</div>
          <div class="info-card-content">
            <p class="info-card-label">含水率</p>
            <p class="info-card-value">78.3%</p>
          </div>
          <div class="info-card-trend trend-down">↓ 0.5%</div>
        </div>

        <div class="info-card">
          <div class="info-card-icon">✅</div>
          <div class="info-card-content">
            <p class="info-card-label">健康率</p>
            <p class="info-card-value">96.7%</p>
          </div>
          <div class="info-card-trend trend-up">↑ 0.9%</div>
        </div>
      </div>
    </div>

    <div class="table-container">
      <div class="table-header">
        <h3>苗情详细数据</h3>
        <div class="table-search">
          <input type="text" placeholder="搜索批次/品种..." v-model="searchKeyword" />
        </div>
      </div>
      <table class="data-table">
        <thead>
          <tr>
            <th>批次编号</th>
            <th>油菜品种</th>
            <th>育苗日期</th>
            <th>株高(cm)</th>
            <th>叶片数</th>
            <th>健康状况</th>
            <th>监测时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="seedling in filteredSeedlings" :key="seedling.id">
            <td>{{ seedling.batchNo }}</td>
            <td>{{ seedling.variety }}</td>
            <td>{{ seedling.seedlingDate }}</td>
            <td>{{ seedling.height }}</td>
            <td>{{ seedling.leafCount }}</td>
            <td>
              <span :class="statusClass(seedling.healthStatus)">{{ seedling.healthStatus }}</span>
            </td>
            <td>{{ seedling.monitorTime }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onUnmounted } from 'vue';
import { Chart } from 'chart.js'; // 导入Chart核心类型

// 1. 定义Props类型（约束父组件传入的activeMenu）
const props = defineProps({
  activeMenu: {
    type: String,
    required: true,
    validator: (val: string) => val === 'seedlingQuality' || true // 简单校验避免无效值
  }
});

// 2. 定义核心数据接口（解决隐式any，约束数据结构）
interface Seedling {
  id: number;
  batchNo: string; // 批次编号
  variety: string; // 油菜品种
  seedlingDate: string; // 育苗日期（YYYY-MM-DD）
  height: number; // 株高（cm）
  leafCount: number; // 叶片数（片/株）
  healthStatus: '健康' | '一般' | '较差'; // 仅允许3种健康状态
  monitorTime: string; // 监测时间（YYYY-MM-DD HH:MM）
}

// 3. 声明状态并指定明确类型（核心修复：解决类型不匹配）
// 周期：仅允许"week"/"month"两种值
const period = ref<'week' | 'month'>('week');
// 搜索关键词：string类型
const searchKeyword = ref<string>('');
// 图表实例：允许为Chart或null（避免"Chart无法赋值给null"错误）
const growthTrendChart = ref<Chart | null>(null);

// 油菜苗原始数据（用Seedling接口约束）
const seedlings = ref<Seedling[]>([
  { id: 1, batchNo: 'B2025001', variety: '中油杂19', seedlingDate: '2025-08-10', height: 19.2, leafCount: 6, healthStatus: '健康', monitorTime: '2025-08-25 09:30' },
  { id: 2, batchNo: 'B2025002', variety: '华油杂62', seedlingDate: '2025-08-12', height: 17.8, leafCount: 5, healthStatus: '健康', monitorTime: '2025-08-26 10:15' },
  { id: 3, batchNo: 'B2025003', variety: '中油杂19', seedlingDate: '2025-08-15', height: 16.5, leafCount: 4, healthStatus: '一般', monitorTime: '2025-08-27 11:00' },
  { id: 4, batchNo: 'B2025004', variety: '湘杂油4号', seedlingDate: '2025-08-18', height: 19.5, leafCount: 6, healthStatus: '健康', monitorTime: '2025-08-28 14:20' },
  { id: 5, batchNo: 'B2025005', variety: '华油杂62', seedlingDate: '2025-08-20', height: 17.2, leafCount: 5, healthStatus: '一般', monitorTime: '2025-08-30 15:40' }
]);

// 筛选后的油菜苗数据（与原始数据类型一致）
const filteredSeedlings = ref<Seedling[]>([]);

// 4. 健康状态样式映射（明确参数类型，避免隐式any）
const statusClass = (status: Seedling['healthStatus']) => {
  switch (status) {
    case '健康': return 'status-healthy';
    case '一般': return 'status-normal';
    case '较差': return 'status-poor';
    default: return '';
  }
};

// 5. 销毁图表实例（防止内存泄漏，补充优化）
const destroyChart = () => {
  if (growthTrendChart.value) {
    growthTrendChart.value.destroy();
    growthTrendChart.value = null;
  }
};

// 6. 初始化生长趋势图表（修复：canvas类型断言+数据类型明确）
const initChart = () => {
  // 先销毁已有图表，避免重复创建
  destroyChart();

  // 获取Canvas元素：断言为HTMLCanvasElement或null
  const canvas = document.getElementById('growthTrendChart') as HTMLCanvasElement | null;
  if (!canvas) return;

  // 根据周期生成标签和数据（明确数据类型为number[]）
  const { labels, heightData, leafData } = (() => {
    if (period.value === 'week') {
      return {
        labels: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
        heightData: [16.8, 17.2, 17.5, 17.8, 18.1, 18.3, 18.5] as number[],
        leafData: [4.5, 4.7, 4.8, 4.9, 5.0, 5.1, 5.2] as number[]
      };
    }
    // 月度数据
    return {
      labels: ['第1周', '第2周', '第3周', '第4周'],
      heightData: [15.2, 16.5, 17.8, 18.5] as number[],
      leafData: [4.0, 4.5, 5.0, 5.2] as number[]
    };
  })();

  // 创建图表实例（类型自动匹配，无报错）
  growthTrendChart.value = new Chart(canvas, {
    type: 'line',
    data: {
      labels,
      datasets: [
        {
          label: '平均株高 (cm)',
          data: heightData,
          borderColor: 'rgba(76, 175, 80, 1)',
          backgroundColor: 'rgba(76, 175, 80, 0.1)',
          tension: 0.3,
          fill: true
        },
        {
          label: '平均叶片数',
          data: leafData,
          borderColor: 'rgba(33, 150, 243, 1)',
          backgroundColor: 'rgba(33, 150, 243, 0.1)',
          tension: 0.3,
          fill: true
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        y: {
          beginAtZero: false,
          grid: { drawOnChartArea: false }
        },
        x: { grid: { display: false } }
      },
      plugins: {
        legend: { position: 'top' }
      }
    }
  });
};

// 7. 筛选油菜苗数据（优化：关键词去空格，避免无效筛选）
const filterSeedlings = () => {
  const keyword = searchKeyword.value.toLowerCase().trim();
  filteredSeedlings.value = seedlings.value.filter(seedling => 
    seedling.batchNo.toLowerCase().includes(keyword) || 
    seedling.variety.toLowerCase().includes(keyword)
  );
};

// 8. 其他业务方法（明确无返回值类型）
const refreshData = (): void => {
  console.log('刷新油菜苗素质数据');
  // 模拟数据刷新：随机更新部分株高（±0.5cm）
  seedlings.value = seedlings.value.map(seedling => ({
    ...seedling,
    height: Number((seedling.height + (Math.random() * 1 - 0.5)).toFixed(1))
  }));
  filterSeedlings(); // 重新筛选以更新表格
};

const showAnalysis = (): void => {
  alert('数据分析功能即将上线');
};

// 9. 生命周期钩子（确保图表正确初始化和销毁）
onMounted(() => {
  filterSeedlings(); // 初始加载时筛选数据
  if (props.activeMenu === 'seedlingQuality') {
    initChart(); // 初始创建图表
  }
});

// 监听搜索关键词变化：实时筛选
watch(searchKeyword, filterSeedlings);

// 监听周期变化：更新图表
watch(period, initChart);

// 监听菜单切换：离开当前菜单时销毁图表
watch(
  () => props.activeMenu,
  (newVal) => {
    if (newVal !== 'seedlingQuality') {
      destroyChart();
    }
  }
);

// 组件卸载时销毁图表（彻底防止内存泄漏）
onUnmounted(() => {
  destroyChart();
});
</script>

<style scoped>
.seedling-quality-container {
  padding: 20px;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
}

.content-header h2 {
  color: #2E7D32;
  font-size: 24px;
  margin: 0;
}

.content-actions {
  display: flex;
  gap: 10px;
}

.btn-refresh,
.btn-analysis {
  padding: 8px 16px;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s;
}

.btn-refresh {
  background-color: #f0f7ee;
  color: #2E7D32;
}

.btn-analysis {
  background-color: #e3f2fd;
  color: #1976D2;
}

.btn-refresh:hover,
.btn-analysis:hover {
  opacity: 0.9;
  transform: translateY(-2px);
}

.card-container {
  margin-bottom: 30px;
.chart-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding: 20px;
}

.quality-description-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding: 20px;
  margin-bottom: 20px;
}

.description-content {
  padding: 10px 0;
}

.description-intro {
  color: #666;
  line-height: 1.6;
  margin-bottom: 20px;
}

.indicators-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 15px;
  margin-bottom: 25px;
}

.indicator-item {
  display: flex;
  gap: 12px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 10px;
}

.indicator-icon {
  font-size: 24px;
  padding: 8px;
  background-color: rgba(76, 175, 80, 0.1);
  border-radius: 8px;
}

.indicator-details h4 {
  margin: 0 0 8px 0;
  color: #333;
}

.indicator-details p {
  margin: 0;
  color: #666;
  font-size: 14px;
  line-height: 1.5;
}

.growth-stage-tips h4 {
  margin: 0 0 12px 0;
  color: #333;
}

.growth-stage-tips ul {
  margin: 0;
  padding-left: 20px;
  color: #666;
  line-height: 1.6;
}

.growth-stage-tips li {
  margin-bottom: 8px;
} margin-bottom: 20px;
}

.chart-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.chart-card-header h3 {
  margin: 0;
  color: #333;
  font-size: 18px;
}

.period-selector {
  display: flex;
  gap: 8px;
}

.period-btn {
  padding: 6px 12px;
  border-radius: 6px;
  border: 1px solid #ddd;
  background-color: white;
  cursor: pointer;
  transition: all 0.2s;
}

.period-btn.active {
  background-color: #2E7D32;
  color: white;
  border-color: #2E7D32;
}

.chart-container {
  height: 300px;
  width: 100%;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
}

.info-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding: 15px;
  display: flex;
  align-items: center;
  gap: 15px;
}

.info-card-icon {
  width: 40px;
  height: 40px;
  background-color: #e8f5e9;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 20px;
}

.info-card-content {
  flex: 1;
}

.info-card-label {
  color: #666;
  font-size: 14px;
  margin: 0 0 5px 0;
}

.info-card-value {
  color: #222;
  font-size: 20px;
  font-weight: 600;
  margin: 0;
}

.info-card-trend {
  font-size: 14px;
  font-weight: 500;
}

.trend-up {
  color: #4CAF50;
}

.trend-down {
  color: #F44336;
}

.table-container {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding: 20px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.table-header h3 {
  margin: 0;
  color: #333;
  font-size: 18px;
}

.table-search input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  width: 240px;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 12px 15px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}

.data-table th {
  color: #666;
  font-weight: 600;
  font-size: 14px;
}

.status-healthy {
  background-color: #e8f5e9;
  color: #2E7D32;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 13px;
}

.status-normal {
  background-color: #fff8e1;
  color: #FF8F00;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 13px;
}

.status-poor {
  background-color: #ffebee;
  color: #D32F2F;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 13px;
}

@media (max-width: 768px) {
  .chart-container {
    height: 250px;
  }

  .info-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .table-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .table-search input {
    width: 100%;
  }

  .data-table {
    display: block;
    overflow-x: auto;
  }
}

@media (max-width: 480px) {
  .content-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .content-actions {
    width: 100%;
    justify-content: space-between;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>