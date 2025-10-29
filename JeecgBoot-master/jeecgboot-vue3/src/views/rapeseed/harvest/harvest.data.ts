import { FormSchema } from '/@/components/Form';
import { BasicColumn } from '/@/components/Table';

export const columns: BasicColumn[] = [
  {
    title: '地块名称',
    dataIndex: 'plotName',
    width: 120,
  },

  {
    title: '面积(亩)',
    dataIndex: 'area',
    width: 120,
  },
  {
    title: '预计产量(kg)',
    dataIndex: 'predictYield',
    width: 120,
  },
  {
    title: '实际产量(kg)',
    dataIndex: 'totalYield',
    width: 120,
  },
  {
    title: '负责农机',
    dataIndex: 'machineName',
    width: 120,
  },
    {
    title: '收获日期',
    dataIndex: 'harvestDate',
    width: 120,
  },
];

export const searchFormSchema: FormSchema[] = [
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
];

export const formSchema: FormSchema[] = [
  {
    field: 'plotName',
    label: '地块名称',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: '请输入地块名称' }],
  },
  {
    field: 'area',
    label: '面积(亩)',
    component: 'InputNumber',
    required: true,
    rules: [{ required: true, message: '请输入面积' }],
  },
  {
    field: 'predictYield',
    label: '预计产量(kg)',
    component: 'InputNumber',
    required: true,
    rules: [{ required: true, message: '请输入预计产量' }],
  },
  {
    field: 'totalYield',
    label: '实际产量(kg)',
    component: 'InputNumber',
    required: true,
    rules: [{ required: true, message: '请输入实际产量' }],
  },
  {
    field: 'machineName',
    label: '负责农机',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: '请输入负责农机' }],
  },
  {
    field: 'harvestDate',
    label: '收获日期',
    component: 'DatePicker',
    required: true,
    rules: [{ required: true, message: '请选择收获日期' }],
  },
];