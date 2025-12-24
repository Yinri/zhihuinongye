package org.jeecg.modules.youcai.entity;

import org.jeecg.common.system.base.entity.JeecgEntity;
import java.util.Date;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@TableName("youcai_soil_fertility")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="土壤肥力表")
public class YoucaiSoilFertility extends JeecgEntity {
    private static final long serialVersionUID = 1L;

    @Schema(description = "地块ID")
    private String plotId;

    @Schema(description = "基地ID")
    private String baseId;

    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "检测日期")
    private Date testDate;

    @Schema(description = "pH值")
    private BigDecimal phValue;

    @Schema(description = "有机质含量(g/kg)")
    private BigDecimal organicMatter;

    @Schema(description = "氮含量(mg/kg)")
    private BigDecimal nitrogen;

    @Schema(description = "磷含量(mg/kg)")
    private BigDecimal phosphorus;

    @Schema(description = "钾含量(mg/kg)")
    private BigDecimal potassium;

    @Schema(description = "肥力等级")
    private String fertilityLevel;

    @Schema(description = "所属部门")
    private String sysOrgCode;

    @TableLogic
    @Schema(description = "删除标志（0-正常，1-删除）")
    private Integer delFlag;
}

