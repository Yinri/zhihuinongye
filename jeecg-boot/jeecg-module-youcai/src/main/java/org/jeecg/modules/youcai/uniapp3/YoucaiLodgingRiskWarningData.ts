import { render } from '@/common/renderUtils';
//列表数据
export const columns = [
    {
    title: '地块ID，外键关联youcai_plots表',
    align:"center",
    dataIndex: 'plotId'
   },
   {
    title: '风险等级：低 / 中 / 高',
    align:"center",
    dataIndex: 'riskLevel'
   },
   {
    title: '倒伏风险得分，0-1之间的小数',
    align:"center",
    dataIndex: 'riskScore'
   },
   {
    title: '建议措施，如"及时排水、加固植株"',
    align:"center",
    dataIndex: 'suggestedMeasures'
   },
   {
    title: '预警状态',
    align:"center",
    dataIndex: 'warningStatus'
   },
];