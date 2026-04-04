package org.jeecg.modules.youcai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.jeecg.modules.youcai.entity.YoucaiSensorInfo;
import org.jeecg.modules.youcai.entity.YoucaiBases;
import org.jeecg.modules.youcai.mapper.YoucaiSensorInfoMapper;
import org.jeecg.modules.youcai.mapper.YoucaiBasesMapper;
import org.jeecg.modules.youcai.service.IFertilizationService;
import org.jeecg.modules.youcai.util.IoTApiUtil;
import org.jeecg.modules.youcai.util.QueftsAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class FertilizationServiceImpl implements IFertilizationService {
    private static final Logger log = LoggerFactory.getLogger(FertilizationServiceImpl.class);

    @Autowired
    private YoucaiSensorInfoMapper sensorInfoMapper;

    @Autowired
    private YoucaiBasesMapper basesMapper;

    @Autowired
    private IoTApiUtil ioTApiUtil;

    @Override
    public Map<String, Object> getPlotStatusByBase(String baseId) {
        Map<String, Object> res = new HashMap<>();
        if (baseId == null || baseId.isEmpty()) {
            res.put("hasData", false);
            res.put("deviceNotConfigured", true);
            return res;
        }

        String soilDeviceCode = getSoilDeviceCodeByBaseId(baseId);
        if (soilDeviceCode == null) {
            res.put("hasData", false);
            res.put("deviceNotConfigured", true);
            res.put("deviceMessage", "该基地未配置物联网设备");
            log.info("基地{}没有配置土壤传感器设备", baseId);
            return res;
        }

        Map<String, Object> sensorData = fetchRealtimeSensorData(soilDeviceCode);
        if (sensorData.isEmpty()) {
            res.put("hasData", false);
            res.put("deviceNotConfigured", true);
            res.put("deviceMessage", "物联网设备数据获取失败");
            return res;
        }

        BigDecimal ec1 = toBigDecimal(sensorData.get("土壤电导率1")).orElse(BigDecimal.ZERO);
        BigDecimal ec2 = toBigDecimal(sensorData.get("土壤电导率2")).orElse(BigDecimal.ZERO);
        BigDecimal ec3 = toBigDecimal(sensorData.get("土壤电导率3")).orElse(BigDecimal.ZERO);

        BigDecimal soilTemp1 = toBigDecimal(sensorData.get("土温1")).orElse(BigDecimal.ZERO);
        BigDecimal soilTemp2 = toBigDecimal(sensorData.get("土温2")).orElse(BigDecimal.ZERO);
        BigDecimal soilTemp3 = toBigDecimal(sensorData.get("土温3")).orElse(BigDecimal.ZERO);

        BigDecimal soilMoist1 = toBigDecimal(sensorData.get("土湿1")).orElse(BigDecimal.ZERO);
        BigDecimal soilMoist2 = toBigDecimal(sensorData.get("土湿2")).orElse(BigDecimal.ZERO);
        BigDecimal soilMoist3 = toBigDecimal(sensorData.get("土湿3")).orElse(BigDecimal.ZERO);

        BigDecimal nitrogen = estimateNitrogenFromEC(ec1, ec2, ec3);
        BigDecimal phosphorus = estimatePhosphorusFromEC(ec1, ec2, ec3);
        BigDecimal potassium = estimatePotassiumFromEC(ec1, ec2, ec3);

        BigDecimal total = nitrogen.add(phosphorus).add(potassium);
        BigDecimal percN = total.compareTo(BigDecimal.ZERO) > 0 
            ? nitrogen.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP) 
            : BigDecimal.ZERO;
        BigDecimal percP = total.compareTo(BigDecimal.ZERO) > 0 
            ? phosphorus.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP) 
            : BigDecimal.ZERO;
        BigDecimal percK = total.compareTo(BigDecimal.ZERO) > 0 
            ? potassium.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP) 
            : BigDecimal.ZERO;

        res.put("hasData", true);
        res.put("deviceNotConfigured", false);
        res.put("latestDate", new Date());
        res.put("nPercent", percN);
        res.put("pPercent", percP);
        res.put("kPercent", percK);
        res.put("ec1", ec1);
        res.put("ec2", ec2);
        res.put("ec3", ec3);
        res.put("soilTemp1", soilTemp1);
        res.put("soilTemp2", soilTemp2);
        res.put("soilTemp3", soilTemp3);
        res.put("soilMoist1", soilMoist1);
        res.put("soilMoist2", soilMoist2);
        res.put("soilMoist3", soilMoist3);
        res.put("estimatedN", nitrogen);
        res.put("estimatedP", phosphorus);
        res.put("estimatedK", potassium);

        return res;
    }

    @Override
    public Map<String, Object> getBaseSoilSeries(String baseId) {
        Map<String, Object> res = new HashMap<>();
        if (baseId == null || baseId.isEmpty()) {
            res.put("hasData", false);
            res.put("deviceNotConfigured", true);
            return res;
        }

        String soilDeviceCode = getSoilDeviceCodeByBaseId(baseId);
        if (soilDeviceCode == null) {
            res.put("hasData", false);
            res.put("deviceNotConfigured", true);
            res.put("deviceMessage", "该基地未配置物联网设备");
            return res;
        }

        Map<String, Object> sensorData = fetchRealtimeSensorData(soilDeviceCode);
        if (sensorData.isEmpty()) {
            res.put("hasData", false);
            res.put("deviceNotConfigured", true);
            return res;
        }

        BigDecimal ec1 = toBigDecimal(sensorData.get("土壤电导率1")).orElse(BigDecimal.ZERO);
        BigDecimal ec2 = toBigDecimal(sensorData.get("土壤电导率2")).orElse(BigDecimal.ZERO);
        BigDecimal ec3 = toBigDecimal(sensorData.get("土壤电导率3")).orElse(BigDecimal.ZERO);

        res.put("hasData", true);
        res.put("deviceNotConfigured", false);
        res.put("dates", List.of(new java.text.SimpleDateFormat("yyyy-MM-dd").format(new Date())));
        res.put("avgN", List.of(estimateNitrogenFromEC(ec1, ec2, ec3)));
        res.put("avgP", List.of(estimatePhosphorusFromEC(ec1, ec2, ec3)));
        res.put("avgK", List.of(estimatePotassiumFromEC(ec1, ec2, ec3)));

        return res;
    }

    @Override
    public Map<String, Object> getBaseRecommendation(String baseId) {
        Map<String, Object> res = new HashMap<>();
        if (baseId == null || baseId.isEmpty()) {
            res.put("hasData", false);
            res.put("deviceNotConfigured", true);
            return res;
        }

        String soilDeviceCode = getSoilDeviceCodeByBaseId(baseId);
        if (soilDeviceCode == null) {
            res.put("hasData", false);
            res.put("deviceNotConfigured", true);
            res.put("deviceMessage", "该基地未配置物联网设备，无法获取实时数据");
            res.put("reason", "该基地未配置物联网设备，请先在传感器管理中添加土壤传感器设备");
            log.info("基地{}没有配置土壤传感器设备，无法计算施肥推荐", baseId);
            return res;
        }

        Map<String, Object> sensorData = fetchRealtimeSensorData(soilDeviceCode);
        if (sensorData.isEmpty()) {
            res.put("hasData", false);
            res.put("deviceNotConfigured", true);
            res.put("deviceMessage", "物联网设备数据获取失败");
            res.put("reason", "传感器数据获取失败，请检查设备连接");
            return res;
        }

        BigDecimal ec1 = toBigDecimal(sensorData.get("土壤电导率1")).orElse(BigDecimal.ZERO);
        BigDecimal ec2 = toBigDecimal(sensorData.get("土壤电导率2")).orElse(BigDecimal.ZERO);
        BigDecimal ec3 = toBigDecimal(sensorData.get("土壤电导率3")).orElse(BigDecimal.ZERO);

        BigDecimal soilMoist1 = toBigDecimal(sensorData.get("土湿1")).orElse(BigDecimal.ZERO);
        BigDecimal soilMoist2 = toBigDecimal(sensorData.get("土湿2")).orElse(BigDecimal.ZERO);
        BigDecimal soilMoist3 = toBigDecimal(sensorData.get("土湿3")).orElse(BigDecimal.ZERO);
        BigDecimal avgMoisture = avg(List.of(soilMoist1, soilMoist2, soilMoist3));

        BigDecimal nitrogen = estimateNitrogenFromEC(ec1, ec2, ec3);
        BigDecimal phosphorus = estimatePhosphorusFromEC(ec1, ec2, ec3);
        BigDecimal potassium = estimatePotassiumFromEC(ec1, ec2, ec3);

        QueftsAlgorithm.SoilFertilityData virtualSoil = new QueftsAlgorithm.SoilFertilityData();
        virtualSoil.setNitrogen(nitrogen);
        virtualSoil.setPhosphorus(phosphorus);
        virtualSoil.setPotassium(potassium);

        BigDecimal targetYield = new BigDecimal("180");
        org.jeecg.modules.youcai.entity.YoucaiFertilization recommendation = 
            QueftsAlgorithm.calculateRecommendation(targetYield, virtualSoil);

        boolean need = recommendation.getNRecommendKgPerMu().compareTo(BigDecimal.ZERO) > 0
                || recommendation.getP2o5RecommendKgPerMu().compareTo(BigDecimal.ZERO) > 0
                || recommendation.getK2oRecommendKgPerMu().compareTo(BigDecimal.ZERO) > 0;

        String adjustedTime = adjustFertilizationTime(avgMoisture, nitrogen);
        String adjustedMethod = adjustFertilizationMethod(ec1, ec2, ec3, avgMoisture);

        res.put("hasData", true);
        res.put("deviceNotConfigured", false);
        res.put("needFertilization", need);
        res.put("recommendedTime", adjustedTime);
        res.put("method", adjustedMethod);
        res.put("recommendN", recommendation.getNRecommendKgPerMu());
        res.put("recommendP2O5", recommendation.getP2o5RecommendKgPerMu());
        res.put("recommendK2O", recommendation.getK2oRecommendKgPerMu());
        res.put("reason", buildFertilizationReason(nitrogen, phosphorus, potassium, recommendation, ec1, ec2, ec3));
        res.put("source", "iot");
        res.put("sensorEC1", ec1);
        res.put("sensorEC2", ec2);
        res.put("sensorEC3", ec3);
        res.put("estimatedN", nitrogen);
        res.put("estimatedP", phosphorus);
        res.put("estimatedK", potassium);

        return res;
    }

    private String getSoilDeviceCodeByBaseId(String baseId) {
        String hardcodedCode = getHardcodedDeviceCode(baseId);
        if (hardcodedCode != null) {
            log.info("通过硬编码映射获取土壤设备: baseId={}, code={}", baseId, hardcodedCode);
            return hardcodedCode;
        }

        try {
            YoucaiBases base = basesMapper.selectById(baseId);
            if (base == null) {
                log.warn("基地{}不存在", baseId);
                return null;
            }
            BigDecimal baseLat = base.getLatitude();
            BigDecimal baseLon = base.getLongitude();
            if (baseLat == null || baseLon == null) {
                return getSensorSerialByNameMatch(base.getBaseName());
            }

            QueryWrapper<YoucaiSensorInfo> qw = new QueryWrapper<>();
            qw.lambda()
                .eq(YoucaiSensorInfo::getIsDelete, 0)
                .eq(YoucaiSensorInfo::getSensorTypeId, 2)
                .apply("ABS(CAST(latitude AS DECIMAL(10,6)) - CAST({0} AS DECIMAL(10,6))) < 0.01", baseLat.toPlainString())
                .apply("ABS(CAST(longitude AS DECIMAL(10,6)) - CAST({0} AS DECIMAL(10,6))) < 0.01", baseLon.toPlainString())
                .last("limit 1");

            YoucaiSensorInfo sensor = sensorInfoMapper.selectOne(qw);
            if (sensor != null && sensor.getSensorSerial() != null) {
                return sensor.getSensorSerial();
            }

            return getSensorSerialByNameMatch(base.getBaseName());
        } catch (Exception e) {
            log.warn("查询土壤传感器设备失败: {}", e.getMessage());
            return null;
        }
    }

    private String getSensorSerialByNameMatch(String baseName) {
        if (baseName == null) return null;
        try {
            QueryWrapper<YoucaiSensorInfo> qw = new QueryWrapper<>();
            qw.lambda()
                .eq(YoucaiSensorInfo::getIsDelete, 0)
                .eq(YoucaiSensorInfo::getSensorTypeId, 2)
                .like(YoucaiSensorInfo::getSensorName, baseName);
            List<YoucaiSensorInfo> sensors = sensorInfoMapper.selectList(qw);
            if (sensors != null && !sensors.isEmpty()) {
                return sensors.get(0).getSensorSerial();
            }
        } catch (Exception e) {
            log.warn("名称匹配传感器失败: {}", e.getMessage());
        }
        return null;
    }

    private String getHardcodedDeviceCode(String baseId) {
        Map<String, String> deviceMap = new HashMap<>();
        deviceMap.put("1992822087463333889", "862635068101757");
        deviceMap.put("1992821962494046210", "862635068112473");
        deviceMap.put("1992821272807862273", "862635068112416");
        deviceMap.put("1992821484976730114", "862635068112390");
        return deviceMap.get(baseId);
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
            if (response.getCode() == 1 && response.getData() != null) {
                dataRef.set(parseSensorData(response.getData()));
                log.info("设备{} 解析后数据: {}", deviceCode, dataRef.get());
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
                            parsed.put(name, new BigDecimal(((Number) sensorNum).toString()));
                        }
                    }
                }
            }
        }
        return parsed;
    }

    private BigDecimal estimateNitrogenFromEC(BigDecimal ec1, BigDecimal ec2, BigDecimal ec3) {
        BigDecimal avgEC = avg(List.of(ec1, ec2, ec3));
        if (avgEC.compareTo(new BigDecimal("0.5")) < 0) {
            return new BigDecimal("30");
        } else if (avgEC.compareTo(new BigDecimal("1.0")) < 0) {
            return new BigDecimal("50");
        } else if (avgEC.compareTo(new BigDecimal("2.0")) < 0) {
            return new BigDecimal("80");
        } else {
            return new BigDecimal("100");
        }
    }

    private BigDecimal estimatePhosphorusFromEC(BigDecimal ec1, BigDecimal ec2, BigDecimal ec3) {
        BigDecimal avgEC = avg(List.of(ec1, ec2, ec3));
        if (avgEC.compareTo(new BigDecimal("0.3")) < 0) {
            return new BigDecimal("10");
        } else if (avgEC.compareTo(new BigDecimal("0.8")) < 0) {
            return new BigDecimal("20");
        } else if (avgEC.compareTo(new BigDecimal("1.5")) < 0) {
            return new BigDecimal("30");
        } else {
            return new BigDecimal("40");
        }
    }

    private BigDecimal estimatePotassiumFromEC(BigDecimal ec1, BigDecimal ec2, BigDecimal ec3) {
        BigDecimal avgEC = avg(List.of(ec1, ec2, ec3));
        if (avgEC.compareTo(new BigDecimal("0.3")) < 0) {
            return new BigDecimal("50");
        } else if (avgEC.compareTo(new BigDecimal("0.8")) < 0) {
            return new BigDecimal("80");
        } else if (avgEC.compareTo(new BigDecimal("1.5")) < 0) {
            return new BigDecimal("120");
        } else {
            return new BigDecimal("150");
        }
    }

    private String adjustFertilizationTime(BigDecimal soilMoisture, BigDecimal nitrogen) {
        if (soilMoisture.compareTo(new BigDecimal("30")) < 0) {
            return "立即灌溉后施肥（土壤偏干）";
        } else if (soilMoisture.compareTo(new BigDecimal("50")) < 0) {
            return "未来1-2天晴好天气施肥";
        } else {
            return "未来3-5天施肥";
        }
    }

    private String adjustFertilizationMethod(BigDecimal ec1, BigDecimal ec2, BigDecimal ec3, BigDecimal moisture) {
        BigDecimal avgEC = avg(List.of(ec1, ec2, ec3));
        if (avgEC.compareTo(new BigDecimal("2.0")) > 0) {
            return "滴灌施肥（减少盐分积累）";
        } else if (moisture.compareTo(new BigDecimal("60")) > 0) {
            return "撒施后浅耕（土壤湿度适中）";
        } else {
            return "水肥一体化（推荐）";
        }
    }

    private String buildFertilizationReason(BigDecimal n, BigDecimal p, BigDecimal k, 
            org.jeecg.modules.youcai.entity.YoucaiFertilization rec,
            BigDecimal ec1, BigDecimal ec2, BigDecimal ec3) {
        StringBuilder sb = new StringBuilder();
        sb.append("基于物联网传感器数据（土壤电导率1/2/3: ");
        sb.append(ec1).append("/").append(ec2).append("/").append(ec3).append(" mS/cm)估算养分：");
        sb.append("氮约").append(n).append("mg/kg, ");
        sb.append("磷约").append(p).append("mg/kg, ");
        sb.append("钾约").append(k).append("mg/kg。");
        
        if (rec.getNRecommendKgPerMu().compareTo(BigDecimal.ZERO) > 0) {
            sb.append("需补充氮肥").append(rec.getNRecommendKgPerMu()).append("kg/亩；");
        }
        if (rec.getP2o5RecommendKgPerMu().compareTo(BigDecimal.ZERO) > 0) {
            sb.append("磷肥").append(rec.getP2o5RecommendKgPerMu()).append("kg/亩；");
        }
        if (rec.getK2oRecommendKgPerMu().compareTo(BigDecimal.ZERO) > 0) {
            sb.append("钾肥").append(rec.getK2oRecommendKgPerMu()).append("kg/亩。");
        }
        
        BigDecimal avgEC = avg(List.of(ec1, ec2, ec3));
        if (avgEC.compareTo(new BigDecimal("2.0")) > 0) {
            sb.append("注意：土壤电导率偏高，建议采用水肥一体化方式降低盐分积累风险。");
        }
        
        return sb.toString();
    }

    private BigDecimal avg(List<BigDecimal> values) {
        if (values == null || values.isEmpty()) return BigDecimal.ZERO;
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        for (BigDecimal v : values) {
            if (v != null) {
                sum = sum.add(v);
                count++;
            }
        }
        return count == 0 ? BigDecimal.ZERO : sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
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
}