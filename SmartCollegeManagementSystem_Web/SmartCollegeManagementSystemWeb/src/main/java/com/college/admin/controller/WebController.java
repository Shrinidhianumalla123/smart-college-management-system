package com.college.admin.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.util.Locale;

@Controller
public class WebController {
 private final JdbcTemplate db;
 public WebController(JdbcTemplate db){this.db=db;}
 private boolean signed(HttpSession s){return s.getAttribute("role")!=null;}
 private String role(HttpSession s){return (String)s.getAttribute("role");}
 private void audit(String text){db.update("INSERT INTO sca_audit(event_text) VALUES(?)",text);}
 @GetMapping("/") public String root(HttpSession s){return signed(s)?"redirect:/dashboard":"redirect:/login";}
 @GetMapping("/login") public String login(){return "login";}
 @PostMapping("/login") public String doLogin(@RequestParam String role,@RequestParam String username,@RequestParam String password,HttpSession s,RedirectAttributes ra){
  String r=role.trim(),u=username.trim(); boolean ok=false;
  if(r.equals("Admin"))ok=u.equals("admin")&&password.equals("Admin@123");
  else if(r.equals("Faculty"))ok=u.equals("faculty")&&password.equals("Faculty@123");
  else if(r.equals("Student")&&!u.isBlank()){
   String expected=u.toLowerCase(Locale.ROOT).replaceAll("\\s+","_")+"@2026";
   if(password.equals(expected)){Integer n=db.queryForObject("SELECT COUNT(*) FROM sca_students WHERE LOWER(name)=LOWER(?)",Integer.class,u); if(n!=null&&n>0)ok=true; else ra.addFlashAttribute("error","Student record not found. Ask the administrator to add you first.");}
  }
  if(!ok){if(!ra.getFlashAttributes().containsKey("error"))ra.addFlashAttribute("error","Credentials do not match the selected portal.");return "redirect:/login";}
  s.setAttribute("role",r);s.setAttribute("username",r.equals("Admin")?"Campus Administrator":r.equals("Faculty")?"Faculty Member":u);s.setAttribute("studentName",r.equals("Student")?u:null);audit(r+" login: "+u);return "redirect:/dashboard";
 }
 @PostMapping("/logout") public String logout(HttpSession s){s.invalidate();return "redirect:/login";}
 @GetMapping("/dashboard") public String dashboard(HttpSession s,Model m){if(!signed(s))return "redirect:/login";if(role(s).equals("Student"))return "redirect:/profile";base(s,m,"Dashboard");m.addAttribute("total",db.queryForObject("SELECT COUNT(*) FROM sca_students",Integer.class));m.addAttribute("average",db.queryForObject("SELECT COALESCE(ROUND(AVG(marks),1),0) FROM sca_students",Double.class));m.addAttribute("high",db.queryForObject("SELECT COUNT(*) FROM sca_students WHERE marks>=85",Integer.class));m.addAttribute("students",db.queryForList("SELECT roll_no,name,course,marks FROM sca_students ORDER BY roll_no LIMIT 5"));return "dashboard";}
 @GetMapping("/students") public String students(@RequestParam(required=false) String q,HttpSession s,Model m){if(!signed(s))return "redirect:/login";if(role(s).equals("Student"))return "redirect:/profile";base(s,m,"Student records");String term=q==null?"":q.trim();m.addAttribute("q",term);m.addAttribute("students",db.queryForList("SELECT roll_no,name,course,marks FROM sca_students WHERE CAST(roll_no AS CHAR) LIKE ? OR name LIKE ? OR course LIKE ? ORDER BY roll_no","%"+term+"%","%"+term+"%","%"+term+"%"));return "students";}
 @PostMapping("/students/save") public String save(@RequestParam(required=false) Integer rollNo,@RequestParam String name,@RequestParam String course,@RequestParam double marks,HttpSession s,RedirectAttributes ra){if(!signed(s)||role(s).equals("Student"))return "redirect:/login";try{if(name.isBlank()||course.isBlank()||marks<0||marks>100)throw new IllegalArgumentException("Enter a name, course, and marks between 0 and 100.");if(rollNo==null){Integer next=db.queryForObject("SELECT COALESCE(MAX(roll_no),100)+1 FROM sca_students",Integer.class);db.update("INSERT INTO sca_students(roll_no,name,course,marks) VALUES(?,?,?,?)",next,name.trim(),course.trim(),marks);}else db.update("UPDATE sca_students SET name=?,course=?,marks=? WHERE roll_no=?",name.trim(),course.trim(),marks,rollNo);audit(role(s)+" saved student: "+name);ra.addFlashAttribute("message","Student record saved.");}catch(Exception e){ra.addFlashAttribute("error","Could not save record. Check that the name is unique and the values are valid.");}return "redirect:/students";}
 @PostMapping("/students/{roll}/delete") public String delete(@PathVariable int roll,HttpSession s,RedirectAttributes ra){if(!signed(s)||!role(s).equals("Admin"))return "redirect:/students";db.update("DELETE FROM sca_students WHERE roll_no=?",roll);audit("Admin deleted student roll "+roll);ra.addFlashAttribute("message","Student deleted.");return "redirect:/students";}
 @GetMapping("/attendance") public String attendance(@RequestParam(required=false) String date,HttpSession s,Model m){if(!signed(s))return "redirect:/login";if(role(s).equals("Student"))return "redirect:/profile";LocalDate d;try{d=LocalDate.parse(date);}catch(Exception e){d=LocalDate.now();}base(s,m,"Attendance");m.addAttribute("date",d);m.addAttribute("rows",db.queryForList("SELECT s.roll_no,s.name,s.course,a.status FROM sca_students s LEFT JOIN sca_attendance a ON a.roll_no=s.roll_no AND a.attendance_date=? ORDER BY s.roll_no",d));m.addAttribute("present",db.queryForObject("SELECT COUNT(*) FROM sca_attendance WHERE attendance_date=? AND status='Present'",Integer.class,d));m.addAttribute("absent",db.queryForObject("SELECT COUNT(*) FROM sca_attendance WHERE attendance_date=? AND status='Absent'",Integer.class,d));return "attendance";}
 @PostMapping("/attendance/save") public String attendanceSave(@RequestParam int rollNo,@RequestParam String date,@RequestParam String status,HttpSession s,RedirectAttributes ra){if(!signed(s)||role(s).equals("Student"))return "redirect:/login";try{LocalDate d=LocalDate.parse(date);if(d.isAfter(LocalDate.now()))throw new IllegalArgumentException();if(status.equals("Not marked"))db.update("DELETE FROM sca_attendance WHERE roll_no=? AND attendance_date=?",rollNo,d);else if(status.equals("Present")||status.equals("Absent"))db.update("INSERT INTO sca_attendance(roll_no,attendance_date,status) VALUES(?,?,?) ON DUPLICATE KEY UPDATE status=VALUES(status)",rollNo,d,status);else throw new IllegalArgumentException();audit(role(s)+" updated attendance roll "+rollNo+" on "+d);}catch(Exception e){ra.addFlashAttribute("error","Could not update attendance. Future dates are not allowed.");}return "redirect:/attendance?date="+date;}
 @GetMapping("/profile") public String profile(HttpSession s,Model m){if(!signed(s))return "redirect:/login";if(!role(s).equals("Student"))return "redirect:/dashboard";base(s,m,"My profile");m.addAttribute("student",db.queryForMap("SELECT roll_no,name,course,marks FROM sca_students WHERE LOWER(name)=LOWER(?)",s.getAttribute("studentName")));m.addAttribute("attendance",db.queryForList("SELECT attendance_date,status FROM sca_attendance WHERE roll_no=(SELECT roll_no FROM sca_students WHERE LOWER(name)=LOWER(?)) ORDER BY attendance_date DESC",s.getAttribute("studentName")));m.addAttribute("percentage",db.queryForObject("SELECT COALESCE(ROUND(100.0*SUM(status='Present')/NULLIF(COUNT(*),0),1),0) FROM sca_attendance WHERE roll_no=(SELECT roll_no FROM sca_students WHERE LOWER(name)=LOWER(?))",Double.class,s.getAttribute("studentName")));return "profile";}
 private void base(HttpSession s,Model m,String page){m.addAttribute("role",role(s));m.addAttribute("displayName",s.getAttribute("username"));m.addAttribute("page",page);}
}
