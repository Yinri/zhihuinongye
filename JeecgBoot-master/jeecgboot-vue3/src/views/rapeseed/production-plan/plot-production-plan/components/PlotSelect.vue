<template>
  <div class="plot-select-container">
    <a-select
      v-model:value="selectedPlotId"
      placeholder="请选择地块"
      :loading="loading"
      :disabled="!baseId || loading"
      show-search
      :filter-option="filterOption"
      @change="handlePlotChange"
      style="width: 100%"
    >
      <a-select-option v-for="plot in plotOptions" :key="plot.id" :value="plot.id">
        {{ plot.plotName }}
      </a-select-option>
    </a-select>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, watch } from 'vue';
import { getPlotListByBaseId } from '../../plot.api';
import { message } from 'ant-design-vue';
import { useMessage } from '/@/hooks/web/useMessage';

const props = defineProps({
  modelValue: {
    type: [String, Number],
    default: undefined,
  },
  baseId: {
    type: [String, Number],
    default: undefined,
  },
  defaultPlotId: {
    type: [String, Number],
    default: undefined,
  },
});

const emit = defineEmits(['update:modelValue', 'change', 'loaded']);

const { createMessage } = useMessage();

// 地块选择值
const selectedPlotId = ref(props.modelValue || props.defaultPlotId);
// 地块选项数据
const plotOptions = ref([]);
// 加载状态
const loading = ref(false);
// 错误信息
const error = ref('');

// 监听外部值变化
watch(
  () => props.modelValue,
  (val) => {
    selectedPlotId.value = val;
  }
);

// 监听基地ID变化
watch(
  () => props.baseId,
  (newBaseId, oldBaseId) => {
    if (newBaseId && newBaseId !== oldBaseId) {
      loadPlotList();
    } else if (!newBaseId) {
      // 基地ID被清空，清空地块选项
      plotOptions.value = [];
      selectedPlotId.value = undefined;
      emit('update:modelValue', undefined);
      emit('change', undefined);
    }
  },
  { immediate: true }
);

// 监听默认地块ID变化
watch(
  () => props.defaultPlotId,
  (val) => {
    if (val && plotOptions.value.length > 0 && !selectedPlotId.value) {
      selectedPlotId.value = val;
      emit('update:modelValue', val);
      emit('change', val);
    }
  }
);

// 加载地块列表
const loadPlotList = async () => {
  if (!props.baseId) {
    plotOptions.value = [];
    return;
  }

  try {
    loading.value = true;
    error.value = '';
    
    // 从服务器获取数据
    const res = await getPlotListByBaseId({ baseId: props.baseId });
    const rows = (res && Array.isArray(res.records)) ? res.records : (Array.isArray(res) ? res : (res?.result || []));
    plotOptions.value = rows || [];
    
    if (props.defaultPlotId && plotOptions.value.length > 0) {
      selectedPlotId.value = String(props.defaultPlotId);
      emit('update:modelValue', String(props.defaultPlotId));
      emit('change', String(props.defaultPlotId));
    } else if (plotOptions.value.length > 0 && !selectedPlotId.value) {
      selectedPlotId.value = String(plotOptions.value[0].id);
      emit('update:modelValue', String(plotOptions.value[0].id));
      emit('change', String(plotOptions.value[0].id));
    }
    
    emit('loaded', plotOptions.value);
  } catch (err) {
    console.error('加载地块列表异常:', err);
    plotOptions.value = [];
    emit('loaded', plotOptions.value);
  } finally {
    loading.value = false;
  }
};

// 地块选择变化处理
const handlePlotChange = (value) => {
  selectedPlotId.value = value;
  emit('update:modelValue', value);
  emit('change', value);
};

// 搜索过滤
const filterOption = (input: string, option: any) => {
  return option.children.toLowerCase().indexOf(input.toLowerCase()) >= 0;
};

// 暴露方法给父组件
defineExpose({
  loadPlotList,
  plotOptions,
  selectedPlotId,
});
</script>

<style lang="less" scoped>
.plot-select-container {
  width: 100%;
}
</style>
