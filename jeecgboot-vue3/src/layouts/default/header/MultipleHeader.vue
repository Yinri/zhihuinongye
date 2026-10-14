<template>
  <div :style="getPlaceholderDomStyle" v-if="getIsShowPlaceholderDom"></div>
  <div ref="headerWrapperRef" :style="getWrapStyle" :class="getClass">
    <LayoutHeader v-if="getShowHeader" />
    <MultipleTabs v-if="getShowTabs" />
  </div>
</template>
<script lang="ts">
  import { defineComponent, unref, computed, ref, CSSProperties } from 'vue';
  import { useResizeObserver } from '@vueuse/core';

  import LayoutHeader from './index.vue';
  import MultipleTabs from '../tabs/index.vue';

  import { useAppStore } from "@/store/modules/app";
  import { useGlobSetting } from "/@/hooks/setting";
  import { useHeaderSetting } from '/@/hooks/setting/useHeaderSetting';
  import { useMenuSetting } from '/@/hooks/setting/useMenuSetting';
  import { useFullContent } from '/@/hooks/web/useFullContent';
  import { useMultipleTabSetting } from '/@/hooks/setting/useMultipleTabSetting';
  import { useAppInject } from '/@/hooks/web/useAppInject';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { useLayoutHeight } from '../content/useContentViewHeight';
  import { TabsThemeEnum } from '/@/enums/appEnum';

  // 代码逻辑说明: 【QQYUN-8774】网站header区域加高
  // 油菜系统定制：header 内含生长阶段按钮行(GrowthStageTag 高72px)，需与老版本保持一致，否则固定header会遮挡内容、标签栏位置被挤掉
  const HEADER_HEIGHT = 160;

  // updateBy:sunjianlei---updateDate:2021-09-03---修改tab切换栏样式：更改高度
  const TABS_HEIGHT = 32;
  const TABS_HEIGHT_CARD = 50;
  const TABS_HEIGHT_SMOOTH = 50;

  export default defineComponent({
    name: 'LayoutMultipleHeader',
    components: { LayoutHeader, MultipleTabs },
    setup() {
      const { setHeaderHeight } = useLayoutHeight();
      const headerWrapperRef = ref<HTMLElement>();
      const measuredHeaderHeight = ref<number>();
      // 同步真实高度，覆盖生育期栏、标签栏及响应式布局，不依赖固定估值。
      useResizeObserver(headerWrapperRef, () => {
        measuredHeaderHeight.value = headerWrapperRef.value?.getBoundingClientRect().height;
      });
      const { prefixCls } = useDesign('layout-multiple-header');

      const appStore = useAppStore()
      const glob = useGlobSetting()

      const { getCalcContentWidth } = useMenuSetting();
      const { getIsMobile } = useAppInject();
      const { getFixed, getShowInsetHeaderRef, getShowFullHeaderRef, getHeaderTheme } = useHeaderSetting();

      const { getFullContent } = useFullContent();

      const { getShowMultipleTab, getTabsTheme } = useMultipleTabSetting();

      const getShowHeader = computed(() => {
        // 控制是否显示顶部
        if (appStore.getLayoutHideHeader) {
          return false;
        }
        return unref(getShowInsetHeaderRef);
      })

      const getShowTabs = computed(() => {
        // 控制是否显示多Tabs切换
        if (appStore.getLayoutHideMultiTabs) {
          return false;
        }
        return unref(getShowMultipleTab) && !unref(getFullContent);
      });

      const getIsShowPlaceholderDom = computed(() => {
        return unref(getFixed) || unref(getShowFullHeaderRef);
      });

      const getWrapStyle = computed((): CSSProperties => {
        const style: CSSProperties = {};
        if (unref(getFixed)) {
          style.width = unref(getIsMobile) ? '100%' : unref(getCalcContentWidth);
        }
        if (unref(getShowFullHeaderRef)) {
          style.top = `${HEADER_HEIGHT}px`;
        }
        return style;
      });

      const getIsFixed = computed(() => {
        return unref(getFixed) || unref(getShowFullHeaderRef);
      });

      // updateBy:sunjianlei---updateDate:2021-09-08---根据主题的不同，动态计算tabs高度
      const getTabsThemeHeight = computed(() => {
        let tabsTheme = unref(getTabsTheme);
        if (tabsTheme === TabsThemeEnum.CARD) {
          return TABS_HEIGHT_CARD;
        } else if (tabsTheme === TabsThemeEnum.SMOOTH) {
          return TABS_HEIGHT_SMOOTH;
        } else {
          return TABS_HEIGHT;
        }
      });

      const getPlaceholderDomStyle = computed((): CSSProperties => {
        let height = unref(measuredHeaderHeight) ?? (
          (unref(getShowHeader) && !unref(getFullContent) ? HEADER_HEIGHT : 0)
          + (unref(getShowTabs) ? unref(getTabsThemeHeight) : 0)
        );
        if (unref(getShowFullHeaderRef) && !unref(getFullContent)) height += HEADER_HEIGHT;
        setHeaderHeight(height);
        return {
          height: `${height}px`,
        };
      });

      const getClass = computed(() => {
        return [prefixCls, `${prefixCls}--${unref(getHeaderTheme)}`, {
          [`${prefixCls}--fixed`]: unref(getIsFixed),
        }];
      });

      return {
        headerWrapperRef,
        glob,
        getClass,
        prefixCls,
        getPlaceholderDomStyle,
        getIsFixed,
        getWrapStyle,
        getIsShowPlaceholderDom,
        getShowTabs,
        getShowHeader,
      };
    },
  });
</script>
<style lang="less" scoped>
  @prefix-cls: ~'@{namespace}-layout-multiple-header';

  .@{prefix-cls} {
    transition: width 0.2s;
    flex: 0 0 auto;
    // 代码逻辑说明: 【issues/8709】LayoutContent样式多出1px
    // &--dark {
    //   margin-left: -1px;
    // }

    &--fixed {
      position: fixed;
      top: 0;
      z-index: @multiple-tab-fixed-z-index;
      width: 100%;
    }

  }
</style>
