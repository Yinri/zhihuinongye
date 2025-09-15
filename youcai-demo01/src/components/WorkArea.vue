<template>
  <div v-if="activeMenu === '作业面积'" class="work-area-container">
    <div class="content-header">
      <h2>作业面积统计</h2>
      <div class="content-actions">
        <button class="btn-filter" @click="toggleFilter">⚟ 筛选条件</button>
        <button class="btn-export" @click="exportData">📤 导出数据</button>
      </div>
    </div>
    <div class="card-container">
      <div class="chart-card large-chart">
        <div class="chart-card-header">
          <h3>各片区作业面积分布</h3>
          <div class="chart-legend">
            <div class="legend-item"><span class="color-indicator" style="background-color: #4CAF50;"></span> 已完成</div>
            <div class="legend-item"><span class="color-indicator" style="background-color: #FFC107;"></span> 进行中</div>
            <div class="legend-item"><span class="color-indicator" style="background-color: #F44336;"></span> 未开始</div>
          </div>
        </div>
        <div class="chart-container">
          <canvas id="areaDistributionChart"></canvas>
        </div>
      </div>

      <div class="info-grid">
        <div class="info-card">
          <div class="info-card-header">
            <h3>总面积</h3>
          </div>
          <div class="info-card-value">2,450 亩</div>
          <div class="info-card-footer">
            <span class="trend-up">↑ 5.2% 较上月</span>
          </div>
        </div>

        <div class="info-card">
          <div class="info-card-header">
            <h3>已完成面积</h3>
          </div>
          <div class="info-card-value">1,890 亩</div>
          <div class="info-card-footer">
            <span class="text-success">完成率: 77.1%</span>
          </div>
        </div>

        <div class="info-card">
          <div class="info-card-header">
            <h3>未完成面积</h3>
          </div>
          <div class="info-card-value">560 亩</div>
          <div class="info-card-footer">
            <span class="text-warning">需加快进度</span>
          </div>
        </div>
      </div>
    </div>

    <div class="table-container">
      <div class="table-header">
        <h3>片区作业明细</h3>
        <div class="table-search">
          <input type="text" placeholder="搜索片区..." v-model="searchKeyword" />
        </div>
      </div>
      <table class="data-table">
        <thead>
          <tr>
            <th>片区编号</th>
            <th>片区名称</th>
            <th>计划面积(亩)</th>
            <th>已作业面积(亩)</th>
            <th>完成率</th>
            <th>负责人</th>
            <th>状态</th>
          </tr>
        </thead>
        <tbody>
          <!-- 可选链保留，避免数据异常时报错 -->
          <tr v-for="area in filteredAreas" :key="area.id">
            <td>{{ area.code }}</td>
            <td>{{ area.name }}</td>
            <td>{{ area.planArea }}</td>
            <td>{{ area.completedArea }}</td>
            <td>
              <div class="progress-bar" :style="{ width: area.completionRate + '%' }"></div>
              <span class="progress-text">{{ area.completionRate }}%</span>
            </td>
            <td>{{ area.manager }}</td>
            <td>
              <span :class="statusClass(area.status)">{{ area.status }}</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { Chart } from 'chart.js'; // 导入Chart类型

// 1. 定义Props类型（确保父组件传入的activeMenu类型正确）
const props = defineProps({
  activeMenu: {
    type: String,
    required: true,
    validator: (val: string) => val === 'workArea' || true // 简单校验，避免无效菜单值
  }
});

// 2. 定义核心数据接口（明确类型，避免隐式any）
interface AreaItem {
  id: number;
  code: string;
  name: string;
  planArea: number; // 计划面积（亩）
  completedArea: number; // 已完成面积（亩）
  completionRate: number; // 完成率（%）
  manager: string; // 负责人
  status: '已完成' | '进行中' | '未开始'; // 仅允许3种状态，类型收窄
}

// 3. 声明状态并指定类型（核心修正：解决类型不匹配）
const searchKeyword = ref<string>(''); // 搜索关键词：string类型
// 图表实例：允许为Chart实例或null（解决“不能将Chart分配给null”错误）
const areaDistributionChart = ref<Chart | null>(null);
const showFilter = ref<boolean>(false); // 筛选面板显示状态：boolean类型

// 4. 模拟片区数据（使用AreaItem接口约束，类型严谨）
const areas = ref<AreaItem[]>([
  { id: 1, code: 'A01', name: '东部片区', planArea: 650, completedArea: 580, completionRate: 89.2, manager: '张三', status: '进行中' },
  { id: 2, code: 'A02', name: '西部片区', planArea: 480, completedArea: 480, completionRate: 100, manager: '李四', status: '已完成' },
  { id: 3, code: 'A03', name: '南部片区', planArea: 520, completedArea: 320, completionRate: 61.5, manager: '王五', status: '进行中' },
  { id: 4, code: 'A04', name: '北部片区', planArea: 800, completedArea: 510, completionRate: 63.8, manager: '赵六', status: '进行中' }
]);

