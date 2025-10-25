import { defHttp } from '/@/utils/http/axios';
import { AxiosRequestConfig } from 'axios';

// 枚举API地址
enum Api {
  // 列表
  List = '/rapeseed/diseaseControl/list',
  // 保存
  Save = '/rapeseed/diseaseControl/save',
  // 编辑
  Edit = '/rapeseed/diseaseControl/edit',
  // 删除
  Delete = '/rapeseed/diseaseControl/delete',
  // 批量删除
  BatchDelete = '/rapeseed/diseaseControl/batchDelete',
  // 详情
  Detail = '/rapeseed/diseaseControl/queryById',
  // 导出
  Export = '/rapeseed/diseaseControl/export',
  // 导入
  Import = '/rapeseed/diseaseControl/importExcel',
}

// 获取病害防控列表
export const getDiseaseControlList = (params?: AxiosRequestConfig) => {
  return defHttp.get({ url: Api.List, params });
};

// 保存病害防控
export const saveDiseaseControl = (params: any) => {
  return defHttp.post({ url: Api.Save, params });
};

// 编辑病害防控
export const editDiseaseControl = (params: any) => {
  return defHttp.put({ url: Api.Edit, params });
};

// 删除病害防控
export const deleteDiseaseControl = (id: string, params?: AxiosRequestConfig) => {
  return defHttp.delete({ url: `${Api.Delete}/${id}`, params });
};

// 批量删除病害防控
export const batchDeleteDiseaseControl = (ids: string[], params?: AxiosRequestConfig) => {
  return defHttp.delete({ url: Api.BatchDelete, data: { ids } });
};

// 获取病害防控详情
export const getDiseaseControlById = (id: string) => {
  return defHttp.get({ url: `${Api.Detail}/${id}` });
};

// 导出病害防控
export const exportDiseaseControl = (params?: any) => {
  return defHttp.post({ url: Api.Export, params }, { isTransformResponse: false });
};

// 导入病害防控
export const importDiseaseControl = (params: any) => {
  return defHttp.uploadFile({ url: Api.Import }, params );
};