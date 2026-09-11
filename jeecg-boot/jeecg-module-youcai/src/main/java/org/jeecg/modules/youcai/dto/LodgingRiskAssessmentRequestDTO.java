package org.jeecg.modules.youcai.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author system
 * @date 2025-11-06
 */
@Data
public class LodgingRiskAssessmentRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 地块ID
     */
    @JsonProperty("plot_id")
    private String plotId;

    /**
     * 植株高度(cm)
     */
    @JsonProperty("plant_height")
    private Double plantHeight;

    /**
     * 茎直径(mm)
     */
    @JsonProperty("stem_diameter")
    private Double stemDiameter;

    /**
     * 生长阶段
     */
    @JsonProperty("growth_stage")
    private String growthStage;

    /**
     * 密度(株/平方米)
     */
    @JsonProperty("density")
    private Double density;

    /**
     * 近3天最大风速(m/s)
     */
    @JsonProperty("wind_speed_3d")
    private Double windSpeed3d;

    /**
     * 近7天累积降雨量(mm)
     */
    @JsonProperty("rainfall_7d")
    private Double rainfall7d;

    /**
     * 土壤类型
     */
    @JsonProperty("soil_type")
    private String soilType;

    /**
     * 天气预报列表
     */
    @JsonProperty("weather_forecast")
    private List<DailyWeatherDTO> dailyWeather;
}