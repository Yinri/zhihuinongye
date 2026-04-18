package org.jeecg.modules.youcai.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.youcai.dto.AnalysisRequestDTO;
import org.jeecg.modules.youcai.entity.YoucaiPestControl;
import org.jeecg.modules.youcai.entity.YoucaiSensorInfo;
import org.jeecg.modules.youcai.mapper.YoucaiPestControlMapper;
import org.jeecg.modules.youcai.service.IYoucaiPestControlService;
import org.jeecg.modules.youcai.service.IYoucaiSensorInfoService;
import org.jeecg.modules.youcai.util.IoTApiUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private IYoucaiSensorInfoService youcaiSensorInfoService;

    private static final String API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";

    @Value("${dashscope.api-key:${DASHSCOPE_API_KEY:}}")
    private String dashscopeApiKey;

    @Override
    public Mono<List<Map<String, Object>>> getPestImages(String baseName, String startDate, String endDate) {
        return getAllPestImages(baseName, startDate, endDate)
                .map(records -> records.isEmpty() ? Collections.emptyList() : Collections.singletonList(records.get(0)));
    }

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
                .header("Authorization", "Bearer " + dashscopeApiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody.toJSONString())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        JSONObject json = JSON.parseObject(result);
        return json.getJSONObject("output").getString("text");
    }

    @Override
    public Mono<List<Map<String, Object>>> getAllPestImages(String baseName, String startDate, String endDate) {
        String deviceCode = resolvePestDeviceCode(baseName);
        if (!StringUtils.hasText(deviceCode)) {
            return Mono.just(Collections.emptyList());
        }

        Integer countType = 1;
        return ioTApiUtil.getPestPhotos(deviceCode, countType, startDate, endDate)
                .map(response -> parsePestImageList(response != null ? response.getData() : null));
    }

    @Override
    public List<YoucaiPestControl> findControl(String plotId, String start, String end) {
        return youcaiPestControlMapper.queryControlHistory(plotId, start, end);
    }

    /**
     * 根据基地名称匹配“性诱测报灯”传感器，并返回其 DeviceCode。
     */
    private String resolvePestDeviceCode(String baseName) {
        if (!StringUtils.hasText(baseName)) {
            return null;
        }

        String baseNamePrefix = normalizeBaseName(baseName);
        LambdaQueryWrapper<YoucaiSensorInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(YoucaiSensorInfo::getIsDelete, 0)
                .likeRight(YoucaiSensorInfo::getSensorName, baseNamePrefix)
                .like(YoucaiSensorInfo::getSensorName, "虫情测报灯")
                .last("limit 1");

        YoucaiSensorInfo sensor = youcaiSensorInfoService.getOne(queryWrapper, false);
        return sensor != null ? sensor.getSensorSerial() : null;
    }

    /**
     * 将 IoT 返回的复杂结构压平为前端直接可展示的数据结构。
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parsePestImageList(Object data) {
        if (!(data instanceof Map)) {
            return Collections.emptyList();
        }

        Map<String, Object> dataMap = (Map<String, Object>) data;
        Object rawList = dataMap.get("imageDetailList");
        if (!(rawList instanceof List)) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> parsedList = new ArrayList<>();
        for (Object item : (List<?>) rawList) {
            if (!(item instanceof Map)) {
                continue;
            }

            Map<String, Object> record = (Map<String, Object>) item;
            Map<String, Object> q = record.get("q") instanceof Map ? (Map<String, Object>) record.get("q") : Collections.emptyMap();
            List<Map<String, Object>> insectDetails = record.get("imageDetailList") instanceof List
                    ? (List<Map<String, Object>>) record.get("imageDetailList")
                    : Collections.emptyList();

            Map<String, Object> insects = new LinkedHashMap<>();
            for (Map<String, Object> insect : insectDetails) {
                String name = insect.get("name") != null ? String.valueOf(insect.get("name")) : null;
                if (StringUtils.hasText(name)) {
                    insects.put(name, insect.getOrDefault("count", 0));
                }
            }

            Map<String, Object> parsed = new HashMap<>();
            parsed.put("dateCreated", q.get("dateCreated"));
            parsed.put("analysis_time", q.get("analysisTime"));
            parsed.put("image_url", q.get("url"));
            parsed.put("thumbnail", q.get("thumbnail"));
            parsed.put("sthumbnail", q.get("sthumbnail"));
            parsed.put("total_count", record.getOrDefault("countSum", 0));
            parsed.put("species_count", record.getOrDefault("harmTypeCount", 0));
            parsed.put("insects", insects);
            parsedList.add(parsed);
        }

        // 将最新一条记录放在最前面，便于首页直接展示最近图片和统计信息。
        parsedList.sort(Comparator.comparing(item -> String.valueOf(item.getOrDefault("analysis_time", "")), Comparator.reverseOrder()));
        return parsedList;
    }

    private String normalizeBaseName(String baseName) {
        return baseName.endsWith("基地") ? baseName.substring(0, baseName.length() - 2) : baseName;
    }

}
