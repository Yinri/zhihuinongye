package org.jeecg.modules.youcai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 和风天气API配置类
 * 
 * 和风天气API请求路径构成：
 * - scheme: 仅支持HTTPS协议
 * - host: 开发者的API Host，请在控制台-设置中查看
 * - path: API的请求路径（或称之为API端点、Endpoint）
 * - path params: 路径参数均为必选参数
 * - query params: 查询参数，包括必选和可选参数，多个查询参数使用&分割
 * - headers: 请求头中需要包含X-QW-Api-Key用于身份验证
 */
@Configuration
@ConfigurationProperties(prefix = "weather.qweather")
@Data
public class QWeatherConfig {
    
    /**
     * 和风天气API密钥
     */
    private String apiKey;


    /**
     * 和风天气API Host
     */
    private String host;


    /**
     * 请求超时时间（毫秒）
     */
    private int timeout = 5000;
    
    /**
     * 重试次数
     */
    private int retryCount = 3;
    
    /**
     * 重试间隔（毫秒）
     */
    private int retryInterval = 1000;
}