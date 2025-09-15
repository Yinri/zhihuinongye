<template>
  <div class="fertilizer-recommendation-container">
    <div class="content-header">
      <h2>追肥推荐管理</h2>
      <div class="content-actions">
        <button class="btn-save" @click="saveFertilizerData">
          <i class="icon-save"></i> 保存施肥数据
        </button>
        <button class="btn-refresh" @click="refreshCharts">
          <i class="icon-refresh"></i> 刷新数据
        </button>
      </div>
    </div>

    <!-- 文件上传区域 -->
    <div class="upload-section card">
      <div class="upload-header">
        <h3>土壤检测数据上传</h3>
        <p class="upload-description">上传土壤养分检测报告，系统将根据土壤数据生成精准施肥方案</p>
      </div>
      <el-upload
        ref="uploadRef"
        class="upload-excel"
        action="#"
        :on-change="handleFileChange"
        :before-upload="beforeUpload"
        :auto-upload="false"
        accept=".xlsx,.xls"
      >
        <div class="upload-area" @click="handleUploadClick">
          <i class="el-icon-upload el-icon-upload--large"></i>
          <div class="upload-text">
            <span class="upload-main-text">点击或拖拽文件至此处上传</span>
            <span class="upload-sub-text">支持.xlsx或.xls格式文件</span>
          </div>
          <el-button size="primary" icon="el-icon-upload2" class="upload-trigger-btn">选择文件</el-button>
        </div>
        <div slot="tip" class="el-upload__tip">
          <i class="el-icon-info"></i> 文件需包含以下列：nitrogen, phosphorus, potassium, organic_matter, ph
        </div>
      </el-upload>
      <div class="upload-actions" v-if="uploadedFile">
        <el-button
          size="primary"
          icon="el-icon-check"
          @click="uploadFile"
          :loading="uploadLoading"
          class="upload-btn"
        >
          {{ uploadLoading ? '上传中...' : '确认上传' }}
        </el-button>
        <el-button
          size="default"
          icon="el-icon-circle-close"
          @click="clearFile"
          class="cancel-btn"
        >
          取消
        </el-button>
      </div>
      <div v-if="uploadMessage" class="upload-message" v-html="uploadMessage"></div>
      
    </div>

    <!-- 推荐施肥量展示 -->
