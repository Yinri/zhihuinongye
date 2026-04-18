package org.jeecg.modules.youcai.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jeecg.modules.youcai.config.PythonServiceConfig;
import org.jeecg.modules.youcai.dto.DiseaseAnalysisRequestDTO;
import org.jeecg.modules.youcai.dto.DiseaseAnalysisResponseDTO;
import org.jeecg.modules.youcai.dto.SporeAnalysisResponseDTO;
import org.jeecg.modules.youcai.entity.YoucaiSensorInfo;
import org.jeecg.modules.youcai.entity.iotEntity.ApiResponse;
import org.jeecg.modules.youcai.service.IDiseaseControlService;
import org.jeecg.modules.youcai.service.IVideoSnapshotService;
import org.jeecg.modules.youcai.service.IYoucaiSensorInfoService;
import org.jeecg.modules.youcai.util.IoTApiUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DiseaseControlServiceImpl implements IDiseaseControlService {

    private static final String DASHSCOPE_API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";

    @Value("${dashscope.api-key:${DASHSCOPE_API_KEY:}}")
    private String dashscopeApiKey;

    @Autowired
    private IoTApiUtil ioTApiUtil;

    @Autowired
    private PythonServiceConfig pythonServiceConfig;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    private IYoucaiSensorInfoService youcaiSensorInfoService;

    @Autowired
    private IVideoSnapshotService videoSnapshotService;

    private final OkHttpClient httpClient = new OkHttpClient();

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
        return analyzeImageList(imageList, "监控截图");
    }

    @Override
    public DiseaseAnalysisResponseDTO analyzeCropDisease(DiseaseAnalysisRequestDTO request) {
        log.info("开始分析作物病害, 基地: {}, 图片类型: {}", request.getBaseName(), request.getImageType());

        DiseaseAnalysisResponseDTO response = new DiseaseAnalysisResponseDTO();

        try {
            String prompt = buildDiseaseAnalysisPrompt(request);
            String llmResponse = callLLMApi(prompt);
            parseDiseaseAnalysisResponse(llmResponse, response);

            log.info("病害分析完成, 病害名称: {}, 危害程度: {}", response.getDiseaseName(), response.getSeverity());
        } catch (Exception e) {
            log.error("病害分析失败: {}", e.getMessage(), e);
            response.setDiseaseName("分析失败");
            response.setDescription("病害分析过程中发生错误: " + e.getMessage());
            response.setSeverity("未知");
        }

        return response;
    }

    @Override
    public SporeAnalysisResponseDTO analyzeSporeByBaseName(String baseName) {
        List<Map<String, Object>> imageList = getSporeImagesByBaseName(baseName);
        if (imageList.isEmpty()) {
            throw new IllegalStateException("未找到孢子图片");
        }

        List<String> imageUrls = new ArrayList<>();
        for (Map<String, Object> imageInfo : imageList) {
            String imageUrl = (String) imageInfo.get("imageUrl");
            if (StringUtils.hasText(imageUrl)) {
                imageUrls.add(imageUrl);
            }
        }

        int successCount = 0;
        Map<String, Integer> diseaseCount = new HashMap<>();
        String highestRiskLevel = "低";

        for (String imageUrl : imageUrls) {
            try {
                byte[] imageBytes = downloadImage(imageUrl);
                if (imageBytes == null || imageBytes.length == 0) {
                    log.warn("下载孢子图片失败: {}", imageUrl);
                    continue;
                }

                DiseaseAnalysisResponseDTO singleResult = analyzeSingleImage(imageBytes);
                if (singleResult != null) {
                    successCount++;
                    if (singleResult.getDiseaseName() != null) {
                        diseaseCount.merge(singleResult.getDiseaseName(), 1, Integer::sum);
                    }
                    highestRiskLevel = getHigherRiskLevel(highestRiskLevel, singleResult.getSeverity());
                }
            } catch (Exception e) {
                log.warn("单张孢子图片分析失败: {}, 错误: {}", imageUrl, e.getMessage());
            }
        }

        String diseaseSummary = diseaseCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .map(e -> e.getKey() + "(" + e.getValue() + "次)")
                .reduce((a, b) -> a + "、" + b)
                .orElse("未检测到病害");

        String prompt = buildSporeAnalysisPromptWithResults(imageUrls.size(), successCount, diseaseSummary, highestRiskLevel);
        String llmResponse = callLLMApi(prompt);

        SporeAnalysisResponseDTO response = new SporeAnalysisResponseDTO();
        parseSporeLLMResponse(llmResponse, response);

        SporeAnalysisResponseDTO.SporeStatistics statistics = new SporeAnalysisResponseDTO.SporeStatistics();
        statistics.setTotalImages(imageList.size());
        statistics.setAnalyzedImages(successCount);
        statistics.setTimeRange(getDefaultTimeRange());
        response.setStatistics(statistics);
        return response;
    }

    @Override
    public DiseaseAnalysisResponseDTO uploadAndAnalyze(MultipartFile file, String imageType, String baseId) {
        log.info("上传并分析图片, 文件名: {}, 类型: {}, 基地ID: {}", file.getOriginalFilename(), imageType, baseId);

        DiseaseAnalysisRequestDTO request = new DiseaseAnalysisRequestDTO();
        request.setImageType(imageType);
        request.setBaseId(baseId);

        try {
            String base64 = Base64.getEncoder().encodeToString(file.getBytes());
            request.setImageBase64(base64);
        } catch (IOException e) {
            log.error("文件读取失败: {}", e.getMessage(), e);
        }

        return analyzeCropDisease(request);
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

    private DiseaseAnalysisResponseDTO analyzeImageList(List<Map<String, Object>> imageList, String imageType) {
        DiseaseAnalysisResponseDTO combinedResult = new DiseaseAnalysisResponseDTO();
        combinedResult.setAnalysisTime(new Date());

        double totalConfidence = 0.0;
        int successCount = 0;
        List<String> allPreventionMeasures = new ArrayList<>();
        List<DiseaseAnalysisResponseDTO.PesticideRecommendation> allPesticides = new ArrayList<>();
        Map<String, Integer> diseaseCount = new HashMap<>();
        String highestSeverity = "轻度";

        for (Map<String, Object> imageInfo : imageList) {
            String imageUrl = (String) imageInfo.get("imageUrl");
            if (!StringUtils.hasText(imageUrl)) {
                continue;
            }

            try {
                byte[] imageBytes = downloadImage(imageUrl);
                if (imageBytes == null || imageBytes.length == 0) {
                    log.warn("下载{}失败: {}", imageType, imageUrl);
                    continue;
                }

                DiseaseAnalysisResponseDTO singleResult = analyzeSingleImage(imageBytes);
                if (singleResult == null) {
                    continue;
                }

                successCount++;
                if (singleResult.getConfidence() != null) {
                    totalConfidence += singleResult.getConfidence();
                }
                if (singleResult.getDiseaseName() != null) {
                    diseaseCount.merge(singleResult.getDiseaseName(), 1, Integer::sum);
                }
                highestSeverity = getHigherSeverity(highestSeverity, singleResult.getSeverity());

                if (singleResult.getPreventionMeasures() != null) {
                    allPreventionMeasures.addAll(singleResult.getPreventionMeasures());
                }
                if (singleResult.getRecommendedPesticides() != null) {
                    allPesticides.addAll(singleResult.getRecommendedPesticides());
                }
            } catch (Exception e) {
                log.warn("单张{}分析失败: {}, 错误: {}", imageType, imageUrl, e.getMessage());
            }
        }

        if (successCount == 0) {
            combinedResult.setDiseaseName("分析失败");
            combinedResult.setDescription("所有" + imageType + "分析均失败");
            combinedResult.setSeverity("未知");
            return combinedResult;
        }

        combinedResult.setConfidence(totalConfidence / successCount);
        combinedResult.setSeverity(highestSeverity);

        String mainDisease = diseaseCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("未知");
        combinedResult.setDiseaseName(mainDisease);
        combinedResult.setDescription(String.format("综合分析了%d张%s（共%d张），主要病害为：%s，检测到%d种不同病害类型",
                successCount, imageType, imageList.size(), mainDisease, diseaseCount.size()));

        Set<String> uniqueMeasures = new LinkedHashSet<>(allPreventionMeasures);
        combinedResult.setPreventionMeasures(new ArrayList<>(uniqueMeasures));

        Map<String, DiseaseAnalysisResponseDTO.PesticideRecommendation> uniquePesticides = new LinkedHashMap<>();
        for (DiseaseAnalysisResponseDTO.PesticideRecommendation pesticide : allPesticides) {
            if (pesticide.getName() != null && !uniquePesticides.containsKey(pesticide.getName())) {
                uniquePesticides.put(pesticide.getName(), pesticide);
            }
        }
        combinedResult.setRecommendedPesticides(new ArrayList<>(uniquePesticides.values()));

        String llmAnalysis = callLLMForDiseaseAdvice(mainDisease);
        parseLLMResponseToResult(llmAnalysis, combinedResult);
        return combinedResult;
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

        String baseNamePrefix = baseName;
        if (baseName.endsWith("基地")) {
            baseNamePrefix = baseName.substring(0, baseName.length() - 2);
        }

        LambdaQueryWrapper<YoucaiSensorInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(YoucaiSensorInfo::getSensorTypeId, 4)
                .eq(YoucaiSensorInfo::getIsDelete, 0)
                .likeRight(YoucaiSensorInfo::getSensorName, baseNamePrefix)
                .last("limit 1");

        YoucaiSensorInfo sensor = youcaiSensorInfoService.getOne(queryWrapper, false);
        return sensor != null ? sensor.getSensorSerial() : null;
    }

    private String buildDiseaseAnalysisPrompt(DiseaseAnalysisRequestDTO request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是油菜农业专家。请基于以下监控截图信息进行病害分析：\n\n");
        prompt.append("图片类型：").append("monitor".equals(request.getImageType()) ? "监控截图" : "孢子捕捉仪图片").append("\n");
        if (request.getBaseName() != null) {
            prompt.append("基地名称：").append(request.getBaseName()).append("\n");
        }
        prompt.append("\n请提供以下分析结果（以JSON格式返回）：\n");
        prompt.append("1. diseaseName: 识别到的病害名称（如：油菜菌核病、油菜霜霉病、健康等）\n");
        prompt.append("2. confidence: 置信度（0-1之间的数值）\n");
        prompt.append("3. description: 病害详细描述\n");
        prompt.append("4. severity: 危害程度（轻度/中度/重度）\n");
        prompt.append("5. preventionMeasures: 防治建议列表\n");
        prompt.append("6. recommendedPesticides: 推荐农药列表（包含name、dosage、usage、precautions）\n");
        prompt.append("7. bestPreventionTime: 最佳防治时期\n");
        prompt.append("8. summary: 综合分析总结\n\n");
        prompt.append("请直接返回JSON格式数据，不要包含其他说明文字。");
        return prompt.toString();
    }

    private String buildSporeAnalysisPromptWithResults(int totalImages, int analyzedImages, String diseaseSummary, String riskLevel) {
        return "你是油菜病害预警专家。请基于以下孢子捕捉仪的实际分析数据进行综合分析：\n"
                + "图片总数：" + totalImages + "张\n"
                + "成功分析图片数：" + analyzedImages + "张\n"
                + "检测到的病害情况：" + diseaseSummary + "\n"
                + "当前风险等级：" + riskLevel + "\n\n"
                + "请提供以下分析结果（以JSON格式返回）：\n"
                + "1. warningLevel: 预警等级（低/中/高）\n"
                + "2. diseaseWarnings: 病害早期预警列表（数组，包含diseaseName、riskLevel、description、suggestion）\n"
                + "3. medicationGuides: 科学用药指导列表（数组，包含pesticideName、dosage、timing、method、precautions）\n"
                + "4. summary: 综合分析总结\n\n"
                + "请直接返回JSON格式数据，不要包含其他说明文字。";
    }

    private DiseaseAnalysisResponseDTO analyzeSingleImage(byte[] imageBytes) {
        try {
            String pythonApiUrl = pythonServiceConfig.getUrl() + "/api/warning/disease/predict";

            okhttp3.RequestBody fileBody = okhttp3.RequestBody.create(imageBytes, okhttp3.MediaType.parse("image/*"));

            okhttp3.RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", "image.jpg", fileBody)
                    .build();

            Request request = new Request.Builder()
                    .url(pythonApiUrl)
                    .post(requestBody)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("调用Python服务失败: {}", response.code());
                    return null;
                }

                String responseBody = response.body().string();
                JSONObject jsonResult = JSON.parseObject(responseBody);

                DiseaseAnalysisResponseDTO result = new DiseaseAnalysisResponseDTO();
                if (jsonResult.containsKey("data")) {
                    JSONObject data = jsonResult.getJSONObject("data");
                    result.setDiseaseName(data.getString("disease"));
                    result.setConfidence(data.getDouble("confidence"));
                    result.setDescription(data.getString("description"));
                    result.setSeverity(getSeverityByDisease(data.getString("disease")));
                }

                String llmAnalysis = callLLMForDiseaseAdvice(result.getDiseaseName());
                parseLLMResponseToResult(llmAnalysis, result);
                return result;
            }
        } catch (Exception e) {
            log.error("单张图片分析失败", e);
            return null;
        }
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

    private byte[] downloadImage(String imageUrl) {
        try {
            Request request = new Request.Builder().url(imageUrl).build();
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    return response.body().bytes();
                }
            }
        } catch (Exception e) {
            log.error("下载图片失败: {}", imageUrl, e);
        }
        return null;
    }

    private String callLLMForDiseaseAdvice(String diseaseName) {
        String prompt = "你是油菜农业专家。请基于以下病害名称给出具体的防治建议：\n"
                + "病害名称：" + diseaseName + "\n\n"
                + "请提供以下分析结果（以JSON格式返回）：\n"
                + "1. preventionMeasures: 防治建议列表（数组）\n"
                + "2. recommendedPesticides: 推荐农药列表（数组，包含name、dosage、usage、precautions）\n"
                + "3. bestPreventionTime: 最佳防治时期\n"
                + "4. summary: 综合分析总结\n\n"
                + "请直接返回JSON格式数据，不要包含其他说明文字。";
        return callLLMApi(prompt);
    }

    private String callLLMApi(String prompt) {
        try {
            if (!StringUtils.hasText(dashscopeApiKey)) {
                log.warn("未配置 DashScope API Key，跳过病害建议生成");
                return null;
            }

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "qwen-turbo");
            requestBody.put("input", new JSONObject().fluentPut("prompt", prompt));
            requestBody.put("parameters", new JSONObject()
                    .fluentPut("temperature", 0.3)
                    .fluentPut("top_p", 0.9)
                    .fluentPut("max_tokens", 2000));

            WebClient client = webClientBuilder.build();
            String result = client.post()
                    .uri(DASHSCOPE_API_URL)
                    .header("Authorization", "Bearer " + dashscopeApiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody.toJSONString())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return parseLLMResponse(result);
        } catch (Exception e) {
            log.error("调用LLM API失败", e);
            return null;
        }
    }

    private String parseLLMResponse(String response) {
        try {
            JSONObject json = JSON.parseObject(response);
            if (json.containsKey("output")) {
                JSONObject output = json.getJSONObject("output");
                return output.getString("text");
            }
        } catch (Exception e) {
            log.error("解析LLM响应失败: {}", e.getMessage(), e);
        }
        return response;
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

    private void parseLLMResponseToResult(String llmResponse, DiseaseAnalysisResponseDTO result) {
        if (llmResponse == null || llmResponse.isEmpty()) {
            return;
        }
        try {
            String jsonStr = extractJson(llmResponse);
            JSONObject json = JSON.parseObject(jsonStr);

            if (json.containsKey("preventionMeasures")) {
                JSONArray measures = json.getJSONArray("preventionMeasures");
                result.setPreventionMeasures(measures.toJavaList(String.class));
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
                result.setRecommendedPesticides(pesticideList);
            }

            if (json.containsKey("bestPreventionTime")) {
                result.setBestPreventionTime(json.getString("bestPreventionTime"));
            }

            if (json.containsKey("summary")) {
                result.setSummary(json.getString("summary"));
            }
        } catch (Exception e) {
            log.error("解析LLM响应到结果失败", e);
            result.setSummary(llmResponse);
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

    private String getSeverityByDisease(String disease) {
        if (disease == null || "健康".equals(disease) || "未知".equals(disease)) {
            return "轻度";
        }
        if (disease.contains("菌核病")) {
            return "重度";
        }
        if (disease.contains("霜霉病") || disease.contains("病毒病")) {
            return "中度";
        }
        return "轻度";
    }

    private String getHigherSeverity(String current, String newSeverity) {
        Map<String, Integer> severityOrder = new HashMap<>();
        severityOrder.put("轻度", 1);
        severityOrder.put("中度", 2);
        severityOrder.put("重度", 3);

        int currentOrder = severityOrder.getOrDefault(current, 0);
        int newOrder = severityOrder.getOrDefault(newSeverity, 0);
        return newOrder > currentOrder ? newSeverity : current;
    }

    private String getHigherRiskLevel(String current, String severity) {
        Map<String, Integer> riskOrder = new HashMap<>();
        riskOrder.put("低", 1);
        riskOrder.put("轻度", 1);
        riskOrder.put("中", 2);
        riskOrder.put("中度", 2);
        riskOrder.put("高", 3);
        riskOrder.put("重度", 3);

        int currentOrder = riskOrder.getOrDefault(current, 0);
        int newOrder = riskOrder.getOrDefault(severity, 0);

        if (newOrder > currentOrder) {
            if (newOrder == 1) {
                return "低";
            }
            if (newOrder == 2) {
                return "中";
            }
            return "高";
        }
        return current;
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
