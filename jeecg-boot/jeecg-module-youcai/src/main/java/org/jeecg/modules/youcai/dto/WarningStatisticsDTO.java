package org.jeecg.modules.youcai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.util.Map;

/**
 * @Description: 预警统计DTO
 * @Author: System
 * @Date: 2025-11-27
 */
@Data
@Schema(description = "预警统计信息")
public class WarningStatisticsDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "总预警数")
    private Long totalCount;

    @Schema(description = "高危预警数")
    private Long highCount;

    @Schema(description = "中危预警数")
    private Long mediumCount;

    @Schema(description = "低危预警数")
    private Long lowCount;

    @Schema(description = "倒伏预警数")
    private Long lodgingCount;

    @Schema(description = "病害预警数")
    private Long diseaseCount;

    @Schema(description = "虫害预警数")
    private Long pestCount;

    @Schema(description = "待处理预警数")
    private Long pendingCount;

    @Schema(description = "处理中预警数")
    private Long processingCount;

    @Schema(description = "已处理预警数")
    private Long resolvedCount;

    @Schema(description = "按类型分组统计")
    private Map<String, Long> typeDistribution;

    @Schema(description = "按等级分组统计")
    private Map<String, Long> levelDistribution;

    @Schema(description = "按状态分组统计")
    private Map<String, Long> statusDistribution;
}
