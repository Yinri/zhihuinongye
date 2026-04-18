import { defHttp } from '/@/utils/http/axios';
import { AxiosRequestConfig } from 'axios';

/**
 * 获取农药列表
 */

enum Api {
  // 农药列表
  PesticideList = '/youcai/youcaiPesticideInfo/name/list',
  AddPestControl = '/youcai/youcaiPestControl/add',
  GetPest='/youcai/youcaiPestControl/images',
  AIAnalysis = '/youcai/youcaiPestControl/aiAnalysis',
  FindPest='/youcai/youcaiPestControl/findImages',
  FindControl='/youcai/youcaiPestControl/findControl'

}

export interface PestImageQueryParams {
  baseName: string;
  StarDate: string;
  EndDate: string;
}

/**
 * 获取农药列表
 *
 *
 */
export const getPesticideList = (params?: AxiosRequestConfig) => {
  return defHttp.get({ url: Api.PesticideList, params });
};
/**
 * 获取当前基地最近一条虫情图片数据。
 */
export const getFirstPest = (params: PestImageQueryParams) => {
  return defHttp.get({ url: Api.GetPest, params, timeout: 30 * 1000 });
};
export const addPestControlRecord = (data: any, config?: AxiosRequestConfig) => {
  return defHttp.post({ url: Api.AddPestControl, data }, config);
}
export const analyzePestWithAI = (data: any) => {
  return defHttp.post({ url: Api.AIAnalysis, data });
};
/**
 * 获取当前基地指定时间范围内的虫情图片明细。
 */
export const getAllPest = (params: PestImageQueryParams) => {
  return defHttp.get({
    url: Api.FindPest,
    params,
    timeout: 30 * 1000,
  });
};
export const getControlHistory = (params) => {
  return defHttp.post({
    url: Api.FindControl,
    params: params
  });
};
