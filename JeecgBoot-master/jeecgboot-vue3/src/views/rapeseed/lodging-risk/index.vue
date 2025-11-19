<template>
  <div class="lodging-risk-page">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-overlay">
      <div class="loading-content">
        <a-spin size="large" />
        <div class="loading-text">正在加载倒伏风险数据...</div>
        <div v-if="errorCount > 0" class="retry-info">
          正在重试 ({{ errorCount }}/{{ maxRetries }})...
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content" v-else>
      <!-- 顶部标题区域 -->
      <div class="page-header">
        <h2 class="page-title">油菜倒伏风险监测</h2>
        <div class="header-actions">
          <a-button type="primary" @click="refreshData">
            <template #icon><ReloadOutlined /></template>
            刷新数据
          </a-button>
        </div>
      </div>

      <!-- 主要布局区域 -->
      <div class="content-layout">
        <!-- 左侧数据卡片区域 -->
        <div class="left-panel">
          <!-- 气象监测卡片 -->
          <div class="weather-card data-card">
            <div class="card-header">
              <h3><CloudOutlined /> 气象监测</h3>
            </div>
            <div class="card-content">
              <div class="weather-grid">
                <div class="weather-item" v-for="item in weatherData" :key="item.key">
                  <div class="item-icon">
                    <FireOutlined v-if="item.icon === 'FireOutlined'" />
                    <DashboardOutlined v-else-if="item.icon === 'DashboardOutlined'" />
                    <EyeOutlined v-else-if="item.icon === 'EyeOutlined'" />
                    <CloudOutlined v-else-if="item.icon === 'CloudOutlined'" />
                    <BulbOutlined v-else-if="item.icon === 'BulbOutlined'" />
                    <ThunderboltOutlined v-else-if="item.icon === 'ThunderboltOutlined'" />
                    <DashboardOutlined v-else />
                  </div>
                  <div class="item-info">
                    <div class="item-label">{{ item.label }}</div>
                    <div class="item-value">{{ item.value }}{{ item.unit }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 中心地图区域 -->
        <div class="center-panel">
          <div class="map-container">
            <div class="map-header">
              <h3><EnvironmentOutlined /> 地块风险分布</h3>
              <div class="map-legend">
                <div class="legend-item" v-for="item in riskLegend" :key="item.level">
                  <div class="legend-color" :style="{ backgroundColor: item.color }"></div>
                  <div class="legend-text">{{ item.level }}</div>
                </div>
              </div>
            </div>
            <div v-if="loading" class="loading-container">
            <div class="loading-spinner"></div>
            <div class="loading-text">正在加载地图数据...</div>
          </div>
          <div v-else-if="dataLoaded" class="map-content">
            <TiandituMap 
              :enableManagement="false" 
              :mapHeight="'100%'"
              :showSensors="false"
              :plotRiskData="plotRisksData"
              :useModalForPlotClick="false"
              :enablePlotHover="true"
              :showLodgingRiskBelow="true"
              @plotClick="handlePlotClick"
            />
          </div>
          <div v-else class="loading-container">
            <div class="loading-text">准备加载地图...</div>
          </div>
          </div>
        </div>

        <!-- 右侧图表区域 -->
        <div class="right-panel">
          <!-- 倒伏风险占比饼图 -->
          <div class="chart-card">
            <div class="card-header">
              <h3><PieChartOutlined /> 倒伏风险占比</h3>
            </div>
            <div class="card-content">
              <div v-if="loading" class="loading-container">
                <div class="loading-spinner"></div>
                <div class="loading-text">加载中...</div>
              </div>
              <Pie v-else-if="dataLoaded" :chartData="pieChartData" :height="'250px'" @click="handlePieClick" />
              <div v-else class="loading-container">
                <div class="loading-text">准备加载数据...</div>
              </div>
            </div>
          </div>

          <!-- 各地块风险条形图 -->
          <div class="chart-card">
            <div class="card-header">
              <h3><BarChartOutlined /> 各地块风险对比</h3>
            </div>
            <div class="card-content">
              <div v-if="loading" class="loading-container">
                <div class="loading-spinner"></div>
                <div class="loading-text">加载中...</div>
              </div>
              <Bar v-else-if="dataLoaded" :chartData="barChartData" :height="'250px'" :option="barChartOption" @click="handleBarClick" />
              <div v-else class="loading-container">
                <div class="loading-text">准备加载数据...</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 倒伏风险弹窗结构展示区域 -->
      <transition name="slide-up">
        <div class="lodging-risk-panel" v-if="showLodgingRiskPanel && selectedPlotData && selectedPlotData.current_risk">
          <div class="panel-header">
            <h3><ExclamationCircleOutlined /> 倒伏风险评估详情</h3>
            <a-button type="text" @click="closeLodgingRiskPanel">
              <template #icon><CloseOutlined /></template>
            </a-button>
          </div>
          <div class="panel-content">
            <!-- 风险等级总览卡片 -->
            <a-card 
              :bordered="false" 
              class="risk-overview-card" 
              :style="{ 
                backgroundColor: getRiskColor(selectedPlotData.current_risk.riskScore / 100) + '15', 
                borderLeft: `5px solid ${getRiskColor(selectedPlotData.current_risk.riskScore / 100)}` 
              }" 
            >
              <div class="risk-overview-content">
                <!-- 左侧风险等级标识 -->
                <div class="risk-level-indicator">
                  <div class="risk-level-icon" :style="{ color: getRiskColor(selectedPlotData.current_risk.riskScore / 100) }">
                    <ExclamationCircleOutlined />
                  </div>
                  <div class="risk-level-text" :style="{ color: getRiskColor(selectedPlotData.current_risk.riskScore / 100) }">
                    {{ selectedPlotData.current_risk.riskLevel }}
                  </div>
                  <div class="risk-probability-range">
                    倒伏概率：{{ selectedPlotData.current_risk.lodgingProbability }}  
                  </div>
                  <div class="last-update">上次更新：{{ formatDateTime(selectedPlotData.calculationTime) }}</div>
                </div>
                
                <!-- 右侧风险因子信息 -->
                <div class="risk-factors-info">
                  <div class="factors-header">
                    <h4>风险因子</h4>
                  </div>
                  
                  <!-- 左右分栏布局 -->
                  <div class="factors-container">
                    <!-- 左侧：气象数据 -->
                    <div class="factors-column">
                      <div class="column-title">
                        <CloudOutlined />
                        气象数据
                      </div>
                      <div class="factors-grid">
                        <div class="factor-item" v-for="factor in weatherFactors" :key="factor.name">
                          <div class="factor-icon" :style="{ color: factor.color }">
                            <Icon :icon="factor.icon" />
                          </div>
                          <div class="factor-details">
                            <div class="factor-name">{{ factor.name }}</div>
                          </div>
                          <div class="factor-value">{{ factor.value }}</div>
                        </div>
                      </div>
                    </div>
                    
                    <!-- 右侧：生长监测数据 -->
                    <div class="factors-column">
                      <div class="column-title">
                        <BulbOutlined />
                        生长监测数据
                      </div>
                      <div class="growth-form-container">
                        <a-form 
                          :model="growthForm" 
                          layout="vertical" 
                          class="growth-form"
                        >
                          <a-form-item label="生长阶段" name="growthStage">
                            <a-select 
                              v-model:value="growthForm.growthStage" 
                              placeholder="请选择生长阶段"
                            >
                              <a-select-option value="苗期">苗期</a-select-option>
                              <a-select-option value="蕾薹期">蕾薹期</a-select-option>
                              <a-select-option value="开花期">开花期</a-select-option>
                              <a-select-option value="角果成熟期">角果成熟期</a-select-option>
                            </a-select>
                          </a-form-item>
                          
                          <a-form-item label="植株高度(cm)" name="plantHeight">
                            <a-input-number 
                              v-model:value="growthForm.plantHeight" 
                              placeholder="请输入植株高度"
                              :min="0"
                              style="width: 100%"
                            />
                          </a-form-item>
                          
                          <a-form-item label="茎直径(mm)" name="stemDiameter">
                            <a-input-number 
                              v-model:value="growthForm.stemDiameter" 
                              placeholder="请输入茎直径"
                              :min="0"
                              style="width: 100%"
                            />
                          </a-form-item>
                          
                          <a-form-item label="种植密度(株/m²)" name="density">
                            <a-input-number 
                              v-model:value="growthForm.density" 
                              placeholder="请输入种植密度"
                              :min="0"
                              style="width: 100%"
                            />
                          </a-form-item>
                          
                          <a-form-item label="监测日期" name="monitorDate">
                            <a-date-picker 
                              v-model:value="growthForm.monitorDate" 
                              placeholder="请选择监测日期"
                              style="width: 100%"
                            />
                          </a-form-item>
                          
                          <a-form-item>
                            <a-button type="primary" @click="submitGrowthForm" style="width: 100%">
                              录入数据
                            </a-button>
                          </a-form-item>
                        </a-form>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </a-card>

            <!-- 核心预警区域 -->
            <a-card :bordered="false" class="risk-forecast-card">
              <template #title>
                <div class="card-title">
                  <ExclamationCircleOutlined />
                  倒伏风险预警（未来7天）
                </div>
              </template>
              
              <!-- 风险预警卡片容器 -->
              <div class="risk-forecast-container">
                <div 
                  v-for="(day, index) in selectedPlotData.forecast_7days?.dailyRisks" 
                  :key="index" 
                  class="risk-day-card" 
                  :style="{ 
                    backgroundColor: getRiskColor(day.riskScore / 100) + '15', 
                    borderLeft: `3px solid ${getRiskColor(day.riskScore / 100)}` 
                  }" 
                >
                  <div class="day-header">
                    <div class="day-date">{{ day.date }}</div>
                    <div class="day-weekday">{{ getWeekday(day.date) }}</div>
                  </div>
                  <div class="risk-level-badge" :style="{ backgroundColor: getRiskColor(day.riskScore / 100), color: 'white' }">
                    {{ getRiskLevel(day.riskScore / 100) }}
                  </div>
                  <div class="risk-details">
                    <div class="risk-probability">
                      <span class="probability-value" :style="{ color: getRiskColor(day.riskScore / 100) }">
                        {{ Math.round(day.riskScore * 100) }}%
                      </span>
                      <span class="probability-label">概率</span>
                    </div>
                    <div class="risk-factors">
                      <span class="factor-item">
                        <span :style="{ color: getRiskColor(day.riskScore / 100) }">风速</span>
                        {{ day.weather.windSpeed }}m/s
                      </span>
                      <span class="factor-item">
                        <span :style="{ color: getRiskColor(day.riskScore / 100) }">降雨量</span>
                        {{ day.weather.rainfall }}mm
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </a-card>

            <!-- 防控建议区 -->
            <a-card :bordered="false" class="suggestions-card">
              <template #title>
                <div class="card-title">
                  <BulbOutlined />
                  防控建议
                </div>
              </template>
              
              <a-collapse v-model:activeKey="activeKey" :bordered="false">
                <a-collapse-panel 
                  v-for="(suggestion, index) in suggestionsData" 
                  :key="String(index + 1)" 
                  :header="suggestion.title" 
                  class="suggestions-panel" 
                >
                  <template #extra>
                    <a-tag :color="index === 0 ? 'red' : 'orange'">
                      {{ index === 0 ? '立即实施' : '中长期策略' }}
                    </a-tag>
                  </template>
                  <div class="suggestions-content">
                    <div v-for="(item, itemIndex) in suggestion.items" :key="itemIndex" class="suggestion-item">
                      <div class="suggestion-icon" :class="index === 0 ? 'danger' : 'warning'">
                        <ExclamationCircleOutlined v-if="index === 0" />
                        <InfoCircleOutlined v-else />
                      </div>
                      <div class="suggestion-text">
                        <div class="suggestion-title">{{ item.title }}</div>
                        <div class="suggestion-desc">{{ item.desc }}</div>
                      </div>
                    </div>
                  </div>
                </a-collapse-panel>
              </a-collapse>
            </a-card>
          </div>
        </div>
      </transition>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, nextTick, watch, onBeforeUnmount,computed } from 'vue';
