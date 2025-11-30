<template>
  <div class="irrigation-page">
    <!-- 选择与概览区域 -->
    <a-row :gutter="16" class="top-row">
      <a-col :xs="24" :md="12">
        <a-card :bordered="false" class="rich-card">
          <template #title>
            <div class="table-title">
              <Icon icon="ant-design:dot-chart-outlined" /> 土壤水分概览
            </div>
          </template>
          <div class="moisture-card" v-if="hasData">
            <a-progress type="dashboard" :percent="soilMoisturePercent" status="normal" :format="progressText" :stroke-color="moistureStateColor" />
            <div class="moisture-info">
              <div>当前含水率：{{ soilMoisturePercent }}%</div>
              <div>
                含水状态：<Tag :color="moistureStateColor">{{ moistureStateLabel }}</Tag>
              </div>
            </div>
          </div>
          <a-empty v-else description="暂无数据，请选择有效地块或检查后台" />
          <div class="statistic-grid">
            <div class="metric">
              <div class="metric-title">趋势</div>
              <div class="metric-value">{{ soilMoistureTrendText }}</div>
            </div>
            <div class="metric">
              <div class="metric-title">上次更新</div>
              <div class="metric-value">{{ lastUpdatedText }}</div>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="24" :md="12">
        <a-card :bordered="false" class="rich-card">
          <template #title>
            <div class="table-title">
              <Icon icon="ant-design:solution-outlined" /> 灌溉建议（Penman）
            </div>
          </template>
          <div class="suggestion-card" v-if="hasData">
            <a-descriptions bordered size="small" :column="1">
              <a-descriptions-item label="是否需要灌溉">
                <Tag :color="penmanSuggestion.needIrrigation ? 'red' : 'green'">{{ penmanSuggestion.needIrrigation ? '需要' : '暂不需要' }}</Tag>
              </a-descriptions-item>
              <a-descriptions-item label="建议时间">{{ penmanSuggestion.recommendedTime || '-' }}</a-descriptions-item>
              <a-descriptions-item label="建议方式">{{ penmanSuggestion.method || '-' }}</a-descriptions-item>
              <a-descriptions-item label="推荐灌水量">
                {{ recommendedVolumeMm }} mm（约 {{ mmToM3PerMu(recommendedVolumeMm) }} m³/亩）
              </a-descriptions-item>
              <a-descriptions-item label="原因" v-if="penmanSuggestion.reason">{{ penmanSuggestion.reason }}</a-descriptions-item>
            </a-descriptions>
            <div class="actions">
              <a-button size="small" @click="copySuggestion" :disabled="!selectedPlotId">复制建议</a-button>
              <a-button size="small" @click="refresh" :disabled="!selectedPlotId">刷新数据</a-button>
              <a-button size="small" @click="openReportModal('irrigation')" :disabled="!selectedPlotId || !hasData">生成报告</a-button>
            </div>
          </div>
          <a-empty v-else description="暂无建议，等待后端数据" />
        </a-card>
      </a-col>
    </a-row>

    <!-- 趋势折线图板块（四个方框） -->
    <a-row :gutter="16" class="mt-4">
      <a-col :xs="24" :md="12">
        <a-card :bordered="false" class="rich-card">
          <template #title>
            <div class="table-title">
              <Icon icon="ant-design:line-chart-outlined" /> 土壤含水率（近24小时）
            </div>
          </template>
          <div v-show="moistureTrendData.length" ref="moistureTrendRef" class="chart-ref chart-220" />
        </a-card>
      </a-col>
      <a-col :xs="24" :md="12">
        <a-card :bordered="false" class="rich-card">
          <template #title>
            <div class="table-title">
              <Icon icon="ant-design:line-chart-outlined" /> 参考蒸散（ET0，近24小时）
            </div>
          </template>
          <div v-show="et0Forecast.length" ref="et0ChartRef" style="width: 100%; height: 220px" />
        </a-card>
      </a-col>
      <a-col :xs="24" :md="12">
        <a-card :bordered="false" class="rich-card">
          <template #title>
            <div class="table-title">
              <Icon icon="ant-design:line-chart-outlined" /> 降雨量（近24小时）
            </div>
          </template>
          <div v-show="penmanInputs.precip && penmanInputs.precip.length" ref="rainChartRef" style="width: 100%; height: 220px" />
        </a-card>
      </a-col>
      <a-col :xs="24" :md="12">
        <a-card :bordered="false" class="rich-card">
          <template #title>
            <div class="table-title">
              <Icon icon="ant-design:line-chart-outlined" /> 平均温度（近24小时）
            </div>
          </template>
          <div v-show="penmanInputs.temp && penmanInputs.temp.length" ref="tempChartRef" style="width: 100%; height: 220px" />
        </a-card>
      </a-col>
    </a-row>

    <!-- Penman 输入数据折线图 -->
    <a-row :gutter="16" class="mt-4">
      <a-col :xs="24" :lg="24">
        <a-card :bordered="false" class="rich-card">
          <template #title>
            <div class="table-title">
              <Icon icon="ant-design:line-chart-outlined" /> Penman 算法输入数据（近24小时）
            </div>
          </template>
          <div v-show="penmanInputs.dates && penmanInputs.dates.length" ref="penmanInputsChartRef" style="width: 100%; height: 320px" />
        </a-card>
      </a-col>
    </a-row>

    <!-- 对比图 -->
    <a-row :gutter="16" class="mt-4">
      <a-col :xs="24" :lg="24">
        <a-card :bordered="false">
          <template #title>
            <div class="table-title">
              <Icon icon="ant-design:area-chart-outlined" /> 干预灌溉 vs 不干预对比
            </div>
          </template>
          <div v-show="comparisonReady" ref="comparisonChartRef" class="chart-ref" style="width: 100%; height: 320px" />
        </a-card>
      </a-col>
    </a-row>

    <!-- 天气与蒸散 / 风险与操作 -->
    <a-row :gutter="16" class="mt-4 row-stretch">
      <a-col :xs="24" :md="12">
        <a-card :bordered="false" class="rich-card">
          <template #title>
            <div class="table-title">
              <Icon icon="ant-design:cloud-outlined" /> 天气与蒸散概览
            </div>
          </template>
          <a-space v-if="hasData" direction="vertical" style="width: 100%">
            <div class="statistic-grid">
              <div class="metric">
              <div class="metric-title">最近1小时 ET0</div>
              <div class="metric-value">{{ et0Last1h }} mm</div>
              </div>
              <div class="metric">
              <div class="metric-title">最近3小时 ET0平均</div>
              <div class="metric-value">{{ et0Last3hAvg }} mm</div>
              </div>
              <div class="metric">
              <div class="metric-title">近24小时 ET0累计</div>
              <div class="metric-value">{{ et0Sum24h }} mm</div>
              </div>
            </div>
            <div class="hint">基于 Penman 估算的参考蒸散量，供灌溉决策参考。</div>
          </a-space>
          <a-empty v-else description="暂无数据" />
        </a-card>
      </a-col>
      <a-col :xs="24" :md="12">
        <a-card :bordered="false" class="rich-card">
          <template #title>
            <div class="table-title">
              <Icon icon="ant-design:warning-outlined" /> 风险与提示 / 快捷操作
            </div>
          </template>
          <a-space v-if="hasData" direction="vertical" style="width: 100%">
            <a-alert :message="`当前灌溉风险：${riskLevel}`" type="warning" show-icon />
            <ul class="tips">
              <li v-for="(tip, idx) in riskTips" :key="idx">{{ tip }}</li>
            </ul>
            <div class="actions">
              <a-button size="small" @click="copySuggestion" :disabled="!selectedPlotId || !hasData">复制建议</a-button>
              <a-button size="small" @click="refresh" :disabled="!selectedPlotId">刷新数据</a-button>
              <a-button size="small" @click="openReportModal('risk')" :disabled="!selectedPlotId || !hasData">导出报告</a-button>
            </div>
          </a-space>
          <a-empty v-else description="暂无数据" />
        </a-card>
      </a-col>
    </a-row>

    <!-- 一键灌溉管理（移至页面底部） -->
    <a-row class="mt-4">
      <a-col :span="24">
        <a-card :bordered="false" class="rich-card">
          <template #title>
            <div class="table-title">
              <Icon icon="ant-design:tool-outlined" /> 一键灌溉管理
            </div>
          </template>
          <div class="quick-card">
            <a-space direction="vertical" style="width: 100%; gap: 8px;">
              <a-row :gutter="12" justify="center" align="middle">
                <a-col :xs="24" :sm="12" :md="3">
                  <div class="form-item form-inline">
                    <div class="label">算法推荐</div>
                    <a-switch v-model:checked="quickUseAlgorithm" size="small" />
                  </div>
                </a-col>

                <!-- 替换 BaseSelect 为自定义基地下拉框 -->
                <a-col :xs="24" :sm="12" :md="5">
                  <div class="form-item">
                    <div class="label">选择基地</div>
                    <div 
                      class="custom-select" 
                      @click="toggleDropdown('base')"
                      style="cursor: pointer; width: 100%;"
                    >
                      <div class="select-value">{{ selectedBase?.baseName || '请选择基地' }}</div>
                      <div class="select-icon" :class="{ open: isDropdownOpen.base }">▼</div>
                      <div class="select-options" v-if="isDropdownOpen.base">
                        <div class="options-scroll">
                          <div
                            class="option-item"
                            v-for="item in baseList"
                            :key="item.baseId"
                            @click.stop="selectItem('base', item)"
                          >
                            {{ item.baseName }}
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </a-col>

                <!-- 替换 PlotSelect 为自定义地块下拉框 -->
                <a-col :xs="24" :sm="12" :md="5">
                  <div class="form-item">
                    <div class="label">选择地块</div>
                    <div 
                      class="custom-select" 
                      @click="toggleDropdown('plot')"
                      style="cursor: pointer; width: 100%;"
                      :class="{ disabled: !selectedBase }"
                    >
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
                </a-col>

                <a-col :xs="24" :sm="12" :md="4">
                  <div class="form-item">
                    <div class="label">灌溉面积(亩)</div>
                    <a-input-number v-model:value="quickArea" :min="0" :step="0.1" style="width: 100%" />
                  </div>
                </a-col>
                <a-col :xs="24" :sm="12" :md="4">
                  <div class="form-item">
                    <div class="label">灌溉次数</div>
                    <a-input-number v-model:value="quickTimes" :min="1" :step="1" style="width: 100%" />
                  </div>
                </a-col>
                <a-col :xs="24" :sm="12" :md="3">
                  <div class="form-item">
                    <div class="label">灌溉方式</div>
                    <a-select v-model:value="quickMethod" :options="irrigationMethodOptions" style="width: 100%" />
                  </div>
                </a-col>
              </a-row>

              <a-row :gutter="12" justify="center" align="middle">
                <a-col :xs="24" :md="8">
                  <div class="form-item">
                    <div class="label">灌溉日期</div>
                    <a-date-picker v-model:value="quickDate" style="width: 100%" />
                  </div>
                </a-col>
                <a-col :xs="24" :md="16">
                  <div class="form-item">
                    <div class="label">备注</div>
                    <a-input v-model:value="quickRemark" placeholder="备注（可选）" />
                  </div>
                </a-col>
              </a-row>

              <a-divider dashed style="margin: 8px 0;" />
              <div v-if="hasData" class="calc-result">
                <span v-if="quickUseAlgorithm">按建议：{{ recommendedVolumeMm }} mm ≈ {{ algoWaterPerMuM3 }} m³/亩</span>
                <span v-else>人工设定：每亩 {{ manualWaterPerMu }} m³</span>
                <span>分次灌溉：{{ quickTimes }} 次，每次约 {{ perTimeWaterM3 }} m³</span>
                <span>总用水量：{{ totalWaterUsageM3 }} m³，预估时长：{{ durationHours }} 小时</span>
              </div>

              <div class="quick-actions">
                <a-space :size="12" align="center">
                  <a-button type="primary" @click="calcQuickIrrigation" :disabled="!selectedPlotId || !hasData">计算用水量</a-button>
                  <a-button @click="executeAutomationQuick" :disabled="!selectedPlotId">执行自动化（占位）</a-button>
                </a-space>
              </div>
            </a-space>
          </div>
        </a-card>
      </a-col>
    </a-row>

    
    <a-modal v-model:open="reportModalVisible" title="智慧灌溉报告" :width="800">
      <div style="padding:16px" v-if="reportType==='irrigation' && hasData" id="irrigationReport">
        <h3>智慧灌溉报告</h3>
        <p>地块：{{ selectedPlotName }}</p>
        <p>含水率：{{ soilMoisturePercent }}%</p>
        <p>建议：{{ penmanSuggestion.needIrrigation ? '需要灌溉' : '暂不需要' }}</p>
        <p>时间：{{ penmanSuggestion.recommendedTime || '-' }}</p>
        <p>方式：{{ penmanSuggestion.method || '-' }}</p>
        <p>推荐灌水量：{{ recommendedVolumeMm }} mm（约 {{ mmToM3PerMu(recommendedVolumeMm) }} m³/亩）</p>
        <h4>ET0 近24小时</h4>
        <table style="width:100%;border-collapse:collapse">
          <thead>
            <tr>
              <th v-for="d in chartDates" :key="d" style="border:1px solid #ddd;padding:4px">{{ d }}</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td v-for="(v,idx) in et0Forecast" :key="idx" style="border:1px solid #ddd;padding:4px">{{ v }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div style="padding:16px" v-if="reportType==='risk' && hasData" id="riskReport">
        <h3>灌溉风险与提示报告</h3>
        <p>地块：{{ selectedPlotName }}</p>
        <p>当前风险等级：{{ riskLevel }}</p>
        <h4>提示与建议</h4>
        <ul class="tips">
          <li v-for="(tip, idx) in riskTips" :key="idx">{{ tip }}</li>
        </ul>
      </div>
      <template #footer>
        <a-space>
          <a-button type="primary" @click="downloadReportAsWord" :disabled="!hasData">下载 Word 文档</a-button>
          <a-button @click="reportModalVisible=false">关闭</a-button>
        </a-space>
      </template>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, watch, Ref, computed, onMounted, nextTick } from 'vue';
