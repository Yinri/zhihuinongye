package org.jeecg.modules.youcai.entity.iotEntity.sensor;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 传感器信息模型
 */
@Data
public class SensorInfo {
    private Sensor q; // 传感器设备信息
    private String traName; // 协议名称
    private Integer id; // 协议ID

    /**
     * 传感器设备信息
     */
    @Data
    public static class Sensor {
        private Integer id; // 传感器ID
        private String sensorName; // 传感器名称
        private String sensorSerial; // 传感器编号（DeviceCode，后续接口需用）
        private Integer sensorTypeId; // 传感器类型：1=气象，2=土壤，4=水质
        private String sensorDataTypeIds; // 检测数据类型ID集合
        private String sensorDataTypeNames; // 检测数据类型名称
        private Integer nums; // 检测指标数量
        private Integer time; // 数据采集间隔（单位：分钟）
        private Integer state; // 在线状态：1=在线，0=离线
        private String lat; // 经度
        private String lng; // 纬度
        private LocalDateTime dateCreated; // 创建时间
        private Integer isDelete; // 是否删除：0=未删除
    }
}