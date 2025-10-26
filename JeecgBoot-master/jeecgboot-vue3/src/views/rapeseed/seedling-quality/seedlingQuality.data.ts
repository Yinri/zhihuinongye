import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { h } from 'vue';
import { Tag } from 'ant-design-vue';

// 表格列配置
export const columns: BasicColumn[] = [
  {
    title: '检测编号',
    dataIndex: 'checkCode',
    width: 120,
    align: 'center',
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
    title: '检测日期',
    dataIndex: 'checkDate',
    width: 120,
    align: 'center',
  },
  {
    title: '苗龄(天)',
    dataIndex: 'seedlingAge',
    width: 100,
    align: 'center',
  },
  {
    title: '株高(cm)',
    dataIndex: 'plantHeight',
    width: 100,
    align: 'center',
  },
  {
    title: '茎粗(mm)',
    dataIndex: 'stemDiameter',
    width: 100,
    align: 'center',
  },
  {
    title: '叶片数',
    dataIndex: 'leafCount',
    width: 100,
    align: 'center',
  },
  {
    title: '根长(cm)',
    dataIndex: 'rootLength',
    width: 100,
    align: 'center',
  },
  {
    title: '鲜重(g)',
    dataIndex: 'freshWeight',
    width: 100,
    align: 'center',
  },
  {
    title: '干重(g)',
    dataIndex: 'dryWeight',
    width: 100,
    align: 'center',
  },
  {
    title: '壮苗指数',
    dataIndex: 'seedlingIndex',
    width: 100,
    align: 'center',
  },
  {
    title: '苗素质等级',
    dataIndex: 'qualityGrade',
    width: 120,
    align: 'center',
    customRender: ({ text }) => {
      const grade = text || '0';
      const color = grade === '0' ? 'green' : grade === '1' ? 'blue' : grade === '2' ? 'orange' : 'red';
      const gradeText = grade === '0' ? '优' : grade === '1' ? '良' : grade === '2' ? '中' : '差';
      return h(Tag, { color }, () => gradeText);
    },
  },
  {
    title: '检测人员',
    dataIndex: 'inspector',
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
    field: 'checkCode',
    label: '检测编号',
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
    field: 'qualityGrade',
    label: '苗素质等级',
    component: 'Select',
    componentProps: {
      options: [
        { label: '优', value: '0' },
        { label: '良', value: '1' },
        { label: '中', value: '2' },
        { label: '差', value: '3' },
      ],
    },
    colProps: { span: 6 },
  },
  {
    field: 'checkDate',
    label: '检测日期',
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
    field: 'checkCode',
    label: '检测编号',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: '请输入检测编号!' }],
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
    field: 'varietyId',
    label: '品种',
    component: 'Select',
    componentProps: {
      api: () => {
        // 这里应该调用获取品种列表的API
        return Promise.resolve([
          { label: '品种A', value: '1' },
          { label: '品种B', value: '2' },
          { label: '品种C', value: '3' },
        ]);
      },
      labelField: 'label',
      valueField: 'value',
      immediate: true,
    },
    rules: [{ required: true, message: '请选择品种!' }],
  },
  {
    field: 'checkDate',
    label: '检测日期',
    component: 'DatePicker',
    required: true,
    rules: [{ required: true, message: '请选择检测日期!' }],
  },
  {
    field: 'seedlingAge',
    label: '苗龄(天)',
    component: 'InputNumber',
    required: true,
    componentProps: {
      min: 0,
      placeholder: '请输入苗龄',
    },
    rules: [{ required: true, message: '请输入苗龄!' }],
  },
  {
    field: 'plantHeight',
    label: '株高(cm)',
    component: 'InputNumber',
    required: true,
    componentProps: {
      min: 0,
      precision: 2,
      placeholder: '请输入株高',
    },
    rules: [{ required: true, message: '请输入株高!' }],
  },
  {
    field: 'stemDiameter',
    label: '茎粗(mm)',
    component: 'InputNumber',
    required: true,
    componentProps: {
      min: 0,
      precision: 2,
      placeholder: '请输入茎粗',
    },
    rules: [{ required: true, message: '请输入茎粗!' }],
  },
  {
    field: 'leafCount',
    label: '叶片数',
    component: 'InputNumber',
    required: true,
    componentProps: {
      min: 0,
      placeholder: '请输入叶片数',
    },
    rules: [{ required: true, message: '请输入叶片数!' }],
  },
  {
    field: 'rootLength',
    label: '根长(cm)',
    component: 'InputNumber',
    componentProps: {
      min: 0,
      precision: 2,
      placeholder: '请输入根长',
    },
  },
  {
    field: 'freshWeight',
    label: '鲜重(g)',
    component: 'InputNumber',
    componentProps: {
      min: 0,
      precision: 2,
      placeholder: '请输入鲜重',
    },
  },
  {
    field: 'dryWeight',
    label: '干重(g)',
    component: 'InputNumber',
    componentProps: {
      min: 0,
      precision: 2,
      placeholder: '请输入干重',
    },
  },
  {
    field: 'seedlingIndex',
    label: '壮苗指数',
    component: 'InputNumber',
    componentProps: {
      min: 0,
      precision: 2,
      placeholder: '请输入壮苗指数',
    },
  },
  {
    field: 'qualityGrade',
    label: '苗素质等级',
    component: 'Select',
    defaultValue: '0',
    componentProps: {
      options: [
        { label: '优', value: '0' },
        { label: '良', value: '1' },
        { label: '中', value: '2' },
        { label: '差', value: '3' },
      ],
    },
  },
  {
    field: 'inspector',
    label: '检测人员',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: '请输入检测人员!' }],
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