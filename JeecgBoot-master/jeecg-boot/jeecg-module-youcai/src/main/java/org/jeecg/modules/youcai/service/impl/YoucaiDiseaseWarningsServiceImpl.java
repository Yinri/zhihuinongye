package org.jeecg.modules.youcai.service.impl;

import org.jeecg.modules.youcai.entity.YoucaiDiseaseWarnings;
import org.jeecg.modules.youcai.mapper.YoucaiDiseaseWarningsMapper;
import org.jeecg.modules.youcai.service.IYoucaiDiseaseWarningsService;
import org.jeecg.modules.youcai.util.IoTApiUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.*;

/**
 * @Description: 病害预警表
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V1.0
 */
@Service
public class YoucaiDiseaseWarningsServiceImpl extends ServiceImpl<YoucaiDiseaseWarningsMapper, YoucaiDiseaseWarnings> implements IYoucaiDiseaseWarningsService {
    @Autowired
    private YoucaiDiseaseWarningsMapper youcaiDiseaseWarningsMapper;
    @Autowired
    private IoTApiUtil ioTApiUtil;
    @Override
    public Mono<List<Map<String, Object>>> getDiseaseImages() {
        String deviceCode = "0527250002";
        Integer countType = 0;
        LocalDate EndDate = LocalDate.now();
        LocalDate StartDate = EndDate.minusDays(30);
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
                    List<Map<String, Object>> imageDetailList =
                            (List<Map<String, Object>>) data.get("imageDetailList");
                    List<Map<String, Object>> parsedList = new ArrayList<>();
                    for (Map<String, Object> record : imageDetailList) {
                        Map<String, Object> q = (Map<String, Object>) record.get("q");
                        if (q == null) continue;
                        String imageUrl = (String) q.get("url");
                        String thumbnail = (String) q.get("thumbnail");
                        String dateCreated = (String) q.get("dateCreated");
                        Map<String, Object> parsed = new HashMap<>();
                        parsed.put("image_url", imageUrl);
                        parsed.put("thumbnail", thumbnail);
                        parsed.put("dateCreated", dateCreated);
                        parsedList.add(parsed);
                    }
                    return parsedList;
                });
    }

}
