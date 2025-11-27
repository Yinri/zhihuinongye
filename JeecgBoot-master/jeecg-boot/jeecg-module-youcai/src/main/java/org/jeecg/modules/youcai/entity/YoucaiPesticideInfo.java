package org.jeecg.modules.youcai.entity;

import org.jeecg.common.system.base.entity.JeecgEntity;
import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.DecimalMax;
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
 * @Description: 农药信息表
 * @Author: jeecg-boot
 * @Date:   2025-10-30
 * @Version: V1.0
 */
@Data
@TableName("youcai_pesticide_info")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="农药信息表")
public class YoucaiPesticideInfo extends JeecgEntity {
    private static final long serialVersionUID = 1L;

    
    /**农药编码*/
    @Excel(name = "农药编码", width = 15)
    @Schema(description = "农药编码")
    @NotBlank(message = "农药编码不能为空")
    @Size(max = 50, message = "农药编码长度不能超过50个字符")
    private java.lang.String pesticideCode;
    
    /**农药名称*/
    @Excel(name = "农药名称", width = 15)
    @Schema(description = "农药名称")
    @NotBlank(message = "农药名称不能为空")
    @Size(max = 100, message = "农药名称长度不能超过100个字符")
    private java.lang.String pesticideName;
    
    /**农药类型*/
    @Excel(name = "农药类型", width = 15)
    @Schema(description = "农药类型")
    @NotBlank(message = "农药类型不能为空")
    @Size(max = 20, message = "农药类型长度不能超过20个字符")
    private java.lang.String pesticideType;
    
    /**有效成分*/
    @Excel(name = "有效成分", width = 15)
    @Schema(description = "有效成分")
    @NotBlank(message = "有效成分不能为空")
    @Size(max = 100, message = "有效成分长度不能超过100个字符")
    private java.lang.String activeIngredient;
    
    /**有效成分含量(%)*/
    @Excel(name = "有效成分含量(%)", width = 15)
    @Schema(description = "有效成分含量(%)")
    @NotNull(message = "有效成分含量不能为空")
    @DecimalMin(value = "0.01", message = "有效成分含量必须大于0")
    @DecimalMax(value = "100.00", message = "有效成分含量不能超过100%")
    private java.math.BigDecimal content;
    
    /**剂型*/
    @Excel(name = "剂型", width = 15)
    @Schema(description = "剂型")
    @NotBlank(message = "剂型不能为空")
    @Size(max = 50, message = "剂型长度不能超过50个字符")
    private java.lang.String formulation;
    
    /**毒性级别*/
    @Excel(name = "毒性级别", width = 15)
    @Schema(description = "毒性级别")
    @NotBlank(message = "毒性级别不能为空")
    @Size(max = 10, message = "毒性级别长度不能超过10个字符")
    private java.lang.String toxicityLevel;
    
    /**防治对象*/
    @Excel(name = "防治对象", width = 15)
    @Schema(description = "防治对象")
    private java.lang.String targetPests;
    
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
    
    
    /**所属部门*/
    @Schema(description = "所属部门")
    private java.lang.String sysOrgCode;
    
    /**删除标志（0-正常，1-删除）*/
    @Excel(name = "删除标志（0-正常，1-删除）", width = 15)
    @Schema(description = "删除标志（0-正常，1-删除）")
    @TableLogic
    private java.lang.Integer delFlag;
}
