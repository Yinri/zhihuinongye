<template>
  <PageWrapper>
    <div class="rapeseed-pest-control">
      <!-- 统计卡片 -->
      <Row :gutter="24" class="mb-4">
        <Col :span="6">
          <Card>
            <template #title>
              <span>病虫害防控进度</span>
            </template>
            <div class="stat-card">
              <Progress :percent="pestControlProgress" :stroke-color="progressColor" />
              <div class="stat-text">当前阶段已完成 {{ pestControlProgress }}%</div>
            </div>
          </Card>
        </Col>
        <Col :span="6">
          <Card>
            <template #title>
              <span>病虫害预警</span>
            </template>
            <div class="stat-card">
              <Statistic title="当前风险等级" :value="pestWarning.riskLevel" />
              <div class="risk-indicator" :class="riskLevelClass">
                {{ riskLevelText }}
              </div>
            </div>
          </Card>
        </Col>
        <Col :span="6">
          <Card>
            <template #title>
              <span>防治统计</span>
            </template>
            <div class="stat-card">
              <Statistic title="防治次数" :value="controlStats.times" suffix="次" />
              <Statistic title="防治面积" :value="controlStats.area" suffix="亩" />
            </div>
          </Card>
        </Col>
        <Col :span="6">
          <Card>
            <template #title>
              <span>下次防治</span>
            </template>
            <div class="stat-card">
              <Statistic title="预计日期" :value="nextControl.date" />
              <Statistic title="防治类型" :value="nextControl.type" />
            </div>
          </Card>
        </Col>
      </Row>

      <!-- 病虫害风险趋势图表 -->
      <Card title="病虫害风险趋势" class="mb-4">
        <div ref="chartRef" :style="{ height: '300px', width: '100%' }"></div>
      </Card>

      <!-- 病虫害防控记录表格 -->
      <Card title="病虫害防控记录" class="mb-4">
        <BasicTable @register="registerTable">
          <template #toolbar>
            <a-button type="primary" @click="handleCreate"> 新增防控记录 </a-button>
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

      <!-- 病虫害防控计划时间线 -->
      <Card title="病虫害防控计划">
        <Timeline>
          <TimelineItem v-for="(item, index) in controlPlan" :key="index" :color="item.color">
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
    <PestControlModal @register="registerModal" @success="handleSuccess" />
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { ref, onMounted, nextTick, computed } from 'vue';
  import { Progress, Statistic, Timeline, TimelineItem } from 'ant-design-vue';
  import { PageWrapper } from '/@/components/Page';
  import { Card, Row, Col } from 'ant-design-vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useECharts } from '/@/hooks/web/useECharts';
  import PestControlModal from './PestControlModal.vue';
  import { columns, searchFormSchema } from './data';
  import { getPestControlList, deletePestControl, getPestControlProgress, getPestWarning, getControlStats, getNextControl, getControlPlan, getRiskTrend } from '/@/api/rapeseed/growth-stage/budding/pest-control';

  const chartRef = ref<HTMLDivElement | null>(null);
  const { setOptions } = useECharts(chartRef);

  // 病虫害防控进度
  const pestControlProgress = ref(60);
  const progressColor = ref('#52c41a');

  // 病虫害预警
  const pestWarning = ref({
    riskLevel: 2,
  });

  // 风险等级样式和文本
  const riskLevelClass = computed(() => {
    const level = pestWarning.value.riskLevel;
    if (level === 1) return 'risk-low';
    if (level === 2) return 'risk-medium';
    if (level === 3) return 'risk-high';
    return 'risk-low';
  });

  const riskLevelText = computed(() => {
    const level = pestWarning.value.riskLevel;
    if (level === 1) return '低风险';
    if (level === 2) return '中风险';
    if (level === 3) return '高风险';
    return '低风险';
  });

  // 防治统计
  const controlStats = ref({
    times: 4,
    area: 125.6,
  });

  // 下次防治
  const nextControl = ref({
    date: '2023-04-08',
    type: '蚜虫防治',
  });

  // 防控计划
  const controlPlan = ref([
    {
      title: '蕾薹期初期病虫害防治',
      date: '2023-03-12',
      description: '防治蚜虫和菜青虫，喷施生物农药',
      color: 'green',
    },
    {
      title: '蕾薹期中期病虫害防治',
      date: '2023-03-22',
      description: '防治霜霉病和菌核病，喷施杀菌剂',
      color: 'blue',
    },
    {
      title: '蕾薹期后期病虫害防治',
      date: '2023-04-08',
      description: '防治蚜虫和小菜蛾，喷施杀虫剂',
      color: 'orange',
    },
  ]);

  const [registerTable, { reload }] = useTable({
    title: '病虫害防控记录',
    api: getPestControlList,
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
    deletePestControl(record.id).then(() => {
      reload();
    });
  }

  function handleSuccess() {
    reload();
  }

  // 初始化图表
  function initChart() {
    const xAxisData = ['1月', '2月', '3月', '4月', '5月', '6月'];
    const riskData = [1, 1, 2, 2, 1, 1];

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
        data: ['病虫害风险等级'],
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
          name: '风险等级',
          min: 0,
          max: 3,
          interval: 1,
          axisLabel: {
            formatter: (value) => {
              if (value === 1) return '低风险';
              if (value === 2) return '中风险';
              if (value === 3) return '高风险';
              return value;
            },
          },
        },
      ],
      series: [
        {
          name: '病虫害风险等级',
          type: 'line',
          smooth: true,
          data: riskData,
          markLine: {
            data: [
              { yAxis: 2, name: '警戒线' },
            ],
          },
        },
      ],
    });
  }

  onMounted(() => {
    nextTick(() => {
      initChart();
    });

    // 获取病虫害防控进度
    getPestControlProgress().then((res) => {
      pestControlProgress.value = res.progress || 60;
    });

    // 获取病虫害预警
    getPestWarning().then((res) => {
      pestWarning.value = res || pestWarning.value;
    });

    // 获取防治统计
    getControlStats().then((res) => {
      controlStats.value = res || controlStats.value;
    });

    // 获取下次防治
    getNextControl().then((res) => {
      nextControl.value = res || nextControl.value;
    });

    // 获取防控计划
    getControlPlan().then((res) => {
      if (res && res.length > 0) {
        controlPlan.value = res;
      }
    });

    // 获取风险趋势数据
    getRiskTrend().then((res) => {
      if (res) {
        // 更新图表数据
        const xAxisData = res.map((item) => item.month);
        const riskData = res.map((item) => item.riskLevel);

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
              name: '病虫害风险等级',
              type: 'line',
              smooth: true,
              data: riskData,
              markLine: {
                data: [
                  { yAxis: 2, name: '警戒线' },
                ],
              },
            },
          ],
        });
      }
    });
  });
</script>

<style lang="less" scoped>
  .rapeseed-pest-control {
    .stat-card {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .stat-text {
      font-size: 14px;
      color: rgba(0, 0, 0, 0.65);
    }

    .risk-indicator {
      display: inline-block;
      padding: 4px 8px;
      border-radius: 4px;
      font-weight: 500;
      font-size: 14px;
      text-align: center;
      
      &.risk-low {
        background-color: #f6ffed;
        color: #52c41a;
        border: 1px solid #b7eb8f;
      }
      
      &.risk-medium {
        background-color: #fff7e6;
        color: #fa8c16;
        border: 1px solid #ffd591;
      }
      
      &.risk-high {
        background-color: #fff2f0;
        color: #ff4d4f;
        border: 1px solid #ffb3b3;
      }
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