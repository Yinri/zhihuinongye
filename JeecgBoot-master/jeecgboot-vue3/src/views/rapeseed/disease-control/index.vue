<template>
  <div class="disease-control-page">
    <!-- 病害防控列表区域 -->
    <a-card :bordered="false" class="table-card">
      <template #toolbar>
        <a-space>
          <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleCreate"> 新增病害防控记录</a-button>
          <a-button type="primary" preIcon="ant-design:export-outlined" @click="handleExport"> 导出</a-button>
          <a-upload
            :fileList="[]"
            :beforeUpload="handleImport"
            accept=".xlsx,.xls"
          >
            <a-button type="primary" preIcon="ant-design:import-outlined"> 导入</a-button>
          </a-upload>
        </a-space>
      </template>
      
      <BasicTable @register="registerTable" :searchInfo="searchInfo" :loading="loading">
        <template #action="{ record }">
          <TableAction
            :actions="[
              {
                icon: 'clarity:note-edit-line',
                onClick: handleEdit.bind(null, record),
              },
              {
                icon: 'ant-design:delete-outlined',
                color: 'error',
                popConfirm: {
                  title: '是否确认删除',
                  confirm: handleDelete.bind(null, record),
                },
              },
            ]"
          />
        </template>
        
        <!-- 空状态自定义 -->
        <template #emptyText>
          <a-empty description="该地块暂无病害防控记录">
            <template #image>
              <Icon icon="ant-design:medicine-box-outlined" style="font-size: 64px; color: #d9d9d9;" />
            </template>
            <a-button type="primary" @click="handleCreate">立即创建</a-button>
          </a-empty>
        </template>
      </BasicTable>
    </a-card>

    <!-- 加载状态提示 -->
    <a-card :bordered="false" v-if="loading" class="loading-card">
      <a-skeleton active :paragraph="{ rows: 5 }" />
    </a-card>

    <!-- 表单弹窗 -->
    <DiseaseControlModal @register="registerModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { BasicTable, useTable, TableAction } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useMessage } from '/@/hooks/web/useMessage';
import { Icon } from '/@/components/Icon';
import { columns, searchFormSchema } from './diseaseControl.data';
import { getDiseaseControlList, deleteDiseaseControl, exportDiseaseControl, importDiseaseControl } from './diseaseControl.api';
import DiseaseControlModal from './DiseaseControlModal.vue';

const { createMessage } = useMessage();

// 加载状态
const loading = ref(false);

const [registerModal, { openModal }] = useModal();
const searchInfo = reactive<Recordable>({});
const [registerTable, { reload, getSelectRows, dataSource }] = useTable({
  title: '病害防控管理',
  api: getDiseaseControlList,
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

/**
 * 加载病害防控数据
 */
async function loadDiseaseControlData() {
  try {
    loading.value = true;
    // 重新加载数据
    await reload();
  } catch (error) {
    console.error('加载病害防控数据失败:', error);
    createMessage.error('加载病害防控数据失败，请稍后重试');
  } finally {
    loading.value = false;
  }
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
  await deleteDiseaseControl(record.id);
  createMessage.success('删除成功');
  reload();
}

function handleSuccess() {
  reload();
}

async function handleExport() {
  try {
    const data = await exportDiseaseControl(searchInfo);
    if (data) {
      const blob = new Blob([data], { type: 'application/vnd.ms-excel' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = '病害防控管理.xlsx';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      createMessage.success('导出成功');
    }
  } catch (error) {
    createMessage.error('导出失败');
  }
}

async function handleImport(file) {
  const formData = new FormData();
  formData.append('file', file);
  try {
    await importDiseaseControl(formData);
    createMessage.success('导入成功');
    reload();
  } catch (error) {
    createMessage.error('导入失败');
  }
  return false;
}


</script>

<style lang="less" scoped>
.disease-control-page {
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

// 响应式布局
@media (max-width: 1200px) {
  .combined-selection-timeline-card {
    .ant-col {
      margin-bottom: 16px;
    }
    
    :deep(.ant-row) {
      flex-direction: column;
      
      .ant-col {
        width: 100% !important;
        margin-bottom: 16px;
        
        &:last-child {
          margin-bottom: 0;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .disease-control-page {
    padding: 16px;
  }
  
  .combined-selection-timeline-card {
  :deep(.ant-card-body) {
    padding-left: 8px;
    padding-right: 8px;
    padding-top: 5px;
    padding-bottom: 5px;
  }
  }
}
</style>