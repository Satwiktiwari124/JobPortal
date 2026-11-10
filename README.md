# JobPortal

A full-stack job portal built with Java, Spring Boot, Spring Security, JWT, MySQL, HTML, Bootstrap and JavaScript.

## Features

### Candidate
- Candidate registration and JWT login/logout
- Browse and search job listings
- Apply to a job once per account
- View application history and status
- Upload and download a PDF resume (maximum 5 MB)
- Update profile email

### Recruiter
- Recruiter registration protected by an invitation code
- Post, edit and delete owned job listings
- View applicants for owned jobs
- Download applicant resumes for owned job applications
- Update application status: APPLIED, SHORTLISTED, REJECTED or SELECTED

### Engineering
- Layered controller/service/repository architecture
- DTO validation and centralized API exception responses
- JWT authentication and role-based authorization
- Recruiter ownership checks for jobs, applications and resume downloads
- Duplicate application protection at service and database levels

## Tech Stack
- Java 21+
- Spring Boot 4.x, Spring Security, Spring Data JPA
- MySQL 8+
- JWT (JJWT)
- HTML5, CSS3, Bootstrap 5, Vanilla JavaScript
- Maven

## Local Setup

1. Install Java 21 or newer and MySQL 8+.
2. Create the database:

   ```sql
   CREATE DATABASE jobportal;
   ```

3. Set environment variables (or use local defaults in `application.properties`):

   - `DB_URL=jdbc:mysql://localhost:3306/jobportal`
   - `DB_USERNAME=root`
   - `DB_PASSWORD=your_mysql_password`
   - `JWT_SECRET=a-long-random-secret-at-least-32-bytes`
   - `UPLOAD_DIR=uploads/resumes`
   - `RECRUITER_REGISTRATION_CODE=your-private-invite-code` (required only to enable recruiter self-registration)

4. Run the application from IntelliJ, or run `mvn spring-boot:run`.
5. Open `http://localhost:8080`.

The application creates/updates tables using Hibernate `ddl-auto=update` by default. For production, configure a migration tool and set `DDL_AUTO=validate` after the schema is provisioned.

## Registration
- Candidate registration is open.
- Recruiter registration is disabled unless `RECRUITER_REGISTRATION_CODE` is configured. The recruiter must enter that code in the registration form. Never commit the real code to source control.

## API Overview

| Method | Endpoint | Access |
|---|---|---|
| POST | `/auth/register` | Public candidate registration |
| POST | `/auth/register/recruiter` | Public + invitation code |
| POST | `/auth/login` | Public |
| POST | `/auth/logout` | Public |
| GET/PUT | `/users/me` | Authenticated |
| GET | `/jobs`, `/jobs/{id}` | Public |
| POST | `/jobs` | Recruiter |
| PUT/DELETE | `/jobs/{id}` | Owning recruiter |
| POST | `/applications/jobs/{jobId}` | Candidate |
| GET | `/applications/my` | Candidate |
| GET | `/applications/recruiter` | Recruiter |
| PATCH | `/applications/{applicationId}/status` | Owning recruiter |
| POST | `/resumes/upload` | Candidate |
| GET | `/resumes/my/download` | Candidate |
| GET | `/resumes/applications/{applicationId}/download` | Owning recruiter |

## Deployment Notes
- Configure database, JWT secret, recruiter invite code and upload directory through environment variables.
- Use HTTPS and set secure cookie handling for production.
- Persist the `UPLOAD_DIR` volume; resume files are stored outside the application database.
- Configure the hosting provider's `PORT` environment variable.
- Do not deploy with development secrets or a publicly known recruiter invite code.
- Test registration, login, both roles, job ownership, applications, resume access and status updates before sharing the public URL.
