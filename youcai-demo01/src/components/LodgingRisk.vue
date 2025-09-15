<template>
  <div class="lodging-risk-container">
    <div class="content-header">
      <h2>倒伏风险预警</h2>
      <div class="content-actions">
        <button class="btn-assess" @click="assessRisk">📊 评估风险</button>
        <button class="btn-forecast" @click="showForecast">🌤️ 7天预测</button>
        <button class="btn-guidelines" @click="showGuidelines">📋 防控指南</button>
      </div>
    </div>

    <div class="risk-status-overview">
      <div class="risk-level-indicator">
        <div class="risk-level-label">当前风险等级</div>
        <div class="risk-level-value" :class="riskLevelClass">
          {{ riskLevelText }}
          <div class="risk-level-meter" :style="{ width: riskPercentage + '%' }"></div>
        </div>
      </div>

      <div class="risk-metrics">
        <div class="metric-card">
          <h4>茎秆强度</h4>
          <p class="metric-value">{{ stemStrength }} MPa</p>
          <p class="metric-status" :class="stemStrengthStatus">{{ stemStrengthText }}</p>
        </div>
        <div class="metric-card">
          <h4>株高/茎粗比</h4>
          <p class="metric-value">{{ heightDiameterRatio.toFixed(1) }}</p>
          <p class="metric-status" :class="heightRatioStatus">{{ heightRatioText }}</p>
        </div>
        <div class="metric-card">
          <h4>近期降水量</h4>
          <p class="metric-value">{{ rainfall }} mm</p>
          <p class="metric-status" :class="rainfallStatus">{{ rainfallText }}</p>
        </div>
        <div class="metric-card">
          <h4>预估风速</h4>
          <p class="metric-value">{{ windSpeed }} m/s</p>
          <p class="metric-status" :class="windStatus">{{ windText }}</p>
        </div>
      </div>
    </div>

    <div class="risk-analysis-section">
      <div class="chart-card">
        <div class="chart-card-header">
          <h3>倒伏风险趋势分析</h3>
          <span class="chart-period">近14天数据</span>
        </div>
        <div class="chart-container">
          <canvas id="riskTrendChart"></canvas>
        </div>
      </div>

      <div class="factors-card">
        <h3>主要影响因素</h3>
        <div class="factors-grid">
          <div class="factor-item" :class="factorClass('stem')">
            <div class="factor-icon">🌱</div>
            <div class="factor-details">
              <h4>茎秆特性</h4>
              <p>茎秆强度和韧性不足</p>
              <div class="factor-impact">影响度: {{ factorImpact.stem }}%</div>
            </div>
          </div>
          <div class="factor-item" :class="factorClass('weather')">
            <div class="factor-icon">🌧️</div>
            <div class="factor-details">
              <h4>气象条件</h4>
              <p>强降水和大风天气影响</p>
              <div class="factor-impact">影响度: {{ factorImpact.weather }}%</div>
            </div>
          </div>
          <div class="factor-item" :class="factorClass('growth')">
            <div class="factor-icon">🌿</div>
            <div class="factor-details">
              <h4>生长状况</h4>
              <p>株高过高，重心不稳</p>
              <div class="factor-impact">影响度: {{ factorImpact.growth }}%</div>
            </div>
          </div>
          <div class="factor-item" :class="factorClass('management')">
            <div class="factor-icon">🚜</div>
            <div class="factor-details">
              <h4>栽培管理</h4>
              <p>种植密度和施肥方案影响</p>
              <div class="factor-impact">影响度: {{ factorImpact.management }}%</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="control-measures">
      <h3>防控建议措施</h3>
      <div class="measures-tabs">
        <button class="measure-tab" :class="{ active: activeMeasureTab === 'immediate' }" @click="activeMeasureTab = 'immediate'">紧急措施</button>
        <button class="measure-tab" :class="{ active: activeMeasureTab === 'short' }" @click="activeMeasureTab = 'short'">短期措施</button>
        <button class="measure-tab" :class="{ active: activeMeasureTab === 'long' }" @click="activeMeasureTab = 'long'">长期措施</button>
      </div>
      <div class="measures-content">
        <div v-if="activeMeasureTab === 'immediate'">
          <ul class="measures-list">
            <li>🌬️ 大风预警期间，可搭建临时防风障</li>
            <li>💧 控制灌溉量，避免田间积水</li>
            <li>🔍 及时巡查，对倾斜植株进行适度支撑</li>
            <li>🌡️ 关注天气预报，提前做好防护准备</li>
          </ul>
        </div>
        <div v-if="activeMeasureTab === 'short'">
          <ul class="measures-list">
            <li>🌾 喷施植物生长调节剂，降低株高</li>
            <li>💪 增施钾肥，提高茎秆强度</li>
            <li>🌱 适时中耕培土，增强根系固着力</li>
            <li>📏 合理调整种植密度，改善田间通风</li>
          </ul>
        </div>
        <div v-if="activeMeasureTab === 'long'">
          <ul class="measures-list">
            <li>🌱 选择抗倒伏品种，培育壮苗</li>
            <li>📊 优化施肥方案，控制氮肥用量</li>
            <li>🔬 采用宽窄行种植模式，增强抗风能力</li>
            <li>📝 建立长期监测档案，分析倒伏规律</li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import Chart from 'chart.js/auto';

