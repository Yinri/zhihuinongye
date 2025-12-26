package org.jeecg.modules.youcai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.youcai.entity.*;
import org.jeecg.modules.youcai.mapper.*;
import org.jeecg.modules.youcai.service.IYoucaiProductionPlansService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * @Description: 生产计划表
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V1.0
 */
@Slf4j
@Service
public class YoucaiProductionPlansServiceImpl extends ServiceImpl<YoucaiProductionPlansMapper, YoucaiProductionPlans> implements IYoucaiProductionPlansService {

    @Autowired
    private YoucaiPlotsMapper youcaiPlotsMapper;

    @Autowired
    private YoucaiSoilFertilityMapper youcaiSoilFertilityMapper;

    @Autowired
    private YoucaiRapeVarietiesMapper youcaiRapeVarietiesMapper;

    @Autowired
    private CropNutrientDemandMapper cropNutrientDemandMapper;

    @Autowired
    private YoucaiHistoricalYieldMapper youcaiHistoricalYieldMapper;

    @Autowired
    private YoucaiBasesMapper youcaiBasesMapper;

    @Override
    public YoucaiProductionPlans generateProductionPlan(String plotId) {
        log.info("开始生成生产计划，地块ID: {}", plotId);

        YoucaiPlots plot = youcaiPlotsMapper.selectById(plotId);
        if (plot == null) {
            throw new RuntimeException("地块不存在");
        }

        YoucaiBases base = youcaiBasesMapper.selectById(plot.getBaseId());
        if (base == null) {
            throw new RuntimeException("基地不存在");
        }

        QueryWrapper<YoucaiSoilFertility> soilQuery = new QueryWrapper<>();
        soilQuery.eq("plot_id", plotId).orderByDesc("test_date").last("LIMIT 1");
        YoucaiSoilFertility soilFertility = youcaiSoilFertilityMapper.selectOne(soilQuery);

        if (soilFertility == null) {
            throw new RuntimeException("地块土壤肥力数据不存在");
        }

        QueryWrapper<YoucaiRapeVarieties> varietyQuery = new QueryWrapper<>();
        varietyQuery.eq("del_flag", 0);
        List<YoucaiRapeVarieties> varieties = youcaiRapeVarietiesMapper.selectList(varietyQuery);

        if (varieties == null || varieties.isEmpty()) {
            throw new RuntimeException("没有可用的油菜品种");
        }

        YoucaiRapeVarieties selectedVariety = selectOptimalVariety(varieties, soilFertility);

        BigDecimal targetYield = calculateTargetYield(base.getBaseName(), plot.getPlotName(), selectedVariety, soilFertility);

        BigDecimal seedTotal = calculateSeedRequirement(selectedVariety, targetYield);

        FertilizerResult fertilizerResult = calculateFertilizerRequirement(selectedVariety, targetYield, soilFertility);

        PesticideResult pesticideResult = calculatePesticideRequirement(selectedVariety, plot.getArea());

        LocalDate currentDate = LocalDate.now();
        int currentYear = currentDate.getYear();
        String planName = String.format("%s-%s-%d生产计划", base.getBaseName(), plot.getPlotName(), currentYear);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        YoucaiProductionPlans plan = new YoucaiProductionPlans();
        plan.setPlotId(plotId);
        plan.setBaseId(plot.getBaseId());
        plan.setPlanName(planName);
        plan.setPlanYear(String.valueOf(currentYear));
        plan.setVarietyId(selectedVariety.getId());
        plan.setTargetYield(targetYield);
        plan.setPlantingArea(plot.getArea());
        try {
            plan.setPlannedSowingDate(dateFormat.parse(currentYear + "-09-20"));
            plan.setPlannedHarvestDate(dateFormat.parse((currentYear + 1) + "-05-20"));
        } catch (ParseException e) {
            throw new RuntimeException("日期解析失败", e);
        }
        plan.setStatus("草稿");
        plan.setSeedTotal(seedTotal.toString());
        plan.setFertilizerTotalN(new BigDecimal(fertilizerResult.totalN).setScale(2, RoundingMode.HALF_UP));
        plan.setFertilizerTotalP(new BigDecimal(fertilizerResult.totalP).setScale(2, RoundingMode.HALF_UP));
        plan.setFertilizerTotalK(new BigDecimal(fertilizerResult.totalK).setScale(2, RoundingMode.HALF_UP));
        plan.setFertilizerSafetyCoeff(new BigDecimal("1.2"));
        plan.setPesticideTotal(new BigDecimal(pesticideResult.total).setScale(2, RoundingMode.HALF_UP));
        plan.setPesticideSafetyCoeff(new BigDecimal("1.2"));
        plan.setSeedParamsSnapshot(buildSeedParamsSnapshot(selectedVariety));
        plan.setNutrientDemandSnapshot(buildNutrientDemandSnapshot(selectedVariety));
        plan.setSoilFertilitySnapshot(buildSoilFertilitySnapshot(soilFertility));

        log.info("生产计划生成成功: {}", planName);
        return plan;
    }

