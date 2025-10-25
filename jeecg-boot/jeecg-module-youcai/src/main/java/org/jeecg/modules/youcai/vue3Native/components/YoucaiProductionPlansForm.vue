<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="YoucaiProductionPlansForm">
          <a-row>
						<a-col :span="24">
							<a-form-item label="地块ID" v-bind="validateInfos.plotId" id="YoucaiProductionPlansForm-plotId" name="plotId">
								<a-input-number v-model:value="formData.plotId" placeholder="请输入地块ID" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="计划年份" v-bind="validateInfos.planYear" id="YoucaiProductionPlansForm-planYear" name="planYear">
								<a-input v-model:value="formData.planYear" placeholder="请输入计划年份"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="油菜品种ID" v-bind="validateInfos.varietyId" id="YoucaiProductionPlansForm-varietyId" name="varietyId">
								<a-input-number v-model:value="formData.varietyId" placeholder="请输入油菜品种ID" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="目标产量(公斤/亩)" v-bind="validateInfos.targetYield" id="YoucaiProductionPlansForm-targetYield" name="targetYield">
								<a-input-number v-model:value="formData.targetYield" placeholder="请输入目标产量(公斤/亩)" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="种植面积(亩)" v-bind="validateInfos.plantingArea" id="YoucaiProductionPlansForm-plantingArea" name="plantingArea">
								<a-input-number v-model:value="formData.plantingArea" placeholder="请输入种植面积(亩)" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="计划播种日期" v-bind="validateInfos.plannedSowingDate" id="YoucaiProductionPlansForm-plannedSowingDate" name="plannedSowingDate">
								<a-date-picker placeholder="请选择计划播种日期"  v-model:value="formData.plannedSowingDate" value-format="YYYY-MM-DD"  style="width: 100%"  allow-clear />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="计划收获日期" v-bind="validateInfos.plannedHarvestDate" id="YoucaiProductionPlansForm-plannedHarvestDate" name="plannedHarvestDate">
								<a-date-picker placeholder="请选择计划收获日期"  v-model:value="formData.plannedHarvestDate" value-format="YYYY-MM-DD"  style="width: 100%"  allow-clear />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="计划状态" v-bind="validateInfos.status" id="YoucaiProductionPlansForm-status" name="status">
								<a-input v-model:value="formData.status" placeholder="请输入计划状态"  allow-clear ></a-input>
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
  import { saveOrUpdate } from '../YoucaiProductionPlans.api';
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
    planYear: '',   
    varietyId: undefined,
    targetYield: undefined,
    plantingArea: undefined,
    plannedSowingDate: '',   
    plannedHarvestDate: '',   
    status: '',   
    delFlag: undefined,
  });
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  //表单验证
  const validatorRules = reactive({
    plotId: [{ required: true, message: '请输入地块ID!'},],
    planYear: [{ required: true, message: '请输入计划年份!'},],
    varietyId: [{ required: true, message: '请输入油菜品种ID!'},],
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
