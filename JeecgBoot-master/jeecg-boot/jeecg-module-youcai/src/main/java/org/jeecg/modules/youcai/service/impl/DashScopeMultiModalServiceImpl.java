package org.jeecg.modules.youcai.service.impl;

import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.youcai.service.IDashScopeMultiModalService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.time.Duration;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 基于 DashScope Java SDK 的多模态图片分析服务。
 */
@Slf4j
@Service
public class DashScopeMultiModalServiceImpl implements IDashScopeMultiModalService {
    private static final Duration DOWNLOAD_TIMEOUT = Duration.ofSeconds(60);
    private static final int MAX_IMAGE_BYTES = 10 * 1024 * 1024;
    private static final int MAX_ANALYSIS_IMAGES = 5;
    private static final int MAX_MODEL_RETRIES = 3;
    private static final int MAX_DOWNLOAD_RETRIES = 3;
    private static final long RETRY_BASE_DELAY_MS = 1000L;
    private static final AtomicInteger DOWNLOAD_THREAD_INDEX = new AtomicInteger(1);


    @Value("${dashscope.api-key:${DASHSCOPE_API_KEY:}}")
    private String dashscopeApiKey;

    @Value("${dashscope.base-url:https://dashscope.aliyuncs.com/api/v1}")
    private String baseUrl;

    @Value("${dashscope.model:qwen-vl-max-latest}")
    private String model;

    @Value("${dashscope.download-concurrency:4}")
    private int downloadConcurrency;

    @Value("${dashscope.image-cache-seconds:60}")
    private long imageCacheSeconds;

    @Value("${dashscope.analysis-cache-seconds:90}")
    private long analysisCacheSeconds;

    @Value("${dashscope.cache-max-entries:256}")
    private int cacheMaxEntries;

