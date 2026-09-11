package org.jeecg.modules.youcai.entity;

import org.jeecg.common.system.base.entity.JeecgEntity;
import java.math.BigDecimal;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @Description: 收获管理记录
 * @Author: jeecg-boot
 * @Date:   2025-11-20
 * @Version: V1.0
 */
@Data
@TableName("youcai_harvest")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="收获管理记录")
public class YoucaiHarvest extends JeecgEntity {
    private static final long serialVersionUID = 1L;


    /** 基地ID */
    @Excel(name = "基地ID", width = 15)
    @Schema(description = "基地ID")
    private String baseId;

    /** 地块ID */
    @Excel(name = "地块ID", width = 15)
    @Schema(description = "地块ID")
    private String plotId;

    /** 本次收割面积(亩) */
    @Excel(name = "面积(亩)", width = 15)
    @Schema(description = "面积(亩)")
    private BigDecimal area;

    /** 预计产量(kg) */
    @Excel(name = "预计产量(kg)", width = 18)
    @Schema(description = "预计产量(kg)")
    private BigDecimal predictYield;

    /** 实际产量(kg) */
    @Excel(name = "实际产量(kg)", width = 18)
    @Schema(description = "实际产量(kg)")
    private BigDecimal totalYield;

    /** 负责农机 */
    @Excel(name = "负责农机", width = 20)
    @Schema(description = "负责农机")
    private String machineName;

    /** 收获日期 */
    @Excel(name = "收获日期", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "收获日期（精确到时分秒）")
    private Date harvestDate;

    /** 作业人员 */
    @Excel(name = "作业人员", width = 15)
    @Schema(description = "作业人员")
    private String operator;

    /** 作业时长(小时) */
    @Excel(name = "作业时长(小时)", width = 15)
    @Schema(description = "作业时长(小时)")
    private BigDecimal workDuration;

    /** 作业备注 */
    @Excel(name = "备注", width = 30)
    @Schema(description = "备注")
    private String remark;


    /** 删除标志（0-正常，1-删除） */
    @Schema(description = "删除标志（0-正常，1-删除）")
    @TableLogic
    private Integer delFlag;
}
