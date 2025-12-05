package org.jeecg.modules.youcai.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: crop_nutrient_demand
 * @Author: jeecg-boot
 * @Date:   2025-12-02
 * @Version: V1.0
 */
@Data
@TableName("crop_nutrient_demand")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="crop_nutrient_demand")
public class CropNutrientDemand implements Serializable {
    private static final long serialVersionUID = 1L;

    /**主键ID*/
    // 修复：ASSIGN_ID对应Long类型，Integer会溢出
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private java.lang.Long id;

    /**作物品种ID（关联品种表）*/
    @Excel(name = "作物品种ID（关联品种表）", width = 15)
    @Schema(description = "作物品种ID（关联品种表）")
    private java.lang.String varietyId;

    /**N肥需肥量（kg/100kg产量）*/
    @Excel(name = "N肥需肥量（kg/100kg产量）", width = 15)
    @Schema(description = "N肥需肥量（kg/100kg产量）")
    @JsonProperty("nDemand") // 正确匹配前端nDemand
    private java.math.BigDecimal nDemand;

    /**P₂O₅需肥量（kg/100kg产量）*/
    @Excel(name = "P₂O₅需肥量（kg/100kg产量）", width = 15)
    @Schema(description = "P₂O₅需肥量（kg/100kg产量）")
    @JsonProperty("pDemand") // 修复：之前错写为nDemand
    private java.math.BigDecimal pDemand;

    /**K₂O需肥量（kg/100kg产量）*/
    @Excel(name = "K₂O需肥量（kg/100kg产量）", width = 15)
    @Schema(description = "K₂O需肥量（kg/100kg产量）")
    @JsonProperty("kDemand") // 修复：之前错写为nDemand
    private java.math.BigDecimal kDemand;

    /**创建时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private java.util.Date createTime;

    /**更新时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private java.util.Date updateTime;

    /**创建人*/
    @Schema(description = "创建人")
    private java.lang.String createBy;

    /**更新人*/
    @Schema(description = "更新人")
    private java.lang.String updateBy;
}