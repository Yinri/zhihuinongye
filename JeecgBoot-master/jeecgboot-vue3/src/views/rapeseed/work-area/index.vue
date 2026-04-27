<template>
  <div class="work-area-container">
    <a-row :gutter="16" style="height: calc(100vh - 180px)">
      <a-col :span="12" style="height: 100%">
        <a-card title="视频监控" class="card-panel video-card">
          <div class="video-grid" :class="`video-grid-${Math.min(videoDevices.length, 4)}`">
            <div v-for="device in videoDevices" :key="device.renderKey" class="video-monitor-card">
              <FlvPlayer
                :url="device.streamUrl"
                :title="device.equipmentName"
                class="video-item"
              />
              <PtzControlPanel
                class="video-control-panel"
                :device-code="device.equipmentCode"
                :channel-num="device.channelNum"
              />
            </div>
            <a-empty v-if="videoDevices.length === 0" description="暂无视频设备" />
          </div>
        </a-card>
      </a-col>
      <a-col :span="12" style="height: 100%">
        <a-card title="农机作业轨迹" class="card-panel track-card">
          <div class="track-controls">
            <a-row :gutter="12" align="middle">
              <a-col :span="7">
                <a-select
                  v-model:value="selectedMachineSn"
                  placeholder="选择农机"
                  style="width: 100%"
                  :not-found-content="machines.length === 0 ? '暂无农机数据' : undefined"
                >
                  <a-select-option
                    v-for="machine in machines"
                    :key="machine.beidouSn"
                    :value="machine.beidouSn"
                  >
                    {{ machine.brand }} {{ machine.model }} ({{ machine.vehicleNumber }})
                  </a-select-option>
                </a-select>
              </a-col>
              <a-col :span="5">
                <a-date-picker
                  v-model:value="selectedDate"
                  placeholder="选择日期"
                  style="width: 100%"
                />
              </a-col>
              <a-col :span="4">
                <a-time-picker
                  v-model:value="startTime"
                  placeholder="开始时间"
                  style="width: 100%"
                  format="HH:mm:ss"
                />
              </a-col>
              <a-col :span="4">
                <a-time-picker
                  v-model:value="endTime"
                  placeholder="结束时间"
                  style="width: 100%"
                  format="HH:mm:ss"
                />
              </a-col>
              <a-col :span="4">
                <a-button type="primary" @click="handleQueryTrack" :loading="trackLoading" style="width: 100%" :disabled="machines.length === 0">
                  查询
                </a-button>
              </a-col>
            </a-row>
          </div>
          <div class="map-container">
            <a-empty v-if="machines.length === 0" description="暂无农机数据" />
            <div v-else-if="trackLoading" class="map-loading">
              <a-spin size="large" tip="加载中..." />
            </div>
            <a-empty v-else-if="trackPoints.length === 0" description="暂无轨迹数据，请选择农机和时间后点击查询" />
            <div v-else ref="mapRef" class="tianditu-map"></div>
          </div>
          <div v-if="trackStats.qualifiedArea > 0" class="track-stats">
            <a-descriptions :column="1" bordered size="small">
              <a-descriptions-item label="合格面积">
                <span style="color: #52c41a; font-weight: bold">{{ trackStats.qualifiedArea }} 亩</span>
              </a-descriptions-item>
            </a-descriptions>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" style="margin-top: 16px">
      <a-col :span="24">
        <a-card title="作业记录列表">
          <template #extra>
            <a-range-picker
              v-model:value="recordDateRange"
              style="width: 280px"
              @change="onRecordDateChange"
            />
          </template>
          <a-table
            :columns="operationColumns"
            :data-source="operationRecords"
            :loading="loading"
            :pagination="{ pageSize: 10 }"
            row-key="id"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.dataIndex === 'startTime'">
                {{ record.startTime }}
              </template>
              <template v-else-if="column.dataIndex === 'endTime'">
                {{ record.endTime }}
              </template>
              <template v-else-if="column.dataIndex === 'qualifiedArea'">
                <span style="color: #52c41a; font-weight: bold">{{ record.qualifiedArea }} 亩</span>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, nextTick, onUnmounted } from 'vue';
