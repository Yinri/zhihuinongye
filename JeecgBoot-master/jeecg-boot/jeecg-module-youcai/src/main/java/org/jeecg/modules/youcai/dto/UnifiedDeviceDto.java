package org.jeecg.modules.youcai.dto;

import lombok.Data;

@Data
public class UnifiedDeviceDto {
    private String deviceCode;
    private String lat;
    private String lng;
    private String deviceType;
}
