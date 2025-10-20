import { defHttp } from '/@/utils/http/axios';

// 获取苗期管理列表
export const getSeedlingList = (params) => {
  return defHttp.get({ url: '/rapeseed/seedling/list', params });
};

// 保存苗期管理记录
export const saveSeedling = (params) => {
  return defHttp.post({ url: '/rapeseed/seedling/save', params });
};

// 更新苗期管理记录
export const updateSeedling = (params) => {
  return defHttp.post({ url: '/rapeseed/seedling/update', params });
};

// 删除苗期管理记录
export const deleteSeedling = (id) => {
  return defHttp.post({ url: `/rapeseed/seedling/delete/${id}` });
};

// 获取苗期管理详情
export const getSeedlingDetail = (id) => {
  return defHttp.get({ url: `/rapeseed/seedling/detail/${id}` });
};

// 获取苗期生长状况
export const getSeedlingGrowthStatus = () => {
  return defHttp.get({ url: '/rapeseed/seedling/growth-status' });
};

// 获取苗期健康评估
export const getSeedlingHealthAssessment = () => {
  return defHttp.get({ url: '/rapeseed/seedling/health-assessment' });
};

// 获取环境监测数据
export const getEnvironmentMonitoring = () => {
  return defHttp.get({ url: '/rapeseed/seedling/environment-monitoring' });
};

// 获取苗期管理计划
export const getSeedlingManagementPlan = () => {
  return defHttp.get({ url: '/rapeseed/seedling/management-plan' });
};