import { message } from 'ant-design-vue';
import { useSelectStore } from '/@/store/selectStore';
import {
  getMachineList,
  getOperationRecords,
  getTrackData,
  getVideoDevices,
  getVideoStream,
} from './workArea.api';
import dayjs from 'dayjs';
import FlvPlayer from './components/FlvPlayer.vue';
import PtzControlPanel from './components/PtzControlPanel.vue';

const selectStore = useSelectStore();
const selectedBaseId = ref<string | number | undefined>(selectStore.selectedBase.baseId);
const selectedBaseName = ref<string>(selectStore.selectedBase.baseName || '');

const mapRef = ref<HTMLDivElement | null>(null);
let map: any = null;
let T: any = null;
let carTrack: any = null;
let validTrackPolyline: any = null;

const machines = ref<any[]>([]);
const selectedMachineSn = ref<string>('');
const selectedMachine = ref<any>(null);
const selectedDate = ref<dayjs.Dayjs>(dayjs());
const startTime = ref<dayjs.Dayjs>(dayjs().startOf('day'));
const endTime = ref<dayjs.Dayjs>(dayjs().endOf('day'));
const recordDateRange = ref<[dayjs.Dayjs, dayjs.Dayjs]>([
  dayjs().subtract(7, 'day'),
  dayjs(),
]);
const trackPoints = ref<any[]>([]);
const trackStats = reactive({
  qualifiedArea: 0,
});
const trackLoading = ref(false);

const videoDevices = ref<any[]>([]);
const MAX_VIDEO_STREAMS = 4;

const operationRecords = ref<any[]>([]);
const loading = ref(false);
let dataLoadToken = 0;

const operationColumns = [
  { title: '开始时间', dataIndex: 'startTime', width: 160 },
  { title: '结束时间', dataIndex: 'endTime', width: 160 },
  { title: '农机编号', dataIndex: 'vehicleNumber', width: 120 },
  { title: '合格面积', dataIndex: 'qualifiedArea', width: 120 },
  { title: '作业地点', dataIndex: 'location', width: 200 },
];

watch(
  () => [selectStore.selectedBase.baseId, selectStore.selectedBase.baseName],
  ([baseId, baseName]) => {
    selectedBaseId.value = baseId;
    selectedBaseName.value = baseName || '';
    resetBaseDependentState();
    if (baseId) {
      void loadData();
    }
  },
  { immediate: true }
);

onUnmounted(() => {
  stopTrackAnimation();
  if (map) {
    map.remove();
    map = null;
  }
});

async function loadData() {
  const baseId = selectedBaseId.value;
  const baseName = selectedBaseName.value;
  const currentToken = ++dataLoadToken;

  if (!baseId) {
    return;
  }

  const baseMachines = await loadMachines(baseId, baseName, currentToken);
  if (currentToken !== dataLoadToken || String(baseId) !== String(selectedBaseId.value || '')) {
    return;
  }

  await Promise.all([
    loadVideoDevices(baseId, currentToken),
    loadOperationRecords(baseMachines, currentToken),
  ]);
}

function resetBaseDependentState() {
  stopTrackAnimation();
  videoDevices.value = [];
  machines.value = [];
  selectedMachineSn.value = '';
  selectedMachine.value = null;
  operationRecords.value = [];
  trackPoints.value = [];
  trackStats.qualifiedArea = 0;
  loading.value = false;
  trackLoading.value = false;
}

function getMachineBaseId(machine: any) {
  return machine?.baseId ?? machine?.jdBaseId ?? machine?.baseid ?? machine?.base?.id ?? '';
}

