<template>
  <div v-if="activeMenu === '病虫害防控'" class="pest-control-container">
    <!-- 预警通知区域 -->
    <div class="upload-section">
      <h3>上传图片进行病虫害识别</h3>
      <input type="file" accept="image/*" @change="handleFileChange" />
      <button class="btn-upload" @click="uploadImage" :disabled="!selectedFile">
        📤 上传并识别
      </button>

       <!-- 图片预览 -->
      <div v-if="previewUrl" class="preview-section">
      <h4>图片预览：</h4>
      <img :src="previewUrl" alt="上传预览" class="w-64 h-64 object-contain border" />
      </div>

      <div v-if="uploadResult" class="upload-result">
        <h4>识别结果：</h4>
        <p><strong>病虫害类型:</strong>{{ uploadResult.name }}</p>
        <p><strong>置信度:</strong>{{ uploadResult.confidence }}</p>
      </div>
    </div>

    
    <div class="content-header">
      <h2>病虫害防控管理</h2>
      <div class="content-actions">
        <button class="btn-detection" @click="startDetection">🔍 开始检测</button>
        <button class="btn-report" @click="generateReport">📋 生成报告</button>
      </div>
    </div>

    <div class="card-container">
      <div class="chart-card">
        <div class="chart-card-header">
          <h3>病虫害发生趋势</h3>
          <div class="period-selector">
            <button class="period-btn" :class="{ active: period === 'month' }" @click="period = 'month'">
              月
            </button>
            <button class="period-btn" :class="{ active: period === 'quarter' }" @click="period = 'quarter'">
              季度
            </button>
          </div>
        </div>
        <div class="chart-container">
          <canvas id="pestTrendChart"></canvas>
        </div>
      </div>

      <div class="info-grid">
        <div class="info-card">
          <div class="info-card-icon">⚠️</div>
          <div class="info-card-content">
            <p class="info-card-label">当前预警</p>
            <p class="info-card-value">低风险</p>
          </div>
        </div>

        <div class="info-card">
          <div class="info-card-icon">🐛</div>
          <div class="info-card-content">
            <p class="info-card-label">主要病虫害</p>
            <p class="info-card-value">蚜虫、菜青虫</p>
          </div>
        </div>

        <div class="info-card">
          <div class="info-card-icon">🌡️</div>
          <div class="info-card-content">
            <p class="info-card-label">风险指数</p>
            <p class="info-card-value">32/100</p>
          </div>
        </div>
      </div>
    </div>

    <div class="pest-analysis-section">
      <h3>虫情分析数据</h3>
      <div class="analysis-grid">
        <div class="analysis-card">
          <h4>种群动态分析</h4>
          <div class="chart-container small-chart">
            <canvas id="populationChart"></canvas>
          </div>
        </div>
        <div class="analysis-card">
          <h4>环境相关性</h4>
          <div class="chart-container small-chart">
            <canvas id="environmentChart"></canvas>
          </div>
        </div>
      </div>
    </div>

    <div class="pest-types-section">
      <h3>病虫害类型与发病情况</h3>
      <div class="types-grid">
        <div class="type-card">
          <div class="type-icon">🐛</div>
          <div class="type-info">
            <h4>蚜虫</h4>
            <p><strong>发病面积:</strong> 120亩</p>
            <p><strong>发病率:</strong> 18.5%</p>
            <p><strong>主要区域:</strong> 东部、南部片区</p>
          </div>
        </div>
        <div class="type-card">
          <div class="type-icon">🦋</div>
          <div class="type-info">
            <h4>菜青虫</h4>
            <p><strong>发病面积:</strong> 85亩</p>
            <p><strong>发病率:</strong> 12.3%</p>
            <p><strong>主要区域:</strong> 南部片区</p>
          </div>
        </div>
        <div class="type-card">
          <div class="type-icon">🍄</div>
          <div class="type-info">
            <h4>菌核病</h4>
            <p><strong>发病面积:</strong> 50亩</p>
            <p><strong>发病率:</strong> 7.2%</p>
            <p><strong>主要区域:</strong> 北部片区</p>
          </div>
        </div>
      </div>
    </div>

    <div class="intervention-comparison">
      <h3>干预组与不干预组趋势对比</h3>
      <div class="chart-container comparison-chart">
        <canvas id="interventionChart"></canvas>
      </div>
      <div class="comparison-legend">
        <div class="legend-item"><span class="color-box干预"></span> 干预组 (已施药)</div>
        <div class="legend-item"><span class="color-box对照"></span> 对照组 (未施药)</div>
      </div>
    </div>

    <div class="detection-results">
      <h3>近期检测结果</h3>
      <div class="results-grid">
        <div class="result-card" v-for="result in detectionResults" :key="result.id">
          <div class="result-card-header">
            <span class="result-date">{{ result.date }}</span>
            <span :class="severityClass(result.severity)">{{ result.severity }}</span>
          </div>
          <div class="result-card-body">
            <p class="result-desc">{{ result.description }}</p>
            <div class="result-solution">
              <p><strong>防治建议:</strong> {{ result.solution }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { Chart } from 'chart.js'; // 导入 Chart 核心类型

import { pestControlApi } from '../services/pestControlApi';
import type { PestDetectionData } from '../services/pestControlApi';

const selectedFile = ref<File | null>(null);
const uploadResult = ref<PestDetectionData | null>(null);
const previewUrl = ref<string | null>(null); // 新增预览 URL

const handleFileChange = (event: Event) => {
  const files = (event.target as HTMLInputElement).files;
  if (files && files.length > 0) {
    selectedFile.value = files[0];
    previewUrl.value = URL.createObjectURL(files[0]); // 生成本地预览 URL
  }
};
const uploadImage = async () => {
  if (!selectedFile.value) return;
  const formData = new FormData();
  formData.append('image', selectedFile.value);

  try {
    const result = await pestControlApi.uploadPestImage(formData);
    uploadResult.value = result;
  } catch (error) {
    console.error('上传失败', error);
    alert('上传失败，请重试');
  }
};

onUnmounted(() => {
  if (previewUrl.value) URL.revokeObjectURL(previewUrl.value);
});



// 1. 定义 Props 类型（保持原有逻辑）
const props = defineProps({
  activeMenu: {
    type: String,
    required: true,
    validator: (val: string) => val === 'pestControl' || true // 简单校验
  }
});

// 2. 定义状态并明确类型（核心修改：解决 null 类型问题）
// 图表实例：允许为 Chart 实例 或 null
const pestTrendChart = ref<Chart | null>(null);
// 周期：仅允许 'month'/'quarter' 两种值（类型收窄，避免无效值）
const period = ref<'month' | 'quarter'>('month');
// 检测结果：定义接口约束数据结构（类型更严谨）
interface DetectionResult {
  id: number;
  date: string;
  severity: '轻微' | '轻度' | '中度' | '严重'; // 仅允许指定严重程度
  description: string;
  solution: string;
}
const detectionResults = ref<DetectionResult[]>([
  {
    id: 1,
    date: '2025-08-24',
    severity: '轻度',
    description: '东部片区发现少量蚜虫聚集，密度0.5头/株',
    solution: '喷施10%吡虫啉可湿性粉剂2000倍液'
  },
  {
    id: 2,
    date: '2025-08-27',
    severity: '轻微',
    description: '南部片区发现零星菜青虫幼虫',
    solution: '人工捕捉，无需药剂防治'
  },
  {
    id: 3,
    date: '2025-08-29',
    severity: '中度',
    description: '北部片区出现菌核病初期症状',
    solution: '喷施50%腐霉利可湿性粉剂1000倍液'
  }
]);

// 3. 严重程度样式映射（保持原有逻辑）
const severityClass = (severity: DetectionResult['severity']) => {
  switch (severity) {
    case '轻微': return 'severity-minor';
    case '轻度': return 'severity-mild';
    case '中度': return 'severity-moderate';
    case '严重': return 'severity-severe';
    default: return '';
  }
};

// 4. 图表初始化（优化：提前销毁旧实例，避免内存泄漏）
const initChart = () => {
  // 获取 Canvas 元素（明确类型：HTMLCanvasElement 或 null）
  const canvas = document.getElementById('pestTrendChart') as HTMLCanvasElement | null;
  if (!canvas) return; // 若元素不存在，直接返回（避免报错）

  // 销毁已有图表实例（防止重复创建）
  if (pestTrendChart.value) {
    pestTrendChart.value.destroy();
    pestTrendChart.value = null; // 重置为 null
  }

  // 根据周期生成标签和数据
  const { labels, aphidData, caterpillarData, sclerotiniaData } = (() => {
    if (period.value === 'month') {
      return {
        labels: ['1日', '5日', '10日', '15日', '20日', '25日', '30日'],
        aphidData: [5, 8, 12, 10, 7, 5, 3], // 蚜虫数据
        caterpillarData: [3, 5, 7, 9, 6, 4, 2], // 菜青虫数据
        sclerotiniaData: [2, 3, 5, 8, 6, 4, 3] // 菌核病数据
      };
    }
    // 季度数据
    return {
      labels: ['9月', '10月', '11月'],
      aphidData: [15, 8, 5],
      caterpillarData: [10, 7, 3],
      sclerotiniaData: [8, 6, 4]
    };
  })();

  // 创建新图表实例（类型自动匹配，无报错）
  pestTrendChart.value = new Chart(canvas, {
    type: 'line',
    data: {
      labels,
      datasets: [
        {
          label: '蚜虫',
          data: aphidData,
          borderColor: 'rgba(255, 152, 0, 1)',
          backgroundColor: 'rgba(255, 152, 0, 0.1)',
          tension: 0.3,
          fill: true
        },
        {
          label: '菜青虫',
          data: caterpillarData,
          borderColor: 'rgba(244, 67, 54, 1)',
          backgroundColor: 'rgba(244, 67, 54, 0.1)',
          tension: 0.3,
          fill: true
        },
        {
          label: '菌核病',
          data: sclerotiniaData,
          borderColor: 'rgba(156, 39, 176, 1)',
          backgroundColor: 'rgba(156, 39, 176, 0.1)',
          tension: 0.3,
          fill: true
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        x: {
          title: {
            display: true,
            text: '时间' // 补充 X 轴标题，图表更清晰
          }
        },
        y: {
          beginAtZero: true,
          title: {
            display: true,
            text: '发生程度指数'
          }
        }
      },
      plugins: {
        legend: {
          position: 'top'
        },
        tooltip: {
          mode: 'index', // 鼠标悬浮时显示同时间点所有数据
          intersect: false
        }
      }
    }
  });
};

// 5. 按钮点击事件（保持原有逻辑）
const startDetection = () => {
  alert('病虫害检测功能启动中...');
};

const generateReport = () => {
  alert('病虫害报告已生成');
};

// 6. 生命周期与监听（优化：确保组件挂载后初始化图表）
onMounted(() => {
  // 仅当当前菜单是 "pestControl" 时初始化（避免无效执行）
  if (props.activeMenu === 'pestControl') {
    initChart();
  }
});

// 监听周期变化，更新图表（逻辑更简洁）
watch(period, initChart);

// 7. 组件卸载时销毁图表（防止内存泄漏，补充优化）
import { onUnmounted } from 'vue';
onUnmounted(() => {
  if (pestTrendChart.value) {
    pestTrendChart.value.destroy();
  }
});
</script>

<style scoped>
/* 保持原有样式不变，此处省略重复代码（与原代码一致） */
.pest-control-container {
  padding: 20px;
}
.upload-section {
  background: white;
  padding: 20px;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  margin-bottom: 20px;
}

.upload-section input[type="file"] {
  margin-bottom: 10px;
}
.preview-section {
  margin-top: 12px;
  text-align: center;
}

.preview-section h4 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #374151; /* 深灰色标题 */
}

.preview-section img {
  width: 1024px;
  height: 1024px;
  object-fit: contain;
  border: 1px solid #d1d5db;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.preview-section img:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.15);
}
.btn-upload {
  padding: 6px 14px;
  border-radius: 8px;
  background-color: #1976D2;
  color: white;
  border: none;
  cursor: pointer;
}

.btn-upload:disabled {
  background-color: #a0c4ff;
  cursor: not-allowed;
}

.upload-result {
  margin-top: 15px;
  background-color: #f9f9f9;
  padding: 12px;
  border-radius: 8px;
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

.btn-detection,
.btn-report {
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

.btn-detection {
  background-color: #e3f2fd;
  color: #1976D2;
}

.btn-report {
  background-color: #f0f7ee;
  color: #2E7D32;
}

.btn-detection:hover,
.btn-report:hover {
  opacity: 0.9;
  transform: translateY(-2px);
}

.card-container {
  margin-bottom: 30px;
}

.chart-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding: 20px;
  margin-bottom: 20px;
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
  background-color: #ffebee;
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
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}

.detection-results {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding: 20px;
}

.detection-results h3 {
  color: #333;
  font-size: 18px;
  margin-top: 0;
  margin-bottom: 20px;
}

.results-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 15px;
}

.result-card {
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  padding: 15px;
}

.result-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.result-date {
  font-size: 13px;
  color: #666;
}

.result-desc {
  margin: 0 0 10px 0;
  color: #333;
}

.result-solution {
  font-size: 14px;
  background-color: #f9f9f9;
  padding: 10px;
  border-radius: 8px;
}

.severity-minor {
  background-color: #e8f5e9;
  color: #2E7D32;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 13px;
}

.severity-mild {
  background-color: #fff8e1;
  color: #FF8F00;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 13px;
}

.severity-moderate {
  background-color: #fff3e0;
  color: #FF5722;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 13px;
}

.severity-severe {
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

  .results-grid {
    grid-template-columns: 1fr;
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