<template>
  <div class="base-select-container">
    <a-select
      v-model:value="selectedBaseId"
      placeholder="请选择基地"
      :loading="loading"
      :disabled="loading"
      show-search
      :filter-option="filterOption"
      @change="handleBaseChange"
      style="width: 100%"
    >
      <a-select-option v-for="base in baseOptions" :key="base.id" :value="base.id">
        {{ base.baseName }}
      </a-select-option>
    </a-select>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, watch } from 'vue';
import { getBaseList } from '../base.api';
import { message } from 'ant-design-vue';
import { useMessage } from '/@/hooks/web/useMessage';

const props = defineProps({
  modelValue: {
    type: [String, Number],
    default: undefined,
  },
  defaultBaseId: {
    type: [String, Number],
    default: undefined,
  },
});

const emit = defineEmits(['update:modelValue', 'change', 'loaded']);

const { createMessage } = useMessage();

// 基地选择值
const selectedBaseId = ref(props.modelValue || props.defaultBaseId);
// 基地选项数据
const baseOptions = ref([]);
// 加载状态
const loading = ref(false);
// 错误信息
const error = ref('');

// 监听外部值变化
watch(
  () => props.modelValue,
  (val) => {
    selectedBaseId.value = val;
  }
);

// 监听默认基地ID变化
watch(
  () => props.defaultBaseId,
  (val) => {
    if (val && !selectedBaseId.value) {
      selectedBaseId.value = val;
      emit('update:modelValue', val);
      emit('change', val);
    }
  }
);

// 加载基地列表
const loadBaseList = async () => {
  try {
    loading.value = true;
    error.value = '';
    
    // 从服务器获取数据
    const res = await getBaseList({});
    const rows = (res && Array.isArray(res.records)) ? res.records : (Array.isArray(res) ? res : (res?.result || []));
    baseOptions.value = rows || [];
    
    if (props.defaultBaseId) {
      selectedBaseId.value = props.defaultBaseId;
      emit('update:modelValue', props.defaultBaseId);
      emit('change', props.defaultBaseId);
    } else if (baseOptions.value.length > 0 && !selectedBaseId.value) {
      selectedBaseId.value = baseOptions.value[0].id;
      emit('update:modelValue', baseOptions.value[0].id);
      emit('change', baseOptions.value[0].id);
    }
    
    emit('loaded', baseOptions.value);
  } catch (err) {
    console.error('加载基地列表异常:', err);
    baseOptions.value = [];
    emit('loaded', baseOptions.value);
  } finally {
    loading.value = false;
  }
};

// 基地选择变化处理
const handleBaseChange = (value) => {
  selectedBaseId.value = value;
  emit('update:modelValue', value);
  emit('change', value);
};

// 搜索过滤
const filterOption = (input: string, option: any) => {
  return option.children.toLowerCase().indexOf(input.toLowerCase()) >= 0;
};

// 组件挂载时加载数据
onMounted(() => {
  loadBaseList();
});

// 暴露方法给父组件
defineExpose({
  loadBaseList,
  baseOptions,
  selectedBaseId,
});
</script>

<style lang="less" scoped>
.base-select-container {
  width: 100%;
}
</style>
