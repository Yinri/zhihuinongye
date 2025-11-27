package org.jeecg.modules.youcai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.youcai.dto.harvest.HarvestStatsDTO;
import org.jeecg.modules.youcai.dto.harvest.HarvesterStatusDTO;
import org.jeecg.modules.youcai.dto.harvest.YieldChartBarDTO;
import org.jeecg.modules.youcai.dto.harvest.PlotHarvestSummaryDTO;
import org.jeecg.modules.youcai.entity.YoucaiHarvest;
import org.jeecg.modules.youcai.entity.YoucaiPlots;
import org.jeecg.modules.youcai.mapper.YoucaiHarvestMapper;
import org.jeecg.modules.youcai.mapper.YoucaiPlotsMapper;
import org.jeecg.modules.youcai.mapper.PlotHarvestSummaryMapper;
import org.jeecg.modules.youcai.entity.YoucaiHarvesterMachine;
import org.jeecg.modules.youcai.mapper.YoucaiHarvesterMachineMapper;
import org.jeecg.modules.youcai.service.IYoucaiHarvestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 收获管理 Service 实现
 */
@Service
@Slf4j
public class YoucaiHarvestServiceImpl extends ServiceImpl<YoucaiHarvestMapper, YoucaiHarvest> implements IYoucaiHarvestService {

    @Autowired
    private YoucaiPlotsMapper plotsMapper;
    @Autowired
    private PlotHarvestSummaryMapper summaryMapper;
    @Autowired
    private YoucaiHarvesterMachineMapper machineMapper;

    @Override
    public HarvestStatsDTO getHarvestStats(String baseId) {
        HarvestStatsDTO dto = new HarvestStatsDTO();

        // 总地块面积
        QueryWrapper<YoucaiPlots> plotWrapper = new QueryWrapper<>();
        plotWrapper.eq(baseId != null, "base_id", baseId)
                .eq("del_flag", 0);
        List<YoucaiPlots> plots = plotsMapper.selectList(plotWrapper);
        double totalPlotArea = plots.stream()
                .map(p -> p.getArea() == null ? 0.0 : p.getArea().doubleValue())
                .mapToDouble(Double::doubleValue).sum();

        // 已收割面积（统计所有收获记录的面积）
        QueryWrapper<YoucaiHarvest> harvestWrapper = new QueryWrapper<>();
        harvestWrapper.eq(baseId != null, "base_id", baseId)
                .eq("del_flag", 0);
        List<YoucaiHarvest> harvests = this.list(harvestWrapper);
        double harvestedArea = harvests.stream()
                .map(h -> h.getArea() == null ? 0.0 : h.getArea().doubleValue())
                .mapToDouble(Double::doubleValue).sum();

        double unharvestedArea = Math.max(0.0, totalPlotArea - harvestedArea);

        // 累计产量（kg -> 吨），注意 totalYield 字段单位为 kg
        double totalYieldTon = harvests.stream()
                .map(h -> h.getTotalYield() == null ? 0.0 : h.getTotalYield().doubleValue())
                .mapToDouble(Double::doubleValue).sum() / 1000.0;

        // 作业农机数（去重机器名）
        long machineCount = harvests.stream()
                .map(YoucaiHarvest::getMachineName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()).size();

        // 今日与昨日对比（面积与产量）
        double todayArea = sumByDateArea(baseId, LocalDate.now());
        double yesterdayArea = sumByDateArea(baseId, LocalDate.now().minusDays(1));
        double todayYieldTon = sumByDateYieldTon(baseId, LocalDate.now());
        double yesterdayYieldTon = sumByDateYieldTon(baseId, LocalDate.now().minusDays(1));

        dto.setHarvestedArea(round(harvestedArea));
        dto.setUnharvestedArea(round(unharvestedArea));
        dto.setTotalYield(round(totalYieldTon));
        dto.setMachineCount((int) machineCount);
        dto.setHarvestedAreaTrend(round(percentChange(todayArea, yesterdayArea)));
        // 未收割面积较昨日趋势（采用总面积不变下，未收割面积的变化）
        double todayUnharvested = Math.max(0.0, totalPlotArea - sumUntilDateArea(baseId, LocalDate.now()));
        double yesterdayUnharvested = Math.max(0.0, totalPlotArea - sumUntilDateArea(baseId, LocalDate.now().minusDays(1)));
        dto.setUnharvestedAreaTrend(round(percentChange(todayUnharvested, yesterdayUnharvested)));
        dto.setTotalYieldTrend(round(percentChange(todayYieldTon, yesterdayYieldTon)));

        return dto;
    }