import { useSelectStore } from '/@/store/selectStore';
import { useMessage } from '/@/hooks/web/useMessage';
import dayjs from 'dayjs';
import { 
  getLodgingRiskDataById, 
  getBatchLodgingRiskDataByBaseId,
  type LodgingRiskAssessmentResponse,
  type BatchLodgingRiskAssessmentResponse
} from './lodgingRisk.api';
import { 
  getLatestGrowthMonitoringByPlotId,
  addGrowthMonitoring,
  type GrowthMonitoringData
} from '/@/api/rapeseed/growthMonitoring';
import { getWeatherSensorData } from '/@/api/rapeseed/weatherSensor';
import type { 
  LodgingRiskData, 
  WeatherSensorData, 
  PlotInfo
} from '/@/api/rapeseed/lodgingRisk';
import TiandituMap from '/@/components/TiandituMap/index.vue';
import Pie from '/@/components/chart/Pie.vue';
import Bar from '/@/components/chart/Bar.vue';
import { Icon } from '/@/components/Icon';
import { debounce, throttle } from 'lodash-es';
import {
  ReloadOutlined,
  EnvironmentOutlined,
  PieChartOutlined,
  BarChartOutlined,
  InfoCircleOutlined,
  CloseOutlined,
  ThunderboltOutlined,
  EyeOutlined,
  DashboardOutlined,
  FireOutlined,
  CloudOutlined,
  BulbOutlined,
  ExclamationCircleOutlined
} from '@ant-design/icons-vue';

// ==================== 状态管理 ====================
const selectStore = useSelectStore();
const { createMessage } = useMessage();

// 加载状态
const loading = ref(false);
const dataLoaded = ref(false);
const errorCount = ref(0);
const maxRetries = 3;

// 地图相关
const mapInstance = ref(null);
const plotLayers = ref([]);
const fieldOverlayRef = ref(null);

// ==================== 图表相关变量 ====================
// 饼图数据
const pieChartData = ref([]);
// 条形图数据
const barChartData = ref([]);
// 条形图配置
const barChartOption = ref({
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    top: '15%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    name: '地块',
    nameTextStyle: {
      color: '#333'
    },
    axisLabel: {
      color: '#333',
      interval: 0,
      rotate: 45,
      width: 80,
      overflow: 'truncate',
      ellipsis: '...'
    },
    axisLine: {
      lineStyle: {
        color: '#333'
      }
    }
  },
  yAxis: {
    type: 'value',
    name: '风险评分(%)',
    nameTextStyle: {
      color: '#333'
    },
    axisLabel: {
      color: '#333',
      formatter: '{value}%'
    },
    axisLine: {
      lineStyle: {
        color: '#333'
      }
    },
    splitLine: {
      lineStyle: {
        color: 'rgba(0, 0, 0, 0.1)'
      }
    }
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    },
    formatter: function(params) {
      const data = params[0];
      // 确保显示地块名称
      const plotName = data.name || '未知地块';
      return `${plotName}<br/>风险评分: ${data.value}%`;
    }
  },
  series: [
    {
      name: '风险评分',
      type: 'bar',
      data: [],
      itemStyle: {
        color: (params) => {
          // 根据风险评分设置不同颜色
          const value = params.value;
          if (value >= 0 && value < 30) {
            return "#52c41a"; // 低风险 - 绿色
          } else if (value >= 30 && value < 50) {
            return "#afcb2b"; // 中低风险 - 黄绿色
          } else if (value >= 50 && value < 65) {
            return "#faad14"; // 中等风险 - 橙色
          } else if (value >= 65 && value < 80) {
            return "#ff4d4f"; // 高风险 - 红色
          } else if (value >= 80 && value <= 100) {
            return "#d32f2f"; // 极高风险 - 深红色
          }
          return "#1890ff"; // 默认蓝色
        }
      }
    }
  ]
});

// 地块详情
const showPlotDetails = ref(false);
const plotDetailsContent = ref('');

// 倒伏风险弹窗相关
const selectedPlotData = ref(null);
const showLodgingRiskPanel = ref(false);

// 数据状态
const lodgingRiskData = ref(null);
const plotRisksData = ref([]); // 地块风险数据，用于传递给地图组件
const baseStatistics = ref({
  totalPlots: 0,
  highRiskPlots: 0,
  mediumRiskPlots: 0,
  lowRiskPlots: 0,
  averageRiskScore: 0,
  lastUpdated: new Date().toISOString()
});

// ==================== 静态数据 ====================
// 气象监测数据 - 初始值，会被API数据更新
const weatherData = reactive([
  { key: 'temperature', label: '空气温度', value: 23.5, unit: '°C', icon: 'FireOutlined' },
  { key: 'humidity', label: '空气湿度', value: 65, unit: '%', icon: 'DashboardOutlined' },
  { key: 'pressure', label: '大气压强', value: 1013, unit: 'hPa', icon: 'EyeOutlined' },
  { key: 'windSpeed', label: '风速', value: 12, unit: 'm/s', icon: 'CloudOutlined' },
  { key: 'windDirection', label: '风向', value: '东南', unit: '', icon: 'CloudOutlined' },
  { key: 'lightIntensity', label: '光照强度', value: 8500, unit: 'lux', icon: 'BulbOutlined' },
  { key: 'rainfall', label: '雨量累计', value: 15.2, unit: 'mm', icon: 'ThunderboltOutlined' },
  { key: 'co2', label: 'CO₂浓度', value: 410, unit: 'ppm', icon: 'DashboardOutlined' },
  { key: 'radiation', label: '总辐射', value: 18.5, unit: 'MJ/m²', icon: 'BulbOutlined' }
]);

// 风险等级图例
const riskLegend = reactive([
  { level: '低风险', color: '#52c41a' },
  { level: '中低风险', color: '#afcb2b' },
  { level: '中等风险', color: '#faad14' },
  { level: '高风险', color: '#ff4d4f' },
  { level: '极高风险', color: '#d32f2f' }
]);


// ==================== 工具函数 ====================
// 获取图标组件
function getIconComponent(iconName: string) {
  const iconMap = {
    'FireOutlined': FireOutlined,
    'DashboardOutlined': DashboardOutlined,
    'EyeOutlined': EyeOutlined,
    'CloudOutlined': CloudOutlined,
    'BulbOutlined': BulbOutlined,
    'ThunderboltOutlined': ThunderboltOutlined
  };
  
  return iconMap[iconName] || DashboardOutlined; // 默认返回DashboardOutlined
}


