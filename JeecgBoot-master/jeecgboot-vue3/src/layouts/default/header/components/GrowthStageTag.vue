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
            <div class="create-option" @click.stop="openCreateDialog">
              创建
            </div>
          </div>
        </div>

        <!-- 地块下拉框（已与基地下拉框结构对齐） -->
        <div class="custom-select" @click="toggleDropdown('plot')">
          <div class="select-value">{{ selectedPlot?.plotName || '请选择地块' }}</div>
          <div class="select-icon" :class="{ open: isDropdownOpen.plot }">▼</div>
          <div class="select-options" v-if="isDropdownOpen.plot">
            <div class="options-scroll">
              <div
                class="option-item"
                v-for="item in plotList"
                :key="item.plotId"
                @click.stop="selectItem('plot', item)"
              >
                {{ item.plotName }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="stage-tag-group">
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

  <!-- 创建基地弹窗 -->
  <div class="create-dialog" v-if="isDialogOpen">
    <div class="dialog-mask" @click.stop></div>
    <div class="dialog-content">
      <div class="dialog-header">
        <h3>创建基地</h3>
        <button class="close-btn" @click="isDialogOpen = false">×</button>
      </div>
      <div class="dialog-form">
        <div class="form-item">
          <label>全称 <span class="required">*</span></label>
          <input type="text" v-model="formData.fullName" placeholder="请输入基地全称">
        </div>
        <div class="form-item">
          <label>基地负责人 <span class="required">*</span></label>
          <input type="text" v-model="formData.manager" placeholder="请输入负责人姓名">
        </div>
        <div class="form-item">
          <label>电话 <span class="required">*</span></label>
          <input type="text" v-model="formData.phone" placeholder="请输入联系电话">
        </div>
        <div class="form-item">
          <label>地址</label>
          <textarea v-model="formData.address" placeholder="请输入基地地址" rows="2"></textarea>
        </div>
        <div class="form-item">
          <label>面积</label>
          <div class="area-input-group">
            <input type="number" v-model.number="formData.area" placeholder="请输入面积" min="0">
            <span>亩</span>
          </div>
        </div>
        <div class="form-item">
          <label>土壤状况</label>
          <div class="soil-types">
            <label>
              <input type="radio" v-model="formData.soilType" value="黏土"> 黏土
            </label>
            <label>
              <input type="radio" v-model="formData.soilType" value="沙土"> 沙土
            </label>
            <label>
              <input type="radio" v-model="formData.soilType" value="壤土"> 壤土
            </label>
          </div>
        </div>
        <div class="form-item">
          <label>基地图片 <span class="required">*</span></label>
          <div class="upload-container">
            <label class="upload-btn">
              <input
                type="file"
                accept="image/jpeg,image/png,image/jpg"
                @change="handleImageUpload"
                hidden
                ref="fileInput"
              >
              <div class="btn-text">点击选择图片</div>
            </label>
            <div class="image-preview" v-if="formData.imageUrl">
              <img :src="formData.imageUrl" alt="基地图片">
              <button class="delete-img" @click.stop="clearImage">×</button>
            </div>
          </div>
        </div>
      </div>
      <div class="dialog-footer">
        <button class="create-btn" @click="handleCreate">创建</button>
        <button class="cancel-btn" @click="isDialogOpen = false">返回</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {ref, reactive, onMounted, watch} from 'vue';
import axios from 'axios';
import { useSelectStore } from '../../../../store/selectStore';
import {getBaseList, getPlotsByBaseId,getPlotById,createBase} from '../../../../views/rapeseed/production-plan/plot-production-plan/base.api';

// 定义基地类型接口
interface BaseItem {
  baseId: string | number;
  baseName: string;
  fullName?: string;
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
  '未播种', '已播种', '苗期', '蕾薹期', '开花期', '角果成熟期', '收获与整地'
]);

