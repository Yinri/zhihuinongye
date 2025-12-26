package org.jeecg.modules.youcai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.jeecg.modules.youcai.entity.YoucaiSoilFertility;
import org.jeecg.modules.youcai.service.IFertilizationService;
import org.jeecg.modules.youcai.service.IYoucaiSoilFertilityService;
import org.jeecg.modules.youcai.util.QueftsAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FertilizationServiceImpl implements IFertilizationService {

    @Autowired
    private IYoucaiSoilFertilityService soilService;

    @Override
    public Map<String, Object> getPlotStatusByBase(String baseId) {
        Map<String, Object> res = new HashMap<>();
        if (baseId == null || baseId.isEmpty()) {
            res.put("hasData", false);
            return res;
        }
        QueryWrapper<YoucaiSoilFertility> q = new QueryWrapper<>();
        q.eq("base_id", baseId)
         .orderByDesc("test_date");
        List<YoucaiSoilFertility> list = soilService.list(q);
        if (list.isEmpty()) {
            res.put("hasData", false);
            return res;
        }
        Date latestDate = list.get(0).getTestDate();
        List<YoucaiSoilFertility> latest = list.stream()
                .filter(x -> x.getTestDate() != null && x.getTestDate().equals(latestDate))
                .collect(Collectors.toList());
        BigDecimal n = avg(latest.stream().map(YoucaiSoilFertility::getNitrogen).filter(Objects::nonNull).collect(Collectors.toList()));
        BigDecimal p = avg(latest.stream().map(YoucaiSoilFertility::getPhosphorus).filter(Objects::nonNull).collect(Collectors.toList()));
        BigDecimal k = avg(latest.stream().map(YoucaiSoilFertility::getPotassium).filter(Objects::nonNull).collect(Collectors.toList()));
        BigDecimal sum = n.add(p).add(k);
        BigDecimal percN = sum.compareTo(BigDecimal.ZERO) > 0 ? n.multiply(BigDecimal.valueOf(100)).divide(sum, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal percP = sum.compareTo(BigDecimal.ZERO) > 0 ? p.multiply(BigDecimal.valueOf(100)).divide(sum, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal percK = sum.compareTo(BigDecimal.ZERO) > 0 ? k.multiply(BigDecimal.valueOf(100)).divide(sum, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        res.put("hasData", true);
        res.put("latestDate", latestDate);
        res.put("nPercent", percN);
        res.put("pPercent", percP);
        res.put("kPercent", percK);
        res.put("ph", avg(latest.stream().map(YoucaiSoilFertility::getPhValue).filter(Objects::nonNull).collect(Collectors.toList())));
        res.put("om", avg(latest.stream().map(YoucaiSoilFertility::getOrganicMatter).filter(Objects::nonNull).collect(Collectors.toList())));
        return res;
    }

    @Override
    public Map<String, Object> getBaseSoilSeries(String baseId) {
        Map<String, Object> res = new HashMap<>();
        if (baseId == null || baseId.isEmpty()) {
            res.put("hasData", false);
            res.put("dates", Collections.emptyList());
            res.put("avgN", Collections.emptyList());
            res.put("avgP", Collections.emptyList());
            res.put("avgK", Collections.emptyList());
            return res;
        }
        QueryWrapper<YoucaiSoilFertility> q = new QueryWrapper<>();
        q.eq("base_id", baseId)
         .orderByDesc("test_date");
        List<YoucaiSoilFertility> list = soilService.list(q);
        if (list.isEmpty()) {
            res.put("hasData", false);
            res.put("dates", Collections.emptyList());
            res.put("avgN", Collections.emptyList());
            res.put("avgP", Collections.emptyList());
            res.put("avgK", Collections.emptyList());
            return res;
        }
        Map<Date, List<YoucaiSoilFertility>> byDate = list.stream()
                .filter(x -> x.getTestDate() != null)
                .collect(Collectors.groupingBy(YoucaiSoilFertility::getTestDate));
        List<Date> dates = byDate.keySet().stream()
                .sorted(Comparator.reverseOrder())
                .limit(7)
                .sorted()
                .collect(Collectors.toList());
        List<BigDecimal> avgN = new ArrayList<>();
        List<BigDecimal> avgP = new ArrayList<>();
        List<BigDecimal> avgK = new ArrayList<>();
        for (Date d : dates) {
            List<YoucaiSoilFertility> day = byDate.getOrDefault(d, Collections.emptyList());
            avgN.add(avg(day.stream().map(YoucaiSoilFertility::getNitrogen).filter(Objects::nonNull).collect(Collectors.toList())));
            avgP.add(avg(day.stream().map(YoucaiSoilFertility::getPhosphorus).filter(Objects::nonNull).collect(Collectors.toList())));
            avgK.add(avg(day.stream().map(YoucaiSoilFertility::getPotassium).filter(Objects::nonNull).collect(Collectors.toList())));
        }
        res.put("hasData", true);
        res.put("dates", dates.stream().map(d -> new java.text.SimpleDateFormat("yyyy-MM-dd").format(d)).collect(Collectors.toList()));
        res.put("avgN", avgN);
        res.put("avgP", avgP);
        res.put("avgK", avgK);
        return res;
    }

    @Override
    public Map<String, Object> getBaseRecommendation(String baseId) {
        Map<String, Object> res = new HashMap<>();
        if (baseId == null || baseId.isEmpty()) {
            res.put("hasData", false);
            return res;
        }
        QueryWrapper<YoucaiSoilFertility> q = new QueryWrapper<>();
        q.eq("base_id", baseId)
         .orderByDesc("test_date")
         .last("LIMIT 1");
        YoucaiSoilFertility latest = soilService.getOne(q);
        if (latest == null) {
            res.put("hasData", false);
            return res;
        }
        BigDecimal targetYield = new BigDecimal("180");
        org.jeecg.modules.youcai.entity.YoucaiFertilization recommendation = QueftsAlgorithm.calculateRecommendation(targetYield, latest);
        boolean need = recommendation.getNRecommendKgPerMu().compareTo(BigDecimal.ZERO) > 0
                || recommendation.getP2o5RecommendKgPerMu().compareTo(BigDecimal.ZERO) > 0
                || recommendation.getK2oRecommendKgPerMu().compareTo(BigDecimal.ZERO) > 0;
        res.put("hasData", true);
        res.put("needFertilization", need);
        res.put("recommendedTime", recommendation.getRecommendedTime());
        res.put("method", recommendation.getMethodRecommend());
        res.put("recommendN", recommendation.getNRecommendKgPerMu());
        res.put("recommendP2O5", recommendation.getP2o5RecommendKgPerMu());
        res.put("recommendK2O", recommendation.getK2oRecommendKgPerMu());
        res.put("reason", recommendation.getReason());
        return res;
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
}

