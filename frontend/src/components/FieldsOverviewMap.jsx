import { useEffect, useRef } from 'react';
import {
  configureLeafletIcons,
  DEFAULT_CENTER,
  DEFAULT_ZOOM,
  L,
  parseCoord,
} from '../utils/leafletSetup';

export default function FieldsOverviewMap({ fields, selectedFieldId, onSelectField }) {
  const mapContainerRef = useRef(null);
  const mapRef = useRef(null);
  const markersLayerRef = useRef(null);

  useEffect(() => {
    configureLeafletIcons();
    if (!mapContainerRef.current || mapRef.current) return undefined;

    const map = L.map(mapContainerRef.current).setView(
      [DEFAULT_CENTER.lat, DEFAULT_CENTER.lon],
      DEFAULT_ZOOM
    );

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; OpenStreetMap',
    }).addTo(map);

    markersLayerRef.current = L.layerGroup().addTo(map);
    mapRef.current = map;

    setTimeout(() => map.invalidateSize(), 250);

    return () => {
      map.remove();
      mapRef.current = null;
      markersLayerRef.current = null;
    };
  }, []);

  useEffect(() => {
    const map = mapRef.current;
    const layer = markersLayerRef.current;
    if (!map || !layer) return;

    layer.clearLayers();
    const withCoords = (fields || []).filter(
      (f) => f.latitude != null && f.longitude != null
    );

    if (withCoords.length === 0) return;

    const bounds = [];

    withCoords.forEach((f) => {
      const lat = parseCoord(f.latitude, null);
      const lon = parseCoord(f.longitude, null);
      if (lat == null || lon == null) return;

      const isSelected = f.id === selectedFieldId;
      const marker = L.marker([lat, lon])
        .bindPopup(
          `<strong>${f.fieldName}</strong><br/>${f.cropName} · ${f.acreage} ac`
        )
        .addTo(layer);

      marker.on('click', () => onSelectField?.(f.id));
      if (isSelected) marker.openPopup();
      bounds.push([lat, lon]);
    });

    if (bounds.length === 1) {
      map.setView(bounds[0], 14);
    } else if (bounds.length > 1) {
      map.fitBounds(bounds, { padding: [40, 40], maxZoom: 14 });
    }
  }, [fields, selectedFieldId, onSelectField]);

  if (!fields?.length) {
    return (
      <section className="card card--wide fields-map-card">
        <h2>Field map</h2>
        <p className="empty">Register a field with map location to see all plots here.</p>
      </section>
    );
  }

  const mappedCount = fields.filter((f) => f.latitude != null && f.longitude != null).length;

  return (
    <section className="card card--wide fields-map-card">
      <div className="card-header-row">
        <div>
          <h2>Field map</h2>
          <p className="field-meta card-intro" style={{ margin: 0 }}>
            {mappedCount} of {fields.length} fields on map — click a marker to select.
          </p>
        </div>
      </div>
      <div ref={mapContainerRef} className="fields-overview-map" aria-label="All farm fields map" />
    </section>
  );
}
