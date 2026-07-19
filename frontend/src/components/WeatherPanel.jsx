function formatTime(iso) {
  if (!iso) return null;
  try {
    return new Date(iso).toLocaleTimeString([], { hour: 'numeric', minute: '2-digit' });
  } catch {
    return null;
  }
}

function WeatherSkeleton() {
  return (
    <div className="weather-grid weather-grid--skeleton" aria-hidden>
      {[1, 2, 3, 4, 5, 6].map((n) => (
        <div key={n} className="weather-stat">
          <div className="skeleton-line skeleton-line--label" />
          <div className="skeleton-line" />
        </div>
      ))}
    </div>
  );
}

export default function WeatherPanel({
  weather,
  loading,
  onRefresh,
  disabled,
  lastUpdatedAt,
}) {
  const updatedLabel = formatTime(lastUpdatedAt);

  return (
    <section className="card">
      <div className="card-header-row">
        <h2>Weather intelligence</h2>
        <button
          type="button"
          className="btn btn-secondary btn-sm"
          onClick={onRefresh}
          disabled={disabled || loading}
        >
          {loading ? 'Loading…' : 'Refresh'}
        </button>
      </div>

      {updatedLabel && !loading && weather && (
        <p className="field-meta last-updated">Last refreshed at {updatedLabel}</p>
      )}

      {!weather && !loading && (
        <p className="empty">Select a field to load live weather for its location.</p>
      )}

      {loading && <WeatherSkeleton />}

      {weather && !loading && (
        <div className="weather-grid">
          <div className="weather-stat">
            <span className="weather-label">Summary</span>
            <span className="weather-value">{weather.summary}</span>
          </div>
          <div className="weather-stat">
            <span className="weather-label">Temperature</span>
            <span className="weather-value">{weather.temperatureC}°C</span>
          </div>
          <div className="weather-stat">
            <span className="weather-label">Humidity</span>
            <span className="weather-value">{weather.humidityPercent}%</span>
          </div>
          <div className="weather-stat">
            <span className="weather-label">Rainfall (1h)</span>
            <span className="weather-value">{weather.rainfallMm} mm</span>
          </div>
          <div className="weather-stat">
            <span className="weather-label">Wind</span>
            <span className="weather-value">{weather.windSpeedMs} m/s</span>
          </div>
          <div className="weather-stat">
            <span className="weather-label">Source</span>
            <span className="weather-value">{weather.source}</span>
          </div>
          {weather.source === 'mock' && (
            <p className="weather-hint span-2">
              Set <code>OPENWEATHER_API_KEY</code> on the server and restart the backend for live
              weather.
            </p>
          )}
        </div>
      )}
    </section>
  );
}
