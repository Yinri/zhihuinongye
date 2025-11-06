package org.jeecg.modules.youcai.entity.iotEntity.auth;

import lombok.Data;
/**
 * 登录请求参数
 */
@Data
public class LoginRequest {
    private String userName; // 用户名（新账号：zxzhycadmin）
    private String password; // 密码：e3a3a21b0b3849938f8dd08e64db801f
}
