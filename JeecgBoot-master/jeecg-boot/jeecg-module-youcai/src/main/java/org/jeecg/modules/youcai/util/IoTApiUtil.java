package org.jeecg.modules.youcai.util;


import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.youcai.entity.iotEntity.ApiResponse;
import org.jeecg.modules.youcai.entity.iotEntity.auth.LoginRequest;
import org.jeecg.modules.youcai.entity.iotEntity.sensor.SensorListRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Collections;

/**
 * 易刚物联网API核心调用类（封装所有接口）
 */
@Component
@Slf4j
public class IoTApiUtil {
    // API基础地址（统一配置，避免硬编码）
    private static final String BASE_URL = "http://zhny.aheagle.com:8887";
    // Token有效期（假设2小时，实际以接口返回为准）
    private static final long TOKEN_EXPIRE_SECONDS = 7200;

    private final WebClient webClient;
    private String token; // 存储JWT Token
    private long tokenExpireTime; // Token过期时间（时间戳：毫秒）

    @Autowired
    public IoTApiUtil(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    // ------------------------------ 1. 登录鉴权 ------------------------------
    /**
     * 登录获取Token（自动处理Token过期刷新）
     */
    public Mono<String> login() {
        LoginRequest request = new LoginRequest();
        request.setUserName("zxzhycadmin"); // 新账号
        request.setPassword("e3a3a21b0b3849938f8dd08e64db801f");

        return webClient.post()
                .uri(BASE_URL + "/LoginSensor/CreateLoginJWT")
                .body(BodyInserters.fromValue(request))
                .retrieve()
                .bodyToMono(ApiResponse.class)
                .map(response -> {
                    if (response.getCode() == 1 && response.getData() != null) {
                        // 解析Token并设置过期时间
                        this.token = response.getData().toString();
                        this.tokenExpireTime = System.currentTimeMillis() + TOKEN_EXPIRE_SECONDS * 1000;
                        log.info("登录成功，Token已更新，过期时间：{}", tokenExpireTime);
                        return this.token;
                    }
                    throw new RuntimeException("登录失败：" + response.getMsg());
                })
                .retryWhen(Retry.fixedDelay(2, Duration.ofSeconds(3)) // 失败重试2次，间隔3秒
                        .filter(e -> e instanceof RuntimeException))
                .doOnError(e -> log.error("登录异常：", e));
    }

    // ------------------------------ 2. 项目相关 ------------------------------
    /**
     * 获取项目列表
     */
    public Mono<ApiResponse> getProjectList() {
        return checkTokenAndRequest(() ->
                webClient.post()
                        .uri(BASE_URL + "/Project/GetProjectList")
                        .header("Authorization", "Bearer " + token)
                        .body(BodyInserters.fromValue(Collections.emptyMap())) // 无参数，传空Map
                        .retrieve()
                        .bodyToMono(ApiResponse.class)
        );
    }

    // ------------------------------ 3. 传感器相关 ------------------------------
    /**
     * 设备在线统计（按项目ID查询）
     */
    public Mono<ApiResponse> getDeviceOnline(Integer projectId) {
        return checkTokenAndRequest(() ->
                webClient.post()
                        .uri(BASE_URL + "/SensorData/GetDeviceOnline")
                        .header("Authorization", "Bearer " + token)
                        .body(BodyInserters.fromValue(Collections.singletonMap("projectId", projectId)))
                        .retrieve()
                        .bodyToMono(ApiResponse.class)
        );
    }

    /**
     * 获取传感器列表（按项目ID+传感器类型）
     */
    public Mono<ApiResponse> getSensorList(SensorListRequest request) {
        return checkTokenAndRequest(() ->
                webClient.post()
                        .uri(BASE_URL + "/SensorData/GetSensorList")
                        .header("Authorization", "Bearer " + token)
                        .body(BodyInserters.fromValue(request))
                        .retrieve()
                        .bodyToMono(ApiResponse.class)
        );
    }

    /**
     * 获取传感器实时数据（按设备编号）
     */
    public Mono<ApiResponse> getSensorRealTimeData(String deviceCode) {
        return checkTokenAndRequest(() ->
                webClient.post()
                        .uri(BASE_URL + "/SensorData/SensorshareNew")
                        .header("Authorization", "Bearer " + token)
                        .body(BodyInserters.fromValue(Collections.singletonMap("deviceCode", deviceCode)))
                        .retrieve()
                        .bodyToMono(ApiResponse.class)
        );
    }

    /**
     * 获取传感器历史数据（按设备编号+时间范围）
     */
//    public Mono<ApiResponse> getSensorHistoryData(String deviceCode, String startDate, String endDate) {
//        return checkTokenAndRequest(
//                webClient.post()
//                        .uri(BASE_URL + "/SensorData/Sensorshare")
//                        .body(BodyInserters.fromValue(Collections.of("DeviceCode", deviceCode, "startDate", startDate, "endDate", endDate)))
//                        .retrieve()
//                        .bodyToMono(ApiResponse.class)
//        );
//    }

    // ------------------------------ 4. 通用方法 ------------------------------
    /**
     * 检查Token有效性：过期则自动刷新
     * @param requestSupplier 请求提供者函数
     * @return 带Token的API响应
     */
    private <T> Mono<T> checkTokenAndRequest(java.util.function.Supplier<Mono<T>> requestSupplier) {
        // 1. Token未初始化或已过期，先刷新Token
        if (token == null || System.currentTimeMillis() > tokenExpireTime) {
            log.info("Token未初始化或已过期，自动刷新Token");
            return login().flatMap(t -> requestSupplier.get())
                    .timeout(Duration.ofSeconds(30)) // 添加总体超时时间
                    .onErrorResume(e -> {
                        log.error("请求失败，返回空响应: {}", e.getMessage());
                        return Mono.empty(); // 返回空Mono而不是类型不匹配的对象
                    });
        }
        // 2. Token有效，直接执行请求
        return requestSupplier.get()
                .timeout(Duration.ofSeconds(30)) // 添加总体超时时间
                .onErrorResume(e -> {
                    log.error("请求失败，返回空响应: {}", e.getMessage());
                    return Mono.empty(); // 返回空Mono而不是类型不匹配的对象
                });
    }

    /**
     * 创建回退响应
     */
    private ApiResponse createFallbackResponse() {
        ApiResponse response = new ApiResponse();
        response.setCode(0);
        response.setMsg("服务暂时不可用，使用回退响应");
        response.setData(null);
        return response;
    }
}
