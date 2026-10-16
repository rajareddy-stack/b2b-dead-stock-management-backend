# Backend

Spring Boot application containing the REST API, business services, persistence models, security configuration, and tests.

Run from this directory:

```powershell
mvn spring-boot:run
mvn test
```

The default local profile uses H2 and seeds demo data. Production uses PostgreSQL, does not seed demo accounts, and optionally creates the initial administrator from `INITIAL_ADMIN_EMAIL` and `INITIAL_ADMIN_PASSWORD`.

For deployment from this standalone backend repository, connect the repository to Render and use its root `render.yaml` Blueprint. It provisions a free Render PostgreSQL instance and Docker web service. The free database is temporary and expires after 30 days. Configure the Vercel frontend's `BACKEND_API_URL` to the Render service URL.

Email alerts use SMTP and are disabled unless `EMAIL_NOTIFICATIONS_ENABLED=true`. Configure `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD`, and `SMTP_FROM` in the hosting provider's secret environment settings; optionally set `APP_PUBLIC_URL` for links in alert emails. In-app notifications remain available when SMTP is disabled.

Production seeds five clearly tagged demo inventory lots once, controlled by `MARKETPLACE_SAMPLE_DATA_ENABLED` (defaults to `true` for the demo deployment). The synthetic supplier account is disabled and cannot sign in. Set the variable to `false` to disable the sample catalog.

Sellers can bulk-create up to 100 inventory listings at `POST /api/listings/bulk`. The Vercel frontend's seller dashboard provides a CSV template and validates rows before submission. Imported columns include SKU, title, description, category title, original and liquidation price, batch quantity, unit, condition, location, optional expiry date, image URL, and semicolon-separated tags.

Public listing APIs and listing pages expose only a seller's company name, industry, city/state, and verification status. Tax IDs, street addresses, phone numbers, and owner email addresses are not included in public listing responses.
