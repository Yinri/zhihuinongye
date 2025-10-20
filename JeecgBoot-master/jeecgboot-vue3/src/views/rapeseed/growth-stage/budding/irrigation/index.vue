<template>
  <div class="irrigation-page">
    <PageWrapper title="智慧灌溉管理">
      <a-row :gutter="24">
        <a-col :span="8">
          <a-card title="灌溉系统状态">
            <div class="system-status">
              <a-row :gutter="16">
                <a-col :span="12">
                  <a-statistic title="系统状态" value="正常运行" value-style="color: #3f8600" />
                </a-col>
                <a-col :span="12">
                  <a-statistic title="上次灌溉" :value="lastIrrigation" />
                </a-col>
              </a-row>
              <a-divider />
              <a-row :gutter="16">
                <a-col :span="12">
                  <a-statistic title="土壤湿度" :value="soilMoisture" suffix="%" />
                </a-col>
                <a-col :span="12">
                  <a-statistic title="预计下次灌溉" :value="nextIrrigation" />
                </a-col>
              </a-row>
            </div>
          </a-card>
        </a-col>
        <a-col :span="16">
          <a-card title="灌溉计划">
            <a-timeline>
              <a-timeline-item v-for="(item, index) in irrigationPlan" :key="index" :color="item.color">
                <template v-if="index === 0">
                  <a-tag color="green">当前阶段</a-tag>
                </template>
                <p>{{ item.time }}</p>
                <p>{{ item.description }}</p>
                <p v-if="item.amount">灌溉量: {{ item.amount }}</p>
              </a-timeline-item>
            </a-timeline>
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="24" style="margin-top: 24px">
        <a-col :span="12">
          <a-card title="土壤湿度趋势">
            <div ref="chartRef" style="height: 300px"></div>
          </a-card>
        </a-col>
        <a-col :span="12">
          <a-card title="灌溉记录">
            <BasicTable @register="registerTable" :pagination="false">
              <template #bodyCell="{ column, record }">
                <template v-if="column.dataIndex === 'action'">
                  <a-button type="link" size="small" @click="viewDetail(record)">查看详情</a-button>
                </template>
              </template>
            </BasicTable>
          </a-card>
        </a-col>
      </a-row>

      <a-row style="margin-top: 24px">
        <a-col :span="24">
          <a-card title="灌溉控制">
            <a-row :gutter="16">
              <a-col :span="6">
                <a-button type="primary" size="large" @click="startIrrigation" :loading="irrigationLoading">
                  开始灌溉
                </a-button>
              </a-col>
              <a-col :span="6">
                <a-button size="large" @click="stopIrrigation" :disabled="!irrigationLoading">
                  停止灌溉
                </a-button>
              </a-col>
              <a-col :span="6">
                <a-button size="large" @click="setSchedule">
                  设置计划
                </a-button>
              </a-col>
              <a-col :span="6">
                <a-button size="large" @click="viewHistory">
                  历史记录
                </a-button>
              </a-col>
            </a-row>
          </a-card>
        </a-col>
      </a-row>
    </PageWrapper>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, nextTick } from 'vue';
import { PageWrapper } from '/@/components/Page';
import { BasicTable, useTable } from '/@/components/Table';
import { Card, Row, Col, Statistic, Divider, Timeline, Tag, Button } from 'ant-design-vue';
import { useMessage } from '/@/hooks/web/useMessage';
import { getIrrigationStatus, getIrrigationPlan, getIrrigationHistory, startIrrigation as startIrrigationApi, stopIrrigation as stopIrrigationApi } from '/@/api/rapeseed/growth-stage/irrigation';
import { useECharts } from '/@/hooks/web/useECharts';

const ACard = Card;
const ARow = Row;
const ACol = Col;
const AStatistic = Statistic;
const ADivider = Divider;
const ATimeline = Timeline;
const ATimelineItem = Timeline.Item;
const ATag = Tag;
const AButton = Button;

