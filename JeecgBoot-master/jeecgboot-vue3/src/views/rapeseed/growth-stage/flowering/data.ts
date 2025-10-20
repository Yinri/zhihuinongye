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
    title: '管理日期',
    dataIndex: 'managementDate',
    width: 120,
  },
  {
    title: '管理类型',
    dataIndex: 'managementType',
    width: 120,
    customRender: ({ record }) => {
      const type = record.managementType;
      const color = type === '0' ? 'blue' : type === '1' ? 'green' : type === '2' ? 'orange' : 'purple';
      const text = type === '0' ? '初花期管理' : type === '1' ? '盛花期管理' : type === '2' ? '末花期管理' : '病虫害防治';
      return h(Tag, { color }, () => text);
    },
  },
  {
    title: '开花率(%)',
    dataIndex: 'floweringRate',
    width: 120,
    customRender: ({ record }) => {
      return `${record.floweringRate} %`;
    },
  },
  {
    title: '主花序长度(cm)',
    dataIndex: 'mainInflorescenceLength',
    width: 140,
    customRender: ({ record }) => {
      return `${record.mainInflorescenceLength} cm`;
    },
  },
  {
    title: '授粉率(%)',
    dataIndex: 'pollinationRate',
    width: 120,
    customRender: ({ record }) => {
      return `${record.pollinationRate} %`;
    },
  },
  {
    title: '施肥量(kg/亩)',
    dataIndex: 'fertilizerAmount',
    width: 120,
    customRender: ({ record }) => {
      return record.fertilizerAmount ? `${record.fertilizerAmount} kg/亩` : '-';
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
    field: 'managementType',
    label: '管理类型',
    component: 'Select',
    componentProps: {
      options: [
        { label: '初花期管理', value: '0' },
        { label: '盛花期管理', value: '1' },
        { label: '末花期管理', value: '2' },
        { label: '病虫害防治', value: '3' },
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
    field: 'managementDate',
    label: '管理日期',
    required: true,
    component: 'DatePicker',
    componentProps: {
      style: { width: '100%' },
    },
  },
  {
    field: 'managementType',
    label: '管理类型',
    required: true,
    component: 'Select',
    componentProps: {
      options: [
        { label: '初花期管理', value: '0' },
        { label: '盛花期管理', value: '1' },
        { label: '末花期管理', value: '2' },
        { label: '病虫害防治', value: '3' },
      ],
    },
  },
  {
    field: 'floweringRate',
    label: '开花率(%)',
    required: true,
    component: 'InputNumber',
    componentProps: {
      placeholder: '请输入开花率',
      min: 0,
      max: 100,
      precision: 1,
    },
  },
  {
    field: 'mainInflorescenceLength',
    label: '主花序长度(cm)',
    required: true,
    component: 'InputNumber',
    componentProps: {
      placeholder: '请输入主花序长度',
      min: 0,
      precision: 1,
    },
  },
  {
    field: 'pollinationRate',
    label: '授粉率(%)',
    component: 'InputNumber',
    componentProps: {
      placeholder: '请输入授粉率',
      min: 0,
      max: 100,
      precision: 1,
    },
  },
  {
    field: 'fertilizerAmount',
    label: '施肥量(kg/亩)',
    component: 'InputNumber',
    componentProps: {
      placeholder: '请输入施肥量',
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