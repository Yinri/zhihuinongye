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
    width: 130,
    align: 'center',
  },
  {
    title: '有效成分',
    dataIndex: 'activeIngredient',
    width: 120,
    align: 'center',
  },
  {
    title: '有效成分含量(%)',
    dataIndex: 'content',
    width: 130,
    align: 'center',
  },
  {
    title: '剂型',
    dataIndex: 'formulation',
    width: 120,
    align: 'center',
  },
  {
    title: '毒性级别',
    dataIndex: 'toxicityLevel',
    width: 100,
    align: 'center',
  },
  {
    title: '防治对象',
    dataIndex: 'targetPests',
    width: 150,
    align: 'center',
  },
  {
    title: '推荐用量范围',
    dataIndex: 'dosageRange',
    width: 150,
    align: 'center',
  },
  {
    title: '施用方法',
    dataIndex: 'applicationMethod',
    width: 150,
    align: 'center',
  },
  {
    title: '生产厂家',
    dataIndex: 'manufacturer',
    width: 150,
    align: 'center',
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
    component: 'Input',
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
    componentProps: {
      maxlength: 50,
    },
    rules: [
      { required: true, message: '请输入农药编码' },
      { max: 50, message: '农药编码长度不能超过50个字符' },
    ],
  },
  {
    field: 'pesticideName',
    label: '农药名称',
    component: 'Input',
    required: true,
    componentProps: {
      maxlength: 100,
    },
    rules: [
      { required: true, message: '请输入农药名称' },
      { max: 100, message: '农药名称长度不能超过100个字符' },
    ],
  },
  {
    field: 'pesticideType',
    label: '农药类型',
    component: 'Select',
    required: true,
    componentProps: {
      options: [
        { label: '杀虫剂', value: '杀虫剂' },
        { label: '杀菌剂', value: '杀菌剂' },
        { label: '除草剂', value: '除草剂' },
        { label: '植物生长调节剂', value: '植物生长调节剂' },
      ],
      placeholder: '请选择农药类型',
    },
    rules: [
      { required: true, message: '请选择农药类型' },
    ],
  },
  {
    field: 'activeIngredient',
    label: '有效成分',
    component: 'Input',
    required: true,
    componentProps: {
      maxlength: 100,
    },
    rules: [
      { required: true, message: '请输入有效成分' },
      { max: 100, message: '有效成分长度不能超过100个字符' },
    ],
  },
  {
    field: 'content',
    label: '有效成分含量(%)',
    component: 'InputNumber',
    componentProps: {
      min: 0.01,
      max: 100,
      precision: 2,
      placeholder: '请输入有效成分含量',
    },
    required: true,
    rules: [
      { required: true, message: '请输入有效成分含量' },
      { type: 'number', min: 0.01, max: 100, message: '有效成分含量范围:0.01-100' },
    ],
  },
  {
    field: 'formulation',
    label: '剂型',
    component: 'Select',
    required: true,
    componentProps: {
      options: [
        { label: '乳油', value: '乳油' },
        { label: '可湿性粉剂', value: '可湿性粉剂' },
        { label: '悬浮剂', value: '悬浮剂' },
        { label: '水乳剂', value: '水乳剂' },
        { label: '颗粒剂', value: '颗粒剂' },
        { label: '水分散粒剂', value: '水分散粒剂' },
      ],
      placeholder: '请选择剂型',
    },
    rules: [
      { required: true, message: '请选择剂型' },
    ],
  },
  {
    field: 'toxicityLevel',
    label: '毒性级别',
    component: 'Select',
    required: true,
    componentProps: {
      options: [
        { label: '微毒', value: '微毒' },
        { label: '低毒', value: '低毒' },
        { label: '中等毒', value: '中等毒' },
        { label: '高毒', value: '高毒' },
        { label: '剧毒', value: '剧毒' },
      ],
      placeholder: '请选择毒性级别',
    },
    rules: [
      { required: true, message: '请选择毒性级别' },
    ],
  },
  {
    field: 'targetPests',
    label: '防治对象',
    component: 'InputTextArea',
    componentProps: {
      placeholder: '请输入防治对象',
      rows: 3,
    },
  },
  {
    field: 'dosageRange',
    label: '推荐用量范围',
    component: 'Input',
    componentProps: {
      placeholder: '如:20-30ml/亩',
      maxlength: 100,
    },
  },
  {
    field: 'applicationMethod',
    label: '施用方法',
    component: 'Select',
    componentProps: {
      options: [
        { label: '叶面喷雾', value: '叶面喷雾' },
        { label: '土壤处理', value: '土壤处理' },
        { label: '灌根', value: '灌根' },
        { label: '拌种', value: '拌种' },
        { label: '沟施', value: '沟施' },
        { label: '浸种', value: '浸种' },
      ],
      placeholder: '请选择施用方法',
    },
  },
  {
    field: 'manufacturer',
    label: '生产厂家',
    component: 'Input',
    componentProps: {
      maxlength: 100,
    },
  },
];
