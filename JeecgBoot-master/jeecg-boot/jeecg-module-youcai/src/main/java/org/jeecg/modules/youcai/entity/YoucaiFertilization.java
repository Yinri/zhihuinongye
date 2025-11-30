package org.jeecg.modules.youcai.entity;

import org.jeecg.common.system.base.entity.JeecgEntity;
import java.math.BigDecimal;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@TableName("youcai_fertilization")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="施肥管理表")
public class YoucaiFertilization extends JeecgEntity {
    private static final long serialVersionUID = 1L;


    @Schema(description = "基地ID")
    private String baseId;

    @Schema(description = "地块ID")
    private String plotId;

    @Schema(description = "地块名称")
    private String plotName;

    @Schema(description = "品种ID")
    private String varietyId;

    @Schema(description = "品种名称")
    private String varietyName;

    @Schema(description = "记录类型")
    private String recordType;

    @Schema(description = "施肥编号")
    private String fertilizationNo;

    @Schema(description = "施肥类型")
    private String fertilizationType;

    @Schema(description = "肥料名称")
    private String fertilizerName;

    @Schema(description = "施肥量(公斤/亩)")
    private BigDecimal fertilizerAmountKgPerMu;

    @Schema(description = "施肥面积(亩)")
    private BigDecimal fertilizationAreaMu;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "施肥日期")
    private Date fertilizationDate;

    @Schema(description = "施肥方式")
    private String fertilizationMethod;

    @Schema(description = "负责人")
    private String responsiblePerson;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "N百分比")
    private BigDecimal nPercent;

    @Schema(description = "P百分比")
    private BigDecimal pPercent;

    @Schema(description = "K百分比")
    private BigDecimal kPercent;

    @Schema(description = "目标产量(公斤/亩)")
    private BigDecimal targetYieldKgPerMu;

    @Schema(description = "N推荐用量(公斤/亩)")
    private BigDecimal nRecommendKgPerMu;

    @Schema(description = "P2O5推荐用量(公斤/亩)")
    private BigDecimal p2o5RecommendKgPerMu;

    @Schema(description = "K2O推荐用量(公斤/亩)")
    private BigDecimal k2oRecommendKgPerMu;

    @Schema(description = "推荐时间")
    private String recommendedTime;

    @Schema(description = "推荐方法")
    private String methodRecommend;

    @Schema(description = "推荐原因")
    private String reason;

    @Schema(description = "预测JSON")
    private String forecastJson;


    @Schema(description = "所属部门")
    private String sysOrgCode;

    @TableLogic
    @Schema(description = "删除标志")
    private Integer delFlag;
}
