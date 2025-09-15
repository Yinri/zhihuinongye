<template>
  <div v-if="activeMenu === '生产计划'" class="production-plan-container">
    <!-- 油菜花专项生产计划卡片 -->
    <div class="rapeseed-plan-card">
      <h3>油菜花专项生产计划</h3>
      <div class="rapeseed-metrics">
        <div class="metric-item">
          <span class="metric-label">计划种植面积</span>
          <span class="metric-value">850 亩</span>
        </div>
        <div class="metric-item">
          <span class="metric-label">已完成面积</span>
          <span class="metric-value">620 亩</span>
        </div>
        <div class="metric-item">
          <span class="metric-label">完成比例</span>
          <span class="metric-value">72.9%</span>
        </div>
        <div class="metric-item">
          <span class="metric-label">预计产量</span>
          <span class="metric-value">2100 kg/亩</span>
        </div>
      </div>
      <div class="plan-progress">
        <div class="progress-label">种植进度</div>
        <div class="progress-bar-container">
          <div class="progress-bar" :style="{ width: '72.9%' }"></div>
        </div>
      </div>
    </div>

    <div class="content-header">
      <h2>生产计划管理</h2>
      <div class="content-actions">
        <button class="btn-refresh" @click="refreshData">🔄 刷新数据</button>
        <button class="btn-export" @click="exportData">📤 导出报表</button>
      </div>
    </div>

    <div class="card-container">
      <!-- 作业面积图示卡片（新增） -->
      <div class="area-visualization-card">
        <div class="chart-card-header">
          <h3>作业面积图示</h3>
          <div class="area-type-selector">
            <button 
              class="area-type-btn" 
              :class="{ active: areaType === 'total' }" 
              @click="areaType = 'total'"
            >
              总面积
            </button>
            <button 
              class="area-type-btn" 
              :class="{ active: areaType === 'crop' }" 
              @click="areaType = 'crop'"
            >
              作物分布
            </button>
            <button 
              class="area-type-btn" 
              :class="{ active: areaType === 'progress' }" 
              @click="areaType = 'progress'"
            >
              进度分布
            </button>
          </div>
        </div>
        <div class="chart-container area-chart-container">
          <canvas id="areaVisualizationChart"></canvas>
        </div>
      </div>

      <div class="info-card">
        <div class="info-card-header">
          <h3>计划完成情况</h3>
          <span class="info-card-badge">总体进度: 78%</span>
        </div>
        <div class="progress-container">
          <div class="progress-bar" :style="{ width: '78%' }"></div>
        </div>
        <div class="info-card-grid">
          <div class="info-card-item">
            <p class="info-card-label">计划播种面积</p>
            <p class="info-card-value">1200 亩</p>
          </div>
          <div class="info-card-item">
            <p class="info-card-label">已完成面积</p>
            <p class="info-card-value">936 亩</p>
          </div>
          <div class="info-card-item">
            <p class="info-card-label">剩余面积</p>
            <p class="info-card-value">264 亩</p>
          </div>
        </div>
      </div>

      <div class="chart-card">
        <div class="chart-card-header">
          <h3>月度生产计划对比</h3>
          <div class="chart-period-selector">
            <button class="period-btn" :class="{ active: period === 'month' }" @click="period = 'month'">月</button>
            <button class="period-btn" :class="{ active: period === 'quarter' }" @click="period = 'quarter'">季</button>
            <button class="period-btn" :class="{ active: period === 'year' }" @click="period = 'year'">年</button>
          </div>
        </div>
        <div class="chart-container">
          <canvas id="productionPlanChart"></canvas>
        </div>
      </div>
    </div>

    <div class="table-container">
      <div class="table-header">
        <h3>详细计划列表</h3>
        <div class="table-search">
          <input type="text" placeholder="搜索计划..." v-model="searchKeyword" />
        </div>
      </div>
      <table class="data-table">
        <thead>
          <tr>
            <th>计划ID</th>
            <th>作物类型</th>
            <th>计划面积(亩)</th>
            <th>计划时间</th>
            <th>负责人</th>
            <th>进度</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="plan in filteredPlans" :key="plan.id">
            <td>{{ plan.id }}</td>
            <td>{{ plan.cropType }}</td>
            <td>{{ plan.area }}</td>
            <td>{{ plan.date }}</td>
            <td>{{ plan.manager }}</td>
            <td>
              <div class="progress-bar-small" :style="{ width: plan.progress + '%' }"></div>
              <span class="progress-text">{{ plan.progress }}%</span>
            </td>
            <td>
              <button class="btn-view" @click="viewPlan(plan.id)">查看</button>
              <button class="btn-edit" @click="editPlan(plan.id)">编辑</button>
            </td>
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
    validator: (val: string) => val === 'productionPlan' // 严格校验，仅允许生产计划菜单
  }
});

