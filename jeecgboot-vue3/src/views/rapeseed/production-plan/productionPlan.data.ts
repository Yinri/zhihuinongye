import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { h } from 'vue';
import { Tag } from 'ant-design-vue';

// 表格列配置
export const columns: BasicColumn[] = [
  {
    title: '计划编号',
    dataIndex: 'planCode',
    width: 120,
    align: 'center',
  },
  {
    title: '计划名称',
    dataIndex: 'planName',
    width: 180,
  },
  {
    title: '地块名称',
    dataIndex: 'plotName',
    width: 150,
  },
  {
    title: '品种名称',
    dataIndex: 'varietyName',
    width: 150,
  },
  {
    title: '种植面积(亩)',
    dataIndex: 'plantingArea',
    width: 100,
    align: 'center',
  },
  {
    title: '目标产量(kg/亩)',
    dataIndex: 'targetYield',
    width: 120,
    align: 'center',
  },
  {
    title: '土壤肥力',
    dataIndex: 'soilFertility',
    width: 100,
    align: 'center',
    customRender: ({ text }) => {
      const fertility = text || '1';
      const color = fertility === '1' ? 'green' : fertility === '2' ? 'orange' : 'red';
      const fertilityText = fertility === '1' ? '高' : fertility === '2' ? '中' : '低';
      return h(Tag, { color }, () => fertilityText);
    },
  },
  {
    title: '种子投入(kg/亩)',
    dataIndex: 'seedInput',
    width: 120,
    align: 'center',
  },
  {
    title: '肥料投入(kg/亩)',
    dataIndex: 'fertilizerInput',
    width: 120,
    align: 'center',
  },
  {
    title: '农药投入(kg/亩)',
    dataIndex: 'pesticideInput',
    width: 120,
    align: 'center',
  },
  {
    title: '播种时间',
    dataIndex: 'sowingTime',
    width: 120,
    align: 'center',
  },
  {
    title: '预计收获时间',
    dataIndex: 'expectedHarvestTime',
    width: 120,
    align: 'center',
  },
  {
    title: '计划状态',
    dataIndex: 'planStatus',
    width: 100,
    align: 'center',
    customRender: ({ text }) => {
      const status = text || '0';
      const color = status === '0' ? 'blue' : status === '1' ? 'green' : status === '2' ? 'orange' : 'red';
      const statusText = status === '0' ? '计划中' : status === '1' ? '执行中' : status === '2' ? '已完成' : '已取消';
      return h(Tag, { color }, () => statusText);
    },
  },
  {
    title: '负责人',
    dataIndex: 'responsiblePerson',
    width: 100,
    align: 'center',
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
    field: 'planCode',
    label: '计划编号',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    field: 'planName',
    label: '计划名称',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    field: 'plotName',
    label: '地块名称',
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
    field: 'soilFertility',
    label: '土壤肥力',
    component: 'Select',
    componentProps: {
      options: [
        { label: '高', value: '1' },
        { label: '中', value: '2' },
        { label: '低', value: '3' },
      ],
    },
    colProps: { span: 6 },
  },
  {
    field: 'planStatus',
    label: '计划状态',
    component: 'Select',
    componentProps: {
      options: [
        { label: '计划中', value: '0' },
        { label: '执行中', value: '1' },
        { label: '已完成', value: '2' },
        { label: '已取消', value: '3' },
      ],
    },
    colProps: { span: 6 },
  },
  {
    field: 'responsiblePerson',
    label: '负责人',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    field: 'sowingTimeRange',
    label: '播种时间',
    component: 'RangePicker',
    componentProps: {
      format: 'YYYY-MM-DD',
      valueFormat: 'YYYY-MM-DD',
    },
    colProps: { span: 6 },
  },
  {
    field: 'expectedHarvestTimeRange',
    label: '预计收获时间',
    component: 'RangePicker',
    componentProps: {
      format: 'YYYY-MM-DD',
      valueFormat: 'YYYY-MM-DD',
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
    field: 'baseId',
    label: '基地',
    component: 'Input',
    show: false, // 隐藏字段，通过模态框组件自动设置
  },
  {
    field: 'planCode',
    label: '计划编号',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: '请输入计划编号!' }],
  },
  {
    field: 'planName',
    label: '计划名称',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: '请输入计划名称!' }],
  },
  {
    field: 'plotId',
    label: '地块',
    component: 'Select',
    required: true,
    componentProps: {
      options: [], // 初始为空，通过模态框组件动态设置
      showSearch: true,
      optionFilterProp: 'children',
      placeholder: '请先选择基地',
      filterOption: (input: string, option: any) => {
        return option.children.toLowerCase().indexOf(input.toLowerCase()) >= 0;
      },
    },
    rules: [{ required: true, message: '请选择地块!' }],
  },
  {
    field: 'varietyId',
    label: '品种',
    component: 'Select',
    required: true,
    componentProps: {
      api: () => Promise.resolve([]), // 实际项目中替换为真实API
      labelField: 'varietyName',
      valueField: 'id',
      showSearch: true,
      optionFilterProp: 'children',
    },
    rules: [{ required: true, message: '请选择品种!' }],
  },
  {
    field: 'plantingArea',
    label: '种植面积(亩)',
    component: 'InputNumber',
    required: true,
    componentProps: {
      min: 0,
      precision: 2,
      placeholder: '请输入种植面积',
    },
    rules: [{ required: true, message: '请输入种植面积!' }],
  },
  {
    field: 'targetYield',
    label: '目标产量(kg/亩)',
    component: 'InputNumber',
    required: true,
    componentProps: {
      min: 0,
      precision: 2,
      placeholder: '请输入目标产量',
    },
    rules: [{ required: true, message: '请输入目标产量!' }],
  },
  {
    field: 'soilFertility',
    label: '土壤肥力',
    component: 'Select',
    required: true,
    componentProps: {
      options: [
        { label: '高', value: '1' },
        { label: '中', value: '2' },
        { label: '低', value: '3' },
      ],
      placeholder: '请选择土壤肥力',
    },
    rules: [{ required: true, message: '请选择土壤肥力!' }],
  },
  {
    field: 'seedInput',
    label: '种子投入(kg/亩)',
    component: 'InputNumber',
    required: true,
    componentProps: {
      min: 0,
      precision: 2,
      placeholder: '请输入种子投入量',
    },
    rules: [{ required: true, message: '请输入种子投入量!' }],
  },
  {
    field: 'fertilizerInput',
    label: '肥料投入(kg/亩)',
    component: 'InputNumber',
    required: true,
    componentProps: {
      min: 0,
      precision: 2,
      placeholder: '请输入肥料投入量',
    },
    rules: [{ required: true, message: '请输入肥料投入量!' }],
  },
  {
    field: 'pesticideInput',
    label: '农药投入(kg/亩)',
    component: 'InputNumber',
    required: true,
    componentProps: {
      min: 0,
      precision: 2,
      placeholder: '请输入农药投入量',
    },
    rules: [{ required: true, message: '请输入农药投入量!' }],
  },
  {
    field: 'sowingTime',
    label: '播种时间',
    component: 'DatePicker',
    required: true,
    componentProps: {
      format: 'YYYY-MM-DD',
      valueFormat: 'YYYY-MM-DD',
      placeholder: '请选择播种时间',
    },
    rules: [{ required: true, message: '请选择播种时间!' }],
  },
  {
    field: 'expectedHarvestTime',
    label: '预计收获时间',
    component: 'DatePicker',
    required: true,
    componentProps: {
      format: 'YYYY-MM-DD',
      valueFormat: 'YYYY-MM-DD',
      placeholder: '请选择预计收获时间',
    },
    rules: [{ required: true, message: '请选择预计收获时间!' }],
  },
  {
    field: 'planStatus',
    label: '计划状态',
    component: 'Select',
    defaultValue: '0',
    componentProps: {
      options: [
        { label: '计划中', value: '0' },
        { label: '执行中', value: '1' },
        { label: '已完成', value: '2' },
        { label: '已取消', value: '3' },
      ],
    },
  },
  {
    field: 'responsiblePerson',
    label: '负责人',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: '请输入负责人!' }],
  },
  {
    field: 'contactPhone',
    label: '联系电话',
    component: 'Input',
    rules: [
      { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码!' },
    ],
  },
  {
    field: 'planDescription',
    label: '计划描述',
    component: 'InputTextArea',
    componentProps: {
      rows: 4,
      placeholder: '请输入计划描述',
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