import { printJS } from '/@/hooks/web/usePrintJS';
import { useMessage } from '/@/hooks/web/useMessage';
import { Icon } from '/@/components/Icon';
import { getPlotStatus, getPenmanPredict, getInterventionComparison } from './irrigation.api';
import { useECharts } from '/@/hooks/web/useECharts';
import { Tag } from 'ant-design-vue';
import { defHttp } from '/@/utils/http/axios';

// ---------------- 核心修改：替换为真实数据库接口 ----------------
// 导入真实的基地/地块接口（与下方验证过的接口一致）
import { useSelectStore } from '/@/store/selectStore';
import { getBaseList, getPlotsByBaseId, getPlotById } from '/@/views/rapeseed/production-plan/plot-production-plan/base.api';

// 定义与数据库表结构一致的类型
interface BaseItem {
  baseId: string | number;
  baseName: string;
  fullName?: string;
  longitude?: string;
  latitude?: string;
}

interface PlotItem {
  plotId: string | number;
  plotName: string;
}

const { createMessage } = useMessage();
const searchInfo = reactive<Recordable>({});

// ---------------- 核心：统一的基地/地块数据源（数据库bases/plots表） ----------------
const baseList = ref<BaseItem[]>([]); // 基地列表（来自bases表）
const plotList = ref<PlotItem[]>([]); // 地块列表（来自plots表）
const selectedBase = ref<BaseItem | null>(null); // 选中基地
const selectedPlot = ref<PlotItem | null>(null); // 选中地块
const isDropdownOpen = reactive({ base: false, plot: false }); // 下拉框展开状态

