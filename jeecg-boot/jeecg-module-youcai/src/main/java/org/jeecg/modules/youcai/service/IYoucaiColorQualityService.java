package org.jeecg.modules.youcai.service;

import java.io.File;

import org.jeecg.modules.youcai.entity.YoucaiColorQuality;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.databind.JsonNode;

public interface IYoucaiColorQualityService extends IService<YoucaiColorQuality>{

    String callFastApi(File file);
    
    String generateAdvice(JsonNode analysisJson) throws Exception;
    
    String callMiduFastApi(File file);

    


    

    
}
