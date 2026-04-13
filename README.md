# ELM-CLOUD

## Docker Local Dependencies

This repository now supports local Docker dependencies for team development:

- MySQL: `8.0.36`
- Redis: `6.2.14`

### Quick Start

1. Copy env template:
   - `cp .env.example .env` (Linux/macOS)
   - `Copy-Item .env.example .env` (PowerShell)
2. Start dependencies:
   - `docker compose up -d`
3. Verify containers:
   - `docker compose ps`

### Notes

- MySQL init script auto-creates databases `elm` and `elm2`.
- Service config files in `elm-configs/*.yml` now use env placeholders like `${DB_HOST:localhost}` and `${REDIS_HOST:localhost}`.
- If you run Java services directly on host, keep `DB_HOST=localhost`.
- If you later containerize Java services, set `DB_HOST=mysql` and `REDIS_HOST=redis` in your `.env`.
