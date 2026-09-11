import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { h } from 'vue';
import { Tag } from 'ant-design-vue';

// 表格列配置
export const columns: BasicColumn[] = [
  {
    title: '作业编号',
    dataIndex: 'workCode',
    width: 120,
    align: 'center',
  },
  {
    title: '作业类型',
    dataIndex: 'workType',
    width: 120,
    align: 'center',
    customRender: ({ text }) => {
      const type = text || '0';
      const color = type === '0' ? 'blue' : type === '1' ? 'green' : type === '2' ? 'orange' : 'purple';
      const typeText = type === '0' ? '播种' : type === '1' ? '移栽' : type === '2' ? '收割' : '其他';
      return h(Tag, { color }, () => typeText);
    },
  },
  {
    title: '地块名称',
    dataIndex: 'plotName',
    width: 150,
  },
  {
    title: '作业面积(亩)',
    dataIndex: 'workArea',
    width: 120,
    align: 'center',
  },
  {
    title: '作业日期',
    dataIndex: 'workDate',
    width: 120,
    align: 'center',
  },
  {
    title: '作业人员',
    dataIndex: 'workerName',
    width: 120,
    align: 'center',
  },
  {
    title: '作业设备',
    dataIndex: 'equipment',
    width: 120,
    align: 'center',
  },
  {
    title: '作业状态',
    dataIndex: 'workStatus',
    width: 100,
    align: 'center',
    customRender: ({ text }) => {
      const status = text || '0';
      const color = status === '0' ? 'green' : status === '1' ? 'orange' : 'red';
      const statusText = status === '0' ? '已完成' : status === '1' ? '进行中' : '计划中';
      return h(Tag, { color }, () => statusText);
    },
  },
  {
    title: '作业时长(小时)',
    dataIndex: 'workDuration',
    width: 120,
    align: 'center',
  },
  {
    title: '作业成本(元)',
    dataIndex: 'workCost',
    width: 120,
    align: 'center',
  },
  {
    title: '备注',
    dataIndex: 'remarks',
    width: 150,
    ellipsis: true,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 150,
    align: 'center',
  },
];

// 查询表单配置
export const searchFormSchema: FormSchema[] = [
  {
    field: 'workCode',
    label: '作业编号',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    field: 'workType',
    label: '作业类型',
    component: 'Select',
    componentProps: {
      options: [
        { label: '播种', value: '0' },
        { label: '移栽', value: '1' },
        { label: '收割', value: '2' },
        { label: '其他', value: '3' },
      ],
    },
    colProps: { span: 6 },
  },
  {
    field: 'plotName',
    label: '地块名称',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    field: 'workStatus',
    label: '作业状态',
    component: 'Select',
    componentProps: {
      options: [
        { label: '已完成', value: '0' },
        { label: '进行中', value: '1' },
        { label: '计划中', value: '2' },
      ],
    },
    colProps: { span: 6 },
  },
  {
    field: 'workDate',
    label: '作业日期',
    component: 'RangePicker',
    colProps: { span: 6 },
  },
];

// 表单字段配置
export const formSchema: FormSchema[] = [
  {
    field: 'id',
    label: '主键',
    component: 'Input',
    show: false,
  },
  {
    field: 'workCode',
    label: '作业编号',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: '请输入作业编号!' }],
  },
  {
    field: 'workType',
    label: '作业类型',
    component: 'Select',
    defaultValue: '0',
    componentProps: {
      options: [
        { label: '播种', value: '0' },
        { label: '移栽', value: '1' },
        { label: '收割', value: '2' },
        { label: '其他', value: '3' },
      ],
    },
    rules: [{ required: true, message: '请选择作业类型!' }],
  },
  {
    field: 'plotId',
    label: '地块',
    component: 'Select',
    componentProps: {
      api: () => {
        // 这里应该调用获取地块列表的API
        return Promise.resolve([
          { label: '地块A', value: '1' },
          { label: '地块B', value: '2' },
          { label: '地块C', value: '3' },
        ]);
      },
      labelField: 'label',
      valueField: 'value',
      immediate: true,
    },
    rules: [{ required: true, message: '请选择地块!' }],
  },
  {
    field: 'workArea',
    label: '作业面积(亩)',
    component: 'InputNumber',
    required: true,
    componentProps: {
      min: 0,
      precision: 2,
      placeholder: '请输入作业面积',
    },
    rules: [{ required: true, message: '请输入作业面积!' }],
  },
  {
    field: 'workDate',
    label: '作业日期',
    component: 'DatePicker',
    required: true,
    rules: [{ required: true, message: '请选择作业日期!' }],
  },
  {
    field: 'workerName',
    label: '作业人员',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: '请输入作业人员!' }],
  },
  {
    field: 'equipment',
    label: '作业设备',
    component: 'Input',
  },
  {
    field: 'workStatus',
    label: '作业状态',
    component: 'Select',
    defaultValue: '0',
    componentProps: {
      options: [
        { label: '已完成', value: '0' },
        { label: '进行中', value: '1' },
        { label: '计划中', value: '2' },
      ],
    },
  },
  {
    field: 'workDuration',
    label: '作业时长(小时)',
    component: 'InputNumber',
    componentProps: {
      min: 0,
      precision: 2,
      placeholder: '请输入作业时长',
    },
  },
  {
    field: 'workCost',
    label: '作业成本(元)',
    component: 'InputNumber',
    componentProps: {
      min: 0,
      precision: 2,
      placeholder: '请输入作业成本',
    },
  },
  {
    field: 'remarks',
    label: '备注',
    component: 'InputTextArea',
    componentProps: {
      rows: 3,
      placeholder: '请输入备注',
    },
  },
];