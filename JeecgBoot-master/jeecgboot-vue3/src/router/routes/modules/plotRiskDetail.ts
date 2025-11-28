import type { AppRouteModule } from '/@/router/types';

import { LAYOUT } from '/@/router/constant';

const plotRiskDetail: AppRouteModule = {
  path: '/rapeseed',
  name: 'PlotRiskDetail',
  component: LAYOUT,
  redirect: '/rapeseed/lodging-risk',
  meta: {
    hideChildrenInMenu: true,
    icon: 'ant-design:home-outlined',
    title: '油菜地块风险详情',
  },
  children: [
    {
      path: 'plot-risk-detail/:plotId',
      name: 'PlotRiskDetailPage',
      component: () => import('/@/views/rapeseed/plot-risk-detail.vue'),
      meta: {
        title: '地块风险详情',
        hideMenu: true,
        showParent: true,
      },
    },
  ],
};

export default plotRiskDetail;