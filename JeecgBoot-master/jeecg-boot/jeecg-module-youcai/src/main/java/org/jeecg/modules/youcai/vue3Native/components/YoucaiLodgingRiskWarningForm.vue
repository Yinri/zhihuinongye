<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="YoucaiLodgingRiskWarningForm">
          <a-row>
						<a-col :span="24">
							<a-form-item label="地块ID，外键关联youcai_plots表" v-bind="validateInfos.plotId" id="YoucaiLodgingRiskWarningForm-plotId" name="plotId">
								<a-input-number v-model:value="formData.plotId" placeholder="请输入地块ID，外键关联youcai_plots表" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="风险等级：低 / 中 / 高" v-bind="validateInfos.riskLevel" id="YoucaiLodgingRiskWarningForm-riskLevel" name="riskLevel">
								<a-input v-model:value="formData.riskLevel" placeholder="请输入风险等级：低 / 中 / 高"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="倒伏风险得分，0-1之间的小数" v-bind="validateInfos.riskScore" id="YoucaiLodgingRiskWarningForm-riskScore" name="riskScore">
								<a-input-number v-model:value="formData.riskScore" placeholder="请输入倒伏风险得分，0-1之间的小数" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="建议措施，如"及时排水、加固植株"" v-bind="validateInfos.suggestedMeasures" id="YoucaiLodgingRiskWarningForm-suggestedMeasures" name="suggestedMeasures">
								<a-textarea v-model:value="formData.suggestedMeasures" :rows="4" placeholder="请输入建议措施，如"及时排水、加固植株"" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="预警状态" v-bind="validateInfos.warningStatus" id="YoucaiLodgingRiskWarningForm-warningStatus" name="warningStatus">
								<a-input v-model:value="formData.warningStatus" placeholder="请输入预警状态"  allow-clear ></a-input>
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
  import { saveOrUpdate } from '../YoucaiLodgingRiskWarning.api';
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
    riskLevel: '',   
    riskScore: undefined,
    suggestedMeasures: '',   
    warningStatus: '',   
    delFlag: undefined,
  });
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  //表单验证
  const validatorRules = reactive({
    plotId: [{ required: true, message: '请输入地块ID，外键关联youcai_plots表!'},],
    riskLevel: [{ required: true, message: '请输入风险等级：低 / 中 / 高!'},],
    riskScore: [{ required: true, message: '请输入倒伏风险得分，0-1之间的小数!'},],
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
