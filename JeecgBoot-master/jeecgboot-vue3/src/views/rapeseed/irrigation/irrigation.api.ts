import { defHttp } from '/@/utils/http/axios';  
import { AxiosRequestConfig } from 'axios';

// 枚举API地址
enum Api {
  // 列表
  List = '/rapeseed/irrigation/list',
  // 保存
  Save = '/rapeseed/irrigation/save',
  // 编辑
  Edit = '/rapeseed/irrigation/edit',
  // 删除
  Delete = '/rapeseed/irrigation/delete',
  // 批量删除
  BatchDelete = '/rapeseed/irrigation/batchDelete',
  // 详情
  Detail = '/rapeseed/irrigation/queryById',
  // 导出
  Export = '/rapeseed/irrigation/export',
  // 导入
  Import = '/rapeseed/irrigation/importExcel',
  // 地块生长与土壤水分状态
  PlotStatus = '/rapeseed/irrigation/plotStatus',
  // Penman 预测与灌溉建议
  PenmanPredict = '/rapeseed/irrigation/penmanPredict',
  // 干预/不干预对比数据
  InterventionComparison = '/rapeseed/irrigation/interventionComparison',
}

// 获取智慧灌溉列表
export const getIrrigationList = (params?: AxiosRequestConfig) => {
  return defHttp.get({ url: Api.List, params });
};

// 保存智慧灌溉
export const saveIrrigation = (params: any) => {
  return defHttp.post({ url: Api.Save, params });
};

// 编辑智慧灌溉
export const editIrrigation = (params: any) => {
  return defHttp.put({ url: Api.Edit, params });
};

// 删除智慧灌溉
export const deleteIrrigation = (id: string, params?: AxiosRequestConfig) => {
  return defHttp.delete({ url: `${Api.Delete}/${id}`, params });
};

// 批量删除智慧灌溉
export const batchDeleteIrrigation = (ids: string[], params?: AxiosRequestConfig) => {
  return defHttp.delete({ url: Api.BatchDelete, data: { ids } });
};

// 获取智慧灌溉详情
export const getIrrigationById = (id: string) => {
  return defHttp.get({ url: `${Api.Detail}/${id}` });
};

// 导出智慧灌溉
export const exportIrrigation = (params?: any) => {
  return defHttp.post({ url: Api.Export, params }, { isTransformResponse: false });
};

// 导入智慧灌溉
export const importIrrigation = (params: any) => {
  return defHttp.uploadFile({ url: Api.Import }, params );
};

// 获取选中地块的生长阶段与土壤水分
export const getPlotStatus = (plotId: string | number) => {
  return defHttp.get({ url: `${Api.PlotStatus}/${plotId}` });
};

// 基于 Penman 算法的灌溉建议（是否需要、时间、方式等）
export const getPenmanPredict = (plotId: string | number) => {
  return defHttp.get({ url: `${Api.PenmanPredict}` , params: { plotId } });
};

// 干预灌溉 vs 不干预灌溉 对比数据（用于图表）
export const getInterventionComparison = (plotId: string | number) => {
  return defHttp.get({ url: `${Api.InterventionComparison}`, params: { plotId } });
};