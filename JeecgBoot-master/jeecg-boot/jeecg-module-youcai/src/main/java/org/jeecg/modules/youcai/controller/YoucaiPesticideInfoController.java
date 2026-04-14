package org.jeecg.modules.youcai.controller;

import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.youcai.entity.YoucaiPesticideInfo;
import org.jeecg.modules.youcai.service.IYoucaiPesticideInfoService;

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
 * @Description: 农药信息表
 * @Author: jeecg-boot
 * @Date:   2025-10-30
 * @Version: V1.0
 */
@Tag(name="农药信息表")
@RestController
@RequestMapping("/youcai/youcaiPesticideInfo")
@Slf4j
public class YoucaiPesticideInfoController extends JeecgController<YoucaiPesticideInfo, IYoucaiPesticideInfoService> {
	@Autowired
	private IYoucaiPesticideInfoService youcaiPesticideInfoService;
	
	/**
	 * 分页列表查询
	 *
	 * @param youcaiPesticideInfo
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "农药信息表-分页列表查询")
	@Operation(summary="农药信息表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<YoucaiPesticideInfo>> queryPageList(YoucaiPesticideInfo youcaiPesticideInfo,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<YoucaiPesticideInfo> queryWrapper = QueryGenerator.initQueryWrapper(youcaiPesticideInfo, req.getParameterMap());
		Page<YoucaiPesticideInfo> page = new Page<YoucaiPesticideInfo>(pageNo, pageSize);
		IPage<YoucaiPesticideInfo> pageList = youcaiPesticideInfoService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param youcaiPesticideInfo
	 * @return
	 */
	@AutoLog(value = "农药信息表-添加")
	@Operation(summary="农药信息表-添加")
	@RequiresPermissions("youcai:youcai_pesticide_info:add")
	@PostMapping(value = "/add")
	public Result<String> add(@Valid @RequestBody YoucaiPesticideInfo youcaiPesticideInfo) {
		youcaiPesticideInfoService.save(youcaiPesticideInfo);
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param youcaiPesticideInfo
	 * @return
	 */
	@AutoLog(value = "农药信息表-编辑")
	@Operation(summary="农药信息表-编辑")
	@RequiresPermissions("youcai:youcai_pesticide_info:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@Valid @RequestBody YoucaiPesticideInfo youcaiPesticideInfo) {
		youcaiPesticideInfoService.updateById(youcaiPesticideInfo);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "农药信息表-通过id删除")
	@Operation(summary="农药信息表-通过id删除")
	@RequiresPermissions("youcai:youcai_pesticide_info:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		youcaiPesticideInfoService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "农药信息表-批量删除")
	@Operation(summary="农药信息表-批量删除")
	@RequiresPermissions("youcai:youcai_pesticide_info:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.youcaiPesticideInfoService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "农药信息表-通过id查询")
	@Operation(summary="农药信息表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<YoucaiPesticideInfo> queryById(@RequestParam(name="id",required=true) String id) {
		YoucaiPesticideInfo youcaiPesticideInfo = youcaiPesticideInfoService.getById(id);
		if(youcaiPesticideInfo==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(youcaiPesticideInfo);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param youcaiPesticideInfo
    */
    @RequiresPermissions("youcai:youcai_pesticide_info:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, YoucaiPesticideInfo youcaiPesticideInfo) {
        return super.exportXls(request, youcaiPesticideInfo, YoucaiPesticideInfo.class, "农药信息表");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("youcai:youcai_pesticide_info:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, YoucaiPesticideInfo.class);
    }

     @GetMapping("/name/list")
     public Result<List<String>> queryPesticideNameList() {
         try {
             List<String> nameList = youcaiPesticideInfoService.getAllNames();
             return Result.OK(nameList);
         } catch (Exception e) {
             log.error("查询农药名称失败", e);
             return Result.error("查询农药名称失败");
         }
     }

     /**
      * 查询所有农药信息数据
      * @return 全部农药信息列表
      */
     @Operation(summary="农药信息表-查询所有数据")
     @GetMapping(value = "/queryAll")
     public Result<List<YoucaiPesticideInfo>> queryAll() {
         QueryWrapper<YoucaiPesticideInfo> queryWrapper = new QueryWrapper<>();
         queryWrapper.orderByDesc("update_time");
         List<YoucaiPesticideInfo> list = youcaiPesticideInfoService.list(queryWrapper);
         if(list.isEmpty()) {
             return Result.error("暂无农药信息数据");
         }
         return Result.OK(list);
     }


 }