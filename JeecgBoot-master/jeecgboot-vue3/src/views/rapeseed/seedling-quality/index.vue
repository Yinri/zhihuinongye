<template>
  <div class="page-container">
    <!-- 横向三列布局 -->
    <div class="columns-container">

      <!-- 第一列：上传模块 -->
      <a-card title="上传图片并选择类型" class="section-card">
        <div class="upload-box">

          <!-- 图片类型选择 -->
          <a-select
            v-model:value="imageType"
            style="width: 100%;"
            placeholder="请选择图片分析类型"
          >
            <a-select-option value="oil_quality">油菜苗密度</a-select-option>
            <a-select-option value="density">油菜颜色指数</a-select-option>
          </a-select>

          <!-- 上传本地图片 -->
          <a-upload
            :show-upload-list="false"
            accept="image/*"
            :before-upload="beforeUpload"
          >
            <a-button type="primary" block>上传本地图片</a-button>
          </a-upload>

          <!-- 线上示例图片 -->
          <div class="online-img-select">
            <span>选择线上示例图片：</span>
            <a-select
              v-model:value="onlineImageUrl"
              style="width: 100%;"
              placeholder="选择示例图片"
            >
              <a-select-option
                v-for="img in onlineImageMap[imageType]"
                :key="img.url"
                :value="img.url"
              >
                {{ img.name }}
              </a-select-option>
            </a-select>
          </div>

          <!-- 预览图 -->
          <div class="preview-box" v-if="previewImage">
            <img :src="previewImage" />
          </div>

          <!-- 提交按钮 -->
          <a-button
            type="primary"
            size="large"
            block
            class="submit-btn"
            @click="submitToBackend"
          >
            提交分析
          </a-button>

        </div>
      </a-card>

            <!-- 第二列：检测结果 -->
      <a-card title="检测结果展示" class="section-card">
        <div v-if="resultData && resultData.length" class="result-section">

          <!-- 原图 -->
          <div class="result-img-box">
            <img :src="previewImage" />
          </div>

          <!-- 颜色分析结果 -->
          <div class="result-info">
            <h3>颜色占比分析：</h3>

            <div
              v-for="(item, index) in resultData"
              :key="index"
              class="color-item"
            >
              <!-- 颜色块 -->
              <div
                class="color-box"
                :style="{ backgroundColor: rgbToCss(item.colour) }"
              ></div>

              <!-- 文字信息 -->
              <div class="color-text">
                <p>
                  RGB：{{ item.colour.map(c => Math.round(c)).join(', ') }}
                </p>
                <p>
                  占比：{{ item.precentage.toFixed(2) }} %
                </p>
              </div>

              <!-- 占比条 -->
              <div class="color-bar">
                <div
                  class="color-bar-inner"
                  :style="{
                    width: item.precentage + '%',
                    backgroundColor: rgbToCss(item.colour)
                  }"
                ></div>
              </div>
            </div>

          </div>
        </div>
         <!-- ========= 苗密度（只显示数量） ========= -->
        <div
          v-else-if="resultData && !Array.isArray(resultData)"
          class="result-section density-section"
        >
          <!-- 原图 -->
          <div class="result-img-box">
            <img :src="previewImage" />
          </div>

          <div class="result-info">
            <h3>苗密度检测结果：</h3>

            <p class="density-number">
              检测到苗株数量：
              <span>{{ resultData.plants_number }}</span>
              株
            </p>
          </div>
        </div>

        <p v-else class="empty-text">
          暂无检测数据，请先上传图片并提交
        </p>
      </a-card>

      <!-- 第三列：防控建议 -->
      <a-card title="智能防控建议" class="section-card">
        <div v-if="adviceText" class="advice-box">
          {{ adviceText }}
        </div>
        <p v-else class="empty-text">暂无防控建议</p>
        <a-form :model="pesticideForm" layout="vertical">
                <a-row :gutter="16">
                  <a-col :span="24">
                    <a-form-item label="选择肥料">
                      <a-select
                        v-model:value="pesticideForm.selectedPesticide"
                        placeholder="请选择肥料"
                        @change="handlePesticideChange"
                        show-search
                        option-filter-prop="children"
                      >
                        <a-select-option
                          v-for="pesticide in pesticideOptions"
                          :key="pesticide.id"
                          :value="pesticide.id"
                        >
                          {{ pesticide.name }}
                        </a-select-option>
                      </a-select>
                    </a-form-item>
                  </a-col>
                </a-row>
                <a-row :gutter="16">
                  <a-col :span="12">
                    <a-form-item label="使用重量">
                      <a-input
                        v-model:value="pesticideForm.dosage"
                        placeholder="请输入使用重量"
                      />
                    </a-form-item>
                  </a-col>
                  <a-col :span="12">
                    <a-form-item label="施肥方法">
                      <a-input
                        v-model:value="pesticideForm.method"
                        placeholder="请输入施肥方法"
                      />
                    </a-form-item>
                  </a-col>
                </a-row>
                <a-form-item>
                  <div class="form-button-center">
                    <a-button
                      type="primary"
                      @click="savePesticideRecord"
                      :loading="savingRecord"
                      :disabled="!pesticideForm.selectedPesticide"
                    >
                      保存使用记录
                    </a-button>
                  </div>
                </a-form-item>
        </a-form>
      </a-card>

    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, watch } from "vue";
