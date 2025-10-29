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

