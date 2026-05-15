# ELM-CLOUD

本项目迭代开发自仿elm平台——软件工程中级实践，将一个集合了商家服务（入驻，开店，配送等）与普通用户服务（个人主页，点单消费，钱包，白条小贷，积分）的单体项目拆分为微服务。

更完整的前后端单体实现请访问：https://gitee.com/lockie_lsx/elm-pls

[如果有疑问，可能会有帮助](./可能的问题.md)

## Docker Local Dependencies

This repository now supports local Docker dependencies for team development:

- MySQL: `8.0.36`
- Redis: `6.2.14`

### Quick Start

1. Copy env template:
   - `cp .env.example .env` (Linux/macOS)
   - `Copy-Item .env.example .env` (PowerShell)
2. (must) Modify your config-server path:
   - elm-config-server/src/main/resources/application.yml
3. Start dependencies:
   - `docker compose up -d`
4. Verify containers:
   - `docker compose ps`

### Notes

- MySQL init script auto-creates databases `elm` and `elm2`.
- Service config files in `elm-configs/*.yml` now use env placeholders like `${DB_HOST:localhost}` and `${REDIS_HOST:localhost}`.
- If you run Java services directly on host, keep `DB_HOST=localhost`.
- If you later containerize Java services, set `DB_HOST=mysql` and `REDIS_HOST=redis` in your `.env`.
- Current local Redis setup does not require a password.
