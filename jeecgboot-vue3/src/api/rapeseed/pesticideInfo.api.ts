import { defHttp } from '/@/utils/http/axios';
import { AxiosResponse } from 'axios';

// 农药信息表接口
enum Api {
  // 获取农药信息列表
  List = '/youcai/youcaiPesticideInfo/list',
  // 保存农药信息
  Save = '/youcai/youcaiPesticideInfo/add',
  // 编辑农药信息
  Edit = '/youcai/youcaiPesticideInfo/edit',
  // 删除农药信息
  Delete = '/youcai/youcaiPesticideInfo/delete',
  // 批量删除农药信息
  DeleteBatch = '/youcai/youcaiPesticideInfo/deleteBatch',
  // 通过id查询农药信息
  QueryById = '/youcai/youcaiPesticideInfo/queryById',
  // 导出excel
  ExportXls = '/youcai/youcaiPesticideInfo/exportXls',
  // 导入excel
  ImportExcel = '/youcai/youcaiPesticideInfo/importExcel',
}

// 获取农药信息列表
export const getPesticideInfoList = (params) => defHttp.get<AxiosResponse>({ url: Api.List, params });

// 保存农药信息
export const savePesticideInfo = (params) => defHttp.post<AxiosResponse>({ url: Api.Save, params });

// 编辑农药信息
export const editPesticideInfo = (params) => defHttp.put<AxiosResponse>({ url: Api.Edit, params });

// 删除农药信息
export const deletePesticideInfo = (params) => defHttp.delete<AxiosResponse>({ url: Api.Delete, params });

// 批量删除农药信息
export const deleteBatchPesticideInfo = (params) => defHttp.delete<AxiosResponse>({ url: Api.DeleteBatch, params });

// 通过id查询农药信息
export const getPesticideInfoById = (params) => defHttp.get<AxiosResponse>({ url: Api.QueryById, params });

// 导出excel
export const exportPesticideInfoXls = (params) => defHttp.download({ url: Api.ExportXls, params });

// 导入excel
export const importPesticideInfoExcel = (params) => defHttp.uploadFile({ url: Api.ImportExcel, params });