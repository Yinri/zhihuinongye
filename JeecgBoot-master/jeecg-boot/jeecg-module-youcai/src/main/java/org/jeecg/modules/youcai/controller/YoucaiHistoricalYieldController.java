package org.jeecg.modules.youcai.controller;

import java.util.Arrays;
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
 * @Description: youcai_historical_yield
 * @Author: jeecg-boot
 * @Date:   2025-11-05
 * @Version: V1.0
 */
@Tag(name="youcai_historical_yield")
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
	//@AutoLog(value = "youcai_historical_yield-分页列表查询")
	@Operation(summary="youcai_historical_yield-分页列表查询")
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
	@AutoLog(value = "youcai_historical_yield-添加")
	@Operation(summary="youcai_historical_yield-添加")
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
	@AutoLog(value = "youcai_historical_yield-编辑")
	@Operation(summary="youcai_historical_yield-编辑")
//	@RequiresPermissions("youcai:youcai_historical_yield:edit")
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
	@AutoLog(value = "youcai_historical_yield-通过id删除")
	@Operation(summary="youcai_historical_yield-通过id删除")
	@RequiresPermissions("youcai:youcai_historical_yield:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		youcaiHistoricalYieldService.removeById(id);
		return Result.OK("删除成功!");
	}
	 @AutoLog(value = "youcai_historical_yield-通过varietyId查询列表")
	 @Operation(summary = "youcai_historical_yield-通过varietyId查询列表")
	 @GetMapping(value = "/queryByVarietyId")
	 public Result<IPage<YoucaiHistoricalYield>> queryByVarietyId(
			 @RequestParam(name = "varietyId", required = false) String varietyId, // 新增品种ID参数，非必传
			 @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
			 @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
			 HttpServletRequest req) {

		 // 1. 初始化查询条件（支持前端传递其他筛选参数，与原有list接口逻辑一致）
		 QueryWrapper<YoucaiHistoricalYield> queryWrapper = QueryGenerator.initQueryWrapper(new YoucaiHistoricalYield(), req.getParameterMap());

		 // 2. 若传递了varietyId，追加等值查询条件
		 if (varietyId != null && !varietyId.trim().isEmpty()) {
			 queryWrapper.eq("variety_id", varietyId); // 注意：字段名需与数据库表中“品种ID”字段一致，若为其他名需修改
		 }

		 // 3. 执行分页查询（复用原有service方法，逻辑统一）
		 Page<YoucaiHistoricalYield> page = new Page<>(pageNo, pageSize);
		 IPage<YoucaiHistoricalYield> pageList = youcaiHistoricalYieldService.page(page, queryWrapper);

		 // 4. 返回结果（与原有接口格式一致，前端无需调整解析逻辑）
		 return Result.OK(pageList);
	 }
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "youcai_historical_yield-批量删除")
	@Operation(summary="youcai_historical_yield-批量删除")
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
	//@AutoLog(value = "youcai_historical_yield-通过id查询")
	@Operation(summary="youcai_historical_yield-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<YoucaiHistoricalYield> queryById(@RequestParam(name="id",required=true) String id) {
		YoucaiHistoricalYield youcaiHistoricalYield = youcaiHistoricalYieldService.getById(id);
		if(youcaiHistoricalYield==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(youcaiHistoricalYield);
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
        return super.exportXls(request, youcaiHistoricalYield, YoucaiHistoricalYield.class, "youcai_historical_yield");
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
