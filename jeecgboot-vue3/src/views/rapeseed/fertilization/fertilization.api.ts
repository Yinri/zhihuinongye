import { defHttp } from '/@/utils/http/axios';
import { AxiosRequestConfig } from 'axios';

// 枚举API地址
enum Api {
  // 列表
  List = '/rapeseed/fertilization/list',
  // 保存
  Save = '/rapeseed/fertilization/save',
  // 编辑
  Edit = '/rapeseed/fertilization/edit',
  // 删除
  Delete = '/rapeseed/fertilization/delete',
  // 批量删除
  BatchDelete = '/rapeseed/fertilization/batchDelete',
  // 详情
  Detail = '/rapeseed/fertilization/queryById',
  // 导出
  Export = '/rapeseed/fertilization/export',
  // 导入
  Import = '/rapeseed/fertilization/importExcel',
}

// 获取施肥管理列表
export const getFertilizationList = (params?: AxiosRequestConfig) => {
  return defHttp.get({ url: Api.List, params });
};

// 保存施肥管理
export const saveFertilization = (params: any) => {
  return defHttp.post({ url: Api.Save, params });
};

// 编辑施肥管理
export const editFertilization = (params: any) => {
  return defHttp.put({ url: Api.Edit, params });
};

// 删除施肥管理
export const deleteFertilization = (id: string, params?: AxiosRequestConfig) => {
  return defHttp.delete({ url: `${Api.Delete}/${id}`, params });
};

// 批量删除施肥管理
export const batchDeleteFertilization = (ids: string[], params?: AxiosRequestConfig) => {
  return defHttp.delete({ url: Api.BatchDelete, data: { ids } });
};

// 获取施肥管理详情
export const getFertilizationById = (id: string) => {
  return defHttp.get({ url: `${Api.Detail}/${id}` });
};

// 导出施肥管理
export const exportFertilization = (params?: any) => {
  return defHttp.post({ url: Api.Export, params }, { isTransformResponse: false });
};

// 导入施肥管理
export const importFertilization = (params: any) => {
  return defHttp.uploadFile({ url: Api.Import }, params );
};