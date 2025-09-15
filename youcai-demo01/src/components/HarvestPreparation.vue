<template>
  <div class="harvest-preparation-container">
    <div class="content-header">
      <h2>收获与整地管理</h2>
      <div class="content-actions">
        <button class="btn-plan" @click="generatePlan">📅 生成计划</button>
        <button class="btn-update" @click="updateProgress">📊 更新进度</button>
        <button class="btn-export" @click="exportReport">📤 导出报告</button>
      </div>
    </div>

    <div class="harvest-overview">
      <div class="progress-section">
        <h3>收获进度</h3>
        <div class="progress-container">
          <div class="progress-bar" :style="{ width: harvestProgress + '%' }"></div>
        </div>
        <div class="progress-stats">
          <div class="stat-item">
            <span class="stat-label">已收获面积</span>
            <span class="stat-value">{{ harvestedArea }} 亩</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">剩余面积</span>
            <span class="stat-value">{{ totalArea - harvestedArea }} 亩</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">预计完成时间</span>
            <span class="stat-value">{{ estimatedCompletion }}</span>
          </div>
        </div>
      </div>

      <div class="yield-prediction">
        <h3>产量预测</h3>
        <div class="prediction-value">{{ predictedYield }} kg/亩</div>
        <div class="prediction-details">
          <div class="prediction-trend">
            <span>较去年</span>
            <span class="trend-up">↑ {{ yieldIncrease }}%</span>
          </div>
          <div class="prediction-factors">
            <span>基于</span>
            <span class="factor-tag">生长状况</span>
            <span class="factor-tag">天气条件</span>
            <span class="factor-tag">历史数据</span>
          </div>
        </div>
      </div>
    </div>

    <div class="main-content">
      <div class="chart-section">
        <div class="chart-card">
          <div class="chart-card-header">
            <h3>收获进度与产量趋势</h3>
            <div class="chart-period">2025年8月</div>
          </div>
          <div class="chart-container">
            <canvas id="harvestTrendChart"></canvas>
          </div>
        </div>

        <div class="chart-card">
          <div class="chart-card-header">
            <h3>整地规划时间表</h3>
            <div class="chart-period">收获后15天计划</div>
          </div>
          <div class="chart-container">
            <canvas id="preparationScheduleChart"></canvas>
          </div>
        </div>
      </div>

      <div class="management-panels">
        <div class="equipment-scheduling">
          <h3>设备调度</h3>
          <div class="equipment-list">
            <div class="equipment-item">
              <div class="equipment-icon">🚜</div>
              <div class="equipment-details">
                <h4>联合收割机 #101</h4>
                <p>状态: <span class="status-active">工作中</span></p>
                <p>当前位置: 地块A3区</p>
                <p>今日进度: 35亩 / 计划40亩</p>
              </div>
              <div class="equipment-actions">
                <button class="btn-assign">重新分配</button>
              </div>
            </div>

            <div class="equipment-item">
              <div class="equipment-icon">🚜</div>
              <div class="equipment-details">
                <h4>联合收割机 #102</h4>
                <p>状态: <span class="status-maintenance">维护中</span></p>
                <p>预计可用: 2025-08-31</p>
                <p>今日进度: 0亩 / 计划40亩</p>
              </div>
              <div class="equipment-actions">
                <button class="btn-assign" disabled>重新分配</button>
              </div>
            </div>

            <div class="equipment-item">
              <div class="equipment-icon">🚜</div>
              <div class="equipment-details">
                <h4>拖拉机 #201</h4>
                <p>状态: <span class="status-active">工作中</span></p>
                <p>当前任务: 秸秆还田</p>
                <p>负责区域: 地块B1-B4区</p>
              </div>
              <div class="equipment-actions">
                <button class="btn-assign">重新分配</button>
              </div>
            </div>
          </div>
        </div>

        <div class="quality-assessment">
          <h3>收获质量评估</h3>
          <div class="quality-metrics">
            <div class="quality-metric">
              <h4>含油量</h4>
              <div class="quality-gauge">
                <div class="gauge-value">42.5%</div>
                <div class="gauge-bar">
                  <div class="gauge-fill" style="width: 85%"></div>
                </div>
              </div>
              <div class="quality-status status-good">良好</div>
            </div>

            <div class="quality-metric">
              <h4>杂质率</h4>
              <div class="quality-gauge">
                <div class="gauge-value">1.2%</div>
                <div class="gauge-bar">
                  <div class="gauge-fill" style="width: 24%"></div>
                </div>
              </div>
              <div class="quality-status status-good">低</div>
            </div>

            <div class="quality-metric">
              <h4>含水率</h4>
              <div class="quality-gauge">
                <div class="gauge-value">8.7%</div>
                <div class="gauge-bar">
                  <div class="gauge-fill" style="width: 43%"></div>
                </div>
              </div>
              <div class="quality-status status-warning">略高</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="field-preparation">
      <h3>整地规划</h3>
      <div class="preparation-steps">
        <div class="step-item active">
          <div class="step-number">1</div>
          <div class="step-content">
            <h4>秸秆处理</h4>
            <p>完成进度: 75%</p>
            <div class="step-progress"><div class="step-progress-fill" style="width: 75%"></div></div>
            <p class="step-details">已完成地块A1-A5区秸秆粉碎还田</p>
          </div>
        </div>

        <div class="step-item">
          <div class="step-number">2</div>
          <div class="step-content">
            <h4>土壤深翻</h4>
            <p>完成进度: 0%</p>
            <div class="step-progress"><div class="step-progress-fill" style="width: 0%"></div></div>
            <p class="step-details">计划9月1日开始，预计3天完成</p>
          </div>
        </div>

        <div class="step-item">
          <div class="step-number">3</div>
          <div class="step-content">
            <h4>土壤改良</h4>
            <p>完成进度: 0%</p>
            <div class="step-progress"><div class="step-progress-fill" style="width: 0%"></div></div>
            <p class="step-details">需施加有机肥200kg/亩，调节pH值至6.5-7.5</p>
          </div>
        </div>

        <div class="step-item">
          <div class="step-number">4</div>
          <div class="step-content">
            <h4>起垄作畦</h4>
            <p>完成进度: 0%</p>
            <div class="step-progress"><div class="step-progress-fill" style="width: 0%"></div></div>
            <p class="step-details">计划垄高25cm，行距40cm，畦宽1.2m</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import Chart from 'chart.js/auto';

