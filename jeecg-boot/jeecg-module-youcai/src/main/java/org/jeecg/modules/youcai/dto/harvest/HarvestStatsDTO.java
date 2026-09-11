package org.jeecg.modules.youcai.dto.harvest;

import lombok.Data;

@Data
public class HarvestStatsDTO {
    private double harvestedArea;         // 已收割面积（亩）
    private double unharvestedArea;       // 未收割面积（亩）
    private double totalYield;            // 累计产量（吨）
    private int machineCount;             // 作业农机数（台）
    private double harvestedAreaTrend;    // 较昨日，百分比
    private double unharvestedAreaTrend;  // 较昨日，百分比
    private double totalYieldTrend;       // 较昨日，百分比
}