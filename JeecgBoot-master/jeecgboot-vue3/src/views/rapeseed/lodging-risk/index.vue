<template>
  <div class="lodging-risk-page">
    <!-- 核心预警区域 -->
    <a-card :bordered="false" class="risk-forecast-card">
      <template #title>
        <div class="card-title">
          <Icon icon="ant-design:alert-outlined" />
          倒伏风险预警（未来7天）
        </div>
      </template>
      
      <!-- 风险预警卡片容器 -->
      <div class="risk-forecast-container">
        <div 
          v-for="(day, index) in riskForecastData" 
          :key="index"
          :class="['risk-day-card', `risk-${day.riskLevel}`]"
        >
          <div class="day-header">
            <div class="day-date">{{ day.date }}</div>
            <div class="day-weekday">{{ day.weekday }}</div>
          </div>
          <div :class="['risk-level-badge', day.riskLevel]">{{ day.riskLevelText }}</div>
          <div class="risk-details">
            <div class="risk-probability">
              <span class="probability-value">{{ day.probability }}%</span>
              <span class="probability-label">概率</span>
            </div>
            <div class="risk-time">{{ day.timePeriod }}</div>
            <div class="risk-factors">
              <span class="factor-item">
                <Icon icon="ant-design:thunderbolt-outlined" :class="`${day.riskLevel}-icon`" />
                {{ day.windLevel }}级
              </span>
              <span class="factor-item">
                <Icon icon="ant-design:cloud-outlined" class="info-icon" />
                {{ day.humidity }}%
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
      
      <div class="history-list">
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
    </a-card>


    <!-- 加载状态提示 -->
    <a-card :bordered="false" v-if="loading" class="loading-card">
      <a-skeleton active :paragraph="{ rows: 5 }" />
    </a-card>

    <!-- 表单弹窗 -->
    <LodgingRiskModal @register="registerModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue';
import { BasicTable, useTable, TableAction } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useMessage } from '/@/hooks/web/useMessage';
import { Icon } from '/@/components/Icon';
import { columns, searchFormSchema } from './lodgingRisk.data';
import { getLodgingRiskList, deleteLodgingRisk, exportLodgingRisk, importLodgingRisk } from './lodgingRisk.api';
import LodgingRiskModal from './LodgingRiskModal.vue';

const { createMessage } = useMessage();

// 加载状态
const loading = ref(false);

// 防控建议折叠面板激活状态
const activeKey = ref(['1']);

// 防控建议数据
const suggestionsData = reactive([
  {
    title: '紧急措施',
    icon: 'ExclamationCircleOutlined',
    color: '#ff4d4f',
    items: [
      {
        title: '立即加固',
        desc: '使用竹竿或支架对高风险区域进行加固处理'
      },
      {
        title: '排水处理',
        desc: '清理田间排水沟，确保积水及时排出'
      },
      {
        title: '暂停施肥',
        desc: '暂停氮肥施用，避免植株徒长增加倒伏风险'
      }
    ]
  },
  {
    title: '后续管理',
    icon: 'ControlOutlined',
    color: '#1890ff',
    items: [
      {
        title: '定期巡查',
        desc: '每日早晚各巡查一次，重点关注高风险区域'
      },
      {
        title: '调整种植密度',
        desc: '下季种植时适当降低密度，增强抗倒伏能力'
      },
      {
        title: '品种选择',
        desc: '选择抗倒伏性强的品种进行种植'
      }
    ]
  }
]);

// 历史记录数据
const historyData = reactive([
  {
    id: 1,
    date: '2024-03-15',
    title: '倒伏事件',
    desc: '东南区域发生中度倒伏，影响面积约2亩',
    type: 'warning',
    actions: ['已处理', '已记录']
  },
  {
    id: 2,
    date: '2024-03-10',
    title: '与周边地块对比',
    desc: '本地块抗倒伏能力低于周边平均水平15%',
    type: 'info',
    actions: ['查看详情', '生成报告']
  }
]);

