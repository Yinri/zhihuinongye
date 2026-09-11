<template>
  <div
    class="ptz-shell"
    :class="{ expanded: isExpanded, disabled: isDisabled }"
    @mouseenter="expandPanel"
    @mouseleave="collapsePanel"
  >
    <a-button class="ptz-trigger" size="small" @click.stop="togglePanel">
      <ControlOutlined />
      <span>云台</span>
      <span class="ptz-trigger-status">{{ activeCommand ? '控制中' : '待命' }}</span>
    </a-button>

    <transition name="ptz-fade">
      <div v-show="isExpanded" class="ptz-panel" @click.stop>
        <div class="ptz-header">
          <div>
            <div class="ptz-title">云台控制</div>
            <div class="ptz-subtitle">按住方向键可连续控制</div>
          </div>
          <div class="ptz-header-actions">
            <span class="ptz-status" :class="{ disabled: isDisabled }">{{ statusText }}</span>
            <a-button class="ptz-icon-btn" size="small" @click="toggleAdvanced">
              <SettingOutlined />
            </a-button>
            <a-button class="ptz-icon-btn" size="small" @click="collapsePanel">
              <ShrinkOutlined />
            </a-button>
          </div>
        </div>

        <div class="ptz-toolbar">
          <a-button
            class="ptz-stop-btn"
            size="small"
            type="primary"
            :disabled="isDisabled"
            @click="handleCommand('stop')"
            aria-label="停止"
          >
            <PauseOutlined />
            <span>停止</span>
          </a-button>
          <a-button class="ptz-more-btn" size="small" @click="toggleAdvanced">
            <MoreOutlined />
            <span>{{ showAdvanced ? '收起更多' : '更多控制' }}</span>
          </a-button>
        </div>

        <div class="ptz-content">
          <div class="ptz-grid">
            <div></div>
            <a-button
              class="ptz-btn direction-btn"
              size="small"
              :disabled="isDisabled"
              :class="{ active: activeCommand === 'up' }"
              @pointerdown.prevent="onPointerDown('up', $event)"
              @pointerup.prevent="onPointerUp"
              @pointerleave="onPointerLeave"
              @pointercancel="onPointerUp"
              aria-label="上"
            >
              <UpOutlined />
            </a-button>
            <div></div>
            <a-button
              class="ptz-btn direction-btn"
              size="small"
              :disabled="isDisabled"
              :class="{ active: activeCommand === 'left' }"
              @pointerdown.prevent="onPointerDown('left', $event)"
              @pointerup.prevent="onPointerUp"
              @pointerleave="onPointerLeave"
              @pointercancel="onPointerUp"
              aria-label="左"
            >
              <LeftOutlined />
            </a-button>
            <div class="ptz-center-dot">PTZ</div>
            <a-button
              class="ptz-btn direction-btn"
              size="small"
              :disabled="isDisabled"
              :class="{ active: activeCommand === 'right' }"
              @pointerdown.prevent="onPointerDown('right', $event)"
              @pointerup.prevent="onPointerUp"
              @pointerleave="onPointerLeave"
              @pointercancel="onPointerUp"
              aria-label="右"
            >
              <RightOutlined />
            </a-button>
            <div></div>
            <a-button
              class="ptz-btn direction-btn"
              size="small"
              :disabled="isDisabled"
              :class="{ active: activeCommand === 'down' }"
              @pointerdown.prevent="onPointerDown('down', $event)"
              @pointerup.prevent="onPointerUp"
              @pointerleave="onPointerLeave"
              @pointercancel="onPointerUp"
              aria-label="下"
            >
              <DownOutlined />
            </a-button>
            <div></div>
          </div>

          <transition name="ptz-advanced">
            <div v-show="showAdvanced" class="ptz-advanced">
              <div class="ptz-speed">
                <span class="ptz-speed-label">速度</span>
                <a-select v-model:value="currentSpeed" size="small" style="width: 110px" :disabled="isDisabled">
                  <a-select-option value="20">慢</a-select-option>
                  <a-select-option value="50">中</a-select-option>
                  <a-select-option value="80">快</a-select-option>
                </a-select>
              </div>
              <div class="ptz-zoom">
                <a-button
                  class="ptz-btn zoom-btn"
                  size="small"
                  :disabled="isDisabled"
                  :class="{ active: activeCommand === 'zoomin' }"
                  @pointerdown.prevent="onPointerDown('zoomin', $event)"
                  @pointerup.prevent="onPointerUp"
                  @pointerleave="onPointerLeave"
                  @pointercancel="onPointerUp"
                  aria-label="放大"
                >
                  <PlusOutlined />
                  <span>放大</span>
                </a-button>
                <a-button
                  class="ptz-btn zoom-btn"
                  size="small"
                  :disabled="isDisabled"
                  :class="{ active: activeCommand === 'zoomout' }"
                  @pointerdown.prevent="onPointerDown('zoomout', $event)"
                  @pointerup.prevent="onPointerUp"
                  @pointerleave="onPointerLeave"
                  @pointercancel="onPointerUp"
                  aria-label="缩小"
                >
                  <MinusOutlined />
                  <span>缩小</span>
                </a-button>
              </div>
            </div>
          </transition>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import {
  ControlOutlined,
  DownOutlined,
  LeftOutlined,
  MinusOutlined,
  MoreOutlined,
  PauseOutlined,
  PlusOutlined,
  RightOutlined,
  SettingOutlined,
  ShrinkOutlined,
  UpOutlined,
} from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import { controlVideoStream, type VideoPtzCommand } from '../workArea.api';

