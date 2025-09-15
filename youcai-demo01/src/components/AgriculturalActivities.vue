<template>
  <div class="agricultural-activities-container">
    <div class="content-header">
      <h2>农事活动记录</h2>
      <div class="content-actions">
        <button class="btn-add" @click="showAddActivityForm">➕ 添加记录</button>
        <button class="btn-import" @click="importActivities">📤 导入数据</button>
        <button class="btn-statistics" @click="showStatistics">📊 统计分析</button>
      </div>
    </div>

    <div class="filter-section">
      <div class="filter-row">
        <div class="filter-group">
          <label>活动类型</label>
          <select v-model="filter.activityType">
            <option value="">全部类型</option>
            <option value="planting">种植</option>
            <option value="fertilization">施肥</option>
            <option value="irrigation">灌溉</option>
            <option value="pestControl">病虫害防治</option>
            <option value="harvest">收获</option>
            <option value="other">其他</option>
          </select>
        </div>

        <div class="filter-group">
          <label>日期范围</label>
          <div class="date-range">
            <input type="date" v-model="filter.startDate">
            <span class="date-separator">至</span>
            <input type="date" v-model="filter.endDate">
          </div>
        </div>

        <div class="filter-group">
          <label>负责人</label>
          <select v-model="filter.responsiblePerson">
            <option value="">全部人员</option>
            <option value="张三">张三</option>
            <option value="李四">李四</option>
            <option value="王五">王五</option>
            <option value="赵六">赵六</option>
          </select>
        </div>

        <div class="filter-actions">
          <button class="btn-filter" @click="filterActivities">筛选</button>
          <button class="btn-reset" @click="resetFilters">重置</button>
        </div>
      </div>
    </div>

    <div class="activities-table-container">
      <table class="activities-table">
        <thead>
          <tr>
            <th>日期</th>
            <th>活动类型</th>
            <th>地块</th>
            <th>负责人</th>
            <th>活动内容</th>
            <th>投入物料</th>
            <th>耗时</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(activity, index) in filteredActivities" :key="activity.id">
            <td>{{ activity.date }}</td>
            <td><span :class="'activity-type-tag type-' + activity.type">{{ formatActivityType(activity.type) }}</span></td>
            <td>{{ activity.field }}</td>
            <td>{{ activity.responsiblePerson }}</td>
            <td class="activity-content">{{ activity.content }}</td>
            <td class="activity-materials">
              <div v-for="(material, idx) in activity.materials" :key="idx" class="material-item">{{ material.name }}: {{ material.amount }}{{ material.unit }}</div>
            </td>
            <td>{{ activity.duration }}小时</td>
            <td class="activity-actions">
              <button class="btn-view" @click="viewActivity(index)">查看</button>
              <button class="btn-edit" @click="editActivity(index)">编辑</button>
              <button class="btn-delete" @click="deleteActivity(index)">删除</button>
            </td>
          </tr>
          <tr v-if="filteredActivities.length === 0">
            <td colspan="8" class="no-data">暂无符合条件的农事活动记录</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 添加/编辑活动弹窗 -->
    <div class="modal-overlay" v-if="showActivityModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>{{ isEditing ? '编辑农事活动' : '添加农事活动' }}</h3>
          <button class="modal-close" @click="closeActivityModal">✕</button>
        </div>
        <div class="modal-body">
          <form class="activity-form">
            <div class="form-row">
              <div class="form-group">
                <label>活动日期 *</label>
                <input type="date" v-model="currentActivity.date" required>
              </div>
              <div class="form-group">
                <label>活动类型 *</label>
                <select v-model="currentActivity.type" required>
                  <option value="">选择类型</option>
                  <option value="planting">种植</option>
                  <option value="fertilization">施肥</option>
                  <option value="irrigation">灌溉</option>
                  <option value="pestControl">病虫害防治</option>
                  <option value="harvest">收获</option>
                  <option value="other">其他</option>
                </select>
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>地块 *</label>
                <select v-model="currentActivity.field" required>
                  <option value="">选择地块</option>
                  <option value="A1区">A1区</option>
                  <option value="A2区">A2区</option>
                  <option value="B1区">B1区</option>
                  <option value="B2区">B2区</option>
                  <option value="C1区">C1区</option>
                  <option value="C2区">C2区</option>
                </select>
              </div>
              <div class="form-group">
                <label>负责人 *</label>
                <select v-model="currentActivity.responsiblePerson" required>
                  <option value="">选择负责人</option>
                  <option value="张三">张三</option>
                  <option value="李四">李四</option>
                  <option value="王五">王五</option>
                  <option value="赵六">赵六</option>
                </select>
              </div>
            </div>

            <div class="form-group">
              <label>活动内容 *</label>
              <textarea v-model="currentActivity.content" rows="3" required></textarea>
            </div>

            <div class="materials-section">
              <label>投入物料</label>
              <div class="materials-list" v-for="(material, idx) in currentActivity.materials" :key="idx">
                <div class="form-row">
                  <div class="form-group">
                    <input type="text" v-model="material.name" placeholder="物料名称">
                  </div>
                  <div class="form-group">
                    <input type="number" v-model.number="material.amount" placeholder="数量" min="0">
                  </div>
                  <div class="form-group">
                    <input type="text" v-model="material.unit" placeholder="单位">
                  </div>
                  <button type="button" class="btn-remove-material" @click="removeMaterial(idx)">✕</button>
                </div>
              </div>
              <button type="button" class="btn-add-material" @click="addMaterial">➕ 添加物料</button>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>耗时(小时)</label>
                <input type="number" v-model.number="currentActivity.duration" step="0.5" min="0">
              </div>
              <div class="form-group">
                <label>完成状态</label>
                <select v-model="currentActivity.status">
                  <option value="completed">已完成</option>
                  <option value="inProgress">进行中</option>
                  <option value="planned">计划中</option>
                  <option value="cancelled">已取消</option>
                </select>
              </div>
            </div>

            <div class="form-group">
              <label>备注</label>
              <textarea v-model="currentActivity.notes" rows="2"></textarea>
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="closeActivityModal">取消</button>
          <button class="btn-save" @click="saveActivity">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';

