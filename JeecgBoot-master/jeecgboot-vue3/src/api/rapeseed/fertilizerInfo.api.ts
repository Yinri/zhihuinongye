import { defHttp } from '/@/utils/http/axios';
import { AxiosResponse } from 'axios';

// 肥料信息表接口
enum Api {
  // 获取肥料信息列表
  List = '/youcai/youcaiFertilizerInfo/list',
  // 保存肥料信息
  Save = '/youcai/youcaiFertilizerInfo/add',
  // 编辑肥料信息
  Edit = '/youcai/youcaiFertilizerInfo/edit',
  // 删除肥料信息
  Delete = '/youcai/youcaiFertilizerInfo/delete',
  // 批量删除肥料信息
  DeleteBatch = '/youcai/youcaiFertilizerInfo/deleteBatch',
  // 通过id查询肥料信息
  QueryById = '/youcai/youcaiFertilizerInfo/queryById',
  // 导出excel
  ExportXls = '/youcai/youcaiFertilizerInfo/exportXls',
  // 导入excel
  ImportExcel = '/youcai/youcaiFertilizerInfo/importExcel',
}

// 获取肥料信息列表
export const getFertilizerInfoList = (params) => defHttp.get<AxiosResponse>({ url: Api.List, params });

// 保存肥料信息
export const saveFertilizerInfo = (params) => defHttp.post<AxiosResponse>({ url: Api.Save, params });

// 编辑肥料信息
export const editFertilizerInfo = (params) => defHttp.put<AxiosResponse>({ url: Api.Edit, params });

// 删除肥料信息
export const deleteFertilizerInfo = (params) => defHttp.delete<AxiosResponse>({ url: Api.Delete, params });

// 批量删除肥料信息
export const deleteBatchFertilizerInfo = (params) => defHttp.delete<AxiosResponse>({ url: Api.DeleteBatch, params });

// 通过id查询肥料信息
export const getFertilizerInfoById = (params) => defHttp.get<AxiosResponse>({ url: Api.QueryById, params });

// 导出excel
export const exportFertilizerInfoXls = (params) => defHttp.download({ url: Api.ExportXls, params });

// 导入excel
export const importFertilizerInfoExcel = (params) => defHttp.uploadFile({ url: Api.ImportExcel, params });