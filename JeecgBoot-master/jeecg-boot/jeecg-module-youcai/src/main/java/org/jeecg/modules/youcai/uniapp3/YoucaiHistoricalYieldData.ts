import { render } from '@/common/renderUtils';
//列表数据
export const columns = [
    {
    title: '年份（如2022）',
    align:"center",
    dataIndex: 'year'
   },
   {
    title: '单产（kg/亩）',
    align:"center",
    dataIndex: 'yield'
   },
   {
    title: '品种ID（关联品种表）',
    align:"center",
    dataIndex: 'varietyId'
   },
   {
    title: '地块名称',
    align:"center",
    dataIndex: 'plot'
   },
];