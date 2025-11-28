<template>
  <div class="plot-risk-detail-page">
    <!-- 返回按钮 -->
    <div class="back-button">
      <a-button @click="goBack">
        <template #icon><ArrowLeftOutlined /></template>
        返回倒伏风险监测
      </a-button>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-overlay">
      <div class="loading-content">
        <a-spin size="large" />
        <div class="loading-text">正在加载地块风险数据...</div>
      </div>
    </div>

    <!-- 地块详情内容 - 模仿弹框结构 -->
    <div class="plot-detail-content" v-else-if="plotData">
      <!-- 风险等级总览卡片 -->
      <a-card 
        :bordered="false" 
        class="risk-overview-card" 
        :style="{ 
          backgroundColor: getRiskColor(plotData.current_risk.riskScore / 100) + '15', 
          borderLeft: `5px solid ${getRiskColor(plotData.current_risk.riskScore / 100)}` 
        }" 
      >
        <div class="risk-overview-content">
          <!-- 左侧风险等级标识 -->
          <div class="risk-level-indicator">
            <div class="risk-level-icon" :style="{ color: getRiskColor(plotData.current_risk.riskScore) }">
              <ExclamationCircleOutlined />
            </div>
            <div class="risk-level-text" :style="{ color: getRiskColor(plotData.current_risk.riskScore) }">
              {{ plotData.current_risk.riskLevel }}
            </div>
            <div class="risk-probability-range">
              倒伏概率：{{ plotData.current_risk.lodgingProbability }}  
            </div>
            <div class="last-update">上次更新：{{ formatDateTime(plotData.calculationTime) }}</div>
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
                <!-- 显示当前生长数据 -->
                <div class="factors-grid">
                  <div class="factor-item" v-for="factor in growthFactors" :key="factor.name">
                    <div class="factor-icon" :style="{ color: factor.color }">
                      <Icon :icon="factor.icon" />
                    </div>
                    <div class="factor-details">
                      <div class="factor-name">{{ factor.name }}</div>
                    </div>
                    <div class="factor-value">{{ factor.value }}</div>
                  </div>
                </div>
                <div style="text-align: center; margin: 20px 0;">
                  <a-button type="primary" size="large" @click="openGrowthFormModal">
                    <template #icon><FormOutlined /></template>
                    更新生长数据
                  </a-button>
                </div>
                
                <!-- 生长数据录入对话框 -->
                <a-modal
                  v-model:open="showGrowthFormModal"
                  title="录入生长监测数据"
                  :width="700"
                  :footer="null"
                  centered
                >
                  <template #title>
                    <div style="display: flex; align-items: center;">
                      <FormOutlined style="margin-right: 8px; color: #1890ff;" />
                      录入生长监测数据
                    </div>
                  </template>
                  
                  <div style="padding: 24px;">
                    <p style="margin-bottom: 16px; color: rgba(0, 0, 0, 0.65);">
                      请准确填写作物的生长数据，以便系统计算倒伏风险
                    </p>
                    
                    <a-form :model="growthForm" layout="vertical">
                      <a-row :gutter="16">
                        <a-col :span="12">
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
                        </a-col>
                        <a-col :span="12">
                          <a-form-item label="监测日期" name="monitorDate">
                            <a-date-picker
                              v-model:value="growthForm.monitorDate"
                              placeholder="请选择监测日期"
                              style="width: 100%"
                            />
                          </a-form-item>
                        </a-col>
                      </a-row>
                      <a-row :gutter="16">
                        <a-col :span="12">
                          <a-form-item label="植株高度 (cm)" name="plantHeight">
                            <a-input-number
                              v-model:value="growthForm.plantHeight"
                              :min="0"
                              :precision="1"
                              placeholder="请输入植株高度"
                              style="width: 100%"
                              :addon-after="'cm'"
                            />
                          </a-form-item>
                        </a-col>
                        <a-col :span="12">
                          <a-form-item label="茎直径 (mm)" name="stemDiameter">
                            <a-input-number
                              v-model:value="growthForm.stemDiameter"
                              :min="0"
                              :precision="1"
                              placeholder="请输入茎直径"
                              style="width: 100%"
                              :addon-after="'mm'"
                            />
                          </a-form-item>
                        </a-col>
                      </a-row>
                      <a-row :gutter="16">
                        <a-col :span="12">
                          <a-form-item label="种植密度 (株/m²)" name="density">
                            <a-input-number
                              v-model:value="growthForm.density"
                              :min="0"
                              :precision="1"
                              placeholder="请输入种植密度"
                              style="width: 100%"
                              :addon-after="'株/m²'"
                            />
                          </a-form-item>
                        </a-col>
                      </a-row>
                      
                      <div style="text-align: right; margin-top: 24px;">
                        <a-space>
                          <a-button @click="showGrowthFormModal = false">
                            取消
                          </a-button>
                          <a-button type="primary" @click="submitGrowthForm" :loading="loading">
                            <template #icon><CheckOutlined /></template>
                            提交数据
                          </a-button>
                        </a-space>
                      </div>
                    </a-form>
                  </div>
                </a-modal>
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
        <div v-if="plotData.forecast_7_days?.dailyRisks && plotData.forecast_7_days.dailyRisks.length > 0" class="risk-forecast-container">
          <div 
            v-for="(day, index) in plotData.forecast_7_days.dailyRisks"  
            :key="index" 
            class="risk-day-card" 
            :style="{ 
              backgroundColor: getRiskColor(day.riskScore) + '15', 
              borderLeft: `3px solid ${getRiskColor(day.riskScore)}` 
            }" 
          >
            <div class="day-header">
              <div class="day-date">{{ formatDate(day.date) }}</div>
              <div class="day-weekday">{{ getWeekday(day.date) }}</div>
            </div>
            <div class="risk-level-badge" :style="{ backgroundColor: getRiskColor(day.riskScore), color: 'white' }">
              {{ getRiskLevel(day.riskScore) }}
            </div>
            <div class="risk-details">
              <div class="risk-probability">
                <span class="probability-value" :style="{ color: getRiskColor(day.riskScore) }">
                  {{ Math.round(day.riskScore * 100) }}%
                </span>
                <span class="probability-label">概率</span>
              </div>
              <div class="risk-factors">
                <span class="factor-item">
                  <span :style="{ color: getRiskColor(day.riskScore) }">风速</span>
                  {{ day.weather?.windSpeed || 0 }}m/s
                </span>
                <span class="factor-item">
                  <span :style="{ color: getRiskColor(day.riskScore) }">降雨量</span>
                  {{ day.weather?.rainfall || 0 }}mm
                </span>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 无数据提示 -->
        <div v-else class="no-data-container">
          <a-empty description="暂无风险预测数据" />
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

    <!-- 错误状态 -->
    <div v-else class="error-state">
      <a-result
        status="error"
        title="加载失败"
        sub-title="无法加载地块风险数据，请稍后重试"
      >
        <template #extra>
          <a-button type="primary" @click="goBack">返回</a-button>
        </template>
      </a-result>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { 
  ArrowLeftOutlined, 
  ExclamationCircleOutlined, 
  BarChartOutlined, 
  CloudOutlined, 
  BulbOutlined,
  InfoCircleOutlined,
  FormOutlined,
  CheckOutlined
} from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import dayjs from 'dayjs';
import { getLodgingRiskDataById } from './lodging-risk/lodgingRisk.api';
import { addGrowthMonitoring } from '/@/api/rapeseed/growthMonitoring';
import { Icon } from '/@/components/Icon';

