package org.jeecg.modules.youcai.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.youcai.dto.UnifiedDeviceDto;
import org.jeecg.modules.youcai.dto.WeatherSensorDataDTO;
import org.jeecg.modules.youcai.entity.YoucaiSensorInfo;
import org.jeecg.modules.youcai.entity.iotEntity.ApiResponse;
import org.jeecg.modules.youcai.entity.iotEntity.sensor.SensorListRequest;
import org.jeecg.modules.youcai.entity.iotEntity.sensor.SensorRealTimeData;
import org.jeecg.modules.youcai.mapper.YoucaiSensorInfoMapper;
import org.jeecg.modules.youcai.service.IYoucaiSensorInfoService;
import org.jeecg.modules.youcai.util.IoTApiUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @Description: 传感器信息表
 * @Author: jeecg-boot
 * @Date:   2025-11-10
 * @Version: V1.0
 */
@Service
@Slf4j
public class YoucaiSensorInfoServiceImpl extends ServiceImpl<YoucaiSensorInfoMapper, YoucaiSensorInfo> implements IYoucaiSensorInfoService {
    
    @Autowired
    private IoTApiUtil ioTApiUtil;
    
    @Override
    public WeatherSensorDataDTO getWeatherSensorData(String baseId) {
        try {
            log.info("获取基地ID {} 的气象传感器实时数据", baseId);
            
            // 1. 查询基地下的气象传感器
            QueryWrapper<YoucaiSensorInfo> queryWrapper = new QueryWrapper<>();
            // queryWrapper.eq("base_id", baseId);
            queryWrapper.eq("sensor_type_id", 1); // 1=气象传感器
            queryWrapper.last("LIMIT 1"); // 只取第一个气象传感器
            
            YoucaiSensorInfo sensorInfo = this.getOne(queryWrapper);
            if (sensorInfo == null) {
                log.warn("基地 {} 下未找到气象传感器", baseId);
                return null;
            }
            
            // 2. 获取传感器实时数据
            Object sensorData = getSensorRealTimeDataRaw(sensorInfo.getSensorSerial());
            if (sensorData == null) {
                log.warn("获取传感器 {} 实时数据失败", sensorInfo.getSensorSerial());
                return null;
            }
            
            // 3. 解析数据并构建DTO
            WeatherSensorDataDTO weatherData = parseWeatherData(sensorData, sensorInfo);
            log.info("成功获取气象传感器数据: {}", weatherData);
            
            return weatherData;
        } catch (Exception e) {
            log.error("获取气象传感器数据失败", e);
            return null;
        }
    }
    