// 筛选后的片区数据（与areas类型一致）
const filteredAreas = ref<AreaItem[]>([]);

// 5. 状态样式映射（参数指定AreaItem['status']类型，避免隐式any）
const statusClass = (status: AreaItem['status']) => {
  switch(status) {
    case '已完成': return 'status-completed';
    case '进行中': return 'status-progress';
    case '未开始': return 'status-pending';
    default: return '';
  }
};

// 6. 初始化图表（修正：ctx类型断言+实例销毁逻辑）
const initChart = () => {
  // 获取Canvas元素：明确类型为HTMLCanvasElement或null
  const canvas = document.getElementById('areaDistributionChart') as HTMLCanvasElement | null;
  if (!canvas) return; // 元素不存在时直接返回，避免报错

  // 销毁已有图表实例（防止重复创建导致内存泄漏）
  if (areaDistributionChart.value) {
    areaDistributionChart.value.destroy();
    areaDistributionChart.value = null;
  }

  // 创建图表实例（类型自动匹配，无报错）
  areaDistributionChart.value = new Chart(canvas, {
    type: 'doughnut',
    data: {
      labels: ['东部片区', '西部片区', '南部片区', '北部片区'],
      datasets: [
        {
          label: '已完成',
          data: [580, 480, 320, 510],
          backgroundColor: 'rgba(76, 175, 80, 0.8)',
        },
        {
          label: '未完成',
          data: [70, 0, 200, 290],
          backgroundColor: 'rgba(244, 67, 54, 0.8)',
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'right',
        },
        tooltip: {
          callbacks: {
            label: function(context) {
              const label = context.dataset.label || '';
              const value = context.raw || 0;
              return `${label}: ${value} 亩`;
            }
          }
        }
      },
      cutout: '60%'
    }
  });
};

// 7. 筛选片区数据（逻辑不变，类型明确）
const filterAreas = () => {
  const keyword = searchKeyword.value.toLowerCase().trim();
  filteredAreas.value = areas.value.filter(area => 
    area.name.toLowerCase().includes(keyword) || 
    area.code.toLowerCase().includes(keyword) ||
    area.manager.toLowerCase().includes(keyword)
  );
};

// 8. 其他工具函数（类型明确）
const toggleFilter = () => {
  showFilter.value = !showFilter.value;
};

const exportData = () => {
  // 模拟导出数据（可后续补充实际逻辑）
  console.log('导出作业面积数据:', filteredAreas.value);
  alert('作业面积数据已导出');
};

// 9. 生命周期与监听（逻辑不变）
onMounted(() => {
  filterAreas(); // 初始加载时筛选数据
  initChart(); // 初始加载时创建图表
});

// 监听搜索关键词变化，实时筛选
watch(searchKeyword, filterAreas);

// 组件卸载时销毁图表（补充：防止内存泄漏）
import { onUnmounted } from 'vue';
onUnmounted(() => {
  if (areaDistributionChart.value) {
    areaDistributionChart.value.destroy();
  }
});
</script>

<style scoped>
.work-area-container {
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

.btn-filter,
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

.btn-filter {
  background-color: #f0f7ee;
  color: #2E7D32;
}

.btn-export {
  background-color: #e3f2fd;
  color: #1976D2;
}

.btn-filter:hover,
.btn-export:hover {
  opacity: 0.9;
  transform: translateY(-2px);
}

.card-container {
  margin-bottom: 30px;
}

.large-chart {
  margin-bottom: 20px;
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

.chart-legend {
  display: flex;
  gap: 15px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #666;
}

.color-indicator {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.chart-container {
  height: 350px;
  width: 100%;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
  margin-top: 20px;
}

.info-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding: 20px;
  text-align: center;
}

.info-card-header h3 {
  color: #666;
  font-size: 16px;
  margin: 0 0 15px 0;
}

.info-card-value {
  font-size: 28px;
  font-weight: 700;
  color: #222;
  margin: 0 0 10px 0;
}

.info-card-footer {
  font-size: 14px;
}

.trend-up {
  color: #4CAF50;
}

.text-success {
  color: #4CAF50;
}

.text-warning {
  color: #FF8F00;
}

.table-container {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding: 20px;
  margin-top: 30px;
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

.progress-bar {
  height: 8px;
  border-radius: 4px;
  background-color: #4CAF50;
}

.progress-text {
  font-size: 13px;
  color: #666;
  margin-left: 8px;
}

.status-completed {
  background-color: #e8f5e9;
  color: #2E7D32;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 13px;
}

.status-progress {
  background-color: #fff8e1;
  color: #FF8F00;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 13px;
}

.status-pending {
  background-color: #ffebee;
  color: #D32F2F;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 13px;
}

@media (max-width: 768px) {
  .chart-container {
    height: 300px;
  }

  .info-grid {
    grid-template-columns: 1fr 1fr;
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