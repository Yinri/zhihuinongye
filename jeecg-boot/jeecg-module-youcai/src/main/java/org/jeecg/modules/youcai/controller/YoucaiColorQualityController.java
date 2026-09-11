package org.jeecg.modules.youcai.controller;


import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.modules.youcai.dto.ColorRequestDTO;
import org.jeecg.modules.youcai.entity.YoucaiColorQuality;
import org.jeecg.modules.youcai.entity.YoucaiDiseaseWarnings;
import org.jeecg.modules.youcai.service.IYoucaiColorQualityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Base64;
import java.util.Map;

@Tag(name = "颜色指数表")
@RestController
@RequestMapping("/youcai/youcaiColorQuality")
@Slf4j
public class YoucaiColorQualityController extends JeecgController<YoucaiColorQuality, IYoucaiColorQualityService>{

    @Autowired
    private IYoucaiColorQualityService youcaiColorQualityService;
    
    @PostMapping("/color")
    public Result<String> color(@RequestBody Map<String, String> body) {
        try {
            String base64 = body.get("file");

            if (base64.contains(",")) {
                base64 = base64.substring(base64.indexOf(",") + 1);
            }

            byte[] imageBytes = Base64.getDecoder().decode(base64);

            // 保存路径
            String filePath = "/root/data/zhihuinongye-master/"
                    + System.currentTimeMillis() + ".jpg";

            File file = new File(filePath);
            file.getParentFile().mkdirs();

            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(imageBytes);
            }

            System.out.println("图片保存成功：" + filePath);

            // 🚀 调用 FastAPI
            String fastApiResult = youcaiColorQualityService.callFastApi(file);
            Result<String> result = new Result<>();
            result.setCode(CommonConstant.SC_OK_200);
            result.setSuccess(true);
            result.setResult(fastApiResult);
            result.setTimestamp(System.currentTimeMillis());
            return result;
            

    
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("Base64转图片失败");
        }
    }

   

    @PostMapping("/advice")
    public Result<String> getAdvice(@RequestBody JsonNode analysisJson) {
        try {
            String advice = youcaiColorQualityService.generateAdvice(analysisJson);
            Result<String> result = new Result<>();
            result.setCode(CommonConstant.SC_OK_200);
            result.setSuccess(true);
            result.setResult(advice);
            result.setTimestamp(System.currentTimeMillis());
            return result;


            
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("生成建议失败：" + e.getMessage());
        }
    }

    @PostMapping("/midu")
    public Result<String> midu(@RequestBody Map<String, String> body) {
        try {
            String base64 = body.get("file");

            if (base64.contains(",")) {
                base64 = base64.substring(base64.indexOf(",") + 1);
            }

            byte[] imageBytes = Base64.getDecoder().decode(base64);

            // 保存路径
            String filePath = "/root/data/zhihuinongye-master/"
                    + System.currentTimeMillis() + ".jpg";

            File file = new File(filePath);
            file.getParentFile().mkdirs();

            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(imageBytes);
            }

            System.out.println("图片保存成功：" + filePath);

            // 🚀 调用 FastAPI
            String fastApiResult = youcaiColorQualityService.callMiduFastApi(file);
            Result<String> result = new Result<>();
            result.setCode(CommonConstant.SC_OK_200);
            result.setSuccess(true);
            result.setResult(fastApiResult);
            result.setTimestamp(System.currentTimeMillis());
            
            return result;
            

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("Base64转图片失败");
        }
    }

    @PostMapping("/add")
    public Result<String> add(@RequestBody YoucaiColorQuality youcaiColorQuality) {
		youcaiColorQualityService.save(youcaiColorQuality);

		return Result.OK("添加成功！");
	}
    



        
    


    
}
