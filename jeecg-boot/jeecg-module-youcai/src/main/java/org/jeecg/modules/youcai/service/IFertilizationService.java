package org.jeecg.modules.youcai.service;

import java.util.Map;

/**
 * 施肥管理业务服务（按基地切换展示）
 */
public interface IFertilizationService {
    Map<String, Object> getPlotStatusByBase(String baseId);
    Map<String, Object> getBaseSoilSeries(String baseId);
    Map<String, Object> getBaseRecommendation(String baseId);
}

