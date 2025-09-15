<script setup lang="ts">
import { ref } from 'vue';
import ProductionPlan from './ProductionPlan.vue';
import WorkArea from './WorkArea.vue';
import RapeseedSeedlingQuality from './RapeseedSeedlingQuality.vue';
import PestControl from './PestControl.vue';
import SmartIrrigation from './SmartIrrigation.vue';
import FertilizerRecommendation from './FertilizerRecommendation.vue';
import DataCollection from './DataCollection.vue';
import CanopyStructure from './CanopyStructure.vue';
import LodgingRisk from './LodgingRisk.vue';
import AgriculturalActivities from './AgriculturalActivities.vue';
import HarvestAndPreparation from './HarvestAndPreparation.vue';

// 1. 定义Props（类型严谨，无报错）
const props = defineProps<{
  selectedBase: string;
  bases: string[];
  stages: string[];
  activeStage: string;
  menuItems: string[];
  activeMenu: string;
  showLogin: boolean;
  username: string;
  password: string;
}>();

// 2. 声明所有需要的Emit事件
const emit = defineEmits<{
  'change-base': [value: string];
  'change-stage': [value: string];
  'change-menu': [value: string];
  logout: [];
  login: [];
  'update:username': [value: string];
  'update:password': [value: string];
}>();

// 3. 菜单图标映射（显式声明返回类型）
const getMenuIcon = (index: number): string => {
  const icons = ['📅', '🗺️', '🌱', '🐜', '💧', '🌿', '📊', '⚠️', '🚜', '🔌', '📝'];
  return icons[index] || '📋';
};

// 处理基地选择变化
const handleBaseChange = (value: string) => {
  emit('change-base', value);
  emit('change-menu', props.menuItems[0]); // 切换基地时默认选中第一个菜单
};
</script>

