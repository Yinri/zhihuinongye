import type { JVxeVueComponent } from './types';
import { JVxeTypes } from './types/JVxeTypes';

let componentMap = new Map<JVxeTypes | string, JVxeVueComponent>();
const JVxeComponents = 'JVxeComponents__';
if (import.meta.env.DEV && componentMap.size === 0 && window[JVxeComponents] && window[JVxeComponents].size > 0) {
  componentMap = window[JVxeComponents];
}

export { componentMap };

/**
 */
export function clearComponent() {
  componentMap.clear();
  // 代码逻辑说明: 【issues/860】生成的一对多代码，热更新之后点击新增卡死[暂时先解决]
  import.meta.env.DEV && (window[JVxeComponents] = componentMap);
}
