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
import org.jeecg.modules.youcai.util.QWeatherApiUtil;
import org.jeecg.common.exception.JeecgBootException;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * @Description: 倒伏风险预警表
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V1.0
 */
@Service
@Slf4j
public class YoucaiLodgingRiskWarningServiceImpl extends ServiceImpl<YoucaiLodgingRiskWarningMapper, YoucaiLodgingRiskWarning> implements IYoucaiLodgingRiskWarningService {
    
    @Autowired
    private YoucaiPlotsMapper youcaiPlotsMapper;

    @Autowired
    private YoucaiGrowthMonitoringMapper youcaiGrowthMonitoringMapper;

    @Autowired
    private YoucaiBasesMapper youcaiBasesMapper;
    
    @Autowired
    private IoTApiUtil ioTApiUtil;
    
    @Autowired
    private QWeatherApiUtil qWeatherApiUtil;
    
    @Override
    public LodgingRiskAssessmentResponseDTO riskAssessmentById(Integer plotId) {

        // 组装LodgingRiskAssessmentRequestDTO
        LodgingRiskAssessmentRequestDTO requestDTO = new LodgingRiskAssessmentRequestDTO();
        requestDTO.setPlotId(plotId);
        // 查询地块信息
        LambdaQueryWrapper<YoucaiPlots> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(YoucaiPlots::getId, plotId);
        YoucaiPlots plot = youcaiPlotsMapper.selectById(plotId);
         if(plot == null){
            return null;
        }

        //查询基地信息
        LambdaQueryWrapper<YoucaiBases> baseWrapper = new LambdaQueryWrapper<>();
        baseWrapper.eq(YoucaiBases::getId, plot.getBaseId());
        YoucaiBases base = youcaiBasesMapper.selectOne(baseWrapper);
        if(base == null){
            return null;
        }
        requestDTO.setSoilType(base.getSoilType());
        
        //根据地块id查询生长监测表，获取monitoring_date最新的记录
        LambdaQueryWrapper<YoucaiGrowthMonitoring> monitorWrapper = new LambdaQueryWrapper<>();
        monitorWrapper.eq(YoucaiGrowthMonitoring::getPlotId, plotId)
                      .orderByDesc(YoucaiGrowthMonitoring::getMonitoringDate)
                      .last("LIMIT 1");
        YoucaiGrowthMonitoring monitor = youcaiGrowthMonitoringMapper.selectOne(monitorWrapper);
        if(monitor == null){
            return null;
        }
        requestDTO.setGrowthStage(monitor.getGrowthStage());
        requestDTO.setPlantHeight(monitor.getPlantHeight());
        requestDTO.setStemDiameter(monitor.getStemDiameter());
        requestDTO.setDensity(monitor.getDensity());

        //直接set需要的参数
        requestDTO.setWindSpeed3d(0.0);
        requestDTO.setRainfall7d(0.0);
         //从第三方接口获取传感器数据
         try {
             // 获取项目列表
             Mono<ApiResponse> projectListMono = ioTApiUtil.getProjectList();
             ApiResponse res = projectListMono.block(); // 同步获取结果
            
             if (res != null && res.getCode() == 1 && res.getData() != null) {
                 int projectId = 0;

                 //解析项目列表，获得第一个项目ID
                 // 使用JSON序列化/反序列化正确处理类型转换
                 List<ProjectInfo> projectList;
                 if (res.getData() instanceof List) {
                     // 即使是List类型，也需要检查元素类型
                     java.util.List<?> tempList = (java.util.List<?>) res.getData();
                     if (!tempList.isEmpty() && tempList.get(0) instanceof ProjectInfo) {
                         // 如果元素已经是ProjectInfo类型，直接使用
                         projectList = (java.util.List<ProjectInfo>) tempList;
                     } else {
                         // 如果元素是LinkedHashMap或其他类型，需要通过JSON序列化/反序列化转换
                         String jsonData = JSONObject.toJSONString(res.getData());
                         projectList = JSONObject.parseArray(jsonData, ProjectInfo.class);
                     }
                 } else {
                     // 如果不是List类型，需要通过JSON序列化/反序列化转换
                     String jsonData = JSONObject.toJSONString(res.getData());
                     projectList = JSONObject.parseArray(jsonData, ProjectInfo.class);
                 }
                
                 if (!projectList.isEmpty()) {
                     System.out.print(projectList);
                     // 获取第一个项目的ID
                     ProjectInfo firstProject = projectList.get(0);
                     // 添加空指针检查，防止firstProject.getQ()为null
                     if (firstProject != null && firstProject.getQ() != null) {
                         projectId = firstProject.getQ().getId();
                     } else {
                         log.warn("项目信息为空，无法获取项目ID");
                         // 根据业务需求，可以选择使用默认值或抛出异常
                         // 这里选择使用默认值0
                         projectId = 0;
                     }
                 }

                 // 获取气象传感器列表（传感器类型ID为1表示气象传感器）
                 SensorListRequest sensorListRequest = new SensorListRequest();
                 sensorListRequest.setProjectId(projectId);
                 sensorListRequest.setSensorTypeId(1); // 1=气象传感器
                
                 Mono<ApiResponse> sensorListMono = ioTApiUtil.getSensorList(sensorListRequest);
                 ApiResponse sensorListResult = sensorListMono.block(); // 同步获取结果
                
                 if (sensorListResult != null && sensorListResult.getCode() == 1 && sensorListResult.getData() != null) {
                     // 获取第一个气象传感器的实时数据
                     // 这里简化处理，实际应用中可能需要根据地块位置选择最近的传感器
                     java.util.List<SensorInfo> sensorList;
                     if (sensorListResult.getData() instanceof java.util.List) {
                         // 如果已经是List类型，直接使用
                         sensorList = (java.util.List<SensorInfo>) sensorListResult.getData();
                     } else {
                         // 如果是LinkedHashMap或其他类型，需要通过JSON序列化/反序列化转换
                         String jsonData = JSONObject.toJSONString(sensorListResult.getData());
                         sensorList = JSONObject.parseArray(jsonData, SensorInfo.class);
                     }
                    
                     if (!sensorList.isEmpty()) {
                         // 获取第一个传感器的信息
                         Object firstSensor = sensorList.get(0);
                         if (firstSensor instanceof SensorInfo) {
                             SensorInfo sensorInfo = (SensorInfo) firstSensor;
                             String deviceCode = sensorInfo.getQ().getSensorSerial();
                            
                             // 获取传感器实时数据
                             Mono<ApiResponse> sensorDataMono = ioTApiUtil.getSensorRealTimeData(deviceCode);
                             ApiResponse sensorDataResult = sensorDataMono.block(); // 同步获取结果
                            
                             if (sensorDataResult != null && sensorDataResult.getCode() == 1 && sensorDataResult.getData() != null) {
                                 // 处理传感器数据，提取风速和降雨量
                                 processSensorData(sensorDataResult.getData(), requestDTO);
                             }
                         }
                     }
                 }
             }
         } catch (Exception e) {
             log.error("获取传感器数据失败，地块ID：{}，错误信息：{}", plotId, e.getMessage(), e);
             // 根据业务需求选择以下两种处理方式之一：
             // 方式1：不中断流程，使用默认值继续执行
             // requestDTO.setWindSpeed3d(0.0);
             // requestDTO.setRainfall7d(0.0);
            
             // 方式2：抛出JeecgBootException中断流程
             throw new JeecgBootException("获取传感器数据失败: " + e.getMessage());
         }


        // 调用和风天气API获取未来7天的天气预报
        try {
            // 构建经纬度坐标字符串，格式为"经度,纬度"，保留小数点后两位
            String location = String.format("%.2f,%.2f", base.getLongitude(), base.getLatitude());
            log.info("开始获取天气预报数据，地块ID: {},  经纬度: {}",
                    base.getId(),  location);
            
            // 获取7天天气预报
            List<DailyWeatherDTO> weatherList = qWeatherApiUtil.get7DayWeatherForecast(location).block();
            
            if (weatherList != null && !weatherList.isEmpty()) {
                requestDTO.setDailyWeather(weatherList);
                log.info("成功获取{}天天气预报数据并设置到请求DTO中", weatherList.size());
                
                // 打印每一天的转换后数据
                for (int i = 0; i < weatherList.size(); i++) {
                    DailyWeatherDTO weather = weatherList.get(i);
                    log.info("第{}天 - 日期: {}, 风速: {}m/s, 降雨量: {}mm", 
                            i+1, weather.getDate(), weather.getWindSpeed(), weather.getRainfall());
                }
            } else {
                log.warn("获取天气预报数据为空，使用默认值");
                // 可以设置默认的天气预报数据
                requestDTO.setDailyWeather(List.of());
            }
        } catch (Exception e) {
            log.error("获取天气预报失败，位置：{}，错误信息：{}", base.getLongitude() + "," + base.getLatitude(), e.getMessage(), e);
            // 根据业务需求选择以下两种处理方式之一：
            // 方式1：不中断流程，使用默认值继续执行
            // requestDTO.setDailyWeather(List.of());
            
            // 方式2：抛出JeecgBootException中断流程
            throw new JeecgBootException("获取天气预报失败: " + e.getMessage());
        }

        //调用

        // 调用原有的风险评估方法
        return new LodgingRiskAssessmentResponseDTO();
    }
    
