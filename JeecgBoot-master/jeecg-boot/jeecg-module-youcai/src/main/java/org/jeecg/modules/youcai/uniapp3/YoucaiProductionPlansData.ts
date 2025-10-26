import { render } from '@/common/renderUtils';
//列表数据
export const columns = [
    {
    title: '地块ID',
    align:"center",
    dataIndex: 'plotId'
   },
   {
    title: '计划年份',
    align:"center",
    dataIndex: 'planYear'
   },
   {
    title: '油菜品种ID',
    align:"center",
    dataIndex: 'varietyId'
   },
   {
    title: '目标产量(公斤/亩)',
    align:"center",
    dataIndex: 'targetYield'
   },
   {
    title: '种植面积(亩)',
    align:"center",
    dataIndex: 'plantingArea'
   },
   {
    title: '计划播种日期',
    align:"center",
    dataIndex: 'plannedSowingDate',
   },
   {
    title: '计划收获日期',
    align:"center",
    dataIndex: 'plannedHarvestDate',
   },
   {
    title: '计划状态',
    align:"center",
    dataIndex: 'status'
   },
];