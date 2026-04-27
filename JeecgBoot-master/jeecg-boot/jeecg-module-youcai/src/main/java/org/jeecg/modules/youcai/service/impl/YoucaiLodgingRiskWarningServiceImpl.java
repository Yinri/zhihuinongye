package org.jeecg.modules.youcai.service.impl;

import org.jeecg.modules.youcai.dto.LodgingRiskAssessmentRequestDTO;
import org.jeecg.modules.youcai.dto.LodgingRiskAssessmentResponseDTO;
import org.jeecg.modules.youcai.dto.DailyWeatherDTO;
import org.jeecg.modules.youcai.dto.VideoLodgingAnalysisResultDTO;
import org.jeecg.modules.youcai.entity.YoucaiBases;
import org.jeecg.modules.youcai.entity.YoucaiGrowthMonitoring;
import org.jeecg.modules.youcai.entity.YoucaiLodgingRiskWarning;
import org.jeecg.modules.youcai.entity.YoucaiPlots;
import org.jeecg.modules.youcai.entity.YoucaiProjectInfo;
import org.jeecg.modules.youcai.entity.YoucaiSensorInfo;
import org.jeecg.modules.youcai.entity.iotEntity.sensor.SensorListRequest;
import org.jeecg.modules.youcai.entity.iotEntity.ApiResponse;
import org.jeecg.modules.youcai.entity.iotEntity.project.ProjectInfo;
import org.jeecg.modules.youcai.entity.iotEntity.sensor.SensorInfo;
import org.jeecg.modules.youcai.entity.iotEntity.sensor.SensorRealTimeData;
import org.jeecg.modules.youcai.mapper.YoucaiBasesMapper;
import org.jeecg.modules.youcai.mapper.YoucaiGrowthMonitoringMapper;
import org.jeecg.modules.youcai.mapper.YoucaiLodgingRiskWarningMapper;
import org.jeecg.modules.youcai.mapper.YoucaiPlotsMapper;
import org.jeecg.modules.youcai.service.IDashScopeMultiModalService;
import org.jeecg.modules.youcai.service.IVideoSnapshotService;
import org.jeecg.modules.youcai.service.IYoucaiLodgingRiskWarningService;
import org.jeecg.modules.youcai.service.IYoucaiPlotsService;
import org.jeecg.modules.youcai.service.IYoucaiProjectInfoService;
import org.jeecg.modules.youcai.service.IYoucaiSensorInfoService;
import org.jeecg.modules.youcai.util.IoTApiUtil;
import org.jeecg.modules.youcai.util.PythonApiUtil;
import org.jeecg.modules.youcai.util.QWeatherApiUtil;
import org.jeecg.common.exception.JeecgBootException;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description: 倒伏风险预警表（优化版本）
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V2.0
 */
@Service
@Slf4j
public class YoucaiLodgingRiskWarningServiceImpl extends ServiceImpl<YoucaiLodgingRiskWarningMapper, YoucaiLodgingRiskWarning> implements IYoucaiLodgingRiskWarningService {
    
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
    
    @Autowired
    private IYoucaiProjectInfoService youcaiProjectInfoService;
    
    @Autowired
    private IYoucaiSensorInfoService youcaiSensorInfoService;
    
    @Autowired
    private IYoucaiPlotsService youcaiPlotsService;

    @Autowired
    private IVideoSnapshotService videoSnapshotService;

    @Autowired
    private IDashScopeMultiModalService dashScopeMultiModalService;
    