// 全局状态仓库（同步选择状态）
const selectStore = useSelectStore();

// 原有选择相关变量（保持兼容）
const selectedBaseId = ref<string | number | undefined>(undefined);
const selectedPlotId = ref<string | number | undefined>(undefined);
const selectedPlotName = ref<string>('');
const lockedPlotId = ref<string | number | undefined>(undefined);
const lockedPlotName = ref<string>('');
const plotSelectRef = ref<any>(null);
const selectedVarietyId = ref<string | number | undefined>(undefined);
const currentStageId = ref<string | number | undefined>(undefined);

// 土壤水分与建议
const hasData = ref<boolean>(false);
const comparisonReady = ref<boolean>(false);
const soilMoisturePercent = ref<number>(0);
const soilMoistureTrendText = ref<string>('');
const penmanSuggestion = reactive({ needIrrigation: false, recommendedTime: '', method: '', reason: '' });
const recommendedVolumeMm = ref<number>(0);
const lastRequestPlotId = ref<string | number | undefined>(undefined);

// 图表
const comparisonChartRef = ref<HTMLDivElement | null>(null);
const { setOptions: setComparisonOptions } = useECharts(comparisonChartRef as Ref<HTMLDivElement>);
const moistureTrendRef = ref<HTMLDivElement | null>(null);
const { setOptions: setMoistureTrendOptions } = useECharts(moistureTrendRef as Ref<HTMLDivElement>);
const et0ChartRef = ref<HTMLDivElement | null>(null);
const { setOptions: setEt0Options } = useECharts(et0ChartRef as Ref<HTMLDivElement>);
const penmanInputsChartRef = ref<HTMLDivElement | null>(null);
const { setOptions: setPenmanInputsOptions } = useECharts(penmanInputsChartRef as Ref<HTMLDivElement>);
const rainChartRef = ref<HTMLDivElement | null>(null);
const { setOptions: setRainOptions } = useECharts(rainChartRef as Ref<HTMLDivElement>);
const tempChartRef = ref<HTMLDivElement | null>(null);
const { setOptions: setTempOptions } = useECharts(tempChartRef as Ref<HTMLDivElement>);

// ---------------- 核心：统一的下拉框交互方法（与数据库交互） ----------------
/** 切换下拉框展开/收起 */
const toggleDropdown = (type: 'base' | 'plot') => {
  // 关闭其他下拉框，只展开当前
  Object.keys(isDropdownOpen).forEach(key => {
    if (key !== type) isDropdownOpen[key as 'base' | 'plot'] = false;
  });
  isDropdownOpen[type] = !isDropdownOpen[type];
};

