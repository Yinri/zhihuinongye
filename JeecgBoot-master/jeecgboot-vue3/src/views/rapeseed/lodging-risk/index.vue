<template>
  <div class="lodging-risk-page">
    
    <!-- 风险等级总览卡片 -->
    <a-card :bordered="false" class="risk-overview-card" :style="{ backgroundColor: getRiskColor(currentRiskLevel) + '20', borderLeft: `5px solid ${getRiskColor(currentRiskLevel)}` }">
      <div class="risk-overview-content">
        <!-- 左侧风险等级标识 -->
        <div class="risk-level-indicator">
          <div class="risk-level-icon" :style="{ color: getRiskColor(currentRiskLevel) }">
            <Icon :icon="getRiskIcon(currentRiskLevel)" />
          </div>
          <div class="risk-level-text" :style="{ color: getRiskColor(currentRiskLevel) }">{{ getRiskLevelText(currentRiskLevel) }}</div>
          <div class="risk-probability-range">倒伏概率：{{ getRiskProbabilityRange(currentRiskLevel) }}</div>
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
          :style="{ backgroundColor: day.riskColor + '15', borderLeft: `3px solid ${day.riskColor}` }"
        >
          <div class="day-header">
            <div class="day-date">{{ day.date }}</div>
            <div class="day-weekday">{{ day.weekday }}</div>
          </div>
          <div class="risk-level-badge" :style="{ backgroundColor: day.riskColor, color: 'white' }">{{ day.riskLevel }}</div>
          <div class="risk-details">
            <div class="risk-probability">
              <span class="probability-value" :style="{ color: day.riskColor }">{{ day.probability }}</span>
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
        <a-collapse-panel v-for="(suggestion, index) in suggestionsData" :key="String(index + 1)" :header="suggestion.title" class="suggestions-panel">
          <template #extra>
            <a-tag :color="index === 0 ? 'red' : 'orange'">{{ index === 0 ? '立即实施' : '中长期策略' }}</a-tag>
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
              <a-button v-for="(action, index) in item.actions" :key="index" 
                :type="item.type === 'warning' ? 'primary' : 'default'" 
                size="small">
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


    <!-- 加载状态提示 -->
    <a-card :bordered="false" v-if="loading" class="loading-card">
      <a-skeleton active :paragraph="{ rows: 5 }" />
    </a-card>

    
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, computed, watch, nextTick } from 'vue';
import { useMessage } from '/@/hooks/web/useMessage';
import { Icon } from '/@/components/Icon';
import { useECharts } from '/@/hooks/web/useECharts';

const { createMessage } = useMessage();

// 加载状态
const loading = ref(false);

// 视图模式：card-卡片视图，chart-趋势图视图
const viewMode = ref('card');

// 趋势图引用
const trendChartRef = ref<HTMLDivElement | null>(null);

// 倒伏风险数据
const lodgingRiskData = ref<any>(null);

// 当前风险等级
const currentRiskLevel = computed(() => {
  if (!lodgingRiskData.value?.current_risk) return '不存在';
  
  const riskLevel = lodgingRiskData.value.current_risk.risk_level;
  // 直接返回英文风险等级，模拟数据已经是英文格式
  return riskLevel || 'medium';
});

// 上次更新时间
const lastUpdateTime = computed(() => {
  if (!lodgingRiskData.value?.calculation_time) return '';
  
  const date = new Date(lodgingRiskData.value.calculation_time);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  
  return `${year}-${month}-${day} ${hours}:${minutes}`;
});

