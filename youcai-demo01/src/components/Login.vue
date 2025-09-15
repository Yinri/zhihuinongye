<template>
  <div v-if="showLogin" class="login-container">
    <div class="login-box">
      <div class="login-header">
        <div class="system-icon">🌾</div>
        <h2>油菜花生产管理系统</h2>
      </div>
      <div class="form-group">
        <label class="form-label">用户名</label>
        <input
          v-model="username"
          type="text"
          placeholder="请输入用户名"
          class="form-input"
          autocomplete="off"
        />
      </div>
      <div class="form-group">
        <label class="form-label">密码</label>
        <input
          v-model="password"
          type="password"
          placeholder="请输入密码"
          class="form-input"
          autocomplete="off"
        />
      </div>
      <button @click="handleLogin" class="login-btn">登录系统</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const props = defineProps({
  showLogin: {
    type: Boolean,
    required: true
  }
});

const emits = defineEmits(['login', 'logout']);

const username = ref('');
const password = ref('');

const handleLogin = () => {
  if (username.value && password.value) {
    emits('login');
  }
};
</script>

<style scoped>
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

/* 背景装饰元素 */
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

/* 登录卡片 - 悬浮玻璃效果 */
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

/* 顶部渐变装饰条 */
.login-box::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 6px;
  background: linear-gradient(90deg, #4CAF50, #8BC34A, #CDDC39);
}

/* 系统图标动画 */
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

/* 标题样式 */
.login-box h2 {
  color: #2E7D32;
  font-size: 26px;
  margin-bottom: 35px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

/* 表单组样式 */
.form-group {
  margin-bottom: 28px;
  text-align: left;
  position: relative;
}

/* 标签样式 */
.form-label {
  display: block;
  margin-bottom: 8px;
  color: #555;
  font-weight: 600;
  font-size: 14px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

/* 输入框样式 */
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

/* 登录按钮样式 */
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

/* 按钮悬停效果 */
.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(76, 175, 80, 0.3);
}

/* 按钮点击效果 */
.login-btn:active {
  transform: translateY(0);
}

/* 按钮动画效果 */
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

/* 响应式设计 */
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
</style>