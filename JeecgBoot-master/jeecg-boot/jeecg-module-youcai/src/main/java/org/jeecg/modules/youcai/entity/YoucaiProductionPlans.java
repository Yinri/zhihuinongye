package org.jeecg.modules.youcai.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: 生产计划表
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V1.0
 */
@Data
@TableName("youcai_production_plans")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="生产计划表")
public class YoucaiProductionPlans implements Serializable {
    private static final long serialVersionUID = 1L;

	@TableId(type = IdType.AUTO)
	@Schema(description = "计划ID")
	private java.lang.Integer id;
	/**地块ID*/
	@Excel(name = "地块ID", width = 15)
    @Schema(description = "地块ID")
    private java.lang.Integer plotId;
	/**计划年份*/
	@Excel(name = "计划年份", width = 15)
    @Schema(description = "计划年份")
    private java.lang.String planYear;
	/**油菜品种ID*/
	@Excel(name = "油菜品种ID", width = 15)
    @Schema(description = "油菜品种ID")
    private java.lang.Integer varietyId;
	/**目标产量(公斤/亩)*/
	@Excel(name = "目标产量(公斤/亩)", width = 15)
    @Schema(description = "目标产量(公斤/亩)")
    private java.math.BigDecimal targetYield;
	/**种植面积(亩)*/
	@Excel(name = "种植面积(亩)", width = 15)
    @Schema(description = "种植面积(亩)")
    private java.math.BigDecimal plantingArea;
	/**计划播种日期*/
	@Excel(name = "计划播种日期", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "计划播种日期")
    private java.util.Date plannedSowingDate;
	/**计划收获日期*/
	@Excel(name = "计划收获日期", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "计划收获日期")
    private java.util.Date plannedHarvestDate;
	/**计划状态*/
	@Excel(name = "计划状态", width = 15)
    @Schema(description = "计划状态")
    private java.lang.String status;
	/**创建人*/
    @Schema(description = "创建人")
    private java.lang.String createBy;
	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "创建时间")
    private java.util.Date createTime;
	/**更新人*/
    @Schema(description = "更新人")
    private java.lang.String updateBy;
	/**更新时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "更新时间")
    private java.util.Date updateTime;
	/**所属部门*/
    @Schema(description = "所属部门")
    private java.lang.String sysOrgCode;
	/**删除标志（0-正常，1-删除）*/
	@Excel(name = "删除标志（0-正常，1-删除）", width = 15)
    @Schema(description = "删除标志（0-正常，1-删除）")
    @TableLogic
    private java.lang.Integer delFlag;
}
