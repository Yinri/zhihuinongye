package org.jeecg.modules.youcai.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.youcai.entity.YoucaiPlots;
import org.jeecg.modules.youcai.entity.YoucaiGrowthMonitoring;
import org.jeecg.modules.youcai.entity.YoucaiIotDevices;
import org.jeecg.modules.youcai.entity.YoucaiSensorInfo;
import org.jeecg.modules.youcai.entity.YoucaiBases;
import org.jeecg.modules.youcai.entity.iotEntity.ApiResponse;
import org.jeecg.modules.youcai.entity.iotEntity.sensor.SensorInfo;
import org.jeecg.modules.youcai.mapper.YoucaiPlotsMapper;
import org.jeecg.modules.youcai.mapper.YoucaiGrowthMonitoringMapper;
import org.jeecg.modules.youcai.mapper.YoucaiIotDevicesMapper;
import org.jeecg.modules.youcai.mapper.YoucaiSensorInfoMapper;
import org.jeecg.modules.youcai.mapper.YoucaiBasesMapper;
import org.jeecg.modules.youcai.service.IIrrigationService;
import org.jeecg.modules.youcai.util.IoTApiUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

//zrt 灌溉监控服务实现类
@Service
public class IrrigationServiceImpl implements IIrrigationService {
    // 新增日志对象
    private static final Logger log = LoggerFactory.getLogger(IrrigationServiceImpl.class);

    @Autowired
    private YoucaiPlotsMapper plotsMapper;
    

    
    @Autowired
    private YoucaiIotDevicesMapper iotDevicesMapper;
    
    @Autowired
    private YoucaiSensorInfoMapper sensorInfoMapper;
    
    @Autowired
    private YoucaiBasesMapper basesMapper;
    
    @Autowired
    private IoTApiUtil ioTApiUtil;

    public Map<String, Object> getPlotStatus(String plotId) {
        log.info("开始查询地块{}的传感器状态数据（实时数据模式）", plotId);
        Map<String, Object> r = new HashMap<>();
        
        YoucaiPlots plot = plotsMapper.selectById(plotId);
        if (plot == null) {
            log.warn("地块{}不存在，返回空数据", plotId);
            r.put("soilMoisturePercent", null);
            r.put("soilMoistureTrend", "");
            r.put("currentStageId", "");
            r.put("lastUpdated", null);
            return r;
        }
        
        String baseId = plot.getBaseId();
        String soilDeviceCode = getDeviceCodeByBaseId(baseId);
        
        // 如果没有配置设备，返回提示信息
        if (soilDeviceCode == null) {
            log.warn("基地{}没有配置土壤传感器设备", baseId);
            r.put("soilMoisturePercent", null);
            r.put("soilMoistureTrend", "该基地未配置物联网设备");
            r.put("currentStageId", "");
            r.put("lastUpdated", null);
            r.put("deviceNotConfigured", true);
            return r;
        }
        
        Map<String, Object> soilData = fetchRealtimeSensorData(soilDeviceCode);
        
        BigDecimal soilMoisture = BigDecimal.ZERO;
        if (!soilData.isEmpty()) {
            soilMoisture = toBigDecimal(soilData.get("soilMoisture")).orElse(toBigDecimal(soilData.get("土壤含水率")).orElse(BigDecimal.ZERO));
        }
        
        boolean hasValidData = soilMoisture.compareTo(BigDecimal.ZERO) > 0;
        
        r.put("soilMoisturePercent", hasValidData ? soilMoisture : null);
        r.put("soilMoistureTrend", hasValidData ? "稳定" : "该基地未配置物联网设备");
        r.put("currentStageId", "");
        r.put("lastUpdated", hasValidData ? new Date() : null);
        
        log.info("地块{}状态查询完成（实时数据），最新含水率：{}%，数据有效：{}", plotId, soilMoisture, hasValidData);
        return r;
    }

