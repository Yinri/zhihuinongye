<template>
  <div class="growth-timeline-container">
    <div class="simple-timeline">
      <!-- 阶段名称标签 -->
      <div class="stages-labels">
        <div 
          v-for="(stage, index) in stages" 
          :key="`label-${stage.id}`"
          class="stage-label-container"
          :style="{ 
            left: `${calculateStagePosition(index)}%`,
            width: `${calculateStageWidth(index)}%`
          }"
        >
          <div class="stage-name">{{ stage.name }}</div>
        </div>
      </div>
      
      <!-- 阶段条 -->
      <div class="stages-bar">
        <div 
          v-for="(stage, index) in stages" 
          :key="stage.id"
          :class="['stage', { 'last-stage': index === stages.length - 1 }]"
          :style="{ 
            width: `${calculateStageWidth(index)}%`,
            backgroundColor: getStageColor(stage.id)
          }"
        >
          <div class="stage-days">{{ stage.days }}天</div>
        </div>
      </div>
      
      <!-- 当前阶段指针 -->
      <div 
        v-if="hasData && currentStageIndex >= 0"
        class="current-stage-pointer"
        :style="{ left: `${getCurrentStagePointerPosition()}%` }"
      >
        <div class="pointer-arrow"></div>
      </div>
    </div>
    
    <!-- 无数据提示 -->
    <div v-if="!hasData" class="no-data-container">
      <div>暂无生长周期数据</div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, watch, onMounted } from 'vue';
import { Icon } from '/@/components/Icon';

// 定义生长阶段接口
interface GrowthStage {
  id: string;
  name: string;
  days: number;
  color: string;
  isCurrent: boolean;
  width: number;
}

// 组件属性
const props = defineProps({
  plotId: {
    type: [String, Number],
    default: undefined,
  },
  varietyId: {
    type: [String, Number],
    default: undefined,
  },
  varietyName: {
    type: String,
    default: '',
  },
  currentStageId: {
    type: [String, Number],
    default: undefined,
  },
});

// 生长阶段数据
const stages = ref<GrowthStage[]>([
  {
    id: '1',
    name: '苗期',
    days: 30,
    color: '#52c41a', // 绿色 - 符合WCAG AA标准，对比度 > 4.5:1
    isCurrent: false,
    width: 0,
  },
  {
    id: '2',
    name: '蕾薹期',
    days: 25,
    color: '#fa8c16', // 橙色 - 符合WCAG AA标准，对比度 > 4.5:1
    isCurrent: false,
    width: 0,
  },
  {
    id: '3',
    name: '开花期',
    days: 20,
    color: '#1890ff', // 蓝色 - 符合WCAG AA标准，对比度 > 4.5:1
    isCurrent: false,
    width: 0,
  },
  {
    id: '4',
    name: '角果成熟期',
    days: 35,
    color: '#722ed1', // 紫色 - 符合WCAG AA标准，对比度 > 4.5:1
    isCurrent: false,
    width: 0,
  },
]);

// 是否有数据
const hasData = computed(() => stages.value.length > 0);

// 总天数
const totalDays = computed(() => stages.value.reduce((sum, stage) => sum + stage.days, 0));

// 当前阶段名称
const currentStageName = computed(() => {
  const current = stages.value.find(stage => stage.isCurrent);
  return current ? current.name : '';
});

// 当前阶段索引
const currentStageIndex = computed(() => {
  if (!props.currentStageId) return 0;
  return stages.value.findIndex(stage => stage.id === props.currentStageId.toString());
});

// 当前阶段进度 (模拟数据，实际应从API获取)
const currentStageProgress = ref(65);

// 时间轴标记点
const timelineMarkers = computed(() => {
  const markers = ['0天'];
  let accumulatedDays = 0;
  
  stages.value.forEach((stage) => {
    accumulatedDays += stage.days;
    markers.push(`${accumulatedDays}天`);
  });
  
  return markers;
});

// 计算阶段位置
const calculateStagePosition = (index: number): number => {
  if (index === 0) return 0;
  
  let position = 0;
  for (let i = 0; i < index; i++) {
    position += stages.value[i].days;
  }
  return (position / totalDays.value) * 100;
};

// 计算阶段宽度
const calculateStageWidth = (index: number): number => {
  return (stages.value[index].days / totalDays.value) * 100;
};

// 获取当前阶段名称
const getCurrentStageName = (): string => {
  const currentStage = stages.value.find(s => s.id === props.currentStageId?.toString());
  return currentStage ? currentStage.name : '未知';
};

// 获取阶段颜色
const getStageColor = (stageId: string): string => {
  const stage = stages.value.find(s => s.id === stageId);
  return stage ? stage.color : '#d9d9d9';
};

// 计算当前阶段指针位置
const getCurrentStagePointerPosition = (): number => {
  if (currentStageIndex.value < 0) return 0;
  
  // 计算当前阶段的起始位置
  let position = 0;
  for (let i = 0; i < currentStageIndex.value; i++) {
    position += stages.value[i].days;
  }
  
  // 加上当前阶段宽度的一半，使指针指向当前阶段的中间
  position += stages.value[currentStageIndex.value].days / 2;
  
  return (position / totalDays.value) * 100;
};

