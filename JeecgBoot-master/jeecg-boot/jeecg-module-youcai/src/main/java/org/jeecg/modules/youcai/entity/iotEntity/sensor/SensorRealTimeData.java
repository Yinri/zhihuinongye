package org.jeecg.modules.youcai.entity.iotEntity.sensor;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 传感器实时数据模型
 */
@Data
public class SensorRealTimeData {
    private String name; // 监测指标名称（如：空气温度）
    private String unit; // 单位（如：℃）
    private Integer sensorDataTypeId; // 监测指标ID
    private Integer sensorTypeId; // 传感器类型ID
    private SensorData q; // 实时数据详情

    /**
     * 实时数据详情
     */
    @Data
    public static class SensorData {
        private Integer id; // 数据ID
        private Integer sensorId; // 传感器ID
        private Double sensorNum; // 数据值（如：24.8）
        private LocalDateTime collectime; // 数据采集时间
        private Integer sensorDataTypeId; // 监测指标ID
    }
}