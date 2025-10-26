<template>
  <div class="farming-records-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-title">
        <Icon icon="ant-design:file-text-outlined" />
        农事记录管理
      </div>
      <div class="page-description">记录和管理各基地的农事活动信息</div>
    </div>

    <!-- 基地与地块选择区域 -->
    <a-row :gutter="16">
      <a-col :span="12">
        <a-card :bordered="false" class="base-select-card">
          <BaseSelect 
            v-model:value="selectedBaseId" 
            @change="handleBaseChange" 
            :defaultBaseId="defaultBaseId"
            ref="baseSelectRef"
          />
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card :bordered="false" class="plot-select-card">
          <PlotSelect 
            v-model:value="selectedPlotId" 
            :baseId="selectedBaseId"
            @change="handlePlotChange"
            ref="plotSelectRef"
          />
        </a-card>
      </a-col>
    </a-row>

    <!-- 生长周期时间轴区域 -->
    <a-row :gutter="16" style="margin-top: 16px;">
      <a-col :span="24">
        <a-card :bordered="false" class="timeline-card">
          <GrowthTimeline 
            :plotId="selectedPlotId"
            :varietyId="selectedVarietyId"
            :varietyName="selectedVarietyName"
            :currentStageId="currentStageId"
          />
        </a-card>
      </a-col>
    </a-row>

    <!-- 农事记录列表区域 -->
    <a-card :bordered="false" v-if="selectedBaseId" class="table-card">
      <template #toolbar>
        <a-space>
          <a-button type="primary" @click="handleCreate"> 新增 </a-button>
          <a-button type="primary" @click="handleExport"> 导出 </a-button>
          <a-upload
            :file-list="fileList"
            :remove="handleRemove"
            :before-upload="beforeUpload"
            accept=".xlsx,.xls"
          >
            <a-button type="primary"> 导入 </a-button>
          </a-upload>
        </a-space>
      </template>
      
      <BasicTable @register="registerTable" :searchInfo="searchInfo" :loading="loading">
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
        
        <!-- 空状态自定义 -->
        <template #emptyText>
          <a-empty description="该地块暂无农事记录数据">
            <template #image>
              <Icon icon="ant-design:file-text-outlined" style="font-size: 64px; color: #d9d9d9;" />
            </template>
            <a-button type="primary" @click="handleCreate">立即创建</a-button>
          </a-empty>
        </template>
      </BasicTable>
    </a-card>

    <!-- 无数据提示 -->
    <a-card :bordered="false" v-if="selectedBaseId && selectedPlotId && !loading && dataSource.length === 0" class="empty-card">
      <a-result
        status="404"
        title="该地块暂无农事记录数据"
        sub-title="您可以点击下方按钮创建新的农事记录"
      >
        <template #icon>
          <Icon icon="ant-design:file-text-outlined" style="font-size: 64px; color: #d9d9d9;" />
        </template>
        <template #extra>
          <a-button type="primary" @click="handleCreate">创建农事记录</a-button>
        </template>
      </a-result>
    </a-card>

    <!-- 未选择基地提示 -->
    <a-card :bordered="false" v-if="!selectedBaseId" class="empty-card">
      <a-result
        status="info"
        title="请先选择基地和地块"
        sub-title="选择基地和地块后即可查看和管理该地块的农事记录"
      >
        <template #icon>
          <Icon icon="ant-design:home-outlined" style="font-size: 64px; color: #1890ff;" />
        </template>
      </a-result>
    </a-card>

    <!-- 加载状态提示 -->
    <a-card :bordered="false" v-if="loading && selectedBaseId" class="loading-card">
      <a-skeleton active :paragraph="{ rows: 5 }" />
    </a-card>

    <!-- 农事记录模态框 -->
    <FarmingRecordsModal @register="registerModal" @success="handleSuccess" :baseId="selectedBaseId" :plotId="selectedPlotId" />
  </div>
</template>

<script lang="ts" setup>
import { reactive, ref, onMounted } from 'vue';
import { BasicTable, useTable, TableAction } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useMessage } from '/@/hooks/web/useMessage';
import { Icon } from '/@/components/Icon';
import { columns, searchFormSchema } from './farmingRecords.data';
import { getFarmingRecordsList, deleteFarmingRecords, exportFarmingRecords, importFarmingRecords } from './farmingRecords.api';
import FarmingRecordsModal from './FarmingRecordsModal.vue';
import BaseSelect from '../production-plan/components/BaseSelect.vue';
import PlotSelect from '../production-plan/components/PlotSelect.vue';
import GrowthTimeline from '../production-plan/components/GrowthTimeline.vue';

const { createMessage } = useMessage();
const [registerModal, { openModal }] = useModal();
const searchInfo = reactive<Recordable>({});
const fileList = ref<any[]>([]);

