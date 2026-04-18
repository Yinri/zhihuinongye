package org.jeecg.modules.youcai.service.impl;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.youcai.service.IVideoSnapshotService;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * 视频截图服务实现
 */
@Slf4j
@Service
public class VideoSnapshotServiceImpl implements IVideoSnapshotService {

    private static final String VIDEO_PHOTO_API = "http://opencv.aheagle.com/videoOpenCv/wlwPhoto?videoId=";
    private static final String VIDEO_PHOTO_PREFIX = "https://img.aheagle.com/preview/yg-iot/";
    private static final String VIDEO_PHOTO_TOKEN = "?tk=cd1528943bae170aa6bc1450b5d78afa";

    @Override
    public String fetchVideoPhoto(String videoId) {
        try {
            String photoUrl = VIDEO_PHOTO_API + videoId;
            log.debug("请求视频截图: {}", photoUrl);

            String response = WebClient.builder()
                    .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                    .build()
                    .get()
                    .uri(photoUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            if (response == null) {
                return null;
            }

            JSONObject jsonResponse = JSONObject.parseObject(response);
            if (jsonResponse.getInteger("code") != 200) {
                return null;
            }

            String imagePath = jsonResponse.getString("data");
            if (imagePath == null || imagePath.isEmpty()) {
                return null;
            }

            return VIDEO_PHOTO_PREFIX + imagePath + VIDEO_PHOTO_TOKEN;
        } catch (Exception e) {
            log.error("获取视频截图失败: {}", e.getMessage(), e);
            return null;
        }
    }
}
