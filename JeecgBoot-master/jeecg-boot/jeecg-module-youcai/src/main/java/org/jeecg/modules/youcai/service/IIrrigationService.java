package org.jeecg.modules.youcai.service;

import java.util.List;
import java.util.Map;

public interface IIrrigationService {
    Map<String, Object> getPlotStatus(Integer plotId);
    Map<String, Object> getPenmanPredict(Integer plotId);
    Map<String, Object> getInterventionComparison(Integer plotId);
}