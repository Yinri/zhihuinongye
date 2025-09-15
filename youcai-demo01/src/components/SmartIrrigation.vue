<template>
  <div class="smart-irrigation-container">
    <!-- 头部标题 -->
    <div class="page-header">
      <h1>智能灌溉决策系统</h1>
      <p class="subtitle">基于多源数据的精准灌溉解决方案</p>
    </div>

    <!-- 数据上传区域 -->
    <div class="upload-section">
      <div class="card">
        <div class="card-header">
          <i class="el-icon-upload"></i>
          <h3>灌溉数据上传</h3>
        </div>
        <div class="card-body">
          <div class="file-upload-form" style="position: relative;">
            <div class="file-input-group">
              <label>NDVI数据文件</label>
              <el-upload
                class="upload-demo"
                :show-file-list="false"
                :on-change="(uploadFile) => uploadFile.raw ? handleFileSelection(uploadFile.raw, 'ndviFile') : null"
                :on-remove="() => handleFileRemove('ndviFile')"
                :auto-upload="false"
                accept=".xlsx,.xls"
              >
                <el-button size="default" :class="ndviFile ? 'file-selected' : ''">
                  <i class="el-icon-document"></i>
                  {{ ndviFile ? ndviFile.name : '选择文件' }}
                </el-button>
              </el-upload>
            </div>
            <div class="file-input-group">
              <label>土壤湿度数据文件</label>
              <el-upload
                class="upload-demo"
                :show-file-list="false"
                :on-change="(uploadFile) => uploadFile.raw ? handleFileSelection(uploadFile.raw, 'soilFile') : null"
                :on-remove="() => handleFileRemove('soilFile')"
                :auto-upload="false"
                accept=".xlsx,.xls"
              >
                <el-button size="default" :class="soilFile ? 'file-selected' : ''">
                  <i class="el-icon-document"></i>
                  {{ soilFile ? soilFile.name : '选择文件' }}
                </el-button>
              </el-upload>
            </div>
            <div class="file-input-group">
              <label>气象数据文件</label>
              <el-upload
                class="upload-demo"
                :show-file-list="false"
                :on-change="(uploadFile) => uploadFile.raw ? handleFileSelection(uploadFile.raw, 'weatherFile') : null"
                :on-remove="() => handleFileRemove('weatherFile')"
                :auto-upload="false"
                accept=".xlsx,.xls"
              >
                <el-button size="default" :class="weatherFile ? 'file-selected' : ''">
                  <i class="el-icon-document"></i>
                  {{ weatherFile ? weatherFile.name : '选择文件' }}
                </el-button>
              </el-upload>
            </div>
            <el-button 
              class="upload-btn"
              @click="uploadFiles"
              :loading="uploading"
              :disabled="!ndviFile || !soilFile || !weatherFile || uploading"
            >
              <i class="el-icon-refresh-right" v-if="uploading"></i>
              <i class="el-icon-line-chart" v-else></i>
              {{ uploading ? '分析中...' : '上传并分析灌溉数据' }}
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 灌溉量显示区域 -->
    <div class="irrigation-result" v-if="showResult">
      <div class="card highlight">
        <div class="card-header">
          <i class="el-icon-waterlily"></i>
          <h3>灌溉决策结果</h3>
        </div>
        <div class="card-body">
          <div class="result-content">
            <div class="irrigation-amount">
              <span class="label">建议灌溉量:</span>
              <span class="value">{{ irrigationAmount.toFixed(2) }} m³</span>
            </div>
            <div class="irrigation-status">
              <el-tag :type="irrigationStatus.type">{{ irrigationStatus.message }}</el-tag>
            </div>
            <div class="irrigation-details">
              <div class="detail-item">
                <span class="label">土壤湿度:</span>
                <span class="value">{{ latestData.soilMoisture.toFixed(1) }}%</span>
                <span class="unit">适宜范围: 40-60%</span>
              </div>
              <div class="detail-item">
                <span class="label">NDVI指数:</span>
                <span class="value">{{ latestData.ndvi.toFixed(2) }}</span>
                <span class="unit">健康范围: >0.5</span>
              </div>
              <div class="detail-item">
                <span class="label">平均温度:</span>
                <span class="value">{{ latestData.temperature.toFixed(1) }}°C</span>
              </div>
              <div class="detail-item">
                <span class="label">今日降雨量:</span>
                <span class="value">{{ latestData.rainfall.toFixed(1) }}mm</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 数据可视化区域 -->
    <div class="visualization-section" v-if="showVisualization">
      <div class="card">
        <div class="card-header">
          <i class="el-icon-pie-chart"></i>
          <h3>数据趋势分析</h3>
        </div>
        <div class="card-body">
          <div class="chart-grid">
            <div class="chart-item">
              <div class="chart-title">NDVI指数趋势</div>
              <canvas id="ndvi-chart" class="chart-container" width="800" height="400"></canvas>
            </div>
            <div class="chart-item">
              <div class="chart-title">土壤湿度趋势</div>
              <canvas id="soil-moisture-chart" class="chart-container" width="800" height="400"></canvas>
            </div>
            <div class="chart-item">
              <div class="chart-title">气象数据综合分析</div>
              <canvas id="weather-chart" class="chart-container" width="800" height="400"></canvas>
            </div>
            <div class="chart-item">
              <div class="chart-title">灌溉需求预测</div>
              <canvas id="irrigation-demand-chart" class="chart-container" width="800" height="400"></canvas>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 加载动画 -->
    <el-loading 
      v-loading="loadingCharts"
      element-loading-text="正在生成图表..."
      element-loading-spinner="el-icon-loading"
      element-loading-background="rgba(0, 0, 0, 0.7)"
      target=".visualization-section"
    ></el-loading>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue';
