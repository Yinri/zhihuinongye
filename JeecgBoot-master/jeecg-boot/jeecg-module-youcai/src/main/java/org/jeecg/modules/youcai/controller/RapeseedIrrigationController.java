package org.jeecg.modules.youcai.controller;

import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.youcai.service.IIrrigationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Tag(name="智慧灌溉(Penman)")
@RestController
@RequestMapping("/rapeseed/irrigation")
@Slf4j
public class RapeseedIrrigationController {
    @Autowired
    private IIrrigationService irrigationService;

    @Operation(summary="地块状态")
    @GetMapping("/plotStatus/{plotId}")
    public Result<Map<String, Object>> plotStatus(@PathVariable Integer plotId) {
        return Result.OK(irrigationService.getPlotStatus(plotId));
    }

    @Operation(summary="Penman建议与数据")
    @GetMapping("/penmanPredict")
    public Result<Map<String, Object>> penmanPredict(@RequestParam Integer plotId) {
        return Result.OK(irrigationService.getPenmanPredict(plotId));
    }

    @Operation(summary="干预对比")
    @GetMapping("/interventionComparison")
    public Result<Map<String, Object>> interventionComparison(@RequestParam Integer plotId) {
        return Result.OK(irrigationService.getInterventionComparison(plotId));
    }
}