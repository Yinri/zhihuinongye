import { render } from '@/common/renderUtils';
//列表数据
export const columns = [
    {
    title: '生产计划ID',
    align:"center",
    dataIndex: 'planId'
   },
   {
    title: '品种ID',
    align:"center",
    dataIndex: 'varietyId'
   },
   {
    title: '种子用量(公斤)',
    align:"center",
    dataIndex: 'seedAmount'
   },
   {
    title: '种子成本(元)',
    align:"center",
    dataIndex: 'seedCost'
   },
   {
    title: '购买日期',
    align:"center",
    dataIndex: 'purchaseDate',
   },
   {
    title: '供应商',
    align:"center",
    dataIndex: 'supplier'
   },
];