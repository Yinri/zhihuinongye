<template>
  <div class="yield-calc-basis">
    <h3 class="section-title">产量计算依据</h3>
    <div class="cards-container">
    <div class="basis-cards">
      <div class="basis-card">
        <p class="card-title">历史 5 年平均产量</p>
        <p class="card-value">175kg / 亩</p>
      </div>
      <div class="basis-card">
        <p class="card-title">当年气象预测</p>
      </div>
      <div class="basis-card">
        <p class="card-title">土壤肥力均值</p>
      </div>
    </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'YieldCalcBasis'
}
</script>

<style scoped>
.yield-calc-basis {
  border: 1px solid #d9d9d9;
  margin-top: 5px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);



  border-radius: 8px;
  padding: 8px 8px;
  background-color: #fff;
}
.cards-container {
  display: flex;
  justify-content: center; /* 水平居中子元素 */
  width: 100%; /* 占满父容器宽度，确保居中生效 */
}

.section-title {
  font-size: 14px;
  font-weight: bold;
  color: #333;
  margin: 0 0 16px 0; /* 取消底部默认边距，实现左上角布局 */
  white-space: nowrap; /* 防止文字换行 */
  padding-left:5px;
}

.basis-cards {
  display: flex;
  justify-content: space-between;
  gap: 200px;
  width:80%;

}

.basis-card {
  flex: 1;
  min-width: 200px;
  background-color: #f0f0f0;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
}

.card-title {
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
  font-weight: bold;
}

.card-value {
  font-size: 16px;
  color: #333;
  font-weight: bold;
}
</style>
