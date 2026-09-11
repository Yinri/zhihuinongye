package org.jeecg.modules.youcai.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 气象传感器数据DTO
 */
@Data
@Accessors(chain = true)
public class WeatherSensorDataDTO {
    
    /**
     * 温度 (°C)
     */
    private Float temperature;
    
    /**
     * 湿度 (%)
     */
    private Float humidity;
    
    /**
     * 气压 (hPa)
     */
    private Float airPressure;
    
    /**
     * 风速 (m/s)
     */
    private Float windSpeed;
    
    /**
     * 风向 (度)
     */
    private Float windDirection;
    
    /**
     * 光照强度 (Lux)
     */
    private Float lightIntensity;
    
    /**
     * 降雨量 (mm)
     */
    private Float rainfall;
    
    /**
     * 二氧化碳浓度 (ppm)
     */
    private Float co2Level;
    
    /**
     * 总辐射 (W/㎡)
     */
    private Float totalRadiation;
    
    /**
     * 数据更新时间
     */
    private String updateTime;
    
    /**
     * 传感器ID
     */
    private String sensorId;
    
    /**
     * 传感器名称
     */
    private String sensorName;
}