import { Chart, registerables } from 'chart.js';
import type {
  Chart as ChartBase,
  ChartConfiguration,
  TooltipItem,
  TooltipModel,
  CartesianParsedData,
  BarParsedData
} from 'chart.js';
import { ElUpload, ElButton, ElTag, ElMessage, ElLoading } from 'element-plus';
import irrigationApi from '../services/irrigationApi';

// 注册Chart.js组件
Chart.register(...registerables);

// -------------- 核心修复1：精确的图表类型定义 --------------
// 定义图表类型常量
type ChartType = 'line' | 'bar' | 'combined';

// 为每种图表类型定义精确的数据集接口
type LineDataset = ChartConfiguration<'line'>['data']['datasets'][0];
type BarDataset = ChartConfiguration<'bar'>['data']['datasets'][0];
type CombinedDataset = LineDataset | BarDataset;

// 为每种图表类型定义配置接口
interface LineChartConfig {
  labels: string[];
  datasets: LineDataset[];
  options: ChartConfiguration<'line'>['options'];
}

interface BarChartConfig {
  labels: string[];
  datasets: BarDataset[];
  options: ChartConfiguration<'bar'>['options'];
}

interface CombinedChartConfig {
  labels: string[];
  datasets: CombinedDataset[];
  options: ChartConfiguration<'bar'>['options']; // 基础类型为bar
}

// -------------- 状态管理 --------------
const ndviFile = ref<File | null>(null);
const soilFile = ref<File | null>(null);
const weatherFile = ref<File | null>(null);

const uploading = ref(false);
const loadingCharts = ref(false);
const showVisualization = ref(true); // 设为true，确保图表容器始终可见
const showResult = ref(false);

const irrigationAmount = ref(0);
const latestData = ref({
  soilMoisture: 0,
  ndvi: 0,
  temperature: 0,
  rainfall: 0
});

const irrigationStatus = ref<{ 
  type: 'success' | 'info' | 'warning' | 'danger'; 
  message: string 
}>({
  type: 'success',
  message: '土壤湿度适宜'
});

// -------------- 核心修复2：精确的图表实例存储 --------------
// 使用更精确的类型存储不同图表实例
const lineCharts = ref<Map<string, Chart<'line'>>>(new Map());
const barCharts = ref<Map<string, Chart<'bar'>>>(new Map());
const resizeHandlers = ref<Map<string, () => void>>(new Map());

// -------------- 生命周期 --------------
onMounted(async () => {
  try {
    // 组件挂载时，尝试预先加载一些示例数据以便用户可以看到图表示例
    // 这可以帮助用户理解系统功能，即使在上传实际数据之前
    console.log('组件挂载，准备初始化示例图表数据');
    await initDemoCharts();
  } catch (error) {
    console.error('初始化示例图表失败:', error);
  }
});

