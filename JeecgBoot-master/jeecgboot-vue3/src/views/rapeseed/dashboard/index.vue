<template>
    <div class="field-warning-panel">
      <!-- 警告信息区域 -->
      <div class="warning-list">
        <p class="warning-item">1.地块A蕾薹期需水量缺口20%。</p>
        <p class="warning-item">2.地块B苗期霜霉病风险中等。</p>
      </div>

      <!-- 功能按钮区域 -->
      <div class="button-group">
        <button @click="showBaseInfoModal = true">基地信息</button>
        <button @click="showEditBaseModal = true">修改基地信息</button>
        <button @click="showPlotInfoModal = true">地块信息</button>
        <button @click="showEditPlotModal = true">修改地块信息</button>
        <button @click="showAddPlotModal = true">增加地块</button>
      </div>
      <!-- 地块航拍图区域 -->
      <div class="field-aerial">
        <img src="../icons/image1.jpg" alt="地块航拍图" class="aerial-img">
        <!-- 地块A标记（可根据需求扩展为动态标记） -->
        <div class="field-marker" style="left: 22%; top: 55%;">地块A</div>
      </div>


    <!-- 基地信息弹窗 -->
    <div v-if="showBaseInfoModal" class="modal-overlay" @click="showBaseInfoModal = false">
      <div class="modal-container" @click.stop>
        <div class="modal-header">基地信息</div>
        <div class="modal-body">
          <div class="form-item">
            <label>简称</label>
            <input type="text" value="基地a" disabled>
          </div>
          <div class="form-item">
            <label>全称</label>
            <input type="text" value="阳光现代农业示范基地" disabled>
          </div>
          <div class="form-item">
            <label>基地负责人</label>
            <input type="text" value="张三" disabled>
          </div>
          <div class="form-item">
            <label>电话</label>
            <input type="text" value="138XXXX1234" disabled>
          </div>
          <div class="form-item">
            <label>地址</label>
            <textarea disabled>某某市某某区某某路 123 号</textarea>
          </div>
          <div class="form-item">
            <label>面积</label>
            <input type="text" value="500 亩" disabled>
          </div>
          <div class="form-item">
            <label>种植作物</label>
            <input type="text" value="中油杂19" disabled>
          </div>
          <div class="form-item">
            <label>土壤状况</label>
            <div class="checkbox-group">
              <label><input type="checkbox" checked disabled> 黏土</label>
              <label><input type="checkbox" disabled> 沙土</label>
              <label><input type="checkbox" disabled> 壤土</label>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="showBaseInfoModal = false">返回</button>
        </div>
      </div>
    </div>

    <!-- 修改基地信息弹窗 -->
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
            <input type="number" v-model="baseForm.area" placeholder="500">
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

    <!-- 地块信息弹窗 -->
    <div v-if="showPlotInfoModal" class="modal-overlay" @click="showPlotInfoModal = false">
      <div class="modal-container" @click.stop>
        <div class="modal-header">地块信息</div>
        <div class="modal-body">
          <div class="form-item">
            <label>地块名</label>
            <input type="text" value="地块A" disabled>
          </div>
          <div class="form-item">
            <label>地块编号</label>
            <input type="text" value="PL-001" disabled>
          </div>
          <div class="form-item">
            <label>所属基地</label>
            <input type="text" value="基地a" disabled>
          </div>
          <div class="form-item">
            <label>地块面积</label>
            <input type="text" value="6.2 亩" disabled>
          </div>
          <div class="form-item">
            <label>生长阶段</label>
            <input type="text" value="播种期" disabled>
          </div>
          <div class="form-item">
            <label>地块位置</label>
            <textarea disabled>基地东区 1 号区域，紧邻灌溉渠北侧</textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="showPlotInfoModal = false">返回</button>
        </div>
      </div>
    </div>

    <!-- 修改地块信息弹窗 -->
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
            <input type="number" v-model="plotForm.area" placeholder="6.2">
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

    <!-- 增加地块弹窗 -->
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
  </div>
</template>

<script>
export default {
  name: 'AgricultureSystem',
  data() {
    return {
      // 弹窗显示控制
      showBaseInfoModal: false,
      showEditBaseModal: false,
      showPlotInfoModal: false,
      showEditPlotModal: false,
      showAddPlotModal: false,

      // 基地信息表单
      baseForm: {
        abbreviation: '基地a',
        fullName: '阳光现代农业示范基地',
        manager: '张三',
        phone: '138XXXX1234',
        address: '某某市某某区某某路 123 号',
        area: 500,
        crop: '中油杂19',
        soilType: ['黏土']
      },

      // 地块信息表单
      plotForm: {
        name: '地块A',
        code: 'PL-001',
        base: '基地a',
        area: 6.2,
        growthStage: '播种期',
        location: '基地东区 1 号区域，紧邻灌溉渠北侧'
      },

      // 新增地块表单
      newPlot: {
        name: '',
        color: 'blue',
        opacity: 50,
        area: 0
      }
    }
  },
  methods: {
    // 提交基地信息表单
    submitBaseForm() {
      console.log('提交基地信息：', this.baseForm);
      this.showEditBaseModal = false;
    },

    // 提交地块信息表单
    submitPlotForm() {
      console.log('提交地块信息：', this.plotForm);
      this.showEditPlotModal = false;
    },

    // 保存新增地块
    saveNewPlot() {
      console.log('保存新增地块：', this.newPlot);
      this.showAddPlotModal = false;
    },

    // 删除地块
    deletePlot() {
      console.log('删除地块');
      this.showAddPlotModal = false;
    }
  }
}
</script>

<style scoped>
.button-group {
  display: flex;
  gap: 10px;
  margin: 20px 0;
}

button {
  padding: 8px 16px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  background-color: #fff;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

button:hover {
  border-color: #1890ff;
  color: #1890ff;
}

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