// 防控建议数据
const suggestionsData = computed(() => {
  // 使用下划线命名的字段
  const suggestions = selectedPlotData.value?.comprehensive_suggestions;
  
  if (!suggestions) return [];
  
  // 如果是API返回的格式（数组格式），需要转换为前端期望的格式
  if (Array.isArray(suggestions)) {
    // 将API返回的数组格式转换为前端期望的对象格式
    const immediate = suggestions.filter(s => s.type === 'immediate');
    const shortTerm = suggestions.filter(s => s.type === 'shortTerm');
    const mediumTerm = suggestions.filter(s => s.type === 'mediumTerm');
    const longTerm = suggestions.filter(s => s.type === 'longTerm');
    
    return [
      {
        title: '立即措施（0-24小时）',
        icon: 'ControlOutlined',
        color: '#1890ff',
        items: immediate.map((item: any) => ({
          title: item.title,
          desc: item.description
        }))
      },
      {
        title: '短期措施（1-3天）',
        icon: 'ControlOutlined',
        color: '#1890ff',
        items: shortTerm.map((item: any) => ({
          title: item.title,
          desc: item.description
        }))
      },
      {
        title: '中期措施（4-7天）',
        icon: 'ControlOutlined',
        color: '#1890ff',
        items: mediumTerm.map((item: any) => ({
          title: item.title,
          desc: item.description
        }))
      },
      {
        title: '长期优化（下季改进）',
        icon: 'ControlOutlined',
        color: '#1890ff',
        items: longTerm.map((item: any) => ({
          title: item.title,
          desc: item.description
        }))
      }
    ];
  }
  
  // 如果已经是前端期望的格式（对象格式）
  return [
    {
      title: '立即措施（0-24小时）',
      icon: 'ControlOutlined',
      color: '#1890ff',
      items: suggestions.immediate?.map((item: string) => ({
        title: '',
        desc: item
      })) || []
    },
    {
      title: '短期措施（1-3天）',
      icon: 'ControlOutlined',
      color: '#1890ff',
      items: suggestions.shortTerm?.map((item: string) => ({
        title: '',
        desc: item
      })) || []
    },
    {
      title: '中期措施（4-7天）',
      icon: 'ControlOutlined',
      color: '#1890ff',
      items: suggestions.mediumTerm?.map((item: string) => ({
        title: '',
        desc: item
      })) || []
    },
    {
      title: '长期优化（下季改进）',
      icon: 'ControlOutlined',
      color: '#1890ff',
      items: suggestions.longTerm?.map((item: string) => ({
        title: '',
        desc: item
      })) || []
    }
  ];
});

// 气象数据计算属性
const weatherFactors = computed(() => {
  if (!selectedPlotData.value?.current_risk?.originalFactors) return [];
  
  const factors = selectedPlotData.value.current_risk.originalFactors;
  const normalizedFactors = selectedPlotData.value.current_risk.normalizedFactors || {};
  
  // 只提取气象相关因子
  const weatherFactorKeys = ['windSpeed3d', 'rainfall7d'];
  
  // 因子图标映射
  const factorIcons: Record<string, string> = {
    'windSpeed3d': 'ant-design:thunderbolt-outlined',
    'rainfall7d': 'ant-design:cloud-rain-outlined'
  };
  
  // 因子名称映射
  const factorNames: Record<string, string> = {
    'windSpeed3d': '近3天最大风速',
    'rainfall7d': '近7天累积降雨'
  };
  
  return weatherFactorKeys.map(key => {
    const value = factors[key];
    const normalizedValue = normalizedFactors[key] || 0;
    let displayValue = '';
    
    // 根据因子类型设置显示值
    switch(key) {
      case 'windSpeed3d':
        displayValue = `${value}m/s`;
        break;
      case 'rainfall7d':
        displayValue = `${value}mm`;
        break;
      default:
        displayValue = value.toString();
    }
    
    return {
      name: factorNames[key] || key,
      value: displayValue,
      icon: factorIcons[key] || 'ant-design:info-circle-outlined',
      percentage: Math.round(normalizedValue * 100),
      color: getFactorColor(normalizedValue)
    };
  });
});

// 生长监测表单数据
const growthForm = reactive({
  growthStage: '',
  plantHeight: null,
  stemDiameter: null,
  density: null,
  monitorDate: null
});

// 风险因子数据
const riskFactors = computed(() => {
  if (!selectedPlotData.value?.current_risk?.originalFactors) return [];
  
  const factors = selectedPlotData.value.current_risk.originalFactors;
  const normalizedFactors = selectedPlotData.value.current_risk.normalizedFactors || {};
  
  // 因子图标映射
  const factorIcons: Record<string, string> = {
    'plantHeight': 'ant-design:rise-outlined',
    'stemDiameter': 'ant-design:column-width-outlined',
    'slendernessRatio': 'ant-design:column-height-outlined',
    'windSpeed3d': 'ant-design:thunderbolt-outlined',
    'rainfall7d': 'ant-design:cloud-rain-outlined',
    'growthStage': 'ant-design:field-time-outlined',
    'density': 'ant-design:border-outlined',
    'soilType': 'ant-design:environment-outlined'
  };
  
  // 因子名称映射
  const factorNames: Record<string, string> = {
    'plantHeight': '株高',
    'stemDiameter': '茎粗',
    'slendernessRatio': '细长比',
    'windSpeed3d': '近3天最大风速',
    'rainfall7d': '近7天累积降雨',
    'growthStage': '生育期',
    'density': '种植密度',
    'soilType': '土壤类型'
  };
  
  return Object.keys(factors).map(key => {
    const value = factors[key];
    const normalizedValue = normalizedFactors[key] || 0;
    let displayValue = '';
    
    // 根据因子类型设置显示值
    switch(key) {
      case 'plantHeight':
        displayValue = `${value}cm`;
        break;
      case 'stemDiameter':
        displayValue = `${value}mm`;
        break;
      case 'slendernessRatio':
        displayValue = value.toString();
        break;
      case 'windSpeed3d':
        displayValue = `${value}m/s`;
        break;
      case 'rainfall7d':
        displayValue = `${value}mm`;
        break;
      case 'growthStage':
        displayValue = value; // 直接显示原始值，如"花期"
        break;
      case 'density':
        displayValue = `${value}株/m²`;
        break;
      case 'soilType':
        displayValue = value; // 直接显示原始值，如"砂壤土"
        break;
      default:
        displayValue = value.toString();
    }
    
    return {
      name: factorNames[key] || key,
      value: displayValue,
      icon: factorIcons[key] || 'ant-design:info-circle-outlined',
      percentage: Math.round(normalizedValue * 100),
      color: getFactorColor(normalizedValue)
    };
  });
});

// 提交生长监测表单
async function submitGrowthForm() {
  // 验证表单
  if (!growthForm.growthStage) {
    createMessage.warning('请选择生长阶段');
    return;
  }
  
  if (!growthForm.plantHeight || growthForm.plantHeight <= 0) {
    createMessage.warning('请输入有效的植株高度');
    return;
  }
  
  if (!growthForm.stemDiameter || growthForm.stemDiameter <= 0) {
    createMessage.warning('请输入有效的茎直径');
    return;
  }
  
  if (!growthForm.density || growthForm.density <= 0) {
    createMessage.warning('请输入有效的种植密度');
    return;
  }
  
  if (!growthForm.monitorDate) {
    createMessage.warning('请选择监测日期');
    return;
  }
  
  // 构建提交数据
  const formData: GrowthMonitoringData = {
    plotId: selectedPlotData.value?.plotId,
    growthStage: growthForm.growthStage,
    plantHeight: growthForm.plantHeight,
    stemDiameter: growthForm.stemDiameter,
    density: growthForm.density,
    monitoringDate: growthForm.monitorDate.format('YYYY-MM-DD')
  };
  
  try {
    // 调用API提交数据
    await addGrowthMonitoring(formData);
    
    // 显示成功消息
    createMessage.success('生长监测数据已成功提交');
    
    // 重置表单
    Object.assign(growthForm, {
      growthStage: '',
      plantHeight: null,
      stemDiameter: null,
      density: null,
      monitorDate: null
    });
    
    // 刷新当前地块的倒伏风险数据
    if (selectedPlotData.value?.plotId) {
      await loadPlotRiskData(selectedPlotData.value.plotId);
    }
  } catch (error) {
    console.error('提交生长监测数据失败:', error);
    createMessage.error('提交失败，请重试');
  }
}

// 根据风险评分获取颜色
function getRiskColor(score: number): string {
  if (score >= 0.0 && score < 0.30) {
    return "#52c41a"; // 低风险 - 绿色
  } else if (score >= 0.30 && score < 0.50) {
    return "#afcb2b"; // 中低风险 - 黄绿色
  } else if (score >= 0.50 && score < 0.65) {
    return "#faad14"; // 中等风险 - 橙色
  } else if (score >= 0.65 && score < 0.80) {
    return "#ff4d4f"; // 高风险 - 红色
  } else if (score >= 0.80 && score <= 1.0) {
    return "#d32f2f"; // 极高风险 - 深红色
  }
  
  return "#faad14"; // 默认橙色
}

