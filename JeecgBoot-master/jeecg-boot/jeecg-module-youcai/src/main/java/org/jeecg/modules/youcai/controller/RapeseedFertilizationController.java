package org.jeecg.modules.youcai.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.youcai.service.IFertilizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name="智慧施肥(基地切换)")
@RestController
@RequestMapping("/rapeseed/fertilization")
@Slf4j
public class RapeseedFertilizationController {

    @Autowired
    private IFertilizationService fertilizationService;

    @Operation(summary="基地-土壤养分概览（最新日均N/P/K、pH、有机质）")
    @GetMapping("/plotStatusByBase/{baseId}")
    public Result<Map<String, Object>> plotStatusByBase(@PathVariable String baseId) {
        Map<String, Object> data = fertilizationService.getPlotStatusByBase(baseId);
        if (Boolean.FALSE.equals(data.get("hasData"))) {
            return Result.OK(data);
        }
        return Result.OK(data);
    }

    @Operation(summary="基地-近7天土壤趋势（按日均值）")
    @GetMapping("/baseSoilSeries/{baseId}")
    public Result<Map<String, Object>> baseSoilSeries(@PathVariable String baseId) {
        Map<String, Object> series = fertilizationService.getBaseSoilSeries(baseId);
        return Result.OK(series);
    }

    @Operation(summary="基地-QUEFTS施肥推荐（基于最新土壤）")
    @GetMapping("/baseRecommend/{baseId}")
    public Result<Map<String, Object>> baseRecommend(@PathVariable String baseId) {
        Map<String, Object> rec = fertilizationService.getBaseRecommendation(baseId);
        return Result.OK(rec);
    }
}

