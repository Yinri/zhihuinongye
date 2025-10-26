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
 * @Description: 土壤肥力表
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V1.0
 */
@Data
@TableName("youcai_soil_fertility")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="土壤肥力表")
public class YoucaiSoilFertility implements Serializable {
    private static final long serialVersionUID = 1L;

	/**肥力ID*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "肥力ID")
    private java.lang.Integer id;
	/**地块ID*/
	@Excel(name = "地块ID", width = 15)
    @Schema(description = "地块ID")
    private java.lang.Integer plotId;
	/**检测日期*/
	@Excel(name = "检测日期", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "检测日期")
    private java.util.Date testDate;
	/**pH值*/
	@Excel(name = "pH值", width = 15)
    @Schema(description = "pH值")
    private java.math.BigDecimal phValue;
	/**有机质含量(g/kg)*/
	@Excel(name = "有机质含量(g/kg)", width = 15)
    @Schema(description = "有机质含量(g/kg)")
    private java.math.BigDecimal organicMatter;
	/**氮含量(mg/kg)*/
	@Excel(name = "氮含量(mg/kg)", width = 15)
    @Schema(description = "氮含量(mg/kg)")
    private java.math.BigDecimal nitrogen;
	/**磷含量(mg/kg)*/
	@Excel(name = "磷含量(mg/kg)", width = 15)
    @Schema(description = "磷含量(mg/kg)")
    private java.math.BigDecimal phosphorus;
	/**钾含量(mg/kg)*/
	@Excel(name = "钾含量(mg/kg)", width = 15)
    @Schema(description = "钾含量(mg/kg)")
    private java.math.BigDecimal potassium;
	/**肥力等级*/
	@Excel(name = "肥力等级", width = 15)
    @Schema(description = "肥力等级")
    private java.lang.String fertilityLevel;
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
