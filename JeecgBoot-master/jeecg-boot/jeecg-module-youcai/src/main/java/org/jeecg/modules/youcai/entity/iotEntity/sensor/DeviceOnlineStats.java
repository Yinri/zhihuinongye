package org.jeecg.modules.youcai.entity.iotEntity.sensor;

import lombok.Data;

import java.util.List;

/**
 * 设备在线统计模型
 */
@Data
public class DeviceOnlineStats {
    private List<DeviceStats> spInfo; // 视频设备
    private List<DeviceStats> qxInfo; // 气象设备
    private List<DeviceStats> trInfo; // 土壤设备
    private List<DeviceStats> szInfo; // 水质设备
    private List<DeviceStats> gpInfo; // 光谱设备
    private List<DeviceStats> cqInfo; // 虫情设备
    private List<DeviceStats> bzInfo; // 孢子设备
    private List<DeviceStats> scdInfo; // 杀虫灯设备
    private List<DeviceStats> dzInfo; // 多轴设备

    /**
     * 单类设备在线统计
     */
    @Data
    public static class DeviceStats {
        private Integer state; // 在线数量
        private Integer count; // 设备总数
    }
}