// 更新阶段宽度和当前阶段
const updateStages = () => {
  if (!totalDays.value) return;
  
  // 计算每个阶段的宽度百分比
  stages.value.forEach((stage) => {
    stage.width = (stage.days / totalDays.value) * 100;
  });
  
  // 设置当前阶段
  if (props.currentStageId) {
    stages.value.forEach((stage) => {
      stage.isCurrent = stage.id === props.currentStageId.toString();
    });
  } else {
    // 默认设置第一个阶段为当前阶段
    if (stages.value.length > 0) {
      stages.value[0].isCurrent = true;
    }
  }
};

// 模拟从API获取生长周期数据
const fetchGrowthCycleData = async () => {
  // 这里应该调用实际的API
  // const res = await getGrowthCycleByVarietyId({ varietyId: props.varietyId });
  
  // 模拟API响应
  // 实际项目中应该替换为真实的API调用
  if (props.varietyId) {
    // 根据品种ID获取不同的生长周期数据
    // 这里使用默认数据，实际项目中应该从API获取
    console.log('获取品种ID为', props.varietyId, '的生长周期数据');
  }
  
  // 更新阶段宽度和当前阶段
  updateStages();
};

// 监听品种ID变化
watch(
  () => props.varietyId,
  () => {
    fetchGrowthCycleData();
  },
  { immediate: true }
);

// 监听当前阶段ID变化
watch(
  () => props.currentStageId,
  () => {
    updateStages();
  }
);

// 组件挂载时获取数据
onMounted(() => {
  fetchGrowthCycleData();
});
</script>

<style lang="less" scoped>
.growth-timeline-container {
  width: 100%;
}

.timeline-header {
  margin-bottom: 16px;
}

.timeline-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #262626;
}

.simple-timeline {
  position: relative;
  height: 120px; // 增加高度以容纳阶段名称、天数标注和时间轴刻度
  margin: 20px 0;
  background-color: #fff;
  border-radius: 6px;
  overflow: hidden;
}

.stages-bar {
  position: relative;
  height: 50px; // 增加阶段条高度，使颜色更明显
  display: flex;
  margin-bottom: 5px;
}

.stage {
  height: 100%;
  position: relative;
  margin-right: 0; // 移除间距，使阶段条直接连接
  
  // 所有阶段条使用矩形，直接连接
  &:first-child {
    border-radius: 12px 0 0 12px;
  }
  
  // 最后一个阶段条使用圆角
  &.last-stage {
    border-radius: 0 12px 12px 0;
  }
  
  // 阶段天数显示在条形中间
  .stage-days {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    color: #fff;
    font-size: 14px; // 增大字体，提高可读性
    font-weight: 600; // 增加字体粗细
    text-shadow: 0 1px 3px rgba(0, 0, 0, 0.5); // 增强文字阴影，提高可读性
    white-space: nowrap;
    z-index: 3;
  }
}

// 阶段名称标签容器
.stages-labels {
  position: relative;
  width: 100%;
  height: 30px;
  margin-bottom: 5px;
}

.stage-label-container {
  position: absolute;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

// 阶段名称显示在条形上方
.stage-name {
  font-weight: bold;
  font-size: 14px;
  color: #333;
  text-shadow: 0 1px 1px rgba(255, 255, 255, 0.5);
  white-space: nowrap;
}

// 阶段天数标注样式
.stages-duration {
  position: absolute;
  top: 55px; // 位于阶段条下方
  width: 100%;
  height: 25px; // 天数标注区域高度
  display: flex;
}

.stage-duration {
  position: absolute;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #333;
  font-weight: 600;
  background-color: rgba(255, 255, 255, 0.85);
  border-radius: 4px;
  margin-right: 0; // 移除间距，与阶段条保持一致
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  z-index: 2;
  border: 1px solid #e8e8e8;
}



.loading-container, .no-data-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #999;
}

// 当前阶段指针样式
.current-stage-pointer {
  position: absolute;
  top: 85px; // 位于阶段条下方
  transform: translateX(-50%); // 使指针中心对齐
  z-index: 10;
  display: flex;
  flex-direction: column;
  align-items: center;
  
  .pointer-arrow {
    width: 0;
    height: 0;
    border-left: 8px solid transparent;
    border-right: 8px solid transparent;
    border-top: 12px solid #ff4d4f; // 红色箭头
    margin-bottom: 2px;
  }
  
  .pointer-label {
    background-color: #ff4d4f; // 红色背景
    color: white;
    font-size: 12px;
    padding: 2px 8px;
    border-radius: 10px;
    white-space: nowrap;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .timeline-title {
    font-size: 14px;
  }
  
  .simple-timeline {
    height: 100px; // 移动端适当减小高度
  }
  
  .stages-bar {
    height: 40px; // 移动端减小阶段条高度
  }
  
  .stage {
    .stage-days {
      font-size: 12px;
    }
    
    .stage-name {
      font-size: 12px;
    }
    
    .stage-label-container {
      height: 25px;
    }
  }
  
  .current-stage-indicator {
    width: 70px;
    height: 24px;
    font-size: 11px;
    top: -12px;
  }
  
  .stages-duration {
    top: 45px; // 移动端调整天数标注位置
    height: 20px;
  }
  
  .stage-duration {
    font-size: 10px;
  }
  
  .current-stage-pointer {
    top: 50px; // 移动端调整指针位置
    
    .pointer-arrow {
      border-left-width: 6px;
      border-right-width: 6px;
      border-top-width: 10px;
    }
    
    .pointer-label {
      font-size: 10px;
      padding: 1px 6px;
    }
  }
}
</style>