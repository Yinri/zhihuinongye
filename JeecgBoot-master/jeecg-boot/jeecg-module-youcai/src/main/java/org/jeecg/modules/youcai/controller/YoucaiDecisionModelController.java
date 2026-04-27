package org.jeecg.modules.youcai.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.youcai.dto.decision.YoucaiDecisionGrowthDTO;
import org.jeecg.modules.youcai.dto.decision.YoucaiDecisionHeightRiskDTO;
import org.jeecg.modules.youcai.dto.decision.YoucaiDecisionLodgingDTO;
import org.jeecg.modules.youcai.dto.decision.YoucaiDecisionPestDTO;
import org.jeecg.modules.youcai.dto.decision.YoucaiDecisionSoilDTO;
import org.jeecg.modules.youcai.dto.decision.YoucaiDecisionYieldDTO;
import org.jeecg.modules.youcai.service.IYoucaiDecisionModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "油菜决策模型")
@RestController
@RequestMapping("/youcai/decisionModel")
public class YoucaiDecisionModelController {

    @Autowired
    private IYoucaiDecisionModelService decisionModelService;

    @Operation(summary = "接口1：基地今日平均亩产预测")
    @GetMapping("/yield-predictions")
    public Result<List<YoucaiDecisionYieldDTO>> getYieldPredictions() {
        return Result.OK(decisionModelService.getInterface1Data());
    }

    @Operation(summary = "接口2：基地最近7天害虫Top3")
    @GetMapping("/pest-top3")
    public Result<List<YoucaiDecisionPestDTO>> getPestTop3() {
        return Result.OK(decisionModelService.getInterface2Data());
    }

    @Operation(summary = "接口3：基地今日土壤指标")
    @GetMapping("/soil-metrics")
    public Result<List<YoucaiDecisionSoilDTO>> getSoilMetrics() {
        return Result.OK(decisionModelService.getInterface3Data());
    }

    @Operation(summary = "接口4：基地今日倒伏预警数据")
    @GetMapping("/lodging-warnings")
    public Result<List<YoucaiDecisionLodgingDTO>> getLodgingWarnings() {
        return Result.OK(decisionModelService.getInterface4Data());
    }

    @Operation(summary = "接口5：基地今日长势指标数据")
    @GetMapping("/growth-indices")
    public Result<List<YoucaiDecisionGrowthDTO>> getGrowthIndices() {
        return Result.OK(decisionModelService.getInterface5Data());
    }

    @Operation(summary = "接口6：基地近15天植株高度和倒伏概率")
    @GetMapping("/height-risk-trends")
    public Result<List<YoucaiDecisionHeightRiskDTO>> getHeightRiskTrends() {
        return Result.OK(decisionModelService.getInterface6Data());
    }
}
