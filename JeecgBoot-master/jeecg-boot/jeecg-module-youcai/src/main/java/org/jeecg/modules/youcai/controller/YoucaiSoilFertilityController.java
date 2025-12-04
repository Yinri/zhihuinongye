package org.jeecg.modules.youcai.controller;

import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.youcai.entity.YoucaiSoilFertility;
import org.jeecg.modules.youcai.service.IYoucaiSoilFertilityService;

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
 * @Description: 土壤肥力表
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V1.0
 */
@Tag(name="土壤肥力表")
@RestController
@RequestMapping("/youcai/youcaiSoilFertility")
@Slf4j
public class YoucaiSoilFertilityController extends JeecgController<YoucaiSoilFertility, IYoucaiSoilFertilityService> {
	@Autowired
	private IYoucaiSoilFertilityService youcaiSoilFertilityService;
	
	/**
	 * 分页列表查询
	 *
	 * @param youcaiSoilFertility
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "土壤肥力表-分页列表查询")
	@Operation(summary="土壤肥力表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<YoucaiSoilFertility>> queryPageList(YoucaiSoilFertility youcaiSoilFertility,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {


        QueryWrapper<YoucaiSoilFertility> queryWrapper = QueryGenerator.initQueryWrapper(youcaiSoilFertility, req.getParameterMap());
		Page<YoucaiSoilFertility> page = new Page<YoucaiSoilFertility>(pageNo, pageSize);
		IPage<YoucaiSoilFertility> pageList = youcaiSoilFertilityService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param youcaiSoilFertility
	 * @return
	 */
	@AutoLog(value = "土壤肥力表-添加")
	@Operation(summary="土壤肥力表-添加")
	@RequiresPermissions("youcai:youcai_soil_fertility:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody YoucaiSoilFertility youcaiSoilFertility) {
		youcaiSoilFertilityService.save(youcaiSoilFertility);

		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param youcaiSoilFertility
	 * @return
	 */
	@AutoLog(value = "土壤肥力表-编辑")
	@Operation(summary="土壤肥力表-编辑")
	@RequiresPermissions("youcai:youcai_soil_fertility:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody YoucaiSoilFertility youcaiSoilFertility) {
		youcaiSoilFertilityService.updateById(youcaiSoilFertility);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "土壤肥力表-通过id删除")
	@Operation(summary="土壤肥力表-通过id删除")
	@RequiresPermissions("youcai:youcai_soil_fertility:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		youcaiSoilFertilityService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "土壤肥力表-批量删除")
	@Operation(summary="土壤肥力表-批量删除")
	@RequiresPermissions("youcai:youcai_soil_fertility:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.youcaiSoilFertilityService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "土壤肥力表-通过id查询")
	@Operation(summary="土壤肥力表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<YoucaiSoilFertility> queryById(@RequestParam(name="id",required=true) String id) {
		YoucaiSoilFertility youcaiSoilFertility = youcaiSoilFertilityService.getById(id);
		if(youcaiSoilFertility==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(youcaiSoilFertility);
	}
	 /**
	  * 通过地块id查询土壤肥力数据
	  *
	  * @param plotId 地块id（根据你的实体类字段名调整，比如你的字段是plotId/landId等）
	  * @return 该地块下的土壤肥力数据列表
	  */
	 @Operation(summary="土壤肥力表-通过地块id查询最新一条")
	 @GetMapping(value = "/queryByPlotId")
	 public Result<YoucaiSoilFertility> queryByPlotId(@RequestParam(name="plotId",required=true) String plotId) {
		 // 1. 构建查询条件：按地块id筛选 + 按更新时间降序（或createTime） + 只取第一条
		 QueryWrapper<YoucaiSoilFertility> queryWrapper = new QueryWrapper<>();
		 queryWrapper.eq("plot_id", plotId)          // 匹配地块id
				 .orderByDesc("update_time")     // 按更新时间降序（最新的排在前面）
				 .last("LIMIT 1");               // 限制只查第一条（MySQL语法）
		 // 若用Oracle/SQLServer，替换为：.last("FETCH FIRST 1 ROWS ONLY")

		 // 2. 查询最新一条数据（注意：这里用getOne而非list）
		 YoucaiSoilFertility latestData = youcaiSoilFertilityService.getOne(queryWrapper);

		 // 3. 结果处理
		 if(latestData == null) {
			 return Result.error("该地块未查询到土壤肥力数据");
		 }
		 return Result.OK(latestData);
	 }
	 /**
	  * 查询所有土壤肥力数据
	  * @return 全部土壤肥力数据列表
	  */
	 @Operation(summary="土壤肥力表-查询所有数据")
	 @GetMapping(value = "/queryAll")
	 public Result<List<YoucaiSoilFertility>> queryAll() {
		 // 1. 构建查询条件（无筛选，仅按更新时间降序）
		 QueryWrapper<YoucaiSoilFertility> queryWrapper = new QueryWrapper<>();
		 queryWrapper.orderByDesc("update_time"); // 可选：按更新时间降序排列

		 // 2. 查询所有数据
		 List<YoucaiSoilFertility> list = youcaiSoilFertilityService.list(queryWrapper);

		 // 3. 结果处理
		 if(list.isEmpty()) {
			 return Result.error("暂无土壤肥力数据");
		 }
		 return Result.OK(list);
	 }
    /**
    * 导出excel
    *
    * @param request
    * @param youcaiSoilFertility
    */
    @RequiresPermissions("youcai:youcai_soil_fertility:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, YoucaiSoilFertility youcaiSoilFertility) {
        return super.exportXls(request, youcaiSoilFertility, YoucaiSoilFertility.class, "土壤肥力表");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("youcai:youcai_soil_fertility:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, YoucaiSoilFertility.class);
    }

}
