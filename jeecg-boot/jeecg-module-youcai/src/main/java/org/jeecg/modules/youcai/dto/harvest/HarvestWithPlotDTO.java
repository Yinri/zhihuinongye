package org.jeecg.modules.youcai.dto.harvest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.modules.youcai.entity.YoucaiHarvest;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Description: 收获管理DTO，包含地块信息
 * @Author: jeecg-boot
 * @Date:   2025-11-26
 * @Version: V1.0
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="收获管理DTO，包含地块信息")
public class HarvestWithPlotDTO {
    
    @Schema(description = "主键ID")
    private String id;
    
    @Schema(description = "基地ID")
    private String baseId;
    
    @Schema(description = "地块ID")
    private String plotId;
    
    @Schema(description = "地块编号")
    private String plotCode;
    
    @Schema(description = "地块名称")
    private String plotName;
    
    @Schema(description = "面积(亩)")
    private BigDecimal area;
    
    @Schema(description = "预计产量(kg)")
    private BigDecimal predictYield;
    
    @Schema(description = "实际产量(kg)")
    private BigDecimal totalYield;
    
    @Schema(description = "负责农机")
    private String machineName;
    
    @Schema(description = "收获日期")
    private Date harvestDate;
    
    @Schema(description = "作业人员")
    private String operator;
    
    @Schema(description = "作业时长(小时)")
    private BigDecimal workDuration;
    
    @Schema(description = "备注")
    private String remark;
    
    @Schema(description = "创建人")
    private String createBy;
    
    @Schema(description = "创建时间")
    private Date createTime;
    
    @Schema(description = "更新人")
    private String updateBy;
    
    @Schema(description = "更新时间")
    private Date updateTime;
}