import { useEffect, useState } from 'react';
import FieldLocationPicker from './FieldLocationPicker';

const emptyBasics = {
  fieldName: '',
  cropName: '',
  acreage: '1',
  sowingDate: '',
};

const emptyLocation = {
  locationMode: 'AUTO_GPS',
  location: '',
  latitude: '',
  longitude: '',
};

export default function FieldEditModal({ field, open, onClose, onSaveBasics, onSaveLocation, saving }) {
  const [tab, setTab] = useState('basics');
  const [basics, setBasics] = useState(emptyBasics);
  const [location, setLocation] = useState(emptyLocation);

  useEffect(() => {
    if (!field || !open) return;
    setBasics({
      fieldName: field.fieldName || '',
      cropName: field.cropName || '',
      acreage: String(field.acreage ?? '1'),
      sowingDate: String(field.sowingDate || '').slice(0, 10),
    });
    setLocation({
      locationMode: field.locationMode || 'AUTO_GPS',
      location: field.location || '',
      latitude: field.latitude != null ? String(field.latitude) : '',
      longitude: field.longitude != null ? String(field.longitude) : '',
    });
    setTab('basics');
  }, [field, open]);

  if (!open || !field) return null;

  const handleBasics = (e) => {
    e.preventDefault();
    onSaveBasics({
      fieldName: basics.fieldName,
      cropName: basics.cropName,
      acreage: Number(basics.acreage),
      sowingDate: basics.sowingDate,
    });
  };

  const handleLocation = (e) => {
    e.preventDefault();
    const body = {
      locationMode: location.locationMode,
      location: location.locationMode === 'MANUAL' ? location.location : field.location,
      latitude:
        location.locationMode === 'AUTO_GPS' ? Number(location.latitude) : undefined,
      longitude:
        location.locationMode === 'AUTO_GPS' ? Number(location.longitude) : undefined,
    };
    onSaveLocation(body);
  };

  return (
    <div className="modal-backdrop" role="presentation" onClick={onClose}>
      <div
        className="modal-card"
        role="dialog"
        aria-modal="true"
        aria-labelledby="edit-field-title"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="modal-header">
          <h2 id="edit-field-title">Edit field</h2>
          <button type="button" className="btn btn-ghost btn-sm" onClick={onClose} aria-label="Close">
            ✕
          </button>
        </div>

        <div className="tab-bar modal-tabs">
          <button
            type="button"
            className={`tab ${tab === 'basics' ? 'active' : ''}`}
            onClick={() => setTab('basics')}
          >
            Crop & details
          </button>
          <button
            type="button"
            className={`tab ${tab === 'location' ? 'active' : ''}`}
            onClick={() => setTab('location')}
          >
            Location
          </button>
        </div>

        {tab === 'basics' && (
          <form className={`form-grid form-grid--2 ${saving ? 'form-disabled' : ''}`} onSubmit={handleBasics}>
            <label>
              Field name
              <input
                required
                disabled={saving}
                value={basics.fieldName}
                onChange={(e) => setBasics({ ...basics, fieldName: e.target.value })}
              />
            </label>
            <label>
              Crop
              <input
                required
                disabled={saving}
                value={basics.cropName}
                onChange={(e) => setBasics({ ...basics, cropName: e.target.value })}
              />
            </label>
            <label>
              Acreage
              <input
                type="number"
                step="0.1"
                min="0.1"
                required
                disabled={saving}
                value={basics.acreage}
                onChange={(e) => setBasics({ ...basics, acreage: e.target.value })}
              />
            </label>
            <label>
              Sowing date
              <input
                type="date"
                required
                disabled={saving}
                value={basics.sowingDate}
                onChange={(e) => setBasics({ ...basics, sowingDate: e.target.value })}
              />
            </label>
            <div className="form-actions span-2">
              <button type="button" className="btn btn-secondary" onClick={onClose} disabled={saving}>
                Cancel
              </button>
              <button type="submit" className="btn btn-primary" disabled={saving}>
                {saving ? 'Saving…' : 'Save details'}
              </button>
            </div>
          </form>
        )}

        {tab === 'location' && (
          <form className={`form-grid form-grid--2 ${saving ? 'form-disabled' : ''}`} onSubmit={handleLocation}>
            <label className="span-2">
              Location mode
              <select
                disabled={saving}
                value={location.locationMode}
                onChange={(e) => setLocation({ ...location, locationMode: e.target.value })}
              >
                <option value="AUTO_GPS">Pin on map (GPS coordinates)</option>
                <option value="MANUAL">Place name (geocoded on save)</option>
              </select>
            </label>
            {location.locationMode === 'MANUAL' && (
              <label className="span-2">
                Location name
                <input
                  required
                  disabled={saving}
                  value={location.location}
                  onChange={(e) => setLocation({ ...location, location: e.target.value })}
                  placeholder="Kolhapur, Maharashtra, India"
                />
              </label>
            )}

            <div className="span-2">
              <FieldLocationPicker
                latitude={location.latitude}
                longitude={location.longitude}
                locationLabel={location.location}
                disabled={saving}
                onChange={({ latitude, longitude, location: label }) =>
                  setLocation((prev) => ({
                    ...prev,
                    latitude,
                    longitude,
                    location: label || prev.location,
                    locationMode:
                      prev.locationMode === 'MANUAL' && !label ? 'MANUAL' : 'AUTO_GPS',
                  }))
                }
              />
            </div>
            <div className="form-actions span-2">
              <button type="button" className="btn btn-secondary" onClick={onClose} disabled={saving}>
                Cancel
              </button>
              <button type="submit" className="btn btn-primary" disabled={saving}>
                {saving ? 'Saving…' : 'Update location'}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}
