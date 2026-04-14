package org.jeecg.modules.youcai.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.youcai.dto.AnalysisRequestDTO;
import org.jeecg.modules.youcai.entity.YoucaiPestControl;
import org.jeecg.modules.youcai.entity.iotEntity.ApiResponse;
import org.jeecg.modules.youcai.mapper.YoucaiPestControlMapper;
import org.jeecg.modules.youcai.service.IYoucaiPestControlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.jeecg.modules.youcai.util.IoTApiUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.*;

/**
 * @Description: 虫害防控表
 * @Author: jeecg-boot
 * @Date: 2025-10-18
 * @Version: V1.0
 */
@Slf4j
@Service
public class YoucaiPestControlServiceImpl extends ServiceImpl<YoucaiPestControlMapper, YoucaiPestControl>
        implements IYoucaiPestControlService {
    @Autowired
    private IoTApiUtil ioTApiUtil;
    @Autowired
    private YoucaiPestControlMapper youcaiPestControlMapper;

    public Mono<List<Map<String, Object>>> getPestImages() {
        String deviceCode = "860048073163923";
        Integer countType = 1;
        LocalDate EndDate = LocalDate.now();
        LocalDate StartDate = EndDate.minusDays(100);
        String startDate = StartDate.toString();
        String endDate = EndDate.toString();
        return ioTApiUtil.getPestPhotos(deviceCode, countType, startDate, endDate)
                .map(response -> {
                    // 假设 response.getData() 返回 Map
                    Map<String, Object> data = (Map<String, Object>) response.getData();
                    if (data == null || !data.containsKey("imageDetailList")) {
                        return Collections.emptyList();
                    }
                    List<Map<String, Object>> imageDetailList = (List<Map<String, Object>>) data.get("imageDetailList");
                    List<Map<String, Object>> parsedList = new ArrayList<>();

                    for (Map<String, Object> record : imageDetailList) {
                        Map<String, Object> q = (Map<String, Object>) record.get("q");
                        List<Map<String, Object>> insectDetails = (List<Map<String, Object>>) record
                                .get("imageDetailList");
                        // insects = { name: count }
                        Map<String, Object> insects = new HashMap<>();
                        if (insectDetails != null) {
                            for (Map<String, Object> insect : insectDetails) {
                                String name = (String) insect.get("name");
                                Object count = insect.get("count");
                                if (name != null) {
                                    insects.put(name, count);
                                }
                            }
                        }
                        Map<String, Object> parsed = new HashMap<>();
                        parsed.put("dateCreated", q != null ? q.get("dateCreated") : null);
                        parsed.put("analysis_time", q != null ? q.get("analysisTime") : null);
                        parsed.put("image_url", q != null ? q.get("url") : null);
                        parsed.put("thumbnail", q != null ? q.get("thumbnail") : null);
                        parsed.put("total_count", record.getOrDefault("countSum", 0));
                        parsed.put("species_count", record.getOrDefault("harmTypeCount", 0));
                        parsed.put("insects", insects);
                        parsedList.add(parsed);
                    }
                    return parsedList;
                });
    }

    private static final String API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
    private static final String API_KEY = "sk-5f775477a8c04db4893d8a39d308e151";

    @Override
    public String aiAnalysis(AnalysisRequestDTO req) throws Exception {
        if (req.getPestData() == null || req.getPestData().isEmpty()) {
            throw new Exception("pest_data 不能为空");
        }
        // ---- 构建 Prompt ----
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是农业专家。请基于最近十天的虫情数据给出具体的防治建议：\n")
                .append("1. 各害虫趋势分析\n")
                .append("2. 防治建议与最佳时期\n")
                .append("3. 推荐适用农药及剂量\n")
                .append("4. 请给出一份非常简短的防治建议\n\n");

        prompt.append("虫情数据：\n");
        for (AnalysisRequestDTO.PestItem item : req.getPestData()) {
            prompt.append("时间：").append(item.getAnalysisTime()).append("\n");
            item.getInsects().forEach((name, count) -> {
                prompt.append("  ").append(name).append(": ").append(count).append("只\n");
            });
            prompt.append("\n");
        }

        // ---- 构建请求体 ----
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "qwen-turbo");
        requestBody.put("input", new JSONObject() {
            {
                put("prompt", prompt.toString());
            }
        });
        requestBody.put("parameters", new JSONObject() {
            {
                put("temperature", 0.3);
                put("top_p", 0.9);
            }
        });

        WebClient client = WebClient.builder().build();

        String result = client.post()
                .uri(API_URL)
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody.toJSONString())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        JSONObject json = JSON.parseObject(result);
        return json.getJSONObject("output").getString("text");
    }

    public Mono<List<Map<String, Object>>> getAllPestImages(String startDate, String endDate) {
        // 固定设备码和 countType
        String deviceCode = "860048073163923";
        Integer countType = 1;
        return ioTApiUtil.getPestPhotos(deviceCode, countType, startDate, endDate)
                .map(response -> {
                    Map<String, Object> data = (Map<String, Object>) response.getData();
                    if (data == null || !data.containsKey("imageDetailList")) {
                        return Collections.emptyList();
                    }
                    List<Map<String, Object>> imageDetailList = (List<Map<String, Object>>) data.get("imageDetailList");
                    List<Map<String, Object>> parsedList = new ArrayList<>();

                    for (Map<String, Object> record : imageDetailList) {
                        Map<String, Object> q = (Map<String, Object>) record.get("q");
                        List<Map<String, Object>> insectDetails = (List<Map<String, Object>>) record
                                .get("imageDetailList");
                        Map<String, Object> insects = new HashMap<>();
                        if (insectDetails != null) {
                            for (Map<String, Object> insect : insectDetails) {
                                String name = (String) insect.get("name");
                                Object count = insect.get("count");
                                if (name != null) {
                                    insects.put(name, count);
                                }
                            }
                        }

                        Map<String, Object> parsed = new HashMap<>();
                        parsed.put("dateCreated", q != null ? q.get("dateCreated") : null);
                        parsed.put("analysis_time", q != null ? q.get("analysisTime") : null);
                        parsed.put("image_url", q != null ? q.get("url") : null);
                        parsed.put("thumbnail", q != null ? q.get("thumbnail") : null);
                        parsed.put("total_count", record.getOrDefault("countSum", 0));
                        parsed.put("species_count", record.getOrDefault("harmTypeCount", 0));
                        parsed.put("insects", insects);
                        parsedList.add(parsed);
                    }
                    return parsedList;
                });
    }

    @Override
    public List<YoucaiPestControl> findControl(String plotId, String start, String end) {
        return youcaiPestControlMapper.queryControlHistory(plotId, start, end);
    }

}
