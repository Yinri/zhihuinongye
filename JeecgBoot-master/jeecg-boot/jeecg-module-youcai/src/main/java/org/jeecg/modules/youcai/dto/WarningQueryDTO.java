package org.jeecg.modules.youcai.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @Description: 预警查询参数DTO
 * @Author: System
 * @Date: 2025-11-27
 */
@Data
@Schema(description = "预警查询参数")
public class WarningQueryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "预警类型，不传则查询全部: lodging,disease,pest")
    private String warningType;

    @Schema(description = "预警等级: high,medium,low")
    private String level;

    @Schema(description = "预警状态，默认pending: pending,processing,resolved,ignored")
    private String warningStatus;

    @Schema(description = "地块ID")
    private String plotId;

    @Schema(description = "基地ID")
    private String baseId;

    @Schema(description = "查询数量限制，默认20条")
    private Integer limit;

    @Schema(description = "是否只查询未过期的预警，默认true")
    private Boolean onlyValid;
}
