package org.jeecg.modules.youcai.entity;

import org.jeecg.common.system.base.entity.JeecgEntity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Description: 生产计划表
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V1.0
 */
@Data
@TableName("youcai_production_plans")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="生产计划表")
public class YoucaiProductionPlans extends JeecgEntity {
    private static final long serialVersionUID = 1L;

    @Excel(name = "基地ID", width = 15)
    @Schema(description = "基地ID")
    private String baseId;

    @Excel(name = "计划名称", width = 20)
    @Schema(description = "计划名称")
    private String planName;

    /** 地块ID */
    @Excel(name = "地块ID", width = 15)
    @Schema(description = "地块ID")
    private String plotId;

    /** 计划年份 */
    @Excel(name = "计划年份", width = 15)
    @Schema(description = "计划年份")
    private String planYear;

    /** 油菜品种ID */
    @Excel(name = "油菜品种ID", width = 15)
    @Schema(description = "油菜品种ID")
    private String varietyId;

    /** 目标产量(公斤/亩) */
    @Excel(name = "目标产量(公斤/亩)", width = 15)
    @Schema(description = "目标产量(公斤/亩)")
    private BigDecimal targetYield;

    @Excel(name = "种植面积(亩)", width = 15)
    @Schema(description = "种植面积(亩)")
    @com.baomidou.mybatisplus.annotation.TableField("planted_area")
    private BigDecimal plantingArea;

    /** 计划播种日期 */
    @Excel(name = "计划播种日期", width = 15, format = "yyyy-MM-dd")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "计划播种日期")
    private Date plannedSowingDate;

    /** 计划收获日期 */
    @Excel(name = "计划收获日期", width = 15, format = "yyyy-MM-dd")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "计划收获日期")
    private Date plannedHarvestDate;

    /** 计划状态 */
    @Excel(name = "计划状态", width = 15)
    @Schema(description = "计划状态")
    private String status;

    /** 所属部门 */
    @Schema(description = "所属部门")
    private String sysOrgCode;

    /** 删除标志 */
    @Excel(name = "删除标志（0-正常，1-删除）", width = 15)
    @Schema(description = "删除标志（0-正常，1-删除）")
    @TableLogic
    private Integer delFlag;


    /* ==============================
       ⭐ 新增字段（肥料相关）
       ============================== */

    @Schema(description = "肥料安全系数")
    @Excel(name = "肥料安全系数", width = 15)
    private BigDecimal fertilizerSafetyCoeff;

    @Schema(description = "肥料组合 JSON")
    private String fertilizerCombination;

    @Schema(description = "肥料总氮投入")
    private BigDecimal fertilizerTotalN;

    @Schema(description = "肥料总磷投入")
    private BigDecimal fertilizerTotalP;

    @Schema(description = "肥料总钾投入")
    private BigDecimal fertilizerTotalK;

    @Schema(description = "养分需求快照 JSON")
    private String nutrientDemandSnapshot;


    /* ==============================
       ⭐ 新增字段（农药相关）
       ============================== */

    @Schema(description = "农药安全系数")
    private BigDecimal pesticideSafetyCoeff;

    @Schema(description = "农药组合 JSON")
    private String pesticideCombination;

    @Schema(description = "农药总投入")
    private BigDecimal pesticideTotal;

    @Schema(description = "农药备注")
    private String pesticideNote;


    /* ==============================
       ⭐ 新增字段（快照）
       ============================== */

    @Schema(description = "土壤肥力快照 JSON")
    private String soilFertilitySnapshot;

    @Schema(description = "种子参数快照 JSON")
    private String seedParamsSnapshot;

}