interface Props {
  deviceCode?: string;
  channelNum?: string;
  speed?: string;
}

const props = withDefaults(defineProps<Props>(), {
  deviceCode: '',
  channelNum: '',
  speed: '33',
});

const activeCommand = ref<VideoPtzCommand | ''>('');
const lastCommandText = ref('待命');
const currentSpeed = ref(props.speed);
const isPressing = ref(false);
const isExpanded = ref(false);
const showAdvanced = ref(false);
const activePointerId = ref<number | null>(null);

const commandTextMap: Record<VideoPtzCommand, string> = {
  stop: '停止',
  up: '上移',
  down: '下移',
  left: '左移',
  right: '右移',
  zoomin: '放大',
  zoomout: '缩小',
};

const isDisabled = computed(() => !props.deviceCode || !props.channelNum);
const statusText = computed(() => {
  if (activeCommand.value) {
    return `执行中: ${commandTextMap[activeCommand.value]}`;
  }
  return `最近指令: ${lastCommandText.value}`;
});

function expandPanel() {
  isExpanded.value = true;
}

function collapsePanel() {
  isExpanded.value = false;
  showAdvanced.value = false;
}

function togglePanel() {
  isExpanded.value = !isExpanded.value;
  if (!isExpanded.value) {
    showAdvanced.value = false;
  }
}

function toggleAdvanced() {
  if (!isExpanded.value) {
    isExpanded.value = true;
  }
  showAdvanced.value = !showAdvanced.value;
}

async function handleCommand(command: VideoPtzCommand, continuous = false) {
  if (!props.deviceCode || !props.channelNum) {
    message.warning('当前视频缺少设备编码或通道号，暂不可控制');
    return;
  }

  activeCommand.value = command;
  try {
    const response = await controlVideoStream(
      {
        DeviceCode: props.deviceCode,
        ChannelNum: props.channelNum,
        command,
        speed: currentSpeed.value,
      }
    );
    if (response?.code !== 1) {
      throw new Error(response?.msg || '云台控制失败');
    }
    if (!continuous || command === 'stop') {
      lastCommandText.value = commandTextMap[command];
    }
  } catch (error) {
    console.error('视频云台控制失败', error);
    message.error({
      content: error instanceof Error ? error.message : '视频云台控制失败',
      key: 'ptz-control-error',
    });
  } finally {
    if (!continuous) {
      activeCommand.value = '';
    }
  }
}

function onPress(command: VideoPtzCommand) {
  isPressing.value = true;
  handleCommand(command, true);
}

function onRelease() {
  if (!isPressing.value) return;
  isPressing.value = false;
  activePointerId.value = null;
  handleCommand('stop');
}

function onPointerDown(command: VideoPtzCommand, event: PointerEvent) {
  if (isDisabled.value || isPressing.value) return;
  activePointerId.value = event.pointerId;
  onPress(command);
}

function onPointerUp(event: PointerEvent) {
  if (activePointerId.value !== null && event.pointerId !== activePointerId.value) {
    return;
  }
  onRelease();
}

function onPointerLeave(event: PointerEvent) {
  if (activePointerId.value !== null && event.pointerId !== activePointerId.value) {
    return;
  }
  onRelease();
}
</script>

<style scoped lang="less">
.ptz-shell {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.ptz-trigger {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 34px;
  padding: 0 12px;
  color: #e2e8f0;
  background: rgba(15, 23, 42, 0.72);
  border: 1px solid rgba(148, 163, 184, 0.22);
  border-radius: 999px;
  backdrop-filter: blur(12px);
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.28);
}

.ptz-trigger-status {
  padding: 0 6px;
  font-size: 11px;
  line-height: 20px;
  color: #bfdbfe;
  background: rgba(37, 99, 235, 0.14);
  border-radius: 999px;
}

