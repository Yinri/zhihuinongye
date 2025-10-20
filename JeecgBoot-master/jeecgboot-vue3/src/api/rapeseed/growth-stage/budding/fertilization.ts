import { defHttp } from '/@/utils/http/axios';

// 获取追肥记录列表
export const getFertilizationList = (params) => {
  return defHttp.get({ url: '/rapeseed/fertilization/list', params });
};

// 保存追肥记录
export const saveFertilization = (params) => {
  return defHttp.post({ url: '/rapeseed/fertilization/save', params });
};

// 更新追肥记录
export const updateFertilization = (params) => {
  return defHttp.post({ url: '/rapeseed/fertilization/update', params });
};

// 删除追肥记录
export const deleteFertilization = (id) => {
  return defHttp.post({ url: `/rapeseed/fertilization/delete/${id}` });
};

// 获取追肥记录详情
export const getFertilizationDetail = (id) => {
  return defHttp.get({ url: `/rapeseed/fertilization/detail/${id}` });
};

// 获取施肥进度
export const getFertilizationProgress = () => {
  return defHttp.get({ url: '/rapeseed/fertilization/progress' });
};

// 获取土壤养分状况
export const getSoilNutrients = () => {
  return defHttp.get({ url: '/rapeseed/fertilization/soil-nutrients' });
};

// 获取施肥统计
export const getFertilizationStats = () => {
  return defHttp.get({ url: '/rapeseed/fertilization/stats' });
};

// 获取下次施肥计划
export const getNextFertilization = () => {
  return defHttp.get({ url: '/rapeseed/fertilization/next' });
};

// 获取追肥计划
export const getFertilizationPlan = () => {
  return defHttp.get({ url: '/rapeseed/fertilization/plan' });
};

// 获取养分趋势数据
export const getNutrientTrend = () => {
  return defHttp.get({ url: '/rapeseed/fertilization/nutrient-trend' });
};
