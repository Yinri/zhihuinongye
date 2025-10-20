<template>
  <div class="production-plan-dashboard">
    <PageWrapper title="油菜生产计划管理">
      <!-- 顶部概览区域 -->
<div class="overview-section">
  <a-row :gutter="16">
    <a-col :span="6">
      <a-card class="overview-card" title="生产计划概览">
        <div class="overview-content">
          <div class="overview-item">
            <span class="overview-label">当前基地：</span>
            <span class="overview-value">{{ selectedBase?.name || '未选择' }}</span>
          </div>
          <div class="overview-item">
            <span class="overview-label">当前地块：</span>
            <span class="overview-value">{{ selectedField?.name || '未选择' }}</span>
          </div>
          <div class="overview-item">
            <span class="overview-label">计划总数：</span>
            <span class="overview-value">{{ productionPlanCount }}</span>
          </div>
        </div>
      </a-card>
    </a-col>
    <a-col :span="6">
      <a-card class="overview-card" title="种子投入">
        <div class="overview-content">
          <div class="overview-item">
            <span class="overview-label">种子类型：</span>
            <span class="overview-value">{{ seedInput.type || '-' }}</span>
          </div>
          <div class="overview-item">
            <span class="overview-label">播种量：</span>
            <span class="overview-value">{{ seedInput.amount || '-' }} kg</span>
          </div>
          <div class="overview-item">
            <span class="overview-label">预计成本：</span>
            <span class="overview-value">¥{{ seedInput.cost || '-' }}</span>
          </div>
        </div>
      </a-card>
    </a-col>
    <a-col :span="6">
      <a-card class="overview-card" title="肥料投入">
        <div class="overview-content">
          <div class="overview-item">
            <span class="overview-label">肥料类型：</span>
            <span class="overview-value">{{ fertilizerInput.type || '-' }}</span>
          </div>
          <div class="overview-item">
            <span class="overview-label">施肥量：</span>
            <span class="overview-value">{{ fertilizerInput.amount || '-' }} kg</span>
          </div>
          <div class="overview-item">
            <span class="overview-label">预计成本：</span>
            <span class="overview-value">¥{{ fertilizerInput.cost || '-' }}</span>
          </div>
        </div>
      </a-card>
    </a-col>
    <a-col :span="6">
      <a-card class="overview-card" title="农药投入">
        <div class="overview-content">
          <div class="overview-item">
            <span class="overview-label">农药类型：</span>
            <span class="overview-value">{{ pesticideInput.type || '-' }}</span>
          </div>
          <div class="overview-item">
            <span class="overview-label">用药量：</span>
            <span class="overview-value">{{ pesticideInput.amount || '-' }} L</span>
          </div>
          <div class="overview-item">
            <span class="overview-label">预计成本：</span>
            <span class="overview-value">¥{{ pesticideInput.cost || '-' }}</span>
          </div>
        </div>
      </a-card>
    </a-col>
  </a-row>
