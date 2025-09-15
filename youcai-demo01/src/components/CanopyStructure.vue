<template>
  <div class="canopy-structure-container">
    <div class="content-header">
      <h2>冠层结构模拟</h2>
      <div class="content-actions">
        <button class="btn-simulate" @click="runSimulation">▶️ 运行模拟</button>
        <button class="btn-reset" @click="resetSimulation">🔄 重置参数</button>
        <button class="btn-save" @click="saveSimulation">💾 保存结果</button>
      </div>
    </div>

    <div class="simulation-grid">
      <!-- 左侧参数控制面板 -->
      <div class="parameter-panel">
        <h3>模拟参数设置</h3>
        <div class="parameter-group">
          <label class="parameter-label">种植密度 (株/m²)</label>
          <div class="parameter-control">
            <input type="range" v-model.number="parameters.density" min="5" max="20" step="0.5">
            <span class="parameter-value">{{ parameters.density }} 株/m²</span>
          </div>
        </div>

        <div class="parameter-group">
          <label class="parameter-label">平均株高 (cm)</label>
          <div class="parameter-control">
            <input type="range" v-model.number="parameters.height" min="10" max="30" step="0.5">
            <span class="parameter-value">{{ parameters.height }} cm</span>
          </div>
        </div>

        <div class="parameter-group">
          <label class="parameter-label">叶面积指数</label>
          <div class="parameter-control">
            <input type="range" v-model.number="parameters.leafArea" min="1" max="6" step="0.1">
            <span class="parameter-value">{{ parameters.leafArea.toFixed(1) }}</span>
          </div>
        </div>

        <div class="parameter-group">
          <label class="parameter-label">生育期</label>
          <select v-model="parameters.growthStage" class="parameter-select">
            <option value="seedling">幼苗期</option>
            <option value="rosette">莲座期</option>
            <option value="bolting">抽薹期</option>
            <option value="flowering">开花期</option>
          </select>
        </div>

        <div class="parameter-group">
          <label class="parameter-label">土壤肥力水平</label>
          <select v-model="parameters.fertilizerLevel" class="parameter-select">
            <option value="low">低</option>
            <option value="medium">中</option>
            <option value="high">高</option>
          </select>
        </div>

        <div class="parameter-group">
          <label class="parameter-label">光照强度 (%)</label>
          <div class="parameter-control">
            <input type="range" v-model.number="parameters.light" min="30" max="100" step="5">
            <span class="parameter-value">{{ parameters.light }}%</span>
          </div>
        </div>
      </div>

      <!-- 右侧模拟结果展示 -->
      <div class="simulation-results">
        <div class="simulation-view">
          <h3>冠层3D结构模拟</h3>
          <div class="simulation-canvas-container">
            <canvas id="canopySimulationCanvas"></canvas>
            <div class="simulation-placeholder" v-if="!simulationRunning">
              <p>点击"运行模拟"按钮开始冠层结构模拟</p>
              <p>模拟将根据参数生成油菜冠层的3D结构可视化</p>
            </div>
          </div>
        </div>

        <div class="simulation-metrics">
          <h3>冠层特征指标</h3>
          <div class="metrics-grid">
            <div class="metric-card">
              <h4>光截获率</h4>
              <p class="metric-value">{{ metrics.lightInterception }}%</p>
              <p class="metric-desc">冠层吸收的光合有效辐射比例</p>
            </div>
            <div class="metric-card">
              <h4>通风系数</h4>
              <p class="metric-value">{{ metrics.ventilation }}</p>
              <p class="metric-desc">空气流通效率指标</p>
            </div>
            <div class="metric-card">
              <h4>群体光合速率</h4>
              <p class="metric-value">{{ metrics.photosynthesis }} μmol/m²/s</p>
              <p class="metric-desc">单位面积冠层光合能力</p>
            </div>
            <div class="metric-card">
              <h4>最佳种植密度</h4>
              <p class="metric-value">{{ metrics.optimalDensity }} 株/m²</p>
              <p class="metric-desc">基于当前参数的推荐密度</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="simulation-history">
      <h3>模拟历史记录</h3>
      <div class="history-table-container">
        <table class="history-table">
          <thead>
            <tr>
              <th>日期时间</th>
              <th>种植密度</th>
              <th>生育期</th>
              <th>光截获率</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(record, index) in simulationHistory" :key="index">
              <td>{{ record.timestamp }}</td>
              <td>{{ record.density }} 株/m²</td>
              <td>{{ formatGrowthStage(record.growthStage) }}</td>
              <td>{{ record.lightInterception }}%</td>
              <td>
                <button class="btn-load" @click="loadSimulation(index)">加载</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { Chart, ArcElement, Tooltip, Legend } from 'chart.js';
import * as THREE from 'three';
// 提示：若遇到类型声明缺失问题，可运行 `npm i --save-dev @types/three` 安装类型声明文件

// 注册Chart.js组件
Chart.register(ArcElement, Tooltip, Legend);

// 定义参数接口
interface Parameters {
  density: number;
  height: number;
  leafArea: number;
  growthStage: 'seedling' | 'rosette' | 'bolting' | 'flowering';
  fertilizerLevel: 'low' | 'medium' | 'high';
  light: number;
}

