<template>
  <div class="rapeseed-dashboard">
    <PageWrapper title="油菜生长决策系统">
      <!-- 基地和地块选择 -->
      <div class="base-field-selector">
        <a-row :gutter="16">
          <a-col :span="8">
            <a-select
              v-model:value="selectedBase"
              placeholder="请选择基地"
              style="width: 100%"
              @change="handleBaseChange"
            >
              <a-select-option v-for="base in baseList" :key="base.id" :value="base.id">
                {{ base.name }}
              </a-select-option>
            </a-select>
          </a-col>
          <a-col :span="8">
            <a-select
              v-model:value="selectedField"
              placeholder="请选择地块"
              style="width: 100%"
              @change="handleFieldChange"
            >
              <a-select-option v-for="field in fieldList" :key="field.id" :value="field.id">
                {{ field.name }} ({{ field.area }}亩)
              </a-select-option>
            </a-select>
          </a-col>
          <a-col :span="8">
            <a-button type="primary" @click="refreshData">刷新数据</a-button>
          </a-col>
        </a-row>
      </div>

      <!-- 关键指标概览 -->
      <div class="dashboard-overview">
        <a-row :gutter="24">
          <a-col :span="6">
            <a-card>
              <template #title>
                <span>基地总面积</span>
              </template>
              <div class="statistic-value">{{ totalBaseArea }} 亩</div>
              <div class="statistic-desc">{{ selectedBaseName }} - {{ fieldCount }}个地块</div>
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card>
              <template #title>
                <span>当前生长阶段</span>
              </template>
              <div class="statistic-value">{{ currentGrowthStage }}</div>
              <div class="statistic-desc">预计持续 {{ stageDuration }} 天</div>
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card>
              <template #title>
                <span>倒伏预警</span>
              </template>
              <div class="statistic-value warning">{{ lodgingWarningCount }} 处</div>
              <div class="statistic-desc">中度风险，需加强监测</div>
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card>
              <template #title>
                <span>产量预估</span>
              </template>
              <div class="statistic-value">{{ estimatedYield }} kg/亩</div>
              <div class="statistic-desc">较历史平均高 {{ yieldIncrease }}%</div>
            </a-card>
          </a-col>
        </a-row>
      </div>

      <!-- 地块分布和生长进度 -->
      <a-row :gutter="24" style="margin-top: 24px">
        <a-col :span="12">
          <a-card title="地块分布概览">
            <div class="field-overview">
              <div class="field-map">
                <div class="map-placeholder">基地地块分布图</div>
              </div>
              <div class="field-list">
                <a-list :data-source="fieldList" size="small">
                  <template #renderItem="{ item }">
                    <a-list-item>
                      <a-list-item-meta>
                        <template #title>
                          <span>{{ item.name }}</span>
                          <a-tag :color="getFieldStatusColor(item.status)">
                            {{ item.statusText }}
                          </a-tag>
                        </template>
                        <template #description>
                          <span>面积: {{ item.area }}亩 | 阶段: {{ item.stage }}</span>
                        </template>
                      </a-list-item-meta>
                    </a-list-item>
                  </template>
                </a-list>
              </div>
            </div>
          </a-card>
        </a-col>
        <a-col :span="12">
          <a-card title="生长阶段进度">
            <div class="growth-progress">
              <a-steps :current="currentStageIndex" direction="vertical" size="small">
                <a-step v-for="stage in growthStages" :key="stage.id" :title="stage.name" :description="stage.description" />
              </a-steps>
              
              <!-- 角果成熟期倒伏预警详情 -->
              <div v-if="currentGrowthStage === '角果成熟期'" style="margin-top: 20px; padding: 15px; background-color: #fff7e6; border-radius: 6px;">
                <h4 style="color: #fa8c16; margin-top: 0;">倒伏风险因素</h4>
                <ul style="margin-bottom: 0;">
                  <li v-for="(factor, index) in lodgingRiskFactors" :key="index">{{ factor }}</li>
                </ul>
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>

      <!-- 决策建议和环境监测 -->
      <a-row :gutter="24" style="margin-top: 24px">
        <a-col :span="12">
          <a-card title="当前阶段决策建议">
            <div class="decision-advice">
              <div class="advice-item">
                <div class="advice-title">倒伏预警</div>
                <div class="advice-content">{{ lodgingPreventionAdvice }}</div>
              </div>
              <div class="advice-item">
                <div class="advice-title">施肥建议</div>
                <div class="advice-content">{{ fertilizationAdvice }}</div>
              </div>
              <div class="advice-item">
                <div class="advice-title">水分管理</div>
                <div class="advice-content">{{ waterManagementAdvice }}</div>
              </div>
              <div class="advice-item">
                <div class="advice-title">病虫害防治</div>
                <div class="advice-content">{{ pestControlAdvice }}</div>
              </div>
            </div>
          </a-card>
        </a-col>
        <a-col :span="12">
          <a-card title="环境监测数据">
            <div class="environment-data">
              <a-row>
                <a-col :span="12">
                  <div class="env-item">
                    <div class="env-label">温度</div>
                    <div class="env-value">{{ temperature }}°C</div>
                    <div class="env-status normal">适宜</div>
                  </div>
                </a-col>
                <a-col :span="12">
                  <div class="env-item">
                    <div class="env-label">湿度</div>
                    <div class="env-value">{{ humidity }}%</div>
                    <div class="env-status normal">适宜</div>
                  </div>
                </a-col>
              </a-row>
              <a-row>
                <a-col :span="12">
                  <div class="env-item">
                    <div class="env-label">土壤湿度</div>
                    <div class="env-value">{{ soilMoisture }}%</div>
                    <div class="env-status warning">偏低</div>
                  </div>
                </a-col>
                <a-col :span="12">
                  <div class="env-item">
                    <div class="env-label">光照强度</div>
                    <div class="env-value">{{ lightIntensity }} lux</div>
                    <div class="env-status normal">适宜</div>
                  </div>
                </a-col>
              </a-row>
            </div>
          </a-card>
        </a-col>
      </a-row>

      <!-- 近期任务和生长趋势 -->
      <a-row :gutter="24" style="margin-top: 24px">
        <a-col :span="12">
          <a-card title="近期任务">
            <a-list :data-source="recentTasks" size="small">
              <template #renderItem="{ item }">
                <a-list-item>
                  <a-list-item-meta>
                    <template #title>
                      <span>{{ item.title }}</span>
                      <a-tag :color="item.priority === 'high' ? 'red' : item.priority === 'medium' ? 'orange' : 'blue'">
                        {{ item.priorityText }}
                      </a-tag>
                    </template>
                    <template #description>
                      <span>{{ item.date }} | {{ item.fieldName }}</span>
                    </template>
                  </a-list-item-meta>
                </a-list-item>
              </template>
            </a-list>
          </a-card>
        </a-col>
        <a-col :span="12">
          <a-card title="生长趋势分析">
            <div ref="chartRef" :style="{ height: '300px' }"></div>
          </a-card>
        </a-col>
      </a-row>
    </PageWrapper>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, computed } from 'vue';
