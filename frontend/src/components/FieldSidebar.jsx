import { fungicideOverdue } from '../utils/spray';

const NAV_ITEMS = [
  { id: 'dashboard', label: 'Dashboard', icon: '◉' },
  { id: 'fields', label: 'My Fields', icon: '▤' },
  { id: 'weather', label: 'Weather', icon: '☀' },
  { id: 'activities', label: 'Activities', icon: '☰' },
  { id: 'advisory', label: 'AI Advisory', icon: '◆' },
  { id: 'disease', label: 'Disease Detection', icon: '◎' },
  { id: 'chat', label: 'AI Chat', icon: '?' },
  { id: 'market', label: 'Market Price', icon: '$' },
  { id: 'growth', label: 'Growth Stage', icon: '↑' },
  { id: 'alerts', label: 'Alerts', icon: '!' },
  { id: 'register', label: '+ New Field', icon: '+', highlight: true },
];

export default function FieldSidebar({
  fields, selectedFieldId, onSelect, onRefresh, onRegisterField, selectedFungicideDays, activeNav, onNavChange
}) {
  const selectedField = fields.find(f => f.id === selectedFieldId);

  return (
    <>
      <aside className="sidebar sidebar--desktop">
        {/* Field Selector */}
        <div className="sidebar-section">
          <div className="sidebar-header">
            <h2>🌾 Fields</h2>
            <button className="btn btn-ghost btn-sm" onClick={onRefresh}>↻</button>
          </div>
          {fields.length === 0 ? (
            <div className="sidebar-empty">
              <p className="field-meta">No fields yet</p>
              <button className="btn btn-primary btn-sm" onClick={() => onNavChange('register')}>+ Register</button>
            </div>
          ) : (
            <div className="field-selector">
              {fields.map(f => {
                const showWarn = f.id === selectedFieldId && fungicideOverdue(selectedFungicideDays);
                return (
                  <button key={f.id} className={`field-pill ${selectedFieldId === f.id ? 'active' : ''}`}
                    onClick={() => { onSelect(f.id); onNavChange('fields'); }}>
                    <span className="field-pill-name">{f.fieldName}</span>
                    <span className="field-pill-crop">{f.cropName}</span>
                    {showWarn && <span className="field-warn-dot" title="Spray due!" />}
                  </button>
                );
              })}
            </div>
          )}
          {selectedField && (
            <div className="field-mini-stats">
              <span>{selectedField.cropName}</span>
              <span>·</span>
              <span>{selectedField.acreage} ac</span>
              <span>·</span>
              <span>{selectedField.cropAgeDays || 0}d</span>
            </div>
          )}
        </div>

        {/* Navigation */}
        <nav className="sidebar-nav">
          {NAV_ITEMS.map(item => (
            <button key={item.id}
              className={`nav-item ${activeNav === item.id ? 'active' : ''} ${item.highlight ? 'nav-highlight' : ''}`}
              onClick={() => {
                if (item.id === 'register') { onRegisterField && onRegisterField(); }
                onNavChange(item.id);
              }}>
              <span className="nav-icon">{item.icon}</span>
              <span className="nav-label">{item.label}</span>
              {item.id === 'alerts' && <span className="nav-badge">•</span>}
            </button>
          ))}
        </nav>

        <div className="sidebar-footer">
          <span className="field-meta">Smart Agri Advisory v1.0</span>
        </div>
      </aside>

      {/* Mobile bottom nav */}
      <nav className="mobile-bottom-nav">
        {NAV_ITEMS.filter(i => i.id !== 'register').slice(0, 5).map(item => (
          <button key={item.id} className={`mobile-nav-item ${activeNav === item.id ? 'active' : ''}`}
            onClick={() => onNavChange(item.id)}>
            <span className="mobile-nav-icon">{item.icon}</span>
            <span className="mobile-nav-label">{item.label}</span>
          </button>
        ))}
      </nav>
    </>
  );
}