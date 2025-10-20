import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '地块ID',
    align: "center",
    dataIndex: 'plotId'
  },
  {
    title: '计划年份',
    align: "center",
    dataIndex: 'planYear'
  },
  {
    title: '油菜品种ID',
    align: "center",
    dataIndex: 'varietyId'
  },
  {
    title: '目标产量(公斤/亩)',
    align: "center",
    dataIndex: 'targetYield'
  },
  {
    title: '种植面积(亩)',
    align: "center",
    dataIndex: 'plantingArea'
  },
  {
    title: '计划播种日期',
    align: "center",
    dataIndex: 'plannedSowingDate',
    customRender:({text}) =>{
      text = !text ? "" : (text.length > 10 ? text.substr(0,10) : text);
      return text;
    },
  },
  {
    title: '计划收获日期',
    align: "center",
    dataIndex: 'plannedHarvestDate',
    customRender:({text}) =>{
      text = !text ? "" : (text.length > 10 ? text.substr(0,10) : text);
      return text;
    },
  },
  {
    title: '计划状态',
    align: "center",
    dataIndex: 'status'
  },
];

// 高级查询数据
export const superQuerySchema = {
  plotId: {title: '地块ID',order: 0,view: 'number', type: 'number',},
  planYear: {title: '计划年份',order: 1,view: 'text', type: 'string',},
  varietyId: {title: '油菜品种ID',order: 2,view: 'number', type: 'number',},
  targetYield: {title: '目标产量(公斤/亩)',order: 3,view: 'number', type: 'number',},
  plantingArea: {title: '种植面积(亩)',order: 4,view: 'number', type: 'number',},
  plannedSowingDate: {title: '计划播种日期',order: 5,view: 'date', type: 'string',},
  plannedHarvestDate: {title: '计划收获日期',order: 6,view: 'date', type: 'string',},
  status: {title: '计划状态',order: 7,view: 'text', type: 'string',},
};
