import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '年份（如2022）',
    align: "center",
    dataIndex: 'year'
  },
  {
    title: '单产（kg/亩）',
    align: "center",
    dataIndex: 'yield'
  },
  {
    title: '品种ID（关联品种表）',
    align: "center",
    dataIndex: 'varietyId'
  },
  {
    title: '地块名称',
    align: "center",
    dataIndex: 'plot'
  },
];

// 高级查询数据
export const superQuerySchema = {
  year: {title: '年份（如2022）',order: 0,view: 'number', type: 'number',},
  yield: {title: '单产（kg/亩）',order: 1,view: 'number', type: 'number',},
  varietyId: {title: '品种ID（关联品种表）',order: 2,view: 'number', type: 'number',},
  plot: {title: '地块名称',order: 3,view: 'text', type: 'string',},
};