// 风险状态数据
const riskPercentage = ref<number>(65);
const stemStrength = ref<number>(2.8);
const heightDiameterRatio = ref<number>(8.5);
const rainfall = ref<number>(45);
const windSpeed = ref<number>(12);

// 影响因素权重
const factorImpact = ref<{[key: string]: number}>({
  stem: 35,
  weather: 40,
  growth: 15,
  management: 10
});

// 标签和状态
const activeMeasureTab = ref<string>('immediate');
const riskTrendChart = ref<Chart | null>(null);

// 计算风险等级
const riskLevelText = ref<string>('中风险');
const riskLevelClass = ref<string>('risk-medium');

// 计算各指标状态
const stemStrengthStatus = ref<string>('status-warning');
const stemStrengthText = ref<string>('偏低');
const heightRatioStatus = ref<string>('status-warning');
const heightRatioText = ref<string>('偏高');
const rainfallStatus = ref<string>('status-danger');
const rainfallText = ref<string>('高');
const windStatus = ref<string>('status-warning');
const windText = ref<string>('较高');

// 计算风险等级样式
const updateRiskLevel = () => {
  if (riskPercentage.value < 30) {
    riskLevelText.value = '低风险';
    riskLevelClass.value = 'risk-low';
  } else if (riskPercentage.value < 70) {
    riskLevelText.value = '中风险';
    riskLevelClass.value = 'risk-medium';
  } else {
    riskLevelText.value = '高风险';
    riskLevelClass.value = 'risk-high';
  }
};

// 评估各指标状态
const updateIndicatorStatus = () => {
  // 茎秆强度状态
  if (stemStrength.value > 3.5) {
    stemStrengthStatus.value = 'status-good';
    stemStrengthText.value = '良好';
  } else if (stemStrength.value > 2.5) {
    stemStrengthStatus.value = 'status-warning';
    stemStrengthText.value = '偏低';
  } else {
    stemStrengthStatus.value = 'status-danger';
    stemStrengthText.value = '差';
  }

  // 株高/茎粗比状态
  if (heightDiameterRatio.value < 7) {
    heightRatioStatus.value = 'status-good';
    heightRatioText.value = '正常';
  } else if (heightDiameterRatio.value < 9) {
    heightRatioStatus.value = 'status-warning';
    heightRatioText.value = '偏高';
  } else {
    heightRatioStatus.value = 'status-danger';
    heightRatioText.value = '高';
  }

  // 降水量状态
  if (rainfall.value < 20) {
    rainfallStatus.value = 'status-good';
    rainfallText.value = '正常';
  } else if (rainfall.value < 50) {
    rainfallStatus.value = 'status-warning';
    rainfallText.value = '较高';
  } else {
    rainfallStatus.value = 'status-danger';
    rainfallText.value = '高';
  }

  // 风速状态
  if (windSpeed.value < 8) {
    windStatus.value = 'status-good';
    windText.value = '正常';
  } else if (windSpeed.value < 14) {
    windStatus.value = 'status-warning';
    windText.value = '较高';
  } else {
    windStatus.value = 'status-danger';
    windText.value = '高';
  }
};

// 获取因素样式
const factorClass = (factor: string): string => {
  const impact = factorImpact.value[factor];
  if (impact > 30) return 'factor-high';
  if (impact > 15) return 'factor-medium';
  return 'factor-low';
};

