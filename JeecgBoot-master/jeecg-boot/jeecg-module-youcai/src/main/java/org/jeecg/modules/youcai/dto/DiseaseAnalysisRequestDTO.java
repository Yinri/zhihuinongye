package org.jeecg.modules.youcai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "病害分析请求DTO")
public class DiseaseAnalysisRequestDTO {
    
    @Schema(description = "图片URL")
    private String imageUrl;
    
    @Schema(description = "图片Base64数据")
    private String imageBase64;
    
    @Schema(description = "基地ID")
    private String baseId;
    
    @Schema(description = "基地名称")
    private String baseName;
    
    @Schema(description = "设备编码")
    private String deviceCode;
    
    @Schema(description = "图片类型：monitor-监控截图, spore-孢子捕捉仪")
    private String imageType;
}
