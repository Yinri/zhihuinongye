/**
 * 地块管理API
 */
import { defHttp } from '/@/utils/http/axios';
import { getUrl } from '/@/utils';

enum Api {
  getPlotList = '/youcai/youcaiPlots/list',
  getPlotListByBaseId = '/youcai/youcaiPlots/queryByBaseId',
  getPlotById = '/youcai/youcaiPlots/queryById',
}

/**
 * 获取地块列表
 * @param params 查询参数
 * @returns 地块列表数据
 */
export const getPlotList: Function = (params) => {
  return defHttp.get({ url: Api.getPlotList, params });
};

/**
 * 根据基地ID获取地块列表
 * @param params 查询参数，包含baseId
 * @returns 地块列表数据
 */
export const getPlotListByBaseId: Function = (params) => {
  return defHttp.get({ url: Api.getPlotListByBaseId, params });
};

/**
 * 根据ID获取地块详情
 * @param id 地块ID
 * @returns 地块详情
 */
export const getPlotById: Function = (id) => {
  return defHttp.get({ url: getUrl(Api.getPlotById, { id }) });
};