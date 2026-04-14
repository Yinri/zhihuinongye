<template>
  <div class="fertilization-page">
    <!-- 物联网设备提示 -->
    <div v-if="!hasIoTDevice" class="iot-warning">
      <a-alert type="warning" :closable="false">
        <template #message>
          <span>{{ deviceMessage || '该基地未配置物联网设备，无法获取实时数据' }}</span>
        </template>
      </a-alert>
    </div>

    <!-- 土壤养分概览 + 智能施肥建议 -->
    <a-row :gutter="16" class="top-row">
      <a-col :xs="24" :md="10">
        <a-card :bordered="false" class="rich-card">
          <template #title>
            <div class="table-title">
              <Icon icon="ant-design:dot-chart-outlined" /> 土壤养分概览
            </div>
          </template>
          <div v-if="sensorDataReady" class="overview-content">
            <div class="ec-display">
              <div class="ec-title">土壤电导率 (mS/cm)</div>
              <div class="ec-grid">
                <div class="ec-item">
                  <div class="ec-label">10cm</div>
                  <div class="ec-value">{{ sensorData.ec1 || '-' }}</div>
                </div>
                <div class="ec-item">
                  <div class="ec-label">30cm</div>
                  <div class="ec-value">{{ sensorData.ec2 || '-' }}</div>
                </div>
                <div class="ec-item">
                  <div class="ec-label">60cm</div>
                  <div class="ec-value">{{ sensorData.ec3 || '-' }}</div>
                </div>
              </div>
            </div>
            <a-divider style="margin: 12px 0" />
            <div class="nutrient-grid">
              <div class="nutrient-item">
                <div class="nutrient-label">估算氮(N)</div>
                <div class="nutrient-value">{{ sensorData.estimatedN || '-' }} <span class="unit">mg/kg</span></div>
              </div>
              <div class="nutrient-item">
                <div class="nutrient-label">估算磷(P)</div>
                <div class="nutrient-value">{{ sensorData.estimatedP || '-' }} <span class="unit">mg/kg</span></div>
              </div>
              <div class="nutrient-item">
                <div class="nutrient-label">估算钾(K)</div>
                <div class="nutrient-value">{{ sensorData.estimatedK || '-' }} <span class="unit">mg/kg</span></div>
              </div>
            </div>
            <a-divider style="margin: 12px 0" />
            <div class="percent-grid">
              <div class="percent-item">
                <a-progress type="circle" :percent="sensorData.nPercent || 0" :width="60" :stroke-color="'#52c41a'" />
                <div class="percent-label">N</div>
              </div>
              <div class="percent-item">
                <a-progress type="circle" :percent="sensorData.pPercent || 0" :width="60" :stroke-color="'#1890ff'" />
                <div class="percent-label">P</div>
              </div>
              <div class="percent-item">
                <a-progress type="circle" :percent="sensorData.kPercent || 0" :width="60" :stroke-color="'#faad14'" />
                <div class="percent-label">K</div>
              </div>
            </div>
          </div>
          <a-empty v-else :description="deviceMessage || '暂无数据'" />
        </a-card>
      </a-col>

      <a-col :xs="24" :md="14">
        <a-card :bordered="false" class="rich-card">
          <template #title>
            <div class="table-title">
              <Icon icon="ant-design:solution-outlined" /> 智能施肥建议
            </div>
          </template>
          <div v-if="recommendDataReady">
            <div class="recommend-header">
              <Tag :color="recommend.needFertilization ? 'red' : 'green'" style="font-size: 14px; padding: 4px 12px">
                {{ recommend.needFertilization ? '需要施肥' : '暂不需要' }}
              </Tag>
              <span class="data-source">数据来源：物联网实时传感器</span>
            </div>
            <a-descriptions bordered size="small" :column="2" class="recommend-desc">
              <a-descriptions-item label="推荐时间">{{ recommend.recommendedTime || '-' }}</a-descriptions-item>
              <a-descriptions-item label="推荐方式">{{ recommend.method || '-' }}</a-descriptions-item>
              <a-descriptions-item label="氮(N)推荐">
                <span class="recommend-value">{{ recommend.recommendN || 0 }} <span class="unit">kg/亩</span></span>
              </a-descriptions-item>
              <a-descriptions-item label="磷(P₂O₅)推荐">
                <span class="recommend-value">{{ recommend.recommendP2O5 || 0 }} <span class="unit">kg/亩</span></span>
              </a-descriptions-item>
              <a-descriptions-item label="钾(K₂O)推荐" :span="2">
                <span class="recommend-value">{{ recommend.recommendK2O || 0 }} <span class="unit">kg/亩</span></span>
              </a-descriptions-item>
              <a-descriptions-item label="分析建议" :span="2">
                <div class="reason-text">{{ recommend.reason || '暂无' }}</div>
              </a-descriptions-item>
            </a-descriptions>
            <div class="actions">
              <a-space :size="12">
                <a-button size="small" @click="copySuggestion" :disabled="!sensorDataReady">
                  <Icon icon="ant-design:copy-outlined" /> 复制建议
                </a-button>
                <a-button size="small" @click="refreshData" :disabled="!selectedBaseId">
                  <Icon icon="ant-design:reload-outlined" /> 刷新数据
                </a-button>
                <a-button size="small" @click="openReportModal" :disabled="!recommendDataReady">
                  <Icon icon="ant-design:file-word-outlined" /> 生成报告
                </a-button>
              </a-space>
            </div>
          </div>
          <a-empty v-else :description="deviceMessage || '暂无推荐数据'" />
        </a-card>
      </a-col>
    </a-row>

    <!-- 土壤分层数据 -->
    <a-row :gutter="16" class="mt-4" v-if="sensorDataReady">
      <a-col :xs="24">
        <a-card :bordered="false" class="rich-card">
          <template #title>
            <div class="table-title">
              <Icon icon="ant-design:database-outlined" /> 土壤分层数据
            </div>
          </template>
          <div class="soil-layers">
            <div class="soil-layer layer-top">
              <div class="layer-label">上层 (10cm)</div>
              <div class="layer-data">
                <div class="layer-item">
                  <span class="layer-item-label">土壤温度</span>
                  <span class="layer-item-value">{{ sensorData.soilTemp1 || '-' }} <span class="unit">℃</span></span>
                </div>
                <div class="layer-item">
                  <span class="layer-item-label">土壤湿度</span>
                  <span class="layer-item-value">{{ sensorData.soilMoist1 || '-' }} <span class="unit">%</span></span>
                </div>
                <div class="layer-item">
                  <span class="layer-item-label">土壤电导率</span>
                  <span class="layer-item-value">{{ sensorData.ec1 || '-' }} <span class="unit">mS/cm</span></span>
                </div>
              </div>
            </div>
            <div class="soil-layer layer-middle">
              <div class="layer-label">中层 (30cm)</div>
              <div class="layer-data">
                <div class="layer-item">
                  <span class="layer-item-label">土壤温度</span>
                  <span class="layer-item-value">{{ sensorData.soilTemp2 || '-' }} <span class="unit">℃</span></span>
                </div>
                <div class="layer-item">
                  <span class="layer-item-label">土壤湿度</span>
                  <span class="layer-item-value">{{ sensorData.soilMoist2 || '-' }} <span class="unit">%</span></span>
                </div>
                <div class="layer-item">
                  <span class="layer-item-label">土壤电导率</span>
                  <span class="layer-item-value">{{ sensorData.ec2 || '-' }} <span class="unit">mS/cm</span></span>
                </div>
              </div>
            </div>
            <div class="soil-layer layer-bottom">
              <div class="layer-label">深层 (60cm)</div>
              <div class="layer-data">
                <div class="layer-item">
                  <span class="layer-item-label">土壤温度</span>
                  <span class="layer-item-value">{{ sensorData.soilTemp3 || '-' }} <span class="unit">℃</span></span>
                </div>
                <div class="layer-item">
                  <span class="layer-item-label">土壤湿度</span>
                  <span class="layer-item-value">{{ sensorData.soilMoist3 || '-' }} <span class="unit">%</span></span>
                </div>
                <div class="layer-item">
                  <span class="layer-item-label">土壤电导率</span>
                  <span class="layer-item-value">{{ sensorData.ec3 || '-' }} <span class="unit">mS/cm</span></span>
                </div>
              </div>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 推荐施肥量柱状图 -->
    <a-row :gutter="16" class="mt-4" v-if="recommendDataReady">
      <a-col :xs="24" :md="24">
        <a-card :bordered="false" class="rich-card">
          <template #title>
            <div class="table-title">
              <Icon icon="ant-design:bar-chart-outlined" /> 推荐施肥量（kg/亩）
            </div>
          </template>
          <div ref="barChartRef" style="width: 100%; height: 280px" />
        </a-card>
      </a-col>
    </a-row>

    <!-- 推荐化肥方案 -->
    <a-row :gutter="16" class="mt-4" v-if="recommendedFertilizers.length > 0">
      <a-col :xs="24" :md="24">
        <a-card :bordered="false" class="rich-card">
          <template #title>
            <div class="table-title">
              <Icon icon="ant-design:experiment-outlined" /> 推荐化肥方案
            </div>
          </template>
          <div class="fertilizer-scheme">
            <a-row :gutter="16">
              <a-col :xs="24" :sm="12" :md="8" v-for="(fert, index) in recommendedFertilizers" :key="index">
                <div class="fertilizer-scheme-card">
                  <div class="scheme-header">
                    <Tag :color="getFertilizerTypeColor(fert.fertilizerType)">{{ fert.fertilizerType }}</Tag>
                    <span class="scheme-npk">NPK: {{ fert.npkRatio || '未设置' }}</span>
                  </div>
                  <div class="scheme-name">{{ fert.fertilizerName }}</div>
                  <div class="scheme-amount">
                    <span class="amount-value">{{ fert.suggestedAmount }}</span>
                    <span class="amount-unit">kg/亩</span>
                  </div>
                  <div class="scheme-detail">
                    <div class="detail-item">
                      <span class="detail-label">剂型</span>
                      <span class="detail-value">{{ fert.formulation || '-' }}</span>
                    </div>
                    <div class="detail-item">
                      <span class="detail-label">推荐用法</span>
                      <span class="detail-value">{{ fert.applicationMethod || '-' }}</span>
                    </div>
                  </div>
                </div>
              </a-col>
            </a-row>
            <div class="scheme-tip">
              <Icon icon="ant-design:info-circle-outlined" />
              <span>以上肥料根据土壤检测结果和NPK需求比例智能匹配，仅供参考。具体施用量请根据实际情况调整。</span>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 报告弹窗 -->
    <a-modal v-model:open="reportModalVisible" title="智能施肥推荐报告" :width="900" class="fertilizer-report-modal">
      <div class="report-container" v-if="recommendDataReady" id="fertilizationReport">
        <div class="report-header">
          <div class="report-icon"><Icon icon="ant-design:experiment-outlined" /></div>
          <h2 class="report-title">智能施肥推荐报告</h2>
          <div class="report-meta">
            <div class="meta-item">
              <span class="meta-label">基地名称</span>
              <span class="meta-value">{{ baseName }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">数据来源</span>
              <span class="meta-value">物联网实时传感器</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">生成时间</span>
              <span class="meta-value">{{ currentTime }}</span>
            </div>
          </div>
        </div>

        <a-divider class="report-divider" />

        <div class="report-section">
          <div class="section-header">
            <Icon icon="ant-design:database-outlined" />
            <h4>一、土壤养分状况</h4>
          </div>
          <a-row :gutter="16" class="section-content">
            <a-col :span="24">
              <div class="overview-summary">
                <div class="summary-item">
                  <div class="summary-icon">
                    <Icon icon="ant-design:field-number-outlined" />
                  </div>
                  <div class="summary-info">
                    <div class="summary-label">土壤电导率</div>
                    <div class="summary-values">
                      <span class="summary-val">10cm: <b>{{ sensorData.ec1 || '-' }}</b></span>
                      <span class="summary-val">30cm: <b>{{ sensorData.ec2 || '-' }}</b></span>
                      <span class="summary-val">60cm: <b>{{ sensorData.ec3 || '-' }}</b></span>
                      <span class="summary-unit">mS/cm</span>
                    </div>
                  </div>
                </div>
              </div>
            </a-col>
          </a-row>
          
          <a-row :gutter="16" class="section-content" style="margin-top: 16px;">
            <a-col :span="14">
              <div class="nutrient-card full">
                <div class="nutrient-card-title">估算养分含量 (mg/kg)</div>
                <div class="nutrient-values large">
                  <div class="nutrient-item n">
                    <span class="nutrient-icon large">N</span>
                    <span class="nutrient-value">{{ sensorData.estimatedN || '-' }}</span>
                    <span class="nutrient-label">氮</span>
                  </div>
                  <div class="nutrient-item p">
                    <span class="nutrient-icon large">P</span>
                    <span class="nutrient-value">{{ sensorData.estimatedP || '-' }}</span>
                    <span class="nutrient-label">磷</span>
                  </div>
                  <div class="nutrient-item k">
                    <span class="nutrient-icon large">K</span>
                    <span class="nutrient-value">{{ sensorData.estimatedK || '-' }}</span>
                    <span class="nutrient-label">钾</span>
                  </div>
                </div>
              </div>
            </a-col>
            <a-col :span="10">
              <div class="percent-card">
                <div class="percent-card-title">养分充足率</div>
                <div class="percent-grid">
                  <div class="percent-item">
                    <a-progress type="circle" :percent="sensorData.nPercent || 0" :width="50" :stroke-color="'#52c41a'" />
                    <div class="percent-label">N</div>
                  </div>
                  <div class="percent-item">
                    <a-progress type="circle" :percent="sensorData.pPercent || 0" :width="50" :stroke-color="'#1890ff'" />
                    <div class="percent-label">P</div>
                  </div>
                  <div class="percent-item">
                    <a-progress type="circle" :percent="sensorData.kPercent || 0" :width="50" :stroke-color="'#faad14'" />
                    <div class="percent-label">K</div>
                  </div>
                </div>
              </div>
            </a-col>
          </a-row>
        </div>

        <a-divider class="report-divider" />

        <div class="report-section">
          <div class="section-header">
            <Icon icon="ant-design:experiment-outlined" />
            <h4>二、施肥推荐</h4>
          </div>
          <div class="section-content">
            <div class="recommend-header">
              <Tag :color="recommend.needFertilization ? 'error' : 'success'" class="need-fertilizer-tag large">
                <Icon :icon="recommend.needFertilization ? 'ant-design:warning-outlined' : 'ant-design:check-circle-outlined'" />
                {{ recommend.needFertilization ? '需要施肥' : '暂不需要' }}
              </Tag>
              <span class="recommend-subtitle" v-if="recommend.needFertilization">根据土壤检测结果，建议及时补充养分</span>
            </div>
            
            <a-row :gutter="16" class="recommend-info-row">
              <a-col :span="8">
                <div class="info-card">
                  <div class="info-card-icon"><Icon icon="ant-design:calendar-outlined" /></div>
                  <div class="info-card-content">
                    <div class="info-card-label">推荐施肥时间</div>
                    <div class="info-card-value">{{ recommend.recommendedTime || '-' }}</div>
                  </div>
                </div>
              </a-col>
              <a-col :span="8">
                <div class="info-card">
                  <div class="info-card-icon"><Icon icon="ant-design:tool-outlined" /></div>
                  <div class="info-card-content">
                    <div class="info-card-label">推荐施肥方式</div>
                    <div class="info-card-value">{{ recommend.method || '-' }}</div>
                  </div>
                </div>
              </a-col>
              <a-col :span="8">
                <div class="info-card">
                  <div class="info-card-icon"><Icon icon="ant-design:car-outlined" /></div>
                  <div class="info-card-content">
                    <div class="info-card-label">推荐肥料类型</div>
                    <div class="info-card-value">复合肥/NPK</div>
                  </div>
                </div>
              </a-col>
            </a-row>

            <div class="fertilizer-recommend">
              <div class="fertilizer-recommend-title">推荐施肥量</div>
              <a-row :gutter="12" class="fertilizer-grid">
                <a-col :span="8">
                  <div class="fertilizer-card n-card">
                    <div class="fertilizer-icon">N</div>
                    <div class="fertilizer-value">{{ recommend.recommendN || 0 }}</div>
                    <div class="fertilizer-unit">kg/亩</div>
                    <div class="fertilizer-label">氮肥</div>
                    <div class="fertilizer-desc">促进植株生长</div>
                  </div>
                </a-col>
                <a-col :span="8">
                  <div class="fertilizer-card p-card">
                    <div class="fertilizer-icon">P</div>
                    <div class="fertilizer-value">{{ recommend.recommendP2O5 || 0 }}</div>
                    <div class="fertilizer-unit">kg/亩</div>
                    <div class="fertilizer-label">磷肥</div>
                    <div class="fertilizer-desc">促进根系发育</div>
                  </div>
                </a-col>
                <a-col :span="8">
                  <div class="fertilizer-card k-card">
                    <div class="fertilizer-icon">K</div>
                    <div class="fertilizer-value">{{ recommend.recommendK2O || 0 }}</div>
                    <div class="fertilizer-unit">kg/亩</div>
                    <div class="fertilizer-label">钾肥</div>
                    <div class="fertilizer-desc">增强抗病抗逆</div>
                  </div>
                </a-col>
              </a-row>
            </div>

            <div class="reason-box" v-if="recommend.reason">
              <div class="reason-header">
                <Icon icon="ant-design:bulb-outlined" />
                <span>分析建议</span>
              </div>
              <p class="reason-content">{{ recommend.reason }}</p>
            </div>

            <!-- 推荐化肥方案 -->
            <div class="fertilizer-scheme-report" v-if="recommendedFertilizers.length > 0">
              <div class="scheme-report-title">推荐化肥方案</div>
              <a-row :gutter="12" class="scheme-report-grid">
                <a-col :span="8" v-for="(fert, index) in recommendedFertilizers" :key="index">
                  <div class="scheme-report-card">
                    <div class="scheme-report-header">
                      <Tag :color="getFertilizerTypeColor(fert.fertilizerType)">{{ fert.fertilizerType }}</Tag>
                      <span class="scheme-report-npk">NPK: {{ fert.npkRatio || '未设置' }}</span>
                    </div>
                    <div class="scheme-report-name">{{ fert.fertilizerName }}</div>
                    <div class="scheme-report-amount">
                      <span class="scheme-amount-value">{{ fert.suggestedAmount }}</span>
                      <span class="scheme-amount-unit">kg/亩</span>
                    </div>
                    <div class="scheme-report-detail">
                      <div class="report-detail-item">
                        <span class="report-detail-label">剂型</span>
                        <span class="report-detail-value">{{ fert.formulation || '-' }}</span>
                      </div>
                      <div class="report-detail-item">
                        <span class="report-detail-label">推荐用法</span>
                        <span class="report-detail-value">{{ fert.applicationMethod || '-' }}</span>
                      </div>
                    </div>
                  </div>
                </a-col>
              </a-row>
            </div>
          </div>
        </div>

        <a-divider class="report-divider" />

        <div class="report-section">
          <div class="section-header">
            <Icon icon="ant-design:info-circle-outlined" />
            <h4>三、施肥注意事项</h4>
          </div>
          <div class="section-content">
            <div class="tips-grid">
              <div class="tip-item">
                <div class="tip-icon"><Icon icon="ant-design:clock-circle-outlined" /></div>
                <div class="tip-content">
                  <div class="tip-title">施肥时机</div>
                  <div class="tip-text">建议在阴天或傍晚进行，避免高温时段施肥造成养分流失</div>
                </div>
              </div>
              <div class="tip-item">
                <div class="tip-icon"><Icon icon="ant-design:water-outlined" /></div>
                <div class="tip-content">
                  <div class="tip-title">水肥配合</div>
                  <div class="tip-text">施肥后适量浇水，促进肥料溶解和吸收</div>
                </div>
              </div>
              <div class="tip-item">
                <div class="tip-icon"><Icon icon="ant-design:safety-outlined" /></div>
                <div class="tip-content">
                  <div class="tip-title">安全防护</div>
                  <div class="tip-text">操作时佩戴防护用品，避免直接接触皮肤</div>
                </div>
              </div>
              <div class="tip-item">
                <div class="tip-icon"><Icon icon="ant-design:bank-outlined" /></div>
                <div class="tip-content">
                  <div class="tip-title">储存注意</div>
                  <div class="tip-text">肥料应存放于干燥通风处，避免受潮结块</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <a-space>
          <a-button type="primary" @click="downloadReport" :disabled="!recommendDataReady">
            <Icon icon="ant-design:download-outlined" /> 下载报告
          </a-button>
          <a-button @click="reportModalVisible = false">
            <Icon icon="ant-design:close-outlined" /> 关闭
          </a-button>
        </a-space>
      </template>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, watch, onMounted, nextTick, computed } from 'vue';
