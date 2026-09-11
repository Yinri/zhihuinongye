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

@Data
@TableName("youcai_agricultural_machine")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "农机设备信息表")
public class YoucaiAgriculturalMachine extends JeecgEntity {
    private static final long serialVersionUID = 1L;

    @Excel(name = "北斗设备编码", width = 15)
    @Schema(description = "北斗设备编码")
    private String beidouSn;

    @Excel(name = "车辆编号", width = 15)
    @Schema(description = "车辆编号")
    private String vehicleNumber;

    @Excel(name = "农机品牌", width = 15)
    @Schema(description = "农机品牌")
    private String brand;

    @Excel(name = "农机型号", width = 15)
    @Schema(description = "农机型号")
    private String model;

    @Excel(name = "机主姓名", width = 15)
    @Schema(description = "机主姓名")
    private String ownerName;

    @Excel(name = "机主电话", width = 15)
    @Schema(description = "机主电话")
    private String ownerPhone;

    @Excel(name = "基地名", width = 15)
    @Schema(description = "基地名")
    private String baseName;
}
