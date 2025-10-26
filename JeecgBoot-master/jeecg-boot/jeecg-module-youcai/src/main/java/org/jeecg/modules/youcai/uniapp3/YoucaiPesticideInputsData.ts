import { render } from '@/common/renderUtils';
//列表数据
export const columns = [
    {
    title: '生产计划ID',
    align:"center",
    dataIndex: 'planId'
   },
   {
    title: '农药名称',
    align:"center",
    dataIndex: 'pesticideName'
   },
   {
    title: '农药类型',
    align:"center",
    dataIndex: 'pesticideType'
   },
   {
    title: '防治对象',
    align:"center",
    dataIndex: 'targetPest'
   },
   {
    title: '使用量(公斤/升)',
    align:"center",
    dataIndex: 'amount'
   },
   {
    title: '成本(元)',
    align:"center",
    dataIndex: 'cost'
   },
   {
    title: '施药日期',
    align:"center",
    dataIndex: 'applicationDate',
   },
   {
    title: '施药方式',
    align:"center",
    dataIndex: 'applicationMethod'
   },
];