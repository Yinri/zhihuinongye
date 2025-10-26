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
    title: '检测日期',
    align: "center",
    dataIndex: 'testDate',
    customRender:({text}) =>{
      text = !text ? "" : (text.length > 10 ? text.substr(0,10) : text);
      return text;
    },
  },
  {
    title: 'pH值',
    align: "center",
    dataIndex: 'phValue'
  },
  {
    title: '有机质含量(g/kg)',
    align: "center",
    dataIndex: 'organicMatter'
  },
  {
    title: '氮含量(mg/kg)',
    align: "center",
    dataIndex: 'nitrogen'
  },
  {
    title: '磷含量(mg/kg)',
    align: "center",
    dataIndex: 'phosphorus'
  },
  {
    title: '钾含量(mg/kg)',
    align: "center",
    dataIndex: 'potassium'
  },
  {
    title: '肥力等级',
    align: "center",
    dataIndex: 'fertilityLevel'
  },
];

// 高级查询数据
export const superQuerySchema = {
  plotId: {title: '地块ID',order: 0,view: 'number', type: 'number',},
  testDate: {title: '检测日期',order: 1,view: 'date', type: 'string',},
  phValue: {title: 'pH值',order: 2,view: 'number', type: 'number',},
  organicMatter: {title: '有机质含量(g/kg)',order: 3,view: 'number', type: 'number',},
  nitrogen: {title: '氮含量(mg/kg)',order: 4,view: 'number', type: 'number',},
  phosphorus: {title: '磷含量(mg/kg)',order: 5,view: 'number', type: 'number',},
  potassium: {title: '钾含量(mg/kg)',order: 6,view: 'number', type: 'number',},
  fertilityLevel: {title: '肥力等级',order: 7,view: 'text', type: 'string',},
};
