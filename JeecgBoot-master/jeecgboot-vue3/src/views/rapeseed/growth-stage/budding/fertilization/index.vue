<template>
  <PageWrapper>
    <div class="rapeseed-fertilization">
      <!-- 统计卡片 -->
      <Row :gutter="24" class="mb-4">
        <Col :span="6">
          <Card>
            <template #title>
              <span>施肥进度</span>
            </template>
            <div class="stat-card">
              <Progress :percent="fertilizationProgress" :stroke-color="progressColor" />
              <div class="stat-text">当前阶段已完成 {{ fertilizationProgress }}%</div>
            </div>
          </Card>
        </Col>
        <Col :span="6">
          <Card>
            <template #title>
              <span>土壤养分状况</span>
            </template>
            <div class="stat-card">
              <Statistic title="氮含量" :value="soilNutrients.nitrogen" suffix="mg/kg" />
              <Statistic title="磷含量" :value="soilNutrients.phosphorus" suffix="mg/kg" />
              <Statistic title="钾含量" :value="soilNutrients.potassium" suffix="mg/kg" />
            </div>
          </Card>
        </Col>
        <Col :span="6">
          <Card>
            <template #title>
              <span>施肥统计</span>
            </template>
            <div class="stat-card">
              <Statistic title="总施肥量" :value="fertilizationStats.totalAmount" suffix="kg" />
              <Statistic title="施肥次数" :value="fertilizationStats.times" suffix="次" />
            </div>
          </Card>
        </Col>
        <Col :span="6">
          <Card>
            <template #title>
              <span>下次施肥</span>
            </template>
            <div class="stat-card">
              <Statistic title="预计日期" :value="nextFertilization.date" />
              <Statistic title="预计用量" :value="nextFertilization.amount" suffix="kg/亩" />
            </div>
          </Card>
        </Col>
      </Row>

      <!-- 土壤养分趋势图表 -->
      <Card title="土壤养分趋势" class="mb-4">
        <div ref="chartRef" :style="{ height: '300px', width: '100%' }"></div>
      </Card>

      <!-- 追肥记录表格 -->
      <Card title="追肥记录" class="mb-4">
        <BasicTable @register="registerTable">
          <template #toolbar>
            <a-button type="primary" @click="handleCreate"> 新增追肥记录 </a-button>
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

      <!-- 追肥计划时间线 -->
      <Card title="追肥计划">
        <Timeline>
          <TimelineItem v-for="(item, index) in fertilizationPlan" :key="index" :color="item.color">
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
    <FertilizationModal @register="registerModal" @success="handleSuccess" />
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
  import FertilizationModal from './FertilizationModal.vue';
  import { columns, searchFormSchema } from './data';
  import { getFertilizationList, deleteFertilization, getFertilizationProgress, getSoilNutrients, getFertilizationStats, getNextFertilization, getFertilizationPlan, getNutrientTrend } from '/@/api/rapeseed/growth-stage/budding/fertilization';

  const chartRef = ref<HTMLDivElement | null>(null);
  const { setOptions } = useECharts(chartRef);

  // 施肥进度
  const fertilizationProgress = ref(45);
  const progressColor = ref('#52c41a');

  // 土壤养分状况
  const soilNutrients = ref({
    nitrogen: 28.5,
    phosphorus: 12.3,
    potassium: 18.7,
  });

  // 施肥统计
  const fertilizationStats = ref({
    totalAmount: 125.6,
    times: 3,
  });

  // 下次施肥
  const nextFertilization = ref({
    date: '2023-04-05',
    amount: 15.2,
  });

  // 追肥计划
  const fertilizationPlan = ref([
    {
      title: '蕾薹期初期追肥',
      date: '2023-03-10',
      description: '施用复合肥，促进花蕾分化，用量20kg/亩',
      color: 'green',
    },
    {
      title: '蕾薹期中期追肥',
      date: '2023-03-25',
      description: '施用氮磷钾肥，补充微量元素，用量15kg/亩',
      color: 'blue',
    },
    {
      title: '蕾薹期后期追肥',
      date: '2023-04-05',
      description: '控制氮肥，增加磷钾肥，促进花蕾发育，用量15kg/亩',
      color: 'orange',
    },
  ]);

  const [registerTable, { reload }] = useTable({
    title: '追肥记录',
    api: getFertilizationList,
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
    deleteFertilization(record.id).then(() => {
      reload();
    });
  }

  function handleSuccess() {
    reload();
  }

  // 初始化图表
  function initChart() {
    const xAxisData = ['1月', '2月', '3月', '4月', '5月', '6月'];
    const nitrogenData = [15.2, 18.5, 22.3, 28.5, 25.6, 23.4];
    const phosphorusData = [8.5, 9.2, 10.5, 12.3, 11.8, 10.9];
    const potassiumData = [12.3, 14.5, 16.2, 18.7, 17.5, 16.8];

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
        data: ['氮含量', '磷含量', '钾含量'],
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
          name: '含量(mg/kg)',
          min: 0,
          max: 30,
          interval: 5,
          axisLabel: {
            formatter: '{value} mg/kg',
          },
        },
      ],
      series: [
        {
          name: '氮含量',
          type: 'line',
          smooth: true,
          data: nitrogenData,
        },
        {
          name: '磷含量',
          type: 'line',
          smooth: true,
          data: phosphorusData,
        },
        {
          name: '钾含量',
          type: 'line',
          smooth: true,
          data: potassiumData,
        },
      ],
    });
  }

  onMounted(() => {
    nextTick(() => {
      initChart();
    });

    // 获取施肥进度
    getFertilizationProgress().then((res) => {
      fertilizationProgress.value = res.progress || 45;
    });

    // 获取土壤养分状况
    getSoilNutrients().then((res) => {
      soilNutrients.value = res || soilNutrients.value;
    });

    // 获取施肥统计
    getFertilizationStats().then((res) => {
      fertilizationStats.value = res || fertilizationStats.value;
    });

    // 获取下次施肥
    getNextFertilization().then((res) => {
      nextFertilization.value = res || nextFertilization.value;
    });

    // 获取追肥计划
    getFertilizationPlan().then((res) => {
      if (res && res.length > 0) {
        fertilizationPlan.value = res;
      }
    });

    // 获取养分趋势数据
    getNutrientTrend().then((res) => {
      if (res) {
        // 更新图表数据
        const xAxisData = res.map((item) => item.month);
        const nitrogenData = res.map((item) => item.nitrogen);
        const phosphorusData = res.map((item) => item.phosphorus);
        const potassiumData = res.map((item) => item.potassium);

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
              name: '氮含量',
              type: 'line',
              smooth: true,
              data: nitrogenData,
            },
            {
              name: '磷含量',
              type: 'line',
              smooth: true,
              data: phosphorusData,
            },
            {
              name: '钾含量',
              type: 'line',
              smooth: true,
              data: potassiumData,
            },
          ],
        });
      }
    });
  });
</script>

<style lang="less" scoped>
  .rapeseed-fertilization {
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