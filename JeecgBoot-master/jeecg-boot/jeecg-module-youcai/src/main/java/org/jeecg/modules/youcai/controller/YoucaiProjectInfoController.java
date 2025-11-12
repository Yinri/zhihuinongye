package org.jeecg.modules.youcai.controller;

import java.util.Arrays;
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
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.youcai.entity.YoucaiProjectInfo;
import org.jeecg.modules.youcai.service.IYoucaiProjectInfoService;

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
 * @Description: 项目信息表
 * @Author: jeecg-boot
 * @Date:   2025-11-10
 * @Version: V1.0
 */
@Tag(name="项目信息表")
@RestController
@RequestMapping("/youcai/projectInfo")
@Slf4j
public class YoucaiProjectInfoController extends JeecgController<YoucaiProjectInfo, IYoucaiProjectInfoService> {
	@Autowired
	private IYoucaiProjectInfoService youcaiProjectInfoService;
	
	/**
	 * 分页列表查询
	 *
	 * @param youcaiProjectInfo
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "项目信息表-分页列表查询")
	@Operation(summary="项目信息表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<YoucaiProjectInfo>> queryPageList(YoucaiProjectInfo youcaiProjectInfo,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<YoucaiProjectInfo> queryWrapper = QueryGenerator.initQueryWrapper(youcaiProjectInfo, req.getParameterMap());
		Page<YoucaiProjectInfo> page = new Page<YoucaiProjectInfo>(pageNo, pageSize);
		IPage<YoucaiProjectInfo> pageList = youcaiProjectInfoService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param youcaiProjectInfo
	 * @return
	 */
	@AutoLog(value = "项目信息表-添加")
	@Operation(summary="项目信息表-添加")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody YoucaiProjectInfo youcaiProjectInfo) {
		youcaiProjectInfoService.save(youcaiProjectInfo);
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param youcaiProjectInfo
	 * @return
	 */
	@AutoLog(value = "项目信息表-编辑")
	@Operation(summary="项目信息表-编辑")
	@RequiresPermissions("youcai:youcai_project_info:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody YoucaiProjectInfo youcaiProjectInfo) {
		youcaiProjectInfoService.updateById(youcaiProjectInfo);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "项目信息表-通过id删除")
	@Operation(summary="项目信息表-通过id删除")
	@RequiresPermissions("youcai:youcai_project_info:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		youcaiProjectInfoService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "项目信息表-批量删除")
	@Operation(summary="项目信息表-批量删除")
	@RequiresPermissions("youcai:youcai_project_info:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.youcaiProjectInfoService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "项目信息表-通过id查询")
	@Operation(summary="项目信息表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<YoucaiProjectInfo> queryById(@RequestParam(name="id",required=true) String id) {
		YoucaiProjectInfo youcaiProjectInfo = youcaiProjectInfoService.getById(id);
		if(youcaiProjectInfo==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(youcaiProjectInfo);
	}

	/**
	 * 导出excel
	 *
	 * @param request
	 * @param youcaiProjectInfo
	 */
	@RequiresPermissions("youcai:youcai_project_info:exportXls")
	@RequestMapping(value = "/exportXls")
	public ModelAndView exportXls(HttpServletRequest request, YoucaiProjectInfo youcaiProjectInfo) {
		return super.exportXls(request, youcaiProjectInfo, YoucaiProjectInfo.class, "项目信息表");
	}

	/**
	  * 通过excel导入数据
	*
	* @param request
	* @param response
	* @return
	*/
	@RequiresPermissions("youcai:youcai_project_info:importExcel")
	@RequestMapping(value = "/importExcel", method = RequestMethod.POST)
	public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
		return super.importExcel(request, response, YoucaiProjectInfo.class);
	}

}