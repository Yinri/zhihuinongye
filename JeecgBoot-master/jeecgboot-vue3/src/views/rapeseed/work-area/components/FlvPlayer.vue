<template>
  <div class="flv-player-container">
    <div class="video-title">{{ title }}</div>
    <div class="video-wrapper" ref="containerRef">
      <div v-if="loading && !error" class="video-loading">
        <a-spin size="small" />
        <span style="margin-left: 8px">{{ loadingText }}</span>
      </div>
      <div v-if="error" class="video-error">
        <span class="error-icon">!</span>
        <span>{{ error }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue';

interface Props {
  url?: string;
  title?: string;
  loadingText?: string;
}

const props = withDefaults(defineProps<Props>(), {
  url: '',
  title: '',
  loadingText: '加载中...',
});

const containerRef = ref<HTMLElement | null>(null);
const loading = ref(true);
const error = ref('');
let player: any = null;
let retryCount = 0;
const maxRetry = 3;
let isMounted = true;
let retryTimeoutId: ReturnType<typeof setTimeout> | null = null;
let loadTimeoutId: ReturnType<typeof setTimeout> | null = null;

declare global {
  interface Window {
    Jessibuca: any;
  }
}

function loadJessibuca(): Promise<any> {
  return new Promise((resolve, reject) => {
    if (window.Jessibuca) {
      resolve(window.Jessibuca);
      return;
    }

    const script = document.createElement('script');
    script.src = '/jessibuca.js';
    script.onload = () => {
      if (window.Jessibuca) {
        resolve(window.Jessibuca);
      } else {
        reject(new Error('Jessibuca not found in window'));
      }
    };
    script.onerror = () => reject(new Error('Failed to load jessibuca.js'));
    document.head.appendChild(script);
  });
}

async function initPlayer() {
  if (!isMounted) return;
  
  if (!props.url) {
    loading.value = false;
    error.value = '无视频流地址';
    return;
  }

  loading.value = true;
  error.value = '';

  try {
    if (!containerRef.value) {
      loading.value = false;
      error.value = '容器元素未找到';
      return;
    }

    destroyPlayer();

    const Jessibuca = await loadJessibuca();
    
    if (!isMounted) return;

    player = new Jessibuca({
      container: containerRef.value,
      videoBuffer: 0.5,
      isResize: true,
      loadingText: '',
      hasAudio: false,
      debug: false,
      supportDblclickFullscreen: true,
      showBandwidth: false,
      operateBtns: {
        fullscreen: false,
        screenshot: false,
        play: false,
        audio: false,
        recorder: false,
      },
      forceNoOffscreen: true,
      isNotMute: false,
      timeout: 15,
      decoder: '/decoder.js',
      wasmDecoder: '/decoder.wasm',
      useWCS: true,
      useMSE: true,
      autoWasm: true,
      heartTimeout: 10,
      useCanvasRender: false,
    });

    player.on('play', () => {
      if (!isMounted) return;
      loading.value = false;
      retryCount = 0;
    });

    player.on('error', (err: any) => {
      console.error('视频播放错误:', err);
      if (!isMounted) return;
      loading.value = false;
      if (err === 'webglContextLostError' && retryCount < maxRetry) {
        retryCount++;
        error.value = `WebGL资源不足，正在重试(${retryCount}/${maxRetry})...`;
        retryTimeoutId = setTimeout(() => {
          if (isMounted) {
            initPlayer();
          }
        }, 2000);
      } else {
        error.value = '视频播放失败';
      }
    });

    player.on('timeout', () => {
      if (!isMounted) return;
      loading.value = false;
      error.value = '视频加载超时';
    });

    player.on('loadingComplete', () => {
      if (!isMounted) return;
      loading.value = false;
    });

    player.on('performance', (performance: any) => {
      if (!isMounted) return;
      if (performance.fps > 0) {
        loading.value = false;
      }
    });

    player.play(props.url);

    loadTimeoutId = setTimeout(() => {
      if (!isMounted) return;
      if (loading.value) {
        loading.value = false;
        if (!error.value) {
          error.value = '视频加载超时，请检查网络连接';
        }
      }
    }, 15000);

  } catch (e: any) {
    console.error('初始化播放器失败:', e);
    if (!isMounted) return;
    error.value = e.message || '初始化播放器失败';
    loading.value = false;
  }
}

function destroyPlayer() {
  if (retryTimeoutId) {
    clearTimeout(retryTimeoutId);
    retryTimeoutId = null;
  }
  if (loadTimeoutId) {
    clearTimeout(loadTimeoutId);
    loadTimeoutId = null;
  }
  if (player) {
    try {
      player.destroy();
    } catch (e) {
    }
    player = null;
  }
}

watch(() => props.url, (newUrl) => {
  retryCount = 0;
  if (newUrl) {
    initPlayer();
  } else {
    destroyPlayer();
    if (isMounted) {
      loading.value = false;
      error.value = '';
    }
  }
});

onMounted(() => {
  isMounted = true;
  initPlayer();
});

onUnmounted(() => {
  isMounted = false;
  destroyPlayer();
});

defineExpose({
  play: () => player?.play(),
  pause: () => player?.pause(),
});
</script>

<style scoped lang="less">
.flv-player-container {
  background: #000;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.video-title {
  padding: 8px 12px;
  background: rgba(0, 0, 0, 0.8);
  color: #fff;
  font-size: 12px;
  flex-shrink: 0;
  z-index: 10;
}

.video-wrapper {
  position: relative;
  width: 100%;
  flex: 1;
  min-height: 0;
  background: #1a1a1a;
}

.video-loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #fff;
  display: flex;
  align-items: center;
  z-index: 5;
}

.video-error {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #ff4d4f;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  text-align: center;
  padding: 0 16px;
  z-index: 5;
  
  .error-icon {
    font-size: 24px;
    width: 32px;
    height: 32px;
    border: 2px solid #ff4d4f;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}
</style>
