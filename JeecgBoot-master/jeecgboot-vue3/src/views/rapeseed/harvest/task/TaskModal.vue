<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="getTitle" @ok="handleSubmit">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref, computed } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicForm, useForm } from '/@/components/Form';
import { formSchema } from './task.data';
import { saveTask, editTask } from './task.api';
import { getMachineList } from '../machine/machine.api';
import { useSelectStore } from '/@/store/selectStore';
import { storeToRefs } from 'pinia';

const emit = defineEmits(['success', 'register']);
const isUpdate = ref(false);

const [registerForm, { resetFields, setFieldsValue, validate, updateSchema }] = useForm({
  labelWidth: 100,
  schemas: formSchema,
  showActionButtonGroup: false,
});

const selectStore = useSelectStore();
const { selectedBase } = storeToRefs(selectStore);

async function loadMachineOptions() {
  try {
    // 仅加载当前基地的农机
    const baseId = selectedBase.value?.baseId;
    const query: any = { pageNo: 1, pageSize: 1000 };
    if (baseId) query.baseId = baseId;
    const res = await getMachineList(query);
    const records = res?.records ?? res?.data?.records ?? [];
    const options = records.map((m: any) => ({ label: m.machineName, value: m.id }));
    await updateSchema({ field: 'assignedMachineId', componentProps: { options } });
  } catch (e) {
    // ignore
  }
}

const [registerModal, { setModalProps }] = useModalInner(async (data) => {
  isUpdate.value = !!data?.isUpdate;
  setModalProps({ confirmLoading: false });
  resetFields();
  await loadMachineOptions();
  if (data?.record) {
    setFieldsValue({ ...data.record });
  }
});

const getTitle = computed(() => (isUpdate.value ? '编辑收获任务' : '新增收获任务'));

async function handleSubmit() {
  const values = await validate();
  try {
    if (isUpdate.value) {
      await editTask(values);
    } else {
      await saveTask(values);
    }
    emit('success');
  } catch (e) {
    // ignore
  }
}
</script>