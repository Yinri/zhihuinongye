import { render } from '@/common/renderUtils';
//列表数据
export const columns = [
    {
    title: '地块ID',
    align:"center",
    dataIndex: 'plotId'
   },
   {
    title: '预警日期',
    align:"center",
    dataIndex: 'warningDate',
   },
   {
    title: '虫害类型',
    align:"center",
    dataIndex: 'pestType'
   },
   {
    title: '虫害名称',
    align:"center",
    dataIndex: 'pestName'
   },
   {
    title: '严重程度',
    align:"center",
    dataIndex: 'severity'
   },
   {
    title: '危害症状',
    align:"center",
    dataIndex: 'symptoms'
   },
   {
    title: '防治建议',
    align:"center",
    dataIndex: 'recommendation'
   },
   {
    title: '预警状态',
    align:"center",
    dataIndex: 'warningStatus'
   },
   {
    title: '虫害影像',
    align:"center",
    dataIndex: 'imageUrl'
   },
];