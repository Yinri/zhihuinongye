package org.jeecg.modules.youcai.task;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.youcai.service.IYoucaiDecisionModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InsectTrendSuggestTask {

    @Autowired
    private IYoucaiDecisionModelService decisionModelService;

    /**
     * 每天凌晨生成基地今日虫情趋势与防治建议，避免大屏请求时等待大模型。
     */
    @Scheduled(cron = "${youcai.decision.insect-trend.cron:0 30 1 * * ?}")
    public void refreshInsectTrendSuggest() {
        log.info("========== 定时任务开始：生成基地今日虫情趋势与防治建议 ==========");
        try {
            decisionModelService.refreshInsectTrendSuggest();
            log.info("========== 定时任务完成：生成基地今日虫情趋势与防治建议 ==========");
        } catch (Exception e) {
            log.error("生成基地今日虫情趋势与防治建议失败", e);
        }
    }
}
