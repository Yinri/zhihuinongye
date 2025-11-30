package org.jeecg.modules.youcai.entity;

import org.jeecg.common.system.base.entity.JeecgEntity;
import java.math.BigDecimal;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
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
    @TableField("device_code")
    private String deviceCode;
    @TableField("plot_id")
    private String plotId;
    @TableField("base_id")
    private String baseId;
    @TableField("hour_ts")
    private Date hourTs;
    @TableField("air_temp_c")
    private BigDecimal airTempC;
    @TableField("rel_humidity_pct")
    private BigDecimal relHumidityPct;
    @TableField("wind_speed_ms")
    private BigDecimal windSpeedMs;
    @TableField("air_pressure_kpa")
    private BigDecimal airPressureKpa;
    @TableField("solar_radiation_wm2")
    private BigDecimal solarRadiationWm2;
    @TableField("precip_mm")
    private BigDecimal precipMm;
    @TableField("soil_moisture_pct")
    private BigDecimal soilMoisturePct;
    @TableField("dew_temp_c")
    private BigDecimal dewTempC;
    @TableField("vpd_kpa")
    private BigDecimal vpdKpa;
    @TableField("sys_org_code")
    private String sysOrgCode;
    @TableField("del_flag")
    private Integer delFlag;
}
