<template>
  <div class="data-collection-container">
    <div class="content-header">
      <h2>数据采集与对接管理</h2>
      <div class="content-actions">
        <button class="btn-refresh" @click="refreshData">🔄 刷新数据</button>
        <button class="btn-config">⚙️ 数据源配置</button>
      </div>
    </div>

    <div class="data-cards-grid">
      <!-- 卫星数据卡片 -->
      <div class="data-card" :class="cardHoverClass">
        <div class="card-icon satellite-icon">🛰️</div>
        <h3 class="card-title">卫星数据</h3>
        <p class="card-desc">高分辨率遥感影像与植被指数分析</p>
        <div class="card-status">
          <span class="status-indicator online"></span>
          <span class="status-text">实时同步中</span>
        </div>
        <div class="card-actions">
          <button class="view-btn">查看详情</button>
        </div>
      </div>

      <!-- 物联数据卡片 -->
      <div class="data-card" :class="cardHoverClass">
        <div class="card-icon iot-icon">🔌</div>
        <h3 class="card-title">物联数据</h3>
        <p class="card-desc">传感器网络实时监测数据采集</p>
        <div class="card-status">
          <span class="status-indicator online"></span>
          <span class="status-text">128个设备在线</span>
        </div>
        <div class="card-actions">
          <button class="view-btn">查看详情</button>
        </div>
      </div>

      <!-- 视频监控卡片 -->
      <div class="data-card" :class="cardHoverClass">
        <div class="card-icon video-icon">📹</div>
        <h3 class="card-title">视频监控</h3>
        <p class="card-desc">田间摄像头实时画面与录像回放</p>
        <div class="card-status">
          <span class="status-indicator online"></span>
          <span class="status-text">8路视频正常</span>
        </div>
        <div class="card-actions">
          <button class="view-btn">查看详情</button>
        </div>
      </div>

      <!-- 无人机采集卡片 -->
      <div class="data-card" :class="cardHoverClass">
        <div class="card-icon drone-icon">🚁</div>
        <h3 class="card-title">无人机采集</h3>
        <p class="card-desc">航拍影像与三维建模数据管理</p>
        <div class="card-status">
          <span class="status-indicator warning"></span>
          <span class="status-text">上次采集: 2小时前</span>
        </div>
        <div class="card-actions">
          <button class="view-btn">查看详情</button>
        </div>
      </div>

      <!-- 多光谱数据卡片 -->
      <div class="data-card" :class="cardHoverClass">
        <div class="card-icon multispectral-icon">🌈</div>
        <h3 class="card-title">多光谱数据</h3>
        <p class="card-desc">作物生长光谱特征分析数据</p>
        <div class="card-status">
          <span class="status-indicator online"></span>
          <span class="status-text">数据可用</span>
        </div>
        <div class="card-actions">
          <button class="view-btn">查看详情</button>
        </div>
      </div>

      <!-- 土壤数据卡片 -->
      <div class="data-card" :class="cardHoverClass">
        <div class="card-icon soil-icon">🌱</div>
        <h3 class="card-title">土壤数据</h3>
        <p class="card-desc">土壤养分与理化性质监测数据</p>
        <div class="card-status">
          <span class="status-indicator online"></span>
          <span class="status-text">32个监测点</span>
        </div>
        <div class="card-actions">
          <button class="view-btn">查看详情</button>
        </div>
      </div>

      <!-- 水分数据卡片 -->
      <div class="data-card" :class="cardHoverClass">
        <div class="card-icon moisture-icon">💧</div>
        <h3 class="card-title">水分数据</h3>
        <p class="card-desc">土壤墒情与作物水分状况监测</p>
        <div class="card-status">
          <span class="status-indicator online"></span>
          <span class="status-text">实时更新</span>
        </div>
        <div class="card-actions">
          <button class="view-btn">查看详情</button>
        </div>
      </div>

      <!-- 气象数据卡片 -->
      <div class="data-card" :class="cardHoverClass">
        <div class="card-icon weather-icon">🌤️</div>
        <h3 class="card-title">气象数据</h3>
        <p class="card-desc">田间小气候与区域气象数据集成</p>
        <div class="card-status">
          <span class="status-indicator online"></span>
          <span class="status-text">对接国家气象局</span>
        </div>
        <div class="card-actions">
          <button class="view-btn">查看详情</button>
        </div>
      </div>

      <!-- 病害数据卡片 -->
      <div class="data-card" :class="cardHoverClass">
        <div class="card-icon disease-icon">🦠</div>
        <h3 class="card-title">病害数据</h3>
        <p class="card-desc">病虫害发生情况与预测数据</p>
        <div class="card-status">
          <span class="status-indicator warning"></span>
          <span class="status-text">低风险预警</span>
        </div>
        <div class="card-actions">
          <button class="view-btn">查看详情</button>
        </div>
      </div>

      <!-- 历史数据卡片 -->
      <div class="data-card" :class="cardHoverClass">
        <div class="card-icon history-icon">📊</div>
        <h3 class="card-title">历史数据</h3>
        <p class="card-desc">历年生产数据与趋势分析</p>
        <div class="card-status">
          <span class="status-indicator online"></span>
          <span class="status-text">5年数据完整</span>
        </div>
        <div class="card-actions">
          <button class="view-btn">查看详情</button>
        </div>
      </div>
    </div>

    <div class="data-sources-status">
      <h3>数据源连接状态</h3>
      <div class="sources-table">
        <div class="table-header">
          <div class="table-col">数据源名称</div>
          <div class="table-col">连接状态</div>
          <div class="table-col">最后同步时间</div>
          <div class="table-col">操作</div>
        </div>
        <div class="table-row" v-for="source in dataSources" :key="source.id">
          <div class="table-col">{{ source.name }}</div>
          <div class="table-col"><span :class="source.status === 'online' ? 'status-online' : 'status-offline'">{{ source.status === 'online' ? '正常' : '断开' }}</span></div>
          <div class="table-col">{{ source.lastSync }}</div>
          <div class="table-col">
            <button class="action-btn" @click="testConnection(source.id)">测试连接</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

