package org.jeecg.modules.youcai.dto.harvest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class PlotHarvestSummaryDTO {
    @Schema(description = "地块ID")
    private String plotId;

    @Schema(description = "基地ID")
    private String baseId;

    @Schema(description = "地块面积(亩)")
    private BigDecimal plotArea;

    @Schema(description = "已收割面积(亩)")
    private BigDecimal harvestedArea;

    @Schema(description = "累计产量(kg)")
    private BigDecimal harvestedYield;

    @Schema(description = "最近收割日期")
    private Date lastHarvestDate;

    @Schema(description = "收割状态：0未收割，1收割中，2已收割")
    private Integer harvestStatus;

    @Schema(description = "收割进度(%)")
    private BigDecimal harvestPercent;
}