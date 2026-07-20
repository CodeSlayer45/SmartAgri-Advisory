import { useState } from 'react';

export default function AppHeader({ backendUp, apiKeyInput, onApiKeyChange, onSaveKey }) {
  const [showKey, setShowKey] = useState(false);

  return (
    <header className="topbar">
      <div className="brand">
        <span className="brand-icon" aria-hidden>
          🌾
        </span>
        <div>
          <h1>Smart Agri Advisory</h1>
          <p className="brand-tagline">Crop intelligence for smarter farming decisions</p>
        </div>
      </div>

      <div className="topbar-actions">
        <span
          className={`status-pill ${backendUp?.up ? 'status-pill--ok' : backendUp === null ? '' : 'status-pill--err'}`}
          title={backendUp?.up ? 'Backend is online and healthy' : backendUp === null ? 'Connecting to backend...' : 'Backend is offline or unreachable'}
        >
          <span className="status-dot" aria-hidden />
          {backendUp === null
            ? 'Connecting…'
            : backendUp.up
              ? 'API online'
              : 'API offline'}
        </span>

        <div className="api-key-box">
          <label htmlFor="api-key">API key</label>
          <div className="api-key-row">
            <input
              id="api-key"
              type={showKey ? 'text' : 'password'}
              value={apiKeyInput}
              onChange={(e) => onApiKeyChange(e.target.value)}
              placeholder="change-me-local"
              autoComplete="off"
            />
            <button
              type="button"
              className="btn btn-ghost btn-sm"
              onClick={() => setShowKey(s => !s)}
              title={showKey ? 'Hide API key' : 'Show API key'}
              aria-label={showKey ? 'Hide API key' : 'Show API key'}
              style={{background:'rgba(255,255,255,0.08)',color:'rgba(255,255,255,0.7)',border:'1px solid rgba(255,255,255,0.12)'}}
            >
              {showKey ? '🙈' : '👁️'}
            </button>
            <button type="button" className="btn btn-secondary btn-sm" onClick={onSaveKey}>
              Save
            </button>
          </div>
        </div>
      </div>
    </header>
  );
}
