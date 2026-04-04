package org.jeecg.modules.youcai.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.youcai.dto.UnifiedDeviceDto;
import org.jeecg.modules.youcai.entity.YoucaiAgriculturalMachine;
import org.jeecg.modules.youcai.entity.YoucaiSensorInfo;
import org.jeecg.modules.youcai.dto.WeatherSensorDataDTO;
import org.jeecg.modules.youcai.entity.iotEntity.ApiResponse;
import org.jeecg.modules.youcai.mapper.YoucaiAgriculturalMachineMapper;
import org.jeecg.modules.youcai.mapper.YoucaiBasesMapper;
import org.jeecg.modules.youcai.service.IYoucaiAgriculturalMachineService;
import org.jeecg.modules.youcai.service.IYoucaiSensorInfoService;
import org.jeecg.modules.youcai.task.SensorDataSyncTask;
import org.jeecg.modules.youcai.util.IoTApiUtil;

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
	
	@Autowired
	private IoTApiUtil ioTApiUtil;
	
	@Autowired
	private SensorDataSyncTask sensorDataSyncTask;

     @Autowired
     private IYoucaiAgriculturalMachineService agriculturalMachineService;


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
    public Result<YoucaiSensorInfo> queryById(@Parameter(name="id", description="主键") @RequestParam(name="id") String id) {
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
        try {
            // 调用定时任务的同步方法
            sensorDataSyncTask.syncSensorData();
            return Result.OK("同步成功!");
        } catch (Exception e) {
            log.error("同步传感器数据失败", e);
            return Result.error("同步失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取基地的气象传感器实时数据
     *
     * @param baseId 基地ID
     * @return
     */
    @AutoLog(value = "传感器信息表-获取气象传感器数据")
    @Operation(summary="传感器信息表-获取气象传感器数据", description="传感器信息表-获取气象传感器数据")
    @GetMapping(value = "/getWeatherSensorData")
    public Result<WeatherSensorDataDTO> getWeatherSensorData(@Parameter(name="baseId", description="基地ID") @RequestParam(name="baseId") String baseId) {
        try {
            log.info("获取基地ID {} 的气象传感器数据", baseId);
            WeatherSensorDataDTO weatherData = youcaiSensorInfoService.getWeatherSensorData(baseId);
            if (weatherData != null) {
                return Result.OK(weatherData);
            } else {
                return Result.error("获取气象传感器数据失败");
            }
        } catch (Exception e) {
            log.error("获取气象传感器数据异常", e);
            return Result.error("获取气象传感器数据异常：" + e.getMessage());
        }
    }
    
    /**
     * 获取基地的传感器列表
     *
     * @param baseId 基地ID
     * @return
     */
    @AutoLog(value = "传感器信息表-获取基地传感器列表")
    @Operation(summary="传感器信息表-获取基地传感器列表", description="传感器信息表-获取基地传感器列表")
    @GetMapping(value = "/getBaseSensorList")
    public Result<List<YoucaiSensorInfo>> getBaseSensorList(@Parameter(name="baseId", description="基地ID") @RequestParam(name="baseId") String baseId) {
        try {
            log.info("获取基地ID {} 的传感器列表", baseId);
            List<YoucaiSensorInfo> sensorList = youcaiSensorInfoService.getBaseSensorList(baseId);
            return Result.OK(sensorList);
        } catch (Exception e) {
            log.error("获取基地传感器列表异常", e);
            return Result.error("获取基地传感器列表异常：" + e.getMessage());
        }
    }
    
    /**
     * 获取传感器历史数据
     *
     * @param sensorId 传感器ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return
     */
    @AutoLog(value = "传感器信息表-获取传感器历史数据")
    @Operation(summary="传感器信息表-获取传感器历史数据", description="传感器信息表-获取传感器历史数据")
    @GetMapping(value = "/getSensorHistoryData")
    public Result<List<WeatherSensorDataDTO>> getSensorHistoryData(
            @Parameter(name="sensorId", description="传感器ID") @RequestParam(name="sensorId") String sensorId,
            @Parameter(name="startDate", description="开始日期") @RequestParam(name="startDate") String startDate,
            @Parameter(name="endDate", description="结束日期") @RequestParam(name="endDate") String endDate) {
        try {
            log.info("获取传感器ID {} 在 {} 至 {} 的历史数据", sensorId, startDate, endDate);
            List<WeatherSensorDataDTO> historyData = youcaiSensorInfoService.getSensorHistoryData(sensorId, startDate, endDate);
            return Result.OK(historyData);
        } catch (Exception e) {
            log.error("获取传感器历史数据异常", e);
            return Result.error("获取传感器历史数据异常：" + e.getMessage());
        }
    }

    @AutoLog(value = "传感器信息表-获取所有设备")
    @GetMapping(value = "/getAllDevices")
    public Result<List<UnifiedDeviceDto>> getAllDevices(@RequestParam(defaultValue = "229") String baseId){
        return youcaiSensorInfoService.getAllDevices(baseId);
    }

    @AutoLog(value = "传感器信息表-获取视频设备列表")
    @Operation(summary="获取视频设备列表", description="根据基地ID获取视频设备列表")
    @GetMapping(value = "/getVideoDevices")
    public Result<List<Map<String, Object>>> getVideoDevices(
            @Parameter(name="baseId", description="基地ID") @RequestParam(name="baseId") String baseId) {
        try {
            log.info("获取基地ID {} 的视频设备列表", baseId);
            return youcaiSensorInfoService.getVideoDevicesByBaseId(baseId);
        } catch (Exception e) {
            log.error("获取视频设备列表异常", e);
            return Result.error("获取视频设备列表异常：" + e.getMessage());
        }
    }

    @AutoLog(value = "传感器信息表-获取视频播放地址")
    @Operation(summary="获取视频播放地址", description="根据设备编码和通道号获取视频播放地址")
    @PostMapping(value = "/getVideoStream")
    public Result<String> getVideoStream(
            @Parameter(name="equipmentCode", description="设备编码") @RequestParam(name="equipmentCode") String equipmentCode,
            @Parameter(name="channelNum", description="通道号") @RequestParam(name="channelNum") String channelNum) {
        try {
            log.info("获取视频播放地址，设备编码: {}, 通道号: {}", equipmentCode, channelNum);
            ApiResponse response = ioTApiUtil.getVideoStrem(equipmentCode, channelNum).block();
            if (response != null && response.getCode() == 1 && response.getData() != null) {
                return Result.OK(response.getData().toString());
            } else {
                log.error("获取视频播放地址失败: {}", response != null ? response.getMsg() : "响应为空");
                return Result.error("获取视频播放地址失败");
            }
        } catch (Exception e) {
            log.error("获取视频播放地址异常", e);
            return Result.error("获取视频播放地址异常：" + e.getMessage());
        }
    }

    /**
     * 获取设备状态信息
     *
     * @param projectId 项目ID，默认为1
     * @return 设备状态信息
     */
    @AutoLog(value = "设备管理-获取设备状态")
    @Operation(summary="设备管理-获取设备状态", description="设备管理-获取设备状态")
    @GetMapping(value = "/deviceStatus")
    public Result<Object> getDeviceStatus(
            @Parameter(name="projectId", description="项目ID") @RequestParam(name="projectId", defaultValue = "229") Integer projectId) {
        try {
            log.info("获取项目ID {} 的设备状态信息", projectId);
            
            // 调用IoTApiUtil获取设备在线状态
            ApiResponse response = ioTApiUtil.getDeviceOnline(projectId).block();
            
            if (response != null && response.getCode() == 1) {
                // 直接返回IoT API的响应数据
                Map<String, Object> result = new HashMap<>();
                result.put("code", response.getCode());
                result.put("msg", response.getMsg());
                result.put("count", response.getCount());
                result.put("data", response.getData());
                
                return Result.OK(result);
            } else {
                log.error("获取设备状态失败: {}", response != null ? response.getMsg() : "响应为空");
                return Result.error("获取设备状态失败");
            }
        } catch (Exception e) {
            log.error("获取设备状态异常", e);
            return Result.error("获取设备状态异常：" + e.getMessage());
        }
    }



     @Operation(summary = "农机设备列表查询")
     @GetMapping(value = "/machine/list")
     public Result<List<YoucaiAgriculturalMachine>> getMachineList() {
         List<YoucaiAgriculturalMachine> list = agriculturalMachineService.getMachineList();
         return Result.OK(list);
     }

     @Operation(summary = "根据农机列表获取作业记录")
    @GetMapping(value = "/records")
    public Result<List<Map<String, Object>>> getOperationRecords(
            @RequestParam(name = "beidouSnList") String beidouSnList,
            @RequestParam(name = "startDate") String startDate,
            @RequestParam(name = "endDate") String endDate) {
        return agriculturalMachineService.getOperationRecordsByBeidouSnList(beidouSnList, startDate, endDate);
    }

     @Operation(summary = "同步作业数据")
     @PostMapping(value = "/sync")
     public Result<Map<String, Object>> syncOperationData(
             @RequestParam(name = "baseId") String baseId,
             @RequestParam(name = "startDate") String startDate,
             @RequestParam(name = "endDate") String endDate) {
         return agriculturalMachineService.syncOperationData(baseId, startDate, endDate);
     }

     @Operation(summary = "获取轨迹数据")
     @GetMapping(value = "/track")
     public Result<List<Map<String, Object>>> getTrackData(
             @RequestParam(name = "beidouSn") String beidouSn,
             @RequestParam(name = "startDate") String startDate,
             @RequestParam(name = "endDate") String endDate) {
         return agriculturalMachineService.getTrackData(beidouSn, startDate, endDate);
     }

}
