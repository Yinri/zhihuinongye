import { defHttp } from '/@/utils/http/axios';

// 获取蕾薹期管理列表
export const getBuddingList = (params) => {
  return defHttp.get({ url: '/rapeseed/budding/list', params });
};

// 保存蕾薹期管理记录
export const saveBudding = (params) => {
  return defHttp.post({ url: '/rapeseed/budding/save', params });
};

// 更新蕾薹期管理记录
export const updateBudding = (params) => {
  return defHttp.post({ url: '/rapeseed/budding/update', params });
};

// 删除蕾薹期管理记录
export const deleteBudding = (id) => {
  return defHttp.post({ url: `/rapeseed/budding/delete/${id}` });
};

// 获取蕾薹期管理详情
export const getBuddingDetail = (id) => {
  return defHttp.get({ url: `/rapeseed/budding/detail/${id}` });
};

// 获取蕾薹期进度
export const getBuddingProgress = () => {
  return defHttp.get({ url: '/rapeseed/budding/progress' });
};

// 获取植株状况
export const getPlantStatus = () => {
  return defHttp.get({ url: '/rapeseed/budding/plant-status' });
};

// 获取生长环境
export const getEnvironment = () => {
  return defHttp.get({ url: '/rapeseed/budding/environment' });
};

// 获取营养状况
export const getNutrition = () => {
  return defHttp.get({ url: '/rapeseed/budding/nutrition' });
};

// 获取蕾薹期管理计划
export const getBuddingManagementPlan = () => {
  return defHttp.get({ url: '/rapeseed/budding/management-plan' });
};
