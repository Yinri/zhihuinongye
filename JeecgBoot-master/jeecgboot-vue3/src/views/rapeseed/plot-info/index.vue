<template>
  <div class="plot-info-page">
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

    <!-- 地块信息管理列表区域 -->
    <a-card :bordered="false" v-if="selectedBaseId" class="table-card">
      <template #toolbar>
        <a-space>
          <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleCreate"> 新增地块</a-button>
          <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
          <j-upload-button type="primary" preIcon="ant-design:import-outlined" @click="onImportXls">导入</j-upload-button>
          <a-dropdown v-if="selectedRowKeys.length > 0">
            <template #overlay>
              <a-menu>
                <a-menu-item key="1" @click="batchHandleDelete">
                  <Icon icon="ant-design:delete-outlined"></Icon>
                  删除
                </a-menu-item>
              </a-menu>
            </template>
            <a-button>
              批量操作
              <Icon icon="mdi:chevron-down"></Icon>
            </a-button>
          </a-dropdown>
        </a-space>
      </template>
      
      <BasicTable @register="registerTable" :rowSelection="rowSelection" :loading="loading">
        <!--操作栏-->
        <template #action="{ record }">
          <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
        </template>
        
        <!-- 空状态自定义 -->
        <template #emptyText>
          <a-empty description="该基地暂无地块信息">
            <template #image>
              <Icon icon="ant-design:environment-outlined" style="font-size: 64px; color: #d9d9d9;" />
            </template>
            <a-button type="primary" @click="handleCreate">立即创建</a-button>
          </a-empty>
        </template>
      </BasicTable>
    </a-card>

    <!-- 无数据提示 -->
    <a-card :bordered="false" v-if="selectedBaseId && !loading && dataSource.length === 0" class="empty-card">
      <a-result
        status="404"
        title="该基地暂无地块信息"
        sub-title="您可以点击下方按钮创建新的地块信息"
      >
        <template #icon>
          <Icon icon="ant-design:environment-outlined" style="font-size: 64px; color: #d9d9d9;" />
        </template>
        <template #extra>
          <a-button type="primary" @click="handleCreate">创建地块信息</a-button>
        </template>
      </a-result>
    </a-card>

    <!-- 未选择基地提示 -->
    <a-card :bordered="false" v-if="!selectedBaseId" class="empty-card">
      <a-result
        status="info"
        title="请先选择基地"
        sub-title="选择基地后即可查看和管理该基地的地块信息"
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

    <!-- 表单弹窗 -->
    <PlotInfoModal @register="registerModal" @success="handleSuccess" :baseId="selectedBaseId" />
  </div>
</template>

<script lang="ts" name="rapeseed-plot-info" setup>
import { ref, onMounted } from 'vue';
import { BasicTable, TableAction, ActionItem } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useListPage } from '/@/hooks/system/useListPage';
import { useMessage } from '/@/hooks/web/useMessage';
import { Icon } from '/@/components/Icon';
import PlotInfoModal from './PlotInfoModal.vue';
import { columns, searchFormSchema } from './plotInfo.data';
import { getPlotInfoList, deletePlotInfo, batchDeletePlotInfo, getImportUrl, getExportUrl } from './plotInfo.api';
import BaseSelect from '../production-plan/components/BaseSelect.vue';
import PlotSelect from '../production-plan/components/PlotSelect.vue';
import GrowthTimeline from '../production-plan/components/GrowthTimeline.vue';

const { createMessage } = useMessage();

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

//注册model
const [registerModal, { openModal }] = useModal();

// 列表页面公共参数、方法
const { prefixCls, tableContext, onExportXls, onImportXls } = useListPage({
  designScope: 'plot-info-list',
  tableProps: {
    title: '地块信息管理',
    api: selectedBaseId.value ? (params) => {
      // 如果有选中的基地ID，添加到查询参数
      if (selectedBaseId.value) {
        params.baseId = selectedBaseId.value;
      }
      return getPlotInfoList(params);
    } : getPlotInfoList,
    columns: columns,
    size: 'small',
    formConfig: {
      schemas: searchFormSchema,
    },
    actionColumn: {
      width: 120,
    },
    beforeFetch: (params) => {
      // 如果有选中的基地ID，添加到查询参数
      if (selectedBaseId.value) {
        params.baseId = selectedBaseId.value;
      }
      return Object.assign({ column: 'createTime', order: 'desc' }, params);
    },
  },
  exportConfig: {
    name: '地块信息列表',
    url: getExportUrl,
  },
  importConfig: {
    url: getImportUrl,
  },
});

//注册table数据
const [registerTable, { reload, updateTableDataRecord, dataSource }, { rowSelection, selectedRows, selectedRowKeys }] = tableContext;

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
  loadPlotInfoData();
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
  loadPlotInfoData();
}

/**
 * 加载地块信息数据
 */
async function loadPlotInfoData() {
  if (!selectedBaseId.value) {
    return;
  }
  
  try {
    loading.value = true;
    // 重新加载数据
    await reload();
  } catch (error) {
    console.error('加载地块信息数据失败:', error);
    createMessage.error('加载地块信息数据失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}

/**
 * 新增事件
 */
function handleCreate() {
  if (!selectedBaseId.value) {
    createMessage.warning('请先选择基地');
    return;
  }
  
  openModal(true, {
    isUpdate: false,
    baseId: selectedBaseId.value,
  });
}

/**
 * 编辑事件
 */
function handleEdit(record: Recordable) {
  openModal(true, {
    record,
    isUpdate: true,
  });
}

/**
 * 详情
 */
function handleDetail(record: Recordable) {
  openModal(true, {
    record,
    isUpdate: true,
    showFooter: false,
  });
}

/**
 * 删除事件
 */
async function handleDelete(record) {
  await deletePlotInfo({ id: record.id }, reload);
}

/**
 * 批量删除事件
 */
async function batchHandleDelete() {
  await batchDeletePlotInfo({ ids: selectedRowKeys.value }, () => {
    selectedRowKeys.value = [];
    reload();
  });
}

/**
 * 成功回调
 */
function handleSuccess() {
  reload();
}

/**
 * 操作栏
 */
function getTableAction(record): ActionItem[] {
  return [
    {
      label: '编辑',
      onClick: handleEdit.bind(null, record),
    },
    {
      label: '详情',
      onClick: handleDetail.bind(null, record),
    },
  ];
}

/**
 * 下拉操作栏
 */
function getDropDownAction(record): ActionItem[] {
  return [
    {
      label: '删除',
      popConfirm: {
        title: '是否确认删除',
        confirm: handleDelete.bind(null, record),
      },
    },
  ];
}

// 组件挂载时，可以设置默认基地ID
onMounted(() => {
  // 这里可以从用户信息或系统设置中获取默认基地ID
  // const defaultBaseId = getDefaultBaseId();
  // if (defaultBaseId) {
  //   selectedBaseId.value = defaultBaseId;
  //   loadPlotInfoData();
  // }
  
  // 模拟设置默认基地ID，实际项目中应该从API或配置中获取
  // defaultBaseId.value = '1';
});
</script>

<style lang="less" scoped>
.plot-info-page {
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