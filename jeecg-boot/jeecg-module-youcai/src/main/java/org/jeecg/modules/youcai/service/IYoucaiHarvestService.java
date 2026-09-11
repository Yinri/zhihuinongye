package org.jeecg.modules.youcai.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.youcai.dto.harvest.HarvestWithPlotDTO;
import org.jeecg.modules.youcai.entity.YoucaiHarvest;

/**
 * 收获管理 Service 接口
 */
public interface IYoucaiHarvestService extends IService<YoucaiHarvest> {
    /**
     * 获取收获统计数据
     */
    org.jeecg.modules.youcai.dto.harvest.HarvestStatsDTO getHarvestStats(String baseId);

    /**
     * 获取农机作业状态
     */
    java.util.List<org.jeecg.modules.youcai.dto.harvest.HarvesterStatusDTO> getHarvesterStatus(String baseId);

    /**
     * 获取今日产量对比图数据（按地块）
     */
    java.util.List<org.jeecg.modules.youcai.dto.harvest.YieldChartBarDTO> getYieldChart(String baseId);

    /**
     * 获取地块收割派生视图汇总（用于地图着色与状态）
     */
    java.util.List<org.jeecg.modules.youcai.dto.harvest.PlotHarvestSummaryDTO> getPlotHarvestSummary(String baseId);
    
    /**
     * 分页查询收获记录，包含地块信息
     * @param page 分页参数
     * @param harvest 查询条件
     * @param plotName 地块名称查询条件
     * @return 包含地块信息的收获记录
     */
    IPage<HarvestWithPlotDTO> queryHarvestWithPlot(Page<HarvestWithPlotDTO> page, YoucaiHarvest harvest, String plotName);
}
