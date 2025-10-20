import { defHttp } from '/@/utils/http/axios';

// 获取成熟期记录列表
export const getMaturityList = (params) => {
  return defHttp.get({ url: '/rapeseed/maturity/list', params });
};

// 保存成熟期记录
export const saveMaturity = (params) => {
  return defHttp.post({ url: '/rapeseed/maturity/save', params });
};

// 更新成熟期记录
export const updateMaturity = (params) => {
  return defHttp.post({ url: '/rapeseed/maturity/update', params });
};

// 删除成熟期记录
export const deleteMaturity = (id) => {
  return defHttp.post({ url: `/rapeseed/maturity/delete/${id}` });
};

// 获取成熟期记录详情
export const getMaturityDetail = (id) => {
  return defHttp.get({ url: `/rapeseed/maturity/detail/${id}` });
};

// 获取成熟期进度
export const getMaturityProgress = () => {
  return defHttp.get({ url: '/rapeseed/maturity/progress' });
};

// 获取成熟期预测
export const getMaturityForecast = () => {
  return defHttp.get({ url: '/rapeseed/maturity/forecast' });
};

// 获取成熟期统计
export const getMaturityStats = () => {
  return defHttp.get({ url: '/rapeseed/maturity/stats' });
};

// 获取最佳收获时间
export const getBestHarvestTime = () => {
  return defHttp.get({ url: '/rapeseed/maturity/harvest-time' });
};

// 获取收获建议
export const getHarvestAdvice = () => {
  return defHttp.get({ url: '/rapeseed/maturity/harvest-advice' });
};

// 获取成熟期管理计划
export const getMaturityPlan = () => {
  return defHttp.get({ url: '/rapeseed/maturity/plan' });
};

// 获取成熟期趋势数据
export const getMaturityTrend = () => {
  return defHttp.get({ url: '/rapeseed/maturity/trend' });
};
