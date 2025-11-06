<template>
  <div class="crop-variety-wrapper">
    <div class="crop-variety-selector">
      <!-- 文字标签 -->
      <span class="variety-label">当前作物品种</span>

      <!-- 按钮容器 -->
      <div class="variety-buttons">
        <button
          v-for="variety in displayedVarieties"
          :key="variety.value"
          :class="['variety-btn', { 'active': selectedVariety === variety.value }]"
          @click="selectVariety(variety.value)"
        >
          {{ variety.label }}
        </button>
        <button class="add-btn" @click="openAddDialog()">+</button>
      </div>

      <!-- 新增品种弹窗 -->
      <a-modal
        title="选择更多作物品种"
        v-model:visible="showAddDialog"
        ok-text="确认添加"
        cancel-text="取消"
        @ok="handleAddVariety"
        @cancel="resetDialogState()"
        :width="600"
        :bodyStyle="{ maxHeight: '400px', padding: '20px' }"
        class="custom-modal"
      >
        <!-- 原生搜索容器，无任何表单包裹 -->
        <div class="search-container">
          <input
            type="text"
            v-model="searchKeyword"
            placeholder="搜索未显示的品种..."
            class="search-input"
            @keyup.enter="handleSearch"
          />
          <button
            class="search-btn"
            @click="handleSearch"
          >
            搜索
          </button>
        </div>

        <!-- 未显示品种列表 -->
        <div class="variety-list">
          <div v-if="filteredUnselectedVarieties.length === 0" class="no-result">
            未找到匹配的品种
          </div>

          <div
            v-for="v in filteredUnselectedVarieties"
            :key="v.value"
            :class="{ 'selected': selectedToAdd === v.value }"
            @click="selectedToAdd = v.value"
            style="display: flex; justify-content: space-between; align-items: center;"
          >
            {{ v.label }}
            <span style="color:#999;font-size: 0.9em;">id:{{ v.value }}</span>
          </div>
        </div>
      </a-modal>
    </div>
  </div>
</template>

