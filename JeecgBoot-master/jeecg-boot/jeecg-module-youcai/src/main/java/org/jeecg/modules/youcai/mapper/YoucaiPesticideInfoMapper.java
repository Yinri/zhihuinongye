package org.jeecg.modules.youcai.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.youcai.entity.YoucaiPesticideInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 农药信息表
 * @Author: jeecg-boot
 * @Date:   2025-10-30
 * @Version: V1.0
 */
public interface YoucaiPesticideInfoMapper extends BaseMapper<YoucaiPesticideInfo> {

    @Select("SELECT pesticide_name FROM youcai_pesticide_info")
    List<String> getAllPesticideNames();

}