<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="YoucaiSoilFertilityForm">
          <a-row>
						<a-col :span="24">
							<a-form-item label="地块ID" v-bind="validateInfos.plotId" id="YoucaiSoilFertilityForm-plotId" name="plotId">
								<a-input-number v-model:value="formData.plotId" placeholder="请输入地块ID" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="检测日期" v-bind="validateInfos.testDate" id="YoucaiSoilFertilityForm-testDate" name="testDate">
								<a-date-picker placeholder="请选择检测日期"  v-model:value="formData.testDate" value-format="YYYY-MM-DD"  style="width: 100%"  allow-clear />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="pH值" v-bind="validateInfos.phValue" id="YoucaiSoilFertilityForm-phValue" name="phValue">
								<a-input-number v-model:value="formData.phValue" placeholder="请输入pH值" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="有机质含量(g/kg)" v-bind="validateInfos.organicMatter" id="YoucaiSoilFertilityForm-organicMatter" name="organicMatter">
								<a-input-number v-model:value="formData.organicMatter" placeholder="请输入有机质含量(g/kg)" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="氮含量(mg/kg)" v-bind="validateInfos.nitrogen" id="YoucaiSoilFertilityForm-nitrogen" name="nitrogen">
								<a-input-number v-model:value="formData.nitrogen" placeholder="请输入氮含量(mg/kg)" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="磷含量(mg/kg)" v-bind="validateInfos.phosphorus" id="YoucaiSoilFertilityForm-phosphorus" name="phosphorus">
								<a-input-number v-model:value="formData.phosphorus" placeholder="请输入磷含量(mg/kg)" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="钾含量(mg/kg)" v-bind="validateInfos.potassium" id="YoucaiSoilFertilityForm-potassium" name="potassium">
								<a-input-number v-model:value="formData.potassium" placeholder="请输入钾含量(mg/kg)" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="肥力等级" v-bind="validateInfos.fertilityLevel" id="YoucaiSoilFertilityForm-fertilityLevel" name="fertilityLevel">
								<a-input v-model:value="formData.fertilityLevel" placeholder="请输入肥力等级"  allow-clear ></a-input>
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
  import { saveOrUpdate } from '../YoucaiSoilFertility.api';
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
    testDate: '',   
    phValue: undefined,
    organicMatter: undefined,
    nitrogen: undefined,
    phosphorus: undefined,
    potassium: undefined,
    fertilityLevel: '',   
    delFlag: undefined,
  });
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  //表单验证
  const validatorRules = reactive({
    plotId: [{ required: true, message: '请输入地块ID!'},],
    testDate: [{ required: true, message: '请输入检测日期!'},],
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
