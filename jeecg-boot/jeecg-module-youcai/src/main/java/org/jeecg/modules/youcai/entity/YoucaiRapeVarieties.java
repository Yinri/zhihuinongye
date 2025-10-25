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
 * @Description: 油菜品种表
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V1.0
 */
@Data
@TableName("youcai_rape_varieties")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="油菜品种表")
public class YoucaiRapeVarieties implements Serializable {
    private static final long serialVersionUID = 1L;

	/**品种ID*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "品种ID")
    private java.lang.Integer id;
	/**品种名称*/
	@Excel(name = "品种名称", width = 15)
    @Schema(description = "品种名称")
    private java.lang.String varietyName;
	/**品种特性*/
	@Excel(name = "品种特性", width = 15)
    @Schema(description = "品种特性")
    private java.lang.String characteristics;
	/**生长周期(天)*/
	@Excel(name = "生长周期(天)", width = 15)
    @Schema(description = "生长周期(天)")
    private java.lang.Integer growthCycle;
	/**产量潜力(公斤/亩)*/
	@Excel(name = "产量潜力(公斤/亩)", width = 15)
    @Schema(description = "产量潜力(公斤/亩)")
    private java.math.BigDecimal yieldPotential;
	/**抗病性*/
	@Excel(name = "抗病性", width = 15)
    @Schema(description = "抗病性")
    private java.lang.String diseaseResistance;
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
