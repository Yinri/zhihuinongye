<template>
  <PageWrapper>
    <!-- 角果成熟期进度概览 -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
      <Card title="角果成熟期特点" :loading="loading">
        <div class="p-4">
          <h4 class="text-base font-medium mb-3">角果成熟期特点</h4>
          <p class="text-gray-600 mb-4">
            角果成熟期是油菜生长的最后阶段，角果由绿色逐渐变为黄色或褐色，种子逐渐成熟。这个阶段的管理重点是保持植株健康，促进种子充分成熟，提高产量和品质。
          </p>
          <Timeline>
            <TimelineItem color="green">
              <div class="font-medium">角果变色期</div>
              <div class="text-gray-500 text-sm">角果开始由绿色变为黄绿色，种子开始成熟</div>
            </TimelineItem>
            <TimelineItem color="orange">
              <div class="font-medium">角果成熟期</div>
              <div class="text-gray-500 text-sm">角果变为黄色或褐色，种子完全成熟</div>
            </TimelineItem>
            <TimelineItem color="red">
              <div class="font-medium">收获期</div>
              <div class="text-gray-500 text-sm">角果干燥，种子含水量降低，适合收获</div>
            </TimelineItem>
          </Timeline>
        </div>
      </Card>
      
      <Card title="角果成熟期管理要点" :loading="loading">
        <div class="p-4">
          <h4 class="text-base font-medium mb-3">管理要点</h4>
          <ul class="list-disc pl-5 space-y-2 text-gray-600">
            <li>控制水分，避免田间积水，促进角果成熟</li>
            <li>防治病虫害，特别是角果螟和菌核病</li>
            <li>适时收获，避免过早或过晚收获影响产量和品质</li>
            <li>收获前7-10天停止灌溉，促进角果干燥</li>
            <li>选择晴天收获，避免雨天收获导致种子发芽</li>
          </ul>
          
          <h4 class="text-base font-medium mt-4 mb-3">收获指标</h4>
          <ul class="list-disc pl-5 space-y-2 text-gray-600">
            <li>全田70%以上角果呈黄色或褐色</li>
            <li>主茎角果大部分变黄，种子呈品种固有颜色</li>
            <li>种子含水量降至12-14%</li>
            <li>植株叶片大部分脱落</li>
          </ul>
        </div>
      </Card>
    </div>
    
    <!-- 角果成熟期进度概览 -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      <Card title="角果成熟期进度" :loading="loading">
        <div class="flex items-center">
          <Progress :percent="progressData.progress" :status="progressData.status" />
          <span class="ml-4 text-gray-500">剩余{{ progressData.daysLeft }}天</span>
        </div>
      </Card>
      
      <Card title="角果状况" :loading="loading">
        <div class="py-2">
          <div class="flex justify-between mb-1">
            <span>角果长度</span>
            <span class="font-bold">{{ maturityData.podLength }} cm</span>
          </div>
          <div class="flex justify-between mb-1">
            <span>角果成熟度</span>
            <span class="font-bold">{{ maturityData.maturity }}%</span>
          </div>
          <div class="flex justify-between">
            <span>千粒重</span>
            <span class="font-bold">{{ maturityData.thousandGrainWeight }} g</span>
          </div>
        </div>
      </Card>
      
      <Card title="产量预估" :loading="loading">
        <div class="py-2">
          <div class="flex justify-between mb-1">
            <span>亩产量</span>
            <span class="font-bold text-green-600">{{ yieldData.yieldPerMu }} kg</span>
          </div>
          <div class="flex justify-between">
            <span>总产量</span>
            <span class="font-bold text-green-600">{{ yieldData.totalYield }} kg</span>
          </div>
        </div>
      </Card>
      
      <Card title="下次管理" :loading="loading">
        <div class="py-2">
          <div class="flex justify-between mb-1">
            <span>管理类型</span>
            <span class="font-bold">{{ nextManagement.type }}</span>
          </div>
          <div class="flex justify-between">
            <span>预计日期</span>
            <span class="font-bold">{{ nextManagement.date }}</span>
          </div>
        </div>
      </Card>
    </div>

    <!-- 角果成熟期生长趋势图表 -->
    <Card title="角果成熟期生长趋势" class="mb-6">
      <div ref="chartRef" :style="{ height: '300px' }"></div>
    </Card>

    <!-- 角果成熟期管理记录表格 -->
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

    <!-- 角果成熟期管理计划时间线 -->
    <Card title="角果成熟期管理计划" class="mt-6">
      <Timeline>
        <TimelineItem color="green">
          <template #dot>
            <Icon icon="ant-design:check-circle-filled" />
          </template>
          初角果期管理
          <p class="text-gray-500 text-sm mt-1">已完成 - 2023-04-15</p>
        </TimelineItem>
        <TimelineItem color="blue">
          <template #dot>
            <Icon icon="ant-design:clock-circle-outlined" />
          </template>
          角果膨大期管理
          <p class="text-gray-500 text-sm mt-1">进行中 - 预计2023-04-25</p>
        </TimelineItem>
        <TimelineItem>
          <template #dot>
            <Icon icon="ant-design:clock-circle-outlined" />
          </template>
          角果成熟期管理
          <p class="text-gray-500 text-sm mt-1">待进行 - 预计2023-05-05</p>
        </TimelineItem>
        <TimelineItem>
          <template #dot>
            <Icon icon="ant-design:clock-circle-outlined" />
          </template>
          收获前管理
          <p class="text-gray-500 text-sm mt-1">待进行 - 预计2023-05-15</p>
        </TimelineItem>
      </Timeline>
    </Card>

    <!-- 角果成熟期管理记录弹窗 -->
    <MaturityModal @register="registerModal" @success="handleSuccess" />
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
  import MaturityModal from './MaturityModal.vue';
  import { 
    getMaturityList, 
    deleteMaturity,
    getMaturityProgress,
    getMaturityStatus,
    getYieldEstimate,
    getNextManagement,
    getMaturityTrend,
  } from '/@/api/rapeseed/growth-stage/maturity';
  import { useECharts } from '/@/hooks/web/useECharts';

  const { createMessage } = useMessage();
  const [registerModal, { openModal }] = useModal();
  const loading = ref(true);
  
  // 搜索条件
  const searchInfo = reactive({});
  
  // 进度数据
  const progressData = reactive({
    progress: 0,
    status: 'normal',
    daysLeft: 0,
  });
  
  // 角果状况数据
  const maturityData = reactive({
    podLength: 0,
    maturity: 0,
    thousandGrainWeight: 0,
  });
  
  // 产量预估数据
  const yieldData = reactive({
    yieldPerMu: 0,
    totalYield: 0,
  });
  
  // 下次管理数据
  const nextManagement = reactive({
    type: '',
    date: '',
  });
  
  // 图表实例
  const chartRef = ref<HTMLDivElement | null>(null);
  const { setOptions } = useECharts(chartRef);

  const [registerTable, { reload }] = useTable({
    title: '角果成熟期管理记录',
    api: getMaturityList,
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
      const progressRes = await getMaturityProgress();
      Object.assign(progressData, progressRes);
      
      // 加载角果状况数据
      const maturityRes = await getMaturityStatus();
      Object.assign(maturityData, maturityRes);
      
      // 加载产量预估数据
      const yieldRes = await getYieldEstimate();
      Object.assign(yieldData, yieldRes);
      
      // 加载下次管理数据
      const managementRes = await getNextManagement();
      Object.assign(nextManagement, managementRes);
      
      // 加载趋势数据
      const trendRes = await getMaturityTrend();
      initChart(trendRes);
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
          type: 'cross',
        },
      },
      legend: {
        data: ['角果长度', '成熟度', '千粒重'],
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
          boundaryGap: false,
          data: data.dates,
        },
      ],
      yAxis: [
        {
          type: 'value',
          name: '长度(cm)',
          position: 'left',
          alignTicks: true,
          axisLine: {
            show: true,
            lineStyle: {
              color: '#5470C6',
            },
          },
          axisLabel: {
            formatter: '{value} cm',
          },
        },
        {
          type: 'value',
          name: '成熟度(%)',
          position: 'right',
          alignTicks: true,
          axisLine: {
            show: true,
            lineStyle: {
              color: '#91CC75',
            },
          },
          axisLabel: {
            formatter: '{value} %',
          },
        },
        {
          type: 'value',
          name: '千粒重(g)',
          position: 'left',
          alignTicks: true,
          axisLine: {
            show: true,
            lineStyle: {
              color: '#FAC858',
            },
          },
          axisLabel: {
            formatter: '{value} g',
          },
        },
      ],
      series: [
        {
          name: '角果长度',
          type: 'line',
          data: data.podLength,
        },
        {
          name: '成熟度',
          type: 'line',
          yAxisIndex: 1,
          data: data.maturity,
        },
        {
          name: '千粒重',
          type: 'line',
          yAxisIndex: 2,
          data: data.thousandGrainWeight,
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
    await deleteMaturity(record.id);
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