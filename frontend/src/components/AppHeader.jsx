export default function AppHeader({ backendUp, apiKeyInput, onApiKeyChange, onSaveKey }) {
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
        >
          <span className="status-dot" />
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
              type="password"
              value={apiKeyInput}
              onChange={(e) => onApiKeyChange(e.target.value)}
              placeholder="change-me-local"
            />
            <button type="button" className="btn btn-secondary btn-sm" onClick={onSaveKey}>
              Save
            </button>
          </div>
        </div>
      </div>
    </header>
  );
}
