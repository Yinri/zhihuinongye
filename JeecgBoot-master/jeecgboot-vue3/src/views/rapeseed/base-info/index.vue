<template>
  <div class="base-info-page">
    <a-card :bordered="false" class="table-card">
      <BasicTable @register="registerTable" :rowSelection="rowSelection" :loading="loading">
        <template #toolbar>
          <a-space>
            <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleCreate">新增</a-button>
            <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls">导出</a-button>
            <j-upload-button type="primary" preIcon="ant-design:import-outlined" @click="onImportXls">导入</j-upload-button>
            <a-dropdown v-if="selectedRowKeys && selectedRowKeys.length > 0">
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
        <template #action="{ record }">
          <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
        </template>
        <template #emptyText>
          <a-empty description="暂无基地信息">
            <template #image>
              <Icon icon="ant-design:file-text-outlined" style="font-size: 64px; color: #d9d9d9;" />
            </template>
            <a-button type="primary" @click="handleCreate">立即创建</a-button>
          </a-empty>
        </template>
      </BasicTable>
    </a-card>

    <BaseInfoModal @register="registerModal" @success="handleSuccess" />
  </div>
  </template>

<script lang="ts" name="rapeseed-base-info" setup>
import { ref, onMounted } from 'vue';
import { BasicTable, TableAction, ActionItem } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useListPage } from '/@/hooks/system/useListPage';
import { useMessage } from '/@/hooks/web/useMessage';
import { Icon } from '/@/components/Icon';
import BaseInfoModal from './components/BaseInfoModal.vue';
import { columns, searchFormSchema } from './baseInfo.data';
import { getBaseInfoList, deleteBaseInfo, batchDeleteBaseInfo, getImportUrl, getExportUrl } from './baseInfo.api';

const { createMessage } = useMessage();
const loading = ref(false);
const [registerModal, { openModal }] = useModal();

const { tableContext, onExportXls, onImportXls } = useListPage({
  designScope: 'base-info-list',
  tableProps: {
    title: '基地信息管理',
    api: getBaseInfoList,
    columns: columns,
    size: 'small',
    formConfig: { schemas: searchFormSchema },
    actionColumn: { width: 120 },
    beforeFetch: (params) => Object.assign({ column: 'createTime', order: 'desc' }, params),
  },
  exportConfig: { name: '基地信息列表', url: getExportUrl },
  importConfig: { url: getImportUrl },
});

const [registerTable, { reload, updateTableDataRecord, dataSource }, { rowSelection, selectedRows, selectedRowKeys }] = tableContext;

function handleCreate() {
  openModal(true, { isUpdate: false });
}

function handleEdit(record: Recordable) {
  openModal(true, { record, isUpdate: true });
}

async function handleDelete(record) {
  await deleteBaseInfo(record.id);
  createMessage.success('删除成功');
  reload();
}

async function batchHandleDelete() {
  await batchDeleteBaseInfo(selectedRowKeys.value);
  selectedRowKeys.value = [];
  createMessage.success('批量删除成功');
  reload();
}

function handleSuccess() {
  reload();
}

function getTableAction(record): ActionItem[] {
  return [
    { label: '编辑', onClick: handleEdit.bind(null, record) },
  ];
}

function getDropDownAction(record): ActionItem[] {
  return [
    { label: '删除', popConfirm: { title: '是否确认删除', confirm: handleDelete.bind(null, record) } },
  ];
}

onMounted(() => {
  reload();
});
</script>

<style lang="less" scoped>
.base-info-page {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 64px);
}
.table-card { margin-top: 16px; }
</style>
