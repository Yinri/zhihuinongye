package org.jeecg.modules.youcai.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@TableName("youcai_iot_devices")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="油菜-物联网设备台账")
public class YoucaiIotDevices implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String deviceCode;
    private String deviceName;
    private Integer sensorTypeId;
    private Integer plotId;
    private BigDecimal lat;
    private BigDecimal lng;
    private BigDecimal altitudeM;
    private BigDecimal windHeightM;
    private Integer status;
    private String createBy;
    private java.util.Date createTime;
    private String updateBy;
    private java.util.Date updateTime;
    private String sysOrgCode;
    private Integer delFlag;
}