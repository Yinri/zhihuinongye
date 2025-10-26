import { defHttp } from '/@/utils/http/axios';
import { AxiosRequestConfig } from 'axios';

// 地块信息管理API接口
enum Api {
  // 获取地块信息列表
  PlotInfoList = '/rapeseed/plotInfo/list',
  // 保存地块信息
  PlotInfoSave = '/rapeseed/plotInfo/add',
  // 编辑地块信息
  PlotInfoEdit = '/rapeseed/plotInfo/edit',
  // 删除地块信息
  PlotInfoDelete = '/rapeseed/plotInfo/delete',
  // 批量删除地块信息
  PlotInfoDeleteBatch = '/rapeseed/plotInfo/deleteBatch',
  // 根据ID获取地块信息详情
  PlotInfoGetById = '/rapeseed/plotInfo/queryById',
  // 导出地块信息
  PlotInfoExportXls = '/rapeseed/plotInfo/exportXls',
  // 导入地块信息
  PlotInfoImportExcel = '/rapeseed/plotInfo/importExcel',
}

// 获取地块信息列表
export const getPlotInfoList = (params) => {
  return defHttp.get({ url: Api.PlotInfoList, params });
};

// 保存/更新地块信息
export const savePlotInfo = (params, isUpdate) => {
  if (isUpdate) {
    return defHttp.put({ url: Api.PlotInfoEdit, params });
  } else {
    return defHttp.post({ url: Api.PlotInfoSave, params });
  }
};

// 删除地块信息
export const deletePlotInfo = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.PlotInfoDelete, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

// 批量删除地块信息
export const batchDeletePlotInfo = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.PlotInfoDeleteBatch, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

// 根据ID获取地块信息详情
export const getPlotInfoById = (params) => {
  return defHttp.get({ url: Api.PlotInfoGetById, params });
};

// 导出地块信息
export const getExportUrl = Api.PlotInfoExportXls;
export const getImportUrl = Api.PlotInfoImportExcel;