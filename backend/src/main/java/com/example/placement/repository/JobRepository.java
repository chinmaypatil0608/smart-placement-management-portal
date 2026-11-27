package com.example.placement.repository;
import com.example.placement.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
public interface JobRepository extends JpaRepository<Job,Long>{}