import { PageWrapper } from '/@/components/Page';
import * as echarts from 'echarts';

// 组件引用
const chartRef = ref<HTMLElement>();

// 基地和地块相关数据
const selectedBase = ref<string>('base1');
const selectedField = ref<string>('field1');

// 基地列表
const baseList = ref([
  { id: 'base1', name: '东郊农业示范基地', totalArea: 1250 },
  { id: 'base2', name: '西郊生态农业园', totalArea: 980 },
  { id: 'base3', name: '南郊高效农业区', totalArea: 1560 }
]);

// 地块列表
const fieldList = ref([
  { id: 'field1', name: 'A区-1号地块', area: 120, baseId: 'base1', status: 'growing', statusText: '生长期', stage: '蕾薹期' },
  { id: 'field2', name: 'A区-2号地块', area: 150, baseId: 'base1', status: 'growing', statusText: '生长期', stage: '蕾薹期' },
  { id: 'field3', name: 'B区-1号地块', area: 180, baseId: 'base1', status: 'flowering', statusText: '开花期', stage: '开花期' },
  { id: 'field4', name: 'B区-2号地块', area: 200, baseId: 'base1', status: 'growing', statusText: '生长期', stage: '蕾薹期' },
  { id: 'field5', name: 'C区-1号地块', area: 160, baseId: 'base1', status: 'mature', statusText: '成熟期', stage: '角果成熟期' },
  { id: 'field6', name: 'C区-2号地块', area: 140, baseId: 'base1', status: 'harvest', statusText: '收获期', stage: '收获期' }
]);

