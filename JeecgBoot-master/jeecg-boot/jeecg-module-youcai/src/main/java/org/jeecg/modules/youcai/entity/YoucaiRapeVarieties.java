package org.jeecg.modules.youcai.entity;

import org.jeecg.common.system.base.entity.JeecgEntity;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.*;
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
 * @Description: 油菜品种表
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V1.0
 */
@Data
@TableName("youcai_rape_varieties")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="油菜品种表")
public class YoucaiRapeVarieties extends JeecgEntity {
    private static final long serialVersionUID = 1L;

	/**品种名称*/
	@Excel(name = "品种名称", width = 15)
    @Schema(description = "品种名称")
    private java.lang.String varietyName;
	/**品种特性*/
	@Excel(name = "品种特性", width = 15)
    @Schema(description = "品种特性")
    private java.lang.String characteristics;
	/**生长周期(天)*/
	@Excel(name = "生长周期(天)", width = 15)
    @Schema(description = "生长周期(天)")
    private java.lang.Integer growthCycle;
	/**产量潜力(公斤/亩)*/
	@Excel(name = "产量潜力(公斤/亩)", width = 15)
    @Schema(description = "产量潜力(公斤/亩)")
    private java.math.BigDecimal yieldPotential;
	/**抗病性*/
	@Excel(name = "抗病性", width = 15)
    @Schema(description = "抗病性")
    private java.lang.String diseaseResistance;
	/**所属部门*/
    @Schema(description = "所属部门")
    private java.lang.String sysOrgCode;
	/**删除标志（0-正常，1-删除）*/
	@Excel(name = "删除标志（0-正常，1-删除）", width = 15)
    @Schema(description = "删除标志（0-正常，1-删除）")
    @TableLogic
    private java.lang.Integer delFlag;


    // 新增：种子计算核心参数（7个）
    /**收获系数（0.3-0.5）*/
    @Excel(name = "收获系数", width = 15)
    @Schema(description = "收获系数（0.3-0.5）")
    @TableField("harvest_coefficient")
    private java.math.BigDecimal harvestCoefficient;

    /**田间保苗率（%，50-100）*/
    @Excel(name = "田间保苗率(%)", width = 15)
    @Schema(description = "田间保苗率（%，50-100）")
    @TableField("seedling_survival_rate")
    private java.lang.Integer seedlingSurvivalRate;

    /**结实率（%，70-95）*/
    @Excel(name = "结实率(%)", width = 15)
    @Schema(description = "结实率（%，70-95）")
    @TableField("seed_setting_rate")
    private java.lang.Integer seedSettingRate;

    /**单株有效角果数（个，200-400）*/
    @Excel(name = "单株有效角果数(个)", width = 15)
    @Schema(description = "单株有效角果数（个，200-400）")
    @TableField("single_plant_pods")
    private java.lang.Integer singlePlantPods;

    /**每角果粒数（粒，15-25）*/
    @Excel(name = "每角果粒数(粒)", width = 15)
    @Schema(description = "每角果粒数（粒，15-25）")
    @TableField("seeds_per_pod")
    private java.lang.Integer seedsPerPod;

    /**千粒重（g，2.5-4.0）*/
    @Excel(name = "千粒重(g)", width = 15)
    @Schema(description = "千粒重（g，2.5-4.0）")
    @TableField("thousand_grain_weight")
    private java.math.BigDecimal thousandGrainWeight;

    /**发芽率（%，85-95）*/
    @Excel(name = "发芽率(%)", width = 15)
    @Schema(description = "发芽率（%，85-95）")
    @TableField("germination_rate")
    private java.lang.Integer germinationRate;

}
