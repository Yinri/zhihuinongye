const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const ts = require('typescript');
const vue = require('vue');

async function main() {
  const hooks = {};
  const timers = new Map();
  let nextTimer = 0;
  const api = { list: [], calls: [], fail: false, deferred: null };
  const module = { exports: {} };
  const code = ts.transpileModule(fs.readFileSync('src/views/rapeseed/work-area/useVideoDevices.ts', 'utf8'), {
    compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2020 },
  }).outputText;
  const fakeVue = { ...vue };
  for (const name of ['onMounted', 'onBeforeUnmount', 'onActivated', 'onDeactivated']) {
    fakeVue[name] = callback => { hooks[name] = callback; };
  }
  vm.runInNewContext(code, {
    exports: module.exports, console,
    setInterval: (callback, delay) => { assert.equal(delay, 30000); timers.set(++nextTimer, callback); return nextTimer; },
    clearInterval: id => timers.delete(id),
    require: name => name === 'vue' ? fakeVue : {
      getVideoDevices: async () => {
        if (api.deferred) return api.deferred;
        if (api.fail) throw new Error('status unavailable');
        return api.list;
      },
      getVideoStream: async code => { api.calls.push(code); return `stream-${code}-${api.calls.length}`; },
    },
  });
  const { normalizeVideoOnline, useVideoDevices } = module.exports;
  for (const value of [1, '1', true]) assert.equal(normalizeVideoOnline(value), true);
  for (const value of [0, '0', false]) assert.equal(normalizeVideoOnline(value), false);
  for (const value of [null, undefined, '', 2]) assert.equal(normalizeVideoOnline(value), null);
  const base = vue.ref('site01');
  const scope = vue.effectScope();
  const state = scope.run(() => useVideoDevices(() => base.value));
  hooks.onMounted();
  assert.equal(timers.size, 1);
  api.list = [{ equipmentCode: 'A', isOnline: 1 }, { equipmentCode: 'B', isOnline: 0 }];
  await state.refreshVideoDevices();
  assert.deepEqual(api.calls, ['A']);
  const firstUrl = state.videoDevices.value[0].streamUrl;
  await state.refreshVideoDevices();
  assert.equal(state.videoDevices.value[0].streamUrl, firstUrl);
  assert.equal(api.calls.length, 1, 'status refresh must not reload an active stream');
  api.list[0].isOnline = 0;
  await state.refreshVideoDevices();
  assert.equal(state.videoDevices.value[0].streamUrl, '');
  api.list[0].isOnline = 1;
  await state.refreshVideoDevices();
  assert.equal(api.calls.length, 2, 'recovering online device must request a fresh stream');
  const recoveredUrl = state.videoDevices.value[0].streamUrl;
  api.fail = true;
  await state.refreshVideoDevices();
  assert.equal(state.videoDevices.value[0].onlineStatus, null);
  assert.equal(state.videoDevices.value[0].streamUrl, recoveredUrl);
  api.fail = false;
  let resolveOld;
  api.deferred = new Promise(resolve => { resolveOld = resolve; });
  const oldRequest = state.refreshVideoDevices();
  base.value = 'site02';
  api.deferred = null;
  api.list = [{ equipmentCode: 'C', isOnline: '1' }];
  await state.refreshVideoDevices();
  resolveOld([{ equipmentCode: 'OLD', isOnline: 1 }]);
  await oldRequest;
  assert.equal(state.videoDevices.value[0].equipmentCode, 'C', 'old base response must be ignored');
  hooks.onDeactivated();
  assert.equal(timers.size, 0);
  const callCount = api.calls.length;
  await state.refreshVideoDevices();
  assert.equal(api.calls.length, callCount);
  hooks.onActivated();
  assert.equal(timers.size, 1);
  hooks.onBeforeUnmount();
  assert.equal(timers.size, 0);
  scope.stop();
  console.log('PASS: status normalization, offline exclusion, stream reuse/recovery, unknown on failure, base race, lifecycle cleanup');
}
main().catch(error => { console.error(error); process.exitCode = 1; });
