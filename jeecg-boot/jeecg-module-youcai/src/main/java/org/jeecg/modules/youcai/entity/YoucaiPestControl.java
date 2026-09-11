package org.jeecg.modules.youcai.entity;

import org.jeecg.common.system.base.entity.JeecgEntity;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
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
 * @Description: 虫害防控表
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V1.0
 */
@Data
@TableName("youcai_pest_control")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="虫害防控表")
public class YoucaiPestControl extends JeecgEntity {
    private static final long serialVersionUID = 1L;

	/**地块ID*/
	@Excel(name = "地块ID", width = 15)
    @Schema(description = "地块ID")
    private String plotId;
	/**生产计划ID*/
	@Excel(name = "生产计划ID", width = 15)
    @Schema(description = "生产计划ID")
    private String planId;
	/**关联预警ID*/
	@Excel(name = "关联预警ID", width = 15)
    @Schema(description = "关联预警ID")
    private String warningId;
	/**虫害类型*/
	@Excel(name = "虫害类型", width = 15)
    @Schema(description = "虫害类型")
    private String pestType;
	/**虫害名称*/
	@Excel(name = "虫害名称", width = 15)
    @Schema(description = "虫害名称")
    private String pestName;
	/**防控日期*/
	@Excel(name = "防控日期", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "防控日期")
    private Date controlDate;
	/**防控方法*/
	@Excel(name = "防控方法", width = 15)
    @Schema(description = "防控方法")
    private String controlMethod;
	/**农药名称*/
	@Excel(name = "农药名称", width = 15)
    @Schema(description = "农药名称")
    private String pesticideName;
	/**农药类型*/
	@Excel(name = "农药类型", width = 15)
    @Schema(description = "农药类型")
    private String pesticideType;
	/**农药用量(毫升/亩)*/
	@Excel(name = "农药用量(毫升/亩)", width = 15)
    @Schema(description = "农药用量(毫升/亩)")
    private BigDecimal pesticideDosage;
	/**农药浓度(%)*/
	@Excel(name = "农药浓度(%)", width = 15)
    @Schema(description = "农药浓度(%)")
    private BigDecimal pesticideConcentration;
	/**施用方法*/
	@Excel(name = "施用方法", width = 15)
    @Schema(description = "施用方法")
    private String applicationMethod;
	/**防控面积(亩)*/
	@Excel(name = "防控面积(亩)", width = 15)
    @Schema(description = "防控面积(亩)")
    private BigDecimal controlArea;
	/**防控成本(元)*/
	@Excel(name = "防控成本(元)", width = 15)
    @Schema(description = "防控成本(元)")
    private BigDecimal controlCost;
	/**防控效果*/
	@Excel(name = "防控效果", width = 15)
    @Schema(description = "防控效果")
    private String controlEffectiveness;
	/**操作员*/
	@Excel(name = "操作员", width = 15)
    @Schema(description = "操作员")
    private String operator;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @Schema(description = "备注")
    private String notes;
	/**所属部门*/
    @Schema(description = "所属部门")
    private String sysOrgCode;
	/**删除标志（0-正常，1-删除）*/
	@Excel(name = "删除标志（0-正常，1-删除）", width = 15)
    @Schema(description = "删除标志（0-正常，1-删除）")
    @TableLogic
    private Integer delFlag;
}
