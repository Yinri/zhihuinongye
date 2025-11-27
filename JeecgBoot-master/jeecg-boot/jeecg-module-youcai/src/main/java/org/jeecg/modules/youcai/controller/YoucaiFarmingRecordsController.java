package org.jeecg.modules.youcai.controller;

import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.youcai.entity.YoucaiFarmingRecords;
import org.jeecg.modules.youcai.service.IYoucaiFarmingRecordsService;

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
 * @Description: 农事记录表
 * @Author: jeecg-boot
 * @Date: 2025-10-30
 * @Version: V1.0
 */
@Tag(name="农事记录表")
@RestController
@RequestMapping("/youcai/farmingRecords")
@Slf4j
public class YoucaiFarmingRecordsController extends JeecgController<YoucaiFarmingRecords, IYoucaiFarmingRecordsService> {


    
	@Autowired
	private IYoucaiFarmingRecordsService youcaiFarmingRecordsService;
	
	/**
	 * 分页列表查询
	 *
	 * @param youcaiFarmingRecords
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "农事记录表-分页列表查询")
	@Operation(summary="农事记录表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<YoucaiFarmingRecords>> queryPageList(YoucaiFarmingRecords youcaiFarmingRecords,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<YoucaiFarmingRecords> queryWrapper = QueryGenerator.initQueryWrapper(youcaiFarmingRecords, req.getParameterMap());
		Page<YoucaiFarmingRecords> page = new Page<YoucaiFarmingRecords>(pageNo, pageSize);
		IPage<YoucaiFarmingRecords> pageList = youcaiFarmingRecordsService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param youcaiFarmingRecords
	 * @return
	 */
	@AutoLog(value = "农事记录表-添加")
	@Operation(summary="农事记录表-添加")
	@RequiresPermissions("youcai:youcai_farming_records:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody YoucaiFarmingRecords youcaiFarmingRecords) {
		youcaiFarmingRecordsService.save(youcaiFarmingRecords);
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param youcaiFarmingRecords
	 * @return
	 */
	@AutoLog(value = "农事记录表-编辑")
	@Operation(summary="农事记录表-编辑")
	@RequiresPermissions("youcai:youcai_farming_records:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody YoucaiFarmingRecords youcaiFarmingRecords) {
		youcaiFarmingRecordsService.updateById(youcaiFarmingRecords);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "农事记录表-通过id删除")
	@Operation(summary="农事记录表-通过id删除")
	@RequiresPermissions("youcai:youcai_farming_records:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		youcaiFarmingRecordsService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "农事记录表-批量删除")
	@Operation(summary="农事记录表-批量删除")
	@RequiresPermissions("youcai:youcai_farming_records:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.youcaiFarmingRecordsService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "农事记录表-通过id查询")
	@Operation(summary="农事记录表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<YoucaiFarmingRecords> queryById(@RequestParam(name="id",required=true) String id) {
		YoucaiFarmingRecords youcaiFarmingRecords = youcaiFarmingRecordsService.getById(id);
		if(youcaiFarmingRecords==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(youcaiFarmingRecords);
	}
	
	/**
	 * 根据基地ID分页查询农事记录，按农事日期排序
	 *
	 * @param baseId 基地ID
	 * @param pageNo 页码
	 * @param pageSize 每页条数
	 * @param req 请求
	 * @return 分页结果
	 */
	@Operation(summary="农事记录表-根据基地ID分页查询")
	@GetMapping(value = "/queryByBaseId")
	public Result<IPage<YoucaiFarmingRecords>> queryByBaseId(@RequestParam(name="baseId",required=false) String baseId,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<YoucaiFarmingRecords> queryWrapper = new QueryWrapper<>();
		if (baseId != null && !baseId.isEmpty()) {
			queryWrapper.eq("base_id", baseId);
		}
		queryWrapper.orderByDesc("farming_date");
		Page<YoucaiFarmingRecords> page = new Page<YoucaiFarmingRecords>(pageNo, pageSize);
		IPage<YoucaiFarmingRecords> pageList = youcaiFarmingRecordsService.page(page, queryWrapper);
		return Result.OK(pageList);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param youcaiFarmingRecords
    */
    @RequiresPermissions("youcai:youcai_farming_records:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, YoucaiFarmingRecords youcaiFarmingRecords) {
        return super.exportXls(request, youcaiFarmingRecords, YoucaiFarmingRecords.class, "农事记录表");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("youcai:youcai_farming_records:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, YoucaiFarmingRecords.class);
    }

}