import { defHttp } from '/@/utils/http/axios';
import { AxiosRequestConfig } from 'axios';

// 枚举API地址
enum Api {
  // 列表
  List = '/rapeseed/varietyInfo/list',
  // 保存
  Save = '/rapeseed/varietyInfo/save',
  // 编辑
  Edit = '/rapeseed/varietyInfo/edit',
  // 删除
  Delete = '/rapeseed/varietyInfo/delete',
  // 批量删除
  BatchDelete = '/rapeseed/varietyInfo/batchDelete',
  // 详情
  Detail = '/rapeseed/varietyInfo/queryById',
  // 导出
  Export = '/rapeseed/varietyInfo/export',
  // 导入
  Import = '/rapeseed/varietyInfo/importExcel',
}

// 获取品种信息列表
export const getVarietyInfoList = (params?: AxiosRequestConfig) => {
  return defHttp.get({ url: Api.List, params });
};

// 保存品种信息
export const saveVarietyInfo = (params: any) => {
  return defHttp.post({ url: Api.Save, params });
};

// 编辑品种信息
export const editVarietyInfo = (params: any) => {
  return defHttp.put({ url: Api.Edit, params });
};

// 删除品种信息
export const deleteVarietyInfo = (id: string, params?: AxiosRequestConfig) => {
  return defHttp.delete({ url: `${Api.Delete}/${id}`, params });
};

// 批量删除品种信息
export const batchDeleteVarietyInfo = (ids: string[], params?: AxiosRequestConfig) => {
  return defHttp.delete({ url: Api.BatchDelete, data: { ids } });
};

// 获取品种信息详情
export const getVarietyInfoById = (id: string) => {
  return defHttp.get({ url: `${Api.Detail}/${id}` });
};

// 导出品种信息
export const exportVarietyInfo = (params?: any) => {
  return defHttp.post({ url: Api.Export, params }, { isTransformResponse: false });
};

// 导入品种信息
export const importVarietyInfo = (params: any) => {
  return defHttp.uploadFile({ url: Api.Import }, params );
};

// 获取导入URL
export const getImportUrl = () => {
  return Api.Import;
};

// 获取导出URL
export const getExportUrl = () => {
  return Api.Export;
};
