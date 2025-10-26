<template>
  <BasicModal
    v-bind="$attrs"
    :title="getTitle"
    @ok="handleSubmit"
    @register="registerModal"
    :width="800"
  >
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form';
  import { formSchema } from './diseaseControl.data';
  import { saveDiseaseControl, editDiseaseControl } from './diseaseControl.api';

  const emit = defineEmits(['success', 'register']);
  const isUpdate = ref(true);
  const rowId = ref('');

  const [registerForm, { setFieldsValue, resetFields, validate }] = useForm({
    labelWidth: 100,
    schemas: formSchema,
    showActionButtonGroup: false,
    baseColProps: { span: 12 },
  });

  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    // 重置表单
    await resetFields();
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;

    if (unref(isUpdate)) {
      // 赋值
      rowId.value = data.record.id;
      await setFieldsValue({
        ...data.record,
      });
    }
  });

  const getTitle = computed(() => (!unref(isUpdate) ? '新增病害防控记录' : '编辑病害防控记录'));

  async function handleSubmit() {
    try {
      const values = await validate();
      setModalProps({ confirmLoading: true });

      if (unref(isUpdate)) {
        await editDiseaseControl({ ...values, id: rowId.value });
      } else {
        await saveDiseaseControl(values);
      }

      closeModal();
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>