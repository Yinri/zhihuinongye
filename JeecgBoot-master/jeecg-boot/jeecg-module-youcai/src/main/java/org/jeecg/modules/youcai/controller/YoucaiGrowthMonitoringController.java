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
	  * 通过baseId查询最新生长监控数据
	  *
	  * @param baseId 基地ID（必填）
	  * @return
	  */
	 @AutoLog(value = "生长监控表-通过baseId查询")
	 @Operation(summary = "生长监控表-通过baseId查询（返回最新数据，超30天按日期推算）")
	 @GetMapping(value = "/queryByBaseId")
	 public Result<YoucaiGrowthMonitoring> queryByBaseId(@RequestParam(name = "baseId", required = true) String baseId) {
		 log.info("查询生长监控数据（最新），baseId：{}", baseId);
		 QueryWrapper<YoucaiGrowthMonitoring> queryWrapper = new QueryWrapper<>();
		 queryWrapper.eq("base_id", baseId)
				 .orderByDesc("monitoring_date")
				 .last("LIMIT 1");

		 YoucaiGrowthMonitoring latestData = youcaiGrowthMonitoringService.getOne(queryWrapper);
		 if (latestData != null && latestData.getMonitoringDate() != null) {
			 long daysAgo = (System.currentTimeMillis() - latestData.getMonitoringDate().getTime())
					 / (1000 * 60 * 60 * 24);
			 if (daysAgo <= 30) {
				 log.info("baseId：{} 监测数据在有效期内（{}天前），直接返回", baseId, daysAgo);
				 return Result.OK(latestData);
			 }
			 String inferred = inferGrowthStageByDate();
			 log.info("baseId：{} 监测数据已过期（{}天前），推算为：{}", baseId, daysAgo, inferred);
			 latestData.setGrowthStage(inferred);
			 return Result.OK(latestData);
		 }

		 log.warn("baseId：{} 无监测数据，按日期推算", baseId);
		 YoucaiGrowthMonitoring fallback = new YoucaiGrowthMonitoring();
		 fallback.setBaseId(baseId);
		 fallback.setGrowthStage(inferGrowthStageByDate());
		 return Result.OK(fallback);
	 }

	 private String inferGrowthStageByDate() {
		 java.util.Calendar cal = java.util.Calendar.getInstance();
		 int month = cal.get(java.util.Calendar.MONTH) + 1;
		 int day = cal.get(java.util.Calendar.DAY_OF_MONTH);

		 if (month == 9 && day >= 15 || month == 10) {
			 return "发芽出苗期";
		 }
		 if (month == 11 || month == 12 || month == 1 || month == 2) {
			 return "苗期";
		 }
		 if (month == 3 && day <= 15) {
			 return "蕾薹期";
		 }
		 if (month == 3 && day > 15 || month == 4) {
			 return "开花期";
		 }
		 if (month == 5 && day <= 15) {
			 return "角果发育成熟期";
		 }
		 return "";
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
