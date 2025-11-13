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
    title: '生长阶段',
    dataIndex: 'growthStage',
    width: 120,
    align: 'center',
    customRender: ({ text }) => {
      const stage = text || '未播种';
      const colorMap: Record<string, string> = {
        '未播种': 'gray',
        '已播种': 'green',
        '苗期': 'blue',
        '蕾薹期': 'cyan',
        '开花期': 'orange',
        '角果成熟期': 'gold',
        '收获与整地': 'purple'
      };
      const color = colorMap[stage] || 'default';
      return h(Tag, { color }, () => stage);
    },
  },
  {
    title: '纬度',
    dataIndex: 'latitude',
    width: 100,
    align: 'center',
  },
  {
    title: '经度',
    dataIndex: 'longitude',
    width: 100,
    align: 'center',
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
    field: 'growthStage',
    label: '生长阶段',
    component: 'Select',
    componentProps: {
      options: [
        { label: '未播种', value: '未播种' },
        { label: '已播种', value: '已播种' },
        { label: '苗期', value: '苗期' },
        { label: '蕾薹期', value: '蕾薹期' },
        { label: '开花期', value: '开花期' },
        { label: '角果成熟期', value: '角果成熟期' },
        { label: '收获与整地', value: '收获与整地' },
      ],
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
    field: 'latitude',
    label: '纬度',
    component: 'InputNumber',
    show: false, // 隐藏字段
    required: false,
    componentProps: {
      min: -90,
      max: 90,
      precision: 6,
      placeholder: '请输入纬度',
    },
  },
  {
    field: 'longitude',
    label: '经度',
    component: 'InputNumber',
    show: false, // 隐藏字段
    required: false,
    componentProps: {
      min: -180,
      max: 180,
      precision: 6,
      placeholder: '请输入经度',
    },
  },
  {
    field: 'growthStage',
    label: '生长阶段',
    component: 'Select',
    defaultValue: '未播种',
    componentProps: {
      options: [
        { label: '未播种', value: '未播种' },
        { label: '已播种', value: '已播种' },
        { label: '苗期', value: '苗期' },
        { label: '蕾薹期', value: '蕾薹期' },
        { label: '开花期', value: '开花期' },
        { label: '角果成熟期', value: '角果成熟期' },
        { label: '收获与整地', value: '收获与整地' },
      ],
    },
    rules: [{ required: true, message: '请选择生长阶段!' }],
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