/** 选择基地/地块选项（同步数据库数据） */
const selectItem = async (type: 'base' | 'plot', item: BaseItem | PlotItem) => {
  if (type === 'base') {
    const baseItem = item as BaseItem;
    // 选中基地（同步数据库数据）
    selectedBase.value = baseItem;
    selectedBaseId.value = baseItem.baseId;
    selectedPlot.value = null;
    selectedPlotId.value = undefined;
    selectedPlotName.value = '';
    
    // 更新全局状态
    selectStore.updateSelectedBase({
      baseId: baseItem.baseId,
      baseName: baseItem.baseName,
      longitude: baseItem.longitude,
      latitude: baseItem.latitude
    });

    // 加载当前基地下的地块（从plots表获取）
    plotList.value = await fetchPlotListByBaseId(baseItem.baseId);

    // 触发基地变更逻辑并清空当前展示数据
    onBaseChange(baseItem.baseId);

    if (plotList.value.length > 0) {
      const target = plotList.value[0] as any;
      lockedPlotId.value = target.plotId;
      lockedPlotName.value = target.plotName;
      selectedPlot.value = target;
      selectedPlotId.value = target.plotId;
      selectedPlotName.value = target.plotName;
      selectStore.updateSelectedPlot(target);
      await onPlotChange(target.plotId);
    }
  } else if (type === 'plot') {
    const plotItem = item as PlotItem;
    if (!lockedPlotId.value) return;
    const target: any = { plotId: lockedPlotId.value, plotName: lockedPlotName.value };
    selectedPlot.value = target;
    selectedPlotId.value = lockedPlotId.value;
    selectedPlotName.value = lockedPlotName.value;
    selectStore.updateSelectedPlot(target);
    await onPlotChange(lockedPlotId.value as any);
  }
  // 关闭下拉框
  isDropdownOpen[type] = false;
};

/**
 * 根据基地ID获取地块列表
 * @param baseId 基地ID
 */
const fetchPlotListByBaseId = async (baseId: string | number) => {
  try {
    // 优先使用导入的接口，如果接口不存在则使用通用请求
    if (typeof getPlotsByBaseId === 'function') {
      const res = await getPlotsByBaseId(baseId);
      const data = res.result || res;
      return data.map((item: any) => ({
        plotId: item.id || item.plotId,
        plotName: item.plotName || item.name
      })) as PlotItem[];
    } else {
      // 备用通用接口
      const res = await defHttp.get({
        url: '/youcai/plot/listByBaseId',
        params: { baseId: baseId }
      });
      const data = res.result || res;
      return data.map((item: any) => ({
        plotId: item.id || item.plotId,
        plotName: item.plotName || item.name
      })) as PlotItem[];
    }
  } catch (error) {
    console.error('获取地块列表失败：', error);
    createMessage.error('获取地块列表失败，请重试');
    return [];
  }
};

/**
 * 获取基地列表（整合版，避免重复定义）
 */
const fetchBaseListData = async () => {
  try {
    // 优先使用导入的接口
    if (typeof getBaseList === 'function') {
      const res = await getBaseList();
      // 从Result对象中获取数据列表，兼容后端返回格式
      const baseDataList = res.result || res || [];
      baseList.value = baseDataList.map((item: any) => ({
        baseId: item.id || item.baseId,
        baseName: item.baseName || item.fullName,
        fullName: item.fullName,
        longitude: item.longitude || '',
        latitude: item.latitude || ''
      })) as BaseItem[];
    } else {
      // 备用通用接口
      const res = await defHttp.get({
        url: '/youcai/base/list',
        params: { delFlag: 0 }
      });
      const baseDataList = res.result || res || [];
      baseList.value = baseDataList.map((item: any) => ({
        baseId: item.id || item.baseId,
        baseName: item.baseName || item.fullName,
        fullName: item.fullName,
        longitude: item.longitude || '',
        latitude: item.latitude || ''
      })) as BaseItem[];
    }
    
    console.log('获取基地列表成功，共', baseList.value.length, '条');

    // 默认选中第一个基地，并加载其地块列表
  if (baseList.value.length > 0) {
      selectedBase.value = baseList.value[0];
      selectedBaseId.value = baseList.value[0].baseId;
      
      // 更新全局状态
      selectStore.updateSelectedBase({
        baseId: baseList.value[0].baseId,
        baseName: baseList.value[0].baseName,
        longitude: baseList.value[0].longitude,
        latitude: baseList.value[0].latitude
      });

      // 加载该基地的地块列表
      plotList.value = await fetchPlotListByBaseId(baseList.value[0].baseId);

      if (plotList.value.length > 0) {
        const target = plotList.value[0] as any;
        lockedPlotId.value = target.plotId;
        lockedPlotName.value = target.plotName;
        selectedPlot.value = target;
        selectedPlotId.value = target.plotId;
        selectedPlotName.value = target.plotName;
        selectStore.updateSelectedPlot(target);
        await onPlotChange(target.plotId);
      }
    }
  } catch (error) {
    console.error('获取基地列表失败：', error);
    baseList.value = [];
    createMessage.error('获取基地列表失败，请检查网络');
  }
};

onMounted(async () => {
  await fetchBaseListData(); // 加载bases表数据
});
 
watch(() => selectStore.selectedBase.baseId, async (baseId, prev) => {
  if (!baseId || baseId === prev) return;
  selectedBaseId.value = baseId;
});

watch(() => selectStore.selectedPlot.plotId, async (pid, prev) => {
  if (!pid || pid === prev) return;
  selectedPlotId.value = pid;
  selectedPlotName.value = selectStore.selectedPlot.plotName || '';
  await onPlotChange(pid as any);
});

watch(selectedBaseId, async (val, oldVal) => {
  if (val === undefined || val === null || val === oldVal) return;
  resetIrrigationState();
  plotList.value = await fetchPlotListByBaseId(val as any);
  if (plotList.value.length > 0) {
    const target = plotList.value[0] as any;
    lockedPlotId.value = target.plotId;
    lockedPlotName.value = target.plotName;
    selectedPlot.value = target;
    selectedPlotId.value = target.plotId;
    selectedPlotName.value = target.plotName;
    selectStore.updateSelectedPlot(target);
    await onPlotChange(target.plotId);
  } else {
    selectedPlot.value = null;
    selectedPlotId.value = undefined;
    selectedPlotName.value = '';
    resetIrrigationState();
    await fetchBaseStatusAndSuggest(val as any);
    await fetchInterventionComparisonBase(val as any);
  }
});

watch(selectedPlotId, async (val, oldVal) => {
  if (val === undefined || val === null || val === oldVal) return;
  await onPlotChange(val as any);
});

// 原有业务逻辑方法
 

