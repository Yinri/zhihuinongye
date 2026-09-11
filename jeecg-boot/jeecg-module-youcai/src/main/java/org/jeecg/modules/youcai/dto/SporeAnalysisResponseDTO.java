package org.jeecg.modules.youcai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "孢子分析响应DTO")
public class SporeAnalysisResponseDTO {
    
    @Schema(description = "预警等级：低/中/高")
    private String warningLevel;
    
    @Schema(description = "病害早期预警信息")
    private List<DiseaseWarning> diseaseWarnings;
    
    @Schema(description = "科学用药指导")
    private List<MedicationGuide> medicationGuides;
    
    @Schema(description = "孢子检测统计")
    private SporeStatistics statistics;
    
    @Schema(description = "综合分析总结")
    private String summary;

    public String getWarningLevel() {
        return warningLevel;
    }

    public void setWarningLevel(String warningLevel) {
        this.warningLevel = warningLevel;
    }

    public List<DiseaseWarning> getDiseaseWarnings() {
        return diseaseWarnings;
    }

    public void setDiseaseWarnings(List<DiseaseWarning> diseaseWarnings) {
        this.diseaseWarnings = diseaseWarnings;
    }

    public List<MedicationGuide> getMedicationGuides() {
        return medicationGuides;
    }

    public void setMedicationGuides(List<MedicationGuide> medicationGuides) {
        this.medicationGuides = medicationGuides;
    }

    public SporeStatistics getStatistics() {
        return statistics;
    }

    public void setStatistics(SporeStatistics statistics) {
        this.statistics = statistics;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
    
    @Data
    @Schema(description = "病害预警")
    public static class DiseaseWarning {
        @Schema(description = "病害名称")
        private String diseaseName;
        
        @Schema(description = "风险等级")
        private String riskLevel;
        
        @Schema(description = "预警描述")
        private String description;
        
        @Schema(description = "建议措施")
        private String suggestion;

        public String getDiseaseName() {
            return diseaseName;
        }

        public void setDiseaseName(String diseaseName) {
            this.diseaseName = diseaseName;
        }

        public String getRiskLevel() {
            return riskLevel;
        }

        public void setRiskLevel(String riskLevel) {
            this.riskLevel = riskLevel;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getSuggestion() {
            return suggestion;
        }

        public void setSuggestion(String suggestion) {
            this.suggestion = suggestion;
        }
    }
    
    @Data
    @Schema(description = "用药指导")
    public static class MedicationGuide {
        @Schema(description = "农药名称")
        private String pesticideName;
        
        @Schema(description = "使用剂量")
        private String dosage;
        
        @Schema(description = "使用时机")
        private String timing;
        
        @Schema(description = "使用方法")
        private String method;
        
        @Schema(description = "注意事项")
        private String precautions;

        public String getPesticideName() {
            return pesticideName;
        }

        public void setPesticideName(String pesticideName) {
            this.pesticideName = pesticideName;
        }

        public String getDosage() {
            return dosage;
        }

        public void setDosage(String dosage) {
            this.dosage = dosage;
        }

        public String getTiming() {
            return timing;
        }

        public void setTiming(String timing) {
            this.timing = timing;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public String getPrecautions() {
            return precautions;
        }

        public void setPrecautions(String precautions) {
            this.precautions = precautions;
        }
    }
    
    @Data
    @Schema(description = "孢子统计")
    public static class SporeStatistics {
        @Schema(description = "图片总数")
        private Integer totalImages;
        
        @Schema(description = "成功分析图片数量")
        private Integer analyzedImages;
        
        @Schema(description = "检测到的孢子类型数量")
        private Integer sporeTypes;
        
        @Schema(description = "时间范围")
        private String timeRange;
        
        @Schema(description = "孢子密度趋势")
        private String trend;

        public Integer getTotalImages() {
            return totalImages;
        }

        public void setTotalImages(Integer totalImages) {
            this.totalImages = totalImages;
        }

        public Integer getAnalyzedImages() {
            return analyzedImages;
        }

        public void setAnalyzedImages(Integer analyzedImages) {
            this.analyzedImages = analyzedImages;
        }

        public Integer getSporeTypes() {
            return sporeTypes;
        }

        public void setSporeTypes(Integer sporeTypes) {
            this.sporeTypes = sporeTypes;
        }

        public String getTimeRange() {
            return timeRange;
        }

        public void setTimeRange(String timeRange) {
            this.timeRange = timeRange;
        }

        public String getTrend() {
            return trend;
        }

        public void setTrend(String trend) {
            this.trend = trend;
        }
    }
}
