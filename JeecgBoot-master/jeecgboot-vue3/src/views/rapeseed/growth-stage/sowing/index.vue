<template>
  <div class="sowing-page">
    <PageWrapper title="播种管理">
      <div class="sowing-content">
        <a-row :gutter="24">
          <a-col :span="8">
            <a-card title="播种进度">
              <a-progress :percent="75" status="active" :stroke-color="progressColor" />
              <div class="progress-info">
                <p>已完成面积: 937.5 亩</p>
                <p>总计划面积: 1,250 亩</p>
                <p>预计完成时间: 3天后</p>
              </div>
            </a-card>
          </a-col>
          <a-col :span="8">
            <a-card title="播种质量评估">
              <div class="quality-metrics">
                <a-row :gutter="16">
                  <a-col :span="12">
                    <a-statistic title="播种深度" :value="seedingDepth" suffix="cm" />
                  </a-col>
                  <a-col :span="12">
                    <a-statistic title="行距" :value="rowSpacing" suffix="cm" />
                  </a-col>
                </a-row>
                <a-row :gutter="16" style="margin-top: 16px">
                  <a-col :span="12">
                    <a-statistic title="株距" :value="plantSpacing" suffix="cm" />
                  </a-col>
                  <a-col :span="12">
                    <a-statistic title="播种量" :value="seedingRate" suffix="kg/亩" />
                  </a-col>
                </a-row>
              </div>
            </a-card>
          </a-col>
          <a-col :span="8">
            <a-card title="天气状况">
              <div class="weather-info">
                <a-row :gutter="16">
                  <a-col :span="12">
                    <a-statistic title="温度" :value="temperature" suffix="°C" />
                  </a-col>
                  <a-col :span="12">
                    <a-statistic title="湿度" :value="humidity" suffix="%" />
                  </a-col>
                </a-row>
                <a-row :gutter="16" style="margin-top: 16px">
                  <a-col :span="12">
                    <a-statistic title="风速" :value="windSpeed" suffix="m/s" />
                  </a-col>
                  <a-col :span="12">
                    <a-statistic title="降雨概率" :value="rainProbability" suffix="%" />
                  </a-col>
                </a-row>
              </div>
            </a-card>
          </a-col>
        </a-row>

        <a-row :gutter="24" style="margin-top: 24px">
          <a-col :span="16">
            <a-card title="播种记录">
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
                  <a-button type="primary" @click="handleAdd">新增播种记录</a-button>
                </template>
              </BasicTable>
            </a-card>
          </a-col>
          <a-col :span="8">
            <a-card title="播种计划">
              <a-timeline>
                <a-timeline-item v-for="(item, index) in sowingPlan" :key="index" :color="item.color">
                  <p>{{ item.date }}</p>
                  <p>{{ item.task }}</p>
                  <p>{{ item.area }} 亩</p>
                </a-timeline-item>
              </a-timeline>
            </a-card>
          </a-col>
        </a-row>
      </div>
    </PageWrapper>

    <SowingModal @register="registerModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue';
import { PageWrapper } from '/@/components/Page';
import { BasicTable, useTable, TableAction } from '/@/components/Table';
import { Card, Row, Col, Progress, Statistic, Timeline, Button, message } from 'ant-design-vue';
import { useModal } from '/@/components/Modal';
import SowingModal from './SowingModal.vue';
import { getSowingList, deleteSowing } from '/@/api/rapeseed/growth-stage/sowing';
import { columns } from './data';

const ACard = Card;
const ARow = Row;
const ACol = Col;
const AProgress = Progress;
const AStatistic = Statistic;
const ATimeline = Timeline;
const ATimelineItem = Timeline.Item;
const AButton = Button;

const progressColor = {
  '0%': '#108ee9',
  '100%': '#87d068',
};

const seedingDepth = ref(2.5);
const rowSpacing = ref(30);
const plantSpacing = ref(15);
const seedingRate = ref(0.3);

const temperature = ref(18);
const humidity = ref(65);
const windSpeed = ref(3.2);
const rainProbability = ref(20);

const sowingPlan = ref([
  {
    date: '2024-03-10',
    task: '东区播种',
    area: 500,
    color: 'green',
  },
  {
    date: '2024-03-12',
    task: '西区播种',
    area: 437.5,
    color: 'blue',
  },
  {
    date: '2024-03-15',
    task: '南区播种',
    area: 312.5,
    color: 'gray',
  },
]);

const [registerTable, { reload }] = useTable({
  title: '播种记录列表',
  columns,
  api: getSowingList,
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
        field: 'status',
        label: '状态',
        component: 'Select',
        componentProps: {
          options: [
            { label: '计划中', value: '0' },
            { label: '进行中', value: '1' },
            { label: '已完成', value: '2' },
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
    await deleteSowing(record.id);
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
.sowing-page {
  .sowing-content {
    .progress-info {
      margin-top: 16px;
      
      p {
        margin-bottom: 8px;
      }
    }
    
    .quality-metrics,
    .weather-info {
      padding: 10px 0;
    }
  }
}
</style>