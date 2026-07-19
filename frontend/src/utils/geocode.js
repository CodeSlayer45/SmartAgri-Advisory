const NOMINATIM = 'https://nominatim.openstreetmap.org';

const headers = {
  Accept: 'application/json',
  'Accept-Language': 'en',
};

/** Search place name → { lat, lon, displayName } */
export async function searchPlace(query) {
  if (!query?.trim()) return null;
  const url = `${NOMINATIM}/search?format=json&q=${encodeURIComponent(query.trim())}&limit=1`;
  const res = await fetch(url, { headers });
  if (!res.ok) throw new Error('Place search failed');
  const data = await res.json();
  if (!data?.length) return null;
  const hit = data[0];
  return {
    latitude: Number(hit.lat),
    longitude: Number(hit.lon),
    displayName: hit.display_name,
  };
}

/** Reverse geocode coordinates → place label */
export async function reverseGeocode(lat, lon) {
  const url = `${NOMINATIM}/reverse?format=json&lat=${lat}&lon=${lon}&zoom=14`;
  const res = await fetch(url, { headers });
  if (!res.ok) return null;
  const data = await res.json();
  return data?.display_name || null;
}
