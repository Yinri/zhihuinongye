package org.jeecg.modules.youcai.controller;

import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.youcai.entity.YoucaiSowingMonitoring;
import org.jeecg.modules.youcai.service.IYoucaiSowingMonitoringService;

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
 * @Description: 播种监控表
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V1.0
 */
@Tag(name="播种监控表")
@RestController
@RequestMapping("/youcai/youcaiSowingMonitoring")
@Slf4j
public class YoucaiSowingMonitoringController extends JeecgController<YoucaiSowingMonitoring, IYoucaiSowingMonitoringService> {
	@Autowired
	private IYoucaiSowingMonitoringService youcaiSowingMonitoringService;
	
	/**
	 * 分页列表查询
	 *
	 * @param youcaiSowingMonitoring
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "播种监控表-分页列表查询")
	@Operation(summary="播种监控表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<YoucaiSowingMonitoring>> queryPageList(YoucaiSowingMonitoring youcaiSowingMonitoring,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {


        QueryWrapper<YoucaiSowingMonitoring> queryWrapper = QueryGenerator.initQueryWrapper(youcaiSowingMonitoring, req.getParameterMap());
		Page<YoucaiSowingMonitoring> page = new Page<YoucaiSowingMonitoring>(pageNo, pageSize);
		IPage<YoucaiSowingMonitoring> pageList = youcaiSowingMonitoringService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param youcaiSowingMonitoring
	 * @return
	 */
	@AutoLog(value = "播种监控表-添加")
	@Operation(summary="播种监控表-添加")
	@RequiresPermissions("youcai:youcai_sowing_monitoring:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody YoucaiSowingMonitoring youcaiSowingMonitoring) {
		youcaiSowingMonitoringService.save(youcaiSowingMonitoring);

		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param youcaiSowingMonitoring
	 * @return
	 */
	@AutoLog(value = "播种监控表-编辑")
	@Operation(summary="播种监控表-编辑")
	@RequiresPermissions("youcai:youcai_sowing_monitoring:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody YoucaiSowingMonitoring youcaiSowingMonitoring) {
		youcaiSowingMonitoringService.updateById(youcaiSowingMonitoring);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "播种监控表-通过id删除")
	@Operation(summary="播种监控表-通过id删除")
	@RequiresPermissions("youcai:youcai_sowing_monitoring:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		youcaiSowingMonitoringService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "播种监控表-批量删除")
	@Operation(summary="播种监控表-批量删除")
	@RequiresPermissions("youcai:youcai_sowing_monitoring:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.youcaiSowingMonitoringService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "播种监控表-通过id查询")
	@Operation(summary="播种监控表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<YoucaiSowingMonitoring> queryById(@RequestParam(name="id",required=true) String id) {
		YoucaiSowingMonitoring youcaiSowingMonitoring = youcaiSowingMonitoringService.getById(id);
		if(youcaiSowingMonitoring==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(youcaiSowingMonitoring);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param youcaiSowingMonitoring
    */
    @RequiresPermissions("youcai:youcai_sowing_monitoring:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, YoucaiSowingMonitoring youcaiSowingMonitoring) {
        return super.exportXls(request, youcaiSowingMonitoring, YoucaiSowingMonitoring.class, "播种监控表");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("youcai:youcai_sowing_monitoring:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, YoucaiSowingMonitoring.class);
    }

}
