<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { useSelectStore } from '/@/store/selectStore'; // 全局状态
// 导入基地/地块查询接口（根据实际API路径调整）
import { getBaseById, getPlotById } from '/src/views/rapeseed/production-plan/plot-production-plan/base.api';
// 导入天地图组件
import TiandituMap from '/@/components/TiandituMap/index.vue';

// 弹窗显示控制
const showBaseInfoModal = ref(false);
const showPlotInfoModal = ref(false);

const selectStore = useSelectStore();

// 表单数据
const baseForm = ref({
  abbreviation: '',
  fullName: '',
  manager: '',
  phone: '',
  address: '',
  area:0,
  soilType: [''] as string[]
});

const plotForm = ref({
  name: '',
  code: '',
  base: '',
  area: 0,
  growthStage: '',
  location: ''
});

const newPlot = ref({
  name: '',
  color: 'blue',
  opacity: 50,
  area: 0
});

// 监听全局状态变化，同步更新数据（可选：切换基地/地块时自动刷新）
watch(
  () => [selectStore.selectedBase.baseId, selectStore.selectedPlot.plotId],
  ([newBaseId, newPlotId]) => {
    // 若弹窗已打开，自动刷新数据
    if (showBaseInfoModal.value && newBaseId) {
      fetchBaseInfo(newBaseId);
    }
    if (showPlotInfoModal.value && newPlotId) {
      fetchPlotInfo(newPlotId);
    }
  },
  { immediate: false }
);

// 查询基地详情
const fetchBaseInfo = async (baseId: string | number) => {
  try {
    const res = await getBaseById(baseId);
    const baseData = res || {}; // 假设接口数据在result中，根据实际调整
    // 赋值给表单（字段名需与接口返回一致）
    baseForm.value = {
      fullName: baseData.baseName || '',
      manager: baseData.manager || '',
      phone: baseData.phone || '',
      address: baseData.address || '',
      area: baseData.area || 0,
      soilType: baseData.soilType ? (Array.isArray(baseData.soilType) ? baseData.soilType : [baseData.soilType]) : []
    };
  } catch (error) {
    console.error('获取基地详情失败：', error);
    // 错误时重置表单
    baseForm.value = {
      abbreviation: '', fullName: '', manager: '', phone: '', address: '', area: 0, crop: '', soilType: []
    };
  }
};

// 查询地块详情（需后端提供按地块ID查询的接口）
const fetchPlotInfo = async (plotId: string | number) => {
  try {
    const res = await getPlotById(plotId); // 新增：地块详情接口（需后端提供）
    const plotData = res|| {};
    // 赋值给表单（字段名需与接口返回一致）
    plotForm.value = {
      name: plotData.plotName || '',
      code: plotData.id || '',
      base: selectStore.selectedBase.baseName || '', // 所属基地名称从全局状态获取
      area: plotData.area || 0,
      growthStage: plotData.growthStage || '',
      location: plotData.location || ''
    };
  } catch (error) {
    console.error('获取地块详情失败：', error);
    // 错误时重置表单
    plotForm.value = {
      name: '', code: '', base: '', area: 0, growthStage: '', location: ''
    };
  }
};

// 点击基地信息按钮
const handleOpenBaseModal = () => {
  const currentBaseId = selectStore.selectedBase.baseId;
  console.log("zzzzzzzzzz",currentBaseId)
  if (!currentBaseId) {
    alert('请先选择基地'); // 或使用之前的 showMessage 提示
    return;
  }
  // 查询数据后打开弹窗
  fetchBaseInfo(currentBaseId).then(() => {
    showBaseInfoModal.value = true;
  });
};

// 点击地块信息按钮
const handleOpenPlotModal = () => {
  const currentPlotId = selectStore.selectedPlot.plotId;

  if (!currentPlotId) {
    alert('请先选择地块'); // 或使用之前的 showMessage 提示
    return;
  }
  // 查询数据后打开弹窗
  fetchPlotInfo(currentPlotId).then(() => {
    showPlotInfoModal.value = true;
  });
};
</script>