const { createMessage } = useMessage();
const chartRef = ref<HTMLDivElement | null>(null);
const { setOptions } = useECharts(chartRef);

const irrigationLoading = ref(false);
const lastIrrigation = ref('3天前');
const nextIrrigation = ref('2天后');
const soilMoisture = ref(65);

const irrigationPlan = ref([
  {
    time: '2024-03-15 08:00',
    description: '蕾薹期第一次灌溉',
    amount: '30立方米/亩',
    color: 'green',
  },
  {
    time: '2024-03-22 08:00',
    description: '蕾薹期第二次灌溉',
    amount: '25立方米/亩',
    color: 'blue',
  },
  {
    time: '2024-03-29 08:00',
    description: '蕾薹期第三次灌溉',
    amount: '25立方米/亩',
    color: 'gray',
  },
]);

const [registerTable, { setTableData }] = useTable({
  columns: [
    {
      title: '灌溉时间',
      dataIndex: 'irrigationTime',
      width: 150,
    },
    {
      title: '灌溉量',
      dataIndex: 'amount',
      width: 120,
      customRender: ({ record }) => `${record.amount} 立方米/亩`,
    },
    {
      title: '操作人',
      dataIndex: 'operator',
      width: 100,
    },
    {
      title: '备注',
      dataIndex: 'remark',
    },
    {
      title: '操作',
      dataIndex: 'action',
      width: 100,
    },
  ],
  dataSource: [],
  pagination: false,
});

// 开始灌溉
async function startIrrigation() {
  irrigationLoading.value = true;
  try {
    await startIrrigationApi();
    createMessage.success('灌溉已开始');
  } catch (error) {
    createMessage.error('启动灌溉失败');
  } finally {
    irrigationLoading.value = false;
  }
}

// 停止灌溉
async function stopIrrigation() {
  try {
    await stopIrrigationApi();
    irrigationLoading.value = false;
    createMessage.success('灌溉已停止');
  } catch (error) {
    createMessage.error('停止灌溉失败');
  }
}

// 设置计划
function setSchedule() {
  createMessage.info('打开设置计划弹窗');
}

// 查看历史记录
function viewHistory() {
  createMessage.info('打开历史记录页面');
}

// 查看详情
function viewDetail(record) {
  createMessage.info(`查看灌溉记录详情: ${record.id}`);
}

// 初始化图表
function initChart() {
  setOptions({
    tooltip: {
      trigger: 'axis',
    },
    xAxis: {
      type: 'category',
      data: ['3-10', '3-11', '3-12', '3-13', '3-14', '3-15', '3-16'],
    },
    yAxis: {
      type: 'value',
      name: '湿度(%)',
      min: 0,
      max: 100,
    },
    series: [
      {
        name: '土壤湿度',
        type: 'line',
        data: [60, 62, 58, 55, 65, 68, 65],
        smooth: true,
        areaStyle: {},
      },
    ],
  });
}

onMounted(async () => {
  // 获取灌溉状态
  try {
    const statusData = await getIrrigationStatus();
    lastIrrigation.value = statusData.lastIrrigation;
    nextIrrigation.value = statusData.nextIrrigation;
    soilMoisture.value = statusData.soilMoisture;
  } catch (error) {
    console.error('获取灌溉状态失败', error);
  }

  // 获取灌溉计划
  try {
    const planData = await getIrrigationPlan();
    irrigationPlan.value = planData;
  } catch (error) {
    console.error('获取灌溉计划失败', error);
  }

  // 获取灌溉记录
  try {
    const historyData = await getIrrigationHistory();
    setTableData(historyData);
  } catch (error) {
    console.error('获取灌溉记录失败', error);
  }

  // 初始化图表
  nextTick(() => {
    initChart();
  });
});
</script>

<style lang="less" scoped>
.irrigation-page {
  .system-status {
    padding: 10px 0;
  }
}
</style>