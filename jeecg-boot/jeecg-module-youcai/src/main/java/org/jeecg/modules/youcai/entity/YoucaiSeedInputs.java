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
 * @Description: 种子投入表
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V1.0
 */
@Data
@TableName("youcai_seed_inputs")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="种子投入表")
public class YoucaiSeedInputs extends JeecgEntity {
    private static final long serialVersionUID = 1L;

	/**生产计划ID*/
	@Excel(name = "生产计划ID", width = 15)
    @Schema(description = "生产计划ID")
    private java.lang.String planId;
	/**品种ID*/
	@Excel(name = "品种ID", width = 15)
    @Schema(description = "品种ID")
    private java.lang.String varietyId;
	/**种子用量(公斤)*/
	@Excel(name = "种子用量(公斤)", width = 15)
    @Schema(description = "种子用量(公斤)")
    private java.math.BigDecimal seedAmount;
	/**种子成本(元)*/
	@Excel(name = "种子成本(元)", width = 15)
    @Schema(description = "种子成本(元)")
    private java.math.BigDecimal seedCost;
	/**购买日期*/
	@Excel(name = "购买日期", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "购买日期")
    private java.util.Date purchaseDate;
	/**供应商*/
	@Excel(name = "供应商", width = 15)
    @Schema(description = "供应商")
    private java.lang.String supplier;
	/**所属部门*/
    @Schema(description = "所属部门")
    private java.lang.String sysOrgCode;
	/**删除标志（0-正常，1-删除）*/
	@Excel(name = "删除标志（0-正常，1-删除）", width = 15)
    @Schema(description = "删除标志（0-正常，1-删除）")
    @TableLogic
    private java.lang.Integer delFlag;
}
