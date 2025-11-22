import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';

// 肥料信息表表格列定义
export const columns: BasicColumn[] = [
  {
    title: '肥料编码',
    dataIndex: 'fertilizerCode',
    width: 120,
    align: 'center',
  },
  {
    title: '肥料名称',
    dataIndex: 'fertilizerName',
    width: 150,
    align: 'center',
  },
  {
    title: '肥料类型',
    dataIndex: 'fertilizerType',
    width: 120,
    align: 'center',
    customRender: ({ text }) => {
      const typeMap = {
        '1': '有机肥',
        '2': '无机肥',
        '3': '微生物肥',
        '4': '复合肥',
      };
      return typeMap[text] || text;
    },
  },
  {
    title: '氮含量(%)',
    dataIndex: 'nitrogenContent',
    width: 100,
    align: 'center',
  },
  {
    title: '磷含量(%)',
    dataIndex: 'phosphorusContent',
    width: 100,
    align: 'center',
  },
  {
    title: '钾含量(%)',
    dataIndex: 'potassiumContent',
    width: 100,
    align: 'center',
  },
  {
    title: '生产厂家',
    dataIndex: 'manufacturer',
    width: 150,
    align: 'center',
  },
  {
    title: '规格',
    dataIndex: 'specification',
    width: 120,
    align: 'center',
  },
  {
    title: '单价(元)',
    dataIndex: 'unitPrice',
    width: 100,
    align: 'center',
  },
  {
    title: '状态',
    dataIndex: 'status',
    width: 80,
    align: 'center',
    customRender: ({ text }) => {
      return text === '1' ? '启用' : '禁用';
    },
  },
];

// 肥料信息表查询表单配置
export const searchFormSchema: FormSchema[] = [
  {
    field: 'fertilizerCode',
    label: '肥料编码',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'fertilizerName',
    label: '肥料名称',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'fertilizerType',
    label: '肥料类型',
    component: 'Select',
    componentProps: {
      options: [
        { label: '有机肥', value: '1' },
        { label: '无机肥', value: '2' },
        { label: '微生物肥', value: '3' },
        { label: '复合肥', value: '4' },
      ],
    },
    colProps: { span: 8 },
  },
];

// 肥料信息表表单配置
export const formSchema: FormSchema[] = [
  {
    field: 'fertilizerCode',
    label: '肥料编码',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: '请输入肥料编码' }],
  },
  {
    field: 'fertilizerName',
    label: '肥料名称',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: '请输入肥料名称' }],
  },
  {
    field: 'fertilizerType',
    label: '肥料类型',
    component: 'Select',
    required: true,
    componentProps: {
      options: [
        { label: '有机肥', value: '1' },
        { label: '无机肥', value: '2' },
        { label: '微生物肥', value: '3' },
        { label: '复合肥', value: '4' },
      ],
    },
    rules: [{ required: true, message: '请选择肥料类型' }],
  },
  {
    field: 'nitrogenContent',
    label: '氮含量(%)',
    component: 'InputNumber',
    componentProps: {
      min: 0,
      max: 100,
      precision: 2,
      formatter: (value) => `${value}%`,
      parser: (value) => value.replace('%', ''),
    },
    rules: [{ required: true, message: '请输入氮含量' }],
  },
  {
    field: 'phosphorusContent',
    label: '磷含量(%)',
    component: 'InputNumber',
    componentProps: {
      min: 0,
      max: 100,
      precision: 2,
      formatter: (value) => `${value}%`,
      parser: (value) => value.replace('%', ''),
    },
    rules: [{ required: true, message: '请输入磷含量' }],
  },
  {
    field: 'potassiumContent',
    label: '钾含量(%)',
    component: 'InputNumber',
    componentProps: {
      min: 0,
      max: 100,
      precision: 2,
      formatter: (value) => `${value}%`,
      parser: (value) => value.replace('%', ''),
    },
    rules: [{ required: true, message: '请输入钾含量' }],
  },
  {
    field: 'manufacturer',
    label: '生产厂家',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: '请输入生产厂家' }],
  },
  {
    field: 'specification',
    label: '规格',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: '请输入规格' }],
  },
  {
    field: 'unitPrice',
    label: '单价(元)',
    component: 'InputNumber',
    componentProps: {
      min: 0,
      precision: 2,
      formatter: (value) => `¥ ${value}`,
      parser: (value) => value.replace('¥ ', ''),
    },
    required: true,
    rules: [{ required: true, message: '请输入单价' }],
  },
  {
    field: 'status',
    label: '状态',
    component: 'Select',
    defaultValue: '1',
    componentProps: {
      options: [
        { label: '启用', value: '1' },
        { label: '禁用', value: '0' },
      ],
    },
  },
  {
    field: 'remark',
    label: '备注',
    component: 'InputTextArea',
  },
];