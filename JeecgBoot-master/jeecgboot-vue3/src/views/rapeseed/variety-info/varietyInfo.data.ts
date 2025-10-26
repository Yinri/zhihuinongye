import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { h } from 'vue';
import { Tag } from 'ant-design-vue';

// 表格列配置
export const columns: BasicColumn[] = [
  {
    title: '品种编号',
    dataIndex: 'varietyCode',
    width: 120,
    align: 'center',
  },
  {
    title: '品种名称',
    dataIndex: 'varietyName',
    width: 180,
  },
  {
    title: '品种类型',
    dataIndex: 'varietyType',
    width: 100,
    align: 'center',
    customRender: ({ text }) => {
      const type = text || '0';
      const color = type === '0' ? 'blue' : type === '1' ? 'green' : 'orange';
      const typeText = type === '0' ? '常规品种' : type === '1' ? '杂交品种' : '转基因品种';
      return h(Tag, { color }, () => typeText);
    },
  },
  {
    title: '生育期(天)',
    dataIndex: 'growthPeriod',
    width: 100,
    align: 'center',
  },
  {
    title: '产量潜力(kg/亩)',
    dataIndex: 'yieldPotential',
    width: 120,
    align: 'center',
  },
  {
    title: '含油率(%)',
    dataIndex: 'oilContent',
    width: 100,
    align: 'center',
  },
  {
    title: '抗病性',
    dataIndex: 'diseaseResistance',
    width: 100,
    align: 'center',
    customRender: ({ text }) => {
      const level = text || '0';
      const color = level === '0' ? 'red' : level === '1' ? 'orange' : level === '2' ? 'yellow' : level === '3' ? 'green' : 'blue';
      const levelText = level === '0' ? '弱' : level === '1' ? '较弱' : level === '2' ? '中等' : level === '3' ? '较强' : '强';
      return h(Tag, { color }, () => levelText);
    },
  },
  {
    title: '抗倒伏性',
    dataIndex: 'lodgingResistance',
    width: 100,
    align: 'center',
    customRender: ({ text }) => {
      const level = text || '0';
      const color = level === '0' ? 'red' : level === '1' ? 'orange' : level === '2' ? 'yellow' : level === '3' ? 'green' : 'blue';
      const levelText = level === '0' ? '弱' : level === '1' ? '较弱' : level === '2' ? '中等' : level === '3' ? '较强' : '强';
      return h(Tag, { color }, () => levelText);
    },
  },
  {
    title: '适应性',
    dataIndex: 'adaptability',
    width: 100,
    align: 'center',
    customRender: ({ text }) => {
      const level = text || '0';
      const color = level === '0' ? 'red' : level === '1' ? 'orange' : level === '2' ? 'yellow' : level === '3' ? 'green' : 'blue';
      const levelText = level === '0' ? '弱' : level === '1' ? '较弱' : level === '2' ? '中等' : level === '3' ? '较强' : '强';
      return h(Tag, { color }, () => levelText);
    },
  },
  {
    title: '品种状态',
    dataIndex: 'varietyStatus',
    width: 100,
    align: 'center',
    customRender: ({ text }) => {
      const status = text || '0';
      const color = status === '0' ? 'green' : status === '1' ? 'orange' : 'red';
      const statusText = status === '0' ? '可用' : status === '1' ? '试验中' : '已停用';
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
    field: 'varietyCode',
    label: '品种编号',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    field: 'varietyName',
    label: '品种名称',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    field: 'varietyType',
    label: '品种类型',
    component: 'Select',
    componentProps: {
      options: [
        { label: '常规品种', value: '0' },
        { label: '杂交品种', value: '1' },
        { label: '转基因品种', value: '2' },
      ],
    },
    colProps: { span: 6 },
  },
  {
    field: 'varietyStatus',
    label: '品种状态',
    component: 'Select',
    componentProps: {
      options: [
        { label: '可用', value: '0' },
        { label: '试验中', value: '1' },
        { label: '已停用', value: '2' },
      ],
    },
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
    field: 'varietyCode',
    label: '品种编号',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: '请输入品种编号!' }],
  },
  {
    field: 'varietyName',
    label: '品种名称',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: '请输入品种名称!' }],
  },
  {
    field: 'varietyType',
    label: '品种类型',
    component: 'Select',
    defaultValue: '0',
    componentProps: {
      options: [
        { label: '常规品种', value: '0' },
        { label: '杂交品种', value: '1' },
        { label: '转基因品种', value: '2' },
      ],
    },
    rules: [{ required: true, message: '请选择品种类型!' }],
  },
  {
    field: 'origin',
    label: '品种来源',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: '请输入品种来源!' }],
  },
  {
    field: 'growthPeriod',
    label: '生育期(天)',
    component: 'InputNumber',
    required: true,
    componentProps: {
      min: 0,
      placeholder: '请输入生育期',
    },
    rules: [{ required: true, message: '请输入生育期!' }],
  },
  {
    field: 'yieldPotential',
    label: '产量潜力(kg/亩)',
    component: 'InputNumber',
    required: true,
    componentProps: {
      min: 0,
      precision: 2,
      placeholder: '请输入产量潜力',
    },
    rules: [{ required: true, message: '请输入产量潜力!' }],
  },
  {
    field: 'oilContent',
    label: '含油率(%)',
    component: 'InputNumber',
    componentProps: {
      min: 0,
      max: 100,
      precision: 2,
      placeholder: '请输入含油率',
    },
  },
  {
    field: 'diseaseResistance',
    label: '抗病性',
    component: 'Select',
    defaultValue: '2',
    componentProps: {
      options: [
        { label: '弱', value: '0' },
        { label: '较弱', value: '1' },
        { label: '中等', value: '2' },
        { label: '较强', value: '3' },
        { label: '强', value: '4' },
      ],
    },
  },
  {
    field: 'lodgingResistance',
    label: '抗倒伏性',
    component: 'Select',
    defaultValue: '2',
    componentProps: {
      options: [
        { label: '弱', value: '0' },
        { label: '较弱', value: '1' },
        { label: '中等', value: '2' },
        { label: '较强', value: '3' },
        { label: '强', value: '4' },
      ],
    },
  },
  {
    field: 'adaptability',
    label: '适应性',
    component: 'Select',
    defaultValue: '2',
    componentProps: {
      options: [
        { label: '弱', value: '0' },
        { label: '较弱', value: '1' },
        { label: '中等', value: '2' },
        { label: '较强', value: '3' },
        { label: '强', value: '4' },
      ],
    },
  },
  {
    field: 'varietyStatus',
    label: '品种状态',
    component: 'Select',
    defaultValue: '0',
    componentProps: {
      options: [
        { label: '可用', value: '0' },
        { label: '试验中', value: '1' },
        { label: '已停用', value: '2' },
      ],
    },
  },
  {
    field: 'characteristics',
    label: '品种特性',
    component: 'InputTextArea',
    componentProps: {
      rows: 4,
      placeholder: '请输入品种特性',
    },
  },
  {
    field: 'cultivationPoints',
    label: '栽培要点',
    component: 'InputTextArea',
    componentProps: {
      rows: 4,
      placeholder: '请输入栽培要点',
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