import { Icon } from '/@/components/Icon';
import { Tag } from 'ant-design-vue';
import { useSelectStore } from '/@/store/selectStore';
import { getPlotStatusByBase, getBaseRecommendation, getBaseSoilSeries, getFertilizerList } from './fertilization.api';
import { useECharts } from '/@/hooks/web/useECharts';
import { useMessage } from '/@/hooks/web/useMessage';

const selectStore = useSelectStore();
const selectedBaseId = ref<string | number | undefined>(undefined);
const lastRequestBaseId = ref<string | number | undefined>(undefined);
const baseName = computed(() => selectStore.selectedBase.baseName || '');
const { createMessage } = useMessage();

const deviceMessage = ref('');
const sensorDataReady = ref(false);
const recommendDataReady = ref(false);
const hasIoTDevice = ref(true);
const currentTime = ref('');

const fertilizerList = ref<any[]>([]);
const recommendedFertilizers = ref<any[]>([]);

const sensorData = reactive({
  ec1: 0,
  ec2: 0,
  ec3: 0,
  soilTemp1: 0,
  soilTemp2: 0,
  soilTemp3: 0,
  soilMoist1: 0,
  soilMoist2: 0,
  soilMoist3: 0,
  estimatedN: 0,
  estimatedP: 0,
  estimatedK: 0,
  nPercent: 0,
  pPercent: 0,
  kPercent: 0
});

