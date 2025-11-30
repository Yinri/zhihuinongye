package org.jeecg.modules.youcai.service;

import java.util.List;
import java.util.Map;
//zrt 灌溉监控服务接口
public interface IIrrigationService {
    Map<String, Object> getPlotStatus(String plotId);
    Map<String, Object> getPenmanPredict(String plotId);
    Map<String, Object> getInterventionComparison(String plotId);
    Map<String, Object> getPlotStatusByBase(String baseId);
    Map<String, Object> getPenmanPredictByBase(String baseId);
    Map<String, Object> getInterventionComparisonByBase(String baseId);
}
