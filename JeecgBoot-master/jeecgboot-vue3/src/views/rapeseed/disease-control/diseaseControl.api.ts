import { defHttp } from '/@/utils/http/axios';

enum Api {
  PestDevices = '/youcai/diseaseControl/pestDevices',
  SporeDevices = '/youcai/diseaseControl/sporeDevices',
  MonitorImages = '/youcai/diseaseControl/monitorImages',
  SporeImages = '/youcai/diseaseControl/sporeImages',
  AnalyzeDisease = '/youcai/diseaseControl/analyzeDisease',
  AnalyzeDiseaseByUrl = '/youcai/diseaseControl/analyzeDiseaseByUrl',
  AnalyzeDiseaseBatch = '/youcai/diseaseControl/analyzeDiseaseBatch',
  AnalyzeMonitorBatch = '/youcai/diseaseControl/analyzeMonitorBatch',
  AnalyzeSpore = '/youcai/diseaseControl/analyzeSpore',
  Overview = '/youcai/diseaseControl/overview',
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

export const analyzeDisease = (file: File) => {
  const formData = new FormData();
  formData.append('file', file);
  return defHttp.post<DiseaseAnalysisResponse>({ 
    url: Api.AnalyzeDisease, 
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  });
};

export const analyzeDiseaseByUrl = (imageUrl: string) => {
  return defHttp.post<DiseaseAnalysisResponse>({ 
    url: Api.AnalyzeDiseaseByUrl, 
    params: { imageUrl }
  });
};

export const analyzeDiseaseBatch = (imageUrls: string[]) => {
  return defHttp.post<DiseaseAnalysisResponse>({ 
    url: Api.AnalyzeDiseaseBatch, 
    data: { imageUrls }
  });
};

export const analyzeMonitorBatch = (params: DiseaseControlBaseParams) => {
  return defHttp.post<DiseaseAnalysisResponse>({ 
    url: Api.AnalyzeMonitorBatch, 
    params,
    timeout: 120 * 1000,
  });
};

export const analyzeSpore = (params: DiseaseControlBaseParams) => {
  return defHttp.post<SporeAnalysisResponse>({ 
    url: Api.AnalyzeSpore, 
    params,
    timeout: 120 * 1000,
  });
};

export const getOverview = (params?: { 
  pestDeviceCode?: string; 
  sporeDeviceCode?: string; 
  projectId?: number;
  startDate?: string; 
  endDate?: string 
}) => {
  return defHttp.get<{ 
    monitorImages: ImageInfo[]; 
    sporeImages: ImageInfo[];
    timeRange: string;
    pestDeviceCode: string;
    sporeDeviceCode: string;
  }>({ url: Api.Overview, params });
};