import { message } from "ant-design-vue";
import { youcaiApiMap, uploadYoucaiImage,getGrowthAdvice,getPesticideList,addControlRecord} from "./seedlingQuality.api";

/* ========= 状态 ========= */
const imageType = ref<string>("oil_quality");
const onlineImageUrl = ref<string>("");
const previewImage = ref<string>("");
const base64Image = ref<string>("");

const resultData = ref<any>(null);
const adviceText = ref<string>("");

/* ========= 示例图片 ========= */
const onlineImageMap: Record<string, any[]> = {
  oil_quality: [
    { url: "https://real-server.com/youcai/quality1.jpg", name: "油菜密度示例一" },
    { url: "https://real-server.com/youcai/quality2.jpg", name: "油菜密度示例二" },
  ],
  density: [
    { url: "https://bpic.588ku.com/element_origin_min_pic/19/07/09/a909be8ad3cc9f9da93c62f5c558aa1e.jpg", name: "颜色指数示例一" },
    { url: "https://th.bing.com/th/id/R.a40465e4923e78a6f6ff5decae12dbe4?rik=yMGkFld4EQIzBg&riu=http%3a%2f%2fpic.baike.soso.com%2fp%2f20140221%2f20140221010641-487941928.jpg&ehk=u%2flM4BrrMchlrRGQx3U1Y9BhaHiZ5lpx91Hp2PIyTeo%3d&risl=&pid=ImgRaw&r=0", name: "颜色指数示例二" },
  ],
};

/* ========= 切换类型清空 ========= */
watch(imageType, () => {
  onlineImageUrl.value = "";
  previewImage.value = "";
  base64Image.value = "";
  resultData.value = null;
  adviceText.value = "";
});

/* ========= 本地图片 → Base64 ========= */
const beforeUpload = (file: File) => {
  const reader = new FileReader();
  reader.onload = (e: any) => {
    base64Image.value = e.target.result;
    previewImage.value = e.target.result;
  };
  reader.readAsDataURL(file);
  return false;
};

/* ========= URL → Base64 ========= */
const urlToBase64 = async (url: string): Promise<string> => {
  const res = await fetch(url);
  const blob = await res.blob();

  return new Promise((resolve) => {
    const reader = new FileReader();
    reader.onload = () => resolve(reader.result as string);
    reader.readAsDataURL(blob);
  });
};

const rgbToCss = (colour: number[]) => {
  const [r, g, b] = colour;
  return `rgb(${Math.round(r)}, ${Math.round(g)}, ${Math.round(b)})`;
};

// 添加农药选择相关变量
const pesticideForm = ref({
  selectedPesticide: null,
  dosage: '',
  method: '',
  defaultDosage: '',
  defaultMethod: ''
})

const pesticideOptions = ref([])
//获取肥料列表
const fetchPesticideOptions = async () => {
  try {
    const response = await getPesticideList()
    const formattedPesticides = response.map((name, index) => ({
      id: `pesticide_${index + 1}`,
      name: name
    }))

    pesticideOptions.value = formattedPesticides || []
  } catch (error) {
    console.error('获取肥料列表失败:', error)
    message.error('获取肥料列表失败')
  }
}
const handlePesticideChange = (value) => {
  // 清空剂量和方法字段，让用户自己填写
  pesticideForm.value.dosage = '';
  pesticideForm.value.method = '';
}
const savingRecord = ref(false)
// 保存农药使用记录
const getDateRange = () => {
  const today = new Date()
  const tenDaysAgo = new Date(today)
  tenDaysAgo.setDate(today.getDate() - 10)
  const formatDate = (date) => date.toISOString().slice(0, 10)
  return {
    start_date: formatDate(tenDaysAgo),
    end_date: formatDate(today)
  }
}