</div>
      <h3>推荐施肥量</h3>
      <div class="recommendation-values">
        <div class="value-item">
          <span class="label">氮肥 (N):</span>
          <span class="value">{{ recommendedNitrogen }} kg/ha</span>
        </div>
        <div class="value-item">
          <span class="label">磷肥 (P):</span>
          <span class="value">{{ recommendedPhosphorus }} kg/ha</span>
        </div>
        <div class="value-item">
          <span class="label">钾肥 (K):</span>
          <span class="value">{{ recommendedPotassium }} kg/ha</span>
        </div>
        <div class="value-item">
          <span class="label">推荐肥料类型:</span>
          <span class="value">{{ recommendedFertilizerType }}</span>
        </div>
        <div class="value-item">
          <span class="label">施用方法:</span>
          <span class="value">{{ recommendedApplicationMethod }}</span>
        </div>
      </div>
    

    <!-- 农户实际施肥数据输入区域 -->
    <div class="input-section card">
      <h3>农户实际施肥数据录入</h3>
      <div class="form-grid">
        <div class="form-group">
          <label for="nitrogen">氮肥施用量 (kg/亩)</label>
          <input type="number" id="nitrogen" v-model.number="fertilizerData.nitrogen" min="0" step="0.1">
        </div>
        <div class="form-group">
          <label for="phosphorus">磷肥施用量 (kg/亩)</label>
          <input type="number" id="phosphorus" v-model.number="fertilizerData.phosphorus" min="0" step="0.1">
        </div>
        <div class="form-group">
          <label for="potassium">钾肥施用量 (kg/亩)</label>
          <input type="number" id="potassium" v-model.number="fertilizerData.potassium" min="0" step="0.1">
        </div>
        <div class="form-group">
          <label for="application-date">施肥日期</label>
          <input type="date" id="application-date" v-model="fertilizerData.applicationDate">
        </div>
        <div class="form-group">
          <label for="fertilizer-type">肥料类型</label>
          <select id="fertilizer-type" v-model="fertilizerData.fertilizerType">
            <option value="urea">尿素</option>
            <option value="compound">复合肥</option>
            <option value="organic">有机肥</option>
            <option value="other">其他</option>
          </select>
        </div>
        <div class="form-group">
          <label for="notes">备注信息</label>
          <textarea id="notes" v-model="fertilizerData.notes" rows="2"></textarea>
        </div>
      </div>
    </div>

    <!-- 数据图表展示区域 -->
    <div class="charts-container">
      <!-- 推荐量与实际氮磷钾差值图表 -->
      <div class="chart-card">
        <div class="chart-card-header">
          <h3>推荐施肥量与实际施用量对比</h3>
          <span class="chart-period">2025年8月数据</span>
        </div>
        <div class="chart-container">
          <canvas id="fertilizerComparisonChart"></canvas>
        </div>
        <div class="chart-insights">
          <p><i class="icon-info"></i> 当前氮肥施用量低于推荐值12.5kg/亩，可能影响油菜开花结果。</p>
        </div>
      </div>

      <!-- 干预组与不干预组趋势对比 -->
      <div class="chart-card">
        <div class="chart-card-header">
          <h3>干预组与对照组生长趋势对比</h3>
          <span class="chart-period">2025年8月1日-8月25日</span>
        </div>
        <div class="chart-container">
          <canvas id="interventionComparisonChart"></canvas>
        </div>
        <div class="comparison-metrics">
          <div class="metric-item">
            <span class="metric-label">干预组产量预测</span>
            <span class="metric-value">185 kg/亩</span>
          </div>
          <div class="metric-item">
            <span class="metric-label">对照组产量预测</span>
            <span class="metric-value">152 kg/亩</span>
          </div>
          <div class="metric-item">
            <span class="metric-label">增产潜力</span>
            <span class="metric-value">+21.7%</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 施肥历史记录表格 -->
    <div class="history-section card">
      <h3>施肥历史记录</h3>
      <div class="table-container">
        <table class="fertilizer-history-table">
          <thead>
            <tr>
              <th>日期</th>
              <th>氮肥(kg/亩)</th>
              <th>磷肥(kg/亩)</th>
              <th>钾肥(kg/亩)</th>
              <th>肥料类型</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(record, index) in fertilizerHistory" :key="index">
              <td>{{ record.applicationDate }}</td>
              <td>{{ record.nitrogen.toFixed(1) }}</td>
              <td>{{ record.phosphorus.toFixed(1) }}</td>
              <td>{{ record.potassium.toFixed(1) }}</td>
              <td>{{ formatFertilizerType(record.fertilizerType) }}</td>
              <td>
                <button class="btn-edit" @click="editRecord(index)">编辑</button>
                <button class="btn-delete" @click="deleteRecord(index)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, getCurrentInstance } from 'vue';
import axios from 'axios';
import Chart from 'chart.js/auto';

// 推荐施肥量相关变量
const recommendedNitrogen = ref(0);
const recommendedPhosphorus = ref(0);
const recommendedPotassium = ref(0);
const recommendedFertilizerType = ref('');
const recommendedApplicationMethod = ref('');

// 获取组件实例
const instance = getCurrentInstance();
const uploadRef = ref(null);

// 文件上传相关变量
const uploadedFile = ref<File | null>(null);
const uploadMessage = ref('');
const uploadLoading = ref(false);

// 文件上传方法
const beforeUpload = (file: File) => {
  const isXLSX = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' || file.type === 'application/vnd.ms-excel';
  if (!isXLSX) {
    uploadMessage.value = '请上传Excel文件(.xlsx或.xls)';
    return false;
  }
  return true;
};

