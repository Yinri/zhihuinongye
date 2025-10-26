<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="YoucaiDiseaseWarningsForm">
          <a-row>
						<a-col :span="24">
							<a-form-item label="地块ID" v-bind="validateInfos.plotId" id="YoucaiDiseaseWarningsForm-plotId" name="plotId">
								<a-input-number v-model:value="formData.plotId" placeholder="请输入地块ID" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="预警日期" v-bind="validateInfos.warningDate" id="YoucaiDiseaseWarningsForm-warningDate" name="warningDate">
								<a-date-picker placeholder="请选择预警日期"  v-model:value="formData.warningDate" value-format="YYYY-MM-DD"  style="width: 100%"  allow-clear />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="病害类型" v-bind="validateInfos.diseaseType" id="YoucaiDiseaseWarningsForm-diseaseType" name="diseaseType">
								<a-input v-model:value="formData.diseaseType" placeholder="请输入病害类型"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="病害名称" v-bind="validateInfos.diseaseName" id="YoucaiDiseaseWarningsForm-diseaseName" name="diseaseName">
								<a-input v-model:value="formData.diseaseName" placeholder="请输入病害名称"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="严重程度" v-bind="validateInfos.severity" id="YoucaiDiseaseWarningsForm-severity" name="severity">
								<a-input v-model:value="formData.severity" placeholder="请输入严重程度"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="症状描述" v-bind="validateInfos.symptoms" id="YoucaiDiseaseWarningsForm-symptoms" name="symptoms">
								<a-textarea v-model:value="formData.symptoms" :rows="4" placeholder="请输入症状描述" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="防治建议" v-bind="validateInfos.recommendation" id="YoucaiDiseaseWarningsForm-recommendation" name="recommendation">
								<a-textarea v-model:value="formData.recommendation" :rows="4" placeholder="请输入防治建议" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="预警状态" v-bind="validateInfos.warningStatus" id="YoucaiDiseaseWarningsForm-warningStatus" name="warningStatus">
								<a-input v-model:value="formData.warningStatus" placeholder="请输入预警状态"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="病害影像" v-bind="validateInfos.imageUrl" id="YoucaiDiseaseWarningsForm-imageUrl" name="imageUrl">
								<a-input v-model:value="formData.imageUrl" placeholder="请输入病害影像"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
          </a-row>
        </a-form>
      </template>
    </JFormContainer>
  </a-spin>
</template>

<script lang="ts" setup>
  import { ref, reactive, defineExpose, nextTick, defineProps, computed, onMounted } from 'vue';
  import { defHttp } from '/@/utils/http/axios';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getDateByPicker, getValueType } from '/@/utils';
  import { saveOrUpdate } from '../YoucaiDiseaseWarnings.api';
  import { Form } from 'ant-design-vue';
  import JFormContainer from '/@/components/Form/src/container/JFormContainer.vue';
  const props = defineProps({
    formDisabled: { type: Boolean, default: false },
    formData: { type: Object, default: () => ({})},
    formBpm: { type: Boolean, default: true }
  });
  const formRef = ref();
  const useForm = Form.useForm;
  const emit = defineEmits(['register', 'ok']);
  const formData = reactive<Record<string, any>>({
    id: '',
    plotId: undefined,
    warningDate: '',   
    diseaseType: '',   
    diseaseName: '',   
    severity: '',   
    symptoms: '',   
    recommendation: '',   
    warningStatus: '',   
    imageUrl: '',   
    delFlag: undefined,
  });
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  //表单验证
  const validatorRules = reactive({
    plotId: [{ required: true, message: '请输入地块ID!'},],
    warningDate: [{ required: true, message: '请输入预警日期!'},],
    diseaseType: [{ required: true, message: '请输入病害类型!'},],
  });
  const { resetFields, validate, validateInfos } = useForm(formData, validatorRules, { immediate: false });
  //日期个性化选择
  const fieldPickers = reactive({
  });

  // 表单禁用
  const disabled = computed(()=>{
    if(props.formBpm === true){
      if(props.formData.disabled === false){
        return false;
      }else{
        return true;
      }
    }
    return props.formDisabled;
  });

  
  /**
   * 新增
   */
  function add() {
    edit({});
  }

  /**
   * 编辑
   */
  function edit(record) {
    nextTick(() => {
      resetFields();
      const tmpData = {};
      Object.keys(formData).forEach((key) => {
        if(record.hasOwnProperty(key)){
          tmpData[key] = record[key]
        }
      })
      //赋值
      Object.assign(formData, tmpData);
    });
  }

  /**
   * 提交数据
   */
  async function submitForm() {
    try {
      // 触发表单验证
      await validate();
    } catch ({ errorFields }) {
      if (errorFields) {
        const firstField = errorFields[0];
        if (firstField) {
          formRef.value.scrollToField(firstField.name, { behavior: 'smooth', block: 'center' });
        }
      }
      return Promise.reject(errorFields);
    }
    confirmLoading.value = true;
    const isUpdate = ref<boolean>(false);
    //时间格式化
    let model = formData;
    if (model.id) {
      isUpdate.value = true;
    }
    //循环数据
    for (let data in model) {
      // 更新个性化日期选择器的值
      model[data] = getDateByPicker(model[data], fieldPickers[data]);
      //如果该数据是数组并且是字符串类型
      if (model[data] instanceof Array) {
        let valueType = getValueType(formRef.value.getProps, data);
        //如果是字符串类型的需要变成以逗号分割的字符串
        if (valueType === 'string') {
          model[data] = model[data].join(',');
        }
      }
    }
    await saveOrUpdate(model, isUpdate.value)
      .then((res) => {
        if (res.success) {
          createMessage.success(res.message);
          emit('ok');
        } else {
          createMessage.warning(res.message);
        }
      })
      .finally(() => {
        confirmLoading.value = false;
      });
  }


  defineExpose({
    add,
    edit,
    submitForm,
  });
</script>

<style lang="less" scoped>
  .antd-modal-form {
    padding: 14px;
  }
</style>
