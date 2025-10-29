<template>
  <div class="insect-control-page">
    <!-- 基地与地块选择区域和生长周期时间轴区域 -->
    <a-card :bordered="false" class="combined-selection-timeline-card">
      <a-row :gutter="16" align="middle">
        <!-- 基地选择区域 -->
        <a-col :span="3">
          <div class="selection-area">
            <div class="selection-title">基地选择</div>
            <BaseSelect
              v-model:value="selectedBaseId"
              @change="handleBaseChange"
              :defaultBaseId="defaultBaseId"
              ref="baseSelectRef"
            />
          </div>
        </a-col>
        <!-- 地块选择区域 -->
        <a-col :span="3">
          <div class="selection-area">
            <div class="selection-title">地块选择</div>
            <PlotSelect
              v-model:value="selectedPlotId"
              :baseId="selectedBaseId"
              @change="handlePlotChange"
              ref="plotSelectRef"
            />
          </div>
        </a-col>
        <!-- 生长周期时间轴区域 -->
        <a-col :span="18">
          <div class="timeline-area">
            <GrowthTimeline
              :plotId="selectedPlotId"
              :varietyId="selectedVarietyId"
              :varietyName="selectedVarietyName"
              :currentStageId="currentStageId"
            />
          </div>
        </a-col>
      </a-row>
    </a-card>
    <a-card class="disease-card" :bordered="false">
      <a-row class="main-content-row">
        <!-- 实时病害图像 -->
        <a-col :span="8">
          <a-card title="实时苗情图像" :bordered="false" class="inner-card">
            <div class="image-container">
              <img class="disease-image" :src="currentImageUrl" alt="实时苗情图像" />
            </div>
            <div class="update-time">更新于：{{ lastUpdateTime }}</div>
          </a-card>
        </a-col>

        <a-col :span="8">
          <a-card title="苗情检测指标和应对措施" :bordered="false" class="inner-card">
            <!-- 显示两个指标 -->
            <div class="indicator">
              <p><strong>颜色分析结果：</strong>{{ colorAnalysis.toFixed(2) }}</p>
              <p><strong>植株密度：</strong>{{ plantDensity }} 株/m²</p>
            </div>

            <!-- 根据专家知识库比对的苗情分级 -->
            <div v-if="seedlingLevel" class="analysis-result" style="white-space: pre-line; margin-top: 12px;">
              <p><strong>苗情等级：</strong>{{ seedlingLevel.grade }}</p>
              <p><strong>防治建议：</strong>{{ seedlingLevel.advice }}</p>
            </div>
            <!-- 化肥选择下拉框，选择后直接显示施用量 -->
            <a-select
              class="fertilizer-select"
              v-model:value="selectedFertilizer"
              placeholder="请选择化肥"
              allowClear
              @change="handleFertilizerAnalyze"
            >
              <a-select-option
                v-for="item in fertilizerOptions"
                :key="item.value"
                :value="item.value"
              >
                {{ item.label }}
              </a-select-option>
            </a-select>
            <!-- 施用量分析 -->
            <div v-if="analysisResult" class="analysis-result">
              {{ analysisResult }}
            </div>
          </a-card>
        </a-col>
        <a-col :span="8">
          <a-card title="苗情分级评估" :bordered="false" class="inner-card">
            <!-- 仪表盘容器 -->
            <div ref="gaugeChart" style="width: 100%; height: 250px;"></div>
          </a-card>
        </a-col>
      </a-row>
    </a-card>

    <a-card title="历史苗情记录" :bordered="false" class="history-card">
      <a-row :gutter="16" align="middle">
        <!-- 时间段选择 -->
        <a-col :span="8">
          <a-range-picker
            v-model:value="selectedHistoryRange"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm"
            show-time
            style="width: 100%;"
          />
        </a-col>
        <!-- 查询按钮 -->
        <a-col :span="2">
          <a-button type="primary" @click="handleQueryHistory">查询</a-button>
        </a-col>
      </a-row>

      <!-- 历史虫情结果展示 -->
      <div v-if="filteredHistory.length" class="history-grid">
                <div
                  v-for="record in filteredHistory"
                  :key="record.id"
                  class="history-card-item"
                >
        <!--          &lt;!&ndash; 左侧苗情图片 &ndash;&gt;-->
                  <div class="history-content">
                    <div class="image-wrapper">
                      <img :src="record.image" alt="苗情图片" class="lishi-image" />
                    </div>

                    <!--          &lt;!&ndash; 右侧苗情信息 &ndash;&gt;-->
                    <div class="record-info">
                      <div class="record-time">操作时间：{{ record.operationTime }}</div>
                      <!--            &lt;!&ndash; 苗情等级与采集时间 &ndash;&gt;-->
                      <div class="pest-entry">
                        <div class="disease-name">
                          苗情等级：
                          <span
                            :style="{
                  color:
                    record.level.includes('良好')
                      ? '#52c41a'
                      : record.level.includes('偏弱')
                      ? '#faad14'
                      : '#ff4d4f',
                  'font-weight': 'bold',
                }"
                          >
                {{ record.level }}
              </span>
                        </div>
                        <div class="disease-count">
                          采集时间：{{ record.time }}
                        </div>
                      </div>
                      <!--            &lt;!&ndash; 化肥与剂量信息 &ndash;&gt;-->
                      <div class="common-info">
                        使用化肥：{{ record.fertilizer }} &nbsp; | &nbsp; 剂量：{{ record.dosage }}
                      </div>
                    </div>
                  </div>
              </div>
      </div>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
