<template>
  <div class="input-tracking-container">
    <div class="section-header">
      <img src="../icons/icon4.svg" class="icon-menu"/> 投入进度跟踪
    </div>

    <!-- 种子投入项 -->
    <div class="tracking-item">
      <div class="item-title">
        <span>种子投入</span>
        <span class="target">目标：500kg</span>
      </div>

      <!-- 自动计算进度（拆分进度条：基础部分+超出部分） -->
      <div class="progress-bar">
        <!-- 基础进度（100%以内，绿色） -->
        <div class="bar base" :style="{ width: Math.min(autoSeedProgress, 100) + '%' }"></div>
        <!-- 超出进度（超过100%的部分，红色） -->
        <div class="bar exceed" v-if="autoSeedProgress > 100" :style="{ width: (autoSeedProgress - 100) + '%' }"></div>
        <span class="progress-text">{{ autoSeedProgress }}%</span>
      </div>
      <div class="auto-desc">
        <span>当前投入量：</span>
        <span class="auto-value">{{ autoSeedInput }}kg / 500kg</span>
      </div>

      <!-- 超目标警告 -->
      <div class="warning-tip" v-if="autoSeedProgress > 100 && !useManualSeed">
        <i class="icon-warning">⚠️</i> 已超出目标投入，请注意控制
      </div>

      <!-- 人工校准区域 -->
      <div class="manual-calibration">
        <label class="switch">
          <input type="checkbox" v-model="useManualSeed">
          <span class="slider"></span>
        </label>
        <span class="manual-label">启用人工校准</span>

        <div class="manual-input-group" v-if="useManualSeed">
          <input
            type="number"
            v-model="manualSeedInput"
            placeholder="输入实际投入量(kg)"
            min="0"
          />
          <button @click="calibrateSeed" class="calibrate-btn">应用校准</button>
        </div>
      </div>

      <div class="calibrated-result" v-if="useManualSeed">
        <span class="result-label">校准后进度：</span>
        <span class="result-value">{{ manualSeedProgress }}%</span>
        <span class="result-desc">({{ manualSeedInput }}kg / 500kg)</span>
      </div>
    </div>

    <!-- 肥料投入项 -->
    <div class="tracking-item">
      <div class="item-title">
        <span>肥料投入</span>
        <span class="target">目标：500kg</span>
      </div>

      <div class="progress-bar">
        <div class="bar base" :style="{ width: Math.min(autoFertilizerProgress, 100) + '%' }"></div>
        <div class="bar exceed" v-if="autoFertilizerProgress > 100" :style="{ width: (autoFertilizerProgress - 100) + '%' }"></div>
        <span class="progress-text">{{ autoFertilizerProgress }}%</span>
      </div>
      <div class="auto-desc">
        <span>当前投入量：</span>
        <span class="auto-value">{{ autoFertilizerInput }}kg / 500kg</span>
      </div>

      <div class="manual-calibration">
        <label class="switch">
          <input type="checkbox" v-model="useManualFertilizer">
          <span class="slider"></span>
        </label>
        <span class="manual-label">启用人工校准</span>

        <div class="manual-input-group" v-if="useManualFertilizer">
          <input
            type="number"
            v-model="manualFertilizerInput"
            placeholder="输入实际投入量(kg)"
            min="0"
          >
          <button @click="calibrateFertilizer" class="calibrate-btn">应用校准</button>
        </div>
      </div>

      <div class="calibrated-result" v-if="useManualFertilizer">
        <span class="result-label">校准后进度：</span>
        <span class="result-value">{{ manualFertilizerProgress }}%</span>
        <span class="result-desc">({{ manualFertilizerInput }}kg / 500kg)</span>
      </div>
    </div>

    <!-- 农药投入项 -->
    <div class="tracking-item">
      <div class="item-title">
        <span>农药投入</span>
        <span class="target">目标：500kg</span>
      </div>

      <div class="progress-bar">
        <div class="bar base" :style="{ width: Math.min(autoPesticideProgress, 100) + '%' }"></div>
        <div class="bar exceed" v-if="autoPesticideProgress > 100" :style="{ width: (autoPesticideProgress - 100) + '%' }"></div>
        <span class="progress-text">{{ autoPesticideProgress }}%</span>
      </div>
      <div class="auto-desc">
        <span>当前投入量：</span>
        <span class="auto-value">{{ autoPesticideInput }}kg / 500kg</span>
      </div>

      <div class="manual-calibration">
        <label class="switch">
          <input type="checkbox" v-model="useManualPesticide"/>
          <span class="slider"></span>
        </label>
        <span class="manual-label">启用人工校准</span>

        <div class="manual-input-group" v-if="useManualPesticide">
          <input
            type="number"
            v-model="manualPesticideInput"
            placeholder="输入实际投入量(kg)"
            min="0"
          >
          <button @click="calibratePesticide" class="calibrate-btn">应用校准</button>
        </div>
      </div>

      <div class="calibrated-result" v-if="useManualPesticide">
        <span class="result-label">校准后进度：</span>
        <span class="result-value">{{ manualPesticideProgress }}%</span>
        <span class="result-desc">({{ manualPesticideInput }}kg / 500kg)</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'InputTracking',
  data() {
    return {
      autoSeedInput: 550,
      useManualSeed: false,
      manualSeedInput: 0,
      manualSeedProgress: 0,

      autoFertilizerInput: 350,
      useManualFertilizer: false,
      manualFertilizerInput: 0,
      manualFertilizerProgress: 0,

      autoPesticideInput: 350,
      useManualPesticide: false,
      manualPesticideInput: 0,
      manualPesticideProgress: 0,

      target: 500
    }
  },
  computed: {
    autoSeedProgress() {
      return Math.round((this.autoSeedInput / this.target) * 100)
    },
    autoFertilizerProgress() {
      return Math.round((this.autoFertilizerInput / this.target) * 100)
    },
    autoPesticideProgress() {
      return Math.round((this.autoPesticideInput / this.target) * 100)
    }
  },
  methods: {
    calibrateSeed() {
      if (this.manualSeedInput >= 0) {
        this.manualSeedProgress = Math.round((this.manualSeedInput / this.target) * 100)
      }
    },
    calibrateFertilizer() {
      if (this.manualFertilizerInput >= 0) {
        this.manualFertilizerProgress = Math.round((this.manualFertilizerInput / this.target) * 100)
      }
    },
    calibratePesticide() {
      if (this.manualPesticideInput >= 0) {
        this.manualPesticideProgress = Math.round((this.manualPesticideInput / this.target) * 100)
      }
    }
  }
}
</script>

