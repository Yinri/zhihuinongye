import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { h } from 'vue';
import { Tag } from 'ant-design-vue';

// 表格列配置
export const columns: BasicColumn[] = [
  {
    title: '计划名称',
    dataIndex: 'planName',
    width: 200,
    align: 'left',
  },
  {
    title: '种植面积',
    dataIndex: 'area',
    width: 120,
    customRender: ({ record }) => {
      return `${record.area} 亩`;
    },
  },
  {
    title: '预计产量',
    dataIndex: 'expectedYield',
    width: 120,
    customRender: ({ record }) => {
      return `${record.expectedYield} 公斤/亩`;
    },
  },
  {
    title: '计划开始时间',
    dataIndex: 'planStartTime',
    width: 150,
  },
  {
    title: '计划结束时间',
    dataIndex: 'planEndTime',
    width: 150,
  },
  {
    title: '当前状态',
    dataIndex: 'status',
    width: 100,
    customRender: ({ record }) => {
      const status = record.status;
      const color = status === '1' ? 'green' : status === '2' ? 'blue' : 'orange';
      const text = status === '1' ? '进行中' : status === '2' ? '已完成' : '未开始';
      return h(Tag, { color }, () => text);
    },
  },
  {
    title: '负责人',
    dataIndex: 'personInCharge',
    width: 120,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 150,
  },
];

// 搜索表单配置
export const searchFormSchema: FormSchema[] = [
  {
    field: 'planName',
    label: '计划名称',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'status',
    label: '状态',
    component: 'Select',
    componentProps: {
      options: [
        { label: '未开始', value: '0' },
        { label: '进行中', value: '1' },
        { label: '已完成', value: '2' },
      ],
    },
    colProps: { span: 8 },
  },
  {
    field: 'personInCharge',
    label: '负责人',
    component: 'Input',
    colProps: { span: 8 },
  },
];

// 表单字段配置
export const formSchema: FormSchema[] = [
  {
    field: 'planName',
    label: '计划名称',
    required: true,
    component: 'Input',
    componentProps: {
      placeholder: '请输入计划名称',
    },
  },
  {
    field: 'area',
    label: '种植面积(亩)',
    required: true,
    component: 'InputNumber',
    componentProps: {
      placeholder: '请输入种植面积',
      min: 0,
      precision: 2,
    },
  },
  {
    field: 'expectedYield',
    label: '预计产量(公斤/亩)',
    required: true,
    component: 'InputNumber',
    componentProps: {
      placeholder: '请输入预计产量',
      min: 0,
      precision: 2,
    },
  },
  {
    field: 'planStartTime',
    label: '计划开始时间',
    required: true,
    component: 'DatePicker',
    componentProps: {
      style: { width: '100%' },
    },
  },
  {
    field: 'planEndTime',
    label: '计划结束时间',
    required: true,
    component: 'DatePicker',
    componentProps: {
      style: { width: '100%' },
    },
  },
  {
    field: 'personInCharge',
    label: '负责人',
    required: true,
    component: 'Input',
    componentProps: {
      placeholder: '请输入负责人姓名',
    },
  },
  {
    field: 'description',
    label: '计划描述',
    component: 'InputTextArea',
    componentProps: {
      placeholder: '请输入计划描述',
      rows: 4,
    },
  },
];