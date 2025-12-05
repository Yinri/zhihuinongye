package org.jeecg.modules.youcai.controller;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import io.swagger.v3.oas.annotations.Parameter;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.youcai.entity.CropNutrientDemand;
import org.jeecg.modules.youcai.service.ICropNutrientDemandService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.common.system.base.controller.JeecgController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.apache.shiro.authz.annotation.RequiresPermissions;
 /**
 * @Description: crop_nutrient_demand
 * @Author: jeecg-boot
 * @Date:   2025-12-02
 * @Version: V1.0
 */
@Tag(name="crop_nutrient_demand")
@RestController
@RequestMapping("/youcai/cropNutrientDemand")
@Slf4j
public class CropNutrientDemandController extends JeecgController<CropNutrientDemand, ICropNutrientDemandService> {
	@Autowired
	private ICropNutrientDemandService cropNutrientDemandService;
	
	/**
	 * 分页列表查询
	 *
	 * @param cropNutrientDemand
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "crop_nutrient_demand-分页列表查询")
	@Operation(summary="crop_nutrient_demand-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<CropNutrientDemand>> queryPageList(CropNutrientDemand cropNutrientDemand,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {


        QueryWrapper<CropNutrientDemand> queryWrapper = QueryGenerator.initQueryWrapper(cropNutrientDemand, req.getParameterMap());
		Page<CropNutrientDemand> page = new Page<CropNutrientDemand>(pageNo, pageSize);
		IPage<CropNutrientDemand> pageList = cropNutrientDemandService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param cropNutrientDemand
	 * @return
	 */
	@AutoLog(value = "crop_nutrient_demand-添加")
	@Operation(summary="crop_nutrient_demand-添加")
//	@RequiresPermissions("youcai:crop_nutrient_demand:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody CropNutrientDemand cropNutrientDemand) {
		cropNutrientDemandService.save(cropNutrientDemand);

		return Result.OK("添加成功！");
	}

	 /**
	  * 编辑/新增作物需肥量（无则增、有则改）
	  */
	 @AutoLog(value = "crop_nutrient_demand-编辑/新增")
	 @Operation(summary="crop_nutrient_demand-编辑/新增（无则增、有则改）")
	 @RequestMapping(
			 value = "/edit",
			 method = {RequestMethod.POST}, // 只保留POST（和Postman一致）
			 consumes = {MediaType.APPLICATION_JSON_VALUE, "application/json;charset=UTF-8"} // 兼容所有JSON请求头
	 )
	 @Transactional
	 public Result<String> edit(@RequestBody CropNutrientDemand cropNutrientDemand) {
		 // 1. 获取品种ID
		 String varietyId = cropNutrientDemand.getVarietyId();

		 // 打印参数（验证接收成功）
		 System.out.println("=== 接收前端参数 ===");
		 System.out.println("varietyId：" + varietyId);
		 System.out.println("nDemand：" + cropNutrientDemand.getNDemand());
		 System.out.println("pDemand：" + cropNutrientDemand.getPDemand());
		 System.out.println("kDemand：" + cropNutrientDemand.getKDemand());

		 // 2. 校验必填
		 if (StringUtils.isBlank(varietyId)) {
			 return Result.error("品种ID不能为空！");
		 }
		 Long varietyIdLong;
		 try {
			 varietyIdLong = Long.valueOf(varietyId);
		 } catch (NumberFormatException e) {
			 return Result.error("品种ID必须为数字格式！");
		 }

		 // 3. 查询已有记录
		 LambdaQueryWrapper<CropNutrientDemand> wrapper = new LambdaQueryWrapper<>();
		 wrapper.eq(CropNutrientDemand::getVarietyId, varietyId);
		 CropNutrientDemand exist = cropNutrientDemandService.getOne(wrapper);

		 // 4. 新增/更新
		 if (exist != null) {
			 cropNutrientDemand.setId(exist.getId());
			 boolean updateSuccess = cropNutrientDemandService.updateById(cropNutrientDemand);
			 return updateSuccess ? Result.OK("更新成功") : Result.error("更新失败，无数据变化");
		 } else {
			 boolean saveSuccess = cropNutrientDemandService.save(cropNutrientDemand);
			 return saveSuccess ? Result.OK("新增成功") : Result.error("新增失败");
		 }
	 }
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "crop_nutrient_demand-通过id删除")
	@Operation(summary="crop_nutrient_demand-通过id删除")
//	@RequiresPermissions("youcai:crop_nutrient_demand:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		cropNutrientDemandService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "crop_nutrient_demand-批量删除")
	@Operation(summary="crop_nutrient_demand-批量删除")
	@RequiresPermissions("youcai:crop_nutrient_demand:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.cropNutrientDemandService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "crop_nutrient_demand-通过id查询")
	@Operation(summary="crop_nutrient_demand-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<CropNutrientDemand> queryById(@RequestParam(name="id",required=true) String id) {
		CropNutrientDemand cropNutrientDemand = cropNutrientDemandService.getById(id);
		if(cropNutrientDemand==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(cropNutrientDemand);
	}
	 /**
	  * 核心修复：将参数类型改为 String，匹配数据库 VARCHAR 类型
	  */
	 @AutoLog(value = "crop_nutrient_demand-根据品种ID查询")
	 @Operation(summary="crop_nutrient_demand-根据品种ID查询")
	 @GetMapping(value = "/getByVarietyId")
	 public Result<CropNutrientDemand> getByVarietyId(
			 @Parameter(description = "作物品种ID（字符串类型）")
			 @RequestParam(name = "varietyId", required = true) String varietyId) {

		 Result<CropNutrientDemand> result = new Result<>();
		 try {
			 // 1. 打印关键信息（确认参数接收正确）
			 log.info("【需肥量查询】接收参数：varietyId={}（长度：{}）", varietyId, varietyId.length());

			 // 2. 严格匹配字符串类型的 variety_id（去除空格）
			 QueryWrapper<CropNutrientDemand> queryWrapper = new QueryWrapper<>();
			 queryWrapper.eq("variety_id", varietyId.trim()) // 字符串精准匹配
					 .orderByDesc("update_time")
					 .last("LIMIT 1");

			 // 3. 执行查询
			 List<CropNutrientDemand> demandList = cropNutrientDemandService.list(queryWrapper);
			 log.info("【需肥量查询】品种ID={} 匹配记录数：{}", varietyId, demandList.size());

			 // 4. 处理结果
			 if (demandList.isEmpty()) {
				 log.warn("【需肥量查询】品种ID={} 无匹配数据", varietyId);
				 result.setSuccess(true); // 改为成功状态
				 result.setResult(null);  // 返回null
			 } else {
				 CropNutrientDemand demand = demandList.get(0);
				 log.info("【需肥量查询】匹配数据：{}", demand);
				 result.setSuccess(true);
				 result.setResult(demand);
			 }
		 } catch (Exception e) {
			 log.error("【需肥量查询】品种ID={} 异常", varietyId, e);
			 result.error500("查询失败：" + e.getMessage());
		 }
		 return result;
	 }
    /**
    * 导出excel
    *
    * @param request
    * @param cropNutrientDemand
    */
    @RequiresPermissions("youcai:crop_nutrient_demand:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, CropNutrientDemand cropNutrientDemand) {
        return super.exportXls(request, cropNutrientDemand, CropNutrientDemand.class, "crop_nutrient_demand");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("youcai:crop_nutrient_demand:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, CropNutrientDemand.class);
    }

}
