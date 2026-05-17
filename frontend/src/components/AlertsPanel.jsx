export default function AlertsPanel({
  alerts,
  loading,
  scanning,
  onRefresh,
  onScan,
  disabled,
}) {
  return (
    <section className="card">
      <div className="card-header-row">
        <h2>Disease alerts</h2>
        <div className="btn-group">
          <button
            type="button"
            className="btn btn-secondary btn-sm"
            onClick={onRefresh}
            disabled={disabled || loading}
          >
            {loading ? 'Loading…' : 'Refresh'}
          </button>
          <button
            type="button"
            className="btn btn-primary btn-sm"
            onClick={onScan}
            disabled={disabled || scanning}
          >
            {scanning ? 'Scanning…' : 'Run alert scan'}
          </button>
        </div>
      </div>

      <p className="field-meta" style={{ marginTop: 0 }}>
        Scans all fields for high risk (same logic as scheduled job every 6 hours).
      </p>

      {!alerts?.length && !loading && (
        <p className="empty">No alerts for this field yet. Run a scan after high-risk conditions.</p>
      )}

      {alerts?.length > 0 && (
        <ul className="alert-list">
          {alerts.map((a) => (
            <li key={a.id} className="alert-item">
              <div className="alert-item-top">
                <span className={`badge badge-${a.severity}`}>{a.severity}</span>
                <span className="field-meta">
                  {a.createdAt ? new Date(a.createdAt).toLocaleString() : ''}
                </span>
              </div>
              <strong>{a.title}</strong>
              <p className="alert-message">{a.message}</p>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
