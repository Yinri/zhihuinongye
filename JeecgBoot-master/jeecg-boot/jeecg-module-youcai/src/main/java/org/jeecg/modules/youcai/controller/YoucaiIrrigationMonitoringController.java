package org.jeecg.modules.youcai.controller;

import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.youcai.entity.YoucaiIrrigationMonitoring;
import org.jeecg.modules.youcai.service.IYoucaiIrrigationMonitoringService;

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
 * @Description: 灌溉监控表
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V1.0
 */
@Tag(name="灌溉监控表")
@RestController
@RequestMapping("/youcai/youcaiIrrigationMonitoring")
@Slf4j
public class YoucaiIrrigationMonitoringController extends JeecgController<YoucaiIrrigationMonitoring, IYoucaiIrrigationMonitoringService> {
	@Autowired
	private IYoucaiIrrigationMonitoringService youcaiIrrigationMonitoringService;
	
	/**
	 * 分页列表查询
	 *
	 * @param youcaiIrrigationMonitoring
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "灌溉监控表-分页列表查询")
	@Operation(summary="灌溉监控表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<YoucaiIrrigationMonitoring>> queryPageList(YoucaiIrrigationMonitoring youcaiIrrigationMonitoring,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {


        QueryWrapper<YoucaiIrrigationMonitoring> queryWrapper = QueryGenerator.initQueryWrapper(youcaiIrrigationMonitoring, req.getParameterMap());
		Page<YoucaiIrrigationMonitoring> page = new Page<YoucaiIrrigationMonitoring>(pageNo, pageSize);
		IPage<YoucaiIrrigationMonitoring> pageList = youcaiIrrigationMonitoringService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param youcaiIrrigationMonitoring
	 * @return
	 */
	@AutoLog(value = "灌溉监控表-添加")
	@Operation(summary="灌溉监控表-添加")
	@RequiresPermissions("youcai:youcai_irrigation_monitoring:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody YoucaiIrrigationMonitoring youcaiIrrigationMonitoring) {
		youcaiIrrigationMonitoringService.save(youcaiIrrigationMonitoring);

		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param youcaiIrrigationMonitoring
	 * @return
	 */
	@AutoLog(value = "灌溉监控表-编辑")
	@Operation(summary="灌溉监控表-编辑")
	@RequiresPermissions("youcai:youcai_irrigation_monitoring:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody YoucaiIrrigationMonitoring youcaiIrrigationMonitoring) {
		youcaiIrrigationMonitoringService.updateById(youcaiIrrigationMonitoring);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "灌溉监控表-通过id删除")
	@Operation(summary="灌溉监控表-通过id删除")
	@RequiresPermissions("youcai:youcai_irrigation_monitoring:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		youcaiIrrigationMonitoringService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "灌溉监控表-批量删除")
	@Operation(summary="灌溉监控表-批量删除")
	@RequiresPermissions("youcai:youcai_irrigation_monitoring:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.youcaiIrrigationMonitoringService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "灌溉监控表-通过id查询")
	@Operation(summary="灌溉监控表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<YoucaiIrrigationMonitoring> queryById(@RequestParam(name="id",required=true) String id) {
		YoucaiIrrigationMonitoring youcaiIrrigationMonitoring = youcaiIrrigationMonitoringService.getById(id);
		if(youcaiIrrigationMonitoring==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(youcaiIrrigationMonitoring);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param youcaiIrrigationMonitoring
    */
    @RequiresPermissions("youcai:youcai_irrigation_monitoring:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, YoucaiIrrigationMonitoring youcaiIrrigationMonitoring) {
        return super.exportXls(request, youcaiIrrigationMonitoring, YoucaiIrrigationMonitoring.class, "灌溉监控表");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("youcai:youcai_irrigation_monitoring:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, YoucaiIrrigationMonitoring.class);
    }

}
