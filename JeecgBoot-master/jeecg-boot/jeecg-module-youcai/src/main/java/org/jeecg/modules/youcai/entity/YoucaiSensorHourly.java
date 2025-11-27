package org.jeecg.modules.youcai.entity;

import org.jeecg.common.system.base.entity.JeecgEntity;
import java.math.BigDecimal;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@TableName("youcai_sensor_hourly")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="油菜-传感器小时级数据")
public class YoucaiSensorHourly extends JeecgEntity {
    private String deviceCode;
    private String plotId;
    private Date hourTs;
    private BigDecimal airTempC;
    private BigDecimal relHumidityPct;
    private BigDecimal windSpeedMs;
    private BigDecimal airPressureKpa;
    private BigDecimal solarRadiationWm2;
    private BigDecimal precipMm;
    private BigDecimal soilMoisturePct;
    private BigDecimal dewTempC;
    private BigDecimal vpdKpa;
    
    private String sysOrgCode;
    private Integer delFlag;
}