onUnmounted(() => {
  // 清理所有图表实例
  lineCharts.value.forEach(chart => chart.destroy());
  barCharts.value.forEach(chart => chart.destroy());
  
  // 清理所有 resize 事件处理器
  resizeHandlers.value.forEach((handler) => {
    window.removeEventListener('resize', handler);
  });
  
  lineCharts.value.clear();
  barCharts.value.clear();
  resizeHandlers.value.clear();
});

// 初始化示例图表（用于组件挂载时预览）
const initDemoCharts = async () => {
  try {
    showVisualization.value = true;
    await nextTick();
    
    // 生成30天的示例数据
    const dates = generateDates(30);
    const ndviData = generateRandomData(30, 0.3, 0.8);
    const soilData = generateRandomData(30, 30, 70);
    const tempData = generateRandomData(30, 15, 30);
    const rainData = generateRandomData(30, 0, 30, true);
    const irrigationDemandData = ndviData.map((ndvi, index) => {
      return Math.max(0, parseFloat((100 - ndvi * 150 - soilData[index] * 0.5 + tempData[index] - rainData[index] * 2).toFixed(2)));
    });
    
    // 立即创建示例图表
    createLineChart('ndvi-chart', {
      labels: dates,
      datasets: [{ label: 'NDVI指数', data: ndviData, borderColor: '#42b983', backgroundColor: 'rgba(66, 185, 131, 0.1)', borderWidth: 2, tension: 0.4, fill: true }],
      options: {
        scales: { y: { min: 0, max: 1, title: { display: true, text: 'NDVI值' } } },
        plugins: { title: { display: true, text: 'NDVI指数趋势（30天）' } }
      }
    });
    
    createLineChart('soil-moisture-chart', {
      labels: dates,
      datasets: [{ label: '土壤湿度', data: soilData, borderColor: '#3b82f6', backgroundColor: 'rgba(59, 130, 246, 0.1)', borderWidth: 2, tension: 0.4, fill: true }],
      options: {
        scales: { y: { min: 0, max: 100, title: { display: true, text: '湿度(%)' } } },
        plugins: { title: { display: true, text: '土壤湿度趋势（30天）' } }
      }
    });
    
    createCombinedChart('weather-chart', {
      labels: dates,
      datasets: [
        { type: 'line', label: '温度(°C)', data: tempData, borderColor: '#ef4444', backgroundColor: 'transparent', borderWidth: 2, tension: 0.4, yAxisID: 'y' },
        { type: 'bar', label: '降雨量(mm)', data: rainData, backgroundColor: '#60a5fa', yAxisID: 'y1' }
      ],
      options: {
        scales: {
          y: { type: 'linear', position: 'left', title: { display: true, text: '温度(°C)' } },
          y1: { type: 'linear', position: 'right', grid: { drawOnChartArea: false }, title: { display: true, text: '降雨量(mm)' } }
        },
        plugins: { title: { display: true, text: '气象数据综合分析（30天）' } }
      }
    });
    
    createLineChart('irrigation-demand-chart', {
      labels: dates,
      datasets: [{ label: '预测灌溉量', data: irrigationDemandData, borderColor: '#8b5cf6', backgroundColor: 'rgba(139, 92, 246, 0.1)', borderWidth: 2, tension: 0.4, fill: true }],
      options: {
        scales: { y: { min: 0, title: { display: true, text: '灌溉量(m³)' } } },
        plugins: { title: { display: true, text: '灌溉需求预测（30天）' } }
      }
    });
  } catch (error) {
    console.error('创建示例图表失败:', error);
  }
};

// -------------- 文件处理 --------------
const handleFileSelection = (file: File | null, fileType: 'ndviFile' | 'soilFile' | 'weatherFile') => { if (!file) return;
  switch (fileType) {
    case 'ndviFile':
      ndviFile.value = file;
      break;
    case 'soilFile':
      soilFile.value = file;
      break;
    case 'weatherFile':
      weatherFile.value = file;
      break;
  }
  return false;
};

const handleFileRemove = (fileType: 'ndviFile' | 'soilFile' | 'weatherFile') => {
  switch (fileType) {
    case 'ndviFile':
      ndviFile.value = null;
      break;
    case 'soilFile':
      soilFile.value = null;
      break;
    case 'weatherFile':
      weatherFile.value = null;
      break;
  }
};

