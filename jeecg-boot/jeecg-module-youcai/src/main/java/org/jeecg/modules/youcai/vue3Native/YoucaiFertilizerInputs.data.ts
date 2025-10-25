import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '生产计划ID',
    align: "center",
    dataIndex: 'planId'
  },
  {
    title: '肥料类型',
    align: "center",
    dataIndex: 'fertilizerType'
  },
  {
    title: '肥料名称',
    align: "center",
    dataIndex: 'fertilizerName'
  },
  {
    title: 'NPK比例',
    align: "center",
    dataIndex: 'npkRatio'
  },
  {
    title: '施肥量(公斤)',
    align: "center",
    dataIndex: 'amount'
  },
  {
    title: '成本(元)',
    align: "center",
    dataIndex: 'cost'
  },
  {
    title: '施肥日期',
    align: "center",
    dataIndex: 'applicationDate',
    customRender:({text}) =>{
      text = !text ? "" : (text.length > 10 ? text.substr(0,10) : text);
      return text;
    },
  },
  {
    title: '施肥方式',
    align: "center",
    dataIndex: 'applicationMethod'
  },
];

// 高级查询数据
export const superQuerySchema = {
  planId: {title: '生产计划ID',order: 0,view: 'number', type: 'number',},
  fertilizerType: {title: '肥料类型',order: 1,view: 'text', type: 'string',},
  fertilizerName: {title: '肥料名称',order: 2,view: 'text', type: 'string',},
  npkRatio: {title: 'NPK比例',order: 3,view: 'text', type: 'string',},
  amount: {title: '施肥量(公斤)',order: 4,view: 'number', type: 'number',},
  cost: {title: '成本(元)',order: 5,view: 'number', type: 'number',},
  applicationDate: {title: '施肥日期',order: 6,view: 'date', type: 'string',},
  applicationMethod: {title: '施肥方式',order: 7,view: 'text', type: 'string',},
};
