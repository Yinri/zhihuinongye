import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '品种ID（关联作物品种表主键）',
    align: "center",
    dataIndex: 'varietyId'
  },
  {
    title: '地块ID（关联地块表主键）',
    align: "center",
    dataIndex: 'plotId'
  },
  {
    title: '肥料ID（关联肥料表主键）',
    align: "center",
    dataIndex: 'fertilizerId'
  },
  {
    title: '肥料类型（冗余字段：如尿素、磷酸二铵）',
    align: "center",
    dataIndex: 'fertilizerType'
  },
  {
    title: '氮肥利用率(%)',
    align: "center",
    dataIndex: 'nRate'
  },
  {
    title: '磷肥利用率(%)',
    align: "center",
    dataIndex: 'pRate'
  },
  {
    title: '钾肥利用率(%)',
    align: "center",
    dataIndex: 'kRate'
  },
];

// 高级查询数据
export const superQuerySchema = {
  varietyId: {title: '品种ID（关联作物品种表主键）',order: 0,view: 'text', type: 'string',},
  plotId: {title: '地块ID（关联地块表主键）',order: 1,view: 'text', type: 'string',},
  fertilizerId: {title: '肥料ID（关联肥料表主键）',order: 2,view: 'text', type: 'string',},
  fertilizerType: {title: '肥料类型（冗余字段：如尿素、磷酸二铵）',order: 3,view: 'text', type: 'string',},
  nRate: {title: '氮肥利用率(%)',order: 4,view: 'number', type: 'number',},
  pRate: {title: '磷肥利用率(%)',order: 5,view: 'number', type: 'number',},
  kRate: {title: '钾肥利用率(%)',order: 6,view: 'number', type: 'number',},
};
