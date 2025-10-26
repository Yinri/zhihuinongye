package org.jeecg.modules.youcai.controller;

import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.youcai.entity.YoucaiFertilizerInputs;
import org.jeecg.modules.youcai.service.IYoucaiFertilizerInputsService;

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
 * @Description: 肥料投入表
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V1.0
 */
@Tag(name="肥料投入表")
@RestController
@RequestMapping("/youcai/youcaiFertilizerInputs")
@Slf4j
public class YoucaiFertilizerInputsController extends JeecgController<YoucaiFertilizerInputs, IYoucaiFertilizerInputsService> {
	@Autowired
	private IYoucaiFertilizerInputsService youcaiFertilizerInputsService;
	
	/**
	 * 分页列表查询
	 *
	 * @param youcaiFertilizerInputs
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "肥料投入表-分页列表查询")
	@Operation(summary="肥料投入表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<YoucaiFertilizerInputs>> queryPageList(YoucaiFertilizerInputs youcaiFertilizerInputs,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {


        QueryWrapper<YoucaiFertilizerInputs> queryWrapper = QueryGenerator.initQueryWrapper(youcaiFertilizerInputs, req.getParameterMap());
		Page<YoucaiFertilizerInputs> page = new Page<YoucaiFertilizerInputs>(pageNo, pageSize);
		IPage<YoucaiFertilizerInputs> pageList = youcaiFertilizerInputsService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param youcaiFertilizerInputs
	 * @return
	 */
	@AutoLog(value = "肥料投入表-添加")
	@Operation(summary="肥料投入表-添加")
	@RequiresPermissions("youcai:youcai_fertilizer_inputs:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody YoucaiFertilizerInputs youcaiFertilizerInputs) {
		youcaiFertilizerInputsService.save(youcaiFertilizerInputs);

		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param youcaiFertilizerInputs
	 * @return
	 */
	@AutoLog(value = "肥料投入表-编辑")
	@Operation(summary="肥料投入表-编辑")
	@RequiresPermissions("youcai:youcai_fertilizer_inputs:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody YoucaiFertilizerInputs youcaiFertilizerInputs) {
		youcaiFertilizerInputsService.updateById(youcaiFertilizerInputs);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "肥料投入表-通过id删除")
	@Operation(summary="肥料投入表-通过id删除")
	@RequiresPermissions("youcai:youcai_fertilizer_inputs:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		youcaiFertilizerInputsService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "肥料投入表-批量删除")
	@Operation(summary="肥料投入表-批量删除")
	@RequiresPermissions("youcai:youcai_fertilizer_inputs:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.youcaiFertilizerInputsService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "肥料投入表-通过id查询")
	@Operation(summary="肥料投入表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<YoucaiFertilizerInputs> queryById(@RequestParam(name="id",required=true) String id) {
		YoucaiFertilizerInputs youcaiFertilizerInputs = youcaiFertilizerInputsService.getById(id);
		if(youcaiFertilizerInputs==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(youcaiFertilizerInputs);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param youcaiFertilizerInputs
    */
    @RequiresPermissions("youcai:youcai_fertilizer_inputs:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, YoucaiFertilizerInputs youcaiFertilizerInputs) {
        return super.exportXls(request, youcaiFertilizerInputs, YoucaiFertilizerInputs.class, "肥料投入表");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("youcai:youcai_fertilizer_inputs:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, YoucaiFertilizerInputs.class);
    }

}
