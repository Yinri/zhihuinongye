import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { h } from 'vue';
import { Tag } from 'ant-design-vue';

// 表格列配置
export const columns: BasicColumn[] = [
  {
    title: '田块编号',
    dataIndex: 'fieldCode',
    width: 120,
    align: 'left',
  },
  {
    title: '田块名称',
    dataIndex: 'fieldName',
    width: 150,
    align: 'left',
  },
  {
    title: '管理日期',
    dataIndex: 'managementDate',
    width: 120,
  },
  {
    title: '管理类型',
    dataIndex: 'managementType',
    width: 120,
    customRender: ({ record }) => {
      const type = record.managementType;
      const color = type === '0' ? 'blue' : type === '1' ? 'green' : type === '2' ? 'orange' : 'purple';
      const text = type === '0' ? '初角果期管理' : type === '1' ? '角果膨大期管理' : type === '2' ? '角果成熟期管理' : '收获前管理';
      return h(Tag, { color }, () => text);
    },
  },
  {
    title: '角果长度(cm)',
    dataIndex: 'podLength',
    width: 120,
    customRender: ({ record }) => {
      return `${record.podLength} cm`;
    },
  },
  {
    title: '成熟度(%)',
    dataIndex: 'maturity',
    width: 120,
    customRender: ({ record }) => {
      return `${record.maturity} %`;
    },
  },
  {
    title: '千粒重(g)',
    dataIndex: 'thousandGrainWeight',
    width: 120,
    customRender: ({ record }) => {
      return `${record.thousandGrainWeight} g`;
    },
  },
  {
    title: '产量预估(kg/亩)',
    dataIndex: 'yieldEstimate',
    width: 120,
    customRender: ({ record }) => {
      return `${record.yieldEstimate} kg/亩`;
    },
  },
  {
    title: '负责人',
    dataIndex: 'personInCharge',
    width: 120,
  },
  {
    title: '备注',
    dataIndex: 'remark',
    width: 150,
  },
];

// 搜索表单配置
export const searchFormSchema: FormSchema[] = [
  {
    field: 'fieldCode',
    label: '田块编号',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'fieldName',
    label: '田块名称',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'managementType',
    label: '管理类型',
    component: 'Select',
    componentProps: {
      options: [
        { label: '初角果期管理', value: '0' },
        { label: '角果膨大期管理', value: '1' },
        { label: '角果成熟期管理', value: '2' },
        { label: '收获前管理', value: '3' },
      ],
    },
    colProps: { span: 8 },
  },
];

// 表单字段配置
export const formSchema: FormSchema[] = [
  {
    field: 'fieldCode',
    label: '田块编号',
    required: true,
    component: 'Input',
    componentProps: {
      placeholder: '请输入田块编号',
    },
  },
  {
    field: 'fieldName',
    label: '田块名称',
    required: true,
    component: 'Input',
    componentProps: {
      placeholder: '请输入田块名称',
    },
  },
  {
    field: 'managementDate',
    label: '管理日期',
    required: true,
    component: 'DatePicker',
    componentProps: {
      style: { width: '100%' },
    },
  },
  {
    field: 'managementType',
    label: '管理类型',
    required: true,
    component: 'Select',
    componentProps: {
      options: [
        { label: '初角果期管理', value: '0' },
        { label: '角果膨大期管理', value: '1' },
        { label: '角果成熟期管理', value: '2' },
        { label: '收获前管理', value: '3' },
      ],
    },
  },
  {
    field: 'podLength',
    label: '角果长度(cm)',
    required: true,
    component: 'InputNumber',
    componentProps: {
      placeholder: '请输入角果长度',
      min: 0,
      precision: 1,
    },
  },
  {
    field: 'maturity',
    label: '成熟度(%)',
    required: true,
    component: 'InputNumber',
    componentProps: {
      placeholder: '请输入成熟度',
      min: 0,
      max: 100,
      precision: 1,
    },
  },
  {
    field: 'thousandGrainWeight',
    label: '千粒重(g)',
    required: true,
    component: 'InputNumber',
    componentProps: {
      placeholder: '请输入千粒重',
      min: 0,
      precision: 1,
    },
  },
  {
    field: 'yieldEstimate',
    label: '产量预估(kg/亩)',
    required: true,
    component: 'InputNumber',
    componentProps: {
      placeholder: '请输入产量预估',
      min: 0,
      precision: 1,
    },
  },
  {
    field: 'personInCharge',
    label: '负责人',
    required: true,
    component: 'Input',
    componentProps: {
      placeholder: '请输入负责人姓名',
    },
  },
  {
    field: 'remark',
    label: '备注',
    component: 'InputTextArea',
    componentProps: {
      placeholder: '请输入备注',
      rows: 4,
    },
  },
];