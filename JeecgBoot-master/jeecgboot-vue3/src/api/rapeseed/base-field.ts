import { defHttp } from '/@/utils/http/axios';

// 获取基地列表
export const getBaseList = (params) => {
  return defHttp.get({ url: '/rapeseed/base/list', params });
};

// 保存基地
export const saveBase = (params) => {
  return defHttp.post({ url: '/rapeseed/base/save', params });
};

// 更新基地
export const updateBase = (params) => {
  return defHttp.post({ url: '/rapeseed/base/update', params });
};

// 删除基地
export const deleteBase = (id) => {
  return defHttp.post({ url: `/rapeseed/base/delete/${id}` });
};

// 获取基地详情
export const getBaseDetail = (id) => {
  return defHttp.get({ url: `/rapeseed/base/detail/${id}` });
};

// 获取地块列表
export const getFieldList = (params) => {
  return defHttp.get({ url: '/rapeseed/field/list', params });
};

// 根据基地ID获取地块列表
export const getFieldListByBaseId = (baseId) => {
  return defHttp.get({ url: `/rapeseed/field/list-by-base/${baseId}` });
};

// 保存地块
export const saveField = (params) => {
  return defHttp.post({ url: '/rapeseed/field/save', params });
};

// 更新地块
export const updateField = (params) => {
  return defHttp.post({ url: '/rapeseed/field/update', params });
};

// 删除地块
export const deleteField = (id) => {
  return defHttp.post({ url: `/rapeseed/field/delete/${id}` });
};

// 获取地块详情
export const getFieldDetail = (id) => {
  return defHttp.get({ url: `/rapeseed/field/detail/${id}` });
};