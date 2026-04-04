package org.jeecg.modules.youcai.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.youcai.entity.YoucaiAgriculturalMachine;
import org.jeecg.modules.youcai.entity.YoucaiBases;
import org.jeecg.modules.youcai.entity.iotEntity.ApiResponse;
import org.jeecg.modules.youcai.mapper.YoucaiAgriculturalMachineMapper;
import org.jeecg.modules.youcai.mapper.YoucaiBasesMapper;
import org.jeecg.modules.youcai.service.IYoucaiAgriculturalMachineService;
import org.jeecg.modules.youcai.util.IoTApiUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class YoucaiAgriculturalMachineServiceImpl extends ServiceImpl<YoucaiAgriculturalMachineMapper, YoucaiAgriculturalMachine> implements IYoucaiAgriculturalMachineService {

    @Autowired
    private IoTApiUtil ioTApiUtil;

    @Autowired
    private YoucaiBasesMapper youcaiBasesMapper;

    @Autowired
    private YoucaiAgriculturalMachineMapper agriculturalMachineMapper;

    @Override
    public Result<List<Map<String, Object>>> getOperationRecordsByBaseId(String baseId, String startDate, String endDate) {
        List<Map<String, Object>> records = new ArrayList<>();
        try {
            YoucaiBases base = youcaiBasesMapper.selectById(baseId);
            if (base == null || base.getBaseName() == null) {
                return Result.error("基地不存在");
            }

            String baseName = base.getBaseName();
            String keyword = baseName.length() >= 2 ? baseName.substring(0, 2) : baseName;
            log.info("基地名称: {}，匹配关键字: {}", baseName, keyword);

            QueryWrapper<YoucaiAgriculturalMachine> machineQuery = new QueryWrapper<>();
            List<YoucaiAgriculturalMachine> machines = agriculturalMachineMapper.selectList(machineQuery);

            for (YoucaiAgriculturalMachine machine : machines) {
                try {
                    ApiResponse jobResponse = ioTApiUtil.getJobInfo(machine.getBeidouSn(), startDate, endDate).block();
                    if (jobResponse != null && jobResponse.getCode() == 200 && jobResponse.getData() != null) {
                        JSONObject dataObj = JSON.parseObject(JSON.toJSONString(jobResponse.getData()));
                        JSONArray resultArray = dataObj.getJSONArray("result");

                        if (resultArray != null && !resultArray.isEmpty()) {
                            for (int i = 0; i < resultArray.size(); i++) {
                                try {
                                    JSONObject item = resultArray.getJSONObject(i);
                                    String location = item.getString("w25");

                                    if (location != null && location.contains(keyword)) {
                                        Map<String, Object> record = new HashMap<>();
                                        record.put("beidouSn", item.getString("w1"));
                                        record.put("vehicleNumber", item.getString("w2"));
                                        record.put("operationType", item.getString("w3"));
                                        record.put("operationDate", item.getString("w4"));
                                        record.put("startTime", item.getString("w5"));
                                        record.put("endTime", item.getString("w6"));
                                        record.put("plotCode", item.getString("w7"));
                                        record.put("depthPoints", item.getInteger("w8"));
                                        record.put("travelDistance", item.getBigDecimal("w9"));
                                        record.put("operationDistance", item.getBigDecimal("w10"));
                                        record.put("lng", item.getBigDecimal("w11"));
                                        record.put("lat", item.getBigDecimal("w12"));
                                        record.put("implementId", item.getString("w13"));
                                        record.put("implementName", item.getString("w14"));
                                        record.put("operationWidth", item.getBigDecimal("w15"));
                                        record.put("operationArea", item.getBigDecimal("w16"));
                                        record.put("qualifiedArea", item.getBigDecimal("w17"));
                                        record.put("overlapArea", item.getBigDecimal("w18"));
                                        record.put("tillageDepth", item.getBigDecimal("w19"));
                                        record.put("passRate", item.getBigDecimal("w20"));
                                        record.put("province", item.getString("w21"));
                                        record.put("city", item.getString("w22"));
                                        record.put("district", item.getString("w23"));
                                        record.put("town", item.getString("w24"));
                                        record.put("location", item.getString("w25"));
                                        record.put("fuelConsumption", item.getString("w26"));
                                        record.put("pesticideAmount", item.getString("w27"));
                                        records.add(record);
                                    }
                                } catch (Exception e) {
                                    log.error("解析第{}条作业数据失败: {}", i + 1, e.getMessage());
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("获取农机 {} 作业数据失败: {}", machine.getBeidouSn(), e.getMessage());
                }
            }
            return Result.OK(records);
        } catch (Exception e) {
            log.error("获取作业记录异常", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    @Override
    public Result<List<Map<String, Object>>> getOperationRecordsByBeidouSnList(String beidouSnList, String startDate, String endDate) {
        List<Map<String, Object>> records = new ArrayList<>();
        try {
            String[] snArray = beidouSnList.split(",");
            for (String beidouSn : snArray) {
                if (beidouSn == null || beidouSn.trim().isEmpty()) {
                    continue;
                }
                try {
                    ApiResponse jobResponse = ioTApiUtil.getJobInfo(beidouSn.trim(), startDate, endDate).block();
                    if (jobResponse != null && jobResponse.getCode() == 200 && jobResponse.getData() != null) {
                        JSONObject dataObj = JSON.parseObject(JSON.toJSONString(jobResponse.getData()));
                        JSONArray resultArray = dataObj.getJSONArray("result");

                        if (resultArray != null && !resultArray.isEmpty()) {
                            for (int i = 0; i < resultArray.size(); i++) {
                                try {
                                    JSONObject item = resultArray.getJSONObject(i);
                                    Map<String, Object> record = new HashMap<>();
                                    record.put("id", item.getString("w1") + "_" + item.getString("w5"));
                                    record.put("beidouSn", item.getString("w1"));
                                    record.put("vehicleNumber", item.getString("w2"));
                                    record.put("startTime", item.getString("w5"));
                                    record.put("endTime", item.getString("w6"));
                                    record.put("qualifiedArea", item.getBigDecimal("w17"));
                                    record.put("location", item.getString("w25"));
                                    records.add(record);
                                } catch (Exception e) {
                                    log.error("解析第{}条作业数据失败: {}", i + 1, e.getMessage());
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("获取农机 {} 作业数据失败: {}", beidouSn, e.getMessage());
                }
            }
            return Result.OK(records);
        } catch (Exception e) {
            log.error("获取作业记录异常", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Map<String, Object>> syncOperationData(String baseId, String startDate, String endDate) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "无需同步，数据直接从API获取");
        return Result.OK(result);
    }

    @Override
    public Result<List<Map<String, Object>>> getTrackData(String beidouSn, String startDate, String endDate) {
        List<Map<String, Object>> trackPoints = new ArrayList<>();
        log.info("传来的参数 beidouSn：{}，startDate：{}，endDate： {}", beidouSn, startDate, endDate);
        try {
            ApiResponse trackResponse = ioTApiUtil.getTrackInfo(beidouSn, startDate, endDate).block();
            log.info("trackResponse通过接口获得的数据为： {}",trackResponse);

            if (trackResponse != null && trackResponse.getCode() == 200 && trackResponse.getData() != null) {
                JSONObject dataObj = JSON.parseObject(JSON.toJSONString(trackResponse.getData()));
                JSONArray resultArray = dataObj.getJSONArray("result");
                log.info("获取到的轨迹数据数量：{}", resultArray != null ? resultArray.size() : 0);

                if (resultArray != null && !resultArray.isEmpty()) {
                    for (int i = 0; i < resultArray.size(); i++) {
                        try {
                            JSONObject item = resultArray.getJSONObject(i);
                            Map<String, Object> point = new HashMap<>();
                            point.put("t1", item.getString("t1"));
                            point.put("t2", item.getString("t2"));
                            point.put("t3", item.getString("t3"));
                            point.put("t4", item.getString("t4"));
                            point.put("t5", item.getString("t5"));
                            point.put("t6", item.getString("t6"));
                            point.put("t7", item.get("t7"));
                            point.put("t8", item.get("t8"));
                            point.put("t9", item.getString("t9"));
                            point.put("workflag", item.getString("workflag"));
                            trackPoints.add(point);
                        } catch (Exception e) {
                            log.error("解析第{}条轨迹数据失败: {}", i + 1, e.getMessage());
                        }
                    }
                }
                log.info("返回的轨迹数据数量：{}", trackPoints.size());
            }
            return Result.OK(trackPoints);
        } catch (Exception e) {
            log.error("获取轨迹数据异常", e);
            return Result.error("获取轨迹失败: " + e.getMessage());
        }
    }

    @Override
    public List<YoucaiAgriculturalMachine> getMachineList() {
        List<YoucaiAgriculturalMachine> machineList = new ArrayList<>();
        machineList = agriculturalMachineMapper.selectList(null);
        return machineList;
    }

}
