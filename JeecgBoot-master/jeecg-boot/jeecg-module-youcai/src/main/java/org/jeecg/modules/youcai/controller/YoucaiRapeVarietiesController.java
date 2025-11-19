package org.jeecg.modules.youcai.controller;

import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.youcai.entity.YoucaiBases;
import org.jeecg.modules.youcai.entity.YoucaiRapeVarieties;
import org.jeecg.modules.youcai.service.IYoucaiRapeVarietiesService;

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
 * @Description: 油菜品种表
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V1.0
 */
@Tag(name="油菜品种表")
@RestController
@RequestMapping("/youcai/youcaiRapeVarieties")
@Slf4j
public class YoucaiRapeVarietiesController extends JeecgController<YoucaiRapeVarieties, IYoucaiRapeVarietiesService> {
	@Autowired
	private IYoucaiRapeVarietiesService youcaiRapeVarietiesService;
	
	/**
	 * 分页列表查询
	 *
	 * @param youcaiRapeVarieties
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "油菜品种表-分页列表查询")
	@Operation(summary="油菜品种表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<YoucaiRapeVarieties>> queryPageList(YoucaiRapeVarieties youcaiRapeVarieties,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {


        QueryWrapper<YoucaiRapeVarieties> queryWrapper = QueryGenerator.initQueryWrapper(youcaiRapeVarieties, req.getParameterMap());
		Page<YoucaiRapeVarieties> page = new Page<YoucaiRapeVarieties>(pageNo, pageSize);
		IPage<YoucaiRapeVarieties> pageList = youcaiRapeVarietiesService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param youcaiRapeVarieties
	 * @return
	 */
	@AutoLog(value = "油菜品种表-添加")
	@Operation(summary="油菜品种表-添加")
//	@RequiresPermissions("youcai:youcai_rape_varieties:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody YoucaiRapeVarieties youcaiRapeVarieties) {
		youcaiRapeVarietiesService.save(youcaiRapeVarieties);

		return Result.OK("添加成功！");
	}


	 /**
	  * 查询所有油菜品种
	  *
	  * @return 所有油菜品种列表
	  */
	 @Operation(summary = "查询所有油菜品种")
	 @GetMapping(value = "/getAll")
	 public Result<List<YoucaiRapeVarieties>> getAllVarieties() {
		 List<YoucaiRapeVarieties> list = youcaiRapeVarietiesService.list();
		 return Result.OK(list);
	 }
	/**
	 *  编辑
	 *
	 * @param youcaiRapeVarieties
	 * @return
	 */
	@AutoLog(value = "油菜品种表-编辑")
	@Operation(summary="油菜品种表-编辑")
//	@RequiresPermissions("youcai:youcai_rape_varieties:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody YoucaiRapeVarieties youcaiRapeVarieties) {
		youcaiRapeVarietiesService.updateById(youcaiRapeVarieties);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "油菜品种表-通过id删除")
	@Operation(summary="油菜品种表-通过id删除")
	@RequiresPermissions("youcai:youcai_rape_varieties:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		youcaiRapeVarietiesService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "油菜品种表-批量删除")
	@Operation(summary="油菜品种表-批量删除")
	@RequiresPermissions("youcai:youcai_rape_varieties:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.youcaiRapeVarietiesService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "油菜品种表-通过id查询")
	@Operation(summary="油菜品种表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<YoucaiRapeVarieties> queryById(@RequestParam(name="id",required=true) String id) {
		YoucaiRapeVarieties youcaiRapeVarieties = youcaiRapeVarietiesService.getById(id);
		if(youcaiRapeVarieties==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(youcaiRapeVarieties);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param youcaiRapeVarieties
    */
    @RequiresPermissions("youcai:youcai_rape_varieties:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, YoucaiRapeVarieties youcaiRapeVarieties) {
        return super.exportXls(request, youcaiRapeVarieties, YoucaiRapeVarieties.class, "油菜品种表");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @return
    */
    @RequiresPermissions("youcai:youcai_rape_varieties:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, YoucaiRapeVarieties.class);
    }

}