const createMessage = message;

// 路由相关
const route = useRoute();
const router = useRouter();

// 状态变量
const loading = ref(true);
const plotData = ref<any>(null);
const activeKey = ref(['1']); // 默认展开第一个折叠面板
const showGrowthFormModal = ref(false); // 控制生长数据录入对话框显示

// 生长监测表单
const growthForm = ref({
  growthStage: '',
  plantHeight: null,
  stemDiameter: null,
  density: null,
  monitorDate: null
});

// 风险建议和分析配置
const RISK_RECOMMENDATIONS = [
  { 
    min: 0.0, max: 0.30, 
    recommendation: "保持常规管理，定期监测",
    analysis: "当前倒伏风险较低，作物生长状况良好。建议保持常规管理，定期监测作物生长状态和天气变化。"
  },
  { 
    min: 0.30, max: 0.50, 
    recommendation: "增加监测频率，注意天气变化",
    analysis: "存在一定倒伏风险，主要受近期天气影响。建议增加监测频率，特别关注大风暴雨等极端天气预警。"
  },
  { 
    min: 0.50, max: 0.65, 
    recommendation: "采取预防措施，加固植株",
    analysis: "倒伏风险较高，作物可能受到多方面因素影响。建议采取预防性措施，如适当培土、加固植株等。"
  },
  { 
    min: 0.65, max: 0.80, 
    recommendation: "立即采取防护措施，减少损失",
    analysis: "倒伏风险高，需要立即采取防护措施。建议检查植株健康状况，考虑减少灌溉，避免加重倒伏风险。"
  },
  { 
    min: 0.80, max: 1.0, 
    recommendation: "紧急处理，考虑收割或转移",
    analysis: "倒伏风险极高，作物面临严重威胁。建议紧急处理，如提前收割或采取其他减少损失的措施。"
  }
];

