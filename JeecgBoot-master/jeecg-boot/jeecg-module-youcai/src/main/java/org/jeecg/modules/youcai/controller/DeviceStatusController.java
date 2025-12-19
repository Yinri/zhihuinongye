package org.jeecg.modules.youcai.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.youcai.entity.iotEntity.ApiResponse;
import org.jeecg.modules.youcai.util.IoTApiUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 设备状态管理
 * @Author: jeecg-boot
 * @Date: 2025-12-18
 * @Version: V1.0
 */
@Tag(name="设备状态管理")
@RestController
@RequestMapping("/rapeseed/device")
@Slf4j
public class DeviceStatusController {

    @Autowired
    private IoTApiUtil ioTApiUtil;

    /**
     * 获取设备状态信息
     *
     * @param projectId 项目ID，默认为1
     * @return 设备状态信息
     */
    @AutoLog(value = "设备状态管理-获取设备状态")
    @Operation(summary="设备状态管理-获取设备状态", description="设备状态管理-获取设备状态")
    @GetMapping(value = "/status")
    public Result<Object> getDeviceStatus(
            @Parameter(name="projectId", description="项目ID") @RequestParam(name="projectId", defaultValue = "1") Integer projectId) {
        try {
            log.info("获取项目ID {} 的设备状态信息", projectId);
            
            // 调用IoTApiUtil获取设备在线状态
            ApiResponse response = ioTApiUtil.getDeviceOnline(projectId).block();
            
            if (response != null && response.getCode() == 1) {
                // 直接返回IoT API的响应数据
                Map<String, Object> result = new HashMap<>();
                result.put("code", response.getCode());
                result.put("msg", response.getMsg());
                result.put("count", response.getCount());
                result.put("data", response.getData());
                
                return Result.OK(result);
            } else {
                log.error("获取设备状态失败: {}", response != null ? response.getMsg() : "响应为空");
                return Result.error("获取设备状态失败");
            }
        } catch (Exception e) {
            log.error("获取设备状态异常", e);
            return Result.error("获取设备状态异常：" + e.getMessage());
        }
    }
}