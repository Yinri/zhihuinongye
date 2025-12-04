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
  // 通过基地id获取地块列表
  getPlotsByBaseId = '/youcai/youcaiPlots/queryByBaseId',
  // 通过地块id获取地块详情
  getPlotById = '/youcai/youcaiGrowthMonitoring/queryByPlotId',
  // 创建基地
  createBase = '/youcai/bases/add',
  // 通过地块id获取当前地块生产计划
  getPlotPlanByPlotId = '/youcai/youcaiProductionPlans/queryByPlotId',
  // 查询所有品种信息
  getAllVariety = '/youcai/youcaiRapeVarieties/getAll',
  // 通过品种id查询该品种的历年产量
  getVarietyHistoryByVarietyId = '/youcai/youcaiHistoricalYield/queryByVarietyId',
  // 新增品种的历年产量
  addVarietyHistory = '/youcai/youcaiHistoricalYield/add',
  // 更新品种的历年产量（根据id更新）
  updateVarietyHistory = '/youcai/youcaiHistoricalYield/edit',
  // 通过品种id查询该品种的种子参数
  getSeedParamsByVarietyId = '/youcai/youcaiRapeVarieties/queryById',
  // 新增品种的种子参数
  addSeedParams = '/youcai/youcaiRapeVarieties/add',
  // 更新品种的种子参数（根据id更新）
  updateSeedParams = '/youcai/youcaiRapeVarieties/edit',
  // 根据品种id查询单产需肥量
  getFertilizerParamsByVarietyId = '/youcai/cropNutrientDemand/getByVarietyId',
  // 编辑单产需肥量（新增/更新一体）
  saveFertilizerParams = '/youcai/cropNutrientDemand/edit',
  //根据地块id获取土壤肥力
  getSoilFertilityByPlotId='/youcai/youcaiSoilFertility/queryByPlotId',
  //根据地块、品种、肥料查询肥料利用率
  getFertilizerUtilizationRate='/youcai/varietyPlotUtilization/getByVPF',
  //通过肥料id查询肥料养分含量
  getFertilizerContentByType='/youcai/youcaiFertilizerInfo/queryById',
  //查询所有肥料信息
  getFertilizerList='/youcai/youcaiFertilizerInfo/queryAll'

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
  return defHttp.get({ url: getUrl(Api.getBaseById, { id: id + '' }) });
};

/**
 * 根据基地ID获取地块列表（修正版）
 * @param baseid 基地ID（参数名与后端保持一致）
 * @returns 地块列表数据
 */
export const getPlotsByBaseId: Function = (baseId) => {
  return defHttp.get({
    url: Api.getPlotsByBaseId,
    params: { baseId: baseId + '' }
  });
};

/**
 * 根据plotId获取地块生长监控数据
 * @param plotId 地块ID
 * @returns 生长监控数据
 */
export const getPlotById: Function = (plotId) => {
  return defHttp.get({
    url: Api.getPlotById,
    params: { plotId: plotId + '' }
  });
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
    params: { plotId: plotId + '' }
  });
};

/**
 * 查询所有作物品种信息
 * @returns 所有品种列表（label为品种名称，value为品种标识）
 */
export const getAllVariety: Function = () => {
  return defHttp.get({ url: Api.getAllVariety });
};

/**
 * 通过品种ID查询该品种的历年产量数据
 * @param varietyId 品种ID
 * @param pageNo 页码（默认1）
 * @param pageSize 每页条数（默认10）
 * @param otherParams 其他筛选参数（如年份、区域等，可选）
 * @returns 分页的产量历史数据
 */
export const getVarietyHistoryByVarietyId: Function = (params) => {
  const { varietyId, pageNo = 1, pageSize = 10, ...otherParams } = params || {};
  return defHttp.get({
    url: Api.getVarietyHistoryByVarietyId,
    params: { varietyId: varietyId + '', pageNo, pageSize, ...otherParams }
  });
};

/**
 * 新增品种的历年产量数据（适配单条提交）
 * @param data 单条产量数据（对象格式，不可传数组）
 * @returns 新增结果
 */
export const addVarietyHistory: Function = (data) => {
  return defHttp.post({
    url: Api.addVarietyHistory,
    data
  });
};

/**
 * 更新品种的历年产量数据（适配单条提交）
 * @param data 单条待更新数据（必须含id，对象格式）
 * @returns 更新结果
 */
export const updateVarietyHistory: Function = (data) => {
  // 改回POST（若后端明确支持PUT再改，优先匹配后端实际方法）
  return defHttp.post({
    url: Api.updateVarietyHistory,
    data
  });
};

/**
 * 通过品种ID查询该品种的种子参数
 * @param varietyId 品种ID（对应接口路径参数）
 * @returns 品种详情（包含种子参数）
 */
