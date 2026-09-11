<template>
  <div class="machine-page">
    <a-card :bordered="false" title="农机档案">
      <template #extra>
        <a-button @click="goBack">
          <Icon icon="ant-design:arrow-left-outlined" /> 返回收获管理
        </a-button>
      </template>
      <BasicTable @register="registerTable">
        <template #toolbar>
          <a-button type="primary" @click="handleCreate">
            <Icon icon="ant-design:plus-outlined" /> 新增
          </a-button>
          <a-button @click="handleExport">
            <Icon icon="ant-design:download-outlined" /> 导出
          </a-button>
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
import { ref, watch, computed } from 'vue';
import { BasicTable, useTable, TableAction } from '/@/components/Table';
import { Icon } from '/@/components/Icon';
import { useMessage } from '/@/hooks/web/useMessage';
import { useRouter } from 'vue-router';
import { columns } from './machine.data';
import { getMachineList, deleteMachine, saveMachine } from './machine.api';
import { getHarvestMachineList } from '../harvest.api';
import { useSelectStore } from '/@/store/selectStore';
import { storeToRefs } from 'pinia';
import MachineModal from './MachineModal.vue';
import { useModal } from '/@/components/Modal';

const { createMessage } = useMessage();
const router = useRouter();
const [registerModal, { openModal }] = useModal();

const selectStore = useSelectStore();
const { selectedBase } = storeToRefs(selectStore);

const machineData = ref<any[]>([]);
const loading = ref(false);

const selectedBaseName = computed(() => {
  const name = selectedBase.value?.baseName || '';
  return name.replace('基地', '');
});

const filteredMachineData = computed(() => {
  if (!selectedBaseName.value) return machineData.value;
  return machineData.value.filter(m => m.baseName === selectedBaseName.value);
});

async function loadMachineData() {
  loading.value = true;
  try {
    const list = await getHarvestMachineList();
    machineData.value = Array.isArray(list) ? list : [];
  } catch (error) {
    console.error('加载农机列表失败:', error);
    machineData.value = [];
  } finally {
    loading.value = false;
  }
}

const [registerTable] = useTable({
  title: '农机档案',
  columns,
  dataSource: filteredMachineData,
  loading: loading,
  showTableSetting: false,
  bordered: true,
  showIndexColumn: false,
  pagination: {
    defaultPageSize: 10,
    showSizeChanger: true,
    pageSizeOptions: ['10', '20', '50', '100'],
  },
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
  await deleteMachine({ id: record.id });
  createMessage.success('删除成功');
  loadMachineData();
}

function handleSuccess() {
  createMessage.success('操作成功');
  loadMachineData();
}

async function handleExport() {
  if (filteredMachineData.value.length === 0) {
    createMessage.warning('暂无数据可导出');
    return;
  }
  
  const headers = ['北斗设备编码', '车辆编号', '农机品牌', '农机型号', '机主姓名', '机主电话', '所属基地'];
  const rows = filteredMachineData.value.map(r => [
    r.beidouSn || '',
    r.vehicleNumber || '',
    r.brand || '',
    r.model || '',
    r.ownerName || '',
    r.ownerPhone || '',
    r.baseName || ''
  ]);
  
  const csvContent = [headers, ...rows].map(row => row.join(',')).join('\n');
  const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' });
  const link = document.createElement('a');
  link.href = URL.createObjectURL(blob);
  link.download = `农机档案_${new Date().toISOString().split('T')[0]}.csv`;
  link.click();
  URL.revokeObjectURL(link.href);
}

loadMachineData();

watch(() => selectedBase.value?.baseId, () => {
  loadMachineData();
});
</script>

<style scoped>
.machine-page { padding: 16px; }
</style>
