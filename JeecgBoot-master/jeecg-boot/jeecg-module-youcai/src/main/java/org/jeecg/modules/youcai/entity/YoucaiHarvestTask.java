package org.jeecg.modules.youcai.entity;

import org.jeecg.common.system.base.entity.JeecgEntity;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("youcai_harvest_task")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "收获计划任务")
public class YoucaiHarvestTask extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;


    @Excel(name = "基地ID", width = 15)
    @Schema(description = "基地ID")
    private String baseId;

    @Excel(name = "地块ID", width = 15)
    @Schema(description = "地块ID")
    private String plotId;

    @Excel(name = "任务标题", width = 20)
    @Schema(description = "任务标题")
    private String taskTitle;

    @Excel(name = "计划收获日期", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "计划收获日期")
    private Date plannedDate;

    @Excel(name = "计划收割面积(亩)", width = 15)
    @Schema(description = "计划收割面积(亩)")
    private BigDecimal plannedArea;

    @Excel(name = "分配农机ID", width = 15)
    @Schema(description = "分配农机ID")
    private String assignedMachineId;

    @Excel(name = "状态", width = 12)
    @Schema(description = "状态：计划中|执行中|已完成|已取消")
    private String status;

    @Excel(name = "执行进度(%)", width = 12)
    @Schema(description = "执行进度百分比(0-100)")
    private BigDecimal progressPercent;

    @Excel(name = "备注", width = 30)
    @Schema(description = "备注")
    private String remark;


    @TableLogic
    @Schema(description = "删除标志（0-正常，1-删除）")
    private Integer delFlag;
}