function getMachineBaseName(machine: any) {
  return machine?.baseName ?? machine?.baseFullName ?? machine?.base?.baseName ?? '';
}

function matchesMachineBase(machine: any, baseId: string | number, baseName: string) {
  const machineBaseId = getMachineBaseId(machine);
  if (machineBaseId !== '' && String(machineBaseId) === String(baseId)) {
    return true;
  }

  const currentBaseKeyword = extractBaseKeyword(baseName);
  const machineBaseKeyword = extractBaseKeyword(getMachineBaseName(machine));
  return Boolean(currentBaseKeyword && machineBaseKeyword && currentBaseKeyword === machineBaseKeyword);
}

async function loadMachines(baseId: string | number, baseName: string, currentToken: number) {
  if (!baseId) {
    machines.value = [];
    return [];
  }

  try {
    const res = await getMachineList();
    const allMachines = res || [];
    const filteredMachines = allMachines.filter((machine: any) =>
      matchesMachineBase(machine, baseId, baseName)
    );

    if (currentToken !== dataLoadToken || String(baseId) !== String(selectedBaseId.value || '')) {
      return [];
    }

    machines.value = filteredMachines;
    if (filteredMachines.length > 0) {
      selectedMachineSn.value = filteredMachines[0].beidouSn;
      selectedMachine.value = filteredMachines[0];
    } else {
      selectedMachineSn.value = '';
      selectedMachine.value = null;
    }
    return filteredMachines;
  } catch (e) {
    console.error('加载农机列表失败', e);
    if (currentToken === dataLoadToken) {
      machines.value = [];
      selectedMachineSn.value = '';
      selectedMachine.value = null;
    }
    return [];
  }
}

function extractBaseKeyword(baseName: string) {
  if (!baseName) {
    return '';
  }
  let normalized = baseName.trim();
  normalized = normalized.replace(/基地$/, '');
  const separators = ['镇', '乡', '街道'];
  for (const separator of separators) {
    const index = normalized.indexOf(separator);
    if (index > 0) {
      return normalized.substring(0, index);
    }
  }
  return normalized;
}

async function loadVideoDevices(baseId = selectedBaseId.value, currentToken = dataLoadToken) {
  if (!baseId) {
    videoDevices.value = [];
    return;
  }

  try {
    const res = await getVideoDevices(String(baseId));
    const devices = (res || []).map((device: any) => ({
      ...device,
      channelNum: String(device.channelNum || '1'),
      streamUrl: '',
      renderKey: `${baseId}-${device.equipmentCode}-${device.channelNum || '1'}`,
    }));
    const limitedDevices = devices.slice(0, MAX_VIDEO_STREAMS);

    if (currentToken !== dataLoadToken || String(baseId) !== String(selectedBaseId.value || '')) {
      return;
    }

    videoDevices.value = limitedDevices;
    await Promise.allSettled(
      videoDevices.value.map(async (device: any, index: number) => {
        try {
          const streamUrl = await getVideoStream(device.equipmentCode, device.channelNum);
          if (currentToken !== dataLoadToken || String(baseId) !== String(selectedBaseId.value || '')) {
            return;
          }
          videoDevices.value[index] = {
            ...videoDevices.value[index],
            streamUrl: streamUrl || '',
          };
        } catch (e) {
          console.error(`加载设备 ${device.equipmentName} 视频流失败`, e);
          if (currentToken !== dataLoadToken || String(baseId) !== String(selectedBaseId.value || '')) {
            return;
          }
          videoDevices.value[index] = {
            ...videoDevices.value[index],
            streamUrl: '',
          };
        }
      })
    );
  } catch (e) {
    console.error('加载视频设备失败', e);
    if (currentToken === dataLoadToken) {
      videoDevices.value = [];
    }
  }
}

