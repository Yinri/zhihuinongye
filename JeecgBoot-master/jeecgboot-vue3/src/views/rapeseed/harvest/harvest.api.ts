import { defHttp } from '/@/utils/http/axios';
import { AxiosRequestConfig } from 'axios';

enum Api {
  GetHarvestList = '/youcai/harvest/list',
  SaveHarvest = '/youcai/harvest/save',
  EditHarvest = '/youcai/harvest/edit',
  DeleteHarvest = '/youcai/harvest/delete',
  DeleteBatch = '/youcai/harvest/deleteBatch',
  GetHarvestById = '/youcai/harvest/queryById',
  ExportHarvest = '/youcai/harvest/export',
  ImportHarvest = '/youcai/harvest/import',
  GetHarvestStats = '/youcai/harvest/stats',
  GetHarvesterStatus = '/youcai/harvest/harvesterStatus',
  GetYieldChart = '/youcai/harvest/yieldChart',
  GetPlotSummary = '/youcai/harvest/plotSummary',
  MachineList = '/youcai/sensorInfo/machine/list',
  OperationRecords = '/youcai/sensorInfo/records',
}

export const getHarvestList = (params?: any) => {
  return defHttp.get<any>({ url: Api.GetHarvestList, params });
};

export const saveHarvest = (params?: any) => {
  return defHttp.post<any>({ url: Api.SaveHarvest, params });
};

export const editHarvest = (params?: any) => {
  return defHttp.put<any>({ url: Api.EditHarvest, params });
};

export const deleteHarvest = (params?: any) => {
  return defHttp.delete<any>({ url: Api.DeleteHarvest, params }, { joinParamsToUrl: true });
};

export const deleteBatch = (params?: any) => {
  return defHttp.delete<any>({ url: Api.DeleteBatch, params }, { joinParamsToUrl: true });
};

export const getHarvestById = (params?: any) => {
  return defHttp.get<any>({ url: Api.GetHarvestById, params });
};

export const exportHarvest = (params?: any) => {
  return defHttp.download<any>(
    {
      url: Api.ExportHarvest,
      params,
    },
    '收获管理.xlsx'
  );
};

export const importHarvest = (params?: any, config?: AxiosRequestConfig) => {
  return defHttp.uploadFile<any>(
    {
      url: Api.ImportHarvest,
    },
    params,
    config
  );
};

export const getHarvestStats = (params?: any) => {
  return defHttp.get<any>({ url: Api.GetHarvestStats, params });
};

export const getHarvesterStatus = (params?: any) => {
  return defHttp.get<any>({ url: Api.GetHarvesterStatus, params });
};

export const getYieldChartData = (params?: any) => {
  return defHttp.get<any>({ url: Api.GetYieldChart, params });
};

export const getPlotHarvestSummary = (params?: any) => {
  return defHttp.get<any>({ url: Api.GetPlotSummary, params });
};

export const getHarvestMachineList = () => {
  return defHttp.get<any>({ url: Api.MachineList });
};

export const getHarvestOperationRecords = (beidouSnList: string[], startDate: string, endDate: string) => {
  return defHttp.get<any>({ 
    url: Api.OperationRecords, 
    params: { 
      beidouSnList: beidouSnList.join(','), 
      startDate, 
      endDate 
    } 
  });
};

export const YIELD_PER_MU = 180;
