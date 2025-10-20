import { defHttp } from '/@/utils/http/axios';

// 获取病虫害防控记录列表
export const getPestControlList = (params) => {
  return defHttp.get({ url: '/rapeseed/pest-control/list', params });
};

// 保存病虫害防控记录
export const savePestControl = (params) => {
  return defHttp.post({ url: '/rapeseed/pest-control/save', params });
};

// 更新病虫害防控记录
export const updatePestControl = (params) => {
  return defHttp.post({ url: '/rapeseed/pest-control/update', params });
};

// 删除病虫害防控记录
export const deletePestControl = (id) => {
  return defHttp.post({ url: `/rapeseed/pest-control/delete/${id}` });
};

// 获取病虫害防控记录详情
export const getPestControlDetail = (id) => {
  return defHttp.get({ url: `/rapeseed/pest-control/detail/${id}` });
};

// 获取病虫害防控进度
export const getPestControlProgress = () => {
  return defHttp.get({ url: '/rapeseed/pest-control/progress' });
};

// 获取病虫害预警
export const getPestWarning = () => {
  return defHttp.get({ url: '/rapeseed/pest-control/warning' });
};

// 获取防治统计
export const getControlStats = () => {
  return defHttp.get({ url: '/rapeseed/pest-control/stats' });
};

// 获取下次防治计划
export const getNextControl = () => {
  return defHttp.get({ url: '/rapeseed/pest-control/next' });
};

// 获取防控计划
export const getControlPlan = () => {
  return defHttp.get({ url: '/rapeseed/pest-control/plan' });
};

// 获取风险趋势数据
export const getRiskTrend = () => {
  return defHttp.get({ url: '/rapeseed/pest-control/risk-trend' });
};