// 风险因子数据
const riskFactors = computed(() => {
  if (!lodgingRiskData.value?.current_risk?.original_factors) return [];
  
  const factors = lodgingRiskData.value.current_risk.original_factors;
  const normalizedFactors = lodgingRiskData.value.current_risk.normalized_factors || {};
  const factorIcons: Record<string, string> = {
    'plant_height': 'ant-design:rise-outlined',
    'stem_diameter': 'ant-design:column-width-outlined',
    'slenderness_ratio': 'ant-design:column-height-outlined',
    'wind_speed_3d': 'ant-design:thunderbolt-outlined',
    'rainfall_7d': 'ant-design:cloud-rain-outlined',
    'growth_stage': 'ant-design:field-time-outlined',
    'density': 'ant-design:border-outlined',
    'soil_type': 'ant-design:environment-outlined'
  };
  
  const factorNames: Record<string, string> = {
    'plant_height': '株高',
    'stem_diameter': '茎粗',
    'slenderness_ratio': '细长比',
    'wind_speed_3d': '近3天最大风速',
    'rainfall_7d': '近7天累积降雨',
    'growth_stage': '生育期',
    'density': '种植密度',
    'soil_type': '土壤类型'
  };
  
  return Object.keys(factors).map(key => {
    const value = factors[key];
    const normalizedValue = normalizedFactors[key] || 0;
    let displayValue = '';
    
    // 根据因子类型设置显示值
    switch(key) {
      case 'plant_height':
        displayValue = `${value}cm`;
        break;
      case 'stem_diameter':
        displayValue = `${value}mm`;
        break;
      case 'slenderness_ratio':
        displayValue = value.toString();
        break;
      case 'wind_speed_3d':
        displayValue = `${value}m/s`;
        break;
      case 'rainfall_7d':
        displayValue = `${value}mm`;
        break;
      case 'growth_stage':
        displayValue = value; // 直接显示原始值，如"花期"
        break;
      case 'density':
        displayValue = `${value}株/m²`;
        break;
      case 'soil_type':
        displayValue = value; // 直接显示原始值，如"砂壤土"
        break;
      default:
        displayValue = value.toString();
    }
    
    // 根据标准化值获取颜色
    const getFactorColor = (normalizedValue) => {
      // 根据标准化值范围确定颜色
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
    };
    
    return {
      name: factorNames[key] || key,
      value: displayValue,
      icon: factorIcons[key] || 'ant-design:info-circle-outlined',
      percentage: Math.round(normalizedValue * 100),
      color: getFactorColor(normalizedValue) // 使用标准化值计算颜色
    };
  });
});

// 防控建议折叠面板激活状态
const activeKey = ref(['1']);

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
      items: suggestions.short_term?.map((item: string) => ({
        title: '',
        desc: item
      })) || []
    },
    {
      title: '中期措施（4-7天）',
      icon: 'ControlOutlined',
      color: '#1890ff',
      items: suggestions.medium_term?.map((item: string) => ({
        title: '',
        desc: item
      })) || []
    },
    {
      title: '长期优化（下季改进）',
      icon: 'ControlOutlined',
      color: '#1890ff',
      items: suggestions.long_term?.map((item: string) => ({
        title: '',
        desc: item
      })) || []
    }
  ];
});

// 历史记录数据
const historyData = computed(() => {
  // 如果没有历史记录数据，返回空数组
  if (!lodgingRiskData.value?.historical_records) return [];
  
  // 从实际数据中获取历史记录
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
  // 适配模拟数据格式
  if (!lodgingRiskData.value?.forecast_7days?.daily_risks) return [];
  
  const dailyRisks = lodgingRiskData.value.forecast_7days.daily_risks;
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
  
  // 根据风险评分获取颜色
  const getRiskColorByScore = (score) => {
    // 根据风险评分范围确定颜色
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
  };
  
  return dailyRisks.map((day: any) => {
    const date = new Date(day.date);
    const weekday = weekdays[date.getDay()];
    
    // 根据风速和降雨量计算风险等级
    let riskLevel = day.risk_level || 'low';
    
    
    // 根据风险等级计算概率
    let probability = day.lodging_probability || '10-30%';
    

    
    return {
      date: `${date.getMonth() + 1}月${date.getDate()}日`,
      weekday,
      riskLevel,
      riskColor: getRiskColorByScore(day.risk_score || 0), // 使用风险评分计算颜色
      probability,
      windSpeed: day.weather?.wind_speed || 0, // 使用原始风速数据
      rainfall: day.weather?.rainfall || 0, // 使用原始降雨量数据
      riskScore: day.risk_score || 0, // 添加风险评分
      fullDate: day.date // 保存完整日期用于比较
    };
  });
});

