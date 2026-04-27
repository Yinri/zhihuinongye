package org.jeecg.modules.youcai.util;


import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.youcai.entity.iotEntity.ApiResponse;
import org.jeecg.modules.youcai.entity.iotEntity.auth.LoginRequest;
import org.jeecg.modules.youcai.entity.iotEntity.sensor.SensorListRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.ClientCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

/**
 * 易刚物联网API核心调用类（封装所有接口）
 */
@Component
@Slf4j
public class IoTApiUtil {
    // API基础地址（统一配置，避免硬编码）
    private static final String BASE_URL = "http://zhny.aheagle.com:8887";
    private static final String HD_BASE_URL = "http://zxcyy.aheagle.com";
    private static final long TOKEN_EXPIRE_SECONDS = 7200;

    private final WebClient webClient;
    private volatile String token;
    private volatile long tokenExpireTime;
    private final Object tokenLock = new Object();

    @Autowired
    public IoTApiUtil(WebClient.Builder webClientBuilder) {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> {
                    ClientCodecConfigurer clientCodecConfigurer = (ClientCodecConfigurer) configurer;
                    clientCodecConfigurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024);
                })
                .build();
        
        this.webClient = webClientBuilder
                .exchangeStrategies(strategies)
                .defaultHeader("Content-Type", "application/json;charset=utf-8")
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
                .header("Content-Type", "application/json;charset=utf-8")
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
     * 获取虫情、孢子、杀虫灯设备列表（根据项目ID和设备类型以及每页数据条数）
     * @param projectId 项目ID
     * @param eqType 设备类型：1.虫情设备，2.孢子仪，3.杀虫灯设备
     * @param limit 每页数据条数，默认150条
     * @return 设备列表响应
     */
    public Mono<ApiResponse> getDeviceList(Integer projectId, Integer eqType, Integer limit) {
        return checkTokenAndRequest(() -> {
            // 构建请求参数
            java.util.Map<String, Object> requestBody = new java.util.HashMap<>();
            requestBody.put("projectId", projectId);
            requestBody.put("eqType", eqType);
            requestBody.put("limit", limit);


            return webClient.post()
                    .uri(BASE_URL + "/SensorData/GetInsertList")
                    .header("Authorization", "Bearer " + token)
                    .body(BodyInserters.fromValue(requestBody))
                    .retrieve()
                    .bodyToMono(ApiResponse.class);
        });
    }


   public Mono<ApiResponse> getPestPhotos(String DeviceCode, Integer CountType, String StarDate, String EndDate) {
    return checkTokenAndRequest(() -> {
        java.util.Map<String, Object> requestBody = new java.util.HashMap<>();
        requestBody.put("DeviceCode", DeviceCode);
        requestBody.put("CountType", CountType);
        requestBody.put("StarDate", StarDate);
        requestBody.put("EndDate", EndDate);
        return webClient.post()
                .uri(BASE_URL + "/SensorData/GetDevImage")
                .header("Authorization", "Bearer " + token)
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                .bodyToMono(ApiResponse.class);
    });
}

    public Mono<ApiResponse> getDiseasePhotos(String DeviceCode, Integer CountType, String StarDate, String EndDate) {
        return checkTokenAndRequest(() -> {
            java.util.Map<String, Object> requestBody = new java.util.HashMap<>();
            requestBody.put("DeviceCode", DeviceCode);
            requestBody.put("CountType", CountType);
            requestBody.put("StarDate", StarDate);
            requestBody.put("EndDate", EndDate);
            return webClient.post()
                    .uri(BASE_URL + "/SensorData/GetDevImage")
                    .header("Authorization", "Bearer " + token)
                    .body(BodyInserters.fromValue(requestBody))
                    .retrieve()
                    .bodyToMono(ApiResponse.class);
        });
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
     * 获取光谱设备列表
     */
    public Mono<ApiResponse> getSpectrumDeviceList(Integer projectId) {
        return checkTokenAndRequest(() ->
                webClient.post()
                        .uri(BASE_URL + "/ALiYun/GetProSensors")
                        .header("Authorization", "Bearer " + token)
                        .body(BodyInserters.fromValue(Collections.singletonMap("projectId", projectId)))
                        .retrieve()
                        .bodyToMono(ApiResponse.class)
        );
    }

    /**
     * 获取视频设备列表
     */
    public Mono<ApiResponse> getVideoDeviceList(Integer projectId) {
        return checkTokenAndRequest(() ->
                webClient.post()
                        .uri(BASE_URL + "/SensorData/GetVideoData")
                        .header("Authorization", "Bearer " + token)
                        .body(BodyInserters.fromValue(Collections.singletonMap("projectId", projectId)))
                        .retrieve()
                        .bodyToMono(ApiResponse.class)
        );
    }


    /**
     * 获取视频播放地址
     */
    public Mono<ApiResponse> getVideoStrem(String DeviceCode,String ChannelNum) {
        return checkTokenAndRequest(() ->
                webClient.post()
                        .uri(BASE_URL + "/SensorData/GetVideoStrem")
                        .header("Authorization", "Bearer " + token)
                        .body(BodyInserters.fromValue(java.util.Map.of("DeviceCode", DeviceCode, "ChannelNum", ChannelNum,"protocol","https_flv")))
                        .retrieve()
                        .bodyToMono(ApiResponse.class)
        );
    }

    /**
     * 视频云台控制
     */
    public Mono<ApiResponse> controlVideoStream(String deviceCode, String channelNum, String command, String speed) {
        return checkTokenAndRequest(() -> {
            java.util.Map<String, Object> requestBody = new java.util.HashMap<>();
            requestBody.put("DeviceCode", deviceCode);
            requestBody.put("ChannelNum", channelNum);
            requestBody.put("command", command);
            requestBody.put("speed", speed == null || speed.isBlank() ? "33" : speed);

            return webClient.post()
                    .uri(BASE_URL + "/SensorData/VideoStremControl")
                    .header("Content-Type", "application/json;charset=utf-8")
                    .header("Authorization", token)
                    .body(BodyInserters.fromValue(requestBody))
                    .retrieve()
                    .bodyToMono(ApiResponse.class);
        });
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

    // ------------------------------ 5. 北斗农机作业接口 ------------------------------
    /**
     * 1.1 作业信息查询
     */
    public Mono<ApiResponse> getJobInfo(String sn, String startDate, String endDate) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("http")
                        .host("zxcyy.aheagle.com")
                        .path("/api/hd/job")
                        .queryParam("sn", sn)
                        .queryParam("startDate", startDate)
                        .queryParam("endDate", endDate)
                        .build())
                .header("Content-Type", "application/json;charset=utf-8")
                .retrieve()
                .bodyToMono(ApiResponse.class);
    }

    /**
     * 1.2 轨迹信息查询（不能跨天查询）
     */
    public Mono<ApiResponse> getTrackInfo(String sn, String startDate, String endDate) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("http")
                        .host("zxcyy.aheagle.com")
                        .path("/api/hd/track")
                        .queryParam("sn", sn)
                        .queryParam("startDate", startDate)
                        .queryParam("endDate", endDate)
                        .build())
                .header("Content-Type", "application/json;charset=utf-8")
                .retrieve()
                .bodyToMono(ApiResponse.class);
    }

    /**
     * 1.3 获取农机在线状态
     */
    public Mono<ApiResponse> getDeviceInfo(String sn) {
        return webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder
                            .scheme("https")
                            .host("zxcyy.aheagle.com")
                            .path("/api/hd/device/info");
                    if (sn != null && !sn.isEmpty()) {
                        builder.queryParam("sn", sn);
                    }
                    return builder.build();
                })
                .header("Content-Type", "application/json;charset=utf-8")
                .retrieve()
                .bodyToMono(ApiResponse.class);
    }
    /**
     * 检查Token有效性：过期则自动刷新
     * @param requestSupplier 请求提供者函数
     * @return 带Token的API响应
     */
    private <T> Mono<T> checkTokenAndRequest(java.util.function.Supplier<Mono<T>> requestSupplier) {
        return Mono.fromCallable(() -> {
            synchronized (tokenLock) {
                if (token == null || System.currentTimeMillis() > tokenExpireTime) {
                    log.info("Token未初始化或已过期，自动刷新Token");
                    token = login().block();
                    if (token == null) {
                        throw new RuntimeException("Token刷新失败");
                    }
                }
                return token;
            }
        }).flatMap(t -> requestSupplier.get())
                .timeout(Duration.ofSeconds(30))
                .onErrorResume(e -> {
                    log.error("请求失败，返回空响应: {}", e.getMessage());
                    return Mono.empty();
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
