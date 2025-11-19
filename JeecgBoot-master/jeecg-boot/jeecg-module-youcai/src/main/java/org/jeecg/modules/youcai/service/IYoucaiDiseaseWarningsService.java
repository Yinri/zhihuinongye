package org.jeecg.modules.youcai.service;

import org.jeecg.modules.youcai.entity.YoucaiDiseaseWarnings;
import com.baomidou.mybatisplus.extension.service.IService;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * @Description: 病害预警表
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V1.0
 */
public interface IYoucaiDiseaseWarningsService extends IService<YoucaiDiseaseWarnings> {
    Mono<List<Map<String, Object>>> getDiseaseImages();
}
