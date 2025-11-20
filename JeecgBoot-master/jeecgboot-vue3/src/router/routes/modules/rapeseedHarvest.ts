import type { AppRouteModule } from '/@/router/types';
import { LAYOUT } from '/@/router/constant';

const rapeseedHarvest: AppRouteModule = {
  path: '/rapeseed/harvest',
  name: 'RapeseedHarvestRoot',
  component: LAYOUT,
  redirect: '/rapeseed/harvest/index',
  meta: {
    title: '收获管理',
  },
  children: [
    {
      path: 'index',
      name: 'RapeseedHarvestIndex',
      component: () => import('/@/views/rapeseed/harvest/index.vue'),
      meta: {
        title: '收获管理首页',
      },
    },
    {
      path: 'machine',
      name: 'RapeseedHarvestMachine',
      component: () => import('/@/views/rapeseed/harvest/machine/index.vue'),
      meta: {
        title: '农机档案',
      },
    },
    {
      path: 'task',
      name: 'RapeseedHarvestTask',
      component: () => import('/@/views/rapeseed/harvest/task/index.vue'),
      meta: {
        title: '收获任务',
      },
    },
  ],
};

export default rapeseedHarvest;