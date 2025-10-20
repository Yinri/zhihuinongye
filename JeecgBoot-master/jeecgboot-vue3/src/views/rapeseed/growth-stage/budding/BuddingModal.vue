<template>
  <BasicModal
    v-bind="$attrs"
    :title="getTitle"
    @register="registerModal"
    @ok="handleSubmit"
    :width="800"
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
    // 重置表单
    await resetFields();
    setModalProps({ confirmLoading: false });

    isUpdate.value = !!data?.isUpdate;

    if (unref(isUpdate)) {
      rowId.value = data.record.id;
      await setFieldsValue({
        ...data.record,
      });
    }
  });

  const getTitle = computed(() => (!unref(isUpdate) ? '新增蕾薹期管理' : '编辑蕾薹期管理'));

  async function handleSubmit() {
    try {
      const values = await validate();

      setModalProps({ confirmLoading: true });

      // TODO: 此处调用API
      if (unref(isUpdate)) {
        // await updateBudding({ ...values, id: rowId });
      } else {
        // await saveBudding(values);
      }

      closeModal();
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>