    public Map<String, Object> getPenmanPredict(String plotId) {
        log.info("开始计算地块{}的Penman灌溉建议（实时数据模式）", plotId);
        Map<String, Object> r = new HashMap<>();
        
        YoucaiPlots plot = plotsMapper.selectById(plotId);
        if (plot == null) {
            log.warn("地块{}不存在，返回空的Penman建议", plotId);
            r.put("chartDates", new ArrayList<>());
            r.put("et0Forecast", new ArrayList<>());
            r.put("soilMoistureSeriesPct", new ArrayList<>());
            r.put("penmanInputs", new HashMap<>());
            r.put("needIrrigation", false);
            r.put("recommendedTime", "");
            r.put("method", "");
            r.put("reason", "");
            r.put("recommendedVolumeMm", BigDecimal.ZERO);
            r.put("flowRateM3PerHour", BigDecimal.ZERO);
            return r;
        }
        
        String baseId = plot.getBaseId();
        String soilDeviceCode = getDeviceCodeByBaseId(baseId);
        String meteoDeviceCode = getMeteoDeviceCodeByBaseId(baseId);
        
        // 如果没有配置设备，返回提示信息
        if (soilDeviceCode == null || meteoDeviceCode == null) {
            log.warn("地块{}所在基地{}没有配置物联网设备，soil={}, meteo={}", plotId, baseId, soilDeviceCode, meteoDeviceCode);
            r.put("chartDates", Collections.singletonList(java.time.LocalDate.now().toString()));
            r.put("et0Forecast", Collections.singletonList(BigDecimal.ZERO));
            r.put("soilMoistureSeriesPct", Collections.singletonList(BigDecimal.ZERO));
            r.put("penmanInputs", new HashMap<>());
            r.put("soilDetail", new HashMap<>());
            r.put("needIrrigation", false);
            r.put("recommendedTime", "暂无数据");
            r.put("method", "暂无数据");
            r.put("reason", "该地块所在基地未配置物联网设备，请先在传感器管理中添加气象站和土壤传感器设备");
            r.put("recommendedVolumeMm", BigDecimal.ZERO);
            r.put("flowRateM3PerHour", BigDecimal.ZERO);
            r.put("deviceNotConfigured", true);
            return r;
        }
        
        Map<String, Object> soilData = fetchRealtimeSensorData(soilDeviceCode);
        Map<String, Object> meteoData = fetchRealtimeSensorData(meteoDeviceCode);
        
        BigDecimal soilMoisture = BigDecimal.ZERO;
        BigDecimal airTemp = BigDecimal.ZERO;
        BigDecimal relHumidity = BigDecimal.ZERO;
        BigDecimal windSpeed = BigDecimal.ZERO;
        BigDecimal solarRadiation = BigDecimal.ZERO;
        BigDecimal precip = BigDecimal.ZERO;
        
        // 土壤传感器详细数据
        BigDecimal soilTemp1 = BigDecimal.ZERO;
        BigDecimal soilTemp2 = BigDecimal.ZERO;
        BigDecimal soilTemp3 = BigDecimal.ZERO;
        BigDecimal soilMoisture1 = BigDecimal.ZERO;
        BigDecimal soilMoisture2 = BigDecimal.ZERO;
        BigDecimal soilMoisture3 = BigDecimal.ZERO;
        
        if (!soilData.isEmpty()) {
            soilMoisture = toBigDecimal(soilData.get("soilMoisture")).orElse(toBigDecimal(soilData.get("土湿1")).orElse(toBigDecimal(soilData.get("土壤含水率")).orElse(BigDecimal.ZERO)));
            soilTemp1 = toBigDecimal(soilData.get("soilTemp1")).orElse(toBigDecimal(soilData.get("土温1")).orElse(BigDecimal.ZERO));
            soilTemp2 = toBigDecimal(soilData.get("soilTemp2")).orElse(toBigDecimal(soilData.get("土温2")).orElse(BigDecimal.ZERO));
            soilTemp3 = toBigDecimal(soilData.get("soilTemp3")).orElse(toBigDecimal(soilData.get("土温3")).orElse(BigDecimal.ZERO));
            soilMoisture1 = toBigDecimal(soilData.get("soilMoisture1")).orElse(toBigDecimal(soilData.get("土湿1")).orElse(BigDecimal.ZERO));
            soilMoisture2 = toBigDecimal(soilData.get("soilMoisture2")).orElse(toBigDecimal(soilData.get("土湿2")).orElse(BigDecimal.ZERO));
            soilMoisture3 = toBigDecimal(soilData.get("soilMoisture3")).orElse(toBigDecimal(soilData.get("土湿3")).orElse(BigDecimal.ZERO));
        }
        if (!meteoData.isEmpty()) {
            airTemp = toBigDecimal(meteoData.get("airTemp")).orElse(toBigDecimal(meteoData.get("空气温度")).orElse(toBigDecimal(meteoData.get("温度")).orElse(BigDecimal.ZERO)));
            relHumidity = toBigDecimal(meteoData.get("relHumidity")).orElse(toBigDecimal(meteoData.get("空气湿度")).orElse(toBigDecimal(meteoData.get("湿度")).orElse(BigDecimal.ZERO)));
            windSpeed = toBigDecimal(meteoData.get("windSpeed")).orElse(toBigDecimal(meteoData.get("风速")).orElse(BigDecimal.ZERO));
            solarRadiation = toBigDecimal(meteoData.get("solarRadiation")).orElse(toBigDecimal(meteoData.get("太阳辐射")).orElse(toBigDecimal(meteoData.get("总辐射")).orElse(BigDecimal.ZERO)));
            precip = toBigDecimal(meteoData.get("precipitation")).orElse(toBigDecimal(meteoData.get("降水量")).orElse(toBigDecimal(meteoData.get("降雨量")).orElse(BigDecimal.ZERO)));
        }
        
        log.info("地块{}实时数据 - 土壤含水率: {}%, 空气温度: {}℃, 湿度: {}%, 风速: {}m/s", 
                plotId, soilMoisture, airTemp, relHumidity, windSpeed);
        
        boolean hasValidData = soilMoisture.compareTo(BigDecimal.ZERO) > 0 || airTemp.compareTo(BigDecimal.ZERO) > 0;
        
        if (!hasValidData) {
            log.warn("地块{}传感器数据无效（均为0），返回空建议", plotId);
            r.put("chartDates", Collections.singletonList(java.time.LocalDate.now().toString()));
            r.put("et0Forecast", Collections.singletonList(BigDecimal.ZERO));
            r.put("soilMoistureSeriesPct", Collections.singletonList(BigDecimal.ZERO));
            r.put("penmanInputs", new HashMap<>());
            r.put("needIrrigation", false);
            r.put("recommendedTime", "暂无数据");
            r.put("method", "暂无数据");
            r.put("reason", "传感器数据获取失败或数据为0，请检查设备连接");
            r.put("recommendedVolumeMm", BigDecimal.ZERO);
            r.put("flowRateM3PerHour", BigDecimal.ZERO);
            return r;
        }
        
        BigDecimal et0 = calculateHourlyEt0(airTemp, relHumidity, windSpeed, solarRadiation);
        
        // 使用实时数据计算灌溉建议（简化版算法）
        Map<String, Object> irrigationAdvice = calculateIrrigationAdvice(
            soilMoisture, soilMoisture1, soilMoisture2, soilMoisture3,
            airTemp, relHumidity, precip, et0
        );
        
        boolean need = (boolean) irrigationAdvice.get("needIrrigation");
        String time = (String) irrigationAdvice.get("recommendedTime");
        String method = (String) irrigationAdvice.get("method");
        String reason = (String) irrigationAdvice.get("reason");
        BigDecimal vol = (BigDecimal) irrigationAdvice.get("recommendedVolumeMm");
        
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("dates", Collections.singletonList(java.time.LocalDate.now().toString()));
        inputs.put("temp", Collections.singletonList(airTemp));
        inputs.put("humidity", Collections.singletonList(relHumidity));
        inputs.put("wind", Collections.singletonList(windSpeed));
        inputs.put("solar", Collections.singletonList(solarRadiation.multiply(new BigDecimal("0.0036")).setScale(2, RoundingMode.HALF_UP)));
        inputs.put("precip", Collections.singletonList(precip));
        
        // 土壤详细数据
        Map<String, Object> soilDetail = new HashMap<>();
        soilDetail.put("soilTemp1", soilTemp1);
        soilDetail.put("soilTemp2", soilTemp2);
        soilDetail.put("soilTemp3", soilTemp3);
        soilDetail.put("soilMoisture1", soilMoisture1);
        soilDetail.put("soilMoisture2", soilMoisture2);
        soilDetail.put("soilMoisture3", soilMoisture3);
        
        r.put("chartDates", Collections.singletonList(java.time.LocalDate.now().toString()));
        r.put("et0Forecast", Collections.singletonList(et0));
        r.put("soilMoistureSeriesPct", Collections.singletonList(soilMoisture));
        r.put("penmanInputs", inputs);
        r.put("soilDetail", soilDetail);
        r.put("needIrrigation", need);
        r.put("recommendedTime", time);
        r.put("method", method);
        r.put("reason", reason);
        r.put("recommendedVolumeMm", vol);
        r.put("flowRateM3PerHour", BigDecimal.ZERO);
        
        log.info("地块{}Penman建议计算完成（实时数据）：是否需要灌溉={}，推荐灌水量={}mm", plotId, need, vol);
        return r;
    }

