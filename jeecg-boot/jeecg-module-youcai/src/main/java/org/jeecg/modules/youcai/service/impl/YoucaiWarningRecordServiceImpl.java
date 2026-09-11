package org.jeecg.modules.youcai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.modules.youcai.constant.WarningConstants;
import org.jeecg.modules.youcai.dto.*;
import org.jeecg.modules.youcai.entity.YoucaiBases;
import org.jeecg.modules.youcai.entity.YoucaiPlots;
import org.jeecg.modules.youcai.entity.YoucaiWarningRecord;
import org.jeecg.modules.youcai.mapper.YoucaiWarningRecordMapper;
import org.jeecg.modules.youcai.service.IYoucaiBasesService;
import org.jeecg.modules.youcai.service.IYoucaiLodgingRiskWarningService;
import org.jeecg.modules.youcai.service.IYoucaiPlotsService;
import org.jeecg.modules.youcai.service.IYoucaiWarningRecordService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 油菜预警记录Service实现
 * @Author: System
 * @Date: 2025-11-27
 */
@Service
@Slf4j
public class YoucaiWarningRecordServiceImpl extends ServiceImpl<YoucaiWarningRecordMapper, YoucaiWarningRecord>
        implements IYoucaiWarningRecordService {

    @Autowired
    private IYoucaiPlotsService plotsService;

    @Autowired
    private IYoucaiBasesService basesService;

    @Autowired
    private IYoucaiLodgingRiskWarningService lodgingRiskWarningService;

    @Override
    public List<FarmingWarningDTO> getWarningList(WarningQueryDTO queryDTO) {
        LambdaQueryWrapper<YoucaiWarningRecord> wrapper = new LambdaQueryWrapper<>();

        // 注意：delFlag字段有@TableLogic注解，MyBatis-Plus会自动添加delFlag=0的条件，无需手动添加

        // 预警类型
        if (queryDTO.getWarningType() != null && !queryDTO.getWarningType().isEmpty()) {
            wrapper.eq(YoucaiWarningRecord::getWarningType, queryDTO.getWarningType());
        }

        // 预警等级
        if (queryDTO.getLevel() != null && !queryDTO.getLevel().isEmpty()) {
            wrapper.eq(YoucaiWarningRecord::getLevel, queryDTO.getLevel());
        }

        // 预警状态（默认查询待处理）
        if (queryDTO.getWarningStatus() != null && !queryDTO.getWarningStatus().isEmpty()) {
            wrapper.eq(YoucaiWarningRecord::getWarningStatus, queryDTO.getWarningStatus());
        } else {
            wrapper.eq(YoucaiWarningRecord::getWarningStatus, WarningConstants.WARNING_STATUS_PENDING);
        }

        // 地块ID
        if (queryDTO.getPlotId() != null && !queryDTO.getPlotId().isEmpty()) {
            wrapper.eq(YoucaiWarningRecord::getPlotId, queryDTO.getPlotId());
        }

        // 基地ID
        if (queryDTO.getBaseId() != null && !queryDTO.getBaseId().isEmpty()) {
            wrapper.eq(YoucaiWarningRecord::getBaseId, queryDTO.getBaseId());
        }

        // 是否只查询未过期的预警（默认true）
        if (queryDTO.getOnlyValid() == null || queryDTO.getOnlyValid()) {
            wrapper.eq(YoucaiWarningRecord::getIsExpired, WarningConstants.EXPIRED_NO);
        }

        // 按预警日期倒序
        wrapper.orderByDesc(YoucaiWarningRecord::getWarningDate);

        // 数量限制
        if (queryDTO.getLimit() != null && queryDTO.getLimit() > 0) {
            wrapper.last("LIMIT " + queryDTO.getLimit());
        } else {
            wrapper.last("LIMIT " + WarningConstants.DEFAULT_LIMIT);
        }

        List<YoucaiWarningRecord> records = this.list(wrapper);

        // 转换为DTO
        return records.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateWarningStatus(String warningId, String status, String handler, String remark) {
        log.info("更新预警状态, id: {}, status: {}", warningId, status);
        
        // 参数验证
        if (!StringUtils.hasText(warningId) || !StringUtils.hasText(status)) {
            log.error("更新预警状态失败: 参数不完整");
            return false;
        }
        
        // 验证状态值是否有效
        if (!WarningConstants.WARNING_STATUS_PENDING.equals(status) && 
            !WarningConstants.WARNING_STATUS_PROCESSING.equals(status) && 
            !WarningConstants.WARNING_STATUS_RESOLVED.equals(status) && 
            !WarningConstants.WARNING_STATUS_IGNORED.equals(status)) {
            log.error("更新预警状态失败: 无效的状态值 - {}", status);
            return false;
        }
        
        YoucaiWarningRecord record = this.getById(warningId);
        if (record == null) {
            log.error("更新预警状态失败: 预警记录不存在, id: {}", warningId);
            return false;
        }

        record.setWarningStatus(status);
        record.setHandler(handler);
        record.setHandleTime(new Date());
        record.setHandleRemark(remark);
        record.setUpdateTime(new Date());
        
        boolean result = this.updateById(record);
        
        if (result) {
            log.info("更新预警状态成功, id: {}, status: {}", warningId, status);
        } else {
            log.error("更新预警状态失败, id: {}, status: {}", warningId, status);
        }
        
        return result;
    }

    @Override
    public WarningStatisticsDTO getWarningStatistics() {
        WarningStatisticsDTO stats = new WarningStatisticsDTO();

        // 查询所有待处理的有效预警
        // 注意：delFlag字段有@TableLogic注解，MyBatis-Plus会自动添加delFlag=0的条件，无需手动添加
        LambdaQueryWrapper<YoucaiWarningRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(YoucaiWarningRecord::getIsExpired, WarningConstants.EXPIRED_NO);

        List<YoucaiWarningRecord> allWarnings = this.list(wrapper);

        // 统计总数
        stats.setTotalCount((long) allWarnings.size());

        // 初始化统计Map
        Map<String, Long> typeCount = new HashMap<>();
        Map<String, Long> levelCount = new HashMap<>();
        Map<String, Long> statusCount = new HashMap<>();

        // 一次遍历完成所有统计
        for (YoucaiWarningRecord warning : allWarnings) {
            // 类型统计
            typeCount.merge(warning.getWarningType(), 1L, Long::sum);
            
            // 等级统计
            levelCount.merge(warning.getLevel(), 1L, Long::sum);
            
            // 状态统计
            statusCount.merge(warning.getWarningStatus(), 1L, Long::sum);
        }

        // 设置等级统计
        stats.setHighCount(levelCount.getOrDefault(WarningConstants.WARNING_LEVEL_HIGH, 0L));
        stats.setMediumCount(levelCount.getOrDefault(WarningConstants.WARNING_LEVEL_MEDIUM, 0L));
        stats.setLowCount(levelCount.getOrDefault(WarningConstants.WARNING_LEVEL_LOW, 0L));

        // 设置类型统计
        stats.setLodgingCount(typeCount.getOrDefault(WarningConstants.WARNING_TYPE_LODGING, 0L));
        stats.setDiseaseCount(typeCount.getOrDefault(WarningConstants.WARNING_TYPE_DISEASE, 0L));
        stats.setPestCount(typeCount.getOrDefault(WarningConstants.WARNING_TYPE_PEST, 0L));

        // 设置状态统计
        stats.setPendingCount(statusCount.getOrDefault(WarningConstants.WARNING_STATUS_PENDING, 0L));
        stats.setProcessingCount(statusCount.getOrDefault(WarningConstants.WARNING_STATUS_PROCESSING, 0L));
        stats.setResolvedCount(statusCount.getOrDefault(WarningConstants.WARNING_STATUS_RESOLVED, 0L));

        // 设置分组统计
        stats.setTypeDistribution(typeCount);
        stats.setLevelDistribution(levelCount);
        stats.setStatusDistribution(statusCount);

        return stats;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateAllWarnings() {
        log.info("开始生成所有地块的预警信息...");
        long startTime = System.currentTimeMillis();

        // 查询所有活跃地块
        LambdaQueryWrapper<YoucaiPlots> plotWrapper = new LambdaQueryWrapper<>();
        plotWrapper.eq(YoucaiPlots::getDelFlag, 0);
        List<YoucaiPlots> plots = plotsService.list(plotWrapper);

        if (plots == null || plots.isEmpty()) {
            log.warn("没有找到活跃地块");
            return;
        }

        int successCount = 0;
        int failCount = 0;

        // 为每个地块生成预警
        for (YoucaiPlots plot : plots) {
            try {
                // 生成倒伏风险预警
                boolean success = generateLodgingWarning(plot.getId());
                if (success) {
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (Exception e) {
                log.error("生成地块预警失败，地块ID: {}, 错误: {}", plot.getId(), e.getMessage(), e);
                failCount++;
            }
        }

        long endTime = System.currentTimeMillis();
        log.info("预警生成完成，总地块数: {}, 成功: {}, 失败: {}, 耗时: {}ms",
                plots.size(), successCount, failCount, endTime - startTime);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean generateLodgingWarning(String plotId) {
        try {
            // 1. 检查是否存在指定小时内的重复预警
            if (isDuplicateWarning(plotId, WarningConstants.WARNING_TYPE_LODGING, WarningConstants.DEFAULT_CHECK_DUPLICATE_HOURS)) {
                log.debug("地块{}在{}小时内已有倒伏预警，跳过生成", plotId, WarningConstants.DEFAULT_CHECK_DUPLICATE_HOURS);
                return false;
            }

            // 2. 调用Python服务获取倒伏风险评估
            LodgingRiskAssessmentResponseDTO riskData = lodgingRiskWarningService.riskAssessmentById(plotId);

            if (riskData == null || riskData.getCurrentRisk() == null) {
                log.warn("获取地块{}的倒伏风险评估失败或数据为空", plotId);
                return false;
            }

            LodgingRiskAssessmentResponseDTO.CurrentRiskDTO currentRisk = riskData.getCurrentRisk();

            // 3. 判断风险等级，只保存中危和高危预警
            String riskLevel = currentRisk.getRiskLevel();
            String level = mapRiskLevelToLevel(riskLevel);

            if (WarningConstants.WARNING_LEVEL_LOW.equals(level)) {
                log.debug("地块{}的倒伏风险为低危，不保存预警", plotId);
                return false;
            }

            // 4. 查询地块和基地信息
            YoucaiPlots plot = plotsService.getById(plotId);
            if (plot == null) {
                log.warn("地块不存在，ID: {}", plotId);
                return false;
            }

            YoucaiBases base = null;
            if (plot.getBaseId() != null) {
                base = basesService.getById(plot.getBaseId());
            }

            // 5. 创建预警记录
            YoucaiWarningRecord record = new YoucaiWarningRecord();
            record.setWarningType(WarningConstants.WARNING_TYPE_LODGING);
            record.setPlotId(plotId);
            record.setPlotName(plot.getPlotName());

            if (base != null) {
                record.setBaseId(base.getId());
                record.setBaseName(base.getBaseName());
            }

            record.setLevel(level);
            record.setTitle("倒伏风险预警 - " + riskLevel);
            record.setDescription(buildLodgingDescription(currentRisk));
            record.setRecommendation(buildLodgingRecommendation(riskData));
            record.setRiskScore(BigDecimal.valueOf(currentRisk.getRiskScore()));
            record.setWarningStatus(WarningConstants.DEFAULT_WARNING_STATUS);
            record.setWarningDate(new Date());
            record.setWarningData(riskData); // 保存完整数据快照
            record.setIsExpired(WarningConstants.EXPIRED_NO);
            record.setCreateTime(new Date());

            // 设置过期时间（48小时后）
            LocalDateTime expireTime = LocalDateTime.now().plusHours(WarningConstants.DEFAULT_EXPIRE_HOURS);
            record.setExpireTime(Date.from(expireTime.atZone(ZoneId.systemDefault()).toInstant()));

            // 6. 保存到数据库
            boolean saved = this.save(record);
            if (saved) {
                log.info("成功生成倒伏预警，地块: {}, 等级: {}", plot.getPlotName(), riskLevel);
            }

            return saved;

        } catch (Exception e) {
            log.error("生成倒伏预警失败，地块ID: {}, 错误: {}", plotId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void expireOldWarnings(int expireHours) {
        log.info("开始标记过期预警，超过{}小时未处理的待处理预警将被标记为过期", expireHours);

        LocalDateTime expireTime = LocalDateTime.now().minusHours(expireHours);
        Date expireDate = Date.from(expireTime.atZone(ZoneId.systemDefault()).toInstant());

        // 注意：delFlag字段有@TableLogic注解，MyBatis-Plus会自动添加delFlag=0的条件，无需手动添加
        LambdaQueryWrapper<YoucaiWarningRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(YoucaiWarningRecord::getWarningStatus, WarningConstants.WARNING_STATUS_PENDING)
                .eq(YoucaiWarningRecord::getIsExpired, WarningConstants.EXPIRED_NO)
                .lt(YoucaiWarningRecord::getWarningDate, expireDate);

        List<YoucaiWarningRecord> records = this.list(wrapper);

        if (records != null && !records.isEmpty()) {
            records.forEach(record -> {
                record.setIsExpired(WarningConstants.EXPIRED_YES);
                record.setUpdateTime(new Date());
            });
            this.updateBatchById(records);
            log.info("已标记{}条预警为过期", records.size());
        } else {
            log.info("没有需要标记为过期的预警");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cleanOldWarnings(int retentionDays) {
        log.info("开始清理历史预警数据，保留{}天内的记录", retentionDays);

        LocalDateTime cleanTime = LocalDateTime.now().minusDays(retentionDays);
        Date cleanDate = Date.from(cleanTime.atZone(ZoneId.systemDefault()).toInstant());

        // 注意：delFlag字段有@TableLogic注解，MyBatis-Plus会自动添加delFlag=0的条件，无需手动添加
        LambdaQueryWrapper<YoucaiWarningRecord> wrapper = new LambdaQueryWrapper<>();
        // 清理已解决和已忽略的预警记录
        wrapper.and(w -> w.eq(YoucaiWarningRecord::getWarningStatus, WarningConstants.WARNING_STATUS_RESOLVED)
                .or()
                .eq(YoucaiWarningRecord::getWarningStatus, WarningConstants.WARNING_STATUS_IGNORED))
                .lt(YoucaiWarningRecord::getUpdateTime, cleanDate);

        long count = this.count(wrapper);
        if (count > 0) {
            // 使用逻辑删除
            List<YoucaiWarningRecord> records = this.list(wrapper);
            records.forEach(record -> record.setDelFlag(WarningConstants.DEL_FLAG_DELETED));
            this.updateBatchById(records);
            log.info("已清理{}条已解决或已忽略的预警记录", count);
        } else {
            log.info("没有需要清理的历史记录");
        }
    }

    @Override
    public boolean isDuplicateWarning(String plotId, String warningType, int withinHours) {
        LocalDateTime checkTime = LocalDateTime.now().minusHours(withinHours);
        Date checkDate = Date.from(checkTime.atZone(ZoneId.systemDefault()).toInstant());

        // 注意：delFlag字段有@TableLogic注解，MyBatis-Plus会自动添加delFlag=0的条件，无需手动添加
        LambdaQueryWrapper<YoucaiWarningRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(YoucaiWarningRecord::getPlotId, plotId)
                .eq(YoucaiWarningRecord::getWarningType, warningType)
                .ge(YoucaiWarningRecord::getWarningDate, checkDate);

        return this.count(wrapper) > 0;
    }

    // ========== 私有辅助方法 ==========

    /**
     * 转换实体为DTO
     */
    private FarmingWarningDTO convertToDTO(YoucaiWarningRecord record) {
        FarmingWarningDTO dto = new FarmingWarningDTO();
        BeanUtils.copyProperties(record, dto);
        dto.setIsExpired(record.getIsExpired() == 1);
        return dto;
    }

    /**
     * 风险等级映射
     */
    private String mapRiskLevelToLevel(String riskLevel) {
        if (riskLevel == null) return WarningConstants.WARNING_LEVEL_LOW;
        if (riskLevel.contains("极高") || riskLevel.contains("严重")) {
            return WarningConstants.WARNING_LEVEL_HIGH;
        } else if (riskLevel.contains("高") || riskLevel.contains("中高")) {
            return WarningConstants.WARNING_LEVEL_HIGH;
        } else if (riskLevel.contains("中")) {
            return WarningConstants.WARNING_LEVEL_MEDIUM;
        } else {
            return WarningConstants.WARNING_LEVEL_LOW;
        }
    }

    /**
     * 构建倒伏预警描述
     */
    private String buildLodgingDescription(LodgingRiskAssessmentResponseDTO.CurrentRiskDTO currentRisk) {
        StringBuilder desc = new StringBuilder();
        desc.append("风险评分: ").append(currentRisk.getRiskScore()).append(", ");
        desc.append("风险等级: ").append(currentRisk.getRiskLevel());

        if (currentRisk.getHighRiskFactors() != null && !currentRisk.getHighRiskFactors().isEmpty()) {
            desc.append("。主要风险因素: ");
            List<String> factors = currentRisk.getHighRiskFactors().stream()
                    .map(LodgingRiskAssessmentResponseDTO.HighRiskFactorDTO::getName)
                    .collect(Collectors.toList());
            desc.append(String.join("、", factors));
        }

        return desc.toString();
    }

    /**
     * 构建倒伏预警建议
     */
    private String buildLodgingRecommendation(LodgingRiskAssessmentResponseDTO riskData) {
        if (riskData.getComprehensiveSuggestions() == null) {
            return "请及时关注地块情况";
        }

        StringBuilder recommendation = new StringBuilder();
        LodgingRiskAssessmentResponseDTO.ComprehensiveSuggestionsDTO suggestions = riskData.getComprehensiveSuggestions();

        if (suggestions.getImmediate() != null && !suggestions.getImmediate().isEmpty()) {
            recommendation.append("立即措施: ").append(String.join("；", suggestions.getImmediate())).append("。");
        }

        if (suggestions.getShortTerm() != null && !suggestions.getShortTerm().isEmpty()) {
            recommendation.append("短期措施: ").append(String.join("；", suggestions.getShortTerm())).append("。");
        }

        return recommendation.length() > 0 ? recommendation.toString() : "请及时关注地块情况";
    }
}