</div>

      <!-- 地块信息区域 -->
      <div class="field-section">
        <a-card title="地块信息" class="field-card">
          <a-row :gutter="24">
            <a-col :span="8">
              <a-select
                v-model:value="selectedBaseId"
                placeholder="请选择基地"
                class="base-selector"
                @change="handleBaseChange"
              >
                <a-select-option v-for="base in baseList" :key="base.id" :value="base.id">
                  {{ base.name }}
                </a-select-option>
              </a-select>
            </a-col>
            <a-col :span="8">
              <a-select
                v-model:value="selectedFieldId"
                placeholder="请选择地块"
                class="field-selector"
                @change="handleFieldChange"
                :disabled="!selectedBaseId"
              >
                <a-select-option v-for="field in fieldList" :key="field.id" :value="field.id">
                  {{ field.name }}
                </a-select-option>
              </a-select>
            </a-col>
            <a-col :span="8">
              <div v-if="selectedBase" class="base-info">
                <a-descriptions :column="2" bordered size="small">
                  <a-descriptions-item label="基地面积">{{ selectedBase.area }} 亩</a-descriptions-item>
                  <a-descriptions-item label="地块数量">{{ selectedBase.fieldCount }} 个</a-descriptions-item>
                </a-descriptions>
              </div>
            </a-col>
          </a-row>
          <a-row v-if="selectedField" style="margin-top: 16px;">
            <a-col :span="24">
              <div class="field-info">
                <a-descriptions :column="4" bordered size="small">
                  <a-descriptions-item label="面积">{{ selectedField.area }} 亩</a-descriptions-item>
                  <a-descriptions-item label="土壤类型">{{ selectedField.soilType }}</a-descriptions-item>
                  <a-descriptions-item label="土壤肥力">{{ selectedField.soilFertility }} 级</a-descriptions-item>
                  <a-descriptions-item label="承包农户">{{ selectedField.contractor }}</a-descriptions-item>
                  <a-descriptions-item label="合同编号">{{ selectedField.contractNo }}</a-descriptions-item>
                  <a-descriptions-item label="目标产量">{{ selectedField.targetYield }} 公斤/亩</a-descriptions-item>
                  <a-descriptions-item label="预计投入">{{ selectedField.investment }} 元</a-descriptions-item>
                  <a-descriptions-item label="状态">
                    <a-tag :color="getFieldStatusColor(selectedField.status)">
                      {{ getFieldStatusText(selectedField.status) }}
                    </a-tag>
                  </a-descriptions-item>
                </a-descriptions>
              </div>
            </a-col>
          </a-row>
        </a-card>
      </div>

      <!-- 生产计划详情区域 -->
      <div class="production-details-section">
        <a-tabs v-model:activeKey="activeTab" type="card">
          <!-- 种子投入 -->
          <a-tab-pane key="seed" tab="种子投入">
            <a-card>
              <a-table :dataSource="seedData" :columns="seedColumns" :pagination="false" bordered>
                <template #bodyCell="{ column, record }">
                  <span v-if="column.dataIndex === 'totalCost'">
                    {{ (record.quantity * record.price).toFixed(2) }} 元
                  </span>
                </template>
                <template #summary>
                  <a-table-summary-row>
                    <a-table-summary-cell :col-span="3" class="summary-label">合计</a-table-summary-cell>
                    <a-table-summary-cell class="summary-value">{{ seedTotalCost }} 元</a-table-summary-cell>
                  </a-table-summary-row>
                </template>
              </a-table>
            </a-card>
          </a-tab-pane>
          
          <!-- 肥料投入 -->
          <a-tab-pane key="fertilizer" tab="肥料投入">
            <a-card>
              <a-table :dataSource="fertilizerData" :columns="fertilizerColumns" :pagination="false" bordered>
                <template #bodyCell="{ column, record }">
                  <span v-if="column.dataIndex === 'totalCost'">
                    {{ (record.quantity * record.price).toFixed(2) }} 元
                  </span>
                </template>
                <template #summary>
                  <a-table-summary-row>
                    <a-table-summary-cell :col-span="3" class="summary-label">合计</a-table-summary-cell>
                    <a-table-summary-cell class="summary-value">{{ fertilizerTotalCost }} 元</a-table-summary-cell>
                  </a-table-summary-row>
                </template>
              </a-table>
            </a-card>
          </a-tab-pane>
          
          <!-- 农药投入 -->
          <a-tab-pane key="pesticide" tab="农药投入">
            <a-card>
              <a-table :dataSource="pesticideData" :columns="pesticideColumns" :pagination="false" bordered>
                <template #bodyCell="{ column, record }">
                  <span v-if="column.dataIndex === 'totalCost'">
                    {{ (record.quantity * record.price).toFixed(2) }} 元
                  </span>
                </template>
                <template #summary>
                  <a-table-summary-row>
                    <a-table-summary-cell :col-span="3" class="summary-label">合计</a-table-summary-cell>
                    <a-table-summary-cell class="summary-value">{{ pesticideTotalCost }} 元</a-table-summary-cell>
                  </a-table-summary-row>
                </template>
              </a-table>
            </a-card>
          </a-tab-pane>
          
          <!-- 数据来源 -->
          <a-tab-pane key="data-source" tab="数据来源">
            <a-card>
              <a-timeline>
                <a-timeline-item>
                  <template #dot>
                    <Icon icon="mdi:database" color="#1890ff" />
                  </template>
                  <div class="timeline-content">
                    <h4>三调数据</h4>
                    <p>第三次全国国土调查数据，包含土地利用现状、土地权属等信息</p>
                    <a-tag color="blue">已同步</a-tag>
                  </div>
                </a-timeline-item>
                <a-timeline-item>
                  <template #dot>
                    <Icon icon="mdi:file-document" color="#52c41a" />
                  </template>
                  <div class="timeline-content">
                    <h4>农户承包合同</h4>
                    <p>农户土地承包经营权合同信息，共{{ contractCount }}份合同</p>
                    <a-tag color="green">已验证</a-tag>
                  </div>
                </a-timeline-item>
                <a-timeline-item>
                  <template #dot>
                    <Icon icon="mdi:chart-line" color="#faad14" />
                  </template>
                  <div class="timeline-content">
                    <h4>历史产量数据</h4>
                    <p>近5年油菜种植产量数据，用于制定目标产量</p>
                    <a-tag color="orange">已分析</a-tag>
                  </div>
                </a-timeline-item>
                <a-timeline-item>
                  <template #dot>
                    <Icon icon="mdi:leaf" color="#722ed1" />
                  </template>
                  <div class="timeline-content">
                    <h4>土壤检测数据</h4>
                    <p>最新土壤肥力检测结果，用于制定施肥计划</p>
                    <a-tag color="purple">已更新</a-tag>
                  </div>
                </a-timeline-item>
              </a-timeline>
            </a-card>
          </a-tab-pane>
        </a-tabs>
      </div>
    </PageWrapper>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted } from 'vue';
