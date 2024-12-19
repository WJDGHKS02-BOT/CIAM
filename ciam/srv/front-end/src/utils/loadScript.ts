export const loadScript = async (src: string, innerHTML?: string): Promise<boolean> => {
  try {
    // 이미 로드된 스크립트인지 확인
    if (document.querySelector(`script[src="${src}"]`)) {
      return false;
    }

    console.log(`Loading script: ${src}`);
    const script = document.createElement('script');
    script.type = 'text/javascript';
    script.async = true;
    script.src = src;

    if (innerHTML) {
      script.innerHTML = innerHTML;
    }

    await new Promise<void>((resolve, reject) => {
      script.onload = () => {
        console.log(`Script loaded successfully: ${src}`);
        resolve();
      };

      script.onerror = (error) => {
        console.error(`Script loading failed: ${src}`, error);
        reject(new Error(`Failed to load script: ${src}`));
      };

      document.head.appendChild(script);
    });

    return true;
  } catch (error) {
    console.error('Script loading failed:', error);
    return false;
  }
};

export default loadScript;