async function loadOperationRecords(baseMachines = machines.value, currentToken = dataLoadToken) {
  if (!selectedBaseId.value || !recordDateRange.value || !recordDateRange.value[0] || !recordDateRange.value[1]) {
    operationRecords.value = [];
    trackStats.qualifiedArea = 0;
    return;
  }
  if (baseMachines.length === 0) {
    operationRecords.value = [];
    trackStats.qualifiedArea = 0;
    return;
  }

  loading.value = true;
  try {
    const beidouSnList = baseMachines.map((m) => m.beidouSn);
    const startDate = recordDateRange.value[0].format('YYYY-MM-DD');
    const endDate = recordDateRange.value[1].format('YYYY-MM-DD');

    const res = await getOperationRecords(beidouSnList, startDate, endDate);
    if (currentToken !== dataLoadToken) {
      return;
    }
    operationRecords.value = res || [];
    calculateTrackStats();
  } catch (e) {
    console.error('加载作业记录失败', e);
    if (currentToken === dataLoadToken) {
      operationRecords.value = [];
      trackStats.qualifiedArea = 0;
    }
  } finally {
    if (currentToken === dataLoadToken) {
      loading.value = false;
    }
  }
}

async function loadTrackData() {
  if (!selectedMachineSn.value || !selectedDate.value) {
    message.warning('请选择农机和日期');
    return;
  }
  stopTrackAnimation();
  trackLoading.value = true;
  try {
    const dateStr = selectedDate.value.format('YYYY-MM-DD');
    const startStr = startTime.value ? startTime.value.format('HH:mm:ss') : '00:00:00';
    const endStr = endTime.value ? endTime.value.format('HH:mm:ss') : '23:59:59';
    
    const res = await getTrackData(
      selectedMachineSn.value,
      `${dateStr} ${startStr}`,
      `${dateStr} ${endStr}`
    );
    
    let rawData = res || [];
    
    if (rawData.length === 0) {
      console.log('未获取到轨迹数据，使用模拟数据');
      rawData = generateMockTrackData();
    }
    
    trackPoints.value = rawData.map((item: any) => ({
      lng: parseFloat(item.t1),
      lat: parseFloat(item.t2),
      time: item.t3,
      depth: parseFloat(item.t5) || 0,
      direction: parseFloat(item.t6) || 0,
      speed: parseFloat(item.t7) || 0,
      isValid: item.workflag === '1',
    }));
    
    trackLoading.value = false;
    await nextTick();
    await nextTick();
    await drawTrack();
  } catch (e) {
    console.error('加载轨迹数据失败', e);
    console.log('使用模拟数据展示轨迹');
    trackPoints.value = generateMockTrackData().map((item: any) => ({
      lng: parseFloat(item.t1),
      lat: parseFloat(item.t2),
      time: item.t3,
      depth: parseFloat(item.t5) || 0,
      direction: parseFloat(item.t6) || 0,
      speed: parseFloat(item.t7) || 0,
      isValid: item.workflag === '1',
    }));
    trackLoading.value = false;
    await nextTick();
    await nextTick();
    await drawTrack();
  }
}

function handleQueryTrack() {
  selectedMachine.value = machines.value.find(m => m.beidouSn === selectedMachineSn.value) || null;
  loadTrackData();
}

function generateMockTrackData() {
  const baseLng = 112.637;
  const baseLat = 31.290;
  const data = [];
  const now = dayjs();
  
  for (let i = 0; i < 50; i++) {
    const offsetLng = (Math.random() - 0.5) * 0.01;
    const offsetLat = (Math.random() - 0.5) * 0.01;
    data.push({
      t1: (baseLng + offsetLng + i * 0.0002).toFixed(7),
      t2: (baseLat + offsetLat + i * 0.0001).toFixed(7),
      t3: now.subtract(i * 3, 'second').format('YYYY-MM-DD HH:mm:ss'),
      t4: '0',
      t5: String(Math.floor(Math.random() * 40)),
      t6: (Math.random() * 360).toFixed(1),
      t7: (10 + Math.random() * 10).toFixed(2),
      t8: '0',
      t9: selectedMachine.value?.beidouSn || '10BA7B69',
      workflag: Math.random() > 0.1 ? '1' : '0',
    });
  }
  return data;
}

