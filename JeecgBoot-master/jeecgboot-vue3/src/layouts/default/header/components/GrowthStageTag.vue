<template>
  <div class="header">
    <div class="growth-stage-tag-container">
      <div class="dropdown-group">
        <!-- 基地下拉框 -->
        <div class="custom-select" @click="toggleDropdown('base')">
          <div class="select-value">{{ selectedBase || '请选择基地' }}</div>
          <div class="select-icon" :class="{ open: isDropdownOpen.base }">▼</div>
          <div class="select-options" v-if="isDropdownOpen.base">
            <div class="options-scroll">
              <div
                class="option-item"
                v-for="item in baseList"
                :key="item"
                @click.stop="selectItem('base', item)"
              >
                {{ item }}
              </div>
            </div>
            <div class="create-option" @click.stop="openCreateDialog">
              创建
            </div>
          </div>
        </div>

        <!-- 地块下拉框 -->
        <div class="custom-select" @click="toggleDropdown('plot')">
          <div class="select-value">{{ selectedPlot || '请选择地块' }}</div>
          <div class="select-icon" :class="{ open: isDropdownOpen.plot }">▼</div>
          <div class="select-options" v-if="isDropdownOpen.plot">
            <div
              class="option-item"
              v-for="item in plotList"
              :key="item"
              @click.stop="selectItem('plot', item)"
            >
              {{ item }}
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
          <label>种植作物</label>
          <select v-model="formData.crop">
            <option value="中油杂19">中油杂19</option>
            <option value="其他">其他</option>
          </select>
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
import { ref, reactive, onMounted, Ref, unref } from 'vue';
import axios from 'axios';
import { useSelectStore } from '../../../../store/selectStore';

// 下拉框数据
const baseList = ref<string[]>([]);
const plotList = ref(['地块A', '地块B']);

// 状态仓库实例
const selectStore = useSelectStore();

// 选中值
const selectedBase = ref('');
const selectedPlot = ref('地块A');

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
const fileInput = ref<HTMLInputElement>(null); // 文件输入框引用
const formData = reactive({
  fullName: '',
  manager: '',
  phone: '',
  address: '',
  area: 0,
  crop: '中油杂19',
  soilType: '黏土',
  imageUrl: '',
  imageBase64: '' // 新增：存储图片Base64字符串（无前缀）
});

// 基础提示函数（无外部依赖）
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
    const res = await axios.get('/jeecgboot/youcai/bases/getAllBases', {
      headers: {
        'X-Access-Token': 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6ImFkbWluIiwiZXhwIjoxNzYyMDI4OTg2fQ.Ldq9jbKiSjBV86GAKbFx6ZnzK6m6QD4ohLSMB_vtmd4'
      },
      timeout: 10000
    });

    if (res.data?.success) {
      const baseDataList = res.data.result || [];
      baseList.value = baseDataList.map((item: any) => item.baseName || item.fullName);
      if (baseList.value.length > 0) {
        selectedBase.value = baseList.value[0];
      }
    } else {
      showMessage('获取基地列表失败：' + (res.data?.message || '未知错误'), true);
    }
  } catch (error) {
    console.error('获取基地列表错误：', error);
    showMessage('获取基地列表失败，请检查网络', true);
  }
};

// 组件挂载时加载数据
onMounted(() => {
  fetchBaseList().then(() => {
    if (selectedBase.value) selectStore.updateSelectedBase(selectedBase.value);
    if (selectedPlot.value) selectStore.updateSelectedPlot(selectedPlot.value);
  });
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
    selectedBase.value = value;
    selectStore.updateSelectedBase(value);
  } else {
    selectedPlot.value = value;
    selectStore.updateSelectedPlot(value);
  }
  isDropdownOpen.value[type] = false;
};

// 打开创建弹窗
const openCreateDialog = () => {
  isDropdownOpen.value.base = false;
  isDialogOpen.value = true;
  resetForm(); // 打开时重置表单
};

