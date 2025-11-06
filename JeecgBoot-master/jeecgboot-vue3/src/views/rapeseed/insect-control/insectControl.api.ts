import { defHttp } from '/@/utils/http/axios';
import { AxiosRequestConfig } from 'axios';

/**
 * 获取农药列表
 */

enum Api {
  // 农药列表
  PesticideList = '/youcai/youcaiPesticideInputs/name/list',
}

/**
 * 获取农药列表
 */
export const getPesticideList = (params?: AxiosRequestConfig) => {
  return defHttp.get({ url: Api.PesticideList, params });
};

