const DEFAULT_TIANDITU_TK = '46fdb68b960da6af775e3287cae51e81';
const TIANDITU_API_SCRIPT_ID = 'tianditu-api-script';

let tiandituApiPromise: Promise<any> | null = null;

export function getTiandituTk() {
  return (import.meta.env.VITE_TIANDITU_TK || DEFAULT_TIANDITU_TK).trim();
}

export function getTiandituApiUrl() {
  return `https://api.tianditu.gov.cn/api?v=4.0&tk=${encodeURIComponent(getTiandituTk())}`;
}

export function loadTiandituAPI() {
  if ((window as any).T) {
    return Promise.resolve((window as any).T);
  }

  if (tiandituApiPromise) {
    return tiandituApiPromise;
  }

  tiandituApiPromise = new Promise((resolve, reject) => {
    const existingScript = document.getElementById(TIANDITU_API_SCRIPT_ID) as HTMLScriptElement | null;

    const handleLoaded = () => {
      if ((window as any).T) {
        resolve((window as any).T);
        return;
      }
      reject(new Error('天地图 API 已返回但未初始化，请检查 tk 是否有效或是否被官方 WAF 拦截'));
    };

    const handleError = () => {
      reject(new Error('天地图 API 加载失败，请检查网络、tk 或天地图控制台域名白名单'));
    };

    if (existingScript) {
      existingScript.addEventListener('load', handleLoaded, { once: true });
      existingScript.addEventListener('error', handleError, { once: true });
      return;
    }

    const script = document.createElement('script');
    script.id = TIANDITU_API_SCRIPT_ID;
    script.type = 'text/javascript';
    script.async = true;
    script.src = getTiandituApiUrl();
    script.onload = handleLoaded;
    script.onerror = handleError;
    document.head.appendChild(script);
  }).catch((error) => {
    tiandituApiPromise = null;
    throw error;
  });

  return tiandituApiPromise;
}
