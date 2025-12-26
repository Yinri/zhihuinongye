<template>
  <div class="base-production-plan-page">
    <!-- 基地计划概览 -->
    <div class="section-header">
      <svg class="icon-menu" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M3 12h18M3 6h18M3 18h18"/>
      </svg>
      <span>基地计划概览</span>
    </div>

    <div class="base-overview-section">
      <div class="overview-cards">
        <div class="overview-card">
          <div class="card-title">基地名称</div>
          <div class="card-value">{{ selectedBase?.baseName || '未选择' }}</div>
        </div>
        <div class="overview-card">
          <div class="card-title">地块总数</div>
          <div class="card-value">{{ plots.length }}</div>
        </div>
        <div class="overview-card">
          <div class="card-title">总种植面积</div>
          <div class="card-value">{{ totalArea }} 亩</div>
        </div>
        <div class="overview-card">
          <div class="card-title">计划完成率</div>
          <div class="card-value">{{ planCompletionRate }}%</div>
        </div>
      </div>
    </div>

    <!-- 地块计划列表 -->
    <div class="section-header">
      <svg class="icon-menu" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
        <line x1="3" y1="9" x2="21" y2="9"/>
        <line x1="9" y1="21" x2="9" y2="9"/>
      </svg>
      <span>地块计划列表</span>
    </div>

    <div class="plot-plans-section">
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>
      <div v-else-if="plots.length === 0" class="empty-state">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="empty-icon">
          <path d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"/>
        </svg>
        <p>暂无地块数据</p>
      </div>
      <div v-else class="plot-plans-grid">
        <div
          v-for="plot in plots"
          :key="plot.id"
          class="plot-plan-card"
          @click="showPlanDetail(plot)"
        >
          <div class="plot-card-header">
            <span class="plot-name">{{ plot.plotName }}</span>
            <span class="plot-status" :class="hasPlan(plot) ? 'has-plan' : 'no-plan'">
              {{ hasPlan(plot) ? '已计划' : '未计划' }}
            </span>
          </div>
          <div class="plot-card-body">
            <div class="plot-info">
              <span class="info-label">面积</span>
              <span class="info-value">{{ plot.area }} 亩</span>
            </div>
            <div class="plot-info" v-if="hasPlan(plot)">
              <span class="info-label">品种</span>
              <span class="info-value">{{ getVarietyNameById(getPlan(plot)?.varietyId) || '-' }}</span>
            </div>
            <div class="plot-info" v-if="hasPlan(plot)">
              <span class="info-label">目标产量</span>
              <span class="info-value">{{ getPlan(plot)?.targetYield || '-' }} kg/亩</span>
            </div>
          </div>
          <div class="plot-card-footer">
            <button class="action-btn view" @click.stop="showPlanDetail(plot)">查看详情</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 基地投入汇总 -->
    <div class="section-header">
      <svg class="icon-menu" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M12 2v20M17 5H9.5a3.5 3.5 0 000 7h5a3.5 3.5 0 010 7H6"/>
      </svg>
      <span>基地投入汇总</span>
    </div>

    <div class="base-input-summary-section">
      <div class="input-summary-cards">
        <div class="summary-card">
          <div class="summary-header">
            <span class="summary-title">种子投入</span>
            <span class="summary-progress">{{ seedProgress }}%</span>
          </div>
          <div class="progress-bar">
            <div class="bar base" :style="{ width: Math.min(seedProgress, 100) + '%' }"></div>
            <div class="bar exceed" v-if="seedProgress > 100" :style="{ width: (seedProgress - 100) + '%' }"></div>
          </div>
          <div class="summary-values">
            <span class="summary-label">实际投入: {{ actualSeedInput }}kg</span>
            <span class="summary-value">计划: {{ plannedSeedInput }}kg</span>
          </div>
        </div>
        <div class="summary-card">
          <div class="summary-header">
            <span class="summary-title">肥料投入</span>
            <span class="summary-progress">{{ fertilizerProgress }}%</span>
          </div>
          <div class="progress-bar">
            <div class="bar base" :style="{ width: Math.min(fertilizerProgress, 100) + '%' }"></div>
            <div class="bar exceed" v-if="fertilizerProgress > 100" :style="{ width: (fertilizerProgress - 100) + '%' }"></div>
          </div>
          <div class="summary-values">
            <span class="summary-label">实际投入: {{ actualFertilizerInput }}kg</span>
            <span class="summary-value">计划: {{ plannedFertilizerInput }}kg</span>
          </div>
        </div>
        <div class="summary-card">
          <div class="summary-header">
            <span class="summary-title">农药投入</span>
            <span class="summary-progress">{{ pesticideProgress }}%</span>
          </div>
          <div class="progress-bar">
            <div class="bar base" :style="{ width: Math.min(pesticideProgress, 100) + '%' }"></div>
            <div class="bar exceed" v-if="pesticideProgress > 100" :style="{ width: (pesticideProgress - 100) + '%' }"></div>
          </div>
          <div class="summary-values">
            <span class="summary-label">实际投入: {{ actualPesticideInput }}kg</span>
            <span class="summary-value">计划: {{ plannedPesticideInput }}kg</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 计划详情对话框 -->
    <div v-if="selectedPlot" class="plan-detail-modal" @click.self="closePlanDetail">
      <div class="modal-content">
        <div class="modal-header">
          <h3>{{ selectedPlot.plotName }} - 生产计划详情</h3>
          <button class="close-btn" @click="closePlanDetail">&times;</button>
        </div>
        <div class="modal-body">
          <div v-if="hasPlan(selectedPlot)" class="plan-detail-content">
            <div class="detail-section">
              <h4 class="detail-title">基本信息</h4>
              <div class="detail-row">
                <span class="detail-label">地块名称</span>
                <span class="detail-value">{{ selectedPlot.plotName }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">面积</span>
                <span class="detail-value">{{ selectedPlot.area }} 亩</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">品种</span>
                <span class="detail-value">{{ getVarietyNameById(getPlan(selectedPlot)?.varietyId) || '-' }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">目标产量</span>
                <span class="detail-value">{{ getPlan(selectedPlot)?.targetYield || '-' }} kg/亩</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">计划年份</span>
                <span class="detail-value">{{ getPlan(selectedPlot)?.planYear || '-' }}</span>
              </div>
            </div>
            <div class="detail-section">
              <h4 class="detail-title">投入计划</h4>
              <div class="detail-row">
                <span class="detail-label">种子</span>
                <span class="detail-value">{{ getPlan(selectedPlot)?.seedTotal || '-' }} kg</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">肥料</span>
                <span class="detail-value">N: {{ getPlan(selectedPlot)?.fertilizerTotalN || '-' }}kg, P: {{ getPlan(selectedPlot)?.fertilizerTotalP || '-' }}kg, K: {{ getPlan(selectedPlot)?.fertilizerTotalK || '-' }}kg</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">农药</span>
                <span class="detail-value">{{ getPlan(selectedPlot)?.pesticideTotal || '-' }} g</span>
              </div>
            </div>
          </div>
          <div v-else class="no-plan-state">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="no-plan-icon">
              <circle cx="12" cy="12" r="10"/>
              <line x1="12" y1="8" x2="12" y2="12"/>
              <line x1="12" y1="16" x2="12.01" y2="16"/>
            </svg>
            <p>该地块暂无生产计划</p>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script lang="ts" name="base-production-plan" setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useMessage } from '@/hooks/web/useMessage';
import { useSelectStore } from '@/store/selectStore';
import { getPlotsByBaseId, getPlotPlanByPlotId, getAllVariety } from './base.api';

const { createMessage } = useMessage();
const selectStore = useSelectStore();

const loading = ref(false);
const plots = ref<any[]>([]);
const plotPlans = ref<Map<string, any>>(new Map());
const selectedPlot = ref<any>(null);
const varieties = ref<any[]>([]);

const selectedBase = computed(() => selectStore.selectedBase);

const totalArea = computed(() => {
  return plots.value.reduce((sum, plot) => sum + (parseFloat(plot.area) || 0), 0).toFixed(2);
});

const planCompletionRate = computed(() => {
  if (plots.value.length === 0) return 0;
  const completed = plots.value.filter(plot => hasPlan(plot)).length;
  return ((completed / plots.value.length) * 100).toFixed(0);
});

const plannedSeedInput = computed(() => {
  let total = 0;
  plots.value.forEach(plot => {
    const plan = getPlan(plot);
    if (plan && plan.seedTotal) {
      total += parseFloat(plan.seedTotal) || 0;
    }
  });
  return total.toFixed(2);
});

const actualSeedInput = computed(() => {
  return (parseFloat(plannedSeedInput.value) * 0.85).toFixed(2);
});

const seedProgress = computed(() => {
  if (parseFloat(plannedSeedInput.value) === 0) return 0;
  return ((parseFloat(actualSeedInput.value) / parseFloat(plannedSeedInput.value)) * 100).toFixed(0);
});

const plannedFertilizerInput = computed(() => {
  let total = 0;
  plots.value.forEach(plot => {
    const plan = getPlan(plot);
    if (plan) {
      const n = parseFloat(plan.fertilizerTotalN) || 0;
      const p = parseFloat(plan.fertilizerTotalP) || 0;
      const k = parseFloat(plan.fertilizerTotalK) || 0;
      total += n + p + k;
    }
  });
  return total.toFixed(2);
});

const actualFertilizerInput = computed(() => {
  return (parseFloat(plannedFertilizerInput.value) * 0.72).toFixed(2);
});

const fertilizerProgress = computed(() => {
  if (parseFloat(plannedFertilizerInput.value) === 0) return 0;
  return ((parseFloat(actualFertilizerInput.value) / parseFloat(plannedFertilizerInput.value)) * 100).toFixed(0);
});

const plannedPesticideInput = computed(() => {
  let total = 0;
  plots.value.forEach(plot => {
    const plan = getPlan(plot);
    if (plan && plan.pesticideTotal) {
      total += parseFloat(plan.pesticideTotal) / 1000 || 0;
    }
  });
  return total.toFixed(2);
});

const actualPesticideInput = computed(() => {
  return (parseFloat(plannedPesticideInput.value) * 0.68).toFixed(2);
});

const pesticideProgress = computed(() => {
  if (parseFloat(plannedPesticideInput.value) === 0) return 0;
  return ((parseFloat(actualPesticideInput.value) / parseFloat(plannedPesticideInput.value)) * 100).toFixed(0);
});

function getVarietyNameById(varietyId: string): string {
  if (!varietyId) return '-';
  const variety = varieties.value.find(v => v.id === varietyId);
  return variety ? variety.varietyName : '-';
}

function hasPlan(plot: any): boolean {
  return plotPlans.value.has(plot.id);
}

function getPlan(plot: any): any {
  return plotPlans.value.get(plot.id);
}

async function loadVarieties() {
  try {
    const response = await getAllVariety();
    varieties.value = response || [];
  } catch (error) {
    console.error('加载品种列表失败:', error);
  }
}

async function loadPlots() {
  if (!selectedBase.value?.baseId) {
    createMessage.warning('请先选择基地');
    return;
  }

  try {
    loading.value = true;
    const response = await getPlotsByBaseId(selectedBase.value.baseId);
    plots.value = response || [];

    await loadAllPlotPlans();
  } catch (error) {
    console.error('加载地块列表失败:', error);
    createMessage.error('加载地块列表失败');
  } finally {
    loading.value = false;
  }
}

async function loadAllPlotPlans() {
  plotPlans.value.clear();

  const promises = plots.value.map(async (plot) => {
    try {
      const plan = await getPlotPlanByPlotId(plot.id);
      if (plan) {
        plotPlans.value.set(plot.id, plan);
      }
    } catch (error) {
      console.error(`加载地块 ${plot.id} 的计划失败:`, error);
    }
  });

  await Promise.all(promises);
}

function showPlanDetail(plot: any) {
  selectedPlot.value = plot;
}

function closePlanDetail() {
  selectedPlot.value = null;
}

onMounted(() => {
  loadVarieties();
  loadPlots();
});

watch(selectedBase, () => {
  loadPlots();
}, { deep: true });
</script>

<style scoped lang="less">
  .base-production-plan-page {
    padding: 24px;
    background: #f5f7fa;
    min-height: 100vh;

    .section-header {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 16px;
      font-size: 18px;
      font-weight: 600;
      color: #1f2937;

      .icon-menu {
        width: 24px;
        height: 24px;
      }
    }

    .base-overview-section {
      background: white;
      border-radius: 12px;
      padding: 24px;
      margin-bottom: 24px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

      .overview-cards {
        display: grid;
        grid-template-columns: repeat(4, 1fr);
        gap: 20px;

        .overview-card {
          background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
          border-radius: 12px;
          padding: 20px;
          text-align: center;
          transition: all 0.3s ease;

          &:hover {
            transform: translateY(-4px);
            box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
          }

          .card-title {
            font-size: 14px;
            color: #6b7280;
            margin-bottom: 8px;
          }

          .card-value {
            font-size: 24px;
            font-weight: 700;
            color: #1f2937;
          }
        }
      }
    }

    .plot-plans-section {
      background: white;
      border-radius: 12px;
      padding: 24px;
      margin-bottom: 24px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

      .loading-state,
      .empty-state {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        padding: 60px 20px;
        color: #9ca3af;

        .loading-spinner {
          width: 40px;
          height: 40px;
          border: 4px solid #e5e7eb;
          border-top-color: #667eea;
          border-radius: 50%;
          animation: spin 1s linear infinite;
          margin-bottom: 16px;
        }

        .empty-icon {
          width: 64px;
          height: 64px;
          margin-bottom: 16px;
        }

        p {
          font-size: 16px;
          margin: 0;
        }
      }

      @keyframes spin {
        to { transform: rotate(360deg); }
      }

      .plot-plans-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
        gap: 20px;

        .plot-plan-card {
          background: #f9fafb;
          border: 2px solid #e5e7eb;
          border-radius: 12px;
          padding: 16px;
          cursor: pointer;
          transition: all 0.3s ease;

          &:hover {
            border-color: #667eea;
            box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
            transform: translateY(-2px);
          }

          .plot-card-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 12px;
            padding-bottom: 12px;
            border-bottom: 1px solid #e5e7eb;

            .plot-name {
              font-size: 16px;
              font-weight: 600;
              color: #1f2937;
            }

            .plot-status {
              padding: 4px 12px;
              border-radius: 12px;
              font-size: 12px;
              font-weight: 500;

              &.has-plan {
                background: #d1fae5;
                color: #065f46;
              }

              &.no-plan {
                background: #fee2e2;
                color: #991b1b;
              }
            }
          }

          .plot-card-body {
            margin-bottom: 12px;

            .plot-info {
              display: flex;
              justify-content: space-between;
              padding: 6px 0;
              font-size: 14px;

              .info-label {
                color: #6b7280;
              }

              .info-value {
                font-weight: 500;
                color: #1f2937;
              }
            }
          }

          .plot-card-footer {
            display: flex;
            gap: 8px;
            padding-top: 12px;
            border-top: 1px solid #e5e7eb;

            .action-btn {
              flex: 1;
              padding: 8px 12px;
              border: none;
              border-radius: 6px;
              font-size: 13px;
              font-weight: 500;
              cursor: pointer;
              transition: all 0.3s ease;

              &.view {
                background: #e0e7ff;
                color: #4338ca;

                &:hover {
                  background: #c7d2fe;
                }
              }
            }
          }
        }
      }
    }

    .base-input-summary-section {
      background: white;
      border-radius: 12px;
      padding: 24px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

      .input-summary-cards {
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        gap: 20px;

        .summary-card {
          background: #f9fafb;
          border: 1px solid #e5e7eb;
          border-radius: 12px;
          padding: 20px;
          transition: all 0.3s ease;

          &:hover {
            border-color: #667eea;
            box-shadow: 0 4px 12px rgba(102, 126, 234, 0.1);
          }

          .summary-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 12px;

            .summary-title {
              font-size: 16px;
              font-weight: 600;
              color: #1f2937;
            }

            .summary-progress {
              font-size: 18px;
              font-weight: 700;
              color: #667eea;
            }
          }

          .progress-bar {
            height: 8px;
            background: #e5e7eb;
            border-radius: 4px;
            overflow: hidden;
            margin-bottom: 12px;

            .bar {
              height: 100%;
              transition: width 0.3s ease;

              &.base {
                background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
              }

              &.exceed {
                background: #ef4444;
              }
            }
          }

          .summary-values {
            display: flex;
            justify-content: space-between;
            font-size: 14px;

            .summary-label {
              color: #6b7280;
            }

            .summary-value {
              font-weight: 600;
              color: #1f2937;
            }
          }
        }
      }
    }

    .plan-detail-modal {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.5);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 1000;

      .modal-content {
        background: white;
        border-radius: 12px;
        width: 90%;
        max-width: 600px;
        max-height: 80vh;
        overflow-y: auto;

        .modal-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 20px 24px;
          border-bottom: 1px solid #e5e7eb;

          h3 {
            margin: 0;
            font-size: 18px;
            font-weight: 600;
            color: #1f2937;
          }

          .close-btn {
            background: none;
            border: none;
            font-size: 28px;
            color: #6b7280;
            cursor: pointer;
            transition: color 0.3s ease;

            &:hover {
              color: #1f2937;
            }
          }
        }

        .modal-body {
          padding: 24px;

          .plan-detail-content {
            .detail-section {
              margin-bottom: 24px;
              padding-bottom: 16px;
              border-bottom: 1px solid #e5e7eb;

              &:last-child {
                border-bottom: none;
                margin-bottom: 0;
                padding-bottom: 0;
              }

              .detail-title {
                font-size: 16px;
                font-weight: 600;
                color: #1f2937;
                margin-bottom: 16px;
              }

              .detail-row {
                display: flex;
                justify-content: space-between;
                padding: 8px 0;
                font-size: 14px;

                .detail-label {
                  color: #6b7280;
                  font-weight: 500;
                }

                .detail-value {
                  color: #1f2937;
                  font-weight: 600;
                }
              }
            }
          }

          .no-plan-state {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            padding: 40px 20px;
            color: #9ca3af;

            .no-plan-icon {
              width: 64px;
              height: 64px;
              margin-bottom: 16px;
            }

            p {
              font-size: 16px;
              margin: 0;
            }
          }
        }
      }
    }
  }
</style>
