package org.jeecg.modules.youcai.controller;

import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.youcai.dto.LodgingRiskAssessmentResponseDTO;
import org.jeecg.modules.youcai.dto.VideoLodgingAnalysisResultDTO;
import org.jeecg.modules.youcai.entity.YoucaiLodgingRiskWarning;
import org.jeecg.modules.youcai.service.IYoucaiLodgingRiskWarningService;

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
 * @Description: 倒伏风险预警表
 * @Author: jeecg-boot
 * @Date:   2025-10-18
 * @Version: V1.0
 */
@Tag(name="倒伏风险预警")
@RestController
@RequestMapping("/youcai/lodgingRisk")
@Slf4j
public class YoucaiLodgingRiskWarningController extends JeecgController<YoucaiLodgingRiskWarning, IYoucaiLodgingRiskWarningService> {
	@Autowired
	private IYoucaiLodgingRiskWarningService youcaiLodgingRiskWarningService;

     /**
      * 根据地块Id查询倒伏风险数据
      */
	
    @AutoLog(value = "倒伏风险预警表-获取倒伏风险数据")
    @Operation(summary="倒伏风险预警表-获取倒伏风险数据")
    @GetMapping(value = "/riskData/{plotId}")
    public Result<LodgingRiskAssessmentResponseDTO> riskAssessment(@PathVariable String plotId) {
        LodgingRiskAssessmentResponseDTO riskData = youcaiLodgingRiskWarningService.riskAssessmentById(plotId);
        log.info("riskData={}", riskData);
        return Result.OK(riskData);
    }
	
	/**
	 * 批量获取基地下所有地块的倒伏风险数据
	 */
	@AutoLog(value = "倒伏风险预警表-批量获取基地下所有地块的倒伏风险数据")
	@Operation(summary="倒伏风险预警表-批量获取基地下所有地块的倒伏风险数据")
	@GetMapping(value = "/batchRiskData/{baseId}")
    public Result<LodgingRiskAssessmentResponseDTO.BatchLodgingRiskAssessmentResponseDTO> batchRiskAssessment(@PathVariable String baseId) {
        LodgingRiskAssessmentResponseDTO.BatchLodgingRiskAssessmentResponseDTO batchRiskData = 
            youcaiLodgingRiskWarningService.batchRiskAssessmentByBaseId(baseId);
        log.info("batchRiskData={}", batchRiskData);
        return Result.OK(batchRiskData);
    }

	/**
	 * 根据视频ID进行倒伏分析
	 */
	@AutoLog(value = "倒伏风险预警表-视频倒伏分析")
	@Operation(summary="倒伏风险预警表-视频倒伏分析")
	@GetMapping(value = "/analysisVideo/{videoId}")
	public Result<VideoLodgingAnalysisResultDTO> videoLodgingAnalysis(@PathVariable String videoId) {
		try {
			VideoLodgingAnalysisResultDTO result = youcaiLodgingRiskWarningService.analyzeLodgingByVideoId(videoId);
			return Result.OK(result);
		} catch (Exception e) {
			log.error("视频倒伏分析失败: {}", e.getMessage(), e);
			return Result.error("视频倒伏分析失败: " + e.getMessage());
		}
	}

	/**
	 * 批量根据视频ID进行倒伏分析
	 */
	@AutoLog(value = "倒伏风险预警表-批量视频倒伏分析")
	@Operation(summary="倒伏风险预警表-批量视频倒伏分析")
	@PostMapping(value = "/analysisVideo/batch")
	public Result<VideoLodgingAnalysisResultDTO> batchVideoLodgingAnalysis(@RequestBody java.util.List<String> videoIds) {
		try {
			if (videoIds == null || videoIds.isEmpty()) {
				return Result.error("视频ID列表不能为空");
			}
			VideoLodgingAnalysisResultDTO result = youcaiLodgingRiskWarningService.batchAnalyzeLodgingByVideoIds(videoIds);
			return Result.OK(result);
		} catch (Exception e) {
			log.error("批量视频倒伏分析失败: {}", e.getMessage(), e);
			return Result.error("批量视频倒伏分析失败: " + e.getMessage());
		}
	}



	
	/**
	 * 分页列表查询
	 *
	 * @param youcaiLodgingRiskWarning
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "倒伏风险预警表-分页列表查询")
	@Operation(summary="倒伏风险预警表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<YoucaiLodgingRiskWarning>> queryPageList(YoucaiLodgingRiskWarning youcaiLodgingRiskWarning,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {


        QueryWrapper<YoucaiLodgingRiskWarning> queryWrapper = QueryGenerator.initQueryWrapper(youcaiLodgingRiskWarning, req.getParameterMap());
		Page<YoucaiLodgingRiskWarning> page = new Page<YoucaiLodgingRiskWarning>(pageNo, pageSize);
		IPage<YoucaiLodgingRiskWarning> pageList = youcaiLodgingRiskWarningService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param youcaiLodgingRiskWarning
	 * @return
	 */
	@AutoLog(value = "倒伏风险预警表-添加")
	@Operation(summary="倒伏风险预警表-添加")
	@RequiresPermissions("youcai:youcai_lodging_risk_warning:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody YoucaiLodgingRiskWarning youcaiLodgingRiskWarning) {
		youcaiLodgingRiskWarningService.save(youcaiLodgingRiskWarning);

		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param youcaiLodgingRiskWarning
	 * @return
	 */
	@AutoLog(value = "倒伏风险预警表-编辑")
	@Operation(summary="倒伏风险预警表-编辑")
	@RequiresPermissions("youcai:youcai_lodging_risk_warning:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody YoucaiLodgingRiskWarning youcaiLodgingRiskWarning) {
		youcaiLodgingRiskWarningService.updateById(youcaiLodgingRiskWarning);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "倒伏风险预警表-通过id删除")
	@Operation(summary="倒伏风险预警表-通过id删除")
	@RequiresPermissions("youcai:youcai_lodging_risk_warning:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		youcaiLodgingRiskWarningService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "倒伏风险预警表-批量删除")
	@Operation(summary="倒伏风险预警表-批量删除")
	@RequiresPermissions("youcai:youcai_lodging_risk_warning:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.youcaiLodgingRiskWarningService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "倒伏风险预警表-通过id查询")
	@Operation(summary="倒伏风险预警表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<YoucaiLodgingRiskWarning> queryById(@RequestParam(name="id",required=true) String id) {
		YoucaiLodgingRiskWarning youcaiLodgingRiskWarning = youcaiLodgingRiskWarningService.getById(id);
		if(youcaiLodgingRiskWarning==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(youcaiLodgingRiskWarning);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param youcaiLodgingRiskWarning
    */
    @RequiresPermissions("youcai:youcai_lodging_risk_warning:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, YoucaiLodgingRiskWarning youcaiLodgingRiskWarning) {
        return super.exportXls(request, youcaiLodgingRiskWarning, YoucaiLodgingRiskWarning.class, "倒伏风险预警表");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("youcai:youcai_lodging_risk_warning:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, YoucaiLodgingRiskWarning.class);
    }

}