// 生长阶段数据
const growthStages = ref([
  { id: 1, name: '播种期', description: '已完成' },
  { id: 2, name: '苗期', description: '已完成' },
  { id: 3, name: '蕾薹期', description: '已完成' },
  { id: 4, name: '开花期', description: '已完成' },
  { id: 5, name: '角果成熟期', description: '倒伏预警：中度风险，需加强监测' },
  { id: 6, name: '收获与整地', description: '未开始' }
]);

// 计算属性
const selectedBaseName = computed(() => {
  const base = baseList.value.find(b => b.id === selectedBase.value);
  return base ? base.name : '';
});

const totalBaseArea = computed(() => {
  const base = baseList.value.find(b => b.id === selectedBase.value);
  return base ? base.totalArea : 0;
});

const fieldCount = computed(() => {
  return fieldList.value.filter(f => f.baseId === selectedBase.value).length;
});

const currentGrowthStage = computed(() => {
  const field = fieldList.value.find(f => f.id === selectedField.value);
  return field ? field.stage : '蕾薹期';
});

const currentStageIndex = computed(() => {
  const stage = growthStages.value.find(s => s.name === currentGrowthStage.value);
  return stage ? stage.id - 1 : 2; // 默认蕾薹期
});

const stageDuration = ref(15);
const pestWarningCount = ref(2);
const estimatedYield = ref(185);
const yieldIncrease = ref(8);

// 倒伏预警相关数据
const lodgingWarningLevel = ref('medium'); // low, medium, high
const lodgingWarningCount = ref(3);
const lodgingRiskFactors = ref(['近期降雨量大', '土壤湿度偏高', '植株高度超过阈值']);

// 环境数据
const temperature = ref(18.5);
const humidity = ref(65);
const soilMoisture = ref(58);
const lightIntensity = ref(45000);

// 决策建议
const fertilizationAdvice = ref('角果成熟期应停止氮肥施用，可适量补充钾肥，增强植株抗倒伏能力');
const waterManagementAdvice = ref('当前土壤湿度偏高，应暂停灌溉，加强排水，降低倒伏风险');
const pestControlAdvice = ref('重点防治茎象甲等害虫，减少茎秆损伤，提高抗倒伏能力');

// 倒伏预警建议
const lodgingPreventionAdvice = ref('建议采取以下措施防止倒伏：1.设置支架或绳索支撑；2.合理控制灌溉量；3.避免在大风天气前进行灌溉；4.及时排水降低土壤湿度');

// 近期任务数据
const recentTasks = ref([
  {
    title: '倒伏风险监测',
    date: '今天',
    priority: 'high',
    priorityText: '紧急',
    fieldName: 'C区-1号地块'
  },
  {
    title: '设置防倒伏支架',
    date: '2天后',
    priority: 'high',
    priorityText: '紧急',
    fieldName: 'C区-1号地块'
  },
  {
    title: '排水系统检查',
    date: '3天后',
    priority: 'medium',
    priorityText: '普通',
    fieldName: 'C区-1号地块'
  },
  {
    title: '茎秆强度检测',
    date: '5天后',
    priority: 'medium',
    priorityText: '普通',
    fieldName: 'C区-1号地块'
  }
]);

// 方法
const handleBaseChange = (baseId: string) => {
  // 重置地块选择
  selectedField.value = '';
  // 根据基地筛选地块
  const fields = fieldList.value.filter(f => f.baseId === baseId);
  if (fields.length > 0) {
    selectedField.value = fields[0].id;
  }
  // 刷新数据
  refreshData();
};

const handleFieldChange = (fieldId: string) => {
  // 根据地块更新数据
  refreshData();
};

const refreshData = () => {
  // 模拟数据刷新
  console.log('刷新数据', selectedBase.value, selectedField.value);
  // 这里可以调用API获取最新数据
};