// 根据风险评分获取风险等级
function getRiskLevel(score: number): string {
  if (score >= 0.0 && score < 0.30) {
    return "低风险";
  } else if (score >= 0.30 && score < 0.50) {
    return "中低风险";
  } else if (score >= 0.50 && score < 0.65) {
    return "中等风险";
  } else if (score >= 0.65 && score < 0.80) {
    return "高风险";
  } else if (score >= 0.80 && score <= 1.0) {
    return "极高风险";
  }
  
  return "中等风险"; // 默认
}

// 格式化时间显示
function formatDateTime(dateTimeStr: string): string {
  if (!dateTimeStr) return '暂无数据';
  
  try {
    const date = new Date(dateTimeStr);
    // 检查日期是否有效
    if (isNaN(date.getTime())) {
      return '时间格式错误';
    }
    
    // 格式化为 YYYY-MM-DD HH:mm:ss
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');
    
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
  } catch (error) {
    console.error('时间格式化错误:', error);
    return '时间格式错误';
  }
}

// ==================== 地图相关函数 ====================
// 处理地块点击事件
const handlePlotClick = async (plotId) => {
  // 支持两种ID格式：plotId和id
  const id = plotId;
  const plot = plotRisksData.value.find(p => p.plotId === id || p.id === id);
  
  // 如果找不到匹配的地块，使用传入的ID创建基本数据结构
  
  // 检查current_risk是否存在，如果不存在则添加默认数据
  if (!plot.current_risk) {
    console.warn('地块数据缺少current_risk属性:', plot);
    // 添加默认的current_risk数据（使用下划线命名）
    plot.current_risk = {
      riskLevel: '未知',
      riskScore: 0,
      lodgingProbability: '未知',
      lastUpdated: new Date().toISOString(),
      riskFactors: {}
    };
    
    // 添加默认的forecast_7_days数据（使用下划线命名）
    plot.forecast_7_days = {
      dailyRisks: []
    };
    
    // 添加默认的comprehensive_suggestions数据（使用下划线命名）
    plot.comprehensive_suggestions = [
      {
        type: 'immediate',
        title: '暂无立即措施',
        description: '数据加载中，请稍后再试'
      },
      {
        type: 'shortTerm',
        title: '暂无短期措施',
        description: '数据加载中，请稍后再试'
      },
      {
        type: 'mediumTerm',
        title: '暂无中期措施',
        description: '数据加载中，请稍后再试'
      },
      {
        type: 'longTerm',
        title: '暂无长期措施',
        description: '数据加载中，请稍后再试'
      }
    ];
  }
  
  // 存储选中的地块数据
  selectedPlotData.value = plot;
  // 显示倒伏风险面板
  showLodgingRiskPanel.value = true;
  // 关闭地块详情面板
  showPlotDetails.value = false;
  
  // 准备趋势图数据
  // prepareTrendChartData(); // 已移除趋势图功能
  
  // 获取最新的生长监测数据
  try {
    const growthData = await getLatestGrowthMonitoringByPlotId(plotId);
    if (growthData) {
      // 将生长监测数据添加到地块数据中
      selectedPlotData.value.growthData = growthData;
      console.log('获取到生长监测数据:', growthData);
      
      // 将获取到的数据填充到表单中
      Object.assign(growthForm, {
        growthStage: growthData.growthStage || '',
        plantHeight: growthData.plantHeight || null,
        stemDiameter: growthData.stemDiameter || null,
        density: growthData.density || null,
        monitorDate: growthData.monitoringDate ? dayjs(growthData.monitoringDate) : null
      });
    }
  } catch (error) {
    console.error('获取生长监测数据失败:', error);
    createMessage.error('获取生长监测数据失败');
  }
  
  console.log('点击地块，显示倒伏风险面板:', plot);
};

// 根据风险评分获取建议措施
function getRecommendation(score: number): string {
  if (score < 0.30) {
    return "保持常规管理，定期监测";
  } else if (score < 0.50) {
    return "增加监测频率，注意天气变化";
  } else if (score < 0.65) {
    return "采取预防措施，加固植株";
  } else if (score < 0.80) {
    return "立即采取防护措施，减少损失";
  } else {
    return "紧急处理，考虑收割或转移";
  }
}

// 获取风险分析描述
function getRiskAnalysis(score: number): string {
  if (score < 0.30) {
    return "当前倒伏风险较低，作物生长状况良好。建议保持常规管理，定期监测作物生长状态和天气变化。";
  } else if (score < 0.50) {
    return "存在一定倒伏风险，主要受近期天气影响。建议增加监测频率，特别关注大风暴雨等极端天气预警。";
  } else if (score < 0.65) {
    return "倒伏风险较高，作物可能受到多方面因素影响。建议采取预防性措施，如适当培土、加固植株等。";
  } else if (score < 0.80) {
    return "倒伏风险高，需要立即采取防护措施。建议检查植株健康状况，考虑减少灌溉，避免加重倒伏风险。";
  } else {
    return "倒伏风险极高，作物面临严重威胁。建议紧急处理，如提前收割或采取其他减少损失的措施。";
  }
}

// 关闭地块详情
function closePlotDetails() {
  showPlotDetails.value = false;
  plotDetailsContent.value = '';
}

// 关闭倒伏风险面板
function closeLodgingRiskPanel() {
  showLodgingRiskPanel.value = false;
  selectedPlotData.value = null;
}

// 获取风险因子名称
function getFactorName(key: string): string {
  const factorNames = {
    'plantHeight': '株高',
    'stemDiameter': '茎粗',
    'slendernessRatio': '细长比',
    'windSpeed3d': '近3天最大风速',
    'rainfall7d': '近7天累积降雨',
    'growthStage': '生育期',
    'density': '种植密度',
    'soilType': '土壤类型'
  };
  return factorNames[key] || key;
}

// 获取风险因子颜色
function getFactorColor(status: string | number): string {
  // 如果是数值类型，将其转换为风险等级
  if (typeof status === 'number') {
    if (status >= 0.0 && status < 0.30) {
      return '#52c41a'; // 低风险 - 绿色
    } else if (status >= 0.30 && status < 0.65) {
      return '#faad14'; // 中等风险 - 橙色
    } else {
      return '#ff4d4f'; // 高风险 - 红色
    }
  }
  
  // 如果是字符串类型，按原来的逻辑处理
  if (status === 'high' || status === 'danger') {
    return '#ff4d4f'; // 高风险 - 红色
  } else if (status === 'medium' || status === 'warning') {
    return '#faad14'; // 中等风险 - 橙色
  } else {
    return '#52c41a'; // 低风险 - 绿色
  }
}

// 获取星期几
function getWeekday(dateStr: string): string {
  const date = new Date(dateStr);
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
  return weekdays[date.getDay()];
}

// 获取建议数据
function getSuggestionsByType() {
  return [
    {
      title: '紧急措施',
      items: [
        { title: '立即排水', desc: '降低土壤湿度，减轻根部负担' },
        { title: '加固支撑', desc: '使用支架或绳索固定易倒伏区域' },
        { title: '暂停施肥', desc: '避免过度生长导致植株脆弱' }
      ]
    },
    {
      title: '中长期策略',
      items: [
        { title: '品种选择', desc: '选择抗倒伏能力强的品种' },
        { title: '合理密植', desc: '控制种植密度，增强通风透光' },
        { title: '科学施肥', desc: '平衡氮磷钾比例，避免氮肥过量' }
      ]
    }
  ];
}

// 视图模式 - 固定为卡片视图，不显示趋势图
const viewMode = ref('card');

// 折叠面板激活的key
const activeKey = ref(['1']);

// ==================== 图表相关函数 ====================
// 处理饼图点击事件
const handlePieClick = (params) => {
  console.log('饼图点击:', params);
};

// 处理条形图点击事件
const handleBarClick = (params) => {
  console.log('条形图点击:', params);
  const plotName = params.name;
  const plot = plotRisksData.value.find(p => p.plotName === plotName);
  if (plot) {
    onPlotClick(plot.plotId);
  }
};

// 准备饼图数据
const preparePieChartData = () => {
  // 检查baseStatistics是否存在
  if (!baseStatistics.value) {
    console.warn('baseStatistics数据未准备好，跳过饼图数据准备');
    return;
  }
  
  // 使用baseStatistics中的数据生成饼图
  pieChartData.value = [
    { 
      value: baseStatistics.value.lowRiskPlots, 
      name: '低风险',
      itemStyle: { color: '#52c41a' }
    },
    { 
      value: baseStatistics.value.mediumRiskPlots, 
      name: '中风险',
      itemStyle: { color: '#faad14' }
    },
    { 
      value: baseStatistics.value.highRiskPlots, 
      name: '高风险',
      itemStyle: { color: '#ff4d4f' }
    },
    {
      value: baseStatistics.value.extremeRiskPlots, 
      name: '极高风险',
      itemStyle: { color: '#d32f2f' }
    }
  ];
  
  console.log('饼图数据已准备:', pieChartData.value);
};

// 准备条形图数据
const prepareBarChartData = () => {
  // 使用plotRisksData中的数据
  const plots = plotRisksData.value || [];
  
  // 按风险分数排序的地块，过滤掉没有current_risk数据的地块
  const sortedPlots = [...plots]
    .filter(plot => plot.current_risk && plot.current_risk.riskScore !== undefined)
    .sort((a, b) => b.current_risk.riskScore - a.current_risk.riskScore);
  
  barChartData.value = sortedPlots.map(plot => ({
    name: plot.plotName,
    value: Math.round(plot.current_risk.riskScore * 100), // 转换为百分比形式
    itemStyle: {
      color: getRiskColor(plot.current_risk.riskScore) // 使用原始值（0-1范围）
    }
  }));
};