function calculateTrackStats() {
  let qualifiedArea = 0;
  operationRecords.value.forEach((record) => {
    if (record.qualifiedArea) {
      qualifiedArea += parseFloat(record.qualifiedArea);
    }
  });
  trackStats.qualifiedArea = parseFloat(qualifiedArea.toFixed(2));
}

function onRecordDateChange() {
  void loadOperationRecords();
}

function loadTiandituAPI() {
  return new Promise((resolve, reject) => {
    if (window.T) {
      T = window.T;
      resolve(T);
      return;
    }

    const script = document.createElement('script');
    script.type = 'text/javascript';
    script.src = 'https://api.tianditu.gov.cn/api?v=4.0&tk=46fdb68b960da6af775e3287cae51e81';
    script.onload = () => {
      T = window.T;
      resolve(T);
    };
    script.onerror = () => {
      reject(new Error('Failed to load Tianditu API'));
    };
    document.head.appendChild(script);
  });
}

function loadD3Script() {
  return new Promise((resolve, reject) => {
    if (window.d3) {
      console.log('d3已加载');
      resolve(true);
      return;
    }
    const script = document.createElement('script');
    script.src = 'https://d3js.org/d3.v3.min.js';
    script.onload = () => {
      console.log('d3.js加载成功');
      resolve(true);
    };
    script.onerror = () => reject(new Error('Failed to load d3.js'));
    document.head.appendChild(script);
  });
}

function loadD3OverlayScript() {
  return new Promise((resolve, reject) => {
    if (window.T && window.T.D3Overlay) {
      console.log('D3Overlay已加载');
      resolve(true);
      return;
    }
    const script = document.createElement('script');
    script.src = 'http://lbs.tianditu.gov.cn/api/js4.0/opensource/openlibrary/D3SvgOverlay.min.js';
    script.onload = () => {
      console.log('D3SvgOverlay加载成功');
      resolve(true);
    };
    script.onerror = () => reject(new Error('Failed to load D3SvgOverlay.js'));
    document.head.appendChild(script);
  });
}

function loadOpenSourceScript() {
  return new Promise((resolve, reject) => {
    if (window.T && window.T.CarTrack) {
      resolve(true);
      return;
    }
    const script = document.createElement('script');
    script.src = 'http://lbs.tianditu.gov.cn/api/js4.0/opensource/openlibrary/CarTrack.min.js';
    script.onload = () => {
      console.log('天地图开源扩展库加载成功');
      resolve(true);
    };
    script.onerror = () => reject(new Error('Failed to load Tianditu CarTrack.js'));
    document.head.appendChild(script);
  });
}

