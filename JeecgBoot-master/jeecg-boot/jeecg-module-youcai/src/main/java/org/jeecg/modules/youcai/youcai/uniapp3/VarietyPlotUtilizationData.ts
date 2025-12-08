import { render } from '@/common/renderUtils';
//列表数据
export const columns = [
    {
    title: '品种ID（关联作物品种表主键）',
    align:"center",
    dataIndex: 'varietyId'
   },
   {
    title: '地块ID（关联地块表主键）',
    align:"center",
    dataIndex: 'plotId'
   },
   {
    title: '肥料ID（关联肥料表主键）',
    align:"center",
    dataIndex: 'fertilizerId'
   },
   {
    title: '肥料类型（冗余字段：如尿素、磷酸二铵）',
    align:"center",
    dataIndex: 'fertilizerType'
   },
   {
    title: '氮肥利用率(%)',
    align:"center",
    dataIndex: 'nRate'
   },
   {
    title: '磷肥利用率(%)',
    align:"center",
    dataIndex: 'pRate'
   },
   {
    title: '钾肥利用率(%)',
    align:"center",
    dataIndex: 'kRate'
   },
];