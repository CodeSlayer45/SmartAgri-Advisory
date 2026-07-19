# Smart Agri Advisory

[![CI](https://github.com/YOUR_USERNAME/smart-agri-advisory/actions/workflows/ci.yml/badge.svg)](https://github.com/YOUR_USERNAME/smart-agri-advisory/actions/workflows/ci.yml)
[![Java 17](https://img.shields.io/badge/Java-17-blue)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-brightgreen)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-61dafb)](https://react.dev/)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)

Smart Agri Advisory digitizes the farmer's notebook — track crops, fertilizers, pesticides, weather, and get disease-risk recommendations in one dashboard.

The risk engine is rule-based and transparent. OpenAI is optional and only rephrases the output in farmer-friendly language — it never changes the actual risk score.

---


## Challenges I Faced

**1. Designing the rule engine thresholds**

The risk scoring logic in `RecommendationServiceImpl` was the hardest part. Initially I set the humidity threshold too low (60%) and almost every field showed HIGH risk. I had to research actual crop disease conditions — for example, powdery mildew needs 80%+ humidity, not 60%. Getting these thresholds right took several iterations of testing against real OpenWeather data.

**2. Parsing OpenWeather API response safely**

The OpenWeather API returns a deeply nested JSON with `main.temp`, `main.humidity`, `rain.1h` (which can be null if no rain), `wind.speed`, `weather[0].description`. The tricky part was handling null values gracefully — especially the `rain.1h` field which doesn't exist at all when it's dry. I wrote the `toD()` helper to safely convert any value to double without throwing.

**3. Keeping OpenAI optional without breaking the flow**

I wanted AI enhancement to be completely optional — the app should work 100% without an OpenAI key. This meant structuring the code so that `OpenAiWordingServiceImpl` returns an `Optional<String>` and the recommendation service falls back to the plain rule output if AI is disabled or fails. The `@Value("${openai.api.enabled:false}")` pattern was new to me — understanding how Spring Boot conditionally injects configs took some time.

**4. Custom API key security**

Instead of Spring Security (which felt overkill for this), I implemented `ApiKeyInterceptor` using Spring's `HandlerInterceptor`. It checks the `X-API-KEY` header on every request. The tricky part was handling the edge case where `APP_API_KEY` env variable isn't set — the interceptor returns 500 with a clear message instead of a confusing auth error.

**5. Frontend map integration with Leaflet**

Getting Leaflet maps to work inside a React component with Vite was harder than expected. The leaflet CSS needs to be imported properly, and the map container needs a fixed height in CSS. GPS coordinates from the Capacitor geolocation plugin sometimes return `null` on Android — I had to add fallback to manual location entry.

**6. Calculating "days since last spray" correctly**

The `daysSinceLastSpray()` method in `RecommendationServiceImpl` looks through all farm activities, filters by type (fungicide/fertilizer/pesticide), finds the latest date, and calculates days difference. The edge cases were: never sprayed (return -1), multiple sprays of same type (pick the latest), and activity dates in the future (edge case from incorrect data entry). Took me a while to get this right with Java streams and `Comparator.naturalOrder()`.

---

## What I Learned

- **Spring Boot constructor injection** — using interfaces (`RecommendationService`, `WeatherService`) makes the code testable with Mockito. Didn't know this at first.
- **JPA repositories** — `findByFieldIdOrderByCreatedAtDesc()` style method names. Spring Data generates the query from the method name itself. Mind-blowing.
- **Optional<> for graceful fallback** — the OpenAI service returns `Optional.empty()` instead of throwing exceptions. The caller handles the fallback cleanly.
- **Environment variables for secrets** — API keys go in env vars, not in code. `@Value("${openweather.api.key:}")` with default empty string.
- **Cron expressions** — configuring advisory alerts to run every 6 hours. `0 0 */6 * * *` — looked like gibberish at first.
- **Reading REST API docs carefully** — OpenWeather returns rain data only when it's raining. The field literally doesn't exist in dry weather.
- **DTO vs Entity separation** — keeping `RecommendationResponse` (DTO) separate from `RecommendationHistory` (JPA entity). Learned this because I initially tried to return entities directly and got JSON serialization issues.
- **Leaflet map container height** — if you don't set a fixed height in CSS, the map is invisible. Spent an hour debugging this.

## Highlights

- Field management with manual location search or GPS coordinates
- Field-level weather, crop age, and activity tracking
- Explainable disease-risk scores and recommended actions
- Scheduled and on-demand advisory alerts
- AI tools for crop-photo analysis, farm questions, market guidance, weather impact, and growth-stage reports
- Recommendation history per field
- React dashboard with Capacitor Android support
- Docker, Render, Swagger, Postman, and Actuator support

## Technology

| Layer | Stack |
| --- | --- |
| Backend | Java 17, Spring Boot, Spring Data JPA, Bean Validation |
| Database | MySQL |
| Frontend | React, Vite, Leaflet |
| Mobile | Capacitor for Android |
| Operations | Docker, Render Blueprint, Spring Boot Actuator |

## Architecture

```text
React / Capacitor dashboard
        |
        | HTTP + X-API-KEY
        v
Spring Boot REST API
        |
        +-- Recommendation and alert services
        +-- Weather and optional OpenAI integrations
        v
MySQL
```

## Prerequisites

- Java 17 or newer
- Maven 3.9 or newer
- MySQL 8 or newer
- Node.js 18 or newer
- OpenWeather API key for live weather and automatic geocoding
- OpenAI API key only when optional AI enhancement is required

## Quick Start

### 1. Create the local database

Open MySQL as an administrative user:

```powershell
mysql -u root -p
```

Run this once:

```sql
CREATE DATABASE IF NOT EXISTS smart_agri
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'smartagri_user'@'localhost'
  IDENTIFIED BY 'choose-a-strong-local-password';

GRANT ALL PRIVILEGES ON smart_agri.* TO 'smartagri_user'@'localhost';
FLUSH PRIVILEGES;
```

The backend creates and updates its tables automatically for the local profile.

### 2. Start the backend

From the repository root:

```powershell
mvn spring-boot:run
```

The API starts at `http://localhost:8081`. Local API requests use this header unless `APP_API_KEY` is configured:

```text
X-API-KEY: change-me-local
```

### 3. Start the dashboard

In a new terminal:

```powershell
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173` and enter the API key in the dashboard header if you changed it.

## Configuration

Use environment variables for local overrides and all production secrets.

| Variable | Purpose | Local default |
| --- | --- | --- |
| `SERVER_PORT` | Backend port | `8081` |
| `DB_URL` | JDBC MySQL connection string | `jdbc:mysql://localhost:3306/smart_agri?createDatabaseIfNotExist=true` |
| `DB_USERNAME` | MySQL username | `smartagri_user` |
| `DB_PASSWORD` | MySQL password | required |
| `APP_API_KEY` | Required API request key | `change-me-local` |
| `OPENWEATHER_API_KEY` | Live weather and geocoding key | empty |
| `OPENAI_API_KEY` | Optional OpenAI key | empty |
| `OPENAI_ENABLED` | Enables optional OpenAI responses | `false` |
| `OPENAI_MODEL` | OpenAI model | `gpt-4o-mini` |
| `ADVISORY_ALERT_CRON` | Alert scan schedule | every 6 hours |
| `CORS_ALLOWED_ORIGINS` | Comma-separated production dashboard origins | local Vite origins |

Example PowerShell session:

```powershell
$env:OPENWEATHER_API_KEY = "your-openweather-key"
$env:DB_PASSWORD = "your-local-mysql-password"
$env:OPENAI_API_KEY = "your-openai-key"
$env:OPENAI_ENABLED = "true"
$env:APP_API_KEY = "replace-with-a-long-random-value"
mvn spring-boot:run
```

Never commit real keys or production credentials.

Use [.env.example](.env.example) as a local configuration reference. Its copied `.env` file is ignored by Git, but Spring Boot requires these values to be exported in your shell or configured in your IDE.

## API Reference

Swagger UI: `http://localhost:8081/swagger-ui.html`

OpenAPI JSON: `http://localhost:8081/v3/api-docs`

Health check: `http://localhost:8081/actuator/health`

All `/api/**` routes require `X-API-KEY`.

| Area | Routes |
| --- | --- |
| Fields | `POST /api/field`, `GET /api/field`, `GET/PATCH/DELETE /api/field/{fieldId}`, `PATCH /api/field/{fieldId}/location` |
| Activities | `POST /api/activity`, `GET /api/activity/field/{fieldId}`, `PUT/DELETE /api/activity/{activityId}` |
| Weather | `GET /api/weather/field/{fieldId}` |
| Recommendations | `POST /api/smart/recommend/{fieldId}`, `GET /api/smart/recommend/history/{fieldId}` |
| Alerts | `GET /api/alerts/field/{fieldId}`, `POST /api/alerts/scan`, `PATCH /api/alerts/{alertId}/dismiss` |
| AI tools | `POST /api/ai/analyze-crop-health`, `POST /api/ai/ask`, `GET /api/ai/market-price`, `GET /api/ai/weather-impact/{fieldId}`, `GET /api/ai/growth-stage/{fieldId}` |
| Insurance | `POST /api/insurance/claim/{fieldId}`, `GET /api/insurance/claims/{fieldId}` |
| Devices | `POST /api/devices/register`, `POST /api/devices/unregister` |

Example field request:

```json
{
  "fieldName": "North Plot",
  "cropName": "Wheat",
  "acreage": 2.5,
  "location": "Kolhapur, Maharashtra, India",
  "locationMode": "MANUAL",
  "sowingDate": "2026-06-01"
}
```

For ready-to-import examples, use [docs/smart-agri-postman-collection.json](docs/smart-agri-postman-collection.json).

## Testing

The project includes automated tests for both backend and frontend, plus a CI pipeline that runs them on every push.

### Backend tests (Java / JUnit 5 + Mockito)

```powershell
mvn clean test
```

Tests cover:
- Recommendation engine — risk scoring logic, weather + activity analysis, edge cases
- Alert service — scheduled scan logic
- OpenAI wording service — fallback behavior
- Controller layer — HTTP request/response validation

### Frontend tests (Vitest + Testing Library)

```powershell
cd frontend
npm test          # single run
npm run test:watch  # watch mode
```

Tests cover:
- `cropTips` — crop-specific advisory tips for all supported crops, case-insensitivity, partial name matching, null/empty handling
- `spray` — date formatting, days-since-last-spray calculation, overdue detection, edge cases

### CI pipeline

Every push to `main` and every pull request triggers [GitHub Actions](.github/workflows/ci.yml) to:
1. Start a MySQL 8 container
2. Run all backend tests with Maven
3. Install frontend dependencies
4. Run all frontend tests with Vitest
5. Build the production frontend bundle


## Deployment

The repository contains a [Dockerfile](Dockerfile) and [Render Blueprint](render.yaml). Production requires a hosted MySQL database plus `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `APP_API_KEY`, and `CORS_ALLOWED_ORIGINS`.

For detailed deployment instructions, see [docs/CLOUD-DEPLOY-RENDER.md](docs/CLOUD-DEPLOY-RENDER.md).

When deploying the dashboard separately, set its build-time API URL:

```env
VITE_API_BASE=https://your-api-domain.example
```

Add that dashboard URL to `CORS_ALLOWED_ORIGINS` on the API.

## Project Structure

```text
src/                 Spring Boot API, services, persistence, and tests
frontend/            React dashboard and Capacitor Android project
docs/                Deployment guide and Postman collection
Dockerfile           Production backend container image
render.yaml          Render deployment configuration
```

## Notes

- Weather and AI features fall back where applicable when optional keys are unavailable.
- AI outputs are advisory only; validate them against local crop conditions and approved agricultural guidance before acting.
