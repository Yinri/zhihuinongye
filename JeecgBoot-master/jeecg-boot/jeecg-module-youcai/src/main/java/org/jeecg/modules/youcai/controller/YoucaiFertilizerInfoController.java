package org.jeecg.modules.youcai.controller;

import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.youcai.entity.YoucaiFertilizerInfo;
import org.jeecg.modules.youcai.service.IYoucaiFertilizerInfoService;

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
 * @Description: 肥料信息表
 * @Author: jeecg-boot
 * @Date:   2025-10-30
 * @Version: V1.0
 */
@Tag(name="肥料信息表")
@RestController
@RequestMapping("/youcai/youcaiFertilizerInfo")
@Slf4j
public class YoucaiFertilizerInfoController extends JeecgController<YoucaiFertilizerInfo, IYoucaiFertilizerInfoService> {
	@Autowired
	private IYoucaiFertilizerInfoService youcaiFertilizerInfoService;
	
	/**
	 * 分页列表查询
	 *
	 * @param youcaiFertilizerInfo
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "肥料信息表-分页列表查询")
	@Operation(summary="肥料信息表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<YoucaiFertilizerInfo>> queryPageList(YoucaiFertilizerInfo youcaiFertilizerInfo,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<YoucaiFertilizerInfo> queryWrapper = QueryGenerator.initQueryWrapper(youcaiFertilizerInfo, req.getParameterMap());
		Page<YoucaiFertilizerInfo> page = new Page<YoucaiFertilizerInfo>(pageNo, pageSize);
		IPage<YoucaiFertilizerInfo> pageList = youcaiFertilizerInfoService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param youcaiFertilizerInfo
	 * @return
	 */
	@AutoLog(value = "肥料信息表-添加")
	@Operation(summary="肥料信息表-添加")
	@RequiresPermissions("youcai:youcai_fertilizer_info:add")
	@PostMapping(value = "/add")
	public Result<String> add(@Valid @RequestBody YoucaiFertilizerInfo youcaiFertilizerInfo) {
		youcaiFertilizerInfoService.save(youcaiFertilizerInfo);
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param youcaiFertilizerInfo
	 * @return
	 */
	@AutoLog(value = "肥料信息表-编辑")
	@Operation(summary="肥料信息表-编辑")
	@RequiresPermissions("youcai:youcai_fertilizer_info:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@Valid @RequestBody YoucaiFertilizerInfo youcaiFertilizerInfo) {
		youcaiFertilizerInfoService.updateById(youcaiFertilizerInfo);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "肥料信息表-通过id删除")
	@Operation(summary="肥料信息表-通过id删除")
	@RequiresPermissions("youcai:youcai_fertilizer_info:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		youcaiFertilizerInfoService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "肥料信息表-批量删除")
	@Operation(summary="肥料信息表-批量删除")
	@RequiresPermissions("youcai:youcai_fertilizer_info:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.youcaiFertilizerInfoService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "肥料信息表-通过id查询")
	@Operation(summary="肥料信息表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<YoucaiFertilizerInfo> queryById(@RequestParam(name="id",required=true) String id) {
		YoucaiFertilizerInfo youcaiFertilizerInfo = youcaiFertilizerInfoService.getById(id);
		if(youcaiFertilizerInfo==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(youcaiFertilizerInfo);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param youcaiFertilizerInfo
    */
    @RequiresPermissions("youcai:youcai_fertilizer_info:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, YoucaiFertilizerInfo youcaiFertilizerInfo) {
        return super.exportXls(request, youcaiFertilizerInfo, YoucaiFertilizerInfo.class, "肥料信息表");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("youcai:youcai_fertilizer_info:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, YoucaiFertilizerInfo.class);
    }

}