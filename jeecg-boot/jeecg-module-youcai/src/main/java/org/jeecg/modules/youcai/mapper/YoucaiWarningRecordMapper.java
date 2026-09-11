package org.jeecg.modules.youcai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.youcai.entity.YoucaiWarningRecord;

/**
 * @Description: 油菜预警记录表Mapper
 * @Author: System
 * @Date: 2025-11-27
 */
public interface YoucaiWarningRecordMapper extends BaseMapper<YoucaiWarningRecord> {

    /**
     * @Description: crop_nutrient_demand
     * @Author: jeecg-boot
     * @Date:   2025-12-02
     * @Version: V1.0
     */
    interface CropNutrientDemandMapper extends BaseMapper<YoucaiWarningRecord.CropNutrientDemand> {

    }
}
