import { defHttp } from '/@/utils/http/axios';
import { AxiosRequestConfig } from 'axios';

// 生产计划管理API接口
enum Api {
  // 获取生产计划列表
  ProductionPlanList = '/youcai/productionPlan/list',
  // 保存生产计划
  ProductionPlanSave = '/youcai/productionPlan/add',
  // 编辑生产计划
  ProductionPlanEdit = '/youcai/productionPlan/edit',
  // 删除生产计划
  ProductionPlanDelete = '/youcai/productionPlan/delete',
  // 批量删除生产计划
  ProductionPlanDeleteBatch = '/youcai/productionPlan/deleteBatch',
  // 根据ID获取生产计划详情
  ProductionPlanGetById = '/youcai/productionPlan/queryById',
  // 导出生产计划
  ProductionPlanExportXls = '/youcai/productionPlan/exportXls',
  // 导入生产计划
  ProductionPlanImportExcel = '/youcai/productionPlan/importExcel',
  // 根据基地ID获取生产计划列表
  ProductionPlanGetByBaseId = '/youcai/productionPlan/listByBaseId',
}

// 获取生产计划列表
export const getProductionPlanList = (params) => {
  return defHttp.get({ url: Api.ProductionPlanList, params });
};

// 保存/更新生产计划
export const saveProductionPlan = (params, isUpdate) => {
  if (isUpdate) {
    return defHttp.put({ url: Api.ProductionPlanEdit, params });
  } else {
    return defHttp.post({ url: Api.ProductionPlanSave, params });
  }
};

// 删除生产计划
export const deleteProductionPlan = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.ProductionPlanDelete, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

// 批量删除生产计划
export const batchDeleteProductionPlan = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.ProductionPlanDeleteBatch, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

// 根据ID获取生产计划详情
export const getProductionPlanById = (params) => {
  return defHttp.get({ url: Api.ProductionPlanGetById, params });
};

// 导出生产计划
export const getExportUrl = Api.ProductionPlanExportXls;
export const getImportUrl = Api.ProductionPlanImportExcel;

// 根据基地ID获取生产计划列表
export const getProductionPlanByBaseId = (params) => {
  return defHttp.get({ url: Api.ProductionPlanGetByBaseId, params });
};
