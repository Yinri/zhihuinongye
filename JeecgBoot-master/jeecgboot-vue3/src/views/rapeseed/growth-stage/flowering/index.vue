<template>
  <PageWrapper>
    <div class="rapeseed-flowering">
      <!-- 统计卡片 -->
      <Row :gutter="24" class="mb-4">
        <Col :span="6">
          <Card>
            <template #title>
              <span>开花期进度</span>
            </template>
            <div class="stat-card">
              <Progress :percent="floweringProgress" :stroke-color="progressColor" />
              <div class="stat-text">当前阶段已完成 {{ floweringProgress }}%</div>
            </div>
          </Card>
        </Col>
        <Col :span="6">
          <Card>
            <template #title>
              <span>开花状况</span>
            </template>
            <div class="stat-card">
              <Statistic title="开花率" :value="floweringStatus.rate" suffix="%" />
              <Statistic title="主花序长度" :value="floweringStatus.mainInflorescenceLength" suffix="cm" />
            </div>
          </Card>
        </Col>
        <Col :span="6">
          <Card>
            <template #title>
              <span>授粉情况</span>
            </template>
            <div class="stat-card">
              <Statistic title="授粉率" :value="pollination.rate" suffix="%" />
              <Statistic title="蜜蜂活动量" :value="pollination.beeActivity" suffix="只/亩" />
            </div>
          </Card>
        </Col>
        <Col :span="6">
          <Card>
            <template #title>
              <span>环境状况</span>
            </template>
            <div class="stat-card">
              <Statistic title="温度" :value="environment.temperature" suffix="°C" />
              <Statistic title="湿度" :value="environment.humidity" suffix="%" />
            </div>
          </Card>
        </Col>
      </Row>

      <!-- 开花期生长趋势图表 -->
      <Card title="开花期生长趋势" class="mb-4">
        <div ref="chartRef" :style="{ height: '300px', width: '100%' }"></div>
      </Card>

      <!-- 开花期管理记录表格 -->
      <Card title="开花期管理记录" class="mb-4">
        <BasicTable @register="registerTable">
          <template #toolbar>
            <a-button type="primary" @click="handleCreate"> 新增记录 </a-button>
          </template>
          <template #action="{ record }">
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
        </BasicTable>
      </Card>

      <!-- 开花期管理计划时间线 -->
      <Card title="开花期管理计划">
        <Timeline>
          <TimelineItem v-for="(item, index) in managementPlan" :key="index" :color="item.color">
            <div class="timeline-content">
              <div class="timeline-title">{{ item.title }}</div>
              <div class="timeline-date">{{ item.date }}</div>
              <div class="timeline-desc">{{ item.description }}</div>
            </div>
          </TimelineItem>
        </Timeline>
      </Card>
    </div>

    <!-- 新增/编辑弹窗 -->
    <FloweringModal @register="registerModal" @success="handleSuccess" />
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { ref, onMounted, nextTick } from 'vue';
  import { Progress, Statistic, Timeline, TimelineItem } from 'ant-design-vue';
  import { PageWrapper } from '/@/components/Page';
  import { Card, Row, Col } from 'ant-design-vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useECharts } from '/@/hooks/web/useECharts';
  import FloweringModal from './FloweringModal.vue';
  import { columns, searchFormSchema } from './data';
  import { getFloweringList, deleteFlowering, getFloweringProgress, getFloweringStatus, getPollination, getEnvironment, getFloweringManagementPlan, getFloweringTrend } from '/@/api/rapeseed/growth-stage/flowering';

  const chartRef = ref<HTMLDivElement | null>(null);
  const { setOptions } = useECharts(chartRef);

  // 开花期进度
  const floweringProgress = ref(75);
  const progressColor = ref('#52c41a');

  // 开花状况
  const floweringStatus = ref({
    rate: 85.6,
    mainInflorescenceLength: 12.5,
  });

  // 授粉情况
  const pollination = ref({
    rate: 78.3,
    beeActivity: 125.6,
  });

  // 环境状况
  const environment = ref({
    temperature: 18.5,
    humidity: 65.2,
  });

  // 开花期管理计划
  const managementPlan = ref([
    {
      title: '初花期管理',
      date: '2023-04-10',
      description: '控制氮肥，增加磷钾肥，促进花芽分化',
      color: 'green',
    },
    {
      title: '盛花期管理',
      date: '2023-04-20',
      description: '加强水分管理，保持土壤湿润，促进授粉',
      color: 'blue',
    },
    {
      title: '末花期管理',
      date: '2023-04-30',
      description: '控制病虫害，减少落花落果，提高结实率',
      color: 'orange',
    },
  ]);

  const [registerTable, { reload }] = useTable({
    title: '开花期管理记录',
    api: getFloweringList,
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
      width: 120,
      title: '操作',
      dataIndex: 'action',
      slots: { customRender: 'action' },
    },
  });

  const [registerModal, { openModal }] = useModal();

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

  function handleDelete(record: Recordable) {
    deleteFlowering(record.id).then(() => {
      reload();
    });
  }

  function handleSuccess() {
    reload();
  }

  // 初始化图表
  function initChart() {
    const xAxisData = ['4月1日', '4月5日', '4月10日', '4月15日', '4月20日', '4月25日', '4月30日'];
    const floweringRateData = [10, 25, 45, 65, 85, 92, 95];
    const pollinationRateData = [0, 5, 15, 35, 55, 70, 78];

    setOptions({
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'cross',
          crossStyle: {
            color: '#999',
          },
        },
      },
      legend: {
        data: ['开花率', '授粉率'],
      },
      xAxis: [
        {
          type: 'category',
          data: xAxisData,
          axisPointer: {
            type: 'shadow',
          },
        },
      ],
      yAxis: [
        {
          type: 'value',
          name: '百分比(%)',
          min: 0,
          max: 100,
          interval: 20,
          axisLabel: {
            formatter: '{value} %',
          },
        },
      ],
      series: [
        {
          name: '开花率',
          type: 'line',
          smooth: true,
          data: floweringRateData,
        },
        {
          name: '授粉率',
          type: 'line',
          smooth: true,
          data: pollinationRateData,
        },
      ],
    });
  }

  onMounted(() => {
    nextTick(() => {
      initChart();
    });

    // 获取开花期进度
    getFloweringProgress().then((res) => {
      floweringProgress.value = res.progress || 75;
    });

    // 获取开花状况
    getFloweringStatus().then((res) => {
      floweringStatus.value = res || floweringStatus.value;
    });

    // 获取授粉情况
    getPollination().then((res) => {
      pollination.value = res || pollination.value;
    });

    // 获取环境状况
    getEnvironment().then((res) => {
      environment.value = res || environment.value;
    });

    // 获取开花期管理计划
    getFloweringManagementPlan().then((res) => {
      if (res && res.length > 0) {
        managementPlan.value = res;
      }
    });

    // 获取开花期趋势数据
    getFloweringTrend().then((res) => {
      if (res) {
        // 更新图表数据
        const xAxisData = res.map((item) => item.date);
        const floweringRateData = res.map((item) => item.floweringRate);
        const pollinationRateData = res.map((item) => item.pollinationRate);

        setOptions({
          xAxis: [
            {
              type: 'category',
              data: xAxisData,
              axisPointer: {
                type: 'shadow',
              },
            },
          ],
          series: [
            {
              name: '开花率',
              type: 'line',
              smooth: true,
              data: floweringRateData,
            },
            {
              name: '授粉率',
              type: 'line',
              smooth: true,
              data: pollinationRateData,
            },
          ],
        });
      }
    });
  });
</script>

<style lang="less" scoped>
  .rapeseed-flowering {
    .stat-card {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .stat-text {
      font-size: 14px;
      color: rgba(0, 0, 0, 0.65);
    }

    .timeline-content {
      .timeline-title {
        font-weight: 500;
        font-size: 16px;
        margin-bottom: 4px;
      }

      .timeline-date {
        color: rgba(0, 0, 0, 0.45);
        font-size: 14px;
        margin-bottom: 4px;
      }

      .timeline-desc {
        color: rgba(0, 0, 0, 0.65);
        font-size: 14px;
      }
    }

    .mb-4 {
      margin-bottom: 24px;
    }
  }
</style>