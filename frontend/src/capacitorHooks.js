import { Capacitor } from '@capacitor/core';

const isNative = () => Capacitor.isNativePlatform();

// Lazy load plugins only when needed
const load = async (name) => {
  if (!isNative()) return null;
  try {
    const mod = await import(`@capacitor/${name}`);
    return mod[Object.keys(mod)[0]];
  } catch { return null; }
};

export { isNative };

export async function getCurrentPosition() {
  const geo = await load('geolocation');
  if (geo) {
    const p = await geo.getCurrentPosition({ enableHighAccuracy: true, timeout: 10000 });
    return { latitude: p.coords.latitude, longitude: p.coords.longitude, accuracy: p.coords.accuracy };
  }
  // Browser fallback
  return new Promise((res, rej) => navigator.geolocation.getCurrentPosition(
    p => res({ latitude: p.coords.latitude, longitude: p.coords.longitude, accuracy: p.coords.accuracy }),
    rej, { enableHighAccuracy: true, timeout: 10000 }
  ));
}

export async function takePhoto() {
  const camera = await load('camera');
  if (camera) {
    const img = await camera.getPhoto({ quality: 90, allowEditing: false, resultType: 'base64', saveToGallery: false });
    return { base64: img.base64String, format: img.format };
  }
  // Browser fallback: file input
  return new Promise((resolve, reject) => {
    const input = document.createElement('input');
    input.type = 'file'; input.accept = 'image/*'; input.capture = 'environment';
    input.onchange = (e) => {
      const file = e.target.files[0];
      if (!file) { reject(new Error('No file')); return; }
      const reader = new FileReader();
      reader.onload = () => resolve({ base64: reader.result.split(',')[1], format: file.type.split('/')[1] });
      reader.onerror = () => reject(new Error('Read failed'));
      reader.readAsDataURL(file);
    };
    input.click();
  });
}

export async function registerPushNotifications() {
  const push = await load('push-notifications');
  if (!push) return null;
  const perm = await push.requestPermissions();
  if (perm.receive !== 'granted') return null;
  await push.register();
  return new Promise(res => push.addListener('registration', t => res(t.value)));
}

export async function writeFile(filename, data) {
  const fs = await load('filesystem');
  if (fs) { await fs.writeFile({ path: filename, data, directory: 'data', recursive: true }); return true; }
  localStorage.setItem(`f_${filename}`, data); return true;
}

export async function readFile(filename) {
  const fs = await load('filesystem');
  if (fs) { const r = await fs.readFile({ path: filename, directory: 'data' }); return r.data; }
  return localStorage.getItem(`f_${filename}`);
}

export async function vibrate() {
  const h = await load('haptics');
  if (h) await h.vibrate({ duration: 50 });
}

export async function shareContent(title, text, url) {
  const s = await load('share');
  if (s) await s.share({ title, text, url });
  else if (navigator.share) await navigator.share({ title, text, url });
  else await navigator.clipboard.writeText(text || url);
}