    public Map<String, Object> getInterventionComparison(String plotId) {
        log.info("开始生成地块{}的灌溉干预对比数据（实时数据模式）", plotId);
        Map<String, Object> r = new HashMap<>();
        
        YoucaiPlots plot = plotsMapper.selectById(plotId);
        if (plot == null) {
            log.warn("地块{}不存在，返回空的对比数据", plotId);
            r.put("dates", new ArrayList<>());
            r.put("withIrrigation", new ArrayList<>());
            r.put("withoutIrrigation", new ArrayList<>());
            return r;
        }
        
        String baseId = plot.getBaseId();
        String soilDeviceCode = getDeviceCodeByBaseId(baseId);
        String meteoDeviceCode = getMeteoDeviceCodeByBaseId(baseId);
        
        Map<String, Object> soilData = fetchRealtimeSensorData(soilDeviceCode);
        Map<String, Object> meteoData = fetchRealtimeSensorData(meteoDeviceCode);
        
        BigDecimal soilMoisture = BigDecimal.ZERO;
        BigDecimal solarRadiation = BigDecimal.ZERO;
        BigDecimal airTemp = BigDecimal.ZERO;
        BigDecimal relHumidity = BigDecimal.ZERO;
        BigDecimal windSpeed = BigDecimal.ZERO;
        
        if (!soilData.isEmpty()) {
            soilMoisture = toBigDecimal(soilData.get("soilMoisture")).orElse(toBigDecimal(soilData.get("土壤含水率")).orElse(BigDecimal.ZERO));
        }
        if (!meteoData.isEmpty()) {
            solarRadiation = toBigDecimal(meteoData.get("solarRadiation")).orElse(toBigDecimal(meteoData.get("太阳辐射")).orElse(BigDecimal.ZERO));
            airTemp = toBigDecimal(meteoData.get("airTemp")).orElse(toBigDecimal(meteoData.get("空气温度")).orElse(BigDecimal.ZERO));
            relHumidity = toBigDecimal(meteoData.get("relHumidity")).orElse(toBigDecimal(meteoData.get("空气湿度")).orElse(BigDecimal.ZERO));
            windSpeed = toBigDecimal(meteoData.get("windSpeed")).orElse(toBigDecimal(meteoData.get("风速")).orElse(BigDecimal.ZERO));
        }
        
        BigDecimal et0 = calculateHourlyEt0(airTemp, relHumidity, windSpeed, solarRadiation);
        
        List<String> dates = Collections.singletonList(java.time.LocalDate.now().toString());
        List<BigDecimal> withIrrigation = Collections.singletonList(soilMoisture);
        
        BigDecimal withoutValue = soilMoisture.subtract(et0);
        if (withoutValue.compareTo(BigDecimal.ZERO) < 0) withoutValue = BigDecimal.ZERO;
        List<BigDecimal> withoutIrrigation = Collections.singletonList(withoutValue);
        
        r.put("dates", dates);
        r.put("withIrrigation", withIrrigation);
        r.put("withoutIrrigation", withoutIrrigation);
        
        log.info("地块{}干预对比数据生成完成（实时数据）", plotId);
        return r;
    }

    public Map<String, Object> getPlotStatusByBase(String baseId) {
        log.info("开始查询基地{}的传感器状态数据（实时数据模式）", baseId);
        Map<String, Object> r = new HashMap<>();
        
        String soilDeviceCode = getDeviceCodeByBaseId(baseId);
        
        // 如果没有配置设备，返回提示信息
        if (soilDeviceCode == null) {
            log.warn("基地{}没有配置土壤传感器设备", baseId);
            r.put("soilMoisturePercent", null);
            r.put("soilMoistureTrend", "该基地未配置物联网设备");
            r.put("currentStageId", "");
            r.put("lastUpdated", null);
            r.put("deviceNotConfigured", true);
            return r;
        }
        
        Map<String, Object> soilData = fetchRealtimeSensorData(soilDeviceCode);
        
        BigDecimal soilMoisture = BigDecimal.ZERO;
        if (!soilData.isEmpty()) {
            soilMoisture = toBigDecimal(soilData.get("soilMoisture")).orElse(toBigDecimal(soilData.get("土壤含水率")).orElse(BigDecimal.ZERO));
        }
        
        boolean hasValidData = soilMoisture.compareTo(BigDecimal.ZERO) > 0;
        
        r.put("soilMoisturePercent", hasValidData ? soilMoisture : null);
        r.put("soilMoistureTrend", hasValidData ? "稳定" : "该基地未配置物联网设备");
        r.put("currentStageId", "");
        r.put("lastUpdated", hasValidData ? new Date() : null);
        
        log.info("基地{}状态查询完成（实时数据），最新含水率：{}%，数据有效：{}", baseId, soilMoisture, hasValidData);
        return r;
    }

