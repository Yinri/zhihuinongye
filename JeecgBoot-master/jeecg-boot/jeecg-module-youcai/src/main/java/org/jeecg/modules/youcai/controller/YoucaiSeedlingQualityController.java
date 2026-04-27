package org.jeecg.modules.youcai.controller;

import com.alibaba.fastjson.JSON;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.youcai.dto.AiTaskRecordDTO;
import org.jeecg.modules.youcai.dto.AiTaskResultDTO;
import org.jeecg.modules.youcai.dto.AiTaskSubmitResponseDTO;
import org.jeecg.modules.youcai.dto.DiseaseAnalysisRequestDTO;
import org.jeecg.modules.youcai.dto.SeedlingAnalysisResponseDTO;
import org.jeecg.modules.youcai.service.IAiAnalysisTaskService;
import org.jeecg.modules.youcai.service.ISeedlingQualityAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@Tag(name = "油菜苗情分析")
@RestController
@RequestMapping("/youcai/seedlingQuality")
public class YoucaiSeedlingQualityController {
    private static final Logger log = LoggerFactory.getLogger(YoucaiSeedlingQualityController.class);

    @Autowired
    private ISeedlingQualityAnalysisService seedlingQualityAnalysisService;

    @Autowired
    private IAiAnalysisTaskService aiAnalysisTaskService;

    @Operation(summary = "AI综合苗情分析")
    @PostMapping("/analyze")
    public Result<SeedlingAnalysisResponseDTO> analyzeSeedling(@RequestBody DiseaseAnalysisRequestDTO request) {
        int imageCount = request != null && request.getImageUrls() != null ? request.getImageUrls().size() : 0;
        if (request == null || request.getImageUrls() == null || request.getImageUrls().isEmpty()) {
            return Result.error("监控截图URL列表不能为空");
        }
        try {
            log.info("收到苗情分析请求, baseName={}, 图片数量={}", request.getBaseName(), imageCount);
            return Result.OK(seedlingQualityAnalysisService.analyzeSeedling(request));
        } catch (Exception e) {
            log.error("苗情分析失败", e);
            return Result.error("苗情分析失败: " + e.getMessage());
        }
    }

    @Operation(summary = "提交苗情分析任务")
    @PostMapping("/analyze/submit")
    public Result<AiTaskSubmitResponseDTO> submitAnalyzeSeedling(@RequestBody DiseaseAnalysisRequestDTO request) {
        int imageCount = request != null && request.getImageUrls() != null ? request.getImageUrls().size() : 0;
        if (request == null || request.getImageUrls() == null || request.getImageUrls().isEmpty()) {
            return Result.error("监控截图URL列表不能为空");
        }
        try {
            log.info("提交苗情分析任务, baseName={}, 图片数量={}", request.getBaseName(), imageCount);
            String cacheKey = "seedling:" + DigestUtils.md5DigestAsHex(
                    JSON.toJSONString(request).getBytes(StandardCharsets.UTF_8));
            AiTaskSubmitResponseDTO submitResponse = aiAnalysisTaskService.submitTask(
                    "seedling",
                    cacheKey,
                    () -> JSON.toJSONString(seedlingQualityAnalysisService.analyzeSeedling(request))
            );
            return Result.OK(submitResponse);
        } catch (Exception e) {
            log.error("提交苗情分析任务失败", e);
            return Result.error("提交苗情分析任务失败: " + e.getMessage());
        }
    }

    @Operation(summary = "查询苗情分析任务状态")
    @GetMapping("/analyze/task/{taskId}")
    public Result<AiTaskResultDTO<SeedlingAnalysisResponseDTO>> getAnalyzeSeedlingTask(@PathVariable String taskId) {
        AiTaskRecordDTO taskRecord = aiAnalysisTaskService.getTask(taskId);
        if (taskRecord == null || !"seedling".equals(taskRecord.getTaskType())) {
            return Result.error("任务不存在");
        }
        AiTaskResultDTO<SeedlingAnalysisResponseDTO> response = new AiTaskResultDTO<>();
        response.setTaskId(taskRecord.getTaskId());
        response.setTaskType(taskRecord.getTaskType());
        response.setStatus(taskRecord.getStatus());
        response.setErrorMessage(taskRecord.getErrorMessage());
        response.setCached(taskRecord.isCached());
        response.setCreatedTime(taskRecord.getCreatedTime());
        response.setFinishedTime(taskRecord.getFinishedTime());
        if (AiTaskRecordDTO.STATUS_SUCCESS.equals(taskRecord.getStatus())
                && taskRecord.getResultJson() != null) {
            response.setResult(JSON.parseObject(taskRecord.getResultJson(), SeedlingAnalysisResponseDTO.class));
        }
        return Result.OK(response);
    }
}
