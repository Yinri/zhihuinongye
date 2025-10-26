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
    title: '关联预警ID',
    align:"center",
    dataIndex: 'warningId'
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
    title: '防控日期',
    align:"center",
    dataIndex: 'controlDate',
   },
   {
    title: '防控方法',
    align:"center",
    dataIndex: 'controlMethod'
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
    title: '农药用量(毫升/亩)',
    align:"center",
    dataIndex: 'pesticideDosage'
   },
   {
    title: '农药浓度(%)',
    align:"center",
    dataIndex: 'pesticideConcentration'
   },
   {
    title: '施用方法',
    align:"center",
    dataIndex: 'applicationMethod'
   },
   {
    title: '防控面积(亩)',
    align:"center",
    dataIndex: 'controlArea'
   },
   {
    title: '防控成本(元)',
    align:"center",
    dataIndex: 'controlCost'
   },
   {
    title: '防控效果',
    align:"center",
    dataIndex: 'controlEffectiveness'
   },
   {
    title: '操作员',
    align:"center",
    dataIndex: 'operator'
   },
   {
    title: '备注',
    align:"center",
    dataIndex: 'notes'
   },
];