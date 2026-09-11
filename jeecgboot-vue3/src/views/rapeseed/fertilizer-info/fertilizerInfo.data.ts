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
  },
  {
    title: 'NPK比例',
    dataIndex: 'npkRatio',
    width: 100,
    align: 'center',
  },
  {
    title: '剂型',
    dataIndex: 'formulation',
    width: 100,
    align: 'center',
  },
  {
    title: '适用作物',
    dataIndex: 'applicationCrops',
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
    component: 'Input',
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
    componentProps: {
      maxlength: 50,
    },
    rules: [
      { required: true, message: '请输入肥料编码' },
      { max: 50, message: '肥料编码长度不能超过50个字符' },
    ],
  },
  {
    field: 'fertilizerName',
    label: '肥料名称',
    component: 'Input',
    required: true,
    componentProps: {
      maxlength: 100,
    },
    rules: [
      { required: true, message: '请输入肥料名称' },
      { max: 100, message: '肥料名称长度不能超过100个字符' },
    ],
  },
  {
    field: 'fertilizerType',
    label: '肥料类型',
    component: 'Select',
    required: true,
    componentProps: {
      options: [
        { label: '有机肥', value: '有机肥' },
        { label: '无机肥', value: '无机肥' },
        { label: '生物肥', value: '生物肥' },
        { label: '复合肥', value: '复合肥' },
      ],
      placeholder: '请选择肥料类型',
    },
    rules: [
      { required: true, message: '请选择肥料类型' },
    ],
  },
  {
    field: 'npkRatio',
    label: 'NPK比例',
    component: 'Input',
    componentProps: {
      placeholder: '如:15-15-15',
      maxlength: 20,
    },
  },
  {
    field: 'formulation',
    label: '剂型',
    component: 'Select',
    required: true,
    componentProps: {
      options: [
        { label: '颗粒', value: '颗粒' },
        { label: '粉末', value: '粉末' },
        { label: '液体', value: '液体' },
        { label: '水溶性粉剂', value: '水溶性粉剂' },
        { label: '悬浮剂', value: '悬浮剂' },
      ],
      placeholder: '请选择剂型',
    },
    rules: [
      { required: true, message: '请选择剂型' },
    ],
  },
  {
    field: 'applicationCrops',
    label: '适用作物',
    component: 'InputTextArea',
    componentProps: {
      placeholder: '请输入适用作物',
      rows: 3,
    },
  },
  {
    field: 'dosageRange',
    label: '推荐用量范围',
    component: 'Input',
    componentProps: {
      placeholder: '如:20-30kg/亩',
      maxlength: 100,
    },
  },
  {
    field: 'applicationMethod',
    label: '施用方法',
    component: 'Select',
    componentProps: {
      options: [
        { label: '基施', value: '基施' },
        { label: '追施', value: '追施' },
        { label: '叶面喷施', value: '叶面喷施' },
        { label: '滴灌', value: '滴灌' },
        { label: '冲施', value: '冲施' },
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