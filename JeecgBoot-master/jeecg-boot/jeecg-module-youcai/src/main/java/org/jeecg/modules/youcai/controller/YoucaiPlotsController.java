package org.jeecg.modules.youcai.controller;

import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.youcai.entity.YoucaiPlots;
import org.jeecg.modules.youcai.service.IYoucaiPlotsService;

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
 * @Description: 地块信息表
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V1.0
 */
@Tag(name="地块信息表")
@RestController
@RequestMapping("/youcai/youcaiPlots")
@Slf4j
public class YoucaiPlotsController extends JeecgController<YoucaiPlots, IYoucaiPlotsService> {
	@Autowired
	private IYoucaiPlotsService youcaiPlotsService;
	
	/**
	 * 分页列表查询
	 *
	 * @param youcaiPlots
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "地块信息表-分页列表查询")
	@Operation(summary="地块信息表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<YoucaiPlots>> queryPageList(YoucaiPlots youcaiPlots,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {


        QueryWrapper<YoucaiPlots> queryWrapper = QueryGenerator.initQueryWrapper(youcaiPlots, req.getParameterMap());
		Page<YoucaiPlots> page = new Page<YoucaiPlots>(pageNo, pageSize);
		IPage<YoucaiPlots> pageList = youcaiPlotsService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param youcaiPlots
	 * @return
	 */
	@AutoLog(value = "地块信息表-添加")
	@Operation(summary="地块信息表-添加")
//	@RequiresPermissions("youcai:youcai_plots:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody YoucaiPlots youcaiPlots) {
		youcaiPlotsService.save(youcaiPlots);

		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param youcaiPlots
	 * @return
	 */
	@AutoLog(value = "地块信息表-编辑")
	@Operation(summary="地块信息表-编辑")
	@RequiresPermissions("youcai:youcai_plots:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody YoucaiPlots youcaiPlots) {
		youcaiPlotsService.updateById(youcaiPlots);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "地块信息表-通过id删除")
	@Operation(summary="地块信息表-通过id删除")
	@RequiresPermissions("youcai:youcai_plots:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		youcaiPlotsService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "地块信息表-批量删除")
	@Operation(summary="地块信息表-批量删除")
	@RequiresPermissions("youcai:youcai_plots:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.youcaiPlotsService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "地块信息表-通过id查询")
	@Operation(summary="地块信息表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<YoucaiPlots> queryById(@RequestParam(name="id",required=true) String id) {
		YoucaiPlots youcaiPlots = youcaiPlotsService.getById(id);
		if(youcaiPlots==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(youcaiPlots);
	}
	 /**
	  * 通过baseid查询
	  *
	  * @param baseId
	  * @return
	  */
	 @Operation(summary = "地块信息表-通过baseId查询")
	 @GetMapping(value = "/queryByBaseId")
	 public Result<List<YoucaiPlots>> queryByBaseId(@RequestParam(name = "baseId", required = true) String baseId) {
		 QueryWrapper<YoucaiPlots> queryWrapper = new QueryWrapper<>();
		 // 注意这里使用数据库实际字段名 base_id，而不是代码中的属性名
		 queryWrapper.eq("base_id", baseId);
		 List<YoucaiPlots> youcaiPlotsList = youcaiPlotsService.list(queryWrapper);
		 return Result.OK(youcaiPlotsList);
	 }


    /**
    * 导出excel
    *
    * @param request
    * @param youcaiPlots
    */
    @RequiresPermissions("youcai:youcai_plots:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, YoucaiPlots youcaiPlots) {
        return super.exportXls(request, youcaiPlots, YoucaiPlots.class, "地块信息表");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("youcai:youcai_plots:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, YoucaiPlots.class);
    }

}
