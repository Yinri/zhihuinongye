package org.jeecg.modules.youcai.controller;

import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.youcai.entity.YoucaiProductionPlans;
import org.jeecg.modules.youcai.service.IYoucaiProductionPlansService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.common.system.base.controller.JeecgController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.apache.shiro.authz.annotation.RequiresPermissions;
 /**
 * @Description: 生产计划表
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V1.0
 */
@Tag(name="生产计划表")
@RestController
@RequestMapping("/youcai/youcaiProductionPlans")
@Slf4j
public class YoucaiProductionPlansController extends JeecgController<YoucaiProductionPlans, IYoucaiProductionPlansService> {
	@Autowired
	private IYoucaiProductionPlansService youcaiProductionPlansService;
	
	/**
	 * 分页列表查询
	 *
	 * @param youcaiProductionPlans
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "生产计划表-分页列表查询")
	@Operation(summary="生产计划表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<YoucaiProductionPlans>> queryPageList(YoucaiProductionPlans youcaiProductionPlans,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {


        QueryWrapper<YoucaiProductionPlans> queryWrapper = QueryGenerator.initQueryWrapper(youcaiProductionPlans, req.getParameterMap());
		Page<YoucaiProductionPlans> page = new Page<YoucaiProductionPlans>(pageNo, pageSize);
		IPage<YoucaiProductionPlans> pageList = youcaiProductionPlansService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	 /**
	  * 通过plotId查询单个生产计划（新增接口）
	  * 适用于单个地块仅关联一个生产计划的场景
	  */
	 @Operation(summary="生产计划表-通过plotId查询单个计划")
	 @GetMapping(value = "/queryByPlotId")
	 public Result<YoucaiProductionPlans> queryByPlotId(@RequestParam(name="plotId", required=true) String plotId) {
		 QueryWrapper<YoucaiProductionPlans> queryWrapper = new QueryWrapper<>();
		 queryWrapper.eq("plot_id", plotId);

		 YoucaiProductionPlans productionPlan = youcaiProductionPlansService.getOne(queryWrapper);
		 if (productionPlan == null) {
			 return Result.OK(null); 
		 }
		 return Result.OK(productionPlan);
	 }

	 /**
	  * 生成生产计划（后端算法生成）
	  */
	 @Operation(summary="生产计划表-生成生产计划")
	 @GetMapping(value = "/generate")
	 public Result<YoucaiProductionPlans> generateProductionPlan(@RequestParam(name="plotId", required=true) String plotId) {
		 try {
			 YoucaiProductionPlans productionPlan = youcaiProductionPlansService.generateProductionPlan(plotId);
			 return Result.OK(productionPlan);
		 } catch (Exception e) {
			 log.error("生成生产计划失败: plotId={}, error={}", plotId, e.getMessage(), e);
			 return Result.error("生成生产计划失败: " + e.getMessage());
		 }
	 }
	/**
	 *   添加
	 *
	 * @param youcaiProductionPlans
	 * @return
	 */
	@AutoLog(value = "生产计划表-添加")
	@Operation(summary="生产计划表-添加")
//	@RequiresPermissions("youcai:youcai_production_plans:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody YoucaiProductionPlans youcaiProductionPlans) {
		youcaiProductionPlansService.save(youcaiProductionPlans);

		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param youcaiProductionPlans
	 * @return
	 */
	@AutoLog(value = "生产计划表-编辑")
	@Operation(summary="生产计划表-编辑")
	//@RequiresPermissions("youcai:youcai_production_plans:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody YoucaiProductionPlans youcaiProductionPlans) {
		youcaiProductionPlansService.updateById(youcaiProductionPlans);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "生产计划表-通过id删除")
	@Operation(summary="生产计划表-通过id删除")
	@RequiresPermissions("youcai:youcai_production_plans:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		youcaiProductionPlansService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "生产计划表-批量删除")
	@Operation(summary="生产计划表-批量删除")
	@RequiresPermissions("youcai:youcai_production_plans:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.youcaiProductionPlansService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "生产计划表-通过id查询")
	@Operation(summary="生产计划表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<YoucaiProductionPlans> queryById(@RequestParam(name="id",required=true) String id) {
		YoucaiProductionPlans youcaiProductionPlans = youcaiProductionPlansService.getById(id);
		if(youcaiProductionPlans==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(youcaiProductionPlans);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param youcaiProductionPlans
    */
    @RequiresPermissions("youcai:youcai_production_plans:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, YoucaiProductionPlans youcaiProductionPlans) {
        return super.exportXls(request, youcaiProductionPlans, YoucaiProductionPlans.class, "生产计划表");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("youcai:youcai_production_plans:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, YoucaiProductionPlans.class);
    }

}
