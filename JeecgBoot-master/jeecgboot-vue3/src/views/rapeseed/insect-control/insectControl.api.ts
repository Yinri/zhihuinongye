import { defHttp } from '/@/utils/http/axios';

const ANALYZE_TIMEOUT = 120 * 1000;

enum Api {
  PestImages = '/youcai/youcaiPestControl/images',
  AIAnalysisSubmit = '/youcai/youcaiPestControl/aiAnalysis/submit',
  AIAnalysisTask = '/youcai/youcaiPestControl/aiAnalysis/task',
  TrendSuggest = '/youcai/youcaiPestControl/trendSuggest',
}

export interface PestImageQueryParams {
  baseName: string;
  StarDate: string;
  EndDate: string;
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

export interface PestAnalysisRequest {
  base_id?: string;
  base_name?: string;
  pest_data: Array<{
    analysis_time?: string;
    insects?: Record<string, number>;
  }>;
  image_urls: string[];
}

export interface PestTrendSuggestParams {
  baseId?: string;
  baseName?: string;
}

export const submitPestAnalysisTask = (data: PestAnalysisRequest) => {
  return defHttp.post<AiTaskSubmitResponse>({
    url: Api.AIAnalysisSubmit,
    data,
    timeout: 30 * 1000,
  });
};

export const getPestAnalysisTask = (taskId: string) => {
  return defHttp.get<AiTaskResultResponse<string>>({
    url: `${Api.AIAnalysisTask}/${taskId}`,
    timeout: ANALYZE_TIMEOUT,
  });
};
/**
 * 获取当前基地指定时间范围内的虫情图片明细。
 */
export const getPestImages = (params: PestImageQueryParams) => {
  return defHttp.get({
    url: Api.PestImages,
    params,
    timeout: 30 * 1000,
  });
};

/**
 * 获取后台定时任务预生成的虫情趋势与防治建议。
 */
export const getPestTrendSuggest = (params: PestTrendSuggestParams) => {
  return defHttp.get<string>({
    url: Api.TrendSuggest,
    params,
    timeout: 30 * 1000,
  });
};
