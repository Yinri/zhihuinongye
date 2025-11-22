import { defHttp } from '/@/utils/http/axios';
import { AxiosRequestConfig } from 'axios';

enum Api {
  List = '/youcai/bases/list',
  Add = '/youcai/bases/add',
  Edit = '/youcai/bases/edit',
  Delete = '/youcai/bases/delete',
  BatchDelete = '/youcai/bases/deleteBatch',
  Detail = '/youcai/bases/queryById',
  Export = '/youcai/bases/exportXls',
  Import = '/youcai/bases/importExcel',
}

export const getBaseInfoList = (params?: AxiosRequestConfig) => {
  return defHttp.get({ url: Api.List, params });
};

export const saveBaseInfo = (params: any) => {
  return defHttp.post({ url: Api.Add, params });
};

export const editBaseInfo = (params: any) => {
  return defHttp.put({ url: Api.Edit, params });
};

export const deleteBaseInfo = (id: string, params?: AxiosRequestConfig) => {
  return defHttp.delete({ url: `${Api.Delete}/${id}`, params });
};

export const batchDeleteBaseInfo = (ids: string[], params?: AxiosRequestConfig) => {
  return defHttp.delete({ url: Api.BatchDelete, params: { ids: ids.join(',') } });
};

export const getBaseInfoById = (id: string) => {
  return defHttp.get({ url: `${Api.Detail}/${id}` });
};

export const exportBaseInfo = (params?: any) => {
  return defHttp.get({ url: Api.Export, params }, { isTransformResponse: false });
};

export const importBaseInfo = (params: any) => {
  return defHttp.uploadFile({ url: Api.Import, params });
};

export const getImportUrl = () => {
  return Api.Import;
};

export const getExportUrl = () => {
  return Api.Export;
};