// 定义指标接口
interface Metrics {
  lightInterception: number;
  ventilation: number;
  photosynthesis: number;
  optimalDensity: number;
}

// 定义模拟历史记录接口
interface SimulationRecord {
  timestamp: string;
  density: number;
  growthStage: string;
  lightInterception: number;
  parameters: Parameters;
}

// 参数状态
const parameters = ref<Parameters>({
  density: 12.5,
  height: 20.0,
  leafArea: 3.5,
  growthStage: 'rosette',
  fertilizerLevel: 'medium',
  light: 75
});

// 指标状态
const metrics = ref<Metrics>({
  lightInterception: 0,
  ventilation: 0,
  photosynthesis: 0,
  optimalDensity: 0
});

// 模拟状态
const simulationRunning = ref<boolean>(false);
const simulationHistory = ref<SimulationRecord[]>([]);
const simulationCanvas = ref<HTMLCanvasElement | null>(null);
const threeScene = ref<any>(null);
const threeRenderer = ref<any>(null);
const threeCamera = ref<any>(null);
const animationId = ref<number | null>(null);

// 格式化生育期显示
const formatGrowthStage = (stage: string): string => {
  const stages = {
    'seedling': '幼苗期',
    'rosette': '莲座期',
    'bolting': '抽薹期',
    'flowering': '开花期'
  };
  return stages[stage as keyof typeof stages] || stage;
};

// 运行模拟
const runSimulation = () => {
  simulationRunning.value = true;

  // 基于参数计算指标（简化模型）
  // 光截获率模型：叶面积指数和密度的函数
  const lightInterception = Math.min(98, 30 + parameters.value.leafArea * 10 + parameters.value.density * 2);
  // 通风系数：密度和株高的函数
  const ventilation = Math.max(0.3, 2.5 - parameters.value.density * 0.1 - parameters.value.height * 0.02);
  // 光合速率：光截获率和光照的函数
  const photosynthesis = Math.round(lightInterception * parameters.value.light * 0.01 * 0.8);
  // 最佳密度：基于当前生育期的推荐值
  const optimalDensity = {
    'seedling': 18,
    'rosette': 15,
    'bolting': 12,
    'flowering': 10
  }[parameters.value.growthStage];

  // 更新指标
  metrics.value = {
    lightInterception: Math.round(lightInterception),
    ventilation: Number(ventilation.toFixed(2)),
    photosynthesis,
    optimalDensity
  };

  // 创建3D模拟
  create3DSimulation();

  // 记录模拟历史
  simulationHistory.value.unshift({
    timestamp: new Date().toLocaleString(),
    density: parameters.value.density,
    growthStage: parameters.value.growthStage,
    lightInterception: metrics.value.lightInterception,
    parameters: {...parameters.value}
  });

  // 限制历史记录数量
  if (simulationHistory.value.length > 10) simulationHistory.value.pop();
};

// 重置模拟参数
const resetSimulation = () => {
  parameters.value = {
    density: 12.5,
    height: 20.0,
    leafArea: 3.5,
    growthStage: 'rosette',
    fertilizerLevel: 'medium',
    light: 75
  };
  stop3DSimulation();
  simulationRunning.value = false;
};

// 保存模拟结果
const saveSimulation = () => {
  if (!simulationRunning.value) {
    alert('请先运行模拟');
    return;
  }
  // 在实际应用中，这里会将结果保存到服务器
  alert(`模拟结果已保存：\n光截获率: ${metrics.value.lightInterception}%\n最佳密度: ${metrics.value.optimalDensity}株/m²`);
};

// 加载历史模拟
const loadSimulation = (index: number) => {
  const record = simulationHistory.value[index];
  parameters.value = record.parameters;
  simulationRunning.value = true;
  runSimulation(); // 重新运行模拟
};

