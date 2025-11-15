package org.jeecg.modules.youcai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.youcai.entity.YoucaiFertilizerInputs;
import org.jeecg.modules.youcai.mapper.YoucaiFertilizerInputsMapper;
import org.jeecg.modules.youcai.service.IYoucaiFertilizerInputsService;
import org.springframework.stereotype.Service;

/**
 * 肥料投入表 Service 实现类（基础实现，可根据业务扩展）
 */
@Service
public class YoucaiFertilizerInputsServiceImpl extends ServiceImpl<YoucaiFertilizerInputsMapper, YoucaiFertilizerInputs> implements IYoucaiFertilizerInputsService {

    // 基础实现：继承 MyBatis-Plus 的默认方法（save、update、remove、list 等）
    // 后续需要扩展业务逻辑（如关联施肥记录、统计投入量等），可在此添加自定义方法
}