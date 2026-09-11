package org.jeecg.modules.youcai.constant;

/**
 * @Description: 预警相关常量
 * @Author: System
 * @Date: 2025-11-27
 */
public class WarningConstants {
    
    // 预警状态
    public static final String WARNING_STATUS_PENDING = "pending";
    public static final String WARNING_STATUS_PROCESSING = "processing";
    public static final String WARNING_STATUS_RESOLVED = "resolved";
    public static final String WARNING_STATUS_IGNORED = "ignored";
    
    // 预警等级
    public static final String WARNING_LEVEL_HIGH = "high";
    public static final String WARNING_LEVEL_MEDIUM = "medium";
    public static final String WARNING_LEVEL_LOW = "low";
    
    // 预警类型
    public static final String WARNING_TYPE_LODGING = "lodging";
    public static final String WARNING_TYPE_DISEASE = "disease";
    public static final String WARNING_TYPE_PEST = "pest";
    
    // 默认值
    public static final String DEFAULT_WARNING_STATUS = WARNING_STATUS_PENDING;
    public static final int DEFAULT_LIMIT = 20;
    public static final int DEFAULT_EXPIRE_HOURS = 48;
    public static final int DEFAULT_CHECK_DUPLICATE_HOURS = 24;
    
    // 删除标志
    public static final int DEL_FLAG_NORMAL = 0;
    public static final int DEL_FLAG_DELETED = 1;
    
    // 过期标志
    public static final int EXPIRED_NO = 0;
    public static final int EXPIRED_YES = 1;
    
    // 私有构造函数，防止实例化
    private WarningConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}