import {ref, reactive, onMounted,onUnmounted, watch,nextTick ,computed } from 'vue';
import { BasicTable, useTable, TableAction } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useMessage } from '/@/hooks/web/useMessage';
import { Icon } from '/@/components/Icon';
import { columns, searchFormSchema } from './diseaseControl.data';
// import { getInsectControlList, deleteInsectControl, exportInsectControl, importInsectControl } from './diseasdeControl.api';
import InsectControlModal from './InsectControlModal.vue';
import BaseSelect from '../production-plan/components/BaseSelect.vue';
import PlotSelect from '../production-plan/components/PlotSelect.vue';
import GrowthTimeline from '../production-plan/components/GrowthTimeline.vue';
import * as echarts from 'echarts'
import { message } from 'ant-design-vue'
import { message as createMessage } from 'ant-design-vue'


const { createMessage } = useMessage();
// 基地选择相关
const selectedBaseId = ref<string | number | undefined>(undefined);
const selectedBaseName = ref<string>('');
const baseSelectRef = ref();
// 地块选择相关
const selectedPlotId = ref<string | number | undefined>(undefined);
const selectedPlotName = ref<string>('');
const plotSelectRef = ref();
// 品种信息
const selectedVarietyId = ref<string | number | undefined>(undefined);
const selectedVarietyName = ref<string>('');
const currentStageId = ref<string | number | undefined>(undefined);
// 加载状态
const loading = ref(false);
// 默认基地ID（可以从URL参数或用户设置中获取）
const defaultBaseId = ref<string | number | undefined>(undefined);
const [registerModal, { openModal }] = useModal();
const searchInfo = reactive<Recordable>({});
const fileList = ref<any[]>([]);
const pesticideOptions = ref([])
const selectedPesticide = ref(null)
const llmAnalysis = ref('')
const analysisResult = ref('')
const selectedHistoryRange = ref([])
const filteredHistory = ref([])
const colorAnalysis = ref(0.65)   // 植株颜色指标 (0~1)
const plantDensity = ref(110)     // 植株密度 (株/m²)
// const [registerTable, { reload, getSelectRows, dataSource }] = useTable({
//   title: '虫害防控管理',
//   api: selectedPlotId.value ? (params) => {
//     // 如果有选中的地块ID，添加到查询参数
//     if (selectedPlotId.value) {
//       params.plotId = selectedPlotId.value;
//     }
//     return getInsectControlList(params);
//   } : getInsectControlList,
//   columns,
//   formConfig: {
//     labelWidth: 120,
//     schemas: searchFormSchema,
//     autoSubmitOnEnter: true,
//   },
//   useSearchForm: true,
//   showTableSetting: true,
//   bordered: true,
//   showIndexColumn: false,
//   actionColumn: {
//     width: 80,
//     title: '操作',
//     dataIndex: 'action',
//     fixed: 'right',
//   },
//   beforeFetch: (params) => {
//     // 如果有选中的基地ID或地块ID，添加到查询参数
//     if (selectedBaseId.value) {
//       params.baseId = selectedBaseId.value;
//     }
//     if (selectedPlotId.value) {
//       params.plotId = selectedPlotId.value;
//     }
//     return params;
//   },
// });

