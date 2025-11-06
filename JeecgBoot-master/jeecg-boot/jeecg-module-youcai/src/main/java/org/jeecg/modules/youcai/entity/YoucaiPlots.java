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
 * @Description: 地块信息表
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V1.0
 */
@Data
@TableName("youcai_plots")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="地块信息表")
public class YoucaiPlots implements Serializable {
    private static final long serialVersionUID = 1L;

	/**地块ID*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "地块ID")
    private java.lang.Integer id;
	/**地块名称*/
	@Excel(name = "地块名称", width = 15)
    @Schema(description = "地块名称")
    private java.lang.String plotName;
	/**所属基地ID*/
	@Excel(name = "所属基地ID", width = 15)
    @Schema(description = "所属基地ID")
    private java.lang.Integer baseId;
	/**地块面积(亩)*/
	@Excel(name = "地块面积(亩)", width = 15)
    @Schema(description = "地块面积(亩)")
    private java.math.BigDecimal area;
    /**生长阶段*/
    @Excel(name = "生长阶段", width = 15, dicCode = "base_growth_stage")
    @Schema(description = "生长阶段（未播种/已播种/苗期/蕾薹期/开花期/角果成熟期/收获与整地）")
    private java.lang.String growthStage;
	/**纬度*/
	@Excel(name = "纬度", width = 15)
    @Schema(description = "纬度")
    private java.math.BigDecimal latitude;
	/**经度*/
	@Excel(name = "经度", width = 15)
    @Schema(description = "经度")
    private java.math.BigDecimal longitude;
	/**创建人*/
    @Schema(description = "创建人")
    private java.lang.String createBy;
	/**创建日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "创建日期")
    private java.util.Date createTime;
	/**更新人*/
    @Schema(description = "更新人")
    private java.lang.String updateBy;
	/**更新日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "更新日期")
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
