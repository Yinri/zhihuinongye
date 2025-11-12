package org.jeecg.modules.youcai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;

/**
 * 天气数据DTO
 */
@Data
public class DailyWeatherDTO {

    /**
     * 日期（YYYY-MM-DD）
     */
    @NotBlank(message = "日期不能为空")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "日期格式错误，应为YYYY-MM-DD")
    @JsonProperty("date")
    private String date;

    /**
     * 预报风速(m/s)
     */
    @NotNull(message = "风速不能为空")
    @JsonProperty("wind_speed")
    private Double windSpeed;

    /**
     * 预报降雨量(mm)
     */
    @NotNull(message = "降雨量不能为空")
    @JsonProperty("rainfall")
    private Double rainfall;
}
