<template>
  <div class="production-plan-page">
    <!-- 基地选择区域 -->
    <a-card :bordered="false" class="base-select-card">
      <BaseSelect 
        v-model:value="selectedBaseId" 
        @change="handleBaseChange" 
        :defaultBaseId="defaultBaseId"
      />
    </a-card>

    <!-- 生产计划列表区域 -->
    <a-card :bordered="false" v-if="selectedBaseId" class="table-card">
      
      <BasicTable @register="registerTable" :rowSelection="rowSelection" :loading="loading">
        <!--插槽:table标题-->
        <template #tableTitle>
          <a-space>
            <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleCreate"> 新增</a-button>
            <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
            <j-upload-button type="primary" preIcon="ant-design:import-outlined" @click="onImportXls">导入</j-upload-button>
            <a-dropdown v-if="selectedRowKeys.length > 0">
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
            <!-- 数据刷新按钮 -->
            <a-button type="default" preIcon="ant-design:reload-outlined" @click="handleRefresh" :loading="loading"> 刷新</a-button>
          </a-space>
        </template>
        <!--操作栏-->
        <template #action="{ record }">
          <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
        </template>
        <!-- 空状态自定义 -->
        <template #emptyText>
          <a-empty description="该基地暂无生产计划数据">
            <template #image>
              <Icon icon="ant-design:inbox-outlined" style="font-size: 64px; color: #d9d9d9;" />
            </template>
            <a-button type="primary" @click="handleCreate">立即创建</a-button>
          </a-empty>
        </template>
      </BasicTable>
    </a-card>

    <!-- 无数据提示 -->
    <a-card :bordered="false" v-if="selectedBaseId && !loading && dataSource.length === 0" class="empty-card">
      <a-result
        status="404"
        title="该基地暂无生产计划数据"
        sub-title="您可以点击下方按钮创建新的生产计划"
      >
        <template #icon>
          <Icon icon="ant-design:inbox-outlined" style="font-size: 64px; color: #d9d9d9;" />
        </template>
        <template #extra>
          <a-button type="primary" @click="handleCreate">创建生产计划</a-button>
        </template>
      </a-result>
    </a-card>

    <!-- 未选择基地提示 -->
    <a-card :bordered="false" v-if="!selectedBaseId" class="empty-card">
      <a-result
        status="info"
        title="请先选择基地"
        sub-title="选择基地后即可查看和管理该基地的生产计划"
      >
        <template #icon>
          <Icon icon="ant-design:home-outlined" style="font-size: 64px; color: #1890ff;" />
        </template>
      </a-result>
    </a-card>

    <!-- 加载状态提示 -->
    <a-card :bordered="false" v-if="loading && selectedBaseId" class="loading-card">
      <a-skeleton active :paragraph="{ rows: 5 }" />
    </a-card>

    <!-- 生产计划模态框 -->
    <ProductionPlanModal @register="registerModal" @success="handleSuccess" :baseId="selectedBaseId" />
  </div>
</template>

<script lang="ts" name="rapeseed-production-plan" setup>
  import { ref, computed, onMounted } from 'vue';
