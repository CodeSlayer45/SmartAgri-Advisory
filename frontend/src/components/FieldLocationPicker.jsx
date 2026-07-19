import { useCallback, useEffect, useRef, useState } from 'react';
import {
  configureLeafletIcons,
  DEFAULT_CENTER,
  DEFAULT_ZOOM,
  L,
  parseCoord,
} from '../utils/leafletSetup';
import { reverseGeocode, searchPlace } from '../utils/geocode';

export default function FieldLocationPicker({
  latitude,
  longitude,
  locationLabel = '',
  onChange,
  disabled = false,
  compact = false,
}) {
  const mapContainerRef = useRef(null);
  const mapRef = useRef(null);
  const markerRef = useRef(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [searching, setSearching] = useState(false);
  const [gpsLoading, setGpsLoading] = useState(false);
  const [mapError, setMapError] = useState('');

  const lat = parseCoord(latitude, DEFAULT_CENTER.lat);
  const lon = parseCoord(longitude, DEFAULT_CENTER.lon);

  const emit = useCallback(
    (nextLat, nextLon, label) => {
      onChange?.({
        latitude: String(nextLat),
        longitude: String(nextLon),
        location: label ?? locationLabel,
      });
    },
    [onChange, locationLabel]
  );

  const placeMarker = useCallback(
    (map, nextLat, nextLon, fly = true) => {
      if (markerRef.current) {
        markerRef.current.setLatLng([nextLat, nextLon]);
      } else {
        markerRef.current = L.marker([nextLat, nextLon], { draggable: !disabled }).addTo(map);
        if (!disabled) {
          markerRef.current.on('dragend', () => {
            const pos = markerRef.current.getLatLng();
            emit(pos.lat, pos.lng);
            reverseGeocode(pos.lat, pos.lng).then((name) => {
              if (name) emit(pos.lat, pos.lng, name);
            });
          });
        }
      }
      if (fly) map.setView([nextLat, nextLon], Math.max(map.getZoom(), 14));
    },
    [disabled, emit]
  );

  useEffect(() => {
    configureLeafletIcons();
    if (!mapContainerRef.current || mapRef.current) return undefined;

    const map = L.map(mapContainerRef.current, {
      scrollWheelZoom: !disabled,
    }).setView([lat, lon], DEFAULT_ZOOM);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
    }).addTo(map);

    placeMarker(map, lat, lon, false);

    if (!disabled) {
      map.on('click', (e) => {
        const { lat: clickLat, lng: clickLng } = e.latlng;
        placeMarker(map, clickLat, clickLng, false);
        emit(clickLat, clickLng);
        reverseGeocode(clickLat, clickLng).then((name) => {
          if (name) emit(clickLat, clickLng, name);
        });
      });
    }

    mapRef.current = map;
    setTimeout(() => map.invalidateSize(), 200);

    return () => {
      map.remove();
      mapRef.current = null;
      markerRef.current = null;
    };
  }, []);

  useEffect(() => {
    const map = mapRef.current;
    if (!map) return;
    placeMarker(map, lat, lon, false);
  }, [lat, lon, placeMarker]);

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!searchQuery.trim() || disabled) return;
    setSearching(true);
    setMapError('');
    try {
      const hit = await searchPlace(searchQuery);
      if (!hit) {
        setMapError('Place not found. Try a nearby town or district name.');
        return;
      }
      emit(hit.latitude, hit.longitude, hit.displayName);
      const map = mapRef.current;
      if (map) placeMarker(map, hit.latitude, hit.longitude);
    } catch {
      setMapError('Search failed. Check your internet connection.');
    } finally {
      setSearching(false);
    }
  };

  const handleUseGps = () => {
    if (disabled || !navigator.geolocation) {
      setMapError('GPS is not available in this browser.');
      return;
    }
    setGpsLoading(true);
    setMapError('');
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        const { latitude: gLat, longitude: gLon } = pos.coords;
        emit(gLat, gLon, 'Farmer live GPS');
        const map = mapRef.current;
        if (map) placeMarker(map, gLat, gLon);
        setGpsLoading(false);
      },
      () => {
        setMapError('Could not read GPS. Allow location permission and try again.');
        setGpsLoading(false);
      },
      { enableHighAccuracy: true, timeout: 15000 }
    );
  };

  return (
    <div className={`field-map-picker ${compact ? 'field-map-picker--compact' : ''}`}>
      <p className="field-meta map-picker-hint">
        Tap the map to pin your field center. Weather and recommendations use these coordinates.
      </p>

      {!disabled && (
        <div className="map-toolbar">
          <form className="map-search" onSubmit={handleSearch}>
            <input
              type="search"
              placeholder="Search village, city, or landmark…"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              disabled={searching}
            />
            <button type="submit" className="btn btn-secondary btn-sm" disabled={searching}>
              {searching ? '…' : 'Find'}
            </button>
          </form>
          <button
            type="button"
            className="btn btn-ghost btn-sm"
            onClick={handleUseGps}
            disabled={gpsLoading}
          >
            {gpsLoading ? 'Locating…' : 'Use my GPS'}
          </button>
        </div>
      )}

      {mapError && <p className="map-error">{mapError}</p>}

      <div
        ref={mapContainerRef}
        className="field-map-canvas"
        aria-label="Field location map"
      />

      <div className="map-coords-row">
        <label>
          Latitude
          <input
            type="number"
            step="any"
            disabled={disabled}
            value={latitude}
            onChange={(e) => emit(e.target.value, String(lon))}
          />
        </label>
        <label>
          Longitude
          <input
            type="number"
            step="any"
            disabled={disabled}
            value={longitude}
            onChange={(e) => emit(String(lat), e.target.value)}
          />
        </label>
      </div>

      {locationLabel && (
        <p className="field-meta map-place-label">
          <strong>Place:</strong> {locationLabel}
        </p>
      )}
    </div>
  );
}
