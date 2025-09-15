import axios from 'axios';

// 定义请求和响应数据类型
interface ProductionPlanItem {
  id: string;
  year: number;
  season: 'spring' | 'autumn';
  area: number; // 计划面积(ha)
  yield_target: number; // 目标产量(kg/ha)
  status: 'draft' | 'approved' | 'executing' | 'completed';
  created_at: string;
  updated_at: string;
}

interface CreateProductionPlanRequest {
  year: number;
  season: 'spring' | 'autumn';
  area: number;
  yield_target: number;
  crop_variety: string;
  planting_date: string;
  fertilization_plan: {
    nitrogen: number;
    phosphorus: number;
    potassium: number;
  };
}

/**
 * 生产计划相关API服务
 */
export const productionPlanApi = {
  /**
   * 获取生产计划列表
   * @returns 生产计划列表
   */
  getProductionPlans: async (): Promise<ProductionPlanItem[]> => {
    const response = await axios.get('/api/production-plans');
    return response.data;
  },

  /**
   * 获取生产计划详情
   * @param planId 计划ID
   * @returns 生产计划详情
   */
  getProductionPlanDetail: async (planId: string): Promise<ProductionPlanItem> => {
    const response = await axios.get(`/api/production-plans/${planId}`);
    return response.data;
  },

  /**
   * 创建生产计划
   * @param planData 生产计划数据
   * @returns 创建结果
   */
  createProductionPlan: async (planData: CreateProductionPlanRequest) => {
    const response = await axios.post('/api/production-plans', planData);
    return response.data;
  },

  /**
   * 更新生产计划
   * @param planId 计划ID
   * @param planData 更新的计划数据
   * @returns 更新结果
   */
  updateProductionPlan: async (
    planId: string,
    planData: Partial<CreateProductionPlanRequest>
  ) => {
    const response = await axios.patch(`/api/production-plans/${planId}`, planData);
    return response.data;
  }
};