const recommend = reactive({
  needFertilization: false,
  recommendedTime: '',
  method: '',
  recommendN: 0,
  recommendP2O5: 0,
  recommendK2O: 0,
  reason: ''
});

const barChartRef = ref<HTMLDivElement | null>(null);
const { setOptions: setBarOptions } = useECharts(barChartRef as any);
const reportModalVisible = ref(false);

function updateTime() {
  const now = new Date();
  currentTime.value = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')} ${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}:${String(now.getSeconds()).padStart(2, '0')}`;
}

function onBaseChange() {
  const { baseId } = selectStore.selectedBase as any;
  selectedBaseId.value = baseId;
}

async function fetchData() {
  updateTime();
  if (!selectedBaseId.value) {
    resetData();
    return;
  }

  const reqId = selectedBaseId.value;
  lastRequestBaseId.value = reqId;

  const results = await Promise.allSettled([
    getPlotStatusByBase(reqId as any),
    getBaseRecommendation(reqId as any)
  ]);

  if (lastRequestBaseId.value !== reqId) return;

  const statusRes = results[0].status === 'fulfilled' ? (results[0] as any).value : {};
  const recRes = results[1].status === 'fulfilled' ? (results[1] as any).value : {};

  hasIoTDevice.value = !(statusRes.deviceNotConfigured || recRes.deviceNotConfigured);
  deviceMessage.value = statusRes.deviceMessage || recRes.deviceMessage || '';

  if (hasIoTDevice.value) {
    sensorDataReady.value = Boolean(statusRes.hasData);
    recommendDataReady.value = Boolean(recRes.hasData);

    if (statusRes.hasData) {
      sensorData.ec1 = Number(statusRes.ec1) || 0;
      sensorData.ec2 = Number(statusRes.ec2) || 0;
      sensorData.ec3 = Number(statusRes.ec3) || 0;
      sensorData.soilTemp1 = Number(statusRes.soilTemp1) || 0;
      sensorData.soilTemp2 = Number(statusRes.soilTemp2) || 0;
      sensorData.soilTemp3 = Number(statusRes.soilTemp3) || 0;
      sensorData.soilMoist1 = Number(statusRes.soilMoist1) || 0;
      sensorData.soilMoist2 = Number(statusRes.soilMoist2) || 0;
      sensorData.soilMoist3 = Number(statusRes.soilMoist3) || 0;
      sensorData.estimatedN = Number(statusRes.estimatedN) || 0;
      sensorData.estimatedP = Number(statusRes.estimatedP) || 0;
      sensorData.estimatedK = Number(statusRes.estimatedK) || 0;
      sensorData.nPercent = Number(statusRes.nPercent) || 0;
      sensorData.pPercent = Number(statusRes.pPercent) || 0;
      sensorData.kPercent = Number(statusRes.kPercent) || 0;
    }

    if (recRes.hasData) {
      recommend.needFertilization = recRes.needFertilization || false;
      recommend.recommendedTime = recRes.recommendedTime || '';
      recommend.method = recRes.method || '';
      recommend.recommendN = Number(recRes.recommendN) || 0;
      recommend.recommendP2O5 = Number(recRes.recommendP2O5) || 0;
      recommend.recommendK2O = Number(recRes.recommendK2O) || 0;
      recommend.reason = recRes.reason || '';
    }
  } else {
    sensorDataReady.value = false;
    recommendDataReady.value = false;
  }

  await nextTick();
  if (recommendDataReady.value) {
    renderBarChart();
    calculateRecommendedFertilizers();
  }
}

