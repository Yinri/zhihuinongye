import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { h } from 'vue';
import { Tag } from 'ant-design-vue';

// 表格列配置
export const columns: BasicColumn[] = [
  {
    title: '田块编号',
    dataIndex: 'fieldCode',
    width: 120,
    align: 'left',
  },
  {
    title: '田块名称',
    dataIndex: 'fieldName',
    width: 150,
    align: 'left',
  },
  {
    title: '播种面积(亩)',
    dataIndex: 'area',
    width: 120,
    customRender: ({ record }) => {
      return `${record.area} 亩`;
    },
  },
  {
    title: '播种日期',
    dataIndex: 'sowingDate',
    width: 120,
  },
  {
    title: '播种深度(cm)',
    dataIndex: 'depth',
    width: 120,
    customRender: ({ record }) => {
      return `${record.depth} cm`;
    },
  },
  {
    title: '播种量(kg/亩)',
    dataIndex: 'seedingRate',
    width: 120,
    customRender: ({ record }) => {
      return `${record.seedingRate} kg/亩`;
    },
  },
  {
    title: '当前状态',
    dataIndex: 'status',
    width: 100,
    customRender: ({ record }) => {
      const status = record.status;
      const color = status === '1' ? 'green' : status === '2' ? 'blue' : 'orange';
      const text = status === '1' ? '进行中' : status === '2' ? '已完成' : '计划中';
      return h(Tag, { color }, () => text);
    },
  },
  {
    title: '负责人',
    dataIndex: 'personInCharge',
    width: 120,
  },
  {
    title: '备注',
    dataIndex: 'remark',
    width: 150,
  },
];

// 搜索表单配置
export const searchFormSchema: FormSchema[] = [
  {
    field: 'fieldCode',
    label: '田块编号',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'fieldName',
    label: '田块名称',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'status',
    label: '状态',
    component: 'Select',
    componentProps: {
      options: [
        { label: '计划中', value: '0' },
        { label: '进行中', value: '1' },
        { label: '已完成', value: '2' },
      ],
    },
    colProps: { span: 8 },
  },
];

// 表单字段配置
export const formSchema: FormSchema[] = [
  {
    field: 'fieldCode',
    label: '田块编号',
    required: true,
    component: 'Input',
    componentProps: {
      placeholder: '请输入田块编号',
    },
  },
  {
    field: 'fieldName',
    label: '田块名称',
    required: true,
    component: 'Input',
    componentProps: {
      placeholder: '请输入田块名称',
    },
  },
  {
    field: 'area',
    label: '播种面积(亩)',
    required: true,
    component: 'InputNumber',
    componentProps: {
      placeholder: '请输入播种面积',
      min: 0,
      precision: 2,
    },
  },
  {
    field: 'sowingDate',
    label: '播种日期',
    required: true,
    component: 'DatePicker',
    componentProps: {
      style: { width: '100%' },
    },
  },
  {
    field: 'depth',
    label: '播种深度(cm)',
    required: true,
    component: 'InputNumber',
    componentProps: {
      placeholder: '请输入播种深度',
      min: 0,
      precision: 1,
    },
  },
  {
    field: 'seedingRate',
    label: '播种量(kg/亩)',
    required: true,
    component: 'InputNumber',
    componentProps: {
      placeholder: '请输入播种量',
      min: 0,
      precision: 2,
    },
  },
  {
    field: 'rowSpacing',
    label: '行距(cm)',
    required: true,
    component: 'InputNumber',
    componentProps: {
      placeholder: '请输入行距',
      min: 0,
      precision: 1,
    },
  },
  {
    field: 'plantSpacing',
    label: '株距(cm)',
    required: true,
    component: 'InputNumber',
    componentProps: {
      placeholder: '请输入株距',
      min: 0,
      precision: 1,
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
    field: 'remark',
    label: '备注',
    component: 'InputTextArea',
    componentProps: {
      placeholder: '请输入备注',
      rows: 4,
    },
  },
];