package org.jeecg.modules.youcai.service;

import org.jeecg.modules.youcai.dto.DiseaseAnalysisRequestDTO;
import org.jeecg.modules.youcai.dto.SeedlingAnalysisResponseDTO;

/**
 * 油菜苗情分析服务接口
 */
public interface ISeedlingQualityAnalysisService {

    /**
     * 基于监控截图进行综合苗情分析
     *
     * @param request 请求参数
     * @return 分析结果
     */
    SeedlingAnalysisResponseDTO analyzeSeedling(DiseaseAnalysisRequestDTO request);
}
