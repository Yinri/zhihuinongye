<template>
  <div class="header">
    <div class="growth-stage-tag-container">
      <div class="dropdown-group">
        <!-- 基地下拉下拉框 -->
        <div class="custom-select" @click="toggleDropdown('base')">
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

        <!-- 地块下拉框（已与基地下拉框结构对齐） -->
        <div class="custom-select" v-if="!hidePlotDropdown" @click="toggleDropdown('plot')">
          <div class="select-value">{{ selectedPlot?.plotName || '请选择地块' }}</div>
          <div class="select-icon" :class="{ open: isDropdownOpen.plot }">▼</div>
          <div class="select-options" v-if="isDropdownOpen.plot">
            <div class="options-scroll">
              <div
                class="option-item"
                v-for="item in plotList"
                :key="item.plotId"
                @click.stop="selectItem('plot', item.plotName)"
              >
                {{ item.plotName }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="stage-tag-group" v-if="!hideGrowthStage && selectedPlot?.plotId && selectedPlot?.plotId !== 'all'">
        <div
          class="stage-tag"
          v-for="(stage, index) in stageList"
          :key="stage"
          :style="{'--index': index}"
          :class="{ active: currentGrowthStage === stage }"
        >
          {{ stage }}
        </div>
      </div>

    </div>
  </div>

  <div class="main-content"></div>

</template>

<script setup lang="ts">
import {ref, reactive, onMounted, watch, computed} from 'vue';
import { useSelectStore } from '../../../../store/selectStore';
import {getBaseList, getPlotsByBaseId,getPlotById} from '../../../../views/rapeseed/production-plan/center/base.api';
import { useRoute } from 'vue-router';

const route = useRoute();

const hideGrowthStage = computed(() => {
  const currentPath = route.path;
  return currentPath.includes('/rapeseed/production-plan/center');
});

const hidePlotDropdown = computed(() => {
  const currentPath = route.path;
  return currentPath.includes('/dashboard/rapeseed') ||
         currentPath.includes('/rapeseed/work-area') ||
         currentPath.includes('/rapeseed/lodging-risk') ||
         currentPath.includes('/rapeseed/harvest/index') ||
         currentPath.includes('/rapeseed/base-info') ||
         currentPath.includes('/rapeseed/plot-info') ||
         currentPath.includes('/rapeseed/variety-info') ||
         currentPath.includes('/rapeseed/fertilizer-info') ||
         currentPath.includes('/rapeseed/pesticide-info') ||
         currentPath.includes('/rapeseed/harvest/machine') ||
         currentPath.includes('/rapeseed/harvest/task') ||
         currentPath.includes('/rapeseed/farming-records')||
         currentPath.includes('/rapeseed/pest-control')||
         currentPath.includes('/rapeseed/disease-control')||
         currentPath.includes('/rapeseed/production-plan')
         ;

});

// 定义基地类型接口
interface BaseItem {
  baseId: string | number;
  baseName: string;
  fullName?: string;
  longitude?: string;  // 添加经度字段
  latitude?: string;   // 添加纬度字段
}

// 下拉框数据（基地和地块均从接口获取）
const baseList = ref<BaseItem[]>([]);
const plotList = ref<{ plotId: string | number; plotName: string }[]>([]);

// 存储当前地块的生长阶段
const currentGrowthStage = ref('');

// 状态仓库实例
const selectStore = useSelectStore();

// 选中值
const selectedBase = ref<BaseItem | null>(null);
const selectedPlot = ref<{ plotId: string | number; plotName: string } | null>(null);

// 下拉框状态
const isDropdownOpen = ref({
  base: false,
  plot: false
});

// 生育阶段数据
const stageList = ref([
  '未播种', '已播种', '苗期', '蕾薹期', '开花期', '角果成熟期', '收获'
]);

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
      selectedBase.value = baseList.value[3];
      selectStore.updateSelectedBase({
        baseId: selectedBase.value.baseId,
        baseName: selectedBase.value.baseName,
        longitude: selectedBase.value.longitude,  // 添加经度
        latitude: selectedBase.value.latitude     // 添加纬度
      });
      // 加载第一个基地的地块
      fetchPlotList();
    }

  } catch (error) {
    console.error('获取基地列表错误：', error);
    showMessage('获取基地列表失败，请检查网络', true);
  }
};

// 获取当前选中基地的ID
const getSelectedBaseId = (): string | number | null => {
  if (!selectedBase.value) return null;
  // 从基地列表中匹配选中的基地ID（双重校验）
  const matchedBase = baseList.value.find(
    item => item.baseName === selectedBase.value?.baseName
  );
  return matchedBase ? matchedBase.baseId : null;
};

