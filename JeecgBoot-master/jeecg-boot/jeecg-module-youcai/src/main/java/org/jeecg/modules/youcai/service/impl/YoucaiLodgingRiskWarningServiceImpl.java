package org.jeecg.modules.youcai.service.impl;

import org.jeecg.modules.youcai.dto.LodgingRiskAssessmentRequestDTO;
import org.jeecg.modules.youcai.dto.LodgingRiskAssessmentResponseDTO;
import org.jeecg.modules.youcai.dto.DailyWeatherDTO;
import org.jeecg.modules.youcai.entity.YoucaiBases;
import org.jeecg.modules.youcai.entity.YoucaiGrowthMonitoring;
import org.jeecg.modules.youcai.entity.YoucaiLodgingRiskWarning;
import org.jeecg.modules.youcai.entity.YoucaiPlots;
import org.jeecg.modules.youcai.entity.iotEntity.sensor.SensorListRequest;
import org.jeecg.modules.youcai.entity.iotEntity.ApiResponse;
import org.jeecg.modules.youcai.entity.iotEntity.project.ProjectInfo;
import org.jeecg.modules.youcai.entity.iotEntity.sensor.SensorInfo;
import org.jeecg.modules.youcai.entity.iotEntity.sensor.SensorRealTimeData;
import org.jeecg.modules.youcai.mapper.YoucaiBasesMapper;
import org.jeecg.modules.youcai.mapper.YoucaiGrowthMonitoringMapper;
import org.jeecg.modules.youcai.mapper.YoucaiLodgingRiskWarningMapper;
import org.jeecg.modules.youcai.mapper.YoucaiPlotsMapper;
import org.jeecg.modules.youcai.service.IYoucaiLodgingRiskWarningService;
import org.jeecg.modules.youcai.util.IoTApiUtil;
import org.jeecg.modules.youcai.util.PythonApiUtil;
import org.jeecg.modules.youcai.util.QWeatherApiUtil;
import org.jeecg.common.exception.JeecgBootException;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * @Description: 倒伏风险预警表（优化版本）
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V2.0
 */
@Service
@Slf4j
public class YoucaiLodgingRiskWarningServiceImpl extends ServiceImpl<YoucaiLodgingRiskWarningMapper, YoucaiLodgingRiskWarning> implements IYoucaiLodgingRiskWarningService {

    // 设置各API调用的超时时间（秒）
    private static final int IOT_API_TIMEOUT = 20; // 增加IoT API超时时间到10秒
    private static final int WEATHER_API_TIMEOUT = 10;
    private static final int PYTHON_API_TIMEOUT = 15;
    
    @Autowired
    private YoucaiPlotsMapper youcaiPlotsMapper;

    @Autowired
    private YoucaiBasesMapper youcaiBasesMapper;

    @Autowired
    private YoucaiGrowthMonitoringMapper youcaiGrowthMonitoringMapper;

    @Autowired
    private QWeatherApiUtil qWeatherApiUtil;

    @Autowired
    private IoTApiUtil ioTApiUtil;
    
    @Autowired
    private PythonApiUtil pythonApiUtil;
    
