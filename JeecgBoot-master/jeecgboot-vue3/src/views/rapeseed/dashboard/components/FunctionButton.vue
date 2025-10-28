<script setup lang="ts">
import { ref } from 'vue';

// 弹窗显示控制
const showBaseInfoModal = ref(false);
const showEditBaseModal = ref(false);
const showPlotInfoModal = ref(false);
const showEditPlotModal = ref(false);
const showAddPlotModal = ref(false);

// 表单数据
const baseForm = ref({
  abbreviation: '基地a',
  fullName: '阳光现代农业示范基地',
  manager: '张三',
  phone: '138XXXX1234',
  address: '某某市某某区某某路 123 号',
  area: 500,
  crop: '中油杂19',
  soilType: ['黏土'] as string[]
});

const plotForm = ref({
  name: '地块A',
  code: 'PL-001',
  base: '基地a',
  area: 6.2,
  growthStage: '播种期',
  location: '基地东区 1 号区域，紧邻灌溉渠北侧'
});

const newPlot = ref({
  name: '',
  color: 'blue',
  opacity: 50,
  area: 0
});

// 表单提交方法
const submitBaseForm = () => {
  console.log('提交基地信息：', baseForm.value);
  showEditBaseModal.value = false;
};
const submitPlotForm = () => {
  console.log('提交地块信息：', plotForm.value);
  showEditPlotModal.value = false;
};
const saveNewPlot = () => {
  console.log('保存新增地块：', newPlot.value);
  showAddPlotModal.value = false;
};
const deletePlot = () => {
  console.log('删除地块');
  showAddPlotModal.value = false;
};
</script>

