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
 * @Description: 播种监控表
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V1.0
 */
@Data
@TableName("youcai_sowing_monitoring")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="播种监控表")
public class YoucaiSowingMonitoring implements Serializable {
    private static final long serialVersionUID = 1L;

	@TableId(type = IdType.AUTO)
	@Schema(description = "播种ID")
	private java.lang.Integer id;
	/**地块ID*/
	@Excel(name = "地块ID", width = 15)
    @Schema(description = "地块ID")
    private java.lang.Integer plotId;
	/**生产计划ID*/
	@Excel(name = "生产计划ID", width = 15)
    @Schema(description = "生产计划ID")
    private java.lang.Integer planId;
	/**播种日期*/
	@Excel(name = "播种日期", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "播种日期")
    private java.util.Date sowingDate;
	/**播种方式*/
	@Excel(name = "播种方式", width = 15)
    @Schema(description = "播种方式")
    private java.lang.String sowingMethod;
	/**播种量(公斤/亩)*/
	@Excel(name = "播种量(公斤/亩)", width = 15)
    @Schema(description = "播种量(公斤/亩)")
    private java.math.BigDecimal seedingRate;
	/**实际播种面积(亩)*/
	@Excel(name = "实际播种面积(亩)", width = 15)
    @Schema(description = "实际播种面积(亩)")
    private java.math.BigDecimal actualSowingArea;
	/**播种状态*/
	@Excel(name = "播种状态", width = 15)
    @Schema(description = "播种状态")
    private java.lang.String sowingStatus;
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
