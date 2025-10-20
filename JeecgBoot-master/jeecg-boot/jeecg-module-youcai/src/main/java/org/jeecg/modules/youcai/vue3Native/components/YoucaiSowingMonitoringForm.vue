<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="YoucaiSowingMonitoringForm">
          <a-row>
						<a-col :span="24">
							<a-form-item label="地块ID" v-bind="validateInfos.plotId" id="YoucaiSowingMonitoringForm-plotId" name="plotId">
								<a-input-number v-model:value="formData.plotId" placeholder="请输入地块ID" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="生产计划ID" v-bind="validateInfos.planId" id="YoucaiSowingMonitoringForm-planId" name="planId">
								<a-input-number v-model:value="formData.planId" placeholder="请输入生产计划ID" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="播种日期" v-bind="validateInfos.sowingDate" id="YoucaiSowingMonitoringForm-sowingDate" name="sowingDate">
								<a-date-picker placeholder="请选择播种日期"  v-model:value="formData.sowingDate" value-format="YYYY-MM-DD"  style="width: 100%"  allow-clear />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="播种方式" v-bind="validateInfos.sowingMethod" id="YoucaiSowingMonitoringForm-sowingMethod" name="sowingMethod">
								<a-input v-model:value="formData.sowingMethod" placeholder="请输入播种方式"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="播种量(公斤/亩)" v-bind="validateInfos.seedingRate" id="YoucaiSowingMonitoringForm-seedingRate" name="seedingRate">
								<a-input-number v-model:value="formData.seedingRate" placeholder="请输入播种量(公斤/亩)" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="实际播种面积(亩)" v-bind="validateInfos.actualSowingArea" id="YoucaiSowingMonitoringForm-actualSowingArea" name="actualSowingArea">
								<a-input-number v-model:value="formData.actualSowingArea" placeholder="请输入实际播种面积(亩)" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="播种状态" v-bind="validateInfos.sowingStatus" id="YoucaiSowingMonitoringForm-sowingStatus" name="sowingStatus">
								<a-input v-model:value="formData.sowingStatus" placeholder="请输入播种状态"  allow-clear ></a-input>
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
  import { saveOrUpdate } from '../YoucaiSowingMonitoring.api';
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
    planId: undefined,
    sowingDate: '',   
    sowingMethod: '',   
    seedingRate: undefined,
    actualSowingArea: undefined,
    sowingStatus: '',   
    delFlag: undefined,
  });
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  //表单验证
  const validatorRules = reactive({
    plotId: [{ required: true, message: '请输入地块ID!'},],
    planId: [{ required: true, message: '请输入生产计划ID!'},],
    sowingDate: [{ required: true, message: '请输入播种日期!'},],
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
