package org.jeecg.modules.youcai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.youcai.dto.AnalysisRequestDTO;
import org.jeecg.modules.youcai.entity.YoucaiPestControl;
import org.jeecg.modules.youcai.entity.YoucaiSensorInfo;
import org.jeecg.modules.youcai.mapper.YoucaiPestControlMapper;
import org.jeecg.modules.youcai.service.IDashScopeMultiModalService;
import org.jeecg.modules.youcai.service.IYoucaiPestControlService;
import org.jeecg.modules.youcai.service.IYoucaiSensorInfoService;
import org.jeecg.modules.youcai.util.IoTApiUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private IYoucaiSensorInfoService youcaiSensorInfoService;

    @Autowired
    private IDashScopeMultiModalService dashScopeMultiModalService;

    @Override
    public Mono<List<Map<String, Object>>> getPestImages(String baseName, String startDate, String endDate) {
        String deviceCode = resolvePestDeviceCode(baseName);
        if (!StringUtils.hasText(deviceCode)) {
            return Mono.just(Collections.emptyList());
        }

        Integer countType = 1;
        return ioTApiUtil.getPestPhotos(deviceCode, countType, startDate, endDate)
                .map(response -> parsePestImageList(response != null ? response.getData() : null));
    }

    @Override
    public String aiAnalysis(AnalysisRequestDTO req) throws Exception {
        if (req == null || req.getPestData() == null || req.getPestData().isEmpty()) {
            throw new Exception("pest_data 不能为空");
        }
        List<String> validImageUrls = req.getImageUrls() == null ? Collections.emptyList() : req.getImageUrls().stream()
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
        if (validImageUrls.isEmpty()) {
            throw new Exception("image_urls 不能为空");
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一名油菜虫情监测与绿色防控专家。")
                .append("请结合虫情测报灯图片和虫情统计数据，只分析与油菜生产相关的害虫动态。")
                .append("重点关注蚜虫、菜青虫、小菜蛾、跳甲、地老虎等油菜常见害虫；")
                .append("对明显与油菜关联弱的杂虫，可合并表述为非主要目标害虫，不要展开。")
                .append("只输出“害虫趋势”和“防治建议”两部分内容。")
                .append("不要输出农药商品名、剂量、使用记录、Markdown 标题或额外说明。")
                .append("结论要简洁、专业、可执行，建议优先体现监测加密、阈值防控、分阶段处置和绿色防控思路。\n\n");

        prompt.append("虫情数据：\n");
        for (AnalysisRequestDTO.PestItem item : req.getPestData()) {
            prompt.append("时间：").append(item.getAnalysisTime()).append("\n");
            if (item.getInsects() != null) {
                item.getInsects().forEach((name, count) -> {
                    prompt.append("  ").append(name).append(": ").append(count).append("只\n");
                });
            }
            prompt.append("\n");
        }

        prompt.append("分析要求：\n");
        prompt.append("1. 先结合连续时间数据判断主要害虫数量变化、是否上升、是否达到需要重点防控的趋势。\n");
        prompt.append("2. 再结合图片判断虫体特征是否支持该趋势，如图片证据不足，要明确写“图片证据有限”。\n");
        prompt.append("3. 防治建议只给田间管理动作，不写具体农药名称和剂量。\n");
        prompt.append("4. 如果当前虫量低且趋势平稳，应建议继续监测而不是过度处置。\n\n");

        prompt.append("请严格按以下纯文本格式输出：\n");
        prompt.append("害虫趋势：用2到4句总结油菜相关主要害虫的变化趋势、风险高低和判断依据。\n");
        prompt.append("防治建议：用2到4句给出巡查、诱控、田间清理、分阶段处置等建议。\n");
        return dashScopeMultiModalService.analyzeImages(validImageUrls, prompt.toString());
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
        String normalized = baseName == null ? "" : baseName.trim();
        String[] separators = {"镇", "乡", "街道"};
        for (String separator : separators) {
            int index = normalized.indexOf(separator);
            if (index > 0) {
                return normalized.substring(0, index);
            }
        }
        return normalized;
    }

}
