package org.jeecg.modules.youcai.entity;

import org.jeecg.common.system.base.entity.JeecgEntity;
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
public class YoucaiHarvesterMachine extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;


    @Excel(name = "基地ID", width = 15)
    @Schema(description = "基地ID")
    private String baseId;

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
    @Schema(description = "状态：空闲|作业中|维修中")
    private String status;

    @Excel(name = "最后位置", width = 30)
    @Schema(description = "位置描述或经纬度JSON")
    private String lastLocation;

    @Excel(name = "备注", width = 30)
    @Schema(description = "备注")
    private String remark;


    @TableLogic
    @Schema(description = "删除标志（0-正常，1-删除）")
    private Integer delFlag;
}
