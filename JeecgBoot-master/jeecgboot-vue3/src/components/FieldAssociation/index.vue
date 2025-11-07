<template>
  <a-modal
    v-model:open="visible"
    title="关联地块"
    width="800px"
    @ok="handleConfirm"
    @cancel="handleCancel"
  >
    <div class="field-association-content">
      <div class="polygon-info">
        <h4>当前地块信息</h4>
        <a-descriptions :column="2" bordered size="small">
          <a-descriptions-item label="地块名称">{{ polygonInfo.name }}</a-descriptions-item>
          <a-descriptions-item label="面积">{{ polygonInfo.area }} 亩</a-descriptions-item>
          <a-descriptions-item label="状态">{{ getStatusText(polygonInfo.status) }}</a-descriptions-item>
          <a-descriptions-item label="负责人">{{ polygonInfo.manager || '未指定' }}</a-descriptions-item>
        </a-descriptions>
      </div>
      
      <div class="association-section">
        <h4>选择要关联的真实地块</h4>
        <a-input-search
          v-model:value="searchKeyword"
          placeholder="搜索地块编号、名称或负责人"
          style="margin-bottom: 16px"
          @search="handleSearch"
        />
        
        <a-table
          :columns="columns"
          :data-source="filteredFields"
          :row-selection="rowSelection"
          :loading="loading"
          :pagination="pagination"
          size="small"
          @change="handleTableChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'status'">
              <a-tag :color="getStatusColor(record.status)">
                {{ getStatusText(record.status) }}
              </a-tag>
            </template>
            <template v-if="column.key === 'action'">
              <a-button type="link" size="small" @click="viewFieldDetail(record)">
                查看详情
              </a-button>
            </template>
          </template>
        </a-table>
      </div>
      
      <div class="association-note">
        <a-alert
          message="关联说明"
          description="选择一个真实地块与当前绘制的地块进行关联，关联后将在地图上显示该地块的详细信息。"
          type="info"
          show-icon
        />
      </div>
    </div>
    
    <!-- 地块详情弹窗 -->
    <a-modal
      v-model:open="detailModalVisible"
      title="地块详情"
      width="600px"
      :footer="null"
    >
      <a-descriptions v-if="selectedField" :column="2" bordered>
        <a-descriptions-item label="地块编号">{{ selectedField.fieldCode }}</a-descriptions-item>
        <a-descriptions-item label="地块名称">{{ selectedField.fieldName }}</a-descriptions-item>
        <a-descriptions-item label="面积">{{ selectedField.area }} 亩</a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="getStatusColor(selectedField.status)">
            {{ getStatusText(selectedField.status) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="负责人">{{ selectedField.manager }}</a-descriptions-item>
        <a-descriptions-item label="联系电话">{{ selectedField.phone }}</a-descriptions-item>
        <a-descriptions-item label="预计产量">{{ selectedField.expectedYield }} 吨</a-descriptions-item>
        <a-descriptions-item label="实际产量">{{ selectedField.actualYield }} 吨</a-descriptions-item>
        <a-descriptions-item label="收割进度">{{ selectedField.progress }}%</a-descriptions-item>
        <a-descriptions-item label="预计收割日期">{{ selectedField.estimatedDate }}</a-descriptions-item>
        <a-descriptions-item label="土壤类型">{{ selectedField.soilType }}</a-descriptions-item>
        <a-descriptions-item label="灌溉方式">{{ selectedField.irrigation }}</a-descriptions-item>
        <a-descriptions-item label="油菜品种">{{ selectedField.variety }}</a-descriptions-item>
        <a-descriptions-item label="种植日期">{{ selectedField.plantDate }}</a-descriptions-item>
        <a-descriptions-item label="备注" :span="2">{{ selectedField.remark || '无' }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </a-modal>
</template>

<script lang="ts" setup>
import { ref, reactive, computed, watch } from 'vue';
import { message } from 'ant-design-vue';
import type { TableColumnType } from 'ant-design-vue';

// 定义接口类型
interface FieldData {
  id: string;
  fieldCode: string;
  fieldName: string;
  area: number;
  status: 'pending' | 'harvesting' | 'completed';
  manager: string;
  phone: string;
  expectedYield: number;
  actualYield: number;
  progress: number;
  estimatedDate: string;
  soilType: string;
  irrigation: string;
  variety: string;
  plantDate: string;
  remark?: string;
}

interface PolygonInfo {
  id?: string;
  name: string;
  area: number;
  status: 'pending' | 'harvesting' | 'completed';
  manager: string;
  points: any[];
}

// 组件属性
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  polygonInfo: {
    type: Object as () => PolygonInfo,
    required: true
  }
});

// 事件定义
const emit = defineEmits(['update:visible', 'confirm']);

// 响应式数据
const searchKeyword = ref('');
const loading = ref(false);
const selectedFieldId = ref<string | null>(null);
const selectedField = ref<FieldData | null>(null);
const detailModalVisible = ref(false);

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 5,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条数据`
});

// 表格列配置
const columns: TableColumnType<FieldData>[] = [
  {
    title: '地块编号',
    dataIndex: 'fieldCode',
    key: 'fieldCode',
    width: 100
  },
  {
    title: '地块名称',
    dataIndex: 'fieldName',
    key: 'fieldName'
  },
  {
    title: '面积(亩)',
    dataIndex: 'area',
    key: 'area',
    width: 80
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 80
  },
  {
    title: '负责人',
    dataIndex: 'manager',
    key: 'manager'
  },
  {
    title: '操作',
    key: 'action',
    width: 80
  }
];

// 模拟地块数据
const allFields = ref<FieldData[]>([
  {
    id: '1',
    fieldCode: 'F001',
    fieldName: '东区1号地块',
    area: 25.5,
    status: 'completed',
    manager: '张三',
    phone: '13800138001',
    expectedYield: 12.8,
    actualYield: 13.2,
    progress: 100,
    estimatedDate: '2023-05-15',
    soilType: '壤土',
    irrigation: '滴灌',
    variety: '中双11号',
    plantDate: '2022-10-15',
    remark: '生长良好，产量稳定'
  },
  {
    id: '2',
    fieldCode: 'F002',
    fieldName: '东区2号地块',
    area: 32.0,
    status: 'harvesting',
    manager: '李四',
    phone: '13800138002',
    expectedYield: 16.0,
    actualYield: 8.5,
    progress: 65,
    estimatedDate: '2023-05-20',
    soilType: '沙壤土',
    irrigation: '喷灌',
    variety: '中双12号',
    plantDate: '2022-10-20',
    remark: '部分区域收割完成'
  },
  {
    id: '3',
    fieldCode: 'F003',
    fieldName: '西区1号地块',
    area: 28.3,
    status: 'pending',
    manager: '王五',
    phone: '13800138003',
    expectedYield: 14.2,
    actualYield: 0,
    progress: 0,
    estimatedDate: '2023-05-25',
    soilType: '黏土',
    irrigation: '漫灌',
    variety: '中双13号',
    plantDate: '2022-10-25',
    remark: '等待收割期'
  },
  {
    id: '4',
    fieldCode: 'F004',
    fieldName: '西区2号地块',
    area: 30.7,
    status: 'pending',
    manager: '赵六',
    phone: '13800138004',
    expectedYield: 15.4,
    actualYield: 0,
    progress: 0,
    estimatedDate: '2023-05-30',
    soilType: '壤土',
    irrigation: '滴灌',
    variety: '中双14号',
    plantDate: '2022-11-01',
    remark: '生长状况良好'
  },
  {
    id: '5',
    fieldCode: 'F005',
    fieldName: '南区1号地块',
    area: 27.8,
    status: 'harvesting',
    manager: '钱七',
    phone: '13800138005',
    expectedYield: 13.9,
    actualYield: 7.2,
    progress: 55,
    estimatedDate: '2023-06-01',
    soilType: '沙土',
    irrigation: '喷灌',
    variety: '中双15号',
    plantDate: '2022-11-05',
    remark: '收割进度正常'
  }
]);

// 计算属性
const visible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
});

// 过滤后的地块数据
const filteredFields = computed(() => {
  let result = allFields.value;
  
  // 搜索过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase();
    result = result.filter(field => 
      field.fieldCode.toLowerCase().includes(keyword) ||
      field.fieldName.toLowerCase().includes(keyword) ||
      field.manager.toLowerCase().includes(keyword)
    );
  }
  
  // 更新分页总数
  pagination.total = result.length;
  
  // 分页
  const start = (pagination.current - 1) * pagination.pageSize;
  const end = start + pagination.pageSize;
  return result.slice(start, end);
});

// 行选择配置
const rowSelection = computed(() => ({
  type: 'radio',
  selectedRowKeys: selectedFieldId.value ? [selectedFieldId.value] : [],
  onChange: (selectedRowKeys: string[]) => {
    selectedFieldId.value = selectedRowKeys.length > 0 ? selectedRowKeys[0] : null;
  }
}));

// 监听弹窗显示状态
watch(() => props.visible, (newVal) => {
  if (newVal) {
    // 重置搜索和选择
    searchKeyword.value = '';
    selectedFieldId.value = null;
    pagination.current = 1;
  }
});

// 方法
const getStatusColor = (status: string) => {
  switch (status) {
    case 'completed':
      return 'green';
    case 'harvesting':
      return 'orange';
    case 'pending':
      return 'red';
    default:
      return 'default';
  }
};

const getStatusText = (status: string) => {
  switch (status) {
    case 'completed':
      return '已完成';
    case 'harvesting':
      return '收割中';
    case 'pending':
      return '未开始';
    default:
      return '未知';
  }
};

const handleSearch = () => {
  pagination.current = 1;
};

const handleTableChange = (pag: any) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
};

const viewFieldDetail = (record: FieldData) => {
  selectedField.value = record;
  detailModalVisible.value = true;
};

const handleConfirm = () => {
  if (!selectedFieldId.value) {
    message.warning('请选择要关联的地块');
    return;
  }
  
  const selectedFieldData = allFields.value.find(field => field.id === selectedFieldId.value);
  if (selectedFieldData) {
    emit('confirm', {
      polygonId: props.polygonInfo.id,
      fieldData: selectedFieldData
    });
    message.success('地块关联成功');
  }
};

const handleCancel = () => {
  emit('update:visible', false);
};
</script>

<style scoped>
.field-association-content {
  max-height: 60vh;
  overflow-y: auto;
}

.polygon-info {
  margin-bottom: 20px;
}

.association-section {
  margin-bottom: 20px;
}

.association-note {
  margin-top: 16px;
}
</style>