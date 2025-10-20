<template>
  <PageWrapper>
    <div class="rapeseed-budding">
      <!-- 统计卡片 -->
      <Row :gutter="24" class="mb-4">
        <Col :span="6">
          <Card>
            <template #title>
              <span>蕾薹期进度</span>
            </template>
            <div class="stat-card">
              <Progress :percent="buddingProgress" :stroke-color="progressColor" />
              <div class="stat-text">当前阶段已完成 {{ buddingProgress }}%</div>
            </div>
          </Card>
        </Col>
        <Col :span="6">
          <Card>
            <template #title>
              <span>植株状况</span>
            </template>
            <div class="stat-card">
              <Statistic title="平均株高" :value="plantStatus.avgHeight" suffix="cm" />
              <Statistic title="蕾薹数" :value="plantStatus.budCount" suffix="个/株" />
            </div>
          </Card>
        </Col>
        <Col :span="6">
          <Card>
            <template #title>
              <span>生长环境</span>
            </template>
            <div class="stat-card">
              <Statistic title="土壤湿度" :value="environment.soilMoisture" suffix="%" />
              <Statistic title="环境温度" :value="environment.temperature" suffix="°C" />
            </div>
          </Card>
        </Col>
        <Col :span="6">
          <Card>
            <template #title>
              <span>营养状况</span>
            </template>
            <div class="stat-card">
              <Statistic title="氮含量" :value="nutrition.nitrogen" suffix="mg/kg" />
              <Statistic title="磷含量" :value="nutrition.phosphorus" suffix="mg/kg" />
            </div>
          </Card>
        </Col>
      </Row>

      <!-- 蕾薹期管理记录表格 -->
      <Card title="蕾薹期管理记录" class="mb-4">
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

      <!-- 蕾薹期管理计划时间线 -->
      <Card title="蕾薹期管理计划">
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
    <BuddingModal @register="registerModal" @success="handleSuccess" />
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { ref, onMounted } from 'vue';
  import { Progress, Statistic, Timeline, TimelineItem } from 'ant-design-vue';
  import { PageWrapper } from '/@/components/Page';
  import { Card, Row, Col } from 'ant-design-vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import BuddingModal from './BuddingModal.vue';
  import { columns, searchFormSchema } from './data';
  import { getBuddingList, deleteBudding, getBuddingProgress, getPlantStatus, getEnvironment, getNutrition, getBuddingManagementPlan } from '/@/api/rapeseed/growth-stage/budding';

  // 蕾薹期进度
  const buddingProgress = ref(65);
  const progressColor = ref('#52c41a');

  // 植株状况
  const plantStatus = ref({
    avgHeight: 45.6,
    budCount: 8.2,
  });

  // 生长环境
  const environment = ref({
    soilMoisture: 68.5,
    temperature: 18.3,
  });

  // 营养状况
  const nutrition = ref({
    nitrogen: 32.6,
    phosphorus: 15.8,
  });

  // 蕾薹期管理计划
  const managementPlan = ref([
    {
      title: '蕾薹期初期管理',
      date: '2023-03-10',
      description: '进行第一次蕾薹期追肥，促进花蕾分化',
      color: 'green',
    },
    {
      title: '病虫害防治',
      date: '2023-03-15',
      description: '防治蚜虫和菜青虫，喷施生物农药',
      color: 'blue',
    },
    {
      title: '蕾薹期中期管理',
      date: '2023-03-20',
      description: '进行第二次蕾薹期追肥，补充微量元素',
      color: 'green',
    },
    {
      title: '蕾薹期后期管理',
      date: '2023-03-30',
      description: '控制氮肥，增加磷钾肥，促进花蕾发育',
      color: 'orange',
    },
  ]);

  const [registerTable, { reload }] = useTable({
    title: '蕾薹期管理记录',
    api: getBuddingList,
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
    deleteBudding(record.id).then(() => {
      reload();
    });
  }

  function handleSuccess() {
    reload();
  }

  onMounted(() => {
    // 获取蕾薹期进度
    getBuddingProgress().then((res) => {
      buddingProgress.value = res.progress || 65;
    });

    // 获取植株状况
    getPlantStatus().then((res) => {
      plantStatus.value = res || plantStatus.value;
    });

    // 获取生长环境
    getEnvironment().then((res) => {
      environment.value = res || environment.value;
    });

    // 获取营养状况
    getNutrition().then((res) => {
      nutrition.value = res || nutrition.value;
    });

    // 获取蕾薹期管理计划
    getBuddingManagementPlan().then((res) => {
      if (res && res.length > 0) {
        managementPlan.value = res;
      }
    });
  });
</script>

<style lang="less" scoped>
  .rapeseed-budding {
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