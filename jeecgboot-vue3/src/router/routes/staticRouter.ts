import type { AppRouteRecordRaw } from '/@/router/types';
import { LAYOUT } from '/@/router/constant';
import plotRiskDetail from './modules/plotRiskDetail';

export const AI_ROUTE: AppRouteRecordRaw = {
  path: '',
  name: 'ai-parent',
  component: LAYOUT,
  meta: {
    title: 'ai',
  },
  children: [
    {
      path: '/ai',
      name: 'ai',
      component: () => import('/@/views/dashboard/ai/index.vue'),
      meta: {
        title: 'AI助手',
      },
    },
  ],
};

// 后端菜单模式不会加载 asyncRoutes；地图跳转的隐藏详情页需在登录后一起注册。
export const staticRoutesList = [AI_ROUTE, plotRiskDetail];
