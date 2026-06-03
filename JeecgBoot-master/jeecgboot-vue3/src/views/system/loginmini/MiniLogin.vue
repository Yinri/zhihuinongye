<template>
  <div :class="prefixCls" class="login-background-img">
    <AppLocalePicker class="absolute top-4 right-4 enter-x xl:text-gray-600" :showText="false"/>
    <AppDarkModeToggle class="absolute top-3 right-7 enter-x" />
    <div class="aui-logo" v-if="!getIsMobile">
      <div>
        <h3>
          <img :src="logoImg" alt="jeecg" />
        </h3>
      </div>
    </div>
    <div v-else class="aui-phone-logo">
      <img :src="logoImg" alt="jeecg" />
    </div>
    <div class="aui-content">
      <div class="aui-container">
        <div class="aui-form">
          <div class="aui-image">
            <div class="aui-image-text">
              <img :src="adTextImg" />
            </div>
          </div>
          <div class="aui-formBox">
            <div class="aui-formWell">
              <div class="aui-flex aui-form-nav investment_title">
                <div class="aui-flex-box activeNav on">{{ t('sys.login.signInFormTitle') }}</div>
              </div>
              <div class="aui-form-box" style="height: 180px">
                <a-form ref="loginRef" :model="formData" @keyup.enter.native="handleLogin">
                  <div class="aui-account">
                    <div class="aui-inputClear">
                      <i class="icon icon-code"></i>
                      <a-form-item>
                        <a-input class="fix-auto-fill" :placeholder="t('sys.login.userName')" v-model:value="formData.username" />
                      </a-form-item>
                    </div>
                    <div class="aui-inputClear">
                      <i class="icon icon-password"></i>
                      <a-form-item>
                        <a-input class="fix-auto-fill" type="password" :placeholder="t('sys.login.password')" v-model:value="formData.password" />
                      </a-form-item>
                    </div>
                  </div>
                </a-form>
              </div>
              <div class="aui-formButton">
                <div class="aui-flex">
                  <a-button :loading="loginLoading" class="aui-link-login" type="primary" @click="handleLogin">
                    {{ t('sys.login.loginButton') }}</a-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script lang="ts" setup name="login-mini">
  import { reactive, ref, toRaw } from 'vue';
  import { useUserStore } from '/@/store/modules/user';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useI18n } from '/@/hooks/web/useI18n';
  import logoImg from '/@/assets/loginmini/icon/jeecg_logo.png';
  import adTextImg from '/@/assets/loginmini/icon/jeecg_ad_text.png';
  import { AppLocalePicker, AppDarkModeToggle } from '/@/components/Application';
  import { useDesign } from "/@/hooks/web/useDesign";
  import { useAppInject } from "/@/hooks/web/useAppInject";

  const { prefixCls } = useDesign('mini-login');
  const { notification, createMessage } = useMessage();
  const userStore = useUserStore();
  const { t } = useI18n();

  const formData = reactive<any>({
    username: 'youcai',
    password: 'YouCai1.',
  });
  const loginLoading = ref<boolean>(false);
  const { getIsMobile } = useAppInject();

  async function handleLogin() {
    if (!formData.username) {
      createMessage.warn(t('sys.login.accountPlaceholder'));
      return;
    }
    if (!formData.password) {
      createMessage.warn(t('sys.login.passwordPlaceholder'));
      return;
    }
    try {
      loginLoading.value = true;
      const { userInfo } = await userStore.login(
        toRaw({
          password: formData.password,
          username: formData.username,
          mode: 'none',
        })
      );
      if (userInfo) {
        notification.success({
          message: t('sys.login.loginSuccessTitle'),
          description: `${t('sys.login.loginSuccessDesc')}: ${userInfo.realname}`,
          duration: 3,
        });
      }
    } catch (error) {
      notification.error({
        message: t('sys.api.errorTip'),
        description: error.message || t('sys.login.networkExceptionMsg'),
        duration: 3,
      });
    } finally {
      loginLoading.value = false;
    }
  }
</script>

<style lang="less" scoped>
  @import '/@/assets/loginmini/style/home.less';
  @import '/@/assets/loginmini/style/base.less';

  :deep(.ant-input:focus) {
    box-shadow: none;
  }
  :deep(.jeecg-dark-switch){
    position:absolute;
    margin-right: 10px;
  }
  .aui-link-login{
    height: 42px;
    padding: 10px 15px;
    font-size: 14px;
    border-radius: 8px;
    margin-top: 15px;
    margin-bottom: 8px;
    flex: 1;
    color: #fff;
  }
  .aui-phone-logo{
    position: absolute;
    margin-left: 10px;
    width: 60px;
    top:2px;
    z-index: 4;
  }
  .top-3{
    top: 0.45rem;
  }
</style>

<style lang="less">
@prefix-cls: ~'@{namespace}-mini-login';
@dark-bg: #293146;

html[data-theme='dark'] {
  .@{prefix-cls} {
    background-color: @dark-bg !important;
    background-image: none;

    &::before {
      background-image: url(/@/assets/svg/login-bg-dark.svg);
    }
    .aui-inputClear{
      background-color: #232a3b !important;
    }
    .ant-input,
    .ant-input-password {
      background-color: #232a3b !important;
    }

    .ant-btn:not(.ant-btn-link):not(.ant-btn-primary) {
      border: 1px solid #4a5569 !important;
    }

    &-form {
      background: @dark-bg !important;
    }

    .app-iconify {
      color: #fff !important;
    }
    .aui-inputClear input,.aui-input-line input,.aui-choice{
      color: #c9d1d9 !important;
    }

    .aui-formBox{
      background-color: @dark-bg !important;
    }
    .aui-third-text span{
      background-color: @dark-bg !important;
    }
    .aui-form-nav .aui-flex-box{
      color: #c9d1d9 !important;
    }

    .aui-formButton .aui-linek-code{
      background:  @dark-bg !important;
      color: white !important;
    }
    .aui-code-line{
      border-left: none !important;
    }
    .ant-checkbox-inner,.aui-success h3{
      border-color: #c9d1d9;
    }
    &-sign-in-way {
      .anticon {
        font-size: 22px !important;
        color: #888 !important;
        cursor: pointer !important;

        &:hover {
          color: @primary-color !important;
        }
      }
    }
  }

  input.fix-auto-fill,
  .fix-auto-fill input {
    -webkit-text-fill-color: #c9d1d9 !important;
    box-shadow: inherit !important;
  }

  .ant-divider-inner-text {
    font-size: 12px !important;
    color: @text-color-secondary !important;
  }
  .aui-third-login a{
    background: transparent;
  }
}
</style>
