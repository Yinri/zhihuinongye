import { defHttp } from '/@/utils/http/axios';

// 气象传感器数据接口
export interface WeatherSensorData {
  temperature: number;      // 温度(℃)
  humidity: number;        // 湿度(%)
  airPressure: number;     // 气压(hPa)
  windSpeed: number;       // 风速(m/s)
  windDirection: number;   // 风向(°)
  rainfall: number;        // 降雨量(mm)
  lightIntensity: number;  // 光照强度(lux)
  co2Level: number;        // CO2浓度(ppm)
  uvIndex: number;         // 紫外线指数
  updateTime: string;      // 数据更新时间
}

// 传感器信息接口
export interface SensorInfo {
  sensorId: string;
  sensorName: string;
  sensorType: string;
  location: {
    longitude: number;
    latitude: number;
  };
  status: 'online' | 'offline';
  lastUpdateTime: string;
}

// 获取基地气象传感器实时数据
export const getWeatherSensorData = (baseId: string) => {
  return defHttp.get<WeatherSensorData>({
    url: '/youcai/sensorInfo/getWeatherSensorData',
    params: { baseId },
  });
};

// 获取基地传感器列表
export const getBaseSensorList = (baseId: string) => {
  return defHttp.get<SensorInfo[]>({
    url: '/youcai/sensorInfo/getBaseSensorList',
    params: { baseId },
  });
};

// 获取传感器历史数据
export const getSensorHistoryData = (params: {
  sensorId: string;
  startDate: string;
  endDate: string;
}) => {
  return defHttp.get<WeatherSensorData[]>({
    url: '/youcai/sensorInfo/getSensorHistoryData',
    params,
  });
};