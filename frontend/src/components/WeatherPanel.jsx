export default function WeatherPanel({ weather, loading, onRefresh, disabled }) {
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

      {!weather && !loading && (
        <p className="empty">Select a field to load live weather for its location.</p>
      )}

      {loading && <p className="empty">Fetching weather from OpenWeather…</p>}

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
        </div>
      )}
    </section>
  );
}