function resetIrrigationState() {
  comparisonReady.value = false;
  soilMoisturePercent.value = 0;
  soilMoistureTrendText.value = '';
  currentStageId.value = '';
  penmanSuggestion.needIrrigation = false;
  penmanSuggestion.recommendedTime = '';
  penmanSuggestion.method = '';
  penmanSuggestion.reason = '';
  recommendedVolumeMm.value = 0;
  flowRateM3PerHour.value = 0;
  chartDates.value = [];
  et0Forecast.value = [];
  moistureTrendData.value = [];
  penmanInputs.value = { dates: [], temp: [], humidity: [], wind: [], solar: [], precip: [] };
  lastUpdatedText.value = '-';
}

function onBaseChange(value: string | number | undefined) {
  selectedPlotId.value = undefined;
  selectedPlotName.value = '';
  resetIrrigationState();
  if (value) {
    fetchBaseStatusAndSuggest(value);
    fetchInterventionComparisonBase(value);
  }
}

function onPlotLoaded(options: any[]) {
  if (selectedPlotId.value) {
    const found = options.find((o: any) => o.id === selectedPlotId.value);
    selectedPlotName.value = found ? found.plotName : '';
  }
}

async function onPlotChange(value: string | number | undefined) {
  if (value) {
    resetIrrigationState();
    const effectiveId = resolveEffectivePlotId(value);
    lastRequestPlotId.value = effectiveId;
    await fetchPlotStatusAndSuggest(effectiveId);
    await fetchInterventionComparison(effectiveId);
  } else {
    selectedPlotName.value = '';
    resetIrrigationState();
    if (selectedBaseId.value) {
      await fetchBaseStatusAndSuggest(selectedBaseId.value as any);
      await fetchInterventionComparisonBase(selectedBaseId.value as any);
    }
  }
}

async function fetchPlotStatusAndSuggest(pid?: string | number) {
  const originId = pid ?? selectedPlotId.value;
  const currentId = resolveEffectivePlotId(originId as any);
  if (!currentId) return;
  try {
    const status = await getPlotStatus(currentId);
    if (lastRequestPlotId.value !== currentId) return;
    console.log('地块状态原始数据：', status);
    
    // 修正：处理BigDecimal转Number，兼容null/字符串
    soilMoisturePercent.value = Number(status?.soilMoisturePercent ?? 0) || 0;
    soilMoistureTrendText.value = status?.soilMoistureTrend ?? '';
    currentStageId.value = status?.currentStageId ?? '';
    lastUpdatedText.value = status?.lastUpdated ? new Date(status.lastUpdated).toLocaleString() : '-';

    const suggest = await getPenmanPredict(currentId);
    console.log('Penman建议原始数据：', suggest);
    
    // 如果期间地块已切换，丢弃本次结果
    if (lastRequestPlotId.value !== currentId) return;

    penmanSuggestion.needIrrigation = Boolean(suggest?.needIrrigation ?? false);
    penmanSuggestion.recommendedTime = suggest?.recommendedTime ?? '';
    penmanSuggestion.method = suggest?.method ?? '';
    penmanSuggestion.reason = suggest?.reason ?? '';
    
    // 修正：灌水量数值转换
    recommendedVolumeMm.value = Number(suggest?.recommendedVolumeMm ?? 0) || 0;
    flowRateM3PerHour.value = Number(suggest?.flowRateM3PerHour ?? 0) || 0;

    // 修正：图表数据转换（兼容BigDecimal）
    chartDates.value = suggest?.chartDates ?? [];
    if ((!chartDates.value || chartDates.value.length === 0) && Array.isArray(suggest?.penmanInputs?.dates)) {
      chartDates.value = suggest.penmanInputs.dates;
    }
    et0Forecast.value = (suggest?.et0Forecast ?? []).map(v => Number(v) || 0);
    penmanInputs.value = suggest?.penmanInputs ?? { dates: [], temp: [], humidity: [], wind: [], solar: [], precip: [] };
    moistureTrendData.value = (suggest?.soilMoistureSeriesPct ?? []).map(v => Number(v) || 0);
    
    if (chartDates.value.length) {
      await nextTick();
      renderMoistureTrendChart(chartDates.value, moistureTrendData.value as number[]);
      renderEt0Chart(chartDates.value, et0Forecast.value);
      renderPenmanInputsChart(penmanInputs.value);
      // 修正：降雨量/温度数值转换
      renderRainChart(chartDates.value, penmanInputs.value.precip.map(v => Number(v) || 0));
      renderTempChart(chartDates.value, penmanInputs.value.temp.map(v => Number(v) || 0));
    }
    const statusHasData = Boolean(status);
    const chartHasData = (chartDates.value?.length ?? 0) > 0 || (moistureTrendData.value?.length ?? 0) > 0 || (penmanInputs.value?.dates?.length ?? 0) > 0;
    const valueHasData = (soilMoisturePercent.value ?? 0) > 0 || (recommendedVolumeMm.value ?? 0) > 0;
    hasData.value = statusHasData || chartHasData || valueHasData;
    if (!hasData.value || !(chartDates.value?.length ?? 0)) {
      if (selectedBaseId.value) {
        await fetchBaseStatusAndSuggest(selectedBaseId.value as any);
        await fetchInterventionComparisonBase(selectedBaseId.value as any);
      } else {
        resetIrrigationState();
      }
    }
  } catch (e) {
    console.error('请求地块数据失败：', e);
    createMessage.error('获取地块数据失败，请重试');
    if (selectedBaseId.value) {
      await fetchBaseStatusAndSuggest(selectedBaseId.value as any);
      await fetchInterventionComparisonBase(selectedBaseId.value as any);
    }
  }
}

