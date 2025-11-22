package org.jeecg.modules.youcai.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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
 * @Description: 基地表
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V1.0
 */
@Data
@TableName("youcai_bases")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="基地表")
public class YoucaiBases implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
    @TableId(type = IdType.AUTO) // 对应数据库自增策略
    @Schema(description = "主键")
    private Integer id; // 类型改为 Integer，与数据库 int 匹配
	/**基地名称*/
	@Excel(name = "基地名称", width = 15)
    @Schema(description = "基地名称")
    private java.lang.String baseName;
	/**负责人*/
	@Excel(name = "负责人", width = 15)
    @Schema(description = "负责人")
    private java.lang.String manager;
	/**联系电话*/
	@Excel(name = "联系电话", width = 15)
    @Schema(description = "联系电话")
    private java.lang.String phone;
	/**基地地址（具体地址）*/
	@Excel(name = "基地地址（具体地址）", width = 15)
    @Schema(description = "基地地址（具体地址）")
    private java.lang.String address;

    /**纬度*/
    @Excel(name = "纬度", width = 15)
    @Schema(description = "纬度")
    private java.math.BigDecimal latitude;
    /**经度*/
    @Excel(name = "经度", width = 15)
    @Schema(description = "经度")
    private java.math.BigDecimal longitude;
    // ---------------------- 新增字段开始 ----------------------
    /**面积（亩）*/
    @Excel(name = "面积（亩）", width = 15)
    @Schema(description = "基地面积（亩）")
    private BigDecimal area;

    /**土壤状况*/
    @Excel(name = "土壤状况", width = 15, dicCode = "soil_type")
    @Schema(description = "土壤状况（黏土/沙土/壤土）")
    @Dict(dicCode = "soil_type")
    private String soilType;
    // ---------------------- 新增字段结束 ----------------------

    /**创建人*/
    @Schema(description = "创建人")
    private java.lang.String createBy;
	/**创建日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建日期")
    private java.util.Date createTime;
	/**更新人*/
    @Schema(description = "更新人")
    private java.lang.String updateBy;
	/**更新日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新日期")
    private java.util.Date updateTime;
	/**所属部门*/
    @Schema(description = "所属部门")
    private java.lang.String sysOrgCode;


}
