# RideOn — Setup & Run Guide

A ride-hailing application (Uber-like) built as a Spring Boot backend and a native Android client.

---

## 1. Tech stack

| Part | Technology |
|------|-----------|
| Backend | Spring Boot 3.5.x, Java 17, Maven |
| Database | PostgreSQL (runtime), H2 (tests) |
| Auth | JWT |
| Email | SMTP (Gmail) |
| Android | Android Studio, Java, Retrofit, osmdroid (OpenStreetMap) |

---

## 2. Prerequisites

Install before you start:

- **JDK 17** — `java -version` should report 17.x
- **PostgreSQL 14+** — running locally on port 5432
- **Android Studio** (recent stable) with an **Android SDK** and either an **emulator (AVD)** or a physical device with USB debugging
- **Git**

The backend uses the **Maven wrapper** (`mvnw`), so a separate Maven install isn't required.

---

## 3. Repository layout

```
<repo-root>/
├── backend/     # Spring Boot project
│   └── src/main/resources/application.properties
├── mobile/     # Android Studio project
└── README.md    # this file
```

---

## 4. Database setup

### 4.1 Create the database

Open `psql` (or pgAdmin) and run:

```sql
CREATE DATABASE ride_on_db;
-- Uses the default 'postgres' user. To use a dedicated one instead:
-- CREATE USER ride_on_user WITH PASSWORD '<your-password>';
-- GRANT ALL PRIVILEGES ON DATABASE ride_on_db TO ride_on_user;
```

### 4.2 First run creates the schema

The backend runs with `spring.jpa.hibernate.ddl-auto=update`, so on **first startup it creates all tables** from the entities. You do **not** need to create tables by hand.

### 4.3 ⚠️ Database migrations (enum check constraints)

The `rides.status` and `notifications.type` columns have **CHECK constraints** listing their allowed values. On a **fresh** database these are generated with all current values and you need to do nothing. **But** if you use an **existing** database that predates newer features (or you hit an error like `violates check constraint "notifications_type_check"`), apply the widenings below once:

```sql
-- Ride statuses (adds SCHEDULED and the rest of the lifecycle)
ALTER TABLE rides DROP CONSTRAINT IF EXISTS rides_status_check;
ALTER TABLE rides ADD CONSTRAINT rides_status_check
    CHECK (status IN ('REQUESTED','SCHEDULED','ASSIGNED','REJECTED',
                      'IN_PROGRESS','FINISHED','CANCELLED'));

-- Notification types (adds RIDE_REMINDER, PANIC, RIDE_CANCELLED)
ALTER TABLE notifications DROP CONSTRAINT IF EXISTS notifications_type_check;
ALTER TABLE notifications ADD CONSTRAINT notifications_type_check
    CHECK (type IN ('NEW_RIDE','RIDE_ACCEPTED','RIDE_FAILED','RIDE_FINISHED',
                    'RIDE_REMINDER','PANIC','RIDE_CANCELLED'));
```

> **Why:** `ddl-auto=update` creates new tables and columns but does **not** alter an existing CHECK constraint when a new enum value is added. A fresh DB is fine; an upgraded one needs these. Running them on a fresh DB is harmless.

---

## 5. Backend — configure & run

### 5.1 Configure `application.properties`

Path: `backend/src/main/resources/application.properties`. Set your local values:

```properties
# --- Database ---
spring.datasource.url=jdbc:postgresql://localhost:5432/ride_on_db
spring.datasource.username=postgres
spring.datasource.password=<your-postgres-password>

spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# --- Email (SMTP / Gmail) ---
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=<your-gmail-address>
spring.mail.password=<your-gmail-app-password>
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

> **Gmail note:** use a **Google App Password** (with 2FA enabled), not your normal account password. Account activation and password-reset emails are sent through this.

> Keep real credentials **out of version control**. If needed, override locally via environment variables or a local (git-ignored) properties file.

### 5.2 Run the backend

From the backend folder:

```bash
# Linux/macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

