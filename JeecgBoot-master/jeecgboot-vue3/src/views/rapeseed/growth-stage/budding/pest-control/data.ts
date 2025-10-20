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
    title: '防治日期',
    dataIndex: 'controlDate',
    width: 120,
  },
  {
    title: '病虫害类型',
    dataIndex: 'pestType',
    width: 120,
    customRender: ({ record }) => {
      const type = record.pestType;
      const color = type === '0' ? 'blue' : type === '1' ? 'green' : type === '2' ? 'orange' : 'purple';
      const text = type === '0' ? '蚜虫' : type === '1' ? '菜青虫' : type === '2' ? '霜霉病' : '菌核病';
      return h(Tag, { color }, () => text);
    },
  },
  {
    title: '防治药剂',
    dataIndex: 'pesticide',
    width: 120,
  },
  {
    title: '用药量(g/亩)',
    dataIndex: 'dosage',
    width: 120,
    customRender: ({ record }) => {
      return `${record.dosage} g/亩`;
    },
  },
  {
    title: '防治方式',
    dataIndex: 'controlMethod',
    width: 120,
    customRender: ({ record }) => {
      const method = record.controlMethod;
      const text = method === '0' ? '喷施' : method === '1' ? '撒施' : method === '2' ? '灌根' : '熏蒸';
      return text;
    },
  },
  {
    title: '防治效果',
    dataIndex: 'controlEffect',
    width: 120,
    customRender: ({ record }) => {
      const effect = record.controlEffect;
      const color = effect === '0' ? 'green' : effect === '1' ? 'orange' : 'red';
      const text = effect === '0' ? '良好' : effect === '1' ? '一般' : '较差';
      return h(Tag, { color }, () => text);
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
    field: 'pestType',
    label: '病虫害类型',
    component: 'Select',
    componentProps: {
      options: [
        { label: '蚜虫', value: '0' },
        { label: '菜青虫', value: '1' },
        { label: '霜霉病', value: '2' },
        { label: '菌核病', value: '3' },
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
    field: 'controlDate',
    label: '防治日期',
    required: true,
    component: 'DatePicker',
    componentProps: {
      style: { width: '100%' },
    },
  },
  {
    field: 'pestType',
    label: '病虫害类型',
    required: true,
    component: 'Select',
    componentProps: {
      options: [
        { label: '蚜虫', value: '0' },
        { label: '菜青虫', value: '1' },
        { label: '霜霉病', value: '2' },
        { label: '菌核病', value: '3' },
      ],
    },
  },
  {
    field: 'pesticide',
    label: '防治药剂',
    required: true,
    component: 'Input',
    componentProps: {
      placeholder: '请输入防治药剂名称',
    },
  },
  {
    field: 'dosage',
    label: '用药量(g/亩)',
    required: true,
    component: 'InputNumber',
    componentProps: {
      placeholder: '请输入用药量',
      min: 0,
      precision: 1,
    },
  },
  {
    field: 'controlMethod',
    label: '防治方式',
    required: true,
    component: 'Select',
    componentProps: {
      options: [
        { label: '喷施', value: '0' },
        { label: '撒施', value: '1' },
        { label: '灌根', value: '2' },
        { label: '熏蒸', value: '3' },
      ],
    },
  },
  {
    field: 'controlEffect',
    label: '防治效果',
    required: true,
    component: 'Select',
    componentProps: {
      options: [
        { label: '良好', value: '0' },
        { label: '一般', value: '1' },
        { label: '较差', value: '2' },
      ],
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