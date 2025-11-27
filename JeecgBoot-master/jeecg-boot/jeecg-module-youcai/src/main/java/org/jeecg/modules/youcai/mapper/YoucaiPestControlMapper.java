package org.jeecg.modules.youcai.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.youcai.entity.YoucaiPestControl;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * @Description: 虫害防控表
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V1.0
 */
public interface YoucaiPestControlMapper extends BaseMapper<YoucaiPestControl> {

      @Select("""
      SELECT *
      FROM youcai_pest_control
      WHERE plot_id = #{plotId}
        AND control_date BETWEEN #{start} AND #{end}
      ORDER BY control_date DESC
      """)
      List<YoucaiPestControl> queryControlHistory(@Param("plotId") String plotId,
                                                  @Param("start") String start,
                                                  @Param("end") String end);
}
