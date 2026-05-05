# User Service Update Notes

## 2026-04-14 - User Migration Phase 1

### Highlights
- Added JWT login endpoint and token parsing support.
- Added user-domain APIs for profile update, username update, password change, and user queries.
- Kept legacy `/users/**` endpoints for backward compatibility during migration.
- Added password hashing with BCrypt for new users.
- Added transparent legacy-plaintext password upgrade to BCrypt after successful login.

### New/Updated API Groups
- Authentication: `/api/auth`
- Registration: `/api/register`, `/api/users`, `/api/persons`
- Current user: `/api/user`
- User query: `/api/users`, `/api/userById`, `/api/userByIdByPass`, `/api/userByUsername`, `/api/userExistsByUsername`
- User updates: `/api/password`, `/api/users/{userId}`, `/api/users/{userId}/username`
- Compatibility: `/users/{userId}`, `/users/exists/{userId}`, `/users/{userId}` (form registration)

### Config and Dependencies
- Added JWT config entries in `elm-configs/elm-user-server-1-dev.yml`.
- Added gateway routing for `/api/**` user endpoints in `elm-configs/elm-gateway-server-1-dev.yml`.
- Added dependencies in `elm-user-server/pom.xml`:
  - `spring-security-crypto`
  - `jjwt-api`, `jjwt-impl`, `jjwt-jackson`
  - `spring-boot-starter-test`

### Testing
- Added controller tests in `elm-user-server/src/test/java/team/tjusw/elm/controller/UserControllerTest.java`.

### Migration Notes
- Existing clients can continue using legacy endpoints while gradually switching to `/api/**` routes.
- Passwords stored in plaintext are upgraded to BCrypt automatically after the first successful login.