    private YoucaiRapeVarieties selectOptimalVariety(List<YoucaiRapeVarieties> varieties, YoucaiSoilFertility soilFertility) {
        YoucaiRapeVarieties bestVariety = varieties.get(0);
        BigDecimal bestScore = BigDecimal.ZERO;

        BigDecimal soilN = soilFertility.getNitrogen() != null ? soilFertility.getNitrogen() : BigDecimal.ZERO;
        BigDecimal soilP = soilFertility.getPhosphorus() != null ? soilFertility.getPhosphorus() : BigDecimal.ZERO;
        BigDecimal soilK = soilFertility.getPotassium() != null ? soilFertility.getPotassium() : BigDecimal.ZERO;

        BigDecimal avgSoilNutrient = soilN.add(soilP).add(soilK).divide(new BigDecimal("3"), 2, RoundingMode.HALF_UP);

        for (YoucaiRapeVarieties variety : varieties) {
            BigDecimal yieldPotential = variety.getYieldPotential() != null ? variety.getYieldPotential() : BigDecimal.ZERO;
            BigDecimal diseaseResistanceScore = calculateDiseaseResistanceScore(variety.getDiseaseResistance());

            BigDecimal soilMatchScore = BigDecimal.ZERO;
            if (avgSoilNutrient.compareTo(new BigDecimal("150")) >= 0) {
                soilMatchScore = new BigDecimal("0.8");
            } else if (avgSoilNutrient.compareTo(new BigDecimal("100")) >= 0) {
                soilMatchScore = new BigDecimal("0.6");
            } else {
                soilMatchScore = new BigDecimal("0.4");
            }

            BigDecimal totalScore = yieldPotential.multiply(new BigDecimal("0.5"))
                    .add(diseaseResistanceScore.multiply(new BigDecimal("0.3")))
                    .add(soilMatchScore.multiply(new BigDecimal("0.2")));

            if (totalScore.compareTo(bestScore) > 0) {
                bestScore = totalScore;
                bestVariety = variety;
            }
        }

        log.info("选择最优品种: {}, 评分: {}", bestVariety.getVarietyName(), bestScore);
        return bestVariety;
    }

    private BigDecimal calculateDiseaseResistanceScore(String diseaseResistance) {
        if (diseaseResistance == null || diseaseResistance.isEmpty()) {
            return new BigDecimal("0.5");
        }

        if (diseaseResistance.contains("强") || diseaseResistance.contains("高")) {
            return new BigDecimal("0.9");
        } else if (diseaseResistance.contains("中")) {
            return new BigDecimal("0.6");
        } else if (diseaseResistance.contains("弱") || diseaseResistance.contains("低")) {
            return new BigDecimal("0.3");
        }

        return new BigDecimal("0.5");
    }

