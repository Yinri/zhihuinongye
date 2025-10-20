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
    title: '作业日期',
    dataIndex: 'operationDate',
    width: 120,
  },
  {
    title: '作业类型',
    dataIndex: 'operationType',
    width: 120,
    customRender: ({ record }) => {
      const type = record.operationType;
      const color = type === '0' ? 'blue' : type === '1' ? 'green' : type === '2' ? 'orange' : 'purple';
      const text = type === '0' ? '收获准备' : type === '1' ? '机械化收获' : type === '2' ? '秸秆处理' : '土地整理';
      return h(Tag, { color }, () => text);
    },
  },
  {
    title: '作业面积(亩)',
    dataIndex: 'operationArea',
    width: 120,
    customRender: ({ record }) => {
      return `${record.operationArea} 亩`;
    },
  },
  {
    title: '产量(kg)',
    dataIndex: 'yield',
    width: 120,
    customRender: ({ record }) => {
      return record.yield ? `${record.yield} kg` : '-';
    },
  },
  {
    title: '作业方式',
    dataIndex: 'operationMethod',
    width: 120,
    customRender: ({ record }) => {
      const method = record.operationMethod;
      const text = method === '0' ? '人工' : method === '1' ? '机械' : '半机械化';
      return h(Tag, { color: 'default' }, () => text);
    },
  },
  {
    title: '作业时长(小时)',
    dataIndex: 'operationDuration',
    width: 120,
    customRender: ({ record }) => {
      return `${record.operationDuration} 小时`;
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
    field: 'operationType',
    label: '作业类型',
    component: 'Select',
    componentProps: {
      options: [
        { label: '收获准备', value: '0' },
        { label: '机械化收获', value: '1' },
        { label: '秸秆处理', value: '2' },
        { label: '土地整理', value: '3' },
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
    field: 'operationDate',
    label: '作业日期',
    required: true,
    component: 'DatePicker',
    componentProps: {
      style: { width: '100%' },
    },
  },
  {
    field: 'operationType',
    label: '作业类型',
    required: true,
    component: 'Select',
    componentProps: {
      options: [
        { label: '收获准备', value: '0' },
        { label: '机械化收获', value: '1' },
        { label: '秸秆处理', value: '2' },
        { label: '土地整理', value: '3' },
      ],
    },
  },
  {
    field: 'operationArea',
    label: '作业面积(亩)',
    required: true,
    component: 'InputNumber',
    componentProps: {
      placeholder: '请输入作业面积',
      min: 0,
      precision: 1,
    },
  },
  {
    field: 'yield',
    label: '产量(kg)',
    component: 'InputNumber',
    componentProps: {
      placeholder: '请输入产量',
      min: 0,
      precision: 1,
    },
  },
  {
    field: 'operationMethod',
    label: '作业方式',
    required: true,
    component: 'Select',
    componentProps: {
      options: [
        { label: '人工', value: '0' },
        { label: '机械', value: '1' },
        { label: '半机械化', value: '2' },
      ],
    },
  },
  {
    field: 'operationDuration',
    label: '作业时长(小时)',
    required: true,
    component: 'InputNumber',
    componentProps: {
      placeholder: '请输入作业时长',
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