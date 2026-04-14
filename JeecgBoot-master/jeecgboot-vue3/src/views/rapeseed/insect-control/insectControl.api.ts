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

/**
 * 获取农药列表
 *
 *
 */
export const getPesticideList = (params?: AxiosRequestConfig) => {
  return defHttp.get({ url: Api.PesticideList, params });
};
export const getFirstPest = (params?: AxiosRequestConfig) => {
  return defHttp.get({ url: Api.GetPest, params });
};
export const addPestControlRecord = (data: any, config?: AxiosRequestConfig) => {
  return defHttp.post({ url: Api.AddPestControl, data }, config);
}
export const analyzePestWithAI = (data: any) => {
  return defHttp.post({ url: Api.AIAnalysis, data });
};
export const getAllPest = (params) => {
  return defHttp.get({
    url: Api.FindPest,
    params: params
  });
};
export const getControlHistory = (params) => {
  return defHttp.post({
    url: Api.FindControl,
    params: params
  });
};
