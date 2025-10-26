import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '基地名称',
    align: "center",
    dataIndex: 'baseName'
  },
  {
    title: '负责人',
    align: "center",
    dataIndex: 'manager'
  },
  {
    title: '联系电话',
    align: "center",
    dataIndex: 'phone'
  },
  {
    title: '基地地址（具体地址）',
    align: "center",
    dataIndex: 'address'
  },
];

// 高级查询数据
export const superQuerySchema = {
  baseName: {title: '基地名称',order: 0,view: 'text', type: 'string',},
  manager: {title: '负责人',order: 1,view: 'text', type: 'string',},
  phone: {title: '联系电话',order: 2,view: 'text', type: 'string',},
  address: {title: '基地地址（具体地址）',order: 3,view: 'text', type: 'string',},
};
