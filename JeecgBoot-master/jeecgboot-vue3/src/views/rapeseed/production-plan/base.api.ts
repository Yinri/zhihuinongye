/**
 * 基地管理API
 */
import { defHttp } from '/@/utils/http/axios';
import { getUrl } from '/@/utils';

enum Api {
  // 获取基地列表
  getBaseList = '/youcai/bases/getAllBases',
  // 通过id获取基地详情
  getBaseById = '/youcai/bases/queryById',
  //通过基地id获取地块列表
  getPlotsByBaseId='/youcai/youcaiPlots/queryByBaseId',
  //通过地块id获取地块详情
  getPlotById='/youcai/youcaiPlots/queryById',
  //创建基地
  createBase='/youcai/bases/add',
  //通过地块id获取当前地块生产计划
  getPlotPlanByPlotId='/youcai/youcaiProductionPlans/queryByPlotId',
  //查询所有品种信息
  getAllVariety='/youcai/youcaiRapeVarieties/getAll',
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

/**
 * 根据基地ID获取地块列表（修正版）
 * @param baseid 基地ID（参数名与后端保持一致）
 * @returns 地块列表数据
 */
export const getPlotsByBaseId: Function = (baseid) => {
  return defHttp.get({
    url: Api.getPlotsByBaseId,
    params: { baseid }  // 明确传递 baseid 参数
  });
};

/**
 * 根据ID获取地块详情
 * @param id 地块ID
 * @returns 地块详情
 */
export const getPlotById: Function = (id) => {
  return defHttp.get({ url: getUrl(Api.getPlotById, { id }) });
};


/**
 * 创建新基地
 * @param data 基地表单数据（包含全称、负责人、电话等信息）
 * @returns 创建结果
 */
export const createBase: Function = (data) => {
  return defHttp.post({
    url: Api.createBase,
    data  // 传递表单数据（与后端接口字段对应）
  });
};

/**
 * 通过地块ID获取生产计划
 * @param plotId 地块ID
 * @returns 生产计划详情（无计划时返回null）
 */
export const getPlotPlanByPlotId: Function = (plotId) => {
  return defHttp.get({
    url: Api.getPlotPlanByPlotId,
    params: { plotId }  // 与后端接口参数名一致
  });
};

/**
 * 查询所有作物品种信息
 * @returns 所有品种列表（label为品种名称，value为品种标识）
 */
export const getAllVariety: Function = () => {
  return defHttp.get({ url: Api.getAllVariety });
};
