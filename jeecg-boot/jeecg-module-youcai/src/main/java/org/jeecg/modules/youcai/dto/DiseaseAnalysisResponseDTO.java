package org.jeecg.modules.youcai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
@Schema(description = "病害分析响应DTO")
public class DiseaseAnalysisResponseDTO {
    
    @Schema(description = "病害名称")
    private String diseaseName;
    
    @Schema(description = "置信度")
    private Double confidence;
    
    @Schema(description = "病害描述")
    private String description;
    
    @Schema(description = "危害程度：轻度/中度/重度")
    private String severity;
    
    @Schema(description = "防治建议")
    private List<String> preventionMeasures;
    
    @Schema(description = "推荐农药")
    private List<PesticideRecommendation> recommendedPesticides;
    
    @Schema(description = "最佳防治时期")
    private String bestPreventionTime;
    
    @Schema(description = "综合分析总结")
    private String summary;

    @Schema(description = "分析时间")
    private Date analysisTime;

    public String getDiseaseName() {
        return diseaseName;
    }

    public void setDiseaseName(String diseaseName) {
        this.diseaseName = diseaseName;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public List<String> getPreventionMeasures() {
        return preventionMeasures;
    }

    public void setPreventionMeasures(List<String> preventionMeasures) {
        this.preventionMeasures = preventionMeasures;
    }

    public List<PesticideRecommendation> getRecommendedPesticides() {
        return recommendedPesticides;
    }

    public void setRecommendedPesticides(List<PesticideRecommendation> recommendedPesticides) {
        this.recommendedPesticides = recommendedPesticides;
    }

    public String getBestPreventionTime() {
        return bestPreventionTime;
    }

    public void setBestPreventionTime(String bestPreventionTime) {
        this.bestPreventionTime = bestPreventionTime;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Date getAnalysisTime() {
        return analysisTime;
    }

    public void setAnalysisTime(Date analysisTime) {
        this.analysisTime = analysisTime;
    }

    @Data
    @Schema(description = "农药推荐")
    public static class PesticideRecommendation {
        @Schema(description = "农药名称")
        private String name;
        
        @Schema(description = "使用剂量")
        private String dosage;
        
        @Schema(description = "使用方法")
        private String usage;
        
        @Schema(description = "注意事项")
        private String precautions;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDosage() {
            return dosage;
        }

        public void setDosage(String dosage) {
            this.dosage = dosage;
        }

        public String getUsage() {
            return usage;
        }

        public void setUsage(String usage) {
            this.usage = usage;
        }

        public String getPrecautions() {
            return precautions;
        }

        public void setPrecautions(String precautions) {
            this.precautions = precautions;
        }
    }
}
