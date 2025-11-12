package org.jeecg.modules.youcai.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
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
 * @Description: 传感器信息表
 * @Author: jeecg-boot
 * @Date:   2025-11-10
 * @Version: V1.0
 */
@Data
@TableName("youcai_sensor_info")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="传感器信息表")
public class YoucaiSensorInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**主键*/
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Integer id;
    
    /**传感器ID（从API获取的原始ID）*/
    @Excel(name = "传感器ID", width = 15)
    @Schema(description = "传感器ID（从API获取的原始ID）")
    private Integer sensorId;
    
    /**传感器名称*/
    @Excel(name = "传感器名称", width = 15)
    @Schema(description = "传感器名称")
    private String sensorName;
    
    /**传感器编号（DeviceCode）*/
    @Excel(name = "传感器编号", width = 15)
    @Schema(description = "传感器编号（DeviceCode）")
    private String sensorSerial;
    
    /**传感器类型：1=气象，2=土壤，4=水质*/
    @Excel(name = "传感器类型", width = 15, dicCode = "sensor_type")
    @Schema(description = "传感器类型：1=气象，2=土壤，4=水质")
    private Integer sensorTypeId;
    
    /**检测数据类型ID集合*/
    @Excel(name = "检测数据类型ID集合", width = 15)
    @Schema(description = "检测数据类型ID集合")
    private String sensorDataTypeIds;
    
    /**检测数据类型名称*/
    @Excel(name = "检测数据类型名称", width = 30)
    @Schema(description = "检测数据类型名称")
    private String sensorDataTypeNames;
    
    /**检测指标数量*/
    @Excel(name = "检测指标数量", width = 15)
    @Schema(description = "检测指标数量")
    private Integer nums;
    
    /**数据采集间隔（单位：分钟）*/
    @Excel(name = "数据采集间隔", width = 15)
    @Schema(description = "数据采集间隔（单位：分钟）")
    private Integer time;
    
    /**在线状态：1=在线，0=离线*/
    @Excel(name = "在线状态", width = 15, dicCode = "online_status")
    @Schema(description = "在线状态：1=在线，0=离线")
    private Integer state;
    
    /**经度*/
    @Excel(name = "经度", width = 15)
    @Schema(description = "经度")
    private String longitude;
    
    /**纬度*/
    @Excel(name = "纬度", width = 15)
    @Schema(description = "纬度")
    private String latitude;
    
    /**协议名称*/
    @Excel(name = "协议名称", width = 15)
    @Schema(description = "协议名称")
    private String protocolName;

    /**是否删除：0=未删除，1=已删除*/
    @Excel(name = "是否删除", width = 15, dicCode = "yn")
    @Schema(description = "是否删除：0=未删除，1=已删除")
    private Integer isDelete;
    
    /**传感器创建时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Excel(name = "传感器创建时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "传感器创建时间")
    private LocalDateTime sensorCreateTime;
    
    /**数据同步时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Excel(name = "数据同步时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "数据同步时间")
    private LocalDateTime syncTime;
    
    /**所属项目ID*/
    @Excel(name = "所属项目ID", width = 15)
    @Schema(description = "所属项目ID")
    private Integer projectId;

    
    /**创建人*/
    @Schema(description = "创建人")
    private String createBy;
    
    /**创建日期*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建日期")
    private LocalDateTime createTime;
    
    /**更新人*/
    @Schema(description = "更新人")
    private String updateBy;
    
    /**更新日期*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新日期")
    private LocalDateTime updateTime;
    
    /**所属部门*/
    @Schema(description = "所属部门")
    private String sysOrgCode;
}