package org.jeecg.modules.youcai.dto;

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
    private String date;

    /**
     * 预报风速(m/s)
     */
    @NotNull(message = "风速不能为空")
    @DecimalMin(value = "0", message = "风速不能小于0")
    @DecimalMax(value = "20", message = "风速不能大于20")
    private Double windSpeed;

    /**
     * 预报降雨量(mm)
     */
    @NotNull(message = "降雨量不能为空")
    @DecimalMin(value = "0", message = "降雨量不能小于0")
    @DecimalMax(value = "300", message = "降雨量不能大于300")
    private Double rainfall;
}