// 2. 定义核心数据接口（约束计划数据结构）
interface ProductionPlan {
  id: string; // 计划ID（如"PP-2025001"）
  cropType: string; // 作物类型（如"油菜"）
  area: number; // 计划面积（亩）
  date: string; // 计划时间（YYYY-MM-DD）
  manager: string; // 负责人
  progress: number; // 进度（%，0-100）
}

// 3. 声明状态并指定明确类型（核心修复：补充新增图表相关状态）
// 周期状态：月度/季度/年度
const period = ref<'month' | 'quarter' | 'year'>('month');
// 作业面积图示类型：总面积/作物分布/进度分布
const areaType = ref<'total' | 'crop' | 'progress'>('total');
// 搜索关键词
const searchKeyword = ref<string>('');
// 图表实例：生产计划对比图 + 作业面积图示（均允许Chart/null）
const productionPlanChart = ref<Chart | null>(null);
const areaVisualizationChart = ref<Chart | null>(null);

// 生产计划原始数据（用接口约束）
const plans = ref<ProductionPlan[]>([
  { id: 'PP-2025001', cropType: '油菜', area: 350, date: '2025-08-25', manager: '张三', progress: 92 },
  { id: 'PP-2025002', cropType: '小麦', area: 280, date: '2025-08-27', manager: '李四', progress: 75 },
  { id: 'PP-2025003', cropType: '油菜', area: 420, date: '2025-08-29', manager: '王五', progress: 60 },
  { id: 'PP-2025004', cropType: '大麦', area: 150, date: '2025-08-30', manager: '赵六', progress: 45 }
]);

// 筛选后的计划数据
const filteredPlans = ref<ProductionPlan[]>([]);

// 4. 图表销毁工具函数（统一处理，防止内存泄漏）
import type { Ref } from 'vue';
const destroyChart = (chartRef: Ref<Chart | null>) => {
  if (chartRef.value) {
    chartRef.value.destroy();
    chartRef.value = null;
  }
};