const uploadFiles = async () => {
  if (!ndviFile.value || !soilFile.value || !weatherFile.value) {
    ElMessage.warning('请选择所有必填文件');
    return;
  }

  uploading.value = true;
  try {
    // 调用真实API，直接传递三个文件参数
    const response = await irrigationApi.uploadIrrigationData(
      ndviFile.value,
      soilFile.value,
      weatherFile.value,
      (progressEvent: any) => {
        // 可选：处理上传进度
        console.log('上传进度:', progressEvent);
      }
    );
    
    // 处理API返回的数据
    if (response) {
      // 使用API返回的数据
      irrigationAmount.value = parseFloat(response.irrigationAmount?.toFixed(2) || '0');
      showResult.value = true;
      updateIrrigationStatus(irrigationAmount.value);

      latestData.value = {
        soilMoisture: parseFloat(response.soilMoisture?.toFixed(1) || '0'),
        ndvi: parseFloat(response.ndvi?.toFixed(2) || '0'),
        temperature: parseFloat(response.temperature?.toFixed(1) || '0'),
        rainfall: parseFloat(response.rainfall?.toFixed(1) || '0')
      };

      await initCharts();
      ElMessage.success('数据分析完成');
    }
  } catch (error) {
    console.error('上传失败:', error);
    ElMessage.error('文件上传失败，请重试');
  } finally {
    uploading.value = false;
  }
};