// 准备趋势图数据
// ==================== 数据加载 ====================
// 加载气象传感器数据
const loadWeatherSensorData = async () => {
  try {
    // 首先检查是否有选中的基地
    if (!selectStore.selectedBase || !selectStore.selectedBase.baseId) {
      console.warn('没有选中的基地，无法加载气象传感器数据');
      createMessage.warning('请先选择一个基地');
      return;
    }
    
    // 有选中的基地，调用API获取气象传感器数据
    const baseId = selectStore.selectedBase.baseId;
    console.log('加载基地ID:', baseId, '的气象传感器数据');
    
    // 调用API获取数据
    const weatherData = await getWeatherSensorData(baseId);
    console.log('气象传感器数据加载成功:', weatherData);
    
    if (weatherData) {
      // 更新气象数据
      updateWeatherData(weatherData);
      createMessage.success('气象传感器数据加载成功');
    } else {
      throw new Error('API返回的气象传感器数据为空');
    }
  } catch (error) {
    console.error('加载气象传感器数据失败:', error);
    createMessage.warning('气象传感器数据加载失败');
    // 不再使用模拟数据作为后备
    throw error;
  }
};

// 更新气象数据
const updateWeatherData = (sensorData) => {
  // 根据API返回的数据更新weatherData数组
  if (sensorData.temperature !== undefined) {
    const tempItem = weatherData.find(item => item.key === 'temperature');
    if (tempItem) tempItem.value = sensorData.temperature;
  }
  
  if (sensorData.humidity !== undefined) {
    const humidityItem = weatherData.find(item => item.key === 'humidity');
    if (humidityItem) humidityItem.value = sensorData.humidity;
  }
  
  if (sensorData.airPressure !== undefined) {
    const pressureItem = weatherData.find(item => item.key === 'pressure');
    if (pressureItem) pressureItem.value = sensorData.airPressure;
  }
  
  if (sensorData.windSpeed !== undefined) {
    const windSpeedItem = weatherData.find(item => item.key === 'windSpeed');
    if (windSpeedItem) windSpeedItem.value = sensorData.windSpeed;
  }
  
  if (sensorData.windDirection !== undefined) {
    const windDirItem = weatherData.find(item => item.key === 'windDirection');
    if (windDirItem) {
      // 将风向角度转换为文字描述
      const direction = convertWindDirection(sensorData.windDirection);
      windDirItem.value = direction;
    }
  }
  
  if (sensorData.lightIntensity !== undefined) {
    const lightItem = weatherData.find(item => item.key === 'lightIntensity');
    if (lightItem) lightItem.value = sensorData.lightIntensity;
  }
  
  if (sensorData.rainfall !== undefined) {
    const rainfallItem = weatherData.find(item => item.key === 'rainfall');
    if (rainfallItem) rainfallItem.value = sensorData.rainfall;
  }
  
  if (sensorData.co2Level !== undefined) {
    const co2Item = weatherData.find(item => item.key === 'co2');
    if (co2Item) co2Item.value = sensorData.co2Level;
  }
  
  if (sensorData.totalRadiation !== undefined) {
    const radiationItem = weatherData.find(item => item.key === 'radiation');
    if (radiationItem) radiationItem.value = sensorData.totalRadiation;
  }
  
  if (sensorData.updateTime !== undefined) {
    // 可以在这里添加更新时间的显示
    console.log('气象数据更新时间:', sensorData.updateTime);
  }
};

// 将风向角度转换为文字描述
const convertWindDirection = (degree) => {
  const directions = ['北', '东北', '东', '东南', '南', '西南', '西', '西北'];
  const index = Math.round(degree / 45) % 8;
  return directions[index];
};

// 加载倒伏风险数据
const loadLodgingRiskData = async () => {
  try {
    loading.value = true;
    dataLoaded.value = false;
    console.log('开始加载倒伏风险数据...');
    
    // 首先加载气象传感器数据
    await loadWeatherSensorData();
    
    // 检查是否有选中的基地
    if (!selectStore.selectedBase || !selectStore.selectedBase.baseId) {
      console.warn('没有选中的基地，无法加载数据');
      createMessage.warning('请先选择一个基地');
      return;
    }
    
    // 调用API获取基地下所有地块的倒伏风险数据
    const baseId = selectStore.selectedBase.baseId;
    console.log('加载基地ID:', baseId, '的倒伏风险数据');
    
    const batchData = await getBatchLodgingRiskDataByBaseId(baseId);
    console.log('倒伏风险数据加载成功:', batchData);
    
    if (batchData && batchData.plotRisks) {
      console.log('plotRisks数据:', batchData.plotRisks);
      // 更新plotRisksData，用于传递给地图组件
      plotRisksData.value = batchData.plotRisks;
      
      // 更新baseStatistics数据
      baseStatistics.value = {
        totalPlots: batchData.baseStatistics.totalPlots,
        highRiskPlots: batchData.baseStatistics.highRiskPlots + batchData.baseStatistics.extremeRiskPlots,
        mediumRiskPlots: batchData.baseStatistics.mediumRiskPlots + batchData.baseStatistics.mediumLowRiskPlots,
        lowRiskPlots: batchData.baseStatistics.lowRiskPlots,
        averageRiskScore: batchData.baseStatistics.averageRiskScore,
        lastUpdated: batchData.calculationTime || new Date().toISOString()
      };
      
      dataLoaded.value = true;
      
      // 准备图表数据
      preparePieChartData();
      prepareBarChartData();
      
      errorCount.value = 0; // 重置错误计数
      createMessage.success(`成功加载基地下${batchData.plotRisks.length}个地块的倒伏风险数据`);
    } else {
      throw new Error('API返回的数据格式不正确');
    }
  } catch (error) {
    console.error('加载倒伏风险数据失败:', error);
    errorCount.value++;
    
    if (errorCount.value < maxRetries) {
      console.log(`第${errorCount.value}次重试加载数据`);
      createMessage.warning(`数据加载失败，正在重试 (${errorCount.value}/${maxRetries})...`);
      setTimeout(() => {
        loadLodgingRiskData();
      }, 2000 * errorCount.value); // 递增延迟
    } else {
      createMessage.error('数据加载失败，请稍后重试');
      // 不再使用模拟数据，直接显示错误状态
      dataLoaded.value = false;
    }
  } finally {
    setTimeout(() => {
      loading.value = false;
    }, 500);
  }
};