.ptz-shell.disabled .ptz-trigger-status {
  color: #cbd5e1;
  background: rgba(148, 163, 184, 0.12);
}

.ptz-panel {
  width: min(292px, calc(100% - 24px));
  max-height: calc(100% - 24px);
  padding: 12px;
  overflow: auto;
  color: #e2e8f0;
  background: linear-gradient(180deg, rgba(15, 23, 42, 0.76) 0%, rgba(2, 6, 23, 0.82) 100%);
  border: 1px solid rgba(148, 163, 184, 0.22);
  border-radius: 16px;
  backdrop-filter: blur(16px);
  box-shadow: 0 14px 32px rgba(15, 23, 42, 0.36);
}

.ptz-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 10px;
}

.ptz-header-actions {
  display: flex;
  align-items: flex-start;
  justify-content: flex-end;
  gap: 6px;
  flex-wrap: wrap;
}

.ptz-title {
  color: #f8fafc;
  font-weight: 600;
  line-height: 20px;
  font-size: 13px;
}

.ptz-subtitle {
  margin-top: 2px;
  color: #94a3b8;
  font-size: 11px;
}

.ptz-status {
  max-width: 52%;
  padding: 2px 8px;
  color: #bfdbfe;
  font-size: 11px;
  line-height: 18px;
  text-align: right;
  background: rgba(37, 99, 235, 0.14);
  border: 1px solid rgba(96, 165, 250, 0.22);
  border-radius: 999px;
}

.ptz-status.disabled {
  color: #cbd5e1;
  background: rgba(148, 163, 184, 0.12);
  border-color: rgba(148, 163, 184, 0.2);
}

.ptz-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.ptz-more-btn,
.ptz-icon-btn {
  color: #cbd5e1;
  background: rgba(15, 23, 42, 0.5);
  border-color: rgba(148, 163, 184, 0.2);
}

.ptz-speed {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
}

.ptz-speed-label {
  color: #e2e8f0;
  white-space: nowrap;
}

.ptz-stop-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  border-radius: 999px;
  box-shadow: 0 8px 16px rgba(37, 99, 235, 0.18);
}

.ptz-content {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.ptz-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 6px;
  flex: 1;
}

.ptz-btn {
  width: 100%;
  height: 36px;
  border-radius: 10px;
  border-color: rgba(148, 163, 184, 0.2);
  background: rgba(15, 23, 42, 0.56);
  color: #e2e8f0;
}

.direction-btn {
  font-size: 16px;
}

.ptz-center-dot {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 36px;
  color: #93c5fd;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.08em;
  background: radial-gradient(circle, rgba(30, 64, 175, 0.28) 0%, rgba(15, 23, 42, 0.3) 72%);
  border: 1px dashed rgba(96, 165, 250, 0.26);
  border-radius: 10px;
}

.ptz-advanced {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding-top: 6px;
  border-top: 1px solid rgba(148, 163, 184, 0.14);
}

.ptz-zoom {
  width: 100%;
  display: flex;
  flex-direction: row;
  gap: 6px;
}

.zoom-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.ptz-btn.active {
  border-color: #60a5fa;
  color: #dbeafe;
  background: linear-gradient(180deg, rgba(37, 99, 235, 0.38) 0%, rgba(30, 64, 175, 0.22) 100%);
  box-shadow: 0 0 0 1px rgba(96, 165, 250, 0.2);
}

.ptz-btn:not(:disabled):hover {
  color: #dbeafe;
  border-color: #60a5fa;
  background: rgba(30, 41, 59, 0.68);
}

.ptz-panel :deep(.ant-select-selector) {
  background: rgba(15, 23, 42, 0.56) !important;
  border-color: rgba(148, 163, 184, 0.2) !important;
  color: #e2e8f0 !important;
}

.ptz-panel :deep(.ant-select-arrow) {
  color: #cbd5e1;
}

.ptz-fade-enter-active,
.ptz-fade-leave-active,
.ptz-advanced-enter-active,
.ptz-advanced-leave-active {
  transition: all 0.18s ease;
}

.ptz-fade-enter-from,
.ptz-fade-leave-to {
  opacity: 0;
  transform: translateY(8px) scale(0.96);
}

.ptz-advanced-enter-from,
.ptz-advanced-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

@media (max-width: 768px) {
  .ptz-header,
  .ptz-toolbar,
  .ptz-content {
    flex-direction: column;
  }

  .ptz-status {
    max-width: 100%;
    text-align: left;
  }

  .ptz-zoom {
    width: 100%;
  }

  .ptz-panel {
    width: min(260px, calc(100% - 20px));
    padding: 10px;
  }
}

</style>
