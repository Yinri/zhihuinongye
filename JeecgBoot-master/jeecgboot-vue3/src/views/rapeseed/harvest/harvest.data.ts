import { FormSchema } from '/@/components/Form';
import { BasicColumn } from '/@/components/Table';

export const columns: BasicColumn[] = [
  {
    title: '收获编号',
    dataIndex: 'harvestCode',
    width: 120,
  },
  {
    title: '地块名称',
    dataIndex: 'plotName',
    width: 120,
  },
  {
    title: '收获日期',
    dataIndex: 'harvestDate',
    width: 120,
  },
  {
    title: '收获方式',
    dataIndex: 'harvestMethod',
    width: 120,
    customRender: ({ record }) => {
      const method = record.harvestMethod;
      const methodMap = {
        '1': { text: '人工收割', color: 'blue' },
        '2': { text: '机械收割', color: 'green' },
      };
      const config = methodMap[method] || { text: '未知', color: 'default' };
      return `<a-tag color="${config.color}">${config.text}</a-tag>`;
    },
  },
  {
    title: '收获面积(亩)',
    dataIndex: 'harvestArea',
    width: 120,
  },
  {
    title: '产量(kg/亩)',
    dataIndex: 'yield',
    width: 120,
  },
  {
    title: '总产量(kg)',
    dataIndex: 'totalYield',
    width: 120,
  },
  {
    title: '水分含量(%)',
    dataIndex: 'moistureContent',
    width: 120,
  },
  {
    title: '收获质量',
    dataIndex: 'harvestQuality',
    width: 120,
    customRender: ({ record }) => {
      const quality = record.harvestQuality;
      const qualityMap = {
        '1': { text: '优', color: 'green' },
        '2': { text: '良', color: 'blue' },
        '3': { text: '中', color: 'orange' },
        '4': { text: '差', color: 'red' },
      };
      const config = qualityMap[quality] || { text: '未知', color: 'default' };
      return `<a-tag color="${config.color}">${config.text}</a-tag>`;
    },
  },
  {
    title: '收获人员',
    dataIndex: 'harvestPerson',
    width: 120,
  },
  {
    title: '备注',
    dataIndex: 'remark',
    width: 120,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'harvestCode',
    label: '收获编号',
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
    field: 'harvestDate',
    label: '收获日期',
    component: 'RangePicker',
    colProps: { span: 8 },
  },
  {
    field: 'harvestMethod',
    label: '收获方式',
    component: 'Select',
    componentProps: {
      options: [
        { label: '人工收割', value: '1' },
        { label: '机械收割', value: '2' },
      ],
    },
    colProps: { span: 8 },
  },
  {
    field: 'harvestQuality',
    label: '收获质量',
    component: 'Select',
    componentProps: {
      options: [
        { label: '优', value: '1' },
        { label: '良', value: '2' },
        { label: '中', value: '3' },
        { label: '差', value: '4' },
      ],
    },
    colProps: { span: 8 },
  },
];

export const formSchema: FormSchema[] = [
  {
    field: 'harvestCode',
    label: '收获编号',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: '请输入收获编号' }],
  },
  {
    field: 'plotName',
    label: '地块名称',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: '请输入地块名称' }],
  },
  {
    field: 'harvestDate',
    label: '收获日期',
    component: 'DatePicker',
    required: true,
    rules: [{ required: true, message: '请选择收获日期' }],
  },
  {
    field: 'harvestMethod',
    label: '收获方式',
    component: 'Select',
    required: true,
    componentProps: {
      options: [
        { label: '人工收割', value: '1' },
        { label: '机械收割', value: '2' },
      ],
    },
    rules: [{ required: true, message: '请选择收获方式' }],
  },
  {
    field: 'harvestArea',
    label: '收获面积(亩)',
    component: 'InputNumber',
    required: true,
    rules: [{ required: true, message: '请输入收获面积' }],
  },
  {
    field: 'yield',
    label: '产量(kg/亩)',
    component: 'InputNumber',
    required: true,
    rules: [{ required: true, message: '请输入产量' }],
  },
  {
    field: 'totalYield',
    label: '总产量(kg)',
    component: 'InputNumber',
    required: true,
    rules: [{ required: true, message: '请输入总产量' }],
  },
  {
    field: 'moistureContent',
    label: '水分含量(%)',
    component: 'InputNumber',
    required: true,
    rules: [{ required: true, message: '请输入水分含量' }],
  },
  {
    field: 'harvestQuality',
    label: '收获质量',
    component: 'Select',
    required: true,
    componentProps: {
      options: [
        { label: '优', value: '1' },
        { label: '良', value: '2' },
        { label: '中', value: '3' },
        { label: '差', value: '4' },
      ],
    },
    rules: [{ required: true, message: '请选择收获质量' }],
  },
  {
    field: 'harvestPerson',
    label: '收获人员',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: '请输入收获人员' }],
  },
  {
    field: 'remark',
    label: '备注',
    component: 'InputTextArea',
  },
];