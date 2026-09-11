package org.jeecg.modules.youcai.util;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.youcai.config.QWeatherConfig;
import org.jeecg.modules.youcai.dto.DailyWeatherDTO;
import org.jeecg.modules.youcai.entity.weatherEntity.QWeatherResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 和风天气API调用工具类
 */
@Component
@Slf4j
public class QWeatherApiUtil {
    
    private final WebClient webClient;
    private final QWeatherConfig config;
    
    @Autowired
    public QWeatherApiUtil(WebClient.Builder webClientBuilder, QWeatherConfig config) {
        this.config = config;
        // 根据和风天气API规范，使用HTTPS协议和用户配置的Host
        String baseUrl = "https://" + config.getHost();
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .build();
    }
    
    /**
     * 获取未来7天的天气预报
     * @param location 经纬度坐标，格式为"经度,纬度"，例如"116.40,39.9"
     * @return 天气预报数据列表
     */
    public Mono<List<DailyWeatherDTO>> get7DayWeatherForecast(String location) {
        log.info("调用和风天气API获取7天天气预报，位置：{}", location);
        
        // 构建完整的请求URL
        String requestUrl = "https://" + config.getHost() + "/v7/weather/7d?location=" + location;
        log.info("完整请求URL: {}", requestUrl);
        log.info("请求头信息: X-QW-Api-Key={}", config.getApiKey());
        
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v7/weather/7d")
                        .queryParam("location", location)
                        .build())
                .header("X-QW-Api-Key", config.getApiKey())
                .retrieve()
                .bodyToMono(QWeatherResponse.class)
                .doOnNext(response -> {
                    // 打印响应状态码和基本信息
                    if (response != null) {
                        log.info("API响应状态码: {}", response.getCode());
                        log.info("响应更新时间: {}", response.getUpdateTime());
                        if (response.getDaily() != null) {
                            log.info("获取到{}天天气预报数据", response.getDaily().size());
                            // 打印每一天的基本天气信息
                            for (int i = 0; i < Math.min(response.getDaily().size(), 7); i++) {
                                QWeatherResponse.WeatherDaily daily = response.getDaily().get(i);
                                log.info("第{}天 - 日期: {}, 天气: {}, 风速: {}km/h, 降雨量: {}mm", 
                                        i+1, daily.getFxDate(), daily.getTextDay(), 
                                        daily.getWindSpeedDay(), daily.getPrecip());
                            }
                        } else {
                            log.warn("响应中的天气数据为空");
                        }
                    } else {
                        log.warn("API响应为空");
                    }
                })
                .map(this::convertToDailyWeatherDTOs)
                .retryWhen(Retry.fixedDelay(config.getRetryCount(), Duration.ofMillis(config.getTimeout()))
                        .doBeforeRetry(retrySignal -> log.warn("重试获取天气数据，重试次数：{}", retrySignal.totalRetries() + 1))
                )
                .doOnError(e -> log.error("获取天气预报失败：", e))
                .doOnSuccess(result -> log.info("成功转换并返回{}天天气预报数据", result.size()));
    }
    
    /**
     * 获取实时天气
     * @param location 经纬度坐标，格式为"经度,纬度"，例如"116.40,39.9"
     * @return 实时天气数据
     */
    public Mono<DailyWeatherDTO> getCurrentWeather(String location) {
        log.info("调用和风天气API获取实时天气，位置：{}", location);
        
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/weather/now")
                        .queryParam("location", location)
                        .build())
                .header("X-QW-Api-Key", config.getApiKey())
                .retrieve()
                .bodyToMono(String.class) // 实时天气返回结构不同，先作为字符串处理
                .map(this::parseCurrentWeather)
                .retryWhen(Retry.fixedDelay(config.getRetryCount(), Duration.ofMillis(config.getRetryInterval()))
                        .doBeforeRetry(retrySignal -> log.warn("重试获取实时天气数据，重试次数：{}", retrySignal.totalRetries() + 1))
                )
                .doOnError(e -> log.error("获取实时天气失败：", e))
                .doOnSuccess(result -> log.info("成功获取实时天气数据"));
    }
    
    /**
     * 将和风天气API响应转换为DailyWeatherDTO列表
     * @param response 和风天气API响应
     * @return DailyWeatherDTO列表
     */
    private List<DailyWeatherDTO> convertToDailyWeatherDTOs(QWeatherResponse response) {
        List<DailyWeatherDTO> result = new ArrayList<>();
        
        if (response == null || response.getDaily() == null) {
            log.warn("天气响应数据为空");
            return result;
        }
        
        for (QWeatherResponse.WeatherDaily daily : response.getDaily()) {
            DailyWeatherDTO dto = new DailyWeatherDTO();
            
            // 确保日期格式正确
            String date = daily.getFxDate();
            if (date != null && date.matches("\\d{4}-\\d{2}-\\d{2}")) {
                dto.setDate(date);
            } else {
                log.warn("日期格式不正确: {}, 使用当前日期", date);
                dto.setDate(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
            }
            
            // 风速转换：和风天气API返回的km/h需要转换为m/s
            if (daily.getWindSpeedDay() != null && !daily.getWindSpeedDay().isEmpty()) {
                try {
                    double windSpeedKmh = Double.parseDouble(daily.getWindSpeedDay());
                    double windSpeedMs = windSpeedKmh / 3.6; // km/h转m/s
                    windSpeedMs = Math.round(windSpeedMs * 100) / 100.0;
                    dto.setWindSpeed(windSpeedMs);
                } catch (NumberFormatException e) {
                    log.warn("风速数据解析失败：{}", daily.getWindSpeedDay());
                    dto.setWindSpeed(0.0);
                }
            } else {
                dto.setWindSpeed(0.0);
            }
            
            // 降雨量转换
            if (daily.getPrecip() != null && !daily.getPrecip().isEmpty()) {
                try {
                    double rainfall = Double.parseDouble(daily.getPrecip());
                    dto.setRainfall(rainfall);
                } catch (NumberFormatException e) {
                    log.warn("降雨量数据解析失败：{}", daily.getPrecip());
                    dto.setRainfall(0.0);
                }
            } else {
                dto.setRainfall(0.0);
            }
            
            result.add(dto);
            log.info("转换天气数据 - 日期: {}, 风速: {}m/s, 降雨量: {}mm", 
                    dto.getDate(), dto.getWindSpeed(), dto.getRainfall());
        }
        
        return result;
    }
    
    /**
     * 解析实时天气数据
     * @param response 实时天气API响应字符串
     * @return DailyWeatherDTO
     */
    private DailyWeatherDTO parseCurrentWeather(String response) {
        DailyWeatherDTO dto = new DailyWeatherDTO();
        
        try {
            // 设置当前日期
            dto.setDate(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
            
            // 这里应该解析JSON响应，但为了简化，先设置默认值
            // 实际应用中应该使用Jackson或其他JSON库解析响应
            dto.setWindSpeed(0.0);
            dto.setRainfall(0.0);
            
            log.info("实时天气数据解析完成，日期：{}", dto.getDate());
        } catch (Exception e) {
            log.error("解析实时天气数据失败：", e);
            // 返回默认值
            dto.setDate(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
            dto.setWindSpeed(0.0);
            dto.setRainfall(0.0);
        }
        
        return dto;
    }
}