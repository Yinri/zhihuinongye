package org.jeecg.modules.youcai.entity.iotEntity.auth;

import lombok.Data;

/**
 * 登录响应（返回Token）
 */
@Data
public class LoginResponse {
    private String data; // JWT Token字符串（接口返回中data字段即为Token）
}