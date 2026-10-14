import { ref, watch, onMounted, onBeforeUnmount, onActivated, onDeactivated } from 'vue';
import { getVideoDevices, getVideoStream } from './workArea.api';

export function normalizeVideoOnline(value: unknown): boolean | null {
  if (value === 1 || value === '1' || value === true) return true;
  if (value === 0 || value === '0' || value === false) return false;
  return null;
}

export function useVideoDevices(getBaseId: () => string | number | undefined, limit = Infinity) {
  const videoDevices = ref<any[]>([]);
  let generation = 0;
  let timer: ReturnType<typeof setInterval> | undefined;
  let pending = false;
  let active = true;

  watch(getBaseId, () => {
    generation++;
    pending = false;
    videoDevices.value = [];
  }, { flush: 'sync' });

  async function refreshVideoDevices() {
    const baseId = getBaseId();
    if (!baseId || !active || pending) return;
    pending = true;
    const token = generation;
    const isCurrent = () => active && token === generation && baseId === getBaseId();
    try {
      const list = await getVideoDevices(String(baseId), true);
      if (!isCurrent()) return;
      const previous = new Map(videoDevices.value.map(device => [device.renderKey, device]));
      videoDevices.value = (list || []).slice(0, limit).map((device: any) => {
        const channelNum = String(device.channelNum || '1');
        const renderKey = `${baseId}-${device.equipmentCode}-${channelNum}`;
        const existing = previous.get(renderKey);
        const onlineStatus = normalizeVideoOnline(device.isOnline);
        // 状态刷新保留原播放地址；设备离线时销毁播放器，恢复在线后重新取流。
        return {
          ...device, channelNum, renderKey, onlineStatus,
          streamUrl: onlineStatus === false ? '' : existing?.streamUrl || '',
        };
      });
      await Promise.allSettled(videoDevices.value.map(async device => {
        if (device.onlineStatus === false || device.streamUrl) return;
        try {
          const url = await getVideoStream(device.equipmentCode, device.channelNum);
          if (!isCurrent() || !videoDevices.value.includes(device) || device.onlineStatus === false) return;
          device.streamUrl = url || '';
        } catch (error) {
          console.warn('获取视频流失败', device.equipmentName, error);
        }
      }));
    } catch (error) {
      if (isCurrent()) videoDevices.value.forEach(device => { device.onlineStatus = null; });
      console.warn('更新视频设备状态失败', error);
    } finally {
      if (token === generation) pending = false;
    }
  }

  function startPolling() {
    active = true;
    if (!timer) timer = setInterval(() => { void refreshVideoDevices(); }, 30000);
  }
  function stopPolling() {
    active = false;
    generation++;
    pending = false;
    if (timer) clearInterval(timer);
    timer = undefined;
  }
  onMounted(startPolling);
  onActivated(() => { startPolling(); void refreshVideoDevices(); });
  onDeactivated(stopPolling);
  onBeforeUnmount(stopPolling);
  return { videoDevices, refreshVideoDevices };
}
