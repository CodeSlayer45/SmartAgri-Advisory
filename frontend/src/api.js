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
  const headers = {
    'Content-Type': 'application/json',
    'X-API-KEY': getApiKey(),
    ...options.headers,
  };

  const response = await fetch(`${API_BASE}${path}`, { ...options, headers });
  const text = await response.text();

  let data = null;
  if (text) {
    try {
      data = JSON.parse(text);
    } catch {
      data = text;
    }
  }

  if (!response.ok) {
    const message =
      typeof data === 'object' && data?.message
        ? data.message
        : typeof data === 'string'
          ? data
          : `Request failed (${response.status})`;
    throw new Error(message);
  }

  return data;
}

export async function checkBackendHealth() {
  try {
    const res = await fetch(`${API_BASE}/actuator/health`);
    if (!res.ok) return { up: false };
    const data = await res.json();
    return { up: data.status === 'UP', status: data.status };
  } catch {
    return { up: false };
  }
}

export const api = {
  getFields: () => request('/api/field'),
  getField: (fieldId) => request(`/api/field/${fieldId}`),
  createField: (body) =>
    request('/api/field', { method: 'POST', body: JSON.stringify(body) }),
  getActivities: (fieldId) => request(`/api/activity/field/${fieldId}`),
  createActivity: (body) =>
    request('/api/activity', { method: 'POST', body: JSON.stringify(body) }),
  getRecommendation: (fieldId) =>
    request(`/api/smart/recommend/${fieldId}`, { method: 'POST' }),
  getWeather: (fieldId) => request(`/api/weather/field/${fieldId}`),
  scanAlerts: () => request('/api/alerts/scan', { method: 'POST' }),
  getAlerts: (fieldId) => request(`/api/alerts/field/${fieldId}`),
};
