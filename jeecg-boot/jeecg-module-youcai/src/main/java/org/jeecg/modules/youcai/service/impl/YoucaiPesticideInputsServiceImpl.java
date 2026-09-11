package org.jeecg.modules.youcai.service.impl;

import org.jeecg.modules.youcai.entity.YoucaiPesticideInputs;
import org.jeecg.modules.youcai.mapper.YoucaiPesticideInputsMapper;
import org.jeecg.modules.youcai.service.IYoucaiPesticideInputsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;

/**
 * @Description: 农药投入表
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V1.0
 */
@Service
public class YoucaiPesticideInputsServiceImpl extends ServiceImpl<YoucaiPesticideInputsMapper, YoucaiPesticideInputs> implements IYoucaiPesticideInputsService {
    @Autowired
    private YoucaiPesticideInputsMapper youcaiPesticideInputsMapper;
    @Override
    public List<String> listDistinctPesticideNames() {
        return youcaiPesticideInputsMapper.selectDistinctPesticideNames();
    }
}