const handleUploadClick = () => {
  // 触发文件选择对话框
  if (uploadRef.value) {
    (uploadRef.value as any).$refs.input.click();
  }
};

const handleFileChange = (file: File) => {
  uploadedFile.value = file;
  uploadMessage.value = `已选择文件: ${file.name}`;
};

// 获取推荐施肥量数据
const fetchRecommendations = async () => {
  try {
    const response = await axios.get('/api/fertilizer/recommendation');
    recommendedNitrogen.value = response.data.nitrogen;
    recommendedPhosphorus.value = response.data.phosphorus;
    recommendedPotassium.value = response.data.potassium;
    recommendedFertilizerType.value = response.data.fertilizerType || '复合肥料(NPK 15-15-15)';
    recommendedApplicationMethod.value = response.data.applicationMethod || '叶面喷施，每亩用水量40-50升';
  } catch (error) {
    console.error('获取推荐施肥量失败:', error);
    // 后端API异常时显示默认值
    recommendedFertilizerType.value = '复合肥料(NPK 15-15-15)';
    recommendedApplicationMethod.value = '叶面喷施，每亩用水量40-50升';
  }
};

const clearFile = () => {
  uploadedFile.value = null;
  uploadMessage.value = '';
};

const uploadFile = async () => {
  if (!uploadedFile.value) {
    uploadMessage.value = '<span class="text-warning"><i class="el-icon-warning"></i> 请先选择文件</span>';
    return;
  }

  const formData = new FormData();
  formData.append('file', uploadedFile.value);

  try {
    uploadLoading.value = true;
    uploadMessage.value = '<span class="text-success"><i class="el-icon-loading el-icon--loading"></i> 文件上传中...</span>';
    
    // 调试信息：打印上传的文件信息
    console.log('上传文件信息:', uploadedFile.value);
    console.log('FormData内容:', Array.from(formData.entries()));
    
    const response = await axios.post('/api/fertilizer/upload-soil-data', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        'X-Requested-With': 'XMLHttpRequest'
      },
      timeout: 30000, // 设置30秒超时
      onUploadProgress: (progressEvent) => {
        const percent = Math.round((progressEvent.loaded * 100) / (progressEvent.total || 1));
        uploadMessage.value = `<span class="text-success"><i class="el-icon-loading el-icon--loading"></i> 文件上传中... ${percent}%</span>`;
      }
    });
    
    uploadMessage.value = '<span class="text-success"><i class="el-icon-check"></i> 文件上传成功，已生成施肥推荐</span>';
    fetchRecommendations();
    clearFile();
  } catch (error) {
    let errorMsg = '未知错误';
    if (error instanceof Error) {
      errorMsg = error.message;
    } else if (axios.isAxiosError(error)) {
      if (error.response) {
        // 服务器返回错误
        errorMsg = `服务器错误: ${error.response.status} - ${error.response.data?.message || '无错误信息'}`;
      } else if (error.request) {
        // 请求已发送但无响应
        errorMsg = '服务器无响应，请检查后端服务是否运行';
      } else {
        // 请求配置错误
        errorMsg = `请求错误: ${error.message}`;
      }
    }
    uploadMessage.value = `<span class="text-danger"><i class="el-icon-error"></i> 文件上传失败: ${errorMsg}</span>`;
    console.error('文件上传错误详情:', error);
  } finally {
    uploadLoading.value = false;
  }
};

// 定义施肥数据接口
interface FertilizerData {
  nitrogen: number;
  phosphorus: number;
  potassium: number;
  applicationDate: string;
  fertilizerType: string;
  notes?: string;
}

// 实际施肥数据
const fertilizerData = ref<FertilizerData>({
  nitrogen: 0,
  phosphorus: 0,
  potassium: 0,
  applicationDate: new Date().toISOString().split('T')[0],
  fertilizerType: 'urea',
  notes: ''
});

