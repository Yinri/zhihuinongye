<template>
  <div class="header">
    <div class="growth-stage-tag-container">
      <div class="dropdown-group">
        <!-- 基地下拉框：拆分选项滚动区和固定按钮区 -->
        <div class="custom-select" @click="toggleDropdown('base')">
          <div class="select-value">{{ selectedBase || '请选择基地' }}</div>
          <div class="select-icon" :class="{ open: isDropdownOpen.base }">▼</div>
          <div class="select-options" v-if="isDropdownOpen.base">
            <!-- 仅基地选项滚动的容器 -->
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
            <!-- 固定在底部的创建按钮 -->
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

  <!-- 弹窗：正方形调整 + 单选土壤 + 按钮居中 -->
  <div class="create-dialog" v-if="isDialogOpen">
    <div class="dialog-mask" @click.stop></div>
    <div class="dialog-content">
      <div class="dialog-header">
        <h3>创建基地</h3>
        <button class="close-btn" @click="isDialogOpen = false">×</button>
      </div>
      <div class="dialog-form">
        <div class="form-item">
          <label>简称</label>
          <input type="text" v-model="formData.shortName" placeholder="请输入">
        </div>
        <div class="form-item">
          <label>全称</label>
          <input type="text" v-model="formData.fullName" placeholder="请输入">
        </div>
        <div class="form-item">
          <label>基地负责人</label>
          <input type="text" v-model="formData.manager" placeholder="请输入">
        </div>
        <div class="form-item">
          <label>电话</label>
          <input type="text" v-model="formData.phone" placeholder="请输入">
        </div>
        <div class="form-item">
          <label>地址</label>
          <textarea v-model="formData.address" placeholder="请输入" rows="2"></textarea>
        </div>
        <div class="form-item">
          <label>面积</label>
          <div class="area-input-group">
            <input type="number" v-model.number="formData.area" placeholder="请输入">
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
          <!-- 改为单选按钮组 -->
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
      </div>
      <div class="dialog-footer">
        <button class="create-btn" @click="handleCreate">创建</button>
        <button class="cancel-btn" @click="isDialogOpen = false">返回</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import axios from 'axios';

// 下拉框数据（从接口获取）
const baseList = ref<string[]>([]);
const plotList = ref(['地块A', '地块B']); // 地块数据可按需从接口获取

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

// 弹窗状态与表单数据（土壤状况改为单选）
const isDialogOpen = ref(false);
const formData = reactive({
  shortName: '',
  fullName: '',
  manager: '',
  phone: '',
  address: '',
  area: 0,
  crop: '中油杂19',
  soilType: '黏土' // 单选值
});

// 异步获取基地列表
const fetchBaseList = async () => {
  try {
    // 替换为实际后端接口地址，示例假设返回格式为 ['基地a', '基地b']
    const res = await axios.get('/jeecg-boot/youcai/bases/queryById');
    baseList.value = res.data;
    // 若需要默认选中第一个基地，可添加以下逻辑
    if (baseList.value.length > 0) {
      selectedBase.value = baseList.value[0];
    }
  } catch (error) {
    console.error('获取基地列表失败', error);
  }
};

// 组件挂载时加载基地列表
onMounted(() => {
  fetchBaseList();
});

// 切换下拉框显示/隐藏
const toggleDropdown = (type: 'base' | 'plot') => {
  Object.keys(isDropdownOpen.value).forEach(key => {
    if (key !== type) {
      isDropdownOpen.value[key as 'base' | 'plot'] = false;
    }
  });
  isDropdownOpen.value[type] = !isDropdownOpen.value[type];
};

// 选择下拉项
const selectItem = (type: 'base' | 'plot', value: string) => {
  if (type === 'base') {
    selectedBase.value = value;
  } else {
    selectedPlot.value = value;
  }
  isDropdownOpen.value[type] = false;
};

// 打开创建弹窗
const openCreateDialog = () => {
  isDropdownOpen.value.base = false;
  isDialogOpen.value = true;
};

// 处理创建逻辑（可在此处调用后端创建接口）
const handleCreate = () => {
  console.log('提交表单数据：', formData);
  // 示例：调用后端创建接口后更新基地列表
  baseList.value.push(formData.shortName);
  selectedBase.value = formData.shortName;
  isDialogOpen.value = false;
};
</script>

<style scoped lang="less">
/* 原有样式保持不变 */
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
  height: 60px;
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
        /* 下拉框整体固定高度 */
        height: 200px;
        /* flex布局拆分滚动区和按钮区 */
        display: flex;
        flex-direction: column;
        z-index: 20;
        overflow: hidden; /* 隐藏整体滚动条 */

        /* 仅基地选项的滚动容器 */
        .options-scroll {
          flex: 1; /* 占满下拉框除按钮外的所有高度 */
          overflow-y: auto; /* 仅选项区滚动 */
          padding: 4px 0;

          /* 滚动条样式优化（可选） */
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

        /* 固定在底部的创建按钮 */
        .create-option {
          padding: 8px 12px;
          color: #409eff;
          cursor: pointer;
          border-top: 1px dashed #eee; /* 分隔线区分选项和按钮 */
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

// 弹窗样式调整
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
    height: 650px; /* 固定高度，接近正方形 */
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
      height: calc(100% - 130px); /* 控制表单高度，避免溢出 */
      overflow-y: auto; /* 内容过多时可滚动 */

      .form-item {
        margin-bottom: 12px; /* 减小间距，缩短整体长度 */
        display: flex;
        flex-direction: column;

        label {
          margin-bottom: 6px;
          font-size: 14px;
          color: #606266;
        }

        input, select, textarea {
          padding: 6px 8px; /* 减小内边距 */
          border: 1px solid #dcdcdc;
          border-radius: 4px;
          font-size: 14px;
          &:focus {
            outline: none;
            border-color: #409eff;
          }
        }

        textarea {
          resize: none; /* 禁止拉伸 */
        }

        .soil-types {
          display: flex;
          gap: 16px;
          align-items: center;

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
      }
    }

    .dialog-footer {
      padding: 12px 16px;
      border-top: 1px solid #eee;
      display: flex;
      justify-content: center; /* 按钮居中 */
      gap: 20px;

      button {
        padding: 6px 16px;
        border-radius: 4px;
        cursor: pointer;
        font-size: 14px;
        border: none;
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
