import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '品种名称',
    align: "center",
    dataIndex: 'varietyName'
  },
  {
    title: '品种特性',
    align: "center",
    dataIndex: 'characteristics'
  },
  {
    title: '生长周期(天)',
    align: "center",
    dataIndex: 'growthCycle'
  },
  {
    title: '产量潜力(公斤/亩)',
    align: "center",
    dataIndex: 'yieldPotential'
  },
  {
    title: '抗病性',
    align: "center",
    dataIndex: 'diseaseResistance'
  },
];

// 高级查询数据
export const superQuerySchema = {
  varietyName: {title: '品种名称',order: 0,view: 'text', type: 'string',},
  characteristics: {title: '品种特性',order: 1,view: 'textarea', type: 'string',},
  growthCycle: {title: '生长周期(天)',order: 2,view: 'number', type: 'number',},
  yieldPotential: {title: '产量潜力(公斤/亩)',order: 3,view: 'number', type: 'number',},
  diseaseResistance: {title: '抗病性',order: 4,view: 'text', type: 'string',},
};