    private BigDecimal calculateTargetYield(String baseName, String plotName, YoucaiRapeVarieties variety, YoucaiSoilFertility soilFertility) {
        String fullPlotName = baseName + plotName;
        QueryWrapper<YoucaiHistoricalYield> yieldQuery = new QueryWrapper<>();
        yieldQuery.eq("plot_name", fullPlotName).orderByDesc("year").last("LIMIT 3");
        List<YoucaiHistoricalYield> historicalYields = youcaiHistoricalYieldMapper.selectList(yieldQuery);

        BigDecimal historicalAvgYield = BigDecimal.ZERO;
        if (historicalYields != null && !historicalYields.isEmpty()) {
            BigDecimal sum = BigDecimal.ZERO;
            for (YoucaiHistoricalYield yield : historicalYields) {
                sum = sum.add(yield.getYield() != null ? yield.getYield() : BigDecimal.ZERO);
            }
            historicalAvgYield = sum.divide(new BigDecimal(historicalYields.size()), 2, RoundingMode.HALF_UP);
        }

        BigDecimal yieldPotential = variety.getYieldPotential() != null ? variety.getYieldPotential() : new BigDecimal("200");

        BigDecimal soilFactor = calculateSoilFactor(soilFertility);

        BigDecimal targetYield;
        if (historicalAvgYield.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal increaseRate = new BigDecimal("0.05");
            targetYield = historicalAvgYield.multiply(BigDecimal.ONE.add(increaseRate));
        } else {
            targetYield = yieldPotential.multiply(soilFactor);
        }

        targetYield = targetYield.setScale(2, RoundingMode.HALF_UP);
        log.info("计算目标产量: 历史平均产量={}, 品种潜力={}, 土壤因子={}, 目标产量={}", 
                historicalAvgYield, yieldPotential, soilFactor, targetYield);

        return targetYield;
    }

    private BigDecimal calculateSoilFactor(YoucaiSoilFertility soilFertility) {
        BigDecimal organicMatter = soilFertility.getOrganicMatter() != null ? soilFertility.getOrganicMatter() : BigDecimal.ZERO;
        BigDecimal nitrogen = soilFertility.getNitrogen() != null ? soilFertility.getNitrogen() : BigDecimal.ZERO;
        BigDecimal phosphorus = soilFertility.getPhosphorus() != null ? soilFertility.getPhosphorus() : BigDecimal.ZERO;
        BigDecimal potassium = soilFertility.getPotassium() != null ? soilFertility.getPotassium() : BigDecimal.ZERO;

        BigDecimal organicFactor = organicMatter.compareTo(new BigDecimal("20")) >= 0 ? new BigDecimal("1.0") :
                organicMatter.compareTo(new BigDecimal("15")) >= 0 ? new BigDecimal("0.9") : new BigDecimal("0.8");

        BigDecimal npkFactor = nitrogen.add(phosphorus).add(potassium).divide(new BigDecimal("3"), 2, RoundingMode.HALF_UP);
        npkFactor = npkFactor.compareTo(new BigDecimal("150")) >= 0 ? new BigDecimal("1.0") :
                npkFactor.compareTo(new BigDecimal("100")) >= 0 ? new BigDecimal("0.9") : new BigDecimal("0.8");

        return organicFactor.multiply(npkFactor);
    }