    @Override
    public LodgingRiskAssessmentResponseDTO riskAssessmentById(String plotId) {

        // 查询地块信息
        YoucaiPlots plot = youcaiPlotsMapper.selectById(plotId);
        if (plot == null) {
            log.warn("地块不存在，地块ID: {}", plotId);
            return null;
        }

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
                fetchIoTSensorData(requestDTO, plotId);
            } catch (Exception e) {
                log.error("获取IoT传感器数据失败: {}", e.getMessage(), e);
            }
        });

        CompletableFuture<Void> weatherFuture = CompletableFuture.runAsync(() -> {
            try {
                fetchWeatherData(requestDTO, plotId);
            } catch (Exception e) {
                log.error("获取天气预报数据失败: {}", e.getMessage(), e);
            }
        });

        // 等待所有数据获取完成
        try {
            CompletableFuture.allOf(iotFuture, weatherFuture)
                .get();
        } catch (Exception e) {
            log.error("获取IoT传感器数据和天气预报数据异常: {}", e.getMessage(), e);
            // 取消未完成的任务
            iotFuture.cancel(true);
            weatherFuture.cancel(true);
        }
            
            // 3. 调用Python服务进行风险评估
            LodgingRiskAssessmentResponseDTO response = callPythonService(requestDTO);
        response.setPlotId(plotId);
        response.setPlotName(plot.getPlotName());
            
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
    private LodgingRiskAssessmentRequestDTO buildBasicRequestData(String plotId) {
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
     * IoT传感器数据获取（优先查询本地数据库）
     */
    private void fetchIoTSensorData(LodgingRiskAssessmentRequestDTO requestDTO, String plotId) {
        try {
            log.debug("开始获取IoT传感器数据，地块ID: {}", plotId);


            YoucaiPlots plot = youcaiPlotsMapper.selectById(plotId);
            if (plot == null) {
                log.warn("地块不存在，地块ID: {}", plotId);
                requestDTO.setDailyWeather(List.of());
                return;
            }
            
            YoucaiBases base = youcaiBasesMapper.selectById(plot.getBaseId());
            //只获取基地名称的前2个字符
            String baseName = base != null ? base.getBaseName().substring(0, 2) : "未知基地";
            if (base == null) {
                log.warn("基地不存在，基地ID: {}", plot.getBaseId());
                return;
            }
            
            // 1. 优先查询本地数据库中的项目信息
            LambdaQueryWrapper<YoucaiProjectInfo> projectQuery = new LambdaQueryWrapper<>();
            projectQuery.eq(YoucaiProjectInfo::getIsDelete, 0)
                       .last("LIMIT 1"); // 只取第一个项目
            List<YoucaiProjectInfo> localProjects = youcaiProjectInfoService.list(projectQuery);
            
            Integer projectId = null;
            boolean needFetchProjectFromAPI = false;
            
            // 2. 如果本地有项目信息，使用本地项目ID
            if (localProjects != null && !localProjects.isEmpty()) {
                projectId = localProjects.get(0).getProjectId();
                log.debug("从本地数据库获取到项目ID: {}", projectId);
            } else {
                // 3. 如果本地没有项目信息，从API获取
                needFetchProjectFromAPI = true;
                log.debug("本地数据库无项目信息，将从API获取");
                
                // 获取项目列表
                ApiResponse projectResponse = ioTApiUtil.getProjectList().block();
                
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
                
                projectId = projectList.get(0).getQ() != null ? 
                    projectList.get(0).getQ().getId() : 0;
                
                if (projectId == 0) {
                    log.warn("项目ID为空，地块ID: {}", plotId);
                    return;
                }
                
                // 保存项目信息到本地数据库
                saveProjectInfoToLocalDB(projectList.get(0));
            }
            
            // 4. 查询本地数据库中的传感器信息
            LambdaQueryWrapper<YoucaiSensorInfo> sensorQuery = new LambdaQueryWrapper<>();
            sensorQuery.eq(YoucaiSensorInfo::getProjectId, projectId)
                      .eq(YoucaiSensorInfo::getSensorTypeId, 1) // 1=气象传感器
                      .eq(YoucaiSensorInfo::getIsDelete, 0)
                      .last("LIMIT 1"); // 只取第一个传感器
            List<YoucaiSensorInfo> localSensors = youcaiSensorInfoService.list(sensorQuery);
            
            String deviceCode = null;
            boolean needFetchSensorFromAPI = false;
            
            // 5. 如果本地有传感器信息，使用本地传感器信息
            if (localSensors != null && !localSensors.isEmpty()) {
                deviceCode = localSensors.get(0).getSensorSerial();
                log.debug("从本地数据库获取到传感器设备编码: {}", deviceCode);
            } else {
                // 6. 如果本地没有传感器信息，从API获取
                needFetchSensorFromAPI = true;
                log.debug("本地数据库无传感器信息，将从API获取");
                
                // 获取气象传感器列表
                SensorListRequest sensorListRequest = new SensorListRequest();
                sensorListRequest.setProjectId(projectId);
                sensorListRequest.setSensorTypeId(1); // 1=气象传感器
                
                ApiResponse sensorListResponse = ioTApiUtil.getSensorList(sensorListRequest).block();
                
                if (sensorListResponse == null || sensorListResponse.getCode() != 1 || sensorListResponse.getData() == null) {
                    log.warn("获取IoT传感器列表失败，地块ID: {}", plotId);
                    return;
                }
            
                // 解析传感器列表
                List<SensorInfo> sensorList = parseSensorList(sensorListResponse.getData());
                if (sensorList.isEmpty()) {
                    log.warn("IoT传感器列表为空，地块ID: {}", plotId);
                    return;
                }

                //比较传感器名称是否以基地名称开头 这里不只是只比较第一个传感器
                for (SensorInfo sensor : sensorList) {
                    if (sensor.getQ().getSensorName().startsWith(baseName)) {
                        log.debug("从IoT传感器列表获取到传感器设备编码: {}", sensor.getQ().getSensorSerial());
                        deviceCode = sensor.getQ().getSensorSerial();
                        break;
                    }
                }
                if (deviceCode == null) {
                    log.warn("IoT传感器列表中未找到以基地名称开头的传感器，地块ID: {}", plotId);
                    return;
                }
            }
                
            
            // 7. 获取传感器实时数据
            ApiResponse sensorDataResponse = ioTApiUtil.getSensorRealTimeData(deviceCode).block();
            
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
     * 天气预报数据获取
     */
    private void fetchWeatherData(LodgingRiskAssessmentRequestDTO requestDTO, String plotId) {
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
            
            // 获取7天天气预报
            List<DailyWeatherDTO> weatherList = qWeatherApiUtil.get7DayWeatherForecast(location)
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
     * Python服务调用
     */
    private LodgingRiskAssessmentResponseDTO callPythonService(LodgingRiskAssessmentRequestDTO requestDTO) {
        try {
            log.debug("开始调用Python服务进行风险评估");
            
            // 使用标志位记录是否发生错误
            final boolean[] errorOccurred = {false};
            
            // 调用Python服务
            LodgingRiskAssessmentResponseDTO response = pythonApiUtil.assessLodgingRisk(requestDTO)
                .doOnError(e -> {
                    errorOccurred[0] = true;
                    log.warn("Python风险评估服务调用失败，错误: {}", e.getMessage());
                })
                .onErrorReturn(createEmptyLodgingRiskResponse())
                .block();
            
            // 检查响应是否有效
            if (response != null) {
                // 检查响应是否为空响应（所有关键字段为null）
                boolean isEmptyResponse = response.getCurrentRisk() == null && 
                                           response.getForecast7Days() == null && 
                                           response.getComprehensiveSuggestions() == null;
                
                if (isEmptyResponse) {
                    if (errorOccurred[0]) {
                        log.warn("Python服务调用失败，返回空响应");
                    } else {
                        log.warn("Python服务返回空响应，可能服务未正确处理请求");
                    }
                } else {
                    log.info("Python服务调用成功，返回倒伏风险评估结果");
                }
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
    
    /**
     * 保存项目信息到本地数据库
     */
    private void saveProjectInfoToLocalDB(ProjectInfo projectInfo) {
        try {
            if (projectInfo == null || projectInfo.getQ() == null) {
                log.warn("项目信息为空，无法保存到本地数据库");
                return;
            }
            
            ProjectInfo.Project projectData = projectInfo.getQ();
            ProjectInfo.ProjectAdmin projectAdmin = projectInfo.getS();
            
            // 检查是否已存在相同的项目ID
            LambdaQueryWrapper<YoucaiProjectInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(YoucaiProjectInfo::getProjectId, projectData.getId())
                       .eq(YoucaiProjectInfo::getIsDelete, 0);
            YoucaiProjectInfo existingProject = youcaiProjectInfoService.getOne(queryWrapper);
            
            if (existingProject != null) {
                log.debug("项目ID {} 已存在于本地数据库，更新同步时间", projectData.getId());
                // 更新同步时间
                existingProject.setSyncTime(LocalDateTime.now());
                youcaiProjectInfoService.updateById(existingProject);
                return;
            }
            
            // 创建新的项目信息记录
            YoucaiProjectInfo youcaiProjectInfo = new YoucaiProjectInfo();
            youcaiProjectInfo.setProjectId(projectData.getId());
            youcaiProjectInfo.setProjectName(projectData.getProjectName());
            youcaiProjectInfo.setProjectCode(projectData.getProjectCode());
            youcaiProjectInfo.setProjectDesc(projectData.getProjectDesc());
            youcaiProjectInfo.setProjectAddress(projectData.getProjectAdress());
            youcaiProjectInfo.setLongitude(projectData.getLng());
            youcaiProjectInfo.setLatitude(projectData.getLat());
            youcaiProjectInfo.setAdminId(projectAdmin.getId());
            youcaiProjectInfo.setAdminUserName(projectAdmin.getUserName());
            youcaiProjectInfo.setAdminFullName(projectAdmin.getFullName());
            youcaiProjectInfo.setAdminPhone(projectAdmin.getPhone());
            youcaiProjectInfo.setIsDelete(0);
            youcaiProjectInfo.setProjectCreateTime(projectData.getDateCreated());
            youcaiProjectInfo.setSyncTime(LocalDateTime.now());
            youcaiProjectInfo.setCreateTime(LocalDateTime.now());
            
            boolean saved = youcaiProjectInfoService.save(youcaiProjectInfo);
            if (saved) {
                log.info("成功保存项目信息到本地数据库，项目ID: {}, 项目名称: {}", 
                    projectData.getId(), projectData.getProjectName());
            } else {
                log.warn("保存项目信息到本地数据库失败，项目ID: {}", projectData.getId());
            }
            
        } catch (Exception e) {
            log.error("保存项目信息到本地数据库异常: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 保存传感器信息到本地数据库
     */
    private void saveSensorInfoToLocalDB(SensorInfo sensorInfo, Integer projectId) {
        try {
            if (sensorInfo == null || sensorInfo.getQ() == null) {
                log.warn("传感器信息为空，无法保存到本地数据库");
                return;
            }
            
            SensorInfo.Sensor sensorData = sensorInfo.getQ();
            
            // 检查是否已存在相同的传感器ID
            LambdaQueryWrapper<YoucaiSensorInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(YoucaiSensorInfo::getSensorId, sensorData.getId())
                       .eq(YoucaiSensorInfo::getIsDelete, 0);
            YoucaiSensorInfo existingSensor = youcaiSensorInfoService.getOne(queryWrapper);
            
            if (existingSensor != null) {
                log.debug("传感器ID {} 已存在于本地数据库，更新同步时间", sensorData.getId());
                // 更新同步时间
                existingSensor.setSyncTime(LocalDateTime.now());
                youcaiSensorInfoService.updateById(existingSensor);
                return;
            }
            
            // 创建新的传感器信息记录
            YoucaiSensorInfo youcaiSensorInfo = new YoucaiSensorInfo();
            youcaiSensorInfo.setSensorId(sensorData.getId());
            youcaiSensorInfo.setSensorName(sensorData.getSensorName());
            youcaiSensorInfo.setSensorSerial(sensorData.getSensorSerial());
            youcaiSensorInfo.setSensorTypeId(sensorData.getSensorTypeId());
            youcaiSensorInfo.setSensorDataTypeIds(sensorData.getSensorDataTypeIds());
            youcaiSensorInfo.setSensorDataTypeNames(sensorData.getSensorDataTypeNames());
            youcaiSensorInfo.setNums(sensorData.getNums());
            youcaiSensorInfo.setTime(sensorData.getTime());
            youcaiSensorInfo.setState(sensorData.getState());
            youcaiSensorInfo.setLongitude(sensorData.getLng());
            youcaiSensorInfo.setLatitude(sensorData.getLat());
            youcaiSensorInfo.setProtocolName(sensorInfo.getTraName());
            youcaiSensorInfo.setIsDelete(0);
            youcaiSensorInfo.setSensorCreateTime(sensorData.getDateCreated());
            youcaiSensorInfo.setSyncTime(LocalDateTime.now());
            youcaiSensorInfo.setProjectId(projectId);
            youcaiSensorInfo.setCreateTime(new Date(System.currentTimeMillis()));
            
            boolean saved = youcaiSensorInfoService.save(youcaiSensorInfo);
            if (saved) {
                log.info("成功保存传感器信息到本地数据库，传感器ID: {}, 传感器名称: {}", 
                    sensorData.getId(), sensorData.getSensorName());
            } else {
                log.warn("保存传感器信息到本地数据库失败，传感器ID: {}", sensorData.getId());
            }
            
        } catch (Exception e) {
            log.error("保存传感器信息到本地数据库异常: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public LodgingRiskAssessmentResponseDTO.BatchLodgingRiskAssessmentResponseDTO batchRiskAssessmentByBaseId(String baseId) {
        log.info("开始批量评估基地下所有地块的倒伏风险，基地ID: {}", baseId);
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 验证基地是否存在
            YoucaiBases base = youcaiBasesMapper.selectById(baseId);
            if (base == null) {
                log.warn("基地不存在，基地ID: {}", baseId);
                return null;
            }
            
            // 2. 查询基地下所有地块
            LambdaQueryWrapper<YoucaiPlots> plotQuery = new LambdaQueryWrapper<>();
            plotQuery.eq(YoucaiPlots::getBaseId, baseId)
                    .eq(YoucaiPlots::getDelFlag, 0); // 只查询未删除的地块
            List<YoucaiPlots> plots = youcaiPlotsMapper.selectList(plotQuery);
            
            if (plots == null || plots.isEmpty()) {
                log.warn("基地下没有地块，基地ID: {}", baseId);
                return null;
            }
            
            // 3. 创建批量响应对象
            LodgingRiskAssessmentResponseDTO.BatchLodgingRiskAssessmentResponseDTO batchResponse = 
                new LodgingRiskAssessmentResponseDTO.BatchLodgingRiskAssessmentResponseDTO();
            batchResponse.setBaseId(baseId);
            batchResponse.setBaseName(base.getBaseName());
            batchResponse.setCalculationTime(new Date());
            
            // 4. 创建地块风险列表
            List<LodgingRiskAssessmentResponseDTO> plotRisks;
            
            // 5. 使用并行流处理每个地块的风险评估
         plotRisks = plots.parallelStream()
             .map(plot -> {
                 try {
                     // 调用单个地块风险评估方法
                     LodgingRiskAssessmentResponseDTO riskData = riskAssessmentById(plot.getId());
                     riskData.setPlotId(plot.getId());
                     riskData.setPlotName(plot.getPlotName());
                     log.info("riskData:{}",riskData);
                     return riskData;
                 } catch (Exception e) {
                     log.error("获取地块{}风险评估失败: {}", plot.getId(), e.getMessage());
                     // 返回一个空的风险评估对象
                     return new LodgingRiskAssessmentResponseDTO();
                 }
             })
             .filter(riskData -> riskData != null)
             .collect(Collectors.toList());
            
            // 8. 计算基地风险统计
            LodgingRiskAssessmentResponseDTO.BaseRiskStatisticsDTO baseStatistics = 
                calculateBaseRiskStatistics(plotRisks);
            
            // 9. 设置响应数据
            batchResponse.setPlotRisks(plotRisks);
            batchResponse.setBaseStatistics(baseStatistics);
            
            long endTime = System.currentTimeMillis();
            log.info("批量评估基地下所有地块的倒伏风险完成，基地ID: {}, 地块数量: {}, 耗时: {}ms", 
                baseId, plotRisks.size(), endTime - startTime);
            
            return batchResponse;
        } catch (Exception e) {
            log.error("批量评估基地下所有地块的倒伏风险异常，基地ID: {}, 错误: {}", baseId, e.getMessage(), e);
            throw new JeecgBootException("批量评估倒伏风险失败: " + e.getMessage());
        }
    }
    
    /**
     * 计算基地风险统计
     */
    private LodgingRiskAssessmentResponseDTO.BaseRiskStatisticsDTO calculateBaseRiskStatistics(
        List<LodgingRiskAssessmentResponseDTO> plotRisks) {
        
        LodgingRiskAssessmentResponseDTO.BaseRiskStatisticsDTO statistics = 
            new LodgingRiskAssessmentResponseDTO.BaseRiskStatisticsDTO();
        
        if (plotRisks == null || plotRisks.isEmpty()) {
            return statistics;
        }
        
        // 初始化统计变量
    int totalPlots = plotRisks.size();
    int highRiskPlots = 0;
    int mediumRiskPlots = 0;
    int lowRiskPlots = 0;
    int extremeRiskPlots = 0;
    double totalRiskScore = 0.0;
    double highestRiskScore = 0.0;
    String highestRiskPlotId = null;
        
        // 风险分布统计
        java.util.Map<String, Integer> riskDistribution = new java.util.HashMap<>();
        
        // 风险分布统计
        for (LodgingRiskAssessmentResponseDTO riskData : plotRisks) {
            if (riskData != null && riskData.getCurrentRisk() != null) {
                LodgingRiskAssessmentResponseDTO.CurrentRiskDTO currentRisk = riskData.getCurrentRisk();
                String riskLevel = currentRisk.getRiskLevel();
                Double riskScore = currentRisk.getRiskScore() != null ? 
                    currentRisk.getRiskScore() : 0.0;
                
                // 累计风险评分
                totalRiskScore += riskScore;
                
                // 记录最高风险评分
                if (riskScore > highestRiskScore) {
                    highestRiskScore = riskScore;
                    // 注意：这里无法直接获取地块ID，因为LodgingRiskAssessmentResponseDTO中没有地块ID字段
                    // 如果需要记录最高风险地块ID，需要在LodgingRiskAssessmentResponseDTO中添加地块ID字段
                }
                
                // 风险分布统计
                riskDistribution.put(riskLevel, riskDistribution.getOrDefault(riskLevel, 0) + 1);
                
                // 根据风险等级分类
            switch (riskLevel) {
                case "极高风险":
                    extremeRiskPlots++;
                    break;
                case "高风险":
                    highRiskPlots++;
                    break;
                case "中风险":
                    mediumRiskPlots++;
                    break;
                case "低风险":
                    lowRiskPlots++;
                    break;
                default:
                    // 默认归为中风险
                    mediumRiskPlots++;
                    break;
            }
            }
        }
        
        // 设置统计数据
    statistics.setTotalPlots(totalPlots);
    statistics.setHighRiskPlots(highRiskPlots);
    statistics.setMediumRiskPlots(mediumRiskPlots);
    statistics.setLowRiskPlots(lowRiskPlots);
    statistics.setExtremeRiskPlots(extremeRiskPlots);
    statistics.setAverageRiskScore(totalPlots > 0 ? totalRiskScore / totalPlots : 0.0);
    statistics.setHighestRiskPlotId(highestRiskPlotId);
    statistics.setHighestRiskScore(highestRiskScore);
    statistics.setRiskDistribution(riskDistribution);
        
        return statistics;
    }

    @Override
    public VideoLodgingAnalysisResultDTO analyzeLodgingByVideoId(String videoId) {
        log.info("开始视频倒伏分析，视频ID: {}", videoId);
        long startTime = System.currentTimeMillis();
        
        try {
            org.jeecg.modules.youcai.dto.VideoLodgingAnalysisResultDTO result = 
                new org.jeecg.modules.youcai.dto.VideoLodgingAnalysisResultDTO();
            result.setVideoId(videoId);
            result.setAnalysisTime(new Date());
            
            String imageUrl = videoSnapshotService.fetchVideoPhoto(videoId);
            if (imageUrl == null || imageUrl.isEmpty()) {
                log.warn("获取视频截图失败，视频ID: {}", videoId);
                throw new JeecgBootException("获取视频截图失败");
            }
            result.setImageUrl(imageUrl);
            log.info("获取到视频截图地址: {}", imageUrl);
            
            org.jeecg.modules.youcai.dto.VideoLodgingAnalysisResultDTO analysisResult = 
                callPythonLodgingAnalysis(imageUrl);
            if (analysisResult != null) {
                result.setLodgingRatio(analysisResult.getLodgingRatio());
                result.setLodgingArea(analysisResult.getLodgingArea());
                result.setTotalArea(analysisResult.getTotalArea());
                result.setRiskLevel(analysisResult.getRiskLevel());
                result.setConfidence(analysisResult.getConfidence());
                result.setDetails(analysisResult.getDetails());
                result.setSuggestions(analysisResult.getSuggestions());
            }
            
            long endTime = System.currentTimeMillis();
            log.info("视频倒伏分析完成，视频ID: {}, 耗时: {}ms", videoId, endTime - startTime);
            return result;
            
        } catch (Exception e) {
            log.error("视频倒伏分析失败，视频ID: {}, 错误: {}", videoId, e.getMessage(), e);
            throw new JeecgBootException("视频倒伏分析失败: " + e.getMessage());
        }
    }

    @Override
    public org.jeecg.modules.youcai.dto.VideoLodgingAnalysisResultDTO batchAnalyzeLodgingByVideoIds(List<String> videoIds) {
        log.info("开始批量视频倒伏分析，视频数量: {}", videoIds.size());
        long startTime = System.currentTimeMillis();
        
        org.jeecg.modules.youcai.dto.VideoLodgingAnalysisResultDTO combinedResult = 
            new org.jeecg.modules.youcai.dto.VideoLodgingAnalysisResultDTO();
        combinedResult.setAnalysisTime(new Date());
        
        double totalLodgingRatio = 0.0;
        double totalLodgingArea = 0.0;
        double totalArea = 0.0;
        double totalConfidence = 0.0;
        int successCount = 0;
        
        org.jeecg.modules.youcai.dto.VideoLodgingAnalysisResultDTO.DetailsDTO combinedDetails = 
            new org.jeecg.modules.youcai.dto.VideoLodgingAnalysisResultDTO.DetailsDTO();
        combinedDetails.setHealthyArea(0.0);
        combinedDetails.setMildLodgingArea(0.0);
        combinedDetails.setModerateLodgingArea(0.0);
        combinedDetails.setSevereLodgingArea(0.0);
        
        List<String> allSuggestions = new java.util.ArrayList<>();
        String highestRiskLevel = "低风险";
        String firstImageUrl = null;
        
        for (String videoId : videoIds) {
            try {
                org.jeecg.modules.youcai.dto.VideoLodgingAnalysisResultDTO singleResult = 
                    analyzeLodgingByVideoId(videoId);
                
                if (singleResult != null) {
                    successCount++;
                    
                    if (firstImageUrl == null && singleResult.getImageUrl() != null) {
                        firstImageUrl = singleResult.getImageUrl();
                    }
                    
                    if (singleResult.getLodgingRatio() != null) {
                        totalLodgingRatio += singleResult.getLodgingRatio();
                    }
                    if (singleResult.getLodgingArea() != null) {
                        totalLodgingArea += singleResult.getLodgingArea();
                    }
                    if (singleResult.getTotalArea() != null) {
                        totalArea += singleResult.getTotalArea();
                    }
                    if (singleResult.getConfidence() != null) {
                        totalConfidence += singleResult.getConfidence();
                    }
                    
                    if (singleResult.getDetails() != null) {
                        if (singleResult.getDetails().getHealthyArea() != null) {
                            combinedDetails.setHealthyArea(
                                combinedDetails.getHealthyArea() + singleResult.getDetails().getHealthyArea());
                        }
                        if (singleResult.getDetails().getMildLodgingArea() != null) {
                            combinedDetails.setMildLodgingArea(
                                combinedDetails.getMildLodgingArea() + singleResult.getDetails().getMildLodgingArea());
                        }
                        if (singleResult.getDetails().getModerateLodgingArea() != null) {
                            combinedDetails.setModerateLodgingArea(
                                combinedDetails.getModerateLodgingArea() + singleResult.getDetails().getModerateLodgingArea());
                        }
                        if (singleResult.getDetails().getSevereLodgingArea() != null) {
                            combinedDetails.setSevereLodgingArea(
                                combinedDetails.getSevereLodgingArea() + singleResult.getDetails().getSevereLodgingArea());
                        }
                    }
                    
                    if (singleResult.getSuggestions() != null) {
                        allSuggestions.addAll(singleResult.getSuggestions());
                    }
                    
                    if (singleResult.getRiskLevel() != null) {
                        highestRiskLevel = getHigherRiskLevel(highestRiskLevel, singleResult.getRiskLevel());
                    }
                }
            } catch (Exception e) {
                log.warn("单个视频分析失败，视频ID: {}, 错误: {}", videoId, e.getMessage());
            }
        }
        
        if (successCount > 0) {
            combinedResult.setVideoId(String.join(",", videoIds));
            combinedResult.setImageUrl(firstImageUrl);
            combinedResult.setLodgingRatio(totalLodgingRatio / successCount);
            combinedResult.setLodgingArea(totalLodgingArea);
            combinedResult.setTotalArea(totalArea);
            combinedResult.setRiskLevel(highestRiskLevel);
            combinedResult.setConfidence(totalConfidence / successCount);
            combinedResult.setDetails(combinedDetails);
            
            java.util.Set<String> uniqueSuggestions = new java.util.LinkedHashSet<>(allSuggestions);
            combinedResult.setSuggestions(new java.util.ArrayList<>(uniqueSuggestions));
        } else {
            combinedResult = createDefaultAnalysisResult();
        }
        
        long endTime = System.currentTimeMillis();
        log.info("批量视频倒伏分析完成，成功数量: {}/{}, 耗时: {}ms", 
            successCount, videoIds.size(), endTime - startTime);
        
        return combinedResult;
    }
    
    private String getHigherRiskLevel(String currentLevel, String newLevel) {
        java.util.Map<String, Integer> riskOrder = new java.util.HashMap<>();
        riskOrder.put("低风险", 1);
        riskOrder.put("中低风险", 2);
        riskOrder.put("中等风险", 3);
        riskOrder.put("高风险", 4);
        riskOrder.put("极高风险", 5);
        
        int currentOrder = riskOrder.getOrDefault(currentLevel, 0);
        int newOrder = riskOrder.getOrDefault(newLevel, 0);
        
        return newOrder > currentOrder ? newLevel : currentLevel;
    }
    
    private org.jeecg.modules.youcai.dto.VideoLodgingAnalysisResultDTO callPythonLodgingAnalysis(String imageUrl) {
        try {
            log.debug("调用 DashScope 进行倒伏分析，图片URL: {}", imageUrl);
            String responseText = dashScopeMultiModalService.analyzeImages(List.of(imageUrl), buildLodgingPrompt());
            org.jeecg.modules.youcai.dto.VideoLodgingAnalysisResultDTO result = parseLodgingAnalysisResponse(responseText);
            result.setImageUrl(imageUrl);
            return result;
        } catch (Exception e) {
            log.error("调用 DashScope 倒伏分析异常: {}", e.getMessage(), e);
            return createDefaultAnalysisResult();
        }
    }
    
    private org.jeecg.modules.youcai.dto.VideoLodgingAnalysisResultDTO createDefaultAnalysisResult() {
        org.jeecg.modules.youcai.dto.VideoLodgingAnalysisResultDTO result = 
            new org.jeecg.modules.youcai.dto.VideoLodgingAnalysisResultDTO();
        result.setLodgingRatio(0.0);
        result.setLodgingArea(0.0);
        result.setTotalArea(0.0);
        result.setRiskLevel("未知");
        result.setConfidence(0.0);
        
        org.jeecg.modules.youcai.dto.VideoLodgingAnalysisResultDTO.DetailsDTO details = 
            new org.jeecg.modules.youcai.dto.VideoLodgingAnalysisResultDTO.DetailsDTO();
        details.setHealthyArea(0.0);
        details.setMildLodgingArea(0.0);
        details.setModerateLodgingArea(0.0);
        details.setSevereLodgingArea(0.0);
        result.setDetails(details);
        
        result.setSuggestions(java.util.Arrays.asList("暂无分析结果，请稍后重试"));
        return result;
    }

    private String buildLodgingPrompt() {
        return "你是一名油菜倒伏风险识别专家。请仅基于当前图片判断油菜群体是否发生倒伏或存在明显倒伏风险，并严格输出 JSON，不要输出 Markdown 或代码块。"
                + "分析时重点关注油菜冠层倾斜、成片压伏、株体贴地、倒伏带分布、受风雨后偏倒方向、群体密度过大造成的互相压伏等特征。"
                + "不要把道路、沟渠、田埂、阴影、拍摄角度透视变形、收割痕迹或非油菜区域误判为倒伏。"
                + "如果画面中油菜主体过少、距离过远、遮挡严重或证据不足，请降低 confidence，并把 riskLevel 控制在低风险或中低风险，不要夸大。"
                + "suggestions 只输出与油菜倒伏防范或处置直接相关的建议，如排水、减轻田间积水、关注大风降雨后复查、分类收获等。"
                + "JSON 字段要求如下："
                + "lodgingRatio(0到100之间的倒伏比例),"
                + "lodgingArea(倒伏面积，单位平方米，可估算),"
                + "totalArea(总面积，单位平方米，可估算),"
                + "riskLevel(低风险/中低风险/中等风险/高风险/极高风险),"
                + "confidence(0到100之间的置信度),"
                + "details(对象，包含healthyArea、mildLodgingArea、moderateLodgingArea、severeLodgingArea),"
                + "suggestions(字符串数组)。"
                + "如果无法精确测量，请给出合理估计，并保持字段完整。";
    }

    private org.jeecg.modules.youcai.dto.VideoLodgingAnalysisResultDTO parseLodgingAnalysisResponse(String responseText) {
        org.jeecg.modules.youcai.dto.VideoLodgingAnalysisResultDTO result = createDefaultAnalysisResult();
        if (!StringUtils.hasText(responseText)) {
            return result;
        }

        try {
            String jsonText = extractJson(responseText);
            JSONObject jsonObject = JSONObject.parseObject(jsonText);
            result.setLodgingRatio(jsonObject.getDouble("lodgingRatio"));
            result.setLodgingArea(jsonObject.getDouble("lodgingArea"));
            result.setTotalArea(jsonObject.getDouble("totalArea"));
            result.setRiskLevel(jsonObject.getString("riskLevel"));
            result.setConfidence(jsonObject.getDouble("confidence"));

            JSONObject detailsJson = jsonObject.getJSONObject("details");
            if (detailsJson != null) {
                org.jeecg.modules.youcai.dto.VideoLodgingAnalysisResultDTO.DetailsDTO details =
                        new org.jeecg.modules.youcai.dto.VideoLodgingAnalysisResultDTO.DetailsDTO();
                details.setHealthyArea(detailsJson.getDouble("healthyArea"));
                details.setMildLodgingArea(detailsJson.getDouble("mildLodgingArea"));
                details.setModerateLodgingArea(detailsJson.getDouble("moderateLodgingArea"));
                details.setSevereLodgingArea(detailsJson.getDouble("severeLodgingArea"));
                result.setDetails(details);
            }

            if (jsonObject.containsKey("suggestions")) {
                result.setSuggestions(jsonObject.getJSONArray("suggestions").toJavaList(String.class));
            }
        } catch (Exception e) {
            log.warn("解析倒伏分析结果失败: {}", e.getMessage());
            result.setSuggestions(java.util.Arrays.asList(responseText));
        }
        return result;
    }

    private String extractJson(String text) {
        int start = text.indexOf("{");
        int end = text.lastIndexOf("}");
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }
}



