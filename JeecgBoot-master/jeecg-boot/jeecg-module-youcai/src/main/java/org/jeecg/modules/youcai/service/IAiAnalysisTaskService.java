package org.jeecg.modules.youcai.service;

import org.jeecg.modules.youcai.dto.AiTaskRecordDTO;
import org.jeecg.modules.youcai.dto.AiTaskSubmitResponseDTO;

import java.util.function.Supplier;

public interface IAiAnalysisTaskService {
    AiTaskSubmitResponseDTO submitTask(String taskType, String cacheKey, Supplier<String> resultSupplier);

    AiTaskRecordDTO getTask(String taskId);
}
