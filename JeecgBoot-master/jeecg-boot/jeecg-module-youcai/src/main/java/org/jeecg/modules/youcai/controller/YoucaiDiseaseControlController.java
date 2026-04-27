package org.jeecg.modules.youcai.controller;

import com.alibaba.fastjson.JSON;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.youcai.dto.AiTaskRecordDTO;
import org.jeecg.modules.youcai.dto.AiTaskResultDTO;
import org.jeecg.modules.youcai.dto.AiTaskSubmitResponseDTO;
import org.jeecg.modules.youcai.dto.DiseaseAnalysisRequestDTO;
import org.jeecg.modules.youcai.dto.DiseaseAnalysisResponseDTO;
import org.jeecg.modules.youcai.dto.SporeAnalysisResponseDTO;
import org.jeecg.modules.youcai.service.IAiAnalysisTaskService;
import org.jeecg.modules.youcai.service.IDiseaseControlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Tag(name = "病害防控分析")
@RestController
@RequestMapping("/youcai/diseaseControl")
public class YoucaiDiseaseControlController {
    private static final Logger log = LoggerFactory.getLogger(YoucaiDiseaseControlController.class);

    @Autowired
    private IDiseaseControlService diseaseControlService;

    @Autowired
    private IAiAnalysisTaskService aiAnalysisTaskService;

