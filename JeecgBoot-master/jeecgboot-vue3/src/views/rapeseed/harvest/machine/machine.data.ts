import { BasicColumn, FormSchema } from '/@/components/Table';

export const columns: BasicColumn[] = [
  { title: '农机名称', dataIndex: 'machineName', width: 150 },
  { title: '品牌', dataIndex: 'brand', width: 120 },
  { title: '型号', dataIndex: 'model', width: 120 },
  { title: '状态', dataIndex: 'status', width: 120 },
  { title: '最后位置', dataIndex: 'lastLocation' },
  { title: '备注', dataIndex: 'remark' },
  { title: '创建时间', dataIndex: 'createTime', width: 180, customRender: ({ text }) => {
      if (!text) return '';
      // 格式化日期，只显示年月日
      const date = new Date(text);
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      return `${year}-${month}-${day}`;
    }, },
];

export const searchFormSchema: FormSchema[] = [
  { field: 'machineName', label: '农机名称', component: 'Input', colProps: { span: 8 } },
  { field: 'status', label: '状态', component: 'Select', colProps: { span: 8 }, componentProps: {
    options: [
      { label: '空闲', value: '空闲' },
      { label: '作业中', value: '作业中' },
      { label: '维修中', value: '维修中' },
    ],
  } },
];

export const formSchema: FormSchema[] = [
  { field: 'id', label: 'ID', component: 'Input', show: false },
  { field: 'machineName', label: '农机名称', component: 'Input', required: true },
  { field: 'brand', label: '品牌', component: 'Input' },
  { field: 'model', label: '型号', component: 'Input' },
  { field: 'status', label: '状态', component: 'Select', required: true, componentProps: {
    options: [
      { label: '空闲', value: '空闲' },
      { label: '作业中', value: '作业中' },
      { label: '维修中', value: '维修中' },
    ],
  } },
  { field: 'lastLocation', label: '最后位置', component: 'Input' },
  { field: 'remark', label: '备注', component: 'InputTextArea' },
];