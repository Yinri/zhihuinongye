<template>
  <BasicModal
    v-bind="$attrs"
    :title="title"
    :loading="loading"
    :width="800"
    @ok="handleSubmit"
    @register="registerModal"
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
const loading = ref(false);
const isUpdate = ref(true);

const [registerForm, { setFieldsValue, resetFields, validate }] = useForm({
  labelWidth: 100,
  schemas: formSchema,
  showActionButtonGroup: false,
});

const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
  resetFields();
  setModalProps({ confirmLoading: false });
  isUpdate.value = !!data?.isUpdate;

  if (unref(isUpdate)) {
    setFieldsValue({
      ...data.record,
    });
  }
});

const title = computed(() => (!unref(isUpdate) ? '新增播种记录' : '编辑播种记录'));

async function handleSubmit() {
  try {
    const values = await validate();
    setModalProps({ confirmLoading: true });
    loading.value = true;

    // TODO: 此处调用API保存数据
    console.log(values);

    closeModal();
    emit('success');
  } finally {
    loading.value = false;
    setModalProps({ confirmLoading: false });
  }
}
</script>