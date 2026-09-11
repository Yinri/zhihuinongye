import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { h } from 'vue';
import { Tag } from 'ant-design-vue';

// 表格列配置
export const columns: BasicColumn[] = [
  {
    title: '地块编号',
    dataIndex: 'id',
    width: 120,
    align: 'center',
  },
  {
    title: '地块编码',
    dataIndex: 'plotCode',
    width: 120,
    align: 'center',
  },
  {
    title: '地块名称',
    dataIndex: 'plotName',
    width: 180,
  },
  {
    title: '地块面积(亩)',
    dataIndex: 'area',
    width: 100,
    align: 'center',
  },
  {
    title: '土壤类型',
    dataIndex: 'soilType',
    width: 100,
    align: 'center',
  },
  {
    title: '地块状态',
    dataIndex: 'status',
    width: 100,
    align: 'center',
    customRender: ({ record }) => {
      const status = record.status;
      let color = '';
      let text = '';
      
      switch(status) {
        case '空闲':
          color = 'green';
          text = '空闲';
          break;
        case '种植中':
          color = 'blue';
          text = '种植中';
          break;
        case '休耕':
          color = 'orange';
          text = '休耕';
          break;
        default:
          color = 'default';
          text = status || '-';
      }
      
      return h(Tag, { color }, () => text);
    },
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
    field: 'id',
    label: '地块编号',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    field: 'plotCode',
    label: '地块编码',
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
    field: 'baseId',
    label: '所属基地ID',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    field: 'soilType',
    label: '土壤类型',
    component: 'Select',
    componentProps: {
      options: [
        { label: '黏土', value: '黏土' },
        { label: '沙土', value: '沙土' },
        { label: '壤土', value: '壤土' },
      ],
      allowClear: true,
    },
    colProps: { span: 6 },
  },
  {
    field: 'status',
    label: '地块状态',
    component: 'Select',
    componentProps: {
      options: [
        { label: '空闲', value: '空闲' },
        { label: '种植中', value: '种植中' },
        { label: '休耕', value: '休耕' },
      ],
      allowClear: true,
    },
    colProps: { span: 6 },
  },
];

// 表单字段配置
export const formSchema: FormSchema[] = [
  {
    field: 'id',
    label: '地块ID',
    component: 'Input',
    show: false,
  },
  {
    field: 'plotCode',
    label: '地块编码',
    component: 'Input',
    required: true,
    disabled: true, // 禁用，由后端自动生成
    componentProps: {
      placeholder: '系统自动生成',
    },
    ifShow: ({ values }) => {
      // 只有在编辑模式下才显示地块编码（有id值表示是编辑模式）
      return !!values.id;
    },
    rules: [{ required: true, message: '地块编码不能为空!' }],
  },
  {
    field: 'plotName',
    label: '地块名称',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: '请输入地块名称!' }],
  },
  {
    field: 'baseId',
    label: '所属基地ID',
    component: 'Input',
    show: false, // 隐藏字段，但保留用于数据传递
  },
  {
    field: 'area',
    label: '地块面积(亩)',
    component: 'InputNumber',
    required: true,
    componentProps: {
      min: 0,
      precision: 2,
      placeholder: '请输入地块面积',
    },
    rules: [{ required: true, message: '请输入地块面积!' }],
  },
  {
    field: 'soilType',
    label: '土壤类型',
    component: 'Select',
    defaultValue: '黏土',
    componentProps: {
      options: [
        { label: '黏土', value: '黏土' },
        { label: '沙土', value: '沙土' },
        { label: '壤土', value: '壤土' },
      ],
    },
    required: true,
    rules: [{ required: true, message: '请选择土壤类型!' }],
  },
  {
    field: 'status',
    label: '地块状态',
    component: 'Select',
    defaultValue: '空闲',
    componentProps: {
      options: [
        { label: '空闲', value: '空闲' },
        { label: '种植中', value: '种植中' },
        { label: '休耕', value: '休耕' },
      ],
    },
    required: true,
    rules: [{ required: true, message: '请选择地块状态!' }],
  },
  {
    field: 'polygonCoords',
    label: '多边形坐标点',
    component: 'Input',
    show: false, // 隐藏字段，用于存储多边形坐标数据
  },
  {
    field: 'createBy',
    label: '创建人',
    component: 'Input',
    show: false, // 隐藏字段，系统自动填充
  },
  {
    field: 'createTime',
    label: '创建日期',
    component: 'DatePicker',
    show: false, // 隐藏字段，系统自动填充
  },
  {
    field: 'updateBy',
    label: '更新人',
    component: 'Input',
    show: false, // 隐藏字段，系统自动填充
  },
  {
    field: 'updateTime',
    label: '更新日期',
    component: 'DatePicker',
    show: false, // 隐藏字段，系统自动填充
  },
  {
    field: 'sysOrgCode',
    label: '所属部门',
    component: 'Input',
    show: false, // 隐藏字段，系统自动填充
  },
  {
    field: 'delFlag',
    label: '删除标志',
    component: 'Input',
    show: false, // 隐藏字段，系统自动填充
  },
];