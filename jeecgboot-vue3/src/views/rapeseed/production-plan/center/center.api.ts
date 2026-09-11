import { defHttp } from '/@/utils/http/axios';

enum Api {
  getPlotsByBaseId = '/youcai/youcaiPlots/queryByBaseId',
  getPlotPlanByPlotId = '/youcai/youcaiProductionPlans/queryByPlotId',
  getSoilFertilityByPlotId = '/youcai/youcaiSoilFertility/queryByPlotId',
  getAllVariety = '/youcai/youcaiRapeVarieties/getAll',
  getFertilizationByBase = '/rapeseed/fertilization/baseRecommend',
  getFertilizationStatus = '/rapeseed/fertilization/plotStatusByBase',
}

export const getPlotsByBaseId = (baseId: string | number) => {
  return defHttp.get({ url: Api.getPlotsByBaseId, params: { baseId } });
};

export const getPlotPlanByPlotId = (plotId: string | number) => {
  return defHttp.get({ url: Api.getPlotPlanByPlotId, params: { plotId } });
};

export const getSoilFertilityByPlotId = (plotId: string | number) => {
  return defHttp.get({ url: Api.getSoilFertilityByPlotId, params: { plotId } });
};

export const getAllVariety = () => {
  return defHttp.get({ url: Api.getAllVariety });
};

export const getFertilizationByBase = (baseId: string) => {
  return defHttp.get({ url: `${Api.getFertilizationByBase}/${baseId}` });
};

export const getFertilizationStatus = (baseId: string) => {
  return defHttp.get({ url: `${Api.getFertilizationStatus}/${baseId}` });
};