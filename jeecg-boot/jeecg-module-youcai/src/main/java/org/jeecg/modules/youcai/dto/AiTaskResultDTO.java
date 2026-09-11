package org.jeecg.modules.youcai.dto;

import lombok.Data;

@Data
public class AiTaskResultDTO<T> {
    private String taskId;
    private String taskType;
    private String status;
    private String errorMessage;
    private boolean cached;
    private long createdTime;
    private Long finishedTime;
    private T result;
}
