package org.jeecg.modules.youcai.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: youcai_historical_yield
 * @Author: jeecg-boot
 * @Date:   2025-11-05
 * @Version: V1.0
 */
@Data
@TableName("youcai_historical_yield")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="youcai_historical_yield")
public class YoucaiHistoricalYield implements Serializable {
    private static final long serialVersionUID = 1L;

	@TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private java.lang.Long id;
	/**年份（如2022）*/
	@Excel(name = "年份（如2022）", width = 15)
    @Schema(description = "年份（如2022）")
    private java.lang.Integer year;
	/**单产（kg/亩）*/
	@Excel(name = "单产（kg/亩）", width = 15)
    @Schema(description = "单产（kg/亩）")
    private java.math.BigDecimal yield;
	/**品种ID（关联品种表）*/
	@Excel(name = "品种ID（关联品种表）", width = 15)
    @Schema(description = "品种ID（关联品种表）")
    private java.lang.Integer varietyId;
	/**地块名称*/
	@Excel(name = "地块名称", width = 15)
    @Schema(description = "地块名称")
    private java.lang.String plot;
	/**录入时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "录入时间")
    private java.util.Date createTime;
}
