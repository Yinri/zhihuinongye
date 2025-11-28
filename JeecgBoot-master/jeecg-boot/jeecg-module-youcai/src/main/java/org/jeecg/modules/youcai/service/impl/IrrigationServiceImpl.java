package org.jeecg.modules.youcai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.youcai.entity.YoucaiPlots;
import org.jeecg.modules.youcai.entity.YoucaiGrowthMonitoring;
import org.jeecg.modules.youcai.entity.YoucaiSensorHourly;
import org.jeecg.modules.youcai.mapper.YoucaiPlotsMapper;
import org.jeecg.modules.youcai.mapper.YoucaiSensorHourlyMapper;
import org.jeecg.modules.youcai.mapper.YoucaiGrowthMonitoringMapper;
import org.jeecg.modules.youcai.service.IIrrigationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

//zrt 灌溉监控服务实现类
@Service
public class IrrigationServiceImpl implements IIrrigationService {
    // 新增日志对象
    private static final Logger log = LoggerFactory.getLogger(IrrigationServiceImpl.class);

    @Autowired
    private YoucaiSensorHourlyMapper sensorHourlyMapper;
    @Autowired
    private YoucaiPlotsMapper plotsMapper;
    @Autowired
    private YoucaiGrowthMonitoringMapper growthMonitoringMapper;

    public Map<String, Object> getPlotStatus(String plotId) {
        log.info("开始查询地块{}的传感器状态数据", plotId);
        Map<String, Object> r = new HashMap<>();
        
        // 先查询地块信息，获取baseId（兼容plot_id为空的情况）
        YoucaiPlots plot = plotsMapper.selectById(plotId);
        if (plot == null) {
            log.warn("地块{}不存在，返回空数据", plotId);
            r.put("soilMoisturePercent", BigDecimal.ZERO);
            r.put("soilMoistureTrend", "稳定");
            r.put("currentStageId", "");
            r.put("lastUpdated", null);
            return r;
        }

        QueryWrapper<YoucaiSensorHourly> qw = new QueryWrapper<>();
        qw.lambda().eq(YoucaiSensorHourly::getDelFlag, 0)
                .eq(YoucaiSensorHourly::getPlotId, plotId);
        
        qw.lambda().orderByDesc(YoucaiSensorHourly::getHourTs)
                .last("limit 24");
        
        List<YoucaiSensorHourly> rows = sensorHourlyMapper.selectList(qw);
        log.info("地块{}（基地{}）查询到{}条传感器数据", plotId, plot.getBaseId(), rows.size());

        BigDecimal latest = rows.stream().map(YoucaiSensorHourly::getSoilMoisturePct).filter(Objects::nonNull).findFirst().orElse(BigDecimal.ZERO);
        List<BigDecimal> series = rows.stream().map(YoucaiSensorHourly::getSoilMoisturePct).filter(Objects::nonNull).collect(Collectors.toList());
        String trend = "稳定";
        if (series.size() >= 2) {
            BigDecimal d = series.get(0).subtract(series.get(series.size() - 1));
            trend = d.signum() > 0 ? "上升" : d.signum() < 0 ? "下降" : "稳定";
        }

        r.put("soilMoisturePercent", latest);
        r.put("soilMoistureTrend", trend);
        
        YoucaiGrowthMonitoring latestMonitor = growthMonitoringMapper.selectOne(
                new LambdaQueryWrapper<YoucaiGrowthMonitoring>()
                        .eq(YoucaiGrowthMonitoring::getPlotId, plotId)
                        .eq(YoucaiGrowthMonitoring::getDelFlag, 0)
                        .orderByDesc(YoucaiGrowthMonitoring::getMonitoringDate)
                        .orderByDesc(YoucaiGrowthMonitoring::getCreateTime)
                        .last("LIMIT 1")
        );
        r.put("currentStageId", latestMonitor != null ? (latestMonitor.getGrowthStage() == null ? "" : latestMonitor.getGrowthStage()) : "");
        r.put("lastUpdated", rows.stream().map(YoucaiSensorHourly::getHourTs).findFirst().orElse(null));
        
        log.info("地块{}状态查询完成，最新含水率：{}%，趋势：{}", plotId, latest, trend);
        return r;
    }

