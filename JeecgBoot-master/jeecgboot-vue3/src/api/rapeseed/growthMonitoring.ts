import { defHttp } from '/@/utils/http/axios';
import { BasePageParams, BaseListResponse } from '/@/api/model/baseModel';

// 生长监测数据接口
export interface GrowthMonitoringData {
  id?: string;
  baseId?: string | number;
  monitoringDate: string;
  growthStage: string;
  plantHeight?: number;
  stemDiameter?: number;
  density?: number;
  healthStatus?: string;
  notes?: string;
}

// 根据基地ID获取最新生长监测数据
export const getLatestGrowthMonitoringByBaseId = (baseId: string | number) => {
  return defHttp.get({
      url: `/youcai/youcaiGrowthMonitoring/queryByBaseId`,
      params: { baseId }
    });
};

// 获取生长监测数据列表（分页）
export const getGrowthMonitoringList = (params: BasePageParams & { 
  baseId?: string | number;
  growthStage?: string;
  monitoringDateRange?: [string, string];
}) => {
  return defHttp.get<BaseListResponse<GrowthMonitoringData>>({
    url: '/youcai/youcaiGrowthMonitoring/list',
    params,
  });
};

// 添加生长监测数据
export const addGrowthMonitoring = (data: GrowthMonitoringData) => {
  return defHttp.post<string>({
    url: '/youcai/youcaiGrowthMonitoring/add',
    data,
  });
};

// 编辑生长监测数据
export const editGrowthMonitoring = (data: GrowthMonitoringData) => {
  return defHttp.put<string>({
    url: '/youcai/youcaiGrowthMonitoring/edit',
    data,
  });
};

// 删除生长监测数据
export const deleteGrowthMonitoring = (id: string) => {
  return defHttp.delete<string>({
    url: `/youcai/youcaiGrowthMonitoring/delete?id=${id}`,
  });
};

// 批量删除生长监测数据
export const batchDeleteGrowthMonitoring = (ids: string) => {
  return defHttp.delete<string>({
    url: `/youcai/youcaiGrowthMonitoring/deleteBatch?ids=${ids}`,
  });
};