// 刷新数据 - 防抖处理
const refreshData = debounce(async () => {
  try {
    loading.value = true;
    dataLoaded.value = false;
    
    // 重新加载数据
    await loadLodgingRiskData();
    
  } catch (error) {
    console.error('刷新数据失败:', error);
    createMessage.error('数据刷新失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}, 300);

// 节流处理的窗口大小变化监听
const handleResize = throttle(() => {
  // 使用Jeecg组件后，不需要手动调整图表大小
  // 组件内部会自动处理响应式调整
}, 200);
  
  // ==================== 监听器 ====================
// 监听数据变化
watch(
  () => lodgingRiskData.value,
  () => {
    // 数据变化时准备图表数据
    preparePieChartData();
    prepareBarChartData();
  },
  { deep: true }
);

// 监听基地选择变化
watch(
  () => selectStore.selectedBase?.baseId,
  (newBaseId, oldBaseId) => {
    console.log('监听到基地变化:', { newBaseId, oldBaseId });
    
    // 检查基地是否有变化
    const baseChanged = newBaseId !== oldBaseId;
    
    if (baseChanged) {
      console.log('基地发生变化，重新加载数据');
      loadLodgingRiskData();
    }
  },
  { immediate: true }
);

// 监听选中的地块数据变化
watch(selectedPlotData, (newVal) => {
  console.log('选中的地块数据变化:', newVal);
}, { deep: true });

// ==================== 生命周期钩子 ====================
onMounted(() => {
  console.log('组件挂载，初始化页面');
  
  // 初始化baseStatistics数据
  baseStatistics.value = {
    totalPlots: 0,
    highRiskPlots: 0,
    mediumRiskPlots: 0,
    lowRiskPlots: 0,
    averageRiskScore: 0,
    lastUpdated: new Date().toISOString()
  };
  
  // 设置初始加载状态
  loading.value = true;
  
  // 首先检查是否有选中的基地
  if (selectStore.selectedBase?.baseId) {
    console.log('已有选中的基地，加载数据');
    loadLodgingRiskData();
  } else {
    console.log('没有选中的基地，请先选择基地');
    createMessage.warning('请先选择一个基地以查看倒伏风险数据');
  }
  
  // 添加窗口大小变化监听
  window.addEventListener('resize', handleResize);
});

// 清理事件监听
onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
});
</script>

<style lang="less" scoped>
.lodging-risk-page {
  padding: 16px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 64px);
  
  // 加载状态样式
  .loading-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(255, 255, 255, 0.9);
    z-index: 9999;
    display: flex;
    justify-content: center;
    align-items: center;
    
    .loading-content {
      text-align: center;
      
      .loading-text {
        margin-top: 16px;
        font-size: 16px;
        color: #666;
      }
      
      .retry-info {
        margin-top: 8px;
        font-size: 14px;
        color: #ff4d4f;
        animation: pulse 1.5s infinite;
      }
    }
  }

  // 页面头部
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding: 0 8px;
    
    .page-title {
      margin: 0;
      font-size: 24px;
      font-weight: 600;
      color: #262626;
    }
  }

  // 主要布局
  .content-layout {
    display: flex;
    gap: 16px;
    margin-bottom: 16px;
    height: 700px; // 减少整体高度，从800px调整为700px
    
    // 左侧面板
    .left-panel {
      width: 280px; // 减小宽度，使气象卡片变窄
      flex-shrink: 0;
      display: flex;
      flex-direction: column;
      overflow-y: visible; // 移除面板级别的滚动
      
      // 气象监测卡片占据整个左侧面板
      .data-card {
        flex: 1; // 让卡片占据全部可用空间
        min-height: 280px; // 设置最小高度，确保内容可见
        overflow: hidden; // 移除内部滚动，让内容自适应
        
        .card-content {
          padding: 16px;
          height: calc(100% - 60px); // 减去header高度
          overflow-y: auto; // 只在内容区域添加滚动
          overflow-x: hidden; // 隐藏水平滚动条
          
          // 自定义滚动条样式
          &::-webkit-scrollbar {
            width: 4px; // 减小滚动条宽度
          }
          
          &::-webkit-scrollbar-track {
            background: #f1f1f1;
            border-radius: 2px;
          }
          
          &::-webkit-scrollbar-thumb {
            background: #c1c1c1;
            border-radius: 2px;
            
            &:hover {
              background: #a8a8a8;
            }
          }
        }
      }
    }
    
    // 中心面板
    .center-panel {
      flex: 1;
      min-width: 0;
    }
    
    // 右侧面板
    .right-panel {
      width: 360px; // 增加宽度，使图表卡片变宽
      flex-shrink: 0;
      display: flex;
      flex-direction: column;
      gap: 16px;
      
      // 确保两个图表卡片高度一致
      .chart-card {
        flex: 1;
        height: 0; // 强制flex子元素计算高度
        
        .card-content {
          height: calc(100% - 48px); // 减去header高度
          padding: 16px;
          
          .pie-chart, .bar-chart {
            width: 100%;
            height: 100%;
          }
        }
      }
    }
  }

  // 数据卡片样式
  .data-card {
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    overflow: hidden;
    transition: all 0.3s ease;
    animation: fadeInUp 0.6s ease-out;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
    }
    
    .card-header {
      padding: 16px;
      border-bottom: 1px solid #f0f0f0;
      
      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 500;
        color: #262626;
        display: flex;
        align-items: center;
        gap: 8px;
      }
    }
    
    .card-content {
      padding: 16px;
      // 确保内容区域可以完整显示
      overflow-y: auto;
      max-height: calc(100% - 60px); // 减去header高度
    }
  }

  // 气象监测卡片
  .weather-card {
    .weather-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr); // 改为2列布局，使卡片更紧凑
      gap: 12px;
      
      .weather-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: 10px 6px; // 减小内边距，节省空间
        border-radius: 8px;
        background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
        border: 1px solid #e9ecef;
        transition: all 0.3s ease;
        position: relative;
        overflow: hidden;
        min-height: 75px; // 减小最小高度
        
        // 科技感边框效果
        &::before {
          content: '';
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          height: 3px;
          background: linear-gradient(90deg, #1890ff, #36cfc9);
          opacity: 0;
          transition: opacity 0.3s;
        }
        
        &:hover {
          background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
          border-color: #91d5ff;
          transform: translateY(-2px); // 减小悬停位移
          box-shadow: 0 4px 12px rgba(24, 144, 255, 0.15);
          
          &::before {
            opacity: 1;
          }
          
          .item-icon {
            transform: scale(1.05); // 减小缩放比例
          }
          
          .item-value {
            color: #1890ff;
            font-weight: 600;
          }
        }
        
        .item-icon {
          font-size: 18px; // 减小图标大小
          color: #1890ff;
          margin-bottom: 6px; // 减小间距
          transition: all 0.3s ease;
          background: rgba(24, 144, 255, 0.1);
          width: 36px; // 减小图标容器
          height: 36px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          flex-shrink: 0; // 防止图标被压缩
          
          // 确保Ant Design图标正确显示
          .anticon {
            font-size: 18px;
            line-height: 1;
          }
        }
        
        .item-info {
          text-align: center;
          width: 100%; // 确保信息区域占满宽度
          
          .item-label {
            font-size: 11px; // 减小字体大小
            color: #8c8c8c;
            margin-bottom: 3px; // 减小间距
            transition: color 0.3s;
            white-space: nowrap; // 防止标签换行
            overflow: hidden;
            text-overflow: ellipsis;
            width: 100%;
            line-height: 1.2; // 确保行高合适
          }
          
          .item-value {
            font-size: 13px; // 减小字体大小
            font-weight: 500;
            color: #262626;
            transition: all 0.3s ease;
            white-space: nowrap; // 防止值换行
            overflow: hidden;
            text-overflow: ellipsis;
            width: 100%;
            line-height: 1.2; // 确保行高合适
          }
        }
      }
    }
  }

  // 地图容器
  .map-container {
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    overflow: hidden;
    height: 100%; // 使用100%高度以适应父容器
    display: flex;
    flex-direction: column;
    animation: fadeInUp 0.6s ease-out;
    
    .map-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px;
      border-bottom: 1px solid #f0f0f0;
      flex-shrink: 0; // 防止header被压缩
      
      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 500;
        color: #262626;
        display: flex;
        align-items: center;
        gap: 8px;
      }
      
      .map-legend {
        display: flex;
        gap: 16px;
        
        .legend-item {
          display: flex;
          align-items: center;
          gap: 6px;
          
          .legend-color {
            width: 12px;
            height: 12px;
            border-radius: 2px;
          }
          
          .legend-text {
            font-size: 12px;
            color: #595959;
          }
        }
      }
    }
    
    .map-content {
      flex: 1;
      background-color: #e6f7ff;
      position: relative;
      overflow: hidden;
      
      #tianditu-map {
        width: 100%;
        height: 100%;
      }
    }
  }

  // 图表卡片
  .chart-card {
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    overflow: hidden;
    transition: all 0.3s ease;
    animation: fadeInUp 0.6s ease-out;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
    }
    
    .card-header {
      padding: 16px;
      border-bottom: 1px solid #f0f0f0;
      
      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 500;
        color: #262626;
        display: flex;
        align-items: center;
        gap: 8px;
      }
    }
  }

  // 底部详情面板
  .bottom-panel {
    background: linear-gradient(135deg, rgba(13, 71, 161, 0.9), rgba(21, 101, 192, 0.8));
    border-radius: 8px;
    padding: 16px;
    margin-top: 16px;
    border: 1px solid rgba(255, 255, 255, 0.1);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    animation: slideUp 0.3s ease-out;
    
    .details-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      padding-bottom: 8px;
      border-bottom: 1px solid rgba(255, 255, 255, 0.1);
      
      h3 {
        color: #fff;
        margin: 0;
        font-size: 16px;
        font-weight: 500;
        display: flex;
        align-items: center;
        gap: 8px;
      }
      
      .ant-btn {
        color: #fff;
        border-color: rgba(255, 255, 255, 0.2);
        
        &:hover {
          color: #1890ff;
          border-color: #1890ff;
          background-color: rgba(24, 144, 255, 0.1);
        }
      }
    }
    
    .details-content {
      color: #fff;
      padding: 0;
      
      :deep(.plot-detail-info) {
        .detail-header {
          display: flex;
          align-items: center;
          margin-bottom: 16px;
          
          h4 {
            margin: 0;
            font-size: 18px;
            color: #fff;
          }
          
          .risk-badge {
            font-weight: 500;
            text-transform: uppercase;
            letter-spacing: 0.5px;
          }
        }
        
        .detail-grid {
          display: grid;
          grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
          gap: 16px;
          margin-bottom: 16px;
          
          .detail-item {
            background: rgba(255, 255, 255, 0.05);
            border-radius: 6px;
            padding: 12px;
            border-left: 3px solid rgba(255, 255, 255, 0.2);
            
            .detail-label {
              display: block;
              font-size: 12px;
              opacity: 0.7;
              margin-bottom: 4px;
            }
            
            .detail-value {
              font-size: 14px;
              font-weight: 500;
            }
          }
        }
        
        .detail-description {
          background: rgba(255, 255, 255, 0.05);
          border-radius: 6px;
          padding: 12px;
          
          h5 {
            margin: 0 0 8px 0;
            font-size: 14px;
            font-weight: 500;
            color: #fff;
          }
          
          p {
            margin: 0;
            font-size: 13px;
            line-height: 1.5;
            opacity: 0.9;
          }
        }
        
        h4 {
          margin-top: 0;
          margin-bottom: 16px;
          font-size: 18px;
          color: #fff;
        }
        
        p {
          margin-bottom: 8px;
          line-height: 1.5;
        }
      }
    }
  }

  // 倒伏风险面板样式
  .lodging-risk-panel {
    position: fixed;
    bottom: 0;
    left: 50%;
    transform: translateX(-50%);
    width: calc(100% - 32px); // 与主页面相同的内边距
    max-width: 1400px; // 与主页面相同的最大宽度
    background: #fff;
    box-shadow: 0 -4px 12px rgba(0, 0, 0, 0.1);
    z-index: 1000;
    max-height: 80vh;
    overflow-y: auto;
    border-radius: 8px 8px 0 0;
    
    .panel-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px 24px;
      border-bottom: 1px solid #f0f0f0;
      background: linear-gradient(135deg, rgba(13, 71, 161, 0.9), rgba(21, 101, 192, 0.8));
      
      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 500;
        color: #fff;
        display: flex;
        align-items: center;
        gap: 8px;
      }
      
      .ant-btn {
        color: #fff;
        border-color: rgba(255, 255, 255, 0.2);
        
        &:hover {
          color: #1890ff;
          border-color: #1890ff;
          background-color: rgba(24, 144, 255, 0.1);
        }
      }
    }
    
    .panel-content {
      padding: 16px 24px;
      
      // 风险等级总览卡片样式
      .risk-overview-card {
        margin-bottom: 16px;
        border-radius: 8px;
        overflow: hidden;
        transition: all 0.3s ease;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
        
        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
        }
        
        .risk-overview-content {
          display: flex;
          padding: 0;
          
          .risk-level-indicator {
            flex: 0 0 220px;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            padding: 24px 16px;
            background-color: rgba(255, 255, 255, 0.4);
            border-right: 1px solid rgba(0, 0, 0, 0.06);
            
            .risk-level-icon {
              font-size: 36px;
              margin-bottom: 12px;
            }
            
            .risk-level-text {
              font-size: 20px;
              font-weight: 600;
              margin-bottom: 8px;
            }
            
            .risk-probability-range {
              font-size: 14px;
              margin-bottom: 8px;
              opacity: 0.85;
            }
            
            .last-update {
              font-size: 12px;
              opacity: 0.6;
            }
          }
          
          .risk-factors-info {
            flex: 1;
            padding: 20px 24px;
            
            .factors-header {
              margin-bottom: 16px;
              
              h4 {
                margin: 0;
                font-size: 16px;
                font-weight: 600;
                color: #262626;
              }
            }
            
            // 左右分栏布局容器
            .factors-container {
              display: flex;
              gap: 24px;
              
              // 单列样式
              .factors-column {
                // 调整左右列比例，左侧气象数据稍窄
                &:first-child {
                  flex: 0 0 42%;
                }
                
                &:last-child {
                  flex: 0 0 58%;
                }
                
                // 列标题样式
                .column-title {
                  display: flex;
                  align-items: center;
                  gap: 8px;
                  margin-bottom: 16px;
                  font-size: 15px;
                  font-weight: 600;
                  color: #262626;
                  padding-bottom: 10px;
                  border-bottom: 2px solid #f0f0f0;
                  position: relative;
                  
                  // 添加下划线动画效果
                  &::after {
                    content: '';
                    position: absolute;
                    bottom: -2px;
                    left: 0;
                    width: 60px;
                    height: 2px;
                    background: linear-gradient(90deg, #1890ff, #40a9ff);
                    border-radius: 1px;
                  }
                  
                  // 图标样式
                  .anticon {
                    color: #1890ff;
                    font-size: 16px;
                  }
                }
                
                // 气象数据网格样式
                .factors-grid {
                  display: grid;
                  grid-template-columns: 1fr;
                  gap: 16px;
                  
                  .factor-item {
                    display: flex;
                    align-items: center;
                    gap: 16px;
                    padding: 16px;
                    border-radius: 10px;
                    background-color: #fff;
                    border: 1px solid #e8e8e8;
                    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
                    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.02);
                    
                    &:hover {
                      background-color: #fff;
                      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
                      transform: translateY(-2px);
                      border-color: #d6f4ff;
                    }
                    
                    .factor-icon {
                      font-size: 20px;
                      width: 48px;
                      height: 48px;
                      display: flex;
                      align-items: center;
                      justify-content: center;
                      border-radius: 12px;
                      background-color: #f0f8ff;
                      flex-shrink: 0;
                      transition: all 0.3s ease;
                    }
                    
                    .factor-details {
                      flex: 1;
                      min-width: 0;
                      
                      .factor-name {
                        font-size: 14px;
                        color: #595959;
                        margin-bottom: 4px;
                        font-weight: 500;
                      }
                    }
                    
                    .factor-value {
                      font-size: 18px;
                      font-weight: 600;
                      color: #262626;
                      white-space: nowrap;
                    }
                  }
                }
                
                // 生长监测表单样式
                .growth-form-container {
                  background-color: #fff;
                  border-radius: 10px;
                  padding: 20px;
                  border: 1px solid #e8e8e8;
                  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.02);
                  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
                  
                  &:hover {
                    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
                  }
                  
                  .growth-form {
                    .ant-form-item {
                      margin-bottom: 18px;
                      
                      .ant-form-item-label {
                        padding-bottom: 6px;
                        
                        label {
                          font-size: 14px;
                          font-weight: 500;
                          color: #262626;
                        }
                      }
                      
                      .ant-select, .ant-input-number, .ant-picker {
                        width: 100%;
                        border-radius: 6px;
                        transition: all 0.3s ease;
                        
                        &:hover, &:focus {
                          border-color: #40a9ff;
                          box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
                        }
                      }
                      
                      .ant-input-number {
                        width: 100%;
                      }
                    }
                    
                    .ant-btn {
                      margin-top: 12px;
                      height: 40px;
                      font-weight: 500;
                      border-radius: 6px;
                      box-shadow: 0 2px 4px rgba(24, 144, 255, 0.3);
                      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
                      
                      &:hover {
                        transform: translateY(-1px);
                        box-shadow: 0 4px 8px rgba(24, 144, 255, 0.4);
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
      
      // 核心预警区域样式
      .risk-forecast-card {
        margin-bottom: 16px;
        border-radius: 8px;
        overflow: hidden;
        transition: all 0.3s ease;
        
        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
        }
        
        .card-title {
          display: flex;
          align-items: center;
          gap: 8px;
        }
        
        .risk-forecast-container {
          display: grid;
          grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
          gap: 12px;
          
          .risk-day-card {
            border-radius: 6px;
            padding: 12px;
            transition: all 0.3s ease;
            
            &:hover {
              transform: translateY(-2px);
              box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
            }
            
            .day-header {
              display: flex;
              flex-direction: column;
              align-items: center;
              margin-bottom: 8px;
              
              .day-date {
                font-weight: 500;
              }
              
              .day-weekday {
                font-size: 12px;
                opacity: 0.7;
              }
            }
            
            .risk-level-badge {
              text-align: center;
              padding: 4px 8px;
              border-radius: 4px;
              font-size: 12px;
              font-weight: 500;
              margin-bottom: 8px;
            }
            
            .risk-details {
              .risk-probability {
                display: flex;
                flex-direction: column;
                align-items: center;
                margin-bottom: 8px;
                
                .probability-value {
                  font-size: 16px;
                  font-weight: 600;
                }
                
                .probability-label {
                  font-size: 12px;
                  opacity: 0.7;
                }
              }
              
              .risk-factors {
                display: flex;
                flex-direction: column;
                gap: 4px;
                
                .factor-item {
                  font-size: 12px;
                  display: flex;
                  justify-content: space-between;
                }
              }
            }
          }
        }
        
        .risk-trend-chart {
          height: 300px;
          
          .trend-chart {
            width: 100%;
            height: 100%;
          }
        }
      }
      
      // 防控建议区样式
      .suggestions-card {
        border-radius: 8px;
        overflow: hidden;
        transition: all 0.3s ease;
        
        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
        }
        
        .card-title {
          display: flex;
          align-items: center;
          gap: 8px;
        }
        
        .suggestions-panel {
          margin-bottom: 8px;
          
          .suggestions-content {
            .suggestion-item {
              display: flex;
              gap: 12px;
              margin-bottom: 12px;
              
              .suggestion-icon {
                display: flex;
                align-items: flex-start;
                padding-top: 2px;
                
                &.danger {
                  color: #ff4d4f;
                }
                
                &.warning {
                  color: #faad14;
                }
              }
              
              .suggestion-text {
                flex: 1;
                
                .suggestion-title {
                  font-weight: 500;
                  margin-bottom: 4px;
                }
                
                .suggestion-desc {
                  font-size: 13px;
                  opacity: 0.8;
                }
              }
            }
          }
        }
      }
    }
  }

  @keyframes slideUp {
    from {
      transform: translateY(20px);
      opacity: 0;
    }
    to {
      transform: translateY(0);
      opacity: 1;
    }
  }

  @keyframes fadeInUp {
    from {
      opacity: 0;
      transform: translateY(20px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  @keyframes slideInRight {
    from {
      opacity: 0;
      transform: translateX(20px);
    }
    to {
      opacity: 1;
      transform: translateX(0);
    }
  }

  @keyframes pulse {
    0% {
      transform: scale(1);
    }
    50% {
      transform: scale(1.05);
    }
    100% {
      transform: scale(1);
    }
  }

  // 加载状态
  .loading-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 40px;
    color: #fff;
    
    .loading-spinner {
      width: 40px;
      height: 40px;
      border: 3px solid rgba(255, 255, 255, 0.2);
      border-top-color: #fff;
      border-radius: 50%;
      animation: spin 1s linear infinite;
      margin-bottom: 16px;
    }
    
    .loading-text {
      font-size: 16px;
      opacity: 0.8;
    }
  }

  @keyframes spin {
    to {
      transform: rotate(360deg);
    }
  }

  // 数据过渡效果
  .fade-enter-active, .fade-leave-active {
    transition: opacity 0.5s ease;
  }

  .fade-enter-from, .fade-leave-to {
    opacity: 0;
  }

  .slide-up-enter-active {
    transition: all 0.3s ease-out;
  }

  .slide-up-leave-active {
    transition: all 0.3s ease-in;
  }

  .slide-up-enter-from {
    opacity: 0;
    transform: translateY(20px);
  }

  .slide-up-leave-to {
    opacity: 0;
    transform: translateY(-20px);
  }

  // 响应式布局
  @media (max-width: 1400px) {
    .content-layout {
      height: 650px; // 调整整体高度，与主布局保持一致
      .left-panel {
        width: 260px; // 调整左侧面板宽度
      }
      .right-panel {
        width: 340px; // 调整右侧面板宽度
      }
      
      .weather-card .weather-grid {
        grid-template-columns: repeat(2, 1fr); // 保持2列布局
        gap: 8px; // 调整间距
        
        .weather-item {
          padding: 8px 4px; // 调整内边距
          min-height: 70px; // 调整最小高度
        }
      }
    }
    
    // 倒伏风险面板响应式样式
    .lodging-risk-panel {
      width: calc(100% - 24px); // 调整内边距
      max-width: 1200px; // 调整最大宽度
    }
  }
  
  @media (max-width: 1200px) {
    .content-layout {
      height: 600px; // 调整整体高度，与主布局保持一致
      .left-panel {
        width: 240px; // 调整左侧面板宽度
      }
      .right-panel {
        width: 320px; // 调整右侧面板宽度
      }
      
      .weather-card .weather-grid {
        grid-template-columns: repeat(2, 1fr); // 保持2列布局
      }
    }
    
    // 倒伏风险面板响应式样式
    .lodging-risk-panel {
      width: calc(100% - 24px); // 调整内边距
      max-width: 1000px; // 调整最大宽度
    }
  }
  
  @media (max-width: 992px) {
    padding: 12px;
    
    .page-header {
      .header-actions {
        flex-wrap: wrap;
        gap: 8px;
      }
    }
    
    .content-layout {
      flex-direction: column;
      gap: 16px;
      height: auto; // 在小屏幕上使用自动高度
      
      .left-panel, .right-panel {
        width: 100%;
        flex-direction: row;
        overflow-x: auto;
        gap: 16px;
        height: auto; // 改为自动高度，确保内容可见
        overflow-y: visible; // 移除垂直滚动
      }
      
      .center-panel {
        height: 450px; // 调整地图高度，与减少后的布局高度相匹配
      }
      
      .weather-card {
        min-width: 300px;
        flex: 1;
        height: auto; // 改为自动高度
        max-height: none; // 移除最大高度限制
      }
      
      .chart-card {
        min-width: 350px;
        flex: 1;
      }
    }
    
    // 倒伏风险面板响应式样式
    .lodging-risk-panel {
      width: calc(100% - 24px); // 调整内边距
      max-width: 900px; // 调整最大宽度
      
      .panel-content {
        .risk-overview-content {
          .risk-factors-info {
            // 修改为左右分栏布局
            .factors-container {
              flex-direction: column;
              gap: 16px;
              
              .factors-column {
                // 在中等屏幕上恢复等宽布局
                &:first-child, &:last-child {
                  flex: 1;
                }
                
                .column-title {
                  font-size: 14px;
                  margin-bottom: 12px;
                }
                
                .factors-grid {
                  grid-template-columns: repeat(2, 1fr); // 保持两列布局
                  gap: 15px;
                  
                  .factor-item {
                    padding: 12px;
                    gap: 12px;
                    
                    .factor-icon {
                      width: 40px;
                      height: 40px;
                      font-size: 18px;
                    }
                    
                    .factor-name {
                      font-size: 13px;
                    }
                    
                    .factor-value {
                      font-size: 16px;
                    }
                  }
                }
                
                .growth-form-container {
                  padding: 16px;
                  
                  .growth-form {
                    .ant-form-item {
                      margin-bottom: 16px;
                    }
                    
                    .ant-btn {
                      height: 36px;
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
  
  @media (max-width: 768px) {
    padding: 8px;
    
    .page-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 12px;
      
      .header-actions {
        width: 100%;
        justify-content: flex-start;
      }
    }
    
    .content-layout {
      gap: 12px;
      height: auto; // 在小屏幕上使用自动高度
      
      .left-panel, .right-panel {
        flex-direction: column;
        overflow-x: visible;
        height: auto; // 在小屏幕上使用自动高度
      }
      
      .center-panel {
        height: 400px; // 调整地图高度，与减少后的布局高度相匹配
      }
      
      .weather-card .weather-grid {
        grid-template-columns: repeat(2, 1fr); // 保持2列布局
      }

    }
    
    .bottom-panel {
      .detail-grid {
        grid-template-columns: 1fr;
      }
    }
    
    // 倒伏风险面板响应式样式
    .lodging-risk-panel {
      width: calc(100% - 16px); // 调整内边距
      max-width: 768px; // 调整最大宽度
      
      .panel-content {
        .risk-overview-content {
          .risk-factors-info {
            // 修改为左右分栏布局
            .factors-container {
              flex-direction: column;
              gap: 14px;
              
              .factors-column {
                // 在小屏幕上恢复等宽布局
                &:first-child, &:last-child {
                  flex: 1;
                }
                
                .column-title {
                  font-size: 14px;
                  margin-bottom: 12px;
                }
                
                .factors-grid {
                  grid-template-columns: 1fr; // 在小屏幕上改为单列布局
                  gap: 12px;
                  
                  .factor-item {
                    padding: 12px;
                    gap: 12px;
                    
                    .factor-icon {
                      width: 36px;
                      height: 36px;
                      font-size: 17px;
                    }
                    
                    .factor-name {
                      font-size: 13px;
                    }
                    
                    .factor-value {
                      font-size: 15px;
                    }
                  }
                }
                
                .growth-form-container {
                  padding: 14px;
                  
                  .growth-form {
                    .ant-form-item {
                      margin-bottom: 14px;
                      
                      .ant-form-item-label {
                        padding-bottom: 4px;
                        
                        label {
                          font-size: 13px;
                        }
                      }
                    }
                    
                    .ant-btn {
                      height: 36px;
                      margin-top: 8px;
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
  
  @media (max-width: 576px) {
    padding: 6px;
    
    .page-header {
      h2 {
        font-size: 20px;
      }
      
      .header-actions {
        .ant-btn {
          padding: 4px 8px;
          font-size: 12px;
        }
      }
    }
    
    .content-layout {
      .center-panel {
        height: 350px; // 调整地图高度，与减少后的布局高度相匹配
      }
      
      .weather-card .weather-grid {
        grid-template-columns: 1fr; // 在小屏幕上改为单列布局
      }
      
      .card-header h3 {
        font-size: 14px;
      }
      
      .weather-item {
        padding: 8px;
        
        .item-label {
          font-size: 11px;
        }
        
        .item-value {
          font-size: 16px;
        }
      }
    }
    
    .map-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 8px;
      
      .map-legend {
        flex-wrap: wrap;
      }
    }
    
    .bottom-panel {
      .details-header {
        h3 {
          font-size: 14px;
        }
      }
      
      .detail-grid {
        grid-template-columns: 1fr;
        gap: 12px;
      }
      
      .detail-item {
        padding: 8px;
      }
    }
    
    // 倒伏风险面板响应式样式
    .lodging-risk-panel {
      width: calc(100% - 12px); // 调整内边距
      max-width: 576px; // 调整最大宽度
      
      .panel-content {
        padding: 12px;
        
        .risk-overview-content {
          flex-direction: column;
          gap: 16px;
          
          .risk-factors-info {
            // 修改为左右分栏布局
            .factors-container {
              flex-direction: column;
              gap: 12px;
              
              .factors-column {
                // 在小屏幕上恢复等宽布局
                &:first-child, &:last-child {
                  flex: 1;
                }
                
                .column-title {
                  font-size: 13px;
                  margin-bottom: 10px;
                  
                  .anticon {
                    font-size: 14px;
                  }
                }
                
                .factors-grid {
                  grid-template-columns: 1fr; // 在小屏幕上改为单列布局
                  gap: 10px;
                  
                  .factor-item {
                    padding: 10px;
                    gap: 10px;
                    
                    .factor-icon {
                      width: 32px;
                      height: 32px;
                      font-size: 16px;
                    }
                    
                    .factor-name {
                      font-size: 12px;
                    }
                    
                    .factor-value {
                      font-size: 14px;
                    }
                  }
                }
                
                .growth-form-container {
                  padding: 12px;
                  
                  .growth-form {
                    .ant-form-item {
                      margin-bottom: 12px;
                      
                      .ant-form-item-label {
                        padding-bottom: 4px;
                        
                        label {
                          font-size: 12px;
                        }
                      }
                    }
                    
                    .ant-btn {
                      height: 32px;
                      margin-top: 6px;
                      font-size: 13px;
                    }
                  }
                }
              }
            }
          }
        }
        
        .warning-grid {
          grid-template-columns: 1fr;
        }
      }
    }
  }
}
</style>
