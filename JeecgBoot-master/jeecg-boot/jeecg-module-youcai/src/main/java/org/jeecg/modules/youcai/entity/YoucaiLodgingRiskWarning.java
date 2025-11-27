package org.jeecg.modules.youcai.entity;

import org.jeecg.common.system.base.entity.JeecgEntity;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
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
 * @Description: 倒伏风险预警表
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V1.0
 */
@Data
@TableName("youcai_lodging_risk_warning")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="倒伏风险预警表")
public class YoucaiLodgingRiskWarning extends JeecgEntity {
    private static final long serialVersionUID = 1L;

	/**地块ID，外键关联youcai_plots表*/
	@Excel(name = "地块ID，外键关联youcai_plots表", width = 15)
    @Schema(description = "地块ID，外键关联youcai_plots表")
    private java.lang.String plotId;
	/**风险等级：低 / 中 / 高*/
	@Excel(name = "风险等级：低 / 中 / 高", width = 15)
    @Schema(description = "风险等级：低 / 中 / 高")
    private java.lang.String riskLevel;
	/**倒伏风险得分，0-1之间的小数*/
	@Excel(name = "倒伏风险得分，0-1之间的小数", width = 15)
    @Schema(description = "倒伏风险得分，0-1之间的小数")
    private java.math.BigDecimal riskScore;
	/**建议措施，如"及时排水、加固植株"*/
	@Excel(name = "建议措施, width = 15")
    @Schema(description = "建议措施")
    private java.lang.String suggestedMeasures;
	/**预警状态*/
	@Excel(name = "预警状态", width = 15)
    @Schema(description = "预警状态")
    private java.lang.String warningStatus;
	/**所属部门*/
    @Schema(description = "所属部门")
    private java.lang.String sysOrgCode;
	/**删除标志（0-正常，1-删除）*/
	@Excel(name = "删除标志（0-正常，1-删除）", width = 15)
    @Schema(description = "删除标志（0-正常，1-删除）")
    @TableLogic
    private java.lang.Integer delFlag;
}
