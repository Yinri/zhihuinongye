import { defHttp } from '/@/utils/http/axios';
import { AxiosRequestConfig } from 'axios';
import { getBaseInfoList } from '../base-info/baseInfo.api';
import { getPlotInfoList } from '../plot-info/plotInfo.api';

enum Api {
  // 农事记录列表
  GetFarmingRecordsList = '/youcai/farmingRecords/list',
  // 保存农事记录
  SaveFarmingRecords = '/youcai/farmingRecords/add',
  // 编辑农事记录
  EditFarmingRecords = '/youcai/farmingRecords/edit',
  // 删除农事记录
  DeleteFarmingRecords = '/youcai/farmingRecords/delete',
  // 批量删除农事记录
  BatchDeleteFarmingRecords = '/youcai/farmingRecords/deleteBatch',
  // 根据ID查询农事记录
  GetFarmingRecordsById = '/youcai/farmingRecords/queryById',
  // 导出农事记录
  ExportFarmingRecords = '/youcai/farmingRecords/exportXls',
  // 导入农事记录
  ImportFarmingRecords = '/youcai/farmingRecords/importExcel',  
}

/**
 * 获取农事记录列表
 * @param params 查询参数
 */
export const getFarmingRecordsList = async (params?: any) => {
  // 获取农事记录数据
  const farmingRecordsData = await defHttp.get<any>({ url: Api.GetFarmingRecordsList, params });
  
  // 如果没有数据，直接返回
  if (!farmingRecordsData || !farmingRecordsData.records || farmingRecordsData.records.length === 0) {
    return farmingRecordsData;
  }
  
  // 获取基地信息列表
  const baseInfoData = await getBaseInfoList({ page: 1, pageSize: 1000 });
  const baseMap = new Map();
  if (baseInfoData && baseInfoData.records) {
    baseInfoData.records.forEach(base => {
      baseMap.set(base.id, base.baseName);
    });
  }
  
  // 获取地块信息列表
  const plotInfoData = await getPlotInfoList({ page: 1, pageSize: 1000 });
  const plotMap = new Map();
  if (plotInfoData && plotInfoData.records) {
    plotInfoData.records.forEach(plot => {
      plotMap.set(plot.id, plot.plotName);
    });
  }
  
  // 为农事记录添加基地名称和地块名称
  farmingRecordsData.records.forEach(record => {
    if (record.baseId && baseMap.has(record.baseId)) {
      record.baseName = baseMap.get(record.baseId);
    }
    if (record.plotId && plotMap.has(record.plotId)) {
      record.plotName = plotMap.get(record.plotId);
    }
  });
  
  return farmingRecordsData;
};

/**
 * 保存农事记录
 * @param params 参数
 */
export const saveFarmingRecords = (params?: any) => {
  return defHttp.post<any>({ url: Api.SaveFarmingRecords, params });
};

/**
 * 编辑农事记录
 * @param params 参数
 */
export const editFarmingRecords = (params?: any) => {
  return defHttp.put<any>({ url: Api.EditFarmingRecords, params });
};

/**
 * 删除农事记录
 * @param params 参数
 */
export const deleteFarmingRecords = (params?: any) => {
  return defHttp.delete<any>({ url: Api.DeleteFarmingRecords, params }, { joinParamsToUrl: true });
};

/**
 * 批量删除农事记录
 * @param params 参数
 */
export const deleteBatch = (params?: any) => {
  return defHttp.delete<any>({ url: Api.BatchDeleteFarmingRecords, params }, { joinParamsToUrl: true });
};

/**
 * 根据ID获取农事记录详情
 * @param params 参数
 */
export const getFarmingRecordsById = (params?: any) => {
  return defHttp.get<any>({ url: Api.GetFarmingRecordsById, params });
};

/**
 * 导出农事记录
 * @param params 参数
 */
export const exportFarmingRecords = (params?: any) => {
  return defHttp.download<any>(
    {
      url: Api.ExportFarmingRecords,
      params,
    },
    '农事记录.xlsx'
  );
};

/**
 * 导入农事记录
 * @param params 参数
 */
export const importFarmingRecords = (params?: any, config?: AxiosRequestConfig) => {
  return defHttp.uploadFile<any>(
    {
      url: Api.ImportFarmingRecords,
    },
    params,
    config
  );
};