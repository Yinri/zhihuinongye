import { defHttp } from '/@/utils/http/axios';
import { AxiosRequestConfig } from 'axios';

// 枚举API地址
enum Api {
  // 列表
  List = '/rapeseed/insectControl/list',
  // 保存
  Save = '/rapeseed/insectControl/save',
  // 编辑
  Edit = '/rapeseed/insectControl/edit',
  // 删除
  Delete = '/rapeseed/insectControl/delete',
  // 批量删除
  BatchDelete = '/rapeseed/insectControl/batchDelete',
  // 详情
  Detail = '/rapeseed/insectControl/queryById',
  // 导出
  Export = '/rapeseed/insectControl/export',
  // 导入
  Import = '/rapeseed/insectControl/importExcel',
}

// 获取虫害防控列表
export const getInsectControlList = (params?: AxiosRequestConfig) => {
  return defHttp.get({ url: Api.List, params });
};

// 保存虫害防控
export const saveInsectControl = (params: any) => {
  return defHttp.post({ url: Api.Save, params });
};

// 编辑虫害防控
export const editInsectControl = (params: any) => {
  return defHttp.put({ url: Api.Edit, params });
};

// 删除虫害防控
export const deleteInsectControl = (id: string, params?: AxiosRequestConfig) => {
  return defHttp.delete({ url: `${Api.Delete}/${id}`, params });
};

// 批量删除虫害防控
export const batchDeleteInsectControl = (ids: string[], params?: AxiosRequestConfig) => {
  return defHttp.delete({ url: Api.BatchDelete, data: { ids } });
};

// 获取虫害防控详情
export const getInsectControlById = (id: string) => {
  return defHttp.get({ url: `${Api.Detail}/${id}` });
};

// 导出虫害防控
export const exportInsectControl = (params?: any) => {
  return defHttp.post({ url: Api.Export, params }, { isTransformResponse: false });
};

// 导入虫害防控
export const importInsectControl = (params: any) => {
  return defHttp.uploadFile({ url: Api.Import }, params );
};