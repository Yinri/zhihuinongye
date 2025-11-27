package org.jeecg.modules.youcai.service;

import org.jeecg.modules.youcai.dto.AnalysisRequestDTO;
import org.jeecg.modules.youcai.entity.YoucaiPestControl;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.youcai.entity.iotEntity.ApiResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @Description: 虫害防控表
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V1.0
 */
public interface IYoucaiPestControlService extends IService<YoucaiPestControl> {
       Mono <List<Map<String, Object>>> getPestImages();
       String aiAnalysis(AnalysisRequestDTO req) throws Exception;
       Mono<List<Map<String, Object>>> getAllPestImages(String startDate, String endDate);
       List<YoucaiPestControl> findControl(String plotId, String start, String end);

}
