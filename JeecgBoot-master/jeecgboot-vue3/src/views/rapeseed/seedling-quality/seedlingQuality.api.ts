import { defHttp } from '/@/utils/http/axios';
import { AxiosRequestConfig } from 'axios';

// 枚举API地址
enum Api {
  // 列表
  List = '/rapeseed/seedlingQuality/list',
  // 保存
  Save = '/rapeseed/seedlingQuality/save',
  // 编辑
  Edit = '/rapeseed/seedlingQuality/edit',
  // 删除
  Delete = '/rapeseed/seedlingQuality/delete',
  // 批量删除
  BatchDelete = '/rapeseed/seedlingQuality/batchDelete',
  // 详情
  Detail = '/rapeseed/seedlingQuality/queryById',
  // 导出
  Export = '/rapeseed/seedlingQuality/export',
  // 导入
  Import = '/rapeseed/seedlingQuality/importExcel',
}

// 获取油菜苗素质列表
export const getSeedlingQualityList = (params?: AxiosRequestConfig) => {
  return defHttp.get({ url: Api.List, params });
};

// 保存油菜苗素质
export const saveSeedlingQuality = (params: any) => {
  return defHttp.post({ url: Api.Save, params });
};

// 编辑油菜苗素质
export const editSeedlingQuality = (params: any) => {
  return defHttp.put({ url: Api.Edit, params });
};

// 删除油菜苗素质
export const deleteSeedlingQuality = (id: string, params?: AxiosRequestConfig) => {
  return defHttp.delete({ url: `${Api.Delete}/${id}`, params });
};

// 批量删除油菜苗素质
export const batchDeleteSeedlingQuality = (ids: string[], params?: AxiosRequestConfig) => {
  return defHttp.delete({ url: Api.BatchDelete, data: { ids } });
};

// 获取油菜苗素质详情
export const getSeedlingQualityById = (id: string) => {
  return defHttp.get({ url: `${Api.Detail}/${id}` });
};

// 导出油菜苗素质
export const exportSeedlingQuality = (params?: any) => {
  return defHttp.post({ url: Api.Export, params }, { isTransformResponse: false });
};

// 导入油菜苗素质
export const importSeedlingQuality = (params: any) => {
  return defHttp.uploadFile({ url: Api.Import }, params );
};