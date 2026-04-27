package org.jeecg.modules.youcai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.youcai.dto.LodgingRiskAssessmentResponseDTO;
import org.jeecg.modules.youcai.dto.decision.YoucaiDecisionGrowthDTO;
import org.jeecg.modules.youcai.dto.decision.YoucaiDecisionHeightRiskDTO;
import org.jeecg.modules.youcai.dto.decision.YoucaiDecisionLodgingDTO;
import org.jeecg.modules.youcai.dto.decision.YoucaiDecisionPestDTO;
import org.jeecg.modules.youcai.dto.decision.YoucaiDecisionSoilDTO;
import org.jeecg.modules.youcai.dto.decision.YoucaiDecisionYieldDTO;
import org.jeecg.modules.youcai.entity.YoucaiBases;
import org.jeecg.modules.youcai.entity.YoucaiFarmingRecords;
import org.jeecg.modules.youcai.entity.YoucaiFertilization;
import org.jeecg.modules.youcai.entity.YoucaiGrowthMonitoring;
import org.jeecg.modules.youcai.entity.YoucaiHistoricalYield;
import org.jeecg.modules.youcai.entity.YoucaiPlots;
import org.jeecg.modules.youcai.mapper.YoucaiFertilizationMapper;
import org.jeecg.modules.youcai.service.IIrrigationService;
import org.jeecg.modules.youcai.service.IFertilizationService;
import org.jeecg.modules.youcai.service.IYoucaiBasesService;
import org.jeecg.modules.youcai.service.IYoucaiDecisionModelService;
import org.jeecg.modules.youcai.service.IYoucaiFarmingRecordsService;
import org.jeecg.modules.youcai.service.IYoucaiGrowthMonitoringService;
import org.jeecg.modules.youcai.service.IYoucaiHistoricalYieldService;
import org.jeecg.modules.youcai.service.IYoucaiLodgingRiskWarningService;
import org.jeecg.modules.youcai.service.IYoucaiPestControlService;
import org.jeecg.modules.youcai.service.IYoucaiPlotsService;
import org.jeecg.modules.youcai.service.IYoucaiColorQualityService;
import org.jeecg.modules.youcai.entity.YoucaiColorQuality;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class YoucaiDecisionModelServiceImpl implements IYoucaiDecisionModelService {

    @Autowired
    private IYoucaiBasesService basesService;

    @Autowired
    private IYoucaiColorQualityService colorQualityService;

    @Autowired
    private IYoucaiPestControlService pestControlService;

    @Autowired
    private IYoucaiPlotsService plotsService;

    @Autowired
    private IYoucaiHistoricalYieldService historicalYieldService;

    @Autowired
    private IYoucaiFarmingRecordsService farmingRecordsService;

    @Autowired
    private YoucaiFertilizationMapper fertilizationMapper;

    @Autowired
    private IIrrigationService irrigationService;

    @Autowired
    private IFertilizationService fertilizationService;

    @Autowired
    private IYoucaiLodgingRiskWarningService lodgingRiskWarningService;

    @Autowired
    private IYoucaiGrowthMonitoringService growthMonitoringService;

    @Override
    public List<YoucaiDecisionYieldDTO> getInterface1Data() {
        List<YoucaiDecisionYieldDTO> result = new ArrayList<>();
        for (YoucaiBases base : listDecisionBases()) {
            result.add(buildYieldData(base));
        }
        return result;
    }

    @Override
    public List<YoucaiDecisionPestDTO> getInterface2Data() {
        List<YoucaiDecisionPestDTO> result = new ArrayList<>();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);
        for (YoucaiBases base : listDecisionBases()) {
            result.add(buildPestData(base, startDate, endDate));
        }
        return result;
    }

    @Override
    public List<YoucaiDecisionSoilDTO> getInterface3Data() {
        List<YoucaiDecisionSoilDTO> result = new ArrayList<>();
        for (YoucaiBases base : listDecisionBases()) {
            result.add(buildSoilData(base));
        }
        return result;
    }

    @Override
    public List<YoucaiDecisionLodgingDTO> getInterface4Data() {
        List<YoucaiDecisionLodgingDTO> result = new ArrayList<>();
        for (YoucaiBases base : listDecisionBases()) {
            result.add(buildLodgingData(base));
        }
        return result;
    }

    @Override
    public List<YoucaiDecisionGrowthDTO> getInterface5Data() {
        List<YoucaiDecisionGrowthDTO> result = new ArrayList<>();
        for (YoucaiBases base : listDecisionBases()) {
            result.add(buildGrowthIndexData(base));
        }
        return result;
    }

    @Override
    public List<YoucaiDecisionHeightRiskDTO> getInterface6Data() {
        List<YoucaiDecisionHeightRiskDTO> result = new ArrayList<>();
        for (YoucaiBases base : listDecisionBases()) {
            result.add(buildHeightRiskTrendData(base));
        }
        return result;
    }

    private List<YoucaiBases> listDecisionBases() {
        QueryWrapper<YoucaiBases> siteQuery = new QueryWrapper<>();
        siteQuery.likeRight("id", "site").orderByAsc("id");
        List<YoucaiBases> bases = basesService.list(siteQuery);
        if (!CollectionUtils.isEmpty(bases)) {
            return bases;
        }
        QueryWrapper<YoucaiBases> fallbackQuery = new QueryWrapper<>();
        fallbackQuery.orderByAsc("id");
        return basesService.list(fallbackQuery);
    }

    private YoucaiDecisionYieldDTO buildYieldData(YoucaiBases base) {
        YoucaiDecisionYieldDTO dto = new YoucaiDecisionYieldDTO();
        dto.setSid(base.getId());
        dto.setNam(base.getBaseName());
        dto.setStg(resolveGrowthStage(base.getId()));
        dto.setSqr(formatArea(base.getArea()));
        dto.setNmb(formatDecimal(calculatePredictedYield(base), 2));
        return dto;
    }

    private YoucaiDecisionPestDTO buildPestData(YoucaiBases base, LocalDate startDate, LocalDate endDate) {
        YoucaiDecisionPestDTO dto = new YoucaiDecisionPestDTO();
        dto.setSid(base.getId());
        dto.setNam(base.getBaseName());

        List<Map<String, Object>> pestImages = fetchPestImages(base.getBaseName(), startDate, endDate);
        Map<String, Integer> pestCountMap = aggregatePestCounts(pestImages);
        List<Map.Entry<String, Integer>> sortedPests = new ArrayList<>(pestCountMap.entrySet());
        sortedPests.sort(Comparator
                .comparing(Map.Entry<String, Integer>::getValue, Comparator.reverseOrder())
                .thenComparing(Map.Entry::getKey));

        List<YoucaiDecisionPestDTO.PestItem> topPests = new ArrayList<>();
        int limit = Math.min(3, sortedPests.size());
        for (int i = 0; i < limit; i++) {
            Map.Entry<String, Integer> entry = sortedPests.get(i);
            YoucaiDecisionPestDTO.PestItem pestItem = new YoucaiDecisionPestDTO.PestItem();
            pestItem.setName(entry.getKey());
            pestItem.setValue(entry.getValue());
            topPests.add(pestItem);
        }
        dto.setBug(topPests);
        dto.setSgt(buildPestSuggestion(topPests));
        return dto;
    }

    private YoucaiDecisionSoilDTO buildSoilData(YoucaiBases base) {
        YoucaiDecisionSoilDTO dto = new YoucaiDecisionSoilDTO();
        dto.setSid(base.getId());
        dto.setNam(base.getBaseName());

        Map<String, Object> irrigationStatus = safeMap(irrigationService.getPlotStatusByBase(base.getId()));
        Map<String, Object> irrigationAdvice = safeMap(irrigationService.getPenmanPredictByBase(base.getId()));
        Map<String, Object> fertilizationStatus = safeMap(fertilizationService.getPlotStatusByBase(base.getId()));
        Map<String, Object> fertilizationAdvice = safeMap(fertilizationService.getBaseRecommendation(base.getId()));

        BigDecimal moisture = toBigDecimal(irrigationStatus.get("soilMoisturePercent"));
        BigDecimal avgEc = averagePositive(
                toBigDecimal(fertilizationStatus.get("ec1")),
                toBigDecimal(fertilizationStatus.get("ec2")),
                toBigDecimal(fertilizationStatus.get("ec3"))
        );

        YoucaiDecisionSoilDTO.Metric waterMetric = new YoucaiDecisionSoilDTO.Metric();
        waterMetric.setNmb(formatDecimal(moisture, 2));
        waterMetric.setLvl(resolveWaterLevel(moisture));
        waterMetric.setSgt(buildWaterSuggestion(irrigationAdvice));

        YoucaiDecisionSoilDTO.Metric ecMetric = new YoucaiDecisionSoilDTO.Metric();
        ecMetric.setNmb(formatDecimal(avgEc, 3));
        ecMetric.setLvl(resolveEcLevel(avgEc));
        ecMetric.setSgt(buildEcSuggestion(fertilizationAdvice));

        dto.setWtr(waterMetric);
        dto.setEcz(ecMetric);
        return dto;
    }

    private YoucaiDecisionLodgingDTO buildLodgingData(YoucaiBases base) {
        YoucaiDecisionLodgingDTO dto = new YoucaiDecisionLodgingDTO();
        dto.setSid(base.getId());
        dto.setNam(base.getBaseName());

        Map<String, YoucaiGrowthMonitoring> latestGrowthMap = mapLatestGrowthMonitoringByBase(base.getId());
        BigDecimal avgHeight = averageNullable(extractPlantHeights(latestGrowthMap.values()));

        int riskLevel = 1;
        List<BigDecimal> probabilities = new ArrayList<>();
        String suggestion = "";
        try {
            LodgingRiskAssessmentResponseDTO.BatchLodgingRiskAssessmentResponseDTO batchRisk =
                    lodgingRiskWarningService.batchRiskAssessmentByBaseId(base.getId());
            if (batchRisk != null && !CollectionUtils.isEmpty(batchRisk.getPlotRisks())) {
                riskLevel = Math.max(riskLevel, resolveBatchRiskLevel(batchRisk));
                probabilities.addAll(extractProbabilityValues(batchRisk.getPlotRisks()));
                suggestion = buildLodgingSuggestion(batchRisk.getPlotRisks(), base.getBaseName(), riskLevel);
            }
        } catch (Exception e) {
            log.warn("获取基地{}倒伏预警失败: {}", base.getId(), e.getMessage());
        }

        if (avgHeight == null) {
            avgHeight = defaultHeightByStage(resolveGrowthStage(base.getId()));
        }
        if (CollectionUtils.isEmpty(probabilities)) {
            probabilities.add(defaultLodgingLow(riskLevel));
            probabilities.add(defaultLodgingHigh(riskLevel));
        }
        if (!StringUtils.hasText(suggestion)) {
            suggestion = buildDefaultLodgingSuggestion(base.getBaseName(), riskLevel);
        }

        dto.setRsk(String.valueOf(riskLevel));
        dto.setHgt(formatStrippedDecimal(avgHeight, 0));
        dto.setLow(formatStrippedDecimal(Collections.min(probabilities), 0));
        dto.setHig(formatStrippedDecimal(Collections.max(probabilities), 0));
        dto.setSgt(suggestion);
        return dto;
    }

    private YoucaiDecisionGrowthDTO buildGrowthIndexData(YoucaiBases base) {
        YoucaiDecisionGrowthDTO dto = new YoucaiDecisionGrowthDTO();
        dto.setSid(base.getId());
        dto.setNam(base.getBaseName());

        Map<String, YoucaiGrowthMonitoring> latestGrowthMap = mapLatestGrowthMonitoringByBase(base.getId());
        List<YoucaiGrowthMonitoring> latestGrowthList = new ArrayList<>(latestGrowthMap.values());

        Map<String, Object> irrigationStatus = safeMap(irrigationService.getPlotStatusByBase(base.getId()));
        Map<String, Object> irrigationAdvice = safeMap(irrigationService.getPenmanPredictByBase(base.getId()));
        Map<String, Object> fertilizationStatus = safeMap(fertilizationService.getPlotStatusByBase(base.getId()));
        Map<String, Object> fertilizationAdvice = safeMap(fertilizationService.getBaseRecommendation(base.getId()));
        YoucaiDecisionPestDTO pestData = buildPestData(base, LocalDate.now().minusDays(6), LocalDate.now());
        YoucaiDecisionLodgingDTO lodgingData = buildLodgingData(base);

        BigDecimal colorIndex = getLatestColorIndex(base.getId());

        int rvi = calculateRvi(latestGrowthList, colorIndex);
        int nbi = calculateNbi(fertilizationStatus, colorIndex);
        int gsi = calculateGsi(latestGrowthList);
        int sei = calculateSei(irrigationStatus, fertilizationStatus, pestData, lodgingData);

        Date latestMonitoringDate = latestMonitoringDate(latestGrowthList);
        dto.setDat(formatDateTime(latestMonitoringDate == null ? new Date() : latestMonitoringDate));
        dto.setRvi(String.valueOf(rvi));
        dto.setNbi(String.valueOf(nbi));
        dto.setGsi(String.valueOf(gsi));
        dto.setSei(String.valueOf(sei));
        dto.setSgt(buildGrowthSuggestion(base.getBaseName(), rvi, nbi, gsi, sei, irrigationAdvice,
                fertilizationAdvice, pestData, lodgingData));
        return dto;
    }

    private BigDecimal getLatestColorIndex(String baseId) {
        QueryWrapper<YoucaiColorQuality> query = new QueryWrapper<>();
        query.eq("base_id", baseId).orderByDesc("monitor_date").last("LIMIT 1");
        YoucaiColorQuality latest = colorQualityService.getOne(query);
        return latest != null ? latest.getColorIndex() : null;
    }

    private YoucaiDecisionHeightRiskDTO buildHeightRiskTrendData(YoucaiBases base) {
        YoucaiDecisionHeightRiskDTO dto = new YoucaiDecisionHeightRiskDTO();
        dto.setSid(base.getId());
        dto.setNam(base.getBaseName());

        List<YoucaiPlots> plots = listBasePlots(base.getId());
        List<String> plotIds = new ArrayList<>();
        for (YoucaiPlots plot : plots) {
            plotIds.add(plot.getId());
        }

        Map<String, List<YoucaiGrowthMonitoring>> growthRecordMap = listGrowthMonitoringHistory(plotIds);
        Map<LocalDate, DailySensorSummary> sensorSummaryMap = Collections.emptyMap();

        Map<String, YoucaiGrowthMonitoring> latestGrowthMap = mapLatestGrowthMonitoringByBase(base.getId());
        BigDecimal baseDefaultHeight = averageNullable(extractPlantHeights(latestGrowthMap.values()));
        if (baseDefaultHeight == null) {
            baseDefaultHeight = defaultHeightByStage(resolveGrowthStage(base.getId()));
        }

        List<YoucaiDecisionHeightRiskDTO.TrendItem> trendItems = new ArrayList<>();
        LocalDate startDate = LocalDate.now().minusDays(14);
        LocalDate endDate = LocalDate.now();
        for (LocalDate day = startDate; !day.isAfter(endDate); day = day.plusDays(1)) {
            BigDecimal avgHeight = calculateBaseHeightForDay(growthRecordMap, day, baseDefaultHeight);
            DailySensorSummary sensorSummary = sensorSummaryMap.get(day);
            BigDecimal riskCenter = estimateDailyLodgingRisk(avgHeight, sensorSummary);

            YoucaiDecisionHeightRiskDTO.TrendItem item = new YoucaiDecisionHeightRiskDTO.TrendItem();
            item.setDat(day.format(DateTimeFormatter.ofPattern("MM-dd")));
            item.setHgt(formatStrippedDecimal(avgHeight, 1));
            item.setLow(formatStrippedDecimal(clamp(riskCenter.subtract(new BigDecimal("6"))), 0));
            item.setHig(formatStrippedDecimal(clamp(riskCenter.add(new BigDecimal("6"))), 0));
            trendItems.add(item);
        }
        dto.setDta(trendItems);
        return dto;
    }

    private BigDecimal calculatePredictedYield(YoucaiBases base) {
        List<YoucaiPlots> plots = listBasePlots(base.getId());
        if (CollectionUtils.isEmpty(plots)) {
            return BigDecimal.ZERO;
        }

        String historyBasePrefix = normalizeHistoricalBaseName(base.getBaseName());
        BigDecimal totalArea = BigDecimal.ZERO;
        BigDecimal weightedYield = BigDecimal.ZERO;

        for (YoucaiPlots plot : plots) {
            if (plot.getArea() == null || plot.getArea().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal plotYield = queryAverageYield(historyBasePrefix, plot.getPlotName());
            if (plotYield == null) {
                continue;
            }
            totalArea = totalArea.add(plot.getArea());
            weightedYield = weightedYield.add(plotYield.multiply(plot.getArea()));
        }

        if (totalArea.compareTo(BigDecimal.ZERO) > 0) {
            return weightedYield.divide(totalArea, 2, RoundingMode.HALF_UP);
        }

        return BigDecimal.ZERO;
    }

    private List<YoucaiPlots> listBasePlots(String baseId) {
        QueryWrapper<YoucaiPlots> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("base_id", baseId).orderByAsc("plot_name");
        return plotsService.list(queryWrapper);
    }

    private BigDecimal queryAverageYield(String historyBasePrefix, String plotName) {
        if (!StringUtils.hasText(historyBasePrefix) || !StringUtils.hasText(plotName)) {
            return null;
        }
        QueryWrapper<YoucaiHistoricalYield> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("plot_name", historyBasePrefix + plotName).orderByDesc("year");
        List<YoucaiHistoricalYield> yields = historicalYieldService.list(queryWrapper);
        if (CollectionUtils.isEmpty(yields)) {
            return null;
        }

        BigDecimal total = BigDecimal.ZERO;
        int count = 0;
        for (YoucaiHistoricalYield item : yields) {
            if (item.getYield() == null) {
                continue;
            }
            total = total.add(item.getYield());
            count++;
        }
        if (count == 0) {
            return null;
        }
        return total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    private List<Map<String, Object>> fetchPestImages(String baseName, LocalDate startDate, LocalDate endDate) {
        if (!StringUtils.hasText(baseName)) {
            return Collections.emptyList();
        }
        try {
            List<Map<String, Object>> data = pestControlService
                    .getPestImages(baseName, startDate.toString(), endDate.toString())
                    .block();
            return data == null ? Collections.emptyList() : data;
        } catch (Exception e) {
            log.warn("获取基地{}近7天虫情数据失败: {}", baseName, e.getMessage());
            return Collections.emptyList();
        }
    }

    private Map<String, Integer> aggregatePestCounts(List<Map<String, Object>> pestImages) {
        Map<String, Integer> counter = new HashMap<>();
        for (Map<String, Object> pestImage : pestImages) {
            Object insectsObject = pestImage.get("insects");
            if (!(insectsObject instanceof Map)) {
                continue;
            }
            Map<?, ?> insects = (Map<?, ?>) insectsObject;
            for (Map.Entry<?, ?> entry : insects.entrySet()) {
                String pestName = entry.getKey() == null ? null : String.valueOf(entry.getKey()).trim();
                Integer count = parseInteger(entry.getValue());
                if (!StringUtils.hasText(pestName) || count == null || count <= 0) {
                    continue;
                }
                counter.merge(pestName, count, Integer::sum);
            }
        }
        return counter;
    }

    private String buildPestSuggestion(List<YoucaiDecisionPestDTO.PestItem> topPests) {
        if (CollectionUtils.isEmpty(topPests)) {
            return "防治建议：近7天暂无有效虫情数据，建议保持测报灯监测并结合田间巡查。";
        }
        int topCount = topPests.get(0).getValue() == null ? 0 : topPests.get(0).getValue();
        List<String> pestNames = new ArrayList<>();
        for (YoucaiDecisionPestDTO.PestItem item : topPests) {
            pestNames.add(item.getName());
        }
        String joinedNames = String.join("、", pestNames);
        if (topCount >= 50) {
            return "防治建议：重点关注" + joinedNames + "，建议立即加密巡田，优先采用诱捕和分区防治，虫量持续上升时及时开展针对性用药。";
        }
        if (topCount >= 20) {
            return "防治建议：当前以" + joinedNames + "为主，建议加强田间巡查，结合黄板诱杀、清沟除草等绿色防控措施。";
        }
        return "防治建议：" + joinedNames + "处于低位发生阶段，建议继续监测，发现局部聚集时及时点状处置。";
    }

    private String buildWaterSuggestion(Map<String, Object> irrigationAdvice) {
        String reason = extractMessage(irrigationAdvice.get("reason"));
        String time = extractMessage(irrigationAdvice.get("recommendedTime"));
        String method = extractMessage(irrigationAdvice.get("method"));
        BigDecimal volume = toBigDecimal(irrigationAdvice.get("recommendedVolumeMm"));

        if (!StringUtils.hasText(reason)) {
            return "灌溉建议：暂无有效灌溉建议，请检查传感器和气象站数据。";
        }

        StringBuilder builder = new StringBuilder("灌溉建议：").append(reason);
        if (StringUtils.hasText(time) && !"暂无数据".equals(time)) {
            builder.append(" 建议时段：").append(time).append("。");
        }
        if (StringUtils.hasText(method) && !"暂无数据".equals(method)) {
            builder.append(" 建议方式：").append(method).append("。");
        }
        if (volume != null && volume.compareTo(BigDecimal.ZERO) > 0) {
            builder.append(" 参考灌水量：").append(volume.stripTrailingZeros().toPlainString()).append("mm。");
        }
        return builder.toString();
    }

    private String buildEcSuggestion(Map<String, Object> fertilizationAdvice) {
        if (Boolean.TRUE.equals(fertilizationAdvice.get("deviceNotConfigured"))) {
            return "施肥建议：" + extractMessage(fertilizationAdvice.get("reason"));
        }

        String reason = extractMessage(fertilizationAdvice.get("reason"));
        String time = extractMessage(fertilizationAdvice.get("recommendedTime"));
        String method = extractMessage(fertilizationAdvice.get("method"));
        BigDecimal n = toBigDecimal(fertilizationAdvice.get("recommendN"));
        BigDecimal p = toBigDecimal(fertilizationAdvice.get("recommendP2O5"));
        BigDecimal k = toBigDecimal(fertilizationAdvice.get("recommendK2O"));

        if (!StringUtils.hasText(reason)) {
            return "施肥建议：暂无有效施肥建议，请检查土壤传感器数据。";
        }

        StringBuilder builder = new StringBuilder("施肥建议：").append(reason);
        if (StringUtils.hasText(time)) {
            builder.append(" 建议时段：").append(time).append("。");
        }
        if (StringUtils.hasText(method)) {
            builder.append(" 推荐方式：").append(method).append("。");
        }
        if (n != null || p != null || k != null) {
            builder.append(" 参考用量 N/P2O5/K2O=");
            builder.append(formatDecimal(n, 2)).append("/")
                    .append(formatDecimal(p, 2)).append("/")
                    .append(formatDecimal(k, 2)).append(" kg/亩。");
        }
        return builder.toString();
    }

    private String resolveWaterLevel(BigDecimal moisture) {
        if (moisture == null) {
            return null;
        }
        if (moisture.compareTo(new BigDecimal("15")) < 0) {
            return "1";
        }
        if (moisture.compareTo(new BigDecimal("20")) < 0) {
            return "2";
        }
        if (moisture.compareTo(new BigDecimal("30")) < 0) {
            return "3";
        }
        if (moisture.compareTo(new BigDecimal("40")) < 0) {
            return "4";
        }
        return "5";
    }

    private String resolveEcLevel(BigDecimal ecValue) {
        if (ecValue == null) {
            return null;
        }
        if (ecValue.compareTo(new BigDecimal("0.10")) < 0) {
            return "1";
        }
        if (ecValue.compareTo(new BigDecimal("0.20")) < 0) {
            return "2";
        }
        if (ecValue.compareTo(new BigDecimal("0.40")) < 0) {
            return "3";
        }
        if (ecValue.compareTo(new BigDecimal("0.60")) < 0) {
            return "4";
        }
        return "5";
    }

    private BigDecimal averagePositive(BigDecimal... values) {
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        for (BigDecimal value : values) {
            if (value != null && value.compareTo(BigDecimal.ZERO) > 0) {
                sum = sum.add(value);
                count++;
            }
        }
        if (count == 0) {
            return null;
        }
        return sum.divide(BigDecimal.valueOf(count), 3, RoundingMode.HALF_UP);
    }

    private BigDecimal averageNullable(List<BigDecimal> values) {
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        for (BigDecimal value : values) {
            if (value != null) {
                sum = sum.add(value);
                count++;
            }
        }
        if (count == 0) {
            return null;
        }
        return sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    private String formatStrippedDecimal(BigDecimal value, int scale) {
        if (value == null) {
            return "";
        }
        return value.setScale(scale, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    private String formatDecimal(BigDecimal value, int scale) {
        if (value == null) {
            return "";
        }
        return value.setScale(scale, RoundingMode.HALF_UP).toPlainString();
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return new BigDecimal(value.toString());
        }
        try {
            return new BigDecimal(String.valueOf(value).trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, YoucaiGrowthMonitoring> mapLatestGrowthMonitoringByBase(String baseId) {
        Map<String, YoucaiGrowthMonitoring> latestMap = new LinkedHashMap<>();
        for (YoucaiPlots plot : listBasePlots(baseId)) {
            QueryWrapper<YoucaiGrowthMonitoring> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("plot_id", plot.getId()).orderByDesc("monitoring_date").last("LIMIT 1");
            List<YoucaiGrowthMonitoring> monitoringList = growthMonitoringService.list(queryWrapper);
            if (!CollectionUtils.isEmpty(monitoringList)) {
                latestMap.put(plot.getId(), monitoringList.get(0));
            }
        }
        return latestMap;
    }

    private List<BigDecimal> extractPlantHeights(Iterable<YoucaiGrowthMonitoring> monitoringList) {
        List<BigDecimal> heights = new ArrayList<>();
        for (YoucaiGrowthMonitoring monitoring : monitoringList) {
            BigDecimal height = toBigDecimal(monitoring.getPlantHeight());
            if (height != null && height.compareTo(BigDecimal.ZERO) > 0) {
                heights.add(height);
            }
        }
        return heights;
    }

    private int resolveBatchRiskLevel(LodgingRiskAssessmentResponseDTO.BatchLodgingRiskAssessmentResponseDTO batchRisk) {
        int riskLevel = 1;
        if (batchRisk.getBaseStatistics() != null) {
            Double avgRiskScore = batchRisk.getBaseStatistics().getAverageRiskScore();
            if (avgRiskScore != null) {
                if (avgRiskScore >= 0.75d) {
                    riskLevel = 3;
                } else if (avgRiskScore >= 0.45d) {
                    riskLevel = 2;
                }
            }
        }
        if (!CollectionUtils.isEmpty(batchRisk.getPlotRisks())) {
            for (LodgingRiskAssessmentResponseDTO risk : batchRisk.getPlotRisks()) {
                riskLevel = Math.max(riskLevel, mapRiskLevel(risk == null ? null : risk.getCurrentRisk()));
            }
        }
        return riskLevel;
    }

    private int mapRiskLevel(LodgingRiskAssessmentResponseDTO.CurrentRiskDTO currentRisk) {
        if (currentRisk == null || !StringUtils.hasText(currentRisk.getRiskLevel())) {
            return 1;
        }
        String riskText = currentRisk.getRiskLevel();
        if (riskText.contains("极高") || riskText.contains("高")) {
            return 3;
        }
        if (riskText.contains("中")) {
            return 2;
        }
        return 1;
    }

    private List<BigDecimal> extractProbabilityValues(List<LodgingRiskAssessmentResponseDTO> plotRisks) {
        List<BigDecimal> values = new ArrayList<>();
        for (LodgingRiskAssessmentResponseDTO plotRisk : plotRisks) {
            if (plotRisk == null || plotRisk.getCurrentRisk() == null) {
                continue;
            }
            BigDecimal probability = parseProbabilityPercent(plotRisk.getCurrentRisk().getLodgingProbability());
            if (probability != null) {
                values.add(probability);
            }
        }
        return values;
    }

    private BigDecimal parseProbabilityPercent(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        if (!StringUtils.hasText(text)) {
            return null;
        }
        text = text.replace("%", "").replaceAll("[^0-9.\\-]", "");
        if (!StringUtils.hasText(text)) {
            return null;
        }
        try {
            BigDecimal number = new BigDecimal(text);
            if (number.compareTo(BigDecimal.ONE) <= 0) {
                number = number.multiply(new BigDecimal("100"));
            }
            return clamp(number);
        } catch (Exception e) {
            return null;
        }
    }

    private String buildLodgingSuggestion(List<LodgingRiskAssessmentResponseDTO> plotRisks, String baseName, int riskLevel) {
        Set<String> suggestionSet = new LinkedHashSet<>();
        for (LodgingRiskAssessmentResponseDTO plotRisk : plotRisks) {
            if (plotRisk == null || plotRisk.getComprehensiveSuggestions() == null) {
                continue;
            }
            LodgingRiskAssessmentResponseDTO.ComprehensiveSuggestionsDTO suggestions =
                    plotRisk.getComprehensiveSuggestions();
            if (!CollectionUtils.isEmpty(suggestions.getImmediate())) {
                suggestionSet.addAll(suggestions.getImmediate());
            }
            if (!CollectionUtils.isEmpty(suggestions.getShortTerm())) {
                suggestionSet.addAll(suggestions.getShortTerm());
            }
            if (suggestionSet.size() >= 3) {
                break;
            }
        }
        if (suggestionSet.isEmpty()) {
            return buildDefaultLodgingSuggestion(baseName, riskLevel);
        }
        return "预防措施：" + String.join("；", new ArrayList<>(suggestionSet));
    }

    private String buildDefaultLodgingSuggestion(String baseName, int riskLevel) {
        if (riskLevel >= 3) {
            return "预防措施：" + baseName + "建议立即排查高杆区域，及时排水、培土并加固植株，必要时分区抢收。";
        }
        if (riskLevel == 2) {
            return "预防措施：" + baseName + "建议加强巡田，及时疏通沟渠并做好培土固根，持续关注大风降雨天气。";
        }
        return "预防措施：" + baseName + "当前倒伏风险较低，建议保持排水通畅并继续开展常规巡查。";
    }

    private BigDecimal defaultHeightByStage(String stage) {
        if ("1".equals(stage)) {
            return new BigDecimal("15");
        }
        if ("2".equals(stage)) {
            return new BigDecimal("45");
        }
        if ("4".equals(stage)) {
            return new BigDecimal("145");
        }
        if ("5".equals(stage)) {
            return new BigDecimal("135");
        }
        return new BigDecimal("120");
    }

    private BigDecimal defaultLodgingLow(int riskLevel) {
        if (riskLevel >= 3) {
            return new BigDecimal("60");
        }
        if (riskLevel == 2) {
            return new BigDecimal("35");
        }
        return new BigDecimal("10");
    }

    private BigDecimal defaultLodgingHigh(int riskLevel) {
        if (riskLevel >= 3) {
            return new BigDecimal("80");
        }
        if (riskLevel == 2) {
            return new BigDecimal("55");
        }
        return new BigDecimal("25");
    }

    private int calculateRvi(List<YoucaiGrowthMonitoring> latestGrowthList, BigDecimal colorIndex) {
        int score = 50;
        int healthScore = 0;
        for (YoucaiGrowthMonitoring item : latestGrowthList) {
            String healthStatus = item.getHealthStatus();
            if (!StringUtils.hasText(healthStatus)) {
                continue;
            }
            if (containsAny(healthStatus, "良", "优", "正常", "健康")) {
                healthScore += 8;
            } else if (containsAny(healthStatus, "一般", "轻度")) {
                healthScore += 2;
            } else if (containsAny(healthStatus, "差", "弱", "异常")) {
                healthScore -= 6;
            }
        }
        BigDecimal avgHeight = averageNullable(extractNumericField(latestGrowthList, "height"));
        BigDecimal avgStem = averageNullable(extractNumericField(latestGrowthList, "stem"));
        BigDecimal avgDensity = averageNullable(extractNumericField(latestGrowthList, "density"));

        score += healthScore;
        if (avgHeight != null) {
            if (avgHeight.compareTo(new BigDecimal("80")) >= 0 && avgHeight.compareTo(new BigDecimal("170")) <= 0) {
                score += 10;
            } else if (avgHeight.compareTo(new BigDecimal("50")) < 0) {
                score -= 10;
            }
        }
        if (avgStem != null) {
            if (avgStem.compareTo(new BigDecimal("8")) >= 0) {
                score += 15;
            } else if (avgStem.compareTo(new BigDecimal("5")) >= 0) {
                score += 8;
            } else {
                score -= 8;
            }
        }
        if (avgDensity != null) {
            if (avgDensity.compareTo(new BigDecimal("8000")) >= 0 && avgDensity.compareTo(new BigDecimal("45000")) <= 0) {
                score += 10;
            } else {
                score -= 5;
            }
        }
        // 多光谱参数 (NDVI)
        if (colorIndex != null) {
            if (colorIndex.compareTo(new BigDecimal("0.7")) > 0) {
                score += 15;
            } else if (colorIndex.compareTo(new BigDecimal("0.5")) > 0) {
                score += 8;
            } else if (colorIndex.compareTo(new BigDecimal("0.3")) < 0) {
                score -= 10;
            }
        } else {
            // 缺失参数使用估计值计算 (假设 NDVI 为 0.6)
            score += 5;
        }
        return clampInt(score);
    }

    private int calculateNbi(Map<String, Object> fertilizationStatus, BigDecimal colorIndex) {
        BigDecimal n = positiveOrNull(toBigDecimal(fertilizationStatus.get("estimatedN")));
        BigDecimal p = positiveOrNull(toBigDecimal(fertilizationStatus.get("estimatedP")));
        BigDecimal k = positiveOrNull(toBigDecimal(fertilizationStatus.get("estimatedK")));
        
        int baseScore;
        if (n == null || p == null || k == null || n.compareTo(BigDecimal.ZERO) <= 0) {
            baseScore = 60;
        } else {
            BigDecimal pRatio = p.divide(n, 4, RoundingMode.HALF_UP);
            BigDecimal kRatio = k.divide(n, 4, RoundingMode.HALF_UP);
            BigDecimal deviation = pRatio.subtract(new BigDecimal("0.35")).abs()
                    .divide(new BigDecimal("0.35"), 4, RoundingMode.HALF_UP)
                    .add(kRatio.subtract(new BigDecimal("1.20")).abs()
                            .divide(new BigDecimal("1.20"), 4, RoundingMode.HALF_UP));
            baseScore = BigDecimal.valueOf(100)
                    .subtract(deviation.multiply(new BigDecimal("40")))
                    .setScale(0, RoundingMode.HALF_UP)
                    .intValue();
        }

        // 使用多光谱参数 (SPAD/NDVI 代理) 修正营养均衡度
        if (colorIndex != null) {
            if (colorIndex.compareTo(new BigDecimal("0.75")) > 0) {
                baseScore += 10;
            } else if (colorIndex.compareTo(new BigDecimal("0.6")) > 0) {
                baseScore += 5;
            } else if (colorIndex.compareTo(new BigDecimal("0.4")) < 0) {
                baseScore -= 10;
            }
        } else {
            // 缺失参数使用估计值 (假设颜色指数良好)
            baseScore += 2;
        }
        
        return clampInt(baseScore);
    }

    private int calculateGsi(List<YoucaiGrowthMonitoring> latestGrowthList) {
        double heightCv = coefficientOfVariation(extractNumericField(latestGrowthList, "height"));
        double stemCv = coefficientOfVariation(extractNumericField(latestGrowthList, "stem"));
        double densityCv = coefficientOfVariation(extractNumericField(latestGrowthList, "density"));
        double totalCv = heightCv * 0.5d + stemCv * 0.3d + densityCv * 0.2d;
        int score = (int) Math.round(100d - Math.min(100d, totalCv * 150d));
        return clampInt(score);
    }

    private int calculateSei(Map<String, Object> irrigationStatus, Map<String, Object> fertilizationStatus,
                             YoucaiDecisionPestDTO pestData, YoucaiDecisionLodgingDTO lodgingData) {
        int stress = 20;
        BigDecimal moisture = toBigDecimal(irrigationStatus.get("soilMoisturePercent"));
        if (moisture != null) {
            if (moisture.compareTo(new BigDecimal("15")) < 0) {
                stress += 35;
            } else if (moisture.compareTo(new BigDecimal("20")) < 0) {
                stress += 20;
            } else if (moisture.compareTo(new BigDecimal("50")) > 0) {
                stress += 15;
            }
        }

        BigDecimal avgEc = averagePositive(
                toBigDecimal(fertilizationStatus.get("ec1")),
                toBigDecimal(fertilizationStatus.get("ec2")),
                toBigDecimal(fertilizationStatus.get("ec3"))
        );
        if (avgEc != null) {
            if (avgEc.compareTo(new BigDecimal("0.10")) < 0 || avgEc.compareTo(new BigDecimal("0.60")) > 0) {
                stress += 20;
            } else if (avgEc.compareTo(new BigDecimal("0.20")) < 0 || avgEc.compareTo(new BigDecimal("0.40")) > 0) {
                stress += 10;
            }
        }

        if (!CollectionUtils.isEmpty(pestData.getBug())) {
            Integer topValue = pestData.getBug().get(0).getValue();
            if (topValue != null) {
                if (topValue >= 50) {
                    stress += 25;
                } else if (topValue >= 20) {
                    stress += 15;
                } else if (topValue > 0) {
                    stress += 5;
                }
            }
        }

        if ("3".equals(lodgingData.getRsk())) {
            stress += 20;
        } else if ("2".equals(lodgingData.getRsk())) {
            stress += 10;
        }
        return clampInt(stress);
    }

    private String buildGrowthSuggestion(String baseName, int rvi, int nbi, int gsi, int sei,
                                         Map<String, Object> irrigationAdvice, Map<String, Object> fertilizationAdvice,
                                         YoucaiDecisionPestDTO pestData, YoucaiDecisionLodgingDTO lodgingData) {
        List<String> suggestions = new ArrayList<>();
        if (sei >= 80) {
            String waterReason = extractMessage(irrigationAdvice.get("reason"));
            if (StringUtils.hasText(waterReason)) {
                suggestions.add("立即灌溉调水：" + waterReason);
            }
            String fertilizationReason = extractMessage(fertilizationAdvice.get("reason"));
            if (StringUtils.hasText(fertilizationReason)) {
                suggestions.add("尽快补肥调理：" + fertilizationReason);
            }
        }
        if (rvi <= 45) {
            suggestions.add("重点巡查弱苗区域，结合叶面喷施和补水促进长势恢复");
        }
        if (nbi <= 60) {
            suggestions.add("关注氮磷钾配比平衡，优先按推荐量进行分次追肥");
        }
        if (gsi <= 60) {
            suggestions.add("加强分区管理，对长势不齐地块开展补苗补弱和差异化水肥调控");
        }
        if (!CollectionUtils.isEmpty(pestData.getBug())) {
            Integer topValue = pestData.getBug().get(0).getValue();
            if (topValue != null && topValue >= 20) {
                suggestions.add("重点关注" + pestData.getBug().get(0).getName() + "等虫情变化，及时点状防治");
            }
        }
        if ("3".equals(lodgingData.getRsk())) {
            suggestions.add("当前倒伏风险偏高，建议同步做好排水、培土和高杆区加固");
        }
        if (suggestions.isEmpty()) {
            suggestions.add(baseName + "当前长势总体稳定，建议维持现有水肥管理并按期复查");
        }
        return "建议措施：" + toOrderedText(suggestions, 3);
    }

    private String toOrderedText(List<String> items, int limit) {
        List<String> ordered = new ArrayList<>();
        int index = 1;
        for (String item : items) {
            if (!StringUtils.hasText(item)) {
                continue;
            }
            ordered.add(index + "、" + item);
            index++;
            if (ordered.size() >= limit) {
                break;
            }
        }
        return String.join("；", ordered);
    }

    private Date latestMonitoringDate(List<YoucaiGrowthMonitoring> latestGrowthList) {
        Date latest = null;
        for (YoucaiGrowthMonitoring item : latestGrowthList) {
            if (item.getMonitoringDate() != null && (latest == null || item.getMonitoringDate().after(latest))) {
                latest = item.getMonitoringDate();
            }
        }
        return latest;
    }

    private String formatDateTime(Date date) {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .format(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
    }

    private Map<String, List<YoucaiGrowthMonitoring>> listGrowthMonitoringHistory(List<String> plotIds) {
        Map<String, List<YoucaiGrowthMonitoring>> recordMap = new LinkedHashMap<>();
        if (CollectionUtils.isEmpty(plotIds)) {
            return recordMap;
        }
        QueryWrapper<YoucaiGrowthMonitoring> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("plot_id", plotIds).orderByAsc("plot_id").orderByAsc("monitoring_date");
        List<YoucaiGrowthMonitoring> monitoringList = growthMonitoringService.list(queryWrapper);
        for (YoucaiGrowthMonitoring monitoring : monitoringList) {
            recordMap.computeIfAbsent(monitoring.getPlotId(), key -> new ArrayList<>()).add(monitoring);
        }
        return recordMap;
    }

    private BigDecimal calculateBaseHeightForDay(Map<String, List<YoucaiGrowthMonitoring>> growthRecordMap,
                                                 LocalDate day, BigDecimal defaultHeight) {
        List<BigDecimal> heights = new ArrayList<>();
        for (List<YoucaiGrowthMonitoring> recordList : growthRecordMap.values()) {
            BigDecimal height = resolveHeightForDay(recordList, day);
            if (height != null) {
                heights.add(height);
            }
        }
        BigDecimal avgHeight = averageNullable(heights);
        return avgHeight == null ? defaultHeight : avgHeight;
    }

    private BigDecimal resolveHeightForDay(List<YoucaiGrowthMonitoring> recordList, LocalDate day) {
        if (CollectionUtils.isEmpty(recordList)) {
            return null;
        }
        YoucaiGrowthMonitoring before = null;
        YoucaiGrowthMonitoring after = null;
        for (YoucaiGrowthMonitoring item : recordList) {
            if (item.getMonitoringDate() == null) {
                continue;
            }
            LocalDate itemDate = item.getMonitoringDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (!itemDate.isAfter(day)) {
                before = item;
            }
            if (!itemDate.isBefore(day)) {
                after = item;
                break;
            }
        }
        if (before != null && after != null) {
            LocalDate beforeDate = before.getMonitoringDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate afterDate = after.getMonitoringDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (beforeDate.equals(afterDate)) {
                return toBigDecimal(before.getPlantHeight());
            }
            return interpolateHeight(before, after, day, beforeDate, afterDate);
        }
        if (before != null) {
            return toBigDecimal(before.getPlantHeight());
        }
        if (after != null) {
            return toBigDecimal(after.getPlantHeight());
        }
        return null;
    }

    private BigDecimal interpolateHeight(YoucaiGrowthMonitoring before, YoucaiGrowthMonitoring after, LocalDate day,
                                         LocalDate beforeDate, LocalDate afterDate) {
        BigDecimal beforeHeight = toBigDecimal(before.getPlantHeight());
        BigDecimal afterHeight = toBigDecimal(after.getPlantHeight());
        if (beforeHeight == null) {
            return afterHeight;
        }
        if (afterHeight == null) {
            return beforeHeight;
        }
        long totalDays = afterDate.toEpochDay() - beforeDate.toEpochDay();
        if (totalDays <= 0) {
            return beforeHeight;
        }
        long passedDays = day.toEpochDay() - beforeDate.toEpochDay();
        BigDecimal ratio = new BigDecimal(passedDays).divide(new BigDecimal(totalDays), 4, RoundingMode.HALF_UP);
        return beforeHeight.add(afterHeight.subtract(beforeHeight).multiply(ratio));
    }

    private BigDecimal estimateDailyLodgingRisk(BigDecimal height, DailySensorSummary summary) {
        BigDecimal wind = summary == null ? BigDecimal.ZERO : defaultIfNull(summary.avgWind);
        BigDecimal precip = summary == null ? BigDecimal.ZERO : defaultIfNull(summary.sumPrecip);
        BigDecimal soil = summary == null ? new BigDecimal("30") : defaultIfNull(summary.avgSoilMoisture);

        BigDecimal score = new BigDecimal("10");
        score = score.add(normalize(height, new BigDecimal("90"), new BigDecimal("180")).multiply(new BigDecimal("30")));
        score = score.add(normalize(wind, new BigDecimal("1"), new BigDecimal("8")).multiply(new BigDecimal("30")));
        score = score.add(normalize(precip, BigDecimal.ZERO, new BigDecimal("40")).multiply(new BigDecimal("20")));
        score = score.add(moistureStress(soil));
        return clamp(score.setScale(2, RoundingMode.HALF_UP));
    }

    private BigDecimal normalize(BigDecimal value, BigDecimal min, BigDecimal max) {
        if (value == null || max.compareTo(min) <= 0) {
            return BigDecimal.ZERO;
        }
        if (value.compareTo(min) <= 0) {
            return BigDecimal.ZERO;
        }
        if (value.compareTo(max) >= 0) {
            return BigDecimal.ONE;
        }
        return value.subtract(min).divide(max.subtract(min), 4, RoundingMode.HALF_UP);
    }

    private BigDecimal moistureStress(BigDecimal soil) {
        if (soil.compareTo(new BigDecimal("15")) < 0) {
            return new BigDecimal("20");
        }
        if (soil.compareTo(new BigDecimal("20")) < 0) {
            return new BigDecimal("12");
        }
        if (soil.compareTo(new BigDecimal("55")) > 0) {
            return new BigDecimal("10");
        }
        if (soil.compareTo(new BigDecimal("45")) > 0) {
            return new BigDecimal("5");
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal clamp(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        if (value.compareTo(new BigDecimal("100")) > 0) {
            return new BigDecimal("100");
        }
        return value;
    }

    private int clampInt(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private BigDecimal positiveOrNull(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        return value;
    }

    private BigDecimal defaultIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private List<BigDecimal> extractNumericField(List<YoucaiGrowthMonitoring> monitoringList, String fieldType) {
        List<BigDecimal> values = new ArrayList<>();
        for (YoucaiGrowthMonitoring item : monitoringList) {
            BigDecimal value = null;
            if ("height".equals(fieldType)) {
                value = toBigDecimal(item.getPlantHeight());
            } else if ("stem".equals(fieldType)) {
                value = toBigDecimal(item.getStemDiameter());
            } else if ("density".equals(fieldType)) {
                value = toBigDecimal(item.getDensity());
            }
            if (value != null && value.compareTo(BigDecimal.ZERO) > 0) {
                values.add(value);
            }
        }
        return values;
    }

    private double coefficientOfVariation(List<BigDecimal> values) {
        BigDecimal avg = averageNullable(values);
        if (avg == null || avg.compareTo(BigDecimal.ZERO) <= 0 || values.size() < 2) {
            return 0d;
        }
        double mean = avg.doubleValue();
        double sum = 0d;
        for (BigDecimal value : values) {
            double diff = value.doubleValue() - mean;
            sum += diff * diff;
        }
        double stdDev = Math.sqrt(sum / values.size());
        return stdDev / mean;
    }

    private static class DailySensorSummary {
        private BigDecimal avgWind;
        private BigDecimal sumPrecip;
        private BigDecimal avgSoilMoisture;
    }

    private Map<String, Object> safeMap(Map<String, Object> data) {
        return data == null ? Collections.emptyMap() : data;
    }

    private String resolveGrowthStage(String baseId) {
        QueryWrapper<YoucaiFarmingRecords> farmingQuery = new QueryWrapper<>();
        farmingQuery.eq("base_id", baseId).orderByDesc("farming_date");
        List<YoucaiFarmingRecords> records = farmingRecordsService.list(farmingQuery);
        for (YoucaiFarmingRecords record : records) {
            String stage = matchGrowthStage(record.getRemark(), record.getMaterials(), record.getRecordCode());
            if (stage != null) {
                return stage;
            }
        }

        QueryWrapper<YoucaiFertilization> fertilizationQuery = new QueryWrapper<>();
        fertilizationQuery.eq("base_id", baseId).orderByDesc("fertilization_date");
        List<YoucaiFertilization> fertilizations = fertilizationMapper.selectList(fertilizationQuery);
        for (YoucaiFertilization item : fertilizations) {
            String stage = matchGrowthStage(item.getRemark(), item.getReason(), item.getRecommendedTime());
            if (stage != null) {
                return stage;
            }
        }

        Date latestOperationTime = getLatestOperationTime(records, fertilizations);
        return fallbackGrowthStage(latestOperationTime);
    }

    private String matchGrowthStage(String... texts) {
        for (String text : texts) {
            if (!StringUtils.hasText(text)) {
                continue;
            }
            if (containsAny(text, "成熟", "黄熟", "角果")) {
                return "5";
            }
            if (containsAny(text, "开花", "花期")) {
                return "4";
            }
            if (containsAny(text, "抽薹", "蕾薹", "现蕾")) {
                return "3";
            }
            if (containsAny(text, "苗期", "移栽", "返青")) {
                return "2";
            }
            if (containsAny(text, "播种", "出苗", "发芽")) {
                return "1";
            }
        }
        return null;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private Date getLatestOperationTime(List<YoucaiFarmingRecords> records, List<YoucaiFertilization> fertilizations) {
        Date latest = null;
        for (YoucaiFarmingRecords record : records) {
            if (record.getFarmingDate() != null && (latest == null || record.getFarmingDate().after(latest))) {
                latest = record.getFarmingDate();
            }
        }
        for (YoucaiFertilization item : fertilizations) {
            if (item.getFertilizationDate() != null && (latest == null || item.getFertilizationDate().after(latest))) {
                latest = item.getFertilizationDate();
            }
        }
        return latest;
    }

    private String fallbackGrowthStage(Date latestOperationTime) {
        if (latestOperationTime == null) {
            return "3";
        }
        @SuppressWarnings("deprecation")
        int month = latestOperationTime.getMonth() + 1;
        if (month >= 9 && month <= 10) {
            return "1";
        }
        if (month == 11 || month == 12) {
            return "2";
        }
        if (month == 1 || month == 2) {
            return "3";
        }
        if (month == 3) {
            return "4";
        }
        return "5";
    }

    private String normalizeHistoricalBaseName(String baseName) {
        if (!StringUtils.hasText(baseName)) {
            return "";
        }
        if (baseName.contains("九里")) {
            return "九里基地";
        }
        if (baseName.contains("洋梓")) {
            return "洋梓基地";
        }
        if (baseName.contains("丰乐")) {
            return "丰乐基地";
        }
        if (baseName.contains("胡集")) {
            return "胡集基地";
        }
        return baseName;
    }

    private String formatArea(BigDecimal area) {
        if (area == null) {
            return "";
        }
        return area.stripTrailingZeros().toPlainString();
    }

    private String extractMessage(Object value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value).trim();
    }
}
