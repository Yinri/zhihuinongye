package org.jeecg.modules.youcai.entity;

import java.io.Serializable;
import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: 肥料信息表
 * @Author: jeecg-boot
 * @Date:   2025-10-30
 * @Version: V1.0
 */
@Data
@TableName("youcai_fertilizer_info")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="肥料信息表")
public class YoucaiFertilizerInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "肥料ID")
    private java.lang.Integer id;
    
    /**肥料编码*/
    @Excel(name = "肥料编码", width = 15)
    @Schema(description = "肥料编码")
    @NotBlank(message = "肥料编码不能为空")
    @Size(max = 50, message = "肥料编码长度不能超过50个字符")
    private java.lang.String fertilizerCode;
    
    /**肥料名称*/
    @Excel(name = "肥料名称", width = 15)
    @Schema(description = "肥料名称")
    @NotBlank(message = "肥料名称不能为空")
    @Size(max = 100, message = "肥料名称长度不能超过100个字符")
    private java.lang.String fertilizerName;
    
    /**肥料类型*/
    @Excel(name = "肥料类型", width = 15)
    @Schema(description = "肥料类型")
    @NotBlank(message = "肥料类型不能为空")
    @Size(max = 20, message = "肥料类型长度不能超过20个字符")
    private java.lang.String fertilizerType;
    
    /**NPK比例*/
    @Excel(name = "NPK比例", width = 15)
    @Schema(description = "NPK比例")
    private java.lang.String npkRatio;
    
    /**剂型*/
    @Excel(name = "剂型", width = 15)
    @Schema(description = "剂型")
    @NotBlank(message = "剂型不能为空")
    @Size(max = 50, message = "剂型长度不能超过50个字符")
    private java.lang.String formulation;
    
    /**适用作物*/
    @Excel(name = "适用作物", width = 15)
    @Schema(description = "适用作物")
    private java.lang.String applicationCrops;
    
    /**推荐用量范围*/
    @Excel(name = "推荐用量范围", width = 15)
    @Schema(description = "推荐用量范围")
    private java.lang.String dosageRange;
    
    /**施用方法*/
    @Excel(name = "施用方法", width = 15)
    @Schema(description = "施用方法")
    private java.lang.String applicationMethod;
    
    /**生产厂家*/
    @Excel(name = "生产厂家", width = 15)
    @Schema(description = "生产厂家")
    private java.lang.String manufacturer;
    
    /**创建人*/
    @Schema(description = "创建人")
    private java.lang.String createBy;
    
    /**创建时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private java.util.Date createTime;
    
    /**更新人*/
    @Schema(description = "更新人")
    private java.lang.String updateBy;
    
    /**更新时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private java.util.Date updateTime;
    
    /**所属部门*/
    @Schema(description = "所属部门")
    private java.lang.String sysOrgCode;
    
    /**删除标志（0-正常，1-删除）*/
    @Excel(name = "删除标志（0-正常，1-删除）", width = 15)
    @Schema(description = "删除标志（0-正常，1-删除）")
    @TableLogic
    private java.lang.Integer delFlag;
}