async function fetchBaseStatusAndSuggest(baseId: string | number) {
  try {
    if (!baseId) return;
    const status = await defHttp.get({ url: `/rapeseed/irrigation/plotStatusByBase/${baseId}` });
    soilMoisturePercent.value = Number(status?.soilMoisturePercent ?? 0) || 0;
    soilMoistureTrendText.value = status?.soilMoistureTrend ?? '';
    lastUpdatedText.value = status?.lastUpdated ? new Date(status.lastUpdated).toLocaleString() : '-';
    const suggest = await defHttp.get({ url: `/rapeseed/irrigation/penmanPredict`, params: { baseId } });
    if (!suggest) return;
    penmanSuggestion.needIrrigation = Boolean(suggest?.needIrrigation ?? false);
    penmanSuggestion.recommendedTime = suggest?.recommendedTime ?? '';
    penmanSuggestion.method = suggest?.method ?? '';
    penmanSuggestion.reason = suggest?.reason ?? '';
    recommendedVolumeMm.value = Number(suggest?.recommendedVolumeMm ?? 0) || 0;
    flowRateM3PerHour.value = Number(suggest?.flowRateM3PerHour ?? 0) || 0;
    chartDates.value = suggest?.chartDates ?? [];
    if ((!chartDates.value || chartDates.value.length === 0) && Array.isArray(suggest?.penmanInputs?.dates)) {
      chartDates.value = suggest.penmanInputs.dates;
    }
    et0Forecast.value = (suggest?.et0Forecast ?? []).map((v: any) => Number(v) || 0);
    penmanInputs.value = suggest?.penmanInputs ?? { dates: [], temp: [], humidity: [], wind: [], solar: [], precip: [] };
    moistureTrendData.value = (suggest?.soilMoistureSeriesPct ?? []).map((v: any) => Number(v) || 0);
    if (chartDates.value.length) {
      await nextTick();
      renderMoistureTrendChart(chartDates.value, moistureTrendData.value as number[]);
      renderEt0Chart(chartDates.value, et0Forecast.value);
      renderPenmanInputsChart(penmanInputs.value);
      renderRainChart(chartDates.value, penmanInputs.value.precip.map((v: any) => Number(v) || 0));
      renderTempChart(chartDates.value, penmanInputs.value.temp.map((v: any) => Number(v) || 0));
    }
    const statusHasData = Boolean(status);
    const chartHasData = (chartDates.value?.length ?? 0) > 0 || (moistureTrendData.value?.length ?? 0) > 0 || (penmanInputs.value?.dates?.length ?? 0) > 0;
    const valueHasData = (soilMoisturePercent.value ?? 0) > 0 || (recommendedVolumeMm.value ?? 0) > 0;
    hasData.value = statusHasData || chartHasData || valueHasData;
  } catch (e) {
    console.error('获取基地数据失败：', e);
    createMessage.error('获取基地数据失败，请重试');
  }
}

async function fetchInterventionComparisonBase(baseId: string | number) {
  try {
    if (!baseId) return;
    const data = await defHttp.get({ url: `/rapeseed/irrigation/interventionComparison`, params: { baseId } });
    const dates = data?.dates ?? [];
    const withIrr = (data?.withIrrigation ?? []).map((v: any) => Number(v) || 0);
    const withoutIrr = (data?.withoutIrrigation ?? []).map((v: any) => Number(v) || 0);
    if (dates.length && withIrr.length === dates.length && withoutIrr.length === dates.length) {
      await nextTick();
      renderComparisonChart(dates, withIrr, withoutIrr);
      hasData.value = true;
      comparisonReady.value = true;
    }
  } catch (e) {
    console.error('获取基地干预对比失败：', e);
    createMessage.error('获取基地干预对比失败');
  }
}

async function fetchInterventionComparison(pid?: string | number) {
  const originId = pid ?? selectedPlotId.value;
  const currentId = resolveEffectivePlotId(originId as any);
  if (!currentId) {
    console.warn('没有选中地块，跳过干预对比数据获取');
    return;
  }
  try {
    console.log('开始获取干预对比数据，地块ID：', currentId);
    const data = await getInterventionComparison(currentId);
    console.log('干预对比原始数据：', data);
    
    if (lastRequestPlotId.value !== currentId) return;
    
    // 检查返回数据是否有效
    if (!data) {
      console.warn('干预对比数据为空');
      if (selectedBaseId.value) await fetchInterventionComparisonBase(selectedBaseId.value as any);
      return;
    }
    
    const dates = data?.dates ?? [];
    const withIrr = (data?.withIrrigation ?? []).map(v => Number(v) || 0);
    const withoutIrr = (data?.withoutIrrigation ?? []).map(v => Number(v) || 0);
    
    console.log('处理后的对比数据：', { dates, withIrr, withoutIrr });
    
    // 验证数据完整性，仅需长度一致即可渲染
    if (!dates.length) {
      console.warn('时间序列数据为空，无法渲染对比图表');
      if (selectedBaseId.value) await fetchInterventionComparisonBase(selectedBaseId.value as any);
      return;
    }
    if (withIrr.length !== dates.length || withoutIrr.length !== dates.length) {
      console.error('数据长度不匹配，日期长度：', dates.length, '干预数据长度：', withIrr.length, '非干预数据长度：', withoutIrr.length);
      if (selectedBaseId.value) await fetchInterventionComparisonBase(selectedBaseId.value as any);
      return;
    }
    
  await nextTick();
  renderComparisonChart(dates, withIrr, withoutIrr);
  hasData.value = true;
  comparisonReady.value = true;
  console.log('干预对比图表渲染完成');
  } catch (e) {
    console.error('获取对比数据失败：', e);
    // 显示具体的错误信息给用户
    createMessage.error(`获取干预对比数据失败：${(e as Error).message || '未知错误'}`);
  }
}

function resolveEffectivePlotId(pid: string | number | undefined): string | number | undefined {
  return lockedPlotId.value ?? pid;
}

function toHourLabels(dates: string[]): string[] {
  return (dates || []).map((s) => {
    const str = String(s || '');
    const parts = str.split(' ');
    const time = parts.length > 1 ? parts[1] : parts[0];
    const hm = time.split(':');
    const hh = hm[0] || time;
    const mm = hm[1] || '00';
    return `${hh}:${mm}`;
  });
}

function renderComparisonChart(dates: string[], withIrr: number[], withoutIrr: number[]) {
  const hours = toHourLabels(dates);
  setComparisonOptions({
    color: ['#37A2DA', '#ffd85c'],
    grid: { left: 40, right: 20, top: 40, bottom: 40 },
    tooltip: { trigger: 'axis', formatter: (params: any) => {
      const lines = params.map((p: any) => `${p.seriesName}: ${p.value}%`).join('<br/>');
      return `${params[0].axisValueLabel}<br/>${lines}`;
    } },
    legend: { data: ['干预灌溉','不干预灌溉'] },
    xAxis: { type: 'category', data: hours },
    yAxis: { type: 'value', name: '土壤含水率(%)' },
    series: [
      { name: '干预灌溉', type: 'line', smooth: true, data: withIrr, areaStyle: { opacity: 0.15 } },
      { name: '不干预灌溉', type: 'line', smooth: true, data: withoutIrr, areaStyle: { opacity: 0.15 } }
    ]
  });
}

