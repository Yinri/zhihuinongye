import { FormSchema } from '/@/components/Form';
import { BasicColumn } from '/@/components/Table';
import { getPlotInfoList } from '../plot-info/plotInfo.api';

export const columns: BasicColumn[] = [
  {
    title: '地块编号',
    dataIndex: 'plotCode',
    width: 120,
  },
  {
    title: '地块名',
    dataIndex: 'plotName',
    width: 150,
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
    customRender: ({ text }) => {
      if (!text) return '';
      // 格式化日期，只显示年月日
      const date = new Date(text);
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      return `${year}-${month}-${day}`;
    },
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
    field: 'plotName',
    label: '地块名',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'harvestDate',
    label: '收获日期',
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
    show: false, // 默认隐藏基地ID字段
    required: true,
    rules: [{ required: true, message: '请输入基地ID' }],
  },
  {
    field: 'plotId',
    label: '地块',
    component: 'Select',
    show: false, // 默认隐藏地块字段，新增时动态显示
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