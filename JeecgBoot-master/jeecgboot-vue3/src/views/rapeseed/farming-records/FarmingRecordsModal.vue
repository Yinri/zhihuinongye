<template>
  <BasicModal
    v-bind="$attrs"
    :title="getTitle"
    @ok="handleSubmit"
    @register="registerModal"
    :width="900"
  >
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form';
  import { formSchema } from './farmingRecords.data';
  import { saveFarmingRecords, editFarmingRecords } from './farmingRecords.api';
  import { useSelectStore } from '/@/store/selectStore';

  const emit = defineEmits(['success', 'register']);
  const isUpdate = ref(true);
  const rowId = ref('');
  
  // 获取基地选择的store
  const selectStore = useSelectStore();

  const [registerForm, { setFieldsValue, resetFields, validate }] = useForm({
    labelWidth: 100,
    schemas: formSchema,
    showActionButtonGroup: false,
    baseColProps: { span: 12 },
    actionColOptions: {
      span: 24,
    },
  });

  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    await resetFields();
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;

    // 新增时预填 baseId
    const baseId = selectStore.selectedBase.baseId;

    if (unref(isUpdate)) {
      rowId.value = data.record.id;
      await setFieldsValue({ ...data.record });
    } else if (baseId) {
      await setFieldsValue({ baseId });
    }
  });

  const getTitle = computed(() => (!unref(isUpdate) ? '新增农事记录' : '编辑农事记录'));

  async function handleSubmit() {
    try {
      const values = await validate();
      setModalProps({ confirmLoading: true });
      
      // 兜底：确保 baseId 已设置
      if (!values.baseId) {
        const baseId = selectStore.selectedBase.baseId;
        if (baseId) {
          values.baseId = baseId;
        }
      }

      if (unref(isUpdate)) {
        await editFarmingRecords({ ...values, id: rowId.value });
      } else {
        await saveFarmingRecords(values);
      }

      closeModal();
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