<script>
import { getAllVariety } from '../base.api';
import { Modal } from 'ant-design-vue';
import { useCropVarietyStore } from '@/store/selectStore';
import { storeToRefs } from 'pinia'; // 用于保持解构后的数据响应式
export default {
  components: {
    aModal: Modal
  },
  data() {
    return {
      // 品种列表
      varieties: [],
      // 当前选中品种
      selectedVariety: '',
      // 弹窗显示状态
      showAddDialog: false,

      allVarieties: [], // 存储接口返回的所有品种
      displayedVarieties: [], // 显示的品种
      searchKeyword: '', // 搜索关键词
      filteredUnselectedVarieties: [], // 筛选后的未显示品种
      selectedToAdd: '' // 弹窗中选中要添加的品种
    };
  },
  created() {
    this.fetchVarieties();
    this.initStore();
  },
  computed: {
    hasMoreVarieties() {
      return this.allVarieties.length > this.displayedVarieties.length;
    }
  },
  methods: {
    // 选择品种
    selectVariety(value) {
      this.selectedVariety = value;
      // 找到选中品种的完整信息（name和id）
      const selectedItem = this.displayedVarieties.find(
        item => item.value === value
      );
      if (selectedItem) {
        // 调用store方法更新状态
        const cropStore = useCropVarietyStore();
        cropStore.setSelectedVariety({
          id: selectedItem.value,
          name: selectedItem.label
        });
      }
    },
    // 获取品种列表后，初始化选中状态到store
    initStore() {
      if (this.displayedVarieties.length) {
        const firstVariety = this.displayedVarieties[0];
        const cropStore = useCropVarietyStore();
        cropStore.setSelectedVariety({
          id: firstVariety.value,
          name: firstVariety.label
        });
      }
    },
    // 获取品种列表
    async fetchVarieties() {
      try {
        const response = await getAllVariety();
        const rawVarieties = response || [];

        // 去重：根据 varietyName 去重
        const uniqueVarieties = Array.from(
          new Map(rawVarieties.map(item => [item.varietyName, item])).values()
        );

        // 映射为 label/value 格式
        this.allVarieties = uniqueVarieties.map(item => ({
          label: item.varietyName,
          value: item.id.toString()
        }));

        // 显示前3个品种
        this.displayedVarieties = this.allVarieties.slice(0, 3);

        // 初始化选中第一个
        if (this.displayedVarieties.length) {
          this.selectedVariety = this.displayedVarieties[0].value;
        }
      } catch (error) {
        console.error('获取品种列表失败:', error);
        this.$message.error('获取品种列表失败，请重试');
      }
    },

    // 打开弹窗（清空搜索框+初始化列表）
    openAddDialog() {
      this.showAddDialog = true;
      // 清空搜索框和选中状态
      this.searchKeyword = '';
      this.selectedToAdd = '';
      // 初始化筛选列表
      this.$nextTick(() => {
        this.handleSearch();
      });
    },

    // 重置弹窗状态
    resetDialogState() {
      this.searchKeyword = '';
      this.selectedToAdd = '';
    },

    // 核心搜索逻辑（原生实现，无任何框架干扰）
    handleSearch() {
      // 强制转为字符串并去空格
      const keyword = String(this.searchKeyword).toLowerCase().trim();

      // 1. 先筛选出未显示的品种
      const unselected = this.allVarieties.filter(v =>
        !this.displayedVarieties.some(d => d.value === v.value)
      );

      // 2. 执行搜索筛选
      if (keyword === '') {
        // 关键词为空时显示所有未显示品种
        this.filteredUnselectedVarieties = [...unselected];
      } else {
        this.filteredUnselectedVarieties = unselected.filter(v =>
          v.label.toLowerCase().includes(keyword) ||
          v.value.toLowerCase().includes(keyword)
        );
      }

      // 打印日志方便排查（可删除）
      console.log('搜索关键词:', keyword);
      console.log('未显示品种数:', unselected.length);
      console.log('筛选结果数:', this.filteredUnselectedVarieties.length);
    },

    // 处理添加品种（替换显示的品种）
    handleAddVariety() {
      if (!this.selectedToAdd) {
        this.$message.warning('请选择要添加的品种');
        return;
      }

      const varietyToAdd = this.allVarieties.find(v => v.value === this.selectedToAdd);
      if (varietyToAdd) {
        if (this.displayedVarieties.length < 3) {
          this.displayedVarieties.push(varietyToAdd);
        } else {
          this.displayedVarieties.splice(-1, 1, varietyToAdd);
        }

        this.selectVariety(varietyToAdd.value);
        this.resetDialogState();
        this.showAddDialog = false;
      }
    }
  }
};
</script>

<style>
.crop-variety-wrapper {
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  padding: 1px;
  display: flex;
  align-items: center;
  gap: 16px;
  background-color: white;
  width: 100%;
  margin-top: 5px;
}

.crop-variety-selector {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 10px 0;
  width: 100%;
}

.variety-label {
  font-weight: bold;
  color: #333;
  white-space: nowrap;
  padding-left: 5px;
}

.variety-buttons {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  flex: 1;
}

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

/* 原生搜索框样式 */
.search-container {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin: 0 auto 16px;
  width: 90%;
}

.search-input {
  height: 40px;
  padding: 0 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  flex: 1;
  font-size: 14px;
}

.search-input:focus {
  outline: none;
  border-color: #22c55e;
}

/* 搜索按钮样式 */
.search-btn {
  padding: 0 16px;
  height: 40px;
  background-color: #22c55e;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.search-btn:hover {
  background-color: #16a34a;
}

.custom-modal .ant-modal-footer {
  margin-top: 20px;
  height: 50px;
  text-align: center;
  display: flex;
  justify-content: center;
  gap: 16px;
}

.custom-modal .ant-modal-title {
  text-align: center;
  width: 100%;
}

.variety-list {
  max-height: 280px;
  overflow-y: auto;
  margin: 10px auto;
  width: 90%;
  border: 1px solid #f0f0f0;
  border-radius: 4px;
}

.variety-list > div {
  padding: 8px 12px;
  cursor: pointer;
}

.variety-list > div:hover {
  background: #f5f5f5;
}

.variety-list > div.selected {
  background: #e6f7ed;
  border-left: 3px solid #22c55e;
}

/* 无结果提示样式 */
.no-result {
  padding: 20px;
  text-align: center;
  color: #999;
  background: #fafafa;
}
</style>
