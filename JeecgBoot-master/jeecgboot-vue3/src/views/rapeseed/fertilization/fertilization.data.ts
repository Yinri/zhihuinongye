import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { h } from 'vue';
import { Tag } from 'ant-design-vue';

// 表格列配置（修复字段映射：与实体类 YoucaiFertilization 字段完全一致）
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
    dataIndex: 'fertilizerAmountKgPerMu', // ✅ 修复：对应实体类字段 fertilizerAmountKgPerMu
    width: 120,
  },
  {
    title: '施肥面积(亩)',
    dataIndex: 'fertilizationAreaMu', // ✅ 修复：对应实体类字段 fertilizationAreaMu
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
  // 可选：添加算法推荐字段显示（如需要在表格中展示推荐用量）
  {
    title: 'N推荐量(kg/亩)',
    dataIndex: 'nRecommendKgPerMu',
    width: 130,
  },
  {
    title: 'P₂O₅推荐量(kg/亩)',
    dataIndex: 'p2o5RecommendKgPerMu',
    width: 140,
  },
  {
    title: 'K₂O推荐量(kg/亩)',
    dataIndex: 'k2oRecommendKgPerMu',
    width: 140,
  },
];

// 查询表单配置（无字段错误，保持原样）
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

// 表单配置（修复字段映射：与实体类字段完全一致）
export const formSchema: FormSchema[] = [
  {
    field: 'plotId',
    label: '地块ID',
    component: 'InputNumber',
    show: false,
  },
  {
    field: 'baseId',
    label: '基地ID',
    component: 'InputNumber',
    show: false,
  },
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
    field: 'fertilizerAmountKgPerMu', // ✅ 修复：对应实体类字段 fertilizerAmountKgPerMu
    label: '施肥量(kg/亩)',
    component: 'InputNumber',
    required: true,
    colProps: { span: 12 },
    componentProps: {
      min: 0,
      precision: 2, // 保留2位小数，符合数据库字段类型
    },
  },
  {
    field: 'fertilizationAreaMu', // ✅ 修复：对应实体类字段 fertilizationAreaMu
    label: '施肥面积(亩)',
    component: 'InputNumber',
    required: true,
    colProps: { span: 12 },
    componentProps: {
      min: 0,
      precision: 2, // 保留2位小数，符合数据库字段类型
    },
  },
  {
    field: 'fertilizationDate',
    label: '施肥日期',
    component: 'DatePicker',
    required: true,
    colProps: { span: 12 },
    componentProps: {
      format: 'YYYY-MM-DD HH:mm:ss',
      valueFormat: 'YYYY-MM-DD HH:mm:ss',
    },
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
  // 可选：添加目标产量字段（供算法计算推荐值使用）
  {
    field: 'targetYieldKgPerMu',
    label: '目标产量(kg/亩)',
    component: 'InputNumber',
    required: true,
    colProps: { span: 12 },
    componentProps: {
      min: 0,
      precision: 1,
      defaultValue: 180, // 默认油菜目标产量180kg/亩
    },
  },
  {
    field: 'remark',
    label: '备注',
    component: 'InputTextArea',
    colProps: { span: 24 },
  },
];