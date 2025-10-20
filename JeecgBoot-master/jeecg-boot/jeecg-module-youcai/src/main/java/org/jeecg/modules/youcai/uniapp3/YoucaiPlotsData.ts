import { render } from '@/common/renderUtils';
//列表数据
export const columns = [
    {
    title: '地块名称',
    align:"center",
    dataIndex: 'plotName'
   },
   {
    title: '所属基地ID',
    align:"center",
    dataIndex: 'baseId'
   },
   {
    title: '地块面积(亩)',
    align:"center",
    dataIndex: 'area'
   },
   {
    title: '纬度',
    align:"center",
    dataIndex: 'latitude'
   },
   {
    title: '经度',
    align:"center",
    dataIndex: 'longitude'
   },
];