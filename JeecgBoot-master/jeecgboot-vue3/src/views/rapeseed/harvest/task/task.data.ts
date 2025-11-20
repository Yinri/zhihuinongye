import { BasicColumn, FormSchema } from '/@/components/Table';

export const columns: BasicColumn[] = [
  { title: '基地ID', dataIndex: 'baseId', width: 100 },
  { title: '地块ID', dataIndex: 'plotId', width: 100 },
  { title: '任务标题', dataIndex: 'taskTitle', width: 180 },
  { title: '计划收获日期', dataIndex: 'plannedDate', width: 180 },
  { title: '计划面积(亩)', dataIndex: 'plannedArea', width: 120 },
  { title: '分配农机ID', dataIndex: 'assignedMachineId', width: 120 },
  { title: '状态', dataIndex: 'status', width: 120 },
  { title: '进度(%)', dataIndex: 'progressPercent', width: 120 },
  { title: '备注', dataIndex: 'remark' },
  { title: '创建时间', dataIndex: 'createTime', width: 180 },
];

export const searchFormSchema: FormSchema[] = [
  { field: 'baseId', label: '基地ID', component: 'Input', colProps: { span: 6 } },
  { field: 'plotId', label: '地块ID', component: 'Input', colProps: { span: 6 } },
  { field: 'status', label: '状态', component: 'Select', colProps: { span: 6 }, componentProps: {
    options: [
      { label: '计划中', value: 'planned' },
      { label: '进行中', value: 'running' },
      { label: '已完成', value: 'done' },
      { label: '已取消', value: 'canceled' },
    ],
  } },
];

export const formSchema: FormSchema[] = [
  { field: 'id', label: 'ID', component: 'Input', show: false },
  { field: 'baseId', label: '基地ID', component: 'InputNumber', required: true },
  { field: 'plotId', label: '地块ID', component: 'InputNumber', required: true },
  { field: 'taskTitle', label: '任务标题', component: 'Input', required: true },
  { field: 'plannedDate', label: '计划收获日期', component: 'DatePicker', componentProps: { showTime: true } },
  { field: 'plannedArea', label: '计划面积(亩)', component: 'InputNumber' },
  { field: 'assignedMachineId', label: '分配农机', component: 'Select', componentProps: { options: [] } },
  { field: 'status', label: '状态', component: 'Select', required: true, componentProps: {
    options: [
      { label: '计划中', value: 'planned' },
      { label: '进行中', value: 'running' },
      { label: '已完成', value: 'done' },
      { label: '已取消', value: 'canceled' },
    ],
  } },
  { field: 'progressPercent', label: '进度(%)', component: 'InputNumber', componentProps: { min: 0, max: 100, step: 1 } },
  { field: 'remark', label: '备注', component: 'InputTextArea' },
];