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
    title: '农药名称',
    align: "center",
    dataIndex: 'pesticideName'
  },
  {
    title: '农药类型',
    align: "center",
    dataIndex: 'pesticideType'
  },
  {
    title: '防治对象',
    align: "center",
    dataIndex: 'targetPest'
  },
  {
    title: '使用量(公斤/升)',
    align: "center",
    dataIndex: 'amount'
  },
  {
    title: '成本(元)',
    align: "center",
    dataIndex: 'cost'
  },
  {
    title: '施药日期',
    align: "center",
    dataIndex: 'applicationDate',
    customRender:({text}) =>{
      text = !text ? "" : (text.length > 10 ? text.substr(0,10) : text);
      return text;
    },
  },
  {
    title: '施药方式',
    align: "center",
    dataIndex: 'applicationMethod'
  },
];

// 高级查询数据
export const superQuerySchema = {
  planId: {title: '生产计划ID',order: 0,view: 'number', type: 'number',},
  pesticideName: {title: '农药名称',order: 1,view: 'text', type: 'string',},
  pesticideType: {title: '农药类型',order: 2,view: 'text', type: 'string',},
  targetPest: {title: '防治对象',order: 3,view: 'text', type: 'string',},
  amount: {title: '使用量(公斤/升)',order: 4,view: 'number', type: 'number',},
  cost: {title: '成本(元)',order: 5,view: 'number', type: 'number',},
  applicationDate: {title: '施药日期',order: 6,view: 'date', type: 'string',},
  applicationMethod: {title: '施药方式',order: 7,view: 'text', type: 'string',},
};
