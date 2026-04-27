package org.jeecg.modules.youcai.dto;

import lombok.Data;

@Data
public class AiTaskRecordDTO {
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_RUNNING = "RUNNING";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";

    private String taskId;
    private String taskType;
    private String status;
    private String cacheKey;
    private String resultJson;
    private String errorMessage;
    private boolean cached;
    private long createdTime;
    private long updatedTime;
    private Long finishedTime;
}
