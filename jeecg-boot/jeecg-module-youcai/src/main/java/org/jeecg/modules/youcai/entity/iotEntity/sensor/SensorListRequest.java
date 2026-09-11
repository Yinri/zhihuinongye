package org.jeecg.modules.youcai.entity.iotEntity.sensor;

import lombok.Data;

/**
 * 获取传感器列表请求参数
 */
@Data
public class SensorListRequest {
    private Integer projectId; // 项目ID（必选）
    private Integer sensorTypeId; // 传感器类型：1=气象，2=土壤
}