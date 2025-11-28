import { defHttp } from '/@/utils/http/axios';

enum Api {
  WarningList = '/youcai/farmingWarning/list',
  UpdateStatus = '/youcai/farmingWarning/updateStatus',
  Statistics = '/youcai/farmingWarning/statistics',
  GenerateAll = '/youcai/farmingWarning/generateAll',
  GeneratePlot = '/youcai/farmingWarning/generate',
}

/**
 * 预警查询参数
 */
export interface WarningQueryParams {
  warningType?: 'lodging' | 'disease' | 'pest';
  level?: 'high' | 'medium' | 'low';
  warningStatus?: 'pending' | 'processing' | 'resolved' | 'ignored';
  plotId?: string;
  baseId?: string;
  limit?: number;
  onlyValid?: boolean;
}

/**
 * 预警信息
 */
export interface FarmingWarning {
  id: string;
  warningType: 'lodging' | 'disease' | 'pest';
  level: 'high' | 'medium' | 'low';
  title: string;
  description: string;
  plotId: string;
  plotName: string;
  baseId?: string;
  baseName?: string;
  warningDate: string;
  warningStatus: string;
  recommendation: string;
  riskScore?: number;
  handler?: string;
  handleTime?: string;
  handleRemark?: string;
  isExpired?: boolean;
  warningData?: any;
}

/**
 * 预警统计信息
 */
export interface WarningStatistics {
  totalCount: number;
  highCount: number;
  mediumCount: number;
  lowCount: number;
  lodgingCount: number;
  diseaseCount: number;
  pestCount: number;
  pendingCount: number;
  processingCount: number;
  resolvedCount: number;
  typeDistribution: Record<string, number>;
  levelDistribution: Record<string, number>;
  statusDistribution: Record<string, number>;
}

/**
 * 获取预警列表
 */
export const getWarningList = (params?: WarningQueryParams) => {
  return defHttp.get<FarmingWarning[]>({ url: Api.WarningList, params });
};

/**
 * 更新预警状态
 */
export const updateWarningStatus = (
  warningId: string,
  status: string,
  handler?: string,
  remark?: string
) => {
  return defHttp.put({
    url: Api.UpdateStatus,
    params: { warningId, status, handler, remark },
    data: {}, // 提供空data对象，确保params作为URL参数传递
  });
};

/**
 * 获取预警统计
 */
export const getWarningStatistics = () => {
  return defHttp.get<WarningStatistics>({ url: Api.Statistics });
};

/**
 * 手动触发生成所有预警（测试用）
 */
export const generateAllWarnings = () => {
  return defHttp.post({ url: Api.GenerateAll });
};

/**
 * 手动触发生成单个地块预警（测试用）
 */
export const generatePlotWarning = (plotId: string) => {
  return defHttp.post({ url: `${Api.GeneratePlot}/${plotId}` });
};