const getFieldStatusColor = (status: string) => {
  const colorMap: Record<string, string> = {
    'growing': 'green',
    'flowering': 'blue',
    'mature': 'orange',
    'harvest': 'red'
  };
  return colorMap[status] || 'default';
};

// 初始化图表
const initChart = () => {
  if (!chartRef.value) return;
  
  const chart = echarts.init(chartRef.value);
  const option = {
    title: {
      text: '油菜生长指标与倒伏风险趋势',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['株高(cm)', '叶片数', '土壤湿度(%)', '温度(°C)', '倒伏风险指数'],
      top: 30
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: ['第1周', '第2周', '第3周', '第4周', '第5周', '第6周']
    },
    yAxis: [
      {
        type: 'value',
        name: '数值',
        position: 'left'
      }
    ],
    series: [
      {
        name: '株高(cm)',
        type: 'line',
        data: [10, 15, 22, 30, 38, 45]
      },
      {
        name: '叶片数',
        type: 'line',
        data: [4, 6, 8, 10, 12, 14]
      },
      {
        name: '土壤湿度(%)',
        type: 'line',
        data: [65, 62, 58, 60, 63, 58]
      },
      {
        name: '温度(°C)',
        type: 'line',
        data: [15, 16, 17, 18, 19, 18.5]
      },
      {
        name: '倒伏风险指数',
        type: 'line',
        lineStyle: {
          color: '#ff4d4f'
        },
        itemStyle: {
          color: '#ff4d4f'
        },
        data: [10, 15, 25, 40, 65, 75]
      }
    ]
  };
  
  chart.setOption(option);
  
  // 响应式调整
  window.addEventListener('resize', () => {
    chart.resize();
  });
};

onMounted(() => {
  // 初始化数据
  handleBaseChange(selectedBase.value);
  // 初始化图表
  initChart();
});
</script>

<style lang="less" scoped>
.rapeseed-dashboard {
  .base-field-selector {
    margin-bottom: 24px;
    padding: 16px;
    background-color: #fafafa;
    border-radius: 6px;
  }
  
  .dashboard-overview {
    margin-bottom: 24px;
    
    .statistic-value {
      font-size: 30px;
      font-weight: bold;
      margin: 10px 0;
      
      &.warning {
        color: #faad14;
      }
    }
    
    .statistic-desc {
      color: rgba(0, 0, 0, 0.45);
      font-size: 14px;
    }
  }
  
  .field-overview {
    .field-map {
      height: 200px;
      background-color: #f5f5f5;
      border-radius: 4px;
      margin-bottom: 16px;
      display: flex;
      align-items: center;
      justify-content: center;
      
      .map-placeholder {
        color: rgba(0, 0, 0, 0.25);
        font-size: 16px;
      }
    }
    
    .field-list {
      max-height: 200px;
      overflow-y: auto;
    }
  }
  
  .growth-progress {
    padding: 10px 0;
  }
  
  .decision-advice {
    .advice-item {
      margin-bottom: 16px;
      padding-bottom: 16px;
      border-bottom: 1px solid #f0f0f0;
      
      &:last-child {
        margin-bottom: 0;
        padding-bottom: 0;
        border-bottom: none;
      }
      
      .advice-title {
        font-weight: bold;
        margin-bottom: 8px;
        color: #1890ff;
      }
      
      .advice-content {
        color: rgba(0, 0, 0, 0.65);
        line-height: 1.5;
      }
    }
  }
  
  .environment-data {
    .env-item {
      padding: 12px 0;
      text-align: center;
      
      .env-label {
        color: rgba(0, 0, 0, 0.45);
        font-size: 14px;
        margin-bottom: 8px;
      }
      
      .env-value {
        font-size: 24px;
        font-weight: bold;
        margin-bottom: 8px;
      }
      
      .env-status {
        font-size: 14px;
        padding: 2px 8px;
        border-radius: 10px;
        
        &.normal {
          background-color: #f6ffed;
          color: #52c41a;
        }
        
        &.warning {
          background-color: #fff7e6;
          color: #fa8c16;
        }
        
        &.danger {
          background-color: #fff1f0;
          color: #ff4d4f;
        }
      }
    }
  }
}
</style>
