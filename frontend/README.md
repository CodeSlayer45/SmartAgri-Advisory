# Smart Agri Dashboard (React)

Step 1 frontend for the Smart Agri Advisory backend.

## Prerequisites

- Backend running on `http://localhost:8081`
- Node.js 18+

## Run

```bash
cd frontend
npm install
npm run dev
```

Open: http://localhost:5173

## API key

Default matches backend: `change-me-local`

Change in the top-right **X-API-KEY** field and click **Save key**.

## Features

- Backend connection status indicator
- List and select farm fields
- Register field (AUTO_GPS or MANUAL)
- **Weather panel** (live OpenWeather by field location)
- Log activities (spray, fertilizer, etc.)
- **Get recommendation** (rule-based engine)
- **Alerts panel** (view + run disease risk scan)

## Step 2 — OpenAI farmer text

When the backend has `openai.api.enabled=true` and a valid `OPENAI_API_KEY`,
click **Get recommendation** to see an extra **OpenAI farmer summary** box
above the rule-engine reasons and actions.
