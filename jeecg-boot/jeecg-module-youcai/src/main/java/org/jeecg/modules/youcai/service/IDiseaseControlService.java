package org.jeecg.modules.youcai.service;

import org.jeecg.modules.youcai.dto.DiseaseAnalysisRequestDTO;
import org.jeecg.modules.youcai.dto.DiseaseAnalysisResponseDTO;
import org.jeecg.modules.youcai.dto.SporeAnalysisResponseDTO;

import java.util.List;
import java.util.Map;

/**
 * 病害防控分析服务接口
 */
public interface IDiseaseControlService {
    
    /**
     * 根据基地ID获取视频监控截图列表
     *
     * @param baseId 基地ID
     * @return 图片列表
     */
    List<Map<String, Object>> getMonitorImagesByBaseId(String baseId);

    /**
     * 根据基地名称获取孢子捕捉仪图片列表
     *
     * @param baseName 基地名称
     * @return 图片列表
     */
    List<Map<String, Object>> getSporeImagesByBaseName(String baseName);

    /**
     * 根据基地ID批量分析监控截图
     *
     * @param baseId 基地ID
     * @return 分析结果
     */
    DiseaseAnalysisResponseDTO analyzeMonitorBatchByBaseId(String baseId);
    
    /**
     * 分析作物病害（监控截图）
     * @param request 请求参数
     * @return 分析结果
     */
    DiseaseAnalysisResponseDTO analyzeDisease(DiseaseAnalysisRequestDTO request);
    
    /**
     * 根据基地名称分析孢子捕捉仪图片
     *
     * @param request 请求参数
     * @return 分析结果
     */
    SporeAnalysisResponseDTO analyzeSpore(DiseaseAnalysisRequestDTO request);
    
}
