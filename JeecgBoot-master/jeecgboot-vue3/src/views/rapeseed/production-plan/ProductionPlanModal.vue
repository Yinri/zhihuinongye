<template>
  <BasicModal
    v-bind="$attrs"
    :title="getTitle"
    :defaultFullscreen="false"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form';
  import { formSchema } from './data';

  const emit = defineEmits(['success', 'register']);

  const isUpdate = ref(true);
  const rowId = ref('');

  const [registerForm, { setFieldsValue, resetFields, validate }] = useForm({
    labelWidth: 100,
    schemas: formSchema,
    showActionButtonGroup: false,
    baseColProps: { span: 24 },
  });

  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    resetFields();
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;

    if (unref(isUpdate)) {
      rowId.value = data.record.id;
      setFieldsValue({
        ...data.record,
      });
    }
  });

  const getTitle = computed(() => (!unref(isUpdate) ? '新增生产计划' : '编辑生产计划'));

  async function handleSubmit() {
    try {
      const values = await validate();
      setModalProps({ confirmLoading: true });
      
      // TODO: 调用API保存数据
      if (unref(isUpdate)) {
        // await updateProductionPlan({ ...values, id: rowId.value });
      } else {
        // await saveProductionPlan(values);
      }
      
      closeModal();
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
  
  function handleCancel() {
    closeModal();
  }
</script>