// 5. 初始化生产计划对比图（原逻辑保留，优化类型）
const initProductionPlanChart = () => {
  destroyChart(productionPlanChart); // 先销毁旧图表

  const canvas = document.getElementById('productionPlanChart') as HTMLCanvasElement | null;
  if (!canvas) return;

  // 根据周期生成数据
  const { labels, planData, actualData } = (() => {
    switch (period.value) {
      case 'month':
        return {
          labels: ['1月', '2月', '3月', '4月', '5月', '6月'],
          planData: [320, 450, 380, 520, 480, 360] as number[],
          actualData: [290, 420, 390, 480, 450, 320] as number[]
        };
      case 'quarter':
        return {
          labels: ['Q1', 'Q2', 'Q3', 'Q4'],
          planData: [1200, 1500, 1300, 1400] as number[],
          actualData: [1100, 1450, 1250, 1380] as number[]
        };
      case 'year':
        return {
          labels: ['2023', '2024', '2025(预测)'],
          planData: [5400, 5800, 6200] as number[],
          actualData: [5200, 5600, 0] as number[] // 补充2024实际数据，更合理
        };
    }
  })();

  // 创建柱状图
  productionPlanChart.value = new Chart(canvas, {
    type: 'bar',
    data: {
      labels,
      datasets: [
        {
          label: '计划面积',
          data: planData,
          backgroundColor: 'rgba(76, 175, 80, 0.6)',
          borderColor: 'rgba(76, 175, 80, 1)',
          borderWidth: 1
        },
        {
          label: '实际面积',
          data: actualData,
          backgroundColor: 'rgba(33, 150, 243, 0.6)',
          borderColor: 'rgba(33, 150, 243, 1)',
          borderWidth: 1
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        y: {
          beginAtZero: true,
          title: { display: true, text: '面积 (亩)' },
          grid: { color: 'rgba(0, 0, 0, 0.05)' }
        },
        x: { grid: { display: false } }
      },
      plugins: {
        legend: { position: 'top' },
        tooltip: { mode: 'index', intersect: false }
      }
    }
  });
};

// 6. 初始化作业面积图示（新增图表逻辑，核心修复点）
const initAreaVisualizationChart = () => {
  destroyChart(areaVisualizationChart); // 先销毁旧图表

  const canvas = document.getElementById('areaVisualizationChart') as HTMLCanvasElement | null;
  if (!canvas) return;

  // 根据areaType切换图表类型和数据
  let chartConfig: import('chart.js').ChartConfiguration;
  switch (areaType.value) {
    // 总面积对比（柱状图）
    case 'total':
      chartConfig = {
        type: 'bar',
        data: {
          labels: ['已完成', '未完成', '计划总面积'],
          datasets: [{
            label: '面积 (亩)',
            data: [936, 264, 1200] as number[],
            backgroundColor: [
              'rgba(76, 175, 80, 0.6)',
              'rgba(244, 67, 54, 0.6)',
              'rgba(33, 150, 243, 0.6)'
            ],
            borderWidth: 1
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          scales: {
            y: { beginAtZero: true, title: { display: true, text: '面积 (亩)' } },
            x: { grid: { display: false } }
          },
          plugins: { legend: { display: false } }
        }
      };
      break;

    // 作物分布（饼图）
    case 'crop':
      chartConfig = {
        type: 'pie',
        data: {
          labels: ['油菜', '小麦', '大麦', '其他'],
          datasets: [{
            label: '作物面积占比',
            data: [770, 280, 150, 0] as number[], // 油菜350+420=770
            backgroundColor: [
              'rgba(76, 175, 80, 0.7)',
              'rgba(255, 152, 0, 0.7)',
              'rgba(156, 39, 176, 0.7)',
              'rgba(158, 158, 158, 0.7)'
            ],
            borderWidth: 1
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: { position: 'right' },
            tooltip: { callbacks: { label: (ctx: any) => `${ctx.label}: ${ctx.raw} 亩` } }
          }
        }
      };
      break;

    // 进度分布（折线图）
    case 'progress':
      chartConfig = {
        type: 'line',
        data: {
          labels: ['1月', '2月', '3月', '4月', '5月', '6月'],
          datasets: [{
            label: '累计完成进度 (%)',
            data: [15, 32, 48, 65, 72, 78] as number[],
            borderColor: 'rgba(76, 175, 80, 1)',
            backgroundColor: 'rgba(76, 175, 80, 0.1)',
            tension: 0.3,
            fill: true
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          scales: {
            y: { min: 0, max: 100, title: { display: true, text: '进度 (%)' } },
            x: { grid: { display: false } }
          },
          plugins: { legend: { position: 'top' } }
        }
      };
      break;
  }

  // 创建图表实例
  areaVisualizationChart.value = new Chart(canvas, chartConfig);
};

// 7. 筛选计划数据（关键词去空格，优化匹配）
const filterPlans = () => {
  const keyword = searchKeyword.value.toLowerCase().trim();
  filteredPlans.value = plans.value.filter(plan => 
    plan.cropType.toLowerCase().includes(keyword) || 
    plan.id.toLowerCase().includes(keyword) ||
    plan.manager.toLowerCase().includes(keyword)
  );
};

// 8. 业务方法（明确类型，无隐式any）
/** 刷新数据 */
const refreshData = (): void => {
  console.log('刷新生产计划数据');
  // 模拟进度更新（±5%，确保0-100范围）
  plans.value = plans.value.map(plan => ({
    ...plan,
    progress: Math.max(0, Math.min(100, plan.progress + (Math.random() * 10 - 5)))
  }));
  filterPlans(); // 重新筛选
  initProductionPlanChart(); // 刷新图表
  initAreaVisualizationChart();
};

/** 导出报表 */
const exportData = (): void => {
  console.log('导出生产计划报表:', filteredPlans.value);
  alert('生产计划报表已导出（格式：Excel）');
};

/** 查看计划 */
const viewPlan = (id: string): void => {
  console.log('查看计划详情:', id);
  // 可扩展：打开详情弹窗或路由跳转
};

/** 编辑计划 */
const editPlan = (id: string): void => {
  console.log('编辑计划:', id);
  // 可扩展：打开编辑弹窗
};

// 9. 生命周期与监听（确保图表正确初始化/销毁）
onMounted(() => {
  filterPlans(); // 初始筛选数据
  initProductionPlanChart(); // 初始化生产计划图表
  initAreaVisualizationChart(); // 初始化作业面积图表
});

// 监听周期变化 → 刷新生产计划图表
watch(period, initProductionPlanChart);

// 监听面积图示类型变化 → 刷新作业面积图表
watch(areaType, initAreaVisualizationChart);

// 监听搜索关键词 → 实时筛选
watch(searchKeyword, filterPlans);

// 监听菜单切换 → 离开时销毁图表
watch(
  () => props.activeMenu,
  (newVal) => {
    if (newVal !== 'productionPlan') {
      destroyChart(productionPlanChart);
      destroyChart(areaVisualizationChart);
    }
  }
);

// 组件卸载 → 彻底销毁图表（防止内存泄漏）
onUnmounted(() => {
  destroyChart(productionPlanChart);
  destroyChart(areaVisualizationChart);
});
</script>

<style scoped>
.production-plan-container {
  padding: 20px;
  background-color: #f8f9fa;
  min-height: calc(100vh - 85px); /* 适配顶部导航高度 */
}

/* 油菜花专项计划卡片样式（新增） */
.rapeseed-plan-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding: 20px;
  margin-bottom: 25px;
  border-left: 4px solid #4CAF50; /* 左侧绿色标识线 */
}

.rapeseed-plan-card h3 {
  color: #2E7D32;
  font-size: 18px;
  margin-bottom: 15px;
  font-weight: 600;
}

.rapeseed-metrics {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 15px;
  margin-bottom: 20px;
}

.metric-item {
  text-align: center;
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.metric-label {
  display: block;
  color: #666;
  font-size: 14px;
  margin-bottom: 5px;
}

.metric-value {
  font-size: 16px;
  font-weight: 600;
  color: #222;
}

.plan-progress {
  width: 100%;
}

.progress-label {
  color: #666;
  font-size: 14px;
  margin-bottom: 8px;
}

.progress-bar-container {
  height: 8px;
  background-color: #f1f1f1;
  border-radius: 4px;
  overflow: hidden;
}

/* 作业面积图示卡片样式（新增） */
.area-visualization-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding: 20px;
  grid-column: 1 / -1; /* 占满所有列，更美观 */
}

.area-type-selector {
  display: flex;
  gap: 8px;
}

.area-type-btn {
  padding: 6px 12px;
  border-radius: 6px;
  border: 1px solid #ddd;
  background-color: white;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
}

.area-type-btn.active {
  background-color: #2E7D32;
  color: white;
  border-color: #2E7D32;
}

.area-chart-container {
  height: 350px; /* 适当增高，适配饼图/折线图 */
}

/* 原有样式保留，补充适配 */
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
.btn-export {
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

.btn-export {
  background-color: #e3f2fd;
  color: #1976D2;
}

.btn-refresh:hover,
.btn-export:hover {
  opacity: 0.9;
  transform: translateY(-2px);
}

.card-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.info-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding: 20px;
}

.info-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.info-card-header h3 {
  margin: 0;
  color: #333;
  font-size: 18px;
}

.info-card-badge {
  background-color: #e8f5e9;
  color: #2E7D32;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
}

.progress-container {
  height: 10px;
  background-color: #f1f1f1;
  border-radius: 5px;
  margin-bottom: 20px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #4CAF50, #8BC34A);
  border-radius: 5px;
  transition: width 0.6s ease;
}

.info-card-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
}

.info-card-item {
  text-align: center;
}

.info-card-label {
  color: #666;
  font-size: 14px;
  margin-bottom: 5px;
}

.info-card-value {
  color: #222;
  font-size: 20px;
  font-weight: 600;
  margin: 0;
}

.chart-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding: 20px;
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

.chart-period-selector {
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

.progress-bar-small {
  height: 8px;
  border-radius: 4px;
  background-color: #4CAF50;
}

.progress-text {
  font-size: 13px;
  color: #666;
  margin-left: 8px;
}

.btn-view,
.btn-edit {
  padding: 5px 10px;
  border-radius: 6px;
  border: none;
  cursor: pointer;
  font-size: 13px;
  margin-right: 5px;
}

.btn-view {
  background-color: #e3f2fd;
  color: #1976D2;
}

.btn-edit {
  background-color: #fff8e1;
  color: #FF8F00;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .card-container {
    grid-template-columns: 1fr;
  }

  .rapeseed-metrics {
    grid-template-columns: repeat(2, 1fr);
  }

  .content-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .content-actions {
    width: 100%;
    justify-content: space-between;
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

  .area-chart-container {
    height: 300px;
  }
}

@media (max-width: 480px) {
  .rapeseed-metrics {
    grid-template-columns: 1fr;
  }

  .area-type-selector {
    flex-wrap: wrap;
  }

  .area-type-btn {
    flex: 1;
    min-width: 80px;
  }
}
</style>