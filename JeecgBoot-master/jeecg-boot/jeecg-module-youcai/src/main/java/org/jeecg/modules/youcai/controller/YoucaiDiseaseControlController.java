package org.jeecg.modules.youcai.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.youcai.config.PythonServiceConfig;
import org.jeecg.modules.youcai.dto.DiseaseAnalysisResponseDTO;
import org.jeecg.modules.youcai.dto.SporeAnalysisResponseDTO;
import org.jeecg.modules.youcai.entity.YoucaiSensorInfo;
import org.jeecg.modules.youcai.entity.iotEntity.ApiResponse;
import org.jeecg.modules.youcai.service.IDiseaseControlService;
import org.jeecg.modules.youcai.service.IYoucaiSensorInfoService;
import org.jeecg.modules.youcai.util.IoTApiUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Tag(name = "病害防控分析")
@RestController
@RequestMapping("/youcai/diseaseControl")
public class YoucaiDiseaseControlController {
    private static final Logger log = LoggerFactory.getLogger(YoucaiDiseaseControlController.class);

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
    private IDiseaseControlService diseaseControlService;

    private final OkHttpClient httpClient = new OkHttpClient();

    @Operation(summary = "获取虫情设备列表")
    @GetMapping("/pestDevices")
    public Result<List<Map<String, Object>>> getPestDevices(
            @Parameter(description = "项目ID") @RequestParam(defaultValue = "229") Integer projectId) {
        try {
            ApiResponse response = ioTApiUtil.getDeviceList(projectId, 1, 150).block();
            if (response != null && response.getCode() == 1 && response.getData() != null) {
                List<Map<String, Object>> devices = parseDeviceList(response.getData());
                return Result.OK(devices);
            }
            return Result.OK(Collections.emptyList());
        } catch (Exception e) {
            log.error("获取虫情设备列表失败", e);
            return Result.error("获取虫情设备列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取孢子仪设备列表")
    @GetMapping("/sporeDevices")
    public Result<List<Map<String, Object>>> getSporeDevices(
            @Parameter(description = "基地名称") @RequestParam String baseName) {
        try {
            String baseNamePrefix = baseName;
            if (baseName.endsWith("基地")) {
                baseNamePrefix = baseName.substring(0, baseName.length() - 2);
            }
            
            LambdaQueryWrapper<YoucaiSensorInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(YoucaiSensorInfo::getSensorTypeId, 4)
                       .eq(YoucaiSensorInfo::getIsDelete, 0)
                       .likeRight(YoucaiSensorInfo::getSensorName, baseNamePrefix);
            
            List<YoucaiSensorInfo> sensorList = youcaiSensorInfoService.list(queryWrapper);
            
            List<Map<String, Object>> devices = new ArrayList<>();
            for (YoucaiSensorInfo sensor : sensorList) {
                Map<String, Object> device = new HashMap<>();
                device.put("deviceCode", sensor.getSensorSerial());
                device.put("deviceName", sensor.getSensorName());
                device.put("lat", sensor.getLatitude());
                device.put("lng", sensor.getLongitude());
                device.put("state", sensor.getState());
                devices.add(device);
            }
            
            return Result.OK(devices);
        } catch (Exception e) {
            log.error("获取孢子仪设备列表失败", e);
            return Result.error("获取孢子仪设备列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取监控截图列表（虫情设备）")
    @GetMapping("/monitorImages")
    public Result<List<Map<String, Object>>> getMonitorImages(
            @Parameter(description = "基地ID") @RequestParam String baseId) {
        try {
            log.info("收到监控截图查询请求, baseId={}", baseId);
            return Result.OK(diseaseControlService.getMonitorImagesByBaseId(baseId));
        } catch (Exception e) {
            log.error("获取监控截图失败", e);
            return Result.error("获取监控截图失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取孢子捕捉仪图片列表")
    @GetMapping("/sporeImages")
    public Result<List<Map<String, Object>>> getSporeImages(
            @Parameter(description = "基地名称") @RequestParam String baseName) {
        try {
            log.info("收到孢子图片查询请求, baseName={}", baseName);
            return Result.OK(diseaseControlService.getSporeImagesByBaseName(baseName));
        } catch (Exception e) {
            log.error("获取孢子捕捉仪图片失败", e);
            return Result.error("获取孢子捕捉仪图片失败: " + e.getMessage());
        }
    }

    @Operation(summary = "AI病害分析（上传图片）")
    @PostMapping(value = "/analyzeDisease", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<DiseaseAnalysisResponseDTO> analyzeDisease(
            @Parameter(description = "图片文件") @RequestParam("file") MultipartFile file) {
        
        log.info("收到病害分析请求: 文件名={}", file.getOriginalFilename());

        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("请上传图片文件");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            return Result.error("图片大小不能超过10MB");
        }

        try {
            String pythonApiUrl = pythonServiceConfig.getUrl() + "/api/warning/disease/predict";

            okhttp3.RequestBody fileBody = okhttp3.RequestBody.create(
                    file.getBytes(),
                    okhttp3.MediaType.parse("image/*"));

            okhttp3.RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", file.getOriginalFilename(), fileBody)
                    .build();

            Request request = new Request.Builder()
                    .url(pythonApiUrl)
                    .post(requestBody)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    return Result.error("调用Python服务失败: " + response.code());
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

                return Result.OK(result);
            }
        } catch (Exception e) {
            log.error("病害分析失败", e);
            return Result.error("病害分析失败: " + e.getMessage());
        }
    }

    @Operation(summary = "AI病害分析（通过图片URL）")
    @PostMapping("/analyzeDiseaseByUrl")
    public Result<DiseaseAnalysisResponseDTO> analyzeDiseaseByUrl(
            @Parameter(description = "图片URL") @RequestParam String imageUrl) {
        
        log.info("收到病害分析请求: 图片URL={}", imageUrl);

        try {
            byte[] imageBytes = downloadImage(imageUrl);
            if (imageBytes == null || imageBytes.length == 0) {
                return Result.error("无法下载图片");
            }

            DiseaseAnalysisResponseDTO result = analyzeSingleImage(imageBytes);
            return Result.OK(result);
        } catch (Exception e) {
            log.error("病害分析失败", e);
            return Result.error("病害分析失败: " + e.getMessage());
        }
    }

    @Operation(summary = "批量病害分析（综合多张图片）")
    @PostMapping("/analyzeDiseaseBatch")
    public Result<DiseaseAnalysisResponseDTO> analyzeDiseaseBatch(
            @RequestBody Map<String, Object> params) {
        
        @SuppressWarnings("unchecked")
        List<String> imageUrls = (List<String>) params.get("imageUrls");
        
        log.info("收到批量病害分析请求: 图片数量={}", imageUrls != null ? imageUrls.size() : 0);
        
        if (imageUrls == null || imageUrls.isEmpty()) {
            return Result.error("图片URL列表不能为空");
        }

        try {
            DiseaseAnalysisResponseDTO combinedResult = new DiseaseAnalysisResponseDTO();
            combinedResult.setAnalysisTime(new Date());
            
            double totalConfidence = 0.0;
            int successCount = 0;
            List<String> allPreventionMeasures = new ArrayList<>();
            List<DiseaseAnalysisResponseDTO.PesticideRecommendation> allPesticides = new ArrayList<>();
            Map<String, Integer> diseaseCount = new HashMap<>();
            String highestSeverity = "轻度";
            
            for (String imageUrl : imageUrls) {
                try {
                    byte[] imageBytes = downloadImage(imageUrl);
                    if (imageBytes == null || imageBytes.length == 0) {
                        log.warn("下载图片失败: {}", imageUrl);
                        continue;
                    }
                    
                    DiseaseAnalysisResponseDTO singleResult = analyzeSingleImage(imageBytes);
                    
                    if (singleResult != null) {
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
                    }
                } catch (Exception e) {
                    log.warn("单张图片分析失败: {}, 错误: {}", imageUrl, e.getMessage());
                }
            }
            
            if (successCount > 0) {
                combinedResult.setConfidence(totalConfidence / successCount);
                combinedResult.setSeverity(highestSeverity);
                
                String mainDisease = diseaseCount.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("未知");
                combinedResult.setDiseaseName(mainDisease);
                
                combinedResult.setDescription(String.format("综合分析了%d张图片，主要病害为：%s，检测到%d种不同病害类型", 
                    successCount, mainDisease, diseaseCount.size()));
                
                Set<String> uniqueMeasures = new LinkedHashSet<>(allPreventionMeasures);
                combinedResult.setPreventionMeasures(new ArrayList<>(uniqueMeasures));
                
                Map<String, DiseaseAnalysisResponseDTO.PesticideRecommendation> uniquePesticides = new LinkedHashMap<>();
                for (DiseaseAnalysisResponseDTO.PesticideRecommendation p : allPesticides) {
                    if (p.getName() != null && !uniquePesticides.containsKey(p.getName())) {
                        uniquePesticides.put(p.getName(), p);
                    }
                }
                combinedResult.setRecommendedPesticides(new ArrayList<>(uniquePesticides.values()));
                
                String llmAnalysis = callLLMForDiseaseAdvice(mainDisease);
                parseLLMResponseToResult(llmAnalysis, combinedResult);
            } else {
                combinedResult.setDiseaseName("分析失败");
                combinedResult.setDescription("所有图片分析均失败");
                combinedResult.setSeverity("未知");
            }
            
            log.info("批量病害分析完成，成功数量: {}/{}", successCount, imageUrls.size());
            return Result.OK(combinedResult);
        } catch (Exception e) {
            log.error("批量病害分析失败", e);
            return Result.error("批量病害分析失败: " + e.getMessage());
        }
    }

    @Operation(summary = "监控截图批量分析（综合所有图片）")
    @PostMapping("/analyzeMonitorBatch")
    public Result<DiseaseAnalysisResponseDTO> analyzeMonitorBatch(
            @Parameter(description = "基地ID") @RequestParam String baseId) {
        try {
            log.info("收到监控截图批量分析请求, baseId={}", baseId);
            return Result.OK(diseaseControlService.analyzeMonitorBatchByBaseId(baseId));
        } catch (Exception e) {
            log.error("监控截图批量分析失败", e);
            return Result.error("监控截图批量分析失败: " + e.getMessage());
        }
    }

    @Operation(summary = "孢子综合分析")
    @PostMapping("/analyzeSpore")
    public Result<SporeAnalysisResponseDTO> analyzeSporeImages(
            @Parameter(description = "基地名称") @RequestParam String baseName) {
        try {
            log.info("收到孢子综合分析请求, baseName={}", baseName);
            return Result.OK(diseaseControlService.analyzeSporeByBaseName(baseName));
        } catch (Exception e) {
            log.error("孢子综合分析失败", e);
            return Result.error("孢子综合分析失败: " + e.getMessage());
        }
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
            if (newOrder == 1) return "低";
            if (newOrder == 2) return "中";
            return "高";
        }
        return current;
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

    private String buildSporeAnalysisPromptWithResults(int totalImages, int analyzedImages, String diseaseSummary, String riskLevel) {
        return "你是油菜病害预警专家。请基于以下孢子捕捉仪的实际分析数据进行综合分析：\n" +
                "图片总数：" + totalImages + "张\n" +
                "成功分析图片数：" + analyzedImages + "张\n" +
                "检测到的病害情况：" + diseaseSummary + "\n" +
                "当前风险等级：" + riskLevel + "\n\n" +
                "请提供以下分析结果（以JSON格式返回）：\n" +
                "1. warningLevel: 预警等级（低/中/高）\n" +
                "2. diseaseWarnings: 病害早期预警列表（数组，包含diseaseName、riskLevel、description、suggestion）\n" +
                "3. medicationGuides: 科学用药指导列表（数组，包含pesticideName、dosage、timing、method、precautions）\n" +
                "4. summary: 综合分析总结\n\n" +
                "请直接返回JSON格式数据，不要包含其他说明文字。";
    }

    @Operation(summary = "获取病害防控综合数据")
    @GetMapping("/overview")
    public Result<Map<String, Object>> getDiseaseControlOverview(
            @Parameter(description = "虫情设备编码") @RequestParam(required = false) String pestDeviceCode,
            @Parameter(description = "孢子设备编码") @RequestParam(required = false) String sporeDeviceCode,
            @Parameter(description = "项目ID") @RequestParam(defaultValue = "229") Integer projectId,
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(7);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("timeRange", startDate.toString() + " 至 " + endDate.toString());

        try {
            if (pestDeviceCode == null || pestDeviceCode.isEmpty()) {
                ApiResponse pestDevicesResponse = ioTApiUtil.getDeviceList(projectId, 1, 1).block();
                if (pestDevicesResponse != null && pestDevicesResponse.getCode() == 1) {
                    List<Map<String, Object>> devices = parseDeviceList(pestDevicesResponse.getData());
                    if (!devices.isEmpty()) {
                        pestDeviceCode = (String) devices.get(0).get("deviceCode");
                    }
                }
            }

            if (sporeDeviceCode == null || sporeDeviceCode.isEmpty()) {
                ApiResponse sporeDevicesResponse = ioTApiUtil.getDeviceList(projectId, 2, 1).block();
                if (sporeDevicesResponse != null && sporeDevicesResponse.getCode() == 1) {
                    List<Map<String, Object>> devices = parseDeviceList(sporeDevicesResponse.getData());
                    if (!devices.isEmpty()) {
                        sporeDeviceCode = (String) devices.get(0).get("deviceCode");
                    }
                }
            }

            if (pestDeviceCode != null && !pestDeviceCode.isEmpty()) {
                ApiResponse pestResponse = ioTApiUtil.getPestPhotos(pestDeviceCode, 0, startDate.toString(), endDate.toString()).block();
                if (pestResponse != null && pestResponse.getCode() == 1 && pestResponse.getData() != null) {
                    result.put("monitorImages", parseImageList(pestResponse.getData()));
                } else {
                    result.put("monitorImages", Collections.emptyList());
                }
            } else {
                result.put("monitorImages", Collections.emptyList());
            }

            if (sporeDeviceCode != null && !sporeDeviceCode.isEmpty()) {
                ApiResponse sporeResponse = ioTApiUtil.getDiseasePhotos(sporeDeviceCode, 0, startDate.toString(), endDate.toString()).block();
                if (sporeResponse != null && sporeResponse.getCode() == 1 && sporeResponse.getData() != null) {
                    result.put("sporeImages", parseImageList(sporeResponse.getData()));
                } else {
                    result.put("sporeImages", Collections.emptyList());
                }
            } else {
                result.put("sporeImages", Collections.emptyList());
            }

            result.put("pestDeviceCode", pestDeviceCode);
            result.put("sporeDeviceCode", sporeDeviceCode);

            return Result.OK(result);
        } catch (Exception e) {
            log.error("获取病害防控综合数据失败", e);
            return Result.error("获取数据失败: " + e.getMessage());
        }
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
    private List<Map<String, Object>> parseDeviceList(Object data) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (data instanceof Map) {
            Map<String, Object> dataMap = (Map<String, Object>) data;
            Object list = dataMap.get("list");
            if (list instanceof List) {
                for (Object item : (List<?>) list) {
                    if (item instanceof Map) {
                        Map<String, Object> device = new HashMap<>();
                        Map<String, Object> itemMap = (Map<String, Object>) item;
                        device.put("deviceCode", itemMap.get("deviceCode"));
                        device.put("deviceName", itemMap.get("deviceName"));
                        device.put("lat", itemMap.get("lat"));
                        device.put("lng", itemMap.get("lng"));
                        device.put("state", itemMap.get("state"));
                        result.add(device);
                    }
                }
            }
        }
        return result;
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

    private String callLLMForDiseaseAdvice(String diseaseName) {
        String prompt = "你是油菜农业专家。请基于以下病害名称给出具体的防治建议：\n" +
                "病害名称：" + diseaseName + "\n\n" +
                "请提供以下分析结果（以JSON格式返回）：\n" +
                "1. preventionMeasures: 防治建议列表（数组）\n" +
                "2. recommendedPesticides: 推荐农药列表（数组，包含name、dosage、usage、precautions）\n" +
                "3. bestPreventionTime: 最佳防治时期\n" +
                "4. summary: 综合分析总结\n\n" +
                "请直接返回JSON格式数据，不要包含其他说明文字。";
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
            log.error("解析LLM响应失败", e);
        }
        return response;
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
        if (text == null) return "{}";
        int start = text.indexOf("{");
        int end = text.lastIndexOf("}");
        if (start != -1 && end != -1 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    private String getDefaultTimeRange() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(7);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return start.format(formatter) + " 至 " + end.format(formatter);
    }
}