// -------------- 核心修复3：图表生成与类型处理 --------------
const initCharts = async () => {
  loadingCharts.value = true;
  showVisualization.value = true;
  await nextTick();

  try {
    // 从API获取趋势数据
    const trendData = await irrigationApi.getTrend();
    
    // 初始化图表数据，如果API返回数据则使用，否则使用模拟数据
    let dates: string[] = [];
    let ndviData: number[] = [];
    let soilData: number[] = [];
    let tempData: number[] = [];
    let rainData: number[] = [];
    let irrigationDemandData: number[] = [];
    
    try {
      if (trendData) {
        // 尝试多种可能的数据结构，增强兼容性
        if (Array.isArray(trendData)) {
          // 如果返回的是数组格式
          dates = trendData.map(item => item.date || generateDates(30)[0]);
          ndviData = trendData.map(item => item.ndvi || 0);
          soilData = trendData.map(item => item.soilMoisture || 0);
          tempData = trendData.map(item => item.temperature || 0);
          rainData = trendData.map(item => item.rainfall || 0);
          irrigationDemandData = trendData.map(item => item.irrigationDemand || 0);
        } else if (trendData.data && typeof trendData.data === 'object') {
          // 如果返回的是带有data字段的对象
          dates = trendData.labels || Object.keys(trendData.data);
          ndviData = Array.isArray(trendData.data.ndvi) ? trendData.data.ndvi : [];
          soilData = Array.isArray(trendData.data.soilMoisture) ? trendData.data.soilMoisture : [];
          tempData = Array.isArray(trendData.data.temperature) ? trendData.data.temperature : [];
          rainData = Array.isArray(trendData.data.rainfall) ? trendData.data.rainfall : [];
          irrigationDemandData = Array.isArray(trendData.data.irrigationDemand) ? trendData.data.irrigationDemand : [];
        } else {
          // 处理其他可能的对象格式
          dates = Object.keys(trendData).slice(0, 30);
          // 从对象中提取各种数据
          ndviData = dates.map(key => trendData[key]?.ndvi || 0);
          soilData = dates.map(key => trendData[key]?.soilMoisture || 0);
          tempData = dates.map(key => trendData[key]?.temperature || 0);
          rainData = dates.map(key => trendData[key]?.rainfall || 0);
          irrigationDemandData = dates.map(key => trendData[key]?.irrigationDemand || 0);
        }
        
        // 确保数据长度足够
        const maxLength = Math.max(ndviData.length, soilData.length, tempData.length, rainData.length, irrigationDemandData.length);
        if (maxLength > 0 && dates.length < maxLength) {
          dates = generateDates(maxLength);
        }
        
        // 确保所有数据数组长度一致
        while (ndviData.length < dates.length) ndviData.push(0);
        while (soilData.length < dates.length) soilData.push(0);
        while (tempData.length < dates.length) tempData.push(0);
        while (rainData.length < dates.length) rainData.push(0);
        while (irrigationDemandData.length < dates.length) irrigationDemandData.push(0);
        
        // 限制数据长度为30天
        dates = dates.slice(0, 30);
        ndviData = ndviData.slice(0, 30);
        soilData = soilData.slice(0, 30);
        tempData = tempData.slice(0, 30);
        rainData = rainData.slice(0, 30);
        irrigationDemandData = irrigationDemandData.slice(0, 30);
      }
    } catch (error) {
      console.error('处理趋势数据时出错:', error);
    }
    
    // 如果API返回的数据不足，使用模拟数据填充或作为回退
    if (!dates.length || !ndviData.length || ndviData.every(val => val === 0)) {
      dates = generateDates(30);
      ndviData = generateRandomData(30, 0.3, 0.8);
      soilData = generateRandomData(30, 30, 70);
      tempData = generateRandomData(30, 15, 30);
      rainData = generateRandomData(30, 0, 30, true);
      irrigationDemandData = ndviData.map((ndvi, index) => {
        return Math.max(0, parseFloat((100 - ndvi * 150 - soilData[index] * 0.5 + tempData[index] - rainData[index] * 2).toFixed(2)));
      });
    }

    // 1. NDVI指数趋势图（折线图）
    createLineChart('ndvi-chart', {
      labels: dates,
      datasets: [{
        type: 'line',
        label: 'NDVI指数',
        data: ndviData,
        borderColor: '#42b983',
        backgroundColor: 'rgba(66, 185, 131, 0.1)',
        borderWidth: 2,
        tension: 0.4,
        fill: true
      }],
      options: {
        scales: { 
          y: { 
            min: 0, 
            max: 1, 
            title: { display: true, text: 'NDVI值' } 
          } 
        },
        plugins: { 
          title: { display: true, text: 'NDVI指数趋势（30天）' },
          tooltip: {
            callbacks: {
              // 核心修复：正确处理tooltip的parsed数据（作为对象访问）
              label: function(context) {
                const tooltipItem = context as TooltipItem<'line'>;
                // 明确访问parsed.y属性（对象类型）
                return `NDVI指数: ${tooltipItem.parsed.y.toFixed(2)}`;
              }
            }
          }
        }
      }
    });

    // 2. 土壤湿度趋势图（折线图）
    createLineChart('soil-moisture-chart', {
      labels: dates,
      datasets: [{
        type: 'line',
        label: '土壤湿度',
        data: soilData,
        borderColor: '#3b82f6',
        backgroundColor: 'rgba(59, 130, 246, 0.1)',
        borderWidth: 2,
        tension: 0.4,
        fill: true
      }],
      options: {
        scales: { 
          y: { 
            min: 0, 
            max: 100, 
            title: { display: true, text: '湿度(%)' } 
          } 
        },
        plugins: { 
          title: { display: true, text: '土壤湿度趋势（30天）' },
          tooltip: {
            callbacks: {
              label: function(context) {
                const tooltipItem = context as TooltipItem<'line'>;
                return `土壤湿度: ${tooltipItem.parsed.y.toFixed(1)}%`;
              }
            }
          }
        }
      }
    });

    // 3. 气象数据综合图（组合图表）
    createCombinedChart('weather-chart', {
      labels: dates,
      datasets: [
        {
          type: 'line',
          label: '温度(°C)',
          data: tempData,
          borderColor: '#ef4444',
          backgroundColor: 'transparent',
          borderWidth: 2,
          tension: 0.4,
          yAxisID: 'y'
        },
        {
          type: 'bar',
          label: '降雨量(mm)',
          data: rainData,
          backgroundColor: '#60a5fa',
          yAxisID: 'y1'
        }
      ],
      options: {
        scales: {
          y: { 
            type: 'linear', 
            position: 'left', 
            title: { display: true, text: '温度(°C)' } 
          },
          y1: { 
            type: 'linear', 
            position: 'right', 
            grid: { drawOnChartArea: false }, 
            title: { display: true, text: '降雨量(mm)' } 
          }
        },
        plugins: { 
          title: { display: true, text: '气象数据综合分析（30天）' },
          tooltip: {
            callbacks: {
              label: function(context) {
                // 核心修复：根据数据集类型处理不同的parsed数据
                const tooltipItem = context as TooltipItem<'line' | 'bar'>;
                if (tooltipItem.datasetIndex === 0) {
                  // 折线图数据 - 温度
                  const parsed = tooltipItem.parsed as CartesianParsedData;
                  return `温度: ${parsed.y.toFixed(1)}°C`;
                } else {
                  // 柱状图数据 - 降雨量
                  const parsed = tooltipItem.parsed as BarParsedData;
                  return `降雨量: ${parsed.y.toFixed(1)}mm`;
                }
              }
            }
          }
        }
      }
    });

    // 4. 灌溉需求预测图（折线图）
    createLineChart('irrigation-demand-chart', {
      labels: dates,
      datasets: [{
        type: 'line',
        label: '预测灌溉量',
        data: irrigationDemandData,
        borderColor: '#8b5cf6',
        backgroundColor: 'rgba(139, 92, 246, 0.1)',
        borderWidth: 2,
        tension: 0.4,
        fill: true
      }],
      options: {
        scales: { 
          y: { 
            min: 0, 
            title: { display: true, text: '灌溉量(m³)' } 
          } 
        },
        plugins: { 
          title: { display: true, text: '灌溉需求预测（30天）' },
          tooltip: {
            callbacks: {
              label: function(context) {
                const tooltipItem = context as TooltipItem<'line'>;
                return `灌溉量: ${tooltipItem.parsed.y.toFixed(2)}m³`;
              }
            }
          }
        }
      }
    });
  } catch (error) {
    console.error('图表初始化失败:', error);
    ElMessage.error('图表生成失败，请重试');
  } finally {
    loadingCharts.value = false;
  }
};

