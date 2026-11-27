package com.example.placement.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="user_accounts")
public class UserAccount {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true) private String email;
 @Column(nullable=false) private String passwordHash;
 @Column(nullable=false) private String role;
 @OneToOne @JoinColumn(name="student_id",unique=true) private Student student;
 private LocalDateTime createdAt=LocalDateTime.now();
 public UserAccount(){}
 public UserAccount(String e,String p,String r,Student s){email=e;passwordHash=p;role=r;student=s;}
 public Long getId(){return id;} public String getEmail(){return email;} public String getPasswordHash(){return passwordHash;} public String getRole(){return role;} public Student getStudent(){return student;}
}