import { PageWrapper } from '/@/components/Page';
import { Icon } from '/@/components/Icon';
import { 
  Card, 
  Row, 
  Col, 
  Select, 
  Descriptions, 
  Tabs, 
  Table, 
  Timeline,
  Tag,
  message
} from 'ant-design-vue';
import { getProductionPlanData, getFieldList, getProductionPlanListByBaseId, getProductionPlanListByFieldId } from '/@/api/rapeseed/production-plan';
import { getBaseList, getFieldListByBaseId } from '/@/api/rapeseed/base-field';

// 概览数据
const totalArea = ref(1250.5);
const targetYield = ref(320);
const soilFertility = ref(2.5);
const totalInvestment = ref(485600);

// 基地数据
const selectedBaseId = ref<string>('');
const baseList = ref([
  {
    id: '1',
    name: 'A区基地',
    area: 450.8,
    fieldCount: 4,
    location: '东经118.5°，北纬30.2°',
    manager: '张经理'
  },
  {
    id: '2',
    name: 'B区基地',
    area: 380.5,
    fieldCount: 3,
    location: '东经118.6°，北纬30.3°',
    manager: '李经理'
  },
  {
    id: '3',
    name: 'C区基地',
    area: 520.2,
    fieldCount: 5,
    location: '东经118.4°，北纬30.1°',
    manager: '王经理'
  }
]);

const selectedBase = computed(() => {
  return baseList.value.find(base => base.id === selectedBaseId.value);
});

// 地块数据
const selectedFieldId = ref<string>('');
const fieldList = ref([
  {
    id: '1',
    name: 'A区-1号地块',
    area: 125.5,
    soilType: '壤土',
    soilFertility: 2.5,
    contractor: '张三',
    contractNo: 'HT2023001',
    status: 'planted',
    targetYield: 320,
    investment: 48560,
    baseId: '1'
  },
  {
    id: '2',
    name: 'A区-2号地块',
    area: 98.3,
    soilType: '砂壤土',
    soilFertility: 2.2,
    contractor: '李四',
    contractNo: 'HT2023002',
    status: 'planned',
    targetYield: 310,
    investment: 38050,
    baseId: '1'
  },
  {
    id: '3',
    name: 'B区-1号地块',
    area: 156.8,
    soilType: '黏土',
    soilFertility: 2.8,
    contractor: '王五',
    contractNo: 'HT2023003',
    status: 'planted',
    targetYield: 330,
    investment: 60780,
    baseId: '2'
  },
  {
    id: '4',
    name: 'B区-2号地块',
    area: 110.2,
    soilType: '壤土',
    soilFertility: 2.3,
    contractor: '赵六',
    contractNo: 'HT2023004',
    status: 'pending',
    targetYield: 315,
    investment: 42720,
    baseId: '2'
  }
]);

const selectedField = computed(() => {
  return fieldList.value.find(field => field.id === selectedFieldId.value);
});

// 生产计划数据
const productionPlanCount = ref<number>(0);

// 种子投入数据
const seedInput = ref({
  type: '',
  amount: 0,
  cost: 0
});

// 肥料投入数据
const fertilizerInput = ref({
  type: '',
  amount: 0,
  cost: 0
});

// 农药投入数据
const pesticideInput = ref({
  type: '',
  amount: 0,
  cost: 0
});

// 当前选中的标签页
const activeTab = ref('seed');

// 种子投入数据
const seedData = ref([
  {
    id: '1',
    name: '高产油菜品种A',
    type: '杂交种',
    quantity: 125.5,
    unit: '公斤',
    price: 45.5
  },
  {
    id: '2',
    name: '抗病油菜品种B',
    type: '常规种',
    quantity: 0,
    unit: '公斤',
    price: 32.8
  }
]);