// 卡片悬停效果类
const cardHoverClass = ref('');

// 数据源状态模拟数据
const dataSources = ref([
  { id: 1, name: '卫星遥感系统', status: 'online', lastSync: '2025-08-26 08:30' },
  { id: 2, name: '物联网传感器网络', status: 'online', lastSync: '2025-08-27 09:45' },
  { id: 3, name: '田间视频监控系统', status: 'online', lastSync: '2025-08-27 10:10' },
  { id: 4, name: '无人机采集平台', status: 'online', lastSync: '2025-08-25 06:20' },
  { id: 5, name: '气象数据服务', status: 'online', lastSync: '2025-08-27 09:30' },
  { id: 6, name: '历史数据库', status: 'online', lastSync: '2025-08-26 00:00' }
]);

// 刷新数据
const refreshData = () => {
  alert('数据已刷新');
};

// 测试连接
const testConnection = (id: number) => {
  alert(`正在测试数据源 #${id} 连接...`);
};
</script>

<style scoped>
.data-collection-container {
  padding: 20px;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.content-header h2 {
  color: #2E7D32;
  font-size: 24px;
  margin: 0;
}

.content-actions {
  display: flex;
  gap: 12px;
}

.btn-refresh, .btn-config {
  padding: 10px 18px;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.25s ease;
}

.btn-refresh {
  background-color: #f0f7ee;
  color: #2E7D32;
}

.btn-config {
  background-color: #e3f2fd;
  color: #1976D2;
}

.btn-refresh:hover, .btn-config:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.data-cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 40px;
}

.data-card {
  background: white;
  border-radius: 16px;
  padding: 25px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
}

.data-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 20px rgba(0, 0, 0, 0.1);
}

.card-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-bottom: 15px;
}

.satellite-icon { background-color: #e8f5e9; color: #2E7D32; }
.iot-icon { background-color: #e0f7fa; color: #00695C; }
.video-icon { background-color: #fff3e0; color: #E65100; }
.drone-icon { background-color: #f3e5f5; color: #6A1B9A; }
.multispectral-icon { background-color: #e8eaf6; color: #3949AB; }
.soil-icon { background-color: #efebe9; color: #4E342E; }
.moisture-icon { background-color: #e3f2fd; color: #1565C0; }
.weather-icon { background-color: #fff8e1; color: #FF8F00; }
.disease-icon { background-color: #ffebee; color: #C62828; }
.history-icon { background-color: #f5f5f5; color: #424242; }

.card-title {
  font-size: 18px;
  color: #333;
  margin: 0 0 10px 0;
}

.card-desc {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
  margin: 0 0 20px 0;
  flex-grow: 1;
}

.card-status {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 15px;
}

.status-indicator {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.online {
  background-color: #4CAF50;
}

.warning {
  background-color: #FFC107;
}

.status-text {
  font-size: 13px;
  color: #666;
}

.card-actions {
  display: flex;
  justify-content: flex-end;
}

.view-btn {
  padding: 8px 16px;
  background-color: #f5f5f5;
  color: #333;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s ease;
}

.view-btn:hover {
  background-color: #e9e9e9;
}

.data-sources-status {
  background: white;
  border-radius: 16px;
  padding: 25px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.data-sources-status h3 {
  color: #333;
  margin: 0 0 20px 0;
  font-size: 18px;
}

.sources-table {
  width: 100%;
  border-radius: 12px;
  overflow: hidden;
}

.table-header {
  display: flex;
  background-color: #f5f5f5;
  padding: 12px 20px;
  font-weight: 600;
  font-size: 14px;
  border-bottom: 1px solid #eee;
}

.table-row {
  display: flex;
  padding: 15px 20px;
  font-size: 14px;
  border-bottom: 1px solid #f5f5f5;
  transition: background-color 0.2s ease;
}

.table-row:hover {
  background-color: #fafafa;
}

.table-col {
  flex: 1;
}

.table-col:nth-child(1) { flex: 2; }
.table-col:nth-child(4) { flex: 1; text-align: right; }

.status-online {
  color: #4CAF50;
}

.status-offline {
  color: #F44336;
}

.action-btn {
  padding: 6px 12px;
  background-color: #e3f2fd;
  color: #1976D2;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s ease;
}

.action-btn:hover {
  background-color: #bbdefb;
}

@media (max-width: 768px) {
  .data-cards-grid {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  }

  .table-header, .table-row {
    padding: 12px 15px;
  }

  .table-col:nth-child(3) {
    display: none;
  }
}
</style>