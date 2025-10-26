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
    title: '灌溉日期',
    align: "center",
    dataIndex: 'irrigationDate',
    customRender:({text}) =>{
      text = !text ? "" : (text.length > 10 ? text.substr(0,10) : text);
      return text;
    },
  },
  {
    title: '灌溉方式',
    align: "center",
    dataIndex: 'irrigationMethod'
  },
  {
    title: '灌溉量(立方米)',
    align: "center",
    dataIndex: 'waterAmount'
  },
  {
    title: '灌溉时长(分钟)',
    align: "center",
    dataIndex: 'irrigationDuration'
  },
];

// 高级查询数据
export const superQuerySchema = {
  plotId: {title: '地块ID',order: 0,view: 'number', type: 'number',},
  planId: {title: '生产计划ID',order: 1,view: 'number', type: 'number',},
  irrigationDate: {title: '灌溉日期',order: 2,view: 'date', type: 'string',},
  irrigationMethod: {title: '灌溉方式',order: 3,view: 'text', type: 'string',},
  waterAmount: {title: '灌溉量(立方米)',order: 4,view: 'number', type: 'number',},
  irrigationDuration: {title: '灌溉时长(分钟)',order: 5,view: 'number', type: 'number',},
};
