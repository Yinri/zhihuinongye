package org.jeecg.modules.youcai.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;
import org.jeecg.modules.youcai.dto.AnalysisRequestDTO;
import org.jeecg.modules.youcai.entity.YoucaiDiseaseWarnings;
import org.jeecg.modules.youcai.mapper.YoucaiDiseaseWarningsMapper;
import org.jeecg.modules.youcai.service.IYoucaiDiseaseWarningsService;
import org.jeecg.modules.youcai.util.IoTApiUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.*;

/**
 * @Description: 病害预警表
 * @Author: jeecg-boot
 * @Date: 2025-10-18
 * @Version: V1.0
 */
@Service
public class YoucaiDiseaseWarningsServiceImpl extends ServiceImpl<YoucaiDiseaseWarningsMapper, YoucaiDiseaseWarnings>
        implements IYoucaiDiseaseWarningsService {
    @Autowired
    private YoucaiDiseaseWarningsMapper youcaiDiseaseWarningsMapper;
    @Autowired
    private IoTApiUtil ioTApiUtil;

    @Override
    public Mono<List<Map<String, Object>>> getDiseaseImages() {
        String deviceCode = "0527250002";
        Integer countType = 0;
        LocalDate EndDate = LocalDate.now();
        LocalDate StartDate = EndDate.minusDays(100);
        return ioTApiUtil.getDiseasePhotos(
                deviceCode,
                countType,
                StartDate.toString(),
                EndDate.toString())
                .map(response -> {
                    Map<String, Object> data = (Map<String, Object>) response.getData();
                    if (data == null || !data.containsKey("imageDetailList")) {
                        return Collections.emptyList();
                    }
                    System.out.println(response);
                    List<Map<String, Object>> imageDetailList = (List<Map<String, Object>>) data.get("imageDetailList");
                    List<Map<String, Object>> parsedList = new ArrayList<>();
                    for (Map<String, Object> record : imageDetailList) {
                        Map<String, Object> q = (Map<String, Object>) record.get("q");
                        if (q == null)
                            continue;
                        String imageUrl = (String) q.get("url");
                        String thumbnail = (String) q.get("thumbnail");
                        String dateCreated = (String) q.get("dateCreated");
                        Map<String, Object> parsed = new HashMap<>();
                        parsed.put("image_url", imageUrl);
                        parsed.put("thumbnail", thumbnail);
                        parsed.put("dateCreated", dateCreated);
                        parsedList.add(parsed);
                    }
                    System.out.println("shuliang");
                    System.out.println(parsedList);
                    return parsedList;
                });
    }

    private static final String PYTHON_API_URL = "http://localhost:8001/api/v1/predict/disease";
    private final OkHttpClient client = new OkHttpClient();

    @Override
    public String predict(byte[] imageBytes) throws Exception {

        // 将 byte[] 转为 multipart/form-data 中的 file 字段
        RequestBody fileBody = RequestBody.create(
                imageBytes,
                MediaType.parse("image/*"));

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                        "file",
                        "upload.jpg", // 给个文件名即可
                        fileBody)
                .build();

        Request request = new Request.Builder()
                .url(PYTHON_API_URL)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {

            if (!response.isSuccessful()) {
                throw new RuntimeException("调用 Python API 失败: " + response);
            }

            return response.body().string();
        }
    }

    private static final String API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
    private static final String API_KEY = "sk-5f775477a8c04db4893d8a39d308e151";

    @Override
    public String analyzeDisease(String disease) throws Exception {
        if (disease == null || disease.isEmpty()) {
            throw new Exception("病害名称不能为空");
        }
        // ---- 构建 Prompt ----
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是油菜农业专家。请基于以下病害名称给出具体的防治建议：\n")
                .append("1. 病害危害程度分析\n")
                .append("2. 防治建议与最佳时期\n")
                .append("3. 推荐适用农药及剂量\n")
                .append("4. 给出一份非常简短的防治总结\n\n")
                .append("病害名称：").append(disease).append("\n");

        // ---- 构建请求体 ----
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "qwen-turbo"); // 可换成你使用的模型
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
        // ---- 调用 LLM ----
        WebClient client = WebClient.builder().build();
        String result = client.post()
                .uri(API_URL)
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody.toJSONString())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // ---- 解析返回文本 ----
        JSONObject json = JSON.parseObject(result);
        return json.getJSONObject("output").getString("text");
    }

    @Override
    public List<YoucaiDiseaseWarnings> getHistoryByTimeRangeAndPlot(Date startTime, Date endTime, String plotId) {
        return youcaiDiseaseWarningsMapper.selectList(
                new LambdaQueryWrapper<YoucaiDiseaseWarnings>()
                        .ge(YoucaiDiseaseWarnings::getControlDate, startTime)
                        .le(YoucaiDiseaseWarnings::getControlDate, endTime)
                        .eq(YoucaiDiseaseWarnings::getPlotId, plotId) // 新增 plotId 条件
                        .orderByAsc(YoucaiDiseaseWarnings::getControlDate));
    }

}