// 初始化风险趋势图表
const initRiskChart = () => {
  const ctx = document.getElementById('riskTrendChart') as HTMLCanvasElement;
  if (!ctx) return;

  // 销毁已有图表
  if (riskTrendChart.value) {
    riskTrendChart.value.destroy();
  }

  // 创建新图表
  riskTrendChart.value = new Chart(ctx, {
    type: 'line',
    data: {
      labels: Array.from({length: 14}, (_, i) => `8/${i+1}`),
      datasets: [
        {
          label: '倒伏风险指数',
          data: [35, 42, 38, 45, 50, 48, 55, 60, 58, 62, 59, 65, 63, 65],
          borderColor: 'rgba(244, 67, 54, 1)',
          backgroundColor: 'rgba(244, 67, 54, 0.1)',
          tension: 0.4,
          fill: true
        },
        {
          label: '茎秆强度 (MPa)',
          data: [3.2, 3.1, 3.0, 2.9, 2.8, 2.9, 2.8, 2.7, 2.8, 2.7, 2.8, 2.8, 2.8, 2.8],
          borderColor: 'rgba(76, 175, 80, 1)',
          backgroundColor: 'transparent',
          tension: 0.4,
          yAxisID: 'y1'
        },
        {
          label: '气象风险因子',
          data: [25, 30, 28, 35, 40, 38, 45, 50, 48, 55, 52, 60, 58, 62],
          borderColor: 'rgba(33, 150, 243, 1)',
          backgroundColor: 'transparent',
          tension: 0.4,
          yAxisID: 'y2'
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        y: {
          beginAtZero: true,
          max: 100,
          title: {
            display: true,
            text: '倒伏风险指数 (%)'
          }
        },
        y1: {
          position: 'right',
          grid: { drawOnChartArea: false },
          max: 5,
          title: {
            display: true,
            text: '茎秆强度 (MPa)'
          }
        },
        y2: {
          position: 'right',
          offset: true,
          grid: { drawOnChartArea: false },
          max: 100,
          title: {
            display: true,
            text: '气象风险因子'
          }
        }
      }
    }
  });
};

// 评估风险
const assessRisk = () => {
  // 模拟风险评估计算
  const newRisk = Math.min(100, Math.max(0, riskPercentage.value + (Math.random() * 10 - 5)));
  riskPercentage.value = Math.round(newRisk * 10) / 10;
  updateRiskLevel();
  alert(`风险评估完成，当前倒伏风险等级为${riskLevelText.value}`);
};

// 显示7天预测
const showForecast = () => {
  alert('7天倒伏风险预测功能即将上线，敬请期待！');
};

// 显示防控指南
const showGuidelines = () => {
  // 在实际应用中，这里可以打开详细的防控指南模态框
  alert('防控指南已显示在页面下方');
};

// 初始化
onMounted(() => {
  updateRiskLevel();
  updateIndicatorStatus();
  initRiskChart();
});

// 清理
onUnmounted(() => {
  if (riskTrendChart.value) {
    riskTrendChart.value.destroy();
  }
});
</script>

<style scoped>
.lodging-risk-container {
  padding: 20px;
}

.risk-status-overview {
  display: grid;
  grid-template-columns: 1fr 3fr;
  gap: 20px;
  margin: 20px 0;
}

.risk-level-indicator {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding: 20px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.risk-level-label {
  font-size: 16px;
  color: #666;
  margin-bottom: 10px;
}

.risk-level-value {
  font-size: 32px;
  font-weight: 700;
  position: relative;
  height: 40px;
  line-height: 40px;
}

.risk-level-meter {
  position: absolute;
  bottom: 0;
  left: 0;
  height: 6px;
  border-radius: 3px;
}

.risk-low .risk-level-meter { background-color: #4CAF50; }
.risk-medium .risk-level-meter { background-color: #FFC107; }
.risk-high .risk-level-meter { background-color: #F44336; }

.risk-low { color: #4CAF50; }
.risk-medium { color: #FF9800; }
.risk-high { color: #F44336; }

.risk-metrics {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
}

.metric-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding: 15px;
}

.metric-value {
  font-size: 24px;
  font-weight: 700;
  margin: 10px 0;
}

.metric-status {
  font-size: 14px;
  padding: 3px 8px;
  border-radius: 4px;
  display: inline-block;
}

.status-good { background-color: #e8f5e9; color: #2E7D32; }
.status-warning { background-color: #fff8e1; color: #FF8F00; }
.status-danger { background-color: #ffebee; color: #D32F2F; }

.risk-analysis-section {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
  margin: 20px 0;
}

.factors-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding: 20px;
}

.factors-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 15px;
  margin-top: 15px;
}

.factor-item {
  padding: 15px;
  border-radius: 12px;
  display: flex;
  gap: 15px;
}

.factor-icon {
  font-size: 24px;
  padding: 10px;
  border-radius: 8px;
  background-color: #f1f3f5;
}

.factor-high { background-color: #fff3e0; border-left: 4px solid #F44336; }
.factor-medium { background-color: #e3f2fd; border-left: 4px solid #2196F3; }
.factor-low { background-color: #e8f5e9; border-left: 4px solid #4CAF50; }

.factor-impact {
  font-size: 12px;
  color: #666;
  margin-top: 5px;
}

.control-measures {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding: 20px;
  margin-top: 20px;
}

.measures-tabs {
  display: flex;
  border-bottom: 1px solid #eee;
  margin-top: 15px;
}

.measure-tab {
  padding: 10px 20px;
  border: none;
  background: none;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: #666;
  position: relative;
}

.measure-tab.active {
  color: #2196F3;
}

.measure-tab.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 100%;
  height: 2px;
  background-color: #2196F3;
}

.measures-content {
  padding: 20px 0;
}

.measures-list {
  padding-left: 20px;
}

.measures-list li {
  margin-bottom: 10px;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .risk-status-overview,
  .risk-analysis-section {
    grid-template-columns: 1fr;
  }

  .risk-metrics {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>