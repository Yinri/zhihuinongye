<template>
  <div class="seedling-page">
    <PageWrapper title="苗期管理">
      <div class="seedling-content">
        <a-row :gutter="24">
          <a-col :span="8">
            <a-card title="苗期生长状况">
              <div class="growth-status">
                <a-row :gutter="16">
                  <a-col :span="12">
                    <a-statistic title="平均株高" :value="plantHeight" suffix="cm" />
                  </a-col>
                  <a-col :span="12">
                    <a-statistic title="叶片数" :value="leafCount" suffix="片" />
                  </a-col>
                </a-row>
                <a-row :gutter="16" style="margin-top: 16px">
                  <a-col :span="12">
                    <a-statistic title="成活率" :value="survivalRate" suffix="%" />
                  </a-col>
                  <a-col :span="12">
                    <a-statistic title="生长天数" :value="growthDays" suffix="天" />
                  </a-col>
                </a-row>
              </div>
            </a-card>
          </a-col>
          <a-col :span="8">
            <a-card title="苗期健康评估">
              <div class="health-assessment">
                <a-progress type="dashboard" :percent="healthScore" :stroke-color="healthColor" />
                <div class="health-info">
                  <p>健康状况: {{ healthStatus }}</p>
                  <p>主要问题: {{ mainIssues }}</p>
                </div>
              </div>
            </a-card>
          </a-col>
          <a-col :span="8">
            <a-card title="环境监测">
              <div class="environment-monitoring">
                <a-row :gutter="16">
                  <a-col :span="12">
                    <a-statistic title="土壤温度" :value="soilTemp" suffix="°C" />
                  </a-col>
                  <a-col :span="12">
                    <a-statistic title="土壤湿度" :value="soilMoisture" suffix="%" />
                  </a-col>
                </a-row>
                <a-row :gutter="16" style="margin-top: 16px">
                  <a-col :span="12">
                    <a-statistic title="光照强度" :value="lightIntensity" suffix="lux" />
                  </a-col>
                  <a-col :span="12">
                    <a-statistic title="土壤pH值" :value="soilPh" />
                  </a-col>
                </a-row>
              </div>
            </a-card>
          </a-col>
        </a-row>

        <a-row :gutter="24" style="margin-top: 24px">
          <a-col :span="16">
            <a-card title="苗期管理记录">
              <BasicTable @register="registerTable">
                <template #bodyCell="{ column, record }">
                  <template v-if="column.dataIndex === 'action'">
                    <TableAction
                      :actions="[
                        {
                          icon: 'clarity:note-edit-line',
                          tooltip: '编辑',
                          onClick: handleEdit.bind(null, record),
                        },
                        {
                          icon: 'ant-design:delete-outlined',
                          color: 'error',
                          tooltip: '删除',
                          popConfirm: {
                            title: '是否确认删除',
                            confirm: handleDelete.bind(null, record),
                          },
                        },
                      ]"
                    />
                  </template>
                </template>
                <template #toolbar>
                  <a-button type="primary" @click="handleAdd">新增管理记录</a-button>
                </template>
              </BasicTable>
            </a-card>
          </a-col>
          <a-col :span="8">
            <a-card title="苗期管理计划">
              <a-timeline>
                <a-timeline-item v-for="(item, index) in managementPlan" :key="index" :color="item.color">
                  <p>{{ item.date }}</p>
                  <p>{{ item.task }}</p>
                  <p>{{ item.description }}</p>
                </a-timeline-item>
              </a-timeline>
            </a-card>
          </a-col>
        </a-row>
      </div>
    </PageWrapper>

    <SeedlingModal @register="registerModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted } from 'vue';
import { PageWrapper } from '/@/components/Page';
import { BasicTable, useTable, TableAction } from '/@/components/Table';
import { Card, Row, Col, Progress, Statistic, Timeline, Button, message } from 'ant-design-vue';
import { useModal } from '/@/components/Modal';
import SeedlingModal from './SeedlingModal.vue';
import { getSeedlingList, deleteSeedling } from '/@/api/rapeseed/growth-stage/seedling';
import { columns } from './data';

const ACard = Card;
const ARow = Row;
const ACol = Col;
const AProgress = Progress;
const AStatistic = Statistic;
const ATimeline = Timeline;
const ATimelineItem = Timeline.Item;
const AButton = Button;

const plantHeight = ref(12.5);
const leafCount = ref(4);
const survivalRate = ref(92);
const growthDays = ref(18);

const healthScore = ref(85);
const healthStatus = computed(() => {
  if (healthScore.value >= 90) return '优秀';
  if (healthScore.value >= 75) return '良好';
  if (healthScore.value >= 60) return '一般';
  return '较差';
});
const mainIssues = ref('轻微缺肥');

const healthColor = computed(() => {
  if (healthScore.value >= 90) return '#52c41a';
  if (healthScore.value >= 75) return '#1890ff';
  if (healthScore.value >= 60) return '#faad14';
  return '#f5222d';
});

const soilTemp = ref(16.5);
const soilMoisture = ref(65);
const lightIntensity = ref(25000);
const soilPh = ref(6.5);

const managementPlan = ref([
  {
    date: '2024-03-15',
    task: '第一次间苗',
    description: '去除弱苗，保留壮苗',
    color: 'green',
  },
  {
    date: '2024-03-20',
    task: '第一次追肥',
    description: '施用氮肥，促进生长',
    color: 'blue',
  },
  {
    date: '2024-03-25',
    task: '病虫害防治',
    description: '预防蚜虫和菜青虫',
    color: 'orange',
  },
  {
    date: '2024-03-30',
    task: '第二次间苗',
    description: '调整种植密度',
    color: 'gray',
  },
]);

const [registerTable, { reload }] = useTable({
  title: '苗期管理记录列表',
  columns,
  api: getSeedlingList,
  showIndexColumn: false,
  useSearchForm: true,
  formConfig: {
    labelWidth: 100,
    schemas: [
      {
        field: 'field',
        label: '田块',
        component: 'Input',
      },
      {
        field: 'managementType',
        label: '管理类型',
        component: 'Select',
        componentProps: {
          options: [
            { label: '间苗', value: '0' },
            { label: '追肥', value: '1' },
            { label: '病虫害防治', value: '2' },
            { label: '灌溉', value: '3' },
          ],
        },
      },
    ],
  },
  actionColumn: {
    width: 150,
    title: '操作',
    dataIndex: 'action',
  },
});

const [registerModal, { openModal }] = useModal();

function handleAdd() {
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
  try {
    await deleteSeedling(record.id);
    message.success('删除成功');
    reload();
  } catch (error) {
    message.error('删除失败');
  }
}

function handleSuccess() {
  reload();
}

onMounted(() => {
  // 页面加载时获取数据
});
</script>

<style lang="less" scoped>
.seedling-page {
  .seedling-content {
    .growth-status,
    .environment-monitoring {
      padding: 10px 0;
    }
    
    .health-assessment {
      text-align: center;
      
      .health-info {
        margin-top: 16px;
        
        p {
          margin-bottom: 8px;
        }
      }
    }
  }
}
</style>