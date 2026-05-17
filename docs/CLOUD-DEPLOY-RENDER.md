# Cloud deploy on Render (step by step)

Deploy the **Spring Boot API** with Docker. MySQL must be hosted separately (Railway, Aiven, PlanetScale, or your VPS).

---

## Part A — Prepare secrets (do this first)

1. **OpenAI key** — replace `sk-your-openai-key` in `application.properties` locally, or set only in Render (recommended for cloud).
2. **OpenWeather key** — from https://home.openweathermap.org/api_keys
3. **MySQL cloud database** — create database `smart_agri` and a user with full access.
4. **APP_API_KEY** — long random string for Postman/mobile (Render can auto-generate via `render.yaml`).

Example JDBC URL format:

```text
jdbc:mysql://YOUR_MYSQL_HOST:3306/smart_agri?createDatabaseIfNotExist=true&useSSL=true&serverTimezone=UTC
```

---

## Part B — Push code to GitHub

1. Create repo on GitHub (e.g. `smart-agri-advisory`).
2. In project folder:

```powershell
cd C:\Users\ARYAN\project\smart-agri-advisory
git init
git add .
git commit -m "Smart agri advisory - production ready"
git branch -M main
git remote add origin https://github.com/YOUR_USER/smart-agri-advisory.git
git push -u origin main
```

---

## Part C — Create MySQL (example: Railway)

1. Go to https://railway.app → New Project → **MySQL**.
2. Open MySQL service → **Connect** → copy host, port, user, password.
3. Build `DB_URL` as above.

---

## Part D — Deploy API on Render

1. Go to https://dashboard.render.com → **New +** → **Blueprint** (if using `render.yaml`)  
   **OR** **New +** → **Web Service** → connect GitHub repo.

2. Settings:
   - **Runtime:** Docker
   - **Root directory:** (leave blank if repo root)
   - **Dockerfile path:** `./Dockerfile`
   - **Health check path:** `/actuator/health`

3. **Environment variables** (Render dashboard → Environment):

| Key | Value |
|-----|--------|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DB_URL` | your JDBC URL |
| `DB_USERNAME` | mysql user |
| `DB_PASSWORD` | mysql password |
| `OPENWEATHER_API_KEY` | your key |
| `OPENAI_API_KEY` | `sk-...` (real key) |
| `OPENAI_ENABLED` | `true` |
| `OPENAI_MODEL` | `gpt-4o-mini` |
| `APP_API_KEY` | strong random secret |

4. Click **Deploy**. Wait until status is **Live**.

5. Your API URL will look like:

```text
https://smart-agri-advisory-api.onrender.com
```

---

## Part E — Smoke test (production)

Health (no API key):

```text
GET https://YOUR-SERVICE.onrender.com/actuator/health
```

API (with header):

```text
GET https://YOUR-SERVICE.onrender.com/api/field
Header: X-API-KEY: your-app-api-key
```

Swagger:

```text
https://YOUR-SERVICE.onrender.com/swagger-ui.html
```

Recommendation with OpenAI:

```text
POST https://YOUR-SERVICE.onrender.com/api/smart/recommend/1
Header: X-API-KEY: your-app-api-key
```

Response should include `aiEnhanced: true` and `farmerAdvisory` when OpenAI is configured.

---

## Part F — Frontend (optional)

Build React app locally and point to cloud API:

1. `frontend/.env.production`:

```env
VITE_API_BASE=https://YOUR-SERVICE.onrender.com
```

2. `npm run build` → deploy `frontend/dist` to **Render Static Site** or **Netlify**.

3. Set API key in dashboard UI after open.

---

## Troubleshooting

| Issue | Fix |
|--------|-----|
| Deploy fails on DB | Check `DB_URL`, SSL params, firewall allows Render IPs |
| 401 on API | Use correct `X-API-KEY` from Render env |
| OpenAI not enhancing | Set `OPENAI_API_KEY` + `OPENAI_ENABLED=true` on Render |
| Cold start slow (free tier) | First request after idle may take 30–60s |

---

## Security reminder

- Do **not** commit real API keys to GitHub.
- Use Render **Environment** secrets only for production.
- Rotate keys if they were ever shared in chat or screenshots.
