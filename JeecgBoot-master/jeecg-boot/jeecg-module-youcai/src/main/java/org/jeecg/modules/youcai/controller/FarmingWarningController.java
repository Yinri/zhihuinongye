package org.jeecg.modules.youcai.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.youcai.dto.FarmingWarningDTO;
import org.jeecg.modules.youcai.dto.WarningQueryDTO;
import org.jeecg.modules.youcai.dto.WarningStatisticsDTO;
import org.jeecg.modules.youcai.service.IYoucaiWarningRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Description: 农事预警统一管理Controller
 * @Author: System
 * @Date: 2025-11-27
 */
@Tag(name = "农事预警统一管理")
@RestController
@RequestMapping("/youcai/farmingWarning")
@Slf4j
public class FarmingWarningController {

    @Autowired
    private IYoucaiWarningRecordService warningRecordService;

    /**
     * 获取农事预警列表（首页使用）
     */
    @Operation(summary = "获取农事预警列表")
    @GetMapping("/list")
    public Result<List<FarmingWarningDTO>> getWarningList(WarningQueryDTO queryDTO) {
        try {
            log.info("查询预警列表，参数: {}", queryDTO);
            List<FarmingWarningDTO> list = warningRecordService.getWarningList(queryDTO);
            return Result.OK(list);
        } catch (Exception e) {
            log.error("获取预警列表失败", e);
            return Result.error("获取预警列表失败: " + e.getMessage());
        }
    }

    /**
     * 更新预警状态
     */
    @Operation(summary = "更新预警状态")
    @PutMapping("/updateStatus")
    public Result<String> updateStatus(
            @RequestParam String warningId,
            @RequestParam String status,
            @RequestParam(required = false) String handler,
            @RequestParam(required = false) String remark) {
        try {
            log.info("更新预警状态，ID: {}, 状态: {}, 处理人: {}", warningId, status, handler);
            boolean success = warningRecordService.updateWarningStatus(warningId, status, handler, remark);
            return success ? Result.OK("更新成功") : Result.error("更新失败");
        } catch (Exception e) {
            log.error("更新预警状态失败", e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 获取预警统计信息
     */
    @Operation(summary = "获取预警统计信息")
    @GetMapping("/statistics")
    public Result<WarningStatisticsDTO> getStatistics() {
        try {
            WarningStatisticsDTO stats = warningRecordService.getWarningStatistics();
            return Result.OK(stats);
        } catch (Exception e) {
            log.error("获取预警统计失败", e);
            return Result.error("获取统计失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发生成所有预警（测试用）
     */
    @Operation(summary = "手动触发生成所有预警")
    @PostMapping("/generateAll")
    public Result<String> generateAllWarnings() {
        try {
            log.info("手动触发生成所有预警");
            warningRecordService.generateAllWarnings();
            return Result.OK("预警生成任务已启动");
        } catch (Exception e) {
            log.error("生成预警失败", e);
            return Result.error("生成预警失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发生成单个地块预警（测试用）
     */
    @Operation(summary = "生成单个地块预警")
    @PostMapping("/generate/{plotId}")
    public Result<String> generateWarning(@PathVariable String plotId) {
        try {
            log.info("手动触发生成地块预警，地块ID: {}", plotId);
            boolean success = warningRecordService.generateLodgingWarning(plotId);
            return success ? Result.OK("预警生成成功") : Result.error("预警生成失败或已存在");
        } catch (Exception e) {
            log.error("生成预警失败", e);
            return Result.error("生成预警失败: " + e.getMessage());
        }
    }
}
