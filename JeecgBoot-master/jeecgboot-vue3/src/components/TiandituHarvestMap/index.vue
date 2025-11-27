<template>
  <div class="harvest-map-container" :style="{ width: mapWidthComputed, height: mapHeightComputed }">
    <div ref="mapEl" class="tdt-map"></div>

    <div v-if="loading" class="map-loading">
      <a-spin size="large" tip="地图加载中..." />
    </div>

    <div v-if="error" class="map-error">
      <a-alert type="error" :message="error" show-icon />
    </div>

    <div v-if="showLegend" :class="['map-legend', legendPosition]">
      <div class="legend-title">图例</div>
      <div class="legend-item">
        <span class="legend-color harvested"></span>
        <span class="legend-label">已收割</span>
      </div>
      <div class="legend-item">
        <span class="legend-color harvesting"></span>
        <span class="legend-label">收割中</span>
      </div>
      <div class="legend-item">
        <span class="legend-color unharvested"></span>
        <span class="legend-label">未收割</span>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';
import { useMessage } from '/@/hooks/web/useMessage';
import { useSelectStore } from '/@/store/selectStore';
import { storeToRefs } from 'pinia';
import { getPlotInfoList } from '/@/views/rapeseed/plot-info/plotInfo.api';

const props = defineProps({
  baseId: { type: [Number, String], default: null },
  plots: { type: Array, default: () => [] },
  mapWidth: { type: [Number, String], default: '100%' },
  mapHeight: { type: [Number, String], default: '480px' },
  enableHover: { type: Boolean, default: true },
  harvestedPlotIds: { type: Array as () => Array<number | string>, default: () => [] },
  harvestingPlotIds: { type: Array as () => Array<number | string>, default: () => [] },
  showLegend: { type: Boolean, default: true },
  legendPosition: { type: String, default: 'bottom-right' }, // bottom-right | bottom-left | top-right | top-left
  enablePopup: { type: Boolean, default: true },
});

const emit = defineEmits(['ready', 'plot-click', 'error']);

const mapWidthComputed = computed(() => typeof props.mapWidth === 'number' ? `${props.mapWidth}px` : props.mapWidth);
const mapHeightComputed = computed(() => typeof props.mapHeight === 'number' ? `${props.mapHeight}px` : props.mapHeight);

const mapEl = ref<HTMLDivElement | null>(null);
let map: any = null;
let T: any = null;
const overlays: any[] = [];
let infoWindow: any = null;

const loading = ref(false);
const error = ref<string | null>(null);
const { createMessage } = useMessage();

// 获取选中基地（兼容性）
const selectStore = useSelectStore();
const { selectedBase } = storeToRefs(selectStore);

async function loadTianditu() {
  return new Promise<void>((resolve, reject) => {
    if ((window as any).T) {
      T = (window as any).T;
      resolve();
      return;
    }
    const script = document.createElement('script');
    script.type = 'text/javascript';
    script.src = 'https://api.tianditu.gov.cn/api?v=4.0&tk=46fdb68b960da6af775e3287cae51e81';
    script.onload = () => {
      T = (window as any).T;
      resolve();
    };
    script.onerror = () => reject(new Error('天地图API加载失败'));
    document.head.appendChild(script);
  });
}

function clearOverlays() {
  if (!map) return;
  try {
    overlays.forEach((ov) => map.removeOverLay(ov));
    overlays.length = 0;
  } catch (e) {}
}

function styleForPlot(plot: any) {
  // 默认边界与填充
  let color = '#1f1f1f';
  let fillColor = '#ffd666';
  let fillOpacity = 0.35;

  const plotKey = plot.id ?? plot.plotId;
  const isHarvested = plotKey != null && props.harvestedPlotIds.includes(plotKey);
  const isHarvesting = plotKey != null && props.harvestingPlotIds.includes(plotKey);
  if (isHarvested) {
    fillColor = '#52c41a'; // 绿色：已收割
    fillOpacity = 0.35;
  } else if (isHarvesting) {
    fillColor = '#faad14'; // 橙色：收割中
    fillOpacity = 0.35;
  }
  return { color, fillColor, fillOpacity };
}

function addPlotPolygon(plot: any) {
  if (!plot?.polygonCoords) return;
  let coords = plot.polygonCoords;
  try {
    if (typeof coords === 'string') coords = JSON.parse(coords);
  } catch (e) {
    return;
  }
  if (!Array.isArray(coords) || coords.length === 0) return;

  const points = coords.map((c: any) => new T.LngLat(c.lng, c.lat));
  const { color, fillColor, fillOpacity } = styleForPlot(plot);
  const polygon = new T.Polygon(points, {
    color,
    weight: 1,
    opacity: 0.9,
    fillColor,
    fillOpacity,
  });
  (polygon as any).plotData = plot;
  polygon.addEventListener('click', (e: any) => {
    emit('plot-click', (polygon as any).plotData);
    if (props.enablePopup) {
      try {
        openPlotInfoWindow(plot, points);
      } catch (err) {}
    }
  });

  if (props.enableHover) {
    const original = { color, weight: 1, opacity: 0.9, fillColor, fillOpacity };
    const hover = { color, weight: 2, opacity: 1, fillColor, fillOpacity: Math.min(fillOpacity + 0.15, 0.7) };
    polygon.addEventListener('mouseover', () => polygon.setStyle(hover));
    polygon.addEventListener('mouseout', () => polygon.setStyle(original));
  }

  map.addOverLay(polygon);
  overlays.push(polygon);
}

