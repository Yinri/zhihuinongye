package org.jeecg.modules.youcai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Schema(description = "油菜苗情分析响应DTO")
public class SeedlingAnalysisResponseDTO {

    @Schema(description = "综合苗情等级：壮苗/正常/偏弱/旺长/未知")
    private String seedlingLevel;

    @Schema(description = "判断到的生育阶段")
    private String growthStage;

    @Schema(description = "置信度")
    private Double confidence;

    @Schema(description = "综合总结")
    private String summary;

    @Schema(description = "主要判断依据")
    private String evidence;

    @Schema(description = "关键苗情指标")
    private List<SeedlingIndicator> indicators;

    @Schema(description = "主要问题")
    private List<String> mainProblems;

    @Schema(description = "田间管理建议")
    private List<String> managementSuggestions;

    @Schema(description = "分析时间")
    private Date analysisTime;

    @Data
    @Schema(description = "苗情指标")
    public static class SeedlingIndicator {
        @Schema(description = "指标名称")
        private String name;

        @Schema(description = "指标值")
        private String value;

        @Schema(description = "指标等级")
        private String level;

        @Schema(description = "指标说明")
        private String description;
    }
}
