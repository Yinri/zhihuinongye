package org.jeecg.modules.youcai.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 倒伏风险评估响应DTO
 * @author system
 * @date 2025-11-05
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LodgingRiskAssessmentResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;
        
    /**
     * 地块ID
     */
    private String plotId;

     /**
     * 地块名称
     */
    private String plotName;

    /**
     * 当前风险
     */
    @JsonProperty("current_risk")
    private CurrentRiskDTO currentRisk;

    /**
     * 未来7天预测
     */
    @JsonProperty("forecast_7days")
    private Forecast7DaysDTO forecast7Days;

    /**
     * 综合防护建议
     */
    @JsonProperty("comprehensive_suggestions")
    private ComprehensiveSuggestionsDTO comprehensiveSuggestions;

    /**
     * 计算时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", timezone = "GMT")
    private Date calculationTime;

    /**
     * 当前风险DTO
     */
    @Data
    public static class CurrentRiskDTO implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 风险评分
         */
        private Double riskScore;

        /**
         * 风险等级
         */
        private String riskLevel;

        /**
         * 倒伏概率
         */
        private String lodgingProbability;

        /**
         * 归一化因子
         */
        private NormalizedFactorsDTO normalizedFactors;

        /**
         * 原始因子
         */
        private OriginalFactorsDTO originalFactors;

        /**
         * 因子贡献度
         */
        private Map<String, Object> factorContributions;

        /**
         * 高风险因子
         */
        private List<HighRiskFactorDTO> highRiskFactors;

        /**
         * 建议
         */
        private SuggestionsDTO suggestions;
        
        /**
         * 计算时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", timezone = "GMT")
        private Date calculationTime;
    }

    /**
     * 归一化因子DTO
     */
    @Data
    public static class NormalizedFactorsDTO implements Serializable {

        private static final long serialVersionUID = 1L;

        private Double plantHeight;

        private Double stemDiameter;

        private Double slendernessRatio;

        private Double windSpeed3d;

        private Double rainfall7d;


        private Double growthStage;

        private Double density;

        private Double soilType;
    }

    /**
     * 原始因子DTO
     */
    @Data
    public static class OriginalFactorsDTO implements Serializable {

        private static final long serialVersionUID = 1L;

        private Double plantHeight;

        private Double stemDiameter;

        private Double windSpeed3d;

        private Double rainfall7d;

        private String growthStage;

        private Double density;

        private String soilType;
    }

    /**
     * 高风险因子DTO
     */
    @Data
    public static class HighRiskFactorDTO implements Serializable {

        private static final long serialVersionUID = 1L;

        private String key;

        private String name;

        private Double normalizedValue;

        private String status;
    }

    /**
     * 未来7天预测DTO
     */
    @Data
    public static class Forecast7DaysDTO implements Serializable {

        private static final long serialVersionUID = 1L;

        private String plotId;

        private List<DailyRiskDTO> dailyRisks;

        private String maxRiskDate;

        private Double maxRiskScore;
        
        /**
         * 计算时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", timezone = "GMT")
        private Date calculationTime;
    }

    /**
     * 每日风险DTO
     */
    @Data
    public static class DailyRiskDTO implements Serializable {

        private static final long serialVersionUID = 1L;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private Date date;

        private Double riskScore;

        private String riskLevel;

        private String lodgingProbability;

        private WeatherInfoDTO weather;
    }

    /**
     * 天气DTO
     */
    @Data
    public static class WeatherDTO implements Serializable {

        private static final long serialVersionUID = 1L;

        private Double windSpeed;

        private Double rainfall;
    }

    /**
     * 综合防护建议DTO
     */
    @Data
    public static class ComprehensiveSuggestionsDTO implements Serializable {

        private static final long serialVersionUID = 1L;

        private List<String> riskAlert;

        private List<String> immediate;

        private List<String> shortTerm;

        private List<String> mediumTerm;
        private List<String> longTerm;
    }
    
    /**
     * 建议DTO
     */
    @Data
    public static class SuggestionsDTO implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        private List<String> urgent;
        
        private List<String> mediumTerm;
        
        private List<String> longTerm;
    }
    
    /**
     * 天气信息DTO
     */
    @Data
    public static class WeatherInfoDTO implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        private Double windSpeed;
        
        private Double rainfall;
    }
    
    /**
     * 批量倒伏风险评估响应DTO
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BatchLodgingRiskAssessmentResponseDTO implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        /**
         * 基地ID
         */
        private String baseId;
        
        /**
         * 基地名称
         */
        private String baseName;
        
        /**
         * 地块风险列表
         */
        private List<LodgingRiskAssessmentResponseDTO> plotRisks;
        
        /**
         * 基地整体风险统计
         */
        private BaseRiskStatisticsDTO baseStatistics;
        
        /**
         * 计算时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", timezone = "GMT")
        private Date calculationTime;
    }
    

    
    /**
     * 基地风险统计DTO
     */
    @Data
    public static class BaseRiskStatisticsDTO implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        /**
         * 地块总数
         */
        private Integer totalPlots;
        
        /**
         * 高风险地块数量
         */
        private Integer highRiskPlots;
        
        /**
         * 极高风险地块数量
         */
        private Integer extremeRiskPlots;
        
        /**
         * 中风险地块数量
         */
        private Integer mediumRiskPlots;
        
        /**
         * 低风险地块数量
         */
        private Integer lowRiskPlots;
        
        /**
         * 平均风险评分
         */
        private Double averageRiskScore;
        
        /**
         * 最高风险地块ID
         */
        private String highestRiskPlotId;
        
        /**
         * 最高风险评分
         */
        private Double highestRiskScore;
        
        /**
         * 风险分布统计
         */
        private Map<String, Integer> riskDistribution;
    }
}