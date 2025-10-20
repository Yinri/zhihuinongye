import { render } from '@/common/renderUtils';
//列表数据
export const columns = [
    {
    title: '生产计划ID',
    align:"center",
    dataIndex: 'planId'
   },
   {
    title: '肥料类型',
    align:"center",
    dataIndex: 'fertilizerType'
   },
   {
    title: '肥料名称',
    align:"center",
    dataIndex: 'fertilizerName'
   },
   {
    title: 'NPK比例',
    align:"center",
    dataIndex: 'npkRatio'
   },
   {
    title: '施肥量(公斤)',
    align:"center",
    dataIndex: 'amount'
   },
   {
    title: '成本(元)',
    align:"center",
    dataIndex: 'cost'
   },
   {
    title: '施肥日期',
    align:"center",
    dataIndex: 'applicationDate',
   },
   {
    title: '施肥方式',
    align:"center",
    dataIndex: 'applicationMethod'
   },
];