    public Map<String, Object> getPenmanPredictByBase(String baseId) {
        log.info("开始计算基地{}的Penman灌溉建议（实时数据模式）", baseId);
        Map<String, Object> r = new HashMap<>();
        
        String soilDeviceCode = getDeviceCodeByBaseId(baseId);
        String meteoDeviceCode = getMeteoDeviceCodeByBaseId(baseId);
        
        // 如果没有配置设备，返回提示信息
        if (soilDeviceCode == null || meteoDeviceCode == null) {
            log.warn("基地{}没有配置物联网设备，soil={}, meteo={}", baseId, soilDeviceCode, meteoDeviceCode);
            r.put("chartDates", Collections.singletonList(java.time.LocalDate.now().toString()));
            r.put("et0Forecast", Collections.singletonList(BigDecimal.ZERO));
            r.put("soilMoistureSeriesPct", Collections.singletonList(BigDecimal.ZERO));
            r.put("penmanInputs", new HashMap<>());
            r.put("soilDetail", new HashMap<>());
            r.put("needIrrigation", false);
            r.put("recommendedTime", "暂无数据");
            r.put("method", "暂无数据");
            r.put("reason", "该基地未配置物联网设备，请先在传感器管理中添加气象站和土壤传感器设备");
            r.put("recommendedVolumeMm", BigDecimal.ZERO);
            r.put("flowRateM3PerHour", BigDecimal.ZERO);
            r.put("deviceNotConfigured", true);
            return r;
        }
        
        Map<String, Object> soilData = fetchRealtimeSensorData(soilDeviceCode);
        Map<String, Object> meteoData = fetchRealtimeSensorData(meteoDeviceCode);
        
        log.debug("土壤传感器数据: {}", soilData);
        log.debug("气象站数据: {}", meteoData);
        
        BigDecimal soilMoisture = BigDecimal.ZERO;
        BigDecimal airTemp = BigDecimal.ZERO;
        BigDecimal relHumidity = BigDecimal.ZERO;
        BigDecimal windSpeed = BigDecimal.ZERO;
        BigDecimal solarRadiation = BigDecimal.ZERO;
        BigDecimal precip = BigDecimal.ZERO;
        
        // 土壤传感器详细数据
        BigDecimal soilTemp1 = BigDecimal.ZERO;
        BigDecimal soilTemp2 = BigDecimal.ZERO;
        BigDecimal soilTemp3 = BigDecimal.ZERO;
        BigDecimal soilMoisture1 = BigDecimal.ZERO;
        BigDecimal soilMoisture2 = BigDecimal.ZERO;
        BigDecimal soilMoisture3 = BigDecimal.ZERO;
        
        if (!soilData.isEmpty()) {
            soilMoisture = toBigDecimal(soilData.get("soilMoisture")).orElse(toBigDecimal(soilData.get("土湿1")).orElse(toBigDecimal(soilData.get("土壤含水率")).orElse(BigDecimal.ZERO)));
            soilTemp1 = toBigDecimal(soilData.get("soilTemp1")).orElse(toBigDecimal(soilData.get("土温1")).orElse(BigDecimal.ZERO));
            soilTemp2 = toBigDecimal(soilData.get("soilTemp2")).orElse(toBigDecimal(soilData.get("土温2")).orElse(BigDecimal.ZERO));
            soilTemp3 = toBigDecimal(soilData.get("soilTemp3")).orElse(toBigDecimal(soilData.get("土温3")).orElse(BigDecimal.ZERO));
            soilMoisture1 = toBigDecimal(soilData.get("soilMoisture1")).orElse(toBigDecimal(soilData.get("土湿1")).orElse(BigDecimal.ZERO));
            soilMoisture2 = toBigDecimal(soilData.get("soilMoisture2")).orElse(toBigDecimal(soilData.get("土湿2")).orElse(BigDecimal.ZERO));
            soilMoisture3 = toBigDecimal(soilData.get("soilMoisture3")).orElse(toBigDecimal(soilData.get("土湿3")).orElse(BigDecimal.ZERO));
        }
        if (!meteoData.isEmpty()) {
            airTemp = toBigDecimal(meteoData.get("airTemp")).orElse(toBigDecimal(meteoData.get("空气温度")).orElse(toBigDecimal(meteoData.get("温度")).orElse(BigDecimal.ZERO)));
            relHumidity = toBigDecimal(meteoData.get("relHumidity")).orElse(toBigDecimal(meteoData.get("空气湿度")).orElse(toBigDecimal(meteoData.get("湿度")).orElse(BigDecimal.ZERO)));
            windSpeed = toBigDecimal(meteoData.get("windSpeed")).orElse(toBigDecimal(meteoData.get("风速")).orElse(BigDecimal.ZERO));
            solarRadiation = toBigDecimal(meteoData.get("solarRadiation")).orElse(toBigDecimal(meteoData.get("太阳辐射")).orElse(BigDecimal.ZERO));
            precip = toBigDecimal(meteoData.get("precipitation")).orElse(toBigDecimal(meteoData.get("降水量")).orElse(BigDecimal.ZERO));
        }
        
        log.info("解析后的实时数据 - 土壤含水率: {}%, 空气温度: {}℃, 湿度: {}%, 风速: {}m/s, 辐射: {}W/m², 降水: {}mm", 
                soilMoisture, airTemp, relHumidity, windSpeed, solarRadiation, precip);
        
        boolean hasValidData = soilMoisture.compareTo(BigDecimal.ZERO) > 0 || airTemp.compareTo(BigDecimal.ZERO) > 0;
        
        if (!hasValidData) {
            log.warn("基地{}传感器数据无效（均为0），返回空建议", baseId);
            r.put("chartDates", Collections.singletonList(java.time.LocalDate.now().toString()));
            r.put("et0Forecast", Collections.singletonList(BigDecimal.ZERO));
            r.put("soilMoistureSeriesPct", Collections.singletonList(BigDecimal.ZERO));
            r.put("penmanInputs", new HashMap<>());
            r.put("needIrrigation", false);
            r.put("recommendedTime", "暂无数据");
            r.put("method", "暂无数据");
            r.put("reason", "传感器数据获取失败或数据为0，请检查设备连接");
            r.put("recommendedVolumeMm", BigDecimal.ZERO);
            r.put("flowRateM3PerHour", BigDecimal.ZERO);
            return r;
        }
        
        BigDecimal et0 = calculateHourlyEt0(airTemp, relHumidity, windSpeed, solarRadiation);
        
        // 使用实时数据计算灌溉建议（简化版算法）
        Map<String, Object> irrigationAdvice = calculateIrrigationAdvice(
            soilMoisture, soilMoisture1, soilMoisture2, soilMoisture3,
            airTemp, relHumidity, precip, et0
        );
        
        boolean need = (boolean) irrigationAdvice.get("needIrrigation");
        String time = (String) irrigationAdvice.get("recommendedTime");
        String method = (String) irrigationAdvice.get("method");
        String reason = (String) irrigationAdvice.get("reason");
        BigDecimal vol = (BigDecimal) irrigationAdvice.get("recommendedVolumeMm");
        
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("dates", Collections.singletonList(java.time.LocalDate.now().toString()));
        inputs.put("temp", Collections.singletonList(airTemp));
        inputs.put("humidity", Collections.singletonList(relHumidity));
        inputs.put("wind", Collections.singletonList(windSpeed));
        inputs.put("solar", Collections.singletonList(solarRadiation.multiply(new BigDecimal("0.0036")).setScale(2, RoundingMode.HALF_UP)));
        inputs.put("precip", Collections.singletonList(precip));
        
        // 土壤详细数据
        Map<String, Object> soilDetail = new HashMap<>();
        soilDetail.put("soilTemp1", soilTemp1);
        soilDetail.put("soilTemp2", soilTemp2);
        soilDetail.put("soilTemp3", soilTemp3);
        soilDetail.put("soilMoisture1", soilMoisture1);
        soilDetail.put("soilMoisture2", soilMoisture2);
        soilDetail.put("soilMoisture3", soilMoisture3);
        
        r.put("chartDates", Collections.singletonList(java.time.LocalDate.now().toString()));
        r.put("et0Forecast", Collections.singletonList(et0));
        r.put("soilMoistureSeriesPct", Collections.singletonList(soilMoisture));
        r.put("penmanInputs", inputs);
        r.put("soilDetail", soilDetail);
        r.put("needIrrigation", need);
        r.put("recommendedTime", time);
        r.put("method", method);
        r.put("reason", reason);
        r.put("recommendedVolumeMm", vol);
        r.put("flowRateM3PerHour", BigDecimal.ZERO);
        
        log.info("基地{}Penman建议计算完成（实时数据）：是否需要灌溉={}，推荐灌水量={}mm", baseId, need, vol);
        return r;
    }

