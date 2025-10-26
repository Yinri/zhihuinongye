<template>
  <div>
    <BasicTable @register="registerTable" :searchInfo="searchInfo">
      <template #toolbar>
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
      </template>
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
    </BasicTable>
    <DataIntegrationModal @register="registerModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" setup>
  import { reactive, ref } from 'vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { columns, searchFormSchema } from './dataIntegration.data';
  import { getDataIntegrationList, deleteDataIntegration, exportDataIntegration, importDataIntegration } from './dataIntegration.api';
  import DataIntegrationModal from './DataIntegrationModal.vue';

  const { createMessage } = useMessage();
  const [registerModal, { openModal }] = useModal();
  const searchInfo = reactive<Recordable>({});
  const fileList = ref<any[]>([]);

  const [registerTable, { reload, getSelectRows }] = useTable({
    title: '数据对接管理',
    api: getDataIntegrationList,
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
    await deleteDataIntegration(record.id);
    createMessage.success('删除成功');
    reload();
  }

  function handleSuccess() {
    createMessage.success('操作成功');
    reload();
  }

  async function handleExport() {
    const data = await getDataIntegrationList(searchInfo);
    exportDataIntegration(data);
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
      await importDataIntegration(formData);
      createMessage.success('导入成功');
      reload();
      fileList.value = [];
    } catch (error) {
      createMessage.error('导入失败');
    }
  }
</script>