    @Override
    public List<HarvesterStatusDTO> getHarvesterStatus(String baseId) {
        // 基于农机档案表的机器列表为基线，并结合最近收获记录推断状态
        QueryWrapper<YoucaiHarvesterMachine> machineQ = new QueryWrapper<>();
        machineQ.eq(baseId != null, "base_id", baseId)
                .eq("del_flag", 0);
        List<YoucaiHarvesterMachine> machines = machineMapper.selectList(machineQ);

        // 收获记录用于推断今日/近3日状态
        QueryWrapper<YoucaiHarvest> harvestQ = new QueryWrapper<>();
        harvestQ.eq(baseId != null, "base_id", baseId)
                .eq("del_flag", 0);
        List<YoucaiHarvest> harvests = this.list(harvestQ);
        Map<String, List<YoucaiHarvest>> byMachine = harvests.stream()
                .filter(h -> h.getMachineName() != null)
                .collect(Collectors.groupingBy(YoucaiHarvest::getMachineName));

        List<HarvesterStatusDTO> result = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // 覆盖档案中的机器；如果没有档案记录但 harvests 有机器名，也纳入返回
        Set<String> seen = new HashSet<>();
        for (YoucaiHarvesterMachine m : machines) {
            String name = m.getMachineName();
            List<YoucaiHarvest> records = byMachine.getOrDefault(name, Collections.emptyList());
            records.sort(Comparator.comparing(YoucaiHarvest::getHarvestDate, Comparator.nullsLast(Date::compareTo)).reversed());
            YoucaiHarvest latest = records.isEmpty() ? null : records.get(0);

            String status = m.getStatus() == null ? "维修中" : m.getStatus();
            if (latest != null && latest.getHarvestDate() != null) {
                LocalDate d = latest.getHarvestDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                if (d.isEqual(today)) {
                    status = "working";
                } else if (!d.isBefore(today.minusDays(3))) {
                    status = "idle";
                }
            }

            HarvesterStatusDTO dto = new HarvesterStatusDTO();
            dto.setName(name);
            dto.setStatus(status);
            // 通过 plotId 查询地块名称
            String location = m.getLastLocation() == null ? "待定位置" : m.getLastLocation();
            if (latest != null && latest.getPlotId() != null) {
                YoucaiPlots plot = plotsMapper.selectById(latest.getPlotId());
                if (plot != null && plot.getPlotName() != null) {
                    location = plot.getPlotName();
                }
            }
            dto.setLocation(location);
            result.add(dto);
            seen.add(name);
        }

        // 补充没有档案但存在记录的机器
        for (Map.Entry<String, List<YoucaiHarvest>> entry : byMachine.entrySet()) {
            if (seen.contains(entry.getKey())) continue;
            List<YoucaiHarvest> records = entry.getValue();
            records.sort(Comparator.comparing(YoucaiHarvest::getHarvestDate, Comparator.nullsLast(Date::compareTo)).reversed());
            YoucaiHarvest latest = records.isEmpty() ? null : records.get(0);
            String status = "维修中";
            if (latest != null && latest.getHarvestDate() != null) {
                LocalDate d = latest.getHarvestDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                if (d.isEqual(today)) {
                    status = "作业中";
                } else if (!d.isBefore(today.minusDays(3))) {
                    status = "空闲";
                }
            }
            HarvesterStatusDTO dto = new HarvesterStatusDTO();
            dto.setName(entry.getKey());
            dto.setStatus(status);
            // 通过 plotId 查询地块名称
            String location = "待定位置";
            if (latest != null && latest.getPlotId() != null) {
                YoucaiPlots plot = plotsMapper.selectById(latest.getPlotId());
                if (plot != null && plot.getPlotName() != null) {
                    location = plot.getPlotName();
                }
            }
            dto.setLocation(location);
            result.add(dto);
        }

        return result;
    }