    public Map<String, Object> getInterventionComparisonByBase(String baseId) {
        log.info("开始生成基地{}的灌溉干预对比数据（实时数据模式）", baseId);
        Map<String, Object> r = new HashMap<>();
        
        String soilDeviceCode = getDeviceCodeByBaseId(baseId);
        String meteoDeviceCode = getMeteoDeviceCodeByBaseId(baseId);
        
        Map<String, Object> soilData = fetchRealtimeSensorData(soilDeviceCode);
        Map<String, Object> meteoData = fetchRealtimeSensorData(meteoDeviceCode);
        
        BigDecimal soilMoisture = BigDecimal.ZERO;
        BigDecimal solarRadiation = BigDecimal.ZERO;
        BigDecimal airTemp = BigDecimal.ZERO;
        BigDecimal relHumidity = BigDecimal.ZERO;
        BigDecimal windSpeed = BigDecimal.ZERO;
        
        if (!soilData.isEmpty()) {
            soilMoisture = toBigDecimal(soilData.get("soilMoisture")).orElse(toBigDecimal(soilData.get("土壤含水率")).orElse(BigDecimal.ZERO));
        }
        if (!meteoData.isEmpty()) {
            solarRadiation = toBigDecimal(meteoData.get("solarRadiation")).orElse(toBigDecimal(meteoData.get("太阳辐射")).orElse(BigDecimal.ZERO));
            airTemp = toBigDecimal(meteoData.get("airTemp")).orElse(toBigDecimal(meteoData.get("空气温度")).orElse(BigDecimal.ZERO));
            relHumidity = toBigDecimal(meteoData.get("relHumidity")).orElse(toBigDecimal(meteoData.get("空气湿度")).orElse(BigDecimal.ZERO));
            windSpeed = toBigDecimal(meteoData.get("windSpeed")).orElse(toBigDecimal(meteoData.get("风速")).orElse(BigDecimal.ZERO));
        }
        
        BigDecimal et0 = calculateHourlyEt0(airTemp, relHumidity, windSpeed, solarRadiation);
        
        List<String> dates = Collections.singletonList(java.time.LocalDate.now().toString());
        List<BigDecimal> withIrrigation = Collections.singletonList(soilMoisture);
        
        BigDecimal withoutValue = soilMoisture.subtract(et0);
        if (withoutValue.compareTo(BigDecimal.ZERO) < 0) withoutValue = BigDecimal.ZERO;
        List<BigDecimal> withoutIrrigation = Collections.singletonList(withoutValue);
        
        r.put("dates", dates);
        r.put("withIrrigation", withIrrigation);
        r.put("withoutIrrigation", withoutIrrigation);
        
        log.info("基地{}干预对比数据生成完成（实时数据）", baseId);
        return r;
    }

