const API_BASE = import.meta.env.VITE_API_BASE || '';

function getApiKey() {
  return localStorage.getItem('smartAgriApiKey') || 'change-me-local';
}

export function setApiKey(key) {
  localStorage.setItem('smartAgriApiKey', key);
}

export function getStoredApiKey() {
  return getApiKey();
}

async function request(path, options = {}) {
  const headers = { 'Content-Type': 'application/json', 'X-API-KEY': getApiKey(), ...options.headers };
  const response = await fetch(`${API_BASE}${path}`, { ...options, headers });
  const text = await response.text();
  let data = text ? (() => { try { return JSON.parse(text); } catch { return text; } })() : null;
  if (!response.ok) throw new Error(data?.message || data || `Request failed (${response.status})`);
  return data;
}

export async function checkBackendHealth() {
  try {
    const res = await fetch(`${API_BASE}/actuator/health`);
    return res.ok ? { up: (await res.json()).status === 'UP' } : { up: false };
  } catch { return { up: false }; }
}

// Helper to create API methods with less repetition
const api = {};
const endpoints = {
  getFields: ['GET', '/api/field'],
  getField: ['GET', '/api/field/{0}'],
  createField: ['POST', '/api/field'],
  deleteField: ['DELETE', '/api/field/{0}'],
  updateField: ['PATCH', '/api/field/{0}'],
  updateFieldLocation: ['PATCH', '/api/field/{0}/location'],
  getActivities: ['GET', '/api/activity/field/{0}'],
  createActivity: ['POST', '/api/activity'],
  updateActivity: ['PUT', '/api/activity/{0}'],
  deleteActivity: ['DELETE', '/api/activity/{0}'],
  getRecommendation: ['POST', '/api/smart/recommend/{0}'],
  getRecommendationHistory: ['GET', '/api/smart/recommend/history/{0}'],
  getWeather: ['GET', '/api/weather/field/{0}'],
  scanAlerts: ['POST', '/api/alerts/scan'],
  getAlerts: ['GET', '/api/alerts/field/{0}'],
  dismissAlert: ['PATCH', '/api/alerts/{0}/dismiss'],
  registerDevice: ['POST', '/api/devices/register'],
  unregisterDevice: ['POST', '/api/devices/unregister'],
  fileInsuranceClaim: ['POST', '/api/insurance/claim/{0}'],
  analyzeCropHealth: ['POST', '/api/ai/analyze-crop-health'],
  askAI: ['POST', '/api/ai/ask'],
  getMarketPrice: ['GET', '/api/ai/market-price'],
  getWeatherImpact: ['GET', '/api/ai/weather-impact/{0}'],
  getGrowthStage: ['GET', '/api/ai/growth-stage/{0}'],
};

for (const [name, [method, path]] of Object.entries(endpoints)) {
  api[name] = (...args) => {
    let url = path;
    let body = null;
    // Replace {0}, {1} etc with actual args — then remaining args are for body
    const used = [];
    args.forEach((arg, i) => {
      if (url.includes(`{${i}}`)) {
        url = url.replace(`{${i}}`, arg);
        used.push(i);
      }
    });
    // Last unused argument that's an object = body
    for (let i = args.length - 1; i >= 0; i--) {
      if (!used.includes(i) && typeof args[i] === 'object' && !Array.isArray(args[i]) && args[i] !== null) {
        body = args[i];
        break;
      }
    }
    const options = { method };
    if (body) options.body = JSON.stringify(body);
    // Handle query params for getMarketPrice
    if (name === 'getMarketPrice') {
      url += `?crop=${encodeURIComponent(args[0])}&location=${encodeURIComponent(args[1] || '')}`;
    }
    return request(url, options);
  };
}

export { api };