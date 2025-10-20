package org.jeecg.modules.youcai.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import org.jeecg.common.constant.ProvinceCityArea;
import org.jeecg.common.util.SpringContextUtils;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: 灌溉监控表
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V1.0
 */
@Data
@TableName("youcai_irrigation_monitoring")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="灌溉监控表")
public class YoucaiIrrigationMonitoring implements Serializable {
    private static final long serialVersionUID = 1L;

	/**灌溉ID*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "灌溉ID")
    private java.lang.Integer id;
	/**地块ID*/
	@Excel(name = "地块ID", width = 15)
    @Schema(description = "地块ID")
    private java.lang.Integer plotId;
	/**生产计划ID*/
	@Excel(name = "生产计划ID", width = 15)
    @Schema(description = "生产计划ID")
    private java.lang.Integer planId;
	/**灌溉日期*/
	@Excel(name = "灌溉日期", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "灌溉日期")
    private java.util.Date irrigationDate;
	/**灌溉方式*/
	@Excel(name = "灌溉方式", width = 15)
    @Schema(description = "灌溉方式")
    private java.lang.String irrigationMethod;
	/**灌溉量(立方米)*/
	@Excel(name = "灌溉量(立方米)", width = 15)
    @Schema(description = "灌溉量(立方米)")
    private java.math.BigDecimal waterAmount;
	/**灌溉时长(分钟)*/
	@Excel(name = "灌溉时长(分钟)", width = 15)
    @Schema(description = "灌溉时长(分钟)")
    private java.lang.Integer irrigationDuration;
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
