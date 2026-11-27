# Smart Placement Management Portal (Complete Source)

A full-stack academic placement portal with student registration/login, admin login, job management, CGPA eligibility, applications and application-status tracking.

## Technology
- Frontend: React 18, Vite 5, JavaScript
- Backend: Java 17+, Spring Boot 3.3.5, Spring Web, Spring Data JPA, Spring Security
- Database: MySQL 8+
- Authentication: JWT (JJWT 0.12.6), BCrypt password hashing

## Project structure
```text
02-Smart-Placement-Portal/
├── backend/
│   ├── pom.xml
│   └── src/main/java/com/example/placement/
│       ├── PlacementPortalApplication.java
│       ├── AdminSeeder.java
│       ├── controller/       # Auth, students, jobs, applications
│       ├── model/            # JPA entities
│       ├── repository/       # Spring Data repositories
│       └── security/         # JWT service/filter and authorization
│   └── src/main/resources/application.properties
├── frontend/
│   ├── package.json
│   ├── vite.config.js
│   ├── index.html
│   └── src/                  # React app and styles
├── database/
│   └── schema.sql
└── README.md
```

## Requirements
- JDK 17 or newer (JDK 21 is supported)
- Apache Maven 3.8+
- Node.js 18+ and npm
- MySQL Server 8+

## IMPORTANT: existing database and upgrade
The backend uses `spring.jpa.hibernate.ddl-auto=update`; it does not intentionally drop existing tables. Nevertheless, **export/backup your current `placement_portal` database before first startup**. This release adds the `user_accounts` table. Existing student rows can be linked to a new student login by registering with the exact same email address. Existing rows do not automatically receive passwords or accounts.

## 1. Database backup
In MySQL Workbench use **Server → Data Export**, select `placement_portal`, choose **Dump Structure and Data**, and export to a SQL file. Verify the export file exists before upgrading.

## 2. Database setup
Open MySQL Workbench and run:
```sql
CREATE DATABASE IF NOT EXISTS placement_portal;
```
The backend connects to `localhost:3306/placement_portal` by default. Override `DB_URL`, `DB_USERNAME` and `DB_PASSWORD` if required.

## 3. Configure and start backend (Windows CMD)
Open CMD in the `backend` folder and set these values in the same window (replace examples locally; never share secrets):
```cmd
set "DB_USERNAME=root"
set "DB_PASSWORD=YOUR_MYSQL_PASSWORD"
set "JWT_SECRET=use-a-private-random-secret-with-at-least-32-characters"
set "ADMIN_EMAIL=admin@placement.local"
set "ADMIN_PASSWORD=Choose-A-Strong-Unique-Admin-Password"
mvn spring-boot:run
```
The backend must print `Started PlacementPortalApplication`. The first run creates tables/columns through Hibernate and seeds the admin account if the configured email does not already exist. The admin password is BCrypt-hashed. If an admin account already exists, changing `ADMIN_PASSWORD` does not reset its stored password.

## 4. Start frontend
Open a second CMD in `frontend`:
```cmd
npm install
npm run dev
```
Open `http://localhost:5173/`. Keep both terminals open while using the app.

Optional frontend API override: create `frontend/.env.local` with `VITE_API_BASE_URL=http://localhost:8080/api`, then restart Vite.

## 5. First-time accounts
- Student: choose **Student registration**, provide name, email, department, CGPA (0–10) and password (8–72 characters).
- Administrator: sign in using the `ADMIN_EMAIL` and `ADMIN_PASSWORD` configured before backend startup. Admin registration is deliberately not exposed publicly.

## Features
### Student
- Register and sign in
- View published opportunities
- Search by company, role, skills or location
- Apply only when CGPA meets the job minimum
- Server-side ownership, eligibility and duplicate-application checks
- View own application history and status

### Administrator
- Secure admin login
- Publish and delete job opportunities (jobs with applications cannot be deleted)
- View student directory
- View all applications
- Update application status: `APPLIED`, `ASSESSMENT`, `INTERVIEW`, `SELECTED`, `REJECTED`

## API summary
| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/auth/register` | Public (student only) |
| POST | `/api/auth/login` | Public |
| GET | `/api/jobs` | Public |
| POST | `/api/jobs` | ADMIN |
| DELETE | `/api/jobs/{id}` | ADMIN |
| GET | `/api/students` | ADMIN |
| GET | `/api/applications` | ADMIN |
| GET | `/api/applications/student/{studentId}` | Owner STUDENT or ADMIN |
| POST | `/api/applications?studentId={id}&jobId={id}` | STUDENT (owner only) |
| PATCH | `/api/applications/{id}/status?status={value}` | ADMIN |

Protected API requests use `Authorization: Bearer <JWT>`. JWTs expire after 24 hours by default (`JWT_EXPIRATION_MS` can override this). The frontend stores the token in `sessionStorage` and clears it on sign-out or HTTP 401.

## Manual verification checklist
1. Register a student with CGPA 8.50; confirm a profile is created.
2. Register the same email again; expect HTTP 409.
3. Try CGPA 11; expect validation failure.
4. Log in as admin; publish a job requiring CGPA 7.00.
5. Log in as a student with CGPA 8.50; apply; confirm history shows `APPLIED`.
6. Apply again; expect HTTP 409 and no duplicate row.
7. Register/log in as a student with CGPA 6.00; the UI disables Apply and backend rejects direct ineligible requests.
8. Log in as admin; change status to `INTERVIEW`; confirm the student sees the updated status.
9. Try admin-only endpoints as a student; expect HTTP 403.
10. Refresh browser and restart backend; confirm persisted data remains.


