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
    title: '关联预警ID',
    align: "center",
    dataIndex: 'warningId'
  },
  {
    title: '虫害类型',
    align: "center",
    dataIndex: 'pestType'
  },
  {
    title: '虫害名称',
    align: "center",
    dataIndex: 'pestName'
  },
  {
    title: '防控日期',
    align: "center",
    dataIndex: 'controlDate',
    customRender:({text}) =>{
      text = !text ? "" : (text.length > 10 ? text.substr(0,10) : text);
      return text;
    },
  },
  {
    title: '防控方法',
    align: "center",
    dataIndex: 'controlMethod'
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
    title: '农药用量(毫升/亩)',
    align: "center",
    dataIndex: 'pesticideDosage'
  },
  {
    title: '农药浓度(%)',
    align: "center",
    dataIndex: 'pesticideConcentration'
  },
  {
    title: '施用方法',
    align: "center",
    dataIndex: 'applicationMethod'
  },
  {
    title: '防控面积(亩)',
    align: "center",
    dataIndex: 'controlArea'
  },
  {
    title: '防控成本(元)',
    align: "center",
    dataIndex: 'controlCost'
  },
  {
    title: '防控效果',
    align: "center",
    dataIndex: 'controlEffectiveness'
  },
  {
    title: '操作员',
    align: "center",
    dataIndex: 'operator'
  },
  {
    title: '备注',
    align: "center",
    dataIndex: 'notes'
  },
];

// 高级查询数据
export const superQuerySchema = {
  plotId: {title: '地块ID',order: 0,view: 'number', type: 'number',},
  planId: {title: '生产计划ID',order: 1,view: 'number', type: 'number',},
  warningId: {title: '关联预警ID',order: 2,view: 'number', type: 'number',},
  pestType: {title: '虫害类型',order: 3,view: 'text', type: 'string',},
  pestName: {title: '虫害名称',order: 4,view: 'text', type: 'string',},
  controlDate: {title: '防控日期',order: 5,view: 'date', type: 'string',},
  controlMethod: {title: '防控方法',order: 6,view: 'text', type: 'string',},
  pesticideName: {title: '农药名称',order: 7,view: 'text', type: 'string',},
  pesticideType: {title: '农药类型',order: 8,view: 'text', type: 'string',},
  pesticideDosage: {title: '农药用量(毫升/亩)',order: 9,view: 'number', type: 'number',},
  pesticideConcentration: {title: '农药浓度(%)',order: 10,view: 'number', type: 'number',},
  applicationMethod: {title: '施用方法',order: 11,view: 'text', type: 'string',},
  controlArea: {title: '防控面积(亩)',order: 12,view: 'number', type: 'number',},
  controlCost: {title: '防控成本(元)',order: 13,view: 'number', type: 'number',},
  controlEffectiveness: {title: '防控效果',order: 14,view: 'text', type: 'string',},
  operator: {title: '操作员',order: 15,view: 'text', type: 'string',},
  notes: {title: '备注',order: 16,view: 'textarea', type: 'string',},
};
