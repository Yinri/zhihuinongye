package org.jeecg.modules.youcai.service.impl;

import org.jeecg.modules.youcai.entity.YoucaiPlots;
import org.jeecg.modules.youcai.entity.YoucaiBases;
import org.jeecg.modules.youcai.mapper.YoucaiPlotsMapper;
import org.jeecg.modules.youcai.service.IYoucaiPlotsService;
import org.jeecg.modules.youcai.service.IYoucaiBasesService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

/**
 * @Description: 地块信息表
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V1.0
 */
@Service
public class YoucaiPlotsServiceImpl extends ServiceImpl<YoucaiPlotsMapper, YoucaiPlots> implements IYoucaiPlotsService {
    
    @Autowired
    private IYoucaiBasesService youcaiBasesService;
    
    @Override
    public boolean save(YoucaiPlots entity) {
        // 生成地块编码
        generatePlotCode(entity);
        return super.save(entity);
    }
    
    @Override
    public boolean saveBatch(java.util.Collection<YoucaiPlots> entityList) {
        for (YoucaiPlots entity : entityList) {
            generatePlotCode(entity);
        }
        return super.saveBatch(entityList);
    }
    
    /**
     * 生成地块编码，格式为 {基地缩写}-{3位序号}
     * @param plot 地块实体
     */
    private void generatePlotCode(YoucaiPlots plot) {
        if (plot.getBaseId() == null || plot.getBaseId().isEmpty()) {
            return;
        }
        
        // 获取基地信息
        YoucaiBases base = youcaiBasesService.getById(plot.getBaseId());
        if (base == null || base.getCodePrefix() == null || base.getCodePrefix().isEmpty()) {
            return;
        }
        
        // 查询该基地下已有的地块，获取最大序号
        QueryWrapper<YoucaiPlots> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("base_id", plot.getBaseId());
        queryWrapper.likeRight("plot_code", base.getCodePrefix() + "-");
        queryWrapper.orderByDesc("plot_code");
        queryWrapper.last("LIMIT 1");
        
        YoucaiPlots lastPlot = this.getOne(queryWrapper);
        
        int sequence = 1; // 默认序号
        if (lastPlot != null && lastPlot.getPlotCode() != null) {
            try {
                String[] parts = lastPlot.getPlotCode().split("-");
                if (parts.length >= 2) {
                    sequence = Integer.parseInt(parts[1]) + 1;
                }
            } catch (NumberFormatException e) {
                // 解析失败，使用默认序号
                sequence = 1;
            }
        }
        
        // 生成地块编码，格式为 {基地缩写}-{3位序号}
        String plotCode = String.format("%s-%03d", base.getCodePrefix(), sequence);
        plot.setPlotCode(plotCode);
    }
}
