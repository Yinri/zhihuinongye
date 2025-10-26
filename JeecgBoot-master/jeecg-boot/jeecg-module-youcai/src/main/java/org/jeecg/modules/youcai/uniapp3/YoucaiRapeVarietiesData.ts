import { render } from '@/common/renderUtils';
//列表数据
export const columns = [
    {
    title: '品种名称',
    align:"center",
    dataIndex: 'varietyName'
   },
   {
    title: '品种特性',
    align:"center",
    dataIndex: 'characteristics'
   },
   {
    title: '生长周期(天)',
    align:"center",
    dataIndex: 'growthCycle'
   },
   {
    title: '产量潜力(公斤/亩)',
    align:"center",
    dataIndex: 'yieldPotential'
   },
   {
    title: '抗病性',
    align:"center",
    dataIndex: 'diseaseResistance'
   },
];