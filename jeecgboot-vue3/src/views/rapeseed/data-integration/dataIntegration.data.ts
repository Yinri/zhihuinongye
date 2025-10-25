import { FormSchema } from '/@/components/Form';
import { BasicColumn } from '/@/components/Table';

export const columns: BasicColumn[] = [
  {
    title: '对接编号',
    dataIndex: 'integrationCode',
    width: 120,
  },
  {
    title: '数据源名称',
    dataIndex: 'dataSourceName',
    width: 120,
  },
  {
    title: '数据源类型',
    dataIndex: 'dataSourceType',
    width: 120,
    customRender: ({ record }) => {
      const type = record.dataSourceType;
      const typeMap = {
        '1': { text: '数据库', color: 'blue' },
        '2': { text: 'API接口', color: 'green' },
        '3': { text: '文件', color: 'orange' },
        '4': { text: '物联网设备', color: 'purple' },
      };
      const config = typeMap[type] || { text: '未知', color: 'default' };
      return `<a-tag color="${config.color}">${config.text}</a-tag>`;
    },
  },
  {
    title: '对接方式',
    dataIndex: 'integrationMethod',
    width: 120,
    customRender: ({ record }) => {
      const method = record.integrationMethod;
      const methodMap = {
        '1': { text: '实时同步', color: 'green' },
        '2': { text: '定时同步', color: 'blue' },
        '3': { text: '手动同步', color: 'orange' },
      };
      const config = methodMap[method] || { text: '未知', color: 'default' };
      return `<a-tag color="${config.color}">${config.text}</a-tag>`;
    },
  },
  {
    title: '同步频率',
    dataIndex: 'syncFrequency',
    width: 120,
  },
  {
    title: '最后同步时间',
    dataIndex: 'lastSyncTime',
    width: 120,
  },
  {
    title: '同步状态',
    dataIndex: 'syncStatus',
    width: 120,
    customRender: ({ record }) => {
      const status = record.syncStatus;
      const statusMap = {
        '1': { text: '正常', color: 'green' },
        '2': { text: '异常', color: 'red' },
        '3': { text: '同步中', color: 'blue' },
        '4': { text: '未同步', color: 'gray' },
      };
      const config = statusMap[status] || { text: '未知', color: 'default' };
      return `<a-tag color="${config.color}">${config.text}</a-tag>`;
    },
  },
  {
    title: '数据量',
    dataIndex: 'dataCount',
    width: 120,
  },
  {
    title: '负责人',
    dataIndex: 'responsiblePerson',
    width: 120,
  },
  {
    title: '备注',
    dataIndex: 'remark',
    width: 120,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'integrationCode',
    label: '对接编号',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'dataSourceName',
    label: '数据源名称',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'dataSourceType',
    label: '数据源类型',
    component: 'Select',
    componentProps: {
      options: [
        { label: '数据库', value: '1' },
        { label: 'API接口', value: '2' },
        { label: '文件', value: '3' },
        { label: '物联网设备', value: '4' },
      ],
    },
    colProps: { span: 8 },
  },
  {
    field: 'integrationMethod',
    label: '对接方式',
    component: 'Select',
    componentProps: {
      options: [
        { label: '实时同步', value: '1' },
        { label: '定时同步', value: '2' },
        { label: '手动同步', value: '3' },
      ],
    },
    colProps: { span: 8 },
  },
  {
    field: 'syncStatus',
    label: '同步状态',
    component: 'Select',
    componentProps: {
      options: [
        { label: '正常', value: '1' },
        { label: '异常', value: '2' },
        { label: '同步中', value: '3' },
        { label: '未同步', value: '4' },
      ],
    },
    colProps: { span: 8 },
  },
];

export const formSchema: FormSchema[] = [
  {
    field: 'integrationCode',
    label: '对接编号',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: '请输入对接编号' }],
  },
  {
    field: 'dataSourceName',
    label: '数据源名称',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: '请输入数据源名称' }],
  },
  {
    field: 'dataSourceType',
    label: '数据源类型',
    component: 'Select',
    required: true,
    componentProps: {
      options: [
        { label: '数据库', value: '1' },
        { label: 'API接口', value: '2' },
        { label: '文件', value: '3' },
        { label: '物联网设备', value: '4' },
      ],
    },
    rules: [{ required: true, message: '请选择数据源类型' }],
  },
  {
    field: 'integrationMethod',
    label: '对接方式',
    component: 'Select',
    required: true,
    componentProps: {
      options: [
        { label: '实时同步', value: '1' },
        { label: '定时同步', value: '2' },
        { label: '手动同步', value: '3' },
      ],
    },
    rules: [{ required: true, message: '请选择对接方式' }],
  },
  {
    field: 'syncFrequency',
    label: '同步频率',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: '请输入同步频率' }],
  },
  {
    field: 'lastSyncTime',
    label: '最后同步时间',
    component: 'DatePicker',
    required: false,
  },
  {
    field: 'syncStatus',
    label: '同步状态',
    component: 'Select',
    required: true,
    componentProps: {
      options: [
        { label: '正常', value: '1' },
        { label: '异常', value: '2' },
        { label: '同步中', value: '3' },
        { label: '未同步', value: '4' },
      ],
    },
    rules: [{ required: true, message: '请选择同步状态' }],
  },
  {
    field: 'dataCount',
    label: '数据量',
    component: 'InputNumber',
    required: true,
    rules: [{ required: true, message: '请输入数据量' }],
  },
  {
    field: 'responsiblePerson',
    label: '负责人',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: '请输入负责人' }],
  },
  {
    field: 'remark',
    label: '备注',
    component: 'InputTextArea',
  },
];