// 收获基础数据
const totalArea = ref<number>(850);
const harvestedArea = ref<number>(630);
const harvestProgress = ref<number>(74);
const estimatedCompletion = ref<string>('2025-08-31');
const predictedYield = ref<number>(185);
const yieldIncrease = ref<number>(8.2);

// 图表实例
let harvestTrendChart = ref<Chart | null>(null);
let preparationScheduleChart = ref<Chart | null>(null);

// 初始化收获趋势图表
const initHarvestTrendChart = () => {
  const ctx = document.getElementById('harvestTrendChart') as HTMLCanvasElement;
  if (!ctx) return;

  harvestTrendChart.value = new Chart(ctx, {
    type: 'line',
    data: {
      labels: ['8/20', '8/21', '8/22', '8/23', '8/24', '8/25', '8/26'],
      datasets: [
        {
          label: '收获面积 (亩)',
          data: [480, 510, 545, 570, 590, 615, 630],
          borderColor: 'rgba(76, 175, 80, 1)',
          backgroundColor: 'rgba(76, 175, 80, 0.1)',
          tension: 0.4,
          fill: true,
          yAxisID: 'y'
        },
        {
          label: '平均产量 (kg/亩)',
          data: [178, 182, 180, 185, 183, 186, 185],
          borderColor: 'rgba(33, 150, 243, 1)',
          backgroundColor: 'transparent',
          tension: 0.4,
          yAxisID: 'y1'
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        y: {
          beginAtZero: true,
          title: {
            display: true,
            text: '收获面积 (亩)'
          }
        },
        y1: {
          position: 'right',
          grid: { drawOnChartArea: false },
          title: {
            display: true,
            text: '平均产量 (kg/亩)'
          }
        }
      }
    }
  });
};

// 初始化整地计划图表
const initPreparationScheduleChart = () => {
  const ctx = document.getElementById('preparationScheduleChart') as HTMLCanvasElement;
  if (!ctx) return;

  preparationScheduleChart.value = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: ['秸秆处理', '土壤深翻', '土壤改良', '起垄作畦', '施肥准备', '播种准备'],
      datasets: [
        {
          label: '计划工时 (小时)',
          data: [12, 18, 10, 15, 8, 6],
          backgroundColor: 'rgba(33, 150, 243, 0.6)'
        },
        {
          label: '已完成工时 (小时)',
          data: [9, 0, 0, 0, 0, 0],
          backgroundColor: 'rgba(76, 175, 80, 0.6)'
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        x: {
          stacked: true
        },
        y: {
          stacked: true,
          beginAtZero: true,
          title: {
            display: true,
            text: '工时 (小时)'
          }
        }
      }
    }
  });
};

// 生成收获计划
const generatePlan = () => {
  alert('收获与整地计划已生成，包含详细的设备调度和人员安排。');
};