// 获取地块列表（根据当前选中的基地ID）
const fetchPlotList = async () => {
  const currentBaseId = getSelectedBaseId();
  if (!currentBaseId) {
    plotList.value = [];
    selectedPlot.value = null;
    selectStore.updateSelectedPlot(null);
    currentGrowthStage.value = ''; // 清空生长阶段
    return;
  }
  try {
    const res = await getPlotsByBaseId(currentBaseId);
    // 添加"全部地块"选项作为第一个选项
    plotList.value = [
      { plotId: 'all', plotName: '全部地块' },
      ...res.map((item: any) => ({
        plotId: String(item.id),
        plotName: item.plotName
      }))
    ];
    // 默认选择"全部地块"
    selectedPlot.value = plotList.value[0];
    selectStore.updateSelectedPlot(plotList.value[0]);
    // 不获取生长阶段，因为选择的是"全部地块"
    currentGrowthStage.value = '';
  } catch (error) {
    console.error('获取地块列表错误：', error);
    showMessage('获取地块列表失败，请检查网络', true);
    currentGrowthStage.value = '';
  }
};

// 组件挂载时加载基地列表
onMounted(() => {
  fetchBaseList();
});

// 切换下拉框显示状态
const toggleDropdown = (type: 'base' | 'plot') => {
  Object.keys(isDropdownOpen.value).forEach(key => {
    if (key !== type) isDropdownOpen.value[key as 'base' | 'plot'] = false;
  });
  isDropdownOpen.value[type] = !isDropdownOpen.value[type];
};

// 选择下拉项
const selectItem = (type: 'base' | 'plot', value: string) => {
  if (type === 'base') {
    // 匹配选中的基地对象
    const matchedBase = baseList.value.find(item => item.baseName === value);
    if (matchedBase) {
      selectedBase.value = matchedBase;
      // 更新全局状态，包含经纬度信息
      selectStore.updateSelectedBase({
        baseId: matchedBase.baseId,
        baseName: matchedBase.baseName,
        longitude: matchedBase.longitude,  // 添加经度
        latitude: matchedBase.latitude     // 添加纬度
      });
      // 重置地块选择
      selectedPlot.value = null;
      selectStore.updateSelectedPlot(null);
      currentGrowthStage.value = '';
      // 加载该基地的地块
      fetchPlotList();
    }
  } else if (type === 'plot') {
    // 匹配选中的地块对象
    const matchedPlot = plotList.value.find(item => item.plotName === value);
    if (matchedPlot) {
      selectedPlot.value = matchedPlot;
      // 同步到全局状态
      selectStore.updateSelectedPlot({
        plotId: String(matchedPlot.plotId), // 关键：转为字符串
        plotName: matchedPlot.plotName
      });
      
      // 只有在选择了具体地块时才获取生长阶段
      if (matchedPlot.plotId !== 'all') {
        // 获取地块生长阶段
        fetchPlotGrowthStage(matchedPlot.plotId);
      } else {
        // 选择"全部地块"时清空生长阶段
        currentGrowthStage.value = '';
      }
    }
  }
  // 关闭下拉框
  isDropdownOpen.value[type] = false;
};

// 打开创建弹窗
// 根据地块ID查询生长阶段
const fetchPlotGrowthStage = async (plotId: string | number) => {
  try {
    const res = await getPlotById(plotId); // 后端接口：按plotId查地块详情
    console.log(plotId);
    currentGrowthStage.value = res.growthStage || ''; // 假设接口返回result.growthStage
  } catch (error) {
    console.error('获取地块生长阶段失败：', error);
    currentGrowthStage.value = '';
  }
};

// 监听选中地块变化，实时更新生长阶段
watch(
  () => selectedPlot.value?.plotId,
  (newPlotId) => {
    if (newPlotId && newPlotId !== 'all') {
      fetchPlotGrowthStage(newPlotId);
    } else {
      currentGrowthStage.value = ''; // 未选地块或选择"全部地块"时清空
    }
  },
  { immediate: true } // 初始加载时执行
);
</script>

<style scoped lang="less">
.header {
  position: relative;
  z-index: 1001;
}

.growth-stage-tag-container {
  display: flex;
  align-items: center;
  gap: 20px;
  width: 100%;
  box-sizing: border-box;
  height: 50px;
  padding: 0 20px;
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
  }

  .stage-tag-group {
    display: flex;
    align-items: center;
    gap: 0;

    .stage-tag {
      position: relative;
      width: 100px;
      text-align: center;
      padding: 8px 0;
      color: #333;
      font-size: 14px;
      background: hsl(
        calc(60 + (var(--index) * 10)),
        100%,
        70%
      );
      &.active {
        font-weight: 700; // 字体加粗（700=bold）
        border: 2px solid #2d8cf0; // 蓝色外边框（醒目不突兀）
        border-radius: 4px; // 边框圆角，更美观
        box-shadow: 0 0 10px rgba(45, 140, 240, 0.3); // 淡蓝色阴影，增强层次感

        // 箭头颜色同步原背景色，避免边框冲突
        &:after {
          border-left-color: hsl(calc(60 + (var(--index) * 10)),
          100%,
          70%);
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




