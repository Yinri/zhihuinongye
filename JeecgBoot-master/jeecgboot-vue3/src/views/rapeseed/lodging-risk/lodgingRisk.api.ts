import { defHttp } from '/@/utils/http/axios';
import { AxiosRequestConfig } from 'axios';

// 枚举API地址
enum Api {
  // 列表
  List = '/rapeseed/lodgingRisk/list',
  // 保存
  Save = '/rapeseed/lodgingRisk/save',
  // 编辑
  Edit = '/rapeseed/lodgingRisk/edit',
  // 删除
  Delete = '/rapeseed/lodgingRisk/delete',
  // 批量删除
  BatchDelete = '/rapeseed/lodgingRisk/batchDelete',
  // 导出
  Export = '/rapeseed/lodgingRisk/export',
  // 导入
  Import = '/rapeseed/lodgingRisk/importExcel',
  // 获取倒伏风险预警数据
  RiskData = '/rapeseed/lodgingRisk/riskData',
}

// 获取倒伏风险预警列表
export const getLodgingRiskList = (params?: AxiosRequestConfig) => {
  return defHttp.get({ url: Api.List, params });
};

// 保存倒伏风险预警
export const saveLodgingRisk = (params: any) => {
  return defHttp.post({ url: Api.Save, params });
};

// 编辑倒伏风险预警
export const editLodgingRisk = (params: any) => {
  return defHttp.put({ url: Api.Edit, params });
};

// 删除倒伏风险预警
export const deleteLodgingRisk = (id: string, params?: AxiosRequestConfig) => {
  return defHttp.delete({ url: `${Api.Delete}/${id}`, params });
};

// 批量删除倒伏风险预警
export const batchDeleteLodgingRisk = (ids: string[], params?: AxiosRequestConfig) => {
  return defHttp.delete({ url: Api.BatchDelete, data: { ids } });
};


// 导出倒伏风险预警
export const exportLodgingRisk = (params?: any) => {
  return defHttp.post({ url: Api.Export, params }, { isTransformResponse: false });
};

// 导入倒伏风险预警
export const importLodgingRisk = (params: any) => {
  return defHttp.uploadFile({ url: Api.Import }, params );
};

// 获取倒伏风险预警数据
export const getLodgingRiskDataById = (plotId: string) => {
  return defHttp.get({ url:`${Api.RiskData}/${plotId}` });
};
