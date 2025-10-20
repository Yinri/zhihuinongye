import { defHttp } from '/@/utils/http/axios';

// 获取花期记录列表
export const getFloweringList = (params) => {
  return defHttp.get({ url: '/rapeseed/flowering/list', params });
};

// 保存花期记录
export const saveFlowering = (params) => {
  return defHttp.post({ url: '/rapeseed/flowering/save', params });
};

// 更新花期记录
export const updateFlowering = (params) => {
  return defHttp.post({ url: '/rapeseed/flowering/update', params });
};

// 删除花期记录
export const deleteFlowering = (id) => {
  return defHttp.post({ url: `/rapeseed/flowering/delete/${id}` });
};

// 获取花期记录详情
export const getFloweringDetail = (id) => {
  return defHttp.get({ url: `/rapeseed/flowering/detail/${id}` });
};

// 获取花期进度
export const getFloweringProgress = () => {
  return defHttp.get({ url: '/rapeseed/flowering/progress' });
};

// 获取花期预测
export const getFloweringForecast = () => {
  return defHttp.get({ url: '/rapeseed/flowering/forecast' });
};

// 获取花期统计
export const getFloweringStats = () => {
  return defHttp.get({ url: '/rapeseed/flowering/stats' });
};

// 获取最佳授粉时间
export const getBestPollinationTime = () => {
  return defHttp.get({ url: '/rapeseed/flowering/pollination-time' });
};

// 获取授粉建议
export const getPollinationAdvice = () => {
  return defHttp.get({ url: '/rapeseed/flowering/pollination-advice' });
};

// 获取花期管理计划
export const getFloweringPlan = () => {
  return defHttp.get({ url: '/rapeseed/flowering/plan' });
};

// 获取花期趋势数据
export const getFloweringTrend = () => {
  return defHttp.get({ url: '/rapeseed/flowering/trend' });
};