function computeCentroid(points: any[]): any {
  if (!points || points.length === 0) return null;
  let sumLng = 0;
  let sumLat = 0;
  points.forEach((p: any) => {
    const lng = typeof p.getLng === 'function' ? p.getLng() : p.lng;
    const lat = typeof p.getLat === 'function' ? p.getLat() : p.lat;
    sumLng += lng;
    sumLat += lat;
  });
  return new T.LngLat(sumLng / points.length, sumLat / points.length);
}

function openPlotInfoWindow(plot: any, points: any[]) {
  if (!map || !T) return;
  const center = computeCentroid(points);
  if (!center) return;

  // 计算状态文本
  const key = plot.id ?? plot.plotId;
  const isHarvested = key != null && props.harvestedPlotIds.includes(key);
  const isHarvesting = key != null && props.harvestingPlotIds.includes(key);
  const statusText = isHarvested ? '已收割' : (isHarvesting ? '收割中' : '未收割');

  const content = `
    <div class="plot-info-window" style="font-size:12px;color:#434343;">
      <div style="font-weight:600;margin-bottom:4px;">${plot.plotName ?? ('地块 #' + (plot.id ?? ''))}</div>
      <div style="margin-bottom:2px;">状态：<span style="font-weight:500;">${statusText}</span></div>
      <div style="margin-bottom:2px;">面积：${plot.area != null ? plot.area + ' 亩' : '未知'}</div>
    </div>
  `;

  // 关闭已有信息窗口
  try {
    if (infoWindow && typeof map.closeInfoWindow === 'function') {
      map.closeInfoWindow();
      infoWindow = null;
    }
  } catch (e) {}

  try {
    // 正确打开信息窗口的方式：通过 map.openInfoWindow(infowin, lnglat)
    infoWindow = new T.InfoWindow(content, { offset: new T.Point(0, -10) });
    map.openInfoWindow(infoWindow, center);
  } catch (e) {
    // 若 InfoWindow 不可用，不抛错，保留 plot-click 事件供父组件弹窗
    infoWindow = null;
  }
}

function fitViewport() {
  if (!map || overlays.length === 0) return;
  try {
    const bounds = new T.Bounds();
    overlays.forEach((ov: any) => {
      if (ov.getBounds) {
        const b = ov.getBounds();
        bounds.extend(b.getSouthWest());
        bounds.extend(b.getNorthEast());
      }
    });
    if ((bounds as any).getCenter) map.setViewport(bounds);
  } catch (e) {}
}

async function renderPlots() {
  clearOverlays();
  const list = props.plots && props.plots.length ? props.plots : await fetchPlots();
  list.forEach(addPlotPolygon);
  fitViewport();
}

async function fetchPlots() {
  try {
    const id = props.baseId ?? selectedBase.value?.baseId;
    if (!id) return [] as any[];
    const res: any = await getPlotInfoList({ baseId: id });
    const records = res?.records ?? [];
    return records;
  } catch (e) {
    createMessage.error('加载地块数据失败');
    emit('error', '加载地块数据失败');
    return [] as any[];
  }
}

async function initMap() {
  try {
    loading.value = true;
    await loadTianditu();
    if (!mapEl.value) throw new Error('地图容器不存在');
    map = new T.Map(mapEl.value);
    map.setMapType((window as any).TMAP_SATELLITE_MAP);
  map.addControl(new T.Control.Zoom());

    const lng = selectedBase.value?.longitude ?? 116.40969;
    const lat = selectedBase.value?.latitude ?? 39.89945;
    map.centerAndZoom(new T.LngLat(lng, lat), selectedBase.value?.latitude ? 16 : 5);

    await renderPlots();
    emit('ready');
  } catch (e: any) {
    error.value = e?.message || '地图初始化失败';
    emit('error', error.value);
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  initMap();
});

onUnmounted(() => {
  clearOverlays();
  try { if (infoWindow) map.removeOverLay(infoWindow); } catch (e) {}
  map = null;
});

watch(() => props.plots, () => {
  if (map) renderPlots();
}, { deep: true });
</script>

<style scoped>
.harvest-map-container { position: relative; }
.tdt-map { width: 100%; height: 100%; border-radius: 6px; overflow: hidden; }
.map-loading { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; background: rgba(255,255,255,0.6); z-index: 10; }
.map-error { position: absolute; top: 8px; left: 8px; right: 8px; z-index: 11; }
.map-legend { position: absolute; z-index: 9999; background: rgba(255,255,255,0.95); border: 1px solid #f0f0f0; border-radius: 6px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); padding: 8px 12px; min-width: 120px; pointer-events: none; }
.map-legend.bottom-right { right: 12px; bottom: 12px; }
.map-legend.bottom-left { left: 12px; bottom: 12px; }
.map-legend.top-right { right: 12px; top: 12px; }
.map-legend.top-left { left: 12px; top: 12px; }
.legend-title { font-size: 12px; font-weight: 600; color: #595959; margin-bottom: 6px; }
.legend-item { display: flex; align-items: center; gap: 8px; margin: 4px 0; }
.legend-color { width: 12px; height: 12px; border: 1px solid #1f1f1f; border-radius: 2px; display: inline-block; }
.legend-color.harvested { background: #52c41a; }
.legend-color.harvesting { background: #faad14; }
.legend-color.unharvested { background: #ffd666; }
.legend-label { font-size: 12px; color: #434343; }
</style>