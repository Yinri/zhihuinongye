package org.jeecg.modules.youcai.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @Description: 农事记录表
 * @Author: jeecg-boot
 * @Date: 2025-10-30
 * @Version: V1.0
 */
@Data
@TableName("youcai_farming_records")
@Schema(description="农事记录表")
public class YoucaiFarmingRecords implements Serializable {
    private static final long serialVersionUID = 1L;

    /**主键ID*/
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private java.lang.String id;
    
    /**记录编号*/
    @Excel(name = "记录编号", width = 15)
    @Schema(description = "记录编号")
    private java.lang.String recordCode;
    
    /**基地ID*/
    @Excel(name = "基地ID", width = 15)
    @Schema(description = "基地ID")
    private java.lang.Integer baseId;
    
    /**地块ID*/
    @Excel(name = "地块ID", width = 15)
    @Schema(description = "地块ID")
    private java.lang.String plotId;
    
    /**地块名称*/
    @Excel(name = "地块名称", width = 15)
    @Schema(description = "地块名称")
    private java.lang.String plotName;
    
    /**农事类型：1-播种，2-施肥，3-灌溉，4-除草，5-病虫害防治，6-收获，7-其他*/
    @Excel(name = "农事类型", width = 15, dicCode = "farming_type")
    @Dict(dicCode = "farming_type")
    @Schema(description = "农事类型：1-播种，2-施肥，3-灌溉，4-除草，5-病虫害防治，6-收获，7-其他")
    private java.lang.Integer farmingType;
    
    /**农事日期*/
    @Excel(name = "农事日期", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "农事日期")
    private java.util.Date farmingDate;
    
    /**作业人员*/
    @Excel(name = "作业人员", width = 15)
    @Schema(description = "作业人员")
    private java.lang.String worker;
    
    /**作业面积(亩)*/
    @Excel(name = "作业面积(亩)", width = 15)
    @Schema(description = "作业面积(亩)")
    private java.math.BigDecimal workArea;
    
    /**使用物资*/
    @Excel(name = "使用物资", width = 30)
    @Schema(description = "使用物资")
    private java.lang.String materials;
    
    /**物资用量*/
    @Excel(name = "物资用量", width = 15)
    @Schema(description = "物资用量")
    private java.lang.String materialAmount;
    
    /**作业时长(小时)*/
    @Excel(name = "作业时长(小时)", width = 15)
    @Schema(description = "作业时长(小时)")
    private java.math.BigDecimal workDuration;
    
    /**作业状态：1-计划中，2-进行中，3-已完成，4-已取消*/
    @Excel(name = "作业状态", width = 15, dicCode = "work_status")
    @Dict(dicCode = "work_status")
    @Schema(description = "作业状态：1-计划中，2-进行中，3-已完成，4-已取消")
    private java.lang.Integer workStatus;
    
    /**备注*/
    @Excel(name = "备注", width = 30)
    @Schema(description = "备注")
    private java.lang.String remark;
    
    /**创建人*/
    @Schema(description = "创建人")
    private java.lang.String createBy;
    
    /**创建日期*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建日期")
    private java.util.Date createTime;
    
    /**更新人*/
    @Schema(description = "更新人")
    private java.lang.String updateBy;
    
    /**更新日期*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新日期")
    private java.util.Date updateTime;
    
    /**删除标志（0代表存在 1代表删除）*/
    @Schema(description = "删除标志（0代表存在 1代表删除）")
    private java.lang.Integer delFlag;
}