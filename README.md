🚀 JobPortal – Full Stack Job Portal Application

A full-stack Job Portal application built using Java, Spring Boot, Spring Security, JWT, MySQL, HTML, CSS, Bootstrap, and JavaScript.

JobPortal provides a platform where candidates can search and apply for jobs, while recruiters can post job openings, manage applications, and track candidate status.

---

✨ Features

👨‍💻 Candidate Features

- Candidate registration and login.
- Secure JWT-based authentication.
- Browse and search available jobs.
- View detailed job descriptions.
- Apply for jobs.
- Apply only once per job.
- View application history and status.
- Upload and download PDF resumes (maximum 5 MB).
- Update profile email.
- Secure logout functionality.

🏢 Recruiter Features

- Recruiter registration protected by an invitation code.
- Secure recruiter authentication.
- Post new job listings.
- Edit and delete owned job listings.
- View applicants for posted jobs.
- Download applicant resumes.
- Manage job applications.
- Update application status:
  - APPLIED
  - SHORTLISTED
  - REJECTED
  - SELECTED

🔐 Security Features

- Spring Security integration.
- JWT-based authentication.
- Role-based authorization.
- Password encryption.
- Protected REST APIs.
- DTO validation.
- Centralized exception handling.
- Recruiter-only access to recruiter operations.

---

🛠️ Tech Stack

Backend

- Java 21+
- Spring Boot 4.x
- Spring Security
- Spring Data JPA
- Hibernate
- JWT (JSON Web Token)
- Maven

Frontend

- HTML5
- CSS3
- Bootstrap 5
- Vanilla JavaScript

Database

- MySQL 8+

Development Tools

- IntelliJ IDEA
- MySQL Workbench
- Postman
- Git
- GitHub

---

📂 Project Structure

JobPortal/
│
├── .mvn/
│   └── wrapper/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/jobPortal/
│   │   │       ├── config/
│   │   │       ├── controller/
│   │   │       ├── dto/
│   │   │       ├── entity/
│   │   │       ├── exception/
│   │   │       ├── repository/
│   │   │       ├── security/
│   │   │       ├── service/
│   │   │       └── JobApplication.java
│   │   │
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── css/
│   │       │   ├── js/
│   │       │   ├── images/
│   │       │   └── index.html
│   │       │
│   │       └── application.properties
│   │
│   └── test/
│
├── .env.example
├── .gitignore
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md

---

⚙️ Installation & Setup

1. Clone the Repository

git clone https://github.com/Satwiktiwari124/JobPortal.git

2. Open Project

Open the project in IntelliJ IDEA.

3. Configure MySQL Database

Create a database using MySQL:

CREATE DATABASE jobportal;

4. Configure Environment Variables

Configure the following environment variables:

DB_URL=jdbc:mysql://localhost:3306/jobportal
DB_USERNAME=root
DB_PASSWORD=your_mysql_password
JWT_SECRET=your_long_random_secret_at_least_32_bytes
UPLOAD_DIR=uploads/resumes
RECRUITER_REGISTRATION_CODE=your_private_invite_code

Never upload real passwords, JWT secrets, or recruiter invitation codes to GitHub.

5. Run the Application

Using Maven:

mvn spring-boot:run

Or run "JobApplication.java" directly from IntelliJ IDEA.

6. Open in Browser

http://localhost:8080

---

🔗 API Endpoints

Method| Endpoint| Access
POST| /auth/register| Public
POST| /auth/register/recruiter| Public + Invitation Code
POST| /auth/login| Public
POST| /auth/logout| Public
GET/PUT| /users/me| Authenticated
GET| /jobs| Public
GET| /jobs/{id}| Public
POST| /jobs| Recruiter
PUT/DELETE| /jobs/{id}| Owning Recruiter
POST| /applications/jobs/{jobId}| Candidate
GET| /applications/my| Candidate
GET| /applications/recruiter| Recruiter
PATCH| /applications/{applicationId}/status| Owning Recruiter
POST| /resumes/upload| Candidate
GET| /resumes/my/download| Candidate
GET| /resumes/applications/{applicationId}/download| Owning Recruiter

---

🔒 Authentication & Authorization

JobPortal uses JWT-based authentication with Spring Security.

- Candidates can access candidate-specific functionalities.
- Recruiters can manage their own job listings.
- Recruiters can view applications for their own jobs.
- Unauthorized users cannot access protected resources.
- Passwords are stored securely using encryption.

---

📄 Resume Management

- Candidates can upload PDF resumes.
- Maximum supported resume size: 5 MB.
- Candidates can download their uploaded resumes.
- Recruiters can download resumes of applicants for their own jobs.
- Resume files are stored outside the application database.

---

🚀 Future Enhancements

- Email notifications for application updates.
- Advanced job search and filtering.
- Job recommendations using Machine Learning.
- Admin dashboard.
- Cloud-based resume storage. 
- Real-time application notifications.

---

👩‍💻 Author

Satwik Tiwari

GitHub: https://github.com/Satwiktiwari124

---

⭐ Support

If you like this project, don't forget to give it a ⭐ on GitHub!

---

Built with ❤️ using Java, Spring Boot, MySQL and JavaScript.