// 创建3D模拟场景
const create3DSimulation = () => {
  stop3DSimulation(); // 先停止现有模拟

  const canvas = document.getElementById('canopySimulationCanvas') as HTMLCanvasElement;
  if (!canvas) return;

  // 设置Three.js场景
  const scene = new THREE.Scene();
  scene.background = new THREE.Color(0xf5f5f5);

  // 创建相机
  threeCamera.value = new THREE.PerspectiveCamera(75, canvas.clientWidth / canvas.clientHeight, 0.1, 1000);
  threeCamera.value.position.z = 50;

  // 创建渲染器
  threeRenderer.value = new THREE.WebGLRenderer({ canvas, antialias: true });
  threeRenderer.value.setSize(canvas.clientWidth, canvas.clientHeight);

  // 添加灯光
  const ambientLight = new THREE.AmbientLight(0xffffff, 0.5);
  scene.add(ambientLight);

  const directionalLight = new THREE.DirectionalLight(0xffffff, 0.8);
  directionalLight.position.set(5, 10, 7.5);
  scene.add(directionalLight);

  // 根据参数创建冠层模型（简化的油菜植株）
  const createPlant = (x: number, z: number) => {
    // 茎
    const stemGeometry = new THREE.CylinderGeometry(0.2, 0.3, parameters.value.height / 5, 8);
    const stemMaterial = new THREE.MeshPhongMaterial({ color: 0x795548 });
    const stem = new THREE.Mesh(stemGeometry, stemMaterial);
    stem.position.y = parameters.value.height / 10;
    stem.position.x = x;
    stem.position.z = z;
    scene.add(stem);

    // 叶
    const leafGeometry = new THREE.SphereGeometry(parameters.value.leafArea / 3, 8, 8);
    const leafMaterial = new THREE.MeshPhongMaterial({ color: 0x4CAF50, transparent: true, opacity: 0.8 });

    // 创建多个叶子
    for (let i = 0; i < Math.floor(parameters.value.leafArea); i++) {
      const leaf = new THREE.Mesh(leafGeometry, leafMaterial);
      const angle = (i / Math.floor(parameters.value.leafArea)) * Math.PI * 2;
      const radius = parameters.value.height / 10;
      leaf.position.x = x + Math.cos(angle) * radius;
      leaf.position.y = parameters.value.height / 10 + Math.random() * parameters.value.height / 10;
      leaf.position.z = z + Math.sin(angle) * radius;
      scene.add(leaf);
    }
  };

  // 基于密度创建植物网格
  const gridSize = 10; // 模拟区域大小
  const spacing = 10 / Math.sqrt(parameters.value.density); // 植株间距

  for (let x = -gridSize/2; x < gridSize/2; x += spacing) {
    for (let z = -gridSize/2; z < gridSize/2; z += spacing) {
      createPlant(x + Math.random() * spacing * 0.5, z + Math.random() * spacing * 0.5);
    }
  }

  // 动画循环
  const animate = () => {
    animationId.value = requestAnimationFrame(animate);
    threeRenderer.value.render(scene, threeCamera.value);
  };

  animate();
};

// 停止3D模拟
const stop3DSimulation = () => {
  if (animationId.value) {
    cancelAnimationFrame(animationId.value);
  }
  if (threeRenderer.value) {
    threeRenderer.value.dispose();
  }
};

// 窗口大小变化时调整渲染器
const handleResize = () => {
  if (threeCamera.value && threeRenderer.value) {
    const canvas = document.getElementById('canopySimulationCanvas') as HTMLCanvasElement;
    if (canvas) {
      threeRenderer.value.setSize(canvas.clientWidth, canvas.clientHeight);
      threeCamera.value.aspect = canvas.clientWidth / canvas.clientHeight;
      threeCamera.value.updateProjectionMatrix();
    }
  }
};

onMounted(() => {
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  stop3DSimulation();
  window.removeEventListener('resize', handleResize);
});
</script>

<style scoped>
.canopy-structure-container {
  padding: 20px;
}

.simulation-grid {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 20px;
  margin: 20px 0;
}

.parameter-panel {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding: 20px;
}

.parameter-group {
  margin-bottom: 20px;
}

.parameter-label {
  display: block;
  margin-bottom: 8px;
  color: #333;
  font-weight: 500;
}

.parameter-control {
  display: flex;
  align-items: center;
  gap: 10px;
}

.parameter-control input[type="range"] {
  flex: 1;
}

.parameter-value {
  min-width: 50px;
  text-align: right;
  font-weight: 500;
}

.parameter-select {
  width: 100%;
  padding: 8px;
  border-radius: 6px;
  border: 1px solid #ddd;
}

.simulation-results {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.simulation-view {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding: 20px;
}

.simulation-canvas-container {
  width: 100%;
  height: 400px;
  position: relative;
}

#canopySimulationCanvas {
  width: 100%;
  height: 100%;
  border-radius: 8px;
}

.simulation-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-color: #f5f5f5;
  border-radius: 8px;
  color: #666;
  text-align: center;
  padding: 20px;
}

.simulation-metrics {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding: 20px;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 15px;
}

.metric-card {
  background-color: #f8f9fa;
  padding: 15px;
  border-radius: 10px;
}

.metric-value {
  font-size: 24px;
  font-weight: 700;
  color: #2E7D32;
  margin: 10px 0;
}

.metric-desc {
  font-size: 12px;
  color: #666;
}

.simulation-history {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding: 20px;
  margin-top: 20px;
}

.history-table-container {
  overflow-x: auto;
}

.history-table {
  width: 100%;
  border-collapse: collapse;
}

.history-table th,
.history-table td {
  padding: 12px 15px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.history-table th {
  background-color: #f5f7fa;
}

.btn-simulate,
.btn-reset,
.btn-save,
.btn-load {
  padding: 8px 12px;
  border-radius: 6px;
  border: none;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.btn-simulate {
  background-color: #4CAF50;
  color: white;
}

.btn-reset {
  background-color: #f5f5f5;
  color: #333;
}

.btn-save {
  background-color: #2196F3;
  color: white;
}

.btn-load {
  background-color: #e3f2fd;
  color: #1976D2;
}

@media (max-width: 768px) {
  .simulation-grid {
    grid-template-columns: 1fr;
  }

  .metrics-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>