<template>
  <!-- 登录界面 -->
  <div v-if="showLogin" class="login-container">
    <div class="login-box">
      <div class="login-header">
        <div class="system-icon">🌾</div>
        <h2>油菜花生产管理系统</h2>
      </div>
      <div class="form-group">
        <label class="form-label">用户名</label>
        <input
          :value="username"
          @input="(event: Event) => {
            const target = event.target as HTMLInputElement;
            emit('update:username', target.value);
          }"
          type="text"
          placeholder="请输入用户名"
          class="form-input"
          autocomplete="off"
        />
      </div>
      <div class="form-group">
        <label class="form-label">密码</label>
        <input
          :value="password"
          @input="(event: Event) => {
            const target = event.target as HTMLInputElement;
            emit('update:password', target.value);
          }"
          type="password"
          placeholder="请输入密码"
          class="form-input"
          autocomplete="off"
        />
      </div>
      <button @click="emit('login')" class="login-btn">登录系统</button>
    </div>
  </div>

  <!-- 主控制台界面（v-else与登录界面互斥） -->
  <div v-else class="dashboard-container">
    <!-- 顶部导航栏 -->
    <header class="top-nav">
      <div class="nav-left">
        <div class="base-select">
          <span class="base-label">基地：</span>
          <select
            :value="selectedBase"
            class="base-selector"
            @change="(event: Event) => {
              const target = event.target as HTMLSelectElement;
              if (target) handleBaseChange(target.value);
            }"
          >
            <option v-for="base in bases" :key="base" :value="base">{{ base }}</option>
          </select>
        </div>

        <!-- 阶段导航 -->
        <nav class="stage-nav">
          <button
            v-for="(stage, index) in stages"
            :key="index"
            class="stage-btn"
            @click="emit('change-stage', stage)"
            :class="{ active: activeStage === stage }"
          >
            {{ stage }}
          </button>
        </nav>
      </div>
      <div class="nav-right">
        <button @click="emit('logout')" class="logout-btn">
          <i class="icon">🔙</i> 退出
        </button>
      </div>
    </header>

    <div class="main-layout">
      <!-- 左侧菜单 -->
      <aside class="sidebar">
        <div class="sidebar-header">
          <div class="sidebar-logo">🌾</div>
          <h3 class="sidebar-title">功能菜单</h3>
        </div>
        <ul class="menu-list">
          <li v-for="(item, index) in menuItems" :key="index" class="menu-item">
            <button
              @click="emit('change-menu', item)"
              :class="{ active: activeMenu === item }"
              class="menu-btn"
            >
              <i class="menu-icon">{{ getMenuIcon(index) }}</i>
              <span class="menu-text">{{ item }}</span>
            </button>
          </li>
        </ul>
      </aside>

      <!-- 主内容区（组件按需渲染） -->
      <main class="main-content">
        <div class="content-header">
          <h3 class="content-title">{{ activeMenu }}</h3>
          <p class="content-subtitle">
            基地：<span class="highlight">{{ selectedBase }}</span>
            | 阶段：<span class="highlight">{{ activeStage }}</span>
          </p>
        </div>
        <div class="content-body">
          <!-- 生产计划 -->
          <ProductionPlan v-if="activeMenu === '生产计划'" :active-menu="activeMenu" />
          <!-- 作业面积 -->
          <WorkArea v-else-if="activeMenu === '作业面积'" :active-menu="activeMenu" />
          <!-- 油菜苗素质 -->
          <RapeseedSeedlingQuality v-else-if="activeMenu === '油菜苗素质'" :active-menu="activeMenu" />
          <!-- 病虫害防控 -->
          <PestControl v-else-if="activeMenu === '病虫害防控'" :active-menu="activeMenu" />
          <!-- 智慧灌溉 -->
          <SmartIrrigation v-else-if="activeMenu === '智慧灌溉'" :active-menu="activeMenu" />
          <!-- 追肥推荐 -->
          <FertilizerRecommendation v-else-if="activeMenu === '追肥推荐'" />
          <!-- 冠层结构模拟 -->
          <CanopyStructure v-else-if="activeMenu === '冠层结构模拟'" :active-menu="activeMenu" />
          <!-- 倒伏风险预警 -->
          <LodgingRisk v-else-if="activeMenu === '倒伏风险预警'" :active-menu="activeMenu" />
          <!-- 收获与整地 -->
          <HarvestAndPreparation v-else-if="activeMenu === '收获与整地'" :active-menu="activeMenu" />
          <!-- 数据采集对接 -->
          <DataCollection v-else-if="activeMenu === '数据采集对接'" />
          <!-- 农事活动记录 -->
          <AgriculturalActivities v-else-if="activeMenu === '农事活动记录'" />
          <!-- 默认显示基地卫星图 -->
          <div v-else class="module-content">
            <h4 class="module-title">{{ selectedBase }} 基地卫星图</h4>
            <p class="module-desc">当前生长阶段：{{ activeStage }}</p>
            <div class="module-placeholder">
              <img :src="`https://dummyimage.com/1200x500/4CAF50/ffffff&text=${selectedBase}+基地卫星图`" alt="基地卫星图" class="placeholder-img" />
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
/* 全局样式 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  transition: all 0.25s ease;
}

.app-container {
  font-family: 'Microsoft YaHei', 'Segoe UI', sans-serif;
  min-height: 100vh;
  background-color: #f5f7fa;
  color: #333;
}

/* 登录界面样式 - 现代化设计 */
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #2E7D32 0%, #1B5E20 100%);
  padding: 20px;
  position: relative;
  overflow: hidden;
}

.login-container::before {
  content: '';
  position: absolute;
  top: -100px;
  right: -100px;
  width: 400px;
  height: 400px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 50%;
}

.login-container::after {
  content: '';
  position: absolute;
  bottom: -100px;
  left: -100px;
  width: 400px;
  height: 400px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 50%;
}

