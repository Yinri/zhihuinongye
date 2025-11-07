package org.jeecg.modules.youcai.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    private Integer plotId;

    /**
     * 植株高度(cm)
     */
    private Double plantHeight;

    /**
     * 茎直径(mm)
     */
    private Double stemDiameter;

    /**
     * 生长阶段
     */
    private String growthStage;

    /**
     * 密度(株/平方米)
     */
    private Double density;

    /**
     * 近3天最大风速(m/s)
     */
    private Double windSpeed3d;

    /**
     * 近7天累积降雨量(mm)
     */
    private Double rainfall7d;

    /**
     * 土壤类型
     */
    private String soilType;

    /**
     * 天气预报列表
     */
    private List<DailyWeatherDTO> dailyWeather;
}