/**
 * 基地选择变化处理
 */
// --- 实时虫情图像 ---
const currentImageUrl = ref('https://tse1.mm.bing.net/th/id/OIP.iIA2SGZVYVnSf-165spLkgHaLH?rs=1&pid=ImgDetMain&o=7&rm=3')
const lastUpdateTime = ref('2025-10-27 10:30')

const lastTime = ref('2025-10-27 18:30')


// 专家知识库规则
const expertKnowledge = [
  { colorMin: 0.7, densityMin: 120, grade: '苗情良好（正常）', advice: '继续保持当前水肥管理' },
  { colorMin: 0.5, densityMin: 80, grade: '苗情偏弱', advice: '适当追肥，提高植株长势' },
  { colorMin: 0.0, densityMin: 0, grade: '苗情异常', advice: '检查病虫害或土壤问题' }
]

// 根据专家知识库比对自动计算苗情等级
const seedlingLevel = computed(() => {
  for (const rule of expertKnowledge) {
    if (colorAnalysis.value >= rule.colorMin && plantDensity.value >= rule.densityMin) {
      return rule
    }
  }
  return { grade: '未知', advice: '未匹配专家规则' }
})
// 化肥选项
const fertilizerOptions = [
  { label: '尿素', value: 'urea' },
  { label: '复合肥', value: 'compound' },
  { label: '磷酸二铵', value: 'dap' },
  { label: '氯化钾', value: 'kcl' },
]

// 化肥施用量映射表
const fertilizerMap = {
  urea: '尿素：每亩10~15公斤，分次追施，以促进分蘖和生长。',
  compound: '复合肥：每亩20~25公斤，基肥或早期追肥施用。',
  dap: '磷酸二铵：每亩15公斤作基肥，促进根系发育。',
  kcl: '氯化钾：每亩8~10公斤，宜与氮肥配合使用。',
}

// 化肥选择后直接显示施用量
const handleFertilizerAnalyze = (value) => {
  if (!value) {
    analysisResult.value = ''
    return
  }
  analysisResult.value = fertilizerMap[value] || '暂无该化肥的施用建议。'
}
const historyData = ref([
  {
    id: 1,
    image: 'https://tse1.mm.bing.net/th/id/OIP.iIA2SGZVYVnSf-165spLkgHaLH?rs=1&pid=ImgDetMain&o=7&rm=3',
    time: '2025-10-28 07:30', // 采集时间
    fertilizer: '尿素',
    dosage: '20kg/亩',
    level: '苗情良好（正常）',
    operationTime: '2025-10-28 08:00', // 操作时间
  },
  {
    id: 2,
    image: 'https://tse1.mm.bing.net/th/id/OIP.iIA2SGZVYVnSf-165spLkgHaLH?rs=1&pid=ImgDetMain&o=7&rm=3',
    time: '2025-10-26 09:15',
    fertilizer: '复合肥',
    dosage: '25kg/亩',
    level: '苗情偏弱',
    operationTime: '2025-10-26 09:30',
  },
  {
    id: 3,
    image: 'https://tse1.mm.bing.net/th/id/OIP.iIA2SGZVYVnSf-165spLkgHaLH?rs=1&pid=ImgDetMain&o=7&rm=3',
    time: '2025-10-22 10:00',
    fertilizer: '磷酸二铵',
    dosage: '30kg/亩',
    level: '苗情异常',
    operationTime: '2025-10-22 10:20',
  },
])

