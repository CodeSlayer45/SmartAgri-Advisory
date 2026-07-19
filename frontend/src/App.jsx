import { useCallback, useEffect, useState } from 'react';
import { api, checkBackendHealth, getStoredApiKey, setApiKey } from './api';
import WeatherPanel from './components/WeatherPanel';
import AlertsPanel from './components/AlertsPanel';
import AppHeader from './components/AppHeader';
import FieldSidebar from './components/FieldSidebar';
import FieldOverview from './components/FieldOverview';
import ActivityCalendar from './components/ActivityCalendar';
import FieldEditModal from './components/FieldEditModal';
import RecommendationHistoryPanel from './components/RecommendationHistoryPanel';
import FieldLocationPicker from './components/FieldLocationPicker';
import FieldsOverviewMap from './components/FieldsOverviewMap';
import AIPanel from './components/AIPanel';

const emptyField = {
  fieldName: '', cropName: '', acreage: '1', location: 'Farmer live GPS',
  locationMode: 'AUTO_GPS', latitude: '16.8524', longitude: '74.5815',
  sowingDate: new Date().toISOString().slice(0, 10),
};
const emptyActivity = {
  activityDate: new Date().toISOString().slice(0, 10),
  activityType: 'fungicide', inputName: 'Mancozeb', notes: 'Preventive spray',
};

