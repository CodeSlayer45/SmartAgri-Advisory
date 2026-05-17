import { useCallback, useEffect, useState } from 'react';
import { api, checkBackendHealth, getStoredApiKey, setApiKey } from './api';
import WeatherPanel from './components/WeatherPanel';
import AlertsPanel from './components/AlertsPanel';

const emptyField = {
  fieldName: '',
  cropName: '',
  acreage: '1',
  location: 'Farmer live GPS',
  locationMode: 'AUTO_GPS',
  latitude: '16.8524',
  longitude: '74.5815',
  sowingDate: new Date().toISOString().slice(0, 10),
};

const emptyActivity = {
  activityDate: new Date().toISOString().slice(0, 10),
  activityType: 'fungicide',
  inputName: 'Mancozeb',
  notes: 'Preventive spray',
};

export default function App() {
  const [apiKeyInput, setApiKeyInput] = useState(getStoredApiKey());
  const [backendUp, setBackendUp] = useState(null);
  const [fields, setFields] = useState([]);
  const [selectedFieldId, setSelectedFieldId] = useState(null);
  const [fieldForm, setFieldForm] = useState(emptyField);
  const [activityForm, setActivityForm] = useState(emptyActivity);
  const [activities, setActivities] = useState([]);
  const [weather, setWeather] = useState(null);
  const [alerts, setAlerts] = useState([]);
  const [recommendation, setRecommendation] = useState(null);
  const [loading, setLoading] = useState(false);
  const [weatherLoading, setWeatherLoading] = useState(false);
  const [alertsLoading, setAlertsLoading] = useState(false);
  const [scanning, setScanning] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const clearMessages = () => {
    setError('');
    setSuccess('');
  };

  const loadFields = useCallback(async () => {
    const data = await api.getFields();
    setFields(data);
    return data;
  }, []);

  const loadActivities = useCallback(async (fieldId) => {
    if (!fieldId) {
      setActivities([]);
      return;
    }
    const data = await api.getActivities(fieldId);
    setActivities(data);
  }, []);

  const loadWeather = useCallback(async (fieldId) => {
    if (!fieldId) {
      setWeather(null);
      return;
    }
    setWeatherLoading(true);
    try {
      const data = await api.getWeather(fieldId);
      setWeather(data);
    } finally {
      setWeatherLoading(false);
    }
  }, []);

  const loadAlerts = useCallback(async (fieldId) => {
    if (!fieldId) {
      setAlerts([]);
      return;
    }
    setAlertsLoading(true);
    try {
      const data = await api.getAlerts(fieldId);
      setAlerts(data);
    } finally {
      setAlertsLoading(false);
    }
  }, []);

  const refreshFieldData = useCallback(
    async (fieldId) => {
      if (!fieldId) return;
      await Promise.all([
        loadActivities(fieldId),
        loadWeather(fieldId),
        loadAlerts(fieldId),
      ]);
    },
    [loadActivities, loadWeather, loadAlerts]
  );

  useEffect(() => {
    checkBackendHealth().then(setBackendUp);
    loadFields()
      .then((data) => {
        if (data.length) {
          setSelectedFieldId((prev) => prev ?? data[0].id);
        }
      })
      .catch((e) => setError(e.message));
  }, [loadFields]);

  useEffect(() => {
    if (selectedFieldId) {
      refreshFieldData(selectedFieldId).catch((e) => setError(e.message));
    }
  }, [selectedFieldId, refreshFieldData]);

  const saveApiKey = () => {
    setApiKey(apiKeyInput.trim());
    setSuccess('API key saved.');
    setError('');
  };

  const handleCreateField = async (e) => {
    e.preventDefault();
    clearMessages();
    setLoading(true);
    try {
      const body = {
        ...fieldForm,
        acreage: Number(fieldForm.acreage),
        latitude:
          fieldForm.locationMode === 'AUTO_GPS'
            ? Number(fieldForm.latitude)
            : undefined,
        longitude:
          fieldForm.locationMode === 'AUTO_GPS'
            ? Number(fieldForm.longitude)
            : undefined,
      };
      const created = await api.createField(body);
      setSuccess(`Field "${created.fieldName}" created.`);
      setFieldForm(emptyField);
      const list = await loadFields();
      setSelectedFieldId(created.id);
      if (!list.find((f) => f.id === created.id)) {
        setFields((prev) => [...prev, created]);
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateActivity = async (e) => {
    e.preventDefault();
    if (!selectedFieldId) {
      setError('Select a field first.');
      return;
    }
    clearMessages();
    setLoading(true);
    try {
      await api.createActivity({ fieldId: selectedFieldId, ...activityForm });
      setSuccess('Activity logged.');
      await loadActivities(selectedFieldId);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleRecommend = async () => {
    if (!selectedFieldId) {
      setError('Select a field first.');
      return;
    }
    clearMessages();
    setLoading(true);
    setRecommendation(null);
    try {
      const data = await api.getRecommendation(selectedFieldId);
      setRecommendation(data);
      setSuccess('Recommendation generated.');
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleScanAlerts = async () => {
    clearMessages();
    setScanning(true);
    try {
      const result = await api.scanAlerts();
      setSuccess(result.message || 'Alert scan completed.');
      if (selectedFieldId) {
        await loadAlerts(selectedFieldId);
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setScanning(false);
    }
  };

  const selectedField = fields.find((f) => f.id === selectedFieldId);

  return (
    <div className="app">
      <header className="header">
        <div>
          <h1>Smart Agri Advisory</h1>
          <p>
            Full farmer dashboard — fields, weather, activities, AI advisory, and
            disease alerts.
          </p>
          <p className="status-line">
            Backend:{' '}
            {backendUp === null ? (
              'checking…'
            ) : backendUp.up ? (
              <span className="status-up">connected</span>
            ) : (
              <span className="status-down">offline — start mvn spring-boot:run</span>
            )}
          </p>
        </div>
        <div className="settings-bar">
          <label>
            X-API-KEY
            <input
              type="password"
              value={apiKeyInput}
              onChange={(e) => setApiKeyInput(e.target.value)}
              placeholder="change-me-local"
            />
          </label>
          <button type="button" className="btn btn-secondary" onClick={saveApiKey}>
            Save key
          </button>
        </div>
      </header>

      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      <div className="grid">
        <section className="card">
          <h2>Your fields</h2>
          {fields.length === 0 ? (
            <p className="empty">No fields yet. Register one below.</p>
          ) : (
            <ul className="field-list">
              {fields.map((f) => (
                <li
                  key={f.id}
                  className={`field-item ${selectedFieldId === f.id ? 'selected' : ''}`}
                  onClick={() => setSelectedFieldId(f.id)}
                  onKeyDown={(e) => e.key === 'Enter' && setSelectedFieldId(f.id)}
                  role="button"
                  tabIndex={0}
                >
                  <div>
                    <strong>{f.fieldName}</strong>
                    <div className="field-meta">
                      {f.cropName} · {f.acreage} acre · {f.cropAgeDays} days
                    </div>
                  </div>
                  <span className="field-meta">{f.locationMode}</span>
                </li>
              ))}
            </ul>
          )}
        </section>

        <section className="card">
          <h2>Register field</h2>
          <form className="form-grid" onSubmit={handleCreateField}>
            <label>
              Field name
              <input
                required
                value={fieldForm.fieldName}
                onChange={(e) =>
                  setFieldForm({ ...fieldForm, fieldName: e.target.value })
                }
              />
            </label>
            <label>
              Crop
              <input
                required
                value={fieldForm.cropName}
                onChange={(e) =>
                  setFieldForm({ ...fieldForm, cropName: e.target.value })
                }
              />
            </label>
            <label>
              Acreage
              <input
                type="number"
                step="0.1"
                min="0.1"
                required
                value={fieldForm.acreage}
                onChange={(e) =>
                  setFieldForm({ ...fieldForm, acreage: e.target.value })
                }
              />
            </label>
            <label>
              Location mode
              <select
                value={fieldForm.locationMode}
                onChange={(e) =>
                  setFieldForm({ ...fieldForm, locationMode: e.target.value })
                }
              >
                <option value="AUTO_GPS">AUTO_GPS</option>
                <option value="MANUAL">MANUAL</option>
              </select>
            </label>
            {fieldForm.locationMode === 'MANUAL' ? (
              <label>
                Location
                <input
                  required
                  value={fieldForm.location}
                  onChange={(e) =>
                    setFieldForm({ ...fieldForm, location: e.target.value })
                  }
                  placeholder="Kolhapur, Maharashtra, India"
                />
              </label>
            ) : (
              <>
                <label>
                  Latitude
                  <input
                    required
                    value={fieldForm.latitude}
                    onChange={(e) =>
                      setFieldForm({ ...fieldForm, latitude: e.target.value })
                    }
                  />
                </label>
                <label>
                  Longitude
                  <input
                    required
                    value={fieldForm.longitude}
                    onChange={(e) =>
                      setFieldForm({ ...fieldForm, longitude: e.target.value })
                    }
                  />
                </label>
              </>
            )}
            <label>
              Sowing date
              <input
                type="date"
                required
                value={fieldForm.sowingDate}
                onChange={(e) =>
                  setFieldForm({ ...fieldForm, sowingDate: e.target.value })
                }
              />
            </label>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              Create field
            </button>
          </form>
        </section>

        <WeatherPanel
          weather={weather}
          loading={weatherLoading}
          onRefresh={() => loadWeather(selectedFieldId).catch((e) => setError(e.message))}
          disabled={!selectedFieldId}
        />

        <AlertsPanel
          alerts={alerts}
          loading={alertsLoading}
          scanning={scanning}
          onRefresh={() => loadAlerts(selectedFieldId).catch((e) => setError(e.message))}
          onScan={handleScanAlerts}
          disabled={!selectedFieldId}
        />

        <section className="card">
          <h2>Log activity</h2>
          {selectedField ? (
            <p className="field-meta" style={{ marginTop: 0 }}>
              For: <strong>{selectedField.fieldName}</strong>
            </p>
          ) : (
            <p className="empty">Select a field first.</p>
          )}
          <form className="form-grid" onSubmit={handleCreateActivity}>
            <label>
              Date
              <input
                type="date"
                required
                value={activityForm.activityDate}
                onChange={(e) =>
                  setActivityForm({ ...activityForm, activityDate: e.target.value })
                }
              />
            </label>
            <label>
              Type
              <select
                value={activityForm.activityType}
                onChange={(e) =>
                  setActivityForm({ ...activityForm, activityType: e.target.value })
                }
              >
                <option value="fungicide">fungicide</option>
                <option value="pesticide">pesticide</option>
                <option value="fertilizer">fertilizer</option>
                <option value="irrigation">irrigation</option>
              </select>
            </label>
            <label>
              Input name
              <input
                value={activityForm.inputName}
                onChange={(e) =>
                  setActivityForm({ ...activityForm, inputName: e.target.value })
                }
              />
            </label>
            <label>
              Notes
              <textarea
                value={activityForm.notes}
                onChange={(e) =>
                  setActivityForm({ ...activityForm, notes: e.target.value })
                }
              />
            </label>
            <button
              type="submit"
              className="btn btn-primary"
              disabled={loading || !selectedFieldId}
            >
              Save activity
            </button>
          </form>
          {activities.length > 0 && (
            <ul className="reason-list" style={{ marginTop: '1rem' }}>
              {activities.map((a) => (
                <li key={a.id}>
                  {a.activityDate}: {a.activityType}
                  {a.inputName ? ` (${a.inputName})` : ''}
                </li>
              ))}
            </ul>
          )}
        </section>

        <section className="card full-width">
          <h2>AI advisory</h2>
          <p className="field-meta" style={{ marginTop: 0 }}>
            Rules decide risk level and actions. Optional OpenAI rewrites text for farmers
            when enabled on the server.
          </p>
          <button
            type="button"
            className="btn btn-primary"
            onClick={handleRecommend}
            disabled={loading || !selectedFieldId}
          >
            Get recommendation
          </button>

          {recommendation && (
            <div style={{ marginTop: '1.25rem' }}>
              <div className="score-row">
                <span className={`badge badge-${recommendation.riskLevel}`}>
                  {recommendation.riskLevel}
                </span>
                <span className="score-value">{recommendation.riskScore}</span>
                <span className="field-meta">risk score</span>
              </div>
              {recommendation.aiEnhanced && recommendation.farmerAdvisory && (
                <div className="farmer-advisory-box">
                  <span className="ai-badge">OpenAI farmer summary</span>
                  <p className="farmer-advisory-text">{recommendation.farmerAdvisory}</p>
                </div>
              )}

              <h3 className="subsection-title">Why (rule engine)</h3>
              <ul className="reason-list">
                {recommendation.explainableReasons.map((r, i) => (
                  <li key={i}>{r}</li>
                ))}
              </ul>
              <h3 className="subsection-title">Actions (rule engine)</h3>
              <ul className="reco-list">
                {recommendation.recommendations.map((r, i) => (
                  <li key={i}>{r}</li>
                ))}
              </ul>
            </div>
          )}
        </section>
      </div>
    </div>
  );
}
