<template>
  <div>
    <!-- 设备状态概览 -->
    <a-card title="设备状态概览" :bordered="false" style="margin-bottom: 16px">
      <template #extra>
        <a-button type="link" @click="fetchDeviceStatus" :loading="deviceStatusLoading">
          <template #icon><ReloadOutlined /></template>
          刷新
        </a-button>
      </template>
      <a-row :gutter="16">
        <a-col :span="6" v-for="(device, key) in deviceStatusData" :key="key">
          <a-card 
            size="small" 
            :title="getDeviceName(key)" 
            :bordered="true"
            :hoverable="true"
            class="device-card"
          >
            <div v-if="device.length > 0" class="device-status-container">
              <div class="status-summary">
                <Statistic 
                  title="设备总数" 
                  :value="getTotalDeviceCount(device)" 
                  suffix="台"
                  :value-style="{ color: '#1890ff' }"
                />
              </div>
              <a-divider style="margin: 12px 0;" />
              <div class="status-details">
                <div v-for="(status, index) in device" :key="index" class="status-item">
                  <div class="status-indicator">
                    <a-badge 
                      :status="status.state === 1 ? 'success' : 'error'" 
                      :text="status.state === 1 ? '在线' : '离线'"
                    />
                  </div>
                  <div class="status-count">
                    <Statistic 
                      :value="status.count" 
                      suffix="台"
                      :value-style="{ 
                        color: status.state === 1 ? '#52c41a' : '#ff4d4f',
                        fontSize: '16px'
                      }"
                    />
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="no-device">
              <a-empty :image="null" description="暂无设备" />
            </div>
          </a-card>
        </a-col>
      </a-row>
    </a-card>

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
  import { reactive, ref, onMounted } from 'vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { ReloadOutlined } from '@ant-design/icons-vue';
  import { Statistic } from 'ant-design-vue';
  import { columns, searchFormSchema } from './dataIntegration.data';
  import { getDataIntegrationList, deleteDataIntegration, exportDataIntegration, importDataIntegration, getDeviceStatus } from './dataIntegration.api';
  import DataIntegrationModal from './DataIntegrationModal.vue';

  const { createMessage } = useMessage();
  const [registerModal, { openModal }] = useModal();
  const searchInfo = reactive<Recordable>({});
  const fileList = ref<any[]>([]);
  const deviceStatusData = ref<Recordable>({});
  const deviceStatusLoading = ref<boolean>(false);

  // 设备名称映射
  const deviceNameMap = {
    spInfo: '视频设备',
    qxInfo: '气象设备',
    trInfo: '土壤设备',
    szInfo: '水质设备',
    gpInfo: '光谱设备',
    cqInfo: '虫情设备',
    bzInfo: '孢子设备',
    scdInfo: '杀虫灯',
    dzInfo: '电子设备',
    zmInfo: '照明设备',
    sfInfo: '施肥设备',
  };

  // 获取设备名称
  function getDeviceName(key: string) {
    return deviceNameMap[key] || '未知设备';
  }

  // 获取设备总数
  function getTotalDeviceCount(device: any[]) {
    return device.reduce((total, status) => total + status.count, 0);
  }

  // 获取设备状态数据
  async function fetchDeviceStatus() {
    deviceStatusLoading.value = true;
    try {
      const res = await getDeviceStatus();
      console.log('API返回的完整响应:', res);
      if (res.code === 1) {
        // 后端返回的数据结构是 Result.ok(result)，其中result包含了实际的API响应
        const data = res.data;
        deviceStatusData.value = data;
        console.log('设备状态数据:', data);
      } else {
        console.log('请求失败:', res);
      }
    } catch (error) {
      console.error('获取设备状态失败:', error);
      createMessage.error('获取设备状态失败');
    } finally {
      deviceStatusLoading.value = false;
    }
  }

  // 页面加载时获取设备状态
  onMounted(() => {
    fetchDeviceStatus();
  });

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
    // 操作成功后刷新设备状态
    fetchDeviceStatus();
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
      // 导入成功后刷新设备状态
      fetchDeviceStatus();
    } catch (error) {
      createMessage.error('导入失败');
    }
  }
</script>

<style scoped>
.device-card {
  height: 100%;
  transition: all 0.3s ease;
}

.device-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.device-status-container {
  padding: 8px 0;
}

.status-summary {
  text-align: center;
  margin-bottom: 8px;
}

.status-details {
  margin-top: 8px;
}

.status-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  padding: 8px;
  border-radius: 6px;
  background-color: #fafafa;
}

.status-item:last-child {
  margin-bottom: 0;
}

.status-indicator {
  flex: 1;
  min-width: 80px;
}

.status-count {
  flex: 1;
  text-align: center;
  min-width: 80px;
}

.no-device {
  padding: 20px 0;
  text-align: center;
}
</style>