// 初始化趋势图
const initTrendChart = () => {
  if (!trendChartRef.value || !lodgingRiskData.value?.forecast_7days?.daily_risks) return;
  
  const { setOptions } = useECharts(trendChartRef.value);
  const dailyRisks = lodgingRiskData.value.forecast_7days.daily_risks;
  const maxRiskDate = lodgingRiskData.value.forecast_7days.max_risk_date;
  
  // 准备图表数据
  const dates = dailyRisks.map(day => {
    const date = new Date(day.date);
    return `${date.getMonth() + 1}月${date.getDate()}日`;
  });
  
  const riskScores = dailyRisks.map(day => (day.risk_score * 100).toFixed(1)); // 转换为百分比
  const windSpeeds = dailyRisks.map(day => day.weather?.wind_speed || 0);
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



/**
 * 加载倒伏风险数据
 */
async function loadLodgingRiskData() {
  try {
    loading.value = true;
    
    // 使用模拟数据替代API调用
    const mockData = {
    // 1. 当前风险
    "current_risk": {
      "risk_score": 0.56,
      "risk_level": "中等风险",
      "lodging_probability": "15-40%",
      // 标准化后的因子值（0-1）
      "normalized_factors": {
        "plant_height": 0.61,          // 株高标准化值
        "stem_diameter": 0.52,         // 茎粗标准化值
        "slenderness_ratio": 0.45,     // 细长比
        "wind_speed_3d": 0.67,         // 近3天最大风速
        "rainfall_7d": 0.34,           // 近7天累积降雨
        "growth_stage": 0.95,          // 生育期（0.95=花期）
        "density": 0.42,               // 种植密度
        "soil_type": 0.60              // 土壤类型
      },
      "original_factors": {
        "plant_height": 145.0,
        "stem_diameter": 7.0,
        "wind_speed_3d": 7.0,
        "rainfall_7d": 15.0,
        "growth_stage": "花期",
        "density": 25.0,
        "soil_type": "砂壤土"
      },

      "factor_contributions": {
          "plant_height": 0.098,         // 株高贡献了9.8%的风险
          "wind_speed_3d": 0.134,        // 风速贡献了13.4%的风险
      // ... 其他因子
       },
      // 高风险因子列表（标准化值>0.7的因子）
      "high_risk_factors": [
      {
        "key": "growth_stage",
        "name": "生育期",
        "normalized_value": 0.95,
        "status": "critical"         // critical(>0.8) 或 warning(0.7-0.8)
      },
      {
        "key": "wind_speed_3d",
        "name": "近3天最大风速",
        "normalized_value": 0.73,
        "status": "warning"
      }
      ],
    },

    // 2. 未来7天预测
    "forecast_7days": {
      "field_id": "field_001",
      "daily_risks": [
        {
          "date": "2025-11-06",
          "risk_score": 0.52,
          "risk_level": "中等风险",
          "lodging_probability": "15-40%",
          "weather": {"wind_speed": 7.0, "rainfall": 15.0}
        },
        {
          "date": "2025-11-07",
          "risk_score": 0.78,
          "risk_level": "高风险",
          "lodging_probability": "40-70%",
          "weather": {"wind_speed": 14.0, "rainfall": 40.0}
        },
        // ... 其他5天数据
	      {
          "date": "2025-11-08",
          "risk_score": 0.52,
          "risk_level": "中等风险",
          "lodging_probability": "15-40%",
          "weather": {"wind_speed": 7.0, "rainfall": 15.0}
        },
        {
          "date": "2025-11-09",
          "risk_score": 0.52,
          "risk_level": "中等风险",
          "lodging_probability": "15-40%",
          "weather": {"wind_speed": 7.0, "rainfall": 15.0}
        },
        {
          "date": "2025-11-10",
          "risk_score": 0.52,
          "risk_level": "中等风险",
          "lodging_probability": "15-40%",
          "weather": {"wind_speed": 7.0, "rainfall": 15.0}
        },
        {
          "date": "2025-11-11",
          "risk_score": 0.52,
          "risk_level": "中等风险",
          "lodging_probability": "15-40%",
          "weather": {"wind_speed": 7.0, "rainfall": 15.0}
        },
        {
          "date": "2025-11-12",
          "risk_score": 0.52,
          "risk_level": "中等风险",
          "lodging_probability": "15-40%",
          "weather": {"wind_speed": 7.0, "rainfall": 15.0}
        },

      ],
      "max_risk_date": "2025-11-07",
      "max_risk_score": 0.78
    },

    // 3. 综合防护建议（核心）
    "comprehensive_suggestions": {
      "risk_alert": [
        "⚡ 当前风险等级：中等风险，建议加强防护准备",
        "🔴 未来高风险日期：2025-11-07, 2025-11-08，请提前做好防护",
        "📍 2025-11-07 风险最高（评分: 0.78），建议当日重点关注",
        "📈 风险呈上升趋势，建议逐步加强防护措施"
      ],
      "immediate": [
        "今日加强田间巡查，密切关注植株状态",
        "检查并清理排水系统，确保排水畅通",
        "⚠️ 明日风险升高，今日需准备好防护材料"
      ],
      "short_term": [
        "未来3天内有2天高风险，需制定分阶段防护方案",
        "1-2天内喷施抗倒伏剂（胺鲜酯30mg/L或多效唑50mg/L）",
        "2025-11-07 风速达14m/s，提前加固支撑",
        "每日巡查田间，及时扶正轻度倾斜植株"
      ],
      "medium_term": [
        "4-7天后风险期：2025-11-10，需提前规划物资",
        "提前采购或预定支撑材料、抗倒伏剂等物资",
        "制定一周防护计划，合理安排人力物力"
      ],
      "long_term": [
        "下季选择抗倒伏品种（茎秆粗壮型、矮秆型）",
        "优化施肥策略：基肥为主，追肥适量，增施钾肥提高抗性",
        "建立倒伏风险档案，总结经验用于下季改进"
      ]
    },

    // 4. 历史记录与对比
    "historical_records": [
      {
        "id": 1,
        "date": "2025-10-28",
        "title": "倒伏事件",
        "desc": "东南区域发生中度倒伏，影响面积约2亩",
        "type": "warning",
        "actions": ["已处理", "已记录"]
      },
      {
        "id": 2,
        "date": "2025-10-25",
        "title": "与周边地块对比",
        "desc": "本地块抗倒伏能力低于周边平均水平15%",
        "type": "info",
        "actions": ["查看详情", "生成报告"]
      },
      {
        "id": 3,
        "date": "2025-10-20",
        "title": "防护措施效果",
        "desc": "前期喷施抗倒伏剂后，倒伏风险降低20%",
        "type": "success",
        "actions": ["查看详情"]
      }
    ],

    "calculation_time": "2025-11-05T14:30:00Z"
  };
    
    // 设置模拟数据
    lodgingRiskData.value = mockData;
    
    // 原来的API调用代码（注释掉）
    // const response = await getLodgingRiskData();
    // if (response && response.data) {
    //   lodgingRiskData.value = response.data;
    // }
  } catch (error) {
    console.error('加载倒伏风险数据失败:', error);
    createMessage.error('加载倒伏风险数据失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}



// 组件挂载时初始化数据
onMounted(() => {
  // 初始化加载数据
  loadLodgingRiskData();
});

// 获取风险等级文本
function getRiskLevelText(level) {
  if (!lodgingRiskData.value?.current_risk?.risk_level) return '未知风险';
  return lodgingRiskData.value.current_risk.risk_level;
}

// 获取风险等级颜色
function getRiskColor(level) {
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
  const riskScore = lodgingRiskData.value?.current_risk?.risk_score || 0.5;
  
  // 根据风险评分范围确定颜色
  if (riskScore >= 0.0 && riskScore < 0.30) {
    return "#4CAF50"; // 低风险 - 绿色
  } else if (riskScore >= 0.30 && riskScore < 0.50) {
    return "#FFEB3B"; // 中低风险 - 黄色
  } else if (riskScore >= 0.50 && riskScore < 0.65) {
    return "#FF9800"; // 中等风险 - 橙色
  } else if (riskScore >= 0.65 && riskScore < 0.80) {
    return "#F44336"; // 高风险 - 红色
  } else if (riskScore >= 0.80 && riskScore <= 1.0) {
    return "#D32F2F"; // 极高风险 - 深红色
  }
  
  return "#FF9800"; // 默认橙色
}

// 获取风险图标
function getRiskIcon(level) {
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
function getRiskProbabilityRange(level) {
  if (!lodgingRiskData.value?.current_risk?.lodging_probability) return '未知';
  return lodgingRiskData.value.current_risk.lodging_probability;
}
</script>

<style lang="less" scoped>
.lodging-risk-page {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 64px);
}

// 风险等级总览卡片样式
.risk-overview-card {
  margin-bottom: 16px;
  transition: all 0.3s;
  
  // 风险等级背景色
  &.risk-low-bg {
    background-color: #E8F5E9;
    border-left: 5px solid #52c41a;
  }
  
  &.risk-medium-low-bg {
    background-color: #FFF9C4;
    border-left: 5px solid #afcb2b;
  }
  
  &.risk-medium-bg {
    background-color: #FFE0B2;
    border-left: 5px solid #faad14;
  }
  
  &.risk-high-bg {
    background-color: #FFCDD2;
    border-left: 5px solid #ff4d4f;
  }
  
  &.risk-very-high-bg {
    background-color: #EF5350;
    border-left: 5px solid #d32f2f;
    color: white;
    
    .risk-level-text,
    .risk-probability-range,
    .last-update,
    .factor-name {
      color: white;
    }
  }
  
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
        
        &.low {
          color: #52c41a;
        }
        
        &.medium-low {
          color: #afcb2b;
        }
        
        &.medium {
          color: #faad14;
        }
        
        &.high {
          color: #ff4d4f;
        }
        
        &.very-high {
          color: white;
        }
      }
      
      .risk-level-text {
        font-size: 40px;
        font-weight: bold;
        margin-bottom: 12px;
        
        &.low {
          color: #52c41a;
        }
        
        &.medium-low {
          color: #afcb2b;
        }
        
        &.medium {
          color: #faad14;
        }
        
        &.high {
          color: #ff4d4f;
        }
        
        &.very-high {
          color: white;
        }
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

.page-header {
  margin-bottom: 24px;
  
  .page-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 20px;
    font-weight: 600;
    color: #262626;
    margin-bottom: 8px;
  }
  
  .page-description {
    color: #8c8c8c;
    font-size: 14px;
  }
}

// 核心预警区域样式
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
      
      &.high {
        background-color: #ff4d4f;
        color: white;
      }
      
      &.medium {
        background-color: #faad14;
        color: white;
      }
      
      &.low {
        background-color: #52c41a;
        color: white;
      }
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
      
      .risk-time {
        font-size: 12px;
        color: #595959;
        margin-bottom: 8px;
        text-align: center;
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
          
          .high-icon {
            color: #ff4d4f;
          }
          
          .medium-icon {
            color: #faad14;
          }
          
          .info-icon {
            color: #1890ff;
          }
          
          .low-icon {
            color: #52c41a;
          }
        }
      }
    }
    
    // 根据风险等级设置左侧边框
    &.risk-high {
      border-left: 3px solid #ff4d4f;
    }
    
    &.risk-medium {
      border-left: 3px solid #faad14;
    }
    
    &.risk-low {
      border-left: 3px solid #52c41a;
    }
  }
}

// 防控建议区样式
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

// 历史记录区样式
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

// 趋势图样式
.risk-trend-chart {
  width: 100%;
  height: 400px;
  padding: 10px;
  
  .trend-chart {
    width: 100%;
    height: 100%;
  }
}

@media (max-width: 768px) {
  .lodging-risk-page {
    padding: 16px;
  }
  
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
            margin-right: 12px;
          }
          
          .factor-details {
            .factor-name {
              font-size: 14px;
            }
            
            .factor-value {
              font-size: 12px;
            }
          }
          
          .factor-indicator {
            width: 80px;
            height: 6px;
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
          
          .risk-probability, .risk-time {
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
</style>
