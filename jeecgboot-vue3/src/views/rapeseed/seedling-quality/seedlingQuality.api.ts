import { defHttp } from '/@/utils/http/axios';

enum Api {
  MonitorImages = '/youcai/diseaseControl/monitorImages',
  AnalyzeSeedlingSubmit = '/youcai/seedlingQuality/analyze/submit',
  AnalyzeSeedlingTask = '/youcai/seedlingQuality/analyze/task',
}

export interface SeedlingBaseParams {
  baseId?: string;
  baseName?: string;
}

export interface ImageInfo {
  imageUrl: string;
  thumbnail?: string;
  dateCreated?: string;
  deviceName?: string;
  videoId?: string;
}

export interface SeedlingIndicator {
  name: string;
  value: string;
  level: string;
  description: string;
}

export interface SeedlingAnalysisResponse {
  seedlingLevel: string;
  growthStage: string;
  confidence: number;
  summary: string;
  evidence: string;
  indicators: SeedlingIndicator[];
  mainProblems: string[];
  managementSuggestions: string[];
  analysisTime?: string;
}

export interface SeedlingAnalysisRequest {
  imageUrls: string[];
  baseName?: string;
}

export interface AiTaskSubmitResponse {
  taskId: string;
  status: string;
  cached: boolean;
}

export interface AiTaskResultResponse<T> {
  taskId: string;
  taskType: string;
  status: string;
  errorMessage?: string;
  cached: boolean;
  createdTime: number;
  finishedTime?: number;
  result?: T;
}

export const getMonitorImages = (params: SeedlingBaseParams) => {
  return defHttp.get<ImageInfo[]>({
    url: Api.MonitorImages,
    params,
    timeout: 30 * 1000,
  });
};

export const submitSeedlingAnalysis = (data: SeedlingAnalysisRequest) => {
  return defHttp.post<AiTaskSubmitResponse>({
    url: Api.AnalyzeSeedlingSubmit,
    data,
    timeout: 30 * 1000,
  });
};

export const getSeedlingAnalysisTask = (taskId: string) => {
  return defHttp.get<AiTaskResultResponse<SeedlingAnalysisResponse>>({
    url: `${Api.AnalyzeSeedlingTask}/${taskId}`,
    timeout: 30 * 1000,
  });
};
