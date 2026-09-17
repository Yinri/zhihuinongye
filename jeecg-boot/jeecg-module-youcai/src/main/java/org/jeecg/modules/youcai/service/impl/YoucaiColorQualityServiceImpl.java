package org.jeecg.modules.youcai.service.impl;

import java.io.File;

import org.apache.http.HttpEntity;
import org.jeecg.modules.youcai.entity.YoucaiColorQuality;
import org.jeecg.modules.youcai.mapper.YoucaiColorQualityMapper;
import org.jeecg.modules.youcai.service.IYoucaiColorQualityService;
import org.jeecg.modules.youcai.service.IDashScopeMultiModalService;
import org.jeecg.modules.youcai.util.IoTApiUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.aliyun.oss.common.utils.HttpHeaders;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.net.MediaType;





@Service
public class YoucaiColorQualityServiceImpl extends ServiceImpl<YoucaiColorQualityMapper,YoucaiColorQuality> implements IYoucaiColorQualityService{


    @Autowired
    private YoucaiColorQualityMapper youcaiDiseaseWarningsMapper;

    @Autowired
    private IoTApiUtil ioTApiUtil;

      private final RestTemplate restTemplate = new RestTemplate();
      private static final String FAST_API_URL = "http://localhost:5000/analysis/dominant";


      @Override
      public String callFastApi(File file) {

          MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
          form.add("file", new FileSystemResource(file));

          return restTemplate.postForObject(
                  FAST_API_URL,
                  form,
                  String.class);
      }



      @Autowired
      private IDashScopeMultiModalService dashScopeMultiModalService;
      @Override
      public String generateAdvice(JsonNode analysisJson) throws Exception {

          // ---- 构建 Prompt ----
          StringBuilder prompt = new StringBuilder();
          prompt.append("你是一名油菜栽培与作物表型分析专家。\n")
                .append("以下是油菜早期生长阶段的叶片颜色分析结果（JSON）：\n")
                .append(analysisJson.toPrettyString()).append("\n\n")
                .append("请完成以下任务：\n")
                .append("1. 判断当前油菜生长状态（健康 / 轻度缺氮 / 重度缺氮 / 逆境胁迫）\n")
                .append("2. 给出判断依据（基于颜色分布）\n")
                .append("3. 给出可执行的肥料和施肥方法\n")
                .append("请用简单的口吻回复，建议不超过200字。");

          // ---- 统一调用大模型（qwen3.8-max）----
          return dashScopeMultiModalService.analyzeText(prompt.toString());
      }


      private static final String Midu_API_URL = "http://localhost:7000/count_plants";
      @Override
      public String callMiduFastApi(File file) {

          MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
          form.add("file", new FileSystemResource(file));

          return restTemplate.postForObject(
                  Midu_API_URL,
                  form,
                  String.class);
      }



    
}
