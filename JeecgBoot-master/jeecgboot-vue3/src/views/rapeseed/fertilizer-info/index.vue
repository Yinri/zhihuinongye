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
    <FertilizerInfoModal @register="registerModal" @success="handleSuccess" />
  </div>
</template>
<script lang="ts">
  import { defineComponent, reactive } from 'vue';

  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';

  import { columns, searchFormSchema } from './fertilizerInfo.data';
  import FertilizerInfoModal from './FertilizerInfoModal.vue';

  import {
    getFertilizerInfoList,
    deleteFertilizerInfo,
    deleteBatchFertilizerInfo,
    exportFertilizerInfoXls,
    importFertilizerInfoExcel,
  } from '/@/api/rapeseed/fertilizerInfo.api';

  export default defineComponent({
    name: 'FertilizerInfo',
    components: { BasicTable, TableAction, FertilizerInfoModal },
    setup() {
      const [registerModal, { openModal }] = useModal();
      const { createMessage } = useMessage();
      
      const searchInfo = reactive<Recordable>({});
      const [registerTable, { reload, getSelectRows, setLoading }] = useTable({
        title: '肥料信息列表',
        api: getFertilizerInfoList,
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
        try {
          setLoading(true);
          await deleteFertilizerInfo({ id: record.id });
          createMessage.success('删除成功');
          reload();
        } catch (error) {
          createMessage.error('删除失败');
        } finally {
          setLoading(false);
        }
      }

      async function handleBatchDelete() {
        try {
          setLoading(true);
          const ids = getSelectRows().map((item) => item.id).join(',');
          await deleteBatchFertilizerInfo({ ids });
          createMessage.success('批量删除成功');
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
          await exportFertilizerInfoXls(params);
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
          await importFertilizerInfoExcel(formData);
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
      };
    },
  });
</script>