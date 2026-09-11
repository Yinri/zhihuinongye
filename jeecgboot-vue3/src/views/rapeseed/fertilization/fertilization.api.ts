import { defHttp } from '/@/utils/http/axios';

enum Api {
  PlotStatusByBase = '/youcai/fertilization/plotStatusByBase',
  BaseSoilSeries = '/youcai/fertilization/baseSoilSeries',
  BaseRecommend = '/youcai/fertilization/baseRecommend',
  FertilizerList = '/youcai/youcaiFertilizerInfo/queryAll',
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

export const getFertilizerList = () => {
  return defHttp.get({ url: Api.FertilizerList });
};

