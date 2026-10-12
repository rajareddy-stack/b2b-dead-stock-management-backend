# Backend

Spring Boot application containing the REST API, business services, persistence models, security configuration, and tests.

Run from this directory:

```powershell
mvn spring-boot:run
mvn test
```

The default local profile uses H2 and seeds demo data. Production uses PostgreSQL, does not seed demo accounts, and optionally creates the initial administrator from `INITIAL_ADMIN_EMAIL` and `INITIAL_ADMIN_PASSWORD`.

For deployment from this standalone backend repository, connect the repository to Render and use its root `render.yaml` Blueprint. It provisions a free Render PostgreSQL instance and Docker web service. The free database is temporary and expires after 30 days. Configure the Vercel frontend's `BACKEND_API_URL` to the Render service URL.
