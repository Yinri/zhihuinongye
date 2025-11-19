import { defHttp } from '/@/utils/http/axios';
import { AxiosRequestConfig } from 'axios';

enum Api {
  // 农事记录列表
  GetFarmingRecordsList = '/youcai/farmingRecords/queryByBaseId',
  // 保存农事记录
  SaveFarmingRecords = '/youcai/farmingRecords/add',
  // 编辑农事记录
  EditFarmingRecords = '/youcai/farmingRecords/edit',
  // 删除农事记录
  DeleteFarmingRecords = '/youcai/farmingRecords/delete',
  // 批量删除农事记录
  BatchDeleteFarmingRecords = '/youcai/farmingRecords/deleteBatch',
  // 根据ID查询农事记录
  GetFarmingRecordsById = '/youcai/farmingRecords/queryById',
  // 导出农事记录
  ExportFarmingRecords = '/youcai/farmingRecords/exportXls',
  // 导入农事记录
  ImportFarmingRecords = '/youcai/farmingRecords/importExcel',  
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
  return defHttp.delete<any>({ url: Api.BatchDeleteFarmingRecords, params }, { joinParamsToUrl: true });
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