// 根据推荐施肥量计算推荐的肥料组合
function calculateRecommendedFertilizers() {
  if (!fertilizerList.value || fertilizerList.value.length === 0) {
    recommendedFertilizers.value = [];
    return;
  }

  const needN = recommend.recommendN || 0;
  const needP = recommend.recommendP2O5 || 0;
  const needK = recommend.recommendK2O || 0;
  const total = needN + needP + needK;

  if (total <= 0) {
    recommendedFertilizers.value = [];
    return;
  }

  // 计算各元素需要的比例
  const ratioN = needN / total;
  const ratioP = needP / total;
  const ratioK = needK / total;

  // 筛选复合肥（包含多种养分）
  const compoundFertilizers = fertilizerList.value.filter((f: any) => {
    const type = f.fertilizerType || '';
    return type.includes('复合肥') || type.includes('复混肥');
  });

  // 计算每种肥料与需求的比例匹配度
  const scoredFertilizers = fertilizerList.value.map((f: any) => {
    const npkStr = f.npkRatio || '';
    const match = npkStr.match(/(\d+)-(\d+)-(\d+)/);
    if (!match) return { fertilizer: f, score: 0, ratioDiff: Infinity };

    const fn = parseFloat(match[1]) || 0;
    const fp = parseFloat(match[2]) || 0;
    const fk = parseFloat(match[3]) || 0;
    const fTotal = fn + fp + fk;

    if (fTotal === 0) return { fertilizer: f, score: 0, ratioDiff: Infinity };

    // 如果某种养分需求为0，但肥料含有该养分，则降低优先级或排除
    const hasExcessNutrient = (needN === 0 && fn > 0) || (needP === 0 && fp > 0) || (needK === 0 && fk > 0);
    if (hasExcessNutrient && total > 0) {
      return { fertilizer: f, score: -1, ratioDiff: Infinity }; // 排除不匹配的肥料
    }

    const fertRatioN = fn / fTotal;
    const fertRatioP = fp / fTotal;
    const fertRatioK = fk / fTotal;

    // 计算比例差异
    const ratioDiff = Math.abs(fertRatioN - ratioN) + Math.abs(fertRatioP - ratioP) + Math.abs(fertRatioK - ratioK);

    // 计算匹配分数（考虑元素充足性）
    let score = 0;
    if (needN > 0 && fn > 0) score += 1 - Math.abs(fn - needN) / needN;
    if (needP > 0 && fp > 0) score += 1 - Math.abs(fp - needP) / needP;
    if (needK > 0 && fk > 0) score += 1 - Math.abs(fk - needK) / needK;

    return { fertilizer: f, score: Math.max(0, score), ratioDiff };
  });

  // 过滤掉不匹配的肥料（score = -1）
  const validFertilizers = scoredFertilizers.filter(sf => sf.score >= 0);

  // 按分数排序，优先选择比例接近的
  validFertilizers.sort((a, b) => {
    if (b.score !== a.score) return b.score - a.score;
    return a.ratioDiff - b.ratioDiff;
  });

  // 选择最匹配的前3种肥料
  const selected = validFertilizers.slice(0, 3).map(sf => sf.fertilizer);

  // 计算每种肥料的建议用量
  const calculated = selected.map(f => {
    const npkStr = f.npkRatio || '';
    const match = npkStr.match(/(\d+)-(\d+)-(\d+)/);
    let fn = 0, fp = 0, fk = 0;
    if (match) {
      fn = parseFloat(match[1]) || 0;
      fp = parseFloat(match[2]) || 0;
      fk = parseFloat(match[3]) || 0;
    }

    // 计算满足N需求的用量
    let suggestedAmount = 0;
    if (fn > 0 && needN > 0) {
      suggestedAmount = Math.max(suggestedAmount, needN / fn);
    }
    if (fp > 0 && needP > 0) {
      suggestedAmount = Math.max(suggestedAmount, needP / fp);
    }
    if (fk > 0 && needK > 0) {
      suggestedAmount = Math.max(suggestedAmount, needK / fk);
    }

    return {
      ...f,
      suggestedAmount: Math.round(suggestedAmount * 100) / 100
    };
  });

  // 过滤掉用量为0的化肥
  recommendedFertilizers.value = calculated.filter(f => f.suggestedAmount > 0);
}

