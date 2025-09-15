<script setup lang="ts">
import { ref } from 'vue';
import BaseLayout from './components/BaseLayout.vue';

// 基地选择相关（类型：string，确保与BaseLayout props匹配）
const selectedBase = ref('基地A');
const bases = ref<string[]>(['基地A', '基地B', '基地C']); // 显式声明string[]类型

// 阶段选择相关（类型：string，显式约束数组元素）
const stages = ref<string[]>(['未播种', '已播种', '苗期', '蕾薹期', '开花期', '角果成熟期', '收获与整地']);
const activeStage = ref(stages.value[0]); // 初始值为string，匹配props

// 菜单相关（显式声明string[]类型）
const menuItems = ref<string[]>([
  '生产计划',
  '作业面积',
  '油菜苗素质',
  '病虫害防控',
  '智慧灌溉',
  '追肥推荐',
  '冠层结构模拟',
  '倒伏风险预警',
  '收获与整地',
  '数据采集对接',
  '农事活动记录'
]);
const activeMenu = ref(menuItems.value[0]); // 初始值为string

// 登录状态（显式声明类型）
const showLogin = ref<boolean>(true);
const username = ref<string>('');
const password = ref<string>('');

// 处理基地选择变化（参数类型约束为string）
const handleBaseChange = (value: string) => {
  selectedBase.value = value;
  activeMenu.value = menuItems.value[0];
};

// 处理阶段选择变化（参数类型约束为string）
const handleStageChange = (value: string) => {
  activeStage.value = value;
};

// 处理菜单选择变化（参数类型约束为string）
const handleMenuChange = (value: string) => {
  activeMenu.value = value;
};

// 处理退出登录
const handleLogout = () => {
  showLogin.value = true;
  username.value = '';
  password.value = '';
};

// 模拟登录逻辑（参数可选，避免无参调用报错）
const handleLogin = () => {
  if (username.value.trim() && password.value.trim()) {
    showLogin.value = false;
  }
};
</script>

<template>
  <div class="app-container">
    <!-- 修正：用v-model:username替代手动@update，简化绑定且类型更安全 -->
    <BaseLayout
      :selected-base="selectedBase"
      :bases="bases"
      :stages="stages"
      :active-stage="activeStage"
      :menu-items="menuItems"
      :active-menu="activeMenu"
      :show-login="showLogin"
      v-model:username="username"
      v-model:password="password"
      @change-base="handleBaseChange"
      @change-stage="handleStageChange"
      @change-menu="handleMenuChange"
      @logout="handleLogout"
      @login="handleLogin"
    />
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

/* 基地选择器样式（原样式漏到全局，移回scoped内） */
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

/* 阶段导航 - 现代化按钮组 */
.stage-nav {
  display: flex;
  flex: 1; /* 占据所有剩余空间 */
  overflow-x: auto;
  overflow-y: hidden;
  height: 100%;
  scrollbar-width: thin;
  scrollbar-color: rgba(255, 255, 255, 0.2) transparent;
  padding: 15px 0;
  gap: 2px;
}

/* 隐藏滚动条但保留功能 */
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

/* 阶段按钮 - 现代卡片式设计 */
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

/* 按钮悬停效果 */
.stage-btn:hover {
  background: rgba(255, 255, 255, 0.18);
  border-color: rgba(255, 255, 255, 0.3);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* 活动状态按钮 */
.stage-btn.active {
  background: rgba(255, 255, 255, 0.25);
  border-color: rgba(255, 255, 255, 0.4);
  color: white;
  font-weight: 500;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

/* 活动按钮的发光效果 */
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

/* 退出按钮美化 */
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

/* 响应式设计 */
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
}

/* 主布局 - 与顶部导航栏协调 */
.main-layout {
  display: flex;
  flex: 1;
  overflow: hidden;
  background: #f8f9fa;
}

/* 左侧菜单 - 现代化侧边栏 */
.sidebar {
  width: 260px; /* 保持宽度 */
  background: linear-gradient(180deg, #2E7D32 0%, #1B5E20 100%);
  color: white;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 85px); /* 匹配新的顶部高度 */
  box-shadow: 2px 0 15px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
}

/* 侧边栏装饰效果 */
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

/* 美化侧边栏滚动条 */
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

/* 左侧菜单按钮 - 现代卡片式设计 */
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

/* 按钮悬停效果 */
.menu-btn:hover {
  background: rgba(255, 255, 255, 0.12);
  color: white;
  transform: translateX(5px);
}

/* 活动状态按钮 */
.menu-btn.active {
  background: rgba(255, 255, 255, 0.18);
  color: white;
  font-weight: 500;
}

/* 活动按钮的左侧装饰条 */
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

/* 主内容区 - 现代清爽设计 */
.main-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #f8f9fa;
  scrollbar-width: thin;
  scrollbar-color: rgba(0, 0, 0, 0.1) transparent;
}

/* 美化主内容区滚动条 */
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

/* 内容卡片 - 悬浮式设计 */
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

/* 模块占位图 - 现代卡片式设计 */
.module-placeholder::after {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  animation: shimmer 2s infinite;
  z-index: 1;
}
</style>