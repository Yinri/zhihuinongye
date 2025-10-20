package org.jeecg.modules.youcai.controller;

import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.youcai.entity.YoucaiDiseaseWarnings;
import org.jeecg.modules.youcai.service.IYoucaiDiseaseWarningsService;

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
 * @Description: 病害预警表
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V1.0
 */
@Tag(name="病害预警表")
@RestController
@RequestMapping("/youcai/youcaiDiseaseWarnings")
@Slf4j
public class YoucaiDiseaseWarningsController extends JeecgController<YoucaiDiseaseWarnings, IYoucaiDiseaseWarningsService> {
	@Autowired
	private IYoucaiDiseaseWarningsService youcaiDiseaseWarningsService;
	
	/**
	 * 分页列表查询
	 *
	 * @param youcaiDiseaseWarnings
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "病害预警表-分页列表查询")
	@Operation(summary="病害预警表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<YoucaiDiseaseWarnings>> queryPageList(YoucaiDiseaseWarnings youcaiDiseaseWarnings,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {


        QueryWrapper<YoucaiDiseaseWarnings> queryWrapper = QueryGenerator.initQueryWrapper(youcaiDiseaseWarnings, req.getParameterMap());
		Page<YoucaiDiseaseWarnings> page = new Page<YoucaiDiseaseWarnings>(pageNo, pageSize);
		IPage<YoucaiDiseaseWarnings> pageList = youcaiDiseaseWarningsService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param youcaiDiseaseWarnings
	 * @return
	 */
	@AutoLog(value = "病害预警表-添加")
	@Operation(summary="病害预警表-添加")
	@RequiresPermissions("youcai:youcai_disease_warnings:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody YoucaiDiseaseWarnings youcaiDiseaseWarnings) {
		youcaiDiseaseWarningsService.save(youcaiDiseaseWarnings);

		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param youcaiDiseaseWarnings
	 * @return
	 */
	@AutoLog(value = "病害预警表-编辑")
	@Operation(summary="病害预警表-编辑")
	@RequiresPermissions("youcai:youcai_disease_warnings:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody YoucaiDiseaseWarnings youcaiDiseaseWarnings) {
		youcaiDiseaseWarningsService.updateById(youcaiDiseaseWarnings);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "病害预警表-通过id删除")
	@Operation(summary="病害预警表-通过id删除")
	@RequiresPermissions("youcai:youcai_disease_warnings:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		youcaiDiseaseWarningsService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "病害预警表-批量删除")
	@Operation(summary="病害预警表-批量删除")
	@RequiresPermissions("youcai:youcai_disease_warnings:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.youcaiDiseaseWarningsService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "病害预警表-通过id查询")
	@Operation(summary="病害预警表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<YoucaiDiseaseWarnings> queryById(@RequestParam(name="id",required=true) String id) {
		YoucaiDiseaseWarnings youcaiDiseaseWarnings = youcaiDiseaseWarningsService.getById(id);
		if(youcaiDiseaseWarnings==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(youcaiDiseaseWarnings);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param youcaiDiseaseWarnings
    */
    @RequiresPermissions("youcai:youcai_disease_warnings:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, YoucaiDiseaseWarnings youcaiDiseaseWarnings) {
        return super.exportXls(request, youcaiDiseaseWarnings, YoucaiDiseaseWarnings.class, "病害预警表");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("youcai:youcai_disease_warnings:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, YoucaiDiseaseWarnings.class);
    }

}