    public Map<String, Object> getPenmanPredict(String plotId) {
        log.info("开始计算地块{}的Penman灌溉建议", plotId);
        Map<String, Object> r = new HashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        // 先校验地块是否存在
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

        List<Map<String, Object>> days = aggregateDaily(plotId, 7);
        List<String> dates = days.stream().map(m -> (String) m.get("date")).collect(Collectors.toList());
        List<BigDecimal> et0 = days.stream().map(m -> (BigDecimal) m.get("et0Mm")).collect(Collectors.toList());
        List<BigDecimal> soilSeries = days.stream().map(m -> (BigDecimal) m.get("soilPct")).collect(Collectors.toList());
        BigDecimal latestMoisture = latestSoilMoisture(plotId);
        boolean need = latestMoisture.compareTo(new BigDecimal("45")) < 0;
        String time = need ? nextMorning() : "";
        String method = need ? "滴灌" : "";
        String reason = need ? "土壤含水率偏低且蒸散量较高" : "";
        BigDecimal vol = need ? recommendedVolume(latestMoisture) : BigDecimal.ZERO;
        
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("dates", dates);
        inputs.put("temp", days.stream().map(m -> (BigDecimal) m.get("tempC")).collect(Collectors.toList()));
        inputs.put("humidity", days.stream().map(m -> (BigDecimal) m.get("rhPct")).collect(Collectors.toList()));
        inputs.put("wind", days.stream().map(m -> (BigDecimal) m.get("windMs")).collect(Collectors.toList()));
        inputs.put("solar", days.stream().map(m -> (BigDecimal) m.get("solarMj")).collect(Collectors.toList()));
        inputs.put("precip", days.stream().map(m -> (BigDecimal) m.get("precipMm")).collect(Collectors.toList()));
        
        r.put("chartDates", dates);
        r.put("et0Forecast", et0);
        r.put("soilMoistureSeriesPct", soilSeries);
        r.put("penmanInputs", inputs);
        r.put("needIrrigation", need);
        r.put("recommendedTime", time);
        r.put("method", method);
        r.put("reason", reason);
        r.put("recommendedVolumeMm", vol);
        r.put("flowRateM3PerHour", BigDecimal.ZERO);
        
        log.info("地块{}Penman建议计算完成：是否需要灌溉={}，推荐灌水量={}mm", plotId, need, vol);
        return r;
    }

    public Map<String, Object> getInterventionComparison(String plotId) {
        log.info("开始生成地块{}的灌溉干预对比数据", plotId);
        Map<String, Object> r = new HashMap<>();
        
        // 先校验地块是否存在
        YoucaiPlots plot = plotsMapper.selectById(plotId);
        if (plot == null) {
            log.warn("地块{}不存在，返回空的对比数据", plotId);
            r.put("dates", new ArrayList<>());
            r.put("withIrrigation", new ArrayList<>());
            r.put("withoutIrrigation", new ArrayList<>());
            return r;
        }

        List<Map<String, Object>> days = aggregateDaily(plotId, 7);
        List<String> dates = days.stream().map(m -> (String) m.get("date")).collect(Collectors.toList());
        List<BigDecimal> moisture = days.stream().map(m -> (BigDecimal) m.get("soilPct")).collect(Collectors.toList());
        List<BigDecimal> et0 = days.stream().map(m -> (BigDecimal) m.get("et0Mm")).collect(Collectors.toList());
        
        List<BigDecimal> without = new ArrayList<>();
        for (int i = 0; i < moisture.size(); i++) {
            BigDecimal v = moisture.get(i).subtract(et0.get(i));
            if (v.compareTo(BigDecimal.ZERO) < 0) v = BigDecimal.ZERO;
            without.add(v);
        }
        
        r.put("dates", dates);
        r.put("withIrrigation", moisture);
        r.put("withoutIrrigation", without);
        
        log.info("地块{}干预对比数据生成完成，数据天数={}", plotId, dates.size());
        return r;
    }

    private BigDecimal latestSoilMoisture(String plotId) {
        // 兼容逻辑：优先plotId，无数据则用baseId
        YoucaiPlots plot = plotsMapper.selectById(plotId);
        if (plot == null) {
            return BigDecimal.ZERO;
        }
        
        QueryWrapper<YoucaiSensorHourly> qw = new QueryWrapper<>();
        qw.lambda().eq(YoucaiSensorHourly::getDelFlag, 0)
                .eq(YoucaiSensorHourly::getPlotId, plotId);
        
        qw.lambda().orderByDesc(YoucaiSensorHourly::getHourTs)
                .last("limit 1");
        
        YoucaiSensorHourly row = sensorHourlyMapper.selectOne(qw);
        return row != null && row.getSoilMoisturePct() != null ? row.getSoilMoisturePct() : BigDecimal.ZERO;
    }

