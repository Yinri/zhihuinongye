<script lang="ts" setup>
  import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue';
  import { Icon } from '/@/components/Icon';

  interface WaterGateDevice {
    id: string;
    name: string;
    status?: string;
    deviceType?: 'gate' | 'water-level' | 'pump' | 'camera' | 'soil' | string;
    category?: string;
    code?: string;
    siteNo?: string;
    longitude?: string | number;
    latitude?: string | number;
    onlineText?: string;
    stateText?: string;
    manageUnit?: string;
    controlCenter?: string;
    linkedGateId?: string;
    linkedGateName?: string;
    online?: unknown;
  }

  interface DeviceTypeMeta {
    label: string;
    color: string;
    shortLabel: string;
  }

  const props = withDefaults(
    defineProps<{
      devices: WaterGateDevice[];
      baseName?: string;
      centerLongitude?: string | number;
      centerLatitude?: string | number;
      mapWidth?: string | number;
      mapHeight?: string | number;
    }>(),
    {
      devices: () => [],
      baseName: '',
      centerLongitude: '',
      centerLatitude: '',
      mapWidth: '100%',
      mapHeight: '320px',
    }
  );

  const mapContainer = ref<HTMLDivElement | null>(null);
  const loading = ref(false);
  const errorText = ref('');
  const selectedDevice = ref<WaterGateDevice | null>(null);
  const popupPosition = ref({ x: 0, y: 0 });

  let mapInstance: any = null;
  let T: any = null;
  let resizeObserver: ResizeObserver | null = null;
  const markers: any[] = [];

  const mapWidthComputed = computed(() => (typeof props.mapWidth === 'number' ? `${props.mapWidth}px` : props.mapWidth));
  const mapHeightComputed = computed(() => (typeof props.mapHeight === 'number' ? `${props.mapHeight}px` : props.mapHeight));

  const deviceTypeMeta: Record<string, DeviceTypeMeta> = {
    gate: { label: '闸门/控制柜', color: '#1677ff', shortLabel: '水阀' },
    'water-level': { label: '田间水位计', color: '#13a8a8', shortLabel: '水位' },
    pump: { label: '水泵', color: '#52c41a', shortLabel: '水泵' },
    camera: { label: '视频球机', color: '#722ed1', shortLabel: '球机' },
    soil: { label: '土壤墒情', color: '#d46b08', shortLabel: '墒情' },
  };

  const validDevices = computed(() =>
    props.devices
      .map((device) => {
        const longitude = Number(device.longitude);
        const latitude = Number(device.latitude);
        return {
          ...device,
          longitude,
          latitude,
        };
      })
      .filter((device) => Number.isFinite(device.longitude) && Number.isFinite(device.latitude))
  );

  const centerPoint = computed(() => {
    const longitude = Number(props.centerLongitude);
    const latitude = Number(props.centerLatitude);
    if (Number.isFinite(longitude) && Number.isFinite(latitude)) {
      return { longitude, latitude, zoom: 15 };
    }
    if (validDevices.value.length) {
      return {
        longitude: validDevices.value[0].longitude,
        latitude: validDevices.value[0].latitude,
        zoom: 16,
      };
    }
    return { longitude: 112.698, latitude: 31.225, zoom: 12 };
  });

  const onlineCount = computed(() => props.devices.filter((device) => getDeviceOnline(device)).length);

  const deviceTypeStats = computed(() =>
    Object.entries(
      props.devices.reduce<Record<string, number>>((acc, device) => {
        const key = getDeviceType(device);
        acc[key] = (acc[key] || 0) + 1;
        return acc;
      }, {})
    ).map(([type, count]) => ({
      type,
      count,
      ...getDeviceTypeMeta(type),
    }))
  );

  function loadTiandituAPI() {
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
      script.onerror = () => reject(new Error('天地图 API 加载失败'));
      document.head.appendChild(script);
    });
  }

  function getDeviceOnline(device: WaterGateDevice) {
    const online = String(device.online ?? '').toLowerCase();
    if (online === '0' || online === 'false' || online === 'offline') {
      return false;
    }
    if (online === '1' || online === 'true' || online === 'online') {
      return true;
    }
    return device.onlineText ? !/离线|offline/i.test(device.onlineText) : true;
  }

  function getStatusText(device: WaterGateDevice) {
    if (device.onlineText) return device.onlineText;
    if (!getDeviceOnline(device)) return '离线';
    return device.stateText || device.status || '未知';
  }

  function getDeviceType(device: WaterGateDevice) {
    return String(device.deviceType || 'gate');
  }

  function getDeviceTypeMeta(type: string) {
    return deviceTypeMeta[type] || { label: '灌溉设备', color: '#4b5563', shortLabel: '设备' };
  }

  function getDeviceCategory(device: WaterGateDevice) {
    return device.category || getDeviceTypeMeta(getDeviceType(device)).label;
  }

  function createDeviceIcon(device: WaterGateDevice, index: number) {
    const online = getDeviceOnline(device);
    const meta = getDeviceTypeMeta(getDeviceType(device));
    const color = online ? meta.color : '#8c8c8c';
    const s = 50;
    const hs = s / 2;
    const cr = 17;
    const cy = 20;
    const label = online ? meta.shortLabel : '离线';
    const svgIcon = `
    <svg width="${s}" height="${s + 18}" viewBox="0 0 ${s} ${s + 18}" fill="none" xmlns="http://www.w3.org/2000/svg">
      <circle cx="${hs}" cy="${cy + 3}" r="${cr}" fill="#000" opacity="0.24"/>
      <circle cx="${hs}" cy="${cy}" r="${cr}" fill="${color}" stroke="#fff" stroke-width="3"/>
      ${getDeviceGlyph(getDeviceType(device), hs, cy)}
      <rect x="${hs - 18}" y="${s - 1}" width="36" height="15" rx="7.5" fill="${color}" stroke="#fff" stroke-width="1.5"/>
      <text x="${hs}" y="${s + 10}" text-anchor="middle" fill="#fff" font-size="9" font-weight="800" font-family="sans-serif">${label}</text>
      <text x="${hs}" y="${cy + 4}" text-anchor="middle" fill="${color}" font-size="9" font-weight="800" font-family="sans-serif">${index + 1}</text>
    </svg>
  `;

    return new T.Icon({
      iconUrl: `data:image/svg+xml;base64,${btoa(unescape(encodeURIComponent(svgIcon)))}`,
      iconSize: new T.Point(s, s + 18),
      iconAnchor: new T.Point(hs, cy + cr + 18),
    });
  }

  function getDeviceGlyph(type: string, hs: number, cy: number) {
    if (type === 'water-level') {
      return `
      <path d="M${hs - 9} ${cy + 3}C${hs - 5} ${cy - 7},${hs} ${cy - 12},${hs + 9} ${cy + 3}" fill="#fff" opacity="0.95"/>
      <path d="M${hs - 8} ${cy + 7}C${hs - 3} ${cy + 11},${hs + 3} ${cy + 11},${hs + 8} ${cy + 7}" stroke="#fff" stroke-width="2.5" stroke-linecap="round"/>
    `;
    }
    if (type === 'pump') {
      return `
      <circle cx="${hs - 2}" cy="${cy}" r="8" fill="#fff" opacity="0.95"/>
      <path d="M${hs - 2} ${cy - 8}V${cy + 8}M${hs - 10} ${cy}H${hs + 12}M${hs + 6} ${cy - 5}L${hs + 12} ${cy}L${hs + 6} ${cy + 5}" stroke="${getDeviceTypeMeta(type).color}" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"/>
    `;
    }
    if (type === 'camera') {
      return `
      <rect x="${hs - 11}" y="${cy - 7}" width="18" height="14" rx="3" fill="#fff" opacity="0.95"/>
      <path d="M${hs + 7} ${cy - 3}L${hs + 13} ${cy - 7}V${cy + 7}L${hs + 7} ${cy + 3}Z" fill="#fff"/>
    `;
    }
    if (type === 'soil') {
      return `
      <path d="M${hs - 10} ${cy + 7}H${hs + 10}" stroke="#fff" stroke-width="3" stroke-linecap="round"/>
      <path d="M${hs - 5} ${cy + 1}H${hs + 5}" stroke="#fff" stroke-width="3" stroke-linecap="round"/>
      <path d="M${hs} ${cy + 1}C${hs - 1} ${cy - 7},${hs + 6} ${cy - 10},${hs + 10} ${cy - 12}" stroke="#fff" stroke-width="2.4" stroke-linecap="round"/>
      <path d="M${hs} ${cy + 1}C${hs + 1} ${cy - 7},${hs - 6} ${cy - 10},${hs - 10} ${cy - 12}" stroke="#fff" stroke-width="2.4" stroke-linecap="round"/>
    `;
    }
    return `
    <path d="M${hs - 10} ${cy - 5}H${hs + 10}V${cy + 5}H${hs - 10}V${cy - 5}Z" fill="#fff" opacity="0.95"/>
    <path d="M${hs} ${cy - 13}V${cy - 6}" stroke="#fff" stroke-width="3" stroke-linecap="round"/>
    <path d="M${hs - 8} ${cy - 13}H${hs + 8}" stroke="#fff" stroke-width="3" stroke-linecap="round"/>
    <path d="M${hs - 13} ${cy}H${hs - 9}M${hs + 9} ${cy}H${hs + 13}" stroke="#fff" stroke-width="3" stroke-linecap="round"/>
    <path d="M${hs - 5} ${cy + 5}C${hs - 5} ${cy + 10},${hs + 5} ${cy + 10},${hs + 5} ${cy + 5}" stroke="#fff" stroke-width="2.5" stroke-linecap="round"/>
  `;
  }

  function clearMarkers() {
    if (!mapInstance) return;
    markers.forEach((marker) => {
      try {
        const handler = (marker as any).__clickHandler;
        if (handler && marker.removeEventListener) {
          marker.removeEventListener('click', handler);
        }
        mapInstance.removeOverLay(marker);
      } catch (error) {}
    });
    markers.length = 0;
  }

  function fitViewport() {
    if (!mapInstance || !validDevices.value.length) return;
    if (markers.length === 1) {
      const device = validDevices.value[0];
      mapInstance.centerAndZoom(new T.LngLat(device.longitude, device.latitude), 16);
      return;
    }

    try {
      let minLng = 180;
      let maxLng = -180;
      let minLat = 90;
      let maxLat = -90;
      markers.forEach((marker) => {
        const point = marker.getLngLat?.();
        if (!point) return;
        minLng = Math.min(minLng, point.lng);
        maxLng = Math.max(maxLng, point.lng);
        minLat = Math.min(minLat, point.lat);
        maxLat = Math.max(maxLat, point.lat);
      });
      const bounds = new T.LngLatBounds(new T.LngLat(minLng, minLat), new T.LngLat(maxLng, maxLat));
      mapInstance.setViewport(bounds);
    } catch (error) {
      mapInstance.centerAndZoom(new T.LngLat(centerPoint.value.longitude, centerPoint.value.latitude), centerPoint.value.zoom);
    }
  }

  function updatePopupPosition(point: any) {
    try {
      const pixel = mapInstance.lngLatToContainerPoint(point);
      popupPosition.value = {
        x: Math.min(Math.max(pixel.x + 12, 12), Math.max((mapContainer.value?.clientWidth || 360) - 260, 12)),
        y: Math.min(Math.max(pixel.y - 88, 12), Math.max((mapContainer.value?.clientHeight || 280) - 160, 12)),
      };
    } catch (error) {
      popupPosition.value = { x: 16, y: 16 };
    }
  }

  function renderMarkers() {
    if (!mapInstance || !T) return;
    clearMarkers();
    selectedDevice.value = null;

    validDevices.value.forEach((device, index) => {
      const typeOffset = getTypeOffset(getDeviceType(device));
      const offsetLng = typeOffset.lng + ((index % 3) - 1) * 0.000018;
      const offsetLat = typeOffset.lat + ((Math.floor(index / 3) % 3) - 1) * 0.000018;
      const point = new T.LngLat(Number(device.longitude) + offsetLng, Number(device.latitude) + offsetLat);
      const marker = new T.Marker(point, { icon: createDeviceIcon(device, index) });
      const clickHandler = () => {
        selectedDevice.value = device;
        updatePopupPosition(point);
      };
      marker.addEventListener('click', clickHandler);
      (marker as any).__clickHandler = clickHandler;
      mapInstance.addOverLay(marker);
      markers.push(marker);
    });

    fitViewport();
  }

  function getTypeOffset(type: string) {
    const offsets: Record<string, { lng: number; lat: number }> = {
      gate: { lng: 0, lat: 0 },
      'water-level': { lng: 0.00008, lat: 0.00002 },
      pump: { lng: -0.00008, lat: 0.00002 },
      camera: { lng: 0.00002, lat: -0.00008 },
      soil: { lng: -0.00002, lat: -0.00002 },
    };
    return offsets[type] || { lng: 0, lat: 0 };
  }

  async function initMap() {
    try {
      loading.value = true;
      errorText.value = '';
      await loadTiandituAPI();
      await nextTick();
      if (!mapContainer.value) return;

      mapInstance = new T.Map(mapContainer.value, { enableScrollWheelZoom: true });
      mapInstance.setMapType((window as any).TMAP_SATELLITE_MAP);
      mapInstance.centerAndZoom(new T.LngLat(centerPoint.value.longitude, centerPoint.value.latitude), centerPoint.value.zoom);
      mapInstance.addControl(new T.Control.Zoom());
      if (typeof mapInstance.enableScrollWheelZoom === 'function') {
        mapInstance.enableScrollWheelZoom();
      }
      renderMarkers();

      resizeObserver = new ResizeObserver(() => {
        try {
          mapInstance?.checkResize?.();
        } catch (error) {}
      });
      resizeObserver.observe(mapContainer.value);
    } catch (error: any) {
      errorText.value = error?.message || '水阀地图初始化失败';
    } finally {
      loading.value = false;
    }
  }

  function resetView() {
    if (!mapInstance) return;
    if (validDevices.value.length) {
      fitViewport();
    } else {
      mapInstance.centerAndZoom(new T.LngLat(centerPoint.value.longitude, centerPoint.value.latitude), centerPoint.value.zoom);
    }
  }

  function closePopup() {
    selectedDevice.value = null;
  }

  watch(
    () => [props.devices, props.centerLongitude, props.centerLatitude],
    () => {
      if (mapInstance) {
        renderMarkers();
        if (!validDevices.value.length) {
          mapInstance.centerAndZoom(new T.LngLat(centerPoint.value.longitude, centerPoint.value.latitude), centerPoint.value.zoom);
        }
      }
    },
    { deep: true }
  );

  onMounted(initMap);

  onUnmounted(() => {
    clearMarkers();
    resizeObserver?.disconnect();
    resizeObserver = null;
    mapInstance = null;
  });