const seedColumns = [
  {
    title: '种子名称',
    dataIndex: 'name',
    key: 'name',
  },
  {
    title: '类型',
    dataIndex: 'type',
    key: 'type',
  },
  {
    title: '数量',
    dataIndex: 'quantity',
    key: 'quantity',
    customRender: ({ record }) => {
      return `${record.quantity} ${record.unit}`;
    }
  },
  {
    title: '单价',
    dataIndex: 'price',
    key: 'price',
    customRender: ({ record }) => {
      return `${record.price} 元/${record.unit}`;
    }
  },
  {
    title: '小计',
    dataIndex: 'totalCost',
    key: 'totalCost',
  }
];

const seedTotalCost = computed(() => {
  return seedData.value.reduce((total, item) => {
    return total + (item.quantity * item.price);
  }, 0).toFixed(2);
});

// 肥料投入数据
const fertilizerData = ref([
  {
    id: '1',
    name: '复合肥',
    type: '氮磷钾复合肥',
    npkRatio: '15-15-15',
    quantity: 1255,
    unit: '公斤',
    price: 3.5
  },
  {
    id: '2',
    name: '尿素',
    type: '氮肥',
    npkRatio: '46-0-0',
    quantity: 627.5,
    unit: '公斤',
    price: 2.8
  },
  {
    id: '3',
    name: '过磷酸钙',
    type: '磷肥',
    npkRatio: '0-16-0',
    quantity: 376.5,
    unit: '公斤',
    price: 1.2
  }
]);

const fertilizerColumns = [
  {
    title: '肥料名称',
    dataIndex: 'name',
    key: 'name',
  },
  {
    title: '类型',
    dataIndex: 'type',
    key: 'type',
  },
  {
    title: '氮磷钾比例',
    dataIndex: 'npkRatio',
    key: 'npkRatio',
  },
  {
    title: '数量',
    dataIndex: 'quantity',
    key: 'quantity',
    customRender: ({ record }) => {
      return `${record.quantity} ${record.unit}`;
    }
  },
  {
    title: '单价',
    dataIndex: 'price',
    key: 'price',
    customRender: ({ record }) => {
      return `${record.price} 元/${record.unit}`;
    }
  },
  {
    title: '小计',
    dataIndex: 'totalCost',
    key: 'totalCost',
  }
];

const fertilizerTotalCost = computed(() => {
  return fertilizerData.value.reduce((total, item) => {
    return total + (item.quantity * item.price);
  }, 0).toFixed(2);
});

// 农药投入数据
const pesticideData = ref([
  {
    id: '1',
    name: '吡虫啉',
    type: '杀虫剂',
    target: '蚜虫',
    quantity: 12.55,
    unit: '公斤',
    price: 85.5
  },
  {
    id: '2',
    name: '多菌灵',
    type: '杀菌剂',
    target: '菌核病',
    quantity: 6.28,
    unit: '公斤',
    price: 45.8
  },
  {
    id: '3',
    name: '草甘膦',
    type: '除草剂',
    target: '杂草',
    quantity: 25.1,
    unit: '公斤',
    price: 25.5
  }
]);

const pesticideColumns = [
  {
    title: '农药名称',
    dataIndex: 'name',
    key: 'name',
  },
  {
    title: '类型',
    dataIndex: 'type',
    key: 'type',
  },
  {
    title: '防治对象',
    dataIndex: 'target',
    key: 'target',
  },
  {
    title: '数量',
    dataIndex: 'quantity',
    key: 'quantity',
    customRender: ({ record }) => {
      return `${record.quantity} ${record.unit}`;
    }
  },
  {
    title: '单价',
    dataIndex: 'price',
    key: 'price',
    customRender: ({ record }) => {
      return `${record.price} 元/${record.unit}`;
    }
  },
  {
    title: '小计',
    dataIndex: 'totalCost',
    key: 'totalCost',
  }
];

const pesticideTotalCost = computed(() => {
  return pesticideData.value.reduce((total, item) => {
    return total + (item.quantity * item.price);
  }, 0).toFixed(2);
});

// 数据来源
const contractCount = ref(15);

// 方法
function handleBaseChange(value: string) {
  console.log('Selected base:', value);
  // 清空地块选择
  selectedFieldId.value = '';
  // 加载该基地下的地块列表
  loadFieldsByBaseId(value);
  // 加载该基地的生产计划数据
  loadProductionPlanData();
}

function handleFieldChange(value: string) {
  console.log('Selected field:', value);
  // 加载该地块的生产计划数据
  loadProductionPlanData();
}

