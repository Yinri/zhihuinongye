import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '地块ID，外键关联youcai_plots表',
    align: "center",
    dataIndex: 'plotId'
  },
  {
    title: '风险等级：低 / 中 / 高',
    align: "center",
    dataIndex: 'riskLevel'
  },
  {
    title: '倒伏风险得分，0-1之间的小数',
    align: "center",
    dataIndex: 'riskScore'
  },
  {
    title: '建议措施，如"及时排水、加固植株"',
    align: "center",
    dataIndex: 'suggestedMeasures'
  },
  {
    title: '预警状态',
    align: "center",
    dataIndex: 'warningStatus'
  },
];

// 高级查询数据
export const superQuerySchema = {
  plotId: {title: '地块ID，外键关联youcai_plots表',order: 0,view: 'number', type: 'number',},
  riskLevel: {title: '风险等级：低 / 中 / 高',order: 1,view: 'text', type: 'string',},
  riskScore: {title: '倒伏风险得分，0-1之间的小数',order: 2,view: 'number', type: 'number',},
  suggestedMeasures: {title: '建议措施，如"及时排水、加固植株"',order: 3,view: 'textarea', type: 'string',},
  warningStatus: {title: '预警状态',order: 4,view: 'text', type: 'string',},
};
