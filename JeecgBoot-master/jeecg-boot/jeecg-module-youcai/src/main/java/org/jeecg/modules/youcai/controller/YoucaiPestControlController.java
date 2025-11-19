package org.jeecg.modules.youcai.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.youcai.dto.AnalysisRequestDTO;
import org.jeecg.modules.youcai.entity.YoucaiPestControl;
import org.jeecg.modules.youcai.service.IYoucaiPestControlService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.common.system.base.controller.JeecgController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.modules.youcai.entity.iotEntity.ApiResponse;
import reactor.core.publisher.Mono;

/**
 * @Description: 虫害防控表
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V1.0
 */
@Tag(name="虫害防控表")
@RestController
@RequestMapping("/youcai/youcaiPestControl")
@Slf4j
public class YoucaiPestControlController extends JeecgController<YoucaiPestControl, IYoucaiPestControlService> {
	@Autowired
	private IYoucaiPestControlService youcaiPestControlService;
	
	/**
	 * 分页列表查询
	 *
	 * @param youcaiPestControl
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "虫害防控表-分页列表查询")
	@Operation(summary="虫害防控表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<YoucaiPestControl>> queryPageList(YoucaiPestControl youcaiPestControl,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {


        QueryWrapper<YoucaiPestControl> queryWrapper = QueryGenerator.initQueryWrapper(youcaiPestControl, req.getParameterMap());
		Page<YoucaiPestControl> page = new Page<YoucaiPestControl>(pageNo, pageSize);
		IPage<YoucaiPestControl> pageList = youcaiPestControlService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加

	 * @param youcaiPestControl
	 * @return
	 */
	@AutoLog(value = "虫害防控表-添加")
	@Operation(summary="虫害防控表-添加")
	@RequiresPermissions("youcai:youcai_pest_control:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody YoucaiPestControl youcaiPestControl) {
		youcaiPestControlService.save(youcaiPestControl);

		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param youcaiPestControl
	 * @return
	 */
	@AutoLog(value = "虫害防控表-编辑")
	@Operation(summary="虫害防控表-编辑")
	@RequiresPermissions("youcai:youcai_pest_control:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody YoucaiPestControl youcaiPestControl) {
		youcaiPestControlService.updateById(youcaiPestControl);
		return Result.OK("编辑成功!");
	}
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "虫害防控表-通过id删除")
	@Operation(summary="虫害防控表-通过id删除")
	@RequiresPermissions("youcai:youcai_pest_control:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		youcaiPestControlService.removeById(id);
		return Result.OK("删除成功!");
	}
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "虫害防控表-批量删除")
	@Operation(summary="虫害防控表-批量删除")
	@RequiresPermissions("youcai:youcai_pest_control:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.youcaiPestControlService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "虫害防控表-通过id查询")
	@Operation(summary="虫害防控表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<YoucaiPestControl> queryById(@RequestParam(name="id",required=true) String id) {
		YoucaiPestControl youcaiPestControl = youcaiPestControlService.getById(id);
		if(youcaiPestControl==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(youcaiPestControl);
	}
    /**
    * 导出excel
    *
    * @param request
    * @param youcaiPestControl
    */
    @RequiresPermissions("youcai:youcai_pest_control:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, YoucaiPestControl youcaiPestControl) {
        return super.exportXls(request, youcaiPestControl, YoucaiPestControl.class, "虫害防控表");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("youcai:youcai_pest_control:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, YoucaiPestControl.class);
    }
	@GetMapping("/images")
	public Mono<Result<List<Map<String, Object>>>> getPestImages() {
		return youcaiPestControlService.getPestImages()
				.map(parsedData -> Result.OK(parsedData))
				.onErrorReturn(Result.error("Failed to retrieve pest images"));
	}
	@PostMapping("/aiAnalysis")
	public Result<?> aiAnalysis(@RequestBody AnalysisRequestDTO req) {
		try {
			String aiResult = youcaiPestControlService.aiAnalysis(req);
			Result r = new Result<>();
			r.setCode(CommonConstant.SC_OK_200);
			r.setSuccess(true);
			r.setMessage("");
			r.setResult(aiResult);
			r.setTimestamp(System.currentTimeMillis());
			return r;
		} catch (Exception e) {
			return Result.error("AI 分析失败: " + e.getMessage());
		}
	}
	@GetMapping("/findImages")
	public Mono<Result<List<Map<String, Object>>>> getPestImages(
			@RequestParam("start_date") @DateTimeFormat(pattern = "yyyy-MM-dd") String startDate,
			@RequestParam("end_date") @DateTimeFormat(pattern = "yyyy-MM-dd") String endDate
	) {
		return youcaiPestControlService.getAllPestImages(startDate, endDate)
				.map(Result::OK)
				.onErrorReturn(Result.error("Failed to retrieve pest images"));
	}
	@PostMapping("findControl")
	public Result<?> findControl(@RequestBody Map<String, Object> params) {
		Integer plotId = (Integer) params.get("plotId");
		String start = (String) params.get("start_date");
		String end = (String) params.get("end_date");
		List<YoucaiPestControl> list = youcaiPestControlService.findControl(plotId, start, end);
		return Result.OK(list);
	}
}
