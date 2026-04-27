package org.jeecg.modules.youcai.controller;

import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.youcai.service.IIrrigationService;
import org.jeecg.modules.youcai.service.IYoucaiFarmingRecordsService;
import org.jeecg.modules.youcai.entity.YoucaiFarmingRecords;
import org.jeecg.modules.youcai.mapper.YoucaiPlotsMapper;
import org.jeecg.modules.youcai.entity.YoucaiPlots;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.math.BigDecimal;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
//zrt 灌溉监控控制器
@Tag(name="智慧灌溉(Penman)")
@RestController
@RequestMapping("/youcai/irrigation")
@Slf4j
public class YoucaiRapeseedIrrigationController {
    @Autowired
    private IIrrigationService irrigationService;
    @Autowired
    private IYoucaiFarmingRecordsService farmingService;
    @Autowired
    private YoucaiPlotsMapper plotsMapper;

    @Operation(summary="地块状态")
    @GetMapping("/plotStatus/{plotId}")
    public Result<Map<String, Object>> plotStatus(@PathVariable String plotId) {
        return Result.OK(irrigationService.getPlotStatus(plotId));
    }

    @Operation(summary="基地状态")
    @GetMapping("/plotStatusByBase/{baseId}")
    public Result<Map<String, Object>> plotStatusByBase(@PathVariable String baseId) {
        return Result.OK(irrigationService.getPlotStatusByBase(baseId));
    }

    @Operation(summary="Penman建议与数据")
    @GetMapping("/penmanPredict")
    public Result<Map<String, Object>> penmanPredict(@RequestParam(required=false) String plotId,
                                                     @RequestParam(required=false) String baseId) {
        if (plotId != null && !plotId.isEmpty()) {
            return Result.OK(irrigationService.getPenmanPredict(plotId));
        } else if (baseId != null && !baseId.isEmpty()) {
            return Result.OK(irrigationService.getPenmanPredictByBase(baseId));
        } else {
            return Result.error("参数不能为空");
        }
    }

    @Operation(summary="干预对比")
    @GetMapping("/interventionComparison")
    public Result<Map<String, Object>> interventionComparison(@RequestParam(required=false) String plotId,
                                                              @RequestParam(required=false) String baseId) {
        if (plotId != null && !plotId.isEmpty()) {
            return Result.OK(irrigationService.getInterventionComparison(plotId));
        } else if (baseId != null && !baseId.isEmpty()) {
            return Result.OK(irrigationService.getInterventionComparisonByBase(baseId));
        } else {
            return Result.error("参数不能为空");
        }
    }

    

    @Operation(summary="灌溉记录列表")
    @GetMapping("/list")
    public Result<IPage<YoucaiFarmingRecords>> list(@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                                    @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                                    @RequestParam(name="plotId", required = false) String plotId,
                                                    @RequestParam(name="baseId", required = false) String baseId,
                                                    HttpServletRequest req) {
        QueryWrapper<YoucaiFarmingRecords> q = new QueryWrapper<>();
        q.eq("farming_type", 3);
        if (plotId != null && !plotId.isEmpty()) q.eq("plot_id", plotId);
        if (baseId != null && !baseId.isEmpty()) q.eq("base_id", baseId);
        q.orderByDesc("farming_date");
        Page<YoucaiFarmingRecords> page = new Page<>(pageNo, pageSize);
        IPage<YoucaiFarmingRecords> pageList = farmingService.page(page, q);
        return Result.OK(pageList);
    }

    @Operation(summary="新增灌溉记录")
    @PostMapping("/save")
    public Result<String> save(@RequestBody Map<String, Object> body) {
        YoucaiFarmingRecords rec = new YoucaiFarmingRecords();
        rec.setRecordCode(str(body.get("irrigationNo")));
        rec.setBaseId(str(body.get("baseId")));
        rec.setPlotId(str(body.get("plotId")));
        rec.setFarmingType(3);
        rec.setFarmingDate(parseDate(body.get("irrigationDate")));
        rec.setWorker(str(body.get("responsiblePerson")));
        rec.setWorkArea(bd(body.get("irrigationArea")));
        rec.setMaterials(str(body.get("irrigationMethod")));
        rec.setMaterialAmount(str(body.get("waterUsage")));
        rec.setWorkDuration(bd(body.get("duration")));
        rec.setRemark(str(body.get("remark")));
        farmingService.save(rec);
        return Result.OK("保存成功");
    }

    @Operation(summary="编辑灌溉记录")
    @PutMapping("/edit")
    public Result<String> edit(@RequestBody Map<String, Object> body) {
        YoucaiFarmingRecords rec = new YoucaiFarmingRecords();
        rec.setId(str(body.get("id")));
        rec.setRecordCode(str(body.get("irrigationNo")));
        rec.setBaseId(str(body.get("baseId")));
        rec.setPlotId(str(body.get("plotId")));
        rec.setFarmingType(3);
        rec.setFarmingDate(parseDate(body.get("irrigationDate")));
        rec.setWorker(str(body.get("responsiblePerson")));
        rec.setWorkArea(bd(body.get("irrigationArea")));
        rec.setMaterials(str(body.get("irrigationMethod")));
        rec.setMaterialAmount(str(body.get("waterUsage")));
        rec.setWorkDuration(bd(body.get("duration")));
        rec.setRemark(str(body.get("remark")));
        farmingService.updateById(rec);
        return Result.OK("编辑成功");
    }

    @Operation(summary="删除灌溉记录")
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable String id) {
        farmingService.removeById(id);
        return Result.OK("删除成功");
    }

    @Operation(summary="批量删除灌溉记录")
    @DeleteMapping("/batchDelete")
    public Result<String> batchDelete(@RequestBody List<String> ids) {
        farmingService.removeByIds(ids);
        return Result.OK("批量删除成功");
    }

    @Operation(summary="灌溉记录详情")
    @GetMapping("/queryById/{id}")
    public Result<YoucaiFarmingRecords> queryById(@PathVariable String id) {
        YoucaiFarmingRecords rec = farmingService.getById(id);
        return rec == null ? Result.error("未找到对应数据") : Result.OK(rec);
    }

    private String str(Object v) { return v == null ? null : String.valueOf(v); }
    private BigDecimal bd(Object v) {
        if (v == null) return null;
        if (v instanceof BigDecimal) return (BigDecimal)v;
        if (v instanceof Number) return new BigDecimal(((Number)v).toString());
        try { return new BigDecimal(String.valueOf(v)); } catch (Exception e) { return null; }
    }
    private Date parseDate(Object v) {
        if (v == null) return null;
        if (v instanceof Date) return (Date)v;
        String s = String.valueOf(v);
        String[] fmts = new String[]{"yyyy-MM-dd HH:mm:ss","yyyy-MM-dd HH:mm","yyyy/MM/dd HH:mm:ss","yyyy/MM/dd HH:mm"};
        for (String f : fmts) {
            try { return new SimpleDateFormat(f).parse(s); } catch (Exception ignored) {}
        }
        try { return new Date(Long.parseLong(s)); } catch (Exception ignored) {}
        return null;
    }
}