// 获取风险颜色
function getRiskColor(score: number): string {
  if (score < 0.3) return '#52c41a'; // 绿色 - 低风险
  if (score < 0.5) return '#faad14'; // 黄色 - 中等风险
  if (score < 0.7) return '#fa8c16'; // 橙色 - 较高风险
  if (score < 0.9) return '#f5222d'; // 红色 - 高风险
  return '#8b0000'; // 深红色 - 极高风险
}

// 获取风险等级
function getRiskLevel(score: number): string {
  if (score < 0.3) return '低风险';
  if (score < 0.5) return '中等风险';
  if (score < 0.7) return '较高风险';
  if (score < 0.9) return '高风险';
  return '极高风险';
}

// 获取建议措施
function getRecommendation(score: number): string {
  const normalizedScore = Math.max(0, Math.min(1, score));
  const riskConfig = RISK_RECOMMENDATIONS.find(config => 
    normalizedScore >= config.min && normalizedScore < config.max
  ) || RISK_RECOMMENDATIONS[RISK_RECOMMENDATIONS.length - 1];
  return riskConfig.recommendation;
}

// 获取因子名称
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

// 获取因子图标
function getFactorIcon(key: string): string {
  const factorIcons = {
    'plantHeight': 'mdi:grass',
    'stemDiameter': 'mdi:circle-outline',
    'slendernessRatio': 'mdi:chart-line',
    'windSpeed3d': 'mdi:weather-windy',
    'rainfall7d': 'mdi:weather-rainy',
    'growthStage': 'mdi:flower',
    'density': 'mdi:grid',
    'soilType': 'mdi:terrain'
  };
  return factorIcons[key] || 'mdi:help-circle';
}

// 获取因子颜色
function getFactorColor(key: string): string {
  const factorColors = {
    'plantHeight': '#52c41a',
    'stemDiameter': '#1890ff',
    'slendernessRatio': '#fa8c16',
    'windSpeed3d': '#722ed1',
    'rainfall7d': '#13c2c2',
    'growthStage': '#eb2f96',
    'density': '#faad14',
    'soilType': '#8c8c8c'
  };
  return factorColors[key] || '#1890ff';
}

// 判断是否为气象因子
function isWeatherFactor(key: string): boolean {
  return ['windSpeed3d', 'rainfall7d', 'temperature', 'humidity'].includes(key);
}

