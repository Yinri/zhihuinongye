package org.jeecg.modules.youcai.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.query.QueryRuleEnum;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.youcai.entity.YoucaiBases;
import org.jeecg.modules.youcai.service.IYoucaiBasesService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.jeecg.common.system.base.controller.JeecgController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import com.alibaba.fastjson.JSON;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.apache.shiro.authz.annotation.RequiresPermissions;
 /**
 * @Description: 基地表
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V1.0
 */
@Tag(name="基地表")
@RestController
@RequestMapping("/youcai/bases")	
@Slf4j
public class YoucaiBasesController extends JeecgController<YoucaiBases, IYoucaiBasesService> {
	@Autowired
	private IYoucaiBasesService youcaiBasesService;
	
	/**
	 * 分页列表查询
	 *
	 * @param youcaiBases
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "基地表-分页列表查询")
	@Operation(summary="基地表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<YoucaiBases>> queryPageList(YoucaiBases youcaiBases,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {


        QueryWrapper<YoucaiBases> queryWrapper = QueryGenerator.initQueryWrapper(youcaiBases, req.getParameterMap());
		Page<YoucaiBases> page = new Page<YoucaiBases>(pageNo, pageSize);
		IPage<YoucaiBases> pageList = youcaiBasesService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param youcaiBases
	 * @return
	 */
	@AutoLog(value = "基地表-添加")
	@Operation(summary="基地表-添加")
//	@RequiresPermissions("youcai:youcai_bases:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody YoucaiBases youcaiBases) {
		youcaiBasesService.save(youcaiBases);

		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param youcaiBases
	 * @return
	 */
	@AutoLog(value = "基地表-编辑")
	@Operation(summary="基地表-编辑")
//	@RequiresPermissions("youcai:youcai_bases:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody YoucaiBases youcaiBases) {
		youcaiBasesService.updateById(youcaiBases);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
    @AutoLog(value = "基地表-通过id删除")
    @Operation(summary="基地表-通过id删除")
    @RequiresPermissions("youcai:youcai_bases:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name="id",required=true) String id) {
        youcaiBasesService.removeById(id);
        return Result.OK("删除成功!");
    }

    @AutoLog(value = "基地表-通过id删除(REST风格)")
    @Operation(summary="基地表-通过id删除(REST风格)")
    @RequiresPermissions("youcai:youcai_bases:delete")
    @DeleteMapping(value = "/delete/{id}")
    public Result<String> deleteByPath(@PathVariable("id") String id) {
        youcaiBasesService.removeById(id);
        return Result.OK("删除成功!");
    }
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "基地表-批量删除")
	@Operation(summary="基地表-批量删除")
	@RequiresPermissions("youcai:youcai_bases:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.youcaiBasesService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "基地表-通过id查询")
	@Operation(summary="基地表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<YoucaiBases> queryById(@RequestParam(name="id",required=true) String id) {
		YoucaiBases youcaiBases = youcaiBasesService.getById(id);
		if(youcaiBases==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(youcaiBases);
	}

	 @Operation(summary="基地表-查询所有基地") // 接口描述（可选，用于Swagger文档）
	 @GetMapping(value = "/getAllBases") // 接口路径，前端需请求这个路径
	 public Result<List<YoucaiBases>> getAllBases() {
		 // 1. 调用Service层方法，查询所有基地数据
		 List<YoucaiBases> baseList = youcaiBasesService.list(); // MyBatis-Plus自带的“查询所有”方法

		 // 2. 判断是否有数据，返回对应结果
		 if (baseList == null || baseList.isEmpty()) {
			 return Result.error("暂无基地数据"); // 无数据时返回错误提示
		 }
		 return Result.OK(baseList); // 有数据时返回成功，携带基地列表
	 }
    /**
    * 导出excel
    *
    * @param request
    * @param youcaiBases
    */
    @RequiresPermissions("youcai:youcai_bases:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, YoucaiBases youcaiBases) {
        return super.exportXls(request, youcaiBases, YoucaiBases.class, "基地表");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("youcai:youcai_bases:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, YoucaiBases.class);
    }

}