// 更新收获进度
const updateProgress = () => {
  // 模拟更新进度
  const newProgress = Math.min(100, harvestProgress.value + Math.random() * 5);
  harvestProgress.value = Math.round(newProgress * 10) / 10;
  harvestedArea.value = Math.round((totalArea.value * harvestProgress.value) / 100);
  alert('收获进度已更新至 ' + harvestProgress.value + '%');
};

// 导出报告
const exportReport = () => {
  alert('收获与整地报告已导出为PDF格式。');
};

// 生命周期钩子
onMounted(() => {
  initHarvestTrendChart();
  initPreparationScheduleChart();
});

onUnmounted(() => {
  if (harvestTrendChart.value) {
    harvestTrendChart.value.destroy();
  }
  if (preparationScheduleChart.value) {
    preparationScheduleChart.value.destroy();
  }
});
</script>

<style scoped>
.harvest-preparation-container {
  padding: 20px;
}

.harvest-overview {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
  margin: 20px 0;
}

.progress-section,
.yield-prediction {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding: 20px;
}

.progress-container {
  height: 20px;
  background-color: #f1f3f5;
  border-radius: 10px;
  overflow: hidden;
  margin: 15px 0;
}

.progress-bar {
  height: 100%;
  background-color: #4CAF50;
  border-radius: 10px;
  transition: width 0.5s ease;
}

.progress-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 15px;
  margin-top: 15px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  display: block;
  color: #666;
  font-size: 14px;
}

.stat-value {
  display: block;
  font-size: 18px;
  font-weight: 700;
  color: #222;
}

.yield-prediction {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.prediction-value {
  font-size: 48px;
  font-weight: 700;
  color: #2E7D32;
  margin: 15px 0;
}

.prediction-details {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  width: 100%;
}

.prediction-trend {
  font-size: 16px;
}

.trend-up {
  color: #4CAF50;
  font-weight: 600;
}

.factor-tag {
  display: inline-block;
  padding: 3px 8px;
  background-color: #e3f2fd;
  color: #1976D2;
  border-radius: 4px;
  font-size: 12px;
  margin: 0 3px;
}

.main-content {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
  margin: 20px 0;
}

.chart-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.management-panels {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.equipment-scheduling,
.quality-assessment {
 background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding: 20px;
}

.equipment-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-top: 15px;
}

.equipment-item {
  display: flex;
  gap: 15px;
  padding: 15px;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
}

.equipment-icon {
  font-size: 24px;
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.equipment-details {
  flex: 1;
}

.equipment-details h4 {
  margin: 0 0 8px 0;
  color: #333;
}

.equipment-details p {
  margin: 4px 0;
  color: #666;
  font-size: 14px;
}

.status-active {
  color: #2E7D32;
  font-weight: 500;
}

.status-maintenance {
  color: #FF8F00;
  font-weight: 500;
}

.quality-metrics {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
  margin-top: 15px;
}

.quality-metric {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.quality-gauge {
  width: 100%;
  height: 60px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.gauge-value {
  font-size: 24px;
  font-weight: 700;
  color: #222;
}

.gauge-bar {
  width: 100%;
  height: 8px;
  background-color: #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
  margin-top: 8px;
}

.gauge-fill {
  height: 100%;
  background-color: #4CAF50;
  border-radius: 4px;
}

.quality-status {
  margin-top: 10px;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
}

.status-good {
  background-color: #e8f5e9;
  color: #2E7D32;
}

.status-warning {
  background-color: #fff8e1;
  color: #FF8F00;
}

.field-preparation {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding: 20px;
  margin-top: 20px;
}

.preparation-steps {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-top: 15px;
}

.step-item {
  padding: 15px;
  border-radius: 12px;
  border: 1px solid #f0f0f0;
}

.step-item.active {
  border-left: 4px solid #2196F3;
  background-color: #f8f9fa;
}

.step-number {
  display: inline-block;
  width: 28px;
  height: 28px;
  background-color: #2196F3;
  color: white;
  border-radius: 50%;
  text-align: center;
  line-height: 28px;
  font-weight: 600;
  margin-right: 10px;
}

.step-content h4 {
  display: inline-block;
  margin: 0 0 10px 0;
  vertical-align: middle;
}

.step-progress {
  height: 8px;
  background-color: #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
  margin: 10px 0;
}

.step-progress-fill {
  height: 100%;
  background-color: #4CAF50;
  border-radius: 4px;
}

.step-details {
  font-size: 14px;
  color: #666;
  margin: 5px 0 0 0;
}

@media (max-width: 1024px) {
  .main-content {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .harvest-overview {
    grid-template-columns: 1fr;
  }

  .quality-metrics {
    grid-template-columns: 1fr;
  }
}
</style>