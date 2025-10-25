<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="YoucaiPestControlForm">
          <a-row>
						<a-col :span="24">
							<a-form-item label="地块ID" v-bind="validateInfos.plotId" id="YoucaiPestControlForm-plotId" name="plotId">
								<a-input-number v-model:value="formData.plotId" placeholder="请输入地块ID" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="生产计划ID" v-bind="validateInfos.planId" id="YoucaiPestControlForm-planId" name="planId">
								<a-input-number v-model:value="formData.planId" placeholder="请输入生产计划ID" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="关联预警ID" v-bind="validateInfos.warningId" id="YoucaiPestControlForm-warningId" name="warningId">
								<a-input-number v-model:value="formData.warningId" placeholder="请输入关联预警ID" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="虫害类型" v-bind="validateInfos.pestType" id="YoucaiPestControlForm-pestType" name="pestType">
								<a-input v-model:value="formData.pestType" placeholder="请输入虫害类型"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="虫害名称" v-bind="validateInfos.pestName" id="YoucaiPestControlForm-pestName" name="pestName">
								<a-input v-model:value="formData.pestName" placeholder="请输入虫害名称"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="防控日期" v-bind="validateInfos.controlDate" id="YoucaiPestControlForm-controlDate" name="controlDate">
								<a-date-picker placeholder="请选择防控日期"  v-model:value="formData.controlDate" value-format="YYYY-MM-DD"  style="width: 100%"  allow-clear />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="防控方法" v-bind="validateInfos.controlMethod" id="YoucaiPestControlForm-controlMethod" name="controlMethod">
								<a-input v-model:value="formData.controlMethod" placeholder="请输入防控方法"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="农药名称" v-bind="validateInfos.pesticideName" id="YoucaiPestControlForm-pesticideName" name="pesticideName">
								<a-input v-model:value="formData.pesticideName" placeholder="请输入农药名称"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="农药类型" v-bind="validateInfos.pesticideType" id="YoucaiPestControlForm-pesticideType" name="pesticideType">
								<a-input v-model:value="formData.pesticideType" placeholder="请输入农药类型"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="农药用量(毫升/亩)" v-bind="validateInfos.pesticideDosage" id="YoucaiPestControlForm-pesticideDosage" name="pesticideDosage">
								<a-input-number v-model:value="formData.pesticideDosage" placeholder="请输入农药用量(毫升/亩)" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="农药浓度(%)" v-bind="validateInfos.pesticideConcentration" id="YoucaiPestControlForm-pesticideConcentration" name="pesticideConcentration">
								<a-input-number v-model:value="formData.pesticideConcentration" placeholder="请输入农药浓度(%)" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="施用方法" v-bind="validateInfos.applicationMethod" id="YoucaiPestControlForm-applicationMethod" name="applicationMethod">
								<a-input v-model:value="formData.applicationMethod" placeholder="请输入施用方法"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="防控面积(亩)" v-bind="validateInfos.controlArea" id="YoucaiPestControlForm-controlArea" name="controlArea">
								<a-input-number v-model:value="formData.controlArea" placeholder="请输入防控面积(亩)" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="防控成本(元)" v-bind="validateInfos.controlCost" id="YoucaiPestControlForm-controlCost" name="controlCost">
								<a-input-number v-model:value="formData.controlCost" placeholder="请输入防控成本(元)" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="防控效果" v-bind="validateInfos.controlEffectiveness" id="YoucaiPestControlForm-controlEffectiveness" name="controlEffectiveness">
								<a-input v-model:value="formData.controlEffectiveness" placeholder="请输入防控效果"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="操作员" v-bind="validateInfos.operator" id="YoucaiPestControlForm-operator" name="operator">
								<a-input v-model:value="formData.operator" placeholder="请输入操作员"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="备注" v-bind="validateInfos.notes" id="YoucaiPestControlForm-notes" name="notes">
								<a-textarea v-model:value="formData.notes" :rows="4" placeholder="请输入备注" />
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
  import { saveOrUpdate } from '../YoucaiPestControl.api';
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
    warningId: undefined,
    pestType: '',   
    pestName: '',   
    controlDate: '',   
    controlMethod: '',   
    pesticideName: '',   
    pesticideType: '',   
    pesticideDosage: undefined,
    pesticideConcentration: undefined,
    applicationMethod: '',   
    controlArea: undefined,
    controlCost: undefined,
    controlEffectiveness: '',   
    operator: '',   
    notes: '',   
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
    pestType: [{ required: true, message: '请输入虫害类型!'},],
    controlDate: [{ required: true, message: '请输入防控日期!'},],
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
