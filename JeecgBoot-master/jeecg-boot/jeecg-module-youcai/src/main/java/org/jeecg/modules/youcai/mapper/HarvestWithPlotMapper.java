package org.jeecg.modules.youcai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.youcai.dto.harvest.HarvestWithPlotDTO;
import org.jeecg.modules.youcai.entity.YoucaiHarvest;

/**
 * 收获管理Mapper，包含地块信息
 */
public interface HarvestWithPlotMapper extends BaseMapper<YoucaiHarvest> {
    
    /**
     * 分页查询收获记录，包含地块信息
     * @param page 分页参数
     * @param harvest 查询条件
     * @param plotName 地块名称查询条件
     * @return 包含地块信息的收获记录
     */
    IPage<HarvestWithPlotDTO> selectHarvestWithPlot(Page<HarvestWithPlotDTO> page, @Param("harvest") YoucaiHarvest harvest, @Param("plotName") String plotName);
}