// -------------- 核心修复4：折线图创建函数（精确类型） --------------
const createLineChart = (elementId: string, config: LineChartConfig) => {
  const canvas = document.getElementById(elementId) as HTMLCanvasElement;
  if (!canvas) {
    console.error(`图表容器 "${elementId}" 不存在`);
    ElMessage.error(`图表容器 "${elementId}" 不存在`);
    return;
  }
  const ctx = canvas.getContext('2d');
  if (!ctx) {
    console.error(`无法获取图表上下文 "${elementId}"`);
    ElMessage.error(`无法获取图表上下文 "${elementId}"`);
    return;
  }

  // 清理旧图表
  if (lineCharts.value.has(elementId)) {
    lineCharts.value.get(elementId)?.destroy();
    removeResizeHandler(elementId);
  }

  try {
    // 创建折线图（明确指定为line类型）
    const chart = new Chart<'line'>(ctx, {
      type: 'line',
      data: {
        labels: config.labels,
        datasets: config.datasets
      },
      options: config.options
    });

    lineCharts.value.set(elementId, chart);
    addResizeHandler(elementId, () => chart.resize());
  } catch (error) {
    console.error(`创建图表 "${elementId}" 失败:`, error);
    ElMessage.error(`创建图表 "${elementId}" 失败`);
  }
};

// -------------- 核心修复5：组合图表创建函数（精确类型处理） --------------
const createCombinedChart = (elementId: string, config: CombinedChartConfig) => {
  const canvas = document.getElementById(elementId) as HTMLCanvasElement;
  if (!canvas) {
    console.error(`图表容器 "${elementId}" 不存在`);
    ElMessage.error(`图表容器 "${elementId}" 不存在`);
    return;
  }
  const ctx = canvas.getContext('2d');
  if (!ctx) {
    console.error(`无法获取图表上下文 "${elementId}"`);
    ElMessage.error(`无法获取图表上下文 "${elementId}"`);
    return;
  }

  // 清理旧图表
  if (barCharts.value.has(elementId)) {
    barCharts.value.get(elementId)?.destroy();
    removeResizeHandler(elementId);
  }

  try {
    // 创建组合图表（基础类型为bar，但包含line数据集）
    const chart = new Chart<'bar'>(ctx, {
      type: 'bar',
      data: {
        labels: config.labels,
        // 类型断言：明确告诉TS这些数据集兼容bar图表
        datasets: config.datasets as ChartConfiguration<'bar'>['data']['datasets']
      },
      options: config.options
    });

    barCharts.value.set(elementId, chart);
    addResizeHandler(elementId, () => chart.resize());
  } catch (error) {
    console.error(`创建图表 "${elementId}" 失败:`, error);
    ElMessage.error(`创建图表 "${elementId}" 失败`);
  }
};

// -------------- 辅助函数：处理图表大小调整 --------------
const addResizeHandler = (elementId: string, handler: () => void) => {
  removeResizeHandler(elementId);
  resizeHandlers.value.set(elementId, handler);
  window.addEventListener('resize', handler);
};

const removeResizeHandler = (elementId: string) => {
  const handler = resizeHandlers.value.get(elementId);
  if (handler) {
    window.removeEventListener('resize', handler);
    resizeHandlers.value.delete(elementId);
  }
};

