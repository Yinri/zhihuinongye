package org.jeecg.modules.youcai.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: variety_plot_utilization
 * @Author: jeecg-boot
 * @Date:   2025-12-04
 * @Version: V1.0
 */
@Data
@TableName("variety_plot_utilization")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="variety_plot_utilization")
public class VarietyPlotUtilization implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键ID（Jeecg通用32位UUID）*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID（Jeecg通用32位UUID）")
    private java.lang.String id;
	/**品种ID（关联作物品种表主键）*/
	@Excel(name = "品种ID（关联作物品种表主键）", width = 15)
    @Schema(description = "品种ID（关联作物品种表主键）")
    private java.lang.String varietyId;
	/**地块ID（关联地块表主键）*/
	@Excel(name = "地块ID（关联地块表主键）", width = 15)
    @Schema(description = "地块ID（关联地块表主键）")
    private java.lang.String plotId;
	/**肥料ID（关联肥料表主键）*/
	@Excel(name = "肥料ID（关联肥料表主键）", width = 15)
    @Schema(description = "肥料ID（关联肥料表主键）")
    private java.lang.String fertilizerId;
	/**肥料类型（冗余字段：如尿素、磷酸二铵）*/
	@Excel(name = "肥料类型（冗余字段：如尿素、磷酸二铵）", width = 15)
    @Schema(description = "肥料类型（冗余字段：如尿素、磷酸二铵）")
    private java.lang.String fertilizerType;
	/**氮肥利用率(%)*/
	@Excel(name = "氮肥利用率(%)", width = 15)
    @Schema(description = "氮肥利用率(%)")
    private java.math.BigDecimal nRate;
	/**磷肥利用率(%)*/
	@Excel(name = "磷肥利用率(%)", width = 15)
    @Schema(description = "磷肥利用率(%)")
    private java.math.BigDecimal pRate;
	/**钾肥利用率(%)*/
	@Excel(name = "钾肥利用率(%)", width = 15)
    @Schema(description = "钾肥利用率(%)")
    private java.math.BigDecimal kRate;
	/**创建人（Jeecg通用字段）*/
    @Schema(description = "创建人（Jeecg通用字段）")
    private java.lang.String createBy;
	/**创建时间（Jeecg通用字段）*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间（Jeecg通用字段）")
    private java.util.Date createTime;
	/**修改人（Jeecg通用字段）*/
    @Schema(description = "修改人（Jeecg通用字段）")
    private java.lang.String updateBy;
	/**修改时间（Jeecg通用字段）*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "修改时间（Jeecg通用字段）")
    private java.util.Date updateTime;
	/**所属机构编码（Jeecg多租户字段）*/
    @Schema(description = "所属机构编码（Jeecg多租户字段）")
    private java.lang.String sysOrgCode;
}
