import { defHttp } from '/@/utils/http/axios';
import { AxiosRequestConfig } from 'axios';

enum Api {
  GetDataIntegrationList = '/rapeseed/data-integration/list',
  SaveDataIntegration = '/rapeseed/data-integration/save',
  EditDataIntegration = '/rapeseed/data-integration/edit',
  DeleteDataIntegration = '/rapeseed/data-integration/delete',
  DeleteBatch = '/rapeseed/data-integration/deleteBatch',
  GetDataIntegrationById = '/rapeseed/data-integration/queryById',
  ExportDataIntegration = '/rapeseed/data-integration/export',
  ImportDataIntegration = '/rapeseed/data-integration/import',
  GetDeviceStatus = '/youcai/sensorInfo/deviceStatus',
  GetAllDevices = '/youcai/sensorInfo/getAllDevices',
}

/**
 * 获取数据对接列表
 * @param params 查询参数
 */
export const getDataIntegrationList = (params?: any) => {
  return defHttp.get<any>({ url: Api.GetDataIntegrationList, params });
};

/**
 * 保存数据对接
 * @param params 参数
 */
export const saveDataIntegration = (params?: any) => {
  return defHttp.post<any>({ url: Api.SaveDataIntegration, params });
};

/**
 * 编辑数据对接
 * @param params 参数
 */
export const editDataIntegration = (params?: any) => {
  return defHttp.put<any>({ url: Api.EditDataIntegration, params });
};

/**
 * 删除数据对接
 * @param params 参数
 */
export const deleteDataIntegration = (params?: any) => {
  return defHttp.delete<any>({ url: Api.DeleteDataIntegration, params }, { joinParamsToUrl: true });
};

/**
 * 批量删除数据对接
 * @param params 参数
 */
export const deleteBatch = (params?: any) => {
  return defHttp.delete<any>({ url: Api.DeleteBatch, params }, { joinParamsToUrl: true });
};

/**
 * 根据ID获取数据对接详情
 * @param params 参数
 */
export const getDataIntegrationById = (params?: any) => {
  return defHttp.get<any>({ url: Api.GetDataIntegrationById, params });
};

/**
 * 导出数据对接
 * @param params 参数
 */
export const exportDataIntegration = (params?: any) => {
  return defHttp.download<any>(
    {
      url: Api.ExportDataIntegration,
      params,
    },
    '数据对接.xlsx'
  );
};

/**
 * 导入数据对接
 * @param params 参数
 */
export const importDataIntegration = (params?: any, config?: AxiosRequestConfig) => {
  return defHttp.uploadFile<any>(
    {
      url: Api.ImportDataIntegration,
    },
    params,
    config
  );
};

/**
 * 获取设备状态信息
 */
export const getDeviceStatus = () => {
  return defHttp.get<any>({ url: Api.GetDeviceStatus });
};

/**
 * 获取所有设备信息
 */
export const getAllDevices = (baseId: number) => {
  return defHttp.get<any>({ url: Api.GetAllDevices, params: { baseId } });
};