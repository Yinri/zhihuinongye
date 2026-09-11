import { BasicColumn, FormSchema } from '/@/components/Table';

export const columns: BasicColumn[] = [
  { title: '任务标题', dataIndex: 'taskTitle', width: 230 },
  { title: '计划收获日期', dataIndex: 'plannedDate', width: 180, customRender: ({ text }) => {
      if (!text) return '';
      // 格式化日期，只显示年月日
      const date = new Date(text);
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      return `${year}-${month}-${day}`;
    }, },
  { title: '计划面积(亩)', dataIndex: 'plannedArea', width: 120 },
  { title: '分配农机', dataIndex: 'assignedMachineName', width: 120 },
  { title: '状态', dataIndex: 'status', width: 120 },
  { title: '进度(%)', dataIndex: 'progressPercent', width: 120 },
  { title: '备注', dataIndex: 'remark' },
  { title: '创建时间', dataIndex: 'createTime', width: 180 },
];

export const searchFormSchema: FormSchema[] = [
  { field: 'status', label: '状态', component: 'Select', colProps: { span: 8 }, componentProps: {
    options: [
      { label: '计划中', value: '计划中' },
      { label: '执行中', value: '执行中' },
      { label: '已完成', value: '已完成' },
      { label: '已取消', value: '已取消' },
    ],
  } },
];

export const formSchema: FormSchema[] = [
  { field: 'id', label: 'ID', component: 'Input', show: false },
  { field: 'taskTitle', label: '任务标题', component: 'Input', required: true },
  { field: 'plannedDate', label: '计划收获日期', component: 'DatePicker', componentProps: { format: 'YYYY-MM-DD' } },
  { field: 'plannedArea', label: '计划面积(亩)', component: 'InputNumber', componentProps: { min: 0, precision: 2 } },
  { field: 'assignedMachineId', label: '分配农机', component: 'Select', componentProps: { options: [] } },
  { field: 'status', label: '状态', component: 'Select', required: true, componentProps: {
    options: [
      { label: '计划中', value: '计划中' },
      { label: '执行中', value: '执行中' },
      { label: '已完成', value: '已完成' },
      { label: '已取消', value: '已取消' },
    ],
  } },
  { field: 'progressPercent', label: '进度(%)', component: 'InputNumber', componentProps: { min: 0, max: 100, step: 1 } },
  { field: 'remark', label: '备注', component: 'InputTextArea' },
];