    @Override
    public LodgingRiskAssessmentResponseDTO riskAssessmentById(Integer plotId) {
        log.info("开始评估地块倒伏风险，地块ID: {}", plotId);
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 获取基础数据（地块、基地、生长监测数据）
            LodgingRiskAssessmentRequestDTO requestDTO = buildBasicRequestData(plotId);
            if (requestDTO == null) {
                log.warn("基础数据获取失败，地块ID: {}", plotId);
                return null;
            }
            
            // 2. 并行获取IoT传感器数据和天气预报数据
        CompletableFuture<Void> iotFuture = CompletableFuture.runAsync(() -> {
            try {
                fetchIoTSensorDataWithTimeout(requestDTO, plotId);
            } catch (Exception e) {
                log.error("获取IoT传感器数据失败: {}", e.getMessage(), e);
            }
        });

        CompletableFuture<Void> weatherFuture = CompletableFuture.runAsync(() -> {
            try {
                fetchWeatherDataWithTimeout(requestDTO, plotId);
            } catch (Exception e) {
                log.error("获取天气预报数据失败: {}", e.getMessage(), e);
            }
        });

        // 等待所有数据获取完成，设置超时
        try {
            CompletableFuture.allOf(iotFuture, weatherFuture)
                .get(20, TimeUnit.SECONDS);
        } catch (java.util.concurrent.TimeoutException e) {
            log.warn("获取IoT传感器数据和天气预报数据超时，地块ID: {}", plotId);
            // 取消未完成的任务
            iotFuture.cancel(true);
            weatherFuture.cancel(true);
        } catch (Exception e) {
            log.error("获取IoT传感器数据和天气预报数据异常: {}", e.getMessage(), e);
            // 取消未完成的任务
            iotFuture.cancel(true);
            weatherFuture.cancel(true);
        }
            
            // 3. 调用Python服务进行风险评估
            LodgingRiskAssessmentResponseDTO response = callPythonServiceWithTimeout(requestDTO);
            
            long endTime = System.currentTimeMillis();
            log.info("倒伏风险评估完成，地块ID: {}, 耗时: {}ms", plotId, endTime - startTime);
            
            return response;
        } catch (Exception e) {
            log.error("倒伏风险评估异常，地块ID: {}, 错误: {}", plotId, e.getMessage(), e);
            throw new JeecgBootException("倒伏风险评估失败: " + e.getMessage());
        }
    }
    
    /**
     * 构建基础请求数据
     */
    private LodgingRiskAssessmentRequestDTO buildBasicRequestData(Integer plotId) {
        // 创建请求DTO
        LodgingRiskAssessmentRequestDTO requestDTO = new LodgingRiskAssessmentRequestDTO();
        requestDTO.setPlotId(plotId);
        
        // 查询地块信息
        YoucaiPlots plot = youcaiPlotsMapper.selectById(plotId);
        if (plot == null) {
            log.warn("地块不存在，地块ID: {}", plotId);
            return null;
        }
        
        // 查询基地信息
        YoucaiBases base = youcaiBasesMapper.selectById(plot.getBaseId());
        if (base == null) {
            log.warn("基地不存在，基地ID: {}", plot.getBaseId());
            return null;
        }
        requestDTO.setSoilType(base.getSoilType());
        
        // 查询最新的生长监测数据
        LambdaQueryWrapper<YoucaiGrowthMonitoring> monitorWrapper = new LambdaQueryWrapper<>();
        monitorWrapper.eq(YoucaiGrowthMonitoring::getPlotId, plotId)
                      .orderByDesc(YoucaiGrowthMonitoring::getMonitoringDate)
                      .last("LIMIT 1");
        YoucaiGrowthMonitoring monitor = youcaiGrowthMonitoringMapper.selectOne(monitorWrapper);
        
        if (monitor == null) {
            log.warn("未找到生长监测数据，地块ID: {}", plotId);
            return null;
        }
        
        // 设置生长监测数据
        requestDTO.setGrowthStage(monitor.getGrowthStage());
        requestDTO.setPlantHeight(monitor.getPlantHeight());
        requestDTO.setStemDiameter(monitor.getStemDiameter());
        requestDTO.setDensity(monitor.getDensity());
        
        return requestDTO;
    }
    
    /**
     * 带超时控制的IoT传感器数据获取
     */
    private void fetchIoTSensorDataWithTimeout(LodgingRiskAssessmentRequestDTO requestDTO, Integer plotId) {
        try {
            log.debug("开始获取IoT传感器数据，地块ID: {}", plotId);
            
            // 获取项目列表，添加超时和错误处理
            Mono<ApiResponse> projectListMono = ioTApiUtil.getProjectList()
                .timeout(Duration.ofSeconds(IOT_API_TIMEOUT))
                .doOnError(e -> log.warn("获取IoT项目列表超时，地块ID: {}, 错误: {}", plotId, e.getMessage()))
                .switchIfEmpty(Mono.fromCallable(() -> {
                    log.warn("获取IoT项目列表返回空响应，地块ID: {}", plotId);
                    return createEmptyApiResponse();
                }));
            
            ApiResponse projectResponse = projectListMono.block();
            
            if (projectResponse == null || projectResponse.getCode() != 1 || projectResponse.getData() == null) {
                log.warn("获取IoT项目列表失败，地块ID: {}", plotId);
                return;
            }
            
            // 解析项目列表，获取第一个项目ID
            List<ProjectInfo> projectList = parseProjectList(projectResponse.getData());
            if (projectList.isEmpty()) {
                log.warn("IoT项目列表为空，地块ID: {}", plotId);
                return;
            }
            
            Integer projectId = projectList.get(0).getQ() != null ? 
                projectList.get(0).getQ().getId() : 0;
            
            if (projectId == 0) {
                log.warn("项目ID为空，地块ID: {}", plotId);
                return;
            }
            
            // 获取气象传感器列表，添加超时和错误处理
            SensorListRequest sensorListRequest = new SensorListRequest();
            sensorListRequest.setProjectId(projectId);
            sensorListRequest.setSensorTypeId(1); // 1=气象传感器
            
            Mono<ApiResponse> sensorListMono = ioTApiUtil.getSensorList(sensorListRequest)
                .timeout(Duration.ofSeconds(IOT_API_TIMEOUT))
                .doOnError(e -> log.warn("获取IoT传感器列表超时，地块ID: {}, 错误: {}", plotId, e.getMessage()))
                .switchIfEmpty(Mono.fromCallable(() -> {
                    log.warn("获取IoT传感器列表返回空响应，地块ID: {}", plotId);
                    return createEmptyApiResponse();
                }));
            
            ApiResponse sensorListResponse = sensorListMono.block();
            
            if (sensorListResponse == null || sensorListResponse.getCode() != 1 || sensorListResponse.getData() == null) {
                log.warn("获取IoT传感器列表失败，地块ID: {}", plotId);
                return;
            }
            
            // 解析传感器列表，获取第一个传感器
            List<SensorInfo> sensorList = parseSensorList(sensorListResponse.getData());
            if (sensorList.isEmpty()) {
                log.warn("IoT传感器列表为空，地块ID: {}", plotId);
                return;
            }
            
            String deviceCode = sensorList.get(0).getQ() != null ? 
                sensorList.get(0).getQ().getSensorSerial() : null;
            
            if (deviceCode == null) {
                log.warn("传感器设备编码为空，地块ID: {}", plotId);
                return;
            }
            
            // 获取传感器实时数据，添加超时和错误处理
            Mono<ApiResponse> sensorDataMono = ioTApiUtil.getSensorRealTimeData(deviceCode)
                .timeout(Duration.ofSeconds(IOT_API_TIMEOUT))
                .doOnError(e -> log.warn("获取IoT传感器实时数据超时，地块ID: {}, 错误: {}", plotId, e.getMessage()))
                .switchIfEmpty(Mono.fromCallable(() -> {
                    log.warn("获取IoT传感器实时数据返回空响应，地块ID: {}", plotId);
                    return createEmptyApiResponse();
                }));
            
            ApiResponse sensorDataResponse = sensorDataMono.block();
            
            if (sensorDataResponse == null || sensorDataResponse.getCode() != 1 || sensorDataResponse.getData() == null) {
                log.warn("获取IoT传感器实时数据失败，地块ID: {}", plotId);
                return;
            }
            
            // 解析传感器数据并设置到请求DTO
            processSensorDataOptimized(sensorDataResponse.getData(), requestDTO);
            
            log.debug("IoT传感器数据获取完成，地块ID: {}", plotId);
            
        } catch (Exception e) {
            log.error("获取IoT传感器数据异常，地块ID: {}, 错误: {}", plotId, e.getMessage(), e);
        }
    }
    
    /**
     * 创建空的API响应对象
     */
    private ApiResponse createEmptyApiResponse() {
        ApiResponse response = new ApiResponse();
        response.setCode(0);
        response.setMsg("请求超时或失败");
        response.setData(null);
        return response;
    }
    
    /**
     * 带超时控制的天气预报数据获取
     */
    private void fetchWeatherDataWithTimeout(LodgingRiskAssessmentRequestDTO requestDTO, Integer plotId) {
        try {
            log.debug("开始获取天气预报数据，地块ID: {}", plotId);
            
            // 根据地块ID查询所属基地，再获取基地的经纬度
            YoucaiPlots plot = youcaiPlotsMapper.selectById(plotId);
            if (plot == null) {
                log.warn("地块不存在，地块ID: {}", plotId);
                requestDTO.setDailyWeather(List.of());
                return;
            }
            
            YoucaiBases base = youcaiBasesMapper.selectById(plot.getBaseId());
            if (base == null) {
                log.warn("基地不存在，基地ID: {}", plot.getBaseId());
                requestDTO.setDailyWeather(List.of());
                return;
            }
            
            // 检查基地是否有经纬度信息
            if (base.getLongitude() == null || base.getLatitude() == null) {
                log.warn("基地经纬度信息不完整，基地ID: {}, 经度: {}, 纬度: {}", 
                    plot.getBaseId(), base.getLongitude(), base.getLatitude());
                requestDTO.setDailyWeather(List.of());
                return;
            }
            
            // 构建位置坐标字符串（经度,纬度）
            String location = base.getLongitude().toString() + "," + base.getLatitude().toString();
            log.debug("使用基地坐标获取天气: {}", location);
            
            // 获取7天天气预报，添加超时和错误处理
            List<DailyWeatherDTO> weatherList = qWeatherApiUtil.get7DayWeatherForecast(location)
                .timeout(Duration.ofSeconds(WEATHER_API_TIMEOUT))
                .doOnError(e -> log.warn("获取天气预报数据超时，地块ID: {}, 错误: {}", plotId, e.getMessage()))
                .onErrorReturn(List.of())
                .block();
            
            if (weatherList != null && !weatherList.isEmpty()) {
                requestDTO.setDailyWeather(weatherList);
                log.debug("成功获取{}天天气预报数据，地块ID: {}, 基地ID: {}", 
                    weatherList.size(), plotId, plot.getBaseId());
            } else {
                log.warn("获取天气预报数据为空，地块ID: {}, 基地ID: {}", plotId, plot.getBaseId());
                requestDTO.setDailyWeather(List.of());
            }
            
        } catch (Exception e) {
            log.error("获取天气预报数据异常，地块ID: {}, 错误: {}", plotId, e.getMessage(), e);
            // 设置空列表，避免后续调用出现空指针
            requestDTO.setDailyWeather(List.of());
        }
    }
    
    /**
     * 带超时控制的Python服务调用
     */
    private LodgingRiskAssessmentResponseDTO callPythonServiceWithTimeout(LodgingRiskAssessmentRequestDTO requestDTO) {
        try {
            log.debug("开始调用Python服务进行风险评估");
            
            // 调用Python服务，添加超时和错误处理
            LodgingRiskAssessmentResponseDTO response = pythonApiUtil.assessLodgingRisk(requestDTO)
                .timeout(Duration.ofSeconds(PYTHON_API_TIMEOUT))
                .doOnError(e -> log.warn("Python风险评估服务超时，错误: {}", e.getMessage()))
                .onErrorReturn(createEmptyLodgingRiskResponse())
                .block();
            
            if (response != null) {
                log.info("Python服务调用成功，返回倒伏风险评估结果");
                return response;
            } else {
                log.warn("Python服务返回结果为空，使用默认响应");
                return createEmptyLodgingRiskResponse();
            }
            
        } catch (Exception e) {
            log.error("调用Python服务异常，错误: {}", e.getMessage(), e);
            return createEmptyLodgingRiskResponse();
        }
    }
    
    /**
     * 创建空的倒伏风险评估响应对象
     */
    private LodgingRiskAssessmentResponseDTO createEmptyLodgingRiskResponse() {
        LodgingRiskAssessmentResponseDTO response = new LodgingRiskAssessmentResponseDTO();
        return response;
    }
    
    /**
     * 优化的项目列表解析方法
     */
    private List<ProjectInfo> parseProjectList(Object data) {
        if (data == null) {
            return List.of();
        }
        
        try {
            if (data instanceof List) {
                List<?> tempList = (List<?>) data;
                if (!tempList.isEmpty() && tempList.get(0) instanceof ProjectInfo) {
                    return (List<ProjectInfo>) tempList;
                }
            }
            
            // 使用JSON序列化/反序列化转换
            String jsonData = JSONObject.toJSONString(data);
            return JSONObject.parseArray(jsonData, ProjectInfo.class);
        } catch (Exception e) {
            log.error("解析项目列表失败: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * 优化的传感器列表解析方法
     */
    private List<SensorInfo> parseSensorList(Object data) {
        if (data == null) {
            return List.of();
        }
        
        try {
            if (data instanceof List) {
                List<?> tempList = (List<?>) data;
                if (!tempList.isEmpty() && tempList.get(0) instanceof SensorInfo) {
                    return (List<SensorInfo>) tempList;
                }
            }
            
            // 使用JSON序列化/反序列化转换
            String jsonData = JSONObject.toJSONString(data);
            return JSONObject.parseArray(jsonData, SensorInfo.class);
        } catch (Exception e) {
            log.error("解析传感器列表失败: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * 优化的传感器数据处理方法
     */
    private void processSensorDataOptimized(Object sensorDataResult, LodgingRiskAssessmentRequestDTO requestDTO) {
        if (sensorDataResult == null) {
            return;
        }
        
        try {
            List<SensorRealTimeData> dataList;
            
            if (sensorDataResult instanceof List) {
                List<?> tempList = (List<?>) sensorDataResult;
                if (!tempList.isEmpty() && tempList.get(0) instanceof SensorRealTimeData) {
                    dataList = (List<SensorRealTimeData>) tempList;
                } else {
                    String jsonData = JSONObject.toJSONString(sensorDataResult);
                    dataList = JSONObject.parseArray(jsonData, SensorRealTimeData.class);
                }
            } else {
                String jsonData = JSONObject.toJSONString(sensorDataResult);
                dataList = JSONObject.parseArray(jsonData, SensorRealTimeData.class);
            }
            
            // 遍历传感器数据，查找风速和降雨量
            for (SensorRealTimeData sensorData : dataList) {
                if (sensorData == null || sensorData.getName() == null || sensorData.getQ() == null) {
                    continue;
                }
                
                String name = sensorData.getName();
                Double value = sensorData.getQ().getSensorNum();
                
                if (value == null) {
                    continue;
                }
                
                // 根据传感器数据名称判断数据类型
                if (name.contains("风速")) {
                    requestDTO.setWindSpeed3d(value);
                    log.debug("获取到风速数据：{} m/s", value);
                } else if (name.contains("降雨量")) {
                    requestDTO.setRainfall7d(value);
                    log.debug("获取到降雨量数据：{} mm", value);
                }
            }
        } catch (Exception e) {
            log.error("处理传感器数据失败: {}", e.getMessage(), e);
            // 不抛出异常，允许流程继续
        }
    }
}


