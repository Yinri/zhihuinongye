// src/services/pestControlApi.ts
import axios from 'axios';

const DATA_API_BASE_URL = '/api';

// 定义返回类型（根据后端返回字段调整）
export interface PestDetectionData {
  name: string; 
  confidence: number;       // 病虫害名称

}

export const pestControlApi = {
  // 上传病虫害图片并返回识别结果
  uploadPestImage: async (formData: FormData): Promise<PestDetectionData> => {
    try {
      const response = await axios.post(`${DATA_API_BASE_URL}/upload`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });

      // 后端返回扁平结构 { success, name, confidence }
      if (response.data.success) {
        return {
          name: response.data.name,
          confidence: response.data.confidence
        };
      } else {
        throw new Error(response.data.message || '识别失败');
      }
    } catch (error) {
      console.error('API 上传错误:', error);
      throw error;
    }
  }
};





// interface PestDetectionData {
//   date: string;
//   severity: '轻微' | '轻度' | '中度' | '严重';
//   description: string;
//   solution: string;
//   area_affected: number; // 受影响面积(亩)
// }

// interface PestTrendParams {
//   period: 'month' | 'quarter' | 'year';
//   pest_type?: string; // 可选，指定病虫害类型
// }

// interface PestTrendData {
//   labels: string[];
//   aphidData: number[]; // 蚜虫数据
//   caterpillarData: number[]; // 菜青虫数据
//   sclerotiniaData: number[]; // 菌核病数据
// }

// /**
//  * 病虫害防控相关API服务
//  */
// export const pestControlApi = {
//   /**
//    * 获取病虫害趋势数据
//    * @param params 查询参数(时间周期、病虫害类型)
//    * @returns 病虫害趋势图表数据
//    */
//   getPestTrendData: async (
//     params: PestTrendParams
//   ): Promise<PestTrendData> => {
//     const response = await axios.get('/api/pest-trend', { params });
//     return response.data;
//   },

//   /**
//    * 获取病虫害检测记录
//    * @param limit 记录数量限制
//    * @returns 检测记录列表
//    */
//   getDetectionRecords: async (limit: number = 10): Promise<PestDetectionData[]> => {
//     const response = await axios.get('/api/pest-detection-records', { params: { limit } });
//     return response.data;
//   },

//   /**
//    * 提交病虫害检测结果
//    * @param detectionData 检测数据
//    * @returns 提交结果
//    */
//   submitDetectionResult: async (
//     detectionData: Omit<PestDetectionData, 'date'>
//   ) => {
//     const response = await axios.post('/api/pest-detection', detectionData);
//     return response.data;
//   },

//   /**
//    * 获取病虫害预警信息
//    * @returns 预警信息
//    */
//   getPestWarning: async () => {
//     const response = await axios.get('/api/pest-warning');
//     return response.data;
//   }
// };