package org.jeecg.modules.youcai.service;

import java.util.List;

/**
 * DashScope 多模态图片分析服务。
 */
public interface IDashScopeMultiModalService {

    /**
     * 使用图片 URL 列表和文本提示词调用 DashScope 多模态模型。
     *
     * @param imageUrls 图片 URL 列表
     * @param prompt 分析提示词
     * @return 模型返回的文本结果
     */
    String analyzeImages(List<String> imageUrls, String prompt);
}