// 判断是否为生长因子
function isGrowthFactor(key: string): boolean {
  return ['plantHeight', 'stemDiameter', 'slendernessRatio', 'growthStage', 'density'].includes(key);
}

// 获取建议类型名称
function getSuggestionTypeName(type: string): string {
  const typeNames = {
    'immediate': '立即措施',
    'shortTerm': '短期措施',
    'mediumTerm': '中期措施',
    'longTerm': '长期措施'
  };
  return typeNames[type] || type;
}

// 格式化日期
function formatDate(dateStr: string): string {
  // 处理API返回的日期格式，如 "Fri Nov 28 00:00:00 CST 2025"
  // dayjs可以自动解析这种格式
  return dayjs(dateStr).format('MM-DD');
}

// 格式化日期时间
function formatDateTime(dateStr: string): string {
  // 处理API返回的日期格式，如 "Fri Nov 28 06:50:14 CST 2025"
  // dayjs可以自动解析这种格式
  return dayjs(dateStr).format('YYYY-MM-DD HH:mm:ss');
}

// 获取星期几
function getWeekday(dateStr: string): string {
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
  return weekdays[dayjs(dateStr).day()];
}

// 返回上一页
function goBack() {
  router.go(-1);
}

// 打开生长数据录入对话框
function openGrowthFormModal() {
  showGrowthFormModal.value = true;
}

