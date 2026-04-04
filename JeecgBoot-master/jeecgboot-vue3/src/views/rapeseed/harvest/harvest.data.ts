import { FormSchema } from '/@/components/Form';
import { BasicColumn } from '/@/components/Table';
import { getPlotInfoList } from '../plot-info/plotInfo.api';

export const columns: BasicColumn[] = [
  {
    title: '农机编号',
    dataIndex: 'vehicleNumber',
    width: 120,
  },
  {
    title: '收割面积(亩)',
    dataIndex: 'qualifiedArea',
    width: 130,
    customRender: ({ text }) => {
      return text ? Number(text).toFixed(2) : '0.00';
    },
  },
  {
    title: '累计产量(kg)',
    dataIndex: 'totalYield',
    width: 130,
    customRender: ({ text }) => {
      return text ? Number(text).toFixed(2) : '0.00';
    },
  },
  {
    title: '开始时间',
    dataIndex: 'startTime',
    width: 160,
    customRender: ({ text }) => {
      if (!text) return '-';
      return text.replace(/\.\d+$/, '');
    },
  },
  {
    title: '结束时间',
    dataIndex: 'endTime',
    width: 160,
    customRender: ({ text }) => {
      if (!text) return '-';
      return text.replace(/\.\d+$/, '');
    },
  },
  {
    title: '作业位置',
    dataIndex: 'location',
    width: 180,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'vehicleNumber',
    label: '农机编号',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'dateRange',
    label: '作业日期',
    component: 'RangePicker',
    colProps: { span: 8 },
    componentProps: {
      showTime: false,
      format: 'YYYY-MM-DD',
    },
  },
];

export const formSchema: FormSchema[] = [
  {
    field: 'baseId',
    label: '基地ID',
    component: 'Input',
    show: false,
    required: true,
    rules: [{ required: true, message: '请输入基地ID' }],
  },
  {
    field: 'plotId',
    label: '地块',
    component: 'Select',
    show: false,
    required: true,
    rules: [{ required: true, message: '请选择地块' }],
    componentProps: {
      options: [],
      fieldNames: { label: 'plotName', value: 'plotId' },
      showSearch: true,
      filterOption: (input: string, option: any) => {
        return option.plotName.toLowerCase().indexOf(input.toLowerCase()) >= 0;
      },
    },
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
      showTime: false,
      format: 'YYYY-MM-DD',
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