// 基地选择相关
const selectedBaseId = ref<string | number | undefined>(undefined);
const selectedBaseName = ref<string>('');
const baseSelectRef = ref();

// 地块选择相关
const selectedPlotId = ref<string | number | undefined>(undefined);
const selectedPlotName = ref<string>('');
const plotSelectRef = ref();

// 品种信息
const selectedVarietyId = ref<string | number | undefined>(undefined);
const selectedVarietyName = ref<string>('');
const currentStageId = ref<string | number | undefined>(undefined);

// 加载状态
const loading = ref(false);

// 默认基地ID（可以从URL参数或用户设置中获取）
const defaultBaseId = ref<string | number | undefined>(undefined);

const [registerTable, { reload, getSelectRows, dataSource }] = useTable({
  title: '农事记录管理',
  api: selectedPlotId.value ? (params) => {
    // 如果有选中的地块ID，添加到查询参数
    if (selectedPlotId.value) {
      params.plotId = selectedPlotId.value;
    }
    return getFarmingRecordsList(params);
  } : getFarmingRecordsList,
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
    // 如果有选中的基地ID或地块ID，添加到查询参数
    if (selectedBaseId.value) {
      params.baseId = selectedBaseId.value;
    }
    if (selectedPlotId.value) {
      params.plotId = selectedPlotId.value;
    }
    return params;
  },
});

/**
 * 基地选择变化处理
 */
function handleBaseChange(baseId) {
  selectedBaseId.value = baseId;
  
  // 获取基地名称
  if (baseSelectRef.value && baseSelectRef.value.baseOptions) {
    const base = baseSelectRef.value.baseOptions.find(item => item.id === baseId);
    selectedBaseName.value = base ? base.baseName : '';
  }
  
  // 清空地块选择
  selectedPlotId.value = undefined;
  selectedPlotName.value = '';
  
  // 清空品种信息
  selectedVarietyId.value = undefined;
  selectedVarietyName.value = '';
  currentStageId.value = undefined;
  
  // 重新加载数据
  loadFarmingRecordsData();
}

/**
 * 地块选择变化处理
 */
function handlePlotChange(plotId) {
  selectedPlotId.value = plotId;
  
  // 获取地块名称
  if (plotSelectRef.value && plotSelectRef.value.plotOptions) {
    const plot = plotSelectRef.value.plotOptions.find(item => item.id === plotId);
    selectedPlotName.value = plot ? plot.plotName : '';
    
    // 获取品种信息（这里假设地块信息中包含品种信息）
    if (plot) {
      selectedVarietyId.value = plot.varietyId;
      selectedVarietyName.value = plot.varietyName || '';
      // 这里可以根据实际情况设置当前阶段ID
      // currentStageId.value = plot.currentStageId;
    }
  }
  
  // 重新加载数据
  loadFarmingRecordsData();
}

/**
 * 加载农事记录数据
 */
async function loadFarmingRecordsData() {
  if (!selectedBaseId.value) {
    return;
  }
  
  try {
    loading.value = true;
    // 重新加载数据
    await reload();
  } catch (error) {
    console.error('加载农事记录数据失败:', error);
    createMessage.error('加载农事记录数据失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}

function handleCreate() {
  if (!selectedBaseId.value || !selectedPlotId.value) {
    createMessage.warning('请先选择基地和地块');
    return;
  }
  
  openModal(true, {
    isUpdate: false,
    baseId: selectedBaseId.value,
    plotId: selectedPlotId.value,
  });
}

function handleEdit(record: Recordable) {
  openModal(true, {
    record,
    isUpdate: true,
  });
}

async function handleDelete(record: Recordable) {
  await deleteFarmingRecords(record.id);
  createMessage.success('删除成功');
  reload();
}

function handleSuccess() {
  createMessage.success('操作成功');
  reload();
}

async function handleExport() {
  const data = await getFarmingRecordsList(searchInfo);
  exportFarmingRecords(data);
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
    await importFarmingRecords(formData);
    createMessage.success('导入成功');
    reload();
    fileList.value = [];
  } catch (error) {
    createMessage.error('导入失败');
  }
}

// 组件挂载时，可以设置默认基地ID
onMounted(() => {
  // 这里可以从用户信息或系统设置中获取默认基地ID
  // const defaultBaseId = getDefaultBaseId();
  // if (defaultBaseId) {
  //   selectedBaseId.value = defaultBaseId;
  //   loadFarmingRecordsData();
  // }
  
  // 模拟设置默认基地ID，实际项目中应该从API或配置中获取
  // defaultBaseId.value = '1';
});
</script>

<style lang="less" scoped>
.farming-records-page {
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

.base-select-card,
.plot-select-card {
  height: 100%;
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
</style>