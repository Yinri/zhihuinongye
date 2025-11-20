package org.jeecg.modules.youcai.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.youcai.dto.harvest.PlotHarvestSummaryDTO;

import java.util.List;

@Mapper
public interface PlotHarvestSummaryMapper {
    List<PlotHarvestSummaryDTO> selectAll();
    List<PlotHarvestSummaryDTO> selectByBaseId(@Param("baseId") Integer baseId);
}