</script>

<template>
  <div class="water-gate-map" :style="{ width: mapWidthComputed, height: mapHeightComputed }">
    <div ref="mapContainer" class="water-gate-map__canvas"></div>

    <div class="water-gate-map__toolbar">
      <button class="water-gate-map__reset" type="button" @click="resetView">
        <Icon icon="ant-design:aim-outlined" />
      </button>
    </div>

    <div class="water-gate-map__legend">
      <span class="legend-status">在线 {{ onlineCount }}</span>
      <span class="legend-status">离线 {{ devices.length - onlineCount }}</span>
      <span v-for="item in deviceTypeStats" :key="item.type" class="legend-type">
        <span class="legend-dot" :style="{ background: item.color }"></span>
        <span>{{ item.label }} {{ item.count }}</span>
      </span>
    </div>

    <transition name="water-gate-popup">
      <div v-if="selectedDevice" class="water-gate-map__popup" :style="{ left: `${popupPosition.x}px`, top: `${popupPosition.y}px` }">
        <div class="popup-head">
          <div class="popup-title">{{ selectedDevice.name }}</div>
          <button class="popup-close" type="button" @click="closePopup">
            <Icon icon="ant-design:close-outlined" />
          </button>
        </div>
        <div class="popup-status" :class="{ offline: !getDeviceOnline(selectedDevice) }">
          {{ getDeviceCategory(selectedDevice) }} · {{ getStatusText(selectedDevice) }}
        </div>
        <div class="popup-row">
          <span>设备编号</span>
          <b>{{ selectedDevice.code || '-' }}</b>
        </div>
        <div class="popup-row">
          <span>站点编号</span>
          <b>{{ selectedDevice.siteNo || '-' }}</b>
        </div>
        <div class="popup-row">
          <span>管控中心</span>
          <b>{{ selectedDevice.controlCenter || '-' }}</b>
        </div>
        <div v-if="selectedDevice.linkedGateName" class="popup-row">
          <span>关联闸门</span>
          <b>{{ selectedDevice.linkedGateName }}</b>
        </div>
      </div>
    </transition>

    <div v-if="loading" class="water-gate-map__loading">
      <a-spin size="large" tip="地图加载中..." />
    </div>
    <div v-if="errorText" class="water-gate-map__error">
      <a-alert type="error" :message="errorText" show-icon />
    </div>
  </div>
