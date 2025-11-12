<template>
  <div class="lodging-risk-page">
    <!-- 全屏加载遮罩 -->
    <div v-if="loading" class="loading-overlay">
      <div class="loading-content">
        <div class="loading-icon">
          <a-spin size="large" />
        </div>
      </div>
    </div>
    
    <!-- 页面内容（仅在非加载状态显示） -->
    <div v-else>
      <!-- 风险等级总览卡片 -->
      <a-card 
        :bordered="false" 
        class="risk-overview-card" 
        :style="{ 
          backgroundColor: getRiskColor(currentRiskLevel) + '20', 
          borderLeft: `5px solid ${getRiskColor(currentRiskLevel)}` 
        }"
      >
        <div class="risk-overview-content">
          <!-- 左侧风险等级标识 -->
          <div class="risk-level-indicator">
            <div class="risk-level-icon" :style="{ color: getRiskColor(currentRiskLevel) }">
              <Icon :icon="getRiskIcon(currentRiskLevel)" />
            </div>
            <div class="risk-level-text" :style="{ color: getRiskColor(currentRiskLevel) }">
              {{ getRiskLevelText(currentRiskLevel) }}
            </div>
            <div class="risk-probability-range">
              倒伏概率：{{ getRiskProbabilityRange(currentRiskLevel) }}
            </div>
            <div class="last-update">上次更新：{{ lastUpdateTime }}</div>
          </div>
          
          <!-- 右侧风险因子信息 -->
          <div class="risk-factors-info">
            <div class="factor-item" v-for="factor in riskFactors" :key="factor.name">
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
      </a-card>

      <!-- 核心预警区域 -->
      <a-card :bordered="false" class="risk-forecast-card">
        <template #title>
          <div class="card-title">
            <Icon icon="ant-design:alert-outlined" />
            倒伏风险预警（未来7天）
          </div>
        </template>
        
        <template #extra>
          <a-button-group>
            <a-button :type="viewMode === 'card' ? 'primary' : 'default'" @click="viewMode = 'card'">
              <Icon icon="ant-design:appstore-outlined" />
              卡片视图
            </a-button>
            <a-button :type="viewMode === 'chart' ? 'primary' : 'default'" @click="viewMode = 'chart'">
              <Icon icon="ant-design:line-chart-outlined" />
              趋势图
            </a-button>
          </a-button-group>
        </template>
        
        <!-- 风险预警卡片容器 -->
        <div v-if="viewMode === 'card'" class="risk-forecast-container">
          <div 
            v-for="(day, index) in riskForecastData" 
            :key="index"
            class="risk-day-card"
            :style="{ 
              backgroundColor: day.riskColor + '15', 
              borderLeft: `3px solid ${day.riskColor}` 
            }"
          >
            <div class="day-header">
              <div class="day-date">{{ day.date }}</div>
              <div class="day-weekday">{{ day.weekday }}</div>
            </div>
            <div class="risk-level-badge" :style="{ backgroundColor: day.riskColor, color: 'white' }">
              {{ day.riskLevel }}
            </div>
            <div class="risk-details">
              <div class="risk-probability">
                <span class="probability-value" :style="{ color: day.riskColor }">
                  {{ day.probability }}
                </span>
                <span class="probability-label">概率</span>
              </div>
              <div class="risk-factors">
                <span class="factor-item">
                  <span :style="{ color: day.riskColor }">风速</span>
                  {{ day.windSpeed }}m/s
                </span>
                <span class="factor-item">
                  <span :style="{ color: day.riskColor }">降雨量</span>
                  {{ day.rainfall }}mm
                </span>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 趋势图视图 -->
        <div v-else-if="viewMode === 'chart'" class="risk-trend-chart">
          <div ref="trendChartRef" class="trend-chart"></div>
        </div>
      </a-card>

      <!-- 防控建议区 -->
      <a-card :bordered="false" class="suggestions-card">
        <template #title>
          <div class="card-title">
            <Icon icon="ant-design:bulb-outlined" />
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
                  <Icon :icon="index === 0 ? 'ant-design:exclamation-circle-outlined' : 'ant-design:info-circle-outlined'" />
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

      <!-- 历史记录区 -->
      <a-card :bordered="false" class="history-card">
        <template #title>
          <div class="card-title">
            <Icon icon="ant-design:history-outlined" />
            历史记录与对比
          </div>
        </template>
        
        <div v-if="historyData.length > 0" class="history-list">
          <div v-for="item in historyData" :key="item.id" class="history-item">
            <div class="history-content">
              <div class="history-info">
                <div class="history-title">{{ item.title }}</div>
                <div class="history-desc">{{ item.desc }}</div>
              </div>
              <div class="history-actions">
                <a-button 
                  v-for="(action, index) in item.actions" 
                  :key="index" 
                  :type="item.type === 'warning' ? 'primary' : 'default'" 
                  size="small"
                >
                  {{ action }}
                </a-button>
              </div>
            </div>
          </div>
        </div>
        
        <div v-else class="empty-history">
          <a-empty description="暂无历史记录数据" />
        </div>
      </a-card>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, computed, watch, nextTick } from 'vue';
