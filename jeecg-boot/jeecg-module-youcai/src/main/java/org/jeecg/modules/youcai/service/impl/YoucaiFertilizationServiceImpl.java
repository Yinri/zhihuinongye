package org.jeecg.modules.youcai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.youcai.entity.YoucaiFertilization;
import org.jeecg.modules.youcai.entity.YoucaiSoilFertility;
import org.jeecg.modules.youcai.mapper.YoucaiFertilizationMapper;
import org.jeecg.modules.youcai.service.IYoucaiFertilizationService;
import org.jeecg.modules.youcai.service.IYoucaiSoilFertilityService;
import org.jeecg.modules.youcai.util.QueftsAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

/**
 * 施肥管理 Service 实现类（集成 QUEFTS 算法）
 */
@Service
public class YoucaiFertilizationServiceImpl extends ServiceImpl<YoucaiFertilizationMapper, YoucaiFertilization> implements IYoucaiFertilizationService {

    @Autowired
    private IYoucaiSoilFertilityService soilFertilityService;

    /**
     * 重写 save 方法：保存施肥记录时，自动调用 QUEFTS 算法计算推荐值
     */
    @Override
    public boolean save(YoucaiFertilization entity) {
        // 1. 校验地块ID是否存在
        if (entity.getPlotId() == null) {
            throw new RuntimeException("地块ID不能为空，无法计算施肥推荐");
        }

        // 2. 获取该地块最新的土壤肥力数据（用于算法计算）
        YoucaiSoilFertility latestSoil = soilFertilityService.getOne(
            new LambdaQueryWrapper<YoucaiSoilFertility>()
                .eq(YoucaiSoilFertility::getPlotId, entity.getPlotId())
                .orderByDesc(YoucaiSoilFertility::getTestDate)
                .last("LIMIT 1")
        );
        if (latestSoil == null) {
            throw new RuntimeException("未查询到该地块的土壤肥力测试数据，请先录入土壤数据");
        }

        // 3. 处理目标产量（默认180kg/亩，用户已设置则用用户值）
        BigDecimal targetYield = entity.getTargetYieldKgPerMu();
        if (targetYield == null || targetYield.compareTo(BigDecimal.ZERO) <= 0) {
            targetYield = new BigDecimal("180"); // 油菜常规目标产量
            entity.setTargetYieldKgPerMu(targetYield); // 回写默认值到实体
        }

        // 4. 调用 QUEFTS 算法计算施肥推荐值（N/P2O5/K2O 推荐量、时间、方式等）
        YoucaiFertilization recommendation = QueftsAlgorithm.calculateRecommendation(targetYield, latestSoil);
        
        // 5. 将算法结果赋值到实体（最终存入数据库）
        entity.setNRecommendKgPerMu(recommendation.getNRecommendKgPerMu());
        entity.setP2o5RecommendKgPerMu(recommendation.getP2o5RecommendKgPerMu());
        entity.setK2oRecommendKgPerMu(recommendation.getK2oRecommendKgPerMu());
        entity.setRecommendedTime(recommendation.getRecommendedTime());
        entity.setMethodRecommend(recommendation.getMethodRecommend());
        entity.setReason(recommendation.getReason());

        // 6. 调用父类方法保存数据到数据库
        return super.save(entity);
    }
}