    private List<Map<String, Object>> aggregateDaily(String plotId, int days) {
        // 兼容逻辑：优先plotId，无数据则用baseId
        YoucaiPlots plot = plotsMapper.selectById(plotId);
        if (plot == null) {
            return new ArrayList<>();
        }
        
        QueryWrapper<YoucaiSensorHourly> qw = new QueryWrapper<>();
        qw.lambda().eq(YoucaiSensorHourly::getDelFlag, 0)
                .eq(YoucaiSensorHourly::getPlotId, plotId);
        
        qw.lambda().orderByDesc(YoucaiSensorHourly::getHourTs)
                .last("limit " + (24 * days));
        
        List<YoucaiSensorHourly> rows = sensorHourlyMapper.selectList(qw);
        log.debug("地块{}聚合日数据：查询到{}小时数据", plotId, rows.size());

        Map<String, List<YoucaiSensorHourly>> byDay = new LinkedHashMap<>();
        rows.forEach(r -> {
            String d = r.getHourTs().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString();
            byDay.computeIfAbsent(d, k -> new ArrayList<>()).add(r);
        });

        List<Map<String, Object>> list = new ArrayList<>();
        for (Map.Entry<String, List<YoucaiSensorHourly>> e : byDay.entrySet()) {
            List<YoucaiSensorHourly> hs = e.getValue();
            BigDecimal temp = avg(hs.stream().map(YoucaiSensorHourly::getAirTempC).collect(Collectors.toList()));
            BigDecimal rh = avg(hs.stream().map(YoucaiSensorHourly::getRelHumidityPct).collect(Collectors.toList()));
            BigDecimal wind = avg(hs.stream().map(YoucaiSensorHourly::getWindSpeedMs).collect(Collectors.toList()));
            BigDecimal solarWm2 = sum(hs.stream().map(YoucaiSensorHourly::getSolarRadiationWm2).collect(Collectors.toList()));
            BigDecimal solarMj = solarWm2.multiply(new BigDecimal("0.0036")).setScale(2, RoundingMode.HALF_UP);
            BigDecimal precip = sum(hs.stream().map(YoucaiSensorHourly::getPrecipMm).collect(Collectors.toList()));
            BigDecimal et0 = sum(hs.stream().map(this::hourlyEt0).collect(Collectors.toList()));
            BigDecimal soil = avg(hs.stream().map(YoucaiSensorHourly::getSoilMoisturePct).collect(Collectors.toList()));
            
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("date", e.getKey());
            m.put("tempC", temp);
            m.put("rhPct", rh);
            m.put("windMs", wind);
            m.put("solarMj", solarMj);
            m.put("precipMm", precip);
            m.put("et0Mm", et0);
            m.put("soilPct", soil);
            list.add(m);
        }

        List<Map<String, Object>> last = list.stream().sorted(Comparator.comparing(m -> (String) m.get("date"))).collect(Collectors.toList());
        if (last.size() > days) last = last.subList(last.size() - days, last.size());
        
        log.debug("地块{}聚合日数据：生成{}天数据", plotId, last.size());
        return last;
    }

    private BigDecimal hourlyEt0(YoucaiSensorHourly r) {
        BigDecimal t = nz(r.getAirTempC());
        BigDecimal rh = nz(r.getRelHumidityPct());
        BigDecimal u2 = nz(r.getWindSpeedMs());
        BigDecimal rsWm2 = nz(r.getSolarRadiationWm2());
        BigDecimal rs = rsWm2.multiply(new BigDecimal("0.0036"));
        
        BigDecimal es = saturationVaporPressure(t);
        BigDecimal ea = es.multiply(rh.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP));
        BigDecimal delta = slopeVaporPressureCurve(t);
        BigDecimal gamma = psychrometricConstant();
        
        BigDecimal rns = rs.multiply(new BigDecimal("0.77"));
        BigDecimal rnl = longwaveApprox(t, ea);
        BigDecimal rn = rns.subtract(rnl);
        BigDecimal g = BigDecimal.ZERO;
        
        BigDecimal numRad = delta.multiply(rn).multiply(new BigDecimal("3600")).divide(new BigDecimal("2.45e6"), 8, RoundingMode.HALF_UP);
        BigDecimal numAer = gamma.multiply(new BigDecimal("900")).divide(t.add(new BigDecimal("273")), 8, RoundingMode.HALF_UP).multiply(u2).multiply(es.subtract(ea));
        BigDecimal denom = delta.add(gamma.multiply(new BigDecimal("1"))).max(new BigDecimal("1e-6"));
        
        BigDecimal et0 = numRad.add(numAer).divide(denom, 6, RoundingMode.HALF_UP);
        if (et0.compareTo(BigDecimal.ZERO) < 0) et0 = BigDecimal.ZERO;
        
        return et0.setScale(2, RoundingMode.HALF_UP);
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

    private String nextMorning() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, 1);
        c.set(Calendar.HOUR_OF_DAY, 6);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        Date d = c.getTime();
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    private BigDecimal recommendedVolume(BigDecimal moisture) {
        if (moisture.compareTo(new BigDecimal("35")) < 0) return new BigDecimal("30");
        if (moisture.compareTo(new BigDecimal("45")) < 0) return new BigDecimal("25");
        if (moisture.compareTo(new BigDecimal("55")) < 0) return new BigDecimal("20");
        if (moisture.compareTo(new BigDecimal("60")) < 0) return new BigDecimal("15");
        return new BigDecimal("10");
    }
}
