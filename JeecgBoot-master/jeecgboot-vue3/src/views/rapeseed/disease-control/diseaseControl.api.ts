import { defHttp } from '/@/utils/http/axios';
import { AxiosRequestConfig } from 'axios';

// 枚举API地址
enum Api {
  GetFirstDisease = '/youcai/youcaiDiseaseWarnings/allImages',
}
// 获取病害防控列表
export const getFirstDisease = (config?: AxiosRequestConfig) => {
  return defHttp.get(
    { url: Api.GetFirstDisease },
    config,
  );
};
