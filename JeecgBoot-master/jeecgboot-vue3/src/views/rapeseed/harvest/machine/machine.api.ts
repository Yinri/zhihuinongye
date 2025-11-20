import { defHttp } from '/@/utils/http/axios';
import { AxiosRequestConfig } from 'axios';

enum Api {
  List = '/youcai/harvesterMachine/list',
  Save = '/youcai/harvesterMachine/save',
  Edit = '/youcai/harvesterMachine/edit',
  Delete = '/youcai/harvesterMachine/delete',
  DeleteBatch = '/youcai/harvesterMachine/deleteBatch',
  QueryById = '/youcai/harvesterMachine/queryById',
  Export = '/youcai/harvesterMachine/export',
  Import = '/youcai/harvesterMachine/import',
}

export const getMachineList = (params?: any) => {
  return defHttp.get<any>({ url: Api.List, params });
};

export const saveMachine = (params?: any) => {
  return defHttp.post<any>({ url: Api.Save, params });
};

export const editMachine = (params?: any) => {
  return defHttp.put<any>({ url: Api.Edit, params });
};

export const deleteMachine = (params?: any) => {
  return defHttp.delete<any>({ url: Api.Delete, params }, { joinParamsToUrl: true });
};

export const deleteMachineBatch = (params?: any) => {
  return defHttp.delete<any>({ url: Api.DeleteBatch, params }, { joinParamsToUrl: true });
};

export const getMachineById = (params?: any) => {
  return defHttp.get<any>({ url: Api.QueryById, params });
};

export const exportMachine = (params?: any) => {
  return defHttp.download<any>({ url: Api.Export, params }, '农机档案.xlsx');
};

export const importMachine = (params?: any, config?: AxiosRequestConfig) => {
  return defHttp.uploadFile<any>({ url: Api.Import }, params, config);
};