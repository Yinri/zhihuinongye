<template>
  <div>
    <BasicTable @register="registerTable" :searchInfo="searchInfo">
      <template #toolbar>
        <a-button type="primary" @click="handleCreate"> 新增 </a-button>
        <a-button type="primary" @click="handleExport"> 导出 </a-button>
        <a-upload
          :showUploadList="false"
          :customRequest="handleImport"
          accept=".xlsx, .xls"
        >
          <a-button type="primary"> 导入 </a-button>
        </a-upload>
        <a-button
          type="primary"
          danger
          :disabled="!selectedRowKeys || selectedRowKeys.length === 0"
          @click="handleBatchDelete"
        >
          批量删除
        </a-button>
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'action'">
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
      </template>
    </BasicTable>
    <PesticideInfoModal @register="registerModal" @success="handleSuccess" />
  </div>
</template>
<script lang="ts">
  import { defineComponent, reactive, ref, watch } from 'vue';

  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useSelectStore } from '/@/store/selectStore';

  import { columns, searchFormSchema } from './pesticideInfo.data';
  import PesticideInfoModal from './PesticideInfoModal.vue';

  import {
    getPesticideInfoList,
    deletePesticideInfo,
    deleteBatchPesticideInfo,
    exportPesticideInfoXls,
    importPesticideInfoExcel,
  } from '/@/api/rapeseed/pesticideInfo.api';

  export default defineComponent({
    name: 'PesticideInfo',
    components: { BasicTable, TableAction, PesticideInfoModal },
    setup() {
      const [registerModal, { openModal }] = useModal();
      const { createMessage } = useMessage();
      const selectStore = useSelectStore();
      
      const searchInfo = reactive<Recordable>({});
      const selectedRowKeys = ref<string[]>([]);
      const [registerTable, { reload, getSelectRows, setLoading, clearSelectedRowKeys }] = useTable({
        title: '农药信息列表',
        api: getPesticideInfoList,
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
          fixed: 'right',
        },
        rowSelection: {
          type: 'checkbox',
          selectedRowKeys: selectedRowKeys,
          onChange: (keys: string[]) => {
            selectedRowKeys.value = keys;
          },
        },
      });

      watch(
        () => selectStore.selectedBase.baseId,
        () => {
          reload();
        }
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
        try {
          setLoading(true);
          await deletePesticideInfo({ id: record.id });
          createMessage.success('删除成功');
          reload();
        } catch (error) {
          createMessage.error('删除失败');
        } finally {
          setLoading(false);
        }
      }

      async function handleBatchDelete() {
        if (!selectedRowKeys.value || selectedRowKeys.value.length === 0) {
          createMessage.warning('请选择要删除的数据');
          return;
        }
        try {
          setLoading(true);
          const ids = selectedRowKeys.value.join(',');
          await deleteBatchPesticideInfo({ ids });
          createMessage.success('批量删除成功');
          selectedRowKeys.value = [];
          clearSelectedRowKeys();
          reload();
        } catch (error) {
          createMessage.error('批量删除失败');
        } finally {
          setLoading(false);
        }
      }

      async function handleExport() {
        try {
          setLoading(true);
          const params = {
            ...searchInfo,
          };
          await exportPesticideInfoXls(params);
          createMessage.success('导出成功');
        } catch (error) {
          createMessage.error('导出失败');
        } finally {
          setLoading(false);
        }
      }

      async function handleImport({ file }) {
        try {
          setLoading(true);
          const formData = new FormData();
          formData.append('file', file);
          await importPesticideInfoExcel(formData);
          createMessage.success('导入成功');
          reload();
        } catch (error) {
          createMessage.error('导入失败');
        } finally {
          setLoading(false);
        }
      }

      function handleSuccess() {
        reload();
      }

      return {
        registerTable,
        registerModal,
        handleCreate,
        handleEdit,
        handleDelete,
        handleBatchDelete,
        handleExport,
        handleImport,
        handleSuccess,
        searchInfo,
        selectedRowKeys,
      };
    },
  });
</script>