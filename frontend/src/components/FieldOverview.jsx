export default function FieldOverview({ field }) {
  if (!field) {
    return (
      <div className="overview-banner overview-banner--empty">
        <p>Select a field from the sidebar to view weather, activities, and AI advisory.</p>
      </div>
    );
  }

  return (
    <div className="overview-banner">
      <div>
        <p className="overview-label">Active field</p>
        <h2 className="overview-title">{field.fieldName}</h2>
      </div>
      <div className="overview-stats">
        <div className="stat-tile">
          <span className="stat-label">Crop</span>
          <span className="stat-value">{field.cropName}</span>
        </div>
        <div className="stat-tile">
          <span className="stat-label">Age</span>
          <span className="stat-value">{field.cropAgeDays} days</span>
        </div>
        <div className="stat-tile">
          <span className="stat-label">Area</span>
          <span className="stat-value">{field.acreage} acre</span>
        </div>
        <div className="stat-tile">
          <span className="stat-label">Location</span>
          <span className="stat-value stat-value--sm">{field.location || '—'}</span>
        </div>
      </div>
    </div>
  );
}
