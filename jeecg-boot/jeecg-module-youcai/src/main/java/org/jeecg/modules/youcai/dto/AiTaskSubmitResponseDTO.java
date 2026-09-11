package org.jeecg.modules.youcai.dto;

import lombok.Data;

@Data
public class AiTaskSubmitResponseDTO {
    private String taskId;
    private String status;
    private boolean cached;
}
