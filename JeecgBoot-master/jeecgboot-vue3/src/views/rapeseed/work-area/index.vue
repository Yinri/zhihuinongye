<template>
  <div>
    <Monitor></Monitor>
    <MonitorPic></MonitorPic>
<!--    <BasicTable @register="registerTable" :searchInfo="searchInfo">-->
<!--      <template #toolbar>-->
<!--        <a-button type="primary" @click="handleCreate"> 新增 </a-button>-->
<!--        <a-button @click="handleExport"> 导出 </a-button>-->
<!--        <Upload-->
<!--          :showUploadList="false"-->
<!--          :beforeUpload="beforeUpload"-->
<!--          :customRequest="handleImport"-->
<!--        >-->
<!--          <a-button> 导入 </a-button>-->
<!--        </Upload>-->
<!--      </template>-->
<!--      <template #bodyCell="{ column, record }">-->
<!--        <template v-if="column.dataIndex === 'action'">-->
<!--          <TableAction-->
<!--            :actions="[-->
<!--              {-->
<!--                icon: 'clarity:note-edit-line',-->
<!--                onClick: handleEdit.bind(null, record),-->
<!--              },-->
<!--              {-->
<!--                icon: 'ant-design:delete-outlined',-->
<!--                color: 'error',-->
<!--                popConfirm: {-->
<!--                  title: '是否确认删除',-->
<!--                  placement: 'leftTop',-->
<!--                  confirm: handleDelete.bind(null, record),-->
<!--                },-->
<!--              },-->
<!--            ]"-->
<!--          />-->
<!--        </template>-->
<!--      </template>-->
<!--    </BasicTable>-->
<!--    <WorkAreaModal @register="registerModal" @success="handleSuccess" />-->
  </div>
</template>
<script lang="ts" setup>
  import { reactive, ref, onMounted } from 'vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { columns, searchFormSchema } from './workArea.data';
  import Monitor from "@/views/rapeseed/work-area/components/Monitor.vue";
  import WorkAreaModal from './WorkAreaModal.vue';
  import { 
    getWorkAreaList, 
    deleteWorkArea, 
    exportWorkArea, 
    importWorkArea 
  } from './workArea.api';
  import { Upload } from 'ant-design-vue';
  import { BaseSelect } from '/@/views/components/BaseSelect';
  import { PlotSelect } from '/@/views/components/PlotSelect';
  import { GrowthTimeline } from '/@/views/components/GrowthTimeline';
  import { Card, Row, Col, Button, Empty, Spin } from 'ant-design-vue';
  import MonitorPic from "@/views/rapeseed/work-area/components/MonitorPic.vue";

  const searchInfo = reactive<Recordable>({});
  const fileList = ref<any[]>([]);
  const loading = ref(false);
  const { createMessage } = useMessage();
  
  // 基地和地块选择相关状态
  const selectedBaseId = ref<string | number | undefined>(undefined);
  const selectedBaseName = ref<string>('');
  const selectedPlotId = ref<string | number | undefined>(undefined);
  const selectedPlotName = ref<string>('');
  const selectedVarietyId = ref<string | number | undefined>(undefined);
  const selectedVarietyName = ref<string>('');
  const currentStageId = ref<string | number | undefined>(undefined);
  
  // 组件引用
  const baseSelectRef = ref(null);
  const plotSelectRef = ref(null);
  
  // 默认基地ID（可以从URL参数或用户设置中获取）
  const defaultBaseId = ref<string | number | undefined>(undefined);

  const [registerTable, { reload, getSelectRows, dataSource }] = useTable({
    api: selectedPlotId.value ? (params) => {
      // 如果有选中的地块ID，添加到查询参数
      if (selectedPlotId.value) {
        params.plotId = selectedPlotId.value;
      }
      return getWorkAreaList(params);
    } : getWorkAreaList,
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
    beforeFetch: (params) => {
      // 如果有选中的基地ID或地块ID，添加到查询参数
      if (selectedBaseId.value) {
        params.baseId = selectedBaseId.value;
      }
      if (selectedPlotId.value) {
        params.plotId = selectedPlotId.value;
      }
      return params;
    },
  });

  const [registerModal, { openModal }] = useModal();

  /**
   * 基地选择变化处理
   */
  function handleBaseChange(baseId) {
    selectedBaseId.value = baseId;
    
    // 获取基地名称
    if (baseSelectRef.value && baseSelectRef.value.baseOptions) {
      const base = baseSelectRef.value.baseOptions.find(item => item.id === baseId);
      selectedBaseName.value = base ? base.baseName : '';
    }
    
    // 清空地块选择
    selectedPlotId.value = undefined;
    selectedPlotName.value = '';
    
    // 清空品种信息
    selectedVarietyId.value = undefined;
    selectedVarietyName.value = '';
    currentStageId.value = undefined;
    
    // 重新加载数据
    loadWorkAreaData();
  }

  /**
   * 地块选择变化处理
   */
  function handlePlotChange(plotId) {
    selectedPlotId.value = plotId;
    
    // 获取地块名称
    if (plotSelectRef.value && plotSelectRef.value.plotOptions) {
      const plot = plotSelectRef.value.plotOptions.find(item => item.id === plotId);
      selectedPlotName.value = plot ? plot.plotName : '';
      
      // 获取品种信息（这里假设地块信息中包含品种信息）
      if (plot) {
        selectedVarietyId.value = plot.varietyId;
        selectedVarietyName.value = plot.varietyName || '';
        // 这里可以根据实际情况设置当前阶段ID
        // currentStageId.value = plot.currentStageId;
      }
    }
    
    // 重新加载数据
    loadWorkAreaData();
  }

  /**
 * 加载作业区域数据
 */
  async function loadWorkAreaData() {
    if (!selectedBaseId.value) {
      return;
    }
    
    try {
      loading.value = true;
      // 重新加载数据
      await reload();
    } catch (error) {
      console.error('加载作业区域数据失败:', error);
      createMessage.error('加载作业区域数据失败，请稍后重试');
    } finally {
      loading.value = false;
    }
  }

  function handleCreate() {
    if (!selectedBaseId.value || !selectedPlotId.value) {
      createMessage.warning('请先选择基地和地块');
      return;
    }
    
    openModal(true, {
      isUpdate: false,
      baseId: selectedBaseId.value,
      plotId: selectedPlotId.value,
    });
  }

  function handleEdit(record: Recordable) {
    openModal(true, {
      record,
      isUpdate: true,
    });
  }

  async function handleDelete(record: Recordable) {
    await deleteWorkArea(record.id);
    createMessage.success('删除成功');
    reload();
  }

  function handleSuccess() {
    createMessage.success('操作成功');
    reload();
  }

  async function handleExport() {
    const data = await getWorkAreaList(searchInfo);
    exportWorkArea(data);
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
      await importWorkArea(formData);
      createMessage.success('导入成功');
      reload();
      fileList.value = [];
    } catch (error) {
      createMessage.error('导入失败');
    }
  }

  // 组件挂载时，可以设置默认基地ID
  onMounted(() => {
    // 这里可以从用户信息或系统设置中获取默认基地ID
    // const defaultBaseId = getDefaultBaseId();
    // if (defaultBaseId) {
    //   selectedBaseId.value = defaultBaseId;
    //   loadWorkAreaData();
    // }
    
    // 模拟设置默认基地ID，实际项目中应该从API或配置中获取
    // defaultBaseId.value = '1';
  });
</script>

