import { defHttp } from '/@/utils/http/axios';
import { AxiosRequestConfig } from 'axios';

enum Api {
  GetHarvestList = '/youcai/harvest/list',
  SaveHarvest = '/youcai/harvest/save',
  EditHarvest = '/youcai/harvest/edit',
  DeleteHarvest = '/youcai/harvest/delete',
  DeleteBatch = '/youcai/harvest/deleteBatch',
  GetHarvestById = '/youcai/harvest/queryById',
  ExportHarvest = '/youcai/harvest/export',
  ImportHarvest = '/youcai/harvest/import',
  GetHarvestStats = '/youcai/harvest/stats',
  GetHarvesterStatus = '/youcai/harvest/harvesterStatus',
  GetYieldChart = '/youcai/harvest/yieldChart',
  GetPlotSummary = '/youcai/harvest/plotSummary',
}

/**
 * 获取收获管理列表
 * @param params 查询参数
 */
export const getHarvestList = (params?: any) => {
  return defHttp.get<any>({ url: Api.GetHarvestList, params });
};

/**
 * 保存收获管理
 * @param params 参数
 */
export const saveHarvest = (params?: any) => {
  return defHttp.post<any>({ url: Api.SaveHarvest, params });
};

/**
 * 编辑收获管理
 * @param params 参数
 */
export const editHarvest = (params?: any) => {
  return defHttp.put<any>({ url: Api.EditHarvest, params });
};

/**
 * 删除收获管理
 * @param params 参数
 */
export const deleteHarvest = (params?: any) => {
  return defHttp.delete<any>({ url: Api.DeleteHarvest, params }, { joinParamsToUrl: true });
};

/**
 * 批量删除收获管理
 * @param params 参数
 */
export const deleteBatch = (params?: any) => {
  return defHttp.delete<any>({ url: Api.DeleteBatch, params }, { joinParamsToUrl: true });
};

/**
 * 根据ID获取收获管理详情
 * @param params 参数
 */
export const getHarvestById = (params?: any) => {
  return defHttp.get<any>({ url: Api.GetHarvestById, params });
};

/**
 * 导出收获管理
 * @param params 参数
 */
export const exportHarvest = (params?: any) => {
  return defHttp.download<any>(
    {
      url: Api.ExportHarvest,
      params,
    },
    '收获管理.xlsx'
  );
};

/**
 * 导入收获管理
 * @param params 参数
 */
export const importHarvest = (params?: any, config?: AxiosRequestConfig) => {
  return defHttp.uploadFile<any>(
    {
      url: Api.ImportHarvest,
    },
    params,
    config
  );
};

/**
 * 收获统计数据（卡片）
 */
export const getHarvestStats = (params?: any) => {
  return defHttp.get<any>({ url: Api.GetHarvestStats, params });
};

/**
 * 农机状态列表
 */
export const getHarvesterStatus = (params?: any) => {
  return defHttp.get<any>({ url: Api.GetHarvesterStatus, params });
};

/**
 * 今日产量对比图
 */
export const getYieldChartData = (params?: any) => {
  return defHttp.get<any>({ url: Api.GetYieldChart, params });
};

/**
 * 地块收割派生视图（用于地图着色）
 */
export const getPlotHarvestSummary = (params?: any) => {
  return defHttp.get<any>({ url: Api.GetPlotSummary, params });
};