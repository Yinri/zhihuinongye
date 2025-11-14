package org.jeecg.modules.youcai.controller;

import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.youcai.entity.YoucaiHistoricalYield;
import org.jeecg.modules.youcai.service.IYoucaiHistoricalYieldService;

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
 * @Description: 油菜历史产量表
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V1.0
 */
@Tag(name="油菜历史产量表")
@RestController
@RequestMapping("/youcai/youcaiHistoricalYield")
@Slf4j
public class YoucaiHistoricalYieldController extends JeecgController<YoucaiHistoricalYield, IYoucaiHistoricalYieldService> {
	@Autowired
	private IYoucaiHistoricalYieldService youcaiHistoricalYieldService;
	
	/**
	 * 分页列表查询
	 *
	 * @param youcaiHistoricalYield
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "油菜历史产量表-分页列表查询")
	@Operation(summary="油菜历史产量表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<YoucaiHistoricalYield>> queryPageList(YoucaiHistoricalYield youcaiHistoricalYield,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<YoucaiHistoricalYield> queryWrapper = QueryGenerator.initQueryWrapper(youcaiHistoricalYield, req.getParameterMap());
		Page<YoucaiHistoricalYield> page = new Page<YoucaiHistoricalYield>(pageNo, pageSize);
		IPage<YoucaiHistoricalYield> pageList = youcaiHistoricalYieldService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param youcaiHistoricalYield
	 * @return
	 */
	@AutoLog(value = "油菜历史产量表-添加")
	@Operation(summary="油菜历史产量表-添加")
//	@RequiresPermissions("youcai:youcai_historical_yield:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody YoucaiHistoricalYield youcaiHistoricalYield) {
		youcaiHistoricalYieldService.save(youcaiHistoricalYield);
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param youcaiHistoricalYield
	 * @return
	 */
	@AutoLog(value = "油菜历史产量表-编辑")
	@Operation(summary="油菜历史产量表-编辑")
	@RequiresPermissions("youcai:youcai_historical_yield:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody YoucaiHistoricalYield youcaiHistoricalYield) {
		youcaiHistoricalYieldService.updateById(youcaiHistoricalYield);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "油菜历史产量表-通过id删除")
	@Operation(summary="油菜历史产量表-通过id删除")
	@RequiresPermissions("youcai:youcai_historical_yield:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		youcaiHistoricalYieldService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "油菜历史产量表-批量删除")
	@Operation(summary="油菜历史产量表-批量删除")
	@RequiresPermissions("youcai:youcai_historical_yield:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.youcaiHistoricalYieldService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "油菜历史产量表-通过id查询")
	@Operation(summary="油菜历史产量表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<YoucaiHistoricalYield> queryById(@RequestParam(name="id",required=true) String id) {
		YoucaiHistoricalYield youcaiHistoricalYield = youcaiHistoricalYieldService.getById(id);
		if(youcaiHistoricalYield==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(youcaiHistoricalYield);
	}
	
	/**
	 * 通过品种ID查询历年产量数据
	 *
	 * @param varietyId 品种ID
	 * @return 历年产量数据列表
	 */
	@AutoLog(value = "油菜历史产量表-通过品种ID查询历年产量数据")
	@Operation(summary="油菜历史产量表-通过品种ID查询历年产量数据")
	@GetMapping(value = "/getYieldByVarietyId")
	public Result<List<YoucaiHistoricalYield>> getYieldByVarietyId(@RequestParam(name="varietyId",required=true) Integer varietyId) {
		List<YoucaiHistoricalYield> yieldList = youcaiHistoricalYieldService.getYieldByVarietyId(varietyId);
		return Result.OK(yieldList);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param youcaiHistoricalYield
    */
    @RequiresPermissions("youcai:youcai_historical_yield:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, YoucaiHistoricalYield youcaiHistoricalYield) {
        return super.exportXls(request, youcaiHistoricalYield, YoucaiHistoricalYield.class, "油菜历史产量表");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("youcai:youcai_historical_yield:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, YoucaiHistoricalYield.class);
    }
}