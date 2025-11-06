<template>
  <div class="yield-adjust-wrapper">
    <div class="yield-label">
      <img src="../icons/icon1.svg" class="icon-yield" alt="产量图标">目标产量
      <span class="yield-formula">（目标产量 = 前三年平均单产 × (1 + 递增率)）</span>
    </div>
    <button class="adjust-btn" @click="handleAdjust">
      <img src="../icons/icon2.svg" class="icon-pencil" alt="产量图标">
      调整
    </button>

    <!-- 弹窗组件 -->
    <div v-if="isModalShow" class="modal-overlay" @click="closeModal">
      <div class="modal-container" @click.stop>
        <!-- 弹窗头部（标题居中） -->
        <div class="modal-header">
          <h3>计划手动调整</h3>
          <button class="modal-close" @click="closeModal">×</button>
        </div>

        <!-- 弹窗内容区（表单字体加粗且靠右对齐） -->
        <div class="modal-body">
          <div class="form-item">
            <label>目标产量</label>
            <input type="number" placeholder="请输入目标产量">
            <button class="verify-btn">校验</button>
          </div>
          <div class="form-item">
            <label>肥料投入量</label>
            <input type="number" placeholder="请输入肥料投入量">
            <button class="verify-btn">校验</button>
          </div>
          <div class="form-item">
            <label>种子投入量</label>
            <input type="number" placeholder="请输入种子投入量">
            <button class="verify-btn">校验</button>
          </div>
          <div class="form-item">
            <label>农药投入量</label>
            <input type="number" placeholder="请输入农药投入量">
            <button class="verify-btn">校验</button>
          </div>
          <div class="form-item">
            <label>调整原因</label>
            <textarea placeholder="请输入调整原因" rows="4"></textarea>
          </div>
        </div>

        <!-- 弹窗底部按钮 -->
        <div class="modal-footer">
          <button class="submit-btn" @click="submitForm">提交审批</button>
          <button class="cancel-btn" @click="closeModal">返回</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      isModalShow: false // 控制弹窗显示/隐藏
    }
  },
  methods: {
    handleAdjust() {
      this.isModalShow = true;
      console.log('点击了调整目标产量');
    },
    closeModal() {
      this.isModalShow = false;
    },
    submitForm() {
      console.log('提交审批');
      this.closeModal();
    }
  }
};
</script>

<style scoped>
.yield-adjust-wrapper {
  margin-top: 5px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  padding: 8px 8px;
  background-color: #fff;
}

.yield-label {
  display: flex;
  align-items: center;
  color: #333;
  font-size: 14px;
  font-weight: bold;
}

.icon-yield {
  width: 16px;
  height: 16px;
  margin-right: 8px;
  fill: #2f5496;
}

.yield-formula {
  font-size: 12px;
  color: #666;
  font-weight: normal;
  margin-left: 6px;
}
.adjust-btn {
  display: flex;
  align-items: center;
  background-color: #fff;
  color: #22c55e;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  padding: 4px 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.adjust-btn:hover {
  background-color: #1890ff;
  color: #fff;
}

.icon-pencil {
  width: 14px;
  height: 14px;
  margin-right: 4px;
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

.modal-container {
  width: 500px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  overflow: hidden;
}

/* 弹窗头部 - 标题居中 */
.modal-header {
  display: flex;
  justify-content: center; /* 标题居中 */
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #e5e7eb;
  position: relative; /* 用于定位关闭按钮 */
}

.modal-header h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
  font-weight: bold;
}

/* 关闭按钮定位到右上角 */
.modal-close {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  background: transparent;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #999;
  transition: color 0.2s;
}

.modal-close:hover {
  color: #333;
}

/* 表单样式 - 字体加粗且靠右对齐输入框 */
.form-item {
  display: flex;
  align-items: center;
  margin-bottom: 18px;
}

.form-item label {
  width: 70px; /* 增加标签宽度，让文字更靠近输入框 */
  text-align: right; /* 标签文字靠右 */
  margin-right: 16px; /* 减少右侧间距，拉近与输入框的距离 */
  font-size: 14px;
  color: #333; /* 加深颜色 */
  font-weight: bold; /* 字体加粗 */
}

.form-item input,
.form-item textarea {
  flex: 1; /* 输入框占剩余空间，与标签右对齐 */
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
}

.form-item input:focus,
.form-item textarea:focus {
  border-color: #1890ff;
}

.form-item textarea {
  resize: vertical;
  min-height: 80px;
}

.verify-btn {
  margin-left: 10px;
  padding: 6px 12px;
  background-color: #f5f7fa;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.verify-btn:hover {
  background-color: #e8f3ff;
  border-color: #1890ff;
  color: #1890ff;
}

.modal-footer {
  display: flex;
  justify-content: center;
  padding: 16px;
  border-top: 1px solid #e5e7eb;
  gap: 16px;
}

.submit-btn {
  padding: 8px 24px;
  background-color: #22c55e;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.submit-btn:hover {
  background-color: #16a34a;
}

.cancel-btn {
  padding: 8px 24px;
  background-color: #fff;
  color: #666;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.cancel-btn:hover {
  background-color: #f5f5f5;
  border-color: #bbb;
}
</style>
