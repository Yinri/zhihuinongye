package org.jeecg.modules.youcai.controller;

import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.youcai.entity.YoucaiSeedInputs;
import org.jeecg.modules.youcai.service.IYoucaiSeedInputsService;

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
 * @Description: 种子投入表
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V1.0
 */
@Tag(name="种子投入表")
@RestController
@RequestMapping("/youcai/youcaiSeedInputs")
@Slf4j
public class YoucaiSeedInputsController extends JeecgController<YoucaiSeedInputs, IYoucaiSeedInputsService> {
	@Autowired
	private IYoucaiSeedInputsService youcaiSeedInputsService;
	
	/**
	 * 分页列表查询
	 *
	 * @param youcaiSeedInputs
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "种子投入表-分页列表查询")
	@Operation(summary="种子投入表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<YoucaiSeedInputs>> queryPageList(YoucaiSeedInputs youcaiSeedInputs,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {


        QueryWrapper<YoucaiSeedInputs> queryWrapper = QueryGenerator.initQueryWrapper(youcaiSeedInputs, req.getParameterMap());
		Page<YoucaiSeedInputs> page = new Page<YoucaiSeedInputs>(pageNo, pageSize);
		IPage<YoucaiSeedInputs> pageList = youcaiSeedInputsService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param youcaiSeedInputs
	 * @return
	 */
	@AutoLog(value = "种子投入表-添加")
	@Operation(summary="种子投入表-添加")
	@RequiresPermissions("youcai:youcai_seed_inputs:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody YoucaiSeedInputs youcaiSeedInputs) {
		youcaiSeedInputsService.save(youcaiSeedInputs);

		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param youcaiSeedInputs
	 * @return
	 */
	@AutoLog(value = "种子投入表-编辑")
	@Operation(summary="种子投入表-编辑")
	@RequiresPermissions("youcai:youcai_seed_inputs:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody YoucaiSeedInputs youcaiSeedInputs) {
		youcaiSeedInputsService.updateById(youcaiSeedInputs);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "种子投入表-通过id删除")
	@Operation(summary="种子投入表-通过id删除")
	@RequiresPermissions("youcai:youcai_seed_inputs:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		youcaiSeedInputsService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "种子投入表-批量删除")
	@Operation(summary="种子投入表-批量删除")
	@RequiresPermissions("youcai:youcai_seed_inputs:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.youcaiSeedInputsService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "种子投入表-通过id查询")
	@Operation(summary="种子投入表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<YoucaiSeedInputs> queryById(@RequestParam(name="id",required=true) String id) {
		YoucaiSeedInputs youcaiSeedInputs = youcaiSeedInputsService.getById(id);
		if(youcaiSeedInputs==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(youcaiSeedInputs);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param youcaiSeedInputs
    */
    @RequiresPermissions("youcai:youcai_seed_inputs:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, YoucaiSeedInputs youcaiSeedInputs) {
        return super.exportXls(request, youcaiSeedInputs, YoucaiSeedInputs.class, "种子投入表");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("youcai:youcai_seed_inputs:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, YoucaiSeedInputs.class);
    }

}