function getFertilizerTypeColor(type: string) {
  if (!type) return 'default';
  if (type.includes('有机')) return 'green';
  if (type.includes('复合')) return 'blue';
  if (type.includes('氮')) return 'cyan';
  if (type.includes('磷')) return 'orange';
  if (type.includes('钾')) return 'purple';
  return 'default';
}

function resetData() {
  hasIoTDevice.value = true;
  sensorDataReady.value = false;
  recommendDataReady.value = false;
  deviceMessage.value = '';
  Object.assign(sensorData, {
    ec1: 0, ec2: 0, ec3: 0,
    soilTemp1: 0, soilTemp2: 0, soilTemp3: 0,
    soilMoist1: 0, soilMoist2: 0, soilMoist3: 0,
    estimatedN: 0, estimatedP: 0, estimatedK: 0,
    nPercent: 0, pPercent: 0, kPercent: 0
  });
  Object.assign(recommend, {
    needFertilization: false,
    recommendedTime: '', method: '',
    recommendN: 0, recommendP2O5: 0, recommendK2O: 0, reason: ''
  });
}

function renderBarChart() {
  const data = [
    recommend.recommendN,
    recommend.recommendP2O5,
    recommend.recommendK2O
  ];
  setBarOptions({
    tooltip: { trigger: 'axis' },
    grid: { left: 50, right: 50, top: 20, bottom: 30 },
    xAxis: { type: 'category', data: ['氮(N)', '磷(P₂O₅)', '钾(K₂O)'] },
    yAxis: { type: 'value', name: 'kg/亩' },
    series: [{
      type: 'bar',
      data: data,
      itemStyle: {
        color: (params: any) => {
          const colors = ['#52c41a', '#1890ff', '#faad14'];
          return colors[params.dataIndex] || '#69c0ff';
        }
      },
      label: {
        show: true,
        position: 'top',
        formatter: '{c} kg/亩'
      }
    }]
  });
}

