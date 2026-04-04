import { BasicColumn, FormSchema } from '/@/components/Table';

export const columns: BasicColumn[] = [
  { title: '北斗设备编码', dataIndex: 'beidouSn', width: 150 },
  { title: '车辆编号', dataIndex: 'vehicleNumber', width: 120 },
  { title: '农机品牌', dataIndex: 'brand', width: 120 },
  { title: '农机型号', dataIndex: 'model', width: 120 },
  { title: '机主姓名', dataIndex: 'ownerName', width: 100 },
  { title: '机主电话', dataIndex: 'ownerPhone', width: 130 },
  { title: '所属基地', dataIndex: 'baseName', width: 120 },
];

export const searchFormSchema: FormSchema[] = [
  { field: 'beidouSn', label: '北斗设备编码', component: 'Input', colProps: { span: 8 } },
  { field: 'vehicleNumber', label: '车辆编号', component: 'Input', colProps: { span: 8 } },
  { field: 'baseName', label: '所属基地', component: 'Input', colProps: { span: 8 } },
];

export const formSchema: FormSchema[] = [
  { field: 'id', label: 'ID', component: 'Input', show: false },
  { field: 'beidouSn', label: '北斗设备编码', component: 'Input', required: true, rules: [{ required: true, message: '请输入北斗设备编码' }] },
  { field: 'vehicleNumber', label: '车辆编号', component: 'Input', required: true, rules: [{ required: true, message: '请输入车辆编号' }] },
  { field: 'brand', label: '农机品牌', component: 'Input' },
  { field: 'model', label: '农机型号', component: 'Input' },
  { field: 'ownerName', label: '机主姓名', component: 'Input' },
  { field: 'ownerPhone', label: '机主电话', component: 'Input' },
  { field: 'baseName', label: '所属基地', component: 'Input' },
];