// 弹窗状态与表单数据
const isDialogOpen = ref(false);
const fileInput = ref<HTMLInputElement>(null);
const formData = reactive({
  fullName: '',
  manager: '',
  phone: '',
  address: '',
  area: 0,
  soilType: '黏土',
  imageUrl: '',
  imageBase64: ''
});

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
      fullName: item.fullName
    }));
    if (baseList.value.length > 0) {
      selectedBase.value = baseList.value[0];
      selectStore.updateSelectedBase({
        baseId: selectedBase.value.baseId,
        baseName: selectedBase.value.baseName
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
    plotList.value = res.map((item: any) => ({
      plotId: item.id, // 按接口实际字段名（如id/plot_id）调整
      plotName: item.plotName  // 按接口实际字段名调整
    }));
    // 选中第一个地块（若有）
    if (plotList.value.length > 0) {
      selectedPlot.value = plotList.value[0]; // 存储完整对象
      // 同步到全局状态（后续步骤会修改全局方法）
      selectStore.updateSelectedPlot(plotList.value[0]);
      fetchPlotGrowthStage(plotList.value[0].plotId);
    } else {
      plotList.value = [];
      selectedPlot.value = null;
      selectStore.updateSelectedPlot(null);
      currentGrowthStage.value = '';
    }
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
      selectStore.updateSelectedBase({
        baseId: matchedBase.baseId,
        baseName: matchedBase.baseName
      });
      // 切换基地后重新加载地块
      fetchPlotList();
    }
  } else {
    // 匹配选中的地块对象
    const matchedPlot = plotList.value.find(item => item.plotName === value);
    if (matchedPlot) {
      selectedPlot.value = matchedPlot;
      // 传递完整的地块对象而不是字符串
      selectStore.updateSelectedPlot(matchedPlot);
    }
  }
  isDropdownOpen.value[type] = false;
};

// 打开创建弹窗
const openCreateDialog = () => {
  isDropdownOpen.value.base = false;
  isDialogOpen.value = true;
  resetForm();
};

// 处理图片上传
const handleImageUpload = (e: Event) => {
  const target = e.target as HTMLInputElement;
  const file = target.files?.[0];
  if (!file) return;

  const validTypes = ['image/jpeg', 'image/png', 'image/jpg'];
  if (!validTypes.includes(file.type)) {
    showMessage('请选择 JPG 或 PNG 格式的图片', true);
    target.value = '';
    return;
  }

  if (file.size > 5 * 1024 * 1024) {
    showMessage('图片大小不能超过 5MB', true);
    target.value = '';
    return;
  }

  const reader = new FileReader();
  reader.onload = (event) => {
    const base64Str = (event.target?.result as string).split(',')[1];
    formData.imageBase64 = base64Str;
    formData.imageUrl = URL.createObjectURL(file);
  };
  reader.readAsDataURL(file);
};

// 清除已选图片
const clearImage = () => {
  formData.imageUrl = '';
  formData.imageBase64 = '';
  if (fileInput.value) {
    fileInput.value.value = '';
  }
};

// 重置表单
const resetForm = () => {
  formData.fullName = '';
  formData.manager = '';
  formData.phone = '';
  formData.address = '';
  formData.area = 0;
  formData.soilType = '黏土';
  clearImage();
};

// 处理创建逻辑
const handleCreate = async () => {
  if (!formData.fullName.trim()) {
    showMessage('请填写基地全称', true);
    return;
  }
  if (formData.fullName.length > 50) {
    showMessage('基地全称不能超过50个字符', true);
    return;
  }
  if (!formData.manager.trim()) {
    showMessage('请填写基地负责人', true);
    return;
  }
  if (!formData.phone.trim()) {
    showMessage('请填写联系电话', true);
    return;
  }
  if (!formData.address.trim()) {
    showMessage('请填写基地地址', true);
    return;
  }
  if (formData.address.length > 200) {
    showMessage('地址不能超过200个字符', true);
    return;
  }
  if (formData.area < 0) {
    showMessage('面积不能为负数', true);
    return;
  }

  try {
    const submitData = {
      baseName: formData.fullName.trim(),
      manager: formData.manager.trim() || null,
      phone: formData.phone.trim() || null,
      address: formData.address.trim(),
      area: formData.area > 0 ? formData.area : null,
      soilType: formData.soilType,
      topViewUrl: formData.imageBase64 || null
    };

    const res=await createBase(submitData);
    showMessage('基地创建成功');
      // 重新加载基地列表（确保能获取到新基地的ID）
      fetchBaseList();
      isDialogOpen.value = false;
  } catch (error) {
    console.error('创建基地请求错误：', error);
    showMessage('网络请求失败，请检查接口地址或Token', true);
  }
};
// 根据地块ID查询生长阶段
const fetchPlotGrowthStage = async (plotId: string | number) => {
  try {
    const res = await getPlotById(plotId); // 后端接口：按plotId查地块详情
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
    if (newPlotId) {
      fetchPlotGrowthStage(newPlotId);
    } else {
      currentGrowthStage.value = ''; // 未选地块时清空
    }
  },
  { immediate: true } // 初始加载时执行
);
</script>

<style scoped lang="less">
.header {
  position: relative;
  z-index: 10;
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
        height: 200px;
        display: flex;
        flex-direction: column;
        z-index: 20;
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