const handleQueryHistory = () => {
  const range = selectedHistoryRange.value
  if (!range || range.length !== 2) {
    message.warning('请先选择开始时间和结束时间')
    return
  }
  const [startTime, endTime] = range.map((t) => new Date(t).getTime())

  filteredHistory.value = historyData.value.filter((item) => {
    const recordTime = new Date(item.operationTime).getTime()
    return recordTime >= startTime && recordTime <= endTime
  })

  if (filteredHistory.value.length === 0) {
    message.info('该时间段内无虫情记录')
  } else {
    console.log('查询结果：', filteredHistory.value)
  }
}

// 假设从专家知识库计算出的苗情等级（1~5）
const seedLevel = ref(3) // 例如 3级：健壮

// 等级说明映射
const levelMap = {
  1: '1级（弱）',
  2: '2级（一般）',
  3: '3级（健壮）',
  4: '4级（偏旺）',
  5: '5级（异常）',
}
const levelLabel = ref(levelMap[seedLevel.value])

const gaugeChart = ref(null)
let chartInstance = null

const renderGauge = () => {
  if (!gaugeChart.value) return
  if (!chartInstance) {
    chartInstance = echarts.init(gaugeChart.value)
  }

  const option = {
    series: [
      {
        type: 'gauge',
        startAngle: 210, // 扩大角度范围，让仪表盘更开阔
        endAngle: -30,
        min: 1,
        max: 5,
        radius: '90%', // 半径更大，让图形更显眼
        splitNumber: 4,

        // 仪表盘主色带
        axisLine: {
          lineStyle: {
            width: 20,
            color: [
              [0.25, '#91cc75'], // 1~2 弱 - 绿
              [0.5, '#fac858'],  // 3 健壮 - 黄
              [0.75, '#ee6666'], // 4 偏旺 - 红
              [1, '#000000'],    // 5 异常 - 黑
            ],
          },
        },

        // 指针样式
        pointer: {
          show: true,
          length: '70%',
          width: 6,
          itemStyle: {
            color: '#2f3542', // 深灰色指针
          },
        },

        // 刻度线
        axisTick: {
          distance: -25,
          length: 8,
          lineStyle: {
            color: '#aaa',
            width: 1,
          },
        },

        // 分隔线
        splitLine: {
          distance: -30,
          length: 16,
          lineStyle: {
            color: '#888',
            width: 2,
          },
        },

        // 等级文字标签
        axisLabel: {
          color: '#333',
          fontSize: 14,
          distance: -50, // 调整距离，避免重叠
          formatter: (value) => {
            if (value === 1) return '1级\n(弱)'
            if (value === 3) return '3级\n(健壮)'
            if (value === 5) return '5级\n(异常)'
            return ''
          },
        },

        // 当前数值显示
        detail: {
          fontSize: 18,
          color: '#333',
          formatter: (value) => `当前等级：${value}级`,
          offsetCenter: [0, '65%'], // 往下移一点，不挡仪表盘
        },

        data: [
          { value: seedLevel.value }, // 动态绑定苗情等级
        ],
      },
    ],
  }
  chartInstance.setOption(option)
}

// 初始化和更新监听
onMounted(() => {
  renderGauge()
})
watch(seedLevel, (newVal) => {
  levelLabel.value = levelMap[newVal]
  renderGauge()
})


function handleBaseChange(baseId) {
  selectedBaseId.value = baseId;

  // 获取基地名称
  if (baseSelectRef.value && baseSelectRef.value.baseOptions) {
    const base = baseSelectRef.value.baseOptions.find(item => item.id === baseId);
    selectedBaseName.value = base ? base.baseName : '';
  }

  // 清空地块选择
  selectedPlotId.value = undefined;
  selectedPlotName.value = '';

  // 清空品种信息
  selectedVarietyId.value = undefined;
  selectedVarietyName.value = '';
  currentStageId.value = undefined;

  // 重新加载数据
  loadInsectControlData();
}

/**
 * 地块选择变化处理
 */
