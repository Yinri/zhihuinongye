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
  // 地块养分与阶段
  PlotStatus = '/rapeseed/fertilization/plotStatus',
  // QUEFTS 推荐
  Recommend = '/rapeseed/fertilization/recommend',
  // 天气与预报
  Forecast = '/rapeseed/fertilization/forecast',
  // 一键施肥执行（占位）
  QuickApply = '/rapeseed/fertilization/quickApply',
  // 一键施肥计划（占位）
  QuickPlan = '/rapeseed/fertilization/quickPlan',
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

// 获取选中地块的养分状态（N/P/K百分比、目标产量）
export const getPlotNutrientStatus = (plotId: string | number) => {
  return defHttp.get({ url: `${Api.PlotStatus}/${plotId}` });
};

// 获取QUEFTS施肥推荐（N/P2O5/K2O、时间、方式、理由）
export const getFertilizationRecommendation = (params: any) => {
  return defHttp.post({ url: Api.Recommend, params });
};

// 获取一周天气预报（降雨、温度、ET0）
export const getWeatherForecast = (plotId: string | number) => {
  return defHttp.get({ url: `${Api.Forecast}/${plotId}` });
};

// 执行一键施肥（占位：后端接入后替换真实逻辑）
export const executeQuickFertilization = (params: any) => {
  return defHttp.post({ url: Api.QuickApply, params });
};

// 计划一键施肥（占位：后端接入后替换真实逻辑）
export const planQuickFertilization = (params: any) => {
  return defHttp.post({ url: Api.QuickPlan, params });
};