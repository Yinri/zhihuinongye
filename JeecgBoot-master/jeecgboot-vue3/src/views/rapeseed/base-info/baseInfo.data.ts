import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { rules } from '/@/utils/helper/validator';

export const columns: BasicColumn[] = [
  { title: '基地名称', dataIndex: 'baseName', width: 160 },
  { title: '负责人', dataIndex: 'manager', width: 120 },
  { title: '联系电话', dataIndex: 'phone', width: 140 },
  { title: '基地地址', dataIndex: 'address', width: 220 },
  { title: '经度', dataIndex: 'longitude', width: 100, align: 'center' },
  { title: '纬度', dataIndex: 'latitude', width: 100, align: 'center' },
  { title: '面积(亩)', dataIndex: 'area', width: 100, align: 'center' },
  { title: '土壤状况', dataIndex: 'soilType', width: 120, align: 'center' },
  { title: '创建时间', dataIndex: 'createTime', width: 160, align: 'center' },
];

export const searchFormSchema: FormSchema[] = [
  { field: 'baseName', label: '基地名称', component: 'Input', colProps: { span: 6 } },
  { field: 'manager', label: '负责人', component: 'Input', colProps: { span: 6 } },
  { field: 'phone', label: '联系电话', component: 'Input', colProps: { span: 6 } },
  {
    field: 'soilType',
    label: '土壤状况',
    component: 'Select',
    componentProps: {
      options: [
        { label: '黏土', value: '黏土' },
        { label: '沙土', value: '沙土' },
        { label: '壤土', value: '壤土' },
      ],
      allowClear: true,
    },
    colProps: { span: 6 },
  },
];

export const formSchema: FormSchema[] = [
  { field: 'id', label: '主键', component: 'Input', show: false },
  { field: 'baseName', label: '基地名称', component: 'Input', required: true, dynamicRules: ({ model, schema }) => rules.duplicateCheckRule('youcai_bases', 'base_name', model, schema, true) },
  { field: 'manager', label: '负责人', component: 'Input' },
  { field: 'phone', label: '联系电话', component: 'Input', dynamicRules: () => rules.phone(false) },
  { field: 'address', label: '基地地址', component: 'Input', required: true, rules: [{ required: true, message: '请输入基地地址' }] },
  { field: 'longitude', label: '经度', component: 'InputNumber', componentProps: { min: -180, max: 180, precision: 6 } },
  { field: 'latitude', label: '纬度', component: 'InputNumber', componentProps: { min: -90, max: 90, precision: 6 } },
  { field: 'area', label: '面积(亩)', component: 'InputNumber', required: true, defaultValue: 0, componentProps: { min: 0, precision: 2 }, rules: [{ required: true, message: '请输入基地面积' }] },
  {
    field: 'soilType',
    label: '土壤状况',
    component: 'Select',
    defaultValue: '黏土',
    componentProps: {
      options: [
        { label: '黏土', value: '黏土' },
        { label: '沙土', value: '沙土' },
        { label: '壤土', value: '壤土' },
      ],
    },
    required: true,
    rules: [{ required: true, message: '请选择土壤状况' }],
  },
];
