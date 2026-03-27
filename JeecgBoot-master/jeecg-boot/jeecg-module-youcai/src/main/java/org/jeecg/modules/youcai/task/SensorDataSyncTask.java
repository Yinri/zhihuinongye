package org.jeecg.modules.youcai.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.youcai.entity.YoucaiIotDevices;
import org.jeecg.modules.youcai.entity.YoucaiSensorHourly;
import org.jeecg.modules.youcai.entity.iotEntity.ApiResponse;
import org.jeecg.modules.youcai.entity.iotEntity.sensor.SensorRealTimeData;
import org.jeecg.modules.youcai.mapper.YoucaiIotDevicesMapper;
import org.jeecg.modules.youcai.mapper.YoucaiSensorHourlyMapper;
import org.jeecg.modules.youcai.util.IoTApiUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * 传感器数据同步定时任务
 * 从IoT平台获取传感器数据并存储到本地数据库
 */
@Component
@Slf4j
public class SensorDataSyncTask {

    @Autowired
    private IoTApiUtil ioTApiUtil;
    
    @Autowired
    private YoucaiIotDevicesMapper iotDevicesMapper;
    
    @Autowired
    private YoucaiSensorHourlyMapper sensorHourlyMapper;

    /**
     * 同步传感器数据 - 每30分钟执行一次
     */
    @Scheduled(cron = "0 */30 * * * ?")
    public void syncSensorData() {
        log.info("========== 定时任务开始：同步传感器数据 ==========");
        try {
            // 获取所有启用的IoT设备
            QueryWrapper<YoucaiIotDevices> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("del_flag", 0);
            List<YoucaiIotDevices> devices = iotDevicesMapper.selectList(queryWrapper);
            
            log.info("共 {} 个设备需要同步", devices.size());
            
            int successCount = 0;
            int failCount = 0;
            
            for (YoucaiIotDevices device : devices) {
                try {
                    syncSingleDeviceData(device);
                    successCount++;
                } catch (Exception e) {
                    log.error("同步设备 {} 数据失败: {}", device.getDeviceCode(), e.getMessage());
                    failCount++;
                }
            }
            
            log.info("传感器数据同步完成：成功 {} 个，失败 {} 个", successCount, failCount);
            log.info("========== 定时任务结束：同步传感器数据 ==========");
            
        } catch (Exception e) {
            log.error("同步传感器数据定时任务执行失败", e);
        }
    }

    /**
     * 同步单个设备的数据
     */
    private void syncSingleDeviceData(YoucaiIotDevices device) {
        String deviceCode = device.getDeviceCode();
        log.info("开始同步设备 {} 的数据", deviceCode);
        
        // 调用IoT平台获取实时数据
        ApiResponse response = ioTApiUtil.getSensorRealTimeData(deviceCode).block();
        
        if (response == null) {
            log.warn("设备 {} 获取实时数据返回null", deviceCode);
            return;
        }
        if (response.getCode() != 1) {
            log.warn("设备 {} 获取实时数据失败: {}", deviceCode, response.getMsg());
            return;
        }
        if (response.getData() == null) {
            log.warn("设备 {} 获取实时数据无数据", deviceCode);
            return;
        }
        
        log.info("设备 {} 获取到数据: {}", deviceCode, response.getData().getClass());
        
        // 解析数据并存储
        Object data = response.getData();
        saveSensorHourlyData(device, data);
    }

