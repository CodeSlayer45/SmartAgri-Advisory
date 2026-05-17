# Smart Agri Advisory Backend

AI-powered smart agricultural advisory platform built with Spring Boot.

## Implemented Improvements
- Rule-based AI risk scoring with explainable reasons.
- Better spray-day logic (days since last fungicide spray).
- Weather linked to a provided `fieldId` using field lat/lon.
- Automatic weather/disease alert scan (scheduler + manual endpoint).
- Cleaner layered architecture (Controller -> Service -> Repository).
- DTO mapping, validation, and centralized error handling.
- OpenAPI docs and Postman collection with example payloads.
- **React dashboard** (`frontend/`) with fields, weather, activities, recommendations, alerts.
- Unit + WebMvc tests for core services and API security.
- **Optional OpenAI wording** on recommendations (rules decide risk; OpenAI writes farmer-friendly text).

## Run (Local)

### Backend
1. Update DB and API key in `src/main/resources/application.properties` (or env vars).
2. Start MySQL.
3. Run:
   ```bash
   mvn spring-boot:run
   ```

### React dashboard (Step 1 UI)
```bash
cd frontend
npm install
npm run dev
```
Open http://localhost:5173 — see `frontend/README.md`.

## API Base URL
`http://localhost:8081/api`

## API Security
All `/api/**` endpoints require this header:
`X-API-KEY: <your_app_api_key>`

Swagger and `/actuator/health` stay public for diagnostics.

## Endpoints
- `POST /field`
- `GET /field`
- `PATCH /field/{fieldId}/location`
- `POST /activity`
- `GET /activity/field/{fieldId}`
- `GET /weather/field/{fieldId}`
- `POST /smart/recommend/{fieldId}`
- `GET /alerts/field/{fieldId}`
- `POST /alerts/scan`

## Swagger
- UI: `http://localhost:8081/swagger-ui.html`
- JSON: `http://localhost:8081/v3/api-docs`

## Example Payloads

### Create Field
`POST /api/field`
AUTO_GPS mode payload:
```json
{
  "fieldName": "Field 1",
  "cropName": "Marigold",
  "acreage": 1.0,
  "location": "Farmer live GPS",
  "locationMode": "AUTO_GPS",
  "latitude": 16.8524,
  "longitude": 74.5815,
  "sowingDate": "2026-04-01"
}
```

Headers:
`X-API-KEY: change-me-local` (or your configured `APP_API_KEY` value)

MANUAL mode payload (location name to geocode):
```json
{
  "fieldName": "Field 2",
  "cropName": "Wheat",
  "acreage": 2.0,
  "location": "Kolhapur, Maharashtra, India",
  "locationMode": "MANUAL",
  "sowingDate": "2026-04-10"
}
```

### Update Field Location Mode
`PATCH /api/field/1/location`
```json
{
  "locationMode": "MANUAL",
  "location": "Sangli, Maharashtra, India"
}
```

### Create Activity
`POST /api/activity`
```json
{
  "fieldId": 1,
  "activityDate": "2026-05-01",
  "activityType": "fungicide",
  "inputName": "Mancozeb",
  "notes": "Preventive spray"
}
```

## Production Hardening Added
- Environment-variable driven secrets and runtime configuration.
- Dedicated production profile: `application-prod.properties`.
- API key interceptor for `/api/**`.
- Actuator health/info/metrics exposure for monitoring.
- Dockerfile for container deployment.

## Environment Variables (Recommended)
- `SERVER_PORT` (default `8081` in prod profile)
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `OPENWEATHER_API_KEY`
- `OPENAI_API_KEY` (optional, for Step 2 farmer text)
- `OPENAI_ENABLED` (`true` to enable OpenAI wording)
- `OPENAI_MODEL` (default `gpt-4o-mini`)
- `APP_API_KEY`
- `ADVISORY_ALERT_CRON` (optional)

## Enable OpenAI wording (Step 2)

In `application.properties` or environment variables:

```properties
openai.api.key=YOUR_OPENAI_API_KEY
openai.api.enabled=true
openai.api.model=gpt-4o-mini
```

Or PowerShell before run:

```powershell
$env:OPENAI_API_KEY="sk-..."
$env:OPENAI_ENABLED="true"
mvn spring-boot:run
```

When enabled, `POST /api/smart/recommend/{fieldId}` returns:
- same `riskLevel`, `riskScore`, reasons, and rule `recommendations`
- plus `aiEnhanced: true` and `farmerAdvisory` (plain-language summary)

If OpenAI is disabled or fails, the API still works with rule-only output (`aiEnhanced: false`).

## Run in Production Profile
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## Docker Deployment
Build image:
```bash
docker build -t smart-agri-advisory:latest .
```

Run container:
```bash
docker run -d --name smart-agri-advisory -p 8081:8081 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SERVER_PORT=8081 \
  -e DB_URL="jdbc:mysql://<host>:3306/smart_agri" \
  -e DB_USERNAME="<db_user>" \
  -e DB_PASSWORD="<db_password>" \
  -e OPENWEATHER_API_KEY="<openweather_key>" \
  -e APP_API_KEY="<strong_api_key>" \
  smart-agri-advisory:latest
```

## Quick Deploy Notes
- **Render (recommended):** full guide → [docs/CLOUD-DEPLOY-RENDER.md](docs/CLOUD-DEPLOY-RENDER.md)
- **Blueprint:** push repo with `render.yaml` → Render → New Blueprint.
- **AWS EC2:** install Docker, run same container command as local Docker section.
- **Health check endpoint:** `/actuator/health`.

### OpenAI Step 2 (local — already in application.properties)

```properties
openai.api.key=${OPENAI_API_KEY:sk-your-openai-key}
openai.api.enabled=true
openai.api.model=gpt-4o-mini
```

Replace `sk-your-openai-key` with your real OpenAI key before demo.