import { BasicTable, TableAction, ActionItem } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useListPage } from '/@/hooks/system/useListPage';
import { useMessage } from '/@/hooks/web/useMessage';
import ProductionPlanModal from './ProductionPlanModal.vue';
import { columns, searchFormSchema } from './productionPlan.data';
import { getProductionPlanList, deleteProductionPlan, batchDeleteProductionPlan, getImportUrl, getExportUrl, getProductionPlanByBaseId } from './productionPlan.api';
import BaseSelect from './components/BaseSelect.vue';
import { Icon } from '/@/components/Icon';

  const { createMessage } = useMessage();
  
  // 基地选择相关
  const selectedBaseId = ref<string | number | undefined>(undefined);
  const baseSelectRef = ref();
  const loading = ref(false);
  
  // 注册model
  const [registerModal, { openModal }] = useModal();

  // 列表页面公共参数、方法
  const { prefixCls, tableContext, onExportXls, onImportXls } = useListPage({
    designScope: 'production-plan-list',
    tableProps: {
      title: '生产计划管理',
      api: selectedBaseId.value ? getProductionPlanByBaseId : getProductionPlanList,
      columns: columns,
      size: 'small',
      formConfig: {
        schemas: searchFormSchema,
      },
      actionColumn: {
        width: 120,
      },
      beforeFetch: (params) => {
        // 如果有选中的基地ID，添加到查询参数
        if (selectedBaseId.value) {
          params.baseId = selectedBaseId.value;
        }
        return Object.assign({ column: 'createTime', order: 'desc' }, params);
      },
      // 使用自定义的fetch方法来支持缓存
      fetchSetting: {
        pageField: 'pageNo',
        sizeField: 'pageSize',
        listField: 'records',
        totalField: 'total',
      },
    },
    exportConfig: {
      name: '生产计划列表',
      url: getExportUrl,
    },
    importConfig: {
      url: getImportUrl,
    },
  });

  // 注册table数据
  const [registerTable, { reload, updateTableDataRecord, getForm }, { rowSelection, selectedRows, selectedRowKeys, dataSource }] = tableContext;

  /**
   * 基地加载完成回调
   */
  function handleBaseLoaded(baseOptions) {
    // 可以在这里处理基地加载完成的逻辑
    console.log('基地列表加载完成:', baseOptions);
  }

  /**
   * 基地选择变化处理
   */
  function handleBaseChange(baseId) {
    selectedBaseId.value = baseId;
    // 清空搜索表单
    if (getForm) {
      getForm().resetFields();
    }
    // 重新加载数据
    loadProductionPlanData();
  }

  /**
   * 刷新数据
   */
  const handleRefresh = async () => {
    if (!selectedBaseId.value) {
      createMessage.warning('请先选择基地');
      return;
    }
    
    try {
      loading.value = true;
      await reload();
      createMessage.success('数据刷新成功');
    } catch (error) {
      console.error('刷新数据失败:', error);
      createMessage.error('数据刷新失败，请稍后重试');
    } finally {
      loading.value = false;
    }
  };

  /**
   * 加载生产计划数据
   */
  async function loadProductionPlanData() {
    if (!selectedBaseId.value) {
      return;
    }
    
    try {
      loading.value = true;
      // 直接从服务器获取数据
      await reload();
      createMessage.success('数据加载成功');
      
    } catch (error) {
      console.error('加载生产计划数据失败:', error);
      createMessage.error('加载生产计划数据失败，请稍后重试');
    } finally {
      loading.value = false;
    }
  }

  /**
   * 新增事件
   */
  function handleCreate() {
    if (!selectedBaseId.value) {
      createMessage.warning('请先选择基地');
      return;
    }
    openModal(true, {
      isUpdate: false,
      baseId: selectedBaseId.value,
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
    await deleteProductionPlan({ id: record.id }, () => {
      reload();
    });
  }
  
  /**
   * 批量删除事件
   */
  async function batchHandleDelete() {
    await batchDeleteProductionPlan({ ids: selectedRowKeys.value }, () => {
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

  // 组件挂载时，可以设置默认基地ID
  onMounted(() => {
    // 这里可以从用户信息或系统设置中获取默认基地ID
    // const defaultBaseId = getDefaultBaseId();
    // if (defaultBaseId) {
    //   selectedBaseId.value = defaultBaseId;
    //   loadProductionPlanData();
    // }
  });
</script>
<style lang="less" scoped>
.production-plan-page {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 64px);
}

.page-header {
  margin-bottom: 24px;
  padding: 24px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  
  .page-title {
    font-size: 24px;
    font-weight: 600;
    color: #262626;
    margin-bottom: 8px;
    display: flex;
    align-items: center;
    gap: 8px;
  }
  
  .page-description {
    font-size: 14px;
    color: #8c8c8c;
  }
}

.base-select-card {
  margin-bottom: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.table-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  
  .table-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 500;
  }
}

.empty-card, .loading-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-top: 24px;
}

// 响应式布局
@media (max-width: 768px) {
  .production-plan-page {
    padding: 16px;
  }
  
  .page-header {
    padding: 16px;
    
    .page-title {
      font-size: 20px;
    }
  }
}
</style>
