<template>
  <PageWrapper>
    <!-- 收获与整地进度概览 -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      <Card title="收获进度" :loading="loading">
        <div class="flex items-center">
          <Progress :percent="progressData.harvestProgress" :status="progressData.harvestStatus" />
          <span class="ml-4 text-gray-500">已完成{{ progressData.harvestedArea }}亩</span>
        </div>
      </Card>
      
      <Card title="整地进度" :loading="loading">
        <div class="flex items-center">
          <Progress :percent="progressData.landPrepProgress" :status="progressData.landPrepStatus" />
          <span class="ml-4 text-gray-500">已完成{{ progressData.preparedArea }}亩</span>
        </div>
      </Card>
      
      <Card title="产量统计" :loading="loading">
        <div class="py-2">
          <div class="flex justify-between mb-1">
            <span>总产量</span>
            <span class="font-bold text-green-600">{{ yieldData.totalYield }} kg</span>
          </div>
          <div class="flex justify-between">
            <span>平均亩产</span>
            <span class="font-bold text-green-600">{{ yieldData.avgYieldPerMu }} kg</span>
          </div>
        </div>
      </Card>
      
      <Card title="下次作业" :loading="loading">
        <div class="py-2">
          <div class="flex justify-between mb-1">
            <span>作业类型</span>
            <span class="font-bold">{{ nextOperation.type }}</span>
          </div>
          <div class="flex justify-between">
            <span>预计日期</span>
            <span class="font-bold">{{ nextOperation.date }}</span>
          </div>
        </div>
      </Card>
    </div>

    <!-- 产量分布图表 -->
    <Card title="产量分布" class="mb-6">
      <div ref="chartRef" :style="{ height: '300px' }"></div>
    </Card>

    <!-- 收获与整地记录表格 -->
    <BasicTable @register="registerTable" :searchInfo="searchInfo">
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

    <!-- 收获与整地计划时间线 -->
    <Card title="收获与整地计划" class="mt-6">
      <Timeline>
        <TimelineItem color="green">
          <template #dot>
            <Icon icon="ant-design:check-circle-filled" />
          </template>
          收获准备
          <p class="text-gray-500 text-sm mt-1">已完成 - 2023-05-10</p>
        </TimelineItem>
        <TimelineItem color="blue">
          <template #dot>
            <Icon icon="ant-design:clock-circle-outlined" />
          </template>
          机械化收获
          <p class="text-gray-500 text-sm mt-1">进行中 - 预计2023-05-20</p>
        </TimelineItem>
        <TimelineItem>
          <template #dot>
            <Icon icon="ant-design:clock-circle-outlined" />
          </template>
          秸秆处理
          <p class="text-gray-500 text-sm mt-1">待进行 - 预计2023-05-25</p>
        </TimelineItem>
        <TimelineItem>
          <template #dot>
            <Icon icon="ant-design:clock-circle-outlined" />
          </template>
          土地整理
          <p class="text-gray-500 text-sm mt-1">待进行 - 预计2023-06-01</p>
        </TimelineItem>
      </Timeline>
    </Card>

    <!-- 收获与整地记录弹窗 -->
    <HarvestModal @register="registerModal" @success="handleSuccess" />
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { ref, onMounted, reactive } from 'vue';
  import { Card, Progress, Timeline, TimelineItem } from 'ant-design-vue';
  import { Icon } from '/@/components/Icon';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { BasicModal, useModal } from '/@/components/Modal';
  import { PageWrapper } from '/@/components/Page';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { columns, searchFormSchema } from './data';
  import HarvestModal from './HarvestModal.vue';
  import { 
    getHarvestList, 
    deleteHarvest,
    getHarvestProgress,
    getYieldStatistics,
    getNextOperation,
    getYieldDistribution,
  } from '/@/api/rapeseed/growth-stage/harvest';
  import { useECharts } from '/@/hooks/web/useECharts';

  const { createMessage } = useMessage();
  const [registerModal, { openModal }] = useModal();
  const loading = ref(true);
  
  // 搜索条件
  const searchInfo = reactive({});
  
  // 进度数据
  const progressData = reactive({
    harvestProgress: 0,
    harvestStatus: 'normal',
    harvestedArea: 0,
    landPrepProgress: 0,
    landPrepStatus: 'normal',
    preparedArea: 0,
  });
  
  // 产量统计数据
  const yieldData = reactive({
    totalYield: 0,
    avgYieldPerMu: 0,
  });
  
  // 下次作业数据
  const nextOperation = reactive({
    type: '',
    date: '',
  });
  
  // 图表实例
  const chartRef = ref<HTMLDivElement | null>(null);
  const { setOptions } = useECharts(chartRef);

  const [registerTable, { reload }] = useTable({
    title: '收获与整地记录',
    api: getHarvestList,
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

  // 加载概览数据
  async function loadOverviewData() {
    try {
      loading.value = true;
      
      // 加载进度数据
      const progressRes = await getHarvestProgress();
      Object.assign(progressData, progressRes);
      
      // 加载产量统计数据
      const yieldRes = await getYieldStatistics();
      Object.assign(yieldData, yieldRes);
      
      // 加载下次作业数据
      const operationRes = await getNextOperation();
      Object.assign(nextOperation, operationRes);
      
      // 加载产量分布数据
      const distributionRes = await getYieldDistribution();
      initChart(distributionRes);
    } catch (error) {
      createMessage.error('加载概览数据失败');
    } finally {
      loading.value = false;
    }
  }

  // 初始化图表
  function initChart(data) {
    setOptions({
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow',
        },
      },
      legend: {
        data: ['实际产量', '目标产量'],
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true,
      },
      xAxis: [
        {
          type: 'category',
          data: data.fields,
          axisTick: {
            alignWithLabel: true,
          },
        },
      ],
      yAxis: [
        {
          type: 'value',
          name: '产量(kg/亩)',
        },
      ],
      series: [
        {
          name: '实际产量',
          type: 'bar',
          barWidth: '60%',
          data: data.actualYield,
          itemStyle: {
            color: '#91CC75',
          },
        },
        {
          name: '目标产量',
          type: 'line',
          data: data.targetYield,
          itemStyle: {
            color: '#5470C6',
          },
        },
      ],
    });
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
    await deleteHarvest(record.id);
    createMessage.success('删除成功');
    reload();
  }

  function handleSuccess() {
    createMessage.success('操作成功');
    reload();
    loadOverviewData();
  }

  onMounted(() => {
    loadOverviewData();
  });
</script>