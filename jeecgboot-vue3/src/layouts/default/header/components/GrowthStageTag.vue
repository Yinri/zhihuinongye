<template>
  <div class="header">
    <div class="growth-stage-tag-container">
      <div class="dropdown-group">
        <!-- 基地下拉下拉框 -->
        <div class="custom-select base-select" @click="toggleDropdown('base')">
          <div class="select-value">{{ selectedBase?.baseName || '请选择基地' }}</div>
          <div class="select-icon" :class="{ open: isDropdownOpen.base }">▼</div>
          <div class="select-options" v-if="isDropdownOpen.base">
            <div class="options-scroll">
              <div
                class="option-item"
                v-for="item in baseList"
                :key="item.baseId"
                @click.stop="selectItem('base', item.baseName)"
              >
                {{ item.baseName }}
              </div>
            </div>
          </div>
        </div>

      </div>

      <div class="stage-tag-group" v-if="!hideGrowthStage && selectedBase?.baseId">
        <div
          class="stage-tag"
          v-for="(stage, index) in stageList"
          :key="stage"
          :style="{'--index': index}"
          :class="{ active: currentGrowthStage === stage }"
        >
          <div v-if="currentGrowthStage === stage" class="stage-indicator" aria-hidden="true">
            <span class="stage-indicator-arrow"></span>
            <span class="stage-indicator-label">当前生育期</span>
          </div>
          {{ stage }}
        </div>
      </div>

    </div>
  </div>

  <div class="main-content"></div>

</template>

<script setup lang="ts">
import {ref, onMounted, computed} from 'vue';
import { useSelectStore } from '../../../../store/selectStore';
import {getBaseList} from '../../../../views/rapeseed/production-plan/center/base.api';
import { useRoute } from 'vue-router';

const route = useRoute();

const hideGrowthStage = computed(() => {
  const currentPath = route.path;
  return currentPath.includes('/rapeseed/production-plan/center');
});

// 定义基地类型接口
interface BaseItem {
  baseId: string | number;
  baseName: string;
  fullName?: string;
  longitude?: string;  // 添加经度字段
  latitude?: string;   // 添加纬度字段
}

// 下拉框数据
const baseList = ref<BaseItem[]>([]);

// 存储当前选中基地的生长阶段
const currentGrowthStage = ref('');

// 状态仓库实例
const selectStore = useSelectStore();

// 选中值
const selectedBase = ref<BaseItem | null>(null);

// 下拉框状态
const isDropdownOpen = ref({
  base: false
});

// 生育阶段数据
const stageList = ref([
  '发芽出苗期', '苗期', '蕾薹期', '开花期', '角果发育成熟期'
]);

const resolveFixedGrowthStage = (base: BaseItem | null) => {
  return base?.baseId ? '角果发育成熟期' : '';
};

// 弹窗状态与表单数据

// 基础提示函数
const showMessage = (text: string, isError = false) => {
  const div = document.createElement('div');
  div.style.position = 'fixed';
  div.style.top = '20px';
  div.style.left = '50%';
  div.style.transform = 'translateX(-50%)';
  div.style.padding = '8px 16px';
  div.style.borderRadius = '4px';
  div.style.color = '#fff';
  div.style.backgroundColor = isError ? '#f56c6c' : '#67c23a';
  div.style.zIndex = '9999';
  div.style.transition = 'opacity 0.3s';
  div.innerText = text;
  document.body.appendChild(div);

  setTimeout(() => {
    div.style.opacity = '0';
    setTimeout(() => div.remove(), 300);
  }, 2000);
};

const syncSelectedBase = (base: BaseItem) => {
  selectedBase.value = base;
  selectStore.updateSelectedBase({
    baseId: base.baseId,
    baseName: base.baseName,
    longitude: base.longitude,
    latitude: base.latitude,
  });
  currentGrowthStage.value = resolveFixedGrowthStage(base);
};

// 获取基地列表
const fetchBaseList = async () => {
  try {
    const res=await getBaseList();
    const baseDataList = res;
    // 存储完整的基地信息（包含ID和名称）
    baseList.value = baseDataList.map((item: any) => ({
      baseId: item.id,
      baseName: item.baseName || item.fullName,
      fullName: item.fullName,
      longitude: item.longitude || '',  // 添加经度信息
      latitude: item.latitude || ''    // 添加纬度信息
    }));
    if (baseList.value.length > 0) {
      const preferredBase = baseList.value.find(
        (item) => String(item.baseId) === String(selectStore.selectedBase?.baseId || '')
      ) || baseList.value[0];
      syncSelectedBase(preferredBase);
    }

  } catch (error) {
    console.error('获取基地列表错误：', error);
    showMessage('获取基地列表失败，请检查网络', true);
  }
};