    /**
     * 解析并保存传感器小时级数据
     */
    private void saveSensorHourlyData(YoucaiIotDevices device, Object data) {
        YoucaiSensorHourly hourly = new YoucaiSensorHourly();
        hourly.setDeviceCode(device.getDeviceCode());
        hourly.setPlotId(device.getPlotId());
        hourly.setBaseId(device.getBaseId());
        hourly.setHourTs(new Date());
        hourly.setDelFlag(0);
        
        try {
            // 解析JSON数据 - 根据IoT平台返回的数据格式解析
            log.info("设备 {} 返回数据类型: {}", device.getDeviceCode(), data.getClass());
            
            // 数据格式为 List<SensorRealTimeData>，每个元素包含 name、unit、q（包含sensorNum数据值）
            if (data instanceof List) {
                List<?> dataList = (List<?>) data;
                log.info("设备 {} 解析到 {} 条传感器数据", device.getDeviceCode(), dataList.size());
                
                for (Object item : dataList) {
                    if (item instanceof JSONObject) {
                        JSONObject json = (JSONObject) item;
                        String name = json.getString("name");
                        JSONObject q = json.getJSONObject("q");
                        
                        if (q != null && name != null) {
                            Double value = q.getDouble("sensorNum");
                            if (value == null) {
                                continue;
                            }
                            BigDecimal bdValue = new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
                            
                            log.debug("设备 {} 传感器: {} = {} {}", device.getDeviceCode(), name, value, q.getString("unit"));
                            
                            // 根据指标名称匹配字段
                            if (name.contains("空气温度") || name.contains("温度") || name.contains("temperature")) {
                                hourly.setAirTempC(bdValue);
                                log.debug("设置温度: {}", bdValue);
                            } else if (name.contains("空气湿度") || name.contains("相对湿度") || name.contains("湿度") || name.contains("humidity")) {
                                hourly.setRelHumidityPct(bdValue);
                                log.debug("设置湿度: {}", bdValue);
                            } else if (name.contains("风速") || name.contains("windSpeed")) {
                                hourly.setWindSpeedMs(bdValue);
                                log.debug("设置风速: {}", bdValue);
                            } else if (name.contains("气压") || name.contains("airPressure")) {
                                hourly.setAirPressureKpa(bdValue);
                                log.debug("设置气压: {}", bdValue);
                            } else if (name.contains("太阳辐射") || name.contains("辐射") || name.contains("radiation") || name.contains("总辐射")) {
                                hourly.setSolarRadiationWm2(bdValue);
                                log.debug("设置辐射: {}", bdValue);
                            } else if (name.contains("降水量") || name.contains("降雨") || name.contains("rainfall") || name.contains("precipitation")) {
                                hourly.setPrecipMm(bdValue);
                                log.debug("设置降水: {}", bdValue);
                            } else if (name.contains("土壤含水率") || name.contains("土壤湿度") || name.contains("soilMoisture")) {
                                hourly.setSoilMoisturePct(bdValue);
                                log.debug("设置土壤含水率: {}", bdValue);
                            } else if (name.contains("露点") || name.contains("dew")) {
                                hourly.setDewTempC(bdValue);
                            } else if (name.contains("VPD") || name.contains("蒸气压")) {
                                hourly.setVpdKpa(new BigDecimal(value).setScale(3, RoundingMode.HALF_UP));
                            }
                        }
                    }
                }
            } else if (data instanceof JSONObject) {
                // 如果是单个JSON对象（备用解析方式）
                JSONObject json = (JSONObject) data;
                parseJsonObject(hourly, json);
            } else {
                log.warn("设备 {} 返回数据格式未知: {}", device.getDeviceCode(), data.getClass());
            }
            
            // 检查是否已有该设备当前小时的数据，避免重复插入
            LocalDateTime now = LocalDateTime.now();
            Date hourStart = Date.from(now.minusMinutes(now.getMinute()).minusSeconds(now.getSecond()).atZone(ZoneId.systemDefault()).toInstant());
            
            QueryWrapper<YoucaiSensorHourly> checkWrapper = new QueryWrapper<>();
            checkWrapper.eq("device_code", device.getDeviceCode());
            checkWrapper.eq("hour_ts", hourStart);
            YoucaiSensorHourly exist = sensorHourlyMapper.selectOne(checkWrapper);
            
            if (exist != null) {
                // 更新已有数据
                hourly.setId(exist.getId());
                hourly.setUpdateTime(new Date());
                sensorHourlyMapper.updateById(hourly);
                log.debug("更新设备 {} 小时数据成功", device.getDeviceCode());
            } else {
                // 插入新数据
                hourly.setCreateTime(new Date());
                sensorHourlyMapper.insert(hourly);
                log.debug("新增设备 {} 小时数据成功", device.getDeviceCode());
            }
            
        } catch (Exception e) {
            log.error("解析设备 {} 数据失败: {}", device.getDeviceCode(), e.getMessage());
        }
    }

    /**
     * 解析单个JSON对象（备用方法）
     */
    private void parseJsonObject(YoucaiSensorHourly hourly, JSONObject json) {
        String[] fields = {"temperature", "humidity", "windSpeed", "airPressure", 
                          "solarRadiation", "rainfall", "soilMoisture", "dewTemperature", "vpd"};
        
        for (String field : fields) {
            if (json.containsKey(field)) {
                try {
                    BigDecimal value = new BigDecimal(json.getDouble(field)).setScale(2, RoundingMode.HALF_UP);
                    
                    switch (field) {
                        case "temperature": hourly.setAirTempC(value); break;
                        case "humidity": hourly.setRelHumidityPct(value); break;
                        case "windSpeed": hourly.setWindSpeedMs(value); break;
                        case "airPressure": hourly.setAirPressureKpa(value); break;
                        case "solarRadiation": hourly.setSolarRadiationWm2(value); break;
                        case "rainfall": hourly.setPrecipMm(value); break;
                        case "soilMoisture": hourly.setSoilMoisturePct(value); break;
                        case "dewTemperature": hourly.setDewTempC(value); break;
                        case "vpd": hourly.setVpdKpa(value.setScale(3, RoundingMode.HALF_UP)); break;
                    }
                } catch (Exception e) {
                    log.debug("解析字段 {} 失败: {}", field, e.getMessage());
                }
            }
        }
    }
}
