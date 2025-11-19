package org.jeecg.modules.youcai.util;

import org.jeecg.modules.youcai.entity.YoucaiFertilization;
import org.jeecg.modules.youcai.entity.YoucaiSoilFertility;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * QUEFTS 施肥推荐算法工具类
 * 核心逻辑：基于目标产量、土壤供肥能力、作物养分需求计算推荐施肥量
 */
public class QueftsAlgorithm {

    // 油菜养分需求参数（kg/100kg 产量）- 可根据实际品种调整
    private static final BigDecimal N_REQUIRE = new BigDecimal("5.8"); // 每100kg产量需N
    private static final BigDecimal P2O5_REQUIRE = new BigDecimal("2.2"); // 每100kg产量需P2O5
    private static final BigDecimal K2O_REQUIRE = new BigDecimal("4.5"); // 每100kg产量需K2O

    // 土壤养分利用效率（%）- 根据土壤测试值调整
    private static final BigDecimal N_UTILIZATION_RATE = new BigDecimal("60"); // 氮利用效率
    private static final BigDecimal P_UTILIZATION_RATE = new BigDecimal("50"); // 磷利用效率
    private static final BigDecimal K_UTILIZATION_RATE = new BigDecimal("55"); // 钾利用效率

    /**
     * 计算 QUEFTS 推荐施肥量
     * @param targetYield 目标产量（kg/亩）
     * @param soilFertility 土壤肥力数据
     * @return 施肥推荐结果
     */
    public static YoucaiFertilization calculateRecommendation(BigDecimal targetYield, YoucaiSoilFertility soilFertility) {
        YoucaiFertilization recommendation = new YoucaiFertilization();

        // 1. 计算作物总养分需求（kg/亩）
        BigDecimal totalNNeed = targetYield.multiply(N_REQUIRE).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal totalP2O5Need = targetYield.multiply(P2O5_REQUIRE).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal totalK2ONeed = targetYield.multiply(K2O_REQUIRE).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        // 2. 计算土壤供肥量（kg/亩）- 基于土壤测试值（mg/kg）转换
        BigDecimal soilN = soilFertility.getNitrogen().multiply(new BigDecimal("0.15")).setScale(2, RoundingMode.HALF_UP); // 土壤氮转换系数
        BigDecimal soilP = soilFertility.getPhosphorus().multiply(new BigDecimal("0.12")).setScale(2, RoundingMode.HALF_UP); // 土壤磷转换系数
        BigDecimal soilK = soilFertility.getPotassium().multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP); // 土壤钾转换系数

        // 3. 计算推荐施肥量 =（总需求 - 土壤供肥）/ 肥料利用效率
        BigDecimal recommendN = totalNNeed.subtract(soilN)
                .divide(N_UTILIZATION_RATE.divide(new BigDecimal("100")), 2, RoundingMode.HALF_UP);
        BigDecimal recommendP2O5 = totalP2O5Need.subtract(soilP)
                .divide(P_UTILIZATION_RATE.divide(new BigDecimal("100")), 2, RoundingMode.HALF_UP);
        BigDecimal recommendK2O = totalK2ONeed.subtract(soilK)
                .divide(K_UTILIZATION_RATE.divide(new BigDecimal("100")), 2, RoundingMode.HALF_UP);

        // 4. 确保推荐量不为负数
        recommendation.setNRecommendKgPerMu(recommendN.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : recommendN);
        recommendation.setP2o5RecommendKgPerMu(recommendP2O5.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : recommendP2O5);
        recommendation.setK2oRecommendKgPerMu(recommendK2O.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : recommendK2O);

        // 5. 补充推荐信息
        recommendation.setRecommendedTime(calculateBestTime(soilFertility));
        recommendation.setMethodRecommend(calculateBestMethod(soilFertility));
        recommendation.setReason(buildRecommendationReason(targetYield, recommendN, recommendP2O5, recommendK2O));

        return recommendation;
    }

    /**
     * 计算最佳施肥时间
     */
    private static String calculateBestTime(YoucaiSoilFertility soilFertility) {
        // 简单逻辑：根据土壤湿度和近期天气（这里用土壤氮含量间接判断）
        if (soilFertility.getNitrogen().compareTo(new BigDecimal("40")) < 0) {
            return "立即施肥（土壤氮含量偏低）";
        } else {
            return "未来3-5天晴好天气施肥";
        }
    }

    /**
     * 计算最佳施肥方式
     */
    private static String calculateBestMethod(YoucaiSoilFertility soilFertility) {
        // 简单逻辑：根据土壤磷钾含量判断
        if (soilFertility.getPhosphorus().compareTo(new BigDecimal("30")) < 0) {
            return "条施（集中供磷）";
        } else if (soilFertility.getPotassium().compareTo(new BigDecimal("50")) < 0) {
            return "滴灌施肥（均匀供钾）";
        } else {
            return "撒施（常规施肥）";
        }
    }

    /**
     * 构建推荐理由
     */
    private static String buildRecommendationReason(BigDecimal targetYield, BigDecimal n, BigDecimal p2o5, BigDecimal k2o) {
        return String.format("目标产量%.1fkg/亩，根据QUEFTS算法计算：" +
                        "土壤供氮量不足，需补充氮肥%.1fkg/亩；" +
                        "土壤供磷量基本满足，需补充磷肥%.1fkg/亩；" +
                        "土壤供钾量不足，需补充钾肥%.1fkg/亩。" +
                        "建议按推荐量施肥，提高肥料利用率。",
                targetYield, n, p2o5, k2o);
    }
}