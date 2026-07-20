import { useEffect, useRef, useState } from 'react';
import { api } from '../api';
import { takePhoto } from '../capacitorHooks';

const TABS = [
  { id: 'disease', icon: '📸', label: 'Disease Detection', action: 'analyzeCropHealth' },
  { id: 'chat', icon: '💬', label: 'Ask AI', action: 'askAI' },
  { id: 'market', icon: '💰', label: 'Market Price', action: 'getMarketPrice' },
  { id: 'weather', icon: '🌤️', label: 'Weather Impact', action: 'getWeatherImpact' },
  { id: 'growth', icon: '📈', label: 'Growth Stage', action: 'getGrowthStage' },
];

export default function AIPanel({ selectedFieldId, selectedField, initialTab }) {
  const [tab, setTab] = useState(initialTab || 'disease');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');
  const [question, setQuestion] = useState('');
  const [photo, setPhoto] = useState(null);
  const fileRef = useRef(null);

  useEffect(() => {
    setTab(initialTab || 'disease');
    setResult(null);
    setError('');
  }, [initialTab]);

  const fetchAI = async (action, params) => {
    setLoading(true); setError(''); setResult(null);
    try {
      const data = await api[action](...params);
      setResult(data);
    } catch (e) { setError(e.message); }
    finally { setLoading(false); }
  };

  const handlePhoto = async () => {
    try {
      const p = await takePhoto();
      setPhoto(`data:image/${p.format};base64,${p.base64}`);
      setResult(null);
    } catch (e) { setError('Camera: ' + e.message); }
  };

  const handleFile = (e) => {
    const file = e.target.files[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = () => { setPhoto(reader.result); setResult(null); };
    reader.readAsDataURL(file);
  };

  const analyzePhoto = () => {
    const b64 = photo.split(',')[1];
    const fmt = photo.split(';')[0].split('/')[1];
    fetchAI('analyzeCropHealth', [{ fieldId: selectedFieldId, imageBase64: b64, imageFormat: fmt }]);
  };

  const renderResult = (lines) => lines?.split('\n').map((l, i) =>
    l.trim() ? <p key={i} className="ai-result-line"><strong>{l.match(/^\d+\.\s*[^:]+/)?.[0]}:</strong>{l.replace(/^\d+\.\s*[^:]+:\s*/, '')}</p> : null
  );

  return (
    <section className="card card--wide">
      <div className="card-header-row">
        <h2>🧠 AI Farm Assistant</h2>
        {selectedField && <span className="field-chip">{selectedField.cropName}</span>}
      </div>

      {!selectedField ? <p className="empty">Select a field first.</p> : <>
        {/* Tabs */}
        <div className="ai-tab-bar">
          {TABS.map(t => (
            <button key={t.id} className={`ai-tab ${tab === t.id ? 'active' : ''}`}
              onClick={() => { setTab(t.id); setResult(null); setError(''); }}>
              <span className="ai-tab-icon">{t.icon}</span>
              <span className="ai-tab-label">{t.label}</span>
            </button>
          ))}
        </div>

        {error && <div className="toast toast-error" style={{marginBottom:'0.75rem'}}>{error}
          <button className="toast-close" onClick={()=>setError('')}>×</button>
        </div>}

        {/* Disease Detection */}
        {tab === 'disease' && (
          <div className="ai-panel-content">
            <p className="field-meta card-intro">Take or upload a crop photo for AI disease detection.</p>
            <div className="ai-photo-upload">
              {photo ? (
                <div className="ai-photo-preview">
                  <img src={photo} alt="crop" className="ai-preview-img" />
                  <div className="btn-group" style={{marginTop:'0.35rem'}}>
                    <button className="btn btn-sm btn-ghost" onClick={()=>{setPhoto(null);setResult(null);}}>Remove</button>
                    <button className="btn btn-primary btn-sm" onClick={analyzePhoto} disabled={loading}>
                      {loading ? '⏳ Analyzing...' : '🔍 Analyze'}
                    </button>
                  </div>
                </div>
              ) : (
                <div className="ai-photo-placeholder">
                  <div className="ai-camera-icon">📸</div>
                  <p>Take or upload a crop photo</p>
                  <div className="btn-group">
                    <button className="btn btn-primary" onClick={handlePhoto}>📷 Camera</button>
                    <button className="btn btn-secondary" onClick={()=>fileRef.current?.click()}>📁 Upload</button>
                  </div>
                  <input ref={fileRef} type="file" accept="image/*" style={{display:'none'}} onChange={handleFile} />
                </div>
              )}
            </div>
            {result?.status === 'success' && <div className="ai-result-box">
              <div className="ai-result-header"><span className="ai-badge">📊 Analysis</span><span className="field-meta">{result.timestamp}</span></div>
              <div className="ai-result-text">{renderResult(result.analysis)}</div>
            </div>}
          </div>
        )}

        {/* Chat */}
        {tab === 'chat' && (
          <div className="ai-panel-content">
            <p className="field-meta card-intro">Ask anything about your farm. AI knows your crop, weather & location.</p>
            <textarea value={question} onChange={e=>setQuestion(e.target.value)}
              placeholder={`e.g. "Should I water ${selectedField?.cropName} today?"`} rows={3} className="ai-question-input" />
            <div className="form-actions" style={{marginTop:'0.5rem'}}>
              <button className="btn btn-primary" onClick={()=>fetchAI('askAI',[{ fieldId: selectedFieldId, question }])}
                disabled={loading || !question.trim()}>{loading ? '⏳ Thinking...' : '💬 Ask AI'}</button>
            </div>
            {result?.answer && <div className="ai-result-box">
              <div className="ai-result-header"><span className="ai-badge">🤖 Answer</span></div>
              <div className="ai-result-text">{result.answer.split('\n').map((l,i)=>l.trim()?<p key={i} className="ai-result-line">{l}</p>:null)}</div>
            </div>}
          </div>
        )}

        {/* Market Price */}
        {tab === 'market' && (
          <div className="ai-panel-content">
            <p className="field-meta card-intro">Market analysis for <strong>{selectedField?.cropName}</strong>.</p>
            <button className="btn btn-primary" onClick={()=>fetchAI('getMarketPrice',[selectedField?.cropName, selectedField?.location])}
              disabled={loading}>{loading ? '⏳ Checking...' : '💰 Check Price'}</button>
            {result?.analysis && <div className="ai-result-box" style={{marginTop:'0.75rem'}}>
              <div className="ai-result-header"><span className="ai-badge">📈 Market</span><span className="field-meta">{result.crop}</span></div>
              <div className="ai-result-text">{renderResult(result.analysis)}</div>
            </div>}
          </div>
        )}

        {/* Weather Impact */}
        {tab === 'weather' && (
          <div className="ai-panel-content">
            <p className="field-meta card-intro">How today's weather affects your <strong>{selectedField?.cropName}</strong>.</p>
            <button className="btn btn-primary" onClick={()=>fetchAI('getWeatherImpact',[selectedFieldId])}
              disabled={loading}>{loading ? '⏳ Analyzing...' : '🌤️ Analyze'}</button>
            {result?.analysis && <div className="ai-result-box" style={{marginTop:'0.75rem'}}>
              <div className="ai-result-header"><span className="ai-badge">🌡️ Weather Impact</span></div>
              {result.weather && <div className="weather-grid" style={{marginBottom:'0.75rem'}}>
                <div className="weather-stat"><span className="weather-label">🌡️ Temp</span><span className="weather-value">{result.weather.temperatureC}°C</span></div>
                <div className="weather-stat"><span className="weather-label">💧 Humidity</span><span className="weather-value">{result.weather.humidityPercent}%</span></div>
                <div className="weather-stat"><span className="weather-label">🌧️ Rain</span><span className="weather-value">{result.weather.rainfallMm}mm</span></div>
              </div>}
              <div className="ai-result-text">{result.analysis.split('\n').map((l,i)=>l.trim()?<p key={i} className="ai-result-line">{l}</p>:null)}</div>
            </div>}
          </div>
        )}

        {/* Growth Stage */}
        {tab === 'growth' && (
          <div className="ai-panel-content">
            <p className="field-meta card-intro">Complete growth report for your <strong>{selectedField?.cropName}</strong>.</p>
            <button className="btn btn-primary" onClick={()=>fetchAI('getGrowthStage',[selectedFieldId])}
              disabled={loading}>{loading ? '⏳ Generating...' : '📊 Generate Report'}</button>
            {result?.aiReport && <div className="ai-result-box" style={{marginTop:'0.75rem'}}>
              <div className="ai-result-header"><span className="ai-badge">🌱 Growth Report</span><span className="field-meta">{result.daysSinceSowing} days old</span></div>
              <div className="growth-stage-banner" style={{marginBottom:'0.5rem'}}>
                <span className="growth-stage-label">Stage</span>
                <span className="growth-stage-value">{result.currentStage}</span>
              </div>
              <div className="ai-result-text">{renderResult(result.aiReport)}</div>
            </div>}
          </div>
        )}

        {loading && !result && <div className="skeleton-stack" style={{marginTop:'1rem'}}>
          <div className="skeleton-line skeleton-line--label"/><div className="skeleton-line"/><div className="skeleton-line skeleton-line--short"/>
        </div>}
      </>}
    </section>
  );
}
