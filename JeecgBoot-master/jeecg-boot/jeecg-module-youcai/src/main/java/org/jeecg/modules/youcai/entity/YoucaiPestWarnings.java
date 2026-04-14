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
 * @Description: 虫害预警表
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V1.0
 */
@Data
@TableName("youcai_pest_warnings")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="虫害预警表")
public class YoucaiPestWarnings extends JeecgEntity {
    private static final long serialVersionUID = 1L;

	/**地块ID*/
	@Excel(name = "地块ID", width = 15)
    @Schema(description = "地块ID")
    private String plotId;
	/**预警日期*/
	@Excel(name = "预警日期", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "预警日期")
    private Date warningDate;
	/**虫害类型*/
	@Excel(name = "虫害类型", width = 15)
    @Schema(description = "虫害类型")
    private String pestType;
	/**虫害名称*/
	@Excel(name = "虫害名称", width = 15)
    @Schema(description = "虫害名称")
    private String pestName;
	/**严重程度*/
	@Excel(name = "严重程度", width = 15)
    @Schema(description = "严重程度")
    private String severity;
	/**危害症状*/
	@Excel(name = "危害症状", width = 15)
    @Schema(description = "危害症状")
    private String symptoms;
	/**防治建议*/
	@Excel(name = "防治建议", width = 15)
    @Schema(description = "防治建议")
    private String recommendation;
	/**预警状态*/
	@Excel(name = "预警状态", width = 15)
    @Schema(description = "预警状态")
    private String warningStatus;
	/**虫害影像*/
	@Excel(name = "虫害影像", width = 15)
    @Schema(description = "虫害影像")
    private String imageUrl;
	/**所属部门*/
    @Schema(description = "所属部门")
    private String sysOrgCode;
	/**删除标志（0-正常，1-删除）*/
	@Excel(name = "删除标志（0-正常，1-删除）", width = 15)
    @Schema(description = "删除标志（0-正常，1-删除）")
    @TableLogic
    private Integer delFlag;
}
