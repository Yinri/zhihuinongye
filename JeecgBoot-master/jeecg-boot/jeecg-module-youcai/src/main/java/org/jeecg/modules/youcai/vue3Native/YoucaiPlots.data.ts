import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '地块名称',
    align: "center",
    dataIndex: 'plotName'
  },
  {
    title: '所属基地ID',
    align: "center",
    dataIndex: 'baseId'
  },
  {
    title: '地块面积(亩)',
    align: "center",
    dataIndex: 'area'
  },
  {
    title: '纬度',
    align: "center",
    dataIndex: 'latitude'
  },
  {
    title: '经度',
    align: "center",
    dataIndex: 'longitude'
  },
];

// 高级查询数据
export const superQuerySchema = {
  plotName: {title: '地块名称',order: 0,view: 'text', type: 'string',},
  baseId: {title: '所属基地ID',order: 1,view: 'number', type: 'number',},
  area: {title: '地块面积(亩)',order: 2,view: 'number', type: 'number',},
  latitude: {title: '纬度',order: 3,view: 'number', type: 'number',},
  longitude: {title: '经度',order: 4,view: 'number', type: 'number',},
};
