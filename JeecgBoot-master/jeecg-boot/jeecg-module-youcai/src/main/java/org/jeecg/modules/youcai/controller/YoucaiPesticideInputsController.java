package org.jeecg.modules.youcai.controller;

import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.config.shiro.IgnoreAuth;
import org.jeecg.modules.youcai.entity.YoucaiPesticideInputs;
import org.jeecg.modules.youcai.service.IYoucaiPesticideInputsService;

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
 * @Description: 农药投入表
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V1.0
 */
@Tag(name="农药投入表")
@RestController
@RequestMapping("/youcai/youcaiPesticideInputs")
@Slf4j
public class YoucaiPesticideInputsController extends JeecgController<YoucaiPesticideInputs, IYoucaiPesticideInputsService> {
	@Autowired
	private IYoucaiPesticideInputsService youcaiPesticideInputsService;
	
	/**
	 * 分页列表查询
	 *
	 * @param youcaiPesticideInputs
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "农药投入表-分页列表查询")
	@Operation(summary="农药投入表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<YoucaiPesticideInputs>> queryPageList(YoucaiPesticideInputs youcaiPesticideInputs,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {


        QueryWrapper<YoucaiPesticideInputs> queryWrapper = QueryGenerator.initQueryWrapper(youcaiPesticideInputs, req.getParameterMap());
		Page<YoucaiPesticideInputs> page = new Page<YoucaiPesticideInputs>(pageNo, pageSize);
		IPage<YoucaiPesticideInputs> pageList = youcaiPesticideInputsService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param youcaiPesticideInputs
	 * @return
	 */
	@AutoLog(value = "农药投入表-添加")
	@Operation(summary="农药投入表-添加")
	@RequiresPermissions("youcai:youcai_pesticide_inputs:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody YoucaiPesticideInputs youcaiPesticideInputs) {
		youcaiPesticideInputsService.save(youcaiPesticideInputs);

		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param youcaiPesticideInputs
	 * @return
	 */
	@AutoLog(value = "农药投入表-编辑")
	@Operation(summary="农药投入表-编辑")
	@RequiresPermissions("youcai:youcai_pesticide_inputs:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody YoucaiPesticideInputs youcaiPesticideInputs) {
		youcaiPesticideInputsService.updateById(youcaiPesticideInputs);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "农药投入表-通过id删除")
	@Operation(summary="农药投入表-通过id删除")
	@RequiresPermissions("youcai:youcai_pesticide_inputs:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		youcaiPesticideInputsService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "农药投入表-批量删除")
	@Operation(summary="农药投入表-批量删除")
	@RequiresPermissions("youcai:youcai_pesticide_inputs:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.youcaiPesticideInputsService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "农药投入表-通过id查询")
	@Operation(summary="农药投入表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<YoucaiPesticideInputs> queryById(@RequestParam(name="id",required=true) String id) {
		YoucaiPesticideInputs youcaiPesticideInputs = youcaiPesticideInputsService.getById(id);
		if(youcaiPesticideInputs==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(youcaiPesticideInputs);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param youcaiPesticideInputs
    */
    @RequiresPermissions("youcai:youcai_pesticide_inputs:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, YoucaiPesticideInputs youcaiPesticideInputs) {
        return super.exportXls(request, youcaiPesticideInputs, YoucaiPesticideInputs.class, "农药投入表");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("youcai:youcai_pesticide_inputs:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, YoucaiPesticideInputs.class);
    }

	@Operation(summary="农药投入表-查询农药")
	@GetMapping("/name/list")
	 public Result<List<String>> getPesticideNameList() {
		 try {
			 List<String> pesticideNames = youcaiPesticideInputsService.listDistinctPesticideNames();
			 return Result.OK(pesticideNames);
		 } catch (Exception e) {
			 log.error("获取农药列表失败", e);
			 return Result.error("获取农药列表失败");
		 }
	 }




}
