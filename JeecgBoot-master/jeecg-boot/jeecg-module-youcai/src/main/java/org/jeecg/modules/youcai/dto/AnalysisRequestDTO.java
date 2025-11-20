package org.jeecg.modules.youcai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;


@Data
public class AnalysisRequestDTO {
    @JsonProperty("pest_data") // 前端 JSON 是 pest_data，这里做映射
    private List<PestItem> pestData;
    @Data
    public static class PestItem {
        @JsonProperty("analysis_time") // 前端 JSON 是 analysis_time
        private String analysisTime;
        private Map<String, Integer> insects;
    }
}
