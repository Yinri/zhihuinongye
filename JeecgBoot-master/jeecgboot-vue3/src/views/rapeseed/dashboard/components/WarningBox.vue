<template>
  <div class="warning-container">
    <!-- 标题栏 -->
    <div class="warning-header">
      <div class="header-left">
        <span class="warning-icon">⚠️</span>
        <span class="header-title">农事预警信息</span>
        <span class="warning-count">{{ warningList.length }} 条预警</span>
      </div>
      <button class="refresh-btn" @click="handleRefresh">
        <span class="refresh-icon">↻</span>
        <span class="refresh-text">刷新</span>
      </button>
    </div>

    <!-- 预警内容区（带滚动条） -->
    <div class="warning-content">
      <div
        class="warning-item"
        :class="{ 'high-risk': item.level === 'high' }"
        v-for="(item, index) in warningList"
        :key="index"
      >
        <!-- 风险等级标识 -->
        <div class="risk-level" :class="item.level">
          {{ item.level === 'high' ? '高危' : '中危' }}
        </div>

        <!-- 预警信息 -->
        <div class="warning-info">
          <div class="info-title">{{ item.title }}</div>
          <div class="info-desc">{{ item.desc }}</div>
          <div class="info-time">{{ item.time }}</div>
        </div>

        <!-- 操作按钮 -->
        <button class="handle-btn">处理</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

// 预警数据结构定义
interface WarningItem {
  level: 'high' | 'medium'; // 风险等级：高危/中危
  title: string; // 预警标题
  desc: string; // 预警详情
  time: string; // 发生时间
}

// 模拟预警数据
const warningList = ref<WarningItem[]>([
  {
    level: 'high',
    title: '地块A需水量缺口',
    desc: '蕾薹期需水量缺口20%，当前土壤湿度18%，低于阈值25%',
    time: '2025-10-28 08:30:45'
  },
  {
    level: 'medium',
    title: '地块B病害风险',
    desc: '苗期霜霉病风险中等，近3日平均湿度85%，需加强通风',
    time: '2025-10-28 07:15:22'
  },
  {
    level: 'high',
    title: '地块C施肥预警',
    desc: '氮元素含量超标30%，可能导致作物徒长，建议暂停氮肥施用',
    time: '2025-10-27 16:42:10'
  },
  {
    level: 'medium',
    title: '气象灾害提示',
    desc: '未来24小时有小雨，需检查排水系统，防止地块积水',
    time: '2025-10-27 14:20:05'
  }
]);

// 刷新预警数据
const handleRefresh = () => {
  // 实际项目中可调用接口刷新数据
  console.log('刷新预警信息');
};
</script>

<style scoped>
.warning-container {
  width: 30%;
  border-radius: 8px;
  border: 1px solid #f5e6c8;
  background-color: #fffbf0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  margin-top: 5px;
}

/* 标题栏样式 */
.warning-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  border-bottom: 1px solid #f5e6c8;
  background-color: #fff7e6;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.warning-icon {
  font-size: 18px;
  color: #e67700;
}

.header-title {
  font-size: 15px;
  font-weight: 600;
  color: #c25c00;
}

.warning-count {
  font-size: 12px;
  color: #e67700;
  background-color: rgba(255, 235, 204, 0.6);
  padding: 2px 6px;
  border-radius: 12px;
}

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  font-size: 12px;
  color: #e67700;
  background-color: transparent;
  border: 1px solid #f5d6a3;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.refresh-btn:hover {
  background-color: #fff0d9;
}

.refresh-icon {
  font-size: 12px;
}

/* 内容区样式（带滚动条） */
.warning-content {
  max-height: 100%; /* 限制高度，超出滚动 */
  overflow-y: auto;
  padding: 8px 0;
}

/* 自定义滚动条 */
.warning-content::-webkit-scrollbar {
  width: 6px;
}

.warning-content::-webkit-scrollbar-track {
  background: #fff7e6;
  border-radius: 3px;
}

.warning-content::-webkit-scrollbar-thumb {
  background: #f5d6a3;
  border-radius: 3px;
}

.warning-content::-webkit-scrollbar-thumb:hover {
  background: #e6b879;
}

/* 预警项样式 */
.warning-item {
  display: flex;
  align-items: stretch;
  padding: 10px 16px;
  border-bottom: 1px dashed #f5e6c8;
  transition: background-color 0.2s;
}

.warning-item:last-child {
  border-bottom: none;
}

.warning-item:hover {
  background-color: rgba(255, 248, 230, 0.8);
}

/* 风险等级标识 */
.risk-level {
  min-width: 40px;
  height: 24px;
  line-height: 24px;
  text-align: center;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  margin-right: 12px;
  flex-shrink: 0;
}

.risk-level.high {
  background-color: #ffe3e3;
  color: #d93025;
  border: 1px solid #ffcdd2;
}

.risk-level.medium {
  background-color: #fff3cd;
  color: #e6a700;
  border: 1px solid #ffeeba;
}

/* 预警信息区 */
.warning-info {
  flex-grow: 1;
  min-width: 0; /* 解决文字溢出问题 */
}

.info-title {
  font-size: 14px;
  font-weight: 500;
  color: #854d0e;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.info-desc {
  font-size: 12px;
  color: #9c6b1c;
  line-height: 1.4;
  margin-bottom: 6px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.info-time {
  font-size: 11px;
  color: #c9a56a;
}

/* 处理按钮 */
.handle-btn {
  padding: 4px 10px;
  font-size: 12px;
  color: #e67700;
  background-color: transparent;
  border: 1px solid #f5d6a3;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  margin-left: 12px;
  flex-shrink: 0;
}

.handle-btn:hover {
  background-color: #fff0d9;
  color: #c25c00;
}

/* 高危项特殊样式 */
.high-risk {
  border-left: 3px solid #ff4d4f;
  margin-left: 3px;
}
</style>
