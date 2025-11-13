<template>
  <div class="variety-info-page">
    <!-- 品种信息列表区域 -->
    <a-card :bordered="false" class="table-card">
      
      <template #toolbar>
        <a-space>
          <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleCreate"> 新增</a-button>
          <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
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
      
      <BasicTable @register="registerTable" :rowSelection="rowSelection" :loading="loading">
        <!--操作栏-->
        <template #action="{ record }">
          <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
        </template>
        
        <!-- 空状态自定义 -->
        <template #emptyText>
          <a-empty description="该地块暂无品种信息数据">
            <template #image>
              <Icon icon="ant-design:file-text-outlined" style="font-size: 64px; color: #d9d9d9;" />
            </template>
            <a-button type="primary" @click="handleCreate">立即创建</a-button>
          </a-empty>
        </template>
      </BasicTable>
    </a-card>

    <!-- 无数据提示 -->
    <a-card :bordered="false" v-if="!loading && dataSource && dataSource.length === 0" class="empty-card">
      <a-result
        status="404"
        title="暂无品种信息数据"
        sub-title="您可以点击下方按钮创建新的品种信息"
      >
        <template #icon>
          <Icon icon="ant-design:file-text-outlined" style="font-size: 64px; color: #d9d9d9;" />
        </template>
        <template #extra>
          <a-button type="primary" @click="handleCreate">创建品种信息</a-button>
        </template>
      </a-result>
    </a-card>

    <!-- 加载状态提示 -->
    <a-card :bordered="false" v-if="loading" class="loading-card">
      <a-skeleton active :paragraph="{ rows: 5 }" />
    </a-card>

    <!-- 表单弹窗 -->
    <VarietyInfoModal @register="registerModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" name="rapeseed-variety-info" setup>
import { ref, onMounted } from 'vue';
import { BasicTable, TableAction, ActionItem } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useListPage } from '/@/hooks/system/useListPage';
import { useMessage } from '/@/hooks/web/useMessage';
import { Icon } from '/@/components/Icon';
import VarietyInfoModal from './VarietyInfoModal.vue';
import { columns, searchFormSchema } from './varietyInfo.data';
import { getVarietyInfoList, deleteVarietyInfo, batchDeleteVarietyInfo, getImportUrl, getExportUrl } from './varietyInfo.api';

const { createMessage } = useMessage();

// 加载状态
const loading = ref(false);

//注册model
const [registerModal, { openModal }] = useModal();

// 列表页面公共参数、方法
const { prefixCls, tableContext, onExportXls, onImportXls } = useListPage({
  designScope: 'variety-info-list',
  tableProps: {
    title: '品种信息管理',
    api: getVarietyInfoList,
    columns: columns,
    size: 'small',
    formConfig: {
      schemas: searchFormSchema,
    },
    actionColumn: {
      width: 120,
    },
    beforeFetch: (params) => {
      return Object.assign({ column: 'createTime', order: 'desc' }, params);
    },
  },
  exportConfig: {
    name: '品种信息列表',
    url: getExportUrl,
  },
  importConfig: {
    url: getImportUrl,
  },
});

//注册table数据
const [registerTable, { reload, updateTableDataRecord, dataSource }, { rowSelection, selectedRows, selectedRowKeys }] = tableContext;

/**
 * 新增事件
 */
function handleCreate() {
  openModal(true, {
    isUpdate: false,
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
  await deleteVarietyInfo({ id: record.id }, reload);
}

/**
 * 批量删除事件
 */
async function batchHandleDelete() {
  await batchDeleteVarietyInfo({ ids: selectedRowKeys.value }, () => {
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

// 组件挂载时
onMounted(() => {
  // 初始化加载数据
  reload();
});
</script>

<style lang="less" scoped>
.variety-info-page {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 64px);
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