async function copySuggestion() {
  const text = `【智能施肥建议】
基地：${baseName.value}
是否需要施肥：${recommend.needFertilization ? '需要' : '暂不需要'}
推荐时间：${recommend.recommendedTime || '-'}
推荐方式：${recommend.method || '-'}
氮(N)：${recommend.recommendN} kg/亩
磷(P₂O₅)：${recommend.recommendP2O5} kg/亩
钾(K₂O)：${recommend.recommendK2O} kg/亩
分析建议：${recommend.reason || '-'}
数据来源：物联网实时传感器`;
  try {
    await navigator.clipboard?.writeText(text);
    createMessage.success('已复制到剪贴板');
  } catch (e) {
    createMessage.warning('复制失败');
  }
}

async function refreshData() {
  await fetchData();
  createMessage.success('数据已刷新');
}

function openReportModal() {
  reportModalVisible.value = true;
}

function downloadReport() {
  try {
    const el = document.getElementById('fertilizationReport');
    if (!el) return;
    const html = '<!DOCTYPE html><html><head><meta charset="utf-8"><title>智能施肥报告</title><style>body{font-family:SimHei;padding:20px;}h2{text-align:center;}ul{padding-left:20px;}</style></head><body>' + el.outerHTML + '</body></html>';
    const blob = new Blob(['\ufeff', html], { type: 'application/msword;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `智能施肥报告_${baseName.value || '未知基地'}.doc`;
    a.click();
    URL.revokeObjectURL(url);
  } catch (e) {
    createMessage.warning('下载失败');
  }
}

watch(() => selectStore.selectedBase, () => onBaseChange());
watch(selectedBaseId, () => fetchData());

onMounted(async () => {
  // 加载肥料数据
  try {
    const res = await getFertilizerList();
    fertilizerList.value = res || [];
  } catch (e) {
    console.error('加载肥料数据失败', e);
  }
  onBaseChange();
  fetchData();
});
</script>

<style scoped>
.fertilization-page {
  padding: 16px;
}
.iot-warning {
  margin-bottom: 16px;
}
.top-row {
  margin-bottom: 16px;
}
.rich-card {
  height: 100%;
  min-height: 280px;
}
.rich-card :deep(.ant-card-body) {
  height: 100%;
}
.table-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}
.overview-content {
  padding: 8px 0;
}
.ec-display {
  text-align: center;
}
.ec-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 12px;
}
.ec-grid {
  display: flex;
  justify-content: space-around;
}
.ec-item {
  text-align: center;
}
.ec-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}
.ec-value {
  font-size: 24px;
  font-weight: 600;
  color: #1890ff;
}
.nutrient-grid {
  display: flex;
  justify-content: space-around;
  padding: 8px 0;
}
.nutrient-item {
  text-align: center;
}
.nutrient-label {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}
.nutrient-value {
  font-size: 18px;
  font-weight: 600;
}
.unit {
  font-size: 12px;
  color: #999;
  font-weight: normal;
}
.percent-grid {
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 12px 0;
}
.percent-item {
  text-align: center;
}
.percent-label {
  margin-top: 8px;
  font-weight: 600;
}
.recommend-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.data-source {
  font-size: 12px;
  color: #999;
}
.recommend-desc {
  margin-bottom: 12px;
}
.recommend-value {
  font-size: 16px;
  font-weight: 600;
  color: #1890ff;
}
.reason-text {
  color: #666;
  line-height: 1.6;
}
.actions {
  margin-top: auto;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}