<template>
  <!-- 新增：天地图与悬浮按钮的父容器 -->
  <div class="map-with-buttons">
    <div class="map-container">
      <!-- 悬浮按钮容器（原按钮组件，新增定位样式） -->
      <div class="horizontal-button-component">
        <button @click="handleOpenBaseModal" class="horizontal-btn">
            <i class="icon-base"></i> 基地信息
          </button>
          <button @click="handleOpenPlotModal" class="horizontal-btn">
            <i class="icon-plot"></i> 地块信息
          </button>
        </div>

        <!-- 替换为天地图组件 -->
        <TiandituMap 
          :enableManagement="false" 
          :mapWidth="'100%'" 
          :mapHeight="'100%'" 
          :showSensors="true"
        />
    </div>
  </div>
  <!-- 弹窗部分 -->
  <div v-if="showBaseInfoModal" class="modal-overlay" @click="showBaseInfoModal = false">
    <div class="modal-container" @click.stop>
      <div class="modal-header">基地信息</div>
      <div class="modal-body">
        <div class="form-item">
          <label>全称</label>
          <input type="text" :value="baseForm.fullName" disabled>
        </div>
        <div class="form-item">
          <label>基地负责人</label>
          <input type="text" :value="baseForm.manager" disabled>
        </div>
        <div class="form-item">
          <label>电话</label>
          <input type="text" :value="baseForm.phone" disabled>
        </div>
        <div class="form-item">
          <label>地址</label>
          <textarea :value="baseForm.address" disabled></textarea>
        </div>
        <div class="form-item">
          <label>面积</label>
          <input type="text" :value="baseForm.area + ' 亩'" disabled>
        </div>
        <div class="form-item">
          <label>土壤状况</label>
          <div class="checkbox-group">
            <label><input type="checkbox" :checked="baseForm.soilType.includes('黏土')" disabled> 黏土</label>
            <label><input type="checkbox" :checked="baseForm.soilType.includes('沙土')" disabled> 沙土</label>
            <label><input type="checkbox" :checked="baseForm.soilType.includes('壤土')" disabled> 壤土</label>
          </div>
        </div>
      </div>
      <div class="modal-footer">
        <button @click="showBaseInfoModal = false">返回</button>
      </div>
    </div>
  </div>


  <div v-if="showPlotInfoModal" class="modal-overlay" @click="showPlotInfoModal = false">
    <div class="modal-container" @click.stop>
      <div class="modal-header">地块信息</div>
      <div class="modal-body">
        <div class="form-item">
          <label>地块名</label>
          <input type="text" :value="plotForm.name" disabled>
        </div>
        <div class="form-item">
          <label>地块编号</label>
          <input type="text" :value="plotForm.code" disabled>
        </div>
        <div class="form-item">
          <label>所属基地</label>
          <input type="text" :value="plotForm.base" disabled>
        </div>
        <div class="form-item">
          <label>地块面积</label>
          <input type="text" :value="plotForm.area + ' 亩'" disabled>
        </div>
        <div class="form-item">
          <label>生长阶段</label>
          <input type="text" :value="plotForm.growthStage" disabled>
        </div>
        <div class="form-item">
          <label>地块位置</label>
          <textarea :value="plotForm.location" disabled></textarea>
        </div>
      </div>
      <div class="modal-footer">
        <button @click="showPlotInfoModal = false">返回</button>
      </div>
    </div>
  </div>

</template>

<style scoped lang="less">
/* 新增：天地图与按钮的父容器样式 */
.map-with-buttons {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden; /* 防止地图超出容器时溢出 */
  background-color: #f0f0f0;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  margin-top: 5px;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  padding: 2px 2px;
}

/* 地图容器：用于保持地图比例 */
.map-container {
  position: relative; /* 作为按钮绝对定位的参考 */
  height: 100%;
  width: 100%;
  min-height: 400px; /* 设置最小高度，确保地图有足够空间显示 */
}

/* 横向按钮组件：修改为绝对定位，基于 map-container 定位 */
.horizontal-button-component {
  position: absolute;
  top: 20px; /* 距离顶部的距离，可调整 */
  right: 20px; /* 改为右侧定位，更符合常见UI模式 */
  transform: none; /* 移除transform，因为我们现在使用right定位 */
  display: flex;
  gap: 8px; /* 增加按钮间距 */
  padding: 8px 12px; /* 增加内边距，使按钮组更大更明显 */
  background-color: rgba(255, 255, 255, 0.95); /* 增加不透明度，提高可见度 */
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15); /* 增强阴影效果 */
  z-index: 1000; /* 提高z-index，确保在地图控件之上 */
  border: 1px solid rgba(0, 0, 0, 0.1); /* 添加边框，提高可见度 */
}

/* 横向按钮样式（增强可见度和交互效果） */
.horizontal-btn {
  padding: 12px 18px; /* 增加内边距，使按钮更大 */
  border: none;
  border-radius: 6px;
  background-color: #fff;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500; /* 增加字体粗细 */
  white-space: nowrap;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1); /* 增强阴影 */
  transition: all 0.2s ease; /* 添加平滑过渡 */
  border: 1px solid rgba(0, 0, 0, 0.06); /* 添加细微边框 */

  &:hover {
    background-color: #f0f7ff;
    color: #1890ff;
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15); /* 增强悬停阴影 */
    border-color: #1890ff; /* 悬停时改变边框颜色 */
  }

  &:active {
    transform: translateY(0); /* 点击时恢复位置 */
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1); /* 点击时减小阴影 */
  }

  i {
    margin-right: 8px;
    font-size: 16px;
  }
}

/* 弹窗样式（完全未修改） */
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
  z-index: 999;
}

.modal-container {
  width: 500px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  overflow: hidden;
}

.modal-header {
  padding: 16px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #e5e7eb;
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.modal-body {
  padding: 16px;
}

.form-item {
  margin-bottom: 16px;
}

label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: #666;
}

input, select, textarea {
  width: 100%;
  padding: 8px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

textarea {
  min-height: 80px;
  resize: vertical;
}

.checkbox-group {
  display: flex;
  gap: 20px;
}

.checkbox-group label {
  display: flex;
  align-items: center;
  margin-bottom: 0;
}

.checkbox-group input {
  width: auto;
  margin-right: 4px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px;
  border-top: 1px solid #e5e7eb;
}

.modal-footer button {
  padding: 6px 16px;
}

.delete-btn {
  background-color: #f5222d;
  color: #fff;
  border-color: #f5222d;
}

.delete-btn:hover {
  background-color: #cf1322;
  border-color: #cf1322;
  color: #fff;
}
</style>
