/**
 * 基地管理API
 */
import { defHttp } from '/@/utils/http/axios';
import { getUrl } from '/@/utils';

enum Api {
  // 获取基地列表
  getBaseList = '/rapeseed/youcaiBases/list',
  // 获取基地详情
  getBaseById = '/rapeseed/youcaiBases/queryById',
}

/**
 * 获取基地列表
 * @param params 查询参数
 * @returns 基地列表数据
 */
export const getBaseList: Function = (params) => {
  return defHttp.get({ url: Api.getBaseList, params });
};

/**
 * 根据ID获取基地详情
 * @param id 基地ID
 * @returns 基地详情
 */
export const getBaseById: Function = (id) => {
  return defHttp.get({ url: getUrl(Api.getBaseById, { id }) });
};