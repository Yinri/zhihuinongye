import { defHttp } from '/@/utils/http/axios';

// 获取生产计划列表
export const getProductionPlanList = (params) => {
  return defHttp.get({ url: '/rapeseed/production-plan/list', params });
};

// 保存生产计划
export const saveProductionPlan = (params) => {
  return defHttp.post({ url: '/rapeseed/production-plan/save', params });
};

// 更新生产计划
export const updateProductionPlan = (params) => {
  return defHttp.post({ url: '/rapeseed/production-plan/update', params });
};

// 删除生产计划
export const deleteProductionPlan = (params) => {
  return defHttp.post({ url: '/rapeseed/production-plan/delete', params });
};

// 获取生产计划详情
export const getProductionPlanDetail = (id) => {
  return defHttp.get({ url: `/rapeseed/production-plan/detail/${id}` });
};

// 根据基地ID获取生产计划列表
export const getProductionPlanListByBaseId = (baseId) => {
  return defHttp.get({ url: `/rapeseed/production-plan/list-by-base/${baseId}` });
};

// 根据地块ID获取生产计划列表
export const getProductionPlanListByFieldId = (fieldId) => {
  return defHttp.get({ url: `/rapeseed/production-plan/list-by-field/${fieldId}` });
};

// 获取生产计划数据（包含基地和地块信息）
export const getProductionPlanData = (params) => {
  return defHttp.get({ url: '/rapeseed/production-plan/data', params });
};
