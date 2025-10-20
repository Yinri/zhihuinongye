import { defHttp } from '/@/utils/http/axios';

// 获取灌溉状态
export const getIrrigationStatus = () => {
  return defHttp.get({ url: '/rapeseed/irrigation/status' });
};

// 获取灌溉计划
export const getIrrigationPlan = () => {
  return defHttp.get({ url: '/rapeseed/irrigation/plan' });
};

// 获取灌溉历史
export const getIrrigationHistory = () => {
  return defHttp.get({ url: '/rapeseed/irrigation/history' });
};

// 开始灌溉
export const startIrrigation = (params) => {
  return defHttp.post({ url: '/rapeseed/irrigation/start', params });
};

// 停止灌溉
export const stopIrrigation = (params) => {
  return defHttp.post({ url: '/rapeseed/irrigation/stop', params });
};

// 设置灌溉计划
export const setIrrigationPlan = (params) => {
  return defHttp.post({ url: '/rapeseed/irrigation/set-plan', params });
};