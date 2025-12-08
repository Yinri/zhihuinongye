package org.jeecg.modules.youcai.controller;

import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.youcai.entity.VarietyPlotUtilization;
import org.jeecg.modules.youcai.service.IVarietyPlotUtilizationService;

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
 * @Description: variety_plot_utilization
 * @Author: jeecg-boot
 * @Date:   2025-12-04
 * @Version: V1.0
 */
@Tag(name="variety_plot_utilization")
@RestController
@RequestMapping("/youcai/varietyPlotUtilization")
@Slf4j
public class VarietyPlotUtilizationController extends JeecgController<VarietyPlotUtilization, IVarietyPlotUtilizationService> {
	@Autowired
	private IVarietyPlotUtilizationService varietyPlotUtilizationService;
	
	/**
	 * 分页列表查询
	 *
	 * @param varietyPlotUtilization
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "variety_plot_utilization-分页列表查询")
	@Operation(summary="variety_plot_utilization-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<VarietyPlotUtilization>> queryPageList(VarietyPlotUtilization varietyPlotUtilization,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {


        QueryWrapper<VarietyPlotUtilization> queryWrapper = QueryGenerator.initQueryWrapper(varietyPlotUtilization, req.getParameterMap());
		Page<VarietyPlotUtilization> page = new Page<VarietyPlotUtilization>(pageNo, pageSize);
		IPage<VarietyPlotUtilization> pageList = varietyPlotUtilizationService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param varietyPlotUtilization
	 * @return
	 */
	@AutoLog(value = "variety_plot_utilization-添加")
	@Operation(summary="variety_plot_utilization-添加")
	@RequiresPermissions("youcai:variety_plot_utilization:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody VarietyPlotUtilization varietyPlotUtilization) {
		varietyPlotUtilizationService.save(varietyPlotUtilization);

		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param varietyPlotUtilization
	 * @return
	 */
	@AutoLog(value = "variety_plot_utilization-编辑")
	@Operation(summary="variety_plot_utilization-编辑")
	@RequiresPermissions("youcai:variety_plot_utilization:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody VarietyPlotUtilization varietyPlotUtilization) {
		varietyPlotUtilizationService.updateById(varietyPlotUtilization);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "variety_plot_utilization-通过id删除")
	@Operation(summary="variety_plot_utilization-通过id删除")
	@RequiresPermissions("youcai:variety_plot_utilization:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		varietyPlotUtilizationService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "variety_plot_utilization-批量删除")
	@Operation(summary="variety_plot_utilization-批量删除")
	@RequiresPermissions("youcai:variety_plot_utilization:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.varietyPlotUtilizationService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "variety_plot_utilization-通过id查询")
	@Operation(summary="variety_plot_utilization-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<VarietyPlotUtilization> queryById(@RequestParam(name="id",required=true) String id) {
		VarietyPlotUtilization varietyPlotUtilization = varietyPlotUtilizationService.getById(id);
		if(varietyPlotUtilization==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(varietyPlotUtilization);
	}
	 /**
	  * 按品种+地块+肥料ID查询利用率
	  */
	 @Operation(summary="品种-地块-肥料利用率-按VPF查询")
	 @GetMapping(value = "/getByVPF")
	 public Result<VarietyPlotUtilization> getByVPF(
			 @RequestParam(name="varietyId", required=true) String varietyId,
			 @RequestParam(name="plotId", required=true) String plotId,
			 @RequestParam(name="fertilizerId", required=true) String fertilizerId) {
		 // 1. 构造查询条件
		 QueryWrapper<VarietyPlotUtilization> queryWrapper = new QueryWrapper<>();
		 queryWrapper.eq("variety_id", varietyId)
				 .eq("plot_id", plotId)
				 .eq("fertilizer_id", fertilizerId);
		 // 2. 查询数据
		 VarietyPlotUtilization utilization = varietyPlotUtilizationService.getOne(queryWrapper);
		 // 3. 结果返回
		 if(utilization == null) {
			 return Result.error("未找到该品种+地块+肥料的利用率配置");
		 }
		 return Result.OK(utilization);
	 }
    /**
    * 导出excel
    *
    * @param request
    * @param varietyPlotUtilization
    */
    @RequiresPermissions("youcai:variety_plot_utilization:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, VarietyPlotUtilization varietyPlotUtilization) {
        return super.exportXls(request, varietyPlotUtilization, VarietyPlotUtilization.class, "variety_plot_utilization");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("youcai:variety_plot_utilization:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, VarietyPlotUtilization.class);
    }

}
