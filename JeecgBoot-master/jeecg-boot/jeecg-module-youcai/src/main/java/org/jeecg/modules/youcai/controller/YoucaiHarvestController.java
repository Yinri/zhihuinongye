package org.jeecg.modules.youcai.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.youcai.entity.YoucaiHarvest;
import org.jeecg.modules.youcai.dto.harvest.HarvestStatsDTO;
import org.jeecg.modules.youcai.dto.harvest.HarvesterStatusDTO;
import org.jeecg.modules.youcai.dto.harvest.YieldChartBarDTO;
import org.jeecg.modules.youcai.dto.harvest.PlotHarvestSummaryDTO;
import org.jeecg.modules.youcai.service.IYoucaiHarvestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * @Description: 收获管理
 * 路由前缀与前端保持一致：/rapeseed/harvest
 */
@Tag(name = "收获管理")
@RestController
@RequestMapping("/youcai/harvest")
@Slf4j
public class YoucaiHarvestController extends JeecgController<YoucaiHarvest, IYoucaiHarvestService> {

    @Autowired
    private IYoucaiHarvestService harvestService;

    /**
     * 分页列表查询
     */
    @Operation(summary = "收获管理-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<YoucaiHarvest>> queryPageList(YoucaiHarvest harvest,
                                                      @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                      @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                      HttpServletRequest req) {
        QueryWrapper<YoucaiHarvest> queryWrapper = QueryGenerator.initQueryWrapper(harvest, req.getParameterMap());
        Page<YoucaiHarvest> page = new Page<>(pageNo, pageSize);
        IPage<YoucaiHarvest> pageList = harvestService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 新增/保存
     */
    @AutoLog(value = "收获管理-保存")
    @Operation(summary = "收获管理-保存")
    @PostMapping(value = "/save")
    public Result<String> save(@RequestBody YoucaiHarvest harvest) {
        harvestService.save(harvest);
        return Result.OK("保存成功！");
    }

    /**
     * 编辑
     */
    @AutoLog(value = "收获管理-编辑")
    @Operation(summary = "收获管理-编辑")
    @PutMapping(value = "/edit")
    public Result<String> edit(@RequestBody YoucaiHarvest harvest) {
        harvestService.updateById(harvest);
        return Result.OK("编辑成功！");
    }

    /**
     * 通过id删除
     */
    @AutoLog(value = "收获管理-通过id删除")
    @Operation(summary = "收获管理-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        harvestService.removeById(id);
        return Result.OK("删除成功！");
    }

    /**
     * 批量删除
     */
    @AutoLog(value = "收获管理-批量删除")
    @Operation(summary = "收获管理-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.harvestService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功！");
    }

    /**
     * 通过id查询
     */
    @Operation(summary = "收获管理-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<YoucaiHarvest> queryById(@RequestParam(name = "id", required = true) String id) {
        YoucaiHarvest harvest = harvestService.getById(id);
        if (harvest == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(harvest);
    }

    /**
     * 导出excel（前端约定路径：/export）
     */
    @Operation(summary = "收获管理-导出Excel")
    @RequestMapping(value = "/export")
    public ModelAndView exportXls(HttpServletRequest request, YoucaiHarvest harvest) {
        return super.exportXls(request, harvest, YoucaiHarvest.class, "收获管理");
    }

    /**
     * 导入excel（前端约定路径：/import）
     */
    @Operation(summary = "收获管理-导入Excel")
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, YoucaiHarvest.class);
    }

    /**
     * 收获统计卡片数据
     */
    @Operation(summary = "收获管理-统计数据")
    @GetMapping(value = "/stats")
    public Result<HarvestStatsDTO> stats(@RequestParam(name = "baseId", required = false) Integer baseId) {
        HarvestStatsDTO dto = harvestService.getHarvestStats(baseId);
        return Result.OK(dto);
    }

    /**
     * 收割机状态列表
     */
    @Operation(summary = "收获管理-农机状态")
    @GetMapping(value = "/harvesterStatus")
    public Result<java.util.List<HarvesterStatusDTO>> harvesterStatus(@RequestParam(name = "baseId", required = false) Integer baseId) {
        java.util.List<HarvesterStatusDTO> list = harvestService.getHarvesterStatus(baseId);
        return Result.OK(list);
    }

    /**
     * 今日产量对比（按地块）
     */
    @Operation(summary = "收获管理-今日产量对比")
    @GetMapping(value = "/yieldChart")
    public Result<java.util.List<YieldChartBarDTO>> yieldChart(@RequestParam(name = "baseId", required = false) Integer baseId) {
        java.util.List<YieldChartBarDTO> list = harvestService.getYieldChart(baseId);
        return Result.OK(list);
    }

    /**
     * 地块收割派生视图汇总（地图着色）
     */
    @Operation(summary = "收获管理-地块收割汇总视图")
    @GetMapping(value = "/plotSummary")
    public Result<java.util.List<PlotHarvestSummaryDTO>> plotSummary(@RequestParam(name = "baseId", required = false) Integer baseId) {
        java.util.List<PlotHarvestSummaryDTO> list = harvestService.getPlotHarvestSummary(baseId);
        return Result.OK(list);
    }
}