</template>

<style scoped lang="less">
  .water-gate-map {
    position: relative;
    overflow: hidden;
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    background: #f5f7fb;
  }

  .water-gate-map__canvas {
    width: 100%;
    height: 100%;
  }

  .water-gate-map__toolbar,
  .water-gate-map__legend {
    position: absolute;
    z-index: 1000;
    display: flex;
    align-items: center;
    border: 1px solid rgba(15, 23, 42, 0.08);
    background: rgba(255, 255, 255, 0.96);
    box-shadow: 0 4px 12px rgba(15, 23, 42, 0.14);
  }

  .water-gate-map__toolbar {
    top: 12px;
    right: 12px;
    justify-content: center;
    padding: 5px;
    border-radius: 8px;
  }

  .water-gate-map__reset,
  .popup-close {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 28px;
    height: 28px;
    padding: 0;
    color: #4b5563;
    cursor: pointer;
    background: #fff;
    border: 1px solid #e5e7eb;
    border-radius: 6px;
  }

  .water-gate-map__legend {
    right: 12px;
    bottom: 12px;
    gap: 6px;
    flex-wrap: wrap;
    max-width: calc(100% - 24px);
    padding: 8px 10px;
    color: #4b5563;
    font-size: 12px;
    border-radius: 8px;
  }

  .legend-status,
  .legend-type {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    white-space: nowrap;
  }

  .legend-dot {
    width: 9px;
    height: 9px;
    flex: 0 0 9px;
    border-radius: 50%;
  }

  .water-gate-map__popup {
    position: absolute;
    z-index: 1001;
    width: 250px;
    padding: 12px;
    color: #1f2937;
    background: rgba(255, 255, 255, 0.98);
    border: 1px solid rgba(15, 23, 42, 0.1);
    border-radius: 8px;
    box-shadow: 0 10px 24px rgba(15, 23, 42, 0.18);
  }

  .popup-head,
  .popup-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 10px;
  }

  .popup-title {
    min-width: 0;
    overflow: hidden;
    font-size: 14px;
    font-weight: 700;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .popup-status {
    display: inline-flex;
    margin: 8px 0;
    padding: 2px 8px;
    color: #0958d9;
    font-size: 12px;
    background: #e6f4ff;
    border-radius: 999px;
  }

  .popup-status.offline {
    color: #595959;
    background: #f5f5f5;
  }

  .popup-row {
    padding: 3px 0;
    color: #6b7280;
    font-size: 12px;

    b {
      max-width: 140px;
      overflow: hidden;
      color: #111827;
      font-weight: 600;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .water-gate-map__loading,
  .water-gate-map__error {
    position: absolute;
    z-index: 1002;
  }

  .water-gate-map__loading {
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255, 255, 255, 0.58);
  }

  .water-gate-map__error {
    top: 56px;
    left: 12px;
    right: 12px;
  }

  .water-gate-popup-enter-active,
  .water-gate-popup-leave-active {
    transition:
      opacity 0.18s ease,
      transform 0.18s ease;
  }

  .water-gate-popup-enter-from,
  .water-gate-popup-leave-to {
    opacity: 0;
    transform: translateY(6px);
  }

  @media (max-width: 768px) {
    .water-gate-map__popup {
      left: 12px !important;
      right: 12px;
      width: auto;
    }
  }
</style>
