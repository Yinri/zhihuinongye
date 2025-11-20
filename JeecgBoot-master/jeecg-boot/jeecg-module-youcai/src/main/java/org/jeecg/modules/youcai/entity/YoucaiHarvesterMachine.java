package org.jeecg.modules.youcai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("youcai_harvester_machine")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "农机档案")
public class YoucaiHarvesterMachine implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Excel(name = "基地ID", width = 15)
    @Schema(description = "基地ID")
    private Integer baseId;

    @Excel(name = "农机名称", width = 20)
    @Schema(description = "农机名称")
    private String machineName;

    @Excel(name = "品牌", width = 15)
    @Schema(description = "品牌")
    private String brand;

    @Excel(name = "型号", width = 15)
    @Schema(description = "型号")
    private String model;

    @Excel(name = "状态", width = 12)
    @Schema(description = "状态：working|idle|maintenance")
    private String status;

    @Excel(name = "最后位置", width = 30)
    @Schema(description = "位置描述或经纬度JSON")
    private String lastLocation;

    @Excel(name = "备注", width = 30)
    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建人")
    private String createBy;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新人")
    private String updateBy;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private Date updateTime;

    @TableLogic
    @Schema(description = "删除标志（0-正常，1-删除）")
    private Integer delFlag;
}