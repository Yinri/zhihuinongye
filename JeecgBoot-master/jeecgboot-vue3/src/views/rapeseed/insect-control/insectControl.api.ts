import { defHttp } from '/@/utils/http/axios';

const ANALYZE_TIMEOUT = 120 * 1000;

enum Api {
  PestImages = '/youcai/youcaiPestControl/images',
  AIAnalysisSubmit = '/youcai/youcaiPestControl/aiAnalysis/submit',
  AIAnalysisTask = '/youcai/youcaiPestControl/aiAnalysis/task',
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

export const submitPestAnalysisTask = (data: any) => {
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
