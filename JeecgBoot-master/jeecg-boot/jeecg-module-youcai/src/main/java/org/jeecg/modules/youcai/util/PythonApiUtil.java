package org.jeecg.modules.youcai.util;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.youcai.config.PythonServiceConfig;
import org.jeecg.modules.youcai.dto.LodgingRiskAssessmentRequestDTO;
import org.jeecg.modules.youcai.dto.LodgingRiskAssessmentResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Python服务API调用工具类
 */
@Component
@Slf4j
public class PythonApiUtil {

    private final WebClient webClient;
    private final PythonServiceConfig pythonServiceConfig;

    @Autowired
    public PythonApiUtil(WebClient.Builder webClientBuilder, PythonServiceConfig pythonServiceConfig) {
        this.webClient = webClientBuilder
                .defaultHeader("Content-Type", "application/json")
                .build();
        this.pythonServiceConfig = pythonServiceConfig;
    }

    /**
     * 调用Python服务进行倒伏风险评估
     * @param requestDTO 评估请求参数
     * @return 评估结果
     */
    public Mono<LodgingRiskAssessmentResponseDTO> assessLodgingRisk(LodgingRiskAssessmentRequestDTO requestDTO) {
        if (!pythonServiceConfig.getEnabled()) {
            log.warn("Python服务未启用，跳过倒伏风险评估");
            return Mono.just(new LodgingRiskAssessmentResponseDTO());
        }

        String pythonServiceUrl = pythonServiceConfig.getUrl();
        if (pythonServiceUrl == null || pythonServiceUrl.isEmpty()) {
            log.error("Python服务URL未配置");
            return Mono.just(new LodgingRiskAssessmentResponseDTO());
        }

        String apiUrl = pythonServiceUrl + "/api/lodging-risk/comprehensive-assessment";
        
        log.info("调用Python服务进行倒伏风险评估，URL: {}", apiUrl);
        log.info("请求参数: {}", JSONObject.toJSONString(requestDTO));
        
        return webClient.post()
                .uri(apiUrl)
                .header("X-API-Key", pythonServiceConfig.getApiKey())
                .body(BodyInserters.fromValue(requestDTO))
                .retrieve()
                .onStatus(
                    status -> status.is4xxClientError(),
                    response -> response.bodyToMono(String.class)
                        .flatMap(body -> {
                            log.error("Python服务返回4xx错误，状态码: {}, 响应体: {}", response.statusCode(), body);
                            return Mono.error(new RuntimeException("Python服务调用失败: " + response.statusCode() + ", 响应: " + body));
                        })
                )
                .onStatus(
                    status -> status.is5xxServerError(),
                    response -> response.bodyToMono(String.class)
                        .flatMap(body -> {
                            log.error("Python服务返回5xx错误，状态码: {}, 响应体: {}", response.statusCode(), body);
                            return Mono.error(new RuntimeException("Python服务内部错误: " + response.statusCode() + ", 响应: " + body));
                        })
                )
                .bodyToMono(String.class) // 首先获取原始响应字符串
                .flatMap(responseBody -> {
                    log.info("Python服务原始响应: {}", responseBody);
                    try {
                        // 手动解析JSON并转换为Result<LodgingRiskAssessmentResponseDTO>
                        Result<LodgingRiskAssessmentResponseDTO> result = JSONObject.parseObject(responseBody, 
                            new com.alibaba.fastjson.TypeReference<Result<LodgingRiskAssessmentResponseDTO>>() {});
                        
                        // 检查Result中的success字段
                        if (result.isSuccess()) {
                            // 如果成功，检查result字段是否为null
                            if (result.getResult() != null) {
                                // 返回result字段中的数据
                                return Mono.just(result.getResult());
                            } else {
                                // 如果result字段为null，记录警告并返回空对象
                                log.warn("Python服务返回成功但result字段为null");
                                return Mono.just(new LodgingRiskAssessmentResponseDTO());
                            }
                        } else {
                            // 如果失败，返回错误信息
                            log.error("Python服务返回失败，错误信息: {}", result.getMessage());
                            return Mono.error(new RuntimeException("Python服务调用失败: " + result.getMessage()));
                        }
                    } catch (Exception e) {
                        log.error("解析Python服务响应失败: {}", e.getMessage(), e);
                        return Mono.error(new RuntimeException("解析Python服务响应失败: " + e.getMessage()));
                    }
                })
                .doOnSuccess(response -> {
                    log.info("Python服务调用成功");
                    String responseJson = JSONObject.toJSONString(response);
                    log.info("返回结果: {}", responseJson);
                    
                    // 检查关键字段是否为空
                    if (response.getCurrentRisk() == null) {
                        log.warn("当前风险评估为空");
                    } else {
                        log.info("当前风险 - 风险评分: {}, 风险等级: {}", 
                                response.getCurrentRisk().getRiskScore(), 
                                response.getCurrentRisk().getRiskLevel());
                    }
                    
                    if (response.getForecast7Days() == null) {
                        log.warn("未来7天预测为空");
                    } else if (response.getForecast7Days().getDailyRisks() == null || response.getForecast7Days().getDailyRisks().isEmpty()) {
                        log.warn("每日风险预测为空");
                    } else {
                        log.info("每日风险预测条数: {}", response.getForecast7Days().getDailyRisks().size());
                    }
                    
                    if (response.getComprehensiveSuggestions() == null) {
                        log.warn("综合建议为空");
                    } else {
                        log.info("综合建议 - 即时建议数: {}, 短期建议数: {}, 中期建议数: {}, 长期建议数: {}", 
                                response.getComprehensiveSuggestions().getImmediate() != null ? response.getComprehensiveSuggestions().getImmediate().size() : 0,
                                response.getComprehensiveSuggestions().getShortTerm() != null ? response.getComprehensiveSuggestions().getShortTerm().size() : 0,
                                response.getComprehensiveSuggestions().getMediumTerm() != null ? response.getComprehensiveSuggestions().getMediumTerm().size() : 0,
                                response.getComprehensiveSuggestions().getLongTerm() != null ? response.getComprehensiveSuggestions().getLongTerm().size() : 0);
                    }
                })
                .doOnError(error -> log.warn("调用Python服务失败: {}", error.getMessage(), error))
                .onErrorReturn(new LodgingRiskAssessmentResponseDTO());
    }
}