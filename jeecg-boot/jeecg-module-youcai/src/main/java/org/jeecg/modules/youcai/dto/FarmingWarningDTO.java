package org.jeecg.modules.youcai.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @Description: 农事预警统一DTO
 * @Author: System
 * @Date: 2025-11-27
 */
@Data
@Schema(description = "农事预警信息")
public class FarmingWarningDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "预警ID")
    private String id;

    @Schema(description = "预警类型: lodging-倒伏, disease-病害, pest-虫害")
    private String warningType;

    @Schema(description = "预警等级: high-高危, medium-中危, low-低危")
    private String level;

    @Schema(description = "预警标题")
    private String title;

    @Schema(description = "预警详情描述")
    private String description;

    @Schema(description = "地块ID")
    private String plotId;

    @Schema(description = "地块名称")
    private String plotName;

    @Schema(description = "基地ID")
    private String baseId;

    @Schema(description = "基地名称")
    private String baseName;

    @Schema(description = "预警日期")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date warningDate;

    @Schema(description = "预警状态: pending-待处理, processing-处理中, resolved-已处理, ignored-已忽略")
    private String warningStatus;

    @Schema(description = "建议措施")
    private String recommendation;

    @Schema(description = "风险评分")
    private BigDecimal riskScore;

    @Schema(description = "处理人")
    private String handler;

    @Schema(description = "处理时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date handleTime;

    @Schema(description = "处理备注")
    private String handleRemark;

    @Schema(description = "是否已过期")
    private Boolean isExpired;

    @Schema(description = "完整预警数据(可选)")
    private Object warningData;
}
