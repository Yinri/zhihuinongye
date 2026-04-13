package org.jeecg.modules.youcai.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 视频倒伏分析结果DTO
 * @author system
 * @date 2026-04-04
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "视频倒伏分析结果")
public class VideoLodgingAnalysisResultDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 视频ID
     */
    @Schema(description = "视频ID")
    private String videoId;

    /**
     * 分析图片URL
     */
    @Schema(description = "分析图片URL")
    private String imageUrl;

    /**
     * 分析时间
     */
    @Schema(description = "分析时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date analysisTime;

    /**
     * 倒伏比例(%)
     */
    @Schema(description = "倒伏比例(%)")
    private Double lodgingRatio;

    /**
     * 倒伏面积(平方米)
     */
    @Schema(description = "倒伏面积(平方米)")
    private Double lodgingArea;

    /**
     * 总面积(平方米)
     */
    @Schema(description = "总面积(平方米)")
    private Double totalArea;

    /**
     * 风险等级
     */
    @Schema(description = "风险等级")
    private String riskLevel;

    /**
     * 置信度(%)
     */
    @Schema(description = "置信度(%)")
    private Double confidence;

    /**
     * 详细数据
     */
    @Schema(description = "详细数据")
    private DetailsDTO details;

    /**
     * 建议措施
     */
    @Schema(description = "建议措施")
    private List<String> suggestions;

    /**
     * 详细数据DTO
     */
    @Data
    @Schema(description = "详细数据")
    public static class DetailsDTO implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 健康区域面积(平方米)
         */
        @Schema(description = "健康区域面积(平方米)")
        private Double healthyArea;

        /**
         * 轻度倒伏面积(平方米)
         */
        @Schema(description = "轻度倒伏面积(平方米)")
        private Double mildLodgingArea;

        /**
         * 中度倒伏面积(平方米)
         */
        @Schema(description = "中度倒伏面积(平方米)")
        private Double moderateLodgingArea;

        /**
         * 重度倒伏面积(平方米)
         */
        @Schema(description = "重度倒伏面积(平方米)")
        private Double severeLodgingArea;
    }
}