// 气象数据计算属性
const weatherFactors = computed(() => {
  if (!plotData.value?.current_risk?.originalFactors) return [];
  
  const factors = plotData.value.current_risk.originalFactors;
  const normalizedFactors = plotData.value.current_risk.normalizedFactors || {};
  
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

// 生长因子数据计算属性
const growthFactors = computed(() => {
  // 尝试从多个可能的位置获取生长因子数据
  let factors = {};
  
  // 尝试从 originalFactors 获取
  if (plotData.value?.current_risk?.originalFactors) {
    factors = plotData.value.current_risk.originalFactors;
  }
  // 尝试从 riskFactors 获取
  else if (plotData.value?.current_risk?.riskFactors) {
    factors = plotData.value.current_risk.riskFactors;
  }
  // 尝试从 currentRisk 直接获取
  else if (plotData.value?.currentRisk) {
    factors = plotData.value.currentRisk;
  }
  
  // 如果没有找到任何因子数据，返回空数组
  if (Object.keys(factors).length === 0) {
    console.log('未找到生长因子数据，可用的数据结构:', JSON.stringify(plotData.value, null, 2));
    return [];
  }
  
  console.log('找到的因子数据:', JSON.stringify(factors, null, 2));
  
  const normalizedFactors = plotData.value?.current_risk?.normalizedFactors || {};
  
  // 只提取生长相关因子
  const growthFactorKeys = ['plantHeight', 'stemDiameter', 'slendernessRatio', 'growthStage', 'density'];
  
  // 因子图标映射
  const factorIcons: Record<string, string> = {
    'plantHeight': 'ant-design:stock-outlined',
    'stemDiameter': 'ant-design:column-width-outlined',
    'slendernessRatio': 'ant-design:rise-outlined',
    'growthStage': 'ant-design:flower-outlined',
    'density': 'ant-design:unordered-list-outlined'
  };
  
  // 因子名称映射
  const factorNames: Record<string, string> = {
    'plantHeight': '植株高度',
    'stemDiameter': '茎直径',
    'slendernessRatio': '细长比',
    'growthStage': '生长阶段',
    'density': '种植密度'
  };
  
  // 过滤出存在的生长因子
  const existingGrowthFactors = growthFactorKeys.filter(key => factors[key] !== undefined && factors[key] !== null);
  
  if (existingGrowthFactors.length === 0) {
    console.log('未找到任何生长因子数据，可用的因子键:', Object.keys(factors));
    return [];
  }
  
  return existingGrowthFactors.map(key => {
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
        displayValue = value.toFixed(2);
        break;
      case 'growthStage':
        displayValue = value;
        break;
      case 'density':
        displayValue = `${value}株/m²`;
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

// 防控建议数据计算属性
const suggestionsData = computed(() => {
  if (!plotData.value?.comprehensive_suggestions) return [];
  
  // 根据API响应的数据结构，将comprehensive_suggestions转换为页面需要的格式
  const suggestions = [];
  
  // 处理风险预警
  if (plotData.value.comprehensive_suggestions.riskAlert && plotData.value.comprehensive_suggestions.riskAlert.length > 0) {
    suggestions.push({
      title: '风险预警',
      items: plotData.value.comprehensive_suggestions.riskAlert.map(alert => ({
        title: alert,
        desc: ''
      }))
    });
  }
  
  // 处理紧急措施
  if (plotData.value.comprehensive_suggestions.immediate && plotData.value.comprehensive_suggestions.immediate.length > 0) {
    suggestions.push({
      title: '紧急措施',
      items: plotData.value.comprehensive_suggestions.immediate.map(measure => ({
        title: measure,
        desc: ''
      }))
    });
  }
  
  // 处理短期措施
  if (plotData.value.comprehensive_suggestions.shortTerm && plotData.value.comprehensive_suggestions.shortTerm.length > 0) {
    suggestions.push({
      title: '短期措施',
      items: plotData.value.comprehensive_suggestions.shortTerm.map(measure => ({
        title: measure,
        desc: ''
      }))
    });
  }
  
  // 处理中期措施
  if (plotData.value.comprehensive_suggestions.mediumTerm && plotData.value.comprehensive_suggestions.mediumTerm.length > 0) {
    suggestions.push({
      title: '中期措施',
      items: plotData.value.comprehensive_suggestions.mediumTerm.map(measure => ({
        title: measure,
        desc: ''
      }))
    });
  }
  
  // 处理长期措施
  if (plotData.value.comprehensive_suggestions.longTerm && plotData.value.comprehensive_suggestions.longTerm.length > 0) {
    suggestions.push({
      title: '长期措施',
      items: plotData.value.comprehensive_suggestions.longTerm.map(measure => ({
        title: measure,
        desc: ''
      }))
    });
  }
  
  return suggestions;
});

// 预测图表数据
const forecastChartData = computed(() => {
  if (!plotData.value || !plotData.value.forecast_7_days || !plotData.value.forecast_7_days.dailyRisks) {
    return [];
  }

  const dailyRisks = plotData.value.forecast_7_days.dailyRisks;
  return dailyRisks.map(day => ({
    name: formatDate(day.date),
    value: day.riskScore
  }));
});

// 提交生长监测表单
async function submitGrowthForm() {
  // 验证表单
  if (!growthForm.value.growthStage) {
    createMessage.warning('请选择生长阶段');
    return;
  }
  
  if (!growthForm.value.plantHeight || growthForm.value.plantHeight <= 0) {
    createMessage.warning('请输入有效的植株高度');
    return;
  }
  
  if (!growthForm.value.stemDiameter || growthForm.value.stemDiameter <= 0) {
    createMessage.warning('请输入有效的茎直径');
    return;
  }
  
  if (!growthForm.value.density || growthForm.value.density <= 0) {
    createMessage.warning('请输入有效的种植密度');
    return;
  }
  
  if (!growthForm.value.monitorDate) {
    createMessage.warning('请选择监测日期');
    return;
  }
  
  // 构建提交数据
  const formData = {
    plotId: plotData.value?.plotId,
    growthStage: growthForm.value.growthStage,
    plantHeight: growthForm.value.plantHeight,
    stemDiameter: growthForm.value.stemDiameter,
    density: growthForm.value.density,
    monitoringDate: growthForm.value.monitorDate.format('YYYY-MM-DD')
  };
  
  try {
    // 显示提交提示
    createMessage.loading('正在提交生长监测数据...', 0);
    
    // 调用API提交数据
    await addGrowthMonitoring(formData);
    
    // 移除提交提示
    createMessage.destroy();
    
    // 显示成功消息
    createMessage.success('生长监测数据已成功提交，系统将重新计算倒伏风险');
    
    // 重置表单
    Object.assign(growthForm.value, {
      growthStage: '',
      plantHeight: null,
      stemDiameter: null,
      density: null,
      monitorDate: null
    });
    
    // 关闭对话框
    showGrowthFormModal.value = false;
    
    // 刷新当前地块的倒伏风险数据
    if (plotData.value?.plotId) {
      await loadPlotData();
      createMessage.success('倒伏风险数据已更新');
    }
  } catch (error) {
    // 移除提交提示
    createMessage.destroy();
    console.error('提交生长监测数据失败:', error);
    createMessage.error('提交失败，请检查网络连接或稍后重试');
  }
}

// 加载地块数据
async function loadPlotData() {
  try {
    loading.value = true;
    const plotId = route.params.plotId as string;
    
    // 调用API获取地块风险数据
    const response = await getLodgingRiskDataById(plotId);
    
    if (response) {
      // 进行字段映射，确保数据结构与模板匹配
      plotData.value = {
        ...response,
        // 将API返回的forecast_7days映射为forecast_7_days
        forecast_7_days: response.forecast_7days || {
          dailyRisks: []
        },
        // 将API返回的current_risk映射为current_risk（如果需要）
        current_risk: response.current_risk || response.currentRisk || {
          riskLevel: '未知',
          riskScore: 0,
          lodgingProbability: '0%',
          riskFactors: {}
        },
        // 确保comprehensive_suggestions字段存在
        comprehensive_suggestions: response.comprehensive_suggestions || {
          riskAlert: [],
          immediate: [],
          shortTerm: [],
          mediumTerm: [],
          longTerm: []
        }
      };
      console.log('转换后地块数据:', JSON.stringify(plotData.value, null, 2));
      console.log('7天预测风险:', plotData.value.forecast_7_days?.dailyRisks);
    } else {
      // 如果API返回空数据，使用默认值
      plotData.value = {
        plotId: plotId,
        plotName: `地块${plotId}`,
        baseName: '示范基地',
        area: 50,
        calculationTime: new Date().toISOString(),
        current_risk: {
          riskLevel: '未知',
          riskScore: 0,
          lodgingProbability: '0%',
          riskFactors: {}
        },
        forecast_7_days: {
          dailyRisks: []
        },
        comprehensive_suggestions: {},
        suggestions: {}
      };
    }
    
  } catch (error) {
    console.error('加载地块数据失败:', error);
    createMessage.error('加载地块数据失败');
    
    // 加载失败时使用默认值
    plotData.value = {
      plotId: route.params.plotId as string,
      plotName: `地块${route.params.plotId}`,
      baseName: '示范基地',
      area: 50,
      calculationTime: new Date().toISOString(),
      current_risk: {
        riskLevel: '未知',
        riskScore: 0,
        lodgingProbability: '0%',
        riskFactors: {}
      },
      forecast_7_days: {
        dailyRisks: []
      },
      comprehensive_suggestions: {},
      suggestions: {}
    };
  } finally {
    loading.value = false;
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadPlotData();
});
</script>

<style lang="less" scoped>
.plot-risk-detail-page {
  min-height: 100vh;
  background: #f0f2f5;
  padding: 16px;
  
  .back-button {
    margin-bottom: 16px;
    display: flex;
    align-items: center;
    gap: 8px;
  }
  
  .loading-overlay {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 300px;
    
    .ant-spin {
      font-size: 24px;
    }
    
    .loading-text {
      font-size: 16px;
      opacity: 0.8;
      margin-top: 16px;
    }
  }
  
  .error-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    min-height: 300px;
    padding: 24px;
    text-align: center;
    
    .error-icon {
      font-size: 48px;
      color: #ff4d4f;
      margin-bottom: 16px;
    }
    
    .error-title {
      font-size: 18px;
      font-weight: 500;
      margin-bottom: 8px;
    }
    
    .error-description {
      color: #666;
      margin-bottom: 16px;
    }
  }
  
  .plot-detail-content {
    max-width: 1400px;
    margin: 0 auto;
    
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
          
          // 高风险因子样式
          .high-risk-factors {
            .high-risk-items {
              display: flex;
              flex-wrap: wrap;
              margin-top: 8px;
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
                    
                    .factor-name {
                      font-size: 14px;
                      color: #666;
                      margin-bottom: 4px;
                    }
                    
                    .factor-value {
                      font-size: 18px;
                      font-weight: 600;
                      color: #262626;
                      margin-bottom: 4px;
                    }
                    
                    .factor-bar {
                      height: 4px;
                      background-color: #f0f0f0;
                      border-radius: 2px;
                      overflow: hidden;
                      margin-top: 8px;
                      
                      .factor-bar-fill {
                        height: 100%;
                        border-radius: 2px;
                        transition: width 0.3s ease;
                      }
                    }
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
                margin-top: 16px;
                
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
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
      }
      
      .card-title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 16px;
        font-weight: 600;
        color: #262626;
      }
      
      .no-data-container {
        display: flex;
        justify-content: center;
        align-items: center;
        padding: 40px 0;
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
    }
    
    // 防控建议卡片样式
    .suggestions-card {
      margin-bottom: 16px;
      border-radius: 8px;
      overflow: hidden;
      transition: all 0.3s ease;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
      }
      
      .card-title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 16px;
        font-weight: 600;
        color: #262626;
      }
      
      .suggestions-panel {
        margin-bottom: 8px;
        border-radius: 8px;
        overflow: hidden;
        
        :deep(.ant-collapse-header) {
          font-weight: 500;
        }
        
        .suggestions-content {
          padding: 8px 0;
          
          .suggestion-item {
            display: flex;
            gap: 12px;
            margin-bottom: 16px;
            
            &:last-child {
              margin-bottom: 0;
            }
            
            .suggestion-icon {
              flex-shrink: 0;
              width: 24px;
              height: 24px;
              border-radius: 50%;
              display: flex;
              align-items: center;
              justify-content: center;
              font-size: 14px;
              
              &.danger {
                background-color: #fff2f0;
                color: #ff4d4f;
              }
              
              &.warning {
                background-color: #fffbe6;
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
                font-size: 14px;
                color: #666;
              }
            }
          }
        }
      }
    }
  }
  
  // 响应式布局
  @media (max-width: 1400px) {
    .plot-detail-content {
      max-width: 1200px;
    }
  }
  
  @media (max-width: 1200px) {
    .plot-detail-content {
      max-width: 1000px;
      
      .risk-overview-content {
        .risk-factors-info {
          .factors-container {
            gap: 16px;
          }
        }
      }
    }
  }
  
  @media (max-width: 992px) {
    padding: 12px;
    
    .plot-detail-content {
      .risk-overview-content {
        flex-direction: column;
        
        .risk-level-indicator {
          flex: none;
          border-right: none;
          border-bottom: 1px solid rgba(0, 0, 0, 0.06);
          padding: 16px;
        }
        
        .risk-factors-info {
          .factors-container {
            flex-direction: column;
            gap: 16px;
            
            .factors-column {
              &:first-child, &:last-child {
                flex: 1;
              }
              
              .column-title {
                font-size: 14px;
                margin-bottom: 12px;
              }
              
              .factors-grid {
                grid-template-columns: repeat(2, 1fr);
                gap: 12px;
                
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
      
      .risk-forecast-card {
        .no-data-container {
          display: flex;
          justify-content: center;
          align-items: center;
          padding: 40px 0;
        }
        
        .risk-forecast-container {
          grid-template-columns: repeat(auto-fit, minmax(100px, 1fr));
          gap: 8px;
          
          .risk-day-card {
            padding: 8px 4px;
          }
        }
      }
    }
  }
}
</style>