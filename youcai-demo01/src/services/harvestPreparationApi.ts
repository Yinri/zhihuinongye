import axios from 'axios';

// 定义请求和响应数据类型
interface HarvestPlan {
  id: string;
  crop_type: string;
  planned_start_date: string;
  planned_end_date: string;
  area: number; // 收获面积(亩)
  expected_yield: number; // 预计产量(kg/亩)
  status: 'draft' | 'in_progress' | 'completed';
}

interface PreparationTask {
  id: string;
  harvest_plan_id: string;
  task_name: string;
  task_type: 'equipment' | 'labor' | 'field' | 'other';
  status: 'pending' | 'in_progress' | 'completed';
  deadline: string;
  responsible_person: string;
}

interface YieldForecast {
  date: string;
  forecast_value: number; // 预测产量(kg/亩)
  confidence: number; // 置信度(%)
  factors: { name: string; value: number; impact: 'positive' | 'negative' | 'neutral' }[];
}

/**
 * 收获准备相关API服务
 */
export const harvestPreparationApi = {
  /**
   * 获取收获计划列表
   * @returns 收获计划列表
   */
  getHarvestPlans: async (): Promise<HarvestPlan[]> => {
    const response = await axios.get('/api/harvest-plans');
    return response.data;
  },

  /**
   * 获取收获计划详情
   * @param planId 计划ID
   * @returns 收获计划详情
   */
  getHarvestPlanDetail: async (planId: string): Promise<HarvestPlan> => {
    const response = await axios.get(`/api/harvest-plans/${planId}`);
    return response.data;
  },

  /**
   * 创建收获计划
   * @param planData 收获计划数据
   * @returns 创建结果
   */
  createHarvestPlan: async (planData: Omit<HarvestPlan, 'id' | 'status'>) => {
    const response = await axios.post('/api/harvest-plans', planData);
    return response.data;
  },

  /**
   * 获取收获准备任务列表
   * @param planId 收获计划ID
   * @returns 任务列表
   */
  getPreparationTasks: async (planId: string): Promise<PreparationTask[]> => {
    const response = await axios.get(`/api/harvest-plans/${planId}/tasks`);
    return response.data;
  },

  /**
   * 更新任务状态
   * @param taskId 任务ID
   * @param status 新状态
   * @returns 更新结果
   */
  updateTaskStatus: async (
    taskId: string,
    status: 'pending' | 'in_progress' | 'completed'
  ) => {
    const response = await axios.patch(`/api/preparation-tasks/${taskId}/status`, {
      status
    });
    return response.data;
  },

  /**
   * 获取产量预测数据
   * @param planId 收获计划ID
   * @returns 产量预测数据
   */
  getYieldForecast: async (planId: string): Promise<YieldForecast[]> => {
    const response = await axios.get(`/api/harvest-plans/${planId}/yield-forecast`);
    return response.data;
  },

  /**
   * 导出收获报告
   * @param planId 收获计划ID
   * @returns 报告下载链接
   */
  exportHarvestReport: async (planId: string): Promise<string> => {
    const response = await axios.post(`/api/harvest-plans/${planId}/export`, {}, {
      responseType: 'blob'
    });
    // 创建下载链接
    const url = window.URL.createObjectURL(new Blob([response.data]));
    return url;
  }
};