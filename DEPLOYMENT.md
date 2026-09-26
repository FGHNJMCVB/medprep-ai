# MedPrep production deployment

The Vercel project serves the Vite frontend. The Java backend needs a separate
long-running host with PostgreSQL; Vercel's frontend build does not run the
Spring Boot service or import question data.

## Before pushing

1. Bring up the Java backend and hosted PostgreSQL database first. Confirm the
   public backend URL answers `GET /api/health` with `{"status":"UP"}`.
2. In Vercel, create or select the GitHub-connected project for this repository.
   In **Settings → Build and Deployment**, set **Root Directory** to `frontend`,
   Framework Preset to **Vite**, Build Command to `npm run build`, and Output
   Directory to `dist`. The production branch should be `main`.
3. In **Settings → Environment Variables**, add `VITE_API_BASE_URL` with the
   public **HTTPS backend origin** (for example `https://api.example.com`),
   without `/api`. Apply it to **Preview** and **Production**. This is a public
   URL, never a password. A changed variable needs a new Vercel deployment.
4. Set backend `CORS_ALLOWED_ORIGINS` to the final Vercel HTTPS origin. If you
   test a Vercel preview domain, include that exact preview origin as well.

When GitHub is connected in ChatGPT, the prepared commit can be published as a
branch and checked before merging. If applying the downloadable patch on your
Mac instead, start from an up-to-date, clean checkout:

```bash
cd ~/Documents/medprep-ai
git fetch origin
git switch -c deploy/production-readiness origin/main
git am ~/Downloads/medprep-production-readiness.patch
cd frontend
npm ci
VITE_API_BASE_URL=https://api.example.com npm run build
cd ..
git push -u origin deploy/production-readiness
```

Replace the example API URL with the real one. Open a GitHub pull request from
`deploy/production-readiness` into `main`, wait for the frontend and backend
build checks, and test Vercel's preview. After the backend and database checks
below pass, merge the pull request. Vercel then deploys the `main` commit to
production. The production deployment should be smoke-tested again after merge.

## Backend host

For a **Render Docker web service** connected to this monorepo, configure
**Root Directory** as `backend/medprep-backend`, **Dockerfile Path** as
`Dockerfile` (or `./Dockerfile`), and **Docker Build Context** as `.`. Render
interprets the latter two relative to Root Directory. Do not enter
`backend/medprep-backend/Dockerfile` or `backend/medprep-backend` again in
those fields: that duplicates the path and fails before the Docker build.
The included Dockerfile builds a Java 17 JAR and uses Render's `PORT` value.

For a non-Docker host, point it at `backend/medprep-backend`, use Java 17, build with
`./mvnw -DskipTests package`, and start with
`java -jar target/medprep-backend-0.0.1-SNAPSHOT.jar`. Configure an HTTPS domain
for the API and set these host environment variables:

| Variable | Value |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DB_URL` | Hosted PostgreSQL JDBC URL, for example `jdbc:postgresql://db.example.com:5432/medprep` |
| `DB_USERNAME`, `DB_PASSWORD` | Hosted PostgreSQL credentials |
| `JWT_SECRET` | A private random value of at least 32 characters; keep stable across restarts |
| `CORS_ALLOWED_ORIGINS` | Exact Vercel HTTPS origin, for example `https://medprep.example.com` (comma-separated if more than one) |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | Optional pair for an initial admin or to rotate an existing admin password; use at least 12 characters |
| `PORT` | Port supplied by the host, if any (defaults to `8080`) |

Configure the host health check at `/api/health`. It returns HTTP 200 only when
the app can query PostgreSQL. The production profile requires database and JWT
settings, disables SQL logging, and validates the schema without changing it.
Back up and migrate the hosted database before deploying any future schema
changes. A code deploy does not copy your localhost PostgreSQL data to the
hosted database.

If the existing database contains the old seeded `admin@medprep.com` account,
set `ADMIN_EMAIL=admin@medprep.com` and a new private `ADMIN_PASSWORD` on the
backend host before exposing the API. The backend will replace that account's
password at startup. Without this rotation, an existing weak admin password
would remain in the database.

## Vercel

Connect the GitHub repository to a Vercel project with **Root Directory** set
to `frontend`. Use the Vite framework preset, build command `npm run build`,
and output directory `dist`. Set the production environment variable
`VITE_API_BASE_URL` to the public HTTPS backend origin, with no `/api` suffix,
for example `https://api.medprep.example.com`. A production build fails if
this value is absent or does not use HTTPS. Redeploy after changing it.

The `frontend/vercel.json` rewrite serves the SPA for direct page visits.
For local development, `npm run dev` uses `http://localhost:8080` by default;
set `JWT_SECRET` for the locally running backend.

## Database and Mock 02

Back up your local database and set up a private hosted PostgreSQL database
before using the live API. Transfer the approved question data through the
private administrator import API. Do not publish answer-key JSON in this public
repository. The Mock 02 ZIP includes `import-mock-set-02.sh` and
`verify-mock-set-02.sql`; its script requires Mock 01 to be present in the
*hosted* database first. With `psql` access to that database, run from the
extracted ZIP directory:

```bash
MEDPREP_DB_NAME='<hosted PostgreSQL connection string>' \
MEDPREP_API_BASE='https://api.example.com' \
./import-mock-set-02.sh
```

Use a private password source (for example `.pgpass`) for `psql`; the script
prompts for administrator credentials. It verifies counts and marks mock-only
questions inactive to keep them out of topic practice.

## Release checks

1. Confirm `GET https://api.example.com/api/health` returns `{"status":"UP"}`.
2. Confirm the Vercel frontend can load subjects, register, sign in and start a
   full mock from its HTTPS origin; check CORS in the browser network panel.
3. Verify 300 rows with `FMGE-MOCK-01-%` and 300 with `FMGE-MOCK-02-%` in the
   hosted database before testing both fixed mocks.
4. Validate build and runtime logs on the actual host before routing users.
