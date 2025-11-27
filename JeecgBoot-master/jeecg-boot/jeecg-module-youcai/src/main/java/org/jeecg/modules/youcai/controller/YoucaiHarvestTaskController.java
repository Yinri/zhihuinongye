package org.jeecg.modules.youcai.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.youcai.entity.YoucaiHarvestTask;
import org.jeecg.modules.youcai.service.IYoucaiHarvestTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

@Tag(name = "收获计划任务")
@RestController
@RequestMapping("/youcai/harvestTask")
public class YoucaiHarvestTaskController extends JeecgController<YoucaiHarvestTask, IYoucaiHarvestTaskService> {

    @Autowired
    private IYoucaiHarvestTaskService taskService;

    @Operation(summary = "收获计划-分页列表查询")
    @GetMapping("/list")
    public Result<IPage<YoucaiHarvestTask>> list(YoucaiHarvestTask task,
                                                 @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                 @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                 HttpServletRequest req) {
        QueryWrapper<YoucaiHarvestTask> queryWrapper = QueryGenerator.initQueryWrapper(task, req.getParameterMap());
        Page<YoucaiHarvestTask> page = new Page<>(pageNo, pageSize);
        IPage<YoucaiHarvestTask> pageList = taskService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @AutoLog("收获计划-保存")
    @Operation(summary = "收获计划-保存")
    @PostMapping("/save")
    public Result<String> save(@RequestBody YoucaiHarvestTask task) {
        taskService.save(task);
        return Result.OK("保存成功！");
    }

    @AutoLog("收获计划-编辑")
    @Operation(summary = "收获计划-编辑")
    @PutMapping("/edit")
    public Result<String> edit(@RequestBody YoucaiHarvestTask task) {
        taskService.updateById(task);
        return Result.OK("编辑成功！");
    }

    @AutoLog("收获计划-通过id删除")
    @Operation(summary = "收获计划-通过id删除")
    @DeleteMapping("/delete")
    public Result<String> delete(@RequestParam(name = "id") String id) {
        taskService.removeById(id);
        return Result.OK("删除成功！");
    }

    @AutoLog("收获计划-批量删除")
    @Operation(summary = "收获计划-批量删除")
    @DeleteMapping("/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids") String ids) {
        taskService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功！");
    }

    @Operation(summary = "收获计划-通过id查询")
    @GetMapping("/queryById")
    public Result<YoucaiHarvestTask> queryById(@RequestParam(name = "id") String id) {
        YoucaiHarvestTask task = taskService.getById(id);
        return task == null ? Result.error("未找到对应数据") : Result.OK(task);
    }

    @Operation(summary = "收获计划-导出Excel")
    @RequestMapping(value = "/export")
    public ModelAndView exportXls(HttpServletRequest request, YoucaiHarvestTask task) {
        return super.exportXls(request, task, YoucaiHarvestTask.class, "收获计划任务");
    }

    @Operation(summary = "收获计划-导入Excel")
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, YoucaiHarvestTask.class);
    }
}
