import { defHttp } from '/@/utils/http/axios';
import { BasePageParams, BaseListResponse } from '/@/api/model/baseModel';

// 风险等级枚举
export enum RiskLevel {
  LOW = 'low',
  MEDIUM = 'medium',
  HIGH = 'high',
}

// 基地信息接口
export interface BaseInfo {
  id: string;
  name: string;
  location: string;
  area: number;
  description?: string;
}

// 地块信息接口
export interface PlotInfo {
  id: string;
  baseId: string;
  name: string;
  area: number;
  location: string;
  cropType: string;
  plantingDate: string;
  growthStage: string;
}

// 风险因子接口
export interface RiskFactor {
  id: string;
  name: string;
  unit: string;
  value: number;
  threshold: number;
  level: RiskLevel;
  description: string;
  icon: string;
  trend: 'up' | 'down' | 'stable';
}

// 预警记录接口
export interface WarningRecord {
  id: string;
  baseId: string;
  plotId: string;
  riskLevel: RiskLevel;
  riskFactors: RiskFactor[];
  warningTime: string;
  warningContent: string;
  status: 'pending' | 'processing' | 'resolved';
  createTime: string;
  updateTime: string;
}

// 历史事件接口
export interface HistoryEvent {
  id: string;
  date: string;
  severity: RiskLevel;
  description: string;
  impact: string;
  measures: string[];
}

// 防控建议接口
export interface PreventionMeasure {
  type: 'emergency' | 'management';
  title: string;
  description: string;
  priority: 'high' | 'medium' | 'low';
  applicableStages: string[];
}

// 图表数据接口
export interface ChartDataPoint {
  time: string;
  value: number;
  level?: RiskLevel;
}

export interface FactorTrendData {
  factorId: string;
  factorName: string;
  data: ChartDataPoint[];
}

// 倒伏风险数据接口
export interface LodgingRiskAssessmentResponse {
  currentRisk: {
    riskLevel: string;
    riskScore: number;
    lodgingProbability: string;
  };
  forecast7Days: {
    maxRiskDate: string;
    dailyRisks: Array<{
      date: string;
      riskScore: number;
      weather: {
        windSpeed: number;
        rainfall: number;
      };
    }>;
  };
  comprehensiveSuggestions: Array<{
    type: string;
    title: string;
    description: string;
  }>;
  calculationTime: string;
}

// 批量获取基地下所有地块倒伏风险数据接口
export interface BatchLodgingRiskAssessmentResponse {
  baseId: string;
  baseName: string;
  plotRisks: LodgingRiskAssessmentResponse[];
  baseStatistics: {
    totalPlots: number;
    lowRiskPlots: number;
    mediumLowRiskPlots: number;
    mediumRiskPlots: number;
    highRiskPlots: number;
    extremeRiskPlots: number;
    averageRiskScore: number;
  };
  calculationTime: string;
}

// 获取基地列表
export const getBaseList = () => {
  return defHttp.get<BaseInfo[]>({
    url: '/rapeseed/base/list',
  });
};

// 获取地块列表
export const getPlotList = (baseId: string) => {
  return defHttp.get<PlotInfo[]>({
    url: `/rapeseed/plot/list/${baseId}`,
  });
};

// 获取风险因子数据
export const getRiskFactors = (params: { baseId: string; plotId?: string; timeRange?: [string, string] }) => {
  return defHttp.get<RiskFactor[]>({
    url: '/rapeseed/risk/factors',
    params,
  });
};

// 获取倒伏风险数据
export const getLodgingRiskData = (params: { 
  baseId: string; 
  plotId?: string; 
  timeRange?: [string, string] 
}) => {
  return defHttp.get<{
    currentRiskLevel: RiskLevel;
    riskProbability: number;
    highRiskPeriod: string;
    riskFactors: RiskFactor[];
    trendData: ChartDataPoint[];
    heatmapData: number[][];
    comparisonData: ChartDataPoint[];
  }>({
    url: '/rapeseed/risk/lodging',
    params,
  });
};

// 获取预警记录列表
export const getWarningRecordList = (params: BasePageParams & { 
  baseId?: string; 
  plotId?: string; 
  riskLevel?: RiskLevel;
  status?: string;
}) => {
  return defHttp.get<BaseListResponse<WarningRecord>>({
    url: '/rapeseed/warning/record/list',
    params,
  });
};

// 获取历史事件
export const getHistoryEvents = (params: { 
  baseId: string; 
  plotId?: string; 
  timeRange?: [string, string] 
}) => {
  return defHttp.get<HistoryEvent[]>({
    url: '/rapeseed/history/events',
    params,
  });
};

// 获取防控建议
export const getPreventionMeasures = (params: { 
  riskLevel: RiskLevel; 
  growthStage: string; 
}) => {
  return defHttp.get<{
    emergency: PreventionMeasure[];
    management: PreventionMeasure[];
  }>({
    url: '/rapeseed/prevention/measures',
    params,
  });
};

// 获取因子趋势数据
export const getFactorTrendData = (params: {
  baseId: string;
  plotId?: string;
  factorIds: string[];
  timeRange?: [string, string];
}) => {
  return defHttp.get<FactorTrendData[]>({
    url: '/rapeseed/factor/trend',
    params,
  });
};

// 创建预警记录
export const createWarningRecord = (data: Omit<WarningRecord, 'id' | 'createTime' | 'updateTime'>) => {
  return defHttp.post<WarningRecord>({
    url: '/rapeseed/warning/record/create',
    data,
  });
};

// 更新预警记录
export const updateWarningRecord = (id: string, data: Partial<WarningRecord>) => {
  return defHttp.put<WarningRecord>({
    url: `/rapeseed/warning/record/update/${id}`,
    data,
  });
};

// 删除预警记录
export const deleteWarningRecord = (id: string) => {
  return defHttp.delete<boolean>({
    url: `/rapeseed/warning/record/delete/${id}`,
  });
};

// 批量获取基地下所有地块倒伏风险数据
export const getBatchLodgingRiskDataByBaseId = (baseId: string) => {
  return defHttp.get<BatchLodgingRiskAssessmentResponse>({
    url: `/youcai/youcaiLodgingRiskWarning/batchRiskData/${baseId}`,
  });
};

// 根据地块ID获取倒伏风险数据
export const getLodgingRiskDataById = (plotId: string) => {
  return defHttp.get<LodgingRiskAssessmentResponse>({
    url: `/youcai/lodgingRisk/riskData/${plotId}`,
  });
};