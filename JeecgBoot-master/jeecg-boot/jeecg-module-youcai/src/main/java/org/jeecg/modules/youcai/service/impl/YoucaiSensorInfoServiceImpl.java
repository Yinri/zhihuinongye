package org.jeecg.modules.youcai.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.youcai.dto.UnifiedDeviceDto;
import org.jeecg.modules.youcai.dto.WeatherSensorDataDTO;
import org.jeecg.modules.youcai.entity.YoucaiAgriculturalMachine;
import org.jeecg.modules.youcai.entity.YoucaiBases;
import org.jeecg.modules.youcai.entity.YoucaiProjectInfo;
import org.jeecg.modules.youcai.entity.YoucaiSensorInfo;
import org.jeecg.modules.youcai.entity.iotEntity.ApiResponse;
import org.jeecg.modules.youcai.entity.iotEntity.sensor.SensorListRequest;
import org.jeecg.modules.youcai.entity.iotEntity.sensor.SensorRealTimeData;
import org.jeecg.modules.youcai.mapper.YoucaiBasesMapper;
import org.jeecg.modules.youcai.mapper.YoucaiSensorInfoMapper;
import org.jeecg.modules.youcai.service.IYoucaiProjectInfoService;
import org.jeecg.modules.youcai.service.IYoucaiSensorInfoService;
import org.jeecg.modules.youcai.util.IoTApiUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.time.LocalDateTime;
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

    @Autowired
    private YoucaiBasesMapper youcaiBasesMapper;

    @Autowired
    private IYoucaiProjectInfoService youcaiProjectInfoService;
    
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

        YoucaiBases base = youcaiBasesMapper.selectById(baseId);
        if (base == null || base.getBaseName() == null) {
            log.warn("基地不存在或基地名称为空，baseId: {}", baseId);
            return Result.OK(result);
        }

        String baseName = base.getBaseName();
        String keyword = extractSensorNamePrefix(baseName);
        log.info("基地名称: {}，匹配关键字: {}", baseName, keyword);

        Integer projectId = getProjectId();
        if (projectId == null) {
            log.warn("获取项目ID失败");
            return Result.OK(result);
        }

        ensureSensorDataForType(projectId, 1, "气象传感器");
        ensureSensorDataForType(projectId, 2, "土壤传感器");
        ensureDeviceDataForType(projectId, 3, "虫情测报设备");
        ensureDeviceDataForType(projectId, 4, "孢子仪设备");
        ensureDeviceDataForType(projectId, 5, "杀虫灯设备");
        ensureSpectrumDeviceData(projectId);

        QueryWrapper<YoucaiSensorInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.likeRight("sensor_name", keyword);
        queryWrapper.eq("is_delete", 0);
        List<YoucaiSensorInfo> sensorList = this.list(queryWrapper);

        if (sensorList != null && !sensorList.isEmpty()) {
            for (YoucaiSensorInfo sensor : sensorList) {
                UnifiedDeviceDto dto = new UnifiedDeviceDto();
                dto.setDeviceCode(sensor.getSensorSerial());
                dto.setDeviceName(sensor.getSensorName());
                dto.setLat(sensor.getLatitude());
                dto.setLng(sensor.getLongitude());
                dto.setState(sensor.getState());

                Integer typeId = sensor.getSensorTypeId();
                if (typeId != null) {
                    switch (typeId) {
                        case 1:
                            dto.setDeviceType("气象传感器");
                            break;
                        case 2:
                            dto.setDeviceType("土壤传感器");
                            break;
                        case 3:
                            dto.setDeviceType("虫情测报设备");
                            break;
                        case 4:
                            dto.setDeviceType("孢子仪设备");
                            break;
                        case 5:
                            dto.setDeviceType("杀虫灯设备");
                            break;
                        case 6:
                            dto.setDeviceType("光谱设备");
                            break;
                        default:
                            dto.setDeviceType("未知设备");
                            break;
                    }
                } else {
                    dto.setDeviceType("未知设备");
                }

                result.add(dto);
            }
        }

        try {
            ApiResponse videoResponse = ioTApiUtil.getVideoDeviceList(projectId).block();
            if (videoResponse != null && videoResponse.getCode() == 1 && videoResponse.getData() != null) {
                JSONArray videoArray = convertToJsonArray(videoResponse.getData());
                if (videoArray != null) {
                    for (int i = 0; i < videoArray.size(); i++) {
                        try {
                            JSONObject item = videoArray.getJSONObject(i);
                            String deviceName = item.getString("equipmentName");
                            if (deviceName != null && deviceName.startsWith(keyword)) {
                                UnifiedDeviceDto dto = new UnifiedDeviceDto();
                                String deviceCode = item.getString("equipmentCode");
                                dto.setDeviceCode(deviceCode);
                                dto.setDeviceName(deviceName);
                                dto.setLat(item.getString("lat"));
                                dto.setLng(item.getString("lng"));
                                dto.setState(item.getInteger("isOnline"));
                                dto.setDeviceType("视频设备");
                                result.add(dto);
                            }
                        } catch (Exception e) {
                            log.error("解析视频设备第{}条数据失败: {}", i + 1, e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取视频设备列表异常: {}", e.getMessage(), e);
        }

        log.info("基地 '{}' 共匹配到 {} 个设备", baseName, result.size());
        return Result.OK(result);
    }

    private Integer getProjectId() {
        QueryWrapper<YoucaiProjectInfo> query = new QueryWrapper<>();
        query.eq("is_delete", 0).last("LIMIT 1");
        YoucaiProjectInfo project = youcaiProjectInfoService.getOne(query);
        return project != null ? project.getProjectId() : null;
    }

    private void ensureSensorDataForType(Integer projectId, int sensorTypeId, String typeName) {
        QueryWrapper<YoucaiSensorInfo> query = new QueryWrapper<>();
        query.eq("sensor_type_id", sensorTypeId).eq("is_delete", 0);
        long count = this.count(query);
        if (count > 0) {
            log.info("{}本地已有 {} 条数据，跳过同步", typeName, count);
            return;
        }

        log.info("{}本地无数据，开始从API同步", typeName);
        try {
            SensorListRequest request = new SensorListRequest();
            request.setProjectId(projectId);
            request.setSensorTypeId(sensorTypeId);
           
            
            ApiResponse response = ioTApiUtil.getSensorList(request).block();

            if (response == null || response.getCode() != 1 || response.getData() == null) {
                log.warn("同步{}失败: {}", typeName, response != null ? response.getMsg() : "响应为空");
                return;
            }

            JSONArray jsonArray = convertToJsonArray(response.getData());
            if (jsonArray == null || jsonArray.isEmpty()) {
                log.info("{}API无数据", typeName);
                return;
            }

            int addCount = 0;
            for (int j = 0; j < jsonArray.size(); j++) {
                try {
                    JSONObject item = jsonArray.getJSONObject(j);
                    JSONObject q = item.getJSONObject("q");
                    if (q == null) continue;

                    YoucaiSensorInfo sensorInfo = new YoucaiSensorInfo();
                    sensorInfo.setSensorId(q.getInteger("id"));
                    sensorInfo.setSensorName(q.getString("sensorName"));
                    sensorInfo.setSensorSerial(q.getString("sensorSerial"));
                    sensorInfo.setSensorTypeId(sensorTypeId);
                    sensorInfo.setSensorDataTypeIds(q.getString("sensorDataTypeIds"));
                    sensorInfo.setSensorDataTypeNames(q.getString("sensorDataTypeNames"));
                    sensorInfo.setNums(q.getInteger("nums"));
                    sensorInfo.setTime(q.getInteger("time"));
                    sensorInfo.setState(q.getInteger("state"));
                    sensorInfo.setLongitude(q.getString("lng"));
                    sensorInfo.setLatitude(q.getString("lat"));
                    sensorInfo.setProtocolName(item.getString("traName"));
                    sensorInfo.setIsDelete(q.getInteger("isDelete"));
                    sensorInfo.setProjectId(projectId);
                    sensorInfo.setSyncTime(LocalDateTime.now());

                    this.save(sensorInfo);
                    addCount++;
                } catch (Exception e) {
                    log.error("同步{}第{}条数据失败: {}", typeName, j + 1, e.getMessage());
                }
            }
            log.info("同步{}完成，新增 {} 条", typeName, addCount);
        } catch (Exception e) {
            log.error("同步{}异常: {}", typeName, e.getMessage(), e);
        }
    }

    private void ensureDeviceDataForType(Integer projectId, int eqType, String typeName) {
        int sensorTypeId = eqType;
        QueryWrapper<YoucaiSensorInfo> query = new QueryWrapper<>();
        query.eq("sensor_type_id", sensorTypeId).eq("is_delete", 0);
        long count = this.count(query);
        if (count > 0) {
            log.info("{}本地已有 {} 条数据，跳过同步", typeName, count);
            return;
        }

        log.info("{}本地无数据，开始从API同步", typeName);
        if (eqType == 3) {
            sensorTypeId = 1;
        }else if (eqType == 4) {
            sensorTypeId = 2;
        } else {
            sensorTypeId = 3;
        }
        try {

            ApiResponse response = ioTApiUtil.getDeviceList(projectId, sensorTypeId, 150).block();

            if (response == null || response.getCode() != 1 || response.getData() == null) {
                log.warn("同步{}失败: {}", typeName, response != null ? response.getMsg() : "响应为空");
                return;
            }

            JSONArray jsonArray = convertToJsonArray(response.getData());
            if (jsonArray == null || jsonArray.isEmpty()) {
                log.info("{}API无数据", typeName);
                return;
            }

            int addCount = 0;
            for (int j = 0; j < jsonArray.size(); j++) {
                try {
                    JSONObject item = jsonArray.getJSONObject(j);
                    String deviceCode = item.getString("deviceCode");
                    if (deviceCode == null || deviceCode.isEmpty()) continue;

                    YoucaiSensorInfo sensorInfo = new YoucaiSensorInfo();
                    sensorInfo.setSensorId(item.getInteger("id"));
                    sensorInfo.setSensorSerial(deviceCode);
                    sensorInfo.setSensorName(item.getString("deviceName"));
                    sensorInfo.setSensorTypeId(eqType);
                    sensorInfo.setState(item.getInteger("isOnline"));
                    sensorInfo.setLongitude(item.getString("lng"));
                    sensorInfo.setLatitude(item.getString("lat"));
                    sensorInfo.setProjectId(projectId);
                    sensorInfo.setIsDelete(0);
                    sensorInfo.setSyncTime(LocalDateTime.now());
            

                    this.save(sensorInfo);
                    addCount++;
                } catch (Exception e) {
                    log.error("同步{}第{}条数据失败: {}", typeName, j + 1, e.getMessage());
                }
            }
            log.info("同步{}完成，新增 {} 条", typeName, addCount);
        } catch (Exception e) {
            log.error("同步{}异常: {}", typeName, e.getMessage(), e);
        }
    }

    private void ensureSpectrumDeviceData(Integer projectId) {
        QueryWrapper<YoucaiSensorInfo> query = new QueryWrapper<>();
        query.eq("sensor_type_id", 6).eq("is_delete", 0);
        long count = this.count(query);
        if (count > 0) {
            log.info("光谱设备本地已有 {} 条数据，跳过同步", count);
            return;
        }

        log.info("光谱设备本地无数据，开始从API同步");
        try {
            ApiResponse response = ioTApiUtil.getSpectrumDeviceList(projectId).block();

            if (response == null || response.getCode() != 1 || response.getData() == null) {
                log.warn("同步光谱设备失败: {}", response != null ? response.getMsg() : "响应为空");
                return;
            }

            JSONArray jsonArray = convertToJsonArray(response.getData());
            if (jsonArray == null || jsonArray.isEmpty()) {
                log.info("光谱设备API无数据");
                return;
            }

            int addCount = 0;
            for (int j = 0; j < jsonArray.size(); j++) {
                try {
                    JSONObject item = jsonArray.getJSONObject(j);
                    JSONObject q = item.getJSONObject("q");
                    if (q == null) continue;

                    String deviceCode = q.getString("sensorSerial");
                    if (deviceCode == null || deviceCode.isEmpty()) continue;

                    YoucaiSensorInfo sensorInfo = new YoucaiSensorInfo();
                    sensorInfo.setSensorId(q.getInteger("id"));
                    sensorInfo.setSensorDataTypeIds(q.getString("sensorDataTypeIds"));
                    sensorInfo.setSensorDataTypeNames(q.getString("sensorDataTypeNames"));
                    sensorInfo.setNums(q.getInteger("nums"));
                    sensorInfo.setTime(q.getInteger("time"));
                    sensorInfo.setState(q.getInteger("state"));
                    sensorInfo.setSensorSerial(deviceCode);
                    sensorInfo.setSensorName(q.getString("sensorName"));
                    sensorInfo.setSensorTypeId(6);
                    sensorInfo.setLongitude(q.getString("lng"));
                    sensorInfo.setLatitude(q.getString("lat"));
                    sensorInfo.setProjectId(projectId);
                    sensorInfo.setIsDelete(0);
                    sensorInfo.setSyncTime(LocalDateTime.now());

                    this.save(sensorInfo);
                    addCount++;
                } catch (Exception e) {
                    log.error("同步光谱设备第{}条数据失败: {}", j + 1, e.getMessage());
                }
            }
            log.info("同步光谱设备完成，新增 {} 条", addCount);
        } catch (Exception e) {
            log.error("同步光谱设备异常: {}", e.getMessage(), e);
        }
    }

    private JSONArray convertToJsonArray(Object data) {
        if (data instanceof JSONArray) {
            return (JSONArray) data;
        } else if (data instanceof List) {
            return new JSONArray((List<?>) data);
        } else if (data instanceof String) {
            return JSON.parseArray((String) data);
        } else {
            try {
                String jsonString = JSON.toJSONString(data);
                return JSON.parseArray(jsonString);
            } catch (Exception e) {
                log.error("转换JSON数组失败: {}", e.getMessage());
                return null;
            }
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

    @Override
    public Result<List<Map<String, Object>>> getVideoDevicesByBaseId(String baseId) {
        try {
            log.info("获取基地ID {} 的视频设备列表", baseId);
            
            YoucaiBases base = youcaiBasesMapper.selectById(baseId);
            if (base == null) {
                log.warn("基地不存在: {}", baseId);
                return Result.error("基地不存在");
            }
            
            String baseName = base.getBaseName();
            Integer projectId = 229;
            ApiResponse response = ioTApiUtil.getVideoDeviceList(projectId).block();
            
            if (response == null || response.getCode() != 1 || response.getData() == null) {
                log.warn("获取视频设备列表失败: {}", response != null ? response.getMsg() : "响应为空");
                return Result.OK(new ArrayList<>());
            }
            
            JSONArray jsonArray = convertToJsonArray(response.getData());
            if (jsonArray == null || jsonArray.isEmpty()) {
                log.info("视频设备API无数据");
                return Result.OK(new ArrayList<>());
            }
            
            List<Map<String, Object>> videoDevices = new ArrayList<>();

            String keyword = extractSensorNamePrefix(baseName);
            for (int i = 0; i < jsonArray.size(); i++) {
                try {
                    JSONObject item = jsonArray.getJSONObject(i);
                    String equipmentName = item.getString("equipmentName");
                    log.info("equipmentName: {}",equipmentName);
                    if (equipmentName != null  && equipmentName.startsWith(keyword)) {
                        Map<String, Object> device = new HashMap<>();
                        device.put("id", item.getInteger("id"));
                        device.put("equipmentName", equipmentName);
                        device.put("equipmentCode", item.getString("equipmentCode"));
                        device.put("channelNum", item.getString("channelNum"));
                        device.put("lng", item.getString("lng"));
                        device.put("lat", item.getString("lat"));
                        device.put("isOnline", item.getInteger("isOnline"));
                        device.put("videoUrl", item.getString("videoUrl"));
                        device.put("imageUrl", item.getString("imageUrl"));
                        videoDevices.add(device);
                    }

                } catch (Exception e) {
                    log.error("解析视频设备第{}条数据失败: {}", i + 1, e.getMessage());
                }
            }
            
            log.info("基地 {} 共获取到 {} 个视频设备", baseName, videoDevices.size());
            return Result.OK(videoDevices);
        } catch (Exception e) {
            log.error("获取视频设备列表异常", e);
            return Result.error("获取视频设备列表异常：" + e.getMessage());
        }
    }

    private String extractSensorNamePrefix(String baseName) {
        if (baseName == null) {
            return "";
        }

        String normalized = baseName.trim();
        String[] separators = {"镇", "乡", "街道"};
        for (String separator : separators) {
            int index = normalized.indexOf(separator);
            if (index > 0) {
                return normalized.substring(0, index);
            }
        }
        return normalized;
    }

}
