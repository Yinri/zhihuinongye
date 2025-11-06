package org.jeecg.modules.youcai.service;

import org.jeecg.modules.youcai.entity.YoucaiPesticideInputs;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: 农药投入表
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V1.0
 */
public interface IYoucaiPesticideInputsService extends IService<YoucaiPesticideInputs> {
    List<String> listDistinctPesticideNames();
}
