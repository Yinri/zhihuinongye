<template>
  <div class="warning-container">
    <!-- 标题栏 -->
    <div class="warning-header">
      <div class="header-left">
        <span class="warning-icon">⚠️</span>
        <span class="header-title">农事预警信息</span>
        <span class="warning-count">{{ warningList.length }} 条预警</span>
      </div>
      <div class="header-right">
        <button class="refresh-btn" @click="handleRefresh" :disabled="loading">
          <span class="refresh-icon" :class="{ rotating: loading }">↻</span>
          <span class="refresh-text">{{ loading ? '加载中' : '刷新' }}</span>
        </button>
      </div>
    </div>

    <!-- 预警内容区（带滚动条） -->
    <div class="warning-content">
      <!-- 空状态 -->
      <div v-if="!loading && warningList.length === 0" class="empty-state">
        <div class="empty-icon">📋</div>
        <div class="empty-text">暂无预警信息</div>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner">⏳</div>
        <div class="loading-text">加载中...</div>
      </div>

      <!-- 预警列表 -->
      <div
        v-for="item in warningList"
        :key="item.id"
        class="warning-item"
        :class="{ 'high-risk': item.level === 'high' }"
      >
        <!-- 风险等级标识 -->
        <div class="risk-level" :class="item.level">
          {{ levelText[item.level] }}
        </div>

        <!-- 预警信息 -->
        <div class="warning-info">
          <div class="info-header">
            <div class="info-title" :title="item.title">
              {{ item.title }}
            </div>
            <div class="location-tags">
              <span v-if="item.baseName" class="base-tag">{{ item.baseName }}</span>
              <span v-if="item.plotName" class="plot-tag">{{ item.plotName }}</span>
            </div>
          </div>
          <div class="info-desc" :title="item.description">{{ item.description }}</div>
          <div class="info-meta">
            <!-- 状态标签 -->
            <span class="warning-status" :class="item.warningStatus">
              {{ getStatusText(item.warningStatus) }}
            </span>
            <span class="info-time">{{ formatTime(item.warningDate) }}</span>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="action-btns">
          <button class="handle-btn" @click="goToDetail(item)">
            去处理
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import { getWarningList, updateWarningStatus, type FarmingWarning } from '../api/warning.api';
import dayjs from 'dayjs';

// 预警等级文本映射
const levelText = {
  high: '高危',
  medium: '中危',
  low: '低危',
};

// 预警状态文本映射
const getStatusText = (status: string) => {
  const statusMap = {
    pending: '待处理',
    processing: '处理中',
    resolved: '已处理',
  };
  return statusMap[status] || status;
};

// 预警数据
const warningList = ref<FarmingWarning[]>([]);
const loading = ref(false);

// 路由实例
const router = useRouter();

// 加载预警数据
const loadWarnings = async () => {
  loading.value = true;
  try {
    // 查询待处理的预警
    const pendingData = await getWarningList({
      warningStatus: 'pending', // 查询待处理的预警
      onlyValid: true, // 只查询未过期的预警
      limit: 10,
    });
    
    // 只显示待处理的预警
    warningList.value = pendingData || [];
  } catch (error) {
    console.error('加载预警失败:', error);
    message.error('加载预警信息失败');
  } finally {
    loading.value = false;
  }
};

// 刷新预警数据
const handleRefresh = () => {
  loadWarnings();
};

// 格式化时间
const formatTime = (dateStr: string) => {
  if (!dateStr) return '';
  return dayjs(dateStr).format('YYYY-MM-DD HH:mm:ss');
};

// 处理预警
const handleWarning = async (item: FarmingWarning, action: string) => {
  try {
    await updateWarningStatus(item.id, action, undefined, undefined);
    message.success('操作成功');

    // 更新本地数据状态
    const index = warningList.value.findIndex((w) => w.id === item.id);
    if (index !== -1) {
      // 根据不同的操作更新状态
      if (action === 'resolved') {
        // 已处理状态，保留在列表中
        warningList.value[index].warningStatus = 'resolved';
      } else if (action === 'processing') {
        // 处理中状态，保留在列表中
        warningList.value[index].warningStatus = 'processing';
      } else if (action === 'pending') {
        // 待处理状态，保留在列表中
        warningList.value[index].warningStatus = 'pending';
      }
    }
  } catch (error) {
    console.error('处理预警失败:', error);
    message.error('操作失败');
  }
};

// 跳转到预警详情页
const goToDetail = async (item: FarmingWarning) => {
  try {
    // 点击"去处理"时，先将状态更新为"处理中"
    if (item.warningStatus === 'pending') {
      await updateWarningStatus(item.id, 'processing', undefined, undefined);
      
      // 更新本地数据状态
      const index = warningList.value.findIndex((w) => w.id === item.id);
      if (index !== -1) {
        warningList.value[index].warningStatus = 'processing';
      }
      
      message.success('预警已标记为处理中');
    }
    
    // 跳转到地块风险详情页面，传递地块ID
    if (item.plotId) {
      router.push(`/rapeseed/plot-risk-detail/${item.plotId}`);
    } else {
      message.error('无法获取地块信息，无法跳转到详情页');
    }
  } catch (error) {
    console.error('更新预警状态失败:', error);
    message.error('操作失败，请重试');
  }
};

// 组件挂载时加载数据
onMounted(() => {
  loadWarnings();
});

// 可选：设置定时刷新（每5分钟）
// setInterval(() => {
//   loadWarnings();
// }, 5 * 60 * 1000);
</script>

<style scoped>
.warning-container {
  width: 30%;
  border-radius: 8px;
  border: 1px solid #e8e8e8;
  background-color: #ffffff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  margin-top: 5px;
  transition: box-shadow 0.3s ease;
}

.warning-container:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

