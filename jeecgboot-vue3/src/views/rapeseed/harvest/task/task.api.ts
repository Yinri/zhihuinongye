import { defHttp } from '/@/utils/http/axios';
import { AxiosRequestConfig } from 'axios';

enum Api {
  List = '/youcai/harvestTask/list',
  Save = '/youcai/harvestTask/save',
  Edit = '/youcai/harvestTask/edit',
  Delete = '/youcai/harvestTask/delete',
  DeleteBatch = '/youcai/harvestTask/deleteBatch',
  QueryById = '/youcai/harvestTask/queryById',
  Export = '/youcai/harvestTask/export',
  Import = '/youcai/harvestTask/import',
}

export const getTaskList = (params?: any) => {
  return defHttp.get<any>({ url: Api.List, params });
};

export const saveTask = (params?: any) => {
  return defHttp.post<any>({ url: Api.Save, params });
};

export const editTask = (params?: any) => {
  return defHttp.put<any>({ url: Api.Edit, params });
};

export const deleteTask = (params?: any) => {
  return defHttp.delete<any>({ url: Api.Delete, params }, { joinParamsToUrl: true });
};

export const deleteTaskBatch = (params?: any) => {
  return defHttp.delete<any>({ url: Api.DeleteBatch, params }, { joinParamsToUrl: true });
};

export const getTaskById = (params?: any) => {
  return defHttp.get<any>({ url: Api.QueryById, params });
};

export const exportTask = (params?: any) => {
  return defHttp.download<any>({ url: Api.Export, params }, '收获任务.xlsx');
};

export const importTask = (params?: any, config?: AxiosRequestConfig) => {
  return defHttp.uploadFile<any>({ url: Api.Import }, params, config);
};