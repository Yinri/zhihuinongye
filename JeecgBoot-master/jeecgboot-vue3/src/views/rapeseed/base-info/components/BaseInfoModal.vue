<template>
  <BasicModal v-bind="$attrs" :title="getTitle" @ok="handleSubmit" @register="registerModal" :width="800">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref, computed, unref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicForm, useForm } from '/@/components/Form';
import { formSchema } from '../baseInfo.data';
import { saveBaseInfo, editBaseInfo } from '../baseInfo.api';

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
  await resetFields();
  setModalProps({ confirmLoading: false });
  isUpdate.value = !!data?.isUpdate;
  if (unref(isUpdate)) {
    rowId.value = data.record.id;
    await setFieldsValue({ ...data.record });
  }
});

const getTitle = computed(() => (!unref(isUpdate) ? '新增基地信息' : '编辑基地信息'));

async function handleSubmit() {
  try {
    const values = await validate();
    setModalProps({ confirmLoading: true });
    if (unref(isUpdate)) {
      await editBaseInfo({ ...values, id: rowId.value });
    } else {
      await saveBaseInfo(values);
    }
    closeModal();
    emit('success');
  } finally {
    setModalProps({ confirmLoading: false });
  }
}
</script>