// 1. 定义核心接口（类型严谨，避免隐式any）
interface ActivityMaterial {
  name: string;
  amount: number;
  unit: string;
}

interface AgriculturalActivity {
  id: string;
  date: string;
  type: 'planting' | 'fertilization' | 'irrigation' | 'pestControl' | 'harvest' | 'other';
  field: string;
  responsiblePerson: string;
  content: string;
  materials: ActivityMaterial[];
  duration: number;
  status: 'completed' | 'inProgress' | 'planned' | 'cancelled';
  notes?: string;
}

// 2. 定义筛选条件接口（优化filter类型）
interface FilterType {
  activityType: string;
  startDate: string;
  endDate: string;
  responsiblePerson: string;
}

// 3. 状态定义（核心修复：补充isEditing的const声明，修复变量未定义错误）
const activities = ref<AgriculturalActivity[]>([]);
const filter = ref<FilterType>({
  activityType: '',
  startDate: '',
  endDate: '',
  responsiblePerson: ''
});
const showActivityModal = ref<boolean>(false);
// 修复：isEditing缺失const声明导致报错
const isEditing = ref<boolean>(false);
const currentActivity = ref<AgriculturalActivity>({
  id: '',
  date: new Date().toISOString().split('T')[0],
  type: 'planting', // 给默认值，避免初始类型不匹配
  field: '',
  responsiblePerson: '',
  content: '',
  materials: [],
  duration: 0,
  status: 'completed',
  notes: ''
});

// 4. 计算属性：筛选后的活动列表（逻辑不变，类型更明确）
const filteredActivities = computed(() => {
  return activities.value.filter(activity => {
    // 类型筛选
    if (filter.value.activityType && activity.type !== filter.value.activityType) {
      return false;
    }
    // 日期筛选（处理空日期场景，避免Invalid Date错误）
    if (filter.value.startDate && new Date(activity.date) < new Date(filter.value.startDate)) {
      return false;
    }
    if (filter.value.endDate && new Date(activity.date) > new Date(filter.value.endDate)) {
      return false;
    }
    // 负责人筛选
    if (filter.value.responsiblePerson && activity.responsiblePerson !== filter.value.responsiblePerson) {
      return false;
    }
    return true;
  });
});

// 5. 格式化活动类型显示（补充类型守卫，避免异常值）
const formatActivityType = (type: string): string => {
  const typeMap: Record<string, string> = {
    'planting': '种植',
    'fertilization': '施肥',
    'irrigation': '灌溉',
    'pestControl': '病虫害防治',
    'harvest': '收获',
    'other': '其他'
  };
  // 类型守卫：处理未定义的类型
  return typeMap[type as keyof typeof typeMap] || '未知类型';
};

// 6. 物料操作（确保数组操作安全）
const addMaterial = () => {
  // 初始化为空物料，避免undefined
  currentActivity.value.materials.push({ name: '', amount: 0, unit: '' });
};

const removeMaterial = (index: number) => {
  // 边界校验：避免无效索引
  if (index >= 0 && index < currentActivity.value.materials.length) {
    currentActivity.value.materials.splice(index, 1);
  }
};

// 7. 活动记录操作（优化深拷贝，避免引用问题）
const showAddActivityForm = () => {
  isEditing.value = false;
  // 重置当前活动，生成唯一ID
  currentActivity.value = {
    id: Date.now().toString(), // 时间戳作为唯一ID
    date: new Date().toISOString().split('T')[0],
    type: 'planting',
    field: '',
    responsiblePerson: '',
    content: '',
    materials: [],
    duration: 0,
    status: 'completed',
    notes: ''
  };
  showActivityModal.value = true;
};

