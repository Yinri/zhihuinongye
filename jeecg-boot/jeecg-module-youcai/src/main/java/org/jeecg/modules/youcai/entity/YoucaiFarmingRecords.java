package org.jeecg.modules.youcai.entity;

import org.jeecg.common.system.base.entity.JeecgEntity;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
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
public class YoucaiFarmingRecords extends JeecgEntity {
    private static final long serialVersionUID = 1L;

    
    /**记录编号*/
    @Excel(name = "记录编号", width = 15)
    @Schema(description = "记录编号")
    private String recordCode;
    
    /**基地ID*/
    @Excel(name = "基地ID", width = 15)
    @Schema(description = "基地ID")
    private String baseId;
    
    /**地块ID*/
    @Excel(name = "地块ID", width = 15)
    @Schema(description = "地块ID")
    private String plotId;
    
    /**农事类型：1-播种，2-施肥，3-灌溉，4-除草，5-病虫害防治，6-收获，7-其他*/
    @Excel(name = "农事类型", width = 15)
    @Schema(description = "农事类型：1-播种，2-施肥，3-灌溉，4-除草，5-病虫害防治，6-收获，7-其他")
    private Integer farmingType;
    
    /**农事日期*/
    @Excel(name = "农事日期", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "农事日期")
    private Date farmingDate;
    
    /**作业人员*/
    @Excel(name = "作业人员", width = 15)
    @Schema(description = "作业人员")
    private String worker;
    
    /**作业面积(亩)*/
    @Excel(name = "作业面积(亩)", width = 15)
    @Schema(description = "作业面积(亩)")
    private BigDecimal workArea;
    
    /**使用物资（多个用逗号分隔）*/
    @Excel(name = "使用物资", width = 30)
    @Schema(description = "使用物资（多个用逗号分隔）")
    private String materials;
    
    /**物资用量（与物资顺序对应，单位需一致）*/
    @Excel(name = "物资用量", width = 15)
    @Schema(description = "物资用量（与物资顺序对应，单位需一致）")
    private String materialAmount;
    
    /**作业时长(小时)*/
    @Excel(name = "作业时长(小时)", width = 15)
    @Schema(description = "作业时长(小时)")
    private BigDecimal workDuration;
    
    /**作业状态：1-计划中，2-进行中，3-已完成，4-已取消*/
    @Excel(name = "作业状态", width = 15)
    @Schema(description = "作业状态：1-计划中，2-进行中，3-已完成，4-已取消")
    private Integer workStatus;
    
    /**备注*/
    @Excel(name = "备注", width = 30)
    @Schema(description = "备注")
    private String remark;
    
    
    /**删除标志（0代表存在 1代表删除）*/
    @Schema(description = "删除标志（0代表存在 1代表删除）")
    private Integer delFlag;
}