    private BigDecimal calculateSeedRequirement(YoucaiRapeVarieties variety, BigDecimal targetYield) {
        BigDecimal harvestCoefficient = variety.getHarvestCoefficient() != null ? variety.getHarvestCoefficient() : new BigDecimal("0.4");
        BigDecimal seedlingSurvivalRate = new BigDecimal(variety.getSeedlingSurvivalRate() != null ? variety.getSeedlingSurvivalRate() : 90);
        BigDecimal seedSettingRate = new BigDecimal(variety.getSeedSettingRate() != null ? variety.getSeedSettingRate() : 85);
        Integer singlePlantPods = variety.getSinglePlantPods() != null ? variety.getSinglePlantPods() : 300;
        Integer seedsPerPod = variety.getSeedsPerPod() != null ? variety.getSeedsPerPod() : 20;
        BigDecimal thousandGrainWeight = variety.getThousandGrainWeight() != null ? variety.getThousandGrainWeight() : new BigDecimal("3.0");
        BigDecimal germinationRate = new BigDecimal(variety.getGerminationRate() != null ? variety.getGerminationRate() : 90);

        BigDecimal theoreticalSeedlings = targetYield.multiply(new BigDecimal("1000"))
                .divide(
                        new BigDecimal(singlePlantPods)
                                .multiply(new BigDecimal(seedsPerPod))
                                .multiply(thousandGrainWeight)
                                .multiply(seedSettingRate.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP))
                                .multiply(harvestCoefficient),
                        2, RoundingMode.HALF_UP
                );

