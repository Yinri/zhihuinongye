package org.jeecg.modules.youcai.entity;

import org.jeecg.common.system.base.entity.JeecgEntity;
import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: 油菜苗素质监测表
 * @Author: jeecg-boot
 * @Date: 2025-12-08
 * @Version: V1.0
 */
@Data
@TableName("youcai_color_quality")
@Accessors(chain = true) 
@EqualsAndHashCode(callSuper = false)
@Schema(description = "油菜苗素质监测表")
public class YoucaiColorQuality extends JeecgEntity {

    private static final long serialVersionUID = 1L;

    /** 所属基地ID */
    @Excel(name = "所属基地ID", width = 15)
    @Schema(description = "所属基地ID")
    private String baseId;

    /** 所属地块ID */
    @Excel(name = "所属地块ID", width = 15)
    @Schema(description = "所属地块ID")
    private String plotId;

    /** 监测日期 */
    @Excel(name = "监测日期", width = 15, format = "yyyy-MM-dd")
    @Schema(description = "监测日期")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private java.util.Date monitorDate;



    /** 颜色指数(NDVI/SPAD) */
    @Excel(name = "颜色指数", width = 15)
    @Schema(description = "颜色指数(NDVI 或叶绿素指数)")
    private BigDecimal colorIndex;

    /** 肥料种类 */
    @Excel(name = "肥料种类", width = 15)
    @Schema(description = "肥料种类")
    private String fertilizerType;

    /** 肥料用量(kg/亩) */
    @Excel(name = "肥料用量(kg/亩)", width = 15)
    @Schema(description = "肥料用量(kg/亩)")
    private BigDecimal fertilizerAmount;

    /** 施肥方式（手动输入） */
    @Excel(name = "施肥方式", width = 15)
    @Schema(description = "施肥方式（手动输入，如：基肥/追肥/叶面喷施）")
    private String fertilizerMethod;

    /** 监测/施肥成本(元) */
    @Excel(name = "成本(元)", width = 15)
    @Schema(description = "监测或施肥成本(元)")
    private BigDecimal operationCost;

    /** 操作员 */
    @Excel(name = "操作员", width = 15)
    @Schema(description = "操作员")
    private String operator;

    /** 备注 */
    @Excel(name = "备注", width = 30)
    @Schema(description = "备注")
    private String notes;

    /** 所属部门 */
    @Schema(description = "所属部门")
    private String sysOrgCode;

    @Excel(name = "删除标志（0-正常，1-删除）", width = 15)
    @Schema(description = "删除标志（0-正常，1-删除）")
    @TableLogic
    private Integer delFlag;

}
