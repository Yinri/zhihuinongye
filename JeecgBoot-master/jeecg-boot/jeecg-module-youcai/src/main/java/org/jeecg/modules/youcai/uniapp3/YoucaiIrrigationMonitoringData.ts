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
    title: '灌溉日期',
    align:"center",
    dataIndex: 'irrigationDate',
   },
   {
    title: '灌溉方式',
    align:"center",
    dataIndex: 'irrigationMethod'
   },
   {
    title: '灌溉量(立方米)',
    align:"center",
    dataIndex: 'waterAmount'
   },
   {
    title: '灌溉时长(分钟)',
    align:"center",
    dataIndex: 'irrigationDuration'
   },
];