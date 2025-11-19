package org.jeecg.modules.youcai.service;

import org.jeecg.modules.youcai.entity.YoucaiSensorInfo;
import org.jeecg.modules.youcai.dto.WeatherSensorDataDTO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: 传感器信息表
 * @Author: jeecg-boot
 * @Date:   2025-11-10
 * @Version: V1.0
 */
public interface IYoucaiSensorInfoService extends IService<YoucaiSensorInfo> {
    
    /**
     * 获取基地的气象传感器实时数据
     * @param baseId 基地ID
     * @return 气象传感器数据
     */
    WeatherSensorDataDTO getWeatherSensorData(String baseId);
    
    /**
     * 获取基地的传感器列表
     * @param baseId 基地ID
     * @return 传感器列表
     */
    List<YoucaiSensorInfo> getBaseSensorList(String baseId);
    
    /**
     * 获取传感器历史数据
     * @param sensorId 传感器ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 历史数据
     */
    List<WeatherSensorDataDTO> getSensorHistoryData(String sensorId, String startDate, String endDate);
}