import { render } from '@/common/renderUtils';
//列表数据
export const columns = [
    {
    title: '作物品种ID（关联品种表）',
    align:"center",
    dataIndex: 'varietyId'
   },
   {
    title: 'N肥需肥量（kg/100kg产量）',
    align:"center",
    dataIndex: 'nDemand'
   },
   {
    title: 'P₂O₅需肥量（kg/100kg产量）',
    align:"center",
    dataIndex: 'pDemand'
   },
   {
    title: 'K₂O需肥量（kg/100kg产量）',
    align:"center",
    dataIndex: 'kDemand'
   },
];