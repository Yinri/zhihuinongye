import { FormSchema } from '/@/components/Form';
import { BasicColumn } from '/@/components/Table';

export const columns: BasicColumn[] = [
  {
    title: '基地ID',
    dataIndex: 'baseId',
    width: 100,
  },
  {
    title: '地块ID',
    dataIndex: 'plotId',
    width: 100,
  },
  {
    title: '本次收割面积(亩)',
    dataIndex: 'area',
    width: 140,
  },
  {
    title: '预计产量(kg)',
    dataIndex: 'predictYield',
    width: 130,
  },
  {
    title: '实际产量(kg)',
    dataIndex: 'totalYield',
    width: 130,
  },
  {
    title: '负责农机',
    dataIndex: 'machineName',
    width: 120,
  },
  {
    title: '收获日期',
    dataIndex: 'harvestDate',
    width: 160,
  },
  {
    title: '作业人员',
    dataIndex: 'operator',
    width: 100,
  },
  {
    title: '作业时长(小时)',
    dataIndex: 'workDuration',
    width: 130,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'baseId',
    label: '基地ID',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'plotId',
    label: '地块ID',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'harvestDate',
    label: '收获日期',
    component: 'RangePicker',
    colProps: { span: 8 },
    componentProps: {
      showTime: true,
      format: 'YYYY-MM-DD HH:mm:ss',
    },
  },
];

export const formSchema: FormSchema[] = [
  {
    field: 'baseId',
    label: '基地ID',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: '请输入基地ID' }],
  },
  {
    field: 'plotId',
    label: '地块ID',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: '请输入地块ID' }],
  },
  {
    field: 'area',
    label: '本次收割面积(亩)',
    component: 'InputNumber',
    required: true,
    rules: [{ required: true, message: '请输入面积' }],
    componentProps: {
      min: 0,
      precision: 2,
    },
  },
  {
    field: 'predictYield',
    label: '预计产量(kg)',
    component: 'InputNumber',
    componentProps: {
      min: 0,
      precision: 2,
    },
  },
  {
    field: 'totalYield',
    label: '实际产量(kg)',
    component: 'InputNumber',
    componentProps: {
      min: 0,
      precision: 2,
    },
  },
  {
    field: 'machineName',
    label: '负责农机',
    component: 'Input',
  },
  {
    field: 'harvestDate',
    label: '收获日期',
    component: 'DatePicker',
    required: true,
    rules: [{ required: true, message: '请选择收获日期' }],
    componentProps: {
      showTime: true,
      format: 'YYYY-MM-DD HH:mm:ss',
    },
  },
  {
    field: 'operator',
    label: '作业人员',
    component: 'Input',
  },
  {
    field: 'workDuration',
    label: '作业时长(小时)',
    component: 'InputNumber',
    componentProps: {
      min: 0,
      precision: 2,
    },
  },
  {
    field: 'remark',
    label: '备注',
    component: 'InputTextArea',
  },
];