function handlePlotChange(plotId) {
  selectedPlotId.value = plotId;

  // 获取地块名称
  if (plotSelectRef.value && plotSelectRef.value.plotOptions) {
    const plot = plotSelectRef.value.plotOptions.find(item => item.id === plotId);
    selectedPlotName.value = plot ? plot.plotName : '';

    // 获取品种信息（这里假设地块信息中包含品种信息）
    if (plot) {
      selectedVarietyId.value = plot.varietyId;
      selectedVarietyName.value = plot.varietyName || '';
      // 这里可以根据实际情况设置当前阶段ID
      // currentStageId.value = plot.currentStageId;
    }
  }

  // 重新加载数据
  loadInsectControlData();
}
/**
 * 加载虫害防控数据
 */
async function loadInsectControlData() {
  if (!selectedBaseId.value) {
    return;
  }

  try {
    loading.value = true;
    // 重新加载数据
    await reload();
  } catch (error) {
    console.error('加载虫害防控数据失败:', error);
    createMessage.error('加载虫害防控数据失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}

function handleCreate() {
  if (!selectedBaseId.value || !selectedPlotId.value) {
    createMessage.warning('请先选择基地和地块');
    return;
  }

  openModal(true, {
    isUpdate: false,
    baseId: selectedBaseId.value,
    plotId: selectedPlotId.value,
  });
}

function handleEdit(record: Recordable) {
  openModal(true, {
    record,
    isUpdate: true,
  });
}

async function handleDelete(record: Recordable) {
  await deleteInsectControl(record.id);
  createMessage.success('删除成功');
  reload();
}

function handleSuccess() {
  createMessage.success('操作成功');
  reload();
}

async function handleExport() {
  const data = await getInsectControlList(searchInfo);
  exportInsectControl(data);
}

function handleRemove() {
  fileList.value = [];
}

function beforeUpload(file) {
  fileList.value = [...fileList.value, file];
  return false;
}

async function handleImport() {
  if (fileList.value.length === 0) {
    createMessage.warning('请选择要导入的文件');
    return;
  }

  const formData = new FormData();
  fileList.value.forEach((file) => {
    formData.append('file', file);
  });

  try {
    await importInsectControl(formData);
    createMessage.success('导入成功');
    reload();
    fileList.value = [];
  } catch (error) {
    createMessage.error('导入失败');
  }
}

// 组件挂载时，可以设置默认基地ID
onMounted(() => {
  // 这里可以从用户信息或系统设置中获取默认基地ID
  // const defaultBaseId = getDefaultBaseId();
  // if (defaultBaseId) {
  //   selectedBaseId.value = defaultBaseId;
  //   loadInsectControlData();
  // }

  // 模拟设置默认基地ID，实际项目中应该从API或配置中获取
  // defaultBaseId.value = '1';
});
</script>

<style lang="less" scoped>
.insect-control-page {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 64px);
}

.page-header {
  margin-bottom: 24px;

  .page-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 20px;
    font-weight: 600;
    color: #262626;
    margin-bottom: 8px;
  }

  .page-description {
    color: #8c8c8c;
    font-size: 14px;
  }
}

.selection-timeline-row {
  margin-bottom: 16px;

  .selection-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 500;
  }

  .timeline-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 500;
  }
}

// 基地地块选择与时间轴组合卡片样式
.combined-selection-timeline-card {
  margin-bottom: 16px;

  :deep(.ant-card-body) {
    padding-left: 8px;
    padding-right: 8px;
    padding-top: 0px;
    padding-bottom: 0px;
  }

  .selection-area, .timeline-area {
    height: 100%;
  }

  .selection-title {
    font-size: 14px;
    font-weight: 500;
    color: #333;
    margin-bottom: 8px;
    display: flex;
    align-items: center;

    &::before {
      content: '';
      display: inline-block;
      width: 3px;
      height: 14px;
      background-color: #52c41a;
      margin-right: 8px;
      border-radius: 2px;
    }
  }

  .timeline-title {
    font-size: 14px;
    font-weight: 500;
    color: #333;
    margin-bottom: 12px;
    display: flex;
    align-items: center;

    &::before {
      content: '';
      display: inline-block;
      width: 3px;
      height: 14px;
      background-color: #1890ff;
      margin-right: 8px;
      border-radius: 2px;
    }
  }
}

// 间距调整类
.mb-8 {
  margin-bottom: 8px;
}

