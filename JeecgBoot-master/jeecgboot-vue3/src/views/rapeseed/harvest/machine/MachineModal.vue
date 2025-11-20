<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="getTitle" @ok="handleSubmit">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref, computed } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicForm, useForm } from '/@/components/Form';
import { formSchema } from './machine.data';
import { saveMachine, editMachine } from './machine.api';

const emit = defineEmits(['success', 'register']);
const isUpdate = ref(false);

const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
  labelWidth: 100,
  schemas: formSchema,
  showActionButtonGroup: false,
});

const [registerModal, { setModalProps }] = useModalInner(async (data) => {
  isUpdate.value = !!data?.isUpdate;
  setModalProps({ confirmLoading: false });
  resetFields();
  if (data?.record) {
    setFieldsValue({ ...data.record });
  }
});

const getTitle = computed(() => (isUpdate.value ? '编辑农机档案' : '新增农机档案'));

async function handleSubmit() {
  const values = await validate();
  try {
    if (isUpdate.value) {
      await editMachine(values);
    } else {
      await saveMachine(values);
    }
    emit('success');
  } catch (e) {
    // ignore
  }
}
</script>