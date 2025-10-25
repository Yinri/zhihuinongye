import { render } from '@/common/renderUtils';
//列表数据
export const columns = [
    {
    title: '基地名称',
    align:"center",
    dataIndex: 'baseName'
   },
   {
    title: '负责人',
    align:"center",
    dataIndex: 'manager'
   },
   {
    title: '联系电话',
    align:"center",
    dataIndex: 'phone'
   },
   {
    title: '基地地址（具体地址）',
    align:"center",
    dataIndex: 'address'
   },
];