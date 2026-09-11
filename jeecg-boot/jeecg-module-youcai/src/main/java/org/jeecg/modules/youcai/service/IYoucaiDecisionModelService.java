package org.jeecg.modules.youcai.service;

import org.jeecg.modules.youcai.dto.decision.YoucaiDecisionGrowthDTO;
import org.jeecg.modules.youcai.dto.decision.YoucaiDecisionHeightRiskDTO;
import org.jeecg.modules.youcai.dto.decision.YoucaiDecisionInsectTrendSuggestDTO;
import org.jeecg.modules.youcai.dto.decision.YoucaiDecisionLodgingDTO;
import org.jeecg.modules.youcai.dto.decision.YoucaiDecisionPestControlDTO;
import org.jeecg.modules.youcai.dto.decision.YoucaiDecisionPestDTO;
import org.jeecg.modules.youcai.dto.decision.YoucaiDecisionSoilDTO;
import org.jeecg.modules.youcai.dto.decision.YoucaiDecisionYieldDTO;

import java.util.List;

/**
 * 油菜决策模型接口服务
 */
public interface IYoucaiDecisionModelService {

    /**
     * 接口1：基地今日平均亩产预测
     */
    List<YoucaiDecisionYieldDTO> getInterface1Data();

    /**
     * 接口2：基地最近7天害虫Top3
     */
    List<YoucaiDecisionPestDTO> getInterface2Data();

    /**
     * 接口3：基地今日土壤指标
     */
    List<YoucaiDecisionSoilDTO> getInterface3Data();

    /**
     * 接口4：基地今日倒伏预警数据
     */
    List<YoucaiDecisionLodgingDTO> getInterface4Data();

    /**
     * 接口5：基地今日长势指标数据
     */
    List<YoucaiDecisionGrowthDTO> getInterface5Data();

    /**
     * 接口6：基地近15天植株高度和倒伏概率
     */
    List<YoucaiDecisionHeightRiskDTO> getInterface6Data();

    /**
     * 接口7：根据害虫名称获取防治建议
     * @param pestNames 害虫名称，多个用逗号拼接
     */
    List<YoucaiDecisionPestControlDTO> getPestControlSuggestions(String pestNames);

    /**
     * 查询基地今日虫情预警与防治数据
     */
    List<YoucaiDecisionInsectTrendSuggestDTO> getInsectTrendSuggest();

    /**
     * 查询单个基地今日虫情预警与防治数据
     */
    YoucaiDecisionInsectTrendSuggestDTO getInsectTrendSuggestByBase(String baseId, String baseName);

    /**
     * 刷新并缓存基地今日虫情预警与防治数据
     */
    List<YoucaiDecisionInsectTrendSuggestDTO> refreshInsectTrendSuggest();
}
