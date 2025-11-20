package org.jeecg.modules.youcai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.youcai.entity.YoucaiHarvest;

/**
 * 收获管理 Service 接口
 */
public interface IYoucaiHarvestService extends IService<YoucaiHarvest> {
    /**
     * 获取收获统计数据
     */
    org.jeecg.modules.youcai.dto.harvest.HarvestStatsDTO getHarvestStats(Integer baseId);

    /**
     * 获取农机作业状态
     */
    java.util.List<org.jeecg.modules.youcai.dto.harvest.HarvesterStatusDTO> getHarvesterStatus(Integer baseId);

    /**
     * 获取今日产量对比图数据（按地块）
     */
    java.util.List<org.jeecg.modules.youcai.dto.harvest.YieldChartBarDTO> getYieldChart(Integer baseId);

    /**
     * 获取地块收割派生视图汇总（用于地图着色与状态）
     */
    java.util.List<org.jeecg.modules.youcai.dto.harvest.PlotHarvestSummaryDTO> getPlotHarvestSummary(Integer baseId);
}