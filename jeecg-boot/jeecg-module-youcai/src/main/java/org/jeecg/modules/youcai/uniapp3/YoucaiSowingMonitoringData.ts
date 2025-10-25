import { render } from '@/common/renderUtils';
//列表数据
export const columns = [
    {
    title: '地块ID',
    align:"center",
    dataIndex: 'plotId'
   },
   {
    title: '生产计划ID',
    align:"center",
    dataIndex: 'planId'
   },
   {
    title: '播种日期',
    align:"center",
    dataIndex: 'sowingDate',
   },
   {
    title: '播种方式',
    align:"center",
    dataIndex: 'sowingMethod'
   },
   {
    title: '播种量(公斤/亩)',
    align:"center",
    dataIndex: 'seedingRate'
   },
   {
    title: '实际播种面积(亩)',
    align:"center",
    dataIndex: 'actualSowingArea'
   },
   {
    title: '播种状态',
    align:"center",
    dataIndex: 'sowingStatus'
   },
];