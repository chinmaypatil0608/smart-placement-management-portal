package com.example.placement.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="job_applications",uniqueConstraints=@UniqueConstraint(columnNames={"student_id","job_id"}))
public class JobApplication {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false) private Student student;
 @ManyToOne(optional=false) private Job job;
 private String status="APPLIED"; private LocalDateTime appliedAt=LocalDateTime.now();
 public JobApplication(){}
 public JobApplication(Student s,Job j){student=s;job=j;}
 public Long getId(){return id;} public Student getStudent(){return student;} public Job getJob(){return job;}
 public String getStatus(){return status;} public void setStatus(String v){status=v;}
 public LocalDateTime getAppliedAt(){return appliedAt;}
}