import { useSelectStore } from '/@/store/selectStore';
const selectStore = useSelectStore();

const { start_date, end_date } = getDateRange()
const savePesticideRecord = async () => {
  if (!pesticideForm.value.selectedPesticide) {
    message.warning('请选择肥料')
    return
  }
  savingRecord.value = true
  try {
    const requestData = {
      baseId:selectStore.selectedBase.baseId,
      plotId:selectStore.selectedPlot.plotId,
      monitorDate: end_date,
      fertilizerType: pesticideOptions.value.find(item => item.id === pesticideForm.value.selectedPesticide)?.name,
      fertilizerAmount: pesticideForm.value.dosage,
      fertilizerMethod: pesticideForm.value.method,
    }
  
    await addControlRecord(requestData);
    
    // 重置表单
    pesticideForm.value.selectedPesticide = null;
    pesticideForm.value.dosage = '';
    pesticideForm.value.method = '';
  } catch (error) {
    console.error('保存使用记录失败:', error)
    message.error('保存使用记录失败')
  } finally {
    savingRecord.value = false
  }
}









/* ========= 提交分析 ========= */
const submitToBackend = async () => {
  try {
    let imageBase64 = "";

    if (base64Image.value) {
      imageBase64 = base64Image.value;
    } else if (onlineImageUrl.value) {
      imageBase64 = await urlToBase64(onlineImageUrl.value);
      previewImage.value = imageBase64;
    } else {
      return message.warning("请上传图片或选择示例图！");
    }
    const apiUrl = youcaiApiMap[imageType.value];
    console.log(imageType.value)
    if (!apiUrl) return message.error("未配置接口！");
    console.log(apiUrl)
    console.log(imageBase64)
    const res = await uploadYoucaiImage(apiUrl, imageBase64);
    
    /* ========= 根据类型分别处理 ========= */
    if (imageType.value === "density") {
      // 👉 苗颜色指数专用逻辑
      const data = typeof res === "string" ? JSON.parse(res) : res;
      resultData.value = data.dominant || [];
      const adviceRes = await getGrowthAdvice(res);
      adviceText.value = adviceRes;
      fetchPesticideOptions()


    } else {
      // 👉 其他类型（例如油菜密度、品质等）
      console.log("后端返回：", res);
      const data = typeof res === "string" ? JSON.parse(res) : res;
      resultData.value = data; 

    }
   
    
    message.success("分析成功！");
  } catch {
    message.error("接口请求失败！");
  }
};
</script>

<style lang="less" scoped>
.page-container {
  padding: 20px;
  background: #f8f9fb;
}

.columns-container {
  display: flex;
  gap: 20px;
}

.section-card {
  flex: 1;
  min-height: 850px;
  border-radius: 14px;
}

/* 第一列居中 */
.upload-box {
  display: flex;
  flex-direction: column;
  gap: 16px;
  align-items: center;
}

.online-img-select {
  width: 100%;
}

.preview-box {
  width: 80%;
  height: 260px;
  border: 1px dashed #d9d9d9;
  border-radius: 12px;
  display: flex;
  justify-content: center;
  align-items: center;

  img {
    max-width: 100%;
    max-height: 100%;
  }
}

.color-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.color-box {
  width: 32px;
  height: 32px;
  border-radius: 4px;
  border: 1px solid #ddd;
  margin-right: 12px;
}

.color-text {
  width: 150px;
  font-size: 13px;
}

.color-bar {
  flex: 1;
  height: 10px;
  background: #f0f0f0;
  border-radius: 5px;
  overflow: hidden;
  margin-left: 10px;
}

.color-bar-inner {
  height: 100%;
  border-radius: 5px;
}
/* 第三列 */
.advice-box {
  background: #f6ffed;
  border: 1px solid #b7eb8f;
  padding: 16px;
  border-radius: 10px;
  color: #389e0d;
  line-height: 1.6;
}

.empty-text {
  text-align: center;
  color: #999;
}

.density-number {
  font-size: 18px;
  margin-top: 16px;
}

.density-number span {
  font-size: 28px;
  font-weight: bold;
  color: #52c41a;
  margin: 0 6px;
}

.form-button-center {
  display: flex;
  justify-content: center;
}
</style>
