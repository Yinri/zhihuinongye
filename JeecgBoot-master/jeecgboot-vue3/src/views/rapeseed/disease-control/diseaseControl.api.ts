import { defHttp } from '/@/utils/http/axios';

enum Api {
  MonitorImages = '/youcai/diseaseControl/monitorImages',
  SporeImages = '/youcai/diseaseControl/sporeImages',
  AnalyzeDiseaseUrls = '/youcai/diseaseControl/analyze',
  AnalyzeDiseaseSubmit = '/youcai/diseaseControl/analyze/submit',
  AnalyzeDiseaseTask = '/youcai/diseaseControl/analyze/task',
  AnalyzeMonitorBatch = '/youcai/diseaseControl/analyzeMonitorBatch',
  AnalyzeMonitorBatchSubmit = '/youcai/diseaseControl/analyzeMonitorBatch/submit',
  AnalyzeMonitorBatchTask = '/youcai/diseaseControl/analyzeMonitorBatch/task',
  AnalyzeSpore = '/youcai/diseaseControl/analyzeSpore',
  AnalyzeSporeSubmit = '/youcai/diseaseControl/analyzeSpore/submit',
  AnalyzeSporeTask = '/youcai/diseaseControl/analyzeSpore/task',
}

export interface DeviceInfo {
  deviceCode: string;
  deviceName: string;
  lat: string;
  lng: string;
  state: number;
}

export interface ImageInfo {
  imageUrl: string;
  thumbnail: string;
  dateCreated: string;
}

export interface DiseaseAnalysisResponse {
  diseaseName: string;
  confidence: number;
  description: string;
  severity: string;
  preventionMeasures: string[];
  recommendedPesticides: PesticideRecommendation[];
  bestPreventionTime: string;
  summary: string;
}

export interface PesticideRecommendation {
  name: string;
  dosage: string;
  usage: string;
  precautions: string;
}

export interface SporeAnalysisResponse {
  warningLevel: string;
  diseaseWarnings: DiseaseWarning[];
  medicationGuides: MedicationGuide[];
  statistics: SporeStatistics;
  summary: string;
}

export interface DiseaseWarning {
  diseaseName: string;
  riskLevel: string;
  description: string;
  suggestion: string;
}

export interface MedicationGuide {
  pesticideName: string;
  dosage: string;
  timing: string;
  method: string;
  precautions: string;
}

export interface SporeStatistics {
  totalImages: number;
  analyzedImages: number;
  sporeTypes: number;
  timeRange: string;
  trend: string;
}

export interface DiseaseControlBaseParams {
  baseId?: string;
  baseName?: string;
}

export interface SporeAnalysisRequest {
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

export const getMonitorImages = (params: DiseaseControlBaseParams) => {
  return defHttp.get<ImageInfo[]>({ 
    url: Api.MonitorImages, 
    params,
    timeout: 30 * 1000,
  });
};

export const getSporeImages = (params: DiseaseControlBaseParams) => {
  return defHttp.get<ImageInfo[]>({ 
    url: Api.SporeImages, 
    params,
    timeout: 30 * 1000,
  });
};

export const analyzeDiseaseUrls = (imageUrls: string[], baseName?: string) => {
  return defHttp.post<DiseaseAnalysisResponse>({ 
    url: Api.AnalyzeDiseaseUrls, 
    data: { imageUrls, baseName },
    timeout: 120 * 1000,
  });
};

export const submitAnalyzeDisease = (imageUrls: string[], baseName?: string) => {
  return defHttp.post<AiTaskSubmitResponse>({
    url: Api.AnalyzeDiseaseSubmit,
    data: { imageUrls, baseName },
    timeout: 30 * 1000,
  });
};

export const getAnalyzeDiseaseTask = (taskId: string) => {
  return defHttp.get<AiTaskResultResponse<DiseaseAnalysisResponse>>({
    url: `${Api.AnalyzeDiseaseTask}/${taskId}`,
    timeout: 30 * 1000,
  });
};

export const analyzeMonitorBatch = (params: DiseaseControlBaseParams) => {
  const query = new URLSearchParams();
  if (params.baseId) {
    query.set('baseId', params.baseId);
  }
  return defHttp.post<DiseaseAnalysisResponse>({
    url: `${Api.AnalyzeMonitorBatch}?${query.toString()}`,
    timeout: 120 * 1000,
  });
};

export const submitAnalyzeMonitorBatch = (params: DiseaseControlBaseParams) => {
  const query = new URLSearchParams();
  if (params.baseId) {
    query.set('baseId', params.baseId);
  }
  return defHttp.post<AiTaskSubmitResponse>({
    url: `${Api.AnalyzeMonitorBatchSubmit}?${query.toString()}`,
    timeout: 30 * 1000,
  });
};

export const getAnalyzeMonitorBatchTask = (taskId: string) => {
  return defHttp.get<AiTaskResultResponse<DiseaseAnalysisResponse>>({
    url: `${Api.AnalyzeMonitorBatchTask}/${taskId}`,
    timeout: 30 * 1000,
  });
};

export const analyzeSpore = (data: SporeAnalysisRequest) => {
  return defHttp.post<SporeAnalysisResponse>({
    url: Api.AnalyzeSpore,
    data,
    timeout: 120 * 1000,
  });
};

export const submitAnalyzeSpore = (data: SporeAnalysisRequest) => {
  return defHttp.post<AiTaskSubmitResponse>({
    url: Api.AnalyzeSporeSubmit,
    data,
    timeout: 30 * 1000,
  });
};

export const getAnalyzeSporeTask = (taskId: string) => {
  return defHttp.get<AiTaskResultResponse<SporeAnalysisResponse>>({
    url: `${Api.AnalyzeSporeTask}/${taskId}`,
    timeout: 30 * 1000,
  });
};