// 施肥历史记录
const fertilizerHistory = ref<FertilizerData[]>([
  {
    nitrogen: 15.2,
    phosphorus: 8.5,
    potassium: 12.0,
    applicationDate: '2025-08-05',
    fertilizerType: 'urea',
    notes: '初花期追肥'
  },
  {
    nitrogen: 10.8,
    phosphorus: 6.2,
    potassium: 9.5,
    applicationDate: '2025-08-15',
    fertilizerType: 'compound',
    notes: '结荚期追肥'
  }
]);

// 图表实例
let fertilizerChart: Chart | null = null;
let interventionChart: Chart | null = null;

// 初始化图表
const initCharts = () => {
  // 销毁已有图表
  if (fertilizerChart) fertilizerChart.destroy();
  if (interventionChart) interventionChart.destroy();

  // 推荐施肥量与实际施用量对比图表
  const fertilizerCtx = document.getElementById('fertilizerComparisonChart') as HTMLCanvasElement;
  fertilizerChart = new Chart(fertilizerCtx, {
    type: 'bar',
    data: {
      labels: ['氮肥 (N)', '磷肥 (P)', '钾肥 (K)'],
      datasets: [
        {
          label: '推荐施用量',
          data: [25.5, 12.8, 18.2],
          backgroundColor: 'rgba(54, 162, 235, 0.6)',
          borderColor: 'rgba(54, 162, 235, 1)',
          borderWidth: 1
        },
        {
          label: '实际施用量',
          data: [fertilizerData.value.nitrogen || 0, fertilizerData.value.phosphorus || 0, fertilizerData.value.potassium || 0],
          backgroundColor: 'rgba(255, 99, 132, 0.6)',
          borderColor: 'rgba(255, 99, 132, 1)',
          borderWidth: 1
        }
      ]
    },
    options: {
  animation: {
      onComplete: function(this: any) {
        const chart = this.chart;
        if (chart && chart.data && chart.data.datasets) {
          chart.data.datasets.forEach((dataset: any) => {
            dataset.disabled = dataset.disabled !== undefined ? dataset.disabled : false;
          });
        }
      }
    },
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        y: {
          beginAtZero: true,
          title: {
            display: true,
            text: '施用量 (kg/亩)'
          }
        }
      },
      plugins: {
        tooltip: {
          callbacks: {
            label: function(context) {
              return `${context.dataset.label}: ${context.raw} kg/亩`;
            }
          }
        }
      }
    }
  });

  // 干预组与对照组趋势对比图表
  const interventionCtx = document.getElementById('interventionComparisonChart') as HTMLCanvasElement;
  interventionChart = new Chart(interventionCtx, {
    type: 'line',
    data: {
      labels: ['8/1', '8/5', '8/10', '8/15', '8/20', '8/25'],
      datasets: [
        {
          label: '干预组 (优化施肥)',
          data: [12.3, 18.5, 25.8, 32.4, 38.7, 45.2],
          borderColor: 'rgba(75, 192, 192, 1)',
          backgroundColor: 'rgba(75, 192, 192, 0.1)',
          tension: 0.3,
          fill: true
        },
        {
          label: '对照组 (传统施肥)',
          data: [12.3, 15.6, 19.2, 22.8, 26.5, 30.1],
          borderColor: 'rgba(255, 159, 64, 1)',
          backgroundColor: 'rgba(255, 159, 64, 0.1)',
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
          beginAtZero: true,
          title: {
            display: true,
            text: '作物生长指数'
          }
        }
      }
    }
  });
};

// 保存施肥数据
const saveFertilizerData = () => {
  // 添加新记录到历史
  fertilizerHistory.value.unshift({...fertilizerData.value});
  // 重置表单
  fertilizerData.value = {
    nitrogen: 0,
    phosphorus: 0,
    potassium: 0,
    applicationDate: new Date().toISOString().split('T')[0],
    fertilizerType: 'urea',
    notes: ''
  };
  // 刷新图表
  initCharts();
  alert('施肥数据已保存！');
};

// 刷新图表
const refreshCharts = () => {
  initCharts();
};

