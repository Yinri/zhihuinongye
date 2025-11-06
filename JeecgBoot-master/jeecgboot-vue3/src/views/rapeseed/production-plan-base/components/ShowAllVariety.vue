<template>
  <div class="dashboard-wrapper">
    <!-- 左侧：品种占比饼状图 -->
    <div class="chart-container">
      <div id="varietyPieChart" style="width: 100%; height: 100%"></div>
    </div>

    <!-- 右侧：品种卡片列表（可滚动） -->
    <div class="cards-container">
      <div class="card-list">
        <div
          v-for="variety in varieties"
          :key="variety.value"
          class="variety-card"
          :class="{ 'active': selectedVariety === variety.value }"
          @click="selectVariety(variety.value)"
        >
          <h3 class="variety-name">{{ variety.label }}</h3>
          <ul class="variety-info">
            <li>种植面积：{{ variety.area }} 亩</li>
            <li>占比：{{ variety.percent }}%</li>
            <li>目标产量：{{ variety.yieldRange }}kg/亩</li>
          </ul>
          <div class="card-buttons">
            <button class="btn-view">查看地块</button>
            <button class="btn-edit">修改计划</button>
          </div>
        </div>
      </div>
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
        <a-form-model-item label="品种名称" prop="label" class="form-item">
          <a-input v-model="newVariety.label" placeholder="请输入作物品种名称" class="variety_input" />
        </a-form-model-item>
        <a-form-model-item label="品种标识" prop="value" class="form-item">
          <a-input v-model="newVariety.value" placeholder="请输入品种标识（英文/数字）" class="variety_input" />
        </a-form-model-item>
        <a-form-model-item label="种植面积" prop="area" class="form-item">
          <a-input v-model="newVariety.area" placeholder="请输入种植面积（亩）" class="variety_input" />
        </a-form-model-item>
        <a-form-model-item label="占比" prop="percent" class="form-item">
          <a-input v-model="newVariety.percent" placeholder="请输入占比（%）" class="variety_input" />
        </a-form-model-item>
        <a-form-model-item label="目标产量" prop="yieldRange" class="form-item">
          <a-input v-model="newVariety.yieldRange" placeholder="请输入目标产量（如 160-180）" class="variety_input" />
        </a-form-model-item>
        <a-form-model-item label="投入效率" prop="efficiency" class="form-item">
          <a-input v-model="newVariety.efficiency" placeholder="请输入投入效率（如 每增产10kg需纯氮1.0kg）" class="variety_input" />
        </a-form-model-item>
        <a-form-model-item label="风险等级" prop="risk" class="form-item">
          <a-select v-model="newVariety.risk" placeholder="请选择风险等级">
            <a-select-option value="低风险">低风险</a-select-option>
            <a-select-option value="中风险">中风险</a-select-option>
            <a-select-option value="高风险">高风险</a-select-option>
          </a-select>
        </a-form-model-item>
      </a-form-model>
    </a-modal>
  </div>
</template>

<script>
import * as echarts from 'echarts'; // 引入ECharts

