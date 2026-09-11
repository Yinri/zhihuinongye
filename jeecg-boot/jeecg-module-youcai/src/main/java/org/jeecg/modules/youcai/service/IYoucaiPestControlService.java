package org.jeecg.modules.youcai.service;

import org.jeecg.modules.youcai.dto.AnalysisRequestDTO;
import org.jeecg.modules.youcai.entity.YoucaiPestControl;
import com.baomidou.mybatisplus.extension.service.IService;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * @Description: 虫害防控表
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V1.0
 */
public interface IYoucaiPestControlService extends IService<YoucaiPestControl> {
       Mono<List<Map<String, Object>>> getPestImages(String baseName, String startDate, String endDate);
       String aiAnalysis(AnalysisRequestDTO req) throws Exception;
}
