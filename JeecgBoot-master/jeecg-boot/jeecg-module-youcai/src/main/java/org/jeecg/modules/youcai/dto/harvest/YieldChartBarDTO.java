package org.jeecg.modules.youcai.dto.harvest;

import lombok.Data;

@Data
public class YieldChartBarDTO {
    private String fieldName; // 地块名称
    private double actual;    // 实际产量（吨）
    private double expected;  // 预计产量（吨）
}