.soil-layers {
  display: flex;
  gap: 16px;
  justify-content: space-between;
}
.soil-layer {
  flex: 1;
  padding: 16px;
  border-radius: 8px;
  background: #fafafa;
}
.layer-top { background: #e6f7ff; }
.layer-middle { background: #f6ffed; }
.layer-bottom { background: #fff7e6; }
.layer-label {
  font-weight: 600;
  margin-bottom: 12px;
  text-align: center;
}
.layer-data {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.layer-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 12px;
  background: #fff;
  border-radius: 4px;
}
.layer-item-label {
  color: #666;
  font-size: 13px;
}
.layer-item-value {
  font-weight: 600;
  color: #333;
}
.mt-4 { margin-top: 16px; }

.fertilizer-scheme {
  .fertilizer-scheme-card {
    background: linear-gradient(135deg, #f0f5ff 0%, #e6f4ff 100%);
    border: 1px solid #91caff;
    border-radius: 12px;
    padding: 16px;
    margin-bottom: 12px;
    transition: all 0.3s;

    &:hover {
      box-shadow: 0 4px 12px rgba(82, 196, 26, 0.2);
      transform: translateY(-2px);
    }

    .scheme-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 8px;

      .scheme-npk {
        font-size: 12px;
        color: #666;
        font-weight: 500;
      }
    }

    .scheme-name {
      font-size: 16px;
      font-weight: 600;
      color: #262626;
      margin-bottom: 12px;
    }

    .scheme-amount {
      background: #fff;
      border-radius: 8px;
      padding: 12px;
      text-align: center;
      margin-bottom: 12px;

      .amount-value {
        font-size: 28px;
        font-weight: 700;
        color: #52c41a;
      }

      .amount-unit {
        font-size: 14px;
        color: #999;
        margin-left: 4px;
      }
    }

    .scheme-detail {
      .detail-item {
        display: flex;
        justify-content: space-between;
        padding: 4px 0;
        font-size: 13px;

        .detail-label {
          color: #999;
        }

        .detail-value {
          color: #333;
          font-weight: 500;
        }
      }
    }
  }

  .scheme-tip {
    background: #f6ffed;
    border: 1px solid #b7eb8f;
    border-radius: 8px;
    padding: 12px;
    margin-top: 16px;
    font-size: 13px;
    color: #52c41a;
    display: flex;
    align-items: flex-start;
    gap: 8px;

    :deep(.anticon) {
      font-size: 16px;
      flex-shrink: 0;
      margin-top: 2px;
    }
  }
}

.fertilizer-scheme-report {
  margin-top: 16px;

  .scheme-report-title {
    font-size: 14px;
    font-weight: 600;
    color: #262626;
    margin-bottom: 12px;
    padding-left: 8px;
    border-left: 3px solid #52c41a;
  }

  .scheme-report-grid {
    .scheme-report-card {
      background: linear-gradient(135deg, #f0f5ff 0%, #e6f4ff 100%);
      border: 1px solid #91caff;
      border-radius: 8px;
      padding: 12px;
      transition: all 0.3s;

      &:hover {
        box-shadow: 0 2px 8px rgba(82, 196, 26, 0.2);
      }

      .scheme-report-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 8px;

        .scheme-report-npk {
          font-size: 12px;
          color: #666;
          font-weight: 500;
        }
      }

      .scheme-report-name {
        font-size: 14px;
        font-weight: 600;
        color: #262626;
        margin-bottom: 8px;
      }

      .scheme-report-amount {
        background: #fff;
        border-radius: 6px;
        padding: 8px;
        text-align: center;
        margin-bottom: 8px;

        .scheme-amount-value {
          font-size: 22px;
          font-weight: 700;
          color: #52c41a;
        }

        .scheme-amount-unit {
          font-size: 12px;
          color: #999;
          margin-left: 2px;
        }
      }

      .scheme-report-detail {
        .report-detail-item {
          display: flex;
          justify-content: space-between;
          padding: 2px 0;
          font-size: 12px;

          .report-detail-label {
            color: #999;
          }

          .report-detail-value {
            color: #333;
            font-weight: 500;
            max-width: 120px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .soil-layers {
    flex-direction: column;
  }
  .nutrient-grid {
    flex-wrap: wrap;
    gap: 12px;
  }
  .nutrient-item {
    flex: 1 1 30%;
  }
}

.fertilizer-report-modal {
  :deep(.ant-modal-content) {
    border-radius: 16px;
    overflow: hidden;
  }
  
  :deep(.ant-modal-header) {
    background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
    padding: 16px 24px;
    
    .ant-modal-title {
      color: #fff;
      font-size: 18px;
      font-weight: 600;
    }
  }
  
  :deep(.ant-modal-close) {
    color: #fff;
    
    &:hover {
      color: #f0f0f0;
    }
  }
}

.report-container {
  padding: 8px 4px;
  max-height: 70vh;
  overflow-y: auto;
}

.report-header {
  text-align: center;
  padding: 16px 0;
}

.report-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  font-size: 28px;
  color: #fff;
  box-shadow: 0 4px 12px rgba(82, 196, 26, 0.3);
}

.report-title {
  font-size: 24px;
  font-weight: 700;
  color: #262626;
  margin: 0 0 16px;
}

.report-meta {
  display: flex;
  justify-content: center;
  gap: 32px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.meta-label {
  font-size: 12px;
  color: #8c8c8c;
  margin-bottom: 4px;
}

.meta-value {
  font-size: 14px;
  color: #262626;
  font-weight: 600;
}

.report-divider {
  margin: 20px 0;
  border-color: #e8e8e8;
}

.report-section {
  margin-bottom: 8px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  
  h4 {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
    color: #262626;
  }
  
  .anticon {
    font-size: 18px;
    color: #52c41a;
  }
}

.section-content {
  padding-left: 4px;
}

.ec-card,
.nutrient-card {
  background: #fafafa;
  border-radius: 12px;
  padding: 16px;
  height: 100%;
}

.ec-card-title,
.nutrient-card-title {
  font-size: 13px;
  color: #595959;
  font-weight: 500;
  margin-bottom: 12px;
  text-align: center;
}

.ec-values {
  display: flex;
  justify-content: space-around;
}

.ec-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  
  .ec-label {
    font-size: 12px;
    color: #8c8c8c;
    margin-bottom: 4px;
  }
  
  .ec-value {
    font-size: 18px;
    font-weight: 700;
    color: #1890ff;
  }
}

.nutrient-values {
  display: flex;
  justify-content: space-around;
}

.nutrient-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 12px;
  border-radius: 8px;
  
  &.n {
    background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%);
  }
  
  &.p {
    background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
  }
  
  &.k {
    background: linear-gradient(135deg, #fff7e6 0%, #ffd591 100%);
  }
}

.nutrient-icon {
  font-size: 14px;
  font-weight: 700;
  color: #262626;
  margin-bottom: 4px;
}

.nutrient-value {
  font-size: 16px;
  font-weight: 700;
  color: #262626;
}

.recommend-header {
  margin-bottom: 12px;
}

.need-fertilizer-tag {
  font-size: 14px;
  padding: 4px 16px;
  border-radius: 6px;
}

.recommend-table {
  margin-bottom: 16px;
  
  :deep(.ant-descriptions-item-label) {
    background: #fafafa;
    font-weight: 500;
  }
  
  .table-value {
    color: #262626;
    font-weight: 500;
  }
}

.fertilizer-recommend {
  margin-bottom: 16px;
}

.fertilizer-recommend-title {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 12px;
}

.fertilizer-grid {
  margin-top: 8px;
}

.fertilizer-card {
  border-radius: 12px;
  padding: 20px 12px;
  text-align: center;
  transition: all 0.3s;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
}

.n-card {
  background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%);
  border: 1px solid #b7eb8f;
  
  .fertilizer-icon {
    background: #52c41a;
  }
}

.p-card {
  background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
  border: 1px solid #91d5ff;
  
  .fertilizer-icon {
    background: #1890ff;
  }
}

.k-card {
  background: linear-gradient(135deg, #fff7e6 0%, #ffd591 100%);
  border: 1px solid #ffc53d;
  
  .fertilizer-icon {
    background: #fa8c16;
  }
}

.fertilizer-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 8px;
  font-size: 16px;
  font-weight: 700;
  color: #fff;
}

.fertilizer-value {
  font-size: 28px;
  font-weight: 700;
  color: #262626;
  line-height: 1.2;
}

.fertilizer-unit {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 2px;
}

.fertilizer-label {
  font-size: 13px;
  color: #595959;
  font-weight: 500;
  margin-top: 8px;
}

.reason-box {
  background: linear-gradient(135deg, #fffbe6 0%, #fff1b8 100%);
  border: 1px solid #ffe58f;
  border-radius: 10px;
  padding: 16px;
}

.reason-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-weight: 600;
  color: #d48806;
  
  .anticon {
    font-size: 16px;
  }
}

.reason-content {
  color: #595959;
  font-size: 13px;
  line-height: 1.7;
  margin: 0;
  white-space: pre-wrap;
}

.overview-summary {
  background: linear-gradient(135deg, #f0f5ff 0%, #e6f0ff 100%);
  border: 1px solid #adc6ff;
  border-radius: 12px;
  padding: 16px 20px;
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.summary-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 22px;
  flex-shrink: 0;
}

.summary-info {
  flex: 1;
}

.summary-label {
  font-size: 14px;
  color: #595959;
  font-weight: 500;
  margin-bottom: 6px;
}

.summary-values {
  display: flex;
  align-items: baseline;
  gap: 16px;
  flex-wrap: wrap;
}

.summary-val {
  font-size: 13px;
  color: #8c8c8c;
  
  b {
    color: #1890ff;
    font-size: 16px;
  }
}

.summary-unit {
  font-size: 12px;
  color: #bfbfbf;
}

.nutrient-card.full {
  background: linear-gradient(135deg, #fafafa 0%, #f5f5f5 100%);
}

.nutrient-values.large {
  padding: 8px 0;
}

.nutrient-values.large .nutrient-item {
  padding: 12px 16px;
}

.nutrient-values.large .nutrient-icon.large {
  font-size: 20px;
  width: 44px;
  height: 44px;
}

.nutrient-values.large .nutrient-value {
  font-size: 22px;
}

.nutrient-values.large .nutrient-label {
  font-size: 13px;
  color: #8c8c8c;
  margin-top: 2px;
}

.percent-card {
  background: #fafafa;
  border-radius: 12px;
  padding: 16px;
  height: 100%;
}

.percent-card-title {
  font-size: 13px;
  color: #595959;
  font-weight: 500;
  text-align: center;
  margin-bottom: 8px;
}

.percent-grid {
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 4px 0;
}

.percent-item {
  text-align: center;
}

.percent-label {
  margin-top: 4px;
  font-weight: 600;
  font-size: 13px;
  color: #595959;
}

.recommend-subtitle {
  font-size: 13px;
  color: #8c8c8c;
}

.recommend-info-row {
  margin-bottom: 16px;
}

.info-card {
  background: #fafafa;
  border-radius: 10px;
  padding: 14px;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  transition: all 0.3s;
  
  &:hover {
    background: #f0f0f0;
  }
}

.info-card-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16px;
  flex-shrink: 0;
}

.info-card-content {
  flex: 1;
  min-width: 0;
}

.info-card-label {
  font-size: 12px;
  color: #8c8c8c;
  margin-bottom: 4px;
}

.info-card-value {
  font-size: 13px;
  color: #262626;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.need-fertilizer-tag.large {
  font-size: 15px;
  padding: 6px 18px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.fertilizer-card .fertilizer-desc {
  font-size: 11px;
  color: #8c8c8c;
  margin-top: 6px;
}

.tips-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.tip-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  background: #fafafa;
  border-radius: 10px;
  padding: 14px;
  transition: all 0.3s;
  
  &:hover {
    background: #f0f0f0;
    transform: translateX(4px);
  }
}

.tip-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 14px;
  flex-shrink: 0;
}

.tip-content {
  flex: 1;
}

.tip-title {
  font-size: 13px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 4px;
}

.tip-text {
  font-size: 12px;
  color: #666;
  line-height: 1.5;
}

@media (max-width: 768px) {
  .tips-grid {
    grid-template-columns: 1fr;
  }
  
  .recommend-info-row .ant-col {
    margin-bottom: 12px;
  }
}
</style>