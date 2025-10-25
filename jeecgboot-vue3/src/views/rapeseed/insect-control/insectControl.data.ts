import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { h } from 'vue';
import { Tag } from 'ant-design-vue';

// 表格列配置
export const columns: BasicColumn[] = [
  {
    title: '防控编号',
    dataIndex: 'controlNo',
    width: 120,
  },
  {
    title: '地块名称',
    dataIndex: 'plotName',
    width: 120,
  },
  {
    title: '品种名称',
    dataIndex: 'varietyName',
    width: 120,
  },
  {
    title: '虫害类型',
    dataIndex: 'insectType',
    width: 120,
    customRender: ({ record }) => {
      const color = record.insectType === '蚜虫' ? 'green' : record.insectType === '菜青虫' ? 'orange' : 'blue';
      return h(Tag, { color }, () => record.insectType || '-');
    },
  },
  {
    title: '发生程度',
    dataIndex: 'severity',
    width: 100,
    customRender: ({ record }) => {
      const color = record.severity === '轻度' ? 'green' : record.severity === '中度' ? 'orange' : 'red';
      return h(Tag, { color }, () => record.severity || '-');
    },
  },
  {
    title: '防控措施',
    dataIndex: 'controlMeasures',
    width: 180,
  },
  {
    title: '用药名称',
    dataIndex: 'pesticideName',
    width: 120,
  },
  {
    title: '用药量',
    dataIndex: 'dosage',
    width: 100,
  },
  {
    title: '防控日期',
    dataIndex: 'controlDate',
    width: 120,
  },
  {
    title: '负责人',
    dataIndex: 'responsiblePerson',
    width: 100,
  },
  {
    title: '备注',
    dataIndex: 'remark',
    width: 150,
  },
];

// 查询表单配置
export const searchFormSchema: FormSchema[] = [
  {
    field: 'controlNo',
    label: '防控编号',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'plotName',
    label: '地块名称',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'insectType',
    label: '虫害类型',
    component: 'Select',
    componentProps: {
      options: [
        { label: '蚜虫', value: '蚜虫' },
        { label: '菜青虫', value: '菜青虫' },
        { label: '小菜蛾', value: '小菜蛾' },
        { label: '跳甲', value: '跳甲' },
        { label: '其他', value: '其他' },
      ],
    },
    colProps: { span: 8 },
  },
  {
    field: 'severity',
    label: '发生程度',
    component: 'Select',
    componentProps: {
      options: [
        { label: '轻度', value: '轻度' },
        { label: '中度', value: '中度' },
        { label: '重度', value: '重度' },
      ],
    },
    colProps: { span: 8 },
  },
  {
    field: 'controlDate',
    label: '防控日期',
    component: 'RangePicker',
    colProps: { span: 8 },
  },
];

// 表单配置
export const formSchema: FormSchema[] = [
  {
    field: 'controlNo',
    label: '防控编号',
    component: 'Input',
    required: true,
    colProps: { span: 12 },
  },
  {
    field: 'plotName',
    label: '地块名称',
    component: 'Input',
    required: true,
    colProps: { span: 12 },
  },
  {
    field: 'varietyName',
    label: '品种名称',
    component: 'Input',
    required: true,
    colProps: { span: 12 },
  },
  {
    field: 'insectType',
    label: '虫害类型',
    component: 'Select',
    componentProps: {
      options: [
        { label: '蚜虫', value: '蚜虫' },
        { label: '菜青虫', value: '菜青虫' },
        { label: '小菜蛾', value: '小菜蛾' },
        { label: '跳甲', value: '跳甲' },
        { label: '其他', value: '其他' },
      ],
    },
    required: true,
    colProps: { span: 12 },
  },
  {
    field: 'severity',
    label: '发生程度',
    component: 'Select',
    componentProps: {
      options: [
        { label: '轻度', value: '轻度' },
        { label: '中度', value: '中度' },
        { label: '重度', value: '重度' },
      ],
    },
    required: true,
    colProps: { span: 12 },
  },
  {
    field: 'controlMeasures',
    label: '防控措施',
    component: 'InputTextArea',
    required: true,
    colProps: { span: 24 },
  },
  {
    field: 'pesticideName',
    label: '用药名称',
    component: 'Input',
    colProps: { span: 12 },
  },
  {
    field: 'dosage',
    label: '用药量',
    component: 'Input',
    colProps: { span: 12 },
  },
  {
    field: 'controlDate',
    label: '防控日期',
    component: 'DatePicker',
    required: true,
    colProps: { span: 12 },
  },
  {
    field: 'responsiblePerson',
    label: '负责人',
    component: 'Input',
    required: true,
    colProps: { span: 12 },
  },
  {
    field: 'remark',
    label: '备注',
    component: 'InputTextArea',
    colProps: { span: 24 },
  },
];