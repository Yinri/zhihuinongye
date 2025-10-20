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
      const text = type === '0' ? '间苗' : type === '1' ? '追肥' : type === '2' ? '病虫害防治' : '灌溉';
      return h(Tag, { color }, () => text);
    },
  },
  {
    title: '平均株高(cm)',
    dataIndex: 'avgHeight',
    width: 120,
    customRender: ({ record }) => {
      return `${record.avgHeight} cm`;
    },
  },
  {
    title: '叶片数',
    dataIndex: 'leafCount',
    width: 100,
    customRender: ({ record }) => {
      return `${record.leafCount} 片`;
    },
  },
  {
    title: '成活率(%)',
    dataIndex: 'survivalRate',
    width: 100,
    customRender: ({ record }) => {
      return `${record.survivalRate} %`;
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
        { label: '间苗', value: '0' },
        { label: '追肥', value: '1' },
        { label: '病虫害防治', value: '2' },
        { label: '灌溉', value: '3' },
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
        { label: '间苗', value: '0' },
        { label: '追肥', value: '1' },
        { label: '病虫害防治', value: '2' },
        { label: '灌溉', value: '3' },
      ],
    },
  },
  {
    field: 'avgHeight',
    label: '平均株高(cm)',
    required: true,
    component: 'InputNumber',
    componentProps: {
      placeholder: '请输入平均株高',
      min: 0,
      precision: 1,
    },
  },
  {
    field: 'leafCount',
    label: '叶片数',
    required: true,
    component: 'InputNumber',
    componentProps: {
      placeholder: '请输入叶片数',
      min: 0,
      precision: 0,
    },
  },
  {
    field: 'survivalRate',
    label: '成活率(%)',
    required: true,
    component: 'InputNumber',
    componentProps: {
      placeholder: '请输入成活率',
      min: 0,
      max: 100,
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