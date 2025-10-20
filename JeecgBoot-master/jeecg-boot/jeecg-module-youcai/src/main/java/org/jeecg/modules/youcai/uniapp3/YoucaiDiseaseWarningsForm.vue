<route lang="json5" type="page">
{
layout: 'default',
style: {
navigationStyle: 'custom',
navigationBarTitleText: '病害预警表',
},
}
</route>
<template>
  <PageLayout :navTitle="navTitle" :backRouteName="backRouteName">
    <scroll-view class="scrollArea" scroll-y>
      <view class="form-container">
        <wd-form ref="form" :model="myFormData">
          <wd-cell-group border>
          <view class="{ 'mt-14px': 0 == 0 }">
               <wd-input
                   label-width="100px"
                   v-model="myFormData['plotId']"
                   :label="get4Label('地块ID')"
                   name='plotId'
                   prop='plotId'
                   placeholder="请选择地块ID"
                   inputMode="numeric"
                   :rules="[
                             { required: true, message: '请输入地块ID!'},
                   ]"
                   clearable
              />
        </view>
          <view class="{ 'mt-14px': 1 == 0 }">
              <online-date
                  :label="get4Label('预警日期')"
                  labelWidth="100px"
                  type="date"
                  name='warningDate'
                  v-model:value="myFormData['warningDate']"
              ></online-date>
        </view>
          <view class="{ 'mt-14px': 0 == 0 }">
              <wd-input
                  label-width="100px"
                  v-model="myFormData['diseaseType']"
                  :label="get4Label('病害类型')"
                  name='diseaseType'
                  prop='diseaseType'
                  placeholder="请选择病害类型"
                  :rules="[
                                  { required: true, message: '请输入病害类型!'},
                  ]"
                  clearable
              />
        </view>
          <view class="{ 'mt-14px': 1 == 0 }">
              <wd-input
                  label-width="100px"
                  v-model="myFormData['diseaseName']"
                  :label="get4Label('病害名称')"
                  name='diseaseName'
                  prop='diseaseName'
                  placeholder="请选择病害名称"
                  :rules="[
                  ]"
                  clearable
              />
        </view>
          <view class="{ 'mt-14px': 0 == 0 }">
              <wd-input
                  label-width="100px"
                  v-model="myFormData['severity']"
                  :label="get4Label('严重程度')"
                  name='severity'
                  prop='severity'
                  placeholder="请选择严重程度"
                  :rules="[
                  ]"
                  clearable
              />
        </view>
          <view class="{ 'mt-14px': 1 == 0 }">
              <wd-textarea
                :label="get4Label('症状描述')"
                 labelWidth="100px"
                 type="textarea"
                 name='symptoms'
                 prop='symptoms'
                 clearable
                 :maxlength="300"
                v-model="myFormData['symptoms']"
              ></wd-textarea>
        </view>
          <view class="{ 'mt-14px': 0 == 0 }">
              <wd-textarea
                :label="get4Label('防治建议')"
                 labelWidth="100px"
                 type="textarea"
                 name='recommendation'
                 prop='recommendation'
                 clearable
                 :maxlength="300"
                v-model="myFormData['recommendation']"
              ></wd-textarea>
        </view>
          <view class="{ 'mt-14px': 1 == 0 }">
              <wd-input
                  label-width="100px"
                  v-model="myFormData['warningStatus']"
                  :label="get4Label('预警状态')"
                  name='warningStatus'
                  prop='warningStatus'
                  placeholder="请选择预警状态"
                  :rules="[
                  ]"
                  clearable
              />
        </view>
          <view class="{ 'mt-14px': 0 == 0 }">
              <wd-input
                  label-width="100px"
                  v-model="myFormData['imageUrl']"
                  :label="get4Label('病害影像')"
                  name='imageUrl'
                  prop='imageUrl'
                  placeholder="请选择病害影像"
                  :rules="[
                  ]"
                  clearable
              />
        </view>
          <view class="{ 'mt-14px': 1 == 0 }">
        </view>
          </wd-cell-group>
        </wd-form>
      </view>
    </scroll-view>
    <view class="footer">
      <wd-button :disabled="loading" block :loading="loading" @click="handleSubmit">提交</wd-button>
    </view>
  </PageLayout>
</template>

