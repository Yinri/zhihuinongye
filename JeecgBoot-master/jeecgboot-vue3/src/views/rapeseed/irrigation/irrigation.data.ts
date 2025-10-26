import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { h } from 'vue';
import { Tag } from 'ant-design-vue';

// 表格列配置
export const columns: BasicColumn[] = [
  {
    title: '灌溉编号',
    dataIndex: 'irrigationNo',
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
    title: '灌溉方式',
    dataIndex: 'irrigationMethod',
    width: 120,
    customRender: ({ record }) => {
      const color = record.irrigationMethod === '滴灌' ? 'green' : record.irrigationMethod === '喷灌' ? 'blue' : 'orange';
      return h(Tag, { color }, () => record.irrigationMethod || '-');
    },
  },
  {
    title: '灌溉面积(亩)',
    dataIndex: 'irrigationArea',
    width: 120,
  },
  {
    title: '用水量(立方米)',
    dataIndex: 'waterUsage',
    width: 120,
  },
  {
    title: '灌溉时长(小时)',
    dataIndex: 'duration',
    width: 120,
  },
  {
    title: '灌溉日期',
    dataIndex: 'irrigationDate',
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
    field: 'irrigationNo',
    label: '灌溉编号',
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
    field: 'irrigationMethod',
    label: '灌溉方式',
    component: 'Select',
    componentProps: {
      options: [
        { label: '滴灌', value: '滴灌' },
        { label: '喷灌', value: '喷灌' },
        { label: '漫灌', value: '漫灌' },
        { label: '微灌', value: '微灌' },
      ],
    },
    colProps: { span: 8 },
  },
  {
    field: 'irrigationDate',
    label: '灌溉日期',
    component: 'RangePicker',
    colProps: { span: 8 },
  },
];

// 表单配置
export const formSchema: FormSchema[] = [
  {
    field: 'irrigationNo',
    label: '灌溉编号',
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
    field: 'irrigationMethod',
    label: '灌溉方式',
    component: 'Select',
    componentProps: {
      options: [
        { label: '滴灌', value: '滴灌' },
        { label: '喷灌', value: '喷灌' },
        { label: '漫灌', value: '漫灌' },
        { label: '微灌', value: '微灌' },
      ],
    },
    required: true,
    colProps: { span: 12 },
  },
  {
    field: 'irrigationArea',
    label: '灌溉面积(亩)',
    component: 'InputNumber',
    required: true,
    colProps: { span: 12 },
  },
  {
    field: 'waterUsage',
    label: '用水量(立方米)',
    component: 'InputNumber',
    required: true,
    colProps: { span: 12 },
  },
  {
    field: 'duration',
    label: '灌溉时长(小时)',
    component: 'InputNumber',
    required: true,
    colProps: { span: 12 },
  },
  {
    field: 'irrigationDate',
    label: '灌溉日期',
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