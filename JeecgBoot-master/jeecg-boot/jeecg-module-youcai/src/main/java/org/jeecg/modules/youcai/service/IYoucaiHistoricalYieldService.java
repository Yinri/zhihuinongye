package org.jeecg.modules.youcai.service;

import org.jeecg.modules.youcai.entity.YoucaiHistoricalYield;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * @Description: 油菜历史产量表
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V1.0
 */
public interface IYoucaiHistoricalYieldService extends IService<YoucaiHistoricalYield> {
    
    /**
     * 通过品种ID查询历年产量数据
     * @param varietyId 品种ID
     * @return 历年产量数据列表
     */
    List<YoucaiHistoricalYield> getYieldByVarietyId(Integer varietyId);
}