package org.jeecg.modules.youcai.service.impl;

import org.jeecg.modules.youcai.entity.YoucaiHistoricalYield;
import org.jeecg.modules.youcai.mapper.YoucaiHistoricalYieldMapper;
import org.jeecg.modules.youcai.service.IYoucaiHistoricalYieldService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.List;

/**
 * @Description: 油菜历史产量表
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V1.0
 */
@Service
public class YoucaiHistoricalYieldServiceImpl extends ServiceImpl<YoucaiHistoricalYieldMapper, YoucaiHistoricalYield> implements IYoucaiHistoricalYieldService {

    @Override
    public List<YoucaiHistoricalYield> getYieldByVarietyId(Integer varietyId) {
        // 使用MyBatis-Plus的查询方法
        QueryWrapper<YoucaiHistoricalYield> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("variety_id", varietyId);
        queryWrapper.orderByAsc("year");
        return this.list(queryWrapper);
    }
}