// 编辑记录
const editRecord = (index: number) => {
  const record = fertilizerHistory.value[index];
  fertilizerData.value = {...record};
  // 从历史中移除
  fertilizerHistory.value.splice(index, 1);
};

// 删除记录
const deleteRecord = (index: number) => {
  if (confirm('确定要删除这条记录吗？')) {
    fertilizerHistory.value.splice(index, 1);
  }
};

// 格式化肥料类型显示
const formatFertilizerType = (type: string): string => {
  switch(type) {
    case 'urea': return '尿素';
    case 'compound': return '复合肥';
    case 'organic': return '有机肥';
    default: return '其他';
  }
};

// 组件挂载时初始化图表
onMounted(() => {
  initCharts();
  fetchRecommendations();
  // 监听窗口大小变化，重绘图表
  window.addEventListener('resize', initCharts);
});

// 组件卸载时清理
onUnmounted(() => {
  window.removeEventListener('resize', initCharts);
  if (fertilizerChart) fertilizerChart.destroy();
  if (interventionChart) interventionChart.destroy();
});
</script>

<style scoped>
.fertilizer-recommendation-container {
  padding: 20px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 15px;
  margin-top: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-weight: 500;
  color: #666;
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
}

.charts-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
  gap: 20px;
  margin: 20px 0;
}

.history-section {
  margin-top: 20px;
}

.table-container {
  overflow-x: auto;
  margin-top: 15px;
}

.fertilizer-history-table {
  width: 100%;
  border-collapse: collapse;
}

.fertilizer-history-table th,
.fertilizer-history-table td {
  padding: 12px 15px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.fertilizer-history-table th {
  background-color: #f5f7fa;
  font-weight: 600;
}

.btn-save,
.btn-refresh,
.btn-edit,
.btn-delete {
  padding: 8px 12px;
  border-radius: 6px;
  border: none;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.btn-save {
  background-color: #4CAF50;
  color: white;
}

.btn-refresh {
  background-color: #2196F3;
  color: white;
}

.btn-edit {
  background-color: #e3f2fd;
  color: #1976D2;
}

.btn-delete {
  background-color: #ffebee;
  color: #D32F2F;
}

.comparison-metrics {
  display: flex;
  justify-content: space-around;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px dashed #eee;
}

.metric-item {
  text-align: center;
}

.metric-label {
  display: block;
  color: #666;
  font-size: 14px;
}

.metric-value {
  display: block;
  font-size: 18px;
  font-weight: 700;
  color: #222;
}

.chart-insights {
  margin-top: 15px;
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 6px;
  font-size: 14px;
  color: #333;
}

.upload-section {
  margin: 20px 0;
}

.upload-header {
  margin-bottom: 15px;
}

.upload-description {
  color: #666;
  margin-top: 5px;
  font-size: 14px;
}

.upload-area {
  border: 2px dashed #c0ccda;
  border-radius: 6px;
  padding: 40px 20px;
  text-align: center;
  transition: all 0.3s;
  cursor: pointer;
}

.upload-area:hover {
  border-color: #409eff;
}

.el-icon-upload--large {
  font-size: 48px;
  color: #c0ccda;
  margin-bottom: 16px;
}

.upload-text .upload-main-text {
  display: block;
  font-size: 16px;
  color: #303133;
  margin-bottom: 6px;
}

.upload-text .upload-sub-text {
  font-size: 14px;
  color: #909399;
}

.upload-actions {
  margin-top: 15px;
  display: flex;
  gap: 10px;
}

.upload-btn {
  flex: 1;
}

.cancel-btn {
  flex: 1;
}

.upload-message {
  margin-top: 15px;
  padding: 10px;
  border-radius: 4px;
  font-size: 14px;
}

.upload-example {
  margin-top: 15px;
  text-align: right;
}

.card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  padding: 20px;
  margin-bottom: 20px;
}

@media (max-width: 768px) {
  .charts-container {
    grid-template-columns: 1fr;
  }
}
</style>