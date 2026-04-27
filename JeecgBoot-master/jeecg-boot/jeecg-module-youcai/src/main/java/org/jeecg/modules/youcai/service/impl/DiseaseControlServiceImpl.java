package org.jeecg.modules.youcai.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.youcai.dto.DiseaseAnalysisRequestDTO;
import org.jeecg.modules.youcai.dto.DiseaseAnalysisResponseDTO;
import org.jeecg.modules.youcai.dto.SporeAnalysisResponseDTO;
import org.jeecg.modules.youcai.service.IDashScopeMultiModalService;
import org.jeecg.modules.youcai.entity.YoucaiSensorInfo;
import org.jeecg.modules.youcai.entity.iotEntity.ApiResponse;
import org.jeecg.modules.youcai.service.IDiseaseControlService;
import org.jeecg.modules.youcai.service.IVideoSnapshotService;
import org.jeecg.modules.youcai.service.IYoucaiSensorInfoService;
import org.jeecg.modules.youcai.util.IoTApiUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DiseaseControlServiceImpl implements IDiseaseControlService {

    @Autowired
    private IoTApiUtil ioTApiUtil;

    @Autowired
    private IYoucaiSensorInfoService youcaiSensorInfoService;

    @Autowired
    private IVideoSnapshotService videoSnapshotService;

    @Autowired
    private IDashScopeMultiModalService dashScopeMultiModalService;

    @Override
    public List<Map<String, Object>> getMonitorImagesByBaseId(String baseId) {
        if (!StringUtils.hasText(baseId)) {
            return Collections.emptyList();
        }

        ResultWrapper videoDevicesWrapper = getVideoDevices(baseId);
        if (!videoDevicesWrapper.success) {
            throw new IllegalStateException(videoDevicesWrapper.message);
        }

        long startTime = System.currentTimeMillis();
        List<Map<String, Object>> images = videoDevicesWrapper.devices.parallelStream()
                .map(this::buildMonitorImage)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        log.info("基地 {} 获取监控截图完成，设备数: {}, 截图数: {}, 耗时: {}ms",
                baseId, videoDevicesWrapper.devices.size(), images.size(), System.currentTimeMillis() - startTime);
        return images;
    }

    @Override
    public List<Map<String, Object>> getSporeImagesByBaseName(String baseName) {
        String deviceCode = resolveSporeDeviceCode(baseName);
        if (!StringUtils.hasText(deviceCode)) {
            return Collections.emptyList();
        }

        ApiResponse response = ioTApiUtil.getDiseasePhotos(deviceCode, 0, defaultStartDate(), defaultEndDate()).block();
        if (response != null && response.getCode() == 1 && response.getData() != null) {
            return parseImageList(response.getData());
        }
        return Collections.emptyList();
    }

    @Override
    public DiseaseAnalysisResponseDTO analyzeMonitorBatchByBaseId(String baseId) {
        List<Map<String, Object>> imageList = getMonitorImagesByBaseId(baseId);
        if (imageList.isEmpty()) {
            throw new IllegalStateException("未找到监控截图");
        }
        return analyzeDiseaseByImageUrls(extractImageUrls(imageList), "监控截图", null);
    }

    @Override
    public DiseaseAnalysisResponseDTO analyzeDisease(DiseaseAnalysisRequestDTO request) {
        if (request == null || request.getImageUrls() == null || request.getImageUrls().isEmpty()) {
            throw new IllegalArgumentException("图片URL列表不能为空");
        }
        return analyzeDiseaseByImageUrls(request.getImageUrls(), "病害图片", request.getBaseName());
    }

    @Override
    public SporeAnalysisResponseDTO analyzeSpore(DiseaseAnalysisRequestDTO request) {
        if (request == null || request.getImageUrls() == null || request.getImageUrls().isEmpty()) {
            throw new IllegalArgumentException("孢子图片URL列表不能为空");
        }
        List<String> imageUrls = request.getImageUrls().stream()
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
        if (imageUrls.isEmpty()) {
            throw new IllegalArgumentException("孢子图片URL列表不能为空");
        }
        String llmResponse = dashScopeMultiModalService.analyzeImages(imageUrls,
                buildSporeMultiModalPrompt(request.getBaseName()));
        SporeAnalysisResponseDTO response = new SporeAnalysisResponseDTO();
        parseSporeLLMResponse(llmResponse, response);

        SporeAnalysisResponseDTO.SporeStatistics statistics = new SporeAnalysisResponseDTO.SporeStatistics();
        statistics.setTotalImages(imageUrls.size());
        statistics.setAnalyzedImages(imageUrls.size());
        statistics.setTimeRange(getDefaultTimeRange());
        response.setStatistics(statistics);
        return response;
    }

    private Map<String, Object> buildMonitorImage(Map<String, Object> device) {
        Object videoId = device.get("id");
        if (videoId == null) {
            return null;
        }

        try {
            String imageUrl = videoSnapshotService.fetchVideoPhoto(String.valueOf(videoId));
            if (!StringUtils.hasText(imageUrl)) {
                return null;
            }

            Map<String, Object> image = new HashMap<>();
            image.put("imageUrl", imageUrl);
            image.put("thumbnail", imageUrl);
            image.put("dateCreated", new Date());
            image.put("deviceName", device.get("equipmentName"));
            image.put("videoId", String.valueOf(videoId));
            return image;
        } catch (Exception e) {
            log.warn("获取视频截图失败，videoId={}, error={}", videoId, e.getMessage());
            return null;
        }
    }

    private DiseaseAnalysisResponseDTO analyzeDiseaseByImageUrls(List<String> imageUrls, String imageType, String baseName) {
        List<String> validImageUrls = imageUrls == null ? Collections.emptyList() : imageUrls.stream()
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
        if (validImageUrls.isEmpty()) {
            throw new IllegalArgumentException("图片URL列表不能为空");
        }

        DiseaseAnalysisResponseDTO response = new DiseaseAnalysisResponseDTO();
        response.setAnalysisTime(new Date());
        String llmResponse = dashScopeMultiModalService.analyzeImages(validImageUrls,
                buildDiseaseMultiModalPrompt(imageType, baseName, validImageUrls.size()));
        parseDiseaseAnalysisResponse(llmResponse, response);
        return response;
    }

    private List<String> extractImageUrls(List<Map<String, Object>> imageList) {
        if (imageList == null || imageList.isEmpty()) {
            return Collections.emptyList();
        }
        return imageList.stream()
                .map(item -> item == null || item.get("imageUrl") == null ? null : String.valueOf(item.get("imageUrl")))
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
    }

    private ResultWrapper getVideoDevices(String baseId) {
        org.jeecg.common.api.vo.Result<List<Map<String, Object>>> result = youcaiSensorInfoService.getVideoDevicesByBaseId(baseId);
        if (result == null) {
            return ResultWrapper.fail("获取视频设备列表失败");
        }
        if (!result.isSuccess()) {
            return ResultWrapper.fail(result.getMessage() != null ? result.getMessage() : "获取视频设备列表失败");
        }
        return ResultWrapper.success(result.getResult());
    }

    private String resolveSporeDeviceCode(String baseName) {
        if (!StringUtils.hasText(baseName)) {
            return null;
        }

        String baseNamePrefix = extractSensorNamePrefix(baseName);

        LambdaQueryWrapper<YoucaiSensorInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(YoucaiSensorInfo::getSensorTypeId, 4)
                .eq(YoucaiSensorInfo::getIsDelete, 0)
                .likeRight(YoucaiSensorInfo::getSensorName, baseNamePrefix)
                .last("limit 1");

        YoucaiSensorInfo sensor = youcaiSensorInfoService.getOne(queryWrapper, false);
        return sensor != null ? sensor.getSensorSerial() : null;
    }

    private String extractSensorNamePrefix(String baseName) {
        String normalized = baseName.trim();
        String[] separators = {"镇", "乡", "街道"};
        for (String separator : separators) {
            int index = normalized.indexOf(separator);
            if (index > 0) {
                return normalized.substring(0, index);
            }
        }
        return normalized;
    }

    private String buildDiseaseMultiModalPrompt(String imageType, String baseName, int imageCount) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一名油菜病害识别与田间防控专家。");
        prompt.append("请仅基于用户提供的").append(imageCount).append("张").append(imageType).append("图片，完成油菜病害识别和防控分析。");
        if (StringUtils.hasText(baseName)) {
            prompt.append("基地名称：").append(baseName).append("。");
        }
        prompt.append("优先在油菜常见病害范围内判断，如霜霉病、菌核病、黑胫病、病毒病、软腐类症状等；");
        prompt.append("若证据不足，不要强行命名病害，可返回未知。");
        prompt.append("分析时只关注叶片、叶柄、茎秆、花序、角果等油菜植株部位是否存在病斑、霉层、腐烂、萎蔫、失绿、畸形、坏死等可见病害症状。");
        prompt.append("不要讨论拍摄背景、设备情况、天气、虫害、施肥、长势评价或与病害无关的信息。");
        prompt.append("如果图片中没有明显病害症状，可判定为健康。");
        prompt.append("如果画面模糊、遮挡严重、主体不是油菜植株，或仅能看到土壤、设施、远景，请明确返回未知，不要编造病名或防治结论。");
        prompt.append("description 字段只描述识别到的油菜病害症状与判断依据，不要写泛泛农技套话。");
        prompt.append("preventionMeasures、recommendedPesticides、bestPreventionTime、summary 字段只输出与当前识别病害直接相关的油菜防控建议。");
        prompt.append("若判定为健康或未知，recommendedPesticides 返回空数组，preventionMeasures 只保留巡查、复核或预防性管理建议。");
        prompt.append("请严格输出 JSON，不要输出 Markdown，不要输出代码块。");
        prompt.append("JSON 字段要求如下：");
        prompt.append("diseaseName(病害名称/健康/未知)，confidence(0到1之间的小数)，description(病害症状描述与判断依据)，");
        prompt.append("severity(轻度/中度/重度)，preventionMeasures(字符串数组)，");
        prompt.append("recommendedPesticides(数组，每项包含name、dosage、usage、precautions)，");
        prompt.append("bestPreventionTime(最佳防治时期)，summary(综合总结)。");
        return prompt.toString();
    }

    private String buildSporeMultiModalPrompt(String baseName) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一名油菜病害早期预警专家。请仅结合多张孢子捕捉仪图片，对油菜病害风险进行识别和防控预警。");
        if (StringUtils.hasText(baseName)) {
            prompt.append("基地名称：").append(baseName).append("。");
        }
        prompt.append("只关注与油菜病害发生相关的孢子风险，优先判断是否提示菌核病、霜霉病等油菜常见病害风险。");
        prompt.append("不要输出与病害无关的背景描述、设备描述、虫害信息或泛化农事建议。");
        prompt.append("diseaseWarnings、medicationGuides、summary 字段只保留油菜病害风险结论和对应防控建议。");
        prompt.append("如果从孢子图中无法支持明确病害，仅返回低风险或中风险并说明需继续监测，不要虚构高风险病名。");
        prompt.append("请严格输出 JSON，不要输出 Markdown，不要输出代码块。");
        prompt.append("JSON 字段要求如下：");
        prompt.append("warningLevel(低/中/高)，");
        prompt.append("diseaseWarnings(数组，每项包含diseaseName、riskLevel、description、suggestion)，");
        prompt.append("medicationGuides(数组，每项包含pesticideName、dosage、timing、method、precautions)，");
        prompt.append("summary(综合总结)。");
        return prompt.toString();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseImageList(Object data) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (data instanceof Map) {
            Map<String, Object> dataMap = (Map<String, Object>) data;
            Object imageDetailList = dataMap.get("imageDetailList");
            if (imageDetailList instanceof List) {
                for (Object item : (List<?>) imageDetailList) {
                    if (item instanceof Map) {
                        Map<String, Object> itemMap = (Map<String, Object>) item;
                        Object q = itemMap.get("q");
                        if (q instanceof Map) {
                            Map<String, Object> qMap = (Map<String, Object>) q;
                            Map<String, Object> image = new HashMap<>();
                            image.put("imageUrl", qMap.get("url"));
                            image.put("thumbnail", qMap.get("thumbnail"));
                            image.put("dateCreated", qMap.get("dateCreated"));
                            result.add(image);
                        }
                    }
                }
            }
        }
        return result;
    }

    private void parseDiseaseAnalysisResponse(String llmResponse, DiseaseAnalysisResponseDTO response) {
        try {
            String jsonStr = extractJson(llmResponse);
            JSONObject json = JSON.parseObject(jsonStr);

            response.setDiseaseName(json.getString("diseaseName"));
            response.setConfidence(json.getDouble("confidence"));
            response.setDescription(json.getString("description"));
            response.setSeverity(json.getString("severity"));
            response.setBestPreventionTime(json.getString("bestPreventionTime"));
            response.setSummary(json.getString("summary"));

            if (json.containsKey("preventionMeasures")) {
                JSONArray measures = json.getJSONArray("preventionMeasures");
                response.setPreventionMeasures(measures.toJavaList(String.class));
            }

            if (json.containsKey("recommendedPesticides")) {
                JSONArray pesticides = json.getJSONArray("recommendedPesticides");
                List<DiseaseAnalysisResponseDTO.PesticideRecommendation> pesticideList = new ArrayList<>();
                for (int i = 0; i < pesticides.size(); i++) {
                    JSONObject p = pesticides.getJSONObject(i);
                    DiseaseAnalysisResponseDTO.PesticideRecommendation pr = new DiseaseAnalysisResponseDTO.PesticideRecommendation();
                    pr.setName(p.getString("name"));
                    pr.setDosage(p.getString("dosage"));
                    pr.setUsage(p.getString("usage"));
                    pr.setPrecautions(p.getString("precautions"));
                    pesticideList.add(pr);
                }
                response.setRecommendedPesticides(pesticideList);
            }
        } catch (Exception e) {
            log.error("解析病害分析响应失败: {}", e.getMessage(), e);
            response.setDiseaseName("解析失败");
            response.setDescription(llmResponse);
        }
    }

    private void parseSporeLLMResponse(String llmResponse, SporeAnalysisResponseDTO response) {
        if (llmResponse == null || llmResponse.isEmpty()) {
            response.setWarningLevel("低");
            response.setSummary("暂无分析结果");
            return;
        }
        try {
            String jsonStr = extractJson(llmResponse);
            JSONObject json = JSON.parseObject(jsonStr);

            response.setWarningLevel(json.getString("warningLevel"));
            response.setSummary(json.getString("summary"));

            if (json.containsKey("diseaseWarnings")) {
                JSONArray warnings = json.getJSONArray("diseaseWarnings");
                List<SporeAnalysisResponseDTO.DiseaseWarning> warningList = new ArrayList<>();
                for (int i = 0; i < warnings.size(); i++) {
                    JSONObject w = warnings.getJSONObject(i);
                    SporeAnalysisResponseDTO.DiseaseWarning dw = new SporeAnalysisResponseDTO.DiseaseWarning();
                    dw.setDiseaseName(w.getString("diseaseName"));
                    dw.setRiskLevel(w.getString("riskLevel"));
                    dw.setDescription(w.getString("description"));
                    dw.setSuggestion(w.getString("suggestion"));
                    warningList.add(dw);
                }
                response.setDiseaseWarnings(warningList);
            }

            if (json.containsKey("medicationGuides")) {
                JSONArray guides = json.getJSONArray("medicationGuides");
                List<SporeAnalysisResponseDTO.MedicationGuide> guideList = new ArrayList<>();
                for (int i = 0; i < guides.size(); i++) {
                    JSONObject g = guides.getJSONObject(i);
                    SporeAnalysisResponseDTO.MedicationGuide mg = new SporeAnalysisResponseDTO.MedicationGuide();
                    mg.setPesticideName(g.getString("pesticideName"));
                    mg.setDosage(g.getString("dosage"));
                    mg.setTiming(g.getString("timing"));
                    mg.setMethod(g.getString("method"));
                    mg.setPrecautions(g.getString("precautions"));
                    guideList.add(mg);
                }
                response.setMedicationGuides(guideList);
            }
        } catch (Exception e) {
            log.error("解析孢子LLM响应失败", e);
            response.setWarningLevel("低");
            response.setSummary(llmResponse);
        }
    }

    private String extractJson(String text) {
        if (text == null) {
            return "{}";
        }
        int start = text.indexOf("{");
        int end = text.lastIndexOf("}");
        if (start != -1 && end != -1 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    private String defaultStartDate() {
        return LocalDate.now().minusDays(7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    private String defaultEndDate() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    private String getDefaultTimeRange() {
        return defaultStartDate() + " 至 " + defaultEndDate();
    }

    private static class ResultWrapper {
        private final boolean success;
        private final String message;
        private final List<Map<String, Object>> devices;

        private ResultWrapper(boolean success, String message, List<Map<String, Object>> devices) {
            this.success = success;
            this.message = message;
            this.devices = devices != null ? devices : Collections.emptyList();
        }

        private static ResultWrapper success(List<Map<String, Object>> devices) {
            return new ResultWrapper(true, null, devices);
        }

        private static ResultWrapper fail(String message) {
            return new ResultWrapper(false, message, Collections.emptyList());
        }
    }
}