// 组件挂载时加载基地列表
onMounted(() => {
  fetchBaseList();
});

// 切换下拉框显示状态
const toggleDropdown = (type: 'base') => {
  isDropdownOpen.value[type] = !isDropdownOpen.value[type];
};

// 选择下拉项
const selectItem = (type: 'base', value: string) => {
  if (type === 'base') {
    // 匹配选中的基地对象
    const matchedBase = baseList.value.find(item => item.baseName === value);
    if (matchedBase) {
      syncSelectedBase(matchedBase);
    }
  }
  // 关闭下拉框
  isDropdownOpen.value[type] = false;
};
</script>

<style scoped lang="less">
.header {
  position: relative;
  z-index: 1001;
}

.growth-stage-tag-container {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  width: 100%;
  box-sizing: border-box;
  height: 72px;
  padding: 10px 20px 8px;
  background-color: #fff;
  position: relative;
  z-index: 1001;

  .dropdown-group {
    display: flex;
    flex-direction: row;
    gap: 15px;

    .custom-select {
      position: relative;
      width: 120px;
      height: 36px;
      background-color: #fff;
      border: 1px solid #e0e0e0;
      border-radius: 6px;
      color: #333;
      cursor: pointer;
      font-size: 14px;

      .select-value {
        height: 100%;
        display: flex;
        align-items: center;
        padding: 0 12px;
        box-sizing: border-box;
      }

      .select-icon {
        position: absolute;
        right: 12px;
        top: 50%;
        transform: translateY(-50%);
        transition: transform 0.2s;
        font-size: 12px;
        color: #666;

        &.open {
          transform: translateY(-50%) rotate(180deg);
        }
      }

      .select-options {
        position: absolute;
        top: 100%;
        left: 0;
        right: 0;
        margin-top: 4px;
        background-color: #fff;
        border: 1px solid #e0e0e0;
        border-radius: 6px;
        box-shadow: 0 3px 10px rgba(0, 0, 0, 0.08);
        max-height: 200px;
        display: flex;
        flex-direction: column;
        z-index: 1002;
        overflow: hidden;

        .options-scroll {
          flex: 1;
          overflow-y: auto;
          padding: 4px 0;

          &::-webkit-scrollbar {
            width: 6px;
          }
          &::-webkit-scrollbar-thumb {
            background-color: #eee;
            border-radius: 3px;
          }

          .option-item {
            padding: 8px 12px;
            color: #333;
            &:hover {
              background-color: #f5f7fa;
            }
          }
        }

        .create-option {
          padding: 8px 12px;
          color: #409eff;
          cursor: pointer;
          border-top: 1px dashed #eee;
          &:hover {
            background-color: #ecf5ff;
          }
        }
      }
    }

    .base-select {
      width: 180px;
    }
  }

  .stage-tag-group {
    display: flex;
    align-items: center;
    gap: 0;
    padding-top: 0;

    .stage-tag {
      position: relative;
      width: 140px;
      text-align: center;
      padding: 10px 0 9px;
      color: #2f3a25;
      font-size: 14px;
      line-height: 18px;
      background: hsl(
        calc(60 + (var(--index) * 10)),
        100%,
        70%
      );
      transition: transform 0.2s ease, box-shadow 0.2s ease, color 0.2s ease;

      &.active {
        color: #7a1010;
        font-size: 15px;
        font-weight: 800;
        border: 2px solid #e53935;
        border-radius: 6px 0 0 6px;
        box-shadow: 0 8px 18px rgba(229, 57, 53, 0.24), 0 0 0 4px rgba(229, 57, 53, 0.08);
        transform: translateY(-2px);
        z-index: 4;

        &:after {
          top: -2px;
          right: -14px;
          border-top-width: 20px;
          border-bottom-width: 20px;
          border-left-width: 12px;
          border-left-color: #e53935;
        }
      }

      &:not(:last-child) {
        margin-right: 10px;
        z-index: 1;
      }

      &:after {
        content: '';
        position: absolute;
        right: -10px;
        top: 0;
        border-top: 18px solid transparent;
        border-bottom: 18px solid transparent;
        border-left: 10px solid hsl(
          calc(60 + (var(--index) * 10)),
          100%,
          70%
        );
        z-index: 2;
      }

      .stage-indicator {
        position: absolute;
        top: -23px;
        left: 50%;
        transform: translateX(-50%);
        display: inline-flex;
        flex-direction: row;
        align-items: center;
        gap: 5px;
        pointer-events: none;
        z-index: 5;

        .stage-indicator-arrow {
          position: relative;
          width: 14px;
          height: 18px;
          border-radius: 999px;
          background: #e53935;
          box-shadow: 0 3px 8px rgba(229, 57, 53, 0.35);

          &:before {
            content: '';
            position: absolute;
            top: -8px;
            left: 50%;
            transform: translateX(-50%);
            width: 0;
            height: 0;
            border-left: 11px solid transparent;
            border-right: 11px solid transparent;
            border-bottom: 14px solid #e53935;
          }
        }

        .stage-indicator-label {
          padding: 3px 10px;
          border-radius: 999px;
          border: 1px solid rgba(229, 57, 53, 0.24);
          background: #fff5f5;
          color: #d32f2f;
          font-size: 12px;
          font-weight: 700;
          line-height: 16px;
          box-shadow: 0 4px 10px rgba(229, 57, 53, 0.12);
          white-space: nowrap;
        }
      }

    }
  }
}

