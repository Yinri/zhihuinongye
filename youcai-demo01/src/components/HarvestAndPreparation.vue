<template>
  <div class="harvest-preparation-container">
    <div class="content-header">
      <h2>收获与整地管理</h2>
      <div class="content-actions">
        <button class="btn-plan" @click="showHarvestPlanForm">📋 制定收获计划</button>
        <button class="btn-update" @click="updateHarvestProgress">🔄 更新进度</button>
        <button class="btn-analysis" @click="showAnalysis">📊 收获分析</button>
      </div>
    </div>

    <!-- 收获进度概览 -->
    <div class="progress-overview">
      <div class="progress-card">
        <h3>总体收获进度</h3>
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: overallHarvestProgress + '%' }"></div>
        </div>
        <div class="progress-text">{{ overallHarvestProgress }}% 已收获</div>
      </div>
      
      <div class="stats-cards">
        <div class="stat-card">
          <div class="stat-icon">🌾</div>
          <div class="stat-content">
            <div class="stat-value">{{ totalHarvested }}吨</div>
            <div class="stat-label">已收获产量</div>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon">🗺️</div>
          <div class="stat-content">
            <div class="stat-value">{{ harvestedArea }}亩</div>
            <div class="stat-label">已收获面积</div>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon">⏱️</div>
          <div class="stat-content">
            <div class="stat-value">{{ remainingDays }}天</div>
            <div class="stat-label">预计剩余时间</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 收获计划与进度详情 -->
    <div class="harvest-details">
      <div class="chart-card">
        <div class="chart-card-header">
          <h3>收获进度趋势</h3>
          <span class="chart-period">近7天数据</span>
        </div>
        <div class="chart-container">
          <canvas id="harvestProgressChart"></canvas>
        </div>
      </div>

      <div class="field-status-card">
        <h3>地块收获状态</h3>
        <div class="field-grid">
          <div v-for="field in fields" :key="field.id" class="field-item" :class="fieldStatusClass(field.status)">
            <div class="field-header">
              <span class="field-name">{{ field.name }}</span>
              <span class="field-status">{{ field.statusText }}</span>
            </div>
            <div class="field-info">
              <div class="field-metric">
                <span class="metric-label">面积:</span>
                <span class="metric-value">{{ field.area }}亩</span>
              </div>
              <div class="field-metric">
                <span class="metric-label">进度:</span>
                <span class="metric-value">{{ field.progress }}%</span>
              </div>
              <div class="field-metric">
                <span class="metric-label">预计产量:</span>
                <span class="metric-value">{{ field.expectedYield }}吨</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 整地准备与建议 -->
    <div class="preparation-section">
      <h3>整地准备与建议</h3>
      <div class="preparation-tabs">
        <button class="preparation-tab" :class="{ active: activePreparationTab === 'tasks' }" @click="activePreparationTab = 'tasks'">整地任务</button>
        <button class="preparation-tab" :class="{ active: activePreparationTab === 'equipment' }" @click="activePreparationTab = 'equipment'">设备准备</button>
        <button class="preparation-tab" :class="{ active: activePreparationTab === 'recommendations' }" @click="activePreparationTab = 'recommendations'">整地建议</button>
      </div>
      
      <div class="preparation-content">
        <div v-if="activePreparationTab === 'tasks'">
          <div class="tasks-list">
            <div v-for="task in preparationTasks" :key="task.id" class="task-item" :class="taskStatusClass(task.status)">
              <div class="task-header">
                <span class="task-name">{{ task.name }}</span>
                <span class="task-status">{{ task.statusText }}</span>
              </div>
              <div class="task-details">
                <span class="task-deadline">截止日期: {{ task.deadline }}</span>
                <span class="task-responsible">负责人: {{ task.responsible }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <div v-if="activePreparationTab === 'equipment'">
          <div class="equipment-list">
            <div v-for="equipment in equipmentList" :key="equipment.id" class="equipment-item">
              <div class="equipment-header">
                <span class="equipment-name">{{ equipment.name }}</span>
                <span class="equipment-status" :class="equipmentStatusClass(equipment.status)">{{ equipment.status }}</span>
              </div>
              <div class="equipment-details">
                <span class="equipment-quantity">数量: {{ equipment.quantity }}</span>
                <span class="equipment-condition">状态: {{ equipment.condition }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <div v-if="activePreparationTab === 'recommendations'">
          <div class="recommendations-list">
            <div class="recommendation-item">
              <div class="recommendation-header">
                <span class="recommendation-title">土壤翻耕深度建议</span>
              </div>
              <div class="recommendation-content">
                根据土壤检测结果，建议翻耕深度为20-25厘米，以改善土壤通气性和保水性。
              </div>
            </div>
            
            <div class="recommendation-item">
              <div class="recommendation-header">
                <span class="recommendation-title">施肥方案</span>
              </div>
              <div class="recommendation-content">
                建议每亩施加有机肥2000-2500公斤，配合氮磷钾复合肥50公斤，提高土壤肥力。
              </div>
            </div>
            
            <div class="recommendation-item">
              <div class="recommendation-header">
                <span class="recommendation-title">轮作建议</span>
              </div>
              <div class="recommendation-content">
                建议与豆科作物轮作，有助于改善土壤结构，减少病虫害发生，提高下季作物产量。
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 制定收获计划弹窗 -->
    <div class="modal-overlay" v-if="showPlanModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>{{ isEditingPlan ? '编辑收获计划' : '制定收获计划' }}</h3>
          <button class="modal-close" @click="closePlanModal">✕</button>
        </div>
        <div class="modal-body">
          <form class="plan-form">
            <div class="form-group">
              <label>计划名称 *</label>
              <input type="text" v-model="currentPlan.name" required placeholder="例如：A1区油菜收获计划">
            </div>
            
            <div class="form-row">
              <div class="form-group">
                <label>计划开始日期 *</label>
                <input type="date" v-model="currentPlan.startDate" required>
              </div>
              
              <div class="form-group">
                <label>计划结束日期 *</label>
                <input type="date" v-model="currentPlan.endDate" required>
              </div>
            </div>
            
            <div class="form-group">
              <label>涉及地块 *</label>
              <select v-model="currentPlan.fieldIds" multiple required>
                <option v-for="field in fields" :key="field.id" :value="field.id">{{ field.name }}</option>
              </select>
              <small>按住Ctrl键可多选</small>
            </div>
            
            <div class="form-group">
              <label>预计总产量 (吨)</label>
              <input type="number" v-model.number="currentPlan.expectedTotalYield" min="0" step="0.1">
            </div>
            
            <div class="form-group">
              <label>负责人 *</label>
              <select v-model="currentPlan.responsiblePerson" required>
                <option value="">选择负责人</option>
                <option value="张三">张三</option>
                <option value="李四">李四</option>
                <option value="王五">王五</option>
                <option value="赵六">赵六</option>
              </select>
            </div>
            
            <div class="form-group">
              <label>收获方式</label>
              <select v-model="currentPlan.harvestMethod">
                <option value="机械收获">机械收获</option>
                <option value="人工收获">人工收获</option>
                <option value="混合方式">混合方式</option>
              </select>
            </div>
            
            <div class="form-group">
              <label>备注</label>
              <textarea v-model="currentPlan.notes" rows="3" placeholder="填写收获计划的其他信息"></textarea>
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="closePlanModal">取消</button>
          <button class="btn-save" @click="saveHarvestPlan">保存计划</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import Chart from 'chart.js/auto';

// 类型定义
interface Field {
  id: string;
  name: string;
  area: number;
  progress: number;
  status: 'notStarted' | 'inProgress' | 'completed';
  statusText: string;
  expectedYield: number;
  actualYield?: number;
}

interface HarvestPlan {
  id: string;
  name: string;
  startDate: string;
  endDate: string;
  fieldIds: string[];
  expectedTotalYield: number;
  actualTotalYield?: number;
  responsiblePerson: string;
  harvestMethod: string;
  status: 'planned' | 'inProgress' | 'completed';
  notes?: string;
}

interface PreparationTask {
  id: string;
  name: string;
  deadline: string;
  responsible: string;
  status: 'pending' | 'inProgress' | 'completed';
  statusText: string;
}

interface Equipment {
  id: string;
  name: string;
  quantity: number;
  condition: string;
  status: 'available' | 'inUse' | 'maintenance';
}

// 状态定义
const overallHarvestProgress = ref<number>(65);
const totalHarvested = ref<number>(125.8);
const harvestedArea = ref<number>(320);
const remainingDays = ref<number>(8);
const activePreparationTab = ref<string>('tasks');
const showPlanModal = ref<boolean>(false);
const isEditingPlan = ref<boolean>(false);
const harvestProgressChart = ref<Chart | null>(null);

// 地块数据
const fields = ref<Field[]>([
  {
    id: 'field-1',
    name: 'A1区',
    area: 120,
    progress: 95,
    status: 'completed',
    statusText: '已完成',
    expectedYield: 48.5,
    actualYield: 47.2
  },
  {
    id: 'field-2',
    name: 'A2区',
    area: 100,
    progress: 80,
    status: 'inProgress',
    statusText: '收获中',
    expectedYield: 40.2
  },
  {
    id: 'field-3',
    name: 'B1区',
    area: 150,
    progress: 45,
    status: 'inProgress',
    statusText: '收获中',
    expectedYield: 60.8
  },
  {
    id: 'field-4',
    name: 'B2区',
    area: 80,
    progress: 0,
    status: 'notStarted',
    statusText: '未开始',
    expectedYield: 32.6
  }
]);

// 收获计划数据
const harvestPlans = ref<HarvestPlan[]>([
  {
    id: 'plan-1',
    name: 'A区油菜收获计划',
    startDate: '2025-05-10',
    endDate: '2025-05-25',
    fieldIds: ['field-1', 'field-2'],
    expectedTotalYield: 88.7,
    actualTotalYield: 85.4,
    responsiblePerson: '张三',
    harvestMethod: '机械收获',
    status: 'inProgress'
  },
  {
    id: 'plan-2',
    name: 'B区油菜收获计划',
    startDate: '2025-05-20',
    endDate: '2025-06-05',
    fieldIds: ['field-3', 'field-4'],
    expectedTotalYield: 93.4,
    responsiblePerson: '李四',
    harvestMethod: '混合方式',
    status: 'planned'
  }
]);

// 整地准备任务
const preparationTasks = ref<PreparationTask[]>([
  {
    id: 'task-1',
    name: 'A1区土壤翻耕',
    deadline: '2025-06-10',
    responsible: '王五',
    status: 'inProgress',
    statusText: '进行中'
  },
  {
    id: 'task-2',
    name: '有机肥采购',
    deadline: '2025-06-05',
    responsible: '张三',
    status: 'completed',
    statusText: '已完成'
  },
  {
    id: 'task-3',
    name: '深耕机维护',
    deadline: '2025-06-08',
    responsible: '赵六',
    status: 'pending',
    statusText: '待处理'
  },
  {
    id: 'task-4',
    name: '土壤检测',
    deadline: '2025-06-15',
    responsible: '李四',
    status: 'pending',
    statusText: '待处理'
  }
]);

// 设备准备情况
const equipmentList = ref<Equipment[]>([
  {
    id: 'eq-1',
    name: '联合收割机',
    quantity: 3,
    condition: '良好',
    status: 'inUse'
  },
  {
    id: 'eq-2',
    name: '深耕机',
    quantity: 2,
    condition: '良好',
    status: 'available'
  },
  {
    id: 'eq-3',
    name: '旋耕机',
    quantity: 2,
    condition: '需维护',
    status: 'maintenance'
  },
  {
    id: 'eq-4',
    name: '播种机',
    quantity: 1,
    condition: '良好',
    status: 'available'
  }
]);

// 当前编辑的收获计划
const currentPlan = ref<HarvestPlan>({
  id: '',
  name: '',
  startDate: new Date().toISOString().split('T')[0],
  endDate: new Date(Date.now() + 15 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
  fieldIds: [],
  expectedTotalYield: 0,
  responsiblePerson: '',
  harvestMethod: '机械收获',
  status: 'planned'
});

// 地块状态样式
const fieldStatusClass = (status: string): string => {
  switch (status) {
    case 'completed': return 'status-completed';
    case 'inProgress': return 'status-in-progress';
    case 'notStarted': return 'status-not-started';
    default: return '';
  }
};

// 任务状态样式
const taskStatusClass = (status: string): string => {
  switch (status) {
    case 'completed': return 'status-completed';
    case 'inProgress': return 'status-in-progress';
    case 'pending': return 'status-pending';
    default: return '';
  }
};

// 设备状态样式
const equipmentStatusClass = (status: string): string => {
  switch (status) {
    case 'available': return 'status-available';
    case 'inUse': return 'status-in-use';
    case 'maintenance': return 'status-maintenance';
    default: return '';
  }
};

// 初始化收获进度图表
const initHarvestProgressChart = () => {
  const ctx = document.getElementById('harvestProgressChart') as HTMLCanvasElement;
  if (!ctx) return;

  // 销毁已有图表
  if (harvestProgressChart.value) {
    harvestProgressChart.value.destroy();
  }

  // 创建新图表
  harvestProgressChart.value = new Chart(ctx, {
    type: 'line',
    data: {
      labels: Array.from({length: 7}, (_, i) => {
        const date = new Date();
        date.setDate(date.getDate() - 6 + i);
        return `${date.getMonth() + 1}/${date.getDate()}`;
      }),
      datasets: [
        {
          label: '总体收获进度 (%)',
          data: [40, 45, 48, 52, 55, 60, 65],
          borderColor: 'rgba(46, 125, 50, 1)',
          backgroundColor: 'rgba(46, 125, 50, 0.1)',
          tension: 0.4,
          fill: true
        },
        {
          label: '日收获量 (吨)',
          data: [12.5, 15.8, 14.2, 16.5, 13.8, 17.2, 15.6],
          borderColor: 'rgba(33, 150, 243, 1)',
          backgroundColor: 'transparent',
          tension: 0.4,
          yAxisID: 'y1'
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        y: {
          beginAtZero: true,
          max: 100,
          title: {
            display: true,
            text: '收获进度 (%)'
          }
        },
        y1: {
          position: 'right',
          grid: { drawOnChartArea: false },
          max: 30,
          title: {
            display: true,
            text: '日收获量 (吨)'
          }
        }
      }
    }
  });
};

// 显示制定收获计划表单
const showHarvestPlanForm = () => {
  isEditingPlan.value = false;
  currentPlan.value = {
    id: Date.now().toString(),
    name: '',
    startDate: new Date().toISOString().split('T')[0],
    endDate: new Date(Date.now() + 15 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
    fieldIds: [],
    expectedTotalYield: 0,
    responsiblePerson: '',
    harvestMethod: '机械收获',
    status: 'planned'
  };
  showPlanModal.value = true;
};

// 关闭计划弹窗
const closePlanModal = () => {
  showPlanModal.value = false;
};

// 保存收获计划
const saveHarvestPlan = () => {
  // 表单验证
  if (!currentPlan.value.name || !currentPlan.value.startDate || !currentPlan.value.endDate || 
      currentPlan.value.fieldIds.length === 0 || !currentPlan.value.responsiblePerson) {
    alert('请填写所有带"*"的必填字段');
    return;
  }

  // 深拷贝避免引用问题
  const planToSave = JSON.parse(JSON.stringify(currentPlan.value));

  if (isEditingPlan.value) {
    // 编辑模式：更新原计划
    const index = harvestPlans.value.findIndex(plan => plan.id === planToSave.id);
    if (index !== -1) {
      harvestPlans.value[index] = planToSave;
    }
  } else {
    // 添加模式：插入新计划
    harvestPlans.value.push(planToSave);
  }

  // 关闭弹窗
  showPlanModal.value = false;
  alert('收获计划保存成功！');
};

// 更新收获进度
const updateHarvestProgress = () => {
  // 模拟更新进度
  const newProgress = Math.min(100, overallHarvestProgress.value + Math.floor(Math.random() * 10));
  overallHarvestProgress.value = newProgress;
  
  // 同步更新已收获面积和产量
  const totalArea = fields.value.reduce((sum, field) => sum + field.area, 0);
  harvestedArea.value = Math.round((newProgress / 100) * totalArea * 10) / 10;
  
  const totalExpectedYield = fields.value.reduce((sum, field) => sum + field.expectedYield, 0);
  totalHarvested.value = Math.round((newProgress / 100) * totalExpectedYield * 10) / 10;
  
  // 更新剩余天数
  remainingDays.value = Math.max(0, remainingDays.value - 1);
  
  // 重新初始化图表以更新数据
  initHarvestProgressChart();
  
  alert('收获进度已更新！');
};

// 显示收获分析
const showAnalysis = () => {
  alert('收获分析功能即将上线，敬请期待！\n将支持：\n1. 产量分析\n2. 收获效率分析\n3. 成本收益分析\n4. 与历史数据对比');
};

// 初始化
onMounted(() => {
  initHarvestProgressChart();
});

// 清理
onUnmounted(() => {
  if (harvestProgressChart.value) {
    harvestProgressChart.value.destroy();
  }
});
</script>

<style scoped>
.harvest-preparation-container {
  padding: 20px;
}

/* 顶部标题栏 */
.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.content-actions {
  display: flex;
  gap: 10px;
}

/* 进度概览 */
.progress-overview {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.progress-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.progress-bar {
  height: 20px;
  background-color: #f1f3f5;
  border-radius: 10px;
  overflow: hidden;
  position: relative;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #4CAF50, #8BC34A);
  border-radius: 10px;
  transition: width 0.3s ease;
}

.progress-text {
  text-align: center;
  font-size: 18px;
  font-weight: 600;
  color: #2E7D32;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 15px;
}

.stat-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding: 15px;
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  font-size: 32px;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: #333;
}

.stat-label {
  font-size: 12px;
  color: #666;
}

/* 收获详情 */
.harvest-details {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.chart-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding: 20px;
}

.chart-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.chart-container {
  height: 300px;
}

.chart-period {
  font-size: 12px;
  color: #666;
}

.field-status-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding: 20px;
}

.field-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 15px;
  margin-top: 15px;
}

.field-item {
  padding: 15px;
  border-radius: 12px;
  border-left: 4px solid #ddd;
  background-color: #f9f9f9;
}

.field-item.status-completed {
  border-left-color: #4CAF50;
  background-color: #e8f5e9;
}

.field-item.status-in-progress {
  border-left-color: #2196F3;
  background-color: #e3f2fd;
}

.field-item.status-not-started {
  border-left-color: #FF9800;
  background-color: #fff8e1;
}

.field-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.field-name {
  font-weight: 600;
  color: #333;
}

.field-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  background-color: #ddd;
  color: #666;
}

.field-item.status-completed .field-status {
  background-color: #4CAF50;
  color: white;
}

.field-item.status-in-progress .field-status {
  background-color: #2196F3;
  color: white;
}

.field-item.status-not-started .field-status {
  background-color: #FF9800;
  color: white;
}

.field-info {
  display: flex;
  gap: 20px;
}

.field-metric {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.metric-label {
  font-size: 12px;
  color: #666;
}

.metric-value {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

/* 整地准备 */
.preparation-section {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding: 20px;
}

.preparation-tabs {
  display: flex;
  border-bottom: 1px solid #eee;
  margin-top: 15px;
}

.preparation-tab {
  padding: 10px 20px;
  border: none;
  background: none;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: #666;
  position: relative;
}

.preparation-tab.active {
  color: #2196F3;
}

.preparation-tab.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 100%;
  height: 2px;
  background-color: #2196F3;
}

.preparation-content {
  padding: 20px 0;
}

/* 任务列表 */
.tasks-list, .equipment-list, .recommendations-list {
  display: grid;
  gap: 15px;
}

.task-item {
  padding: 15px;
  border-radius: 12px;
  border-left: 4px solid #ddd;
  background-color: #f9f9f9;
}

.task-item.status-completed {
  border-left-color: #4CAF50;
  background-color: #e8f5e9;
}

.task-item.status-in-progress {
  border-left-color: #2196F3;
  background-color: #e3f2fd;
}

.task-item.status-pending {
  border-left-color: #FF9800;
  background-color: #fff8e1;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.task-name {
  font-weight: 600;
  color: #333;
}

.task-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  background-color: #ddd;
  color: #666;
}

.task-item.status-completed .task-status {
  background-color: #4CAF50;
  color: white;
}

.task-item.status-in-progress .task-status {
  background-color: #2196F3;
  color: white;
}

.task-item.status-pending .task-status {
  background-color: #FF9800;
  color: white;
}

.task-details {
  display: flex;
  gap: 20px;
  font-size: 12px;
  color: #666;
}

/* 设备列表 */
.equipment-item {
  padding: 15px;
  border-radius: 12px;
  border: 1px solid #eee;
  background-color: #f9f9f9;
}

.equipment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.equipment-name {
  font-weight: 600;
  color: #333;
}

.equipment-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
}

.equipment-status.status-available {
  background-color: #4CAF50;
  color: white;
}

.equipment-status.status-in-use {
  background-color: #2196F3;
  color: white;
}

.equipment-status.status-maintenance {
  background-color: #FF9800;
  color: white;
}

.equipment-details {
  display: flex;
  gap: 20px;
  font-size: 12px;
  color: #666;
}

/* 建议列表 */
.recommendation-item {
  padding: 15px;
  border-radius: 12px;
  border: 1px solid #eee;
  background-color: #f9f9f9;
  margin-bottom: 15px;
}

.recommendation-header {
  margin-bottom: 8px;
}

.recommendation-title {
  font-weight: 600;
  color: #333;
}

.recommendation-content {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

/* 按钮样式 */
.btn-plan, .btn-update, .btn-analysis, .btn-cancel, .btn-save {
  padding: 10px 20px;
  border-radius: 6px;
  border: none;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-plan {
  background-color: #4CAF50;
  color: white;
}

.btn-update {
  background-color: #2196F3;
  color: white;
}

.btn-analysis {
  background-color: #9C27B0;
  color: white;
}

.btn-cancel {
  background-color: #f5f5f5;
  color: #333;
}

.btn-save {
  background-color: #4CAF50;
  color: white;
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  border-radius: 16px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
}

.modal-header {
  padding: 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  color: #333;
  font-size: 18px;
  margin: 0;
}

.modal-close {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #666;
  padding: 5px;
}

.modal-body {
  padding: 20px;
}

.modal-footer {
  padding: 20px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 表单样式 */
.plan-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-row {
  display: flex;
  gap: 15px;
}

.form-row .form-group {
  flex: 1;
}

.form-group label {
  font-weight: 500;
  color: #333;
}

.form-group input, .form-group select, .form-group textarea {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
}

.form-group input:focus, .form-group select:focus, .form-group textarea:focus {
  outline: none;
  border-color: #2196F3;
  box-shadow: 0 0 0 2px rgba(33, 150, 243, 0.1);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .progress-overview,
  .harvest-details {
    grid-template-columns: 1fr;
  }
  
  .content-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .content-actions {
    width: 100%;
    justify-content: space-between;
  }
  
  .form-row {
    flex-direction: column;
  }
  
  .stats-cards {
    grid-template-columns: 1fr;
  }
}
</style>