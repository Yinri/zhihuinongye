package org.jeecg.modules.youcai.entity;

import org.jeecg.common.system.base.entity.JeecgEntity;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableField;
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
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;

/**
 * @Description: 地块信息表
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V1.0
 */
@Data
@TableName(value = "youcai_plots", autoResultMap = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="地块信息表")
public class YoucaiPlots extends JeecgEntity {
    private static final long serialVersionUID = 1L;

	/**地块名称*/
	@Excel(name = "地块名称", width = 15)
    @Schema(description = "地块名称")
    private java.lang.String plotName;
	/**所属基地ID*/
	@Excel(name = "所属基地ID", width = 15)
    @Schema(description = "所属基地ID")
    private java.lang.String baseId;
    
    @Excel(name = "地块编码", width = 15)
    @Schema(description = "地块编码")
    private java.lang.String plotCode;
    @Excel(name = "土壤类型", width = 15)
    @Schema(description = "土壤类型")
    private java.lang.String soilType;

    @Excel(name = "地块状态", width = 15)
    @Schema(description = "地块状态（空闲/种植中/休耕）")
    private java.lang.String status;
	/**地块面积(亩)*/
	@Excel(name = "地块面积(亩)", width = 15)
    @Schema(description = "地块面积(亩)")
    private java.math.BigDecimal area;

	/**多边形坐标点(JSON格式)*/
	@TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "多边形坐标点(JSON格式)")
    private String polygonCoords;
	/**所属部门*/
    @Schema(description = "所属部门")
    private java.lang.String sysOrgCode;
	/**删除标志（0-正常，1-删除）*/
	@Excel(name = "删除标志（0-正常，1-删除）", width = 15)
    @Schema(description = "删除标志（0-正常，1-删除）")
    @TableLogic
    private java.lang.Integer delFlag;

}
    
