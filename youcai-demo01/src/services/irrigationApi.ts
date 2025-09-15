import axios from 'axios';
import type { AxiosInstance, AxiosResponse, CancelToken } from 'axios';

const API_BASE_URL = 'http://localhost:8000/api/irrigation';
const DATA_API_BASE_URL = 'http://localhost:8000/api';

// 定义数据接口
export interface ReportData {
  id: string;
  createdAt: string;
  content: string;
  // 根据实际API响应结构补充字段
}

export interface TrendData {
  // 根据实际API响应结构定义
  [key: string]: any;
}

export interface AnalysisData {
  // 根据实际API响应结构定义
  [key: string]: any;
}

export interface GrowthStage {
  stage: string;
  startDate: string;
  endDate: string;
  recommendations: string[];
}

export interface GrowthStagesData {
  stages: GrowthStage[];
}

// 创建API客户端实例
const createApiClient = (baseURL: string): AxiosInstance => {
  const client = axios.create({
    baseURL,
    timeout: 10000,
    // 移除显式Content-Type设置，让Axios自动处理文件上传类型
  });

  // 响应拦截器 - 统一错误处理
  client.interceptors.response.use(
    (response: AxiosResponse) => response.data,
    (error) => {
      console.error('API Error详情:', JSON.stringify(error.response, null, 2));
      return Promise.reject(error);
    }
  );

  return client;
};

const apiClient: AxiosInstance = createApiClient(API_BASE_URL);
const dataApiClient: AxiosInstance = createApiClient(DATA_API_BASE_URL);

// 灌溉相关API方法
export default {
  /**
   * 上传灌溉相关数据文件
   * @param ndviFile NDVI数据文件
   * @param soilFile 土壤湿度数据文件
   * @param weatherFile 气象数据文件
   * @param onUploadProgress 上传进度回调函数
   */
  uploadIrrigationData(
    ndviFile: File, 
    soilFile: File, 
    weatherFile: File, 
    onUploadProgress?: (progressEvent: any) => void
  ): Promise<any> {
    // 创建FormData对象
    const formData = new FormData();
    formData.append('ndvi_file', ndviFile);
    formData.append('soil_file', soilFile);
    formData.append('weather_file', weatherFile);
    
    return dataApiClient.post('/irrigation/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      onUploadProgress,
    });
  },

  /**
   * 获取灌溉报告
   * @param cancelToken 取消令牌
   */
  getReport(cancelToken?: CancelToken): Promise<ReportData> {
    return apiClient.get<ReportData>('/report', { cancelToken }).then(response => response.data);
  },

  /**
   * 获取趋势数据
   * @param cancelToken 取消令牌
   */
  getTrend(cancelToken?: CancelToken): Promise<TrendData> {
    return apiClient.get<TrendData>('/trend', { cancelToken });
  },

  /**
   * 获取分析数据
   * @param cancelToken 取消令牌
   */
  getAnalysis(cancelToken?: CancelToken): Promise<AnalysisData> {
    return apiClient.get<AnalysisData>('/analysis', { cancelToken });
  },

  /**
   * 获取生长阶段信息
   * @param cancelToken 取消令牌
   */
  getGrowthStages(cancelToken?: CancelToken): Promise<GrowthStagesData> {
    return apiClient.get<GrowthStagesData>('/growth-stages', { cancelToken }).then(response => response.data);
  }
};
