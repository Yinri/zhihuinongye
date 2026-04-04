import { defHttp } from '/@/utils/http/axios';  
import { useGlobSetting } from '/@/hooks/setting';
import { AxiosRequestConfig } from 'axios';

// 枚举API地址
enum Api {
  List = '/youcai/irrigation/list',
  Save = '/youcai/irrigation/save',
  Edit = '/youcai/irrigation/edit',
  Delete = '/youcai/irrigation/delete',
  BatchDelete = '/youcai/irrigation/batchDelete',
  Detail = '/youcai/irrigation/queryById',
  Export = '/youcai/irrigation/export',
  Import = '/youcai/irrigation/importExcel',
  PlotStatus = '/youcai/irrigation/plotStatus',
  PlotStatusByBase = '/youcai/irrigation/plotStatusByBase',
  PenmanPredict = '/youcai/irrigation/penmanPredict',
  InterventionComparison = '/youcai/irrigation/interventionComparison',
  FarmingAdd = '/youcai/farmingRecords/add',
}

// 获取智慧灌溉列表
export const getIrrigationList = (params?: AxiosRequestConfig) => {
  return defHttp.get({ url: Api.List, params });
};

// 保存智慧灌溉
export const saveIrrigation = (params: any) => {
  return defHttp.post({ url: Api.Save, data: params });
};

// 编辑智慧灌溉
export const editIrrigation = (params: any) => {
  return defHttp.request({ url: Api.Edit, method: 'PUT', data: params });
};

export const saveFarmingRecord = (params: any) => {
  return defHttp.post({ url: Api.FarmingAdd, data: params });
};

export const saveIrrigationDirect = (params: any) => {
  const { domainUrl } = useGlobSetting();
  return defHttp.post({ url: `${domainUrl}/youcai/irrigation/save`, data: params });
};

// 删除智慧灌溉
export const deleteIrrigation = (id: string, params?: AxiosRequestConfig) => {
  return defHttp.delete({ url: `${Api.Delete}/${id}`, params });
};

// 批量删除智慧灌溉
export const batchDeleteIrrigation = (ids: string[], params?: AxiosRequestConfig) => {
  return defHttp.delete({ url: Api.BatchDelete, data: ids, params });
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

export const getPlotStatusByBase = (baseId: string | number) => {
  return defHttp.get({ url: `${Api.PlotStatusByBase}/${baseId}` });
};

// 基于 Penman 算法的灌溉建议（是否需要、时间、方式等）
export const getPenmanPredict = (plotId?: string | number, baseId?: string | number) => {
  const params: any = {};
  if (plotId != null) params.plotId = plotId;
  if (baseId != null) params.baseId = baseId;
  return defHttp.get({ url: `${Api.PenmanPredict}` , params });
};

// 干预灌溉 vs 不干预灌溉 对比数据（用于图表）
export const getInterventionComparison = (plotId?: string | number, baseId?: string | number) => {
  const params: any = {};
  if (plotId != null) params.plotId = plotId;
  if (baseId != null) params.baseId = baseId;
  return defHttp.get({ url: `${Api.InterventionComparison}`, params });
};