// 风险预警数据
const riskForecastData = ref([
  {
    date: '3月10日',
    weekday: '周一',
    riskLevel: 'high',
    riskLevelText: '高风险',
    probability: 78,
    timePeriod: '午后至夜间',
    windLevel: 6,
    humidity: 85
  },
  {
    date: '3月11日',
    weekday: '周二',
    riskLevel: 'high',
    riskLevelText: '高风险',
    probability: 72,
    timePeriod: '全天',
    windLevel: 7,
    humidity: 90
  },
  {
    date: '3月12日',
    weekday: '周三',
    riskLevel: 'medium',
    riskLevelText: '中风险',
    probability: 55,
    timePeriod: '上午',
    windLevel: 5,
    humidity: 82
  },
  {
    date: '3月13日',
    weekday: '周四',
    riskLevel: 'medium',
    riskLevelText: '中风险',
    probability: 48,
    timePeriod: '无显著',
    windLevel: 4,
    humidity: 78
  },
  {
    date: '3月14日',
    weekday: '周五',
    riskLevel: 'low',
    riskLevelText: '低风险',
    probability: 25,
    timePeriod: '无',
    windLevel: 3,
    humidity: 72
  },
  {
    date: '3月15日',
    weekday: '周六',
    riskLevel: 'low',
    riskLevelText: '低风险',
    probability: 18,
    timePeriod: '无',
    windLevel: 3,
    humidity: 68
  },
  {
    date: '3月16日',
    weekday: '周日',
    riskLevel: 'low',
    riskLevelText: '低风险',
    probability: 15,
    timePeriod: '无',
    windLevel: 2,
    humidity: 65
  }
]);

const [registerModal, { openModal }] = useModal();
const searchInfo = reactive<Recordable>({});
const fileList = ref<any[]>([]);

const [registerTable, { reload, getSelectRows, dataSource }] = useTable({
  title: '倒伏风险预警管理',
  api: getLodgingRiskList,
  columns,
  formConfig: {
    labelWidth: 120,
    schemas: searchFormSchema,
    autoSubmitOnEnter: true,
  },
  useSearchForm: true,
  showTableSetting: true,
  bordered: true,
  showIndexColumn: false,
  actionColumn: {
    width: 80,
    title: '操作',
    dataIndex: 'action',
    fixed: 'right',
  },
  beforeFetch: (params) => {
    return params;
  },
});

/**
 * 加载倒伏风险数据
 */
async function loadLodgingRiskData() {
  try {
    loading.value = true;
    // 重新加载数据
    await reload();
  } catch (error) {
    console.error('加载倒伏风险数据失败:', error);
    createMessage.error('加载倒伏风险数据失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}

function handleCreate() {
  openModal(true, {
    isUpdate: false,
  });
}

function handleEdit(record: Recordable) {
  openModal(true, {
    record,
    isUpdate: true,
  });
}

async function handleDelete(record: Recordable) {
  await deleteLodgingRisk(record.id);
  createMessage.success('删除成功');
  reload();
}

function handleSuccess() {
  createMessage.success('操作成功');
  reload();
}

async function handleExport() {
  const data = await getLodgingRiskList(searchInfo);
  exportLodgingRisk(data);
}

function handleRemove() {
  fileList.value = [];
}

function beforeUpload(file) {
  fileList.value = [...fileList.value, file];
  return false;
}

async function handleImport() {
  if (fileList.value.length === 0) {
    createMessage.warning('请选择要导入的文件');
    return;
  }

  const formData = new FormData();
  fileList.value.forEach((file) => {
    formData.append('file', file);
  });

  try {
    await importLodgingRisk(formData);
    createMessage.success('导入成功');
    reload();
    fileList.value = [];
  } catch (error) {
    createMessage.error('导入失败');
  }
}

// 组件挂载时初始化数据
onMounted(() => {
  // 初始化加载数据
  loadLodgingRiskData();
});
</script>

<style lang="less" scoped>
.lodging-risk-page {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 64px);
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
    gap: 8px;
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
    
    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      transform: translateY(-2px);
    }
    
    .day-header {
      padding: 8px 12px;
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
      padding: 4px 0;
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
      padding: 8px 12px;
      
      .risk-probability {
        display: flex;
        flex-direction: column;
        align-items: center;
        margin-bottom: 8px;
        
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
        margin-bottom: 4px;
        text-align: center;
      }
      
      .risk-factors {
        display: flex;
        justify-content: space-around;
        gap: 8px;
        margin-top: 8px;
        
        .factor-item {
          display: flex;
          align-items: center;
          gap: 2px;
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
      }
    }
  }
}

// 间距调整类
.mb-8 {
  margin-bottom: 8px;
}

.table-card {
  margin-top: 16px;
  
  .table-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 500;
  }
}

.empty-card,
.loading-card {
  margin-top: 16px;
  text-align: center;
}

// 响应式布局
@media (max-width: 1200px) {
  .combined-selection-timeline-card {
    .ant-col {
      margin-bottom: 16px;
    }
    
    :deep(.ant-row) {
      flex-direction: column;
      
      .ant-col {
        width: 100% !important;
        margin-bottom: 16px;
        
        &:last-child {
          margin-bottom: 0;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .lodging-risk-page {
    padding: 16px;
  }
  
  .combined-selection-timeline-card {
  :deep(.ant-card-body) {
    padding-left: 8px;
    padding-right: 8px;
    padding-top: 5px;
    padding-bottom: 5px;
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