<template>
  <!-- 新增：图片与悬浮按钮的父容器 -->
  <div class="image-with-buttons">
    <div class="image-container">
    <!-- 悬浮按钮容器（原按钮组件，新增定位样式） -->
      <div class="horizontal-button-component">
        <button @click="showBaseInfoModal = true" class="horizontal-btn">
            <i class="icon-base"></i> 基地信息
          </button>
          <button @click="showEditBaseModal = true" class="horizontal-btn">
            <i class="icon-edit"></i> 修改基地信息
          </button>
          <button @click="showPlotInfoModal = true" class="horizontal-btn">
            <i class="icon-plot"></i> 地块信息
          </button>
          <button @click="showEditPlotModal = true" class="horizontal-btn">
            <i class="icon-edit"></i> 修改地块信息
          </button>
          <button @click="showAddPlotModal = true" class="horizontal-btn">
            <i class="icon-add"></i> 增加地块
          </button>
        </div>

        <!-- 新增：图片元素 -->
        <img
          src="../icons/image1.jpg"
        alt="基地/地块示意图"
        class="content-image"
        >
    </div>
  </div>
  <!-- 弹窗部分（完全未修改） -->
  <div v-if="showBaseInfoModal" class="modal-overlay" @click="showBaseInfoModal = false">
    <div class="modal-container" @click.stop>
      <div class="modal-header">基地信息</div>
      <div class="modal-body">
        <div class="form-item">
          <label>简称</label>
          <input type="text" :value="baseForm.abbreviation" disabled>
        </div>
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
          <label>种植作物</label>
          <input type="text" :value="baseForm.crop" disabled>
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

  <div v-if="showEditBaseModal" class="modal-overlay" @click="showEditBaseModal = false">
    <div class="modal-container" @click.stop>
      <div class="modal-header">修改基地信息</div>
      <div class="modal-body">
        <div class="form-item">
          <label>简称</label>
          <input type="text" v-model="baseForm.abbreviation" placeholder="基地a">
        </div>
        <div class="form-item">
          <label>全称</label>
          <input type="text" v-model="baseForm.fullName" placeholder="阳光现代农业示范基地">
        </div>
        <div class="form-item">
          <label>基地负责人</label>
          <input type="text" v-model="baseForm.manager" placeholder="张三">
        </div>
        <div class="form-item">
          <label>电话</label>
          <input type="text" v-model="baseForm.phone" placeholder="138XXXX1234">
        </div>
        <div class="form-item">
          <label>地址</label>
          <textarea v-model="baseForm.address" placeholder="某某市某某区某某路 123 号"></textarea>
        </div>
        <div class="form-item">
          <label>面积</label>
          <input type="number" v-model.number="baseForm.area" placeholder="500">
          <span>亩</span>
        </div>
        <div class="form-item">
          <label>种植作物</label>
          <select v-model="baseForm.crop">
            <option value="中油杂19">中油杂19</option>
            <option value="其他">其他</option>
          </select>
        </div>
        <div class="form-item">
          <label>土壤状况</label>
          <div class="checkbox-group">
            <label><input type="checkbox" v-model="baseForm.soilType" value="黏土"> 黏土</label>
            <label><input type="checkbox" v-model="baseForm.soilType" value="沙土"> 沙土</label>
            <label><input type="checkbox" v-model="baseForm.soilType" value="壤土"> 壤土</label>
          </div>
        </div>
      </div>
      <div class="modal-footer">
        <button @click="submitBaseForm">确定</button>
        <button @click="showEditBaseModal = false">返回</button>
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

  <div v-if="showEditPlotModal" class="modal-overlay" @click="showEditPlotModal = false">
    <div class="modal-container" @click.stop>
      <div class="modal-header">修改地块信息</div>
      <div class="modal-body">
        <div class="form-item">
          <label>地块名</label>
          <input type="text" v-model="plotForm.name" placeholder="地块A">
        </div>
        <div class="form-item">
          <label>地块编号</label>
          <input type="text" v-model="plotForm.code" placeholder="PL-001">
        </div>
        <div class="form-item">
          <label>所属基地</label>
          <input type="text" v-model="plotForm.base" placeholder="基地a">
        </div>
        <div class="form-item">
          <label>地块面积</label>
          <input type="number" v-model.number="plotForm.area" placeholder="6.2">
          <span>亩</span>
        </div>
        <div class="form-item">
          <label>生长阶段</label>
          <input type="text" v-model="plotForm.growthStage" placeholder="播种期">
        </div>
        <div class="form-item">
          <label>地块位置</label>
          <textarea v-model="plotForm.location" placeholder="基地东区 1 号区域，紧邻灌溉渠北侧"></textarea>
        </div>
      </div>
      <div class="modal-footer">
        <button @click="submitPlotForm">确定</button>
        <button @click="showEditPlotModal = false">返回</button>
      </div>
    </div>
  </div>

  <div v-if="showAddPlotModal" class="modal-overlay" @click="showAddPlotModal = false">
    <div class="modal-container" @click.stop>
      <div class="modal-header">增加地块</div>
      <div class="modal-body">
        <div class="form-item">
          <label>地块名</label>
          <input type="text" v-model="newPlot.name" placeholder="请输入">
        </div>
        <div class="form-item">
          <label>颜色</label>
          <select v-model="newPlot.color">
            <option value="blue">蓝色</option>
            <option value="green">绿色</option>
            <option value="red">红色</option>
          </select>
        </div>
        <div class="form-item">
          <label>透明度</label>
          <input type="range" v-model.number="newPlot.opacity" min="0" max="100" step="1">
          <span>{{ newPlot.opacity }}%</span>
        </div>
        <div class="form-item">
          <label>面积</label>
          <input type="number" v-model.number="newPlot.area" placeholder="请输入">
          <span>亩</span>
        </div>
      </div>
      <div class="modal-footer">
        <button @click="saveNewPlot">保存</button>
        <button @click="showAddPlotModal = false">取消</button>
        <button class="delete-btn" @click="deletePlot">删除</button>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">/* 新增：的父容器图片与按钮样式 */
.image-with-buttons {
  position: relative;
  width: 100%;
  height:100%;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden; /* 防止图片超出容器时溢出 */
  background-color: #fff7e6;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  margin-top: 5px;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  padding: 2px 2px;
}

/* 图片容器：用于保持图片比例 */
.image-container {
  position: relative; /* 作为按钮绝对定位的参考 */
  height:100%;
  max-width: 100%;
  max-height: 100%;
}

/* 新增：图片样式 */
.content-image {
  height: 100%;
  object-fit: contain; /* 确保图片完整显示，不拉伸，保持比例 */
  border-radius: 8px;
  display: block;

}

/* 横向按钮组件：修改为绝对定位，基于 image-container 定位 */
.horizontal-button-component {
  position: absolute;
  top: 5px; /* 距离顶部的距离，可调整 */
  left: 30%;
  transform: translateX(-50%);
  display: flex;
  gap: 5px;
  padding: 5px 5px;
  background-color: rgba(249, 249, 249, 0.85);
  border-radius: 8px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  z-index: 10;
}

/* 横向按钮样式（未修改核心样式） */
.horizontal-btn {
  padding: 10px 15px;
  border: none;
  border-radius: 6px;
  background-color: #fff;
  cursor: pointer;
  font-size: 14px;
  white-space: nowrap;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  transition: all 0.2s;

  &:hover {
    background-color: #f0f7ff;
    color: #1890ff;
    transform: translateY(-2px);
    box-shadow: 0 3px 8px rgba(0, 0, 0, 0.1);
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