<script lang="ts" setup>
import { onLoad } from '@dcloudio/uni-app'
import { http } from '@/utils/http'
import { useToast } from 'wot-design-uni'
import { useRouter } from '@/plugin/uni-mini-router'
import { ref, onMounted, computed,reactive } from 'vue'
import OnlineImage from '@/components/online/view/online-image.vue'
import OnlineFile from '@/components/online/view/online-file.vue'
import OnlineFileCustom from '@/components/online/view/online-file-custom.vue'
import OnlineSelect from '@/components/online/view/online-select.vue'
import OnlineTime from '@/components/online/view/online-time.vue'
import OnlineDate from '@/components/online/view/online-date.vue'
import OnlineRadio from '@/components/online/view/online-radio.vue'
import OnlineCheckbox from '@/components/online/view/online-checkbox.vue'
import OnlineMulti from '@/components/online/view/online-multi.vue'
import OnlinePopupLinkRecord from '@/components/online/view/online-popup-link-record.vue'
import OnlinePca from '@/components/online/view/online-pca.vue'
import SelectDept from '@/components/SelectDept/SelectDept.vue'
import SelectUser from '@/components/SelectUser/SelectUser.vue'
import {duplicateCheck} from "@/service/api";
defineOptions({
  name: 'YoucaiDiseaseWarningsForm',
  options: {
    styleIsolation: 'shared',
  },
})
const toast = useToast()
const router = useRouter()
const form = ref(null)
// 定义响应式数据
const myFormData = reactive({})
const loading = ref(false)
const navTitle = ref('新增')
const dataId = ref('')
const backRouteName = ref('YoucaiDiseaseWarningsList')
// 定义 initForm 方法
const initForm = (item) => {
  console.log('initForm item', item)
  if(item?.dataId){
    dataId.value = item.dataId;
    navTitle.value = item.dataId?'编辑':'新增';
    initData();
  }
}
// 初始化数据
const initData = () => {
  http.get("/youcai/youcaiDiseaseWarnings/queryById",{id:dataId.value}).then((res) => {
    if (res.success) {
      let obj = res.result
      Object.assign(myFormData, { ...obj })
    }else{
      toast.error(res?.message || '表单加载失败！')
    }
  })
}
const handleSuccess = () => {
  uni.$emit('refreshList');
  router.back()
}
/**
 * 校验唯一
 * @param values
 * @returns {boolean}
 */
async function fieldCheck(values: any) {
   const onlyField = [
   ];
   for (const field of onlyField) {
      if (values[field]) {
          // 仅校验有值的字段
          const res: any = await duplicateCheck({
              tableName: 'youcai_disease_warnings',
              fieldName: field,  // 使用处理后的字段名
              fieldVal: values[field],
              dataId: values.id,
          });
          if (!res.success) {
              toast.warning(res.message);
              return true;  // 校验失败
          }
      }
   }
   return false;  // 校验通过
}
// 提交表单
const handleSubmit = async () => {
 // 判断字段必填和正则
  if (await fieldCheck(myFormData)) {
    return
  }
  let url = dataId.value?'/youcai/youcaiDiseaseWarnings/edit':'/youcai/youcaiDiseaseWarnings/add';
  form.value
    .validate()
    .then(({ valid, errors }) => {
      if (valid) {
        loading.value = true;
        http.post(url,myFormData).then((res) => {
          loading.value = false;
          if (res.success) {
            toast.success('保存成功');
            handleSuccess()
          }else{
            toast.error(res?.message || '表单保存失败！')
          }
        })
      }
    })
    .catch((error) => {
      console.log(error, 'error')
      loading.value = false;
    })
}
// 标题
const get4Label = computed(() => {
  return (label) => {
    return label && label.length > 4 ? label.substring(0, 4) : label;
  }
})

// 标题
const getFormSchema = computed(() => {
  return (dictTable,dictCode,dictText) => {
    return {
      dictCode,
      dictTable,
      dictText
    };
  }
})
/**
 * 获取日期控件的扩展类型
 * @param picker
 * @returns {string}
 */
const getDateExtendType = (picker: string) => {
    let mapField = {
      month: 'year-month',
      year: 'year',
      quarter: 'quarter',
      week: 'week',
      day: 'date',
    }
    return picker && mapField[picker]
      ? mapField[picker]
      : 'date'
}
//设置pop返回值
const setFieldsValue = (data) => {
   Object.assign(myFormData, {...data })
}
// onLoad 生命周期钩子
onLoad((option) => {
  initForm(option)
})
</script>

<style lang="scss" scoped>
.footer {
  width: 100%;
  padding: 10px 20px;
  padding-bottom: calc(constant(safe-area-inset-bottom) + 10px);
  padding-bottom: calc(env(safe-area-inset-bottom) + 10px);
}
:deep(.wd-cell__label) {
  font-size: 14px;
  color: #444;
}
:deep(.wd-cell__value) {
  text-align: left;
}
</style>
