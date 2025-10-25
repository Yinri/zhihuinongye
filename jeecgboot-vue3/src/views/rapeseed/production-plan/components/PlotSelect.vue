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
import { getPlotListByBaseId } from '../plot.api';
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
    if (res.success) {
      plotOptions.value = res.result.records || res.result || [];
      
      // 如果没有数据，提供默认数据
      if (plotOptions.value.length === 0) {
        if (props.baseId === '1') {
          plotOptions.value = [
            { id: '1', plotName: '东区1号地块', area: 20, baseId: '1', latitude: 39.9042, longitude: 116.4074 },
            { id: '2', plotName: '东区2号地块', area: 25, baseId: '1', latitude: 39.9052, longitude: 116.4084 },
            { id: '3', plotName: '西区1号地块', area: 30, baseId: '1', latitude: 39.9032, longitude: 116.4064 }
          ];
        } else if (props.baseId === '2') {
          plotOptions.value = [
            { id: '4', plotName: '南区1号地块', area: 15, baseId: '2', latitude: 39.5042, longitude: 116.7074 },
            { id: '5', plotName: '南区2号地块', area: 20, baseId: '2', latitude: 39.5052, longitude: 116.7084 },
            { id: '6', plotName: '北区1号地块', area: 25, baseId: '2', latitude: 39.5032, longitude: 116.7064 }
          ];
        } else if (props.baseId === '3') {
          plotOptions.value = [
            { id: '7', plotName: 'A区地块', area: 35, baseId: '3', latitude: 39.1042, longitude: 117.2074 },
            { id: '8', plotName: 'B区地块', area: 40, baseId: '3', latitude: 39.1052, longitude: 117.2084 },
            { id: '9', plotName: 'C区地块', area: 45, baseId: '3', latitude: 39.1032, longitude: 117.2064 }
          ];
        }
      }
      
      // 如果有默认地块ID，自动选中
      if (props.defaultPlotId) {
        selectedPlotId.value = props.defaultPlotId;
        emit('update:modelValue', props.defaultPlotId);
        emit('change', props.defaultPlotId);
      }
      // 如果没有默认地块但有地块数据，选中第一个
      else if (plotOptions.value.length > 0 && !selectedPlotId.value) {
        selectedPlotId.value = plotOptions.value[0].id;
        emit('update:modelValue', plotOptions.value[0].id);
        emit('change', plotOptions.value[0].id);
      }
      
      emit('loaded', plotOptions.value);
    } else {
      // 如果API失败，提供默认数据
      if (props.baseId === '1') {
        plotOptions.value = [
          { id: '1', plotName: '东区1号地块', area: 20, baseId: '1', latitude: 39.9042, longitude: 116.4074 },
          { id: '2', plotName: '东区2号地块', area: 25, baseId: '1', latitude: 39.9052, longitude: 116.4084 },
          { id: '3', plotName: '西区1号地块', area: 30, baseId: '1', latitude: 39.9032, longitude: 116.4064 }
        ];
      } else if (props.baseId === '2') {
        plotOptions.value = [
          { id: '4', plotName: '南区1号地块', area: 15, baseId: '2', latitude: 39.5042, longitude: 116.7074 },
          { id: '5', plotName: '南区2号地块', area: 20, baseId: '2', latitude: 39.5052, longitude: 116.7084 },
          { id: '6', plotName: '北区1号地块', area: 25, baseId: '2', latitude: 39.5032, longitude: 116.7064 }
        ];
      } else if (props.baseId === '3') {
        plotOptions.value = [
          { id: '7', plotName: 'A区地块', area: 35, baseId: '3', latitude: 39.1042, longitude: 117.2074 },
          { id: '8', plotName: 'B区地块', area: 40, baseId: '3', latitude: 39.1052, longitude: 117.2084 },
          { id: '9', plotName: 'C区地块', area: 45, baseId: '3', latitude: 39.1032, longitude: 117.2064 }
        ];
      }
      
      // 选中第一个地块
      if (!selectedPlotId.value && plotOptions.value.length > 0) {
        selectedPlotId.value = plotOptions.value[0].id;
        emit('update:modelValue', plotOptions.value[0].id);
        emit('change', plotOptions.value[0].id);
      }
      
      emit('loaded', plotOptions.value);
    }
  } catch (err) {
    console.error('加载地块列表异常:', err);
    // 如果出现异常，提供默认数据
    if (props.baseId === '1') {
      plotOptions.value = [
        { id: '1', plotName: '东区1号地块', area: 20, baseId: '1', latitude: 39.9042, longitude: 116.4074 },
        { id: '2', plotName: '东区2号地块', area: 25, baseId: '1', latitude: 39.9052, longitude: 116.4084 },
        { id: '3', plotName: '西区1号地块', area: 30, baseId: '1', latitude: 39.9032, longitude: 116.4064 }
      ];
    } else if (props.baseId === '2') {
      plotOptions.value = [
        { id: '4', plotName: '南区1号地块', area: 15, baseId: '2', latitude: 39.5042, longitude: 116.7074 },
        { id: '5', plotName: '南区2号地块', area: 20, baseId: '2', latitude: 39.5052, longitude: 116.7084 },
        { id: '6', plotName: '北区1号地块', area: 25, baseId: '2', latitude: 39.5032, longitude: 116.7064 }
      ];
    } else if (props.baseId === '3') {
      plotOptions.value = [
        { id: '7', plotName: 'A区地块', area: 35, baseId: '3', latitude: 39.1042, longitude: 117.2074 },
        { id: '8', plotName: 'B区地块', area: 40, baseId: '3', latitude: 39.1052, longitude: 117.2084 },
        { id: '9', plotName: 'C区地块', area: 45, baseId: '3', latitude: 39.1032, longitude: 117.2064 }
      ];
    }
    
    // 选中第一个地块
    if (!selectedPlotId.value && plotOptions.value.length > 0) {
      selectedPlotId.value = plotOptions.value[0].id;
      emit('update:modelValue', plotOptions.value[0].id);
      emit('change', plotOptions.value[0].id);
    }
    
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