        BigDecimal seedInputKg = theoreticalSeedlings.multiply(thousandGrainWeight)
                .divide(
                        (germinationRate.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP))
                                .multiply(seedlingSurvivalRate.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP))
                                .multiply(new BigDecimal("1000")),
                        2, RoundingMode.HALF_UP
                );

        log.info("计算种子需求: 目标产量={}, 理论苗数={}, 种子用量={}kg", targetYield, theoreticalSeedlings, seedInputKg);
        return seedInputKg;
    }

    private FertilizerResult calculateFertilizerRequirement(YoucaiRapeVarieties variety, BigDecimal targetYield, YoucaiSoilFertility soilFertility) {
        QueryWrapper<CropNutrientDemand> nutrientQuery = new QueryWrapper<>();
        nutrientQuery.eq("variety_id", variety.getId());
        CropNutrientDemand nutrientDemand = cropNutrientDemandMapper.selectOne(nutrientQuery);

        BigDecimal nDemand = nutrientDemand != null && nutrientDemand.getNDemand() != null ? nutrientDemand.getNDemand() : new BigDecimal("5.0");
        BigDecimal pDemand = nutrientDemand != null && nutrientDemand.getPDemand() != null ? nutrientDemand.getPDemand() : new BigDecimal("2.0");
        BigDecimal kDemand = nutrientDemand != null && nutrientDemand.getKDemand() != null ? nutrientDemand.getKDemand() : new BigDecimal("4.0");

        BigDecimal soilN = soilFertility.getNitrogen() != null ? soilFertility.getNitrogen() : BigDecimal.ZERO;
        BigDecimal soilP = soilFertility.getPhosphorus() != null ? soilFertility.getPhosphorus() : BigDecimal.ZERO;
        BigDecimal soilK = soilFertility.getPotassium() != null ? soilFertility.getPotassium() : BigDecimal.ZERO;

        BigDecimal soilNDemand = soilN.multiply(new BigDecimal("0.15")).multiply(new BigDecimal("2.25")).divide(new BigDecimal("1000"), 4, RoundingMode.HALF_UP);
        BigDecimal soilPDemand = soilP.multiply(new BigDecimal("0.15")).multiply(new BigDecimal("2.25")).divide(new BigDecimal("1000"), 4, RoundingMode.HALF_UP);
        BigDecimal soilKDemand = soilK.multiply(new BigDecimal("0.15")).multiply(new BigDecimal("2.25")).divide(new BigDecimal("1000"), 4, RoundingMode.HALF_UP);

        BigDecimal targetNDemand = targetYield.multiply(nDemand).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal targetPDemand = targetYield.multiply(pDemand).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal targetKDemand = targetYield.multiply(kDemand).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        BigDecimal nRate = new BigDecimal("0.35");
        BigDecimal pRate = new BigDecimal("0.25");
        BigDecimal kRate = new BigDecimal("0.35");
        BigDecimal safetyCoeff = new BigDecimal("1.2");

        BigDecimal needN = targetNDemand.subtract(soilNDemand).max(new BigDecimal("0.001"));
        BigDecimal needP = targetPDemand.subtract(soilPDemand).max(new BigDecimal("0.001"));
        BigDecimal needK = targetKDemand.subtract(soilKDemand).max(new BigDecimal("0.001"));

        BigDecimal totalN = needN.multiply(safetyCoeff).divide(nRate, 2, RoundingMode.HALF_UP);
        BigDecimal totalP = needP.multiply(safetyCoeff).divide(pRate, 2, RoundingMode.HALF_UP);
        BigDecimal totalK = needK.multiply(safetyCoeff).divide(kRate, 2, RoundingMode.HALF_UP);

        log.info("计算肥料需求: N={}, P={}, K={}", totalN, totalP, totalK);

        FertilizerResult result = new FertilizerResult();
        result.totalN = totalN.toString();
        result.totalP = totalP.toString();
        result.totalK = totalK.toString();
        return result;
    }

    private PesticideResult calculatePesticideRequirement(YoucaiRapeVarieties variety, BigDecimal area) {
        BigDecimal basePesticidePerMu = new BigDecimal("0.3");
        BigDecimal diseaseFactor = calculateDiseaseResistanceScore(variety.getDiseaseResistance());
        BigDecimal pesticidePerMu = basePesticidePerMu.divide(diseaseFactor, 2, RoundingMode.HALF_UP);
        BigDecimal totalPesticide = pesticidePerMu.multiply(area).multiply(new BigDecimal("1.2"));

        log.info("计算农药需求: 每亩用量={}, 总面积={}, 总用量={}", pesticidePerMu, area, totalPesticide);

        PesticideResult result = new PesticideResult();
        result.total = totalPesticide.toString();
        result.note = "根据品种抗病性和地块面积计算";
        return result;
    }

    private String buildSeedParamsSnapshot(YoucaiRapeVarieties variety) {
        return String.format("{\"harvestCoefficient\":%s,\"seedlingSurvivalRate\":%d,\"seedSettingRate\":%d,\"singlePlantPods\":%d,\"seedsPerPod\":%d,\"thousandGrainWeight\":%s,\"germinationRate\":%d}",
                variety.getHarvestCoefficient(),
                variety.getSeedlingSurvivalRate(),
                variety.getSeedSettingRate(),
                variety.getSinglePlantPods(),
                variety.getSeedsPerPod(),
                variety.getThousandGrainWeight(),
                variety.getGerminationRate());
    }

    private String buildNutrientDemandSnapshot(YoucaiRapeVarieties variety) {
        QueryWrapper<CropNutrientDemand> nutrientQuery = new QueryWrapper<>();
        nutrientQuery.eq("variety_id", variety.getId());
        CropNutrientDemand nutrientDemand = cropNutrientDemandMapper.selectOne(nutrientQuery);

        if (nutrientDemand == null) {
            return "{\"nDemand\":5.0,\"pDemand\":2.0,\"kDemand\":4.0}";
        }

        return String.format("{\"nDemand\":%s,\"pDemand\":%s,\"kDemand\":%s}",
                nutrientDemand.getNDemand(),
                nutrientDemand.getPDemand(),
                nutrientDemand.getKDemand());
    }

    private String buildSoilFertilitySnapshot(YoucaiSoilFertility soilFertility) {
        return String.format("{\"phValue\":%s,\"organicMatter\":%s,\"nitrogen\":%s,\"phosphorus\":%s,\"potassium\":%s,\"fertilityLevel\":\"%s\"}",
                soilFertility.getPhValue(),
                soilFertility.getOrganicMatter(),
                soilFertility.getNitrogen(),
                soilFertility.getPhosphorus(),
                soilFertility.getPotassium(),
                soilFertility.getFertilityLevel());
    }

    private static class FertilizerResult {
        String totalN;
        String totalP;
        String totalK;
    }

    private static class PesticideResult {
        String total;
        String note;
    }
}
