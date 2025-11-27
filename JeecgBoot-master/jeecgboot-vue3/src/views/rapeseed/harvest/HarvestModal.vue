<template>
  <BasicModal
    v-bind="$attrs"
    :title="getTitle"
    @ok="handleSubmit"
    @register="registerModal"
    :width="500"
  >
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form';
  import { formSchema } from './harvest.data';
  import { saveHarvest, editHarvest } from './harvest.api';
  import { useSelectStore } from '/@/store/selectStore';
  import { storeToRefs } from 'pinia';
  import { getPlotInfoList } from '../plot-info/plotInfo.api';
  import cloneDeep from 'lodash-es/cloneDeep';
  const emit = defineEmits(['success', 'register']);
  const isUpdate = ref(true);
  const rowId = ref('');

  // 获取当前选中的基地
  const selectStore = useSelectStore();
  const { selectedBase } = storeToRefs(selectStore);

  const [registerForm, { setFieldsValue, resetFields, validate, updateSchema }] = useForm({
    labelWidth: 130, // 增加标签宽度
    schemas: formSchema,
    showActionButtonGroup: false,
    baseColProps: { span: 24 }, // 修改为单列布局
  });

  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    // 重置表单
    await resetFields();
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;

    // 根据是否编辑模式动态更新表单配置
    const dynamicSchema = cloneDeep(formSchema);
    
    if (unref(isUpdate)) {
      // 编辑模式：隐藏基地ID和地块ID字段
      dynamicSchema.forEach(item => {
        if (item.field === 'baseId' || item.field === 'plotId') {
          item.show = false;
        }
      });
      
      // 赋值
      rowId.value = data.record.id;
      await setFieldsValue({
        ...data.record,
      });
    } else {
      // 新增模式：显示地块选择器，隐藏基地ID
      dynamicSchema.forEach(item => {
        if (item.field === 'baseId') {
          item.show = false; // 隐藏基地ID，但会自动填充
        } else if (item.field === 'plotId') {
          item.show = true; // 显示地块选择器
        }
      });
      
      // 新增时自动填充当前选中的基地ID和加载地块列表
      if (selectedBase.value?.baseId) {
        // 加载当前基地下的地块列表
        try {
          const plotRes = await getPlotInfoList({ baseId: selectedBase.value.baseId });
          const plotOptions = plotRes?.records || [];
          
          // 更新表单中的地块选项
          await setFieldsValue({
            baseId: selectedBase.value.baseId,
          });
          
          // 更新plotId字段的选项
          const plotIdSchema = dynamicSchema.find(item => item.field === 'plotId');
          if (plotIdSchema && plotIdSchema.componentProps) {
            plotIdSchema.componentProps.options = plotOptions;
          }
        } catch (error) {
          console.error('加载地块列表失败:', error);
        }
      }
    }
    
    // 更新表单配置
    await updateSchema(dynamicSchema);
  });

  const getTitle = computed(() => (!unref(isUpdate) ? '新增收获记录' : '编辑收获记录'));

  async function handleSubmit() {
      try {
        const values = await validate();
        setModalProps({ confirmLoading: true });
        
        // 根据是否编辑模式决定提交的数据
        let submitData = { ...values };
        
        if (unref(isUpdate)) {
          // 编辑模式：不提交基地ID和地块ID
          delete submitData.baseId;
          delete submitData.plotId;
          await editHarvest({ ...submitData, id: rowId.value });
        } else {
          // 新增模式：提交所有数据
          await saveHarvest(submitData);
        }

        closeModal();
        emit('success');
      } finally {
        setModalProps({ confirmLoading: false });
      }
    }
</script>