    private final WebClient webClient = WebClient.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(MAX_IMAGE_BYTES))
            .build();
    private final ConcurrentHashMap<String, CacheEntry<String>> imageDataUriCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CacheEntry<String>> analysisResultCache = new ConcurrentHashMap<>();
    private ExecutorService downloadExecutor;

    @PostConstruct
    public void configureDashScope() {
        if (StringUtils.hasText(baseUrl)) {
            Constants.baseHttpApiUrl = baseUrl;
        }
        int threadCount = Math.max(1, downloadConcurrency);
        this.downloadExecutor = Executors.newFixedThreadPool(threadCount, new DownloadThreadFactory());
    }

    @PreDestroy
    public void shutdownExecutor() {
        if (downloadExecutor != null) {
            downloadExecutor.shutdown();
        }
    }

    @Override
    public String analyzeImages(List<String> imageUrls, String prompt) {
        if (!StringUtils.hasText(dashscopeApiKey)) {
            throw new IllegalStateException("未配置 DashScope API Key");
        }
        List<String> validImageUrls = imageUrls == null ? Collections.emptyList() : imageUrls.stream()
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
        if (validImageUrls.isEmpty()) {
            throw new IllegalArgumentException("图片 URL 列表不能为空");
        }
        if (validImageUrls.size() > MAX_ANALYSIS_IMAGES) {
            log.info("AI图片分析数量超限，原始数量: {}，截断后数量: {}", validImageUrls.size(), MAX_ANALYSIS_IMAGES);
            validImageUrls = new ArrayList<>(validImageUrls.subList(0, MAX_ANALYSIS_IMAGES));
        }
        if (!StringUtils.hasText(prompt)) {
            throw new IllegalArgumentException("提示词不能为空");
        }

        try {
            String analysisCacheKey = buildAnalysisCacheKey(validImageUrls, prompt);
            String cachedResult = getCachedValue(analysisResultCache, analysisCacheKey);
            if (cachedResult != null) {
                return cachedResult;
            }

            List<Map<String, Object>> content = new ArrayList<>(buildImageContent(validImageUrls));
            content.add(Collections.singletonMap("text", prompt));

            MultiModalMessage userMessage = MultiModalMessage.builder()
                    .role(Role.USER.getValue())
                    .content(content)
                    .build();

            MultiModalConversationParam param = MultiModalConversationParam.builder()
                    .apiKey(dashscopeApiKey)
                    .model(model)
                    .messages(Collections.singletonList(userMessage))
                    .build();

            MultiModalConversationResult result = callConversationWithRetry(param, validImageUrls.size());
            Object text = result.getOutput()
                    .getChoices()
                    .get(0)
                    .getMessage()
                    .getContent()
                    .get(0)
                    .get("text");
            String responseText = text == null ? "" : String.valueOf(text);
            cacheValue(analysisResultCache, analysisCacheKey, responseText, analysisCacheSeconds);
            return responseText;
        } catch (Exception e) {
            log.error("DashScope 多模态分析失败，图片数: {}", validImageUrls.size(), e);
            throw new IllegalStateException("DashScope 多模态分析失败: " + e.getMessage(), e);
        }
    }

    private List<Map<String, Object>> buildImageContent(List<String> validImageUrls) {
        List<CompletableFuture<IndexedContent>> futures = IntStream.range(0, validImageUrls.size())
                .mapToObj(index -> CompletableFuture.supplyAsync(
                        () -> new IndexedContent(index,
                                Collections.singletonMap("image", getOrDownloadAsDataUri(validImageUrls.get(index)))),
                        downloadExecutor))
                .collect(Collectors.toList());

        return futures.stream()
                .map(CompletableFuture::join)
                .sorted(Comparator.comparingInt(IndexedContent::index))
                .map(IndexedContent::content)
                .collect(Collectors.toList());
    }

    private String getOrDownloadAsDataUri(String imageUrl) {
        String cachedDataUri = getCachedValue(imageDataUriCache, imageUrl);
        if (cachedDataUri != null) {
            return cachedDataUri;
        }
        String dataUri = downloadAsDataUri(imageUrl);
        cacheValue(imageDataUriCache, imageUrl, dataUri, imageCacheSeconds);
        return dataUri;
    }

    private String downloadAsDataUri(String imageUrl) {
        DownloadedImage downloadedImage = null;
        Exception lastException = null;
        for (int attempt = 1; attempt <= MAX_DOWNLOAD_RETRIES; attempt++) {
            try {
                downloadedImage = webClient.get()
                        .uri(imageUrl)
                        .exchangeToMono(response -> {
                            if (!response.statusCode().is2xxSuccessful()) {
                                return response.createException().flatMap(error ->
                                        reactor.core.publisher.Mono.error(new IllegalStateException("下载图片失败: " + error.getMessage())));
                            }
                            String contentType = response.headers().contentType()
                                    .map(Object::toString)
                                    .orElseGet(() -> inferMimeType(imageUrl));
                            return response.bodyToMono(byte[].class)
                                    .map(bytes -> new DownloadedImage(bytes, contentType));
                        })
                        .timeout(DOWNLOAD_TIMEOUT)
                        .block();
                break;
            } catch (Exception e) {
                lastException = e;
                if (attempt == MAX_DOWNLOAD_RETRIES) {
                    break;
                }
                log.warn("下载图片失败，准备重试，attempt={}, url={}", attempt, imageUrl, e);
                sleepQuietly(RETRY_BASE_DELAY_MS * attempt);
            }
        }

        if (downloadedImage == null && lastException != null) {
            throw new IllegalStateException("下载图片失败: " + lastException.getMessage(), lastException);
        }

        if (downloadedImage == null || downloadedImage.bytes.length == 0) {
            throw new IllegalStateException("下载图片失败: 空响应, url=" + imageUrl);
        }
        if (downloadedImage.bytes.length > MAX_IMAGE_BYTES) {
            throw new IllegalStateException("图片过大，超过限制: " + imageUrl);
        }

        String mimeType = StringUtils.hasText(downloadedImage.contentType)
                ? downloadedImage.contentType
                : inferMimeType(imageUrl);
        String base64 = Base64.getEncoder().encodeToString(downloadedImage.bytes);
        return "data:" + mimeType + ";base64," + base64;
    }

    private MultiModalConversationResult callConversationWithRetry(MultiModalConversationParam param, int imageCount) throws Exception {
        Exception lastException = null;
        for (int attempt = 1; attempt <= MAX_MODEL_RETRIES; attempt++) {
            try {
                MultiModalConversation conversation = new MultiModalConversation();
                return conversation.call(param);
            } catch (Exception e) {
                lastException = e;
                if (attempt == MAX_MODEL_RETRIES) {
                    break;
                }
                log.warn("DashScope 调用失败，准备重试，attempt={}, imageCount={}", attempt, imageCount, e);
                sleepQuietly(RETRY_BASE_DELAY_MS * attempt);
            }
        }
        throw lastException == null ? new IllegalStateException("DashScope 调用失败") : lastException;
    }

    private String buildAnalysisCacheKey(List<String> validImageUrls, String prompt) {
        String raw = model + "|" + prompt + "|" + String.join("||", validImageUrls);
        return sha256(raw);
    }

    private String getCachedValue(ConcurrentHashMap<String, CacheEntry<String>> cache, String key) {
        CacheEntry<String> entry = cache.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.isExpired()) {
            cache.remove(key, entry);
            return null;
        }
        return entry.value;
    }

    private void cacheValue(ConcurrentHashMap<String, CacheEntry<String>> cache, String key, String value, long ttlSeconds) {
        if (value == null || ttlSeconds <= 0) {
            return;
        }
        long expireAt = System.currentTimeMillis() + Duration.ofSeconds(ttlSeconds).toMillis();
        cache.put(key, new CacheEntry<>(value, expireAt));
        trimCache(cache);
    }

    private void trimCache(ConcurrentHashMap<String, CacheEntry<String>> cache) {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        if (cache.size() <= cacheMaxEntries) {
            return;
        }
        cache.entrySet().stream()
                .sorted(Comparator.comparingLong(entry -> entry.getValue().expireAtMillis))
                .limit(Math.max(1, cache.size() - cacheMaxEntries))
                .map(Map.Entry::getKey)
                .forEach(cache::remove);
    }

    private String sha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (Exception e) {
            throw new IllegalStateException("生成缓存键失败", e);
        }
    }

    private String inferMimeType(String imageUrl) {
        String lowerUrl = imageUrl == null ? "" : imageUrl.toLowerCase();
        int queryIndex = lowerUrl.indexOf('?');
        if (queryIndex >= 0) {
            lowerUrl = lowerUrl.substring(0, queryIndex);
        }
        if (lowerUrl.endsWith(".png")) {
            return "image/png";
        }
        if (lowerUrl.endsWith(".webp")) {
            return "image/webp";
        }
        if (lowerUrl.endsWith(".gif")) {
            return "image/gif";
        }
        return "image/jpeg";
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("重试等待被中断", e);
        }
    }

    private static final class DownloadedImage {
        private final byte[] bytes;
        private final String contentType;

        private DownloadedImage(byte[] bytes, String contentType) {
            this.bytes = bytes;
            this.contentType = contentType;
        }
    }

    private static final class IndexedContent {
        private final int index;
        private final Map<String, Object> content;

        private IndexedContent(int index, Map<String, Object> content) {
            this.index = index;
            this.content = content;
        }

        private int index() {
            return index;
        }

        private Map<String, Object> content() {
            return content;
        }
    }

    private static final class CacheEntry<T> {
        private final T value;
        private final long expireAtMillis;

        private CacheEntry(T value, long expireAtMillis) {
            this.value = value;
            this.expireAtMillis = expireAtMillis;
        }

        private boolean isExpired() {
            return System.currentTimeMillis() >= expireAtMillis;
        }
    }

    private static final class DownloadThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setName("dashscope-image-download-" + DOWNLOAD_THREAD_INDEX.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        }
    }
}
