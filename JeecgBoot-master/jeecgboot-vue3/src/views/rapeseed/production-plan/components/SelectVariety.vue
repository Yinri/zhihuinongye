<template>
  <div class="crop-variety-wrapper">
    <div class="crop-variety-selector">
      <!-- 文字标签 -->
      <span class="variety-label">当前作物品种</span>

      <!-- 按钮容器 -->
      <div class="variety-buttons">
        <button
          v-for="variety in varieties"
          :key="variety.value"
          :class="['variety-btn', { 'active': selectedVariety === variety.value }]"
          @click="selectVariety(variety.value)"
        >
          {{ variety.label }}
        </button>
        <button class="add-btn" @click="showAddDialog = true">+</button>
      </div>

      <!-- 新增品种弹窗 -->
      <a-modal
        title="添加作物品种"
        v-model:visible="showAddDialog"
        ok-text="确认添加"
        cancel-text="取消"
        @ok="handleAddVariety"
        class="custom-modal"
      >
        <a-form-model
          ref="varietyForm"
          :model="newVariety"
          :rules="rules"
          layout="vertical"
        >
          <a-form-model-item
            label="品种名称"
            prop="label"
            class="form-item"
          >
            <a-input
              v-model="newVariety.label"
              placeholder="请输入作物品种名称"
              class="variety_input"
            />
          </a-form-model-item>
          <a-form-model-item
            label="品种标识"
            prop="value"
            class="form-item"
          >
            <a-input
              v-model="newVariety.value"
              placeholder="请输入品种标识（英文/数字）"
              class="variety_input"
            />
          </a-form-model-item>
        </a-form-model>
      </a-modal>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      // 品种列表
      varieties: [
        {label: '中油杂 19', value: 'zhongyouza19'},
        {label: '华油杂 62', value: 'huayouza62'},
        {label: '阳光 2009', value: 'yangguang2009'}
      ],
      // 当前选中品种
      selectedVariety: 'yangguang2009',
      // 弹窗显示状态
      showAddDialog: false,
      // 新品种表单数据
      newVariety: {
        label: '',
        value: ''
      },
      // 表单验证规则
      rules: {
        label: [
          {required: true, message: '请输入品种名称', trigger: 'blur'}
        ],
        value: [
          {required: true, message: '请输入品种标识', trigger: 'blur'},
          {pattern: /^[a-zA-Z0-9]+$/, message: '标识只能包含英文和数字', trigger: 'blur'}
        ]
      }
    };
  },
  methods: {
    // 选择品种
    selectVariety(value) {
      this.selectedVariety = value;
    },
    // 处理添加品种
    handleAddVariety() {
      this.$refs.varietyForm.validate(valid => {
        if (valid) {
          // 检查是否已存在相同标识的品种
          const exists = this.varieties.some(v => v.value === this.newVariety.value);
          if (exists) {
            this.$message.error('该品种标识已存在，请更换');
            return;
          }

          // 添加新品种
          this.varieties.push({...this.newVariety});
          // 自动选中新添加的品种
          this.selectedVariety = this.newVariety.value;
          // 关闭弹窗并重置表单
          this.showAddDialog = false;
          this.$refs.varietyForm.resetFields();
          this.$message.success('品种添加成功');
        }
      });
    }
  }
};
</script>

<style>
.crop-variety-wrapper {
  border: 1px solid #d9d9d9; /* 边框样式，颜色可根据需求调整 */
  border-radius: 8px; /* 可选：添加圆角让边框更柔和 */
  padding: 1px; /* 内边距，避免内容紧贴边框 */
  display: flex;
  align-items: center;
  gap: 16px;
  background-color: white;
  width:100%;
}
/* 外层容器使用flex布局，实现水平排列 */
.crop-variety-selector {
  display: flex;
  align-items: center; /* 垂直居中对齐 */
  gap: 16px; /* 文字和按钮之间的间距 */
  padding: 10px 0; /* 添加上下内边距 */
}

/* 文字标签样式 */
.variety-label {
  font-weight: 500;
  color: #333; /* 文字颜色 */
  white-space: nowrap; /* 防止文字换行 */
  padding-left:5px;
  font-weight:bold;
}

/* 按钮容器 */
.variety-buttons {
  display: flex;
  gap: 10px; /* 按钮之间的间距 */
  flex-wrap: wrap; /* 当按钮过多时自动换行 */
}

/* 按钮样式 */
.variety-btn {
  padding: 6px 14px;
  border: 1px solid #d1d5db;
  border-radius: 10px;
  background-color: #fff;
  cursor: pointer;
  transition: all 0.2s ease;
}

.variety-btn.active {
  background-color: #22c55e;
  color: #fff;
  border-color: #22c55e;
}

.add-btn {
  padding: 6px 14px;
  border: 1px solid #d1d5db;
  border-radius: 20px;
  background-color: #fff;
  cursor: pointer;
  position: relative;
}
/*弹窗输入框样式*/
.form-item{
  margin:25px;
}
.variety_input{
  top:10px;
  margin-bottom: 10px;
  width:90%;
  height: 40px; /* 输入框高度 */
  padding: 0 12px; /* 输入框内边距（文字与边框的距离） */
  border: 1px solid #d9d9d9; /* 边框样式 */
  border-radius: 6px; /* 输入框圆角（与外层边框风格统一） */
  font-size: 14px; /* 输入文字大小 */
  transition: border-color 0.2s; /* 边框颜色过渡动画 */
}
.custom-modal .ant-modal-footer {
  margin-top: 20px;
  height:50px;
  text-align: center; /* 兼容旧版Ant Design Vue */
  display: flex;
  justify-content: center; /* 核心：水平居中 */
  gap: 16px; /* 可选：增加两个按钮之间的间距 */
}
.custom-modal .ant-modal-title {
  text-align: center; /* 标题文字居中 */
  width: 100%; /* 确保标题占满容器宽度，避免居中效果受内容长度影响 */
}
</style>
