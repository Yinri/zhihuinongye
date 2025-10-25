import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { h } from 'vue';
import { Tag } from 'ant-design-vue';

// 表格列配置
export const columns: BasicColumn[] = [
  {
    title: '地块编号',
    dataIndex: 'plotCode',
    width: 120,
    align: 'center',
  },
  {
    title: '地块名称',
    dataIndex: 'plotName',
    width: 180,
  },
  {
    title: '地块面积(亩)',
    dataIndex: 'plotArea',
    width: 100,
    align: 'center',
  },
  {
    title: '地块类型',
    dataIndex: 'plotType',
    width: 100,
    align: 'center',
    customRender: ({ text }) => {
      const type = text || '0';
      const color = type === '0' ? 'blue' : type === '1' ? 'green' : 'orange';
      const typeText = type === '0' ? '水田' : type === '1' ? '旱地' : '山地';
      return h(Tag, { color }, () => typeText);
    },
  },
  {
    title: '土壤类型',
    dataIndex: 'soilType',
    width: 100,
    align: 'center',
  },
  {
    title: '土壤pH值',
    dataIndex: 'soilPh',
    width: 100,
    align: 'center',
  },
  {
    title: '地理位置',
    dataIndex: 'location',
    width: 200,
  },
  {
    title: '负责人',
    dataIndex: 'responsiblePerson',
    width: 100,
    align: 'center',
  },
  {
    title: '联系电话',
    dataIndex: 'contactPhone',
    width: 120,
    align: 'center',
  },
  {
    title: '地块状态',
    dataIndex: 'plotStatus',
    width: 100,
    align: 'center',
    customRender: ({ text }) => {
      const status = text || '0';
      const color = status === '0' ? 'green' : status === '1' ? 'orange' : 'red';
      const statusText = status === '0' ? '可用' : status === '1' ? '维护中' : '已停用';
      return h(Tag, { color }, () => statusText);
    },
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
    field: 'plotCode',
    label: '地块编号',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    field: 'plotName',
    label: '地块名称',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    field: 'plotType',
    label: '地块类型',
    component: 'Select',
    componentProps: {
      options: [
        { label: '水田', value: '0' },
        { label: '旱地', value: '1' },
        { label: '山地', value: '2' },
      ],
    },
    colProps: { span: 6 },
  },
  {
    field: 'soilType',
    label: '土壤类型',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    field: 'plotStatus',
    label: '地块状态',
    component: 'Select',
    componentProps: {
      options: [
        { label: '可用', value: '0' },
        { label: '维护中', value: '1' },
        { label: '已停用', value: '2' },
      ],
    },
    colProps: { span: 6 },
  },
  {
    field: 'responsiblePerson',
    label: '负责人',
    component: 'Input',
    colProps: { span: 6 },
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
    field: 'plotCode',
    label: '地块编号',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: '请输入地块编号!' }],
  },
  {
    field: 'plotName',
    label: '地块名称',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: '请输入地块名称!' }],
  },
  {
    field: 'plotArea',
    label: '地块面积(亩)',
    component: 'InputNumber',
    required: true,
    componentProps: {
      min: 0,
      precision: 2,
      placeholder: '请输入地块面积',
    },
    rules: [{ required: true, message: '请输入地块面积!' }],
  },
  {
    field: 'plotType',
    label: '地块类型',
    component: 'Select',
    defaultValue: '0',
    componentProps: {
      options: [
        { label: '水田', value: '0' },
        { label: '旱地', value: '1' },
        { label: '山地', value: '2' },
      ],
    },
    rules: [{ required: true, message: '请选择地块类型!' }],
  },
  {
    field: 'soilType',
    label: '土壤类型',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: '请输入土壤类型!' }],
  },
  {
    field: 'soilPh',
    label: '土壤pH值',
    component: 'InputNumber',
    componentProps: {
      min: 0,
      max: 14,
      precision: 1,
      placeholder: '请输入土壤pH值',
    },
  },
  {
    field: 'location',
    label: '地理位置',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: '请输入地理位置!' }],
  },
  {
    field: 'longitude',
    label: '经度',
    component: 'InputNumber',
    componentProps: {
      min: -180,
      max: 180,
      precision: 6,
      placeholder: '请输入经度',
    },
  },
  {
    field: 'latitude',
    label: '纬度',
    component: 'InputNumber',
    componentProps: {
      min: -90,
      max: 90,
      precision: 6,
      placeholder: '请输入纬度',
    },
  },
  {
    field: 'responsiblePerson',
    label: '负责人',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: '请输入负责人!' }],
  },
  {
    field: 'contactPhone',
    label: '联系电话',
    component: 'Input',
    rules: [
      { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码!' },
    ],
  },
  {
    field: 'plotStatus',
    label: '地块状态',
    component: 'Select',
    defaultValue: '0',
    componentProps: {
      options: [
        { label: '可用', value: '0' },
        { label: '维护中', value: '1' },
        { label: '已停用', value: '2' },
      ],
    },
  },
  {
    field: 'plotDescription',
    label: '地块描述',
    component: 'InputTextArea',
    componentProps: {
      rows: 4,
      placeholder: '请输入地块描述',
    },
  },
  {
    field: 'remarks',
    label: '备注',
    component: 'InputTextArea',
    componentProps: {
      rows: 3,
      placeholder: '请输入备注',
    },
  },
];