import { useMessage } from '/@/hooks/web/useMessage';
import { Icon } from '/@/components/Icon';
import { useECharts } from '/@/hooks/web/useECharts';
import { useSelectStore } from '/@/store/selectStore';
import { getLodgingRiskDataById } from './lodgingRisk.api';

// ==================== 状态管理 ====================
const { createMessage } = useMessage();
const selectStore = useSelectStore();
const loading = ref(false);
const viewMode = ref<'card' | 'chart'>('card');
const trendChartRef = ref<HTMLDivElement | null>(null);
const lodgingRiskData = ref<any>(null);
const activeKey = ref(['1']);

// ==================== 计算属性 ====================

// 当前风险等级
const currentRiskLevel = computed(() => {
  if (!lodgingRiskData.value?.current_risk) return '不存在';
  return lodgingRiskData.value.current_risk.riskLevel || '中等风险';
});

// 上次更新时间
const lastUpdateTime = computed(() => {
  if (!lodgingRiskData.value?.calculationTime) return '';
  
  const date = new Date(lodgingRiskData.value.calculationTime);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  
  return `${year}-${month}-${day} ${hours}:${minutes}`;
});

// 风险因子数据
const riskFactors = computed(() => {
  if (!lodgingRiskData.value?.current_risk?.originalFactors) return [];
  
  const factors = lodgingRiskData.value.current_risk.originalFactors;
  const normalizedFactors = lodgingRiskData.value.current_risk.normalizedFactors || {};
  
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

// 防控建议数据
const suggestionsData = computed(() => {
  if (!lodgingRiskData.value?.comprehensive_suggestions) return [];
  
  const suggestions = lodgingRiskData.value.comprehensive_suggestions;
  
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

// 历史记录数据
const historyData = computed(() => {
  if (!lodgingRiskData.value?.historical_records) return [];
  
  return lodgingRiskData.value.historical_records.map((record: any) => ({
    id: record.id || Date.now(),
    date: record.date || new Date().toISOString().split('T')[0],
    title: record.title || '历史记录',
    desc: record.desc || '',
    type: record.type || 'info',
    actions: record.actions || ['查看详情']
  }));
});

// 风险预警数据
const riskForecastData = computed(() => {
  if (!lodgingRiskData.value?.forecast_7days?.dailyRisks) return [];
  
  const dailyRisks = lodgingRiskData.value.forecast_7days.dailyRisks;
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
  
  return dailyRisks.map((day: any) => {
    const date = new Date(day.date);
    const weekday = weekdays[date.getDay()];
    
    return {
      date: `${date.getMonth() + 1}月${date.getDate()}日`,
      weekday,
      riskLevel: day.riskLevel || 'low',
      riskColor: getRiskColorByScore(day.riskScore || 0),
      probability: day.lodgingProbability || '10-30%',
      windSpeed: day.weather?.windSpeed || 0,
      rainfall: day.weather?.rainfall || 0,
      riskScore: day.riskScore || 0,
      fullDate: day.date
    };
  });
});

// ==================== 工具函数 ====================

// 根据标准化值获取因子颜色
function getFactorColor(normalizedValue: number): string {
  if (normalizedValue >= 0.0 && normalizedValue < 0.30) {
    return "#4CAF50"; // 低风险 - 绿色
  } else if (normalizedValue >= 0.30 && normalizedValue < 0.50) {
    return "#FFEB3B"; // 中低风险 - 黄色
  } else if (normalizedValue >= 0.50 && normalizedValue < 0.65) {
    return "#FF9800"; // 中等风险 - 橙色
  } else if (normalizedValue >= 0.65 && normalizedValue < 0.80) {
    return "#F44336"; // 高风险 - 红色
  } else if (normalizedValue >= 0.80 && normalizedValue <= 1.0) {
    return "#D32F2F"; // 极高风险 - 深红色
  }
  
  return "#FF9800"; // 默认橙色
}

// 根据风险评分获取颜色
function getRiskColorByScore(score: number): string {
  if (score >= 0.0 && score < 0.30) {
    return "#4CAF50"; // 低风险 - 绿色
  } else if (score >= 0.30 && score < 0.50) {
    return "#FFEB3B"; // 中低风险 - 黄色
  } else if (score >= 0.50 && score < 0.65) {
    return "#FF9800"; // 中等风险 - 橙色
  } else if (score >= 0.65 && score < 0.80) {
    return "#F44336"; // 高风险 - 红色
  } else if (score >= 0.80 && score <= 1.0) {
    return "#D32F2F"; // 极高风险 - 深红色
  }
  
  return "#FF9800"; // 默认橙色
}

// 获取风险等级文本
function getRiskLevelText(level: string): string {
  if (!lodgingRiskData.value?.current_risk?.riskLevel) return '未知风险';
  return lodgingRiskData.value.current_risk.riskLevel;
}

// 获取风险等级颜色
function getRiskColor(level: string): string {
  // 直接根据中文风险等级返回对应颜色
  const colorMap = {
    '低风险': '#52c41a',        // 绿色
    '中低风险': '#afcb2b',      // 黄绿色
    '中等风险': '#faad14',      // 橙色
    '高风险': '#ff4d4f',        // 红色
    '极高风险': '#d32f2f'       // 深红色
  };
  
  // 如果有匹配的中文风险等级，直接返回对应颜色
  if (colorMap[level]) {
    return colorMap[level];
  }
  
  // 否则根据风险评分动态计算颜色
  const riskScore = lodgingRiskData.value?.current_risk?.riskScore || 0.5;
  return getRiskColorByScore(riskScore);
}

// 获取风险图标
function getRiskIcon(level: string): string {
  const iconMap = {
    '低风险': 'ant-design:check-circle-outlined',
    '中低风险': 'ant-design:info-circle-outlined',
    '中等风险': 'ant-design:exclamation-circle-outlined',
    '高风险': 'ant-design:warning-outlined',
    '极高风险': 'ant-design:close-circle-outlined'
  };
  return iconMap[level] || 'ant-design:question-circle-outlined';
}

// 获取风险概率范围
function getRiskProbabilityRange(level: string): string {
  if (!lodgingRiskData.value?.current_risk?.lodgingProbability) return '未知';
  return lodgingRiskData.value.current_risk.lodgingProbability;
}

// ==================== 图表初始化 ====================

// 初始化趋势图
const initTrendChart = () => {
  if (!trendChartRef.value || !lodgingRiskData.value?.forecast_7days?.dailyRisks) return;
  
  const { setOptions } = useECharts(trendChartRef.value);
  const dailyRisks = lodgingRiskData.value.forecast_7days.dailyRisks;
  const maxRiskDate = lodgingRiskData.value.forecast_7days.max_risk_date;
  
  // 准备图表数据
  const dates = dailyRisks.map(day => {
    const date = new Date(day.date);
    return `${date.getMonth() + 1}月${date.getDate()}日`;
  });
  
  const riskScores = dailyRisks.map(day => (day.riskScore * 100).toFixed(1)); // 转换为百分比
  const windSpeeds = dailyRisks.map(day => day.weather?.windSpeed || 0);
  const rainfalls = dailyRisks.map(day => day.weather?.rainfall || 0);
  
  // 找出最高风险日的索引
  const maxRiskIndex = dailyRisks.findIndex(day => day.date === maxRiskDate);
  
  const option = {
    title: {
      text: '未来7天倒伏风险趋势',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6a7985'
        }
      },
      formatter: function(params) {
        let result = params[0].name + '<br/>';
        params.forEach(param => {
          if (param.seriesName === '风险评分') {
            result += `${param.seriesName}: ${param.value}%<br/>`;
          } else if (param.seriesName === '风速') {
            result += `${param.seriesName}: ${param.value}m/s<br/>`;
          } else if (param.seriesName === '降雨量') {
            result += `${param.seriesName}: ${param.value}mm<br/>`;
          }
        });
        return result;
      }
    },
    legend: {
      data: ['风险评分', '风速', '降雨量'],
      top: 30
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates
    },
    yAxis: [
      {
        type: 'value',
        name: '风险评分(%)',
        min: 0,
        max: 100,
        position: 'left',
        axisLine: {
          lineStyle: {
            color: '#5470C6'
          }
        },
        axisLabel: {
          formatter: '{value}%'
        }
      },
      {
        type: 'value',
        name: '风速(m/s)/降雨量(mm)',
        min: 0,
        position: 'right',
        axisLine: {
          lineStyle: {
            color: '#91CC75'
          }
        }
      }
    ],
    series: [
      {
        name: '风险评分',
        type: 'line',
        yAxisIndex: 0,
        data: riskScores,
        smooth: true,
        itemStyle: {
          color: '#5470C6'
        },
        markPoint: {
          data: []
        }
      },
      {
        name: '风速',
        type: 'line',
        yAxisIndex: 1,
        data: windSpeeds,
        smooth: true,
        itemStyle: {
          color: '#91CC75'
        }
      },
      {
        name: '降雨量',
        type: 'line',
        yAxisIndex: 1,
        data: rainfalls,
        smooth: true,
        itemStyle: {
          color: '#FAC858'
        }
      }
    ]
  };
  
  setOptions(option);
};

// ==================== 数据加载 ====================

// 加载倒伏风险数据
const loadLodgingRiskData = async () => {
  try {
    loading.value = true;
    console.log('开始加载倒伏风险数据...');
    
    // 检查是否有选中的地块
    if (!selectStore.selectedPlot || !selectStore.selectedPlot.plotId) {
      console.log('没有选中的地块，不加载倒伏风险数据');
      return;
    }
    
    const plotId = selectStore.selectedPlot.plotId;
    console.log('加载地块ID:', plotId, '的倒伏风险数据');
    
    // 模拟加载过程，确保用户能看到加载状态
    await new Promise(resolve => setTimeout(resolve, 500));
    
    // 调用API获取数据
    const res = await getLodgingRiskDataById(plotId);
    console.log('API返回数据:', res);
    
    if (res) {
      console.log('倒伏风险数据加载成功:', res);
      lodgingRiskData.value = res;
    } else {
      console.warn('倒伏风险数据为空');
      createMessage.warning('未获取到倒伏风险数据');
    }
  } catch (error) {
    console.error('加载倒伏风险数据失败:', error);
    createMessage.error('加载倒伏风险数据失败');
  } finally {
    // 确保加载状态至少显示1秒，提供更好的用户体验
    setTimeout(() => {
      loading.value = false;
    }, 500);
  }
};

// ==================== 监听器 ====================

// 监听视图模式变化
watch(
  () => viewMode.value,
  (newVal) => {
    if (newVal === 'chart') {
      nextTick(() => {
        initTrendChart();
      });
    }
  }
);

// 监听数据变化
watch(
  () => lodgingRiskData.value,
  () => {
    if (viewMode.value === 'chart') {
      nextTick(() => {
        initTrendChart();
      });
    }
  },
  { deep: true }
);

// 监听地块选择变化
watch(
  () => selectStore.selectedPlot?.plotId,
  (newPlotId) => {
    console.log('监听到地块ID变化:', newPlotId);
    if (newPlotId) {
      loadLodgingRiskData();
    } else {
      console.log('地块ID为空，不加载倒伏风险数据');
    }
  },
  { immediate: true }
);

// ==================== 生命周期钩子 ====================

// 组件挂载时初始化数据
onMounted(() => {
  console.log('组件挂载，检查是否有选中的地块');
  
  // 设置初始加载状态
  loading.value = true;
  
  // 如果已经有选中的地块，则加载数据
  if (selectStore.selectedPlot?.plotId) {
    console.log('已有选中的地块，加载数据');
    loadLodgingRiskData();
  } else {
    console.log('没有选中的地块，不加载数据');
    // 如果没有选中的地块，也显示一段时间的加载状态，然后隐藏
    setTimeout(() => {
      loading.value = false;
    }, 500);
  }
});
</script>

<style lang="less" scoped>
.lodging-risk-page {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 64px);
  
  // 全屏加载遮罩样式
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
      
      .loading-icon {
        margin-bottom: 16px;
      }
    }
  }

  // ==================== 风险等级总览卡片样式 ====================
  .risk-overview-card {
    margin-bottom: 16px;
    transition: all 0.3s;

    .risk-overview-content {
      display: flex;
      padding: 16px;

      // 左侧风险等级标识
      .risk-level-indicator {
        flex: 1;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        padding-right: 24px;
        border-right: 1px dashed rgba(0, 0, 0, 0.1);

        .risk-level-icon {
          font-size: 64px;
          margin-bottom: 16px;
        }

        .risk-level-text {
          font-size: 40px;
          font-weight: bold;
          margin-bottom: 12px;
        }
        
        .risk-probability-range {
          font-size: 24px;
          font-weight: 500;
          margin-bottom: 8px;
          color: #595959;
        }
        
        .last-update {
          font-size: 14px;
          color: #8c8c8c;
        }
      }
      
      // 右侧风险因子信息
      .risk-factors-info {
        flex: 2;
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        grid-gap: 16px;
        padding-left: 24px;
        
        .factor-item {
          display: flex;
          align-items: center;
          padding: 12px;
          border-radius: 8px;
          background-color: #f9f9f9;

          .factor-icon {
            width: 40px;
            height: 40px;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 50%;
            background-color: rgba(24, 144, 255, 0.1);
            margin-right: 12px;
            font-size: 18px;
          }
          
          .factor-details {
            flex: 1;

            .factor-name {
              font-size: 14px;
              color: #262626;
            }
          }
          
          .factor-value {
            font-size: 16px;
            font-weight: 500;
            color: #595959;
            margin-left: 8px;
          }
        }
      }
    }
  }

  // ==================== 核心预警区域样式 ====================
  .risk-forecast-card {
    margin-bottom: 16px;
    
    .card-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 16px;
      font-weight: 500;
      color: #262626;
    }

    .risk-forecast-container {
      display: flex;
      justify-content: space-between;
      gap: 12px;
      padding: 16px 0;
    }
    
    .risk-day-card {
      flex: 1;
      border-radius: 8px;
      border: 1px solid #e8e8e8;
      background: #fff;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
      transition: all 0.3s;
      overflow: hidden;
      min-width: 0;
      max-width: 160px;
      
      &:hover {
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        transform: translateY(-2px);
      }
      
      .day-header {
        padding: 10px 12px;
        background: #fafafa;
        text-align: center;
        
        .day-date {
          font-size: 14px;
          font-weight: 500;
          color: #262626;
        }
        
        .day-weekday {
          font-size: 12px;
          color: #8c8c8c;
          margin-top: 2px;
        }
      }
      
      .risk-level-badge {
        text-align: center;
        padding: 6px 0;
        font-size: 12px;
        font-weight: 500;
      }
      
      .risk-details {
        padding: 12px;
        
        .risk-probability {
          display: flex;
          flex-direction: column;
          align-items: center;
          margin-bottom: 12px;
          
          .probability-value {
            font-size: 24px;
            font-weight: 600;
            line-height: 1;
            color: #262626;
          }
          
          .probability-label {
            font-size: 12px;
            color: #8c8c8c;
          }
        }
        
        .risk-factors {
          display: flex;
          flex-direction: column;
          gap: 8px;
          margin-top: 8px;
          
          .factor-item {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 4px;
            font-size: 11px;
            color: #595959;
          }
        }
      }
    }
  }

  // ==================== 防控建议区样式 ====================
  .suggestions-card {
    margin-bottom: 16px;
    
    .card-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 16px;
      font-weight: 500;
      color: #262626;
    }
    
    :deep(.ant-collapse) {
      background-color: transparent;
      border: none;
    }
    
    :deep(.ant-collapse-item) {
      border: 1px solid #e8e8e8;
      margin-bottom: 12px;
      border-radius: 8px;
      overflow: hidden;
      
      &:last-child {
        margin-bottom: 0;
      }
    }
    
    :deep(.ant-collapse-header) {
      padding: 12px 16px;
      background-color: #fafafa;
      font-weight: 500;
      font-size: 14px;
    }
    
    :deep(.ant-collapse-content) {
      border-top: 1px solid #e8e8e8;
    }
    
    :deep(.ant-collapse-content-box) {
      padding: 16px;
    }
    
    .suggestions-content {
      .suggestion-item {
        display: flex;
        margin-bottom: 16px;
        
        &:last-child {
          margin-bottom: 0;
        }
        
        .suggestion-icon {
          margin-right: 12px;
          font-size: 16px;
          
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
            font-size: 14px;
            font-weight: 500;
            color: #262626;
            margin-bottom: 4px;
          }
          
          .suggestion-desc {
            font-size: 13px;
            color: #8c8c8c;
            line-height: 1.5;
          }
        }
      }
    }
  }

  // ==================== 历史记录区样式 ====================
  .history-card {
    margin-bottom: 16px;
    
    .card-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 16px;
      font-weight: 500;
      color: #262626;
    }
    
    .history-list {
      .history-item {
        padding: 12px 0;
        border-bottom: 1px solid #f0f0f0;
        
        &:last-child {
          border-bottom: none;
        }
        
        .history-content {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          
          .history-info {
            flex: 1;
            
            .history-title {
              font-size: 14px;
              font-weight: 500;
              color: #262626;
              margin-bottom: 4px;
            }
            
            .history-desc {
              font-size: 13px;
              color: #8c8c8c;
              line-height: 1.5;
            }
          }
          
          .history-actions {
            display: flex;
            gap: 8px;
          }
        }
      }
    }
    
    .empty-history {
      padding: 40px 0;
      text-align: center;
    }
  }

  // ==================== 趋势图样式 ====================
  .risk-trend-chart {
    width: 100%;
    height: 400px;
    padding: 10px;
    
    .trend-chart {
      width: 100%;
      height: 100%;
    }
  }

  // ==================== 响应式样式 ====================
  @media (max-width: 768px) {
    padding: 16px;
    
    .risk-overview-card {
      .risk-overview-content {
        flex-direction: column;
        
        .risk-level-indicator {
          padding-right: 0;
          padding-bottom: 16px;
          border-right: none;
          border-bottom: 1px dashed rgba(0, 0, 0, 0.1);
          
          .risk-level-icon {
            font-size: 48px;
            margin-bottom: 12px;
          }
          
          .risk-level-text {
            font-size: 32px;
            margin-bottom: 8px;
          }
          
          .risk-probability-range {
            font-size: 20px;
            margin-bottom: 6px;
          }
        }
        
        .risk-factors-info {
          padding-left: 0;
          padding-top: 16px;
          
          .factor-item {
            margin-bottom: 12px;
            
            .factor-icon {
              width: 32px;
              height: 32px;
              margin-right: 10px;
            }
          }
        }
      }
    }
    
    .risk-forecast-card {
      .risk-forecast-container {
        gap: 8px;
        
        .risk-day-card {
          width: 120px;
          
          .day-header {
            padding: 6px 8px;
            
            .day-date {
              font-size: 13px;
            }
            
            .day-weekday {
              font-size: 11px;
            }
          }
          
          .risk-level-badge {
            font-size: 11px;
            padding: 3px 0;
          }
          
          .risk-details {
            padding: 6px 8px;
            
            .risk-probability {
              font-size: 11px;
            }
            
            .risk-factors {
              gap: 6px;
              
              .factor-item {
                font-size: 10px;
              }
            }
          }
        }
      }
    }
    
    .suggestions-card {
      :deep(.ant-collapse-header) {
        padding: 10px 12px;
        font-size: 13px;
      }
      
      :deep(.ant-collapse-content-box) {
        padding: 12px;
      }
      
      .suggestions-content {
        .suggestion-item {
          margin-bottom: 12px;
          
          .suggestion-icon {
            margin-right: 10px;
            font-size: 14px;
          }
          
          .suggestion-text {
            .suggestion-title {
              font-size: 13px;
            }
            
            .suggestion-desc {
              font-size: 12px;
            }
          }
        }
      }
    }
    
    .history-card {
      .history-list {
        .history-item {
          padding: 10px 0;
          
          .history-content {
            flex-direction: column;
            
            .history-info {
              margin-bottom: 8px;
              
              .history-title {
                font-size: 13px;
              }
              
              .history-desc {
                font-size: 12px;
              }
            }
          }
        }
      }
    }
  }
}
</style>