.login-box {
  background: rgba(255, 255, 255, 0.95);
  padding: 45px;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  width: 100%;
  max-width: 450px;
  text-align: center;
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.login-box:hover {
  transform: translateY(-5px);
  box-shadow: 0 25px 70px rgba(0, 0, 0, 0.25);
}

.login-box::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 6px;
  background: linear-gradient(90deg, #4CAF50, #8BC34A, #CDDC39);
}

.system-icon {
  font-size: 60px;
  margin-bottom: 20px;
  color: #4CAF50;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); }
  100% { transform: scale(1); }
}

.login-box h2 {
  color: #2E7D32;
  font-size: 26px;
  margin-bottom: 35px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.form-group {
  margin-bottom: 28px;
  text-align: left;
  position: relative;
}

.form-label {
  display: block;
  margin-bottom: 8px;
  color: #555;
  font-weight: 600;
  font-size: 14px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.form-input {
  width: 100%;
  padding: 14px 16px;
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  font-size: 15px;
  background-color: white;
  transition: all 0.3s ease;
}

.form-input:focus {
  outline: none;
  border-color: #4CAF50;
  box-shadow: 0 0 0 4px rgba(76, 175, 80, 0.15);
  background-color: white;
}

.login-btn {
  width: 100%;
  padding: 15px;
  background: linear-gradient(90deg, #4CAF50, #8BC34A);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(76, 175, 80, 0.3);
}

.login-btn:active {
  transform: translateY(0);
}

.login-btn::after {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  animation: shine 2s infinite;
}

@keyframes shine {
  0% { left: -100%; }
  100% { left: 100%; }
}

@media (max-width: 768px) {
  .login-box {
    padding: 35px 25px;
    margin: 0 15px;
  }
  
  .system-icon {
    font-size: 50px;
  }
  
  .login-box h2 {
    font-size: 22px;
  }
}

@media (max-width: 480px) {
  .login-box {
    padding: 30px 20px;
  }
  
  .system-icon {
    font-size: 45px;
  }
  
  .login-box h2 {
    font-size: 20px;
  }
}

/* 主控台样式 */
.dashboard-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

/* 顶部导航栏 */
.top-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  background: linear-gradient(135deg, #2E7D32 0%, #1B5E20 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.15);
  height: 85px;
  z-index: 10;
  position: relative;
  overflow: hidden;
}

.top-nav::after {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 300px;
  height: 300px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 50%;
  transform: translate(50%, -50%);
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 20px;
  width: 100%;
  height: 100%;
  position: relative;
  z-index: 2;
}

.base-select {
  display: flex;
  align-items: center;
  gap: 10px;
  white-space: nowrap;
  padding: 5px 10px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  backdrop-filter: blur(5px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.base-label {
  color: white;
  font-weight: 500;
  font-size: 16px;
  opacity: 0.9;
}

.base-selector {
  padding: 10px 15px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 6px;
  font-size: 16px;
  background: rgba(255, 255, 255, 0.15);
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
}

.base-selector:hover {
  background: rgba(255, 255, 255, 0.25);
  border-color: rgba(255, 255, 255, 0.4);
}

.base-selector:focus {
  outline: none;
  box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.3);
}

.base-selector option {
  background: #2E7D32;
  color: white;
}

/* 阶段导航 */
.stage-nav {
  display: flex;
  flex: 1;
  overflow-x: auto;
  overflow-y: hidden;
  height: 100%;
  scrollbar-width: thin;
  scrollbar-color: rgba(255, 255, 255, 0.2) transparent;
  padding: 15px 0;
  gap: 2px;
}

.stage-nav::-webkit-scrollbar {
  height: 4px;
}

.stage-nav::-webkit-scrollbar-track {
  background: transparent;
}

.stage-nav::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 2px;
}

.stage-btn {
  flex: 0 0 auto;
  padding: 12px 20px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  cursor: pointer;
  font-size: 15px;
  color: white;
  white-space: nowrap;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  min-width: 100px;
}

.stage-btn:hover {
  background: rgba(255, 255, 255, 0.18);
  border-color: rgba(255, 255, 255, 0.3);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stage-btn.active {
  background: rgba(255, 255, 255, 0.25);
  border-color: rgba(255, 255, 255, 0.4);
  color: white;
  font-weight: 500;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

.stage-btn.active::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  animation: shimmer 2s infinite;
}

@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

.nav-right {
  display: flex;
  align-items: center;
  height: 100%;
  position: relative;
  z-index: 2;
}

.logout-btn {
  padding: 12px 24px;
  background: rgba(244, 67, 54, 0.2);
  color: #ff8a80;
  border: 1px solid rgba(244, 67, 54, 0.3);
  border-radius: 8px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 500;
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
}

.logout-btn:hover {
  background: rgba(244, 67, 54, 0.3);
  color: #ff5252;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(244, 67, 54, 0.2);
}

@media (max-width: 1200px) {
  .nav-left {
    gap: 15px;
  }
  
  .stage-btn {
    font-size: 14px;
    padding: 10px 16px;
    min-width: 90px;
  }
}

@media (max-width: 768px) {
  .top-nav {
    padding: 0 15px;
    height: auto;
    min-height: 80px;
    flex-wrap: wrap;
  }
  
  .nav-left {
    flex-wrap: wrap;
    gap: 10px;
    justify-content: center;
    padding: 10px 0;
  }
  
  .stage-nav {
    justify-content: center;
    padding: 10px 0;
    width: 100%;
  }
  
  .nav-right {
    padding: 10px 0;
    justify-content: center;
  }
}

/* 主布局 */
.main-layout {
  display: flex;
  flex: 1;
  overflow: hidden;
  background: #f8f9fa;
}

/* 左侧菜单 */
.sidebar {
  width: 260px;
  background: linear-gradient(180deg, #2E7D32 0%, #1B5E20 100%);
  color: white;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 85px);
  box-shadow: 2px 0 15px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
}

.sidebar::before {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 300px;
  height: 300px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 50%;
  transform: translate(-50%, 50%);
  z-index: 0;
}

.sidebar-header {
  padding: 25px 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.03);
  position: relative;
  z-index: 1;
}

.sidebar-logo {
  font-size: 28px;
}

.sidebar-title {
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.menu-list {
  list-style: none;
  padding: 0;
  flex: 1;
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: rgba(255, 255, 255, 0.2) transparent;
  position: relative;
  z-index: 1;
}

.menu-list::-webkit-scrollbar {
  width: 5px;
}

.menu-list::-webkit-scrollbar-track {
  background: transparent;
}

.menu-list::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 10px;
}

.menu-item {
  margin-bottom: 0;
}

.menu-btn {
  width: 100%;
  padding: 18px 25px;
  background: transparent;
  border: none;
  color: #e0e0e0;
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 15px;
  text-align: left;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.menu-btn:hover {
  background: rgba(255, 255, 255, 0.12);
  color: white;
  transform: translateX(5px);
}

.menu-btn.active {
  background: rgba(255, 255, 255, 0.18);
  color: white;
  font-weight: 500;
}

.menu-btn.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  width: 4px;
  height: 100%;
  background: #8BC34A;
}

.menu-icon {
  width: 24px;
  text-align: center;
  font-size: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 主内容区 */
.main-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #f8f9fa;
  scrollbar-width: thin;
  scrollbar-color: rgba(0, 0, 0, 0.1) transparent;
}

.main-content::-webkit-scrollbar {
  width: 8px;
}

.main-content::-webkit-scrollbar-track {
  background: transparent;
}

.main-content::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.1);
  border-radius: 4px;
}

.main-content::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.15);
}