    private BigDecimal saturationVaporPressure(BigDecimal t) {
        BigDecimal b = t;
        double v = 0.6108 * Math.exp((17.27 * b.doubleValue()) / (b.doubleValue() + 237.3));
        return new BigDecimal(v);
    }

    private BigDecimal slopeVaporPressureCurve(BigDecimal t) {
        BigDecimal es = saturationVaporPressure(t);
        double v = 4098.0 * es.doubleValue() / Math.pow(t.doubleValue() + 237.3, 2);
        return new BigDecimal(v);
    }

    private BigDecimal psychrometricConstant() {
        return new BigDecimal("0.066");
    }

    private BigDecimal longwaveApprox(BigDecimal t, BigDecimal ea) {
        double tk = t.doubleValue() + 273.15;
        double sigma = 4.903e-9;
        double rnl = sigma * Math.pow(tk, 4) * (0.34 - 0.14 * Math.sqrt(ea.doubleValue())) * 0.8;
        return new BigDecimal(rnl);
    }

    private BigDecimal avg(List<BigDecimal> list) {
        List<BigDecimal> v = list.stream().filter(Objects::nonNull).collect(Collectors.toList());
        if (v.isEmpty()) return BigDecimal.ZERO;
        BigDecimal s = v.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return s.divide(new BigDecimal(v.size()), 6, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<BigDecimal> list) {
        List<BigDecimal> v = list.stream().filter(Objects::nonNull).collect(Collectors.toList());
        if (v.isEmpty()) return BigDecimal.ZERO;
        BigDecimal s = v.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return s.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal nz(BigDecimal v) { 
        return v == null ? BigDecimal.ZERO : v; 
    }

    private Map<String, Object> fetchRealtimeSensorData(String deviceCode) {
        Map<String, Object> result = new HashMap<>();
        log.info("开始获取设备{}的实时数据...", deviceCode);
        try {
            AtomicReference<Map<String, Object>> dataRef = new AtomicReference<>(new HashMap<>());
            var optionalResponse = ioTApiUtil.getSensorRealTimeData(deviceCode).blockOptional();
            if (optionalResponse.isEmpty()) {
                log.warn("设备{} API返回空响应", deviceCode);
                return result;
            }
            var response = optionalResponse.get();
            log.info("设备{} API响应: code={}, msg={}", deviceCode, response.getCode(), response.getMsg());
            if (response.getCode() == 1 && response.getData() != null) {
                log.info("设备{} 原始数据: {}", deviceCode, response.getData());
                dataRef.set(parseSensorData(response.getData()));
                log.info("设备{} 解析后数据: {}", deviceCode, dataRef.get());
            } else {
                log.warn("设备{} API返回异常: code={}, msg={}", deviceCode, response.getCode(), response.getMsg());
            }
            result = dataRef.get();
        } catch (Exception e) {
            log.error("获取设备{}实时数据失败: {}", deviceCode, e.getMessage(), e);
        }
        return result;
    }

    private Map<String, Object> parseSensorData(Object data) {
        Map<String, Object> parsed = new HashMap<>();
        if (data instanceof List) {
            List<?> list = (List<?>) data;
            for (Object item : list) {
                if (item instanceof Map) {
                    Map<?, ?> sensor = (Map<?, ?>) item;
                    String name = sensor.get("name") != null ? sensor.get("name").toString() : null;
                    Object qObj = sensor.get("q");
                    if (name != null && qObj instanceof Map) {
                        Map<?, ?> q = (Map<?, ?>) qObj;
                        Object sensorNum = q.get("sensorNum");
                        if (sensorNum instanceof Number) {
                            String key = mapSensorName(name);
                            parsed.put(key, new BigDecimal(((Number) sensorNum).toString()));
                            parsed.put(name, new BigDecimal(((Number) sensorNum).toString()));
                        }
                    }
                }
            }
        } else if (data instanceof Map) {
            Map<?, ?> dataMap = (Map<?, ?>) data;
            for (Map.Entry<?, ?> entry : dataMap.entrySet()) {
                String key = entry.getKey().toString();
                Object value = entry.getValue();
                if (value instanceof Number) {
                    parsed.put(key, new BigDecimal(value.toString()));
                } else {
                    parsed.put(key, value);
                }
            }
        }
        return parsed;
    }
    
    private String mapSensorName(String name) {
        if (name == null) return null;
        switch (name) {
            case "土湿1": case "土湿2": case "土湿3":
                return "soilMoisture";
            case "土温1": case "土温2": case "土温3":
                return "soilTemp";
            case "空气温度": case "温度":
                return "airTemp";
            case "空气湿度": case "湿度":
                return "relHumidity";
            case "风速":
                return "windSpeed";
            case "总辐射":
                return "solarRadiation";
            case "降雨量": case "降水量":
                return "precipitation";
            default:
                return name;
        }
    }

    private String getDeviceCodeByBaseId(String baseId) {
        String deviceCode = getSensorSerialByBaseIdAndType(baseId, 2, "土壤传感器");
        if (deviceCode != null) {
            return deviceCode;
        }
        log.warn("基地{}没有配置土壤传感器设备", baseId);
        return null;
    }

    private String getMeteoDeviceCodeByBaseId(String baseId) {
        String deviceCode = getSensorSerialByBaseIdAndType(baseId, 1, "气象站");
        if (deviceCode != null) {
            return deviceCode;
        }
        log.warn("基地{}没有配置气象站设备", baseId);
        return null;
    }

    private String getSensorSerialByBaseIdAndType(String baseId, Integer sensorTypeId, String deviceLabel) {
        if (baseId == null || baseId.isEmpty()) {
            log.warn("{}查询失败：baseId为空", deviceLabel);
            return null;
        }

        try {
            YoucaiBases base = basesMapper.selectById(baseId);
            if (base == null) {
                log.warn("基地{}不存在", baseId);
                return null;
            }
            return getSensorSerialByBaseNameAndType(base.getBaseName(), sensorTypeId, deviceLabel);
        } catch (Exception e) {
            log.warn("查询{}失败: {}", deviceLabel, e.getMessage());
            return null;
        }
    }

    private String getSensorSerialByBaseNameAndType(String baseName, Integer sensorTypeId, String deviceLabel) {
        List<String> keywords = buildBaseNameKeywords(baseName);
        if (keywords.isEmpty()) {
            log.warn("{}查询失败：基地名为空，sensorTypeId={}", deviceLabel, sensorTypeId);
            return null;
        }

        for (String keyword : keywords) {
            String sensorSerial = querySensorSerialByKeyword(sensorTypeId, keyword, true);
            if (sensorSerial != null) {
                log.info("通过基地名关键词前缀匹配到{}: baseName={}, keyword={}, sensorTypeId={}, code={}",
                    deviceLabel, baseName, keyword, sensorTypeId, sensorSerial);
                return sensorSerial;
            }
        }

        for (String keyword : keywords) {
            String sensorSerial = querySensorSerialByKeyword(sensorTypeId, keyword, false);
            if (sensorSerial != null) {
                log.info("通过基地名关键词模糊匹配到{}: baseName={}, keyword={}, sensorTypeId={}, code={}",
                    deviceLabel, baseName, keyword, sensorTypeId, sensorSerial);
                return sensorSerial;
            }
        }

        log.warn("未找到匹配的{}: baseName={}, sensorTypeId={}, keywords={}", deviceLabel, baseName, sensorTypeId, keywords);
        return null;
    }

    private String querySensorSerialByKeyword(Integer sensorTypeId, String keyword, boolean prefixMatch) {
        if (keyword == null || keyword.isEmpty()) {
            return null;
        }

        QueryWrapper<YoucaiSensorInfo> qw = new QueryWrapper<>();
        qw.lambda()
            .eq(YoucaiSensorInfo::getIsDelete, 0)
            .eq(YoucaiSensorInfo::getSensorTypeId, sensorTypeId)
            .isNotNull(YoucaiSensorInfo::getSensorSerial)
            .ne(YoucaiSensorInfo::getSensorSerial, "")
            .orderByAsc(YoucaiSensorInfo::getSensorName);
        if (prefixMatch) {
            qw.lambda().likeRight(YoucaiSensorInfo::getSensorName, keyword);
        } else {
            qw.lambda().like(YoucaiSensorInfo::getSensorName, keyword);
        }
        qw.last("limit 1");

        YoucaiSensorInfo sensor = sensorInfoMapper.selectOne(qw);
        return sensor != null ? sensor.getSensorSerial() : null;
    }

    private List<String> buildBaseNameKeywords(String baseName) {
        LinkedHashSet<String> keywords = new LinkedHashSet<>();
        if (baseName == null) {
            return new ArrayList<>(keywords);
        }

        String normalized = baseName.replaceAll("\\s+", "");
        if (normalized.isEmpty()) {
            return new ArrayList<>(keywords);
        }

        keywords.add(normalized);

        String withoutProjectSuffix = normalized.replaceAll("(基地|示范区|核心区)$", "");
        if (!withoutProjectSuffix.isEmpty()) {
            keywords.add(withoutProjectSuffix);
        }

        int firstRegionSuffixIndex = indexOfFirstRegionSuffix(normalized);
        if (firstRegionSuffixIndex > 0) {
            keywords.add(normalized.substring(0, firstRegionSuffixIndex));
        }

        String[] parts = normalized.split("[乡镇街道村社区]");
        for (String part : parts) {
            if (part != null && part.length() >= 2) {
                keywords.add(part);
            }
        }

        return new ArrayList<>(keywords);
    }

    private int indexOfFirstRegionSuffix(String baseName) {
        char[] suffixes = {'乡', '镇', '街', '道', '村', '社', '区'};
        int index = -1;
        for (char suffix : suffixes) {
            int currentIndex = baseName.indexOf(suffix);
            if (currentIndex > 0 && (index == -1 || currentIndex < index)) {
                index = currentIndex;
            }
        }
        return index;
    }

    private Optional<BigDecimal> toBigDecimal(Object value) {
        if (value == null) return Optional.empty();
        if (value instanceof BigDecimal) return Optional.of((BigDecimal) value);
        if (value instanceof Number) return Optional.of(new BigDecimal(((Number) value).toString()));
        try {
            return Optional.of(new BigDecimal(value.toString()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private BigDecimal calculateHourlyEt0(BigDecimal t, BigDecimal rh, BigDecimal u2, BigDecimal rsWm2) {
        if (t == null || t.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        
        BigDecimal rs = rsWm2.multiply(new BigDecimal("0.0036"));
        
        BigDecimal es = saturationVaporPressure(t);
        BigDecimal ea = rh.compareTo(BigDecimal.ZERO) > 0 
            ? es.multiply(rh.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP))
            : es.multiply(new BigDecimal("0.5"));
        BigDecimal delta = slopeVaporPressureCurve(t);
        BigDecimal gamma = psychrometricConstant();
        
        BigDecimal rns = rs.multiply(new BigDecimal("0.77"));
        BigDecimal rnl = longwaveApprox(t, ea);
        BigDecimal rn = rns.subtract(rnl);
        
        BigDecimal numRad = delta.multiply(rn).multiply(new BigDecimal("3600")).divide(new BigDecimal("2.45e6"), 8, RoundingMode.HALF_UP);
        BigDecimal numAer = gamma.multiply(new BigDecimal("900")).divide(t.add(new BigDecimal("273")), 8, RoundingMode.HALF_UP).multiply(u2).multiply(es.subtract(ea));
        BigDecimal denom = delta.add(gamma.multiply(new BigDecimal("1"))).max(new BigDecimal("1e-6"));
        
        BigDecimal et0 = numRad.add(numAer).divide(denom, 6, RoundingMode.HALF_UP);
        if (et0.compareTo(BigDecimal.ZERO) < 0) et0 = BigDecimal.ZERO;
        
        return et0.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * 根据实时传感器数据计算灌溉建议（简化算法）
     */
    private Map<String, Object> calculateIrrigationAdvice(
            BigDecimal soilMoisture,
            BigDecimal soilMoisture1, BigDecimal soilMoisture2, BigDecimal soilMoisture3,
            BigDecimal airTemp, BigDecimal relHumidity, BigDecimal precip, BigDecimal et0) {
        
        Map<String, Object> result = new HashMap<>();
        
        // 土壤湿度阈值（%）
        BigDecimal LOW_THRESHOLD = new BigDecimal("40");      // 干旱需要灌溉
        BigDecimal MEDIUM_THRESHOLD = new BigDecimal("55");  // 适中
        BigDecimal HIGH_THRESHOLD = new BigDecimal("70");    // 偏高
        
        // 判断土壤湿度状态
        String moistureLevel;
        boolean needIrrigation = false;
        
        if (soilMoisture.compareTo(LOW_THRESHOLD) < 0) {
            moistureLevel = "干旱";
            needIrrigation = true;
        } else if (soilMoisture.compareTo(MEDIUM_THRESHOLD) < 0) {
            moistureLevel = "适宜";
            needIrrigation = false;
        } else if (soilMoisture.compareTo(HIGH_THRESHOLD) < 0) {
            moistureLevel = "良好";
            needIrrigation = false;
        } else {
            moistureLevel = "过高";
            needIrrigation = false;
        }
        
        // 判断气象条件
        String weatherCondition = "";
        if (precip.compareTo(new BigDecimal("5")) > 0) {
            weatherCondition = "，近期有降雨";
        } else if (relHumidity.compareTo(new BigDecimal("80")) > 0) {
            weatherCondition = "，空气湿度较高";
        } else if (relHumidity.compareTo(new BigDecimal("40")) < 0) {
            weatherCondition = "，空气干燥";
        }
        
        // 判断土层湿度差异
        String layerStatus = "";
        BigDecimal avgMoisture = soilMoisture;
        if (soilMoisture1.subtract(soilMoisture3).compareTo(new BigDecimal("15")) > 0) {
            layerStatus = "，表层湿度明显高于深层";
        } else if (soilMoisture3.subtract(soilMoisture1).compareTo(new BigDecimal("15")) > 0) {
            layerStatus = "，深层湿度高于表层";
        }
        
        // 计算推荐灌水量
        BigDecimal recommendedVolume = BigDecimal.ZERO;
        if (needIrrigation) {
            // 目标含水率设为55%
            BigDecimal targetMoisture = new BigDecimal("55");
            BigDecimal deficit = targetMoisture.subtract(soilMoisture);
            if (deficit.compareTo(BigDecimal.ZERO) > 0) {
                // 简单估算：每1% deficit ≈ 0.67 mm/亩 (假设土层30cm，容重1.3)
                recommendedVolume = deficit.multiply(new BigDecimal("0.67")).setScale(1, RoundingMode.HALF_UP);
                if (recommendedVolume.compareTo(new BigDecimal("50")) > 0) {
                    recommendedVolume = new BigDecimal("40"); // 最多50mm/亩
                }
            }
        }
        
        // 生成建议时间
        String recommendedTime = "";
        String method = "";
        String reason = "";
        
        if (needIrrigation) {
            // 根据温度和时间决定灌溉时间
            if (airTemp.compareTo(new BigDecimal("30")) > 0) {
                recommendedTime = "建议清晨5:00-7:00或傍晚17:00后灌溉";
                method = "漫灌";
                reason = String.format("当前土壤含水率偏低(%s%%)%s，当前气温较高(%.1f℃)%s，蒸散强烈，需及时补充水分。",
                    soilMoisture.toPlainString(), layerStatus, airTemp, weatherCondition);
            } else if (airTemp.compareTo(new BigDecimal("20")) > 0) {
                recommendedTime = "建议上午8:00-10:00或下午15:00-17:00灌溉";
                method = "漫灌";
                reason = String.format("当前土壤含水率%s(%s%%)%s，气温适宜(%.1f℃)%s，建议适量灌溉。",
                    moistureLevel, soilMoisture.toPlainString(), layerStatus, airTemp, weatherCondition);
            } else {
                recommendedTime = "建议上午10:00-12:00灌溉";
                method = "漫灌";
                reason = String.format("当前土壤含水率%s(%s%%)%s，气温较低(%.1f℃)%s，灌溉量不宜过大。",
                    moistureLevel, soilMoisture.toPlainString(), layerStatus, airTemp, weatherCondition);
            }
        } else {
            if (moistureLevel.equals("良好")) {
                recommendedTime = "暂不需要灌溉";
                method = "保持现状";
                reason = String.format("当前土壤含水率充足(%s%%)%s%s，水分状况良好，无需灌溉。注意及时排水防止积水。",
                    soilMoisture.toPlainString(), layerStatus, weatherCondition);
            } else if (moistureLevel.equals("过高")) {
                recommendedTime = "暂不需要灌溉";
                method = "注意排水";
                reason = String.format("当前土壤含水率偏高(%s%%)%s%s，建议加强田间排水，避免根系腐烂。",
                    soilMoisture.toPlainString(), layerStatus, weatherCondition);
            } else {
                recommendedTime = "近期关注";
                method = "漫灌";
                reason = String.format("当前土壤含水率适宜(%s%%)%s%s，可维持当前灌溉策略。",
                    moistureLevel, soilMoisture.toPlainString(), layerStatus, weatherCondition);
            }
        }
        
        result.put("needIrrigation", needIrrigation);
        result.put("recommendedTime", recommendedTime);
        result.put("method", method);
        result.put("reason", reason);
        result.put("recommendedVolumeMm", recommendedVolume);
        result.put("moistureLevel", moistureLevel);
        result.put("et0", et0);
        
        return result;
    }
}
