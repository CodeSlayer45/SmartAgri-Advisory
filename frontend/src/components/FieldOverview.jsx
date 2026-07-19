import { cropTip } from '../utils/cropTips';

export default function FieldOverview({
  field,
  onEditField,
  onDeleteField,
  deleteDisabled,
  fungicideDays,
}) {
  if (!field) {
    return (
      <div className="overview-banner overview-banner--empty">
        <p>Select a field from the sidebar to view weather, activities, and AI advisory.</p>
      </div>
    );
  }

  const tip = cropTip(field.cropName);
  const sprayLabel =
    fungicideDays === -1
      ? 'No fungicide logged'
      : fungicideDays === 0
        ? 'Fungicide today'
        : `${fungicideDays} days since fungicide`;

  return (
    <div className="overview-banner">
      <div className="overview-banner-main">
        <div>
          <p className="overview-label">Active field</p>
          <h2 className="overview-title">{field.fieldName}</h2>
          {tip && <p className="crop-tip">{tip}</p>}
        </div>
        <div className="overview-actions">
          {onEditField && (
            <button type="button" className="btn btn-secondary btn-sm" onClick={onEditField}>
              Edit field
            </button>
          )}
          {onDeleteField && (
            <button
              type="button"
              className="btn btn-danger-ghost btn-sm overview-delete"
              onClick={() => onDeleteField(field)}
              disabled={deleteDisabled}
            >
              Delete field
            </button>
          )}
        </div>
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
          <span className="stat-label">Spray status</span>
          <span
            className={`stat-value stat-value--sm ${fungicideDays > 10 || fungicideDays === -1 ? 'stat-warn' : ''}`}
          >
            {sprayLabel}
          </span>
        </div>
        <div className="stat-tile">
          <span className="stat-label">Location</span>
          <span className="stat-value stat-value--sm">{field.location || '—'}</span>
        </div>
      </div>
    </div>
  );
}