    @Override
    public List<YoucaiSensorInfo> getBaseSensorList(String baseId) {
        try {
            log.info("获取基地ID {} 的传感器列表", baseId);
            
            QueryWrapper<YoucaiSensorInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("base_id", baseId);
            queryWrapper.orderByAsc("sensor_type", "sensor_name");
            
            List<YoucaiSensorInfo> sensorList = this.list(queryWrapper);
            log.info("基地 {} 共有 {} 个传感器", baseId, sensorList.size());
            
            return sensorList;
        } catch (Exception e) {
            log.error("获取基地传感器列表失败", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<WeatherSensorDataDTO> getSensorHistoryData(String sensorId, String startDate, String endDate) {
        try {
            log.info("获取传感器ID {} 在 {} 至 {} 的历史数据", sensorId, startDate, endDate);
            
            // 1. 查询传感器信息
            YoucaiSensorInfo sensorInfo = this.getById(sensorId);
            if (sensorInfo == null) {
                log.warn("未找到传感器ID为 {} 的传感器", sensorId);
                return new ArrayList<>();
            }
            
            // 2. 获取历史数据（这里需要根据实际API实现）
            // 目前IoTApiUtil中没有获取历史数据的方法，这里先返回空列表
            // 实际项目中需要实现历史数据获取逻辑
            log.warn("获取历史数据功能尚未实现");
            return new ArrayList<>();
            
        } catch (Exception e) {
            log.error("获取传感器历史数据失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public Result<List<UnifiedDeviceDto>> getAllDevices(String baseId) {
        List<UnifiedDeviceDto> result = new ArrayList<>();

        //这里根据baseId查询项目ID
        Integer projectId = 229;

        try {
            // 1. 虫情设备 (eqType = 1)
            ApiResponse pestResponse = ioTApiUtil.getDeviceList(projectId, 1).block();
            if (pestResponse != null && pestResponse.getCode() == 1 && pestResponse.getData() != null) {
                List<UnifiedDeviceDto> pestDevices = parseDeviceData(pestResponse.getData(), "虫情设备");
                result.addAll(pestDevices);
                log.info("获取到 {} 个虫情设备", pestDevices.size());
            }
            
            // 2. 孢子仪设备 (eqType = 2)
            ApiResponse sporeResponse = ioTApiUtil.getDeviceList(projectId, 2).block();
            if (sporeResponse != null && sporeResponse.getCode() == 1 && sporeResponse.getData() != null) {
                List<UnifiedDeviceDto> sporeDevices = parseDeviceData(sporeResponse.getData(), "孢子仪设备");
                result.addAll(sporeDevices);
                log.info("获取到 {} 个孢子仪设备", sporeDevices.size());
            }
            
            // 3. 杀虫灯设备 (eqType = 3)
            ApiResponse lampResponse = ioTApiUtil.getDeviceList(projectId, 3).block();
            if (lampResponse != null && lampResponse.getCode() == 1 && lampResponse.getData() != null) {
                List<UnifiedDeviceDto> lampDevices = parseDeviceData(lampResponse.getData(), "杀虫灯设备");
                result.addAll(lampDevices);
                log.info("获取到 {} 个杀虫灯设备", lampDevices.size());
            }
            
            // 4. 气象传感器 (sensorTypeId = 1)
            SensorListRequest weatherRequest = new SensorListRequest();
            weatherRequest.setProjectId(projectId);
            weatherRequest.setSensorTypeId(1);
            ApiResponse weatherResponse = ioTApiUtil.getSensorList(weatherRequest).block();
            if (weatherResponse != null && weatherResponse.getCode() == 1 && weatherResponse.getData() != null) {
                List<UnifiedDeviceDto> weatherDevices = parseDeviceData(weatherResponse.getData(), "气象传感器");
                result.addAll(weatherDevices);
                log.info("获取到 {} 个气象传感器", weatherDevices.size());
            }
            
            // 5. 土壤传感器 (sensorTypeId = 2)
            SensorListRequest soilRequest = new SensorListRequest();
            soilRequest.setProjectId(projectId);
            soilRequest.setSensorTypeId(2);
            ApiResponse soilResponse = ioTApiUtil.getSensorList(soilRequest).block();
            if (soilResponse != null && soilResponse.getCode() == 1 && soilResponse.getData() != null) {
                List<UnifiedDeviceDto> soilDevices = parseDeviceData(soilResponse.getData(), "土壤传感器");
                result.addAll(soilDevices);
                log.info("获取到 {} 个土壤传感器", soilDevices.size());
            }
            
            // 6. 水质传感器 (sensorTypeId = 4)
            SensorListRequest waterRequest = new SensorListRequest();
            waterRequest.setProjectId(projectId);
            waterRequest.setSensorTypeId(4);
            ApiResponse waterResponse = ioTApiUtil.getSensorList(waterRequest).block();
            if (waterResponse != null && waterResponse.getCode() == 1 && waterResponse.getData() != null) {
                List<UnifiedDeviceDto> waterDevices = parseDeviceData(waterResponse.getData(), "水质传感器");
                result.addAll(waterDevices);
                log.info("获取到 {} 个水质传感器", waterDevices.size());
            }
            
            // 7. 光谱设备
            ApiResponse spectrumResponse = ioTApiUtil.getSpectrumDeviceList(projectId).block();
            if (spectrumResponse != null && spectrumResponse.getCode() == 1 && spectrumResponse.getData() != null) {
                List<UnifiedDeviceDto> spectrumDevices = parseSpectrumDeviceData(spectrumResponse.getData());
                result.addAll(spectrumDevices);
                log.info("获取到 {} 个光谱设备", spectrumDevices.size());
            }
            
            log.info("总共获取到 {} 个设备", result.size());
            return Result.OK(result);
            
        } catch (Exception e) {
            log.error("获取设备列表失败", e);
            return Result.error("获取设备列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 解析设备数据，转换为UnifiedDeviceDto列表
     */
    private List<UnifiedDeviceDto> parseDeviceData(Object data, String deviceType) {
        List<UnifiedDeviceDto> deviceList = new ArrayList<>();
        
        try {
            // 将数据转换为JSON数组
            JSONArray jsonArray = null;
            if (data instanceof JSONArray) {
                jsonArray = (JSONArray) data;
            } else if (data instanceof String) {
                jsonArray = JSON.parseArray((String) data);
            } else if (data instanceof List) {
                jsonArray = new JSONArray((List<?>) data);
            } else {
                // 尝试将对象转换为JSON，然后再解析为数组
                String jsonString = JSON.toJSONString(data);
                jsonArray = JSON.parseArray(jsonString);
            }
            
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject deviceJson = jsonArray.getJSONObject(i);
                    UnifiedDeviceDto device = new UnifiedDeviceDto();
                    
                    // 设置设备编号
                    // 传感器设备使用sensorSerial作为设备编号
                    if (deviceJson.containsKey("sensorSerial")) {
                        device.setDeviceCode(deviceJson.getString("sensorSerial"));
                    } else if (deviceJson.containsKey("DeviceCode")) {
                        device.setDeviceCode(deviceJson.getString("DeviceCode"));
                    } else if (deviceJson.containsKey("deviceCode")) {
                        device.setDeviceCode(deviceJson.getString("deviceCode"));
                    }
                    
                    // 设置经纬度
                    if (deviceJson.containsKey("Lat")) {
                        device.setLat(deviceJson.getString("Lat"));
                    } else if (deviceJson.containsKey("lat")) {
                        device.setLat(deviceJson.getString("lat"));
                    }
                    
                    if (deviceJson.containsKey("Lng")) {
                        device.setLng(deviceJson.getString("Lng"));
                    } else if (deviceJson.containsKey("lng")) {
                        device.setLng(deviceJson.getString("lng"));
                    }
                    
                    // 设置设备类型
                    device.setDeviceType(deviceType);
                    
                    // 只有当设备编号不为空时才添加到列表
                    if (device.getDeviceCode() != null && !device.getDeviceCode().isEmpty()) {
                        deviceList.add(device);
                    }
                }
            }
        } catch (Exception e) {
            log.error("解析设备数据失败", e);
        }
        
        return deviceList;
    }
    
    /**
     * 解析光谱设备数据，转换为UnifiedDeviceDto列表
     */
    private List<UnifiedDeviceDto> parseSpectrumDeviceData(Object data) {
        List<UnifiedDeviceDto> deviceList = new ArrayList<>();
        
        try {
            // 将数据转换为JSON数组
            JSONArray jsonArray = null;
            if (data instanceof JSONArray) {
                jsonArray = (JSONArray) data;
            } else if (data instanceof String) {
                jsonArray = JSON.parseArray((String) data);
            } else if (data instanceof List) {
                jsonArray = new JSONArray((List<?>) data);
            } else {
                // 尝试将对象转换为JSON，然后再解析为数组
                String jsonString = JSON.toJSONString(data);
                jsonArray = JSON.parseArray(jsonString);
            }
            
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject item = jsonArray.getJSONObject(i);
                    
                    // 光谱设备的数据结构是 {"q": {...}, "cropName": "..."}
                    if (item.containsKey("q")) {
                        JSONObject deviceJson = item.getJSONObject("q");
                        UnifiedDeviceDto device = new UnifiedDeviceDto();
                        
                        // 设置设备编号 (sensorSerial)
                        if (deviceJson.containsKey("sensorSerial")) {
                            device.setDeviceCode(deviceJson.getString("sensorSerial"));
                        }
                        
                        // 设置经纬度
                        if (deviceJson.containsKey("lat")) {
                            device.setLat(deviceJson.getString("lat"));
                        }
                        
                        if (deviceJson.containsKey("lng")) {
                            device.setLng(deviceJson.getString("lng"));
                        }
                        
                        // 设置设备类型为光谱设备
                        device.setDeviceType("光谱设备");
                        
                        // 只有当设备编号不为空时才添加到列表
                        if (device.getDeviceCode() != null && !device.getDeviceCode().isEmpty()) {
                            deviceList.add(device);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("解析光谱设备数据失败", e);
        }
        
        return deviceList;
    }

    /**
     * 获取传感器实时数据（原始数据）
     */
    private Object getSensorRealTimeDataRaw(String deviceCode) {
        try {
            log.info("开始获取传感器实时数据，设备编号: {}", deviceCode);
            
            // 使用IoTApiUtil获取实时数据
            ApiResponse<Object> response = ioTApiUtil.getSensorRealTimeData(deviceCode).block();
            
            if (response == null) {
                log.error("获取传感器实时数据失败: API响应为null");
                return null;
            }
            
            log.info("API响应状态: {}, 消息: {}", response.getCode(), response.getMsg());
            
            if (response.getCode() == 1 && response.getData() != null) {
                // 直接返回原始数据
                Object data = response.getData();
                log.info("传感器数据类型: {}", data.getClass().getName());
                log.info("传感器数据内容: {}", data.toString());
                
                return data;
            } else {
                log.warn("获取传感器实时数据失败: {}", response.getMsg());
                return null;
            }
        } catch (Exception e) {
            log.error("获取传感器实时数据异常", e);
            return null;
        }
    }
    
    /**
     * 获取传感器实时数据
     */
    private JSONObject getSensorRealTimeData(String deviceCode) {
        try {
            // 获取原始数据
            Object data = getSensorRealTimeDataRaw(deviceCode);
            
            if (data == null) {
                return null;
            }
            
            // 如果已经是JSONObject类型，直接返回
            if (data instanceof JSONObject) {
                return (JSONObject) data;
            }
            
            // 如果是ArrayList，直接返回null，让调用方处理
            if (data instanceof ArrayList) {
                log.info("数据是ArrayList类型，直接返回null，让调用方处理");
                return null;
            }
            
            // 尝试将数据转换为JSONObject
            try {
                return (JSONObject) JSON.toJSON(data);
            } catch (Exception e) {
                log.error("无法将传感器数据转换为JSONObject: {}", data);
                return null;
            }
        } catch (Exception e) {
            log.error("获取传感器实时数据异常", e);
            return null;
        }
    }
    
    /**
     * 解析气象数据
     */
    private WeatherSensorDataDTO parseWeatherData(Object sensorData, YoucaiSensorInfo sensorInfo) {
        WeatherSensorDataDTO weatherData = new WeatherSensorDataDTO();
        
        // 设置传感器基本信息
        weatherData.setSensorId(sensorInfo.getId().toString());
        weatherData.setSensorName(sensorInfo.getSensorName());
        
        // 如果是JSONObject类型，使用原有逻辑
        if (sensorData instanceof JSONObject) {
            JSONObject realTimeData = (JSONObject) sensorData;
            
            // 解析实时数据
            if (realTimeData.containsKey("Temperature")) {
                weatherData.setTemperature(realTimeData.getFloatValue("Temperature"));
            }
            
            if (realTimeData.containsKey("Humidity")) {
                weatherData.setHumidity(realTimeData.getFloatValue("Humidity"));
            }
            
            if (realTimeData.containsKey("AirPressure")) {
                weatherData.setAirPressure(realTimeData.getFloatValue("AirPressure"));
            }
            
            if (realTimeData.containsKey("WindSpeed")) {
                weatherData.setWindSpeed(realTimeData.getFloatValue("WindSpeed"));
            }
            
            if (realTimeData.containsKey("WindDirection")) {
                weatherData.setWindDirection(realTimeData.getFloatValue("WindDirection"));
            }
            
            if (realTimeData.containsKey("LightIntensity")) {
                weatherData.setLightIntensity(realTimeData.getFloatValue("LightIntensity"));
            }
            
            if (realTimeData.containsKey("Rainfall")) {
                weatherData.setRainfall(realTimeData.getFloatValue("Rainfall"));
            }
            
            if (realTimeData.containsKey("CO2")) {
                weatherData.setCo2Level(realTimeData.getFloatValue("CO2"));
            }
            
            if (realTimeData.containsKey("TotalRadiation")) {
                weatherData.setTotalRadiation(realTimeData.getFloatValue("TotalRadiation"));
            }
            
            // 设置更新时间
            if (realTimeData.containsKey("UpdateTime")) {
                weatherData.setUpdateTime(realTimeData.getString("UpdateTime"));
            } else {
                weatherData.setUpdateTime(String.valueOf(System.currentTimeMillis()));
            }
        }
        // 如果是ArrayList类型，解析数组中的数据
        else if (sensorData instanceof ArrayList) {
            ArrayList<?> dataList = (ArrayList<?>) sensorData;
            log.info("解析ArrayList格式的传感器数据，共{}项", dataList.size());
            
            // 遍历数组，解析各项传感器数据
            for (Object item : dataList) {
                if (item instanceof Map) {
                    Map<?, ?> sensorItem = (Map<?, ?>) item;
                    String name = String.valueOf(sensorItem.get("name"));
                    Object q = sensorItem.get("q");
                    
                    log.info("解析传感器项: name={}, q={}", name, q);
                    
                    // 解析q字段
                    if (q instanceof Map) {
                        Map<?, ?> qMap = (Map<?, ?>) q;
                        Object sensorNum = qMap.get("sensorNum");
                        Object collectime = qMap.get("collectime");
                        
                        // 根据传感器名称设置对应的值
                        if ("空气温度".equals(name) && sensorNum != null) {
                            weatherData.setTemperature(Float.parseFloat(String.valueOf(sensorNum)));
                            log.info("设置温度: {}", sensorNum);
                        } else if ("空气湿度".equals(name) && sensorNum != null) {
                            weatherData.setHumidity(Float.parseFloat(String.valueOf(sensorNum)));
                            log.info("设置湿度: {}", sensorNum);
                        } else if ("大气压强".equals(name) && sensorNum != null) {
                            weatherData.setAirPressure(Float.parseFloat(String.valueOf(sensorNum)));
                            log.info("设置大气压强: {}", sensorNum);
                        } else if ("风速".equals(name) && sensorNum != null) {
                            weatherData.setWindSpeed(Float.parseFloat(String.valueOf(sensorNum)));
                            log.info("设置风速: {}", sensorNum);
                        } else if ("风向".equals(name) && sensorNum != null) {
                            weatherData.setWindDirection(Float.parseFloat(String.valueOf(sensorNum)));
                            log.info("设置风向: {}", sensorNum);
                        } else if ("降雨量".equals(name) && sensorNum != null) {
                            weatherData.setRainfall(Float.parseFloat(String.valueOf(sensorNum)));
                            log.info("设置降雨量: {}", sensorNum);
                        } else if ("光照强度".equals(name) && sensorNum != null) {
                            weatherData.setLightIntensity(Float.parseFloat(String.valueOf(sensorNum)));
                            log.info("设置光照强度: {}", sensorNum);
                        } else if ("CO2".equals(name) && sensorNum != null) {
                            weatherData.setCo2Level(Float.parseFloat(String.valueOf(sensorNum)));
                            log.info("设置CO2: {}", sensorNum);
                        } else if ("总辐射".equals(name) && sensorNum != null) {
                            weatherData.setTotalRadiation(Float.parseFloat(String.valueOf(sensorNum)));
                            log.info("设置总辐射: {}", sensorNum);
                        }
                        
                        // 设置更新时间（使用最后一项的时间）
                        if (collectime != null) {
                            weatherData.setUpdateTime(String.valueOf(collectime));
                        }
                    }
                }
            }
            
            // 如果没有设置更新时间，使用当前时间
            if (weatherData.getUpdateTime() == null) {
                weatherData.setUpdateTime(String.valueOf(System.currentTimeMillis()));
            }
        }
        
        return weatherData;
    }
}