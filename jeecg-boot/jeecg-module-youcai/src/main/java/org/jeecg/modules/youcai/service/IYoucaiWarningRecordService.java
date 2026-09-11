package org.jeecg.modules.youcai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.youcai.dto.FarmingWarningDTO;
import org.jeecg.modules.youcai.dto.WarningQueryDTO;
import org.jeecg.modules.youcai.dto.WarningStatisticsDTO;
import org.jeecg.modules.youcai.entity.YoucaiWarningRecord;

import java.util.List;

/**
 * @Description: 油菜预警记录Service接口
 * @Author: System
 * @Date: 2025-11-27
 */
public interface IYoucaiWarningRecordService extends IService<YoucaiWarningRecord> {

    /**
     * 获取农事预警列表（首页使用）
     * @param queryDTO 查询参数
     * @return 预警列表
     */
    List<FarmingWarningDTO> getWarningList(WarningQueryDTO queryDTO);

    /**
     * 更新预警状态
     * @param warningId 预警ID
     * @param status 新状态
     * @param handler 处理人
     * @param remark 处理备注
     * @return 是否成功
     */
    boolean updateWarningStatus(String warningId, String status, String handler, String remark);

    /**
     * 获取预警统计信息
     * @return 统计数据
     */
    WarningStatisticsDTO getWarningStatistics();

    /**
     * 生成所有地块的预警信息（定时任务调用）
     */
    void generateAllWarnings();

    /**
     * 生成单个地块的倒伏风险预警
     * @param plotId 地块ID
     * @return 是否成功
     */
    boolean generateLodgingWarning(String plotId);

    /**
     * 清理过期预警（定时任务调用）
     * @param expireHours 超过多少小时未处理视为过期
     */
    void expireOldWarnings(int expireHours);

    /**
     * 清理历史预警数据（定时任务调用）
     * @param retentionDays 保留天数
     */
    void cleanOldWarnings(int retentionDays);

    /**
     * 检查是否存在重复预警
     * @param plotId 地块ID
     * @param warningType 预警类型
     * @param withinHours 多少小时内
     * @return 是否重复
     */
    boolean isDuplicateWarning(String plotId, String warningType, int withinHours);

    /**
     * @Description: crop_nutrient_demand
     * @Author: jeecg-boot
     * @Date:   2025-12-02
     * @Version: V1.0
     */
    interface ICropNutrientDemandService extends IService<YoucaiWarningRecord.CropNutrientDemand> {

    }
}
