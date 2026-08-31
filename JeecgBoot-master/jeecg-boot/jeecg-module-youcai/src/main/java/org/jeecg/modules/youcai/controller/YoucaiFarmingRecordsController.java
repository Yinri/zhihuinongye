package org.jeecg.modules.youcai.controller;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.system.query.QueryGenerator;
//import org.jeecg.modules.youcai.dto.YoucaiFarmingRecordsExcel;
import org.jeecg.modules.youcai.entity.YoucaiFarmingRecords;
import org.jeecg.modules.youcai.service.IYoucaiFarmingRecordsService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.base.controller.JeecgController;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.youcai.entity.YoucaiBases;
import org.jeecg.modules.youcai.entity.YoucaiGrowthMonitoring;
import org.jeecg.modules.youcai.entity.YoucaiPlots;
import org.jeecg.modules.youcai.service.IYoucaiBasesService;
import org.jeecg.modules.youcai.service.IYoucaiGrowthMonitoringService;
import org.jeecg.modules.youcai.service.IYoucaiPlotsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.entity.enmus.ExcelType;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;

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

	@Autowired
	private IYoucaiBasesService youcaiBasesService;

	@Autowired
	private IYoucaiGrowthMonitoringService growthMonitoringService;

	@Autowired
	private IYoucaiPlotsService youcaiPlotsService;
	
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
	//@RequiresPermissions("youcai:youcai_farming_records:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody YoucaiFarmingRecords youcaiFarmingRecords) {
		if (!StringUtils.hasText(youcaiFarmingRecords.getRecordCode())) {
			youcaiFarmingRecords.setRecordCode(generateRecordCode(youcaiFarmingRecords.getBaseId()));
		}
		youcaiFarmingRecordsService.save(youcaiFarmingRecords);
		return Result.OK("添加成功！");
	}

	private String generateRecordCode(String baseId) {
		String seqPrefix = buildRecordCodePrefix(baseId);
		int seq = getNextSeq(seqPrefix);
		return formatRecordCode(seqPrefix, seq);
	}

	private String generateRecordCode(String baseId, Map<String, Integer> importSeqMap) {
		String seqPrefix = buildRecordCodePrefix(baseId);
		int seq = importSeqMap.compute(seqPrefix, (key, value) -> value == null ? getNextSeq(key) : value + 1);
		return formatRecordCode(seqPrefix, seq);
	}

	private String buildRecordCodePrefix(String baseId) {
		// 1. 基地缩写
		String prefix = "YCC";
		if (StringUtils.hasText(baseId)) {
			YoucaiBases base = youcaiBasesService.getById(baseId);
			if (base != null && StringUtils.hasText(base.getCodePrefix())) {
				prefix = base.getCodePrefix();
			}
		}
		// 2. 年份
		String year = String.valueOf(java.time.Year.now().getValue());
		// 3. 生长阶段缩写
		String stageAbbr = "";
		if (StringUtils.hasText(baseId)) {
			LambdaQueryWrapper<YoucaiGrowthMonitoring> qw = new LambdaQueryWrapper<>();
			qw.eq(YoucaiGrowthMonitoring::getBaseId, baseId).orderByDesc(YoucaiGrowthMonitoring::getMonitoringDate).last("LIMIT 1");
			YoucaiGrowthMonitoring gm = growthMonitoringService.getOne(qw);
			if (gm != null && StringUtils.hasText(gm.getGrowthStage())) {
				stageAbbr = growthStageToAbbr(gm.getGrowthStage());
			}
		}
		// 4. 同前缀序号
		return prefix + "-" + year + (stageAbbr.isEmpty() ? "" : "-" + stageAbbr);
	}

	private String formatRecordCode(String seqPrefix, int seq) {
		return seqPrefix + "-" + String.format("%03d", seq);
	}

	private String growthStageToAbbr(String stage) {
		if (stage == null) return "";
		if (stage.contains("发芽") || stage.contains("出苗")) return "FY";
		if (stage.contains("苗期")) return "MQ";
		if (stage.contains("蕾薹")) return "LT";
		if (stage.contains("开花")) return "KH";
		if (stage.contains("角果") || stage.contains("成熟")) return "JG";
		return "";
	}

	private int getNextSeq(String prefix) {
		LambdaQueryWrapper<YoucaiFarmingRecords> qw = new LambdaQueryWrapper<>();
		qw.likeRight(YoucaiFarmingRecords::getRecordCode, prefix).orderByDesc(YoucaiFarmingRecords::getRecordCode).last("LIMIT 1");
		YoucaiFarmingRecords latest = youcaiFarmingRecordsService.getOne(qw);
		if (latest == null || !StringUtils.hasText(latest.getRecordCode())) return 1;
		String code = latest.getRecordCode();
		int lastDash = code.lastIndexOf('-');
		if (lastDash < 0) return 1;
		try { return Integer.parseInt(code.substring(lastDash + 1)) + 1; } catch (NumberFormatException e) { return 1; }
	}
	
	/**
	 *  编辑
	 *
	 * @param youcaiFarmingRecords
	 * @return
	 */
	@AutoLog(value = "农事记录表-编辑")
	@Operation(summary="农事记录表-编辑")
	//@RequiresPermissions("youcai:youcai_farming_records:edit")
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
	//@RequiresPermissions("youcai:youcai_farming_records:delete")
	@DeleteMapping(value = "/delete/{id}")
	public Result<String> delete(@PathVariable(name="id",required=true) String id) {
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
	//@RequiresPermissions("youcai:youcai_farming_records:deleteBatch")
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
//
//    /**
//    * 导出excel
//    *
//    * @param request
//    * @param youcaiFarmingRecords
//    */
    //@RequiresPermissions("youcai:youcai_farming_records:exportXls")
//    @RequestMapping(value = "/exportXls")
//    public ModelAndView exportXls(HttpServletRequest request, YoucaiFarmingRecords youcaiFarmingRecords) {
//		QueryWrapper<YoucaiFarmingRecords> queryWrapper = QueryGenerator.initQueryWrapper(youcaiFarmingRecords, request.getParameterMap());
//		String selections = request.getParameter("selections");
//		if (StringUtils.hasText(selections)) {
//			queryWrapper.in("id", Arrays.asList(selections.split(",")));
//		}
//		List<YoucaiFarmingRecords> records = youcaiFarmingRecordsService.list(queryWrapper);
//		Map<String, YoucaiBases> baseMap = youcaiBasesService.list().stream()
//				.collect(Collectors.toMap(YoucaiBases::getId, item -> item, (left, right) -> left));
//		Map<String, YoucaiPlots> plotMap = youcaiPlotsService.list().stream()
//				.collect(Collectors.toMap(YoucaiPlots::getId, item -> item, (left, right) -> left));
//		List<YoucaiFarmingRecordsExcel> exportList = records.stream()
//				.map(item -> toExcel(item, baseMap, plotMap))
//				.collect(Collectors.toList());
//
//		ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
//		mv.addObject(NormalExcelConstants.FILE_NAME, "农事记录表");
//		mv.addObject(NormalExcelConstants.CLASS, YoucaiFarmingRecordsExcel.class);
//		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
//		String realname = sysUser == null ? "" : sysUser.getRealname();
//		ExportParams exportParams = new ExportParams("农事记录表报表", "导出人:" + realname, "农事记录表");
//		exportParams.setType(ExcelType.XSSF);
//		mv.addObject(NormalExcelConstants.PARAMS, exportParams);
//		mv.addObject(NormalExcelConstants.DATA_LIST, exportList);
//		return mv;
//    }
//
//    /**
//      * 通过excel导入数据
//    *
//    * @param request
//    * @param response
//    * @return
//    */
//    //@RequiresPermissions("youcai:youcai_farming_records:importExcel")
//    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
//    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
//		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
//		Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
//		if (fileMap.isEmpty()) {
//			return Result.error("文件导入失败：未获取到上传文件");
//		}
//		for (MultipartFile file : fileMap.values()) {
//			ImportParams params = new ImportParams();
//			params.setTitleRows(2);
//			params.setHeadRows(1);
//			params.setNeedSave(false);
//			try {
//				List<YoucaiFarmingRecordsExcel> excelList = ExcelImportUtil.importExcel(file.getInputStream(), YoucaiFarmingRecordsExcel.class, params);
//				if (excelList == null || excelList.isEmpty()) {
//					return Result.error("文件导入失败：未读取到数据");
//				}
//				List<YoucaiFarmingRecords> records = new ArrayList<>(excelList.size());
//				Map<String, Integer> importSeqMap = new HashMap<>();
//				Set<String> recordCodes = new HashSet<>();
//				for (int i = 0; i < excelList.size(); i++) {
//					YoucaiFarmingRecords record = fromExcel(excelList.get(i), i + 1, importSeqMap, recordCodes);
//					records.add(record);
//				}
//				youcaiFarmingRecordsService.saveBatch(records);
//				return Result.ok("文件导入成功！数据行数：" + records.size());
//			} catch (IllegalArgumentException e) {
//				log.warn("农事记录导入校验失败", e);
//				return Result.error("文件导入失败：" + e.getMessage());
//			} catch (Exception e) {
//				String msg = e.getMessage();
//				log.error("农事记录导入失败", e);
//				if (msg != null && msg.contains("Duplicate entry")) {
//					return Result.error("文件导入失败：记录编号重复");
//				}
//				return Result.error("文件导入失败：" + msg);
//			}
//		}
//		return Result.error("文件导入失败！");
//    }
//
//	private YoucaiFarmingRecordsExcel toExcel(YoucaiFarmingRecords item, Map<String, YoucaiBases> baseMap, Map<String, YoucaiPlots> plotMap) {
//		YoucaiFarmingRecordsExcel excel = new YoucaiFarmingRecordsExcel();
//		excel.setRecordCode(item.getRecordCode());
//		excel.setBaseId(item.getBaseId());
//		YoucaiBases base = baseMap.get(item.getBaseId());
//		excel.setBaseName(base == null ? null : base.getBaseName());
//		excel.setPlotId(item.getPlotId());
//		YoucaiPlots plot = plotMap.get(item.getPlotId());
//		excel.setPlotName(plot == null ? null : plot.getPlotName());
//		excel.setFarmingType(farmingTypeToName(item.getFarmingType()));
//		excel.setFarmingDate(item.getFarmingDate());
//		excel.setWorker(item.getWorker());
//		excel.setWorkArea(item.getWorkArea());
//		excel.setMaterials(item.getMaterials());
//		excel.setMaterialAmount(item.getMaterialAmount());
//		excel.setWorkDuration(item.getWorkDuration());
//		excel.setWorkStatus(workStatusToName(item.getWorkStatus()));
//		excel.setRemark(item.getRemark());
//		return excel;
//	}
//
//	private YoucaiFarmingRecords fromExcel(YoucaiFarmingRecordsExcel excel, int rowNo, Map<String, Integer> importSeqMap, Set<String> recordCodes) {
//		YoucaiBases base = resolveBase(excel.getBaseId(), excel.getBaseName(), rowNo);
//		YoucaiPlots plot = resolvePlot(excel.getPlotId(), excel.getPlotName(), base.getId(), rowNo);
//		YoucaiFarmingRecords record = new YoucaiFarmingRecords();
//		record.setBaseId(base.getId());
//		record.setPlotId(plot == null ? null : plot.getId());
//		record.setFarmingType(parseFarmingType(excel.getFarmingType(), rowNo));
//		record.setFarmingDate(excel.getFarmingDate());
//		record.setWorker(excel.getWorker());
//		record.setWorkArea(excel.getWorkArea());
//		record.setMaterials(excel.getMaterials());
//		record.setMaterialAmount(excel.getMaterialAmount());
//		record.setWorkDuration(excel.getWorkDuration());
//		record.setWorkStatus(parseWorkStatus(excel.getWorkStatus(), rowNo));
//		record.setRemark(excel.getRemark());
//		record.setDelFlag(0);
//
//		String recordCode = excel.getRecordCode();
//		if (!StringUtils.hasText(recordCode)) {
//			recordCode = generateRecordCode(base.getId(), importSeqMap);
//		}
//		if (!recordCodes.add(recordCode)) {
//			throw new IllegalArgumentException("第" + rowNo + "行记录编号在本次导入中重复：" + recordCode);
//		}
//		LambdaQueryWrapper<YoucaiFarmingRecords> qw = new LambdaQueryWrapper<>();
//		qw.eq(YoucaiFarmingRecords::getRecordCode, recordCode).last("LIMIT 1");
//		if (youcaiFarmingRecordsService.getOne(qw) != null) {
//			throw new IllegalArgumentException("第" + rowNo + "行记录编号已存在：" + recordCode);
//		}
//		record.setRecordCode(recordCode);
//		return record;
//	}

	private YoucaiBases resolveBase(String baseId, String baseName, int rowNo) {
		if (StringUtils.hasText(baseId)) {
			YoucaiBases base = youcaiBasesService.getById(baseId);
			if (base == null) {
				throw new IllegalArgumentException("第" + rowNo + "行基地ID不存在：" + baseId);
			}
			return base;
		}
		if (!StringUtils.hasText(baseName)) {
			throw new IllegalArgumentException("第" + rowNo + "行基地ID或基地名称必填");
		}
		LambdaQueryWrapper<YoucaiBases> qw = new LambdaQueryWrapper<>();
		qw.eq(YoucaiBases::getBaseName, baseName).last("LIMIT 2");
		List<YoucaiBases> bases = youcaiBasesService.list(qw);
		if (bases.isEmpty()) {
			throw new IllegalArgumentException("第" + rowNo + "行基地名称不存在：" + baseName);
		}
		if (bases.size() > 1) {
			throw new IllegalArgumentException("第" + rowNo + "行基地名称不唯一，请填写基地ID：" + baseName);
		}
		return bases.get(0);
	}

	private YoucaiPlots resolvePlot(String plotId, String plotName, String baseId, int rowNo) {
		if (StringUtils.hasText(plotId)) {
			YoucaiPlots plot = youcaiPlotsService.getById(plotId);
			if (plot == null) {
				throw new IllegalArgumentException("第" + rowNo + "行地块ID不存在：" + plotId);
			}
			if (StringUtils.hasText(plot.getBaseId()) && !baseId.equals(plot.getBaseId())) {
				throw new IllegalArgumentException("第" + rowNo + "行地块不属于当前基地：" + plotId);
			}
			return plot;
		}
		if (!StringUtils.hasText(plotName)) {
			return null;
		}
		LambdaQueryWrapper<YoucaiPlots> qw = new LambdaQueryWrapper<>();
		qw.eq(YoucaiPlots::getPlotName, plotName).eq(YoucaiPlots::getBaseId, baseId).last("LIMIT 2");
		List<YoucaiPlots> plots = youcaiPlotsService.list(qw);
		if (plots.isEmpty()) {
			throw new IllegalArgumentException("第" + rowNo + "行当前基地下不存在地块名称：" + plotName);
		}
		if (plots.size() > 1) {
			throw new IllegalArgumentException("第" + rowNo + "行地块名称不唯一，请填写地块ID：" + plotName);
		}
		return plots.get(0);
	}

	private Integer parseFarmingType(String value, int rowNo) {
		if (!StringUtils.hasText(value)) {
			throw new IllegalArgumentException("第" + rowNo + "行农事类型必填");
		}
		String text = value.trim();
		if (text.startsWith("1") || text.equals("播种")) return 1;
		if (text.startsWith("2") || text.equals("施肥")) return 2;
		if (text.startsWith("3") || text.equals("灌溉")) return 3;
		if (text.startsWith("4") || text.equals("除草")) return 4;
		if (text.startsWith("5") || text.equals("病虫害防治")) return 5;
		if (text.startsWith("6") || text.equals("收获")) return 6;
		if (text.startsWith("7") || text.equals("其他")) return 7;
		throw new IllegalArgumentException("第" + rowNo + "行农事类型不合法：" + value);
	}

	private Integer parseWorkStatus(String value, int rowNo) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		String text = value.trim();
		if (text.startsWith("1") || text.equals("计划中")) return 1;
		if (text.startsWith("2") || text.equals("进行中")) return 2;
		if (text.startsWith("3") || text.equals("已完成")) return 3;
		if (text.startsWith("4") || text.equals("已取消")) return 4;
		throw new IllegalArgumentException("第" + rowNo + "行作业状态不合法：" + value);
	}

	private String farmingTypeToName(Integer value) {
		if (value == null) return null;
		switch (value) {
			case 1: return "播种";
			case 2: return "施肥";
			case 3: return "灌溉";
			case 4: return "除草";
			case 5: return "病虫害防治";
			case 6: return "收获";
			case 7: return "其他";
			default: return String.valueOf(value);
		}
	}

	private String workStatusToName(Integer value) {
		if (value == null) return null;
		switch (value) {
			case 1: return "计划中";
			case 2: return "进行中";
			case 3: return "已完成";
			case 4: return "已取消";
			default: return String.valueOf(value);
		}
	}

}