    @Operation(summary = "获取监控截图列表")
    @GetMapping("/monitorImages")
    public Result<List<Map<String, Object>>> getMonitorImages(
            @Parameter(description = "基地ID") @RequestParam String baseId) {
        try {
            log.info("收到监控截图查询请求, baseId={}", baseId);
            return Result.OK(diseaseControlService.getMonitorImagesByBaseId(baseId));
        } catch (Exception e) {
            log.error("获取监控截图失败", e);
            return Result.error("获取监控截图失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取孢子捕捉仪图片列表")
    @GetMapping("/sporeImages")
    public Result<List<Map<String, Object>>> getSporeImages(
            @Parameter(description = "基地名称") @RequestParam String baseName) {
        try {
            log.info("收到孢子图片查询请求, baseName={}", baseName);
            return Result.OK(diseaseControlService.getSporeImagesByBaseName(baseName));
        } catch (Exception e) {
            log.error("获取孢子捕捉仪图片失败", e);
            return Result.error("获取孢子捕捉仪图片失败: " + e.getMessage());
        }
    }

    @Operation(summary = "AI病害分析")
    @PostMapping("/analyze")
    public Result<DiseaseAnalysisResponseDTO> analyzeDisease(
            @RequestBody DiseaseAnalysisRequestDTO request) {
        log.info("收到病害URL分析请求: 图片数量={}",
                request != null && request.getImageUrls() != null ? request.getImageUrls().size() : 0);
        if (request == null || request.getImageUrls() == null || request.getImageUrls().isEmpty()) {
            return Result.error("图片URL列表不能为空");
        }
        try {
            return Result.OK(diseaseControlService.analyzeDisease(request));
        } catch (Exception e) {
            log.error("病害URL分析失败", e);
            return Result.error("病害URL分析失败: " + e.getMessage());
        }
    }

    @Operation(summary = "提交病害分析任务")
    @PostMapping("/analyze/submit")
    public Result<AiTaskSubmitResponseDTO> submitAnalyzeDisease(
            @RequestBody DiseaseAnalysisRequestDTO request) {
        int imageCount = request != null && request.getImageUrls() != null ? request.getImageUrls().size() : 0;
        if (request == null || request.getImageUrls() == null || request.getImageUrls().isEmpty()) {
            return Result.error("图片URL列表不能为空");
        }
        try {
            log.info("提交病害分析任务, baseName={}, 图片数量={}", request.getBaseName(), imageCount);
            String cacheKey = "disease:" + DigestUtils.md5DigestAsHex(
                    JSON.toJSONString(request).getBytes(StandardCharsets.UTF_8));
            AiTaskSubmitResponseDTO submitResponse = aiAnalysisTaskService.submitTask(
                    "disease",
                    cacheKey,
                    () -> {
                        try {
                            return JSON.toJSONString(diseaseControlService.analyzeDisease(request));
                        } catch (Exception e) {
                            throw new IllegalStateException(e.getMessage(), e);
                        }
                    }
            );
            return Result.OK(submitResponse);
        } catch (Exception e) {
            log.error("提交病害分析任务失败", e);
            return Result.error("提交病害分析任务失败: " + e.getMessage());
        }
    }

    @Operation(summary = "查询病害分析任务状态")
    @GetMapping("/analyze/task/{taskId}")
    public Result<AiTaskResultDTO<DiseaseAnalysisResponseDTO>> getAnalyzeDiseaseTask(@PathVariable String taskId) {
        AiTaskRecordDTO taskRecord = aiAnalysisTaskService.getTask(taskId);
        if (taskRecord == null || !"disease".equals(taskRecord.getTaskType())) {
            return Result.error("任务不存在");
        }
        AiTaskResultDTO<DiseaseAnalysisResponseDTO> response = new AiTaskResultDTO<>();
        response.setTaskId(taskRecord.getTaskId());
        response.setTaskType(taskRecord.getTaskType());
        response.setStatus(taskRecord.getStatus());
        response.setErrorMessage(taskRecord.getErrorMessage());
        response.setCached(taskRecord.isCached());
        response.setCreatedTime(taskRecord.getCreatedTime());
        response.setFinishedTime(taskRecord.getFinishedTime());
        if (AiTaskRecordDTO.STATUS_SUCCESS.equals(taskRecord.getStatus())
                && taskRecord.getResultJson() != null) {
            response.setResult(JSON.parseObject(taskRecord.getResultJson(), DiseaseAnalysisResponseDTO.class));
        }
        return Result.OK(response);
    }

    @Operation(summary = "监控截图批量分析")
    @PostMapping("/analyzeMonitorBatch")
    public Result<DiseaseAnalysisResponseDTO> analyzeMonitorBatch(
            @Parameter(description = "基地ID") @RequestParam String baseId) {
        try {
            log.info("收到监控截图批量分析请求, baseId={}", baseId);
            return Result.OK(diseaseControlService.analyzeMonitorBatchByBaseId(baseId));
        } catch (Exception e) {
            log.error("监控截图批量分析失败", e);
            return Result.error("监控截图批量分析失败: " + e.getMessage());
        }
    }

    @Operation(summary = "提交监控截图批量分析任务")
    @PostMapping("/analyzeMonitorBatch/submit")
    public Result<AiTaskSubmitResponseDTO> submitAnalyzeMonitorBatch(
            @Parameter(description = "基地ID") @RequestParam String baseId) {
        if (baseId == null || baseId.trim().isEmpty()) {
            return Result.error("基地ID不能为空");
        }
        try {
            log.info("提交监控截图批量分析任务, baseId={}", baseId);
            String cacheKey = "disease-monitor-batch:" + DigestUtils.md5DigestAsHex(
                    baseId.getBytes(StandardCharsets.UTF_8));
            AiTaskSubmitResponseDTO submitResponse = aiAnalysisTaskService.submitTask(
                    "disease_monitor_batch",
                    cacheKey,
                    () -> {
                        try {
                            return JSON.toJSONString(diseaseControlService.analyzeMonitorBatchByBaseId(baseId));
                        } catch (Exception e) {
                            throw new IllegalStateException(e.getMessage(), e);
                        }
                    }
            );
            return Result.OK(submitResponse);
        } catch (Exception e) {
            log.error("提交监控截图批量分析任务失败", e);
            return Result.error("提交监控截图批量分析任务失败: " + e.getMessage());
        }
    }

    @Operation(summary = "查询监控截图批量分析任务状态")
    @GetMapping("/analyzeMonitorBatch/task/{taskId}")
    public Result<AiTaskResultDTO<DiseaseAnalysisResponseDTO>> getAnalyzeMonitorBatchTask(@PathVariable String taskId) {
        AiTaskRecordDTO taskRecord = aiAnalysisTaskService.getTask(taskId);
        if (taskRecord == null || !"disease_monitor_batch".equals(taskRecord.getTaskType())) {
            return Result.error("任务不存在");
        }
        AiTaskResultDTO<DiseaseAnalysisResponseDTO> response = new AiTaskResultDTO<>();
        response.setTaskId(taskRecord.getTaskId());
        response.setTaskType(taskRecord.getTaskType());
        response.setStatus(taskRecord.getStatus());
        response.setErrorMessage(taskRecord.getErrorMessage());
        response.setCached(taskRecord.isCached());
        response.setCreatedTime(taskRecord.getCreatedTime());
        response.setFinishedTime(taskRecord.getFinishedTime());
        if (AiTaskRecordDTO.STATUS_SUCCESS.equals(taskRecord.getStatus())
                && taskRecord.getResultJson() != null) {
            response.setResult(JSON.parseObject(taskRecord.getResultJson(), DiseaseAnalysisResponseDTO.class));
        }
        return Result.OK(response);
    }

    @Operation(summary = "孢子综合分析")
    @PostMapping("/analyzeSpore")
    public Result<SporeAnalysisResponseDTO> analyzeSporeImages(
            @RequestBody DiseaseAnalysisRequestDTO request) {
        int imageCount = request != null && request.getImageUrls() != null ? request.getImageUrls().size() : 0;
        try {
            log.info("收到孢子综合分析请求, baseName={}, 图片数量={}",
                    request != null ? request.getBaseName() : null, imageCount);
            if (request == null || request.getImageUrls() == null || request.getImageUrls().isEmpty()) {
                return Result.error("孢子图片URL列表不能为空");
            }
            return Result.OK(diseaseControlService.analyzeSpore(request));
        } catch (Exception e) {
            log.error("孢子综合分析失败", e);
            return Result.error("孢子综合分析失败: " + e.getMessage());
        }
    }

    @Operation(summary = "提交孢子综合分析任务")
    @PostMapping("/analyzeSpore/submit")
    public Result<AiTaskSubmitResponseDTO> submitAnalyzeSpore(
            @RequestBody DiseaseAnalysisRequestDTO request) {
        int imageCount = request != null && request.getImageUrls() != null ? request.getImageUrls().size() : 0;
        if (request == null || request.getImageUrls() == null || request.getImageUrls().isEmpty()) {
            return Result.error("孢子图片URL列表不能为空");
        }
        try {
            log.info("提交孢子综合分析任务, baseName={}, 图片数量={}", request.getBaseName(), imageCount);
            String cacheKey = "disease-spore:" + DigestUtils.md5DigestAsHex(
                    JSON.toJSONString(request).getBytes(StandardCharsets.UTF_8));
            AiTaskSubmitResponseDTO submitResponse = aiAnalysisTaskService.submitTask(
                    "disease_spore",
                    cacheKey,
                    () -> {
                        try {
                            return JSON.toJSONString(diseaseControlService.analyzeSpore(request));
                        } catch (Exception e) {
                            throw new IllegalStateException(e.getMessage(), e);
                        }
                    }
            );
            return Result.OK(submitResponse);
        } catch (Exception e) {
            log.error("提交孢子综合分析任务失败", e);
            return Result.error("提交孢子综合分析任务失败: " + e.getMessage());
        }
    }

    @Operation(summary = "查询孢子综合分析任务状态")
    @GetMapping("/analyzeSpore/task/{taskId}")
    public Result<AiTaskResultDTO<SporeAnalysisResponseDTO>> getAnalyzeSporeTask(@PathVariable String taskId) {
        AiTaskRecordDTO taskRecord = aiAnalysisTaskService.getTask(taskId);
        if (taskRecord == null || !"disease_spore".equals(taskRecord.getTaskType())) {
            return Result.error("任务不存在");
        }
        AiTaskResultDTO<SporeAnalysisResponseDTO> response = new AiTaskResultDTO<>();
        response.setTaskId(taskRecord.getTaskId());
        response.setTaskType(taskRecord.getTaskType());
        response.setStatus(taskRecord.getStatus());
        response.setErrorMessage(taskRecord.getErrorMessage());
        response.setCached(taskRecord.isCached());
        response.setCreatedTime(taskRecord.getCreatedTime());
        response.setFinishedTime(taskRecord.getFinishedTime());
        if (AiTaskRecordDTO.STATUS_SUCCESS.equals(taskRecord.getStatus())
                && taskRecord.getResultJson() != null) {
            response.setResult(JSON.parseObject(taskRecord.getResultJson(), SporeAnalysisResponseDTO.class));
        }
        return Result.OK(response);
    }
}
