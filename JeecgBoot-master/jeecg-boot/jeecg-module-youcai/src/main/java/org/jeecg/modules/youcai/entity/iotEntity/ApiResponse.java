package org.jeecg.modules.youcai.entity.iotEntity;

import lombok.Data;

/**
 * 通用API返回结果封装（适配所有接口）
 * @param <T> 响应数据类型
 */
@Data
public class ApiResponse<T> {
    private int code;         // 状态码：1=成功，其他=失败
    private String msg;       // 提示信息（失败时非空）
    private int count;        // 数据总数（列表接口用）
    private T data;           // 业务数据（成功时非空）
}