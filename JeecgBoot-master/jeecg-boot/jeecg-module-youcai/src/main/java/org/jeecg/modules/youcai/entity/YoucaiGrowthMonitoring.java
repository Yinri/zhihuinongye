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
 * @Description: 生长监控表
 * @Author: jeecg-boot
 * @Date:   2025-11-06
 * @Version: V1.0
 */
@Data
@TableName("youcai_growth_monitoring")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="生长监控表")
public class YoucaiGrowthMonitoring extends JeecgEntity {
    private static final long serialVersionUID = 1L;

	/**地块ID*/
	@Excel(name = "地块ID", width = 15)
    @Schema(description = "地块ID")
    private java.lang.String plotId;
	/**监测日期*/
	@Excel(name = "监测日期", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "监测日期")
    private java.util.Date monitoringDate;
	/**生长阶段*/
	@Excel(name = "生长阶段", width = 15)
    @Schema(description = "生长阶段")
    private java.lang.String growthStage;
	/**植株高度(cm)*/
	@Excel(name = "植株高度(cm)", width = 15)
    @Schema(description = "植株高度(cm)")
    private Double plantHeight;
	/**茎直径(mm)*/
	@Excel(name = "茎直径(mm)", width = 15)
    @Schema(description = "茎直径(mm)")
    private Double stemDiameter;
	/**种植密度(株/亩)*/
	@Excel(name = "种植密度(株/亩)", width = 15)
    @Schema(description = "种植密度(株/亩)")
    private Double density;
	/**健康状况*/
	@Excel(name = "健康状况", width = 15)
    @Schema(description = "健康状况")
    private java.lang.String healthStatus;
	/**备注信息*/
	@Excel(name = "备注信息", width = 15)
    @Schema(description = "备注信息")
    private java.lang.String notes;
	/**所属部门*/
    @Schema(description = "所属部门")
    private java.lang.String sysOrgCode;
	/**删除标志（0-正常，1-删除）*/
	@Excel(name = "删除标志（0-正常，1-删除）", width = 15)
    @Schema(description = "删除标志（0-正常，1-删除）")
    @TableLogic
    private java.lang.Integer delFlag;
}
