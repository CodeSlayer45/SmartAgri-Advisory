export default function FieldSidebar({ fields, selectedFieldId, onSelect, onRefresh }) {
  return (
    <aside className="sidebar card">
      <div className="card-header-row">
        <h2>Farm fields</h2>
        <button type="button" className="btn btn-ghost btn-sm" onClick={onRefresh}>
          Refresh
        </button>
      </div>

      {fields.length === 0 ? (
        <div className="empty-state">
          <p className="empty-state-title">No fields yet</p>
          <p className="empty">Register your first field in the main panel.</p>
        </div>
      ) : (
        <ul className="field-list">
          {fields.map((f) => (
            <li
              key={f.id}
              className={`field-item ${selectedFieldId === f.id ? 'selected' : ''}`}
              onClick={() => onSelect(f.id)}
              onKeyDown={(e) => e.key === 'Enter' && onSelect(f.id)}
              role="button"
              tabIndex={0}
            >
              <div className="field-item-main">
                <strong>{f.fieldName}</strong>
                <span className="field-chip">{f.cropName}</span>
              </div>
              <div className="field-meta">
                {f.acreage} ac · {f.cropAgeDays} days · {f.locationMode}
              </div>
            </li>
          ))}
        </ul>
      )}
    </aside>
  );
}
