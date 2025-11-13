<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="getTitle" @ok="handleSubmit" @cancel="handleCancel" :width="800">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form';
  import { formSchema } from './plotInfo.data';
  import { savePlotInfo, editPlotInfo } from './plotInfo.api';
  import { useMessage } from '/@/hooks/web/useMessage';

  const emit = defineEmits(['success', 'register', 'cancel']);
  const isUpdate = ref(true);
  const rowId = ref('');
  const { createMessage } = useMessage();

  //表单配置
  const [registerForm, { setFieldsValue, setProps, resetFields, validate }] = useForm({
    labelWidth: 120,
    schemas: formSchema,
    showActionButtonGroup: false,
    baseColProps: { span: 24 },
  });

  //表单赋值
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    //重置表单
    await resetFields();
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;

    if (unref(isUpdate)) {
      //表单赋值
      await setFieldsValue({
        ...data.record,
      });
      rowId.value = data.record.id;
    } else {
      //新增时重置表单
      setFieldsValue({
        growthStage: '未播种',
      });
      
      // 如果有传入的多边形坐标数据，设置到表单中
      if (data?.polygonCoords) {
        await setFieldsValue({
          polygonCoords: JSON.stringify(data.polygonCoords),
        });
      }
      
      // 如果有传入的基地ID，设置到表单中
      if (data?.baseId) {
        await setFieldsValue({
          baseId: data.baseId,
        });
      }
      
      // 如果有传入的面积值，设置到表单中
      if (data?.area !== undefined) {
        await setFieldsValue({
          area: data.area,
        });
      }
    }
    // 隐藏底部时禁用表单
    setProps({ disabled: !data?.showFooter });
  });

  //设置标题
  const getTitle = computed(() => (!unref(isUpdate) ? '新增地块信息' : '编辑地块信息'));

  /**
   * 表单提交事件
   */
  async function handleSubmit() {
    try {
      const values = await validate();
      setModalProps({ confirmLoading: true });
      
      // 处理多边形坐标数据
      if (values.polygonCoords) {
        try {
          // 如果已经是字符串，不需要再次转换
          if (typeof values.polygonCoords === 'string') {
            // 验证是否为有效的JSON
            JSON.parse(values.polygonCoords);
          } else {
            // 如果是对象，转换为JSON字符串
            values.polygonCoords = JSON.stringify(values.polygonCoords);
          }
        } catch (e) {
          createMessage.error('多边形坐标数据格式错误');
          return;
        }
      }
      
      //提交表单
      await savePlotInfo(values, isUpdate.value);
      //关闭弹窗
      closeModal();
      //刷新列表
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
  
  /**
   * 表单取消事件
   */
  function handleCancel() {
    // 关闭弹窗
    closeModal();
    // 触发取消事件
    emit('cancel');
  }
</script>