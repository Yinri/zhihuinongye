import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { h } from 'vue';
import { Tag } from 'ant-design-vue';

// 表格列配置
export const columns: BasicColumn[] = [
  {
    title: '预警编号',
    dataIndex: 'warningNo',
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
    title: '风险等级',
    dataIndex: 'riskLevel',
    width: 100,
    customRender: ({ record }) => {
      const color = record.riskLevel === '低' ? 'green' : record.riskLevel === '中' ? 'orange' : 'red';
      return h(Tag, { color }, () => record.riskLevel || '-');
    },
  },
  {
    title: '株高(cm)',
    dataIndex: 'plantHeight',
    width: 100,
  },
  {
    title: '茎粗(mm)',
    dataIndex: 'stemDiameter',
    width: 100,
  },
  {
    title: '根系强度',
    dataIndex: 'rootStrength',
    width: 100,
    customRender: ({ record }) => {
      const color = record.rootStrength === '强' ? 'green' : record.rootStrength === '中' ? 'orange' : 'red';
      return h(Tag, { color }, () => record.rootStrength || '-');
    },
  },
  {
    title: '天气状况',
    dataIndex: 'weatherCondition',
    width: 120,
  },
  {
    title: '预警日期',
    dataIndex: 'warningDate',
    width: 120,
  },
  {
    title: '预防措施',
    dataIndex: 'preventiveMeasures',
    width: 180,
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
    field: 'warningNo',
    label: '预警编号',
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
    field: 'riskLevel',
    label: '风险等级',
    component: 'Select',
    componentProps: {
      options: [
        { label: '低', value: '低' },
        { label: '中', value: '中' },
        { label: '高', value: '高' },
      ],
    },
    colProps: { span: 8 },
  },
  {
    field: 'warningDate',
    label: '预警日期',
    component: 'RangePicker',
    colProps: { span: 8 },
  },
];

// 表单配置
export const formSchema: FormSchema[] = [
  {
    field: 'warningNo',
    label: '预警编号',
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
    field: 'riskLevel',
    label: '风险等级',
    component: 'Select',
    componentProps: {
      options: [
        { label: '低', value: '低' },
        { label: '中', value: '中' },
        { label: '高', value: '高' },
      ],
    },
    required: true,
    colProps: { span: 12 },
  },
  {
    field: 'plantHeight',
    label: '株高(cm)',
    component: 'InputNumber',
    required: true,
    colProps: { span: 12 },
  },
  {
    field: 'stemDiameter',
    label: '茎粗(mm)',
    component: 'InputNumber',
    required: true,
    colProps: { span: 12 },
  },
  {
    field: 'rootStrength',
    label: '根系强度',
    component: 'Select',
    componentProps: {
      options: [
        { label: '强', value: '强' },
        { label: '中', value: '中' },
        { label: '弱', value: '弱' },
      ],
    },
    required: true,
    colProps: { span: 12 },
  },
  {
    field: 'weatherCondition',
    label: '天气状况',
    component: 'Input',
    required: true,
    colProps: { span: 12 },
  },
  {
    field: 'warningDate',
    label: '预警日期',
    component: 'DatePicker',
    required: true,
    colProps: { span: 12 },
  },
  {
    field: 'preventiveMeasures',
    label: '预防措施',
    component: 'InputTextArea',
    required: true,
    colProps: { span: 24 },
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