function renderMoistureTrendChart(dates: string[], data: number[]) {
  const hours = toHourLabels(dates);
  setMoistureTrendOptions({
    color: ['#5B8FF9'],
    grid: { left: 38, right: 12, top: 24, bottom: 24 },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: hours },
    yAxis: { type: 'value', name: '%', min: 0, max: 100 },
    series: [{ name: '含水率', type: 'line', smooth: true, data, areaStyle: { opacity: 0.2 } }]
  });
}

function renderEt0Chart(dates: string[], data: number[]) {
  const hours = toHourLabels(dates);
  setEt0Options({
    color: ['#73C0DE'],
    grid: { left: 40, right: 20, top: 24, bottom: 24 },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: hours },
    yAxis: { type: 'value', name: 'mm/h' },
    series: [{ name: 'ET0', type: 'line', smooth: true, data, areaStyle: { opacity: 0.15 } }]
  });
}

function renderPenmanInputsChart(inputs: any) {
  const hours = toHourLabels(inputs.dates || []);
  setPenmanInputsOptions({
    grid: { left: 50, right: 30, top: 40, bottom: 40 },
    tooltip: { trigger: 'axis' },
    legend: { data: ['温度(°C)','湿度(%)','风速(m/s)','太阳辐射(MJ/m²)','降水(mm)'] },
    xAxis: { type: 'category', data: hours },
    yAxis: { type: 'value' },
    series: [
      { name: '温度(°C)', type: 'line', smooth: true, data: inputs.temp.map(v => Number(v) || 0), areaStyle: { opacity: 0.08 } },
      { name: '湿度(%)', type: 'line', smooth: true, data: inputs.humidity.map(v => Number(v) || 0), areaStyle: { opacity: 0.08 } },
      { name: '风速(m/s)', type: 'line', smooth: true, data: inputs.wind.map(v => Number(v) || 0), areaStyle: { opacity: 0.08 } },
      { name: '太阳辐射(MJ/m²)', type: 'line', smooth: true, data: inputs.solar.map(v => Number(v) || 0), areaStyle: { opacity: 0.08 } },
      { name: '降水(mm)', type: 'bar', data: inputs.precip.map(v => Number(v) || 0), yAxisIndex: 0, itemStyle: { opacity: 0.6 } }
    ]
  });
}

function renderRainChart(dates: string[], precip: number[]) {
  const hours = toHourLabels(dates);
  setRainOptions({
    color: ['#54D8C7'],
    grid: { left: 40, right: 20, top: 24, bottom: 24 },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: hours },
    yAxis: { type: 'value', name: 'mm' },
    series: [{ name: '降雨量', type: 'bar', data: precip, itemStyle: { opacity: 0.7 } }]
  });
}

function renderTempChart(dates: string[], temp: number[]) {
  const hours = toHourLabels(dates);
  setTempOptions({
    color: ['#F6BD16'],
    grid: { left: 40, right: 20, top: 24, bottom: 24 },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: hours },
    yAxis: { type: 'value', name: '°C' },
    series: [{ name: '平均温度', type: 'line', smooth: true, data: temp, areaStyle: { opacity: 0.2 } }]
  });
}

// 水分状态颜色
const moistureStatus = computed(() => {
  if (soilMoisturePercent.value < 40) return 'exception';
  if (soilMoisturePercent.value < 60) return 'normal';
  return 'success';
});

// 额外状态与计算
const lastUpdatedText = ref<string>('-');
const moistureStateLabel = computed(() => {
  if (soilMoisturePercent.value < 40) return '偏低';
  if (soilMoisturePercent.value < 60) return '适中';
  return '偏高';
});
const moistureStateColor = computed(() => {
  if (soilMoisturePercent.value < 40) return 'red';
  if (soilMoisturePercent.value < 60) return 'blue';
  return 'green';
});

const progressText = (p: number) => `${Math.round((p || 0) * 10) / 10}%`;

function mmToM3PerMu(mm: number): number {
  return Math.round(mm * 0.6667 * 10) / 10;
}

const et0Forecast = ref<number[]>([]);
const chartDates = ref<string[]>([]);
const moistureTrendData = ref<number[]>([]);
const penmanInputs = ref<any>({ dates: [], temp: [], humidity: [], wind: [], solar: [], precip: [] });
const et0Last1h = computed<number>(() => {
  const a = et0Forecast.value;
  return a.length ? a[a.length - 1] : 0;
});
const et0Last3hAvg = computed<number>(() => {
  const a = et0Forecast.value.slice(-3);
  if (!a.length) return 0;
  return Math.round((a.reduce((s, v) => s + (v || 0), 0) / a.length) * 10) / 10;
});
const et0Sum24h = computed<number>(() => {
  const a = et0Forecast.value.slice(-24);
  if (!a.length) return 0;
  return Math.round((a.reduce((s, v) => s + (v || 0), 0)) * 10) / 10;
});

const riskLevel = computed(() => {
  if (soilMoisturePercent.value < 35) return '较高';
  if (soilMoisturePercent.value < 45) return '中等';
  return '较低';
});
const riskTips = computed<string[]>(() => {
  if (soilMoisturePercent.value < 40) {
    return [
      '建议在清晨低蒸发时段进行滴灌，避免地表径流',
      '分次灌溉更利于均匀入渗，减少浪费',
    ];
  }
  if (soilMoisturePercent.value < 60) {
    return [
      '关注未来两日蒸散量变化，必要时小水勤灌',
      '保持田间通风，避免积水抑制根系生长',
    ];
  }
  return [
    '土壤含水率较高，暂不推荐灌溉',
    '注意病害风险，保持叶面干燥并加强田间巡查',
  ];
});

async function copySuggestion() {
  const text = `地块：${selectedPlotName.value}\n需要灌溉：${penmanSuggestion.needIrrigation ? '是' : '否'}\n时间：${penmanSuggestion.recommendedTime || '-'}\n方式：${penmanSuggestion.method || '-'}\n推荐灌水量：${recommendedVolumeMm.value} mm（约 ${mmToM3PerMu(recommendedVolumeMm.value)} m³/亩）\n原因：${penmanSuggestion.reason || '-'}`;
  try {
    await navigator.clipboard?.writeText(text);
    createMessage.success('已复制建议到剪贴板');
  } catch (e) {
    createMessage.warning('复制失败，请手动选择文本进行复制');
  }
}