/* 标题栏样式 */
.warning-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  border-bottom: 1px solid #f0f0f0;
  background: linear-gradient(135deg, #fafafa 0%, #f5f5f5 100%);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-right {
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

.refresh-btn:hover:not(:disabled) {
  background-color: #fff0d9;
}

.refresh-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.refresh-icon {
  font-size: 12px;
}

.rotating {
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 内容区样式（带滚动条） */
.warning-content {
  max-height: 500px; /* 增加最大高度 */
  overflow-y: auto;
  padding: 12px 0;
  min-height: 200px;
  background: linear-gradient(to bottom, #ffffff, #fafafa);
}

/* 空状态样式 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #c9a56a;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
  opacity: 0.5;
}

.empty-text {
  font-size: 14px;
  color: #9c6b1c;
}

/* 加载状态样式 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.loading-spinner {
  font-size: 36px;
  margin-bottom: 12px;
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.loading-text {
  font-size: 14px;
  color: #9c6b1c;
}

/* 自定义滚动条 */
.warning-content::-webkit-scrollbar {
  width: 4px;
}

.warning-content::-webkit-scrollbar-track {
  background: #f0f0f0;
  border-radius: 2px;
}

.warning-content::-webkit-scrollbar-thumb {
  background: #bfbfbf;
  border-radius: 2px;
}

.warning-content::-webkit-scrollbar-thumb:hover {
  background: #8c8c8c;
}

/* 预警项样式 */
.warning-item {
  display: flex;
  align-items: flex-start;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  transition: all 0.2s;
  background-color: #ffffff;
}

.warning-item:last-child {
  border-bottom: none;
}

.warning-item:hover {
  background-color: #fafafa;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

/* 风险等级标识 */
.risk-level {
  min-width: 40px;
  height: 24px;
  line-height: 24px;
  text-align: center;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  margin-right: 12px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.risk-level.high {
  background-color: #fff2f0;
  color: #ff4d4f;
  border: 1px solid #ffccc7;
}

.risk-level.medium {
  background-color: #fffbe6;
  color: #faad14;
  border: 1px solid #ffe58f;
}

.risk-level.low {
  background-color: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

/* 预警信息区 */
.warning-info {
  flex-grow: 1;
  min-width: 0; /* 解决文字溢出问题 */
}

.info-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 6px;
}

.info-title {
  font-size: 14px;
  font-weight: 600;
  color: #854d0e;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.4;
  max-height: 2.8em;
  flex: 1;
  margin-right: 8px;
  word-break: break-word;
}

.location-tags {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}

.base-tag {
  display: inline-block;
  font-size: 11px;
  padding: 2px 6px;
  background-color: #f6ffed;
  color: #52c41a;
  border-radius: 4px;
  font-weight: normal;
  flex-shrink: 0;
}

.plot-tag {
  display: inline-block;
  font-size: 11px;
  padding: 2px 6px;
  background-color: #e6f7ff;
  color: #1890ff;
  border-radius: 4px;
  font-weight: normal;
  flex-shrink: 0;
}

.info-desc {
  font-size: 12px;
  color: #9c6b1c;
  line-height: 1.4;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.info-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-time {
  font-size: 11px;
  color: #c9a56a;
}

/* 操作按钮组 */
.action-btns {
  display: flex;
  gap: 6px;
  margin-left: 12px;
  flex-shrink: 0;
  flex-wrap: wrap;
  max-width: 180px;
  align-self: flex-start;
  margin-top: 2px;
}

.handle-btn,
.resolve-btn,
.processing-status,
.pause-btn,
.reopen-btn,
.resolved-status {
  padding: 4px 10px;
  font-size: 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid;
  font-weight: 500;
  white-space: nowrap;
}

.handle-btn {
  color: #e67700;
  background-color: #fff7e6;
  border-color: #ffd591;
}

.handle-btn:hover {
  background-color: #ffe7ba;
  color: #c25c00;
  border-color: #ffab00;
}

.resolve-btn {
  color: #52c41a;
  background-color: #f6ffed;
  border-color: #b7eb8f;
}

.resolve-btn:hover {
  background-color: #d9f7be;
  color: #389e0d;
  border-color: #95de64;
}

.processing-status {
  color: #0958d9;
  background-color: #e6f7ff;
  border-color: #91d5ff;
  cursor: default;
}

.pause-btn {
  color: #722ed1;
  background-color: #f9f0ff;
  border-color: #d3adf7;
}

.pause-btn:hover {
  background-color: #efdbff;
  color: #531dab;
  border-color: #b37feb;
}

.reopen-btn {
  color: #1890ff;
  background-color: #e6f7ff;
  border-color: #91d5ff;
}

.reopen-btn:hover {
  background-color: #bae7ff;
  color: #0958d9;
  border-color: #69c0ff;
}

.resolved-status {
  color: #389e0d;
  background-color: #f6ffed;
  border-color: #b7eb8f;
  cursor: default;
}

/* 状态标签样式 */
.warning-status {
  display: inline-flex;
  align-items: center;
  font-size: 11px;
  padding: 3px 8px;
  border-radius: 10px;
  font-weight: 500;
  margin-right: 8px;
}

.warning-status.pending {
  background-color: #fff7e6;
  color: #d46b08;
  border: 1px solid #ffd591;
}

.warning-status.processing {
  background-color: #e6f7ff;
  color: #0958d9;
  border: 1px solid #91d5ff;
}

.warning-status.resolved {
  background-color: #f6ffed;
  color: #389e0d;
  border: 1px solid #b7eb8f;
}

/* 高危项特殊样式 */
.high-risk {
  border-left: 3px solid #ff4d4f;
  margin-left: 3px;
  background-color: #fff2f0;
}

.high-risk:hover {
  background-color: #ffebe8;
}
</style>
