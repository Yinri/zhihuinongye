import { defHttp } from '/@/utils/http/axios';
import { AxiosRequestConfig } from 'axios';

// 枚举API地址
enum Api {
  // 列表
  List = '/rapeseed/workArea/list',
  // 保存
  Save = '/rapeseed/workArea/save',
  // 编辑
  Edit = '/rapeseed/workArea/edit',
  // 删除
  Delete = '/rapeseed/workArea/delete',
  // 批量删除
  BatchDelete = '/rapeseed/workArea/batchDelete',
  // 详情
  Detail = '/rapeseed/workArea/queryById',
  // 导出
  Export = '/rapeseed/workArea/export',
  // 导入
  Import = '/rapeseed/workArea/importExcel',
}

// 获取作业面积列表
export const getWorkAreaList = (params?: AxiosRequestConfig) => {
  return defHttp.get({ url: Api.List, params });
};

// 保存作业面积
export const saveWorkArea = (params: any) => {
  return defHttp.post({ url: Api.Save, params });
};

// 编辑作业面积
export const editWorkArea = (params: any) => {
  return defHttp.put({ url: Api.Edit, params });
};

// 删除作业面积
export const deleteWorkArea = (id: string, params?: AxiosRequestConfig) => {
  return defHttp.delete({ url: `${Api.Delete}/${id}`, params });
};

// 批量删除作业面积
export const batchDeleteWorkArea = (ids: string[], params?: AxiosRequestConfig) => {
  return defHttp.delete({ url: Api.BatchDelete, data: { ids } });
};

// 获取作业面积详情
export const getWorkAreaById = (id: string) => {
  return defHttp.get({ url: `${Api.Detail}/${id}` });
};

// 导出作业面积
export const exportWorkArea = (params?: any) => {
  return defHttp.post({ url: Api.Export, params }, { isTransformResponse: false });
};

// 导入作业面积
export const importWorkArea = (params: any) => {
  return defHttp.uploadFile({ url: Api.Import }, params);
};