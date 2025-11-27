package org.jeecg.modules.youcai.entity;

import org.jeecg.common.system.base.entity.JeecgEntity;
import java.math.BigDecimal;
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
public class YoucaiIotDevices extends JeecgEntity {
    private String deviceCode;
    private String deviceName;
    private Integer sensorTypeId;
    private String plotId;
    private BigDecimal lat;
    private BigDecimal lng;
    private BigDecimal altitudeM;
    private BigDecimal windHeightM;
    private Integer status;
    
    private String sysOrgCode;
    private Integer delFlag;
}