.main-content {
  position: relative;
  z-index: 5;
}

.create-dialog {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9999;

  .dialog-mask {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.5);
  }

  .dialog-content {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 500px;
    max-height: 90vh;
    background-color: #fff;
    border-radius: 6px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    overflow: hidden;
    z-index: 1;

    .dialog-header {
      padding: 16px;
      border-bottom: 1px solid #eee;
      display: flex;
      justify-content: space-between;
      align-items: center;

      h3 {
        margin: 0;
        font-size: 18px;
      }

      .close-btn {
        width: 24px;
        height: 24px;
        border: none;
        background: none;
        font-size: 20px;
        cursor: pointer;
        &:hover {
          color: #f56c6c;
        }
      }
    }

    .dialog-form {
      padding: 16px;
      max-height: calc(90vh - 130px);
      overflow-y: auto;

      .form-item {
        margin-bottom: 16px;
        display: flex;
        flex-direction: column;

        label {
          margin-bottom: 6px;
          font-size: 14px;
          color: #606266;

          .required {
            color: #f56c6c;
          }
        }

        input, select, textarea {
          padding: 8px 10px;
          border: 1px solid #dcdcdc;
          border-radius: 4px;
          font-size: 14px;
          &:focus {
            outline: none;
            border-color: #409eff;
          }
        }

        textarea {
          resize: none;
        }

        .soil-types {
          display: flex;
          gap: 16px;
          align-items: center;
          padding: 4px 0;

          label {
            margin-bottom: 0;
            display: flex;
            align-items: center;
            cursor: pointer;
          }

          input {
            margin-right: 4px;
          }
        }

        .upload-container {
          display: flex;
          align-items: center;
          gap: 16px;
          margin-top: 4px;

          .upload-btn {
            .btn-text {
              padding: 8px 16px;
              border: 1px dashed #dcdcdc;
              border-radius: 4px;
              color: #606266;
              cursor: pointer;
              transition: all 0.2s;

              &:hover {
                border-color: #409eff;
                color: #409eff;
              }
            }
          }

          .image-preview {
            position: relative;
            width: 80px;
            height: 80px;
            border-radius: 4px;
            overflow: hidden;
            border: 1px solid #eee;

            img {
              width: 100%;
              height: 100%;
              object-fit: cover;
            }

            .delete-img {
              position: absolute;
              top: 2px;
              right: 2px;
              width: 18px;
              height: 18px;
              border: none;
              border-radius: 50%;
              background-color: rgba(0, 0, 0, 0.5);
              color: #fff;
              font-size: 12px;
              cursor: pointer;
              display: flex;
              align-items: center;
              justify-content: center;
              &:hover {
                background-color: #f56c6c;
              }
            }
          }
        }
      }
    }

    .dialog-footer {
      padding: 12px 16px;
      border-top: 1px solid #eee;
      display: flex;
      justify-content: center;
      gap: 20px;

      button {
        padding: 8px 20px;
        border-radius: 4px;
        cursor: pointer;
        font-size: 14px;
        border: none;
        transition: background-color 0.2s;
      }

      .create-btn {
        background-color: #67c23a;
        color: #fff;
        &:hover {
          background-color: #5daf34;
        }
      }

      .cancel-btn {
        background-color: #fff;
        color: #606266;
        border: 1px solid #dcdcdc;
        &:hover {
          background-color: #f5f7fa;
        }
      }
    }
  }
}
</style>




