<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="YoucaiPesticideInputsForm">
          <a-row>
						<a-col :span="24">
							<a-form-item label="生产计划ID" v-bind="validateInfos.planId" id="YoucaiPesticideInputsForm-planId" name="planId">
								<a-input-number v-model:value="formData.planId" placeholder="请输入生产计划ID" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="农药名称" v-bind="validateInfos.pesticideName" id="YoucaiPesticideInputsForm-pesticideName" name="pesticideName">
								<a-input v-model:value="formData.pesticideName" placeholder="请输入农药名称"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="农药类型" v-bind="validateInfos.pesticideType" id="YoucaiPesticideInputsForm-pesticideType" name="pesticideType">
								<a-input v-model:value="formData.pesticideType" placeholder="请输入农药类型"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="防治对象" v-bind="validateInfos.targetPest" id="YoucaiPesticideInputsForm-targetPest" name="targetPest">
								<a-input v-model:value="formData.targetPest" placeholder="请输入防治对象"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="使用量(公斤/升)" v-bind="validateInfos.amount" id="YoucaiPesticideInputsForm-amount" name="amount">
								<a-input-number v-model:value="formData.amount" placeholder="请输入使用量(公斤/升)" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="成本(元)" v-bind="validateInfos.cost" id="YoucaiPesticideInputsForm-cost" name="cost">
								<a-input-number v-model:value="formData.cost" placeholder="请输入成本(元)" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="施药日期" v-bind="validateInfos.applicationDate" id="YoucaiPesticideInputsForm-applicationDate" name="applicationDate">
								<a-date-picker placeholder="请选择施药日期"  v-model:value="formData.applicationDate" value-format="YYYY-MM-DD"  style="width: 100%"  allow-clear />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="施药方式" v-bind="validateInfos.applicationMethod" id="YoucaiPesticideInputsForm-applicationMethod" name="applicationMethod">
								<a-input v-model:value="formData.applicationMethod" placeholder="请输入施药方式"  allow-clear ></a-input>
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
  import { saveOrUpdate } from '../YoucaiPesticideInputs.api';
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
    planId: undefined,
    pesticideName: '',   
    pesticideType: '',   
    targetPest: '',   
    amount: undefined,
    cost: undefined,
    applicationDate: '',   
    applicationMethod: '',   
    delFlag: undefined,
  });
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  //表单验证
  const validatorRules = reactive({
    planId: [{ required: true, message: '请输入生产计划ID!'},],
    pesticideName: [{ required: true, message: '请输入农药名称!'},],
    pesticideType: [{ required: true, message: '请输入农药类型!'},],
    amount: [{ required: true, message: '请输入使用量(公斤/升)!'},],
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
