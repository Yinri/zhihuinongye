package org.jeecg.modules.youcai.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Description: 油菜预警记录表
 * @Author: System
 * @Date: 2025-11-27
 * @Version: V1.0
 */
@Data
@TableName(value = "youcai_warning_records", autoResultMap = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "油菜预警记录表")
public class YoucaiWarningRecord extends JeecgEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 预警类型
     */
    @Excel(name = "预警类型", width = 15)
    @Schema(description = "预警类型: lodging-倒伏, disease-病害, pest-虫害")
    private String warningType;

    /**
     * 地块ID
     */
    @Excel(name = "地块ID", width = 15)
    @Schema(description = "地块ID")
    private String plotId;

    /**
     * 地块名称
     */
    @Excel(name = "地块名称", width = 15)
    @Schema(description = "地块名称")
    private String plotName;

    /**
     * 基地ID
     */
    @Excel(name = "基地ID", width = 15)
    @Schema(description = "基地ID")
    private String baseId;

    /**
     * 基地名称
     */
    @Excel(name = "基地名称", width = 15)
    @Schema(description = "基地名称")
    private String baseName;

    /**
     * 预警等级
     */
    @Excel(name = "预警等级", width = 15)
    @Schema(description = "预警等级: high-高危, medium-中危, low-低危")
    private String level;

    /**
     * 预警标题
     */
    @Excel(name = "预警标题", width = 15)
    @Schema(description = "预警标题")
    private String title;

    /**
     * 预警描述
     */
    @Excel(name = "预警描述", width = 15)
    @Schema(description = "预警描述")
    private String description;

    /**
     * 处理建议
     */
    @Excel(name = "处理建议", width = 15)
    @Schema(description = "处理建议")
    private String recommendation;

    /**
     * 风险评分
     */
    @Excel(name = "风险评分", width = 15)
    @Schema(description = "风险评分(0-1)")
    private BigDecimal riskScore;

    /**
     * 预警状态
     */
    @Excel(name = "预警状态", width = 15)
    @Schema(description = "状态: pending-待处理, processing-处理中, resolved-已处理, ignored-已忽略")
    private String warningStatus;

    /**
     * 处理人
     */
    @Excel(name = "处理人", width = 15)
    @Schema(description = "处理人")
    private String handler;

    /**
     * 处理时间
     */
    @Excel(name = "处理时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "处理时间")
    private Date handleTime;

    /**
     * 处理备注
     */
    @Excel(name = "处理备注", width = 15)
    @Schema(description = "处理备注")
    private String handleRemark;

    /**
     * 预警数据快照(JSON格式)
     */
    @Schema(description = "Python服务返回的完整数据(JSON格式)")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object warningData;

    /**
     * 预警生成时间
     */
    @Excel(name = "预警生成时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "预警生成时间")
    private Date warningDate;

    /**
     * 预警过期时间
     */
    @Excel(name = "预警过期时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "预警过期时间")
    private Date expireTime;

    /**
     * 是否已过期
     */
    @Excel(name = "是否已过期", width = 15)
    @Schema(description = "是否已过期: 0-未过期, 1-已过期")
    private Integer isExpired;

    /**
     * 删除标志
     */
    @Excel(name = "删除标志", width = 15)
    @Schema(description = "删除标志（0-正常，1-删除）")
    @TableLogic
    private Integer delFlag;
}
