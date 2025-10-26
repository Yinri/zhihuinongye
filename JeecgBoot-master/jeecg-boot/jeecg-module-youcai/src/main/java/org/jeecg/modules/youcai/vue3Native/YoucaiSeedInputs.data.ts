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
    title: '品种ID',
    align: "center",
    dataIndex: 'varietyId'
  },
  {
    title: '种子用量(公斤)',
    align: "center",
    dataIndex: 'seedAmount'
  },
  {
    title: '种子成本(元)',
    align: "center",
    dataIndex: 'seedCost'
  },
  {
    title: '购买日期',
    align: "center",
    dataIndex: 'purchaseDate',
    customRender:({text}) =>{
      text = !text ? "" : (text.length > 10 ? text.substr(0,10) : text);
      return text;
    },
  },
  {
    title: '供应商',
    align: "center",
    dataIndex: 'supplier'
  },
];

// 高级查询数据
export const superQuerySchema = {
  planId: {title: '生产计划ID',order: 0,view: 'number', type: 'number',},
  varietyId: {title: '品种ID',order: 1,view: 'number', type: 'number',},
  seedAmount: {title: '种子用量(公斤)',order: 2,view: 'number', type: 'number',},
  seedCost: {title: '种子成本(元)',order: 3,view: 'number', type: 'number',},
  purchaseDate: {title: '购买日期',order: 4,view: 'date', type: 'string',},
  supplier: {title: '供应商',order: 5,view: 'text', type: 'string',},
};
