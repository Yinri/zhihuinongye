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
import org.jeecg.modules.youcai.entity.YoucaiHarvesterMachine;
import org.jeecg.modules.youcai.service.IYoucaiHarvesterMachineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

@Tag(name = "农机档案")
@RestController
@RequestMapping("/youcai/harvesterMachine")
public class YoucaiHarvesterMachineController extends JeecgController<YoucaiHarvesterMachine, IYoucaiHarvesterMachineService> {

    @Autowired
    private IYoucaiHarvesterMachineService machineService;

    @Operation(summary = "农机档案-分页列表查询")
    @GetMapping("/list")
    public Result<IPage<YoucaiHarvesterMachine>> list(YoucaiHarvesterMachine machine,
                                                     @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                     @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                     HttpServletRequest req) {
        QueryWrapper<YoucaiHarvesterMachine> queryWrapper = QueryGenerator.initQueryWrapper(machine, req.getParameterMap());
        Page<YoucaiHarvesterMachine> page = new Page<>(pageNo, pageSize);
        IPage<YoucaiHarvesterMachine> pageList = machineService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @AutoLog("农机档案-保存")
    @Operation(summary = "农机档案-保存")
    @PostMapping("/save")
    public Result<String> save(@RequestBody YoucaiHarvesterMachine machine) {
        machineService.save(machine);
        return Result.OK("保存成功！");
    }

    @AutoLog("农机档案-编辑")
    @Operation(summary = "农机档案-编辑")
    @PutMapping("/edit")
    public Result<String> edit(@RequestBody YoucaiHarvesterMachine machine) {
        machineService.updateById(machine);
        return Result.OK("编辑成功！");
    }

    @AutoLog("农机档案-通过id删除")
    @Operation(summary = "农机档案-通过id删除")
    @DeleteMapping("/delete")
    public Result<String> delete(@RequestParam(name = "id") String id) {
        machineService.removeById(id);
        return Result.OK("删除成功！");
    }

    @AutoLog("农机档案-批量删除")
    @Operation(summary = "农机档案-批量删除")
    @DeleteMapping("/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids") String ids) {
        machineService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功！");
    }

    @Operation(summary = "农机档案-通过id查询")
    @GetMapping("/queryById")
    public Result<YoucaiHarvesterMachine> queryById(@RequestParam(name = "id") String id) {
        YoucaiHarvesterMachine machine = machineService.getById(id);
        return machine == null ? Result.error("未找到对应数据") : Result.OK(machine);
    }

    @Operation(summary = "农机档案-导出Excel")
    @RequestMapping(value = "/export")
    public ModelAndView exportXls(HttpServletRequest request, YoucaiHarvesterMachine machine) {
        return super.exportXls(request, machine, YoucaiHarvesterMachine.class, "农机档案");
    }

    @Operation(summary = "农机档案-导入Excel")
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, YoucaiHarvesterMachine.class);
    }
}
