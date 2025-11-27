import { BasicColumn, FormSchema } from '/@/components/Table';
import { h } from 'vue';
import { Tag } from 'ant-design-vue';

export const columns: BasicColumn[] = [
  {
    title: '记录编号',
    dataIndex: 'recordCode',
    width: 120,
  },
  {
    title: '基地名称',
    dataIndex: 'baseName',
    width: 150,
  },
  {
    title: '地块名称',
    dataIndex: 'plotName',
    width: 150,
  },
  {
    title: '农事类型',
    dataIndex: 'farmingType',
    width: 120,
    customRender: ({ record }) => {
      const type = record.farmingType;
      const typeMap = {
        1: { text: '播种', color: 'blue' },
        2: { text: '施肥', color: 'green' },
        3: { text: '灌溉', color: 'cyan' },
        4: { text: '除草', color: 'orange' },
        5: { text: '病虫害防治', color: 'red' },
        6: { text: '收获', color: 'purple' },
        7: { text: '其他', color: 'default' },
      };
      const config = typeMap[type] || { text: '未知', color: 'default' };
      return h(Tag, { color: config.color }, () => config.text);
    },
  },
  {
    title: '农事日期',
    dataIndex: 'farmingDate',
    width: 180,
  },
  {
    title: '作业人员',
    dataIndex: 'worker',
    width: 120,
  },
  {
    title: '作业面积(亩)',
    dataIndex: 'workArea',
    width: 120,
  },
  {
    title: '使用物资',
    dataIndex: 'materials',
    width: 150,
    ellipsis: true,
  },
  {
    title: '物资用量',
    dataIndex: 'materialAmount',
    width: 120,
  },
  {
    title: '作业时长(小时)',
    dataIndex: 'workDuration',
    width: 120,
  },
  {
    title: '作业状态',
    dataIndex: 'workStatus',
    width: 120,
    customRender: ({ record }) => {
      const status = record.workStatus;
      const statusMap = {
        1: { text: '计划中', color: 'blue' },
        2: { text: '进行中', color: 'processing' },
        3: { text: '已完成', color: 'success' },
        4: { text: '已取消', color: 'error' },
      };
      const config = statusMap[status] || { text: '未知', color: 'default' };
      return h(Tag, { color: config.color }, () => config.text);
    },
  },
  {
    title: '备注',
    dataIndex: 'remark',
    width: 200,
    ellipsis: true,
    customRender: ({ text }) => {
      if (!text) return '-';
      return text;
    },
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'recordCode',
    label: '记录编号',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'plotName',
    label: '地块名称',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'farmingType',
    label: '农事类型',
    component: 'Select',
    componentProps: {
      options: [
        { label: '播种', value: 1 },
        { label: '施肥', value: 2 },
        { label: '灌溉', value: 3 },
        { label: '除草', value: 4 },
        { label: '病虫害防治', value: 5 },
        { label: '收获', value: 6 },
        { label: '其他', value: 7 },
      ],
    },
    colProps: { span: 8 },
  },
  {
    field: 'farmingDate',
    label: '农事日期',
    component: 'RangePicker',
    componentProps: {
      showTime: true,
      format: 'YYYY-MM-DD HH:mm:ss',
    },
    colProps: { span: 8 },
  },
  {
    field: 'workStatus',
    label: '作业状态',
    component: 'Select',
    componentProps: {
      options: [
        { label: '计划中', value: 1 },
        { label: '进行中', value: 2 },
        { label: '已完成', value: 3 },
        { label: '已取消', value: 4 },
      ],
    },
    colProps: { span: 8 },
  },
];

export const formSchema: FormSchema[] = [
  {
    field: 'id',
    label: 'ID',
    component: 'Input',
    show: false,
  },
  {
    field: 'recordCode',
    label: '记录编号',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: '请输入记录编号' }],
    show: false,
  },
  {
    field: 'baseId',
    label: '基地ID',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: '请输入基地ID' }],
    show: false,
  },
  {
    field: 'plotId',
    label: '地块ID',
    component: 'Input',
    show: false,
  },
  {
    field: 'farmingType',
    label: '农事类型',
    component: 'Select',
    required: true,
    componentProps: {
      options: [
        { label: '播种', value: 1 },
        { label: '施肥', value: 2 },
        { label: '灌溉', value: 3 },
        { label: '除草', value: 4 },
        { label: '病虫害防治', value: 5 },
        { label: '收获', value: 6 },
        { label: '其他', value: 7 },
      ],
    },
    rules: [{ required: true, message: '请选择农事类型' }],
  },
  {
    field: 'farmingDate',
    label: '农事日期',
    component: 'DatePicker',
    componentProps: {
      showTime: true,
      format: 'YYYY-MM-DD HH:mm:ss',
    },
  },
  {
    field: 'worker',
    label: '作业人员',
    component: 'Input',
  },
  {
    field: 'workArea',
    label: '作业面积(亩)',
    component: 'InputNumber',
    componentProps: {
      min: 0,
      precision: 2,
    },
  },
  {
    field: 'materials',
    label: '使用物资',
    component: 'Input',
    helpMessage: '多个物资用逗号分隔',
  },
  {
    field: 'materialAmount',
    label: '物资用量',
    component: 'Input',
    helpMessage: '与物资顺序对应，用逗号分隔',
  },
  {
    field: 'workDuration',
    label: '作业时长(小时)',
    component: 'InputNumber',
    componentProps: {
      min: 0,
      precision: 2,
    },
  },
  {
    field: 'workStatus',
    label: '作业状态',
    component: 'Select',
    componentProps: {
      options: [
        { label: '计划中', value: 1 },
        { label: '进行中', value: 2 },
        { label: '已完成', value: 3 },
        { label: '已取消', value: 4 },
      ],
    },
  },
  {
    field: 'remark',
    label: '备注',
    component: 'InputTextArea',
  },
];
