function formatTime(iso) {
  if (!iso) return null;
  try {
    return new Date(iso).toLocaleTimeString([], { hour: 'numeric', minute: '2-digit' });
  } catch {
    return null;
  }
}

function getWeatherEmoji(summary) {
  if (!summary) return '🌤️';
  const s = summary.toLowerCase();
  if (s.includes('rain') || s.includes('drizzle') || s.includes('shower')) return '🌧️';
  if (s.includes('thunder') || s.includes('storm')) return '⛈️';
  if (s.includes('cloud') || s.includes('overcast')) return '☁️';
  if (s.includes('fog') || s.includes('mist') || s.includes('haze')) return '🌫️';
  if (s.includes('snow') || s.includes('ice')) return '❄️';
  if (s.includes('clear') || s.includes('sunny')) return '☀️';
  if (s.includes('partly') || s.includes('few')) return '⛅';
  return '🌤️';
}

function WeatherSkeleton() {
  return (
    <div className="weather-grid" aria-hidden>
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
  const weatherEmoji = getWeatherEmoji(weather?.summary);

  return (
    <section className="card">
      <div className="card-header-row">
        <h2>🌤️ Weather intelligence</h2>
        <button
          type="button"
          className="btn btn-secondary btn-sm"
          onClick={onRefresh}
          disabled={disabled || loading}
        >
          {loading ? '⟳ Loading…' : '↻ Refresh'}
        </button>
      </div>

      {updatedLabel && !loading && weather && (
        <p className="field-meta last-updated">Last refreshed at {updatedLabel}</p>
      )}

      {!weather && !loading && (
        <p className="empty" style={{padding:'1.5rem 0'}}>
          🌿 Select a field to load live weather for its location.
        </p>
      )}

      {loading && <WeatherSkeleton />}

      {weather && !loading && (
        <>
          <div className="weather-icon-row">
            <span className="weather-icon-large" aria-label={weather.summary}>{weatherEmoji}</span>
            <div>
              <span className="weather-temp-large">{weather.temperatureC}°C</span>
              <div className="weather-desc">{weather.summary}</div>
            </div>
          </div>
          <div className="weather-grid">
            <div className="weather-stat">
              <span className="weather-label">💧 Humidity</span>
              <span className="weather-value">{weather.humidityPercent}%</span>
            </div>
            <div className="weather-stat">
              <span className="weather-label">🌧️ Rainfall (1h)</span>
              <span className="weather-value">{weather.rainfallMm} mm</span>
            </div>
            <div className="weather-stat">
              <span className="weather-label">💨 Wind</span>
              <span className="weather-value">{weather.windSpeedMs} m/s</span>
            </div>
            {weather.source && (
              <div className="weather-stat">
                <span className="weather-label">📡 Source</span>
                <span className="weather-value" style={{fontSize:'0.85rem',textTransform:'capitalize'}}>{weather.source}</span>
              </div>
            )}
          </div>
          {weather.source === 'mock' && (
            <p className="weather-hint span-2" style={{marginTop:'0.75rem'}}>
              Set <code>OPENWEATHER_API_KEY</code> on the server and restart the backend for live weather data.
            </p>
          )}
        </>
      )}
    </section>
  );
}
