package org.jeecg.modules.youcai.entity.weatherEntity;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 和风天气API响应实体类
 */
@Data
public class QWeatherResponse {
    
    /**
     * 响应状态码
     */
    private String code;
    
    /**
     * 更新时间
     */
    private String updateTime;
    
    /**
     * 响应数据
     */
    private List<WeatherDaily> daily;
    
    /**
     * 引用来源信息
     */
    private Refer refer;
    
    /**
     * 天气预报数据
     */
    @Data
    public static class WeatherDaily {
        
        /**
         * 预报日期
         */
        private String fxDate;
        
        /**
         * 日出时间
         */
        private String sunrise;
        
        /**
         * 日落时间
         */
        private String sunset;
        
        /**
         * 月升时间
         */
        private String moonrise;
        
        /**
         * 月落时间
         */
        private String moonset;
        
        /**
         * 月相
         */
        private String moonPhase;
        
        /**
         * 最低温度
         */
        private String tempMin;
        
        /**
         * 最高温度
         */
        private String tempMax;
        
        /**
         * 白天天气状况代码
         */
        private String iconDay;
        
        /**
         * 晚间天气状况代码
         */
        private String iconNight;
        
        /**
         * 白天天气状况文字描述
         */
        private String textDay;
        
        /**
         * 晚间天气状况文字描述
         */
        private String textNight;
        
        /**
         * 风向360角度
         */
        private String wind360Day;
        
        /**
         * 风向
         */
        private String windDirDay;
        
        /**
         * 风力等级
         */
        private String windScaleDay;
        
        /**
         * 风速，公里/小时
         */
        private String windSpeedDay;
        
        /**
         * 降水量
         */
        private String precip;
        
        /**
         * 降水概率
         */
        private String pop;
        
        /**
         * 大气压强
         */
        private String pressure;
        
        /**
         * 相对湿度
         */
        private String humidity;
        
        /**
         * 能见度，公里
         */
        private String vis;
        
        /**
         * 云量
         */
        private String cloud;
        
        /**
         * 紫外线强度指数
         */
        private String uv;
    }
    
    /**
     * 引用来源信息
     */
    @Data
    public static class Refer {
        
        /**
         * 数据来源列表
         */
        private List<String> sources;
        
        /**
         * 许可证列表
         */
        private List<String> license;
    }
}