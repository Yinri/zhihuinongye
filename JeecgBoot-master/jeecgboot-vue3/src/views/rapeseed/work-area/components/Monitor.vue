<template>
  <div class="sowing-monitor-header">
    <div class="left-section">
      <span class="module-title">播种实时监控</span>
      <div class="control-group">
        <div class="camera-switch">
          <img src="../icons/icon1.svg" class="camera-icon" alt="摄像头图标">
          <select>
            <option>摄像头切换</option>
            <option>摄像头1</option>
            <option>摄像头2</option>
          </select>
        </div>
        <div class="zoom-control">
          <button class="zoom-btn">+</button>
          <button class="zoom-btn">-</button>
          <span>画面缩放</span>
        </div>
        <button class="fullscreen-btn">
          全屏
          <img src="../icons/icon2.svg" class="fullscreen-icon" alt="全屏图标">
        </button>
      </div>
    </div>
    <div class="right-section">
      <div class="status-item">
        <span class="status-dot green"></span>
        <span>传输正常</span>
      </div>
      <div class="status-item">
        <span class="status-dot yellow"></span>
        <span>传输延迟</span>
      </div>
      <div class="time-info">
        最后识别：2025/09/22 10:08am
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SowingMonitorHeader'
}
</script>

<style scoped>
.sowing-monitor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  margin-top: 5px;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  padding: 8px 8px;
}

.left-section {
  display: flex;
  align-items: center;
  gap: 20px;
}

.module-title {
  font-size: 14px;
  font-weight: bold;
  color: #333;
}

.control-group {
  display: flex;
  align-items: center;
  gap: 16px;
}

.camera-switch {
  display: flex;
  align-items: center;
  gap: 4px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  padding: 4px 8px;
}

.camera-icon {
  width: 16px;
  height: 16px;
}

.camera-switch select {
  border: none;
  outline: none;
  background-color: transparent;
  font-size: 14px;
}

.zoom-control {
  display: flex;
  align-items: center;
  gap: 4px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  padding: 4px 8px;
}

.zoom-btn {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background-color: #f5f5f5;
  cursor: pointer;
  font-size: 12px;
  font-weight: bold;
}

.zoom-control span {
  font-size: 14px;
  color: #666;
}

.fullscreen-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  padding: 4px 8px;
  background-color: #fff;
  cursor: pointer;
  font-size: 14px;
}

.fullscreen-icon {
  width: 16px;
  height: 16px;
}

.right-section {
  display: flex;
  align-items: center;
  gap: 24px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #666;
}

.status-dot {
  display: inline-block;
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.green {
  background-color: #36d399;
}

.yellow {
  background-color: #fbbd23;
}

.time-info {
  font-size: 14px;
  color: #999;
}
</style>
