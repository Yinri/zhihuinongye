import { defHttp } from '/@/utils/http/axios';

// 获取播种列表
export const getSowingList = (params) => {
  return defHttp.get({ url: '/rapeseed/sowing/list', params });
};

// 保存播种
export const saveSowing = (params) => {
  return defHttp.post({ url: '/rapeseed/sowing/save', params });
};

// 更新播种
export const updateSowing = (params) => {
  return defHttp.post({ url: '/rapeseed/sowing/update', params });
};

// 删除播种
export const deleteSowing = (id) => {
  return defHttp.post({ url: `/rapeseed/sowing/delete/${id}` });
};

// 获取播种详情
export const getSowingDetail = (id) => {
  return defHttp.get({ url: `/rapeseed/sowing/detail/${id}` });
};

// 获取播种进度
export const getSowingProgress = () => {
  return defHttp.get({ url: '/rapeseed/sowing/progress' });
};

// 获取播种质量评估
export const getSowingQuality = () => {
  return defHttp.get({ url: '/rapeseed/sowing/quality' });
};

// 获取天气信息
export const getWeatherInfo = () => {
  return defHttp.get({ url: '/rapeseed/sowing/weather' });
};

// 获取播种计划
export const getSowingPlan = () => {
  return defHttp.get({ url: '/rapeseed/sowing/plan' });
};
