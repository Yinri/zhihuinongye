import { defHttp } from '/@/utils/http/axios';
import { AxiosRequestConfig } from 'axios';

// 枚举API地址
enum Api {
  GetFirstDisease = '/youcai/youcaiDiseaseWarnings/allImages',
  PesticideList = '/youcai/youcaiPesticideInfo/name/list',
  AddPestControl = '/youcai/youcaiDiseaseWarnings/add',
  QueryHistory = '/youcai/youcaiDiseaseWarnings/history',
  UploadDiseaseImage = '/youcai/youcaiDiseaseWarnings/upload',
  DiseaseAnalysis = '/youcai/youcaiDiseaseWarnings/diseaseAnalysis', // 新增 LLM 分析

}
// 获取病害防控列表
export const getFirstDisease = (config?: AxiosRequestConfig) => {
  return defHttp.get(
    { url: Api.GetFirstDisease },
    config,
  );
};

export const getPesticideList = (params?: AxiosRequestConfig) => {
  return defHttp.get({ url: Api.PesticideList, params });
};
export const addPestControlRecord = (data: any, config?: AxiosRequestConfig) => {
  return defHttp.post({ url: Api.AddPestControl, data }, config);
}
export const queryHistoryByTimeRange = (params) => {
  return defHttp.get({
    url: Api.QueryHistory,
    params: params
  });
};
export const uploadDiseaseImage = (base64Image: string) => {
  return defHttp.post(
    {
      url: Api.UploadDiseaseImage,
      data: {
        file: base64Image, // 👈 JSON 方式发送 Base64
      },
      headers: {
        "Content-Type": "application/json",
      },
    },
    {
      isTransformResponse: false,
    }
  );
};
export const diseaseAnalysis = (disease: string, config?: AxiosRequestConfig) => {
  return defHttp.post(
    {
      url: Api.DiseaseAnalysis,
      data: { disease },
      headers: {
        "Content-Type": "application/json",
      },
    },
    config
  );
};


