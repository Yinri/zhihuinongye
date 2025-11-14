package org.jeecg.modules.youcai.mapper;

import org.jeecg.modules.youcai.entity.YoucaiHistoricalYield;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * @Description: 油菜历史产量表
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V1.0
 */
public interface YoucaiHistoricalYieldMapper extends BaseMapper<YoucaiHistoricalYield> {
    
    /**
     * 通过品种ID查询历年产量数据
     * @param varietyId 品种ID
     * @return 历年产量数据列表
     */
    List<YoucaiHistoricalYield> selectByVarietyId(@Param("varietyId") Integer varietyId);
}