async function drawTrack() {
  if (trackPoints.value.length === 0) {
    return;
  }

  if (!map) {
    await nextTick();
    if (!mapRef.value) {
      console.error('地图容器不存在');
      return;
    }
    try {
      await loadTiandituAPI();
      await loadD3Script();
      await loadD3OverlayScript();
      await loadOpenSourceScript();

      map = new T.Map(mapRef.value);
      map.setMapType(window.TMAP_SATELLITE_MAP);

      const base = selectStore.selectedBase;
      if (base && base.longitude && base.latitude) {
        map.centerAndZoom(new T.LngLat(base.longitude, base.latitude), 15);
      } else {
        map.centerAndZoom(new T.LngLat(112.5, 31.3), 10);
      }
    } catch (e) {
      console.error('初始化地图失败', e);
      return;
    }
  }

  if (!T.CarTrack) {
    console.error('CarTrack类未加载，请确保天地图开源扩展库已正确加载');
    return;
  }

  if (carTrack) {
    carTrack.clear();
    carTrack = null;
  }
  if (validTrackPolyline) {
    map.removeOverLay(validTrackPolyline);
    validTrackPolyline = null;
  }

  const allPoints = trackPoints.value.map((p) => new T.LngLat(p.lng, p.lat));
  const validPoints = trackPoints.value.filter((p) => p.isValid).map((p) => new T.LngLat(p.lng, p.lat));

  console.log('轨迹点数量:', allPoints.length, '有效点数量:', validPoints.length);

  if (allPoints.length > 0) {
    const firstPoint = trackPoints.value[0];
    map.centerAndZoom(new T.LngLat(firstPoint.lng, firstPoint.lat), 15);
  }

  if (validPoints.length > 0) {
    validTrackPolyline = new T.Polyline(validPoints, {
      color: '#52c41a',
      weight: 5,
      opacity: 0.9,
    });
    map.addOverLay(validTrackPolyline);
  }

  const carTrackOptions = {
    interval: 25,
    speed: 1,
    dynamicLine: true,
    Datas: allPoints,
    polylinestyle: {
      color: '#1890ff',
      width: 6,
      opacity: 0.9
    },
    arrowStyle: {
      display: true,
      offset: 30,
      repeat: 60,
      pixelSize: 5,
      headAngle: 75,
      color: '#fff',
      weight: 3
    },
    passedLineColor: '#1890ff',
    notPassedLineColor: '#cccccc',
    loop: true
  };

  console.log('创建CarTrack, 配置:', carTrackOptions);
  carTrack = new T.CarTrack(map, carTrackOptions);
  console.log('CarTrack创建完成:', carTrack);
  carTrack.start();
  console.log('CarTrack已启动');
}

function stopTrackAnimation() {
  if (carTrack) {
    carTrack.stop();
  }
}
</script>

<style scoped lang="less">
.work-area-container {
  padding: 16px;
}

.card-panel {
  margin-bottom: 16px;
}

.video-card {
  height: 100%;
  
  :deep(.ant-card-body) {
    height: calc(100% - 57px);
    padding: 8px;
    box-sizing: border-box;
    overflow: hidden;
  }
}

.video-grid {
  height: 100%;
  width: 100%;
  display: grid;
  gap: 8px;
  overflow: hidden;
}

.video-grid-0,
.video-grid-1 {
  grid-template-columns: 1fr;
  grid-template-rows: 1fr;
}

.video-grid-2 {
  grid-template-columns: repeat(2, 1fr);
  grid-template-rows: 1fr;
}

.video-grid-3 {
  grid-template-columns: repeat(2, 1fr);
  grid-template-rows: repeat(2, 1fr);
}

.video-grid-4 {
  grid-template-columns: repeat(2, 1fr);
  grid-template-rows: repeat(2, 1fr);
}

.video-item {
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.video-monitor-card {
  position: relative;
  min-width: 0;
  min-height: 0;
  border-radius: 12px;
  overflow: hidden;
  background: #020617;
  border: 1px solid rgba(148, 163, 184, 0.18);
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.14);
}

.video-monitor-card :deep(.flv-player-container) {
  height: 100%;
  min-height: 180px;
  border-radius: 0;
}

.video-monitor-card :deep(.video-wrapper) {
  min-height: 140px;
}

.video-control-panel {
  position: absolute;
  right: 12px;
  bottom: 12px;
  z-index: 20;
}

.track-card {
  height: 100%;
  
  :deep(.ant-card-body) {
    height: calc(100% - 57px);
    padding: 12px;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
  }
}

.track-controls {
  margin-bottom: 12px;
  flex-shrink: 0;
}

.map-container {
  flex: 1;
  min-height: 0;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.map-loading {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  border-radius: 8px;
}

.tianditu-map {
  width: 100%;
  height: 100%;
}

.track-stats {
  margin-top: 12px;
  flex-shrink: 0;
}
</style>
