package org.jeecg.modules.youcai.service.impl;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.modules.youcai.dto.AiTaskRecordDTO;
import org.jeecg.modules.youcai.dto.AiTaskSubmitResponseDTO;
import org.jeecg.modules.youcai.service.IAiAnalysisTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@Slf4j
@Service
public class AiAnalysisTaskServiceImpl implements IAiAnalysisTaskService {
    private static final String TASK_KEY_PREFIX = "youcai:ai:task:";
    private static final String CACHE_KEY_PREFIX = "youcai:ai:cache:";
    private static final AtomicInteger THREAD_INDEX = new AtomicInteger(1);

    @Autowired
    private RedisUtil redisUtil;

    @Value("${youcai.ai.task-expire-seconds:21600}")
    private int taskExpireSeconds;

    @Value("${youcai.ai.cache-expire-seconds:43200}")
    private int cacheExpireSeconds;

    @Value("${youcai.ai.executor-size:4}")
    private int executorSize;

    private ExecutorService executorService;

    @PostConstruct
    public void initExecutor() {
        int poolSize = Math.max(1, executorSize);
        executorService = Executors.newFixedThreadPool(poolSize, new AiTaskThreadFactory());
    }

    @PreDestroy
    public void shutdownExecutor() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    @Override
    public AiTaskSubmitResponseDTO submitTask(String taskType, String cacheKey, Supplier<String> resultSupplier) {
        long now = System.currentTimeMillis();
        String taskId = UUID.randomUUID().toString().replace("-", "");
        String cachedResult = getCachedResult(cacheKey);

        AiTaskRecordDTO taskRecord = new AiTaskRecordDTO();
        taskRecord.setTaskId(taskId);
        taskRecord.setTaskType(taskType);
        taskRecord.setCacheKey(cacheKey);
        taskRecord.setCreatedTime(now);
        taskRecord.setUpdatedTime(now);

        AiTaskSubmitResponseDTO submitResponse = new AiTaskSubmitResponseDTO();
        submitResponse.setTaskId(taskId);

        if (StringUtils.hasText(cachedResult)) {
            taskRecord.setStatus(AiTaskRecordDTO.STATUS_SUCCESS);
            taskRecord.setCached(true);
            taskRecord.setResultJson(cachedResult);
            taskRecord.setFinishedTime(now);
            saveTask(taskRecord);

            submitResponse.setStatus(AiTaskRecordDTO.STATUS_SUCCESS);
            submitResponse.setCached(true);
            return submitResponse;
        }

        taskRecord.setStatus(AiTaskRecordDTO.STATUS_PENDING);
        taskRecord.setCached(false);
        saveTask(taskRecord);

        executorService.submit(() -> runTask(taskId, taskType, cacheKey, resultSupplier));

        submitResponse.setStatus(AiTaskRecordDTO.STATUS_PENDING);
        submitResponse.setCached(false);
        return submitResponse;
    }

    @Override
    public AiTaskRecordDTO getTask(String taskId) {
        Object raw = redisUtil.get(buildTaskKey(taskId));
        if (raw == null) {
            return null;
        }
        return JSON.parseObject(String.valueOf(raw), AiTaskRecordDTO.class);
    }

    private void runTask(String taskId, String taskType, String cacheKey, Supplier<String> resultSupplier) {
        AiTaskRecordDTO taskRecord = getTask(taskId);
        if (taskRecord == null) {
            return;
        }

        taskRecord.setStatus(AiTaskRecordDTO.STATUS_RUNNING);
        taskRecord.setUpdatedTime(System.currentTimeMillis());
        saveTask(taskRecord);

        try {
            String resultJson = resultSupplier.get();
            if (!StringUtils.hasText(resultJson)) {
                throw new IllegalStateException("AI分析结果为空");
            }
            taskRecord.setStatus(AiTaskRecordDTO.STATUS_SUCCESS);
            taskRecord.setResultJson(resultJson);
            taskRecord.setCached(false);
            taskRecord.setErrorMessage(null);
            taskRecord.setFinishedTime(System.currentTimeMillis());
            taskRecord.setUpdatedTime(taskRecord.getFinishedTime());
            saveTask(taskRecord);
            cacheResult(cacheKey, resultJson);
        } catch (Exception e) {
            log.error("AI任务执行失败, taskId={}, taskType={}", taskId, taskType, e);
            taskRecord.setStatus(AiTaskRecordDTO.STATUS_FAILED);
            taskRecord.setErrorMessage(e.getMessage());
            taskRecord.setFinishedTime(System.currentTimeMillis());
            taskRecord.setUpdatedTime(taskRecord.getFinishedTime());
            saveTask(taskRecord);
        }
    }

    private void saveTask(AiTaskRecordDTO taskRecord) {
        redisUtil.set(buildTaskKey(taskRecord.getTaskId()), JSON.toJSONString(taskRecord), taskExpireSeconds);
    }

    private void cacheResult(String cacheKey, String resultJson) {
        if (!StringUtils.hasText(cacheKey) || !StringUtils.hasText(resultJson)) {
            return;
        }
        redisUtil.set(buildCacheKey(cacheKey), resultJson, cacheExpireSeconds);
    }

    private String getCachedResult(String cacheKey) {
        if (!StringUtils.hasText(cacheKey)) {
            return null;
        }
        Object raw = redisUtil.get(buildCacheKey(cacheKey));
        return raw == null ? null : String.valueOf(raw);
    }

    private String buildTaskKey(String taskId) {
        return TASK_KEY_PREFIX + taskId;
    }

    private String buildCacheKey(String cacheKey) {
        return CACHE_KEY_PREFIX + cacheKey;
    }

    private static final class AiTaskThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setName("youcai-ai-task-" + THREAD_INDEX.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        }
    }
}
