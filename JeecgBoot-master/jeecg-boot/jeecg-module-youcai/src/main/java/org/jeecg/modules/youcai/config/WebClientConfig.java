package org.jeecg.modules.youcai.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * WebClient配置
 */
@Configuration
public class WebClientConfig {

    /**
     * 创建WebClient Bean
     */
    @Bean
    public WebClient webClient() {
        // 配置连接池
        ConnectionProvider connectionProvider = ConnectionProvider.builder("http-pool")
                .maxConnections(100) // 最大连接数
                .pendingAcquireTimeout(Duration.ofSeconds(45)) // 获取连接超时时间
                .pendingAcquireMaxCount(-1) // 最大等待获取连接数，-1表示不限制
                .build();

        // 配置HttpClient
        HttpClient httpClient = HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // 连接超时时间（毫秒）
                .responseTimeout(Duration.ofSeconds(30)) // 响应超时时间
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(30, TimeUnit.SECONDS)) // 读取超时时间
                                .addHandlerLast(new WriteTimeoutHandler(30, TimeUnit.SECONDS))); // 写入超时时间

        // 创建WebClient
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 设置最大内存大小为10MB
                .build();
    }
}