const viewActivity = (index: number) => {
  // 边界校验：避免无效索引
  if (index < 0 || index >= filteredActivities.value.length) return;
  const activity = filteredActivities.value[index];
  alert(`查看活动：\n日期：${activity.date}\n类型：${formatActivityType(activity.type)}\n地块：${activity.field}`);
};

const editActivity = (index: number) => {
  // 边界校验：避免无效索引
  if (index < 0 || index >= filteredActivities.value.length) return;
  const activity = filteredActivities.value[index];
  isEditing.value = true;
  // 深拷贝：避免直接修改原数据（优化JSON拷贝，处理特殊场景）
  currentActivity.value = JSON.parse(JSON.stringify(activity));
  showActivityModal.value = true;
};

const deleteActivity = (index: number) => {
  // 边界校验：避免无效索引
  if (index < 0 || index >= filteredActivities.value.length) return;
  if (confirm('确定要删除这条活动记录吗？删除后不可恢复！')) {
    const activity = filteredActivities.value[index];
    // 找到原数组中的索引（避免筛选后索引不匹配）
    const originalIndex = activities.value.findIndex(a => a.id === activity.id);
    if (originalIndex !== -1) {
      activities.value.splice(originalIndex, 1);
    }
  }
};

const saveActivity = () => {
  // 表单校验：确保必填字段不为空
  const { type, field, responsiblePerson, content } = currentActivity.value;
  if (!type || !field || !responsiblePerson || !content) {
    alert('请填写所有带"*"的必填字段');
    return;
  }

  // 深拷贝：避免引用污染
  const activityToSave = JSON.parse(JSON.stringify(currentActivity.value));

  if (isEditing.value) {
    // 编辑模式：更新原数据
    const index = activities.value.findIndex(a => a.id === activityToSave.id);
    if (index !== -1) {
      activities.value[index] = activityToSave;
    }
  } else {
    // 添加模式：插入到数组头部
    activities.value.unshift(activityToSave);
  }

  // 关闭弹窗
  showActivityModal.value = false;
};

const closeActivityModal = () => {
  showActivityModal.value = false;
};

// 8. 筛选和重置（逻辑优化，确保响应式更新）
const filterActivities = () => {
  console.log('筛选条件：', filter.value);
  // computed会自动响应filter变化，无需额外操作
};

const resetFilters = () => {
  // 重置筛选条件（触发computed更新）
  filter.value = {
    activityType: '',
    startDate: '',
    endDate: '',
    responsiblePerson: ''
  };
};

// 9. 导入和统计（保留原逻辑，补充用户反馈）
const importActivities = () => {
  alert('导入功能说明：支持Excel格式（.xlsx），需包含"日期、类型、地块、负责人、内容"列');
};

const showStatistics = () => {
  alert('统计分析功能即将上线，将支持：\n1. 按活动类型统计频次\n2. 按地块统计投入物料\n3. 按负责人统计工作量');
};

// 10. 初始化示例数据（确保数据符合接口类型）
const initSampleData = () => {
  activities.value = [
    {
      id: '1',
      date: '2025-08-20',
      type: 'fertilization',
      field: 'A1区',
      responsiblePerson: '张三',
      content: '油菜花期追肥，以氮肥为主，配合磷钾肥',
      materials: [
        { name: '尿素', amount: 15, unit: 'kg' },
        { name: '磷酸二氢钾', amount: 5, unit: 'kg' }
      ],
      duration: 6,
      status: 'completed',
      notes: '天气晴朗，施肥后浇水'
    },
    {
      id: '2',
      date: '2025-08-22',
      type: 'pestControl',
      field: 'A2区',
      responsiblePerson: '李四',
      content: '蚜虫防治，使用生物农药',
      materials: [
        { name: '苦参碱', amount: 2.5, unit: 'L' },
        { name: '助剂', amount: 0.5, unit: 'L' }
      ],
      duration: 4,
      status: 'completed',
      notes: '下午施药，注意防护'
    },
    {
      id: '3',
      date: '2025-08-25',
      type: 'irrigation',
      field: 'B1区,B2区',
      responsiblePerson: '王五',
      content: '喷灌系统浇水，土壤湿度低于60%',
      materials: [],
      duration: 8,
      status: 'completed',
      notes: '夜间灌溉，节约用水'
    }
  ];
};

// 组件挂载时初始化数据
onMounted(() => {
  initSampleData();
});
</script>

<style scoped>
.agricultural-activities-container {
  padding: 20px;
  background-color: #f8f9fa;
  min-height: calc(100vh - 85px); /* 适配顶部导航高度 */
}