async function refresh() {
  await fetchPlotStatusAndSuggest();
  await fetchInterventionComparison();
  createMessage.success('数据已刷新');
}

// 快速灌溉管理（状态与计算）
const quickArea = ref<number>(0);
const quickMethod = ref<string>('滴灌');
const quickUseAlgorithm = ref<boolean>(true);
const manualWaterPerMu = ref<number>(0);
const quickDate = ref<any>();
const quickRemark = ref<string>('');
const quickTimes = ref<number>(1);

const irrigationMethodOptions = [
  { label: '滴灌', value: '滴灌' },
  { label: '喷灌', value: '喷灌' },
  { label: '漫灌', value: '漫灌' },
  { label: '微灌', value: '微灌' },
];

const algoWaterPerMuM3 = computed<number>(() => mmToM3PerMu(recommendedVolumeMm.value));
const waterPerMuM3 = computed<number>(() => (quickUseAlgorithm.value ? algoWaterPerMuM3.value : manualWaterPerMu.value));
const totalWaterUsageM3 = computed<number>(() => Math.round(waterPerMuM3.value * quickArea.value * 10) / 10);
const flowRateM3PerHour = ref<number>(0);
const durationHours = computed<number>(() => {
  const rate = flowRateM3PerHour.value;
  if (!rate || !totalWaterUsageM3.value) return 0;
  return Math.round((totalWaterUsageM3.value / rate) * 10) / 10;
});
const perTimeWaterM3 = computed<number>(() => {
  const times = Math.max(1, quickTimes.value || 1);
  if (!totalWaterUsageM3.value) return 0;
  return Math.round((totalWaterUsageM3.value / times) * 10) / 10;
});

const reportModalVisible = ref<boolean>(false);
const reportType = ref<string>('irrigation');

function openReportModal(type?: string) {
  reportType.value = type || 'irrigation';
  reportModalVisible.value = true;
}

function downloadReportAsWord() {
  const elId = reportType.value === 'risk' ? 'riskReport' : 'irrigationReport';
  const el = document.getElementById(elId);
  if (!el) {
    createMessage.warning('报告内容为空，无法下载');
    return;
  }
  const html = el.outerHTML;
  const doc = '<!DOCTYPE html><html><head><meta charset="utf-8"><title>智慧灌溉报告</title></head><body>' + html + '</body></html>';
  const blob = new Blob(['\ufeff', doc], { type: 'application/msword;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = '智慧灌溉报告.doc';
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
  createMessage.success('报告已下载');
}

function calcQuickIrrigation() {
  if (!selectedBaseId.value || !selectedPlotId.value) {
    createMessage.warning('请先选择基地和地块');
    return;
  }
  if (!quickArea.value || quickArea.value <= 0) {
    createMessage.warning('请输入有效的灌溉面积');
    return;
  }
  createMessage.success('已计算用水量');
}

 

function executeAutomationQuick() {
  if (!selectedPlotId.value) {
    createMessage.warning('请先选择地块');
    return;
  }
  createMessage.info('已触发自动化（占位）');
}

function exportReport() {
  printJS({ type: 'html', printable: 'irrigationReport' });
}
</script>

<style lang="less" scoped>
.irrigation-page {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 64px);
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

.moisture-card {
  display: flex;
  align-items: center;
  gap: 16px;
}

.suggestion-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.rich-card {
  :deep(.ant-card-body) {
    padding-top: 16px;
  }
}

.mini-title {
  margin: 8px 0 4px;
  font-size: 13px;
  color: #555;
}

.statistic-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-top: 8px;
}
.metric-title {
  font-size: 12px;
  color: #888;
}
.metric-value {
  font-size: 16px;
  font-weight: 600;
}

.actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.tips {
  margin: 0;
  padding-left: 18px;
  color: #666;
}

.mt-4 {
  margin-top: 16px;
}

.top-row {
  align-items: stretch;
  :deep(.ant-col) {
    display: flex;
  }
  :deep(.ant-card) {
    height: 100%;
    display: flex;
    flex-direction: column;
    width: 100%;
    flex: 1 1 auto;
  }
}

.row-stretch {
  align-items: stretch;
  :deep(.ant-col) {
    display: flex;
  }
  :deep(.ant-card) {
    height: 100%;
    display: flex;
    flex-direction: column;
    width: 100%;
    flex: 1 1 auto;
  }
}

/* 快速灌溉管理样式 */
.quick-card {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.form-item {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 6px;
}
.form-inline {
  flex-direction: row;
  align-items: center;
}
.form-item .label {
  width: auto;
  font-size: 13px;
  color: #555;
  font-weight: 500;
}

.calc-result {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 8px 12px;
  font-size: 13px;
  color: #555;
  background: #fcfcfc;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
}

.quick-actions {
  display: flex;
  justify-content: center;
  margin-top: 8px;
}

.chart-ref { width: 100%; }
.chart-220 { height: 220px; }
.chart-320 { height: 320px; }

/* 自定义下拉框样式（与目标下拉框一致） */
.custom-select {
  position: relative;
  height: 36px;
  line-height: 36px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 0 12px;
  box-sizing: border-box;
  background: #fff;
  z-index: 10;
}
.custom-select.disabled {
  cursor: not-allowed;
  background-color: #f5f7fa;
  color: #c0c4cc;
}
.select-value {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.select-icon {
  position: absolute;
  right: 12px;
  top: 0;
  width: 16px;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s;
}
.select-icon.open {
  transform: rotate(180deg);
}
.select-options {
  position: absolute;
  top: 40px;
  left: 0;
  right: 0;
  max-height: 200px;
  overflow-y: auto;
  background: #fff;
  border: 1px solid #e6e6e6;
  border-radius: 4px;
  z-index: 999;
}
.options-scroll {
  padding: 4px 0;
}
.option-item {
  padding: 0 12px;
  height: 32px;
  line-height: 32px;
  cursor: pointer;
}
.option-item:hover {
  background-color: #f5f7fa;
}

@media (max-width: 768px) {
  .irrigation-page {
    padding: 16px;
  }
  .statistic-grid {
    grid-template-columns: 1fr;
  }
  .chart-220 { height: 180px; }
  .chart-320 { height: 260px; }
}

@media (min-width: 1600px) {
  .irrigation-page { padding: 32px; }
  .chart-220 { height: 260px; }
  .chart-320 { height: 360px; }
  .quick-card { gap: 16px; }
}
</style>
