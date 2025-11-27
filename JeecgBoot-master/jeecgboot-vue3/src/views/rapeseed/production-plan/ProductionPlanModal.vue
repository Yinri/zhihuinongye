<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="getTitle" @ok="handleSubmit" :width="900">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref, computed, unref, watch } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicForm, useForm } from '/@/components/Form';
import { formSchema } from './productionPlan.data';
import { saveProductionPlan } from './productionPlan.api';
import { getPlotListByBaseId } from './plot.api';

  const props = defineProps({
    baseId: {
      type: [String, Number],
      default: undefined,
    },
  });

  const emit = defineEmits(['success', 'register']);
  const isUpdate = ref(true);
  const rowId = ref('');
  const currentBaseId = ref(props.baseId);

  //表单配置
  const [registerForm, { setFieldsValue, setProps, resetFields, validate, updateSchema }] = useForm({
    labelWidth: 120,
    schemas: formSchema,
    showActionButtonGroup: false,
    baseColProps: { span: 12 },
  });

  //表单赋值
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    //重置表单
    await resetFields();
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;

    // 设置当前基地ID
    if (data?.baseId) {
      currentBaseId.value = data.baseId;
    } else if (props.baseId) {
      currentBaseId.value = props.baseId;
    }

    if (unref(isUpdate)) {
      //表单赋值
      await setFieldsValue({
        ...data.record,
      });
      rowId.value = data.record.id;
      
      // 如果是编辑模式，使用记录中的基地ID
      if (data.record.baseId) {
        currentBaseId.value = data.record.baseId;
      }
    } else {
      //新增时重置表单
      setFieldsValue({
        planStatus: '0',
        soilFertility: '1',
        baseId: currentBaseId.value,
      });
    }

    // 更新地块选择选项
    await updatePlotOptions();
    
    // 隐藏底部时禁用表单
    setProps({ disabled: !data?.showFooter });
  });

  // 监听基地ID变化
  watch(
    () => currentBaseId.value,
    (newVal) => {
      if (newVal) {
        updatePlotOptions();
        // 更新表单中的基地ID
        if (!isUpdate.value) {
          setFieldsValue({ baseId: newVal });
        }
      }
    }
  );

  const plotLoading = ref(false);
  const plotError = ref('');



  /**
   * 更新地块选择选项
   */
  async function updatePlotOptions() {
    if (!currentBaseId.value) {
      // 如果没有基地ID，清空地块选项
      updateSchema([
        {
          field: 'plotId',
          componentProps: {
            options: [],
            placeholder: '请先选择基地',
          },
        },
      ]);
      plotError.value = '';
      return;
    }

    try {
      plotLoading.value = true;
      plotError.value = '';
      
      // 根据基地ID获取地块列表
      const plotList = await getPlotListByBaseId({ baseId: currentBaseId.value });
      
      if (plotList && plotList.length > 0) {
        const plotOptions = plotList.map((plot) => ({
          label: plot.plotName,
          value: String(plot.id),
        }));

        // 更新地块选择组件的选项
        updateSchema([
          {
            field: 'plotId',
            componentProps: {
              options: plotOptions,
              placeholder: '请选择地块',
            },
          },
        ]);
      } else {
        plotError.value = '该基地下暂无地块数据';
        updateSchema([
          {
            field: 'plotId',
            componentProps: {
              options: [],
              placeholder: '该基地下暂无地块数据',
            },
          },
        ]);
      }
    } catch (error) {
      console.error('获取地块列表失败:', error);
      plotError.value = '获取地块列表失败，请稍后重试';
      updateSchema([
        {
          field: 'plotId',
          componentProps: {
            options: [],
            placeholder: '获取地块列表失败，请稍后重试',
          },
        },
      ]);
    } finally {
      plotLoading.value = false;
    }
  }

  //设置标题
  const getTitle = computed(() => (!unref(isUpdate) ? '新增生产计划' : '编辑生产计划'));

  /**
   * 根据ID获取品种列表
   */
  async function getVarietyList() {
    // 实际项目中替换为真实API
    return Promise.resolve([
      { id: '1', varietyName: '品种1' },
      { id: '2', varietyName: '品种2' },
      { id: '3', varietyName: '品种3' },
    ]);
  }

  /**
   * 表单提交事件
   */
  async function handleSubmit() {
    try {
      const values = await validate();
      setModalProps({ confirmLoading: true });
      
      // 确保基地ID被正确设置
      if (currentBaseId.value) {
        values.baseId = currentBaseId.value;
      }
      
      //提交表单
      await saveProductionPlan(values, isUpdate.value);
      //关闭弹窗
      closeModal();
      //刷新列表
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
