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
    title: '施肥日期',
    dataIndex: 'fertilizationDate',
    width: 120,
  },
  {
    title: '肥料类型',
    dataIndex: 'fertilizerType',
    width: 120,
    customRender: ({ record }) => {
      const type = record.fertilizerType;
      const color = type === '0' ? 'blue' : type === '1' ? 'green' : type === '2' ? 'orange' : 'purple';
      const text = type === '0' ? '氮肥' : type === '1' ? '磷肥' : type === '2' ? '钾肥' : '复合肥';
      return h(Tag, { color }, () => text);
    },
  },
  {
    title: '施肥量(kg/亩)',
    dataIndex: 'fertilizerAmount',
    width: 120,
    customRender: ({ record }) => {
      return `${record.fertilizerAmount} kg/亩`;
    },
  },
  {
    title: '施肥方式',
    dataIndex: 'fertilizationMethod',
    width: 120,
    customRender: ({ record }) => {
      const method = record.fertilizationMethod;
      const text = method === '0' ? '撒施' : method === '1' ? '沟施' : method === '2' ? '穴施' : '喷施';
      return text;
    },
  },
  {
    title: '土壤养分变化',
    dataIndex: 'nutrientChange',
    width: 150,
    customRender: ({ record }) => {
      return `N+${record.nitrogenChange} P+${record.phosphorusChange} K+${record.potassiumChange}`;
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
    field: 'fertilizerType',
    label: '肥料类型',
    component: 'Select',
    componentProps: {
      options: [
        { label: '氮肥', value: '0' },
        { label: '磷肥', value: '1' },
        { label: '钾肥', value: '2' },
        { label: '复合肥', value: '3' },
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
    field: 'fertilizationDate',
    label: '施肥日期',
    required: true,
    component: 'DatePicker',
    componentProps: {
      style: { width: '100%' },
    },
  },
  {
    field: 'fertilizerType',
    label: '肥料类型',
    required: true,
    component: 'Select',
    componentProps: {
      options: [
        { label: '氮肥', value: '0' },
        { label: '磷肥', value: '1' },
        { label: '钾肥', value: '2' },
        { label: '复合肥', value: '3' },
      ],
    },
  },
  {
    field: 'fertilizerAmount',
    label: '施肥量(kg/亩)',
    required: true,
    component: 'InputNumber',
    componentProps: {
      placeholder: '请输入施肥量',
      min: 0,
      precision: 1,
    },
  },
  {
    field: 'fertilizationMethod',
    label: '施肥方式',
    required: true,
    component: 'Select',
    componentProps: {
      options: [
        { label: '撒施', value: '0' },
        { label: '沟施', value: '1' },
        { label: '穴施', value: '2' },
        { label: '喷施', value: '3' },
      ],
    },
  },
  {
    field: 'nitrogenChange',
    label: '氮含量变化(mg/kg)',
    component: 'InputNumber',
    componentProps: {
      placeholder: '请输入氮含量变化',
      precision: 1,
    },
  },
  {
    field: 'phosphorusChange',
    label: '磷含量变化(mg/kg)',
    component: 'InputNumber',
    componentProps: {
      placeholder: '请输入磷含量变化',
      precision: 1,
    },
  },
  {
    field: 'potassiumChange',
    label: '钾含量变化(mg/kg)',
    component: 'InputNumber',
    componentProps: {
      placeholder: '请输入钾含量变化',
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