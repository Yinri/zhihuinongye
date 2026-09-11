package org.jeecg.modules.youcai.mapper;

import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.youcai.entity.YoucaiPesticideInputs;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * @Description: 农药投入表
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V1.0
 */
public interface YoucaiPesticideInputsMapper extends BaseMapper<YoucaiPesticideInputs> {
    @Select("SELECT DISTINCT pesticide_name FROM youcai_pesticide_inputs WHERE del_flag = 0")
    List<String> selectDistinctPesticideNames();
}
