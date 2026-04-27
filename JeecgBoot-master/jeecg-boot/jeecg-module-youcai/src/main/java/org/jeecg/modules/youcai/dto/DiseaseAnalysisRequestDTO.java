package org.jeecg.modules.youcai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "病害分析请求DTO")
public class DiseaseAnalysisRequestDTO {

    @Schema(description = "图片URL列表")
    private List<String> imageUrls;

    @Schema(description = "基地名称")
    private String baseName;
}
