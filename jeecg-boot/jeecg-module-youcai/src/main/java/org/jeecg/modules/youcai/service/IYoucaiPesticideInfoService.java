package org.jeecg.modules.youcai.service;

import org.jeecg.modules.youcai.entity.YoucaiPesticideInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * @Description: 农药信息表
 * @Author: jeecg-boot
 * @Date:   2025-10-30
 * @Version: V1.0
 */
public interface IYoucaiPesticideInfoService extends IService<YoucaiPesticideInfo> {
    List<String> getAllNames();

}