import { defHttp } from '/@/utils/http/axios';
import { AxiosRequestConfig } from 'axios';

enum Api {
  List = '/youcai/workArea/list',
  Save = '/youcai/workArea/save',
  Edit = '/youcai/workArea/edit',
  Delete = '/youcai/workArea/delete',
  BatchDelete = '/youcai/workArea/batchDelete',
  Detail = '/youcai/workArea/queryById',
  Export = '/youcai/workArea/export',
  Import = '/youcai/workArea/importExcel',
  MachineList = '/youcai/sensorInfo/machine/list',
  OperationRecords = '/youcai/sensorInfo/records',
  SyncOperation = '/youcai/sensorInfo/sync',
  TrackData = '/youcai/sensorInfo/track',
  VideoList = '/youcai/sensorInfo/getVideoDevices',
  VideoStream = '/youcai/sensorInfo/getVideoStream',
  AllDevices = '/youcai/sensorInfo/getAllDevices',
}

export const getWorkAreaList = (params?: AxiosRequestConfig) => {
  return defHttp.get({ url: Api.List, params });
};

export const saveWorkArea = (params: any) => {
  return defHttp.post({ url: Api.Save, params });
};

export const editWorkArea = (params: any) => {
  return defHttp.put({ url: Api.Edit, params });
};

export const deleteWorkArea = (id: string, params?: AxiosRequestConfig) => {
  return defHttp.delete({ url: `${Api.Delete}/${id}`, params });
};

export const batchDeleteWorkArea = (ids: string[], params?: AxiosRequestConfig) => {
  return defHttp.delete({ url: Api.BatchDelete, data: { ids } });
};

export const getWorkAreaById = (id: string) => {
  return defHttp.get({ url: `${Api.Detail}/${id}` });
};

export const exportWorkArea = (params?: any) => {
  return defHttp.post({ url: Api.Export, params }, { isTransformResponse: false });
};

export const importWorkArea = (params: any) => {
  return defHttp.uploadFile({ url: Api.Import }, params);
};

export const getMachineList = () => {
  return defHttp.get({ url: Api.MachineList });
};

export const getOperationRecords = (beidouSnList: string[], startDate: string, endDate: string) => {
  return defHttp.get({ 
    url: Api.OperationRecords, 
    params: { 
      beidouSnList: beidouSnList.join(','), 
      startDate, 
      endDate 
    } 
  });
};

export const syncOperationData = (baseId: string, startDate: string, endDate: string) => {
  return defHttp.post({ url: Api.SyncOperation, params: { baseId, startDate, endDate } });
};

export const getTrackData = (beidouSn: string, startDate: string, endDate: string) => {
  return defHttp.get({ url: Api.TrackData, params: { beidouSn, startDate, endDate } });
};

export const getVideoDevices = (baseId: string) => {
  return defHttp.get({ url: Api.VideoList, params: { baseId } });
};

export const getVideoStream = (equipmentCode: string, channelNum: string) => {
  return defHttp.post({ url: `${Api.VideoStream}?equipmentCode=${equipmentCode}&channelNum=${channelNum}` }, { successMessageMode: 'none' });
};

export const getAllDevices = (baseId: string) => {
  return defHttp.get({ url: Api.AllDevices, params: { baseId } });
};
