import axios from 'axios';

// 定义请求和响应数据类型
interface SoilTestData {
  nitrogen: number;  // 土壤氮含量(kg/ha)
  phosphorus: number;  // 土壤磷含量(kg/ha)
  potassium: number;  // 土壤钾含量(kg/ha)
  organic_matter: number;  // 有机质含量(%)
  ph: number;  // pH值
}

interface CropGrowthStage {
  stage: 'seedling' | 'bud' | 'flowering' | 'maturity';  // 作物生长阶段
  days_after_planting: number;  // 播种后天数
}

interface FertilizerRecommendation {
  nitrogen_recommendation: number;  // 氮肥推荐量(kg/ha)
  phosphorus_recommendation: number;  // 磷肥推荐量(kg/ha)
  potassium_recommendation: number;  // 钾肥推荐量(kg/ha)
  fertilizer_type: string;  // 推荐肥料类型
  application_method: string;  // 推荐施用方法
  application_timing: string;  // 推荐施用时间
}

/**
 * 施肥推荐相关API服务
 */
export const fertilizerApi = {
  /**
   * 获取施肥推荐
   * @param soilData 土壤检测数据
   * @param cropStage 作物生长阶段
   * @param targetYield 目标产量(kg/ha)
   * @returns 施肥推荐结果
   */
  getFertilizerRecommendation: async (
    soilData: SoilTestData,
    cropStage: CropGrowthStage,
    targetYield: number
  ): Promise<FertilizerRecommendation> => {
    const response = await axios.post('/api/fertilizer/recommendation', {
      soil_data: soilData,
      crop_stage: cropStage,
      target_yield: targetYield
    });
    return response.data;
  },

  /**
   * 获取土壤检测历史数据
   * @param fieldId 田块ID
   * @returns 土壤检测历史记录
   */
  getSoilTestHistory: async (fieldId: string) => {
    const response = await axios.get(`/api/soil-test-history/${fieldId}`);
    return response.data;
  },

  /**
   * 提交土壤检测数据
   * @param soilData 土壤检测数据
   * @param fieldId 田块ID
   * @returns 提交结果
   */
  submitSoilTestData: async (
    soilData: SoilTestData,
    fieldId: string
  ) => {
    const response = await axios.post('/api/soil-test-data', {
      soil_data: soilData,
      field_id: fieldId
    });
    return response.data;
  }
};