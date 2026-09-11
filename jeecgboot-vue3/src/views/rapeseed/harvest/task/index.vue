<template>
  <div class="task-page">
    <a-card :bordered="false" title="收获计划任务">
      <template #extra>
        <a-button @click="goBack">
          <Icon icon="ant-design:arrow-left-outlined" /> 返回收获管理
        </a-button>
      </template>
      <BasicTable @register="registerTable" :searchInfo="searchInfo">
        <template #toolbar>
          <a-button type="primary" @click="handleCreate">
            <Icon icon="ant-design:plus-outlined" /> 新增
          </a-button>
          <a-button @click="handleExport">
            <Icon icon="ant-design:download-outlined" /> 导出
          </a-button>
          <a-upload :file-list="fileList" :before-upload="beforeUpload" @remove="handleRemove">
            <a-button>
              <Icon icon="ant-design:upload-outlined" /> 导入
            </a-button>
          </a-upload>
        </template>
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'action'">
            <TableAction :actions="[
              { icon: 'ant-design:edit-outlined', tooltip: '编辑', onClick: handleEdit.bind(null, record) },
              { icon: 'ant-design:delete-outlined', color: 'error', tooltip: '删除', popConfirm: { title: '是否确认删除', confirm: handleDelete.bind(null, record) } },
            ]" />
          </template>
        </template>
      </BasicTable>
    </a-card>
    <TaskModal @register="registerModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" setup>
import { reactive, ref, watch } from 'vue';
import { BasicTable, useTable, TableAction } from '/@/components/Table';
import { Icon } from '/@/components/Icon';
import { useMessage } from '/@/hooks/web/useMessage';
import { useRouter } from 'vue-router';
import { columns, searchFormSchema } from './task.data';
import { getTaskList, deleteTask, exportTask, importTask } from './task.api';
import { useSelectStore } from '/@/store/selectStore';
import { storeToRefs } from 'pinia';
import TaskModal from './TaskModal.vue';
import { useModal } from '/@/components/Modal';

const { createMessage } = useMessage();
const [registerModal, { openModal }] = useModal();
const router = useRouter();
const selectStore = useSelectStore();
const { selectedBase } = storeToRefs(selectStore);
const searchInfo = reactive<Recordable>({ baseId: selectedBase.value?.baseId });
const fileList = ref<any[]>([]);

const [registerTable, { reload }] = useTable({
  api: getTaskList,
  title: '收获计划任务',
  columns,
  formConfig: { labelWidth: 120, schemas: searchFormSchema, autoSubmitOnEnter: true },
  useSearchForm: true,
  showTableSetting: false,
  bordered: true,
  showIndexColumn: false,
});

function handleCreate() {
  openModal(true, { isUpdate: false });
}

function goBack() {
  router.push('/rapeseed/harvest');
}

function handleEdit(record: Recordable) {
  openModal(true, { record, isUpdate: true });
}

async function handleDelete(record: Recordable) {
  await deleteTask(record.id);
  createMessage.success('删除成功');
  reload();
}

function handleSuccess() {
  createMessage.success('操作成功');
  reload();
}

async function handleExport() {
  const data = await getTaskList(searchInfo);
  exportTask(data);
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
  fileList.value.forEach((file) => formData.append('file', file));
  try {
    await importTask(formData);
    createMessage.success('导入成功');
    reload();
    fileList.value = [];
  } catch (error) {
    createMessage.error('导入失败');
  }
}

// 基地切换时按基地过滤并刷新列表
watch(() => selectedBase.value?.baseId, (newBaseId) => {
  searchInfo.baseId = newBaseId;
  reload();
});
</script>

<style scoped>
.task-page { padding: 16px; }
</style>