// 处理图片上传
const handleImageUpload = (e: Event) => {
  const target = e.target as HTMLInputElement;
  const file = target.files?.[0];
  if (!file) return;

  // 校验图片格式
  const validTypes = ['image/jpeg', 'image/png', 'image/jpg'];
  if (!validTypes.includes(file.type)) {
    showMessage('请选择 JPG 或 PNG 格式的图片', true);
    target.value = '';
    return;
  }

  // 校验图片大小（5MB以内）
  if (file.size > 5 * 1024 * 1024) {
    showMessage('图片大小不能超过 5MB', true);
    target.value = '';
    return;
  }
  // 新增：转为Base64字符串（去掉data:...前缀）
  const reader = new FileReader();
  reader.onload = (event) => {
    const base64Str = (event.target?.result as string).split(',')[1]; // 关键：分割前缀
    formData.imageBase64 = base64Str; // 存储处理后的Base64
    formData.imageUrl = URL.createObjectURL(file); // 保留预览
  };
  reader.readAsDataURL(file); // 触发转换
};

// 清除已选图片
const clearImage = () => {
  formData.imageUrl = '';
  formData.imageBase64 = '';
  if (fileInput.value) {
    fileInput.value.value = ''; // 重置文件输入
  }
};

// 重置表单
const resetForm = () => {
  formData.fullName = '';
  formData.manager = '';
  formData.phone = '';
  formData.address = '';
  formData.area = 0;
  formData.crop = '中油杂19';
  formData.soilType = '黏土';
  clearImage();
};

// 处理创建逻辑（核心功能）
const handleCreate = async () => {
  // 表单校验
  // 1. 基地名称（base_name）：非空+长度限制
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
  // if (!formData.imageBase64) {
  //   showMessage('请选择基地图片', true);
  //   return;
  // }
  // 2. 地址（address）：非空+长度限制
  if (!formData.address.trim()) {
    showMessage('请填写基地地址', true);
    return;
  }
  if (formData.address.length > 200) {
    showMessage('地址不能超过200个字符', true);
    return;
  }
  // 3. 面积（area）：非空（后端默认0.00，但前端建议填值）
  if (formData.area < 0) {
    showMessage('面积不能为负数', true);
    return;
  }

  try {
    // 构建表单数据（支持文件上传）
    // 1. 构建JSON格式数据（用imageBase64字段）
    const submitData = {
      baseName: formData.fullName.trim(),  // 对应实体类 baseName
      manager: formData.manager.trim() || null,
      phone: formData.phone.trim() || null,
      address: formData.address.trim(),
      area: formData.area > 0 ? formData.area : null,  // 避免传0，若需0则保留
      crop: formData.crop,
      soilType: formData.soilType,         // 对应实体类 soilType
      topViewUrl: formData.imageBase64 || null  // 对应实体类 topViewUrl
    };
    // 调用后端接口
    const res = await axios.post('/jeecgboot/youcai/bases/add', submitData, {
      headers: {
        'X-Access-Token': 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6ImFkbWluIiwiZXhwIjoxNzYyMDI4OTg2fQ.Ldq9jbKiSjBV86GAKbFx6ZnzK6m6QD4ohLSMB_vtmd4',
        'Content-Type': 'application/json'
      },
      timeout: 30000 // 30秒超时
    });

    // 处理接口响应
    if (res.data?.success) {
      showMessage('基地创建成功');
      // 更新基地列表
      baseList.value.push(formData.fullName.trim());
      selectedBase.value = formData.fullName.trim();
      selectStore.updateSelectedBase(selectedBase.value);
      // 关闭弹窗
      isDialogOpen.value = false;
    } else {
      showMessage('创建失败：' + (res.data?.message || '服务器异常'), true);
    }
  } catch (error) {
    console.error('创建基地请求错误：', error);
    showMessage('网络请求失败，请检查接口地址或Token', true);
  }
};
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
