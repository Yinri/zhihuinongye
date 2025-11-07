package org.jeecg.modules.youcai.controller;

import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.youcai.entity.YoucaiGrowthMonitoring;
import org.jeecg.modules.youcai.service.IYoucaiGrowthMonitoringService;

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
 * @Description: 生长监控表
 * @Author: jeecg-boot
 * @Date:   2025-11-06
 * @Version: V1.0
 */
@Tag(name="生长监控表")
@RestController
@RequestMapping("/youcai/youcaiGrowthMonitoring")
@Slf4j
public class YoucaiGrowthMonitoringController extends JeecgController<YoucaiGrowthMonitoring, IYoucaiGrowthMonitoringService> {
	@Autowired
	private IYoucaiGrowthMonitoringService youcaiGrowthMonitoringService;
	
	/**
	 * 分页列表查询
	 *
	 * @param youcaiGrowthMonitoring
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "生长监控表-分页列表查询")
	@Operation(summary="生长监控表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<YoucaiGrowthMonitoring>> queryPageList(YoucaiGrowthMonitoring youcaiGrowthMonitoring,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {


        QueryWrapper<YoucaiGrowthMonitoring> queryWrapper = QueryGenerator.initQueryWrapper(youcaiGrowthMonitoring, req.getParameterMap());
		Page<YoucaiGrowthMonitoring> page = new Page<YoucaiGrowthMonitoring>(pageNo, pageSize);
		IPage<YoucaiGrowthMonitoring> pageList = youcaiGrowthMonitoringService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param youcaiGrowthMonitoring
	 * @return
	 */
	@AutoLog(value = "生长监控表-添加")
	@Operation(summary="生长监控表-添加")
	@RequiresPermissions("youcai:youcai_growth_monitoring:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody YoucaiGrowthMonitoring youcaiGrowthMonitoring) {
		youcaiGrowthMonitoringService.save(youcaiGrowthMonitoring);

		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param youcaiGrowthMonitoring
	 * @return
	 */
	@AutoLog(value = "生长监控表-编辑")
	@Operation(summary="生长监控表-编辑")
	@RequiresPermissions("youcai:youcai_growth_monitoring:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody YoucaiGrowthMonitoring youcaiGrowthMonitoring) {
		youcaiGrowthMonitoringService.updateById(youcaiGrowthMonitoring);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "生长监控表-通过id删除")
	@Operation(summary="生长监控表-通过id删除")
	@RequiresPermissions("youcai:youcai_growth_monitoring:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		youcaiGrowthMonitoringService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "生长监控表-批量删除")
	@Operation(summary="生长监控表-批量删除")
	@RequiresPermissions("youcai:youcai_growth_monitoring:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.youcaiGrowthMonitoringService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "生长监控表-通过id查询")
	@Operation(summary="生长监控表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<YoucaiGrowthMonitoring> queryById(@RequestParam(name="id",required=true) String id) {
		YoucaiGrowthMonitoring youcaiGrowthMonitoring = youcaiGrowthMonitoringService.getById(id);
		if(youcaiGrowthMonitoring==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(youcaiGrowthMonitoring);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param youcaiGrowthMonitoring
    */
    @RequiresPermissions("youcai:youcai_growth_monitoring:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, YoucaiGrowthMonitoring youcaiGrowthMonitoring) {
        return super.exportXls(request, youcaiGrowthMonitoring, YoucaiGrowthMonitoring.class, "生长监控表");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("youcai:youcai_growth_monitoring:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, YoucaiGrowthMonitoring.class);
    }

}
