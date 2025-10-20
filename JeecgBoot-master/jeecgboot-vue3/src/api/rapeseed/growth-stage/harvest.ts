import { defHttp } from '/@/utils/http/axios';

// 获取收获记录列表
export const getHarvestList = (params) => {
  return defHttp.get({ url: '/rapeseed/harvest/list', params });
};

// 保存收获记录
export const saveHarvest = (params) => {
  return defHttp.post({ url: '/rapeseed/harvest/save', params });
};

// 更新收获记录
export const updateHarvest = (params) => {
  return defHttp.post({ url: '/rapeseed/harvest/update', params });
};

// 删除收获记录
export const deleteHarvest = (id) => {
  return defHttp.post({ url: `/rapeseed/harvest/delete/${id}` });
};

// 获取收获记录详情
export const getHarvestDetail = (id) => {
  return defHttp.get({ url: `/rapeseed/harvest/detail/${id}` });
};

// 获取收获进度
export const getHarvestProgress = () => {
  return defHttp.get({ url: '/rapeseed/harvest/progress' });
};

// 获取收获预测
export const getHarvestForecast = () => {
  return defHttp.get({ url: '/rapeseed/harvest/forecast' });
};

// 获取收获统计
export const getHarvestStats = () => {
  return defHttp.get({ url: '/rapeseed/harvest/stats' });
};

// 获取最佳收获时间
export const getBestHarvestTime = () => {
  return defHttp.get({ url: '/rapeseed/harvest/best-time' });
};

// 获取收获建议
export const getHarvestAdvice = () => {
  return defHttp.get({ url: '/rapeseed/harvest/advice' });
};

// 获取收获计划
export const getHarvestPlan = () => {
  return defHttp.get({ url: '/rapeseed/harvest/plan' });
};

// 获取收获趋势数据
export const getHarvestTrend = () => {
  return defHttp.get({ url: '/rapeseed/harvest/trend' });
};

// 获取产量统计
export const getYieldStatistics = () => {
  return defHttp.get({ url: '/rapeseed/harvest/yield-statistics' });
};

// 获取下次作业计划
export const getNextOperation = () => {
  return defHttp.get({ url: '/rapeseed/harvest/next-operation' });
};

// 获取收获与整地计划
export const getHarvestManagementPlan = () => {
  return defHttp.get({ url: '/rapeseed/harvest/management-plan' });
};

// 获取产量分布数据
export const getYieldDistribution = () => {
  return defHttp.get({ url: '/rapeseed/harvest/yield-distribution' });
};
