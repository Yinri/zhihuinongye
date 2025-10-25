import { defHttp } from '/@/utils/http/axios';
import { AxiosRequestConfig } from 'axios';

enum Api {
  GetFarmingRecordsList = '/rapeseed/farming-records/list',
  SaveFarmingRecords = '/rapeseed/farming-records/save',
  EditFarmingRecords = '/rapeseed/farming-records/edit',
  DeleteFarmingRecords = '/rapeseed/farming-records/delete',
  DeleteBatch = '/rapeseed/farming-records/deleteBatch',
  GetFarmingRecordsById = '/rapeseed/farming-records/queryById',
  ExportFarmingRecords = '/rapeseed/farming-records/export',
  ImportFarmingRecords = '/rapeseed/farming-records/import',
}

/**
 * 获取农事记录列表
 * @param params 查询参数
 */
export const getFarmingRecordsList = (params?: any) => {
  return defHttp.get<any>({ url: Api.GetFarmingRecordsList, params });
};

/**
 * 保存农事记录
 * @param params 参数
 */
export const saveFarmingRecords = (params?: any) => {
  return defHttp.post<any>({ url: Api.SaveFarmingRecords, params });
};

/**
 * 编辑农事记录
 * @param params 参数
 */
export const editFarmingRecords = (params?: any) => {
  return defHttp.put<any>({ url: Api.EditFarmingRecords, params });
};

/**
 * 删除农事记录
 * @param params 参数
 */
export const deleteFarmingRecords = (params?: any) => {
  return defHttp.delete<any>({ url: Api.DeleteFarmingRecords, params }, { joinParamsToUrl: true });
};

/**
 * 批量删除农事记录
 * @param params 参数
 */
export const deleteBatch = (params?: any) => {
  return defHttp.delete<any>({ url: Api.DeleteBatch, params }, { joinParamsToUrl: true });
};

/**
 * 根据ID获取农事记录详情
 * @param params 参数
 */
export const getFarmingRecordsById = (params?: any) => {
  return defHttp.get<any>({ url: Api.GetFarmingRecordsById, params });
};

/**
 * 导出农事记录
 * @param params 参数
 */
export const exportFarmingRecords = (params?: any) => {
  return defHttp.download<any>(
    {
      url: Api.ExportFarmingRecords,
      params,
    },
    '农事记录.xlsx'
  );
};

/**
 * 导入农事记录
 * @param params 参数
 */
export const importFarmingRecords = (params?: any, config?: AxiosRequestConfig) => {
  return defHttp.uploadFile<any>(
    {
      url: Api.ImportFarmingRecords,
    },
    params,
    config
  );
};