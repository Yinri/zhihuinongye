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
 * @Description: 肥料投入表
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V1.0
 */
@Data
@TableName("youcai_fertilizer_inputs")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="肥料投入表")
public class YoucaiFertilizerInputs implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "肥料ID")
    private java.lang.Integer id;
	/**生产计划ID*/
	@Excel(name = "生产计划ID", width = 15)
    @Schema(description = "生产计划ID")
    private java.lang.Integer planId;
	/**肥料类型*/
	@Excel(name = "肥料类型", width = 15)
    @Schema(description = "肥料类型")
    private java.lang.String fertilizerType;
	/**肥料名称*/
	@Excel(name = "肥料名称", width = 15)
    @Schema(description = "肥料名称")
    private java.lang.String fertilizerName;
	/**NPK比例*/
	@Excel(name = "NPK比例", width = 15)
    @Schema(description = "NPK比例")
    private java.lang.String npkRatio;
	/**施肥量(公斤)*/
	@Excel(name = "施肥量(公斤)", width = 15)
    @Schema(description = "施肥量(公斤)")
    private java.math.BigDecimal amount;
	/**成本(元)*/
	@Excel(name = "成本(元)", width = 15)
    @Schema(description = "成本(元)")
    private java.math.BigDecimal cost;
	/**施肥日期*/
	@Excel(name = "施肥日期", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "施肥日期")
    private java.util.Date applicationDate;
	/**施肥方式*/
	@Excel(name = "施肥方式", width = 15)
    @Schema(description = "施肥方式")
    private java.lang.String applicationMethod;
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
