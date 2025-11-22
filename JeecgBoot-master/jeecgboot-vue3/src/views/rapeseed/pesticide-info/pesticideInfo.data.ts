import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';

// 农药信息表表格列定义
export const columns: BasicColumn[] = [
  {
    title: '农药编码',
    dataIndex: 'pesticideCode',
    width: 120,
    align: 'center',
  },
  {
    title: '农药名称',
    dataIndex: 'pesticideName',
    width: 150,
    align: 'center',
  },
  {
    title: '农药类型',
    dataIndex: 'pesticideType',
    width: 120,
    align: 'center',
    customRender: ({ text }) => {
      const typeMap = {
        '1': '杀虫剂',
        '2': '杀菌剂',
        '3': '除草剂',
        '4': '植物生长调节剂',
      };
      return typeMap[text] || text;
    },
  },
  {
    title: '有效成分',
    dataIndex: 'activeIngredient',
    width: 120,
    align: 'center',
  },
  {
    title: '有效成分含量(%)',
    dataIndex: 'activeIngredientContent',
    width: 120,
    align: 'center',
  },
  {
    title: '毒性级别',
    dataIndex: 'toxicityLevel',
    width: 100,
    align: 'center',
    customRender: ({ text }) => {
      const levelMap = {
        '1': '微毒',
        '2': '低毒',
        '3': '中等毒',
        '4': '高毒',
        '5': '剧毒',
      };
      return levelMap[text] || text;
    },
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

// 农药信息表查询表单配置
export const searchFormSchema: FormSchema[] = [
  {
    field: 'pesticideCode',
    label: '农药编码',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'pesticideName',
    label: '农药名称',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'pesticideType',
    label: '农药类型',
    component: 'Select',
    componentProps: {
      options: [
        { label: '杀虫剂', value: '1' },
        { label: '杀菌剂', value: '2' },
        { label: '除草剂', value: '3' },
        { label: '植物生长调节剂', value: '4' },
      ],
    },
    colProps: { span: 8 },
  },
];

// 农药信息表表单配置
export const formSchema: FormSchema[] = [
  {
    field: 'pesticideCode',
    label: '农药编码',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: '请输入农药编码' }],
  },
  {
    field: 'pesticideName',
    label: '农药名称',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: '请输入农药名称' }],
  },
  {
    field: 'pesticideType',
    label: '农药类型',
    component: 'Select',
    required: true,
    componentProps: {
      options: [
        { label: '杀虫剂', value: '1' },
        { label: '杀菌剂', value: '2' },
        { label: '除草剂', value: '3' },
        { label: '植物生长调节剂', value: '4' },
      ],
    },
    rules: [{ required: true, message: '请选择农药类型' }],
  },
  {
    field: 'activeIngredient',
    label: '有效成分',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: '请输入有效成分' }],
  },
  {
    field: 'activeIngredientContent',
    label: '有效成分含量(%)',
    component: 'InputNumber',
    componentProps: {
      min: 0,
      max: 100,
      precision: 2,
      formatter: (value) => `${value}%`,
      parser: (value) => value.replace('%', ''),
    },
    required: true,
    rules: [{ required: true, message: '请输入有效成分含量' }],
  },
  {
    field: 'toxicityLevel',
    label: '毒性级别',
    component: 'Select',
    required: true,
    componentProps: {
      options: [
        { label: '微毒', value: '1' },
        { label: '低毒', value: '2' },
        { label: '中等毒', value: '3' },
        { label: '高毒', value: '4' },
        { label: '剧毒', value: '5' },
      ],
    },
    rules: [{ required: true, message: '请选择毒性级别' }],
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