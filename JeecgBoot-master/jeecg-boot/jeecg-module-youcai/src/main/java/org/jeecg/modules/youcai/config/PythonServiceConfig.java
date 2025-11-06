package org.jeecg.modules.youcai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Python服务配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "python.service")
public class PythonServiceConfig {

    /**
     * Python服务地址
     */
    private String url;

    /**
     * API密钥
     */
    private String apiKey;

    /**
     * 是否启用
     */
    private Boolean enabled = true;
}
