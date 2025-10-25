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
    title: '预警日期',
    align: "center",
    dataIndex: 'warningDate',
    customRender:({text}) =>{
      text = !text ? "" : (text.length > 10 ? text.substr(0,10) : text);
      return text;
    },
  },
  {
    title: '病害类型',
    align: "center",
    dataIndex: 'diseaseType'
  },
  {
    title: '病害名称',
    align: "center",
    dataIndex: 'diseaseName'
  },
  {
    title: '严重程度',
    align: "center",
    dataIndex: 'severity'
  },
  {
    title: '症状描述',
    align: "center",
    dataIndex: 'symptoms'
  },
  {
    title: '防治建议',
    align: "center",
    dataIndex: 'recommendation'
  },
  {
    title: '预警状态',
    align: "center",
    dataIndex: 'warningStatus'
  },
  {
    title: '病害影像',
    align: "center",
    dataIndex: 'imageUrl'
  },
];

// 高级查询数据
export const superQuerySchema = {
  plotId: {title: '地块ID',order: 0,view: 'number', type: 'number',},
  warningDate: {title: '预警日期',order: 1,view: 'date', type: 'string',},
  diseaseType: {title: '病害类型',order: 2,view: 'text', type: 'string',},
  diseaseName: {title: '病害名称',order: 3,view: 'text', type: 'string',},
  severity: {title: '严重程度',order: 4,view: 'text', type: 'string',},
  symptoms: {title: '症状描述',order: 5,view: 'textarea', type: 'string',},
  recommendation: {title: '防治建议',order: 6,view: 'textarea', type: 'string',},
  warningStatus: {title: '预警状态',order: 7,view: 'text', type: 'string',},
  imageUrl: {title: '病害影像',order: 8,view: 'text', type: 'string',},
};
