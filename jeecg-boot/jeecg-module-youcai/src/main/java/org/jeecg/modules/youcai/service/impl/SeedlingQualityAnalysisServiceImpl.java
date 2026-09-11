package org.jeecg.modules.youcai.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.youcai.dto.DiseaseAnalysisRequestDTO;
import org.jeecg.modules.youcai.dto.SeedlingAnalysisResponseDTO;
import org.jeecg.modules.youcai.service.IDashScopeMultiModalService;
import org.jeecg.modules.youcai.service.ISeedlingQualityAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SeedlingQualityAnalysisServiceImpl implements ISeedlingQualityAnalysisService {

    @Autowired
    private IDashScopeMultiModalService dashScopeMultiModalService;

    @Override
    public SeedlingAnalysisResponseDTO analyzeSeedling(DiseaseAnalysisRequestDTO request) {
        List<String> imageUrls = request.getImageUrls() == null ? Collections.emptyList() : request.getImageUrls().stream()
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
        if (imageUrls.isEmpty()) {
            throw new IllegalArgumentException("监控截图URL列表不能为空");
        }

        String llmResponse = dashScopeMultiModalService.analyzeImages(
                imageUrls,
                buildSeedlingPrompt(request.getBaseName(), imageUrls.size())
        );

        SeedlingAnalysisResponseDTO response = new SeedlingAnalysisResponseDTO();
        response.setAnalysisTime(new Date());
        parseSeedlingResponse(llmResponse, response);
        return response;
    }

    private String buildSeedlingPrompt(String baseName, int imageCount) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一名油菜苗情评估专家。");
        prompt.append("请仅基于用户提供的").append(imageCount).append("张监控截图，对油菜苗情进行综合分析。");
        if (StringUtils.hasText(baseName)) {
            prompt.append("基地名称：").append(baseName).append("。");
        }
        prompt.append("分析时只关注能从图像中直接观察到的苗情信息，不要讨论拍摄设备、天气、背景、病虫害防治方案等无关内容。");
        prompt.append("重点评估以下苗情指标：生育期、植株高度、植株密度、整齐度、叶片数/叶龄、叶色与叶片活力、株型长势、缺苗断垄或黄化冻害等异常、根颈粗壮度。");
        prompt.append("如果某项从当前监控截图无法可靠判断，请明确返回“无法判断”，不要臆测。");
        prompt.append("请优先判断当前群体属于壮苗、正常、偏弱、旺长还是未知，并明确给出生育期判断、植株高度估计和植株密度判断，再给出简洁、可执行的田间管理建议。");
        prompt.append("请严格输出 JSON，不要输出 Markdown，不要输出代码块。");
        prompt.append("JSON 字段要求如下：");
        prompt.append("seedlingLevel(壮苗/正常/偏弱/旺长/未知)，");
        prompt.append("growthStage(必须输出当前生育期，苗期/越冬前/蕾薹初期/抽薹期/现蕾期/未知)，");
        prompt.append("confidence(0到1之间的小数)，");
        prompt.append("summary(综合总结)，");
        prompt.append("evidence(主要判断依据)，");
        prompt.append("indicators(数组，固定输出9项，依次为植株高度、植株密度、整齐度、叶片数/叶龄、生育期判断、叶色与叶片活力、株型长势、异常苗情、根颈粗壮度；");
        prompt.append("其中植株高度的value尽量输出估计高度范围或厘米数，植株密度的value输出稀疏/适中/偏密或类似判断；");
        prompt.append("每项包含name、value、level、description)，");
        prompt.append("mainProblems(字符串数组，没有问题返回空数组)，");
        prompt.append("managementSuggestions(字符串数组，给出3到5条建议)。");
        return prompt.toString();
    }

    private void parseSeedlingResponse(String llmResponse, SeedlingAnalysisResponseDTO response) {
        try {
            String jsonStr = extractJson(llmResponse);
            JSONObject json = JSON.parseObject(jsonStr);
            response.setSeedlingLevel(defaultText(json.getString("seedlingLevel"), "未知"));
            response.setGrowthStage(defaultText(json.getString("growthStage"), "未知"));
            response.setConfidence(json.getDouble("confidence"));
            response.setSummary(defaultText(json.getString("summary"), "暂无综合结论"));
            response.setEvidence(defaultText(json.getString("evidence"), "暂无判断依据"));

            JSONArray indicators = json.getJSONArray("indicators");
            if (indicators != null) {
                response.setIndicators(indicators.toJavaList(SeedlingAnalysisResponseDTO.SeedlingIndicator.class));
            } else {
                response.setIndicators(Collections.emptyList());
            }

            JSONArray mainProblems = json.getJSONArray("mainProblems");
            response.setMainProblems(mainProblems != null ? mainProblems.toJavaList(String.class) : Collections.emptyList());

            JSONArray suggestions = json.getJSONArray("managementSuggestions");
            response.setManagementSuggestions(suggestions != null ? suggestions.toJavaList(String.class) : Collections.emptyList());
        } catch (Exception e) {
            log.error("解析苗情分析响应失败", e);
            response.setSeedlingLevel("未知");
            response.setGrowthStage("未知");
            response.setSummary("模型返回结果解析失败");
            response.setEvidence(llmResponse);
            response.setIndicators(Collections.emptyList());
            response.setMainProblems(Collections.emptyList());
            response.setManagementSuggestions(Collections.emptyList());
        }
    }

    private String extractJson(String text) {
        if (!StringUtils.hasText(text)) {
            return "{}";
        }
        int start = text.indexOf("{");
        int end = text.lastIndexOf("}");
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    private String defaultText(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }
}
