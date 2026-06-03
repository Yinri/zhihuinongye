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

    <!-- 农事记录列表区域 -->
    <a-card :bordered="false" class="table-card">
      <template #extra>
        <a-space>
          <a-button type="primary" @click="handleCreate">
            <Icon icon="ant-design:plus-outlined" /> 新增
          </a-button>
          <a-button @click="handleExport">
            <Icon icon="ant-design:export-outlined" /> 导出
          </a-button>
          <a-upload
            :show-upload-list="false"
            :before-upload="handleImportFile"
            accept=".xlsx,.xls"
          >
            <a-button>
              <Icon icon="ant-design:import-outlined" /> 导入
            </a-button>
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
          <a-empty description="暂无农事记录数据">
            <template #image>
              <Icon icon="ant-design:file-text-outlined" style="font-size: 64px; color: #d9d9d9;" />
            </template>
            <a-button type="primary" @click="handleCreate">立即创建</a-button>
          </a-empty>
        </template>
      </BasicTable>
    </a-card>

    <!-- 农事记录模态框 -->
    <FarmingRecordsModal @register="registerModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" setup>
import { reactive, ref, watch, computed } from 'vue';
import { BasicTable, useTable, TableAction } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useMessage } from '/@/hooks/web/useMessage';
import { Icon } from '/@/components/Icon';
import { columns, searchFormSchema } from './farmingRecords.data';
import { getFarmingRecordsList, deleteFarmingRecords, exportFarmingRecords, importFarmingRecords } from './farmingRecords.api';
import FarmingRecordsModal from './FarmingRecordsModal.vue';
import { useSelectStore } from '/@/store/selectStore';

const { createMessage } = useMessage();
const [registerModal, { openModal }] = useModal();
const searchInfo = reactive<Recordable>({});

// 获取基地选择的store
const selectStore = useSelectStore();

// 加载状态
const loading = ref(false);

// 初始化表格
const [registerTable, { reload, getForm }] = useTable({
  title:'',
  api: getFarmingRecordsList,
  beforeFetch: (params) => {
    // 从store中获取基地ID，并添加到查询参数中
    const baseId = selectStore.selectedBase.baseId;
    if (baseId) {
      params.baseId = baseId;
    }
    return params;
  },
  afterFetch: (data) => {
    // 确保数据中有baseName和plotName字段
    if (data && data.records) {
      data.records.forEach(record => {
        if (!record.baseName && record.baseId) {
          record.baseName = `基地${record.baseId}`;
        }
        if (!record.plotName && record.plotId) {
          record.plotName = `地块${record.plotId}`;
        }
      });
    }
    return data;
  },
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
});

// 监听基地ID变化，重新加载数据
watch(
  () => selectStore.selectedBase?.baseId,
  () => {
    // 无论 baseId 是否存在，都重新加载数据
    reload();
  },
  { immediate: true }
);

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
  await deleteFarmingRecords(record.id);
  reload();
}

function handleSuccess() {
  reload();
}

async function handleExport() {
  const form = getForm();
  const formValues = form.getFieldsValue();
  const params = {
    ...formValues,
    baseId: selectStore.selectedBase.baseId,
  };
  try {
    const response = await exportFarmingRecords(params);
    const blob = response.data;
    if (!(await isExcelBlob(blob))) {
      createMessage.error(await readBlobError(blob));
      return;
    }
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `农事记录表${await getExcelExtension(blob)}`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  } catch (e) {
    createMessage.error('导出失败');
  }
}

async function isExcelBlob(blob: Blob) {
  if (!blob) return false;
  const type = blob.type || '';
  if (type.includes('application/vnd.ms-excel') || type.includes('application/vnd.openxmlformats-officedocument.spreadsheetml.sheet')) {
    return true;
  }
  const bytes = new Uint8Array(await blob.slice(0, 4).arrayBuffer());
  const isXlsx = bytes[0] === 0x50 && bytes[1] === 0x4b;
  const isXls = bytes[0] === 0xd0 && bytes[1] === 0xcf && bytes[2] === 0x11 && bytes[3] === 0xe0;
  return isXlsx || isXls;
}

async function getExcelExtension(blob: Blob) {
  const bytes = new Uint8Array(await blob.slice(0, 4).arrayBuffer());
  const isXls = bytes[0] === 0xd0 && bytes[1] === 0xcf && bytes[2] === 0x11 && bytes[3] === 0xe0;
  return isXls ? '.xls' : '.xlsx';
}

async function readBlobError(blob: Blob) {
  if (!blob) return '导出失败';
  try {
    const text = await blob.text();
    if (!text) return '导出失败';
    const data = JSON.parse(text);
    return data.message || data.msg || '导出失败';
  } catch {
    return '导出失败';
  }
}

async function handleImportFile(file) {
  try {
    await importFarmingRecords(file);
    reload();
  } catch (error) {
    // 拦截器已处理错误提示
  }
  return false;
}
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