// 获取地块状态颜色
function getFieldStatusColor(status: string) {
  const statusMap = {
    'planted': 'green',
    'planned': 'blue',
    'pending': 'orange'
  };
  return statusMap[status] || 'default';
}

// 获取地块状态文本
function getFieldStatusText(status: string) {
  const statusMap = {
    'planted': '已种植',
    'planned': '已规划',
    'pending': '待规划'
  };
  return statusMap[status] || '未知';
}

// 生命周期
onMounted(() => {
  // 加载基地列表
  loadBaseList();
  
  // 加载生产计划数据
  loadProductionPlanData();
});

// 加载基地列表
async function loadBaseList() {
  try {
    // const data = await getBaseList();
    // baseList.value = data;
    // 默认选择第一个基地
    if (baseList.value.length > 0) {
      selectedBaseId.value = baseList.value[0].id;
      loadFieldsByBaseId(selectedBaseId.value);
    }
  } catch (error) {
    message.error('加载基地列表失败');
  }
}

// 根据基地ID加载地块列表
async function loadFieldsByBaseId(baseId: string) {
  try {
    // const data = await getFieldListByBaseId(baseId);
    // fieldList.value = data;
    
    // 筛选当前基地下的地块
    fieldList.value = [
      {
        id: '1',
        name: 'A区-1号地块',
        area: 125.5,
        soilType: '壤土',
        soilFertility: 2.5,
        contractor: '张三',
        contractNo: 'HT2023001',
        status: 'planted',
        targetYield: 320,
        investment: 48560,
        baseId: baseId
      },
      {
        id: '2',
        name: 'A区-2号地块',
        area: 98.3,
        soilType: '砂壤土',
        soilFertility: 2.2,
        contractor: '李四',
        contractNo: 'HT2023002',
        status: 'planned',
        targetYield: 310,
        investment: 38050,
        baseId: baseId
      }
    ].filter(field => field.baseId === baseId);
    
    // 默认选择第一个地块
    if (fieldList.value.length > 0) {
      selectedFieldId.value = fieldList.value[0].id;
    }
  } catch (error) {
    message.error('加载地块列表失败');
  }
}

// 加载生产计划数据
async function loadProductionPlanData() {
  try {
    let params = {};
    
    // 如果已选择基地，则加载该基地的生产计划数据
    if (selectedBaseId.value) {
      // const data = await getProductionPlanListByBaseId(selectedBaseId.value);
      // 更新数据
    }
    
    // 如果已选择地块，则加载该地块的生产计划数据
    if (selectedFieldId.value) {
      // const data = await getProductionPlanListByFieldId(selectedFieldId.value);
      // 更新数据
    }
    
    // 默认加载所有生产计划数据
    // const data = await getProductionPlanData(params);
    // 更新数据
  } catch (error) {
    message.error('加载生产计划数据失败');
  }
}
</script>

<style lang="less" scoped>
.production-plan-dashboard {
  padding: 24px;
  
  .overview-section {
    margin-bottom: 24px;
  }

  .overview-card {
    height: 100%;
    
    .card-content {
      display: flex;
      align-items: center;
      
      .card-icon {
        margin-right: 16px;
      }
      
      .card-info {
        .card-title {
          font-size: 14px;
          color: rgba(0, 0, 0, 0.65);
          margin-bottom: 8px;
        }
        
        .card-value {
          font-size: 24px;
          font-weight: 500;
          color: rgba(0, 0, 0, 0.85);
          
          .unit {
            font-size: 14px;
            color: rgba(0, 0, 0, 0.65);
            margin-left: 4px;
          }
        }
      }
    }
  }

  .overview-content {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .overview-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .overview-label {
    color: rgba(0, 0, 0, 0.65);
    font-size: 14px;
  }

  .overview-value {
    font-weight: 500;
    font-size: 14px;
    color: rgba(0, 0, 0, 0.85);
  }
  
  .field-section {
    margin-bottom: 24px;
    
    .field-card {
      .base-selector, .field-selector {
        width: 100%;
        margin-bottom: 16px;
      }
      
      .base-info, .field-info {
        margin-top: 16px;
      }
    }
  }
  
  .production-details-section {
    .ant-table-summary-row {
      .summary-label {
        font-weight: 500;
        text-align: right;
      }
      
      .summary-value {
        font-weight: 500;
      }
    }
    
    .timeline-content {
      h4 {
        margin-bottom: 8px;
      }
      
      p {
        margin-bottom: 8px;
        color: rgba(0, 0, 0, 0.65);
      }
    }
  }
}
</style>
