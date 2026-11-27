package com.example.placement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "jobs")
public class Job {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank @Column(nullable = false) private String company;
    @NotBlank @Column(nullable = false) private String title;
    @NotBlank @Column(nullable = false, length = 1000) private String requiredSkills;
    @DecimalMin("0.0") @DecimalMax("10.0") private double minimumCgpa;
    private String location;

    public Job() {}
    public Long getId() { return id; }
    public String getCompany() { return company; }
    public void setCompany(String value) { company = value; }
    public String getTitle() { return title; }
    public void setTitle(String value) { title = value; }
    public String getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(String value) { requiredSkills = value; }
    public double getMinimumCgpa() { return minimumCgpa; }
    public void setMinimumCgpa(double value) { minimumCgpa = value; }
    public String getLocation() { return location; }
    public void setLocation(String value) { location = value; }
}