.content-header {
  margin-bottom: 20px;
  padding: 0 5px;
}

.content-title {
  color: #333;
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 8px;
  background: linear-gradient(90deg, #2E7D32, #4CAF50);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.content-subtitle {
  color: #666;
  font-size: 15px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.highlight {
  color: #2E7D32;
  font-weight: 600;
  background: rgba(46, 125, 50, 0.08);
  padding: 2px 8px;
  border-radius: 4px;
}

.content-body {
  background: white;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.06);
  min-height: calc(100% - 70px);
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.content-body:hover {
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.09);
}

/* 模块样式补充 */
.module-content {
  width: 100%;
}

.module-title {
  font-size: 18px;
  color: #333;
  margin-bottom: 10px;
  font-weight: 600;
}

.module-desc {
  font-size: 14px;
  color: #666;
  margin-bottom: 20px;
}

.module-placeholder {
  width: 100%;
  height: 400px;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f9f9f9;
  border-radius: 8px;
  position: relative;
  overflow: hidden;
}

.placeholder-img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .main-content {
    padding: 15px;
  }
  
  .content-title {
    font-size: 22px;
  }
  
  .content-body {
    padding: 20px;
  }
  
  .module-title {
    font-size: 18px;
  }
  
  .module-placeholder {
    height: 300px;
  }
}
</style>