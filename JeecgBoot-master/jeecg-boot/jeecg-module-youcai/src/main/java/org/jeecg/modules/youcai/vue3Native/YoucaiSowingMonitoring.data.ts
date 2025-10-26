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
    title: '生产计划ID',
    align: "center",
    dataIndex: 'planId'
  },
  {
    title: '播种日期',
    align: "center",
    dataIndex: 'sowingDate',
    customRender:({text}) =>{
      text = !text ? "" : (text.length > 10 ? text.substr(0,10) : text);
      return text;
    },
  },
  {
    title: '播种方式',
    align: "center",
    dataIndex: 'sowingMethod'
  },
  {
    title: '播种量(公斤/亩)',
    align: "center",
    dataIndex: 'seedingRate'
  },
  {
    title: '实际播种面积(亩)',
    align: "center",
    dataIndex: 'actualSowingArea'
  },
  {
    title: '播种状态',
    align: "center",
    dataIndex: 'sowingStatus'
  },
];

// 高级查询数据
export const superQuerySchema = {
  plotId: {title: '地块ID',order: 0,view: 'number', type: 'number',},
  planId: {title: '生产计划ID',order: 1,view: 'number', type: 'number',},
  sowingDate: {title: '播种日期',order: 2,view: 'date', type: 'string',},
  sowingMethod: {title: '播种方式',order: 3,view: 'text', type: 'string',},
  seedingRate: {title: '播种量(公斤/亩)',order: 4,view: 'number', type: 'number',},
  actualSowingArea: {title: '实际播种面积(亩)',order: 5,view: 'number', type: 'number',},
  sowingStatus: {title: '播种状态',order: 6,view: 'text', type: 'string',},
};
