package com.college.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SmartCollegeManagementWebApplication {
    public static void main(String[] args) { SpringApplication.run(SmartCollegeManagementWebApplication.class, args); }
    @Bean CommandLineRunner schema(JdbcTemplate db) { return args -> {
        db.execute("CREATE TABLE IF NOT EXISTS sca_students (roll_no INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(100) NOT NULL UNIQUE, course VARCHAR(120) NOT NULL, marks DECIMAL(5,2) NOT NULL CHECK (marks >= 0 AND marks <= 100)) ENGINE=InnoDB");
        db.execute("CREATE TABLE IF NOT EXISTS sca_attendance (roll_no INT NOT NULL, attendance_date DATE NOT NULL, status ENUM('Present','Absent') NOT NULL, PRIMARY KEY(roll_no, attendance_date), CONSTRAINT fk_sca_att_student FOREIGN KEY(roll_no) REFERENCES sca_students(roll_no) ON DELETE CASCADE) ENGINE=InnoDB");
        db.execute("CREATE TABLE IF NOT EXISTS sca_audit (id BIGINT AUTO_INCREMENT PRIMARY KEY, event_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, event_text VARCHAR(500) NOT NULL) ENGINE=InnoDB");
        Integer count = db.queryForObject("SELECT COUNT(*) FROM sca_students", Integer.class);
        if (count != null && count == 0) {
            db.update("INSERT INTO sca_students(roll_no,name,course,marks) VALUES(101,'Aarav Sharma','B.Tech Cyber Security',88.5),(102,'Srushti Badgujar','B.Tech Cyber Security',94.0),(103,'Riya Patel','B.Tech Computer Engineering',82.5),(104,'Aditya Verma','B.Tech Information Technology',79.0),(105,'Neha Deshmukh','B.Tech Cyber Security',91.5)");
        }
    }; }
}
