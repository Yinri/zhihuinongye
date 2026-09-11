package org.jeecg.modules.youcai.service;

import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.youcai.entity.YoucaiAgriculturalMachine;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface IYoucaiAgriculturalMachineService extends IService<YoucaiAgriculturalMachine> {

    Result<List<Map<String, Object>>> getOperationRecordsByBaseId(String baseId, String startDate, String endDate);

    Result<List<Map<String, Object>>> getOperationRecordsByBeidouSnList(String beidouSnList, String startDate, String endDate);

    Result<Map<String, Object>> syncOperationData(String baseId, String startDate, String endDate);

    Result<List<Map<String, Object>>> getTrackData(String beidouSn, String startDate, String endDate);

    List<YoucaiAgriculturalMachine> getMachineList();

}