    @Override
    public List<YieldChartBarDTO> getYieldChart(String baseId) {
        // 统计今日每个地块的实际与预计产量（kg -> 吨）
        QueryWrapper<YoucaiHarvest> wrapper = new QueryWrapper<>();
        wrapper.eq(baseId != null, "base_id", baseId)
                .eq("del_flag", 0)
                .apply("date(harvest_date) = curdate()");
        List<YoucaiHarvest> todays = this.list(wrapper);

        // 按 plotId 分组，然后查询地块名称
        Map<String, List<YoucaiHarvest>> byPlot = todays.stream()
                .filter(h -> h.getPlotId() != null)
                .collect(Collectors.groupingBy(YoucaiHarvest::getPlotId));

        List<YieldChartBarDTO> result = new ArrayList<>();
        byPlot.forEach((plotId, list) -> {
            // 查询地块名称
            YoucaiPlots plot = plotsMapper.selectById(plotId);
            String plotName = (plot != null && plot.getPlotName() != null) ? plot.getPlotName() : plotId;
            
            double actualKg = list.stream().map(h -> h.getTotalYield() == null ? 0.0 : h.getTotalYield().doubleValue()).mapToDouble(Double::doubleValue).sum();
            double expectedKg = list.stream().map(h -> h.getPredictYield() == null ? 0.0 : h.getPredictYield().doubleValue()).mapToDouble(Double::doubleValue).sum();
            YieldChartBarDTO dto = new YieldChartBarDTO();
            dto.setFieldName(plotName);
            dto.setActual(round(actualKg / 1000.0));
            dto.setExpected(round(expectedKg / 1000.0));
            result.add(dto);
        });

        // 如今日无数据，返回空列表
        return result;
    }

    @Override
    public List<PlotHarvestSummaryDTO> getPlotHarvestSummary(String baseId) {
        if (baseId == null) {
            return summaryMapper.selectAll();
        }
        return summaryMapper.selectByBaseId(baseId);
    }

    private double sumByDateArea(String baseId, LocalDate date) {
        QueryWrapper<YoucaiHarvest> wrapper = new QueryWrapper<>();
        wrapper.eq(baseId != null, "base_id", baseId)
                .eq("del_flag", 0)
                .apply("date(harvest_date) = {0}", date);
        return this.list(wrapper).stream()
                .map(h -> h.getArea() == null ? 0.0 : h.getArea().doubleValue())
                .mapToDouble(Double::doubleValue).sum();
    }

    private double sumByDateYieldTon(String baseId, LocalDate date) {
        QueryWrapper<YoucaiHarvest> wrapper = new QueryWrapper<>();
        wrapper.eq(baseId != null, "base_id", baseId)
                .eq("del_flag", 0)
                .apply("date(harvest_date) = {0}", date);
        return this.list(wrapper).stream()
                .map(h -> h.getTotalYield() == null ? 0.0 : h.getTotalYield().doubleValue())
                .mapToDouble(Double::doubleValue).sum() / 1000.0;
    }

    private double sumUntilDateArea(String baseId, LocalDate dateInclusive) {
        QueryWrapper<YoucaiHarvest> wrapper = new QueryWrapper<>();
        wrapper.eq(baseId != null, "base_id", baseId)
                .eq("del_flag", 0)
                .apply("date(harvest_date) <= {0}", dateInclusive);
        return this.list(wrapper).stream()
                .map(h -> h.getArea() == null ? 0.0 : h.getArea().doubleValue())
                .mapToDouble(Double::doubleValue).sum();
    }

    private double percentChange(double today, double yesterday) {
        if (yesterday == 0) {
            return today == 0 ? 0.0 : 100.0;
        }
        return ((today - yesterday) / Math.abs(yesterday)) * 100.0;
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
