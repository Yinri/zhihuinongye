package org.jeecg.modules.youcai.task;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.youcai.service.IYoucaiWarningRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @Description: 预警生成定时任务
 * @Author: System
 * @Date: 2025-11-27
 */
@Component
@Slf4j
public class WarningGenerationTask {

    @Autowired
    private IYoucaiWarningRecordService warningRecordService;

    /**
     * 生成预警信息 - 每小时执行一次
     * cron表达式：秒 分 时 日 月 星期
     * 0 0 * * * ? 表示每小时的0分0秒执行
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void generateWarnings() {
        log.info("========== 定时任务开始：生成预警信息 ==========");
        try {
            warningRecordService.generateAllWarnings();
            log.info("========== 定时任务完成：生成预警信息 ==========");
        } catch (Exception e) {
            log.error("生成预警信息定时任务执行失败", e);
        }
    }

    /**
     * 标记过期预警 - 每天凌晨2点执行
     * 48小时未处理的待处理预警标记为过期
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void expireWarnings() {
        log.info("========== 定时任务开始：标记过期预警 ==========");
        try {
            warningRecordService.expireOldWarnings(48);
            log.info("========== 定时任务完成：标记过期预警 ==========");
        } catch (Exception e) {
            log.error("标记过期预警定时任务执行失败", e);
        }
    }

    /**
     * 清理历史预警 - 每天凌晨3点执行
     * 清理3个月之前的预警记录
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanWarnings() {
        log.info("========== 定时任务开始：清理历史预警 ==========");
        try {
            warningRecordService.cleanOldWarnings(90); // 保留90天（3个月）
            log.info("========== 定时任务完成：清理历史预警 ==========");
        } catch (Exception e) {
            log.error("清理历史预警定时任务执行失败", e);
        }
    }
}
