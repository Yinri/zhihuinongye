import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { h } from 'vue';
import { Tag } from 'ant-design-vue';

// 表格列配置
export const columns: BasicColumn[] = [
  {
    title: '施肥编号',
    dataIndex: 'fertilizationNo',
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
    title: '施肥类型',
    dataIndex: 'fertilizationType',
    width: 120,
    customRender: ({ record }) => {
      const color = record.fertilizationType === '基肥' ? 'green' : record.fertilizationType === '追肥' ? 'blue' : 'orange';
      return h(Tag, { color }, () => record.fertilizationType || '-');
    },
  },
  {
    title: '肥料名称',
    dataIndex: 'fertilizerName',
    width: 120,
  },
  {
    title: '施肥量(kg/亩)',
    dataIndex: 'fertilizerAmount',
    width: 120,
  },
  {
    title: '施肥面积(亩)',
    dataIndex: 'fertilizationArea',
    width: 120,
  },
  {
    title: '施肥日期',
    dataIndex: 'fertilizationDate',
    width: 120,
  },
  {
    title: '施肥方式',
    dataIndex: 'fertilizationMethod',
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
    field: 'fertilizationNo',
    label: '施肥编号',
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
    field: 'fertilizationType',
    label: '施肥类型',
    component: 'Select',
    componentProps: {
      options: [
        { label: '基肥', value: '基肥' },
        { label: '追肥', value: '追肥' },
        { label: '叶面肥', value: '叶面肥' },
      ],
    },
    colProps: { span: 8 },
  },
  {
    field: 'fertilizationDate',
    label: '施肥日期',
    component: 'RangePicker',
    colProps: { span: 8 },
  },
];

// 表单配置
export const formSchema: FormSchema[] = [
  {
    field: 'fertilizationNo',
    label: '施肥编号',
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
    field: 'fertilizationType',
    label: '施肥类型',
    component: 'Select',
    componentProps: {
      options: [
        { label: '基肥', value: '基肥' },
        { label: '追肥', value: '追肥' },
        { label: '叶面肥', value: '叶面肥' },
      ],
    },
    required: true,
    colProps: { span: 12 },
  },
  {
    field: 'fertilizerName',
    label: '肥料名称',
    component: 'Input',
    required: true,
    colProps: { span: 12 },
  },
  {
    field: 'fertilizerAmount',
    label: '施肥量(kg/亩)',
    component: 'InputNumber',
    required: true,
    colProps: { span: 12 },
  },
  {
    field: 'fertilizationArea',
    label: '施肥面积(亩)',
    component: 'InputNumber',
    required: true,
    colProps: { span: 12 },
  },
  {
    field: 'fertilizationDate',
    label: '施肥日期',
    component: 'DatePicker',
    required: true,
    colProps: { span: 12 },
  },
  {
    field: 'fertilizationMethod',
    label: '施肥方式',
    component: 'Input',
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