// -------------- 工具函数 --------------
const generateDates = (days: number): string[] => {
  const dates: string[] = [];
  const today = new Date();
  for (let i = days - 1; i >= 0; i--) {
    const date = new Date(today);
    date.setDate(date.getDate() - i);
    dates.push(`${date.getMonth() + 1}/${date.getDate()}`);
  }
  return dates;
};

const generateRandomData = (length: number, min: number, max: number, isInteger = false): number[] => {
  return Array.from({ length }, () => {
    const value = min + Math.random() * (max - min);
    return isInteger ? Math.round(value) : parseFloat(value.toFixed(2));
  });
};

const updateIrrigationStatus = (amount: number) => {
  if (amount > 50) {
    irrigationStatus.value = { type: 'danger', message: '严重缺水，急需灌溉' };
  } else if (amount > 20) {
    irrigationStatus.value = { type: 'warning', message: '土壤较干，建议灌溉' };
  } else if (amount > 5) {
    irrigationStatus.value = { type: 'info', message: '土壤湿度适宜' };
  } else {
    irrigationStatus.value = { type: 'success', message: '土壤湿润，无需灌溉' };
  }
};
</script>

<style scoped>
/* 全局容器 */
.smart-irrigation-container {
  padding: 20px;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

/* 页面头部 */
.page-header {
  text-align: center;
  margin-bottom: 30px;
  padding: 30px 0;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.page-header h1 {
  font-size: 32px;
  color: #333;
  margin-bottom: 10px;
  font-weight: 600;
}

.subtitle {
  color: #666;
  font-size: 16px;
}

/* 通用卡片样式 */
.card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 15px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  overflow: hidden;
  margin-bottom: 30px;
}

.card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  align-items: center;
  padding: 20px 25px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
}

.card-header i {
  font-size: 18px;
  color: #42b983;
  margin-right: 10px;
}

.card-header h3 {
  font-size: 18px;
  color: #333;
  font-weight: 600;
  margin: 0;
}

.card-body {
  padding: 25px;
}

/* 高亮卡片（结果区域） */
.highlight {
  border-left: 4px solid #42b983;
  background: linear-gradient(135deg, #ffffff 0%, #f8fff8 100%);
}

/* 上传区域 */
.file-upload-form {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
  align-items: end;
  z-index: 1;
}

.file-input-group {
  display: flex;
  flex-direction: column;
}

.file-input-group label {
  margin-bottom: 10px;
  font-weight: 600;
  color: #555;
  font-size: 14px;
}

.file-selected {
  background-color: #e6f7ff !important;
  border-color: #91d5ff !important;
  color: #1890ff !important;
}

/* 确保上传按钮始终可点击 */
.upload-btn {
  position: relative;
  z-index: 10;
  background: linear-gradient(135deg, #42b983 0%, #359469 100%);
  border: none;
  color: white;
  height: 40px;
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.3s ease;
  cursor: pointer;
}

.upload-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #359469 0%, #287d55 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(66, 185, 131, 0.3);
}

/* 结果显示区域 */
.result-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.irrigation-amount {
  display: flex;
  align-items: baseline;
  gap: 15px;
  padding: 20px;
  background: rgba(66, 185, 131, 0.1);
  border-radius: 8px;
  justify-content: center;
  text-align: center;
}

.irrigation-amount .label {
  font-size: 18px;
  color: #555;
}

.irrigation-amount .value {
  font-size: 42px;
  font-weight: 700;
  color: #42b983;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.irrigation-status {
  display: flex;
  justify-content: center;
}

.irrigation-details {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 15px;
  background: #fafafa;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.detail-item:hover {
  background: #f0f0f0;
  transform: translateY(-2px);
}

.detail-item .label {
  font-size: 14px;
  color: #666;
  margin-bottom: 5px;
}

.detail-item .value {
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.detail-item .unit {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

/* 可视化图表区域 */
.chart-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 25px;
}

.chart-item {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  padding: 15px;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 15px;
  text-align: center;
}

.chart-container {
  width: 100%;
  height: 300px;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .smart-irrigation-container {
    padding: 10px;
  }
  
  .file-upload-form {
    grid-template-columns: 1fr;
  }
  
  .chart-grid {
    grid-template-columns: 1fr;
  }
  
  .chart-container {
    height: 250px;
  }
  
  .irrigation-amount .value {
    font-size: 32px;
  }
}
</style>
    