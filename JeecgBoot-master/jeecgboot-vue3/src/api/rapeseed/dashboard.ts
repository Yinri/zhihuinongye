import { defHttp } from '/@/utils/http/axios';

enum Api {
  // 获取首页统计数据
  getDashboardData = '/rapeseed/dashboard/getData',
  // 获取生长阶段进度
  getGrowthProgress = '/rapeseed/dashboard/getGrowthProgress',
  // 获取近期任务
  getRecentTasks = '/rapeseed/dashboard/getRecentTasks',
}

// 获取首页统计数据
export const getDashboardData = () => {
  return defHttp.get({ url: Api.getDashboardData });
};

// 获取生长阶段进度
export const getGrowthProgress = () => {
  return defHttp.get({ url: Api.getGrowthProgress });
};

// 获取近期任务
export const getRecentTasks = () => {
  return defHttp.get({ url: Api.getRecentTasks });
};