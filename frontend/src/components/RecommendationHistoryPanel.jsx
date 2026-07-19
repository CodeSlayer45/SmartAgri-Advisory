export default function RecommendationHistoryPanel({ history, loading, onRefresh, disabled }) {
  return (
    <div className="reco-history">
      <div className="card-header-row">
        <h3 className="subsection-title" style={{ margin: 0 }}>
          Recommendation history
        </h3>
        <button
          type="button"
          className="btn btn-ghost btn-sm"
          onClick={onRefresh}
          disabled={disabled || loading}
        >
          {loading ? 'Loading…' : 'Refresh'}
        </button>
      </div>

      {loading && (
        <div className="skeleton-stack">
          <div className="skeleton-line" />
          <div className="skeleton-line skeleton-line--short" />
        </div>
      )}

      {!loading && (!history || history.length === 0) && (
        <p className="empty">No saved recommendations yet. Run “Get recommendation” to build history.</p>
      )}

      {!loading && history?.length > 0 && (
        <ul className="reco-history-list">
          {history.map((h) => (
            <li key={h.id} className="reco-history-item">
              <div className="reco-history-top">
                <span className={`badge badge-${h.riskLevel}`}>{h.riskLevel}</span>
                <span className="field-meta">
                  Score {h.riskScore}
                  {h.createdAt
                    ? ` · ${new Date(h.createdAt).toLocaleString()}`
                    : ''}
                </span>
              </div>
              {h.farmerAdvisory && (
                <p className="reco-history-advisory">{h.farmerAdvisory}</p>
              )}
              {h.explainableReasons?.length > 0 && (
                <ul className="reason-list reco-history-reasons">
                  {h.explainableReasons.slice(0, 3).map((r, i) => (
                    <li key={i}>{r}</li>
                  ))}
                </ul>
              )}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
