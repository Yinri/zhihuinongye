package org.jeecg.modules.youcai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import org.jeecg.common.system.base.entity.JeecgEntity;

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
public class YoucaiHistoricalYield {
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "ID")
    private java.lang.String id;

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
    private java.lang.String varietyId;
	/**地块名称*/
	@Excel(name = "地块名称", width = 15)
    @Schema(description = "地块名称")
    private java.lang.String plotName;
    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @Excel(name = "创建时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date createTime;
}