export default {
  data() {
    return {
      // 品种列表（新增面积、占比、产量、效率、风险等字段）
      varieties: [
        {label: '中油杂 19', value: 'zhongyouza19', area: 150, percent: 8, yieldRange: '150-170', efficiency: '每增产8kg需纯氮0.8kg', risk: '低风险', riskClass: 'risk-low'},
        {label: '华油杂 62', value: 'huayouza62', area: 250, percent: 12, yieldRange: '140-160', efficiency: '每增产9kg需纯氮0.9kg', risk: '低风险', riskClass: 'risk-low'},
        {label: '阳光 2009', value: 'yangguang2009', area: 200, percent: 10, yieldRange: '160-180', efficiency: '每增产10kg需纯氮1.0kg', risk: '中风险', riskClass: 'risk-medium'}
      ],
      selectedVariety: 'yangguang2009', // 当前选中品种
      showAddDialog: false, // 弹窗显示状态
      newVariety: { // 新品种表单数据
        label: '',
        value: '',
        area: '',
        percent: '',
        yieldRange: '',
        efficiency: '',
        risk: '中风险'
      },
      rules: { // 表单验证规则
        label: [{required: true, message: '请输入品种名称', trigger: 'blur'}],
        value: [{required: true, message: '请输入品种标识', trigger: 'blur'}, {pattern: /^[a-zA-Z0-9]+$/, message: '标识只能包含英文和数字', trigger: 'blur'}],
        area: [{required: true, message: '请输入种植面积', trigger: 'blur'}, {type: 'number', message: '请输入数字', trigger: 'blur'}],
        percent: [{required: true, message: '请输入占比', trigger: 'blur'}, {type: 'number', message: '请输入数字', trigger: 'blur'}],
        yieldRange: [{required: true, message: '请输入目标产量', trigger: 'blur'}],
        efficiency: [{required: true, message: '请输入投入效率', trigger: 'blur'}],
        risk: [{required: true, message: '请选择风险等级', trigger: 'change'}]
      }
    };
  },
  mounted() {
    this.initPieChart(); // 初始化饼状图
  },
  methods: {
    // 初始化饼状图
    initPieChart() {
      const pieDom = document.getElementById('varietyPieChart');
      const pieChart = echarts.init(pieDom);
      const option = {
        series: [
          {
            type: 'pie',
            radius: '60%',
            data: this.varieties.map(v => ({name: v.label, value: v.percent})),
            emphasis: {itemStyle: {shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)'}}
          }
        ]
      };
      pieChart.setOption(option);
      // 窗口resize时，图表自适应
      window.addEventListener('resize', () => pieChart.resize());
    },
    // 选择品种
    selectVariety(value) {
      this.selectedVariety = value;
    },
    // 处理添加品种
    handleAddVariety() {
      this.$refs.varietyForm.validate(valid => {
        if (valid) {
          const exists = this.varieties.some(v => v.value === this.newVariety.value);
          if (exists) {
            this.$message.error('该品种标识已存在，请更换');
            return;
          }
          // 补充风险样式类
          const riskClass = {
            '低风险': 'risk-low',
            '中风险': 'risk-medium',
            '高风险': 'risk-high'
          }[this.newVariety.risk] || 'risk-medium';
          this.varieties.push({...this.newVariety, riskClass});
          this.selectedVariety = this.newVariety.value;
          this.showAddDialog = false;
          this.$refs.varietyForm.resetFields();
          this.$message.success('品种添加成功');
          this.initPieChart(); // 重新渲染饼状图
        }
      });
    }
  }
};
</script>

<style>
/* 整体布局：左侧饼图 + 右侧卡片列表 */
.dashboard-wrapper {
  display: flex;
  border: 1px solid #d9d9d9; /* 边框样式，颜色可根据需求调整 */
  border-radius: 8px; /* 可选：添加圆角让边框更柔和 */
  padding: 1px; /* 内边距，避免内容紧贴边框 */
  gap: 16px;
  align-items: stretch;
  background-color: white;
  width:100%;
  height:200px;
}

/* 左侧饼状图容器 */
.chart-container {
  flex: 1;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  padding: 10px;
  height:100%;
  width:20px;
}

/* 右侧卡片列表容器 */
.cards-container {
  flex: 2;
  display: flex;
  flex-direction: column;
}

/* 卡片列表（可滚动） */
.card-list {
  display: flex; /* 横向排列 */
  gap: 15px;
  overflow-x: auto; /* 横向滚动 */
  overflow-y: hidden; /* 禁用垂直滚动 */
  padding: 10px 0; /* 上下内边距 */
  height: auto; /* 取消固定高度，由内容撑开 */
  padding-bottom: 15px; /* 底部留白，避免滚动条遮挡 */
}

/* 品种卡片样式 */
.variety-card {
  min-width: 200px;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  padding: 15px;
  background-color: #f9f9f9;
  transition: all 0.2s ease;
}

.variety-card.active {
  border-color: #22c55e;
  box-shadow: 0 0 8px rgba(34, 197, 94, 0.3);
}

/* 品种名称 */
.variety-name {
  font-size: 18px;
  margin: 0 0 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}



/* 品种信息列表 */
.variety-info {
  list-style: none;
  padding: 0;
  margin: 10px 0;
}

.variety-info li {
  margin-bottom: 8px;
  color: #666;
}

/* 卡片按钮 */
.card-buttons {
  display: flex;
  gap: 20px;
  margin-top: 10px;
}

.btn-view, .btn-edit {
  padding: 1px 2px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn-view {
  background-color: #fff;
  border: 1px solid #d9d9d9;
}

.btn-edit {
  background-color: #22c55e;
  color: #fff;
}

/* 弹窗样式（保持原有） */
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

.form-item {
  margin: 25px;
}

.variety_input {
  top: 10px;
  margin-bottom: 10px;
  width: 90%;
  height: 40px;
  padding: 0 12px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s;
}
</style>