    /**
     * 处理传感器数据，提取风速和降雨量信息
     * @param sensorDataResult 传感器数据结果
     * @param requestDTO 请求DTO
     */
    private void processSensorData(Object sensorDataResult, LodgingRiskAssessmentRequestDTO requestDTO) {
        try {
            java.util.List<SensorRealTimeData> dataList;
            if (sensorDataResult instanceof java.util.List) {
                // 如果已经是List类型，需要检查元素类型
                java.util.List<?> tempList = (java.util.List<?>) sensorDataResult;
                if (!tempList.isEmpty() && tempList.get(0) instanceof SensorRealTimeData) {
                    dataList = (java.util.List<SensorRealTimeData>) tempList;
                } else {
                    // 如果元素类型不是SensorRealTimeData，需要通过JSON序列化/反序列化转换
                    String jsonData = JSONObject.toJSONString(sensorDataResult);
                    dataList = JSONObject.parseArray(jsonData, SensorRealTimeData.class);
                }
            } else {
                // 如果不是List类型，需要通过JSON序列化/反序列化转换
                String jsonData = JSONObject.toJSONString(sensorDataResult);
                dataList = JSONObject.parseArray(jsonData, SensorRealTimeData.class);
            }
            
            // 遍历传感器数据，查找风速和降雨量
            for (SensorRealTimeData sensorData : dataList) {
                String name = sensorData.getName();
                
                // 根据传感器数据名称判断数据类型
                if (name != null && sensorData.getQ() != null) {
                    Double value = sensorData.getQ().getSensorNum();
                    
                    if (value != null) {
                        // 风速数据（单位：m/s）
                        if (name.contains("风速") || name.toLowerCase().contains("wind")) {
                            requestDTO.setWindSpeed3d(value);
                            log.info("获取到风速数据：{} m/s", value);
                        }
                        // 降雨量数据（单位：mm）
                        else if (name.contains("降雨") || name.contains("雨量") || name.toLowerCase().contains("rain")) {
                            requestDTO.setRainfall7d(value);
                            log.info("获取到降雨量数据：{} mm", value);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("处理传感器数据失败，错误信息：{}", e.getMessage(), e);
            // 根据业务需求选择以下两种处理方式之一：
            // 方式1：不中断流程，使用默认值继续执行
            // (无需处理，因为requestDTO已经初始化了默认值)
            
            // 方式2：抛出JeecgBootException中断流程
            throw new JeecgBootException("处理传感器数据失败: " + e.getMessage());
        }
    }
}


