<template>
  <div class="machine-page">
    <a-card :bordered="false" title="农机档案">
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
    <MachineModal @register="registerModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" setup>
import { reactive, ref, watch } from 'vue';
import { BasicTable, useTable, TableAction } from '/@/components/Table';
import { Icon } from '/@/components/Icon';
import { useMessage } from '/@/hooks/web/useMessage';
import { columns as baseColumns, searchFormSchema } from './machine.data';
import { getMachineList, deleteMachine, exportMachine, importMachine } from './machine.api';
import { getHarvesterStatus } from '../harvest.api';
import { h, computed } from 'vue';
import { useSelectStore } from '/@/store/selectStore';
import { storeToRefs } from 'pinia';
import MachineModal from './MachineModal.vue';
import { useModal } from '/@/components/Modal';

const { createMessage } = useMessage();
const [registerModal, { openModal }] = useModal();

const selectStore = useSelectStore();
const { selectedBase } = storeToRefs(selectStore);

const searchInfo = reactive<Recordable>({ baseId: selectedBase.value?.baseId });
const fileList = ref<any[]>([]);

const liveStatusDict = ref<Record<string, string>>({});

async function loadLiveStatus() {
  try {
    const baseId = selectedBase.value?.baseId;
    const params = baseId ? { baseId } : undefined; // 避免传递空值
    const list = await getHarvesterStatus(params);
    const dict: Record<string, string> = {};
    (Array.isArray(list) ? list : []).forEach((item: any) => { dict[item.name] = item.status; });
    liveStatusDict.value = dict;
  } catch (e) {
    // ignore
  }
}

const dynamicColumns = computed(() => {
  const liveCol = {
    title: '机器实时状态',
    dataIndex: 'liveStatus',
    width: 120,
    customRender: ({ record }) => {
      const status = liveStatusDict.value[record.machineName] || 'unknown';
      const label = status === 'working' ? '作业中' : status === 'idle' ? '待命' : status === 'maintenance' ? '维护中' : '未知';
      const color = status === 'working' ? '#52c41a' : status === 'idle' ? '#faad14' : '#8c8c8c';
      return h('span', { style: { color, fontWeight: 500 } }, label);
    },
  } as any;
  return [...baseColumns, liveCol];
});

const [registerTable, { reload }] = useTable({
  api: getMachineList,
  title: '农机档案',
  columns: dynamicColumns.value,
  formConfig: { labelWidth: 120, schemas: searchFormSchema, autoSubmitOnEnter: true },
  useSearchForm: true,
  showTableSetting: false,
  bordered: true,
  showIndexColumn: false,
});

function handleCreate() {
  openModal(true, { isUpdate: false });
}

function handleEdit(record: Recordable) {
  openModal(true, { record, isUpdate: true });
}

async function handleDelete(record: Recordable) {
  await deleteMachine(record.id);
  createMessage.success('删除成功');
  reload();
}

function handleSuccess() {
  createMessage.success('操作成功');
  reload();
}

async function handleExport() {
  const data = await getMachineList(searchInfo);
  exportMachine(data);
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
    await importMachine(formData);
    createMessage.success('导入成功');
    reload();
    fileList.value = [];
  } catch (error) {
    createMessage.error('导入失败');
  }
}

// 初始化实时状态字典
loadLiveStatus();

// 基地切换时按基地过滤并刷新列表与状态
watch(() => selectedBase.value?.baseId, (newBaseId) => {
  searchInfo.baseId = newBaseId;
  loadLiveStatus();
  reload();
});
</script>

<style scoped>
.machine-page { padding: 16px; }
</style>