// 修正API接口实现（确保参数名与后端@RequestParam一致）
export const getSeedParamsByVarietyId: Function = (varietyId) => {
  return defHttp.get({
    url: Api.getSeedParamsByVarietyId,
    params: { id: varietyId + '' } // 强制转换为字符串，与后端String类型匹配
  });
};

/**
 * 新增品种的种子参数（同时创建品种基础信息）
 * @param data 品种完整信息（含基础信息和种子参数）
 * 数据格式示例：
 * {
 *   varietyName: "华油杂62",
 *   characteristics: "半冬性杂交种...",
 *   harvestCoefficient: 0.45,
 *   seedlingSurvivalRate: 88,
 *   // ... 其他品种字段和种子参数
 * }
 * @returns 新增结果（包含生成的品种ID）
 */
export const addSeedParams: Function = (data) => {
  return defHttp.post({
    url: Api.addSeedParams,
    data, // 传递完整的品种信息对象
  });
};

/**
 * 更新品种的种子参数（同时支持更新品种基础信息）
 * @param data 待更新的品种信息（必须包含id）
 * 数据格式示例：
 * {
 *   id: 1, // 品种ID（必填）
 *   harvestCoefficient: 0.46, // 待更新的种子参数
 *   seedlingSurvivalRate: 89,
 *   // ... 其他需要更新的字段
 * }
 * @returns 更新结果
 */
export const updateSeedParams: Function = (data) => {
  // 改回POST（匹配后端实际方法）
  return defHttp.post({
    url: Api.updateSeedParams,
    data, // 传递含id的更新对象
  });
};

// ===================== 单产需肥量相关接口（核心修正） =====================

/**
 * 通过品种ID查询该品种的单产需肥量
 * @param varietyId 品种ID
 * @returns 单产需肥量详情（nDemand/pDemand/kDemand等）
 */
export const getFertilizerParamsByVarietyId: Function = (varietyId) => {
  return defHttp.get({
    url: Api.getFertilizerParamsByVarietyId,
    params: { varietyId: varietyId + '' } // 强制转字符串，与后端参数类型匹配
  });
};
/**
 * 【核心】新增/更新品种的单产需肥量（后端edit接口，无则增、有则改）
 * @param data 需肥量数据（必须含varietyId，无需id）
 * 数据格式示例：
 * {
 *   varietyId: "1993510300000000001", // 强制字符串类型！
 *   nDemand: 5.8, // N肥需肥量（kg/100kg产量）
 *   pDemand: 2.5, // P₂O₅需肥量（kg/100kg产量）
 *   kDemand: 4.3  // K₂O需肥量（kg/100kg产量）
 * }
 * @returns 保存结果（新增/更新成功提示）
 */

// 需肥量保存接口（核心修改：删除自定义请求头，用form-data格式）
export const saveFertilizerParams: Function = (data) => {
  // 强制varietyId为字符串
  const submitData = {
    ...data,
    varietyId: data.varietyId + ''
  };
  // 用JSON格式提交（匹配后端@RequestBody）
  return defHttp.post({
    url: Api.saveFertilizerParams,
    data: submitData,
    // 强制JSON请求头，删除charset
    headers: {
      'Content-Type': 'application/json'
    }
  });
};
/**
 * 通过地块ID获取最新的土壤肥力数据
 * @param plotId 地块ID
 * @returns 最新的土壤肥力详情（phValue/organicMatter等）
 */
export const getSoilFertilityByPlotId: Function = (plotId) => {
  return defHttp.get({
    url: Api.getSoilFertilityByPlotId,
    params: { plotId: plotId + '' } // 强制转字符串，匹配后端参数类型
  });
};
/**
 * 根据地块、品种、肥料ID查询肥料利用率
 * @param params 参数对象（包含plotId、varietyId、fertilizerId）
 * @returns 肥料利用率数据
 */
export const getFertilizerUtilizationRate: Function = (params) => {
  // 强制所有ID转字符串，匹配后端参数类型
  const submitParams = {
    ...params,
    plotId: params.plotId + '',
    varietyId: params.varietyId + '',
    fertilizerId: params.fertilizerId + ''
  };
  return defHttp.get({
    url: Api.getFertilizerUtilizationRate,
    params: submitParams
  });
};
/**
 * 通过肥料ID查询肥料养分含量
 * @param fertilizerId 肥料ID
 * @returns 肥料详情（包含养分含量等字段）
 */
export const getFertilizerContentByType: Function = (fertilizerId) => {
  return defHttp.get({
    url: Api.getFertilizerContentByType,
    params: { id: fertilizerId + '' } // 匹配后端@RequestParam(name="id")，强制转字符串
  });
};
/**
 * 查询所有肥料信息
 * @returns 所有肥料信息列表（包含肥料名称、养分含量等字段）
 */
export const getFertilizerList: Function = () => {
  return defHttp.get({
    url: Api.getFertilizerList
  });
};
