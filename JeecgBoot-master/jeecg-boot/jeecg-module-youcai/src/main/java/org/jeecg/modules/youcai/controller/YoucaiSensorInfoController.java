package org.jeecg.modules.youcai.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.youcai.entity.YoucaiSensorInfo;
import org.jeecg.modules.youcai.service.IYoucaiSensorInfoService;

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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.apache.shiro.authz.annotation.RequiresPermissions;

 /**
 * @Description: 传感器信息表
 * @Author: jeecg-boot
 * @Date:   2025-11-10
 * @Version: V1.0
 */
@Tag(name="传感器信息表管理")
@RestController
@RequestMapping("/youcai/sensorInfo")
@Slf4j
public class YoucaiSensorInfoController extends JeecgController<YoucaiSensorInfo, IYoucaiSensorInfoService> {
	@Autowired
	private IYoucaiSensorInfoService youcaiSensorInfoService;
	
	/**
	 * 分页列表查询
	 *
	 * @param youcaiSensorInfo 查询条件
	 * @param pageNo 页码
	 * @param pageSize 页大小
	 * @param req 请求
	 * @return
	 */
	//@AutoLog(value = "传感器信息表-分页列表查询")
	@Operation(summary="传感器信息表-分页列表查询", description="传感器信息表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<YoucaiSensorInfo>> queryPageList(YoucaiSensorInfo youcaiSensorInfo,
								   @Parameter(name="pageNo", description="页码") @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @Parameter(name="pageSize", description="页大小") @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<YoucaiSensorInfo> queryWrapper = QueryGenerator.initQueryWrapper(youcaiSensorInfo, req.getParameterMap());
		Page<YoucaiSensorInfo> page = new Page<YoucaiSensorInfo>(pageNo, pageSize);
		IPage<YoucaiSensorInfo> pageList = youcaiSensorInfoService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param youcaiSensorInfo
	 * @return
	 */
	@AutoLog(value = "传感器信息表-添加")
	@Operation(summary="传感器信息表-添加", description="传感器信息表-添加")
	@RequiresPermissions("youcai:sensor_info:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody YoucaiSensorInfo youcaiSensorInfo) {
		youcaiSensorInfoService.save(youcaiSensorInfo);
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param youcaiSensorInfo
	 * @return
	 */
	@AutoLog(value = "传感器信息表-编辑")
	@Operation(summary="传感器信息表-编辑", description="传感器信息表-编辑")
	@RequiresPermissions("youcai:sensor_info:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody YoucaiSensorInfo youcaiSensorInfo) {
		youcaiSensorInfoService.updateById(youcaiSensorInfo);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "传感器信息表-通过id删除")
	@Operation(summary="传感器信息表-通过id删除", description="传感器信息表-通过id删除")
	@RequiresPermissions("youcai:sensor_info:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@Parameter(name="id", description="主键") @RequestParam(name="id") Integer id) {
		youcaiSensorInfoService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "传感器信息表-批量删除")
	@Operation(summary="传感器信息表-批量删除", description="传感器信息表-批量删除")
	@RequiresPermissions("youcai:sensor_info:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@Parameter(name="ids", description="主键集合") @RequestParam(name="ids") String ids) {
		this.youcaiSensorInfoService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "传感器信息表-通过id查询")
	@Operation(summary="传感器信息表-通过id查询", description="传感器信息表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<YoucaiSensorInfo> queryById(@Parameter(name="id", description="主键") @RequestParam(name="id") Integer id) {
		YoucaiSensorInfo youcaiSensorInfo = youcaiSensorInfoService.getById(id);
		if(youcaiSensorInfo==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(youcaiSensorInfo);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param youcaiSensorInfo
    */
    @RequiresPermissions("youcai:sensor_info:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, YoucaiSensorInfo youcaiSensorInfo) {
        return super.exportXls(request, youcaiSensorInfo, YoucaiSensorInfo.class, "传感器信息表");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("youcai:sensor_info:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, YoucaiSensorInfo.class);
    }
    
    /**
     * 同步传感器数据
     *
     * @param projectId 项目ID
     * @return
     */
    @AutoLog(value = "传感器信息表-同步传感器数据")
    @Operation(summary="传感器信息表-同步传感器数据", description="传感器信息表-同步传感器数据")
    @PostMapping(value = "/syncSensorData")
    public Result<String> syncSensorData(@Parameter(name="projectId", description="项目ID") @RequestParam(name="projectId") Integer projectId) {
        // TODO: 实现同步传感器数据的逻辑
        return Result.OK("同步成功!");
    }
}