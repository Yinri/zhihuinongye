import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';

// 表格列配置
export const columns: BasicColumn[] = [
  {
    title: '品种名称',
    dataIndex: 'varietyName',
    width: 150,
    align: 'center',
  },
  {
    title: '生长周期(天)',
    dataIndex: 'growthCycle',
    width: 120,
    align: 'center',
  },
  {
    title: '产量潜力(kg/亩)',
    dataIndex: 'yieldPotential',
    width: 130,
    align: 'center',
  },
  {
    title: '抗病性',
    dataIndex: 'diseaseResistance',
    width: 120,
    align: 'center',
  },
  {
    title: '收获系数',
    dataIndex: 'harvestCoefficient',
    width: 100,
    align: 'center',
  },
  {
    title: '田间保苗率(%)',
    dataIndex: 'seedlingSurvivalRate',
    width: 130,
    align: 'center',
  },
  {
    title: '结实率(%)',
    dataIndex: 'seedSettingRate',
    width: 100,
    align: 'center',
  },
  {
    title: '单株有效角果数(个)',
    dataIndex: 'singlePlantPods',
    width: 150,
    align: 'center',
  },
  {
    title: '每角果粒数(粒)',
    dataIndex: 'seedsPerPod',
    width: 130,
    align: 'center',
  },
  {
    title: '千粒重(g)',
    dataIndex: 'thousandGrainWeight',
    width: 100,
    align: 'center',
  },
  {
    title: '发芽率(%)',
    dataIndex: 'germinationRate',
    width: 100,
    align: 'center',
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
    field: 'varietyName',
    label: '品种名称',
    component: 'Input',
    colProps: { span: 8 },
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
    field: 'varietyName',
    label: '品种名称',
    component: 'Input',
    required: true,
    componentProps: {
      maxlength: 255,
      placeholder: '请输入品种名称',
    },
    rules: [{ required: true, message: '请输入品种名称!' }],
  },
  {
    field: 'growthCycle',
    label: '生长周期(天)',
    component: 'InputNumber',
    componentProps: {
      min: 0,
      placeholder: '请输入生长周期',
    },
  },
  {
    field: 'yieldPotential',
    label: '产量潜力(kg/亩)',
    component: 'InputNumber',
    componentProps: {
      min: 0,
      precision: 2,
      placeholder: '请输入产量潜力',
    },
  },
  {
    field: 'diseaseResistance',
    label: '抗病性',
    component: 'Input',
    componentProps: {
      maxlength: 200,
      placeholder: '请输入抗病性描述',
    },
  },
  {
    field: 'harvestCoefficient',
    label: '收获系数',
    component: 'InputNumber',
    defaultValue: 0.45,
    componentProps: {
      min: 0.3,
      max: 0.5,
      precision: 2,
      step: 0.01,
      placeholder: '范围:0.3-0.5',
    },
  },
  {
    field: 'seedlingSurvivalRate',
    label: '田间保苗率(%)',
    component: 'InputNumber',
    defaultValue: 85,
    componentProps: {
      min: 50,
      max: 100,
      placeholder: '范围:50-100',
    },
  },
  {
    field: 'seedSettingRate',
    label: '结实率(%)',
    component: 'InputNumber',
    defaultValue: 85,
    componentProps: {
      min: 70,
      max: 95,
      placeholder: '范围:70-95',
    },
  },
  {
    field: 'singlePlantPods',
    label: '单株有效角果数(个)',
    component: 'InputNumber',
    defaultValue: 300,
    componentProps: {
      min: 200,
      max: 400,
      placeholder: '范围:200-400',
    },
  },
  {
    field: 'seedsPerPod',
    label: '每角果粒数(粒)',
    component: 'InputNumber',
    defaultValue: 20,
    componentProps: {
      min: 15,
      max: 25,
      placeholder: '范围:15-25',
    },
  },
  {
    field: 'thousandGrainWeight',
    label: '千粒重(g)',
    component: 'InputNumber',
    defaultValue: 3.5,
    componentProps: {
      min: 2.5,
      max: 4.0,
      precision: 1,
      step: 0.1,
      placeholder: '范围:2.5-4.0',
    },
  },
  {
    field: 'germinationRate',
    label: '发芽率(%)',
    component: 'InputNumber',
    defaultValue: 90,
    componentProps: {
      min: 85,
      max: 95,
      placeholder: '范围:85-95',
    },
  },
  {
    field: 'characteristics',
    label: '品种特性',
    component: 'InputTextArea',
    componentProps: {
      rows: 4,
      placeholder: '请输入品种特性描述',
    },
  },
];