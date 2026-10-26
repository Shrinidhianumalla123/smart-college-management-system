# Smart College Management System — Web Edition

This is a browser-based conversion of the JavaFX MySQL attendance demo. It uses Java 21, Spring Boot, Thymeleaf templates, HTML/CSS, and MySQL/MariaDB.

## Included
- Login portals for Admin, Faculty, and existing Student records
- Dashboard summary
- Student directory: search, add, edit; Admin can delete
- Attendance management by class date
- Student profile and attendance history
- MySQL tables `sca_students`, `sca_attendance`, `sca_audit`
- Responsive styling for desktop and mobile

## Run locally
1. Install JDK 21 and Maven 3.9+.
2. Start MySQL/MariaDB (XAMPP is fine).
3. In phpMyAdmin, run the original `setup_database.sql` from the desktop ZIP, or create database `secure_college_admin` manually. This app creates its tables on startup but the configured database itself must already exist.
4. From this project folder run:
   - Windows CMD: `mvnw.cmd spring-boot:run` (if Maven Wrapper is present) or `mvn spring-boot:run`
   - Open `http://localhost:8080`

Environment variables:
- `DB_URL` default `jdbc:mysql://localhost:3306/secure_college_admin?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`
- `DB_USER` default `root`
- `DB_PASSWORD` default blank
- `PORT` default `8080`

## Demo logins
- Admin: `admin` / `Admin@123`
- Faculty: `faculty` / `Faculty@123`
- Student: full name as recorded / lowercase name with spaces replaced by underscores + `@2026`

## Deploy
Deploy this project as a Java 21 Spring Boot web service on a host that supports Java applications (for example Render). Create a hosted MySQL database and configure `DB_URL`, `DB_USER`, and `DB_PASSWORD` as service environment variables. Do not use the local XAMPP database for public hosting. Do not commit real credentials. The demo credentials are intentionally simple and are only suitable for a classroom prototype, not real student data.

The hosting account and database credentials must be supplied/configured by the project owner; this source ZIP is prepared for deployment but is not itself a live hosted website.