export default function App() {
  const [apiKeyInput, setApiKeyInput] = useState(getStoredApiKey());
  const [backendUp, setBackendUp] = useState(null);
  const [fields, setFields] = useState([]);
  const [selectedFieldId, setSelectedFieldId] = useState(null);
  const [fieldForm, setFieldForm] = useState(emptyField);
  const [activityForm, setActivityForm] = useState(emptyActivity);
  const [weather, setWeather] = useState({ data: null, loading: false });
  const [alerts, setAlerts] = useState({ data: [], loading: false });
  const [recommendation, setRecommendation] = useState(null);
  const [recoHistory, setRecoHistory] = useState({ data: [], loading: false });
  const [activities, setActivities] = useState([]);
  const [loading, setLoading] = useState(false);
  const [activeNav, setActiveNav] = useState('fields');
  const [editFieldOpen, setEditFieldOpen] = useState(false);
  const [scanning, setScanning] = useState(false);
  const [actingId, setActingId] = useState(null);
  const [toast, setToast] = useState({ error: '', success: '' });

  const t = (type, msg) => setToast({ error: '', success: '', [type]: msg });
  const clearToast = () => setToast({ error: '', success: '' });

  const loadFields = useCallback(async () => {
    const data = await api.getFields();
    setFields(data); return data;
  }, []);

  const loadActivities = useCallback(async (fid) => {
    if (!fid) { setActivities([]); return; }
    const data = await api.getActivities(fid);
    setActivities([...data].sort((a, b) => (b.activityDate || '').localeCompare(a.activityDate || '') || (b.id||0) - (a.id||0)));
  }, []);

  const fetchWithState = async (setState, fn) => {
    setState(s => ({ ...s, loading: true }));
    try { const d = await fn(); setState({ data: d, loading: false }); return d; }
    catch { setState(s => ({ ...s, loading: false })); return null; }
  };

  const refreshFieldData = useCallback(async (fid) => {
    if (!fid) return;
    await Promise.all([
      loadActivities(fid),
      fetchWithState(setWeather, () => api.getWeather(fid)),
      fetchWithState(setAlerts, () => api.getAlerts(fid)),
      fetchWithState(setRecoHistory, () => api.getRecommendationHistory(fid)),
    ]);
  }, [loadActivities]);

  useEffect(() => { if (toast.error || toast.success) { const t2 = setTimeout(clearToast, 5000); return () => clearTimeout(t2); } }, [toast]);
  useEffect(() => {
    checkBackendHealth().then(setBackendUp);
    loadFields().then(d => { if (d.length) setSelectedFieldId(p => p ?? d[0].id); }).catch(e => t('error', e.message));
  }, [loadFields]);
  useEffect(() => { if (selectedFieldId) refreshFieldData(selectedFieldId).catch(e => t('error', e.message)); }, [selectedFieldId, refreshFieldData]);

  const handleCreateField = async (e) => {
    e.preventDefault(); clearToast(); setLoading(true);
    try {
      const body = { ...fieldForm, acreage: Number(fieldForm.acreage) };
      if (fieldForm.locationMode === 'AUTO_GPS') { body.latitude = Number(fieldForm.latitude); body.longitude = Number(fieldForm.longitude); }
      else { delete body.latitude; delete body.longitude; }
      const created = await api.createField(body);
      t('success', `Field "${created.fieldName}" created.`);
      setFieldForm(emptyField); await loadFields();
      setSelectedFieldId(created.id); setActiveNav('fields');
    } catch (err) { t('error', err.message); }
    finally { setLoading(false); }
  };

  const handleRecommend = async () => {
    if (!selectedFieldId) { t('error', 'Select a field first.'); return; }
    clearToast(); setLoading(true); setRecommendation(null);
    try { const d = await api.getRecommendation(selectedFieldId); setRecommendation(d); t('success', 'Recommendation generated.'); await fetchWithState(setRecoHistory, () => api.getRecommendationHistory(selectedFieldId)); }
    catch (err) { t('error', err.message); }
    finally { setLoading(false); }
  };

  const handleDeleteField = async (field) => {
    if (!field?.id || !window.confirm(`Delete "${field.fieldName}"? All data will be removed.`)) return;
    clearToast(); setActingId('del');
    try {
      await api.deleteField(field.id); t('success', 'Field deleted.');
      setRecommendation(null); setWeather({ data: null, loading: false }); setAlerts({ data: [], loading: false }); setActivities([]);
      const list = await loadFields();
      setSelectedFieldId(list.length ? list[0].id : null);
    } catch (err) { t('error', err.message); }
    finally { setActingId(null); }
  };

  const selectedField = fields.find(f => f.id === selectedFieldId);

  return (
    <div className="app-shell">
      <AppHeader backendUp={backendUp} apiKeyInput={apiKeyInput} onApiKeyChange={setApiKeyInput} onSaveKey={() => { setApiKey(apiKeyInput.trim()); t('success', 'API key saved.'); }} />
      {(toast.error || toast.success) && (
        <div className="toast-stack">
          {toast.error && <div className="toast toast-error" role="alert">{toast.error}<button className="toast-close" onClick={clearToast}>×</button></div>}
          {toast.success && <div className="toast toast-success" role="status">{toast.success}<button className="toast-close" onClick={clearToast}>×</button></div>}
        </div>
      )}
      <div className="app-body">
        <FieldSidebar fields={fields} selectedFieldId={selectedFieldId} onSelect={setSelectedFieldId}
          onRefresh={() => loadFields().catch(e => t('error', e.message))} onRegisterField={() => setActiveNav('register')}
          activeNav={activeNav} onNavChange={setActiveNav} selectedFungicideDays={42} />
        <main className="main-panel">
          {activeNav === 'register' && (
            <section className="card">
              <h2>➕ Register new field</h2>
              <p className="field-meta card-intro">Add a plot with crop details and location.</p>
              <form className={`form-grid form-grid--2 ${loading ? 'form-disabled' : ''}`} onSubmit={handleCreateField}>
                <label>Field name <input required value={fieldForm.fieldName} disabled={loading} onChange={e => setFieldForm(f => ({...f, fieldName: e.target.value}))} /></label>
                <label>Crop <input required value={fieldForm.cropName} onChange={e => setFieldForm(f => ({...f, cropName: e.target.value}))} /></label>
                <label>Acreage <input type="number" step="0.1" min="0.1" required value={fieldForm.acreage} onChange={e => setFieldForm(f => ({...f, acreage: e.target.value}))} /></label>
                <label>Location mode <select value={fieldForm.locationMode} onChange={e => setFieldForm(f => ({...f, locationMode: e.target.value}))}>
                  <option value="AUTO_GPS">GPS coordinates</option><option value="MANUAL">Place name</option>
                </select></label>
                {fieldForm.locationMode === 'MANUAL' && <label className="span-2">Location <input required value={fieldForm.location} onChange={e => setFieldForm(f => ({...f, location: e.target.value}))} placeholder="Kolhapur, India" /></label>}
                <div className="span-2"><FieldLocationPicker latitude={fieldForm.latitude} longitude={fieldForm.longitude} disabled={loading}
                  onChange={({latitude, longitude, location}) => setFieldForm(p => ({...p, latitude, longitude, location: location || p.location}))} /></div>
                <label>Sowing date <input type="date" required value={fieldForm.sowingDate} onChange={e => setFieldForm(f => ({...f, sowingDate: e.target.value}))} /></label>
                <div className="form-actions span-2"><button type="submit" className="btn btn-primary" disabled={loading}>{loading ? 'Creating…' : 'Create field'}</button></div>
              </form>
            </section>
          )}

          {selectedField && activeNav === 'fields' && (
            <>
              <FieldOverview field={selectedField} onEditField={() => setEditFieldOpen(true)}
                onDeleteField={handleDeleteField} deleteDisabled={actingId === 'del'} />
              <FieldEditModal field={selectedField} open={editFieldOpen} onClose={() => setEditFieldOpen(false)}
                onSaveBasics={async (body) => { clearToast(); await api.updateField(selectedFieldId, body); t('success', 'Updated.'); await loadFields(); setEditFieldOpen(false); }}
                onSaveLocation={async (body) => { clearToast(); await api.updateFieldLocation(selectedFieldId, body); t('success', 'Location updated.'); await loadFields(); setEditFieldOpen(false); }} />
              <FieldsOverviewMap fields={fields} selectedFieldId={selectedFieldId} onSelectField={setSelectedFieldId} />
            </>
          )}

          {selectedField && activeNav === 'weather' && (
            <WeatherPanel weather={weather.data} loading={weather.loading}
              onRefresh={() => fetchWithState(setWeather, () => api.getWeather(selectedFieldId))} disabled={!selectedFieldId} />
          )}

          {selectedField && activeNav === 'activities' && (
            <section className="card">
              <h2>📋 Field activities</h2>
              {selectedField && <p className="field-meta card-intro">Logging for <strong>{selectedField.fieldName}</strong></p>}
              <form className={`form-grid form-grid--2 ${loading ? 'form-disabled' : ''}`} onSubmit={async (e) => {
                e.preventDefault(); if (!selectedFieldId) { t('error', 'Select a field.'); return; }
                clearToast(); setLoading(true);
                try { await api.createActivity({ fieldId: selectedFieldId, ...activityForm }); t('success', 'Activity logged.'); await loadActivities(selectedFieldId); }
                catch (err) { t('error', err.message); } finally { setLoading(false); }
              }}>
                <label>Date <input type="date" required value={activityForm.activityDate} disabled={loading} onChange={e => setActivityForm(f => ({...f, activityDate: e.target.value}))} /></label>
                <label>Type <select value={activityForm.activityType} disabled={loading} onChange={e => setActivityForm(f => ({...f, activityType: e.target.value}))}>
                  <option value="fungicide">fungicide</option><option value="pesticide">pesticide</option><option value="fertilizer">fertilizer</option><option value="irrigation">irrigation</option>
                </select></label>
                <label>Input <input value={activityForm.inputName} disabled={loading} onChange={e => setActivityForm(f => ({...f, inputName: e.target.value}))} /></label>
                <label className="span-2">Notes <textarea value={activityForm.notes} disabled={loading} onChange={e => setActivityForm(f => ({...f, notes: e.target.value}))} /></label>
                <div className="form-actions span-2"><button type="submit" className="btn btn-primary" disabled={loading || !selectedFieldId}>Save activity</button></div>
              </form>
              <ActivityCalendar activities={activities} fieldId={selectedFieldId}
                onDeleteActivity={(id) => { clearToast(); api.deleteActivity(id).then(() => { t('success', 'Deleted.'); loadActivities(selectedFieldId); }).catch(e => t('error', e.message)); }}
                onEditActivity={async (id, body) => { await api.updateActivity(id, body); t('success', 'Updated.'); await loadActivities(selectedFieldId); }}
                deletingId={actingId} disabled={loading} />
            </section>
          )}

          {selectedField && activeNav === 'advisory' && (
            <section className="card card--wide">
              <h2>🧠 AI crop advisory</h2>
              <p className="field-meta card-intro">Risk analysis based on weather, crop age & activities.</p>
              <button className="btn btn-primary" onClick={handleRecommend} disabled={loading || !selectedFieldId}>{loading ? '⏳' : '🎯 Get recommendation'}</button>
              {recommendation && (
                <div className="reco-panel">
                  <div className="score-row"><span className={`badge badge-${recommendation.riskLevel}`}>{recommendation.riskLevel}</span><span className="score-value">{recommendation.riskScore}</span><span className="field-meta">risk score</span></div>
                  {recommendation.aiEnhanced && recommendation.farmerAdvisory && <div className="farmer-advisory-box"><span className="ai-badge">AI Summary</span><p className="farmer-advisory-text">{recommendation.farmerAdvisory}</p></div>}
                  <h3 className="subsection-title">Why</h3>
                  <ul className="reason-list">{recommendation.explainableReasons.map((r,i) => <li key={i}>{r}</li>)}</ul>
                  <h3 className="subsection-title">Actions</h3>
                  <ul className="reco-list">{recommendation.recommendations.map((r,i) => <li key={i}>{r}</li>)}</ul>
                </div>
              )}
              <RecommendationHistoryPanel history={recoHistory.data} loading={recoHistory.loading} disabled={!selectedFieldId} onRefresh={() => fetchWithState(setRecoHistory, () => api.getRecommendationHistory(selectedFieldId))} />
            </section>
          )}

          {selectedField && activeNav === 'alerts' && (
            <AlertsPanel alerts={alerts.data} loading={alerts.loading} scanning={scanning}
              onRefresh={() => fetchWithState(setAlerts, () => api.getAlerts(selectedFieldId))}
              onScan={async () => { setScanning(true); try { const r = await api.scanAlerts(); t('success', r.message); await fetchWithState(setAlerts, () => api.getAlerts(selectedFieldId)); } catch (e) { t('error', e.message); } finally { setScanning(false); } }}
              onDismiss={async (id) => { setActingId(id); try { await api.dismissAlert(id); t('success', 'Dismissed.'); await fetchWithState(setAlerts, () => api.getAlerts(selectedFieldId)); } catch (e) { t('error', e.message); } finally { setActingId(null); } }}
              dismissingId={actingId} disabled={!selectedFieldId} />
          )}

          {(activeNav === 'disease' || activeNav === 'chat' || activeNav === 'market' || activeNav === 'growth') && (
            <AIPanel selectedFieldId={selectedFieldId} selectedField={selectedField} initialTab={activeNav} />
          )}

          {!selectedField && activeNav !== 'register' && (
            <div className="empty" style={{padding:'3rem'}}>
              <h2>👋 Welcome to Smart Agri Advisory</h2>
              <p>Select a field from the sidebar or register a new one to get started.</p>
              <button className="btn btn-primary" onClick={() => setActiveNav('register')}>➕ Register field</button>
            </div>
          )}
        </main>
      </div>
    </div>
  );
}