<style scoped>
.input-tracking-container {
  width: 50%;
  float: right;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  background-color: #fff;
  box-sizing: border-box;
  margin-left: auto;
  margin-top: 5px;
  padding: 8px 8px;
}

.section-header {
  display: flex;
  align-items: center;
  color: #333;
  font-weight: bold;
  margin-bottom: 20px;
}

.icon-menu {
  width: 18px;
  height: 18px;
  margin-right: 8px;
}

.tracking-item {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px dashed #f0f0f0;
}

.tracking-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.item-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.target {
  font-size: 13px;
  font-weight: normal;
  color: #666;
}

.progress-bar {
  height: 12px;
  width:80%;
  background-color: #f5f5f5;
  border-radius: 6px;
  overflow: hidden;
  position: relative;
  margin-bottom: 8px;
}

.bar {
  height: 100%;
  float: left;
  transition: width 0.3s ease;
}

/* 基础进度条（100%以内，绿色） */
.base {
  background-color: #52c41a;
}

/* 超出进度条（超过100%，红色） */
.exceed {
  background-color: #ff4d4f;
}

.progress-text {
  position: absolute;
  right: 6px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 12px;
  font-weight: bold;
  color:grey;
  text-shadow: 0 0 2px rgba(0, 0, 0, 0.2);
}

.auto-desc {
  font-size: 13px;
  color: #666;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
}

.auto-value {
  color: #333;
  margin-left: 4px;
  font-weight: 500;
}

.manual-calibration {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  flex-wrap: wrap;
  gap: 10px;
}

.switch {
  position: relative;
  display: inline-block;
  width: 40px;
  height: 20px;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ccc;
  transition: .3s;
  border-radius: 20px;
}

.slider:before {
  position: absolute;
  content: "";
  height: 16px;
  width: 16px;
  left: 2px;
  bottom: 2px;
  background-color: white;
  transition: .3s;
  border-radius: 50%;
}

input:checked + .slider {
  background-color: #1890ff;
}

input:checked + .slider:before {
  transform: translateX(20px);
}

.manual-label {
  font-size: 13px;
  color: #666;
}

.manual-input-group {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
}

.manual-input-group input {
  width: 120px;
  padding: 4px 8px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 13px;
}

.calibrate-btn {
  padding: 4px 12px;
  background-color: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.calibrate-btn:hover {
  background-color: #096dd9;
}

.calibrated-result {
  display: flex;
  align-items: center;
  font-size: 13px;
  margin-top: 6px;
  padding-top: 6px;
  border-top: 1px dashed #e8f3ff;
}

.result-label {
  color: #666;
}

.result-value {
  color: #1890ff;
  font-weight: 500;
  margin: 0 4px;
}

.result-desc {
  color: #888;
}

.warning-tip {
  display: flex;
  align-items: center;
  color: #ff4d4f;
  font-size: 13px;
  margin-top: 8px;
}

.icon-warning {
  margin-right: 4px;
}
</style>
