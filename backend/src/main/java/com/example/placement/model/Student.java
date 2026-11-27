package com.example.placement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "students")
public class Student {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Column(nullable = false)
    private String name;

    @Email @NotBlank @Column(nullable = false, unique = true)
    private String email;

    @NotBlank @Column(nullable = false)
    private String department;

    @DecimalMin("0.0") @DecimalMax("10.0") @Column(nullable = false)
    private double cgpa;

    public Student() {}
    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String value) { name = value; }
    public String getEmail() { return email; }
    public void setEmail(String value) { email = value; }
    public String getDepartment() { return department; }
    public void setDepartment(String value) { department = value; }
    public double getCgpa() { return cgpa; }
    public void setCgpa(double value) { cgpa = value; }
}
