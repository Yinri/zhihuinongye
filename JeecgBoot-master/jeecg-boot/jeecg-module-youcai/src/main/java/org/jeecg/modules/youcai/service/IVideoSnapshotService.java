package org.jeecg.modules.youcai.service;

/**
 * 视频截图服务
 */
public interface IVideoSnapshotService {

    /**
     * 根据视频ID获取最新截图URL
     *
     * @param videoId 视频ID
     * @return 截图URL，获取失败时返回 null
     */
    String fetchVideoPhoto(String videoId);
}
