package org.jeecg.modules.youcai.service;

import org.jeecg.modules.youcai.dto.LodgingRiskAssessmentRequestDTO;
import org.jeecg.modules.youcai.dto.LodgingRiskAssessmentResponseDTO;
import org.jeecg.modules.youcai.dto.VideoLodgingAnalysisResultDTO;
import org.jeecg.modules.youcai.entity.YoucaiLodgingRiskWarning;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: 倒伏风险预警表
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V1.0
 */
public interface IYoucaiLodgingRiskWarningService extends IService<YoucaiLodgingRiskWarning> {    
    LodgingRiskAssessmentResponseDTO riskAssessmentById(String plotId);
    
    /**
     * 批量获取基地下所有地块的倒伏风险数据
     * @param baseId 基地ID
     * @return 批量倒伏风险评估响应DTO
     */
    LodgingRiskAssessmentResponseDTO.BatchLodgingRiskAssessmentResponseDTO batchRiskAssessmentByBaseId(String baseId);

    /**
     * 根据视频ID进行倒伏分析
     * @param videoId 视频ID
     * @return 视频倒伏分析结果DTO
     */
    VideoLodgingAnalysisResultDTO analyzeLodgingByVideoId(String videoId);

    /**
     * 批量根据视频ID进行倒伏分析
     * @param videoIds 视频ID列表
     * @return 综合视频倒伏分析结果DTO
     */
    VideoLodgingAnalysisResultDTO batchAnalyzeLodgingByVideoIds(List<String> videoIds);
}