The API starts on **http://localhost:8080**. Leave it running.

### 5.3 Run the tests (optional)

Tests use an in-memory **H2** database (profile `test`) — no PostgreSQL needed:

```bash
./mvnw test
```

---

## 6. Android — configure & run

### 6.1 Open the project

Open the `mobile/` folder in **Android Studio** and let Gradle sync.

### 6.2 Point the app at your backend

The base URL lives in the Retrofit client (e.g. a `BASE_URL` constant in **`RetrofitClient`**). Set it to reach your running backend:

- **Android emulator** → the host machine's `localhost` is **`10.0.2.2`**:
  ```java
  public static final String BASE_URL = "http://10.0.2.2:8080/";
  ```
- **Physical device** (same Wi-Fi as your PC) → use your PC's LAN IP:
  ```java
  public static final String BASE_URL = "http://<your-PC-LAN-IP>:8080/";   // e.g. http://192.168.0.20:8080/
  ```

> `localhost` inside the emulator refers to the emulator itself, not your PC — that's why `10.0.2.2` is required.

### 6.3 Run

Pick an emulator or device and press **Run**. Register a user (activation email is sent via the SMTP settings), then log in.

### 6.4 Maps & location (emulator)

The app uses **osmdroid** (OpenStreetMap) for maps. On an emulator there is no real GPS, so for map centering and the driver "Stop here" feature:

- Emulator **⋮ (Extended controls) → Location** → set a latitude/longitude (e.g. Novi Sad `45.2671, 19.8335`) → **Set Location**.

Location permission is requested at runtime the first time it's needed.

---

## 7. Test accounts / seed data

For a populated database to demo against (admin, riders, drivers with vehicles, finished rides, ratings), see **`seed_data.sql`** in the repo and load it after the schema exists (i.e. after the backend has started once):

```bash
psql -d ride_on_db -f seed_data.sql
```

> **Important:** drivers' vehicles must have `current_latitude`/`current_longitude` set, or they won't appear on maps and rider ETA can't be computed. The seed script sets these.

Default demo logins are listed at the top of the seed file.

---

## 8. Troubleshooting

| Symptom | Cause / fix |
|---------|-------------|
| `violates check constraint "notifications_type_check"` (or `rides_status_check`) | Existing DB predates a new enum value — run the migrations in **§4.3**. |
| App can't reach the backend / timeouts | Wrong `BASE_URL`. Emulator → `http://10.0.2.2:8080/`; device → PC LAN IP. Backend must be running. |
| Map opens on the ocean (0°, 0°) | No location to center on — set a mock location in the emulator (**§6.4**); ensure the ride has valid pickup coordinates. |
| Vehicle marker doesn't move | Vehicle position is static by design (no live driver-location streaming); the pin shows the last known position. |
| "Couldn't get your current location" (driver Stop here) | Location services off / no fix — set a mock location in the emulator (**§6.4**). |
| No activation / reset emails | SMTP settings wrong; use a Gmail **App Password** and confirm STARTTLS settings (**§5.1**). |
| Tests fail to start | Ensure JDK 17; tests use H2 (`test` profile) and don't need PostgreSQL. |

---

## 9. Quick start (summary)

```bash
# 1. Database
createdb ride_on_db                 # or CREATE DATABASE in psql

# 2. Backend
cd backend
# edit src/main/resources/application.properties (DB + mail)
./mvnw spring-boot:run              # http://localhost:8080

# 3. (optional) seed data
psql -d ride_on_db -f ../seed_data.sql

# 4. Android
# open mobile/ in Android Studio
# set RetrofitClient BASE_URL to http://10.0.2.2:8080/ (emulator)
# Run
```

---

**Repository:** https://github.com/kzi-nastava/mrs-team20-Tim-Djordjina
**Team:** team20 — Tim Djordjina