.table-card {
  margin-top: 16px;

  .table-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 500;
  }
}

.empty-card,
.loading-card {
  margin-top: 16px;
  text-align: center;
}

// 响应式布局
@media (max-width: 1200px) {
  .combined-selection-timeline-card {
    .ant-col {
      margin-bottom: 16px;
    }

    :deep(.ant-row) {
      flex-direction: column;

      .ant-col {
        width: 100% !important;
        margin-bottom: 16px;

        &:last-child {
          margin-bottom: 0;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .insect-control-page {
    padding: 16px;
  }

  .combined-selection-timeline-card {
    :deep(.ant-card-body) {
      padding-left: 8px;
      padding-right: 8px;
      padding-top: 5px;
      padding-bottom: 5px;
    }
  }
}
.indicator p {
  margin: 4px 0;
  font-size: 15px;
}

.disease-card {
  padding: 24px;
  background-color: #f0f2f5;
}

.main-content-row {
  display: flex;
  gap: 16px; /* 列间距 */
  align-items: stretch; /* 三列等高 */
}

.main-content-row > .ant-col {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 250px;
}
.disease-row {
  display: flex;
  align-items: center;
  gap: 16px;
}
.prediction-info {
  flex: 1;
}
.inner-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: flex-start; /* 保证卡片内上下元素间距 */
}

/* ---------- 图像区域 ---------- */
.image-container {
  flex: none;
  height: 600px; /* 固定高度，防止塌陷 */
  display: flex;
  justify-content: center;
  align-items: center;
}

.disease-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 6px;
}
.lishi-image {
  width: 200px;
  height: 130px;
  object-fit: cover;
  border-radius: 8px;
}
/* ---------- 图表区域 ---------- */
.chart-container {
  width: 100%;
  height: 600px; /* 图表固定高度，避免被压缩 */
}

/* ---------- 农药选择与分析 ---------- */
.pesticide-select {
  width: 100%;
  margin-top: 12px;
}

.analysis-result {
  margin-top: 8px;
  padding: 12px;
  background-color: #f6ffed;
  border: 1px solid #b7eb8f;
  border-radius: 4px;
  color: #389e0d;
  font-weight: 500;
  text-align: left;
}

/* ---------- 时间显示 ---------- */
.update-time {
  margin-top: 8px;
  font-size: 12px;
  color: #888;
  text-align: right;
}


.history-card {
  margin-top: 16px;           /* 与上面内容保持间距 */
  padding: 16px;              /* 内部间距 */
  background-color: #f0f2f5;  /* 和主卡片统一背景 */
  border-radius: 8px;         /* 圆角 */
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.history-card .ant-row {
  align-items: flex-start;    /* 左对齐，顶部对齐 */
  gap: 8px;                   /* 列间距 */
}

.history-card .ant-col {
  display: flex;
  align-items: flex-start;    /* 左对齐 */
}

.history-card a-select {
  width: 100%;
}

.history-card a-button {
  width: 100%;
}
.history-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 16px;           /* 与上面内容保持间距 */

}

.history-card-item {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 12px;
}

.history-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.record-info {
  flex: 1;
}

.disease-entry {
  margin: 6px 0;
}

.common-info {
  color: #666;
  font-size: 13px;
}

.image-wrapper {
  flex-shrink: 0;
}

.pest-image {
  width: 200px;
  height: 130px;
  object-fit: cover;
  border-radius: 8px;
}

.record-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.record-time {
  font-weight: bold;
  margin-bottom: 8px;
  color: #333;
}

.pest-entry {
  margin-bottom: 6px;
}

.disease-name {
  font-weight: 500;
  color: #1677ff;
}

.disease-count {
  font-weight: 500;
  color: #1677ff;
}

.fertilizer-select {
  margin-top: 20px;
  width: 100%;
  margin-bottom: 12px;
}
.no-data {
  text-align: center;
  color: #999;
  margin-top: 20px;
}

/* 响应式：小屏幕换行 */
@media (max-width: 992px) {
  .main-content-row {
    flex-direction: column;
  }
  .main-content-row > .ant-col {
    min-width: 100%;
  }
}


</style>
