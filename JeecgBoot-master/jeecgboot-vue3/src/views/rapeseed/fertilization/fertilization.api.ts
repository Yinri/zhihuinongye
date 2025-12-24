import { defHttp } from '/@/utils/http/axios';

enum Api {
  PlotStatusByBase = '/rapeseed/fertilization/plotStatusByBase',
  BaseSoilSeries = '/rapeseed/fertilization/baseSoilSeries',
  BaseRecommend = '/rapeseed/fertilization/baseRecommend',
}

export const getPlotStatusByBase = (baseId: string | number) => {
  return defHttp.get({ url: `${Api.PlotStatusByBase}/${baseId}` });
};

export const getBaseSoilSeries = (baseId: string | number) => {
  return defHttp.get({ url: `${Api.BaseSoilSeries}/${baseId}` });
};

export const getBaseRecommendation = (baseId: string | number) => {
  return defHttp.get({ url: `${Api.BaseRecommend}/${baseId}` });
};