/* 顶部标题栏 */
.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.content-header h2 {
  color: #2E7D32;
  font-size: 24px;
  margin: 0;
}

.content-actions {
  display: flex;
  gap: 10px;
}

/* 筛选区域 */
.filter-section {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding: 20px;
  margin: 0 0 20px 0;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  align-items: flex-end;
}

.filter-group {
  flex: 1;
  min-width: 180px;
}

.filter-group label {
  display: block;
  margin-bottom: 8px;
  color: #666;
  font-weight: 500;
}

.filter-group select,
.filter-group input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
}

.date-range {
  display: flex;
  gap: 10px;
  align-items: center;
}

.date-separator {
  color: #666;
  white-space: nowrap;
}

.filter-actions {
  display: flex;
  gap: 10px;
  padding-bottom: 25px; /* 对齐筛选框底部 */
}

/* 表格区域 */
.activities-table-container {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding: 20px;
  overflow-x: auto;
}

.activities-table {
  width: 100%;
  border-collapse: collapse;
}

.activities-table th,
.activities-table td {
  padding: 12px 15px;
  text-align: left;
  border-bottom: 1px solid #eee;
  font-size: 14px;
}

.activities-table th {
  background-color: #f5f7fa;
  font-weight: 600;
  color: #333;
}

/* 活动类型标签 */
.activity-type-tag {
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 12px;
  white-space: nowrap;
}

.type-planting { background-color: #e8f5e9; color: #2E7D32; }
.type-fertilization { background-color: #fff8e1; color: #FF8F00; }
.type-irrigation { background-color: #e3f2fd; color: #1976D2; }
.type-pestControl { background-color: #ffebee; color: #D32F2F; }
.type-harvest { background-color: #f3e5f5; color: #7B1FA2; }
.type-other { background-color: #f5f5f5; color: #757575; }

/* 表格内容适配 */
.activity-content {
  max-width: 200px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.activity-materials {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.material-item {
  font-size: 12px;
  color: #666;
}

.activity-actions {
  display: flex;
  gap: 5px;
}

/* 按钮样式统一 */
.btn-filter,
.btn-reset,
.btn-view,
.btn-edit,
.btn-delete,
.btn-add,
.btn-import,
.btn-statistics,
.btn-add-material,
.btn-remove-material,
.btn-save,
.btn-cancel {
  padding: 8px 12px;
  border-radius: 6px;
  border: none;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.btn-filter {
  background-color: #2196F3;
  color: white;
}

.btn-reset {
  background-color: #f5f5f5;
  color: #333;
}

.btn-view {
  background-color: #e3f2fd;
  color: #1976D2;
}

.btn-edit {
  background-color: #fff8e1;
  color: #FF8F00;
}

.btn-delete {
  background-color: #ffebee;
  color: #D32F2F;
}

.btn-add {
  background-color: #4CAF50;
  color: white;
}

.btn-import {
  background-color: #FF9800;
  color: white;
}

.btn-statistics {
  background-color: #9C27B0;
  color: white;
}

.btn-add-material {
  background-color: #f5f5f5;
  color: #333;
  border: 1px solid #ddd;
  margin-top: 5px;
}

.btn-remove-material {
  background-color: #ffebee;
  color: #D32F2F;
  border: none;
  padding: 8px;
  height: fit-content;
}

.btn-save {
  background-color: #4CAF50;
  color: white;
  padding: 10px 20px;
}

.btn-cancel {
  background-color: #f5f5f5;
  color: #333;
  padding: 10px 20px;
}

/* 空数据提示 */
.no-data {
  text-align: center;
  padding: 40px 0;
  color: #999;
  font-size: 14px;
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
  max-width: 800px;
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
  transition: color 0.2s;
}

.modal-close:hover {
  color: #D32F2F;
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
.activity-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-row {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.form-group {
  flex: 1;
  min-width: 200px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #666;
  font-weight: 500;
  font-size: 14px;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #2196F3;
}

.materials-section {
  margin-top: 10px;
}

.materials-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin: 15px 0;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .content-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .content-actions {
    width: 100%;
    justify-content: space-between;
  }

  .filter-row {
    gap: 10px;
  }

  .filter-group {
    min-width: 100%;
  }

  .filter-actions {
    padding-bottom: 0;
    width: 100%;
    justify-content: flex-end;
  }

  .activity-content {
    max-width: 150px;
  }

  .form-group {
    min-width: 100%;
  }
}

@media (max-width: 480px) {
  .content-actions {
    flex-wrap: wrap;
    gap: 8px;
  }

  .btn-add,
  .btn-import,
  .btn-statistics {
    flex: 1;
    min-width: 100px;
  }

  .activities-table th,
  .activities-table td {
    padding: 8px 10px;
    font-size: 12px;
  }

  .activity-type-tag {
    padding: 2px 5px;
    font-size: 11px;
  }
}
</style>