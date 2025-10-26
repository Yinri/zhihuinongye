import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { h } from 'vue';
import { Tag } from 'ant-design-vue';

// 表格列配置
export const columns: BasicColumn[] = [
  {
    title: '防控编号',
    dataIndex: 'controlCode',
    width: 120,
    align: 'center',
  },
  {
    title: '地块名称',
    dataIndex: 'plotName',
    width: 150,
  },
  {
    title: '病害名称',
    dataIndex: 'diseaseName',
    width: 150,
  },
  {
    title: '病害类型',
    dataIndex: 'diseaseType',
    width: 120,
    align: 'center',
    customRender: ({ text }) => {
      const type = text || '0';
      const color = type === '0' ? 'red' : type === '1' ? 'orange' : type === '2' ? 'blue' : 'green';
      const typeText = type === '0' ? '真菌性病害' : type === '1' ? '细菌性病害' : type === '2' ? '病毒性病害' : '生理性病害';
      return h(Tag, { color }, () => typeText);
    },
  },
  {
    title: '发生程度',
    dataIndex: 'occurrenceLevel',
    width: 100,
    align: 'center',
    customRender: ({ text }) => {
      const level = text || '0';
      const color = level === '0' ? 'green' : level === '1' ? 'blue' : level === '2' ? 'orange' : 'red';
      const levelText = level === '0' ? '轻度' : level === '1' ? '中度' : level === '2' ? '重度' : '严重';
      return h(Tag, { color }, () => levelText);
    },
  },
  {
    title: '防控日期',
    dataIndex: 'controlDate',
    width: 120,
    align: 'center',
  },
  {
    title: '防控方法',
    dataIndex: 'controlMethod',
    width: 120,
    align: 'center',
    customRender: ({ text }) => {
      const method = text || '0';
      const color = method === '0' ? 'blue' : method === '1' ? 'green' : method === '2' ? 'orange' : 'purple';
      const methodText = method === '0' ? '化学防治' : method === '1' ? '生物防治' : method === '2' ? '物理防治' : '农业防治';
      return h(Tag, { color }, () => methodText);
    },
  },
  {
    title: '药剂名称',
    dataIndex: 'pesticideName',
    width: 150,
  },
  {
    title: '使用剂量',
    dataIndex: 'dosage',
    width: 120,
    align: 'center',
  },
  {
    title: '防控面积(亩)',
    dataIndex: 'controlArea',
    width: 120,
    align: 'center',
  },
  {
    title: '防控效果',
    dataIndex: 'controlEffect',
    width: 100,
    align: 'center',
    customRender: ({ text }) => {
      const effect = text || '0';
      const color = effect === '0' ? 'green' : effect === '1' ? 'blue' : effect === '2' ? 'orange' : 'red';
      const effectText = effect === '0' ? '优秀' : effect === '1' ? '良好' : effect === '2' ? '一般' : '较差';
      return h(Tag, { color }, () => effectText);
    },
  },
  {
    title: '防控人员',
    dataIndex: 'controller',
    width: 120,
    align: 'center',
  },
  {
    title: '备注',
    dataIndex: 'remarks',
    width: 150,
    ellipsis: true,
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
    field: 'controlCode',
    label: '防控编号',
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
    field: 'diseaseName',
    label: '病害名称',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    field: 'diseaseType',
    label: '病害类型',
    component: 'Select',
    componentProps: {
      options: [
        { label: '真菌性病害', value: '0' },
        { label: '细菌性病害', value: '1' },
        { label: '病毒性病害', value: '2' },
        { label: '生理性病害', value: '3' },
      ],
    },
    colProps: { span: 6 },
  },
  {
    field: 'controlDate',
    label: '防控日期',
    component: 'RangePicker',
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
    field: 'controlCode',
    label: '防控编号',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: '请输入防控编号!' }],
  },
  {
    field: 'plotId',
    label: '地块',
    component: 'Select',
    componentProps: {
      api: () => {
        // 这里应该调用获取地块列表的API
        return Promise.resolve([
          { label: '地块A', value: '1' },
          { label: '地块B', value: '2' },
          { label: '地块C', value: '3' },
        ]);
      },
      labelField: 'label',
      valueField: 'value',
      immediate: true,
    },
    rules: [{ required: true, message: '请选择地块!' }],
  },
  {
    field: 'diseaseName',
    label: '病害名称',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: '请输入病害名称!' }],
  },
  {
    field: 'diseaseType',
    label: '病害类型',
    component: 'Select',
    defaultValue: '0',
    componentProps: {
      options: [
        { label: '真菌性病害', value: '0' },
        { label: '细菌性病害', value: '1' },
        { label: '病毒性病害', value: '2' },
        { label: '生理性病害', value: '3' },
      ],
    },
    rules: [{ required: true, message: '请选择病害类型!' }],
  },
  {
    field: 'occurrenceLevel',
    label: '发生程度',
    component: 'Select',
    defaultValue: '0',
    componentProps: {
      options: [
        { label: '轻度', value: '0' },
        { label: '中度', value: '1' },
        { label: '重度', value: '2' },
        { label: '严重', value: '3' },
      ],
    },
  },
  {
    field: 'controlDate',
    label: '防控日期',
    component: 'DatePicker',
    required: true,
    rules: [{ required: true, message: '请选择防控日期!' }],
  },
  {
    field: 'controlMethod',
    label: '防控方法',
    component: 'Select',
    defaultValue: '0',
    componentProps: {
      options: [
        { label: '化学防治', value: '0' },
        { label: '生物防治', value: '1' },
        { label: '物理防治', value: '2' },
        { label: '农业防治', value: '3' },
      ],
    },
    rules: [{ required: true, message: '请选择防控方法!' }],
  },
  {
    field: 'pesticideName',
    label: '药剂名称',
    component: 'Input',
  },
  {
    field: 'dosage',
    label: '使用剂量',
    component: 'Input',
    componentProps: {
      placeholder: '请输入使用剂量',
    },
  },
  {
    field: 'controlArea',
    label: '防控面积(亩)',
    component: 'InputNumber',
    required: true,
    componentProps: {
      min: 0,
      precision: 2,
      placeholder: '请输入防控面积',
    },
    rules: [{ required: true, message: '请输入防控面积!' }],
  },
  {
    field: 'controlEffect',
    label: '防控效果',
    component: 'Select',
    defaultValue: '0',
    componentProps: {
      options: [
        { label: '优秀', value: '0' },
        { label: '良好', value: '1' },
        { label: '一般', value: '2' },
        { label: '较差', value: '3' },
      ],
    },
  },
  {
    field: 'controller',
    label: '防控人员',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: '请输入防控人员!' }],
  },
  {
    field: 'symptoms',
    label: '病害症状',
    component: 'InputTextArea',
    componentProps: {
      rows: 3,
      placeholder: '请输入病害症状',
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
