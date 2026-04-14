// src/api/youcai.ts
import { defHttp } from "/@/utils/http/axios";
import { AxiosRequestConfig } from 'axios';

/**
 * 后端接口地址统一管理
 */
export enum Api {
  OilQuality = "/youcai/youcaiColorQuality/midu",   // 油菜苗密度 
  Density = "/youcai/youcaiColorQuality/color", 
  GetGrowthAdvice ="/youcai/youcaiColorQuality/advice",
  PesticideList = '/youcai/youcaiPesticideInfo/name/list',
  AddPestControl ='/youcai/youcaiColorQuality/add'
    // 油菜颜色指数
}

/**
 * 图片类型 → 接口映射
 */
export const youcaiApiMap: Record<string, string> = {
  oil_quality: Api.OilQuality,
  density: Api.Density,
};

/**
 * Base64 图片上传（JSON 方式）
 */
export const uploadYoucaiImage = (
  url: string,
  base64Image: string,
) => {
  return defHttp.post(
    {
      url:url,
      data: {
        file: base64Image, // Base64 字符串            // 图片类型
      },
      headers: {
        "Content-Type": "application/json",
      },
    },
  );
};

export const addControlRecord = (data: any, config?: AxiosRequestConfig) => {
  return defHttp.post({ url: Api.AddPestControl, data }, config);
}

export const getGrowthAdvice = (analysisResult: any) => {
  return defHttp.post({
    url:Api.GetGrowthAdvice,
    data: analysisResult,
    headers: {
      "Content-Type": "application/json",
    },
  });
};

export const getPesticideList = (params?: AxiosRequestConfig) => {
  return defHttp.get({ url: Api.PesticideList, params });
};


