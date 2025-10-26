import { defHttp } from '/@/utils/http/axios';
import { AxiosRequestConfig } from 'axios';

enum Api {
  GetHarvestList = '/rapeseed/harvest/list',
  SaveHarvest = '/rapeseed/harvest/save',
  EditHarvest = '/rapeseed/harvest/edit',
  DeleteHarvest = '/rapeseed/harvest/delete',
  DeleteBatch = '/rapeseed/harvest/deleteBatch',
  GetHarvestById = '/rapeseed/harvest/queryById',
  ExportHarvest = '/rapeseed/harvest/export',
  ImportHarvest = '/rapeseed/harvest/import',
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