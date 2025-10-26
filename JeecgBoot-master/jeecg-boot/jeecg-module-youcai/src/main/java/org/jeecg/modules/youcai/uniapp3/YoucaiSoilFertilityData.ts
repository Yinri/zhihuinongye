import { render } from '@/common/renderUtils';
//列表数据
export const columns = [
    {
    title: '地块ID',
    align:"center",
    dataIndex: 'plotId'
   },
   {
    title: '检测日期',
    align:"center",
    dataIndex: 'testDate',
   },
   {
    title: 'pH值',
    align:"center",
    dataIndex: 'phValue'
   },
   {
    title: '有机质含量(g/kg)',
    align:"center",
    dataIndex: 'organicMatter'
   },
   {
    title: '氮含量(mg/kg)',
    align:"center",
    dataIndex: 'nitrogen'
   },
   {
    title: '磷含量(mg/kg)',
    align:"center",
    dataIndex: 'phosphorus'
   },
   {
    title: '钾含量(mg/kg)',
    align:"center",
    dataIndex: 'potassium'
   },
   {
    title: '肥力等级',
    align:"center",
    dataIndex: 'fertilityLevel'
   },
];